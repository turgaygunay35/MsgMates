package com.msgmates.app.ui.groups.wizard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.msgmates.app.R
import com.msgmates.app.databinding.FragmentGroupWizardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupWizardFragment : Fragment() {

    private var _binding: FragmentGroupWizardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupWizardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        navigateToFirstStep()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun navigateToFirstStep() {
        // Navigate to step 1 - Select Members
        findNavController().navigate(R.id.dest_step_select_members)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
