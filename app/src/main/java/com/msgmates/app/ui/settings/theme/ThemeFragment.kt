package com.msgmates.app.ui.settings.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msgmates.app.databinding.FragmentThemeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ThemeFragment : Fragment() {

    private var _binding: FragmentThemeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ThemeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeBinding.inflate(inflater, container, false)
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
            // Theme mode
            rbLightTheme.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateThemeMode(ThemeMode.LIGHT)
            }

            rbDarkTheme.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateThemeMode(ThemeMode.DARK)
            }

            rbSystemTheme.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateThemeMode(ThemeMode.SYSTEM)
            }

            // Color theme
            rbMsgMatesGradient.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateColorTheme(ColorTheme.MSGMATES_GRADIENT)
            }

            rbBlueTheme.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateColorTheme(ColorTheme.BLUE)
            }

            rbGreenTheme.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateColorTheme(ColorTheme.GREEN)
            }

            // Font size
            rbSmallFont.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateFontSize(FontSize.SMALL)
            }

            rbMediumFont.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateFontSize(FontSize.MEDIUM)
            }

            rbLargeFont.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateFontSize(FontSize.LARGE)
            }

            // Chat bubble density
            rbTightBubbles.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateBubbleDensity(BubbleDensity.TIGHT)
            }

            rbMediumBubbles.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateBubbleDensity(BubbleDensity.MEDIUM)
            }

            rbLooseBubbles.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.updateBubbleDensity(BubbleDensity.LOOSE)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.apply {
                    // Update theme mode radio buttons
                    rbLightTheme.isChecked = state.themeMode == ThemeMode.LIGHT
                    rbDarkTheme.isChecked = state.themeMode == ThemeMode.DARK
                    rbSystemTheme.isChecked = state.themeMode == ThemeMode.SYSTEM

                    // Update color theme radio buttons
                    rbMsgMatesGradient.isChecked = state.colorTheme == ColorTheme.MSGMATES_GRADIENT
                    rbBlueTheme.isChecked = state.colorTheme == ColorTheme.BLUE
                    rbGreenTheme.isChecked = state.colorTheme == ColorTheme.GREEN

                    // Update font size radio buttons
                    rbSmallFont.isChecked = state.fontSize == FontSize.SMALL
                    rbMediumFont.isChecked = state.fontSize == FontSize.MEDIUM
                    rbLargeFont.isChecked = state.fontSize == FontSize.LARGE

                    // Update bubble density radio buttons
                    rbTightBubbles.isChecked = state.bubbleDensity == BubbleDensity.TIGHT
                    rbMediumBubbles.isChecked = state.bubbleDensity == BubbleDensity.MEDIUM
                    rbLooseBubbles.isChecked = state.bubbleDensity == BubbleDensity.LOOSE

                    // Update preview
                    updatePreview(state)

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

    private fun updatePreview(state: ThemeUiState) {
        binding.apply {
            // Update preview card based on current settings
            cardPreview.setCardBackgroundColor(
                when (state.colorTheme) {
                    ColorTheme.MSGMATES_GRADIENT -> requireContext().getColor(com.msgmates.app.R.color.primary_blue)
                    ColorTheme.BLUE -> requireContext().getColor(com.msgmates.app.R.color.primary_blue)
                    ColorTheme.GREEN -> requireContext().getColor(com.msgmates.app.R.color.primary_green)
                }
            )

            // Update font size in preview
            val fontSize = when (state.fontSize) {
                FontSize.SMALL -> 12f
                FontSize.MEDIUM -> 14f
                FontSize.LARGE -> 16f
            }
            tvPreviewText.textSize = fontSize
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class ColorTheme {
    MSGMATES_GRADIENT, BLUE, GREEN
}

enum class FontSize {
    SMALL, MEDIUM, LARGE
}

enum class BubbleDensity {
    TIGHT, MEDIUM, LOOSE
}
