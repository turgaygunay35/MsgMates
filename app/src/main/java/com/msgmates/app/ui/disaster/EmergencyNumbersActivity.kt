package com.msgmates.app.ui.disaster

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.R
import com.msgmates.app.databinding.ActivityEmergencyNumbersBinding

class EmergencyNumbersActivity : ComponentActivity() {

    private lateinit var binding: ActivityEmergencyNumbersBinding
    private lateinit var adapter: EmergencyNumbersAdapter

    private val emergencyNumbers = listOf(
        EmergencyNumber("Acil Yardım Hattı", "112", "🚨", R.color.emergency_red),
        EmergencyNumber("Polis İmdat", "155", "👮", R.color.emergency_blue),
        EmergencyNumber("İtfaiye", "110", "🚒", R.color.emergency_red),
        EmergencyNumber("Jandarma", "156", "🛡️", R.color.emergency_blue),
        EmergencyNumber("Doğalgaz Arıza", "187", "⛽", R.color.emergency_orange),
        EmergencyNumber("Su Arıza", "185", "💧", R.color.emergency_blue),
        EmergencyNumber("Orman Yangınları", "177", "🔥", R.color.emergency_red),
        EmergencyNumber("Sahil Güvenlik", "158", "⚓", R.color.emergency_blue),
        EmergencyNumber("TCDD Acil Durum", "131", "🚂", R.color.emergency_orange),
        EmergencyNumber("Karayolları", "159", "🛣️", R.color.emergency_orange)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmergencyNumbersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // RecyclerView
        adapter = EmergencyNumbersAdapter { emergencyNumber ->
            makePhoneCall(emergencyNumber.number)
        }

        binding.rvEmergencyNumbers.layoutManager = LinearLayoutManager(this)
        binding.rvEmergencyNumbers.adapter = adapter

        // Set data
        adapter.submitList(emergencyNumbers)
    }

    private fun makePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }

        // Check if we have permission to make calls
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Fallback to dialer
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(dialIntent)
        }
    }
}

data class EmergencyNumber(
    val name: String,
    val number: String,
    val icon: String,
    val colorRes: Int
)
