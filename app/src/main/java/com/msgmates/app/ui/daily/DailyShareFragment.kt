package com.msgmates.app.ui.daily

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.msgmates.app.databinding.FragmentDailyShareBinding
import java.util.*

class DailyShareFragment : Fragment() {

    private var _binding: FragmentDailyShareBinding? = null
    private val binding get() = _binding!!

    private var selectedVideoUri: Uri? = null
    private var videoDuration: Long = 0
    private var videoSize: Long = 0

    companion object {
        private const val REQUEST_VIDEO_PICK = 1001
        private const val MAX_DURATION_SECONDS = 30
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyShareBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardVideoSelection.setOnClickListener {
            selectVideo()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnShare.setOnClickListener {
            shareVideo()
        }
    }

    private fun selectVideo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        startActivityForResult(intent, REQUEST_VIDEO_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_VIDEO_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                processSelectedVideo(uri)
            }
        }
    }

    private fun processSelectedVideo(uri: Uri) {
        selectedVideoUri = uri

        // Video süresini kontrol et
        val duration = getVideoDuration(uri)
        if (duration > MAX_DURATION_SECONDS * 1000) { // milliseconds
            Toast.makeText(requireContext(), "Video süresi 30 saniyeyi geçemez!", Toast.LENGTH_SHORT).show()
            return
        }

        videoDuration = duration
        videoSize = getVideoSize(uri)

        // UI'yi güncelle
        updateVideoInfo()

        // Paylaş butonunu aktifleştir
        binding.btnShare.isEnabled = true
    }

    private fun getVideoDuration(uri: Uri): Long {
        return try {
            val cursor = requireContext().contentResolver.query(
                uri,
                arrayOf(MediaStore.Video.Media.DURATION),
                null,
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    it.getLong(0)
                } else {
                    0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun getVideoSize(uri: Uri): Long {
        return try {
            val cursor = requireContext().contentResolver.query(
                uri,
                arrayOf(MediaStore.Video.Media.SIZE),
                null,
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    it.getLong(0)
                } else {
                    0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun updateVideoInfo() {
        // Video thumbnail göster
        binding.ivVideoPlaceholder.visibility = View.GONE
        binding.ivVideoThumbnail.visibility = View.VISIBLE

        // Video bilgilerini göster
        binding.layoutVideoInfo.visibility = View.VISIBLE

        // Süre formatla
        val durationSeconds = videoDuration / 1000
        binding.tvDuration.text = "${durationSeconds}s"

        // Boyut formatla
        val sizeMB = videoSize / (1024 * 1024)
        binding.tvSize.text = "${sizeMB}MB"
    }

    private fun shareVideo() {
        if (selectedVideoUri == null) {
            Toast.makeText(requireContext(), "Lütfen bir video seçin!", Toast.LENGTH_SHORT).show()
            return
        }

        val description = binding.etDescription.text.toString().trim()

        // TODO: Video'yu sunucuya yükle ve günlük bar'a ekle
        Toast.makeText(requireContext(), "Video paylaşıldı! (Geliştirme aşamasında)", Toast.LENGTH_SHORT).show()

        // Geri dön
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
