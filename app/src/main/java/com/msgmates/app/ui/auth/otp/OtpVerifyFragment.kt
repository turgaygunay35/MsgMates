package com.msgmates.app.ui.auth.otp

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
import androidx.navigation.fragment.navArgs
import com.msgmates.app.R
import com.msgmates.app.databinding.FragmentOtpVerifyBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * OTP doğrulama ekranı
 * Kullanıcıdan OTP kodu alır ve doğrular
 */
@AndroidEntryPoint
class OtpVerifyFragment : Fragment() {

    private var _binding: FragmentOtpVerifyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OtpVerifyViewModel by viewModels()
    private val args: OtpVerifyFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        
        // Telefon numarasını ViewModel'e gönder
        viewModel.init(args.phone)
    }

    private fun setupUI() {
        // Telefon numarasını göster
        binding.tvPhoneDisplay.text = formatPhoneNumber(args.phone)

        // OTP input listener
        binding.etOtp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onOtpChanged(s.toString())
            }
        })

        // Doğrula butonu
        binding.btnVerify.setOnClickListener {
            viewModel.verifyOtp()
        }

        // Kodu tekrar gönder
        binding.tvResendCode.setOnClickListener {
            viewModel.resendCode()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: OtpVerifyUiState) {
        // OTP input validasyonu
        binding.tilOtp.error = if (state.otp.isNotEmpty() && !state.isOtpValid) {
            getString(R.string.error_otp_invalid)
        } else null

        // Doğrula butonu durumu
        binding.btnVerify.isEnabled = state.isVerifyButtonEnabled

        // Loading durumu
        binding.progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.btnVerify.visibility = if (state.isLoading) View.GONE else View.VISIBLE

        // Geri sayım
        updateCountdown(state.countdownSeconds, state.canResend)

        // Kodu tekrar gönder butonu
        binding.tvResendCode.visibility = if (state.canResend) View.VISIBLE else View.GONE

        // Hata mesajı
        binding.tvError.visibility = if (state.errorMessage != null) View.VISIBLE else View.GONE
        state.errorMessage?.let { errorKey ->
            binding.tvError.text = getStringResource(errorKey)
        }

        // Doğrulama başarılı - Ana ekrana geç
        if (state.isVerificationSuccess) {
            navigateToMain()
        }
    }

    private fun updateCountdown(seconds: Int, canResend: Boolean) {
        if (canResend) {
            binding.tvCountdown.text = getString(R.string.otp_countdown_finished)
        } else {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            binding.tvCountdown.text = getString(
                R.string.otp_countdown,
                minutes,
                remainingSeconds
            )
        }
    }

    private fun formatPhoneNumber(phone: String): String {
        return if (phone.length == 11) {
            "${phone.substring(0, 3)} ${phone.substring(3, 6)} ${phone.substring(6, 8)} ${phone.substring(8, 11)}"
        } else {
            phone
        }
    }

    private fun navigateToMain() {
        // Auth graph'ından çık ve Main'e git
        findNavController().popBackStack(R.id.dest_main, false)
    }

    private fun getStringResource(key: String): String {
        return when (key) {
            "error_otp_invalid" -> getString(R.string.error_otp_invalid)
            "error_otp_expired" -> getString(R.string.error_otp_expired)
            "error_otp_too_many_attempts" -> getString(R.string.error_otp_too_many_attempts)
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