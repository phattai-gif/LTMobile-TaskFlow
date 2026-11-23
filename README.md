# 🚀 LTMobile-TaskFlow: Hệ Thống Quản Lý & Tối Ưu Năng Suất Công Việc Toàn Diện

> **TaskFlow** là ứng dụng di động quản lý công việc và tối ưu năng suất cá nhân/đội ngũ phát triển trên nền tảng **Android Native (Kotlin & Jetpack Compose)**, hoạt động theo triết lý **Local-First & Offline-First** kết hợp với nền tảng điện toán đám mây **Firebase BaaS**. Ứng dụng tích hợp hệ thống đo lường thời gian tập trung Pomodoro chuyên sâu, trợ lý ảo thông minh AI, hệ thống Gamification vinh danh thành tích (Streak & Badges) cùng cơ chế bảo mật sinh trắc học và mã hóa dữ liệu đa lớp.

---

## 📑 Mục Lục
- [1. Giới Thiệu Hệ Thống](#1-giới-thiệu-hệ-thống)
  - [1.1. Bối cảnh & Vấn đề thực tế](#11-bối-cảnh--vấn-đề-thực-tế)
  - [1.2. Giải pháp hệ thống TaskFlow](#12-giải-pháp-hệ-thống-taskflow)
  - [1.3. Bảng phân quyền đối tượng sử dụng](#13-bảng-phân-quyền-đối-tượng-sử-dụng)
  - [1.4. Nguyên tắc thiết kế & Kiến trúc hệ thống](#14-nguyên-tắc-thiết-kế--kiến-trúc-hệ-thống)
- [2. Danh Sách Thành Viên Nhóm](#2-danh-sách-thành-viên-nhóm)
- [3. Bảng Tổng Hợp Các Tính Năng Chính](#3-bảng-tổng-hợp-các-tính-năng-chính)
- [4. Bảng Công Nghệ Sử Dụng (Tech Stack)](#4-bảng-công-nghệ-sử-dụng-tech-stack)
- [5. Quản Lý Database & Cấu Trúc Dữ Liệu](#5-quản-lý-database--cấu-trúc-dữ-liệu)
  - [5.1. Cơ sở dữ liệu cục bộ (Room ORM / SQLite)](#51-cơ-sở-dữ-liệu-cục-bộ-room-orm--sqlite)
  - [5.2. Cấu trúc SharedPreferences](#52-cấu-trúc-sharedpreferences)
  - [5.3. Lưu trữ & Đồng bộ Đám mây (Firebase Cloud)](#53-lưu-trữ--đồng-bộ-đám-mây-firebase-cloud)
- [6. Cấu Trúc Thư Mục (Project Structure)](#6-cấu-trúc-thư-mục-project-structure)
- [7. Hướng Dẫn Cài Đặt & Khởi Chạy (How to Run)](#7-hướng-dẫn-cài-đặt--khởi-chạy-how-to-run)
  - [7.1. Yêu cầu môi trường phát triển](#71-yêu-cầu-môi-trường-phát-triển)
  - [7.2. Các bước Clone & Mở dự án](#72-các-bước-clone--mở-dự-án)
  - [7.3. Cấu hình biến môi trường & Firebase Services](#73-cấu-hình-biến-môi-trường--firebase-services)
  - [7.4. Biên dịch & Khởi chạy ứng dụng](#74-biên-dịch--khởi-chạy-ứng-dụng)
  - [7.5. Xử lý các lỗi thường gặp (Troubleshooting)](#75-xử-lý-các-lỗi-thường-gặp-troubleshooting)

---

## 1. Giới Thiệu Hệ Thống

### 1.1. Bối cảnh & Vấn đề thực tế
Trong kỷ nguyên số hóa với lượng thông tin bùng nổ, cá nhân và đội nhóm thường xuyên đối mặt với:
- **Tình trạng quá tải nhiệm vụ (Task Overload):** Thiếu công cụ phân loại mức độ ưu tiên khoa học, dẫn đến trễ hạn (overdue) và giảm sút hiệu suất làm việc.
- **Sự gián đoạn kết nối mạng:** Nhiều ứng dụng quản lý hiện nay phụ thuộc 100% vào Internet. Khi người dùng di chuyển vào vùng sóng yếu, máy bay hoặc mất mạng, họ hoàn toàn bị cô lập khỏi dữ liệu công việc của chính mình.
- **Mất tập trung & Rủi ro kiệt sức (Burnout):** Thiếu phương pháp cân bằng giữa thời gian làm việc sâu (Deep Work) và nghỉ ngơi khoa học.
- **Vấn đề bảo mật thông tin cá nhân:** Các ghi chú, kế hoạch bảo mật nội bộ cần lớp khóa bảo vệ độc lập ngay cả khi thiết bị di động đã được mở khóa màn hình ngoài.

### 1.2. Giải pháp hệ thống TaskFlow
**TaskFlow** được thiết kế để giải quyết triệt để các bài toán trên thông qua giải pháp công nghệ toàn diện:
1. **Kiến trúc Local-First & Offline-First:** Ứng dụng hoạt động độc lập hoàn toàn trên thiết bị thông qua SQLite/Room Database. Người dùng có thể tạo, sửa, xóa, thống kê công việc mà không cần kết nối mạng. Dữ liệu sẽ đồng bộ đám mây và phân tích hành vi người dùng khi có kết nối Internet thông qua **Firebase Services**.
2. **Hệ thống Quản lý Vòng đời Công việc Nâng cao:** Tích hợp quy tắc lặp lại thông minh (Recurring Logic), tự động sinh công việc kỳ hạn tiếp theo dựa trên chu kỳ ngày/tuần/tháng/năm kèm giới hạn ngày kết thúc hoặc số lần lặp.
3. **Đồng hồ tập trung Pomodoro chuyên sâu:** Triển khai **Android Foreground Service** độc lập, duy trì đếm giờ chuẩn xác đến từng mili-giây ngay cả khi ứng dụng bị thu nhỏ hoặc màn hình tắt, hỗ trợ điều khiển trực tiếp trên thanh thông báo.
4. **Hệ thống Gamification tăng động lực:** Tự động tính chuỗi ngày hoàn thành liên tiếp (Streak Tracker) và mở khóa **21 Huy hiệu danh hiệu** kích thích tinh thần làm việc.
5. **Bảo mật đa tầng:** Mã hóa mã PIN bằng thuật toán băm **SHA-256 kèm Salt ngẫu nhiên**, kết hợp cơ chế khóa ứng dụng tự động và chống tấn công dò mã (Brute-force lockout).

### 1.3. Bảng phân quyền đối tượng sử dụng
Hệ thống được thiết kế hướng tới hỗ trợ linh hoạt từ người dùng cá nhân đến mô hình tổ chức nhóm:

| Đối tượng (Role) | Mô tả vai trò | Quyền hạn trong hệ thống |
| :--- | :--- | :--- |
| **Guest / Anonymous User** | Người dùng trải nghiệm cục bộ | - Sử dụng đầy đủ tính năng cốt lõi Offline (CRUD Task, Pomodoro, Calendar, Badges).<br>- Lưu trữ dữ liệu an toàn trên SQLite cục bộ.<br>- Khóa mã PIN bảo vệ ứng dụng. |
| **Authenticated Member** | Thành viên đã đăng nhập tài khoản | - Kế thừa toàn bộ quyền của Guest.<br>- Đồng bộ thông tin danh tính qua Firebase Authentication (Email/Password hoặc Google Sign-In).<br>- Đồng bộ dữ liệu lên Firebase Realtime Database.<br>- Tự động sao lưu và phục hồi dữ liệu qua JSON Backup chuẩn SAF. |
| **Team Leader / Project Owner** | Trưởng nhóm / Quản trị viên dự án | - Toàn quyền quản trị luồng công việc.<br>- Xuất báo cáo hiệu suất, tỷ lệ hoàn thành (Completion Rate), tổng giờ tập trung Pomodoro.<br>- Xuất file dữ liệu dự án chuẩn hóa phục vụ phân tích hoặc chuyển giao. |

### 1.4. Nguyên tắc thiết kế & Kiến trúc hệ thống
Hệ thống tuân thủ nghiêm ngặt các tiêu chuẩn kỹ thuật hiện đại của Google:
- **Kiến trúc MVVM (Model - View - ViewModel):** Tách biệt rõ ràng tầng Giao diện (UI Layer), tầng Trình bày & Logic trạng thái (State & ViewModel Layer), và tầng Dữ liệu (Repository & Data Layer).
- **Single Source of Truth (SSOT):** Room Database đóng vai trò là nguồn dữ liệu chuẩn mực duy nhất. Mọi luồng dữ liệu đều được phát ra dưới dạng reactive stream (`Flow<T>` / `StateFlow<T>`) từ Database lên UI.
- **Unidirectional Data Flow (UDF):** Trạng thái (UI State) đi từ ViewModel xuống Composable; Sự kiện hành động (Events/User Actions) đi từ Composable ngược lên ViewModel.
- **Material Design 3 (M3):** Thiết kế giao diện phẳng hiện đại, chuyển sắc dynamic color, bo góc tiêu chuẩn và tối ưu không gian hiển thị theo lưới tỉ lệ vàng Figma.

---

## 2. Danh Sách Thành Viên Nhóm

| STT | Họ và Tên | Mã Số Sinh Viên | Vai Trò Đảm Nhiệm | Kênh Liên Hệ / Email |
| :---: | :--- | :---: | :---: | :--- |
| 1 | **Trần Phát Tài** | `086206011804` | **Team Leader** & Full-stack Mobile Dev | taiphattran2006@gmail.com |
| 2 | **Chu Trí Đạt** | `070206002492` | Core Member & Module Developer | dat.chu@example.com |
| 3 | **Nguyễn Quốc Thái** | `087206004822` | Core Member & QA / Testing | thai.nguyen@example.com |

---

## 3. Bảng Tổng Hợp Các Tính Năng Chính

Dưới đây là chi tiết các tính năng nghiệp vụ đã được xây dựng và xác thực 100% trong mã nguồn dự án:

| STT | Tên tính năng | Tệp mã nguồn phụ trách | Mô tả chi tiết luồng hoạt động dựa trên mã nguồn |
| :---: | :--- | :--- | :--- |
| **01** | **Xác thực người dùng (Authentication)** | `ui/auth/AuthViewModel.kt`<br>`ui/auth/LoginScreen.kt`<br>`ui/auth/RegisterScreen.kt` | - Đăng nhập/Đăng ký tài khoản qua **Firebase Authentication** bằng Email/Password.<br>- Hỗ trợ đăng nhập nhanh một chạm qua **Google Sign-In** (`GoogleAuthProvider`).<br>- Lắng nghe trạng thái đăng nhập tự động (`AuthStateListener`), cập nhật StateFlow thời gian thực.<br>- Ghi nhận sự kiện chuyển đổi người dùng lên Firebase Analytics. |
| **02** | **Quản lý công việc (Task CRUD)** | `ui/screens/CreateTaskScreen.kt`<br>`ui/activity/AddEditTaskActivity.kt`<br>`ui/detail/TaskDetailContent.kt` | - Tạo mới, xem chi tiết, chỉnh sửa và xóa task (có hộp thoại xác nhận an toàn).<br>- Hỗ trợ nhập liệu tiếng Việt chuẩn có dấu với `TextFieldValue` bảo toàn con trỏ.<br>- Các trường nghiệp vụ: Tiêu đề, Mô tả, Hạn chót (DueDate), Giờ nhắc (DueTime), Mức độ ưu tiên (`URGENT`, `HIGH`, `MEDIUM`, `LOW`), Trạng thái (`TODO`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`). |
| **03** | **Công việc lặp lại (Recurring Tasks)** | `util/RecurrenceHelper.kt`<br>`util/TaskCompletionHelper.kt` | - Hỗ trợ các chu kỳ: Hằng ngày (DAILY), Hằng tuần (WEEKLY), Hằng tháng (MONTHLY), Hằng năm (YEARLY).<br>- Tùy chọn giới hạn ngày kết thúc (`repeatEndDate`) hoặc giới hạn số lần lặp (`repeatLimitCount`).<br>- Khi người dùng tick hoàn thành task cha, `TaskCompletionHelper` tự động tính toán mốc thời gian kỳ tiếp theo và insert một bản ghi Task mới độc lập vào Room DB. |
| **04** | **Báo thức & Nhắc việc chính xác (Exact Alarms)** | `util/AlarmScheduler.kt`<br>`receiver/AlarmReceiver.kt`<br>`util/TaskReminderTiming.kt` | - Sử dụng Android `AlarmManager.setExactAndAllowWhileIdle()` để đánh thức thiết bị và phát thông báo chuẩn xác từng phút.<br>- Hỗ trợ các mốc nhắc trước: 5 phút, 10 phút, 15 phút, 30 phút, 1 giờ hoặc đúng giờ.<br>- Kênh thông báo Notification Channel ưu tiên cao kèm cờ báo rung (`VIBRATE`) và âm thanh thông báo. |
| **05** | **Khôi phục báo thức tự động (Alarm Recovery)** | `receiver/BootReceiver.kt`<br>`receiver/TimeChangeReceiver.kt`<br>`util/TaskReminderRecovery.kt` | - Đón nhận các Intent hệ thống: `ACTION_BOOT_COMPLETED` (sau khi khởi động lại máy), `TIME_SET` và `TIMEZONE_CHANGED` (thay đổi ngày giờ/múi giờ hệ thống).<br>- Tự động truy vấn lại danh sách task chưa hoàn thành từ Database và tái đăng ký lại toàn bộ lịch hẹn giờ báo thức. |
| **06** | **Đồng hồ tập trung Pomodoro (Focus Timer)** | `ui/pomodoro/PomodoroScreen.kt`<br>`pomodoro/PomodoroService.kt`<br>`pomodoro/PomodoroTimerEngine.kt` | - Đồng hồ kỹ thuật Pomodoro chuẩn: 25 phút tập trung (Focus), 5 phút nghỉ ngắn (Short Break), 15 phút nghỉ dài (Long Break).<br>- Chạy nền dưới dạng **Foreground Service** (`specialUse` FGS Type) với thanh điều khiển Notification tương tác liên tục.<br>- Thuật toán đếm giờ chuẩn dựa trên `SystemClock.elapsedRealtime()`, loại bỏ triệt để sai số giật lag khi CPU ngủ.<br>- Tự động tích lũy số phút tập trung và lưu phiên vào `pomodoro_sessions`. |
| **07** | **Lịch biểu trực quan (Calendar Schedule)** | `ui/calendar/CalendarScreen.kt`<br>`ui/calendar/CalendarViewModel.kt`<br>`ui/calendar/CalendarDateHelper.kt` | - Lưới hiển thị lịch tháng 7 cột trực quan.<br>- Đánh dấu chỉ báo chấm màu (**Event Dots**) tại các ngày có công việc tương ứng.<br>- Cho phép chọn từng ngày để duyệt danh sách các task trong ngày theo trình tự thời gian. |
| **08** | **Thống kê năng suất (Productivity Stats)** | `ui/stats/StatsScreen.kt`<br>`ui/stats/StatsViewModel.kt` | - Trực quan hóa số liệu theo mô hình Bento Grid Dashboard.<br>- Hiển thị tỷ lệ hoàn thành (Completion Rate) dạng vòng tròn xoay sinh động.<br>- Thống kê biểu đồ cột năng suất 7 ngày trong tuần và phân bổ task theo 4 cấp độ ưu tiên.<br>- Tổng hợp tổng số phút và số phiên Pomodoro đã hoàn thành. |
| **09** | **Chuỗi thói quen & 21 Huy hiệu (Streak & Badges)** | `ui/streak/StreakViewModel.kt`<br>`ui/badges/BadgesViewModel.kt`<br>`ui/badges/BadgesScreen.kt` | - Tự động tính chuỗi ngày liên tục hoàn thành công việc (`Current Streak`, `Best Streak`).<br>- Hệ thống **21 Huy hiệu danh hiệu** chia làm 3 nhóm:<br>  + *7 Mốc chuỗi ngày (Streak Milestones):* Starter (3d), Sparkstarter (7d), Streaker (30d), Achiever (50d), Champion (100d), Legend (200d), Master (365d).<br>  + *7 Mốc số lượng task (Task Done Milestones):* Novice (10), Doer (30), Achiever (50), Executor (100), Expert (250), Champion (500), Master (1000).<br>  + *7 Huy hiệu thói quen đặc biệt:* Early Bird, Night Owl, Weekend Warrior, Pomodoro Master, Century Club, Consistency King, Speed Demon. |
| **10** | **Bảo mật mã PIN đa lớp (App PIN Security)** | `util/PinManager.kt`<br>`ui/pin/SetPinScreen.kt` | - Bảo vệ ứng dụng bằng mã PIN 4 chữ số độc lập.<br>- Lưu mật mã băm dưới dạng **SHA-256 kèm chuỗi Salt ngẫu nhiên** trong SharedPreferences.<br>- Thuật toán chống dò mã (Anti-Brute Force): Tự động đếm số lần nhập sai liên tiếp và áp đặt thời gian khóa (Cooldown Lockout). |
| **11** | **Sao lưu & Thẩm định dữ liệu (Backup & Restore)** | `util/BackupManager.kt`<br>`data/validator/BackupValidator.kt`<br>`ui/data/DataManagementScreen.kt`<br>`ui/data/RestoreTasksScreen.kt` | - Xuất/Nhập file JSON qua Android Storage Access Framework (SAF) chuẩn hóa.<br>- Bộ thẩm định **BackupValidator** 5 bước: Kiểm tra cú pháp JSON, kiểm tra version schema, phát hiện payload rỗng, thẩm định kiểu dữ liệu từng trường và đối soát xung đột khóa.<br>- Cung cấp 2 chế độ khôi phục dữ liệu: **Ghi đè hoàn toàn (Overwrite)** hoặc **Hợp nhất thông minh (Merge)**. |
| **12** | **Trợ lý ảo phân tích NLP (AI Task Assistant)** | `ui/AiAssistantBottomSheet.kt` | - Màn hình nhập liệu thông minh dạng BottomSheet.<br>- Tích hợp bộ bóc tách ngôn ngữ tự nhiên (NLP Regex Parser) cho phép nhập câu lệnh tự do (VD: *"Học Kotlin lúc 20:00 tối mai mức Urgent"*) để tự động trích xuất tiêu đề, ngày giờ và độ ưu tiên.<br>- Cung cấp sẵn các danh thiếp gợi ý (Quick Prompt Pills) thao tác nhanh. |

---

## 4. Bảng Công Nghệ Sử Dụng (Tech Stack)

```
+--------------------------------------------------------------------------------+
|                               TaskFlow Tech Stack                              |
+--------------------------------------------------------------------------------+
|  Mobile Client   : Kotlin 2.0.21, Jetpack Compose M3, Coroutines, StateFlow    |
|  Local Storage   : Android Room ORM 2.6.1, SQLite, SharedPreferences           |
|  Cloud Backend   : Firebase Auth, Firebase Realtime Database, Analytics        |
|  Core Services   : Foreground Service (Pomodoro), AlarmManager (Exact Alarm)   |
|  Build & Tooling : Gradle 8.9, AGP 8.7.3, KSP 2.0.21, Target SDK 35 (Android 15) |
+--------------------------------------------------------------------------------+
```

| Thành phần kiến trúc | Công nghệ / Thư viện | Phiên bản | Mục đích & Chức năng trong hệ thống |
| :--- | :--- | :---: | :--- |
| **Ngôn ngữ lập trình** | **Kotlin** | `2.0.21` | Ngôn ngữ phát triển ứng dụng di động chính thức, an toàn kiểu dữ liệu (null-safety), hỗ trợ Coroutines. |
| **Giao diện người dùng** | **Jetpack Compose Material 3** | BOM `2024.10.01` | Khung giao diện lập trình khai báo (Declarative UI) hiện đại nhất của Google, hỗ trợ animations, adaptive theme. |
| **Điều hướng ứng dụng** | **Navigation Compose** | `2.8.9` | Điều hướng giữa các Composable Screens thông qua hệ thống định tuyến (Routes & Destinations). |
| **Quản lý vòng đời** | **AndroidX Lifecycle & ViewModel** | `2.8.7` | Quản lý trạng thái màn hình độc lập với chu kỳ xoay máy, tích hợp `viewModelScope`. |
| **Xử lý bất đồng bộ** | **Kotlinx Coroutines & Flow** | `1.8.1` | Lập trình phản ứng bất đồng bộ (Reactive Programming), quản lý tác vụ ngầm non-blocking. |
| **Cơ sở dữ liệu cục bộ** | **Android Room ORM** | `2.6.1` | Tầng trừu tượng hóa trên nền SQLite; hỗ trợ Transaction, TypeConverters và Reactive Flow Queries. |
| **Trình xử lý Annotation** | **Google KSP (Kotlin Symbol Processing)**| `2.0.21-1.0.27`| Trình phân tích cú pháp mã nguồn hiệu năng cao thay thế cho KAPT, dùng cho Room Database Compiler. |
| **Xác thực đám mây** | **Firebase Authentication** | BOM `33.1.0` | Quản lý định danh người dùng qua Email/Mật khẩu và Google Sign-In. |
| **Cơ sở dữ liệu đám mây** | **Firebase Realtime Database** | BOM `33.1.0` | Lưu trữ dữ liệu đám mây và đồng bộ trạng thái tài khoản. |
| **Phân tích hành vi** | **Firebase Analytics** | BOM `33.1.0` | Theo dõi tần suất đăng nhập, tính năng sử dụng và luồng thao tác của người dùng. |
| **Đăng nhập Google** | **Play Services Auth** | `21.2.0` | SDK tích hợp giao diện Google One-Tap Sign-In và lấy OAuth ID Token. |
| **Tuần hoàn dữ liệu JSON**| **Google Gson** | `2.10.1` | Đọc/ghi và chuyển đổi thực thể sang chuỗi JSON phục vụ tính năng Backup & Restore. |
| **Kiểm thử tự động** | **JUnit 4, Espresso, Compose Test**| `4.13.2` / `3.6.1`| Kiểm thử đơn vị (Unit Tests), kiểm thử logic Repository và kiểm thử giao diện Compose. |
| **Hệ thống Build** | **Android Gradle Plugin (AGP)** | `8.7.3` | Trình biên dịch mã nguồn, tích hợp R8/Proguard và đóng gói tệp APK/AAB. |

---

## 5. Quản Lý Database & Cấu Trúc Dữ Liệu

### 5.1. Cơ sở dữ liệu cục bộ (Room ORM / SQLite)
Cơ sở dữ liệu cục bộ của TaskFlow mang tên `task_database` (phiên bản schema hiện tại: `5`), gồm 2 bảng quan hệ chặt chẽ:

#### 1. Bảng `tasks` (Quản lý thông tin công việc)
```sql
CREATE TABLE IF NOT EXISTS `tasks` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `title` TEXT NOT NULL,
    `description` TEXT NOT NULL,
    `dueDate` INTEGER NOT NULL,
    `dueTime` INTEGER NOT NULL,
    `priority` TEXT NOT NULL,             -- URGENT, HIGH, MEDIUM, LOW
    `status` TEXT NOT NULL,               -- TODO, IN_PROGRESS, COMPLETED, CANCELLED
    `isComplete` INTEGER NOT NULL,        -- 0: Chưa hoàn thành, 1: Đã hoàn thành
    `isRecurring` INTEGER NOT NULL,       -- 0: Không lặp, 1: Có lặp lại
    `recurrenceType` TEXT NOT NULL,       -- NONE, DAILY, WEEKLY, MONTHLY, YEARLY
    `recurrenceInterval` INTEGER NOT NULL DEFAULT 1,
    `reminderMinutes` INTEGER NOT NULL DEFAULT 0,
    `repeatEndDate` INTEGER NOT NULL DEFAULT 0,
    `repeatLimitCount` INTEGER NOT NULL DEFAULT 0,
    `currentOccurrence` INTEGER NOT NULL DEFAULT 1,
    `isPaused` INTEGER NOT NULL DEFAULT 0,
    `createdAt` INTEGER NOT NULL,
    `updatedAt` INTEGER NOT NULL,
    `estimatedPomodoros` INTEGER NOT NULL DEFAULT 0,
    `completedPomodoros` INTEGER NOT NULL DEFAULT 0,
    `totalFocusTimeMinutes` INTEGER NOT NULL DEFAULT 0,
    `completedAt` INTEGER                 -- Timestamp khi hoàn thành, dùng cho Streak & Badges
);
```

#### 2. Bảng `pomodoro_sessions` (Lịch sử các phiên tập trung)
```sql
CREATE TABLE IF NOT EXISTS `pomodoro_sessions` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `taskId` INTEGER NOT NULL,
    `startTime` INTEGER NOT NULL,
    `endTime` INTEGER NOT NULL,
    `durationInMinutes` INTEGER NOT NULL,
    `sessionType` TEXT NOT NULL,          -- FOCUS, SHORT_BREAK, LONG_BREAK
    `isCompleted` INTEGER NOT NULL,       -- 1: Hoàn thành trọn vẹn, 0: Bị ngắt quãng
    FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS `index_pomodoro_sessions_taskId` ON `pomodoro_sessions` (`taskId`);
CREATE INDEX IF NOT EXISTS `index_pomodoro_sessions_startTime` ON `pomodoro_sessions` (`startTime`);
```

### 5.2. Cấu trúc SharedPreferences
Dùng để lưu trữ cấu hình bảo mật và cài đặt hệ thống nhanh:
- **`app_pin_preferences`:**
  - `pin_hash`: Chuỗi băm SHA-256 của mã PIN kèm Salt ngẫu nhiên.
  - `pin_salt`: Chuỗi Salt sinh ngẫu nhiên theo thiết bị.
  - `failed_attempts`: Số lần nhập sai mã PIN liên tiếp.
  - `lockout_until_timestamp`: Mốc thời gian kết thúc khóa ứng dụng do nhập sai quá 5 lần.
  - `is_pin_enabled`: Cờ bật/tắt tính năng khóa ứng dụng.
- **`pomodoro_settings`:**
  - `focus_duration`: Thời gian làm việc sâu (mặc định: 25 phút).
  - `short_break_duration`: Thời gian nghỉ ngắn (mặc định: 5 phút).
  - `long_break_duration`: Thời gian nghỉ dài (mặc định: 15 phút).
  - `auto_start_breaks`: Tự động bắt đầu giờ nghỉ ngơi.

### 5.3. Lưu trữ & Đồng bộ Đám mây (Firebase Cloud)
- **Firebase Authentication:** Lưu trữ User UID, Email, Display Name, Photo URL, Provider Data (Google / Password).
- **Firebase Realtime Database:** Đồng bộ dữ liệu sao lưu đám mây theo nút người dùng `/users/{uid}/backups/`.
- **Firebase Analytics:** Thu thập các sự kiện người dùng (`login`, `task_create`, `task_complete`, `pomodoro_finish`).

---

## 6. Cấu Trúc Thư Mục (Project Structure)

Dưới đây là sơ đồ cây cấu trúc thư mục thực tế của dự án kèm chú thích chức năng từng gói:

```
LTMobile-TaskFlow/
│
├── .gitignore                          # Cấu hình bỏ qua các file build tạm thời, IDE và keys
├── README.md                           # Tài liệu kỹ thuật chi tiết của toàn bộ dự án
│
└── Code/                               # Thư mục gốc của dự án Android Studio
    ├── build.gradle.kts                # Cấu hình Gradle Root (KSP, Google Services, Plugins)
    ├── settings.gradle.kts             # Định nghĩa module và khai báo kho lưu trữ Maven/Google
    ├── gradle/
    │   └── libs.versions.toml          # Quản lý phiên bản tập trung (Version Catalog)
    │
    └── app/                            # Module ứng dụng chính (Application Module)
        ├── build.gradle.kts            # Cấu hình dependencies, SDK 35, Jetpack Compose, Room
        ├── proguard-rules.pro          # Quy tắc làm gọn và bảo vệ mã nguồn khi build Release
        │
        └── src/
            ├── main/
            │   ├── AndroidManifest.xml # Khai báo quyền, Activities, Receivers, Foreground Services
            │   │
            │   ├── java/com/team/taskmanagementapp/
            │   │   ├── MainActivity.kt         # Single-Activity làm điểm neo khởi chạy Compose UI
            │   │   ├── TaskApplication.kt      # Application Class khởi tạo Database & Notification Channels
            │   │   │
            │   │   ├── data/                   # TẦNG DỮ LIỆU (DATA LAYER)
            │   │   │   ├── local/
            │   │   │   │   ├── dao/            # Data Access Objects (TaskDao, PomodoroDao)
            │   │   │   │   ├── db/             # Room Database & TypeConverters (Enums, Dates)
            │   │   │   │   └── entity/         # Room Entities (Task, PomodoroSession)
            │   │   │   ├── model/              # Data Models, Enums (Priority, Status, Recurrence)
            │   │   │   ├── repository/         # Repository Pattern (TaskRepository, PomodoroRepository)
            │   │   │   └── validator/          # Bộ thẩm định tính toàn vẹn dữ liệu (BackupValidator)
            │   │   │
            │   │   ├── pomodoro/               # MODULE POMODORO FOCUS ENGINE
            │   │   │   ├── PomodoroService.kt          # Android Foreground Service đếm giờ ngầm
            │   │   │   ├── PomodoroTimerEngine.kt      # Máy trạng thái tính giờ chuẩn bằng elapsedRealtime
            │   │   │   ├── PomodoroTimerController.kt  # Interface giao tiếp giữa UI và Background Service
            │   │   │   └── PomodoroCompletionTracker.kt# Ghi nhận và lưu phiên tập trung vào Database
            │   │   │
            │   │   ├── receiver/               # BROADCAST RECEIVERS
            │   │   │   ├── AlarmReceiver.kt            # Đón Intent phát báo thức và tạo Notification
            │   │   │   ├── BootReceiver.kt             # Khôi phục lịch hẹn sau khi máy khởi động lại
            │   │   │   └── TimeChangeReceiver.kt       # Khôi phục lịch hẹn sau khi đổi ngày giờ hệ thống
            │   │   │
            │   │   ├── ui/                     # TẦNG GIAO DIỆN (JETPACK COMPOSE UI)
            │   │   │   ├── TaskFlowApp.kt              # Scaffold trung tâm & Navigation Host
            │   │   │   ├── AiAssistantBottomSheet.kt   # Trợ lý ảo AI & Phân tích ngôn ngữ NLP
            │   │   │   ├── activity/                   # Các màn hình dạng Activity độc lập
            │   │   │   ├── auth/                       # Màn hình Chào mừng, Đăng nhập & Đăng ký
            │   │   │   ├── badges/                     # Màn hình & ViewModel hệ thống 21 Huy hiệu
            │   │   │   ├── calendar/                   # Màn hình Lịch biểu & Lưới sự kiện theo tháng
            │   │   │   ├── components/                 # Các Composable dùng chung (BottomBar, Dialogs)
            │   │   │   ├── data/                       # Màn hình Quản lý dữ liệu, Xuất & Phục hồi JSON
            │   │   │   ├── detail/                     # Màn hình xem chi tiết công việc
            │   │   │   ├── home/                       # Dashboard chính, Thống kê nhanh & Danh sách task
            │   │   │   ├── navigation/                 # Định nghĩa Routes & Destinations
            │   │   │   ├── pin/                        # Màn hình bàn phím số thiết lập & nhập mã PIN
            │   │   │   ├── pomodoro/                   # Giao diện đồng hồ đếm ngược Pomodoro & Cài đặt
            │   │   │   ├── screens/                    # Màn hình Tạo mới công việc (CreateTaskScreen)
            │   │   │   ├── settings/                   # Màn hình Cài đặt cá nhân, Bảo mật, Dữ liệu
            │   │   │   ├── stats/                      # Màn hình Bento Grid thống kê năng suất 7 ngày
            │   │   │   ├── streak/                     # Màn hình theo dõi chuỗi hoàn thành (Streak)
            │   │   │   ├── theme/                      # Hệ thống Design Tokens (Màu sắc, Typography, Shapes)
            │   │   │   └── viewmodel/                  # ViewModels quản lý trạng thái từng phân hệ
            │   │   │
            │   │   └── util/                   # TIỆN ÍCH HỆ THỐNG (UTILITIES)
            │   │       ├── AlarmScheduler.kt           # Tiện ích tương tác với Android AlarmManager
            │   │       ├── BackupManager.kt            # Xuất/Nhập dữ liệu JSON qua Storage Access Framework
            │   │       ├── DateTimeUtils.kt            # Định dạng và chuẩn hóa mốc ngày giờ
            │   │       ├── NotificationHelper.kt       # Tạo và hiển thị các kênh Notification
            │   │       ├── PinManager.kt               # Quản lý mã hóa SHA-256 & Khóa chống Brute-force
            │   │       ├── RecurrenceHelper.kt         # Thuật toán tính ngày đến hạn của task lặp
            │   │       └── TaskCompletionHelper.kt     # Xử lý hoàn thành task và sinh task chu kỳ mới
            │   │
            │   └── res/                        # TÀI NGUYÊN (RESOURCES)
            │       ├── drawable/               # Icons vector và hình minh họa XML
            │       ├── mipmap/                 # App Launcher Icons đa kích thước (hdpi -> xxxhdpi)
            │       └── values/                 # Chuỗi văn bản đa ngôn ngữ, Styles và Màu sắc
            │
            └── test/                           # BỘ KIỂM THỬ ĐƠN VỊ (UNIT TESTS)
                └── java/com/team/taskmanagementapp/
                    ├── BackupValidatorTest.kt          # Kiểm thử bộ thẩm định dữ liệu sao lưu
                    ├── RecurrenceHelperTest.kt         # Kiểm thử thuật toán tính chu kỳ lặp
                    └── PomodoroTimerEngineTest.kt      # Kiểm thử máy trạng thái đồng hồ tập trung
```

---

## 7. Hướng Dẫn Cài Đặt & Khởi Chạy (How to Run)

### 7.1. Yêu cầu môi trường phát triển
Để đảm bảo dự án biên dịch mượt mà không gặp lỗi không tương thích, máy tính của bạn cần đáp ứng:
- **Hệ điều hành:** Windows 10/11 (64-bit), macOS (Apple Silicon M-series hoặc Intel) hoặc Linux Ubuntu 20.04+.
- **Java Development Kit (JDK):** **JDK 17** (Temurin 17, Oracle JDK 17 hoặc Android Studio Embedded JDK 17).
- **Môi trường phát triển tích hợp (IDE):** **Android Studio Ladybug (2024.2.1+)** hoặc **Koala**.
- **Android SDK:**
  - `compileSdk`: **35** (Android 15)
  - `minSdk`: **26** (Android 8.0 Oreo - tương thích 95%+ thiết bị hiện nay)
  - `targetSdk`: **35**
- **Thiết bị chạy:**
  - *Máy ảo (Android Emulator):* Google Pixel 7/8/9 chạy hệ điều hành từ Android 10.0 (API 29) đến Android 15 (API 35), đã cài đặt Google Play Services.
  - *Thiết bị thật:* Bật chế độ Nhà phát triển (**Developer Options**) và **USB Debugging**.

### 7.2. Các bước Clone & Mở dự án
Mở terminal (Git Bash, Command Prompt hoặc PowerShell) và chạy lệnh:

```bash
# 1. Clone mã nguồn từ GitHub Repository chính thức
git clone https://github.com/phattai-gif/LTMobile-TaskFlow.git

# 2. Di chuyển vào thư mục dự án
cd LTMobile-TaskFlow

# 3. Kiểm tra nhánh làm việc mới nhất
git checkout feature/ui-auth-figma
```

Mở IDE **Android Studio**:
- Chọn **File** -> **Open...**
- Điều hướng và chọn thư mục `Code/` bên trong thư mục vừa clone (Đảm bảo mở đúng thư mục `Code` - nơi chứa file `settings.gradle.kts`).
- Chờ IDE tải các dependencies và hoàn tất tiến trình **Gradle Sync**.

### 7.3. Cấu hình biến môi trường & Firebase Services

#### 1. Cấu hình đường dẫn Android SDK (`local.properties`)
Nếu Android Studio chưa tự sinh, hãy tạo file `local.properties` tại thư mục `Code/local.properties`:

*Trên Windows:*
```properties
sdk.dir=C\:\\Users\\<Ten_User_Cua_Ban>\\AppData\\Local\\Android\\Sdk
```

*Trên macOS:*
```properties
sdk.dir=/Users/<Ten_User_Cua_Ban>/Library/Android/sdk
```

#### 2. Cấu hình Firebase (`google-services.json`)
Dự án sử dụng Firebase cho xác thực và đo lường. File `google-services.json` cần được đặt tại:
```
LTMobile-TaskFlow/Code/app/google-services.json
```
> [!NOTE]
> Dự án đã tích hợp sẵn file cấu hình `google-services.json` của Firebase dự án TaskFlow liên kết với package `com.team.taskmanagementapp`. Nếu bạn muốn dùng dự án Firebase cá nhân, hãy tải file từ **Firebase Console -> Project Settings -> General -> Your Apps** và thay thế vào thư mục trên.

### 7.4. Biên dịch & Khởi chạy ứng dụng

#### Cách 1: Chạy trực tiếp từ giao diện Android Studio
1. Chọn thiết bị chạy trên thanh công cụ (chọn máy ảo Pixel hoặc điện thoại thật đã cắm cáp USB).
2. Chọn configuration là **`app`**.
3. Nhấn nút **Run** (biểu tượng tam giác xanh ▶️) hoặc phím tắt `Shift + F10` (Windows) / `Control + R` (macOS).

#### Cách 2: Biên dịch và chạy bằng dòng lệnh Gradle Wrapper
Mở Terminal tại thư mục `Code/`:

*Trên Windows:*
```cmd
# Kiểm tra biên dịch mã nguồn Kotlin
gradlew.bat compileDebugKotlin

# Đóng gói file cài đặt Debug APK
gradlew.bat assembleDebug

# Cài đặt trực tiếp lên thiết bị đang kết nối
gradlew.bat installDebug
```

*Trên macOS / Linux:*
```bash
chmod +x gradlew
./gradlew compileDebugKotlin
./gradlew assembleDebug
./gradlew installDebug
```
*Tệp tin APK sau khi build thành công sẽ nằm tại: `Code/app/build/outputs/apk/debug/app-debug.apk`.*

### 7.5. Xử lý các lỗi thường gặp (Troubleshooting)

1. **Lỗi `JDK Version Mismatch` hoặc `Unsupported class file major version`:**
   - *Nguyên nhân:* Dự án yêu cầu **Java 17**. Nếu máy tính đang trỏ vào Java 8, 11 hoặc Java 21, Gradle sẽ báo lỗi.
   - *Khắc phục:* Trong Android Studio, vào **Settings (Preferences)** -> **Build, Execution, Deployment** -> **Build Tools** -> **Gradle** -> Mục **Gradle JDK**, chọn đúng **`Embedded JDK version 17`** hoặc **`jbr-17`**.

2. **Lỗi `Missing google-services.json`:**
   - *Nguyên nhân:* File `google-services.json` bị thiếu hoặc đặt sai vị trí.
   - *Khắc phục:* Đảm bảo file nằm chính xác tại `Code/app/google-services.json` (ngang hàng với `Code/app/build.gradle.kts`).

3. **Lỗi không hiện thông báo hoặc báo thức trên Android 13+ (API 33+):**
   - *Nguyên nhân:* Android 13 trở lên yêu cầu quyền hiển thị thông báo thời gian thực (`POST_NOTIFICATIONS`) và quyền hẹn giờ chính xác (`SCHEDULE_EXACT_ALARM`).
   - *Khắc phục:* Khi ứng dụng vừa khởi động, hãy chọn **Allow** (Cho phép) khi hộp thoại hệ thống xin quyền hiển thị. Nếu lỡ từ chối, vào **Cài đặt thiết bị** -> **Ứng dụng** -> **TaskFlow** -> **Quyền (Permissions)** -> Bật quyền **Thông báo (Notifications)** và **Báo thức & Lời nhắc (Alarms & Reminders)**.

4. **Lỗi `Gradle sync failed: Connection timed out`:**
   - *Nguyên nhân:* Kết nối mạng không ổn định khi tải các thư viện từ Google Maven / Maven Central.
   - *Khắc phục:* Nhấn **File** -> **Sync Project with Gradle Files**, hoặc kiểm tra lại kết nối mạng / tắt proxy / VPN nếu đang bật.

---

## 📜 Giấy Phép & Bản Quyền (License)
Dự án được phát triển phục vụ mục đích học tập và nghiên cứu trong khuôn khổ môn học **Lập Trình Thiết Bị Di Động**. Mọi quyền sở hữu trí tuệ thuộc về nhóm phát triển **TaskFlow**. Mọi hành vi tái sử dụng mã nguồn vui lòng trích dẫn nguồn tác giả.
