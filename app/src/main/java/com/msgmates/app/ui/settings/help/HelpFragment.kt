package com.msgmates.app.ui.settings.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.databinding.FragmentHelpBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HelpViewModel by viewModels()
    private lateinit var faqAdapter: FaqAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupUI()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupUI() {
        binding.apply {
            // Contact support
            btnContactSupport.setOnClickListener {
                viewModel.openContactSupport()
            }

            // Send feedback
            btnSendFeedback.setOnClickListener {
                viewModel.openFeedback()
            }

            // About
            btnAbout.setOnClickListener {
                viewModel.openAbout()
            }
        }
    }

    private fun setupRecyclerView() {
        faqAdapter = FaqAdapter { faqItem ->
            viewModel.toggleFaqItem(faqItem.id)
        }

        binding.recyclerViewFaq.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = faqAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.apply {
                    // Update FAQ list
                    faqAdapter.submitList(state.faqItems)

                    // Show/hide empty state
                    if (state.faqItems.isEmpty()) {
                        tvEmptyFaq.visibility = View.VISIBLE
                        recyclerViewFaq.visibility = View.GONE
                    } else {
                        tvEmptyFaq.visibility = View.GONE
                        recyclerViewFaq.visibility = View.VISIBLE
                    }

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

data class FaqItem(
    val id: String,
    val question: String,
    val answer: String,
    val isExpanded: Boolean = false
)
