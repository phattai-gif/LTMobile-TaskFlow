package com.team.taskmanagementapp.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.auth.FirebaseAuth
import com.team.taskmanagementapp.ui.pin.PinScreenMode
import com.team.taskmanagementapp.ui.pin.SetPinScreen
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary
import com.team.taskmanagementapp.util.PinManager
import java.util.Locale

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val pinManager = remember { PinManager.getInstance(context) }
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { fbAuth ->
            currentUser = fbAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    // Security & Preferences states
    var pinLockEnabled by remember { mutableStateOf(pinManager.isPinLockEnabled()) }
    var biometricEnabled by remember { mutableStateOf(pinManager.isBiometricEnabled()) }
    var taskRemindersEnabled by rememberSaveable { mutableStateOf(true) }
    var dailySummaryEnabled by rememberSaveable { mutableStateOf(false) }

    // Dialog & PIN Screen states
    var activePinMode by remember { mutableStateOf<PinScreenMode?>(null) }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var featureNoticeMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // Resolve Account Details
    val isGoogleAccount = remember(currentUser) {
        currentUser?.providerData?.any { it.providerId == "google.com" } == true
    }
    val resolvedEmail = remember(currentUser) {
        currentUser?.email?.takeIf { it.isNotBlank() }
            ?: if (currentUser != null) "" else "user@taskflow.app"
    }
    val resolvedName = remember(currentUser, resolvedEmail) {
        currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: run {
                val prefix = resolvedEmail.substringBefore('@')
                val words = prefix.split('.', '_', '-').filter { it.isNotBlank() }
                if (words.size > 1) {
                    words.joinToString(" ") { word ->
                        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    }
                } else if (prefix.isNotBlank()) {
                    prefix.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                } else {
                    "User"
                }
            }
    }
    val initials = remember(resolvedName, resolvedEmail) {
        getInitials(resolvedName, resolvedEmail)
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Log out",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of your TaskFlow account?",
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = "Log out",
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }

    if (featureNoticeMessage != null) {
        AlertDialog(
            onDismissRequest = { featureNoticeMessage = null },
            title = { Text("Settings", fontWeight = FontWeight.Bold) },
            text = { Text(featureNoticeMessage.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { featureNoticeMessage = null }) {
                    Text("OK", color = TaskFlowPrimary)
                }
            }
        )
    }

    if (activePinMode != null) {
        Dialog(
            onDismissRequest = { activePinMode = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true
            )
        ) {
            SetPinScreen(
                mode = activePinMode!!,
                onSuccess = {
                    val completedMode = activePinMode
                    activePinMode = null
                    when (completedMode) {
                        PinScreenMode.SET_NEW -> {
                            pinLockEnabled = true
                            featureNoticeMessage = "PIN lock has been enabled."
                        }
                        PinScreenMode.CHANGE -> {
                            featureNoticeMessage = "Security PIN updated successfully."
                        }
                        PinScreenMode.VERIFY_TO_DISABLE -> {
                            pinLockEnabled = false
                            biometricEnabled = false
                            pinManager.setBiometricEnabled(false)
                            featureNoticeMessage = "PIN lock has been disabled."
                        }
                        PinScreenMode.UNLOCK, null -> {}
                    }
                },
                onBack = { activePinMode = null }
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top Bar: Title ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // ── Account Profile Card (Above Security) ─────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar circle with Initials
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE0EAFF),
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1D4ED8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Display Name
                Text(
                    text = resolvedName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Email
                Text(
                    text = resolvedEmail,
                    fontSize = 13.5.sp,
                    color = Color(0xFF6B7280)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Linked Account Badge Pill
                if (currentUser == null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF3F4F6),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "•",
                                color = Color(0xFF6B7280),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Guest Mode",
                                color = Color(0xFF4B5563),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else if (isGoogleAccount) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✓",
                                color = Color(0xFF047857),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Connected with Google",
                                color = Color(0xFF047857),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEEF2FF),
                        border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✓",
                                color = Color(0xFF4338CA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Signed in with Email",
                                color = Color(0xFF4338CA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // ── Section 1: Security ───────────────────────────────────────────
            SettingsSectionHeader(title = "Security")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Lock,
                        title = "PIN Lock",
                        subtitle = if (pinLockEnabled) "Require a PIN when opening TaskFlow" else "Enable to protect your tasks with a PIN",
                        checked = pinLockEnabled,
                        onCheckedChange = { enable ->
                            if (enable) {
                                if (pinManager.hasPin()) {
                                    pinManager.setPinLockEnabled(true)
                                    pinLockEnabled = true
                                } else {
                                    activePinMode = PinScreenMode.SET_NEW
                                }
                            } else {
                                activePinMode = PinScreenMode.VERIFY_TO_DISABLE
                            }
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsClickableRow(
                        icon = Icons.Outlined.Dialpad,
                        title = "Change PIN",
                        subtitle = if (pinLockEnabled) "Create or update your security PIN" else "Enable PIN Lock to change your PIN",
                        enabled = pinLockEnabled,
                        onClick = {
                            if (pinLockEnabled) {
                                activePinMode = PinScreenMode.CHANGE
                            } else {
                                featureNoticeMessage = "Please turn on PIN Lock first."
                            }
                        }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Fingerprint,
                        iconTint = if (pinLockEnabled) Color(0xFF2563EB) else Color(0xFF94A3B8),
                        title = "Biometric Unlock",
                        subtitle = if (pinLockEnabled) "Unlock with fingerprint or face recognition" else "Enable PIN Lock to use biometric unlock",
                        checked = biometricEnabled && pinLockEnabled,
                        onCheckedChange = { enable ->
                            if (pinLockEnabled) {
                                biometricEnabled = enable
                                pinManager.setBiometricEnabled(enable)
                            } else {
                                featureNoticeMessage = "Please turn on PIN Lock first."
                            }
                        }
                    )
                }
            }

            // ── Section 2: Notifications ──────────────────────────────────────
            SettingsSectionHeader(title = "Notifications")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Task Reminders",
                        subtitle = "Get notified when tasks are due",
                        checked = taskRemindersEnabled,
                        onCheckedChange = { taskRemindersEnabled = it }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsSwitchRow(
                        icon = Icons.Outlined.AccessTime,
                        title = "Daily Summary",
                        subtitle = "Receive a daily overview of your tasks",
                        checked = dailySummaryEnabled,
                        onCheckedChange = { dailySummaryEnabled = it }
                    )
                }
            }

            // ── Section 3: Backup & Restore ───────────────────────────────────
            SettingsSectionHeader(title = "Backup & Restore")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsClickableRow(
                        icon = Icons.Outlined.Upload,
                        title = "Export Tasks",
                        subtitle = "Save your tasks to a backup file",
                        onClick = { featureNoticeMessage = "Task export completed. File saved to downloads." }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    SettingsClickableRow(
                        icon = Icons.Outlined.Download,
                        title = "Import Tasks",
                        subtitle = "Import tasks from a backup file",
                        onClick = { featureNoticeMessage = "Select a valid TaskFlow JSON backup file to import." }
                    )
                }
            }

            // ── Section 4: About ──────────────────────────────────────────────
            SettingsSectionHeader(title = "About")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEEF4FF),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "TaskFlow",
                                tint = TaskFlowPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "TaskFlow",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Version 1.0",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Privacy Policy & Terms of Service",
                        fontSize = 13.sp,
                        color = TaskFlowPrimary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            featureNoticeMessage = "TaskFlow Privacy Policy & Terms of Service (Version 1.0)."
                        }
                    )
                }
            }

            // ── Section 5: Log out Button (After About) ───────────────────────
            Surface(
                onClick = { showLogoutDialog = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = "Log out",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log out",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF111827)
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconTint: Color = Color(0xFF4B5563)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TaskFlowPrimary,
                uncheckedThumbColor = Color(0xFF9CA3AF),
                uncheckedTrackColor = Color(0xFFE5E7EB)
            )
        )
    }
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    iconTint: Color = if (enabled) Color(0xFF4B5563) else Color(0xFFCBD5E1)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) Color(0xFF1F2937) else Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (enabled) Color(0xFF6B7280) else Color(0xFF94A3B8)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = if (enabled) Color(0xFF9CA3AF) else Color(0xFFE2E8F0),
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun getInitials(name: String, email: String): String {
    val cleanName = name.trim()
    if (cleanName.isNotBlank()) {
        val parts = cleanName.split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2) {
            return "${parts.first().first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
        } else if (cleanName.length >= 2) {
            return cleanName.take(2).uppercase()
        } else {
            return cleanName.uppercase()
        }
    }
    val cleanEmail = email.substringBefore('@').trim()
    if (cleanEmail.length >= 2) {
        val parts = cleanEmail.split('.', '_', '-').filter { it.isNotBlank() }
        if (parts.size >= 2) {
            return "${parts.first().first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
        }
        return cleanEmail.take(2).uppercase()
    }
    return "TF"
}
