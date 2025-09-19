package com.msgmates.app.ui.groups.wizard.steps

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
import com.msgmates.app.R
import com.msgmates.app.databinding.FragmentStepGroupMetaBinding
import com.msgmates.app.domain.groups.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StepGroupMetaFragment : Fragment() {

    private var _binding: FragmentStepGroupMetaBinding? = null
    private val binding get() = _binding!!

    internal val viewModel: StepGroupMetaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepGroupMetaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupClickListeners()
        setupTextWatchers()
        observeViewModel()
        loadSelectedUsers()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnNext.setOnClickListener {
            navigateToNextStep()
        }

        binding.btnChangeAvatar.setOnClickListener {
            // TODO: Open image picker
            // For now, just set a placeholder
            viewModel.updateAvatarUri("content://placeholder/group_avatar")
        }
    }

    private fun setupTextWatchers() {
        binding.etGroupName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateGroupName(s.toString())
            }
        })

        binding.etGroupDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateGroupDescription(s.toString())
            }
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isValid.collect { isValid ->
                binding.btnNext.isEnabled = isValid
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // TODO: Show loading indicator
            }
        }
    }

    private fun loadSelectedUsers() {
        val selectedUserIds = arguments?.getStringArray("selectedUserIds")
        if (selectedUserIds != null) {
            // TODO: Load users from repository based on IDs
            // For now, create dummy users
            val dummyUsers = selectedUserIds.map { id ->
                User(
                    id = id,
                    name = "User $id",
                    phoneNumber = "+90 555 123 45 67",
                    avatarUrl = null,
                    isOnline = false,
                    lastSeen = null
                )
            }
            viewModel.setSelectedUsers(dummyUsers)
        }
    }

    private fun navigateToNextStep() {
        val groupData = viewModel.getGroupData()
        val bundle = Bundle().apply {
            putString("groupName", groupData.name)
            putString("groupDescription", groupData.description)
            putString("avatarUri", groupData.avatarUri)
            putStringArray("selectedUserIds", groupData.selectedUsers.map { it.id }.toTypedArray())
        }
        findNavController().navigate(R.id.dest_step_review_create, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
