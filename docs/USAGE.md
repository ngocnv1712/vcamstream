# Hướng dẫn sử dụng VCamStream

## Mục lục

1. [Cài đặt](#cài-đặt)
2. [Cấu hình ban đầu](#cấu-hình-ban-đầu)
3. [Lấy Stream Key](#lấy-stream-key)
4. [Chuẩn bị nội dung](#chuẩn-bị-nội-dung)
5. [Bắt đầu Stream](#bắt-đầu-stream)
6. [Trong khi Stream](#trong-khi-stream)
7. [Xử lý sự cố](#xử-lý-sự-cố)

---

## Cài đặt

### Yêu cầu thiết bị

- Android 7.0 (Nougat) trở lên
- RAM tối thiểu 2GB (khuyến nghị 4GB)
- Kết nối Internet ổn định (WiFi hoặc 4G/5G)
- Dung lượng trống: ~50MB

### Cài đặt từ APK

1. Tải file APK từ nguồn phân phối
2. Trên thiết bị Android, vào **Cài đặt** → **Bảo mật**
3. Bật **Nguồn không xác định** hoặc **Cài đặt ứng dụng không rõ nguồn gốc**
4. Mở file APK và chọn **Cài đặt**
5. Hoàn tất cài đặt

### Cấp quyền ứng dụng

Khi mở ứng dụng lần đầu, cấp các quyền sau:

| Quyền | Mục đích |
|-------|----------|
| Truy cập ảnh và video | Chọn file để stream |
| Camera | Yêu cầu bởi thư viện streaming |
| Microphone | Yêu cầu bởi thư viện streaming |
| Thông báo | Hiển thị trạng thái stream |

---

## Cấu hình ban đầu

### Màn hình chính

Khi mở ứng dụng, bạn sẽ thấy màn hình cấu hình với các phần:

```
┌────────────────────────────────────┐
│         VCamStream                 │
├────────────────────────────────────┤
│  ○ Facebook Live                   │
│  ○ YouTube Live                    │
│  ○ TikTok Live                     │
│  ○ RTMP tùy chỉnh                  │
├────────────────────────────────────┤
│  Stream Key: [_______________]     │
├────────────────────────────────────┤
│  [Chọn Video]    [Chọn Hình ảnh]   │
│                                    │
│  File đã chọn: video.mp4           │
├────────────────────────────────────┤
│  Chất lượng: [720p ▼]              │
├────────────────────────────────────┤
│       [BẮT ĐẦU STREAM]             │
└────────────────────────────────────┘
```

---

## Lấy Stream Key

### Facebook Live

1. Truy cập [Facebook Creator Studio](https://business.facebook.com/creatorstudio)
2. Chọn **Tạo bài viết** → **Phát trực tiếp**
3. Hoặc trên Facebook App:
   - Vào trang/profile của bạn
   - Chọn **Live** → **Phát trực tiếp**
4. Trong phần **Cài đặt phát trực tiếp**, tìm **Stream Key**
5. Sao chép stream key (không bao gồm URL)

**Lưu ý**: Stream key Facebook thường hết hạn sau 7 ngày.

### YouTube Live

1. Truy cập [YouTube Studio](https://studio.youtube.com)
2. Chọn **Tạo** (biểu tượng +) → **Phát trực tiếp**
3. Thiết lập thông tin stream (tiêu đề, mô tả, quyền riêng tư)
4. Trong tab **Cài đặt stream**, tìm **Stream key**
5. Nhấn **Sao chép** để copy stream key

**Yêu cầu**:
- Kênh phải được xác minh
- Không vi phạm cộng đồng trong 90 ngày

### TikTok Live

1. Mở TikTok trên điện thoại
2. Vào **Profile** → **Công cụ sáng tạo**
3. Chọn **LIVE Studio** hoặc **Go Live**
4. Vào **Cài đặt LIVE** → **Server URL và Stream Key**
5. Sao chép Stream Key

**Yêu cầu**:
- Tài khoản phải có ít nhất 1000 followers
- Tuổi từ 16 trở lên (18+ để nhận quà)

### RTMP tùy chỉnh

Nếu sử dụng máy chủ RTMP riêng:

1. Chọn **RTMP tùy chỉnh**
2. Nhập đầy đủ URL RTMP, ví dụ:
   ```
   rtmp://your-server.com:1935/live/stream_key
   ```
3. URL phải bao gồm cả stream key trong đường dẫn

---

## Chuẩn bị nội dung

### Video

**Định dạng khuyến nghị**:
- Container: MP4
- Video codec: H.264
- Audio codec: AAC
- Tỷ lệ khung hình: 16:9
- Frame rate: 30fps

**Cách chọn video**:
1. Nhấn **Chọn Video** trên màn hình chính
2. Duyệt và chọn file video từ thiết bị
3. Xác nhận file đã chọn hiển thị đúng

**Lưu ý**:
- Video quá dài có thể gây nóng thiết bị
- Khuyến nghị video dưới 2 tiếng cho stream liên tục
- Bật **Loop** nếu muốn phát lặp lại

### Hình ảnh

**Định dạng hỗ trợ**:
- JPG/JPEG
- PNG
- WebP

**Cách chọn hình ảnh**:
1. Nhấn **Chọn Hình ảnh** trên màn hình chính
2. Chọn ảnh từ thư viện
3. Ảnh sẽ được stream như frame tĩnh

**Lưu ý**:
- Sử dụng ảnh tỷ lệ 16:9 để tránh letterbox
- Độ phân giải tối thiểu: 1280x720

---

## Bắt đầu Stream

### Kiểm tra trước khi stream

- [ ] Kết nối WiFi/4G ổn định
- [ ] Pin thiết bị > 50% hoặc đang sạc
- [ ] Stream key đã nhập đúng
- [ ] Đã chọn video/hình ảnh
- [ ] Chất lượng phù hợp với tốc độ mạng

### Chọn chất lượng phù hợp

| Chất lượng | Tốc độ mạng tối thiểu | Phù hợp với |
|------------|----------------------|-------------|
| 480p | 3 Mbps upload | 3G/4G yếu |
| 720p | 5 Mbps upload | 4G ổn định, WiFi |
| 1080p | 8 Mbps upload | WiFi mạnh, 5G |

### Bắt đầu

1. Kiểm tra tất cả cấu hình đã đúng
2. Nhấn **BẮT ĐẦU STREAM**
3. Ứng dụng chuyển sang màn hình stream (ngang)
4. Đợi kết nối (thường 5-15 giây)
5. Khi thấy chỉ báo **LIVE**, stream đã bắt đầu

---

## Trong khi Stream

### Màn hình Stream

```
┌──────────────────────────────────────────────────┐
│ ● LIVE                     00:15:32  │ 2.5 Mbps │
├──────────────────────────────────────────────────┤
│                                                  │
│                                                  │
│              [Video Preview]                     │
│                                                  │
│                                                  │
├──────────────────────────────────────────────────┤
│                              [⏹]                 │
└──────────────────────────────────────────────────┘
```

### Thông tin hiển thị

| Phần tử | Ý nghĩa |
|---------|---------|
| ● LIVE | Đang phát trực tiếp |
| 00:15:32 | Thời gian đã stream |
| 2.5 Mbps | Bitrate hiện tại |
| [⏹] | Nút dừng stream |

### Điều khiển

- **Dừng stream**: Nhấn nút ⏹ (vuông) ở góc dưới
- **Xác nhận dừng**: Chọn OK trong dialog xác nhận
- **Quay về cấu hình**: Tự động sau khi dừng

### Lưu ý khi stream

1. **Không tắt màn hình**: Giữ màn hình sáng khi stream
2. **Không chuyển app**: Tránh chuyển sang ứng dụng khác
3. **Theo dõi bitrate**: Bitrate giảm mạnh = mạng yếu
4. **Theo dõi nhiệt độ**: Thiết bị quá nóng có thể gây lag

---

## Xử lý sự cố

### Lỗi kết nối

**Triệu chứng**: Hiển thị "Kết nối thất bại"

**Nguyên nhân có thể**:
1. Stream key sai hoặc hết hạn
2. Kết nối mạng không ổn định
3. Nền tảng đang bảo trì

**Giải pháp**:
1. Kiểm tra lại stream key
2. Thử kết nối WiFi khác hoặc 4G
3. Thử lại sau vài phút
4. Kiểm tra trạng thái nền tảng

### Stream bị ngắt giữa chừng

**Triệu chứng**: Mất chỉ báo LIVE, hiển thị lỗi

**Giải pháp**:
1. Nhấn **Thử lại** trong dialog
2. Nếu vẫn lỗi, quay về cấu hình và bắt đầu lại
3. Kiểm tra kết nối mạng
4. Giảm chất lượng stream xuống 480p

### Video không phát

**Triệu chứng**: Màn hình đen, không có preview

**Nguyên nhân có thể**:
1. File video không hỗ trợ
2. File bị hỏng
3. Không có quyền truy cập

**Giải pháp**:
1. Thử file video khác
2. Chuyển đổi video sang MP4 H.264
3. Kiểm tra quyền truy cập storage

### Chất lượng stream kém

**Triệu chứng**: Hình bị vỡ, lag, mờ

**Giải pháp**:
1. Giảm chất lượng stream (1080p → 720p → 480p)
2. Di chuyển đến nơi có WiFi/4G mạnh hơn
3. Đóng các ứng dụng nền khác
4. Khởi động lại thiết bị

### Thiết bị quá nóng

**Triệu chứng**: Cảnh báo nhiệt độ, giảm hiệu năng

**Giải pháp**:
1. Dừng stream tạm thời
2. Tháo ốp lưng điện thoại
3. Đặt thiết bị nơi thoáng mát
4. Giảm chất lượng stream
5. Sử dụng quạt tản nhiệt nếu có

---

## Mẹo sử dụng

### Tối ưu chất lượng stream

1. **Sử dụng WiFi 5GHz**: Ổn định hơn WiFi 2.4GHz
2. **Gần router**: Giảm độ trễ và mất gói
3. **Video gốc chất lượng cao**: Video nguồn kém → stream kém
4. **Tỷ lệ 16:9**: Tránh viền đen

### Tiết kiệm pin

1. Giảm độ sáng màn hình
2. Bật chế độ máy bay + WiFi
3. Sử dụng chất lượng 720p thay vì 1080p
4. Cắm sạc khi stream dài

### Lập lịch stream

1. Chuẩn bị video trước
2. Test stream riêng tư trước khi public
3. Lấy stream key mới trước mỗi phiên
4. Thông báo người xem về lịch stream

---

## FAQ

**Q: Có thể stream khi tắt màn hình không?**
A: Không khuyến nghị. Một số thiết bị sẽ dừng stream khi tắt màn hình.

**Q: Stream key có thời hạn không?**
A: Facebook: 7 ngày. YouTube: Vĩnh viễn (persistent key). TikTok: Mỗi phiên.

**Q: Có thể stream nhiều nền tảng cùng lúc?**
A: Hiện tại không. Cần stream riêng từng nền tảng.

**Q: Video tối đa bao nhiêu GB?**
A: Không giới hạn dung lượng, nhưng thiết bị cần đủ RAM để xử lý.

**Q: Có thể thêm overlay/text vào stream?**
A: Hiện tại không. Cần chỉnh sửa video trước khi stream.

---

## Liên hệ hỗ trợ

Nếu gặp vấn đề không thể giải quyết:

1. Kiểm tra mục [Xử lý sự cố](#xử-lý-sự-cố) ở trên
2. Gửi báo cáo lỗi kèm:
   - Model thiết bị
   - Phiên bản Android
   - Nền tảng stream
   - Mô tả lỗi chi tiết
   - Screenshot nếu có
