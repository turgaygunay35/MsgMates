package com.msgmates.app.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.msgmates.app.databinding.FragmentContactsTelemetryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsTelemetryFragment : Fragment() {

    private var _binding: FragmentContactsTelemetryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsTelemetryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsTelemetryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        observeViewModel()
        loadTelemetryData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.telemetryData.collect { data ->
                updateUI(data)
            }
        }
    }

    private fun loadTelemetryData() {
        viewModel.loadTelemetryData()
    }

    private fun updateUI(data: com.msgmates.app.analytics.ContactsTelemetryData) {
        binding.apply {
            // Sync metrics
            tvSyncDuration.text = "${data.syncDurationMs}ms"
            tvBatchSize.text = data.batchSize.toString()
            tvTotalSyncCount.text = data.totalSyncCount.toString()
            tvLastSyncTime.text = formatTimestamp(data.lastSyncTimestamp)

            // Error metrics
            tvErrorCount.text = data.errorCodes.size.toString()
            tvErrorCodes.text = data.errorCodes.joinToString(", ")

            // Usage metrics
            tvPullToRefreshCount.text = data.pullToRefreshCount.toString()
            tvFavoritesFilterUsage.text = data.filterFavoritesUsage.toString()
            binding.tvMsgMatesFilterUsage.text = data.filterMsgMatesUsage.toString()

            // Performance indicators
            val errorRate = if (data.totalSyncCount > 0) {
                (data.errorCodes.size.toFloat() / data.totalSyncCount * 100).toInt()
            } else {
                0
            }
            tvErrorRate.text = "%$errorRate"

            val avgSyncTime = if (data.totalSyncCount > 0) {
                data.syncDurationMs / data.totalSyncCount
            } else {
                0L
            }
            tvAvgSyncTime.text = "${avgSyncTime}ms"
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return "Hiç senkronize edilmemiş"

        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000L -> "Az önce"
            diff < 3600_000L -> "${diff / 60_000L} dakika önce"
            diff < 86400_000L -> "${diff / 3600_000L} saat önce"
            else -> "${diff / 86400_000L} gün önce"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
