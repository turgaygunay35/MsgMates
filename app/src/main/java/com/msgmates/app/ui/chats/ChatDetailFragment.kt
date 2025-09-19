package com.msgmates.app.ui.chats

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.R
import com.msgmates.app.core.analytics.EventLogger
import com.msgmates.app.core.extensions.toast
import com.msgmates.app.core.messaging.model.LocalAttachment
import com.msgmates.app.core.notifications.MessageNotificationManager
import com.msgmates.app.core.upload.AttachmentUploader
import com.msgmates.app.databinding.FragmentChatDetailBinding
import com.msgmates.app.ui.call.CallViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Chat Detail screen showing individual conversation with call functionality,
 * presence status, and disaster mode integration.
 */
@AndroidEntryPoint
class ChatDetailFragment : Fragment() {
    private var _binding: FragmentChatDetailBinding? = null
    internal val binding get() = _binding!!

    internal val viewModel: ChatDetailViewModel by viewModels()
    private val callViewModel: CallViewModel by viewModels()
    // COMMENTED OUT FOR CLEAN BUILD
    // private val args: ChatDetailFragmentArgs by navArgs()

    // MessageNotificationManager'ı inject et
    private lateinit var messageNotificationManager: MessageNotificationManager

    private lateinit var messagesAdapter: MessagesAdapter

    // File picker
    @Inject
    lateinit var attachmentUploader: AttachmentUploader

    @Inject
    lateinit var attachmentDownloader: com.msgmates.app.core.download.AttachmentDownloader

    @Inject
    lateinit var audioRecorder: com.msgmates.app.core.audio.AudioRecorder

    @Inject
    lateinit var audioPlayer: com.msgmates.app.core.audio.AudioPlayer

