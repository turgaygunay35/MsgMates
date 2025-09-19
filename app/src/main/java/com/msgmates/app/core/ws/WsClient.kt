package com.msgmates.app.core.ws

import com.msgmates.app.core.auth.RefreshCoordinator
import com.msgmates.app.core.auth.TokenRepository
import com.msgmates.app.core.env.EnvConfig
import com.msgmates.app.core.messaging.MessageRepository
import com.msgmates.app.core.messaging.ws.WsEvent
import com.msgmates.app.core.messaging.ws.events.MessageCreated
import com.msgmates.app.core.messaging.ws.events.MessageUpdated
import com.msgmates.app.core.messaging.ws.events.Receipt
import com.msgmates.app.core.messaging.ws.events.Typing
import com.msgmates.app.core.network.NetworkConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

@Singleton
class WsClient @Inject constructor(
    private val env: EnvConfig,
    private val tokenRepository: TokenRepository,
    private val refreshCoordinator: RefreshCoordinator,
    private val messageRepository: MessageRepository
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val backoff = WsBackoff()

    private val _typingEvents = MutableSharedFlow<Typing>()
    val typingEvents: SharedFlow<Typing> = _typingEvents

    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var autoReconnect = true
    private var isConnecting = false

    fun connect(): WebSocket? {
        // Don't connect in disaster mode
        if (NetworkConfig.isDisasterMode()) {
            android.util.Log.d("WsClient", "Disaster mode active, skipping WebSocket connection")
            return null
        }

        // Prevent multiple simultaneous connection attempts
        if (isConnecting) {
            android.util.Log.d("WsClient", "Connection already in progress, skipping")
            return webSocket
        }

        isConnecting = true

        val token = runCatching { tokenRepository.getTokensSync().access }.getOrNull()
        val reqBuilder = Request.Builder().url(env.wsUrl)

        if (!token.isNullOrBlank()) {
            reqBuilder.header("Authorization", "Bearer $token")
        }

        val ws = OkHttpClient().newWebSocket(
            reqBuilder.build(),
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                    android.util.Log.d("WsClient", "WebSocket connected")
                    isConnected = true
                    isConnecting = false
                    this@WsClient.webSocket = webSocket
                    backoff.reset() // Reset backoff on successful connection
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    android.util.Log.d("WsClient", "Message received: $text")
                    scope.launch {
                        handleMessage(text)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    android.util.Log.d("WsClient", "WebSocket closing: $code $reason")
                    isConnected = false
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    android.util.Log.d("WsClient", "WebSocket closed: $code $reason")
                    isConnected = false
                    isConnecting = false
                    this@WsClient.webSocket = null

                    // Auto-reconnect if not intentionally closed and auto-reconnect is enabled
                    if (code != 1000 && autoReconnect && !NetworkConfig.isDisasterMode()) {
                        scheduleReconnect()
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                    android.util.Log.e("WsClient", "WebSocket failed", t)
                    isConnected = false
                    isConnecting = false
                    this@WsClient.webSocket = null

                    // Auto-reconnect on failure if enabled and not in disaster mode
                    if (autoReconnect && !NetworkConfig.isDisasterMode()) {
                        scheduleReconnect()
                    }
                }
            }
        )

        return ws
    }

    internal suspend fun handleMessage(text: String) {
        try {
            val event = json.decodeFromString<WsEvent>(text)

            when (event.type) {
                "message_created" -> {
                    val messageCreated = json.decodeFromJsonElement(MessageCreated.serializer(), event.payload)
                    messageRepository.upsertMessageFromWs(messageCreated.message)
                }
                "message_updated" -> {
                    val messageUpdated = json.decodeFromJsonElement(MessageUpdated.serializer(), event.payload)
                    messageRepository.upsertMessageFromWs(messageUpdated.message)
                }
                "receipt" -> {
                    val receipt = json.decodeFromJsonElement(Receipt.serializer(), event.payload)
                    messageRepository.handleReceipt(receipt.receipt)
                }
                "typing" -> {
                    val typing = json.decodeFromJsonElement(Typing.serializer(), event.payload)
                    _typingEvents.emit(typing)
                }
                else -> {
                    android.util.Log.w("WsClient", "Unknown event type: ${event.type}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WsClient", "Failed to parse WebSocket message", e)
        }
    }

    suspend fun connectWithRetry(): WebSocket? {
        var delayMs = 1000L
        repeat(5) { attempt ->
            try {
                // Access token refresh check
                val tokens = tokenRepository.getTokensSync()
                if (tokens.access != null) {
                    try {
                        refreshCoordinator.blockingRefresh()
                    } catch (e: Exception) {
                        // Ignore refresh errors
                    }
                }

                val ws = connect()
                android.util.Log.d("WsClient", "WebSocket connected on attempt ${attempt + 1}")
                return ws
            } catch (e: Exception) {
                android.util.Log.w("WsClient", "Connection attempt ${attempt + 1} failed", e)
                if (attempt < 4) { // Son deneme değilse bekle
                    delay(delayMs)
                    delayMs = (delayMs * 2).coerceAtMost(30_000) // Max 30s
                }
            }
        }
        android.util.Log.e("WsClient", "All connection attempts failed")
        return null
    }

    fun disconnect() {
        autoReconnect = false
        webSocket?.close(1000, "Logout")
        webSocket = null
        isConnected = false
        isConnecting = false
        backoff.reset()
        android.util.Log.d("WsClient", "WebSocket disconnected")
    }

    fun enableAutoReconnect() {
        autoReconnect = true
    }

    fun disableAutoReconnect() {
        autoReconnect = false
    }

    internal fun scheduleReconnect() {
        scope.launch {
            if (backoff.hasReachedMaxAttempts()) {
                android.util.Log.w("WsClient", "Max reconnection attempts reached, giving up")
                return@launch
            }

            val delay = backoff.nextDelay()
            android.util.Log.d("WsClient", "Scheduling reconnect in ${delay}ms (attempt ${backoff.getAttemptCount()})")
            connect()
        }
    }
}
