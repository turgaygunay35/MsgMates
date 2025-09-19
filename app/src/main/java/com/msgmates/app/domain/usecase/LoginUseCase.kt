package com.msgmates.app.domain.usecase

import com.msgmates.app.data.remote.auth.RequestCodeResponse
import com.msgmates.app.data.repository.auth.AuthRepository // ← DOĞRU import
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phoneNumber: String): Result<RequestCodeResponse> {
        return authRepository.requestCode(phoneNumber) // mevcut kontrat
    }
}
