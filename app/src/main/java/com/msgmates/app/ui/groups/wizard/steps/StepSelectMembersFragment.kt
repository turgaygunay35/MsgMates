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
import com.msgmates.app.databinding.FragmentStepSelectMembersBinding
// import com.msgmates.app.ui.groups.wizard.adapters.UserSelectionAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StepSelectMembersFragment : Fragment() {

    private var _binding: FragmentStepSelectMembersBinding? = null
    private val binding get() = _binding!!

    internal val viewModel: StepSelectMembersViewModel by viewModels()

    // private lateinit var userAdapter: UserSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepSelectMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        // userAdapter = UserSelectionAdapter(
        //     onUserClick = { user ->
        //         viewModel.toggleUserSelection(user)
        //     }
        // )

        // binding.rvUsers.apply {
        //     adapter = userAdapter
        //     layoutManager = LinearLayoutManager(requireContext())
        // }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateSearchQuery(s.toString())
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            navigateToNextStep()
        }
    }

    private fun observeViewModel() {
        // viewLifecycleOwner.lifecycleScope.launch {
        //     viewModel.users.collect { users ->
        //         userAdapter.submitList(users)
        //     }
        // }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedCount.collect { count ->
                binding.tvSelectedCount.text = "$count kişi seçildi"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.canProceed.collect { canProceed ->
                binding.btnNext.isEnabled = canProceed
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // TODO: Show loading indicator
            }
        }
    }

    private fun navigateToNextStep() {
        val selectedUsers = viewModel.getSelectedUsers()
        if (selectedUsers.isNotEmpty()) {
            // Pass selected users to next step
            val bundle = Bundle().apply {
                putStringArray("selectedUserIds", selectedUsers.map { it.id }.toTypedArray())
            }
            findNavController().navigate(R.id.dest_step_group_meta, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
