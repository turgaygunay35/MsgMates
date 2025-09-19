package com.msgmates.app.data.local.prefs

import android.content.Context
import android.location.Location
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.disasterDataStore: DataStore<Preferences> by preferencesDataStore(name = "disaster_prefs")

@Singleton
class DisasterPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_DISASTER_ENABLED = booleanPreferencesKey("disaster_enabled")
        private val KEY_AUTO_ENABLE_ON_EARTHQUAKE = booleanPreferencesKey("auto_enable_on_earthquake")
        private val KEY_ENERGY_SAVING = booleanPreferencesKey("energy_saving")
        private val KEY_SELECTED_CITY = stringPreferencesKey("selected_city")
        private val KEY_HELP_KEYWORD = stringPreferencesKey("help_keyword")
        private val KEY_EARTHQUAKE_THRESHOLD = doublePreferencesKey("earthquake_threshold")
        private val KEY_CURRENT_LATITUDE = doublePreferencesKey("current_latitude")
        private val KEY_CURRENT_LONGITUDE = doublePreferencesKey("current_longitude")
        private val KEY_CURRENT_ACCURACY = doublePreferencesKey("current_accuracy")
        private val KEY_CURRENT_TIMESTAMP = longPreferencesKey("current_timestamp")
        private val KEY_LAST_KNOWN_LATITUDE = doublePreferencesKey("last_known_latitude")
        private val KEY_LAST_KNOWN_LONGITUDE = doublePreferencesKey("last_known_longitude")
        private val KEY_LAST_KNOWN_ACCURACY = doublePreferencesKey("last_known_accuracy")
        private val KEY_LAST_KNOWN_TIMESTAMP = longPreferencesKey("last_known_timestamp")
    }

    // Afet modu durumu
    val isDisasterEnabled: Flow<Boolean> = context.disasterDataStore.data.map { preferences ->
        preferences[KEY_DISASTER_ENABLED] ?: false
    }

    // Deprem otomatik açılış
    val autoEnableOnEarthquake: Flow<Boolean> = context.disasterDataStore.data.map { preferences ->
        preferences[KEY_AUTO_ENABLE_ON_EARTHQUAKE] ?: false
    }

    // Enerji tasarrufu
    val energySaving: Flow<Boolean> = context.disasterDataStore.data.map { preferences ->
        preferences[KEY_ENERGY_SAVING] ?: false
    }

    suspend fun setDisasterEnabled(enabled: Boolean) {
        context.disasterDataStore.edit { preferences ->
            preferences[KEY_DISASTER_ENABLED] = enabled
        }
    }

    suspend fun setAutoEnableOnEarthquake(enabled: Boolean) {
        context.disasterDataStore.edit { preferences ->
            preferences[KEY_AUTO_ENABLE_ON_EARTHQUAKE] = enabled
        }
    }

    suspend fun setEnergySaving(enabled: Boolean) {
        context.disasterDataStore.edit { preferences ->
            preferences[KEY_ENERGY_SAVING] = enabled
        }
    }

    // Seçilen şehir
    val selectedCity: Flow<String> = context.disasterDataStore.data.map { preferences ->
        preferences[KEY_SELECTED_CITY] ?: "İstanbul"
    }

    suspend fun setSelectedCity(city: String) {
        context.disasterDataStore.edit { preferences ->
            preferences[KEY_SELECTED_CITY] = city
        }
    }

    // Yardım kelimesi
    val helpKeyword: Flow<String> = context.disasterDataStore.data.map { preferences ->
        preferences[KEY_HELP_KEYWORD] ?: "yardım"
    }

    suspend fun setHelpKeyword(keyword: String) {
        context.disasterDataStore.edit { preferences ->
            preferences[KEY_HELP_KEYWORD] = keyword
        }
    }

    // Deprem eşiği
    val earthquakeThreshold: Flow<Double> = context.disasterDataStore.data.map { preferences ->
        preferences[KEY_EARTHQUAKE_THRESHOLD] ?: 6.5
    }

    suspend fun setEarthquakeThreshold(threshold: Double) {
        context.disasterDataStore.edit { preferences ->
            preferences[KEY_EARTHQUAKE_THRESHOLD] = threshold
        }
    }

    // Mevcut konum
    val currentLocation: Flow<Location?> = context.disasterDataStore.data.map { preferences ->
        val latitude = preferences[KEY_CURRENT_LATITUDE]
        val longitude = preferences[KEY_CURRENT_LONGITUDE]
        val accuracy = preferences[KEY_CURRENT_ACCURACY]
        val timestamp = preferences[KEY_CURRENT_TIMESTAMP]

        if (latitude != null && longitude != null) {
            Location("disaster_prefs").apply {
                this.latitude = latitude
                this.longitude = longitude
                this.accuracy = accuracy?.toFloat() ?: 0f
                this.time = timestamp ?: System.currentTimeMillis()
            }
        } else {
            null
        }
    }

    suspend fun setCurrentLocation(location: Location) {
        context.disasterDataStore.edit { preferences ->
            preferences[KEY_CURRENT_LATITUDE] = location.latitude
            preferences[KEY_CURRENT_LONGITUDE] = location.longitude
            preferences[KEY_CURRENT_ACCURACY] = location.accuracy.toDouble()
            preferences[KEY_CURRENT_TIMESTAMP] = location.time
        }
    }

    // Son bilinen konum
    val lastKnownLocation: Flow<Location?> = context.disasterDataStore.data.map { preferences ->
        val latitude = preferences[KEY_LAST_KNOWN_LATITUDE]
        val longitude = preferences[KEY_LAST_KNOWN_LONGITUDE]
        val accuracy = preferences[KEY_LAST_KNOWN_ACCURACY]
        val timestamp = preferences[KEY_LAST_KNOWN_TIMESTAMP]

        if (latitude != null && longitude != null) {
            Location("disaster_prefs").apply {
                this.latitude = latitude
                this.longitude = longitude
                this.accuracy = accuracy?.toFloat() ?: 0f
                this.time = timestamp ?: System.currentTimeMillis()
            }
        } else {
            null
        }
    }

    suspend fun setLastKnownLocation(location: Location) {
        context.disasterDataStore.edit { preferences ->
            preferences[KEY_LAST_KNOWN_LATITUDE] = location.latitude
            preferences[KEY_LAST_KNOWN_LONGITUDE] = location.longitude
            preferences[KEY_LAST_KNOWN_ACCURACY] = location.accuracy.toDouble()
            preferences[KEY_LAST_KNOWN_TIMESTAMP] = location.time
        }
    }
}
