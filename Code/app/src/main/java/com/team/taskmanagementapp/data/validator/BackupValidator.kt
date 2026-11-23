package com.team.taskmanagementapp.data.validator

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.team.taskmanagementapp.data.model.ValidationResult

object BackupValidator {

    /**
     * Quy trình thẩm định tính toàn vẹn 5 bước theo kiến trúc TaskFlow:
     * 1. Cú pháp JSON (Syntax check)
     * 2. Cấu trúc mảng/đối tượng (Structure check)
     * 3. Trường dữ liệu bắt buộc (Required fields check)
     * 4. Kiểu dữ liệu & Enum hợp lệ (Data types & Enum validity)
     * 5. Ràng buộc toàn vẹn dữ liệu (Integrity constraints)
     */
    fun validate(jsonString: String): ValidationResult {
        // Bước 1: Kiểm tra cú pháp JSON
        if (jsonString.isBlank()) {
            return ValidationResult.Error("Tệp tin rỗng hoặc không có dữ liệu JSON.", step = 1)
        }

        val rootElement: JsonElement
        try {
            rootElement = JsonParser.parseString(jsonString)
        } catch (e: Exception) {
            return ValidationResult.Error("Lỗi cú pháp JSON: ${e.localizedMessage ?: "Cấu trúc không hợp lệ"}", step = 1)
        }

        // Bước 2: Kiểm tra cấu trúc mảng hoặc đối tượng bọc
        val taskArray: JsonArray = when {
            rootElement.isJsonArray -> rootElement.asJsonArray
            rootElement.isJsonObject -> {
                val obj = rootElement.asJsonObject
                when {
                    obj.has("tasks") && obj.get("tasks").isJsonArray -> obj.getAsJsonArray("tasks")
                    obj.has("taskList") && obj.get("taskList").isJsonArray -> obj.getAsJsonArray("taskList")
                    obj.has("data") && obj.get("data").isJsonArray -> obj.getAsJsonArray("data")
                    else -> return ValidationResult.Error(
                        "Không tìm thấy mảng danh sách công việc ('tasks') trong tệp sao lưu.",
                        step = 2
                    )
                }
            }
            else -> return ValidationResult.Error("Định dạng JSON gốc phải là mảng hoặc đối tượng.", step = 2)
        }

        if (taskArray.size() == 0) {
            return ValidationResult.Error("Mảng sao lưu không chứa công việc nào.", step = 2)
        }

        val validPriorityNames = setOf("LOW", "MEDIUM", "HIGH", "URGENT")
        val validStatusNames = setOf("TODO", "PENDING", "IN_PROGRESS", "COMPLETED", "OVERDUE")
        val validRecurrenceNames = setOf("NONE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY")

        var validCount = 0

        // Bước 3, 4, 5: Duyệt và thẩm định từng phần tử
        for (i in 0 until taskArray.size()) {
            val item = taskArray.get(i)
            if (!item.isJsonObject) {
                return ValidationResult.Error("Phần tử thứ $i không phải đối tượng công việc hợp lệ.", step = 3)
            }

            val taskObj = item.asJsonObject

            // Bước 3: Trường bắt buộc (phải có tiêu đề)
            val titleProp = when {
                taskObj.has("title") -> taskObj.get("title")
                taskObj.has("taskTitle") -> taskObj.get("taskTitle")
                taskObj.has("name") -> taskObj.get("name")
                else -> null
            }

            if (titleProp == null || titleProp.isJsonNull || titleProp.asString.isBlank()) {
                return ValidationResult.Error("Công việc thứ ${i + 1} thiếu trường tiêu đề bắt buộc.", step = 3)
            }

            val title = titleProp.asString.trim()

            // Bước 4: Kiểu dữ liệu & Enum hợp lệ
            if (taskObj.has("priority") && !taskObj.get("priority").isJsonNull) {
                val pStr = taskObj.get("priority").asString.uppercase()
                if (pStr !in validPriorityNames) {
                    return ValidationResult.Error("Công việc '$title' có mức ưu tiên không hợp lệ ($pStr).", step = 4)
                }
            }

            if (taskObj.has("status") && !taskObj.get("status").isJsonNull) {
                val sStr = taskObj.get("status").asString.uppercase()
                if (sStr !in validStatusNames) {
                    return ValidationResult.Error("Công việc '$title' có trạng thái không hợp lệ ($sStr).", step = 4)
                }
            }

            if (taskObj.has("recurrenceType") && !taskObj.get("recurrenceType").isJsonNull) {
                val rStr = taskObj.get("recurrenceType").asString.uppercase()
                if (rStr !in validRecurrenceNames) {
                    return ValidationResult.Error("Công việc '$title' có quy tắc lặp lại không hợp lệ ($rStr).", step = 4)
                }
            }

            // Bước 5: Ràng buộc độ dài & toàn vẹn dữ liệu
            if (title.length > 300) {
                return ValidationResult.Error("Tiêu đề công việc quá dài (tối đa 300 ký tự).", step = 5)
            }

            if (taskObj.has("description") && !taskObj.get("description").isJsonNull) {
                val desc = taskObj.get("description").asString
                if (desc.length > 5000) {
                    return ValidationResult.Error("Mô tả công việc '$title' vượt quá giới hạn 5000 ký tự.", step = 5)
                }
            }

            validCount++
        }

        return ValidationResult.Success(validItemCount = validCount)
    }
}
