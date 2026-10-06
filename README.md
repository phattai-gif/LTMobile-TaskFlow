# LTMobile-TaskFlow: Ứng dụng tổ chức và theo dõi tiến độ công việc

## 👥 Danh sách thành viên nhóm

| STT | Họ và Tên | MSSV | Vai trò |
| :---: | :--- | :---: | :---: |
| 1 | **Trần Phát Tài** | 086206011804 | Leader |
| 2 | **Chu Trí Đạt** | 070206002492 | Member |
| 3 | **Nguyễn Quốc Thái** | 087206004822 | Member |

---

## ✨ Tính năng nổi bật (Key Features)

Ứng dụng được thiết kế theo định hướng **Local-first** (Ưu tiên cục bộ): Hoạt động ngoại tuyến 100%, bảo mật tuyệt đối, tốc độ tức thì và tích hợp các tiện ích nâng cao:

### 1. 📌 Quản lý công việc toàn diện (Core Task Management)
* **Vòng đời CRUD đầy đủ:** Tạo mới, xem chi tiết, chỉnh sửa, xóa (kèm hộp thoại xác nhận) và đánh dấu hoàn thành.
* **Cấu trúc dữ liệu 7 trường nghiệp vụ:** Tiêu đề, Mô tả, Ngày hạn (`DueDate` chuẩn hóa 00:00:00), Giờ nhắc (`DueTime`), Độ ưu tiên (`HIGH` / `MEDIUM` / `LOW`), Trạng thái (`PENDING` / `IN_PROGRESS` / `COMPLETED` / `OVERDUE`), Quy tắc lặp lại.
* **Lọc đa tiêu chí & Sắp xếp linh hoạt:** Lọc theo trạng thái và mức ưu tiên; Sắp xếp theo hạn chót (tăng/giảm dần) hoặc mức ưu tiên.
* **Danh ngôn động lực 5s (Quote Rotator):** Tự động luân chuyển danh ngôn truyền cảm hứng mỗi 5 giây tại màn hình chi tiết công việc.

### 2. 📅 Lịch biểu trực quan (Calendar View)
* Lưới lịch tháng 7 cột tùy biến hiển thị chỉ báo chấm màu (**Event Dots**) tại các ngày có công việc.
* Chọn ngày để lọc và xem danh sách công việc kèm giờ nhắc chi tiết tương ứng trong ngày.

### 3. 🔁 Công việc lặp lại định kỳ (Recurring Tasks)
* **Hỗ trợ các chu kỳ lặp:** Hằng ngày (Daily), Hằng tuần (Weekly), Hằng tháng (Monthly), Hằng năm (Yearly).
* **Tùy chọn nâng cao:** Đặt ngày kết thúc lặp (`repeatEndDate`) hoặc giới hạn số lần lặp (`repeatLimitCount`).
* Thuật toán `RecurrenceHelper` tự động tính toán kỳ hạn tiếp theo khi hoàn thành công việc.

### 4. 🔔 Nhắc nhở chính xác & Tự phục hồi (Exact Alarms & Recovery)
* Hẹn giờ chính xác theo thời gian thực (`AlarmManager.setExactAndAllowWhileIdle()`).
* Kênh thông báo `task_reminders_channel` độ ưu tiên cao kèm âm thanh chuông báo thức, rung và nút hành động nhanh (Hoàn thành / Báo lại 10 phút).
* Tự động đăng ký lại toàn bộ lịch nhắc sau khi thiết bị khởi động lại (`BootReceiver`) hoặc thay đổi múi giờ (`TimeChangeReceiver`).

### 5. 🔒 Bảo mật đa lớp (PIN Lock & Biometric Authentication)
* Khóa ứng dụng bằng mã PIN 4 số tùy biến; băm mật mã bằng thuật toán SHA-256 kèm chuỗi Salt ngẫu nhiên.
* **Cơ chế chống dò mã (Anti-Brute Force):** Tự động khóa tạm thời 30 giây khi nhập sai 5 lần liên tiếp.
* Tích hợp xác thực sinh trắc học vân tay (`AndroidX BiometricPrompt`); tự động vô hiệu hóa vân tay an toàn khi đổi hoặc xóa mã PIN.

