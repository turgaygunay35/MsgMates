package com.msgmates.app.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Base repository class that provides common database operations with proper threading
 */
abstract class BaseRepository {

    /**
     * Execute database operation on IO dispatcher
     */
    protected suspend fun <T> dbOperation(operation: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            operation()
        }
    }

    /**
     * Execute database operation on IO dispatcher with result handling
     */
    protected suspend fun <T> dbOperationResult(
        operation: suspend () -> T,
        onError: (Throwable) -> T
    ): T {
        return withContext(Dispatchers.IO) {
            try {
                operation()
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }
}
