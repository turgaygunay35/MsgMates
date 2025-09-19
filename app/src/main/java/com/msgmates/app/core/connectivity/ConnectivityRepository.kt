package com.msgmates.app.core.connectivity

import com.msgmates.app.ui.common.ConnectionStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@Singleton
class ConnectivityRepository @Inject constructor() {

    private val _status = flow {
        val statuses = listOf(
            ConnectionStatus.LIVE,
            ConnectionStatus.SYNC,
            ConnectionStatus.OFFLINE,
            ConnectionStatus.DISASTER
        )

        var index = 0
        while (true) {
            emit(statuses[index])
            delay(5000) // 5 saniyede bir değiş
            index = (index + 1) % statuses.size
        }
    }

    val status: Flow<ConnectionStatus> = _status.flowOn(Dispatchers.IO)

    suspend fun retryConnect() {
        // Simulate reconnection process on IO dispatcher to avoid ANR
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            delay(2000) // 2 saniye bekle
            // Status will automatically change to SYNC then LIVE via the flow
        }
    }
}
