package com.msgmates.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.msgmates.app.databinding.FragmentTermsOfServiceBinding

/**
 * Kullanıcı Koşulları ve Gizlilik Politikası sayfası
 * KVKK uyumlu kapsamlı veri işleme bilgileri
 */
class TermsOfServiceFragment : Fragment() {

    private var _binding: FragmentTermsOfServiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTermsOfServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        // Geri butonu - LoginFragment'a dön
        // Bu fragment sadece okuma amaçlı, geri dönüş LoginFragment'a
        // Navigation otomatik olarak LoginFragment'a dönecek

        // Debug: Fragment yüklendi mi kontrol et
        android.util.Log.d("TermsOfServiceFragment", "Fragment loaded successfully")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
