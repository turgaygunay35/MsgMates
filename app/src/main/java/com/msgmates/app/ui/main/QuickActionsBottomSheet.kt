package com.msgmates.app.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.msgmates.app.databinding.BottomsheetQuickActionsBinding

class QuickActionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetQuickActionsBinding? = null
    private val binding get() = _binding!!

    private var onDailyShareClickListener: (() -> Unit)? = null
    private var onCreateGroupClickListener: (() -> Unit)? = null

    fun setOnDailyShareClickListener(listener: () -> Unit) {
        onDailyShareClickListener = listener
    }

    fun setOnCreateGroupClickListener(listener: () -> Unit) {
        onCreateGroupClickListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetQuickActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardDailyShare.setOnClickListener {
            onDailyShareClickListener?.invoke()
            dismiss()
        }

        binding.cardCreateGroup.setOnClickListener {
            onCreateGroupClickListener?.invoke()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): QuickActionsBottomSheet {
            return QuickActionsBottomSheet()
        }
    }
}
