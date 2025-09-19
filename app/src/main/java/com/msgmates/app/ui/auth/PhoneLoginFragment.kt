package com.msgmates.app.ui.auth

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.msgmates.app.BuildConfig
import com.msgmates.app.data.local.prefs.SessionPrefs
import com.msgmates.app.databinding.FragmentPhoneLoginBinding
import com.msgmates.app.security.PhoneUtils
import kotlinx.coroutines.launch

class PhoneLoginFragment : Fragment() {

    private var _b: FragmentPhoneLoginBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentPhoneLoginBinding.inflate(inflater, container, false)

        // Debug build’de alfanumerik girişe izin ver
        if (BuildConfig.DEBUG) {
            b.editPhone.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }

        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Uzun basarak otomatik debug login
        b.btnSend.setOnLongClickListener {
            if (BuildConfig.ENABLE_PASSWORD_BYPASS) {
                b.editPhone.setText("+905551111111")
                b.btnSend.performClick()
                true
            } else {
                false
            }
        }

        b.btnSend.setOnClickListener {
            val raw = b.editPhone.text?.toString()?.trim().orEmpty()
            val e164 = PhoneUtils.normalizeOrNull(raw, "TR")
            if (e164 == null) {
                b.editPhone.error = "Geçersiz telefon"
                return@setOnClickListener
            }
            val prefs = SessionPrefs(requireContext())
            lifecycleScope.launch {
                // Normal akış: phone’u kaydet
                prefs.setPhoneE164(e164)
                // Buradan OTP ekranına ya da TOS’a yönlendirme yapılabilir
            }
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
