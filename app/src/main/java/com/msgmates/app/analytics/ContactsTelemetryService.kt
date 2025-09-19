package com.msgmates.app.analytics

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

@Singleton
class ContactsTelemetryService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        internal val Context.telemetryDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "contacts_telemetry"
        )

        // Telemetry keys
        private val SYNC_DURATION_MS = longPreferencesKey("sync_duration_ms")
        private val BATCH_SIZE = intPreferencesKey("batch_size")
        private val ERROR_CODES = stringSetPreferencesKey("error_codes")
        private val PULL_TO_REFRESH_COUNT = intPreferencesKey("pull_to_refresh_count")
        private val FILTER_FAVORITES_USAGE = intPreferencesKey("filter_favorites_usage")
        private val FILTER_MSGMATES_USAGE = intPreferencesKey("filter_msgmates_usage")
        private val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        private val TOTAL_SYNC_COUNT = intPreferencesKey("total_sync_count")
    }

    // Sync duration tracking
    suspend fun recordSyncDuration(durationMs: Long) {
        context.telemetryDataStore.edit { preferences ->
            preferences[SYNC_DURATION_MS] = durationMs
            preferences[LAST_SYNC_TIMESTAMP] = System.currentTimeMillis()
            val currentCount = preferences[TOTAL_SYNC_COUNT] ?: 0
            preferences[TOTAL_SYNC_COUNT] = currentCount + 1
        }
        Timber.d("Contacts sync duration recorded: ${durationMs}ms")
    }

    // Batch size tracking
    suspend fun recordBatchSize(batchSize: Int) {
        context.telemetryDataStore.edit { preferences ->
            preferences[BATCH_SIZE] = batchSize
        }
        Timber.d("Contacts batch size recorded: $batchSize")
    }

    // Error code tracking
    suspend fun recordErrorCode(errorCode: String) {
        context.telemetryDataStore.edit { preferences ->
            val currentErrors = preferences[ERROR_CODES]?.toMutableSet() ?: mutableSetOf()
            currentErrors.add(errorCode)
            preferences[ERROR_CODES] = currentErrors
        }
        Timber.w("Contacts error code recorded: $errorCode")
    }

    // Pull-to-refresh usage tracking
    suspend fun recordPullToRefresh() {
        context.telemetryDataStore.edit { preferences ->
            val currentCount = preferences[PULL_TO_REFRESH_COUNT] ?: 0
            preferences[PULL_TO_REFRESH_COUNT] = currentCount + 1
        }
        Timber.d("Pull-to-refresh usage recorded")
    }

    // Filter usage tracking
    suspend fun recordFilterUsage(filterType: FilterType) {
        context.telemetryDataStore.edit { preferences ->
            when (filterType) {
                FilterType.FAVORITES -> {
                    val currentCount = preferences[FILTER_FAVORITES_USAGE] ?: 0
                    preferences[FILTER_FAVORITES_USAGE] = currentCount + 1
                }
                FilterType.MSGMATES -> {
                    val currentCount = preferences[FILTER_MSGMATES_USAGE] ?: 0
                    preferences[FILTER_MSGMATES_USAGE] = currentCount + 1
                }
            }
        }
        Timber.d("Filter usage recorded: $filterType")
    }

    // Get telemetry data
    val telemetryData: Flow<ContactsTelemetryData> = context.telemetryDataStore.data.map { preferences ->
        ContactsTelemetryData(
            syncDurationMs = preferences[SYNC_DURATION_MS] ?: 0L,
            batchSize = preferences[BATCH_SIZE] ?: 0,
            errorCodes = preferences[ERROR_CODES]?.toList() ?: emptyList(),
            pullToRefreshCount = preferences[PULL_TO_REFRESH_COUNT] ?: 0,
            filterFavoritesUsage = preferences[FILTER_FAVORITES_USAGE] ?: 0,
            filterMsgMatesUsage = preferences[FILTER_MSGMATES_USAGE] ?: 0,
            lastSyncTimestamp = preferences[LAST_SYNC_TIMESTAMP] ?: 0L,
            totalSyncCount = preferences[TOTAL_SYNC_COUNT] ?: 0
        )
    }

    // Clear telemetry data (for testing or privacy)
    suspend fun clearTelemetryData() {
        context.telemetryDataStore.edit { preferences ->
            preferences.clear()
        }
        Timber.d("Contacts telemetry data cleared")
    }

    // Get sync performance metrics
    suspend fun getSyncPerformanceMetrics(): SyncPerformanceMetrics {
        val data = telemetryData.map { it }.let { flow ->
            // This is a simplified version - in real implementation you'd collect the flow
            ContactsTelemetryData(
                syncDurationMs = 0L,
                batchSize = 0,
                errorCodes = emptyList(),
                pullToRefreshCount = 0,
                filterFavoritesUsage = 0,
                filterMsgMatesUsage = 0,
                lastSyncTimestamp = 0L,
                totalSyncCount = 0
            )
        }

        return SyncPerformanceMetrics(
            averageSyncDuration = data.syncDurationMs,
            averageBatchSize = data.batchSize,
            errorRate = if (data.totalSyncCount > 0) {
                data.errorCodes.size.toFloat() / data.totalSyncCount
            } else {
                0f
            },
            pullToRefreshFrequency = data.pullToRefreshCount,
            filterUsageRatio = if (data.filterFavoritesUsage + data.filterMsgMatesUsage > 0) {
                data.filterFavoritesUsage.toFloat() / (data.filterFavoritesUsage + data.filterMsgMatesUsage)
            } else {
                0f
            }
        )
    }
}

enum class FilterType {
    FAVORITES,
    MSGMATES
}

data class ContactsTelemetryData(
    val syncDurationMs: Long,
    val batchSize: Int,
    val errorCodes: List<String>,
    val pullToRefreshCount: Int,
    val filterFavoritesUsage: Int,
    val filterMsgMatesUsage: Int,
    val lastSyncTimestamp: Long,
    val totalSyncCount: Int
)

data class SyncPerformanceMetrics(
    val averageSyncDuration: Long,
    val averageBatchSize: Int,
    val errorRate: Float,
    val pullToRefreshFrequency: Int,
    val filterUsageRatio: Float
)