### 6. 💾 Sao lưu & Phục hồi an toàn (JSON Backup / Restore via SAF)
* Xuất/nhập tệp sao lưu JSON qua chuẩn Storage Access Framework (SAF), không yêu cầu quyền nguy hiểm truy cập bộ nhớ.
* Bộ thẩm định `BackupValidator` kiểm tra tính toàn vẹn 5 bước (cú pháp, phiên bản, cấu trúc mảng, kiểu dữ liệu, ràng buộc khóa ngoại).
* Cơ chế khôi phục dữ liệu nguyên tử trong một `@Transaction` duy nhất (All-or-Nothing).

### 7. ⏱️ Đồng hồ tập trung Pomodoro (Focus Timer)
* **Chu kỳ kỹ thuật chuẩn:** 25 phút tập trung, 5 phút nghỉ ngắn, 15 phút nghỉ dài.
* Quản lý qua `PomodoroService` (Foreground Service) chạy nền độc lập; thông báo thường trực hỗ trợ Tạm dừng / Bỏ qua / Dừng phiên.
* Máy trạng thái `PomodoroTimerEngine` dùng mốc thời gian đích đơn điệu (`elapsedRealtime()`), loại bỏ sai số đếm lùi.
* Ghi nhận lịch sử phiên vào Room DB và tích hợp thống kê thời gian tập trung theo từng công việc.

### 8. 📊 Thống kê năng suất Bento Grid Dashboard
* Thẻ tỷ lệ hoàn thành dạng cung tròn xoay sinh động (`CircularCompletionRateView`).
* Biểu đồ cột năng suất 7 ngày trong tuần (`WeeklyProductivityChartView`).
* Thống kê phân bổ công việc theo các mức độ ưu tiên.

### 9. 🏆 Hệ thống Gamification: Streak Tracker & 21 Huy hiệu thành tích
* `StreakCalculator` tính toán chuỗi ngày liên tục (`Current Streak`, `Best Streak`), tự động cập nhật khi qua 00:00.
* Thông báo nhắc nhở bảo vệ chuỗi phát lúc 20:00 hằng ngày nếu người dùng chưa hoàn thành công việc nào.
* **Bộ sưu tập 21 Huy hiệu chia làm 3 nhóm:**
  * 🌟 **7 Streak Milestones:** Starter (3 ngày), Sparkstarter (7 ngày), Streaker (30 ngày), Achiever (50 ngày), Champion (100 ngày), Legend (200 ngày), Master (365 ngày).
  * 🎯 **7 Task Done Milestones:** Task Novice (10 tasks), Task Doer (30 tasks), Task Achiever (50 tasks), Task Executor (100 tasks), Task Expert (250 tasks), Task Champion (500 tasks), Task Master (1000 tasks).
  * ⚡ **7 Special Habit Badges:** Early Bird (hoàn thành trước 8h), Night Owl (hoàn thành sau 22h), Weekend Warrior, Pomodoro Master, Century Club, Consistency King, Speed Demon.

### 10. 🤖 Trợ lý ảo AI thông minh & Phím tắt màn hình chính
* **Xử lý ngôn ngữ tự nhiên (NLP):** `TaskCommandParser` trích xuất câu lệnh bằng Regex (Tạo việc, Xem hôm nay, Đếm việc quá hạn).
* **Google Gemini 2.5 Flash:** Kết nối qua Firebase Vertex AI SDK, bảo vệ bảo mật qua Firebase App Check.
* **Launcher App Shortcuts:** Nhấn giữ icon ứng dụng trên màn hình chính để tạo nhanh công việc hoặc xem danh sách việc hôm nay.

### 11. 📱 Home Screen Task Widget
* Widget hiển thị danh sách công việc của ngày hôm nay kèm giờ nhắc.
* Tích hợp Checkbox tương tác trực tiếp trên màn hình chính để hoàn thành công việc mà không cần mở ứng dụng.
* Tự động chuyển sang ngày mới lúc 00:00 (`WidgetMidnightScheduler`) và tự làm mới ngay khi cơ sở dữ liệu thay đổi (`InvalidationTracker`).
