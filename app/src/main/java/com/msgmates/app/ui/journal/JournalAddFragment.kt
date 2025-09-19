package com.msgmates.app.ui.journal

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.msgmates.app.databinding.FragmentJournalAddBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JournalAddFragment : Fragment() {

    private var _binding: FragmentJournalAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JournalViewModel by viewModels()

    private var selectedDuration = 24 // Default 24 saat
    private var selectedMediaUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedMediaUri = it
            binding.ivPreview.setImageURI(it)
            binding.ivPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDurationChips()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupDurationChips() {
        val durations = listOf(6, 12, 18, 24)

        durations.forEach { duration ->
            val chip = Chip(requireContext()).apply {
                text = "$duration saat"
                isCheckable = true
                isChecked = duration == selectedDuration
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedDuration = duration
                        // Diğer chip'leri uncheck et
                        for (i in 0 until binding.chipGroupDuration.childCount) {
                            val child = binding.chipGroupDuration.getChildAt(i)
                            if (child is Chip && child != this) {
                                child.isChecked = false
                            }
                        }
                    }
                }
            }
            binding.chipGroupDuration.addView(chip)
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectMedia.setOnClickListener {
            // Galeri aç
            galleryLauncher.launch("image/*")
        }

        binding.btnPublish.setOnClickListener {
            publishStory()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Loading state'i yönet
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.btnPublish.isEnabled = !state.isLoading
            }
        }
    }

    private fun publishStory() {
        val description = binding.etDescription.text.toString().trim()

        if (selectedMediaUri == null) {
            // Hata: Medya seçilmemiş
            return
        }

        // Mock story ekleme
        viewModel.addStory(
            type = com.msgmates.app.data.journal.model.JournalType.PHOTO,
            contentUrl = selectedMediaUri.toString(),
            textContent = description,
            durationHours = selectedDuration
        )

        // Başarılı mesajı göster ve geri dön
        android.widget.Toast.makeText(requireContext(), "Hikaye paylaşıldı!", android.widget.Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
