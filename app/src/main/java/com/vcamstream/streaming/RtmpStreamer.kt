package com.vcamstream.streaming

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import android.view.Surface
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.library.base.FromFileBase
import com.pedro.library.rtmp.RtmpFromFile
import com.pedro.library.util.sources.video.NoVideoSource
import com.pedro.library.view.OpenGlView
import com.pedro.rtmp.rtmp.RtmpClient
import com.pedro.encoder.input.sources.video.VideoFileSource
import java.io.File
import java.io.IOException

/**
 * RTMP Streamer - Stream video/image lên Facebook, TikTok, YouTube
 * Sử dụng RootEncoder library (không cần root)
 */
class RtmpStreamer(
    private val context: Context,
    private val listener: StreamListener
) {
    companion object {
        private const val TAG = "RtmpStreamer"

        // Default stream settings
        const val DEFAULT_VIDEO_BITRATE = 2500 * 1024  // 2.5 Mbps
        const val DEFAULT_AUDIO_BITRATE = 128 * 1024   // 128 Kbps
        const val DEFAULT_FPS = 30
        const val DEFAULT_WIDTH = 1280
        const val DEFAULT_HEIGHT = 720
    }

    interface StreamListener {
        fun onStreamStarted()
        fun onStreamStopped()
        fun onStreamError(error: String)
        fun onConnectionSuccess()
        fun onConnectionFailed(reason: String)
        fun onBitrateChanged(bitrate: Long)
    }

    private var rtmpFromFile: RtmpFromFile? = null
    private var isStreaming = false
    private var rtmpUrl: String = ""

    // Stream settings
    var videoBitrate = DEFAULT_VIDEO_BITRATE
    var audioBitrate = DEFAULT_AUDIO_BITRATE
    var fps = DEFAULT_FPS
    var width = DEFAULT_WIDTH
    var height = DEFAULT_HEIGHT

    /**
     * Khởi tạo streamer với OpenGlView để preview
     */
    fun init(openGlView: OpenGlView) {
        rtmpFromFile = RtmpFromFile(context, object : com.pedro.common.ConnectChecker {
            override fun onConnectionStarted(url: String) {
                Log.d(TAG, "Connection started: $url")
            }

            override fun onConnectionSuccess() {
                Log.d(TAG, "Connection success")
                listener.onConnectionSuccess()
            }

            override fun onConnectionFailed(reason: String) {
                Log.e(TAG, "Connection failed: $reason")
                listener.onConnectionFailed(reason)
                isStreaming = false
            }

            override fun onNewBitrate(bitrate: Long) {
                listener.onBitrateChanged(bitrate)
            }

            override fun onDisconnect() {
                Log.d(TAG, "Disconnected")
                isStreaming = false
                listener.onStreamStopped()
            }

            override fun onAuthError() {
                Log.e(TAG, "Auth error")
                listener.onStreamError("Authentication failed")
            }

            override fun onAuthSuccess() {
                Log.d(TAG, "Auth success")
            }
        })

        rtmpFromFile?.setView(openGlView)
    }

    /**
     * Bắt đầu stream từ video file
     */
    fun startStreamFromVideo(
        videoPath: String,
        rtmpUrl: String,
        loop: Boolean = true
    ): Boolean {
        this.rtmpUrl = rtmpUrl

        try {
            val streamer = rtmpFromFile ?: return false

            // Prepare với video file
            if (!streamer.prepareVideo(videoPath)) {
                listener.onStreamError("Failed to prepare video")
                return false
            }

            if (!streamer.prepareAudio(videoPath)) {
                // Audio có thể không có, không phải lỗi critical
                Log.w(TAG, "No audio track or failed to prepare audio")
            }

            // Set loop
            streamer.setLoopMode(loop)

            // Start streaming
            streamer.startStream(rtmpUrl)
            isStreaming = true
            listener.onStreamStarted()

            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error starting stream", e)
            listener.onStreamError(e.message ?: "Unknown error")
            return false
        }
    }

    /**
     * Bắt đầu stream từ image (static image stream)
     */
    fun startStreamFromImage(
        imagePath: String,
        rtmpUrl: String,
        audioPath: String? = null
    ): Boolean {
        this.rtmpUrl = rtmpUrl

        try {
            val streamer = rtmpFromFile ?: return false
            val bitmap = BitmapFactory.decodeFile(imagePath)

            if (bitmap == null) {
                listener.onStreamError("Failed to load image")
                return false
            }

            // Prepare video encoder với settings
            if (!streamer.prepareVideo(width, height, fps, videoBitrate)) {
                listener.onStreamError("Failed to prepare video encoder")
                return false
            }

            // Prepare audio nếu có
            if (audioPath != null) {
                if (!streamer.prepareAudio(audioPath)) {
                    Log.w(TAG, "Failed to prepare audio")
                }
            } else {
                // Prepare silent audio
                if (!streamer.prepareAudio(audioBitrate, 44100, true)) {
                    Log.w(TAG, "Failed to prepare audio")
                }
            }

            // Start stream
            streamer.startStream(rtmpUrl)
            isStreaming = true

            // Inject bitmap frames
            startBitmapInjection(bitmap)

            listener.onStreamStarted()
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error starting image stream", e)
            listener.onStreamError(e.message ?: "Unknown error")
            return false
        }
    }

    /**
     * Inject bitmap frames liên tục cho image stream
     */
    private fun startBitmapInjection(bitmap: Bitmap) {
        Thread {
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
            val frameInterval = 1000L / fps

            while (isStreaming) {
                rtmpFromFile?.getGlInterface()?.setImageBitmap(scaledBitmap)
                Thread.sleep(frameInterval)
            }

            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
        }.start()
    }

    /**
     * Dừng stream
     */
    fun stopStream() {
        isStreaming = false
        rtmpFromFile?.stopStream()
        listener.onStreamStopped()
    }

    /**
     * Pause/Resume video
     */
    fun pauseVideo() {
        rtmpFromFile?.pauseRecord()
    }

    fun resumeVideo() {
        rtmpFromFile?.resumeRecord()
    }

    /**
     * Kiểm tra trạng thái
     */
    fun isStreaming(): Boolean = isStreaming

    /**
     * Thay đổi video source trong khi đang stream
     */
    fun changeVideoSource(newVideoPath: String): Boolean {
        if (!isStreaming) return false

        try {
            // TODO: Implement source switching
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error changing source", e)
            return false
        }
    }

    /**
     * Lấy thông tin stream hiện tại
     */
    fun getStreamStats(): StreamStats {
        return StreamStats(
            isConnected = isStreaming,
            bitrate = rtmpFromFile?.getBitrate() ?: 0,
            fps = fps,
            resolution = "${width}x${height}",
            url = rtmpUrl
        )
    }

    /**
     * Release resources
     */
    fun release() {
        stopStream()
        rtmpFromFile = null
    }

    data class StreamStats(
        val isConnected: Boolean,
        val bitrate: Long,
        val fps: Int,
        val resolution: String,
        val url: String
    )
}
