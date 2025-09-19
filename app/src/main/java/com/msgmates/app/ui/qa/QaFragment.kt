package com.msgmates.app.ui.qa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.msgmates.app.databinding.FragmentQaBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QaFragment : Fragment() {
    private var _binding: FragmentQaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnToggleTheme.setOnClickListener {
            viewModel.toggleTheme()
            Toast.makeText(requireContext(), "Tema değiştirildi", Toast.LENGTH_SHORT).show()
        }

        binding.btnToggleRtl.setOnClickListener {
            viewModel.toggleRtl()
            Toast.makeText(requireContext(), "RTL simülasyonu değiştirildi", Toast.LENGTH_SHORT).show()
        }

        binding.btnTestBadge.setOnClickListener {
            viewModel.toggleBadgeTest()
            Toast.makeText(requireContext(), "Badge testi değiştirildi", Toast.LENGTH_SHORT).show()
        }

        binding.btnSimulateOffline.setOnClickListener {
            viewModel.toggleOfflineSimulation()
            Toast.makeText(requireContext(), "Offline simülasyonu değiştirildi", Toast.LENGTH_SHORT).show()
        }

        binding.btnToggleDisaster.setOnClickListener {
            viewModel.toggleDisasterMode()
            Toast.makeText(requireContext(), "Afet modu değiştirildi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.qaStatus.observe(viewLifecycleOwner) { status ->
            binding.tvQaStatus.text = status
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
