package com.msgmates.app.core.messaging

import com.msgmates.app.core.db.dao.AttachmentDao
import com.msgmates.app.core.db.dao.MessageDao
import com.msgmates.app.core.db.dao.OutboxDao
import com.msgmates.app.core.db.entity.AttachmentEntity
import com.msgmates.app.core.db.entity.MessageEntity
import com.msgmates.app.core.db.entity.OutboxEntity
import com.msgmates.app.core.messaging.model.LocalAttachment
import com.msgmates.app.core.messaging.model.UiMessage
import com.msgmates.app.core.messaging.remote.MessagingApi
import com.msgmates.app.core.messaging.remote.dto.AttachmentStub
import com.msgmates.app.core.messaging.remote.dto.MessageDTO
import com.msgmates.app.core.messaging.remote.dto.ReceiptDTO
import com.msgmates.app.core.messaging.remote.dto.SendMessageRequest
import com.msgmates.app.core.messaging.remote.dto.SyncRequest
import com.msgmates.app.core.notification.ChatNotification
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val attachmentDao: AttachmentDao,
    private val outboxDao: OutboxDao,
    private val messagingApi: MessagingApi,
    private val chatNotification: ChatNotification,
    private val floodProtection: FloodProtection
) {

    fun stream(convoId: String): Flow<List<UiMessage>> {
        return messageDao.streamByConversation(convoId).map { messagesWithAttachments ->
            messagesWithAttachments.map { (message, attachments) ->
                UiMessage.fromEntity(message, attachments)
            }
        }
    }

    suspend fun sendText(convoId: String, text: String): Result<Unit> {
        return try {
            // Check flood protection
            if (!floodProtection.checkRateLimit(convoId)) {
                return Result.failure(Exception("Çok hızlı mesaj gönderiyorsunuz, lütfen bekleyin"))
            }

            val clientMsgId = UUID.randomUUID().toString()
            val message = MessageEntity(
                id = clientMsgId,
                convoId = convoId,
                senderId = "current_user", // TODO: Get from auth
                body = text,
                msgType = "text",
                sentAt = null,
                localCreatedAt = System.currentTimeMillis(),
                status = "queued",
                replyToId = null
            )

            messageDao.insert(message)
            outboxDao.enqueue(OutboxEntity(msgId = clientMsgId))

            // Update status to sending when queued
            messageDao.updateStatus(clientMsgId, "sending", null)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendWithAttachments(
        convoId: String,
        text: String?,
        localAttachments: List<LocalAttachment>
    ): Result<Unit> {
        return try {
            val clientMsgId = UUID.randomUUID().toString()
            val message = MessageEntity(
                id = clientMsgId,
                convoId = convoId,
                senderId = "current_user", // TODO: Get from auth
                body = text,
                msgType = if (localAttachments.isNotEmpty()) localAttachments.first().kind else "text",
                sentAt = null,
                localCreatedAt = System.currentTimeMillis(),
                status = "queued",
                replyToId = null
            )

            messageDao.insert(message)

            // Save attachments locally
            localAttachments.forEach { localAttachment ->
                val attachmentId = UUID.randomUUID().toString()
                val attachment = AttachmentEntity(
                    id = attachmentId,
                    msgId = clientMsgId,
                    kind = localAttachment.kind,
                    mime = localAttachment.mime,
                    size = localAttachment.size,
                    width = localAttachment.width,
                    height = localAttachment.height,
                    durationMs = localAttachment.durationMs,
                    remoteUrl = null,
                    localUri = localAttachment.localUri.toString(),
                    thumbB64 = localAttachment.thumbB64
                )
                attachmentDao.insert(attachment)
            }

            outboxDao.enqueue(OutboxEntity(msgId = clientMsgId))

            // Update status to sending when queued
            messageDao.updateStatus(clientMsgId, "sending", null)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ackDelivered(ids: List<String>) {
        try {
            val receipt = ReceiptDTO(
                type = "delivered",
                messageIds = ids,
                at = System.currentTimeMillis()
            )
            messagingApi.receipts(receipt)
            messageDao.markDelivered(ids)
        } catch (e: Exception) {
            // Log error but don't fail
            android.util.Log.e("MessageRepository", "Failed to ack delivered", e)
        }
    }

    suspend fun ackRead(ids: List<String>) {
        try {
            val receipt = ReceiptDTO(
                type = "read",
                messageIds = ids,
                at = System.currentTimeMillis()
            )
            messagingApi.receipts(receipt)
            messageDao.markRead(ids)
        } catch (e: Exception) {
            // Log error but don't fail
            android.util.Log.e("MessageRepository", "Failed to ack read", e)
        }
    }

    suspend fun sync(since: Long, convoId: String?) {
        try {
            // Add clock skew tolerance (2-5 seconds)
            val clockSkewTolerance = 3000L // 3 seconds
            val adjustedSince = maxOf(0, since - clockSkewTolerance)

            val syncRequest = SyncRequest(since = adjustedSince, conversationId = convoId)
            val syncResponse = messagingApi.sync(syncRequest)

            // Upsert messages
            syncResponse.messages.forEach { messageDTO ->
                upsertMessage(messageDTO)
            }

            // Update receipts
            syncResponse.receipts.forEach { receiptDTO ->
                when (receiptDTO.type) {
                    "delivered" -> messageDao.markDelivered(receiptDTO.messageIds)
                    "read" -> messageDao.markRead(receiptDTO.messageIds)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MessageRepository", "Sync failed", e)
        }
    }

    private suspend fun upsertMessage(messageDTO: MessageDTO) {
        val message = MessageEntity(
            id = messageDTO.id,
            convoId = messageDTO.conversationId,
            senderId = messageDTO.senderId,
            body = messageDTO.body,
            msgType = messageDTO.type,
            sentAt = messageDTO.sentAt,
            localCreatedAt = messageDTO.sentAt, // Use sentAt as localCreatedAt for remote messages
            status = "sent",
            replyToId = null, // TODO: Add reply support
            edited = messageDTO.edited,
            deleted = messageDTO.deleted
        )

        messageDao.insert(message)

        // Save attachments
        messageDTO.attachments?.forEach { attachmentDTO ->
            val attachment = AttachmentEntity(
                id = attachmentDTO.id,
                msgId = messageDTO.id,
                kind = attachmentDTO.kind,
                mime = attachmentDTO.mime,
                size = attachmentDTO.size,
                width = attachmentDTO.width,
                height = attachmentDTO.height,
                durationMs = attachmentDTO.durationMs,
                remoteUrl = attachmentDTO.remoteUrl,
                localUri = null,
                thumbB64 = null
            )
            attachmentDao.insert(attachment)
        }
    }

    suspend fun processOutboxItem(msgId: String): Result<Unit> {
        return try {
            val message = messageDao.getById(msgId) ?: return Result.failure(Exception("Message not found"))
            val attachments = attachmentDao.forMessage(msgId)

            val attachmentStubs = attachments.map { attachment ->
                AttachmentStub(
                    kind = attachment.kind,
                    mime = attachment.mime,
                    size = attachment.size,
                    remoteId = attachment.remoteUrl?.let { extractIdFromUrl(it) },
                    fileName = attachment.localUri?.let { extractFileNameFromUri(it) }
                )
            }

            val sendRequest = SendMessageRequest(
                conversationId = message.convoId,
                clientMsgId = message.id,
                type = message.msgType,
                body = message.body,
                attachments = attachmentStubs.takeIf { it.isNotEmpty() }
            )

            val response = messagingApi.send(sendRequest)

            // Update message status
            messageDao.updateStatus(
                id = message.id,
                status = "sent",
                sentAt = response.sentAt
            )

            // Remove from outbox
            outboxDao.delete(msgId)

            Result.success(Unit)
        } catch (e: Exception) {
            // Update message status to failed
            messageDao.updateStatus(msgId, "failed")
            Result.failure(e)
        }
    }

    private fun extractIdFromUrl(url: String): String? {
        // Extract ID from URL - implement based on your URL structure
        return url.substringAfterLast("/")
    }

    private fun extractFileNameFromUri(uri: String): String? {
        // Extract filename from URI - implement based on your URI structure
        return uri.substringAfterLast("/")
    }

    suspend fun upsertMessageFromWs(messageDTO: MessageDTO) {
        upsertMessage(messageDTO)
    }

    suspend fun handleReceipt(receiptDTO: ReceiptDTO) {
        when (receiptDTO.type) {
            "delivered" -> messageDao.markDelivered(receiptDTO.messageIds)
            "read" -> messageDao.markRead(receiptDTO.messageIds)
        }
    }

    fun showMessageNotification(
        conversationId: String,
        senderName: String,
        messageText: String,
        messageId: String,
        isGroup: Boolean = false,
        groupName: String? = null
    ) {
        if (isGroup && groupName != null) {
            chatNotification.showGroupMessageNotification(
                conversationId = conversationId,
                groupName = groupName,
                senderName = senderName,
                messageText = messageText,
                messageId = messageId
            )
        } else {
            chatNotification.showMessageNotification(
                conversationId = conversationId,
                senderName = senderName,
                messageText = messageText,
                messageId = messageId
            )
        }
    }
}