    @Inject
    lateinit var typingManager: com.msgmates.app.core.typing.TypingManager
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Request persistable permission for scoped storage
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    android.util.Log.w("FilePicker", "Could not take persistable permission", e)
                }
                handleFileSelection(uri)
            }
        }
    }

    private var isMuted = false

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // MessageNotificationManager'ı initialize et
            messageNotificationManager = com.msgmates.app.core.notifications.MessageNotificationManager()

            setupRecyclerView()
            setupMenu()
            setupClickListeners()
            setupCallButtons()
            observeViewModel()
        } catch (t: Throwable) {
            android.util.Log.e("CrashGuard", "ChatDetailFragment onViewCreated failed", t)
            toast("Beklenmeyen bir hata oluştu")
        }
    }

    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter(
            onAttachmentClick = { remoteUrl, localUri, mimeType ->
                handleAttachmentClick(remoteUrl, localUri, mimeType)
            },
            onAudioPlayClick = { remoteUrl, localUri ->
                handleAudioPlayClick(remoteUrl, localUri)
            }
        )
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMessages.adapter = messagesAdapter
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_chat_detail_topappbar, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_video_call -> {
                            EventLogger.logCallStart("video")
                            viewModel.startFakeCall()
                            true
                        }
                        R.id.action_voice_call -> {
                            EventLogger.logCallStart("voice")
                            viewModel.startFakeCall()
                            true
                        }
                        R.id.action_more -> {
                            EventLogger.logMenuOpen("chat_detail_overflow")
                            // TODO: Show overflow menu
                            true
                        }
                        R.id.menu_view_profile -> {
                            EventLogger.logMenuOpen("view_profile")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.profileFragment)
                            true
                        }
                        R.id.menu_search_in_chat -> {
                            EventLogger.logMenuOpen("search_in_chat")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.chatSearchFragment)
                            true
                        }
                        R.id.menu_media_files -> {
                            EventLogger.logMenuOpen("media_files")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.mediaFilesFragment)
                            true
                        }
                        R.id.menu_mute_toggle -> {
                            isMuted = !isMuted
                            EventLogger.log("mute_toggle", mapOf("muted" to isMuted))
                            val message = if (isMuted) getString(R.string.muted) else getString(R.string.unmuted)
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.menu_disappearing -> {
                            EventLogger.logMenuOpen("disappearing")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.disappearingSettingsFragment)
                            true
                        }
                        R.id.menu_chat_theme -> {
                            EventLogger.logMenuOpen("chat_theme")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.chatThemeFragment)
                            true
                        }
                        R.id.menu_report -> {
                            EventLogger.logMenuOpen("report")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.reportFragment)
                            true
                        }
                        R.id.menu_block -> {
                            EventLogger.logMenuOpen("block")
                            // COMMENTED OUT FOR CLEAN BUILD
                            // findNavController().navigate(R.id.blockFragment)
                            true
                        }
                        R.id.action_disaster_message -> {
                            // Afet mesajı gönder
                            Toast.makeText(requireContext(), "Afet mesajı gönderildi", Toast.LENGTH_SHORT).show()
                            EventLogger.log("disaster_message_sent")
                            true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner, Lifecycle.State.RESUMED
        )
    }

    private fun setupClickListeners() {
        // TODO: Set navigation click listener when toolbar is properly initialized

        // Status capsule click listener
        binding.root.findViewById<View>(R.id.status_capsule_detail)?.setOnClickListener {
            Toast.makeText(requireContext(), "Durum kapsülü tıklandı", Toast.LENGTH_SHORT).show()
        }

        // Message composer click listeners
        binding.composer.btnSend.setOnClickListener {
            val text = binding.composer.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendText(text)
                binding.composer.etMessage.text?.clear()
            }
        }

        binding.composer.btnAttach.setOnClickListener {
            openFilePicker()
        }

        // Microphone button - push to talk
        binding.composer.btnMic.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    startVoiceRecording()
                    true
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    stopVoiceRecording()
                    true
                }
                else -> false
            }
        }

        // Enable/disable send button based on text input
        binding.composer.etMessage.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Handle typing indicator
                if (s.isNullOrBlank()) {
                    typingManager.stopTyping { isTyping ->
                        // TODO: Send typing status via WS
                        android.util.Log.d("Typing", "Typing: $isTyping")
                    }
                } else {
                    typingManager.startTyping(
                        scope = lifecycleScope,
                        conversationId = "temp_conversation_id" // COMMENTED OUT FOR CLEAN BUILD
                        // conversationId = args.conversationId
                    ) { isTyping ->
                        // TODO: Send typing status via WS
                        android.util.Log.d("Typing", "Typing: $isTyping")
                    }
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.composer.btnSend.isEnabled = !s.isNullOrBlank()
            }
        })
    }

    private fun setupCallButtons() {
        // TODO: Implement call buttons when toolbar is properly initialized
        // For now, we'll use the menu items
    }

    private fun startCall(isVideo: Boolean) {
        val permissions = if (isVideo) {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }

        if (hasPermissions(permissions)) {
            performCall(isVideo)
        } else {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE)
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun performCall(isVideo: Boolean) {
        val calleeId = "temp_conversation_id" // COMMENTED OUT FOR CLEAN BUILD
        // val calleeId = args.conversationId
        val calleeName = "Kullanıcı" // TODO: Get from conversation data
        val calleeAvatar = null // TODO: Get from conversation data

        callViewModel.startOutgoingCall(calleeId, calleeName, calleeAvatar, isVideo)

        // Navigate to outgoing call
        val bundle = Bundle().apply {
            putString("calleeId", calleeId)
            putString("calleeName", calleeName)
            putString("calleeAvatar", calleeAvatar)
            putBoolean("isVideo", isVideo)
        }
        findNavController().navigate(R.id.dest_outgoing_call, bundle)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allGranted) {
                // Permissions granted, retry call
                val isVideo = permissions.contains(Manifest.permission.CAMERA)
                performCall(isVideo)
            } else {
                // Show permission denied dialog
                showPermissionDeniedDialog(permissions)
            }
        }
    }

    private fun showPermissionDeniedDialog(permissions: Array<out String>) {
        val isVideo = permissions.contains(Manifest.permission.CAMERA)
        val title = if (isVideo) "Kamera İzni Gerekli" else "Mikrofon İzni Gerekli"
        val message = if (isVideo) {
            "Görüntülü arama yapabilmek için kamera iznine ihtiyacımız var. Lütfen ayarlardan izin verin."
        } else {
            "Sesli arama yapabilmek için mikrofon iznine ihtiyacımız var. Lütfen ayarlardan izin verin."
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ayarlara Git") { _, _ ->
                // Open app settings
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun observeViewModel() {
        // Initialize viewModel with conversation ID
        viewModel.initialize("temp_conversation_id") // COMMENTED OUT FOR CLEAN BUILD
        // viewModel.initialize(args.conversationId)

        // Observe messages
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                messagesAdapter.submitList(messages) {
                    // Auto-scroll to bottom when new messages arrive
                    if (messages.isNotEmpty()) {
                        binding.rvMessages.scrollToPosition(messages.size - 1)
                    }
                }

                // Acknowledge delivered for visible messages
                val visibleMessageIds = messages.filter { it.status == "sent" }.map { it.id }
                if (visibleMessageIds.isNotEmpty()) {
                    viewModel.ackDelivered(visibleMessageIds)
                }
            }
        }

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.composer.btnSend.isEnabled = !isLoading
                if (isLoading) {
                    binding.composer.btnSend.alpha = 0.5f
                } else {
                    binding.composer.btnSend.alpha = 1.0f
                }
            }
        }

        // Observe errors
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }

        // Observe title
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.title.collect { title ->
                binding.root.findViewById<android.widget.TextView>(R.id.tv_title)?.text = title
            }
        }

        // Observe subtitle (presence)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.subtitle.collect { subtitle ->
                binding.root.findViewById<android.widget.TextView>(R.id.tv_subtitle)?.text = subtitle
            }
        }

        // Observe call active status
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.callActive.collect { callActive ->
                val statusCapsule = binding.root.findViewById<View>(R.id.status_capsule_detail)
                if (callActive != null) {
                    statusCapsule?.visibility = View.VISIBLE
                    statusCapsule?.setBackgroundResource(R.drawable.bg_call_capsule)
                } else {
                    statusCapsule?.visibility = View.GONE
                }
            }
        }

        // Observe call duration
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.callDuration.collect { duration ->
                val callActive = viewModel.callActive.value
                if (callActive != null) {
                    val durationText = viewModel.formatCallDuration(duration)
                    val statusCapsule = binding.root.findViewById<View>(R.id.status_capsule_detail)
                    val textView = statusCapsule?.findViewById<android.widget.TextView>(R.id.tv_status_text)
                    textView?.text = "${getString(R.string.call_active)} — $durationText"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Sohbet detayı görünürken okundu olarak işaretle
        markConversationAsRead()

        // Acknowledge read for all visible messages
        val visibleMessageIds = messagesAdapter.currentList.filter { it.status == "delivered" }.map { it.id }
        if (visibleMessageIds.isNotEmpty()) {
            viewModel.ackRead(visibleMessageIds)
        }
    }

    private fun markConversationAsRead() {
        val conversationId = "temp_conversation_id" // COMMENTED OUT FOR CLEAN BUILD
        // val conversationId = args.conversationId

        lifecycleScope.launch {
            try {
                messageNotificationManager.markConversationRead(conversationId)
                EventLogger.log("conversation_marked_read", mapOf("conversation_id" to conversationId))
            } catch (e: Exception) {
                android.util.Log.e("ChatDetailFragment", "Error marking conversation as read", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // File picker methods
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        filePickerLauncher.launch(intent)
    }

    private fun handleFileSelection(uri: Uri) {
        lifecycleScope.launch {
            try {
                val fileName = getFileName(uri)
                val mimeType = requireContext().contentResolver.getType(uri)
                val kind = getAttachmentKind(mimeType)

                // Show progress
                Toast.makeText(requireContext(), "Dosya yükleniyor...", Toast.LENGTH_SHORT).show()

                // Upload file
                val uploadResponse = attachmentUploader.uploadLocal(
                    uri = uri,
                    kind = kind,
                    convoId = "temp_conversation_id", // COMMENTED OUT FOR CLEAN BUILD
                    // convoId = args.conversationId,
                    onProgress = { progress ->
                        // Update progress UI if needed
                        android.util.Log.d("Upload", "Progress: $progress%")
                    }
                )

                // Create LocalAttachment
                val attachment = LocalAttachment(
                    kind = kind,
                    mime = mimeType ?: "application/octet-stream",
                    size = getFileSize(uri),
                    width = null,
                    height = null,
                    durationMs = null,
                    localUri = uri,
                    fileName = fileName,
                    thumbB64 = null
                )

                // Send message with attachment
                viewModel.sendWithAttachments(null, listOf(attachment))

                Toast.makeText(requireContext(), "Dosya yüklendi!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.util.Log.e("FileUpload", "Upload failed", e)
                Toast.makeText(requireContext(), "Dosya yüklenemedi: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName ?: "unknown_file"
    }

    private fun getAttachmentKind(mimeType: String?): String {
        return when {
            mimeType?.startsWith("image/") == true -> "image"
            mimeType?.startsWith("video/") == true -> "video"
            mimeType?.startsWith("audio/") == true -> "audio"
            else -> "file"
        }
    }

    private fun getFileSize(uri: Uri): Long? {
        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.available().toLong()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun handleAttachmentClick(remoteUrl: String, localUri: String, mimeType: String) {
        if (remoteUrl.isNotEmpty()) {
            // Download from remote URL
            lifecycleScope.launch {
                try {
                    val fileName = "attachment_${System.currentTimeMillis()}"
                    val downloadedUri = attachmentDownloader.downloadAttachment(
                        remoteUrl = remoteUrl,
                        fileName = fileName,
                        mimeType = mimeType,
                        onProgress = { progress ->
                            android.util.Log.d("Download", "Progress: $progress%")
                        }
                    )

                    if (downloadedUri != null) {
                        Toast.makeText(requireContext(), "Dosya indirildi!", Toast.LENGTH_SHORT).show()
                        // Open the downloaded file
                        openFile(downloadedUri)
                    } else {
                        Toast.makeText(requireContext(), "Dosya indirilemedi", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("Download", "Download failed", e)
                    Toast.makeText(requireContext(), "İndirme hatası: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else if (localUri.isNotEmpty()) {
            // Open local file
            val uri = Uri.parse(localUri)
            openFile(uri)
        }
    }

    private fun openFile(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, requireContext().contentResolver.getType(uri))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("OpenFile", "Failed to open file", e)
            Toast.makeText(requireContext(), "Dosya açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    // Voice recording methods
    private fun startVoiceRecording() {
        // Check permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
            return
        }

        lifecycleScope.launch {
            try {
                val result = audioRecorder.startRecording()
                if (result.isSuccess) {
                    // Update UI to show recording state
                    binding.composer.btnMic.setImageResource(R.drawable.ic_mic_recording)
                    binding.composer.etMessage.hint = getString(R.string.recording)
                    Toast.makeText(requireContext(), "Kayıt başladı", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Kayıt başlatılamadı: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("VoiceRecording", "Failed to start recording", e)
                Toast.makeText(requireContext(), "Kayıt hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopVoiceRecording() {
        lifecycleScope.launch {
            try {
                val result = audioRecorder.stopRecording()
                if (result.isSuccess) {
                    val audioUri = result.getOrNull()
                    if (audioUri != null) {
                        // Upload and send audio
                        sendVoiceMessage(audioUri)
                    } else {
                        Toast.makeText(requireContext(), "Kayıt çok kısa", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Kayıt durdurulamadı: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Reset UI
                binding.composer.btnMic.setImageResource(R.drawable.ic_mic)
                binding.composer.etMessage.hint = getString(R.string.type_message)
            } catch (e: Exception) {
                android.util.Log.e("VoiceRecording", "Failed to stop recording", e)
                Toast.makeText(requireContext(), "Kayıt hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendVoiceMessage(audioUri: Uri) {
        lifecycleScope.launch {
            try {
                val fileName = "voice_note_${System.currentTimeMillis()}.m4a"

                // Upload audio file
                val uploadResponse = attachmentUploader.uploadLocal(
                    uri = audioUri,
                    kind = "audio",
                    convoId = "temp_conversation_id", // COMMENTED OUT FOR CLEAN BUILD
                    // convoId = args.conversationId,
                    onProgress = { progress ->
                        android.util.Log.d("VoiceUpload", "Progress: $progress%")
                    }
                )

                // Create audio attachment
                val attachment = LocalAttachment(
                    kind = "audio",
                    mime = "audio/mp4",
                    size = getFileSize(audioUri),
                    width = null,
                    height = null,
                    durationMs = null, // TODO: Calculate duration
                    localUri = audioUri,
                    fileName = fileName,
                    thumbB64 = null
                )

                // Send voice message
                viewModel.sendWithAttachments(null, listOf(attachment))

                Toast.makeText(requireContext(), "Sesli not gönderildi!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.util.Log.e("VoiceMessage", "Failed to send voice message", e)
                Toast.makeText(requireContext(), "Sesli not gönderilemedi: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleAudioPlayClick(remoteUrl: String, localUri: String) {
        val audioUri = if (localUri.isNotEmpty()) {
            Uri.parse(localUri)
        } else if (remoteUrl.isNotEmpty()) {
            Uri.parse(remoteUrl)
        } else {
            return
        }

        try {
            if (audioPlayer.isCurrentlyPlaying(audioUri)) {
                // If currently playing this audio, pause it
                audioPlayer.pauseAudio()
            } else {
                // Stop any currently playing audio and play this one
                audioPlayer.stopAudio()
                audioPlayer.playAudio(audioUri)
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioPlay", "Failed to play audio", e)
            Toast.makeText(requireContext(), "Ses çalınamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
