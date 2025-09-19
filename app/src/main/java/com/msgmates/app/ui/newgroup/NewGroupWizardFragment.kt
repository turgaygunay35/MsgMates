package com.msgmates.app.ui.newgroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.msgmates.app.databinding.FragmentPlaceholderSimpleBinding

class NewGroupWizardFragment : Fragment() {

    private var _binding: FragmentPlaceholderSimpleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceholderSimpleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = "Yeni Grup"
        binding.tvBody.text = "Yeni grup oluşturma sihirbazı yakında burada olacak."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
