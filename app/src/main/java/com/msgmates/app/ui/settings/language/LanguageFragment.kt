package com.msgmates.app.ui.settings.language

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msgmates.app.databinding.FragmentLanguageBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LanguageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
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
            // Language selection
            rbTurkish.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateLanguage(Language.TURKISH)
            }

            rbEnglish.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateLanguage(Language.ENGLISH)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.apply {
                    // Update language radio buttons
                    rbTurkish.isChecked = state.language == Language.TURKISH
                    rbEnglish.isChecked = state.language == Language.ENGLISH

                    // Show restart dialog if language changed
                    if (state.showRestartDialog) {
                        showRestartDialog()
                        viewModel.clearRestartDialog()
                    }

                    // Loading state
                    if (state.isLoading) {
                        progressBar.visibility = View.VISIBLE
                    } else {
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showRestartDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Uygulamayı Yeniden Başlat")
            .setMessage(
                "Dil değişikliğinin etkili olması için uygulamayı yeniden başlatmanız gerekiyor. Şimdi yeniden başlatmak istiyor musunuz?"
            )
            .setPositiveButton("Yeniden Başlat") { _, _ ->
                viewModel.restartApp()
            }
            .setNegativeButton("Daha Sonra") { _, _ ->
                // Do nothing
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

enum class Language {
    TURKISH, ENGLISH
}
