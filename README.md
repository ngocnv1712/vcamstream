# VCamStream

**Ứng dụng Android stream video/hình ảnh lên các nền tảng phát sóng trực tiếp**

VCamStream cho phép người dùng stream video đã quay sẵn hoặc hình ảnh tĩnh lên Facebook Live, YouTube Live, TikTok Live hoặc máy chủ RTMP tùy chỉnh mà không cần camera trực tiếp.

## Tính năng

- **Đa nền tảng**: Hỗ trợ Facebook Live, YouTube Live, TikTok Live và RTMP tùy chỉnh
- **Nguồn linh hoạt**: Stream từ video (MP4) hoặc hình ảnh tĩnh
- **Chất lượng đa dạng**: 480p (1.5 Mbps), 720p (2.5 Mbps), 1080p (4.5 Mbps)
- **Lặp video**: Hỗ trợ phát lặp lại video liên tục
- **Thống kê realtime**: Hiển thị thời gian stream, bitrate, chất lượng
- **Giao diện Material Design 3**: UI hiện đại, dễ sử dụng

## Yêu cầu hệ thống

- **Android**: 7.0 (API 24) trở lên
- **Target SDK**: 34 (Android 14)
- **Kết nối mạng**: Internet ổn định để stream

## Cài đặt

### Build từ source

```bash
# Clone repository
git clone <repository-url>
cd VCamStream

# Build debug APK
./gradlew assembleDebug

# APK output: app/build/outputs/apk/debug/app-debug.apk
```

### Cài đặt APK

1. Cho phép cài đặt từ nguồn không xác định trên thiết bị
2. Cài đặt file APK đã build
3. Cấp quyền cần thiết khi được yêu cầu

## Sử dụng

### 1. Chọn nền tảng stream

- **Facebook Live**: Sử dụng stream key từ Facebook Creator Studio
- **YouTube Live**: Sử dụng stream key từ YouTube Studio
- **TikTok Live**: Sử dụng stream key từ TikTok Live Studio
- **RTMP tùy chỉnh**: Nhập URL RTMP đầy đủ của máy chủ riêng

### 2. Nhập Stream Key

Lấy stream key từ nền tảng tương ứng và nhập vào ứng dụng.

### 3. Chọn nguồn video/hình ảnh

- Chọn file video MP4 từ thiết bị
- Hoặc chọn hình ảnh để stream như ảnh tĩnh

### 4. Chọn chất lượng

| Chất lượng | Độ phân giải | Bitrate |
|------------|--------------|---------|
| 480p       | 854x480      | 1.5 Mbps |
| 720p       | 1280x720     | 2.5 Mbps |
| 1080p      | 1920x1080    | 4.5 Mbps |

### 5. Bắt đầu stream

Nhấn nút "BẮT ĐẦU STREAM" để khởi động phiên stream.

## Cấu trúc dự án

```
VCamStream/
├── app/
│   ├── src/main/
│   │   ├── java/com/vcamstream/
│   │   │   ├── ui/              # Activities (MainActivity, StreamActivity)
│   │   │   ├── streaming/       # Logic RTMP streaming (RtmpStreamer)
│   │   │   ├── util/            # Utilities (PrefsManager)
│   │   │   └── media/           # Media utilities (reserved)
│   │   ├── res/                 # Resources (layouts, drawables, values)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

## Công nghệ sử dụng

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| Kotlin | 1.9.20 | Ngôn ngữ chính |
| RootEncoder | 2.4.5 | RTMP streaming |
| ExoPlayer (Media3) | 1.2.0 | Video playback |
| Material Design 3 | - | UI components |
| Glide | 4.16.0 | Image loading |
| Kotlin Coroutines | 1.7.3 | Async operations |

## Quyền ứng dụng

Ứng dụng yêu cầu các quyền sau:

- `INTERNET` - Kết nối stream
- `ACCESS_NETWORK_STATE` - Kiểm tra trạng thái mạng
- `READ_MEDIA_VIDEO`, `READ_MEDIA_IMAGES`, `READ_MEDIA_AUDIO` - Truy cập media
- `CAMERA`, `RECORD_AUDIO` - Quyền media (yêu cầu bởi thư viện)
- `FOREGROUND_SERVICE` - Chạy service nền khi stream
- `POST_NOTIFICATIONS` - Hiển thị thông báo stream

## Tài liệu chi tiết

- [Kiến trúc ứng dụng](docs/ARCHITECTURE.md)
- [Hướng dẫn sử dụng chi tiết](docs/USAGE.md)

## Lưu ý

- Đảm bảo kết nối mạng ổn định trước khi stream
- Sử dụng video định dạng MP4 với tỷ lệ 16:9 để có chất lượng tốt nhất
- Stream key có thể hết hạn, cần lấy key mới từ nền tảng

## License

[Thêm thông tin license tại đây]

---

**VCamStream** - Stream video/image to Facebook, YouTube, TikTok
