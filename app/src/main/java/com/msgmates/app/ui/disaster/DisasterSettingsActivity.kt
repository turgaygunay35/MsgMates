package com.msgmates.app.ui.disaster

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.R
import com.msgmates.app.data.local.prefs.DisasterPreferences
import com.msgmates.app.databinding.ActivityDisasterSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DisasterSettingsActivity : ComponentActivity() {

    private lateinit var binding: ActivityDisasterSettingsBinding
    private lateinit var cityAdapter: CityAdapter

    @Inject
    lateinit var disasterPreferences: DisasterPreferences

    private val speechRecognizer by lazy { SpeechRecognizer.createSpeechRecognizer(this) }
    private val voiceResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                val spokenText = results[0].lowercase()
                checkVoiceCommand(spokenText)
            }
        }
    }

    private val cities = listOf(
        City("İstanbul", "34.0", "41.0", "istanbul"),
        City("Ankara", "32.9", "39.9", "ankara"),
        City("İzmir", "27.1", "38.4", "izmir"),
        City("Bursa", "29.1", "40.2", "bursa"),
        City("Antalya", "30.7", "36.9", "antalya"),
        City("Adana", "35.3", "37.0", "adana"),
        City("Konya", "32.5", "37.9", "konya"),
        City("Gaziantep", "37.4", "37.1", "gaziantep"),
        City("Mersin", "34.6", "36.8", "mersin"),
        City("Diyarbakır", "40.2", "37.9", "diyarbakir"),
        City("Kayseri", "35.5", "38.7", "kayseri"),
        City("Eskişehir", "30.5", "39.8", "eskisehir"),
        City("Samsun", "36.3", "41.3", "samsun"),
        City("Denizli", "29.1", "37.8", "denizli"),
        City("Malatya", "38.3", "38.4", "malatya")
    )

    private var selectedCity: City? = null
    private var helpKeyword = "yardım"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDisasterSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadSettings()
    }

    private fun setupUI() {
        // Toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // City selection
        cityAdapter = CityAdapter { city ->
            selectedCity = city
            binding.tvSelectedCity.text = city.name
            saveSettings()
        }

        binding.rvCities.layoutManager = LinearLayoutManager(this)
        binding.rvCities.adapter = cityAdapter
        cityAdapter.submitList(cities)

        // Help keyword input
        binding.etHelpKeyword.setText(helpKeyword)
        binding.etHelpKeyword.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                helpKeyword = s.toString().trim()
                saveSettings()
            }
        })

        // Test voice command
        binding.btnTestVoice.setOnClickListener {
            startVoiceRecognition()
        }

        // Auto enable switch
        binding.switchAutoEnable.setOnCheckedChangeListener { _, isChecked ->
            CoroutineScope(Dispatchers.IO).launch {
                disasterPreferences.setAutoEnableOnEarthquake(isChecked)
            }
        }

        // Earthquake threshold
        binding.etEarthquakeThreshold.setText("6.5")
        binding.etEarthquakeThreshold.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val threshold = s.toString().toDoubleOrNull() ?: 6.5
                // TODO: Save earthquake threshold
            }
        })

        // Save button
        binding.btnSave.setOnClickListener {
            saveSettings()
            Toast.makeText(this, "Ayarlar kaydedildi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            disasterPreferences.autoEnableOnEarthquake.collect { autoEnable ->
                runOnUiThread {
                    binding.switchAutoEnable.isChecked = autoEnable
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            disasterPreferences.selectedCity.collect { selectedCity ->
                runOnUiThread {
                    binding.tvSelectedCity.text = selectedCity
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            disasterPreferences.helpKeyword.collect { helpKeyword ->
                runOnUiThread {
                    binding.etHelpKeyword.setText(helpKeyword)
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            disasterPreferences.earthquakeThreshold.collect { threshold ->
                runOnUiThread {
                    binding.etEarthquakeThreshold.setText(threshold.toString())
                }
            }
        }
    }

    internal fun saveSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            selectedCity?.let { city ->
                disasterPreferences.setSelectedCity(city.name)
            }
            disasterPreferences.setHelpKeyword(helpKeyword)

            val threshold = binding.etEarthquakeThreshold.text.toString().toDoubleOrNull() ?: 6.5
            disasterPreferences.setEarthquakeThreshold(threshold)
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Yardım kelimesini söyleyin: '$helpKeyword'")
        }

        try {
            voiceResultLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Ses tanıma kullanılamıyor", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkVoiceCommand(spokenText: String) {
        if (spokenText.contains(helpKeyword)) {
            binding.tvVoiceTestResult.text = "✅ Sesli komut tanındı: '$spokenText'"
            binding.tvVoiceTestResult.setTextColor(getColor(android.R.color.holo_green_dark))

            // TODO: Trigger emergency broadcast
            Toast.makeText(this, "Yardım komutu algılandı!", Toast.LENGTH_LONG).show()
        } else {
            binding.tvVoiceTestResult.text = "❌ Komut tanınmadı. Beklenen: '$helpKeyword'"
            binding.tvVoiceTestResult.setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}

data class City(
    val name: String,
    val longitude: String,
    val latitude: String,
    val code: String
)
