package com.msgmates.app.ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msgmates.app.R
import com.msgmates.app.databinding.FragmentOutgoingCallBinding
import com.msgmates.app.domain.call.CallUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OutgoingCallFragment : Fragment() {

    private var _binding: FragmentOutgoingCallBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CallViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOutgoingCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.fabHangup.setOnClickListener {
            viewModel.endCall()
            findNavController().navigateUp()
        }

        binding.fabMute.setOnClickListener {
            viewModel.toggleMute()
        }

        binding.fabSpeaker.setOnClickListener {
            viewModel.toggleSpeaker()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.callState.collect { state ->
                when (state) {
                    is CallUiState.Outgoing -> {
                        updateOutgoingUI(state)
                    }
                    is CallUiState.Ongoing -> {
                        // Navigate to ongoing call
                        navigateToOngoingCall(state)
                    }
                    is CallUiState.Idle -> {
                        findNavController().navigateUp()
                    }
                    else -> {
                        // Handle other states if needed
                    }
                }
            }
        }
    }

    private fun updateOutgoingUI(state: CallUiState.Outgoing) {
        binding.tvCallerName.text = state.calleeName
        binding.tvCallStatus.text = "Aranıyor…"

        // Update call type indicator
        if (state.callType == com.msgmates.app.domain.call.CallType.VIDEO) {
            binding.ivCallType.setImageResource(R.drawable.ic_videocam)
            binding.tvCallType.text = "Görüntülü Arama"
        } else {
            binding.ivCallType.setImageResource(R.drawable.ic_phone)
            binding.tvCallType.text = "Sesli Arama"
        }

        // TODO: Load caller avatar
        // Glide.with(this)
        //     .load(state.calleeAvatar)
        //     .placeholder(R.drawable.ic_person)
        //     .into(binding.ivCallerAvatar)
    }

    private fun navigateToOngoingCall(state: CallUiState.Ongoing) {
        val bundle = Bundle().apply {
            putString("callId", state.callId)
            putString("callerId", state.callerId)
            putString("callerName", state.callerName)
            putString("callerAvatar", state.callerAvatar)
            putBoolean("isVideo", state.callType == com.msgmates.app.domain.call.CallType.VIDEO)
        }
        findNavController().navigate(R.id.dest_ongoing_call, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
