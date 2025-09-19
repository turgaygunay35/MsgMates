package com.msgmates.app.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.msgmates.app.R
import com.msgmates.app.core.auth.AuthGuard
import com.msgmates.app.core.auth.SessionStarter
import com.msgmates.app.core.auth.TokenRepository
import com.msgmates.app.ui.auth.AuthActivity
import com.msgmates.app.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenRepository: TokenRepository

    @Inject
    lateinit var sessionStarter: SessionStarter

    @Inject
    lateinit var authGuard: AuthGuard

    private val splashDelay = 2000L // 2 saniye

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Splash ekranını göster
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthStatus()
        }, splashDelay)
    }

    private fun checkAuthStatus() {
        lifecycleScope.launch {
            // AuthGuard ile güvenli kontrol
            val isAuthenticated = authGuard.isAuthenticated()

            if (isAuthenticated) {
                // Token var, sessiz yenileme dene
                val refreshSuccess = sessionStarter.ensureFreshSession()
                if (refreshSuccess) {
                    // Başarılı, MainGraph'e git
                    navigateToMain()
                } else {
                    // Refresh başarısız, AuthGraph'e git
                    navigateToAuth()
                }
            } else {
                // Token yok, AuthGraph'e git
                navigateToAuth()
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Splash ekranında geri tuşu çalışmasın
        // Uygulamadan çıkış yap
        finishAffinity()
    }
}
