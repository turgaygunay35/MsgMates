package com.msgmates.app.ui.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.msgmates.app.databinding.BottomSheetMessageOptionsBinding
import com.msgmates.app.domain.chats.Conversation

class MessageOptionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMessageOptionsBinding? = null
    private val binding get() = _binding!!

    private var conversation: Conversation? = null
    private var onMarkReadListener: (() -> Unit)? = null
    private var onMuteListener: (() -> Unit)? = null
    private var onStarListener: (() -> Unit)? = null
    private var onArchiveListener: (() -> Unit)? = null
    private var onDeleteListener: (() -> Unit)? = null

    companion object {
        private const val ARG_CONVERSATION = "conversation"

        fun newInstance(conversation: Conversation): MessageOptionsBottomSheet {
            val args = Bundle().apply {
                putSerializable(ARG_CONVERSATION, conversation)
            }
            return MessageOptionsBottomSheet().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        conversation = arguments?.getSerializable(ARG_CONVERSATION) as? Conversation
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMessageOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        updateUI()
    }

    private fun setupClickListeners() {
        binding.optionMarkRead.setOnClickListener {
            onMarkReadListener?.invoke()
            dismiss()
        }

        binding.optionMute.setOnClickListener {
            onMuteListener?.invoke()
            dismiss()
        }

        binding.optionStar.setOnClickListener {
            onStarListener?.invoke()
            dismiss()
        }

        binding.optionArchive.setOnClickListener {
            onArchiveListener?.invoke()
            dismiss()
        }

        binding.optionDelete.setOnClickListener {
            onDeleteListener?.invoke()
            dismiss()
        }
    }

    private fun updateUI() {
        conversation?.let { conv ->
            // Update UI based on conversation state
            // For example, show different text for mute/unmute
            val muteText = if (conv.isMuted) "Sessizden çıkar" else "Sessize al"
            // Find the TextView in the mute option layout
            val muteLayout = binding.optionMute
            val textView = muteLayout.getChildAt(1) as? android.widget.TextView
            textView?.text = muteText
        }
    }

    fun setOnMarkReadListener(listener: () -> Unit) {
        onMarkReadListener = listener
    }

    fun setOnMuteListener(listener: () -> Unit) {
        onMuteListener = listener
    }

    fun setOnStarListener(listener: () -> Unit) {
        onStarListener = listener
    }

    fun setOnArchiveListener(listener: () -> Unit) {
        onArchiveListener = listener
    }

    fun setOnDeleteListener(listener: () -> Unit) {
        onDeleteListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
