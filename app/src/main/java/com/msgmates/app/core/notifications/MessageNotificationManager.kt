package com.msgmates.app.core.notifications

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Manages message notifications and unread count tracking.
 * Provides methods to mark conversations as read and track unread message counts.
 */
@Singleton
class MessageNotificationManager @Inject constructor() {

    // Sahte okunmamış sayı (5'ten başlayıp azalacak)
    private val _unreadCount = MutableStateFlow(5)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    /**
     * Sohbeti okundu olarak işaretle
     * Gerçek uygulamada burada bildirim yönetimi yapılır
     */
    suspend fun markConversationRead(conversationId: String) {
        withContext(Dispatchers.IO) {
            // Sahte delay (gerçek API çağrısı simülasyonu)
            kotlinx.coroutines.delay(500)

            // Okunmamış sayısını azalt (minimum 0)
            val currentCount = _unreadCount.value
            if (currentCount > 0) {
                _unreadCount.value = currentCount - 1
            }

            // Log for debugging
            android.util.Log.d(
                "MessageNotificationManager", "Marked conversation $conversationId as read. Unread count: ${_unreadCount.value}"
            )
        }
    }

    /**
     * Okunmamış sayısını sıfırla (test için)
     */
    fun resetUnreadCount() {
        _unreadCount.value = 5
    }

    /**
     * Belirli bir sohbetin okunmamış mesaj sayısını getir
     * Gerçek uygulamada bu veritabanından gelecek
     */
    suspend fun getUnreadCountForConversation(conversationId: String): Int {
        return withContext(Dispatchers.IO) {
            // Sahte delay
            kotlinx.coroutines.delay(100)

            // Şimdilik genel okunmamış sayıyı döndür
            _unreadCount.value
        }
    }

    /**
     * Tüm okunmamış mesajları işaretle
     */
    suspend fun markAllAsRead() {
        kotlinx.coroutines.delay(300)
        _unreadCount.value = 0
        android.util.Log.d("MessageNotificationManager", "Marked all conversations as read")
    }
}
