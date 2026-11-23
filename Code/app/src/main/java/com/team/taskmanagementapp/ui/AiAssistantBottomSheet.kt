package com.team.taskmanagementapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCreateTask: (() -> Unit)? = null,
    onNavigateToTodayTasks: (() -> Unit)? = null,
    onNavigateToStats: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            AiChatMessage(
                text = "Hi! I can create tasks, update Todo / In Progress / Completed, change due dates or priority, and find your tasks. What would you like to do?",
                isFromUser = false
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        messages.add(AiChatMessage(text = trimmed, isFromUser = true))
        inputText = ""

        coroutineScope.launch {
            delay(100)
            listState.animateScrollToItem(messages.size - 1)

            // Simulate intelligent assistant response
            delay(500)
            val lower = trimmed.lowercase()
            val reply = when {
                lower.contains("create") || lower.contains("add task") -> {
                    "I can help you create a task right away. Tap below or type the title, priority, and due date."
                }
                lower.contains("today") -> {
                    "You have today's tasks ready on your dashboard. Focus on the high-priority ones first to maintain your streak!"
                }
                lower.contains("progress") || lower.contains("update") -> {
                    "To update task progress, tell me which task you want to mark as In Progress or Completed."
                }
                lower.contains("hello") || lower.contains("hi") -> {
                    "Hello! How can I boost your productivity today?"
                }
                else -> {
                    "I've received your request: \"$trimmed\". I'm analyzing your tasks to assist you with the best workflow!"
                }
            }
            messages.add(AiChatMessage(text = reply, isFromUser = false))
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                color = Color(0xFFCBD5E1),
                shape = RoundedCornerShape(2.dp)
            ) {
                Box(modifier = Modifier.size(width = 44.dp, height = 4.dp))
            }
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 12.dp)
        ) {
            // ── Header: AI Avatar, English Title & Subtitle, Close Button ─────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dark Blue AI Avatar (preserved)
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier.size(44.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_chatbot_ai),
                        contentDescription = "TaskFlow AI Assistant",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Assistant Title & Subtitle in English
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Assistant",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Create, update and find tasks",
                        fontSize = 12.5.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                // Close Button
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Chat Messages List ───────────────────────────────────────────
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .height(340.dp)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    if (message.isFromUser) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = 18.dp,
                                    bottomEnd = 4.dp
                                ),
                                color = TaskFlowPrimary,
                                modifier = Modifier.fillMaxWidth(0.82f)
                            ) {
                                Text(
                                    text = message.text,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    lineHeight = 21.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = 4.dp,
                                    bottomEnd = 18.dp
                                ),
                                color = Color(0xFFF3F4F6),
                                modifier = Modifier.fillMaxWidth(0.92f)
                            ) {
                                Text(
                                    text = message.text,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1F2937),
                                    lineHeight = 21.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Suggestion Pills: "Today's tasks", "Create a task", "Update progress"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SuggestionChipPill(
                    text = "Today's tasks",
                    onClick = {
                        sendMessage("Show me today's tasks")
                        onNavigateToTodayTasks?.invoke()
                    }
                )
                SuggestionChipPill(
                    text = "Create a task",
                    onClick = {
                        sendMessage("I want to create a new task")
                    }
                )
                SuggestionChipPill(
                    text = "Update progress",
                    onClick = {
                        sendMessage("Update my task progress")
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Input Area: Text Field, Voice/Mic, and Send Button ────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Rounded Input Box
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Ask TaskFlow AI...",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color(0xFFFAFAFA),
                        focusedBorderColor = TaskFlowPrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            sendMessage(inputText)
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                )

                // Voice / Mic Button
                Surface(
                    onClick = {
                        isListening = !isListening
                        if (isListening) {
                            inputText = "Create a high priority task for project demo"
                        }
                    },
                    shape = CircleShape,
                    color = if (isListening) TaskFlowPrimary.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = if (isListening) TaskFlowPrimary else Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Send Button
                Surface(
                    onClick = {
                        sendMessage(inputText)
                    },
                    shape = CircleShape,
                    color = if (inputText.isNotBlank()) TaskFlowPrimary else TaskFlowPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Disclaimer Note at bottom ────────────────────────────────────
            Text(
                text = "Your request and task summary are sent to Gemini. Changes happen only after you confirm.",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun SuggestionChipPill(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, Color(0xFF93C5FD)),
        modifier = Modifier.height(38.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2563EB)
            )
        }
    }
}
