package com.vcamstream.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.vcamstream.R
import com.vcamstream.databinding.ActivityMainBinding
import com.vcamstream.util.PrefsManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PrefsManager

    private var selectedVideoPath: String? = null
    private var selectedImagePath: String? = null
    private var sourceType: SourceType = SourceType.NONE

    enum class SourceType { NONE, VIDEO, IMAGE }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickVideoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedVideo(uri)
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsManager(this)

        checkPermissions()
        setupUI()
        loadSavedConfig()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET
        )

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            requestPermissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    private fun setupUI() {
        // Platform selection
        binding.chipFacebook.setOnClickListener { selectPlatform("facebook") }
        binding.chipYoutube.setOnClickListener { selectPlatform("youtube") }
        binding.chipTiktok.setOnClickListener { selectPlatform("tiktok") }
        binding.chipCustom.setOnClickListener { selectPlatform("custom") }

        // Source selection
        binding.btnSelectVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "video/*"
            }
            pickVideoLauncher.launch(intent)
        }

        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        }

        binding.btnClearSource.setOnClickListener {
            clearSource()
        }

        // Stream key input
        binding.etStreamKey.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                prefs.streamKey = binding.etStreamKey.text.toString()
            }
        }

        // Custom RTMP URL
        binding.etRtmpUrl.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                prefs.customRtmpUrl = binding.etRtmpUrl.text.toString()
            }
        }

        // Quality selection
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio720p -> prefs.quality = "720p"
                R.id.radio1080p -> prefs.quality = "1080p"
                R.id.radio480p -> prefs.quality = "480p"
            }
        }

        // Start stream button
        binding.btnStartStream.setOnClickListener {
            startStream()
        }

        // Help button
        binding.btnHelp.setOnClickListener {
            showHelpDialog()
        }
    }

    private fun loadSavedConfig() {
        // Load platform
        selectPlatform(prefs.platform)

        // Load stream key
        binding.etStreamKey.setText(prefs.streamKey)

        // Load custom URL
        binding.etRtmpUrl.setText(prefs.customRtmpUrl)

        // Load quality
        when (prefs.quality) {
            "720p" -> binding.radio720p.isChecked = true
            "1080p" -> binding.radio1080p.isChecked = true
            "480p" -> binding.radio480p.isChecked = true
        }

        // Load source
        prefs.sourcePath?.let { path ->
            if (prefs.sourceType == "video") {
                selectedVideoPath = path
                sourceType = SourceType.VIDEO
                updateSourceDisplay(path, "Video")
            } else if (prefs.sourceType == "image") {
                selectedImagePath = path
                sourceType = SourceType.IMAGE
                updateSourceDisplay(path, "Image")
            }
        }
    }

    private fun selectPlatform(platform: String) {
        prefs.platform = platform

        // Reset all chips
        binding.chipFacebook.isChecked = false
        binding.chipYoutube.isChecked = false
        binding.chipTiktok.isChecked = false
        binding.chipCustom.isChecked = false

        // Update UI based on platform
        when (platform) {
            "facebook" -> {
                binding.chipFacebook.isChecked = true
                binding.layoutCustomUrl.visibility = android.view.View.GONE
                binding.tvRtmpHint.text = "Lấy Stream Key từ Facebook Live Producer"
            }
            "youtube" -> {
                binding.chipYoutube.isChecked = true
                binding.layoutCustomUrl.visibility = android.view.View.GONE
                binding.tvRtmpHint.text = "Lấy Stream Key từ YouTube Studio > Go Live"
            }
            "tiktok" -> {
                binding.chipTiktok.isChecked = true
                binding.layoutCustomUrl.visibility = android.view.View.GONE
                binding.tvRtmpHint.text = "Lấy Stream Key từ TikTok LIVE Studio"
            }
            "custom" -> {
                binding.chipCustom.isChecked = true
                binding.layoutCustomUrl.visibility = android.view.View.VISIBLE
                binding.tvRtmpHint.text = "Nhập RTMP URL đầy đủ"
            }
        }
    }

    private fun handleSelectedVideo(uri: Uri) {
        val path = getRealPathFromUri(uri)
        if (path != null) {
            selectedVideoPath = path
            selectedImagePath = null
            sourceType = SourceType.VIDEO
            prefs.sourcePath = path
            prefs.sourceType = "video"
            updateSourceDisplay(path, "Video")
        } else {
            Toast.makeText(this, "Cannot access video file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        val path = getRealPathFromUri(uri)
        if (path != null) {
            selectedImagePath = path
            selectedVideoPath = null
            sourceType = SourceType.IMAGE
            prefs.sourcePath = path
            prefs.sourceType = "image"
            updateSourceDisplay(path, "Image")
        } else {
            Toast.makeText(this, "Cannot access image file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.MediaColumns.DATA)

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                path = cursor.getString(columnIndex)
            }
        }

        return path
    }

    private fun updateSourceDisplay(path: String, type: String) {
        val fileName = path.substringAfterLast("/")
        binding.tvSelectedSource.text = fileName
        binding.tvSourceType.text = "Type: $type"
    }

    private fun clearSource() {
        selectedVideoPath = null
        selectedImagePath = null
        sourceType = SourceType.NONE
        prefs.sourcePath = null
        prefs.sourceType = null
        binding.tvSelectedSource.text = "No source selected"
        binding.tvSourceType.text = ""
    }

    private fun startStream() {
        // Validate inputs
        if (sourceType == SourceType.NONE) {
            Toast.makeText(this, "Please select a video or image", Toast.LENGTH_SHORT).show()
            return
        }

        val streamKey = binding.etStreamKey.text.toString().trim()
        val customUrl = binding.etRtmpUrl.text.toString().trim()

        if (prefs.platform == "custom") {
            if (customUrl.isEmpty()) {
                Toast.makeText(this, "Please enter RTMP URL", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            if (streamKey.isEmpty()) {
                Toast.makeText(this, "Please enter Stream Key", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Build RTMP URL
        val rtmpUrl = buildRtmpUrl(prefs.platform, streamKey, customUrl)

        // Start StreamActivity
        val intent = Intent(this, StreamActivity::class.java).apply {
            putExtra("rtmp_url", rtmpUrl)
            putExtra("source_type", sourceType.name)
            putExtra("source_path", if (sourceType == SourceType.VIDEO) selectedVideoPath else selectedImagePath)
            putExtra("quality", prefs.quality)
        }
        startActivity(intent)
    }

    private fun buildRtmpUrl(platform: String, streamKey: String, customUrl: String): String {
        return when (platform) {
            "facebook" -> "rtmps://live-api-s.facebook.com:443/rtmp/$streamKey"
            "youtube" -> "rtmp://a.rtmp.youtube.com/live2/$streamKey"
            "tiktok" -> "rtmp://push.tiktokcdn.com/live/$streamKey"
            "custom" -> customUrl
            else -> customUrl
        }
    }

    private fun showHelpDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hướng dẫn sử dụng")
            .setMessage("""
                1. Chọn nền tảng stream (Facebook, YouTube, TikTok)

                2. Lấy Stream Key:
                   • Facebook: Creator Studio > Live > Go Live
                   • YouTube: YouTube Studio > Go Live > Stream
                   • TikTok: TikTok LIVE Studio

                3. Chọn video hoặc ảnh làm nguồn

                4. Chọn chất lượng stream

                5. Nhấn "Start Streaming"

                Lưu ý:
                • Đảm bảo kết nối mạng ổn định
                • Video nên có định dạng MP4
                • Ảnh nên có tỷ lệ 16:9
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
}
