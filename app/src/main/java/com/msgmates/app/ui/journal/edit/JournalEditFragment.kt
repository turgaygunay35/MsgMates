package com.msgmates.app.ui.journal.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msgmates.app.databinding.FragmentJournalEditBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JournalEditFragment : Fragment() {

    private var _binding: FragmentJournalEditBinding? = null
    private val binding get() = _binding!!

    internal val viewModel: JournalEditViewModel by viewModels()
    // COMMENTED OUT FOR CLEAN BUILD
    // private val args: JournalEditFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
        // COMMENTED OUT FOR CLEAN BUILD
        // viewModel.loadEntry(args.entryId)
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                com.msgmates.app.R.id.action_save -> {
                    viewModel.saveEntry()
                    true
                }
                else -> false
            }
        }

        binding.editTitle.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.updateTitle(s.toString())
            }
        })

        binding.editContent.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.updateContent(s.toString())
            }
        })

        binding.buttonAddPhoto.setOnClickListener {
            // TODO: Open photo picker
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.contentLayout.visibility = if (state.isLoading) View.GONE else View.VISIBLE

                binding.editTitle.setText(state.title)
                binding.editContent.setText(state.content)

                // Title validation
                binding.layoutTitle.error = if (!state.isTitleValid) {
                    getString(
                        com.msgmates.app.R.string.journal_error_title_required
                    )
                } else {
                    null
                }

                // Content validation
                binding.layoutContent.error = if (!state.isContentValid) {
                    getString(
                        com.msgmates.app.R.string.journal_error_content_required
                    )
                } else {
                    null
                }

                // Save button state
                binding.toolbar.menu.findItem(com.msgmates.app.R.id.action_save)?.isEnabled = state.isFormValid

                state.error?.let { _ ->
                    // TODO: Show error snackbar
                }
            }
        }

        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is JournalEditEvent.EntryCreated -> {
                        findNavController().navigateUp()
                    }
                    is JournalEditEvent.EntryUpdated -> {
                        findNavController().navigateUp()
                    }
                    is JournalEditEvent.Error -> {
                        // TODO: Show error
                    }
                    null -> { /* Clear event */ }
                }
                viewModel.clearEvent()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
