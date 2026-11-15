-- Chạy trong Database Inspector trên task_management_db khi app debug đang mở.
-- Tên và ngày trùng nhau được bỏ qua khi chạy lại câu lệnh.
WITH demo(title, description, local_day, local_time, priority, status, done) AS (
    VALUES
    (
        '[LTDD24] Ôn bài buổi sáng',
        'Kiểm tra Task lúc 09:00 trong danh sách hôm nay.',
        datetime('now', 'localtime', 'start of day'),
        '09:00:00', 'HIGH', 'TODO', 0
    ),
    (
        '[LTDD24] Làm bài tập buổi chiều',
        'Kiểm tra thứ tự giờ và trạng thái đang thực hiện.',
        datetime('now', 'localtime', 'start of day'),
        '15:00:00', 'MEDIUM', 'IN_PROGRESS', 0
    ),
    (
        '[LTDD24] Chuẩn bị nội dung ngày mai',
        'Chọn ngày mai để thấy công việc này.',
        datetime('now', 'localtime', 'start of day', '+1 day'),
        '10:00:00', 'LOW', 'TODO', 0
    ),
    (
        '[LTDD24] Ôn lại bài tháng trước',
        'Chọn ngày 15 tháng trước để thấy công việc quá hạn.',
        datetime('now', 'localtime', 'start of month', '-1 month', '+14 days'),
        '14:00:00', 'URGENT', 'OVERDUE', 0
    ),
    (
        '[LTDD24] Chuẩn bị kế hoạch tháng sau',
        'Dữ liệu thử cho một công việc được hoàn thành sớm.',
        datetime('now', 'localtime', 'start of month', '+1 month', '+9 days'),
        '08:30:00', 'LOW', 'COMPLETED', 1
    )
)
INSERT INTO tasks (
    title, description, dueDate, dueTime, priority, status, isComplete,
    isRecurring, recurrenceType, recurrenceInterval, reminderMinutes,
    repeatEndDate, repeatLimitCount, currentOccurrence, isPaused,
    createdAt, updatedAt, estimatedPomodoros, completedPomodoros,
    totalFocusTimeMinutes, completedAt
)
SELECT
    demo.title,
    demo.description,
    CAST(strftime('%s', demo.local_day, 'utc') AS INTEGER) * 1000,
    CAST(strftime('%s', date(demo.local_day) || ' ' || demo.local_time, 'utc') AS INTEGER) * 1000,
    demo.priority, demo.status, demo.done,
    0, 'NONE', 1, 10,
    0, 0, 1, 0,
    CAST(strftime('%s', 'now') AS INTEGER) * 1000,
    CAST(strftime('%s', 'now') AS INTEGER) * 1000,
    0, 0, 0,
    CASE WHEN demo.done = 1
        THEN CAST(strftime('%s', 'now') AS INTEGER) * 1000
        ELSE NULL
    END
FROM demo
WHERE NOT EXISTS (
    SELECT 1 FROM tasks
    WHERE tasks.title = demo.title
      AND tasks.dueDate = CAST(strftime('%s', demo.local_day, 'utc') AS INTEGER) * 1000
);
