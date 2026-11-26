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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.ai.AiChatHistoryManager
import com.team.taskmanagementapp.ai.FirebaseAiService
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.data.model.enums.Priority
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary
import com.team.taskmanagementapp.util.DateTimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val actionTask: Task? = null,
    val actionType: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCreateTask: (() -> Unit)? = null,
    onNavigateToTodayTasks: (() -> Unit)? = null,
    onNavigateToStats: (() -> Unit)? = null,
    onNavigateToTaskDetail: ((Long) -> Unit)? = null
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        AiChatHistoryManager.initIfNeeded(context)
    }
    val messages = AiChatHistoryManager.messages
    val isThinking = AiChatHistoryManager.isThinking

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    var inputTextValue by remember { mutableStateOf(TextFieldValue("")) }
    var isListening by remember { mutableStateOf(false) }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || isThinking) return

        inputTextValue = TextFieldValue("")
        AiChatHistoryManager.sendUserMessage(context, trimmed) {
            coroutineScope.launch {
                delay(100)
                listState.animateScrollToItem(messages.size)
            }
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

                // Header Actions: New Chat / Refresh & Close Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            AiChatHistoryManager.resetConversation(context)
                            coroutineScope.launch {
                                listState.scrollToItem(0)
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New Conversation",
                            tint = Color(0xFF4B5563),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

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
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp)
                                ) {
                                    Text(
                                        text = message.text,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1F2937),
                                        lineHeight = 21.sp
                                    )

                                    if (message.actionTask != null && message.actionType == "CREATED") {
                                        val task = message.actionTask
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = "Success",
                                                            tint = Color(0xFF16A34A),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Text(
                                                            text = "Đã lưu vào TaskFlow",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF16A34A)
                                                        )
                                                    }

                                                    val (badgeBg, badgeText) = when (task.priority) {
                                                        Priority.URGENT, Priority.HIGH -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
                                                        Priority.MEDIUM -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
                                                        Priority.LOW -> Pair(Color(0xFFE0E7FF), Color(0xFF4F46E5))
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = badgeBg
                                                    ) {
                                                        Text(
                                                            text = task.priority.name,
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = badgeText,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = task.title,
                                                    fontSize = 14.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A)
                                                )

                                                if (task.description.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = task.description,
                                                        fontSize = 12.5.sp,
                                                        color = Color(0xFF64748B),
                                                        maxLines = 2
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))
                                                val dueCombined = DateTimeUtils.getCombinedDueTimestamp(task.dueDate, task.dueTime)
                                                if (dueCombined > 0L) {
                                                    val formattedDue = DateTimeUtils.formatTimestamp(dueCombined, "HH:mm - EEEE, dd/MM/yyyy")
                                                    Text(
                                                        text = "⏰ $formattedDue",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFF2563EB)
                                                    )
                                                }

                                                if (onNavigateToTaskDetail != null) {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Surface(
                                                        onClick = {
                                                            onNavigateToTaskDetail.invoke(task.id.toLong())
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = TaskFlowPrimary.copy(alpha = 0.08f),
                                                        border = BorderStroke(1.dp, TaskFlowPrimary.copy(alpha = 0.25f)),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(36.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = "Xem chi tiết task",
                                                                fontSize = 12.5.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = TaskFlowPrimary
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else if (message.actionTask != null && (message.actionType == "COMPLETED" || message.actionType == "DELETED")) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (message.actionType == "COMPLETED") Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = if (message.actionType == "COMPLETED") Color(0xFF16A34A) else Color(0xFFDC2626),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = if (message.actionType == "COMPLETED") {
                                                        "Đã hoàn thành: ${message.actionTask.title}"
                                                    } else {
                                                        "Đã xóa: ${message.actionTask.title}"
                                                    },
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (message.actionType == "COMPLETED") Color(0xFF166534) else Color(0xFF991B1B)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isThinking) {
                    item {
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
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = TaskFlowPrimary
                                    )
                                    Text(
                                        text = "AI is thinking...",
                                        fontSize = 13.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
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
                    value = inputTextValue,
                    onValueChange = { inputTextValue = it },
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
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            sendMessage(inputTextValue.text)
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
                            inputTextValue = TextFieldValue("Create a high priority task for project demo")
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
                        if (!isThinking) {
                            sendMessage(inputTextValue.text)
                        }
                    },
                    shape = CircleShape,
                    color = if (inputTextValue.text.isNotBlank() && !isThinking) TaskFlowPrimary else TaskFlowPrimary.copy(alpha = 0.5f),
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
