package com.msgmates.app.ui.disaster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.R
import com.msgmates.app.config.Constants

class SosMessagesDialog : DialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SosMessagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_sos_messages, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewSos)
        adapter = SosMessagesAdapter(Constants.SOS_MESSAGES) { message ->
            // Send SOS message
            sendSosMessage(message)
            dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        return view
    }

    @Suppress("UNUSED_PARAMETER")
    private fun sendSosMessage(_ignored: String) {
        // Implement SOS message sending
        // This would send the message via BLE and/or network
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
