# Kiến trúc ứng dụng VCamStream

## Tổng quan

VCamStream sử dụng kiến trúc Activity-based với sự phân tách rõ ràng giữa các tầng UI, Streaming và Utility.

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                            │
│  ┌─────────────────────┐    ┌─────────────────────────────┐ │
│  │    MainActivity     │───▶│      StreamActivity         │ │
│  │   (Cấu hình stream) │    │   (Điều khiển stream)       │ │
│  └─────────────────────┘    └─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Streaming Layer                        │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                    RtmpStreamer                         ││
│  │  • Quản lý kết nối RTMP                                 ││
│  │  • Xử lý video/image streaming                          ││
│  │  • Cung cấp thống kê realtime                           ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Utility Layer                          │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                    PrefsManager                         ││
│  │  • Lưu trữ cấu hình người dùng                          ││
│  │  • SharedPreferences wrapper                            ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   External Libraries                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  RootEncoder │  │   ExoPlayer  │  │    Glide     │      │
│  │    (RTMP)    │  │   (Video)    │  │   (Image)    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

## Chi tiết các thành phần

### 1. UI Layer

#### MainActivity (`com.vcamstream.ui.MainActivity`)

**Mục đích**: Màn hình cấu hình chính cho phiên stream.

**Chức năng**:
- Hiển thị form nhập thông tin stream
- Quản lý việc chọn nền tảng (Facebook, YouTube, TikTok, Custom)
- Xử lý chọn file video/hình ảnh từ thiết bị
- Quản lý cấu hình chất lượng stream
- Lưu trữ cấu hình qua PrefsManager
- Xử lý quyền truy cập media

**Lifecycle**:
```
onCreate() → Khởi tạo UI, load cấu hình đã lưu
onResume() → Kiểm tra quyền
onClick() → Xử lý tương tác người dùng
startActivity() → Chuyển sang StreamActivity
```

**Layout**: `activity_main.xml`
- Orientation: Portrait
- Components: RadioGroup (platforms), EditText (stream key), Buttons (video/image picker), Spinner (quality)

---

#### StreamActivity (`com.vcamstream.ui.StreamActivity`)

**Mục đích**: Màn hình điều khiển và giám sát phiên stream đang diễn ra.

**Chức năng**:
- Hiển thị preview video fullscreen với OpenGlView
- Điều khiển bắt đầu/dừng stream
- Hiển thị trạng thái LIVE
- Hiển thị thống kê realtime (duration, bitrate, quality)
- Xử lý lỗi kết nối với dialog retry

**Interface Implementation**: `StreamListener`
- `onConnectionSuccessRtmp()` - Kết nối thành công
- `onConnectionFailedRtmp(reason)` - Kết nối thất bại
- `onDisconnectRtmp()` - Ngắt kết nối
- `onNewBitrateRtmp(bitrate)` - Cập nhật bitrate

**Layout**: `activity_stream.xml`
- Orientation: Landscape
- Fullscreen immersive mode
- Components: OpenGlView (preview), ImageButton (controls), TextView (stats)

---

### 2. Streaming Layer

#### RtmpStreamer (`com.vcamstream.streaming.RtmpStreamer`)

**Mục đích**: Lớp core xử lý việc stream RTMP.

**Cấu trúc**:
```kotlin
class RtmpStreamer(
    private val context: Context,
    private val openGlView: OpenGlView
) {
    // Wrapper của RtmpFromFile từ RootEncoder library
    private var rtmpFromFile: RtmpFromFile?

    // Callback listener
    private var listener: StreamListener?
}
```

**Interface StreamListener**:
```kotlin
interface StreamListener {
    fun onConnectionSuccessRtmp()
    fun onConnectionFailedRtmp(reason: String)
    fun onDisconnectRtmp()
    fun onNewBitrateRtmp(bitrate: Long)
}
```

**Cấu hình chất lượng**:
| Quality | Resolution | Video Bitrate | Audio Bitrate | FPS |
|---------|------------|---------------|---------------|-----|
| 480p    | 854x480    | 1,500 kbps    | 128 kbps      | 30  |
| 720p    | 1280x720   | 2,500 kbps    | 128 kbps      | 30  |
| 1080p   | 1920x1080  | 4,500 kbps    | 128 kbps      | 30  |

