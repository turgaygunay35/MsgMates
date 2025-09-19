package com.msgmates.app.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.msgmates.app.R
import com.msgmates.app.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_auth) as NavHostFragment
        val navController = navHostFragment.navController

        // Auth navigation graph'ını ayarla
        navController.setGraph(R.navigation.nav_auth)
    }

    override fun onBackPressed() {
        // Auth ekranında geri tuşu uygulamadan çıkış yapsın
        // Main'e götürmez - sadece uygulamadan çıkış
        finishAffinity()
    }
}
