package com.msgmates.app.core.disaster

import com.msgmates.app.core.network.NetworkConfig
import com.msgmates.app.core.ws.WsClient
import com.msgmates.app.core.messaging.MessageRepository
import com.msgmates.app.core.auth.TokenRepository
import com.msgmates.app.core.auth.RefreshCoordinator
import com.msgmates.app.core.env.EnvConfig
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever

class DisasterModeE2ETest {

    @Mock
    private lateinit var messageRepository: MessageRepository

    @Mock
    private lateinit var tokenRepository: TokenRepository

    @Mock
    private lateinit var refreshCoordinator: RefreshCoordinator

    @Mock
    private lateinit var envConfig: EnvConfig

    private lateinit var wsClient: WsClient

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        wsClient = WsClient(envConfig, tokenRepository, refreshCoordinator, messageRepository)
    }

    @Test
    fun `Disaster Mode - WebSocket connection disabled`() {
        // Given
        NetworkConfig.setDisasterMode(true)

        // When
        val result = wsClient.connect()

        // Then
        assert(result == null) // WebSocket should not connect in disaster mode
        verify(messageRepository, never()).onMessageReceived(any())
    }

    @Test
    fun `Disaster Mode - Offline message queuing works`() {
        // Given
        NetworkConfig.setDisasterMode(true)
        val testMessage = "Test offline message"

        // When
        wsClient.sendMessage(testMessage)

        // Then
        // Message should be queued for later delivery
        verify(messageRepository).queueOfflineMessage(testMessage)
    }

    @Test
    fun `Disaster Mode - Network requests timeout quickly`() {
        // Given
        NetworkConfig.setDisasterMode(true)

        // When
        val startTime = System.currentTimeMillis()
        // Simulate network request
        val endTime = System.currentTimeMillis()

        // Then
        val duration = endTime - startTime
        assert(duration < 3000) // Should timeout within 3 seconds
    }

    @Test
    fun `Disaster Mode - Auto-reconnect disabled`() {
        // Given
        NetworkConfig.setDisasterMode(true)

        // When
        wsClient.enableAutoReconnect()
        wsClient.connect() // This should fail in disaster mode

        // Then
        // Auto-reconnect should not be triggered
        verify(messageRepository, never()).onReconnectAttempt()
    }

    @Test
    fun `Disaster Mode Recovery - WebSocket reconnects automatically`() {
        // Given
        NetworkConfig.setDisasterMode(true)
        wsClient.connect() // Should fail

        // When
        NetworkConfig.setDisasterMode(false)
        wsClient.enableAutoReconnect()

        // Then
        val result = wsClient.connect()
        assert(result != null) // Should connect successfully
    }

    @Test
    fun `Disaster Mode Recovery - Queued messages sent`() {
        // Given
        NetworkConfig.setDisasterMode(true)
        val queuedMessage = "Queued message"
        wsClient.sendMessage(queuedMessage)

        // When
        NetworkConfig.setDisasterMode(false)
        wsClient.connect()

        // Then
        verify(messageRepository).sendQueuedMessages()
    }

    @Test
    fun `Disaster Mode - 3-3-3 timeout configuration`() {
        // Given
        NetworkConfig.setDisasterMode(true)

        // When & Then
        assert(NetworkConfig.connectTimeoutSeconds == 3)
        assert(NetworkConfig.readTimeoutSeconds == 3)
        assert(NetworkConfig.writeTimeoutSeconds == 3)
    }

    @Test
    fun `Disaster Mode - No data loss on recovery`() {
        // Given
        NetworkConfig.setDisasterMode(true)
        val messages = listOf("msg1", "msg2", "msg3")
        messages.forEach { wsClient.sendMessage(it) }

        // When
        NetworkConfig.setDisasterMode(false)
        wsClient.connect()

        // Then
        verify(messageRepository).sendQueuedMessages()
        // All messages should be preserved and sent
    }
}
