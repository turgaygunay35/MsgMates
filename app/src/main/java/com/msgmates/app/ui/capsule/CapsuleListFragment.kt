package com.msgmates.app.ui.capsule

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.msgmates.app.R

class CapsuleListFragment : Fragment(R.layout.fragment_capsule_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun onItemClick(id: String) {
            val b = bundleOf("capsuleId" to id)
            // COMMENTED OUT FOR CLEAN BUILD
            // findNavController().navigate(R.id.capsuleDetailFragment, b)
        }

        fun onAddClick() {
            // COMMENTED OUT FOR CLEAN BUILD
            // findNavController().navigate(R.id.dest_caps_edit)
        }
    }
}
