package com.msgmates.app.data.secure

interface SecureTokenStore {
    suspend fun getTokenData(): TokenData?
    suspend fun saveTokenData(tokenData: TokenData)
    suspend fun clear()
}
