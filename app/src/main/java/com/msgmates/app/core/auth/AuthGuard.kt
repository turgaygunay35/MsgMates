package com.msgmates.app.core.auth

import android.content.Context
import android.content.Intent
import com.msgmates.app.ui.auth.AuthActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthGuard @Inject constructor(
    private val tokenRepository: TokenRepository
) {

    /**
     * Kullanıcının geçerli bir oturumu var mı kontrol et
     * Sadece access token varlığını kontrol eder
     */
    fun isAuthenticated(): Boolean {
        val tokens = tokenRepository.getTokensSync()
        val hasAccessToken = tokens.access?.isNotBlank() == true
        android.util.Log.d("AuthGuard", "Tokens: access=${tokens.access?.take(20)}..., hasAccess=$hasAccessToken")
        return hasAccessToken
    }

    /**
     * Oturum yoksa AuthActivity'ye yönlendir
     * Tüm backstack'i temizler
     */
    fun redirectToAuth(context: Context) {
        val intent = Intent(context, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    /**
     * Oturum kontrolü yap ve gerekirse yönlendir
     * Main akışına erişim için zorunlu
     */
    fun checkAuthAndRedirect(context: Context): Boolean {
        return if (isAuthenticated()) {
            true // Oturum var, devam et
        } else {
            redirectToAuth(context)
            false // Oturum yok, yönlendirildi
        }
    }

    /**
     * Fragment seviyesinde auth kontrolü
     * Main akışındaki her fragment için gerekli
     * Deep link desteği ile
     */
    fun requireAuth(context: Context): Boolean {
        if (!isAuthenticated()) {
            redirectToAuth(context)
            return false
        }
        return true
    }

    /**
     * Deep link ile gelen istekleri kontrol et
     * Önce login tamamlanır, sonra hedefe yönlendirilir
     */
    fun requireAuthWithDeepLink(context: Context, deepLink: String? = null): Boolean {
        if (!isAuthenticated()) {
            // Deep link'i kaydet, login sonrası kullanılacak
            if (deepLink != null) {
                saveDeepLinkForLater(deepLink)
            }
            redirectToAuth(context)
            return false
        }
        return true
    }

    /**
     * Deep link'i geçici olarak kaydet
     */
    private fun saveDeepLinkForLater(deepLink: String) {
        // TODO: SharedPreferences veya SecureStore'a kaydet
        // Login sonrası bu link'e yönlendir
    }

    /**
     * Kaydedilen deep link'i al ve temizle
     */
    fun getAndClearSavedDeepLink(): String? {
        // TODO: Kaydedilen deep link'i döndür ve temizle
        return null
    }
}
