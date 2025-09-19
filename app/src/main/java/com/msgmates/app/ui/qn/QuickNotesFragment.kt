package com.msgmates.app.ui.qn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.msgmates.app.databinding.FragmentBlankBinding

class QuickNotesFragment : Fragment() {
    private var _b: FragmentBlankBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _b = FragmentBlankBinding.inflate(inflater, container, false)
        return _b!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { b.title.text = "Kısa Notlar" }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
