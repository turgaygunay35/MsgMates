package com.msgmates.app.ui.settings.profile

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msgmates.app.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    internal val viewModel: ProfileViewModel by viewModels()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateProfilePhoto(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
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
            // Username input with validation
            etUsername.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    viewModel.updateUsername(s.toString())
                }
            })

            // Status message input with character limit
            etStatusMessage.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString()
                    viewModel.updateStatusMessage(text)

                    // Update character count
                    tvCharCount.text = "${text.length}/150"
                    tvCharCount.setTextColor(
                        if (text.length > 150) {
                            requireContext().getColor(android.R.color.holo_red_dark)
                        } else {
                            requireContext().getColor(android.R.color.darker_gray)
                        }
                    )
                }
            })

            // Profile photo change
            btnChangePhoto.setOnClickListener {
                imagePickerLauncher.launch("image/*")
            }

            // Profile photo delete
            btnDeletePhoto.setOnClickListener {
                viewModel.deleteProfilePhoto()
            }

            // QR code share
            btnQrShare.setOnClickListener {
                findNavController().navigate(ProfileFragmentDirections.actionProfileToQrShare())
            }

            // Share settings
            btnShareSettings.setOnClickListener {
                findNavController().navigate(ProfileFragmentDirections.actionProfileToShareSettings())
            }

            // Save button
            btnSave.setOnClickListener {
                viewModel.saveProfile()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.apply {
                    // Update fields
                    etUsername.setText(state.username)
                    etStatusMessage.setText(state.statusMessage)
                    etPhone.setText(state.phone)

                    // Update character count
                    tvCharCount.text = "${state.statusMessage.length}/150"
                    tvCharCount.setTextColor(
                        if (state.statusMessage.length > 150) {
                            requireContext().getColor(android.R.color.holo_red_dark)
                        } else {
                            requireContext().getColor(android.R.color.darker_gray)
                        }
                    )

                    // Update profile photo
                    if (state.profilePhotoUri != null) {
                        ivProfilePhoto.setImageURI(state.profilePhotoUri)
                        btnDeletePhoto.visibility = View.VISIBLE
                    } else {
                        ivProfilePhoto.setImageResource(com.msgmates.app.R.drawable.ic_profile_placeholder)
                        btnDeletePhoto.visibility = View.GONE
                    }

                    // Username validation
                    if (state.usernameError != null) {
                        tilUsername.error = state.usernameError
                    } else {
                        tilUsername.error = null
                    }

                    // Status message validation
                    if (state.statusMessageError != null) {
                        tilStatusMessage.error = state.statusMessageError
                    } else {
                        tilStatusMessage.error = null
                    }

                    // Loading state
                    if (state.isLoading) {
                        progressBar.visibility = View.VISIBLE
                        btnSave.isEnabled = false
                    } else {
                        progressBar.visibility = View.GONE
                        btnSave.isEnabled = true
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
