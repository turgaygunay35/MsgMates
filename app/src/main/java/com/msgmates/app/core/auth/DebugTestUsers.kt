package com.msgmates.app.core.auth

import com.msgmates.app.BuildConfig

/**
 * Debug test kullanıcıları - sadece debug build'de aktif
 * Release build'de bu sınıf derlenir ama çalışmaz
 */
object DebugTestUsers {
    
    // Test numaraları → OTP kodları mapping
    private val testUsers = mapOf(
        "05320001980" to "111111",
        "05320001981" to "222222", 
        "05320001982" to "333333",
        "05320001983" to "444444",
        "05320001984" to "555555",
        "05320001985" to "666666",
        "05320001986" to "777777",
        "05320001987" to "888888",
        "05320001988" to "999999",
        "05320001989" to "000000"
    )
    
    /**
     * Debug modda test kullanıcısı kontrolü
     * @param phone Telefon numarası
     * @return Test kullanıcısıysa true, değilse false
     */
    fun isTestUser(phone: String): Boolean {
        return BuildConfig.DEBUG && testUsers.containsKey(phone)
    }
    
    /**
     * Test kullanıcısı için OTP kodu al
     * @param phone Telefon numarası
     * @return Test kullanıcısıysa OTP kodu, değilse null
     */
    fun getTestOtp(phone: String): String? {
        return if (BuildConfig.DEBUG) testUsers[phone] else null
    }
    
    /**
     * Test kullanıcısı OTP doğrulama
     * @param phone Telefon numarası
     * @param otp Girilen OTP kodu
     * @return Test kullanıcısıysa ve OTP doğruysa true
     */
    fun verifyTestOtp(phone: String, otp: String): Boolean {
        return BuildConfig.DEBUG && testUsers[phone] == otp
    }
}