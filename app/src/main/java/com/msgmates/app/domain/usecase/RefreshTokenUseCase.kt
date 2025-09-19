package com.msgmates.app.domain.usecase

import com.msgmates.app.data.repository.auth.AuthRepository // ← DOĞRU import
import javax.inject.Inject

class RefreshTokenUseCase @Inject constructor(
    @Suppress("unused")
    private val authRepository: AuthRepository
) {
    // İleride gerçek implementasyonu eklersin; şimdilik derlemeyi kilitlemesin.
    suspend operator fun invoke(): Result<Unit> = Result.success(Unit)
}
