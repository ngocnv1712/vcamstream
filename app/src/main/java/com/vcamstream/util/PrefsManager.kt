package com.vcamstream.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Quản lý SharedPreferences cho app
 */
class PrefsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "vcamstream_prefs",
        Context.MODE_PRIVATE
    )

    var platform: String
        get() = prefs.getString("platform", "facebook") ?: "facebook"
        set(value) = prefs.edit().putString("platform", value).apply()

    var streamKey: String
        get() = prefs.getString("stream_key", "") ?: ""
        set(value) = prefs.edit().putString("stream_key", value).apply()

    var customRtmpUrl: String
        get() = prefs.getString("custom_rtmp_url", "") ?: ""
        set(value) = prefs.edit().putString("custom_rtmp_url", value).apply()

    var quality: String
        get() = prefs.getString("quality", "720p") ?: "720p"
        set(value) = prefs.edit().putString("quality", value).apply()

    var sourcePath: String?
        get() = prefs.getString("source_path", null)
        set(value) = prefs.edit().putString("source_path", value).apply()

    var sourceType: String?
        get() = prefs.getString("source_type", null)
        set(value) = prefs.edit().putString("source_type", value).apply()

    var loopVideo: Boolean
        get() = prefs.getBoolean("loop_video", true)
        set(value) = prefs.edit().putBoolean("loop_video", value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
