package com.msgmates.app.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.msgmates.app.R
import com.msgmates.app.core.navigation.SafeNavigation

class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Settings Card
        requireView().findViewById<View>(R.id.cardSettings).setOnClickListener {
            SafeNavigation.safeNavigate(this, R.id.dest_settings) {
                findNavController().navigate(R.id.dest_settings)
            }
        }

        // Disaster Mode Card
        requireView().findViewById<View>(R.id.cardDisaster).setOnClickListener {
            SafeNavigation.safeNavigate(this, R.id.dest_disaster_mode) {
                findNavController().navigate(R.id.dest_disaster_mode)
            }
        }

        // Web Card
        requireView().findViewById<View>(R.id.cardWeb).setOnClickListener {
            // TODO: Navigate to web fragment - dest_web not found in nav_graph
            // findNavController().navigate(R.id.dest_web)
        }

        // Capsule Card
        requireView().findViewById<View>(R.id.cardCapsule).setOnClickListener {
            // TODO: Navigate to capsule list fragment - dest_capsule not found in nav_graph
            // findNavController().navigate(R.id.dest_capsule)
        }

        // Notes Card
        requireView().findViewById<View>(R.id.cardNotes).setOnClickListener {
            // TODO: Navigate to quick notes fragment - dest_notes not found in nav_graph
            // findNavController().navigate(R.id.dest_notes)
        }
    }
}
