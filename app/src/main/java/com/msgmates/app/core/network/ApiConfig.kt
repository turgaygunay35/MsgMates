package com.msgmates.app.core.network

object ApiConfig {
    // Nginx /api ile route ediyorsa "api/" ver, yoksa "" (boş).
    // İlk deneme: "" (boş). Gerekirse sadece BuildConfig'te "api/" yapıp yeniden derle.
    const val API_PATH_PREFIX: String = "" // BuildConfig.API_PATH_PREFIX // "" veya "api/"
}
