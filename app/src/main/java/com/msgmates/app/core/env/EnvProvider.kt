package com.msgmates.app.core.env

import android.content.Context
import com.msgmates.app.R
import java.io.IOException
import kotlinx.serialization.json.Json

object EnvProvider {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    fun load(context: Context): EnvConfig {
        val flavor = context.getString(R.string.env_name) // "dev" | "prod"
        val fileName = "env.$flavor.json"

        try {
            android.util.Log.d("EnvProvider", "FLAVOR=$flavor, file=$fileName")
            // Kök assets'i listeleyelim (dosya gerçekten paketlenmiş mi?)
            android.util.Log.d("EnvProvider", "assets root: " + context.assets.list("")?.joinToString())

            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            android.util.Log.d("EnvProvider", "env json first 300: ${jsonString.take(300)}")

            return json.decodeFromString<EnvConfig>(jsonString)
        } catch (e: IOException) {
            android.util.Log.e("EnvProvider", "Failed to load $fileName", e)
            throw RuntimeException("Failed to load environment config from $fileName", e)
        } catch (e: Exception) {
            android.util.Log.e("EnvProvider", "Failed to parse $fileName", e)
            throw RuntimeException("Failed to parse environment config from $fileName", e)
        }
    }
}
