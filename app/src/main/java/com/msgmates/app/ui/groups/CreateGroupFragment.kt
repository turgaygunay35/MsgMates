package com.msgmates.app.ui.groups

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.databinding.FragmentCreateGroupBinding

class CreateGroupFragment : Fragment() {

    private var _binding: FragmentCreateGroupBinding? = null
    private val binding get() = _binding!!

    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var selectedMembersAdapter: SelectedMembersAdapter

    private val selectedMembers = mutableListOf<Contact>()
    private val allContacts = mutableListOf<Contact>()
    private val filteredContacts = mutableListOf<Contact>()

    companion object {
        private const val MAX_MEMBERS = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerViews()
        setupClickListeners()
        loadContacts()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerViews() {
        // Seçilen üyeler RecyclerView
        selectedMembersAdapter = SelectedMembersAdapter { contact ->
            removeSelectedMember(contact)
        }
        binding.rvSelectedMembers.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvSelectedMembers.adapter = selectedMembersAdapter

        // Kişiler RecyclerView
        contactsAdapter = ContactsAdapter { contact ->
            toggleMemberSelection(contact)
        }
        binding.rvContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContacts.adapter = contactsAdapter
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCreateGroup.setOnClickListener {
            createGroup()
        }

        // Arama
        binding.etSearchContacts.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterContacts(s.toString())
            }
        })
    }

    private fun loadContacts() {
        // TODO: Gerçek kişileri yükle (şimdilik mock data)
        allContacts.clear()
        allContacts.addAll(getMockContacts())
        filteredContacts.clear()
        filteredContacts.addAll(allContacts)
        contactsAdapter.submitList(filteredContacts)
    }

    private fun getMockContacts(): List<Contact> {
        return listOf(
            Contact("1", "Ahmet Yılmaz", "ahmet@msgmates.com", true),
            Contact("2", "Ayşe Demir", "ayse@msgmates.com", true),
            Contact("3", "Mehmet Kaya", "mehmet@msgmates.com", true),
            Contact("4", "Fatma Öz", "fatma@msgmates.com", true),
            Contact("5", "Ali Çelik", "ali@msgmates.com", true),
            Contact("6", "Zeynep Arslan", "zeynep@msgmates.com", true),
            Contact("7", "Mustafa Şahin", "mustafa@msgmates.com", true),
            Contact("8", "Elif Yıldız", "elif@msgmates.com", true),
            Contact("9", "Oğuz Türk", "oguz@msgmates.com", true),
            Contact("10", "Selin Ak", "selin@msgmates.com", true)
        )
    }

    internal fun filterContacts(query: String) {
        filteredContacts.clear()
        if (query.isBlank()) {
            filteredContacts.addAll(allContacts)
        } else {
            filteredContacts.addAll(
                allContacts.filter { contact ->
                    contact.name.contains(query, ignoreCase = true) ||
                        contact.email.contains(query, ignoreCase = true)
                }
            )
        }
        contactsAdapter.submitList(filteredContacts)
    }

    private fun toggleMemberSelection(contact: Contact) {
        if (selectedMembers.contains(contact)) {
            removeSelectedMember(contact)
        } else {
            addSelectedMember(contact)
        }
    }

    private fun addSelectedMember(contact: Contact) {
        if (selectedMembers.size >= MAX_MEMBERS) {
            Toast.makeText(requireContext(), "Maksimum $MAX_MEMBERS kişi seçebilirsiniz!", Toast.LENGTH_SHORT).show()
            return
        }

        selectedMembers.add(contact)
        updateUI()
    }

    private fun removeSelectedMember(contact: Contact) {
        selectedMembers.remove(contact)
        updateUI()
    }

    private fun updateUI() {
        // Üye sayısını güncelle
        binding.tvMemberCount.text = "${selectedMembers.size}/$MAX_MEMBERS"

        // Seçilen üyeler listesini güncelle
        selectedMembersAdapter.submitList(selectedMembers.toList())

        // Grup oluştur butonunu aktifleştir/devre dışı bırak
        val groupName = binding.etGroupName.text.toString().trim()
        binding.btnCreateGroup.isEnabled = groupName.isNotEmpty() && selectedMembers.isNotEmpty()
    }

    private fun createGroup() {
        val groupName = binding.etGroupName.text.toString().trim()
        val groupDescription = binding.etGroupDescription.text.toString().trim()

        if (groupName.isEmpty()) {
            Toast.makeText(requireContext(), "Grup adı gerekli!", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedMembers.isEmpty()) {
            Toast.makeText(requireContext(), "En az bir üye seçmelisiniz!", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Grubu sunucuya oluştur
        Toast.makeText(requireContext(), "Grup oluşturuldu! (Geliştirme aşamasında)", Toast.LENGTH_SHORT).show()

        // Geri dön
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Mock Contact data class
data class Contact(
    val id: String,
    val name: String,
    val email: String,
    val isMsgMatesUser: Boolean
)
