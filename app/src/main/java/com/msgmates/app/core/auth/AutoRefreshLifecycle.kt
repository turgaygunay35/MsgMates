package com.msgmates.app.core.auth

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Singleton
class AutoRefreshLifecycle @Inject constructor(
    private val tokenRepo: TokenRepository,
    private val refresh: RefreshCoordinator
) : DefaultLifecycleObserver {

    private var job: Job? = null

    override fun onStart(owner: LifecycleOwner) {
        job = owner.lifecycleScope.launch {
            while (isActive) {
                // Token refresh logic - simplified for now
                val tokens = tokenRepo.getTokensSync()
                if (tokens.access != null) {
                    // Basic refresh check - can be enhanced later
                    try {
                        refresh.blockingRefresh()
                    } catch (e: Exception) {
                        android.util.Log.e("AutoRefresh", "Refresh failed", e)
                    }
                }
                delay(30_000) // 30 sn'de bir kontrol
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        job?.cancel()
        job = null
    }
}
