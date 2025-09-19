package com.msgmates.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.msgmates.app.BuildConfig
import com.msgmates.app.ui.main.MainActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DEBUG + bypass: direkt ana ekrana atla (emülatörde hız)
        if (BuildConfig.DEBUG && BuildConfig.ENABLE_PASSWORD_BYPASS) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Normal akış; hatada AuthActivity'ye güvenli düş
        lifecycleScope.launch {
            try {
                when (gate(this@LauncherActivity)) {
                    is GateResult.NeedPhone -> {
                        val i = Intent(this@LauncherActivity, AuthActivity::class.java)
                        i.putExtra("fragment", "phone")
                        startActivity(i); finish()
                    }
                    is GateResult.NeedTos -> {
                        val i = Intent(this@LauncherActivity, AuthActivity::class.java)
                        i.putExtra("fragment", "tos")
                        startActivity(i); finish()
                    }
                    is GateResult.Ready -> {
                        startActivity(Intent(this@LauncherActivity, MainActivity::class.java))
                        finish()
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Toast.makeText(this@LauncherActivity, "Açılışta hata: giriş ekranına alınıyor…", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LauncherActivity, AuthActivity::class.java))
                finish()
            }
        }
    }
}