**Luồng xử lý Video Stream**:
```
1. prepareVideo(quality) → Set encoder parameters
2. prepareAudio() → Configure audio from video file
3. startStream(rtmpUrl) → Connect to RTMP server
4. RootEncoder handles encoding & transmission
5. stopStream() → Disconnect & cleanup
```

**Luồng xử lý Image Stream**:
```
1. Load bitmap from file path
2. prepareVideo(quality) → Set encoder parameters
3. prepareAudio() → Configure silent or background audio
4. startStream(rtmpUrl) → Connect to RTMP server
5. Inject bitmap frames at configured FPS
6. stopStream() → Disconnect & cleanup
```

**Thống kê được cung cấp**:
- `getBitrate()` - Bitrate hiện tại (bits/second)
- `getFps()` - Frame rate thực tế
- `getResolution()` - Độ phân giải đang stream

---

### 3. Utility Layer

#### PrefsManager (`com.vcamstream.util.PrefsManager`)

**Mục đích**: Quản lý lưu trữ cấu hình người dùng.

**Cấu trúc**:
```kotlin
class PrefsManager(context: Context) {
    private val prefs: SharedPreferences

    // Getters & Setters cho các cấu hình
    var platform: String        // "facebook", "youtube", "tiktok", "custom"
    var streamKey: String       // Stream key của nền tảng
    var customRtmpUrl: String   // URL RTMP tùy chỉnh
    var quality: String         // "480p", "720p", "1080p"
    var videoPath: String       // Đường dẫn file video
    var imagePath: String       // Đường dẫn file hình ảnh
    var sourceType: String      // "video" hoặc "image"
    var loopEnabled: Boolean    // Bật/tắt loop video
}
```

**Lưu trữ**:
- SharedPreferences với mode PRIVATE
- Tên file: `vcamstream_prefs`
- Persistence across app restarts

---

### 4. RTMP URL Construction

**Nền tảng được hỗ trợ**:

| Platform | RTMP URL Pattern |
|----------|------------------|
| Facebook | `rtmps://live-api-s.facebook.com:443/rtmp/{stream_key}` |
| YouTube  | `rtmp://a.rtmp.youtube.com/live2/{stream_key}` |
| TikTok   | `rtmp://push.tiktok.com/live/{stream_key}` |
| Custom   | User-provided full RTMP URL |

---

## Luồng dữ liệu chính

### 1. Cấu hình Stream

```
User Input (MainActivity)
    │
    ▼
PrefsManager.save()
    │
    ▼
SharedPreferences Storage
    │
    ▼
Intent → StreamActivity
```

### 2. Phiên Stream

```
StreamActivity.onCreate()
    │
    ▼
PrefsManager.load() → Get configuration
    │
    ▼
RtmpStreamer.initialize(openGlView)
    │
    ▼
RtmpStreamer.prepare(quality, sourcePath)
    │
    ▼
RtmpStreamer.startStream(rtmpUrl)
    │
    ▼
StreamListener callbacks → Update UI
    │
    ▼
RtmpStreamer.stopStream()
```

---

## Xử lý lỗi

### Các trường hợp lỗi chính

| Lỗi | Xử lý |
|-----|-------|
| Kết nối RTMP thất bại | Hiển thị dialog với option retry |
| Mất kết nối giữa stream | Tự động dừng, thông báo người dùng |
| File không tồn tại | Toast thông báo, quay về MainActivity |
| Quyền bị từ chối | Hiển thị dialog yêu cầu cấp quyền |

### Error Recovery Flow

```
Error detected
    │
    ▼
Stop stream safely
    │
    ▼
Show error dialog
    │
    ├── Retry → Re-attempt connection
    │
    └── Cancel → Return to MainActivity
```

---

## Thread Model

```
Main Thread (UI)
    │
    ├── MainActivity UI interactions
    ├── StreamActivity UI updates
    └── StreamListener callbacks (posted to main)

Background Threads (RootEncoder)
    │
    ├── Video encoding thread
    ├── Audio encoding thread
    ├── RTMP transmission thread
    └── Statistics collection thread
```

---

## Tương lai mở rộng

### Media Layer (Reserved)
Package `com.vcamstream.media` được dự trữ cho:
- Custom video processing
- Audio mixing utilities
- Thumbnail generation
- Video format conversion

### Potential Enhancements
- MVVM architecture with ViewModel
- Room database for stream history
- Multiple bitrate streaming (ABR)
- Picture-in-picture support
- Scheduled streaming
- Stream analytics dashboard
