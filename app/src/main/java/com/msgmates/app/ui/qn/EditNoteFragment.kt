package com.msgmates.app.ui.qn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.msgmates.app.databinding.FragmentEditNoteBinding

class EditNoteFragment : Fragment() {

    private var _b: FragmentEditNoteBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentEditNoteBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val n: String = arguments?.getString("title") ?: ""
        val m: String = arguments?.getString("body") ?: ""

        // Elvis gereksizdi; setText doğrudan non-null String alıyor
        b.etTitle.setText(n)
        b.etBody.setText(m)
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
