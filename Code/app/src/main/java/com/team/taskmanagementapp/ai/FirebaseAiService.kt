package com.team.taskmanagementapp.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service providing Gemini AI capabilities powered by Firebase AI Logic.
 */
object FirebaseAiService {
    private const val TAG = "FirebaseAiService"
    private const val MODEL_NAME = "gemini-1.5-flash"

    private val generativeModel: GenerativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(MODEL_NAME)
    }

    /**
     * Generates a smart response using the Firebase Gemini model.
     */
    suspend fun generateResponse(
        prompt: String,
        contextInfo: String = ""
    ): String = withContext(Dispatchers.IO) {
        val systemInstruction = """
            You are TaskFlow AI, an intelligent productivity and task management assistant in the TaskFlow app.
            Help users organize tasks, boost productivity, set priorities, manage time, and answer queries concisely and politely.
            Respond in the same language as the user (Vietnamese or English).
        """.trimIndent()

        val fullPrompt = buildString {
            append(systemInstruction)
            append("\n\n")
            if (contextInfo.isNotBlank()) {
                append("User Tasks Context:\n")
                append(contextInfo)
                append("\n\n")
            }
            append("User: ")
            append(prompt)
        }

        try {
            val response = generativeModel.generateContent(fullPrompt)
            val output = response.text?.trim()
            if (!output.isNullOrBlank()) {
                output
            } else {
                "I'm sorry, I couldn't generate a response at this moment."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating AI response via Firebase AI Logic", e)
            throw e
        }
    }
}
