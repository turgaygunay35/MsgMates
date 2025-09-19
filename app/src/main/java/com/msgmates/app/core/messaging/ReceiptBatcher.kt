package com.msgmates.app.core.messaging

import android.util.Log
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class ReceiptBatcher @Inject constructor(
    private val messageRepository: MessageRepository
) {

    private val deliveredQueue = ConcurrentLinkedQueue<String>()
    private val readQueue = ConcurrentLinkedQueue<String>()
    private val mutex = Mutex()

    private val batchDelay = 2000L // 2 seconds
    private val maxBatchSize = 50

    fun addDelivered(messageId: String) {
        deliveredQueue.offer(messageId)
        scheduleBatch()
    }

    fun addRead(messageId: String) {
        readQueue.offer(messageId)
        scheduleBatch()
    }

    private fun scheduleBatch() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(batchDelay)
            processBatches()
        }
    }

    private suspend fun processBatches() {
        mutex.withLock {
            // Process delivered receipts
            if (deliveredQueue.isNotEmpty()) {
                val deliveredIds = mutableListOf<String>()
                repeat(maxBatchSize) {
                    deliveredQueue.poll()?.let { deliveredIds.add(it) }
                }

                if (deliveredIds.isNotEmpty()) {
                    try {
                        messageRepository.ackDelivered(deliveredIds)
                        Log.d("ReceiptBatcher", "Sent ${deliveredIds.size} delivered receipts")
                    } catch (e: Exception) {
                        Log.e("ReceiptBatcher", "Failed to send delivered receipts", e)
                        // Re-queue failed receipts
                        deliveredIds.forEach { deliveredQueue.offer(it) }
                    }
                }
            }

            // Process read receipts
            if (readQueue.isNotEmpty()) {
                val readIds = mutableListOf<String>()
                repeat(maxBatchSize) {
                    readQueue.poll()?.let { readIds.add(it) }
                }

                if (readIds.isNotEmpty()) {
                    try {
                        messageRepository.ackRead(readIds)
                        Log.d("ReceiptBatcher", "Sent ${readIds.size} read receipts")
                    } catch (e: Exception) {
                        Log.e("ReceiptBatcher", "Failed to send read receipts", e)
                        // Re-queue failed receipts
                        readIds.forEach { readQueue.offer(it) }
                    }
                }
            }
        }
    }

    fun flush() {
        CoroutineScope(Dispatchers.IO).launch {
            processBatches()
        }
    }
}
