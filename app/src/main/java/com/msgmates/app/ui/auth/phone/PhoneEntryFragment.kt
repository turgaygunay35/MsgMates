package com.msgmates.app.ui.auth.phone

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.msgmates.app.R
import com.msgmates.app.databinding.FragmentPhoneEntryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Telefon numarası girişi ekranı
 * Kullanıcıdan telefon numarası alır ve doğrulama kodu ister
 */
@AndroidEntryPoint
class PhoneEntryFragment : Fragment() {

    private var _binding: FragmentPhoneEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PhoneEntryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Telefon numarası input listener
        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onPhoneChanged(s.toString())
            }
        })

        // Kullanıcı koşulları checkbox listener
        binding.cbTerms.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onTermsAccepted(isChecked)
        }

        // Kullanıcı koşulları metni tıklama
        binding.tvTerms.setOnClickListener {
            showTermsDialog()
        }

        // Gizlilik politikası tıklama
        binding.tvPrivacyPolicy.setOnClickListener {
            // Placeholder - gizlilik politikası ekranına yönlendir
        }

        // Kodu gönder butonu
        binding.btnSendCode.setOnClickListener {
            viewModel.sendCode()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: PhoneEntryUiState) {
        // Telefon input validasyonu
        binding.tilPhone.error = if (state.phone.isNotEmpty() && !state.isPhoneValid) {
            getString(R.string.error_invalid_tr_phone)
        } else null

        // Gönder butonu durumu
        binding.btnSendCode.isEnabled = state.isSendButtonEnabled

        // Loading durumu
        binding.progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.btnSendCode.visibility = if (state.isLoading) View.GONE else View.VISIBLE

        // Hata mesajı
        binding.tvError.visibility = if (state.errorMessage != null) View.VISIBLE else View.GONE
        state.errorMessage?.let { errorKey ->
            binding.tvError.text = getStringResource(errorKey)
        }

        // Kod gönderildi - OTP ekranına geç
        if (state.isCodeSent) {
            navigateToOtpVerification(state.phone)
        }
    }

    private fun showTermsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.terms_dialog_title)
            .setMessage(R.string.terms_dialog_content)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToOtpVerification(phone: String) {
        val action = PhoneEntryFragmentDirections.actionPhoneEntryToOtpVerify(phone)
        findNavController().navigate(action)
    }

    private fun getStringResource(key: String): String {
        return when (key) {
            "error_invalid_tr_phone" -> getString(R.string.error_invalid_tr_phone)
            "error_terms_required" -> getString(R.string.error_terms_required)
            "error_network" -> getString(R.string.error_network)
            "error_generic" -> getString(R.string.error_generic)
            else -> getString(R.string.error_generic)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}