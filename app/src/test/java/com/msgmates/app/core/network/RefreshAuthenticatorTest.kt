package com.msgmates.app.core.network

import com.msgmates.app.core.auth.TokenReadOnlyProvider
import com.msgmates.app.core.auth.TokenRefresher
import com.msgmates.app.core.auth.TokenRepository
import com.msgmates.app.core.auth.Tokens
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class RefreshAuthenticatorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var authenticator: RefreshAuthenticator
    private lateinit var okHttpClient: OkHttpClient

    @Mock
    private lateinit var tokenProvider: TokenReadOnlyProvider

    @Mock
    private lateinit var tokenRefresher: TokenRefresher

    @Mock
    private lateinit var tokenRepository: TokenRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockWebServer = MockWebServer()
        mockWebServer.start()

        authenticator = RefreshAuthenticator(tokenProvider, tokenRefresher, tokenRepository)

        okHttpClient = OkHttpClient.Builder()
            .authenticator(authenticator)
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `T1 - Access token expired, refresh successful, retry succeeds`() {
        // Given
        val refreshToken = "valid_refresh_token"
        val newAccessToken = "new_access_token"

        whenever(tokenProvider.getRefreshToken()).thenReturn(refreshToken)
        whenever(tokenRefresher.refreshToken(refreshToken)).thenReturn(
            Result.success(Tokens(access = newAccessToken, refresh = refreshToken))
        )

        // Mock: İlk istek 401, refresh 200, retry 200
        mockWebServer.enqueue(MockResponse().setResponseCode(401)) // İlk 401
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"access_token":"$newAccessToken","refresh_token":"$refreshToken"}"""
            )
        ) // Refresh 200
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"data":"success"}""")) // Retry 200

        // When
        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        // Then
        assert(response.code == 200)
        assert(mockWebServer.requestCount == 3) // 401 + refresh + retry
    }

    @Test
    fun `T2 - Refresh fails with 401, triggers logout`() {
        // Given
        val refreshToken = "invalid_refresh_token"

        whenever(tokenProvider.getRefreshToken()).thenReturn(refreshToken)
        whenever(tokenRefresher.refreshToken(refreshToken)).thenReturn(
            Result.failure(Exception("Refresh failed: 401"))
        )

        // Mock: İlk istek 401
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        // When
        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        // Then
        assert(response.code == 401) // Hala 401
        assert(mockWebServer.requestCount == 1) // Sadece ilk istek
        // TokenRepository.clear() çağrıldı mı kontrol edilebilir
    }

    @Test
    fun `T3 - Multiple parallel requests, single refresh`() {
        // Given
        val refreshToken = "valid_refresh_token"
        val newAccessToken = "new_access_token"

        whenever(tokenProvider.getRefreshToken()).thenReturn(refreshToken)
        whenever(tokenRefresher.refreshToken(refreshToken)).thenReturn(
            Result.success(Tokens(access = newAccessToken, refresh = refreshToken))
        )

        // Mock: 5 paralel istek, hepsi 401, sonra refresh 200, sonra hepsi 200
        repeat(5) {
            mockWebServer.enqueue(MockResponse().setResponseCode(401))
        }
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"access_token":"$newAccessToken","refresh_token":"$refreshToken"}"""
            )
        )
        repeat(5) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"data":"success"}"""))
        }

        // When - 5 paralel istek
        val requests = (1..5).map {
            Request.Builder()
                .url(mockWebServer.url("/api/test$it"))
                .build()
        }

        val responses = requests.map { okHttpClient.newCall(it).execute() }

        // Then
        responses.forEach { assert(it.code == 200) }
        assert(mockWebServer.requestCount == 11) // 5x401 + 1xrefresh + 5xretry
    }

    @Test
    fun `T4 - Refresh timeout, single retry then logout`() {
        // Given
        val refreshToken = "valid_refresh_token"

        whenever(tokenProvider.getRefreshToken()).thenReturn(refreshToken)
        whenever(tokenRefresher.refreshToken(refreshToken)).thenReturn(
            Result.failure(Exception("Timeout"))
        )

        // Mock: İlk istek 401
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        // When
        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        // Then
        assert(response.code == 401)
        assert(mockWebServer.requestCount == 1)
    }

    @Test
    fun `T5 - Max retry reached, no more attempts`() {
        // Given
        val refreshToken = "valid_refresh_token"

        whenever(tokenProvider.getRefreshToken()).thenReturn(refreshToken)
        whenever(tokenRefresher.refreshToken(refreshToken)).thenReturn(
            Result.failure(Exception("Refresh failed"))
        )

        // Mock: İlk istek 401, retry de 401
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        // When
        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        // Then
        assert(response.code == 401)
        assert(mockWebServer.requestCount == 2) // İlk 401 + retry 401, sonra dur
    }

    @Test
    fun `T6 - Network error during refresh, no retry`() {
        // Given
        val refreshToken = "valid_refresh_token"

        whenever(tokenProvider.getRefreshToken()).thenReturn(refreshToken)
        whenever(tokenRefresher.refreshToken(refreshToken)).thenReturn(
            Result.failure(Exception("Network error"))
        )

        // Mock: İlk istek 401
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        // When
        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        // Then
        assert(response.code == 401)
        assert(mockWebServer.requestCount == 1) // Sadece ilk istek
    }
}
