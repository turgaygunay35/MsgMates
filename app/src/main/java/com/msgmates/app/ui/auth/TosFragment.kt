package com.msgmates.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.msgmates.app.data.local.prefs.SessionPrefs
import com.msgmates.app.databinding.FragmentTosBinding
import kotlinx.coroutines.launch

class TosFragment : Fragment() {

    private var _b: FragmentTosBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentTosBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = SessionPrefs(requireContext())

        b.btnAccept.setOnClickListener {
            lifecycleScope.launch {
                prefs.setAcceptedTos(true)
                prefs.setLoggedIn(true)
                // Buradan ana ekrana veya Launcher'a dön
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}
