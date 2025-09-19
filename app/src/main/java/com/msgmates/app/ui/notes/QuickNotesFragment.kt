package com.msgmates.app.ui.notes

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.R
import com.msgmates.app.data.repository.QuickNotesRepository
import kotlinx.coroutines.launch

class QuickNotesFragment : Fragment(R.layout.fragment_quick_notes) {

    private lateinit var repo: QuickNotesRepository
    private lateinit var adapter: QuickNotesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = QuickNotesRepository(requireContext())
        adapter = QuickNotesAdapter()

        val rv = view.findViewById<RecyclerView>(R.id.rv)
        val btnAdd = view.findViewById<View>(R.id.btnAdd)
        val etNote = view.findViewById<EditText>(R.id.etNote)
        val empty = view.findViewById<TextView>(R.id.empty)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            repo.notes.collect { list ->
                adapter.submitList(list)
                empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        btnAdd.setOnClickListener {
            val text = etNote.text?.toString()?.trim().orEmpty()
            if (text.isNotEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val note = com.msgmates.app.domain.model.QuickNote(
                        id = System.currentTimeMillis().toString(),
                        title = text.take(50), // İlk 50 karakter başlık
                        body = text,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    repo.add(note)
                    etNote.setText("")
                }
            }
        }
    }
}
