package com.msgmates.app.core.auth

import com.msgmates.app.core.auth.remote.AuthApiService
import com.msgmates.app.core.auth.remote.RequestCodeRequest
import com.msgmates.app.core.auth.remote.VerifyCodeRequest
import com.msgmates.app.util.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Auth Repository - telefon doğrulama işlemleri
 */
@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenRepository: TokenRepository
) {
    
    /**
     * Doğrulama kodu iste
     * @param phone Telefon numarası (05XXXXXXXXX formatında)
     * @return Result<Unit>
     */
    suspend fun requestCode(phone: String): Result<Unit> {
        return try {
            // Debug modda test kullanıcısı kontrolü
            if (DebugTestUsers.isTestUser(phone)) {
                // Test kullanıcısı - mock başarı döndür
                Result.Success(Unit)
            } else {
                // Gerçek API çağrısı
                val request = RequestCodeRequest(phone)
                val response = authApiService.requestCode(request)
                
                if (response.success) {
                    Result.Success(Unit)
                } else {
                    Result.Error(Exception(response.message ?: "Kod gönderilemedi"))
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Doğrulama kodunu doğrula
     * @param phone Telefon numarası
     * @param otp OTP kodu
     * @return Result<AuthTokens>
     */
    suspend fun verifyCode(phone: String, otp: String): Result<AuthTokens> {
        return try {
            // Debug modda test kullanıcısı kontrolü
            if (DebugTestUsers.isTestUser(phone)) {
                if (DebugTestUsers.verifyTestOtp(phone, otp)) {
                    // Test kullanıcısı ve OTP doğru - mock token oluştur
                    val now = System.currentTimeMillis() / 1000
                    val mockTokens = AuthTokens(
                        access = AccessToken("test_access_token_$phone", now + 3600),
                        refresh = RefreshToken("test_refresh_token_$phone", now + 86400)
                    )
                    Result.Success(mockTokens)
                } else {
                    Result.Error(Exception("Doğrulama kodu hatalı"))
                }
            } else {
                // Gerçek API çağrısı
                val request = VerifyCodeRequest(phone, otp)
                val response = authApiService.verifyCode(request)
                
                if (response.success && response.tokens != null) {
                    val tokens = AuthTokens(
                        access = AccessToken(
                            response.tokens.accessToken,
                            response.tokens.accessTokenExpiry
                        ),
                        refresh = RefreshToken(
                            response.tokens.refreshToken,
                            response.tokens.refreshTokenExpiry
                        )
                    )
                    Result.Success(tokens)
                } else {
                    Result.Error(Exception(response.message ?: "Doğrulama başarısız"))
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
