package com.team.taskmanagementapp.ai

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.team.taskmanagementapp.ui.AiChatMessage

/**
 * Manages persistent chat history for the AI Assistant across bottom sheet dismiss/reopen and app sessions.
 */
object AiChatHistoryManager {
    private const val PREFS_NAME = "taskflow_ai_chat_prefs"
    private const val KEY_CHAT_HISTORY = "ai_chat_history"

    val defaultGreeting = AiChatMessage(
        text = "Hi! I can create tasks, update Todo / In Progress / Completed, change due dates or priority, and find your tasks. What would you like to do?",
        isFromUser = false
    )

    private val gson = Gson()
    val messages: SnapshotStateList<AiChatMessage> = mutableStateListOf()
    private var isInitialized = false

    fun initIfNeeded(context: Context) {
        if (isInitialized) return
        isInitialized = true
        loadMessages(context)
    }

    private fun loadMessages(context: Context) {
        try {
            val prefs = getPrefs(context)
            val json = prefs.getString(KEY_CHAT_HISTORY, null)
            if (!json.isNullOrBlank()) {
                val type = object : TypeToken<List<AiChatMessage>>() {}.type
                val savedList: List<AiChatMessage>? = gson.fromJson(json, type)
                if (!savedList.isNullOrEmpty()) {
                    messages.clear()
                    messages.addAll(savedList)
                    return
                }
            }
        } catch (_: Exception) {}

        if (messages.isEmpty()) {
            messages.add(defaultGreeting)
        }
    }

    fun saveMessages(context: Context) {
        try {
            val prefs = getPrefs(context)
            val json = gson.toJson(messages.toList())
            prefs.edit().putString(KEY_CHAT_HISTORY, json).apply()
        } catch (_: Exception) {}
    }

    fun addMessage(context: Context, message: AiChatMessage) {
        messages.add(message)
        saveMessages(context)
    }

    fun resetConversation(context: Context) {
        messages.clear()
        messages.add(defaultGreeting)
        saveMessages(context)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
