package com.msgmates.app.ui.capsule

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.msgmates.app.R

class CapsuleDetailFragment : Fragment(R.layout.fragment_capsule_detail) {

    private val capsuleId: String by lazy { arguments?.getString("capsuleId").orEmpty() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: capsuleId ile UI doldur
    }
}
