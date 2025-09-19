package com.msgmates.app.ui.settings.notifications

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msgmates.app.databinding.FragmentNotificationsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupUI()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupUI() {
        binding.apply {
            // Message notifications
            switchMessageNotifications.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateMessageNotifications(isChecked)
            }

            // Call notifications
            switchCallNotifications.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateCallNotifications(isChecked)
            }

            // Silent hours start
            btnSilentHoursStart.setOnClickListener {
                showTimePickerDialog { hour, minute ->
                    viewModel.updateSilentHoursStart(hour, minute)
                }
            }

            // Silent hours end
            btnSilentHoursEnd.setOnClickListener {
                showTimePickerDialog { hour, minute ->
                    viewModel.updateSilentHoursEnd(hour, minute)
                }
            }

            // Custom ringtone
            btnCustomRingtone.setOnClickListener {
                viewModel.openRingtonePicker()
            }

            // Notification content
            rbHideContent.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateNotificationContent(NotificationContent.HIDE)
            }

            rbShowSummary.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateNotificationContent(NotificationContent.SUMMARY)
            }

            rbShowFullContent.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateNotificationContent(NotificationContent.FULL)
            }
        }
    }

    private fun showTimePickerDialog(onTimeSelected: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.apply {
                    // Update switches
                    switchMessageNotifications.isChecked = state.messageNotifications
                    switchCallNotifications.isChecked = state.callNotifications

                    // Update silent hours
                    tvSilentHoursStart.text = state.silentHoursStartText
                    tvSilentHoursEnd.text = state.silentHoursEndText

                    // Update ringtone
                    tvCustomRingtone.text = state.customRingtoneName

                    // Update notification content radio buttons
                    rbHideContent.isChecked = state.notificationContent == NotificationContent.HIDE
                    rbShowSummary.isChecked = state.notificationContent == NotificationContent.SUMMARY
                    rbShowFullContent.isChecked = state.notificationContent == NotificationContent.FULL

                    // Loading state
                    if (state.isLoading) {
                        progressBar.visibility = View.VISIBLE
                    } else {
                        progressBar.visibility = View.GONE
                    }

                    // Success message
                    if (state.showSuccessMessage) {
                        // Show success message
                        viewModel.clearSuccessMessage()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

enum class NotificationContent {
    HIDE, SUMMARY, FULL
}
