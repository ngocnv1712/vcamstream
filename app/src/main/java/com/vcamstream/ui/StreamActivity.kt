package com.vcamstream.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.pedro.library.view.OpenGlView
import com.vcamstream.databinding.ActivityStreamBinding
import com.vcamstream.streaming.RtmpStreamer
import java.util.Locale

class StreamActivity : AppCompatActivity(), RtmpStreamer.StreamListener {

    private lateinit var binding: ActivityStreamBinding
    private lateinit var streamer: RtmpStreamer

    private var rtmpUrl: String = ""
    private var sourceType: String = ""
    private var sourcePath: String = ""
    private var quality: String = "720p"

    private var isStreaming = false
    private var streamStartTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        binding = ActivityStreamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get intent extras
        rtmpUrl = intent.getStringExtra("rtmp_url") ?: ""
        sourceType = intent.getStringExtra("source_type") ?: ""
        sourcePath = intent.getStringExtra("source_path") ?: ""
        quality = intent.getStringExtra("quality") ?: "720p"

        setupStreamer()
        setupUI()
    }

    private fun setupStreamer() {
        streamer = RtmpStreamer(this, this)

        // Set quality
        when (quality) {
            "480p" -> {
                streamer.width = 854
                streamer.height = 480
                streamer.videoBitrate = 1500 * 1024
            }
            "720p" -> {
                streamer.width = 1280
                streamer.height = 720
                streamer.videoBitrate = 2500 * 1024
            }
            "1080p" -> {
                streamer.width = 1920
                streamer.height = 1080
                streamer.videoBitrate = 4500 * 1024
            }
        }

        streamer.init(binding.openGlView)
    }

    private fun setupUI() {
        // Start/Stop button
        binding.btnToggleStream.setOnClickListener {
            if (isStreaming) {
                stopStreaming()
            } else {
                startStreaming()
            }
        }

        // Back button
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Info display
        binding.tvStreamUrl.text = rtmpUrl.take(50) + "..."
        binding.tvQuality.text = quality
        binding.tvSource.text = sourcePath.substringAfterLast("/")

        // Auto start
        binding.openGlView.post {
            startStreaming()
        }
    }

    private fun startStreaming() {
        binding.btnToggleStream.isEnabled = false
        binding.tvStatus.text = "Connecting..."
        binding.progressBar.visibility = View.VISIBLE

        val success = when (sourceType) {
            "VIDEO" -> streamer.startStreamFromVideo(sourcePath, rtmpUrl, loop = true)
            "IMAGE" -> streamer.startStreamFromImage(sourcePath, rtmpUrl)
            else -> false
        }

        if (!success) {
            binding.btnToggleStream.isEnabled = true
            binding.tvStatus.text = "Failed to start"
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun stopStreaming() {
        streamer.stopStream()
        isStreaming = false
        updateUI()
    }

    private fun updateUI() {
        binding.btnToggleStream.text = if (isStreaming) "Stop" else "Start"
        binding.tvStatus.text = if (isStreaming) "LIVE" else "Stopped"
        binding.tvStatus.setTextColor(
            if (isStreaming)
                getColor(android.R.color.holo_red_light)
            else
                getColor(android.R.color.darker_gray)
        )
    }

    private fun updateStreamTime() {
        if (isStreaming && streamStartTime > 0) {
            val elapsed = System.currentTimeMillis() - streamStartTime
            val seconds = (elapsed / 1000) % 60
            val minutes = (elapsed / (1000 * 60)) % 60
            val hours = (elapsed / (1000 * 60 * 60))

            binding.tvDuration.text = String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                hours, minutes, seconds
            )

            binding.tvDuration.postDelayed({ updateStreamTime() }, 1000)
        }
    }

    // StreamListener callbacks
    override fun onStreamStarted() {
        runOnUiThread {
            isStreaming = true
            streamStartTime = System.currentTimeMillis()
            binding.btnToggleStream.isEnabled = true
            binding.progressBar.visibility = View.GONE
            updateUI()
            updateStreamTime()
        }
    }

    override fun onStreamStopped() {
        runOnUiThread {
            isStreaming = false
            streamStartTime = 0
            binding.btnToggleStream.isEnabled = true
            updateUI()
        }
    }

    override fun onStreamError(error: String) {
        runOnUiThread {
            binding.progressBar.visibility = View.GONE
            binding.btnToggleStream.isEnabled = true
            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            binding.tvStatus.text = "Error"
        }
    }

    override fun onConnectionSuccess() {
        runOnUiThread {
            Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionFailed(reason: String) {
        runOnUiThread {
            binding.progressBar.visibility = View.GONE
            binding.btnToggleStream.isEnabled = true
            binding.tvStatus.text = "Connection failed"

            AlertDialog.Builder(this)
                .setTitle("Connection Failed")
                .setMessage("Reason: $reason\n\nPlease check:\n• Stream key is correct\n• Network connection\n• Platform is ready for stream")
                .setPositiveButton("Retry") { _, _ -> startStreaming() }
                .setNegativeButton("Back") { _, _ -> finish() }
                .show()
        }
    }

    override fun onBitrateChanged(bitrate: Long) {
        runOnUiThread {
            val kbps = bitrate / 1024
            binding.tvBitrate.text = "${kbps} Kbps"
        }
    }

    override fun onBackPressed() {
        if (isStreaming) {
            AlertDialog.Builder(this)
                .setTitle("Stop Stream?")
                .setMessage("Are you sure you want to stop streaming?")
                .setPositiveButton("Stop") { _, _ ->
                    stopStreaming()
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        streamer.release()
    }
}
