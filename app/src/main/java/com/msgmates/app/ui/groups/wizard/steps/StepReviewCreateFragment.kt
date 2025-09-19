package com.msgmates.app.ui.groups.wizard.steps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.R
import com.msgmates.app.databinding.FragmentStepReviewCreateBinding
import com.msgmates.app.domain.groups.User
import com.msgmates.app.ui.groups.wizard.adapters.MemberChipAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StepReviewCreateFragment : Fragment() {

    private var _binding: FragmentStepReviewCreateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StepReviewCreateViewModel by viewModels()

    private lateinit var memberAdapter: MemberChipAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepReviewCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadGroupData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        memberAdapter = MemberChipAdapter()
        binding.rvMembers.apply {
            adapter = memberAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCreate.setOnClickListener {
            viewModel.createGroup()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.groupName.collect { name ->
                binding.tvGroupName.text = name
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.groupDescription.collect { description ->
                if (description != null && description.isNotEmpty()) {
                    binding.tvGroupDescription.text = description
                    binding.tvGroupDescription.visibility = View.VISIBLE
                } else {
                    binding.tvGroupDescription.visibility = View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedUsers.collect { users ->
                memberAdapter.submitList(users)
                binding.tvMemberCount.text = "${users.size} üye"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isCreating.collect { isCreating ->
                binding.btnCreate.isEnabled = !isCreating
                binding.btnCreate.text = if (isCreating) "Oluşturuluyor..." else "Oluştur"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.creationResult.collect { result ->
                when (result) {
                    is CreationResult.Success -> {
                        // Navigate to chat detail
                        val bundle = Bundle().apply {
                            putString("conversationId", result.groupId)
                        }
                        findNavController().navigate(R.id.action_group_wizard_to_chat_detail, bundle)
                    }
                    is CreationResult.Error -> {
                        // Show error message
                        // TODO: Show error dialog
                    }
                    null -> {
                        // No result yet
                    }
                }
            }
        }
    }

    private fun loadGroupData() {
        val groupName = arguments?.getString("groupName") ?: ""
        val groupDescription = arguments?.getString("groupDescription")
        val avatarUri = arguments?.getString("avatarUri")
        val selectedUserIds = arguments?.getStringArray("selectedUserIds") ?: emptyArray()

        // Create dummy users for now
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

        viewModel.setGroupData(groupName, groupDescription, avatarUri, dummyUsers)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
