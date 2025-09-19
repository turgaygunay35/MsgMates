package com.msgmates.app.core.location

import android.location.Location
import com.msgmates.app.data.local.prefs.DisasterPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class LocationRepository @Inject constructor(
    private val disasterPreferences: DisasterPreferences
) {

    fun getCurrentLocation(): Flow<Location?> {
        return disasterPreferences.currentLocation
    }

    suspend fun saveCurrentLocation(location: Location) {
        disasterPreferences.setCurrentLocation(location)
    }

    fun getLastKnownLocation(): Flow<Location?> {
        return disasterPreferences.lastKnownLocation
    }

    suspend fun saveLastKnownLocation(location: Location) {
        disasterPreferences.setLastKnownLocation(location)
    }
}
