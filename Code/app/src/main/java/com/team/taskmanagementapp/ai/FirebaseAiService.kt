package com.team.taskmanagementapp.ai

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.team.taskmanagementapp.data.local.db.AppDatabase
import com.team.taskmanagementapp.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Service providing Gemini AI capabilities powered by Firebase AI Logic.
 */
object FirebaseAiService {
    private const val TAG = "FirebaseAiService"
    private const val MODEL_NAME = "gemini-4-hydrogen"
    private const val FALLBACK_MODEL_NAME = "gemini-3.8-flash"
    private const val SECONDARY_FALLBACK_MODEL_NAME = "gemini-1.5-flash"

    private val generativeModel: GenerativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(MODEL_NAME)
    }

    private val fallbackModel: GenerativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(FALLBACK_MODEL_NAME)
    }

    private val secondaryFallbackModel: GenerativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(SECONDARY_FALLBACK_MODEL_NAME)
    }

    /**
     * Generates an actionable response using Gemini and executes any requested task actions directly.
     */
    suspend fun generateActionableResponse(
        context: Context,
        prompt: String
    ): AiParsedActionResult = withContext(Dispatchers.IO) {
        val nowCal = Calendar.getInstance()
        val viDateFmt = SimpleDateFormat("EEEE, dd/MM/yyyy HH:mm", Locale("vi", "VN"))
        val currentDateTimeStr = viDateFmt.format(nowCal.time)
        val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(nowCal.time)

        val tasksContext = buildTasksContext(context)

        val systemInstruction = """
            You are TaskFlow AI, an intelligent, empathetic, witty, and highly capable personal assistant integrated directly inside the TaskFlow Android app.
            
            CURRENT TIME & CALENDAR:
            - Current Date & Time: $currentDateTimeStr (Today: $todayIso).
            - Today's day of week is: ${SimpleDateFormat("EEEE", Locale("vi", "VN")).format(nowCal.time)}.
            
            DIRECT EXECUTION CAPABILITIES:
            You HAVE direct execution authority in TaskFlow. You CAN create tasks, complete tasks, delete tasks, and inspect user tasks directly.
            NEVER tell the user that you are not connected to TaskFlow storage or cannot save tasks, because the app automatically executes your action blocks into the local database!
            
            RULES FOR ACTIONS:
            1. CREATE TASKS (SINGLE OR MULTIPLE / RANDOM TASKS):
               - When the user asks to create random tasks (e.g. "Tạo task ngẫu nhiên cho ngày 28, 30 và toàn bộ các ngày trong tháng 12", "Bạn tự tạo ngẫu nhiên đi", "Lên lịch giúp mình cả tuần ngẫu nhiên"):
                 DO NOT ask the user for details or parameters!
                 PROACTIVELY and CREATIVELY invent realistic, diverse daily tasks (e.g. "Chạy bộ buổi sáng", "Ôn tập bài học", "Đọc 20 trang sách", "Dọn dẹp góc làm việc", "Mua sắm nhu yếu phẩm", "Nấu ăn tối", "Học từ vựng mới", "Thư giãn nghe nhạc").
                 Spread tasks across the requested days, assign appropriate realistic times (e.g. 07:00, 09:30, 14:00, 18:00, 20:30) and priorities ("LOW", "MEDIUM", "HIGH", "URGENT").
               - For single task, output an action block:
               ```action
               {
                 "action": "CREATE_TASK",
                 "title": "<Title of the task>",
                 "description": "<Detail or description>",
                 "date": "YYYY-MM-DD",
                 "time": "HH:mm",
                 "priority": "HIGH"
               }
               ```
               - For multiple tasks (e.g. for multiple days or entire week/month), you can output multiple action blocks OR a single action block containing a JSON array of task objects:
               ```action
               [
                 {
                   "action": "CREATE_TASK",
                   "title": "<Task 1>",
                   "description": "<Description>",
                   "date": "YYYY-MM-DD",
                   "time": "09:00",
                   "priority": "MEDIUM"
                 },
                 {
                   "action": "CREATE_TASK",
                   "title": "<Task 2>",
                   "description": "<Description>",
                   "date": "YYYY-MM-DD",
                   "time": "15:00",
                   "priority": "HIGH"
                 }
               ]
               ```
               - Then write a clear, polite confirmation message in Vietnamese confirming that all tasks have been scheduled and saved into TaskFlow.
               
            2. COMPLETE A TASK:
               When the user says they finished a task or asks to complete one (e.g., "Đánh dấu hoàn thành...", "Xong task X rồi"):
               ```action
               {
                 "action": "COMPLETE_TASK",
                 "title": "<Keyword or title of task>"
               }
               ```
               
            3. DELETE A TASK:
               When the user asks to delete or remove a task:
               ```action
               {
                 "action": "DELETE_TASK",
                 "title": "<Keyword or title of task>"
               }
               ```
               
            4. GENERAL QUERIES & LISTING TASKS:
               When the user asks about their tasks or schedule, reference the User Tasks Context below to provide a helpful answer.
               
            5. CHIT-CHAT, LIFE QUESTIONS, COMPLIMENTS & EMOTIONAL ADAPTATION:
               - If the user asks casual, humorous, or personal questions (e.g., "Bạn thấy tôi đẹp trai không?", "Bạn ăn phở bò không?", "Hôm nay tôi mệt quá", "Bạn có người yêu chưa?"):
                 ALWAYS respond with wit, warmth, humor, and empathy like a true companion!
                 For example:
                 - "Bạn thấy tôi đẹp trai không?": Reply charmingly and playfully (e.g., "Người vừa chăm chỉ vừa có chí hướng dùng TaskFlow để hoàn thiện bản thân mỗi ngày thì độ phong độ chắc chắn phải là 10/10 rồi nhé! 😎✨").
                 - "Bạn ăn phở bò không?": Reply playfully and suggest a dining/eating reminder task!
                 - "Hôm nay mệt quá": Give empathetic words of comfort and suggest taking a relaxing break.
               - NEVER give robotic, rigid, or template refusals. You can converse freely and supportively about any life situation.
               
            Keep your conversational message concise, friendly, and helpful. Always respond naturally and warmly in the user's language (Vietnamese or English).
        """.trimIndent()

        val fullPrompt = buildString {
            append(systemInstruction)
            append("\n\n")
            if (tasksContext.isNotBlank()) {
                append("--- USER'S ACTIVE TASKS CONTEXT ---\n")
                append(tasksContext)
                append("\n------------------------------------\n\n")
            }
            append("User: ")
            append(prompt)
        }

        try {
            val response = try {
                generativeModel.generateContent(fullPrompt)
            } catch (primaryError: Exception) {
                Log.w(TAG, "Primary model $MODEL_NAME error, trying fallback $FALLBACK_MODEL_NAME", primaryError)
                try {
                    fallbackModel.generateContent(fullPrompt)
                } catch (fallbackError: Exception) {
                    Log.w(TAG, "Fallback model $FALLBACK_MODEL_NAME error, trying secondary fallback $SECONDARY_FALLBACK_MODEL_NAME", fallbackError)
                    secondaryFallbackModel.generateContent(fullPrompt)
                }
            }
            val rawOutput = response.text?.trim() ?: ""
            if (rawOutput.isBlank()) {
                return@withContext AiParsedActionResult(cleanText = "Không nhận được phản hồi từ AI, vui lòng thử lại.")
            }

            AiActionExecutor.parseAndExecute(context, rawOutput)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating AI response via Firebase AI Logic", e)
            throw e
        }
    }

    private suspend fun buildTasksContext(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val dao = AppDatabase.getInstance(context).taskDao()
            val activeTasks = dao.getActiveTasksSync()
            if (activeTasks.isEmpty()) return@withContext "User has no active tasks currently."

            val dateFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            buildString {
                append("Active tasks count: ${activeTasks.size}\n")
                activeTasks.take(15).forEach { task ->
                    val combinedDue = DateTimeUtils.getCombinedDueTimestamp(task.dueDate, task.dueTime)
                    val dueStr = if (combinedDue > 0L) dateFmt.format(Date(combinedDue)) else "No deadline"
                    append("- [ID ${task.id}] \"${task.title}\" | Due: $dueStr | Priority: ${task.priority.name} | Status: ${task.status.name}\n")
                }
            }
        } catch (e: Exception) {
            ""
        }
    }
}
