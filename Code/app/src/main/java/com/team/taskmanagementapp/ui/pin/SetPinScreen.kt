package com.team.taskmanagementapp.ui.pin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary
import com.team.taskmanagementapp.util.BiometricAuthHelper
import com.team.taskmanagementapp.util.PinManager
import android.content.ContextWrapper
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class PinScreenMode {
    SET_NEW,
    CHANGE,
    VERIFY_TO_DISABLE,
    UNLOCK
}

@Composable
fun SetPinScreen(
    mode: PinScreenMode = PinScreenMode.SET_NEW,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pinManager = remember { PinManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val fragmentActivity = remember(context) {
        context as? FragmentActivity
            ?: (context as? ContextWrapper)?.baseContext as? FragmentActivity
    }

    var enteredPin by remember { mutableStateOf("") }
    var tempNewPin by remember { mutableStateOf("") }
    var currentStep by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessAnim by remember { mutableStateOf(false) }

    val isOnlyBiometric = mode == PinScreenMode.UNLOCK && pinManager.isBiometricEnabled() && !pinManager.isPinLockEnabled()

    fun triggerBiometricPrompt() {
        if (isSuccessAnim) return
        if (fragmentActivity != null && pinManager.isBiometricEnabled()) {
            val status = BiometricAuthHelper.checkBiometricStatus(context)
            if (status == BiometricAuthHelper.BiometricStatus.READY) {
                BiometricAuthHelper.showBiometricPrompt(
                    activity = fragmentActivity,
                    title = "Biometric Unlock",
                    subtitle = "Scan your fingerprint to unlock TaskFlow",
                    negativeButtonText = if (pinManager.isPinLockEnabled()) "Use PIN" else "Cancel",
                    onSuccess = {
                        isSuccessAnim = true
                        errorMessage = null
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(500)
                            onSuccess()
                        }
                    },
                    onError = { errorCode, errString ->
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                            errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            errorMessage = errString.toString()
                        }
                    }
                )
            } else {
                when (status) {
                    BiometricAuthHelper.BiometricStatus.NOT_ENROLLED -> {
                        errorMessage = "No fingerprints enrolled in device settings."
                    }
                    BiometricAuthHelper.BiometricStatus.NO_HARDWARE -> {
                        errorMessage = "Biometric hardware is not available on this device."
                    }
                    else -> {
                        errorMessage = "Biometric sensor is currently unavailable."
                    }
                }
            }
        }
    }

    // Auto-prompt Biometric when opening in UNLOCK mode
    LaunchedEffect(mode) {
        if (mode == PinScreenMode.UNLOCK && pinManager.isBiometricEnabled()) {
            triggerBiometricPrompt()
        }
    }

    // Shake animation for incorrect PIN
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        coroutineScope.launch {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -20f at 50
                    20f at 100
                    -15f at 150
                    15f at 200
                    -8f at 250
                    8f at 300
                    0f at 400
                }
            )
        }
    }

    // Determine current title and subtitle based on mode and step
    val (title, subtitle) = when (mode) {
        PinScreenMode.SET_NEW -> {
            if (currentStep == 0) {
                "Set Your PIN" to "Create a 4-digit PIN to secure your app"
            } else {
                "Confirm Your PIN" to "Re-enter your 4-digit PIN to confirm"
            }
        }
        PinScreenMode.CHANGE -> {
            when (currentStep) {
                0 -> "Enter Current PIN" to "Enter your existing 4-digit security PIN"
                1 -> "Set Your PIN" to "Create your new 4-digit PIN"
                else -> "Confirm Your PIN" to "Re-enter your new 4-digit PIN to confirm"
            }
        }
        PinScreenMode.VERIFY_TO_DISABLE -> {
            "Enter Current PIN" to "Enter your 4-digit PIN to turn off PIN Lock"
        }
        PinScreenMode.UNLOCK -> {
            if (isOnlyBiometric) {
                "Fingerprint Unlock" to "Touch the fingerprint sensor to unlock TaskFlow"
            } else {
                "Enter Your PIN" to "Enter your 4-digit security PIN to continue"
            }
        }
    }

    fun handlePinComplete(pin: String) {
        when (mode) {
            PinScreenMode.SET_NEW -> {
                if (currentStep == 0) {
                    tempNewPin = pin
                    enteredPin = ""
                    errorMessage = null
                    currentStep = 1
                } else {
                    if (pin == tempNewPin) {
                        pinManager.savePin(pin)
                        onSuccess()
                    } else {
                        errorMessage = "PINs do not match. Please try again."
                        triggerShake()
                        enteredPin = ""
                        tempNewPin = ""
                        currentStep = 0
                    }
                }
            }
            PinScreenMode.CHANGE -> {
                when (currentStep) {
                    0 -> {
                        if (pinManager.verifyPin(pin)) {
                            enteredPin = ""
                            errorMessage = null
                            currentStep = 1
                        } else {
                            errorMessage = "Incorrect PIN. Please try again."
                            triggerShake()
                            enteredPin = ""
                        }
                    }
                    1 -> {
                        tempNewPin = pin
                        enteredPin = ""
                        errorMessage = null
                        currentStep = 2
                    }
                    2 -> {
                        if (pin == tempNewPin) {
                            pinManager.savePin(pin)
                            onSuccess()
                        } else {
                            errorMessage = "PINs do not match. Please try again."
                            triggerShake()
                            enteredPin = ""
                            tempNewPin = ""
                            currentStep = 1
                        }
                    }
                }
            }
            PinScreenMode.VERIFY_TO_DISABLE -> {
                if (pinManager.verifyPin(pin)) {
                    pinManager.setPinLockEnabled(false)
                    onSuccess()
                } else {
                    errorMessage = "Incorrect PIN. Please try again."
                    triggerShake()
                    enteredPin = ""
                }
            }
            PinScreenMode.UNLOCK -> {
                if (pinManager.verifyPin(pin)) {
                    isSuccessAnim = true
                    errorMessage = null
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(450)
                        onSuccess()
                    }
                } else {
                    errorMessage = "Incorrect PIN. Please try again."
                    triggerShake()
                    enteredPin = ""
                }
            }
        }
    }

    fun onDigitClick(digit: String) {
        if (isSuccessAnim) return
        if (enteredPin.length < 4) {
            errorMessage = null
            val newPin = enteredPin + digit
            enteredPin = newPin
            if (newPin.length == 4) {
                coroutineScope.launch {
                    kotlinx.coroutines.delay(120)
                    handlePinComplete(newPin)
                }
            }
        }
    }

    fun onBackspaceClick() {
        if (isSuccessAnim) return
        if (enteredPin.isNotEmpty()) {
            errorMessage = null
            enteredPin = enteredPin.dropLast(1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ── Top Navigation Bar ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mode != PinScreenMode.UNLOCK) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1F2937)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(4.dp))
                androidx.compose.material3.TextButton(onClick = onBack) {
                    Text(
                        text = "Sign out",
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Top Icon Badge (Rounded Square with Blue Checkmark) ─────────────
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isSuccessAnim) Color(0xFFDCFCE7) else Color.White,
                border = BorderStroke(1.2.dp, if (isSuccessAnim) Color(0xFF86EFAC) else Color(0xFFE2E8F0)),
                shadowElevation = 2.dp,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isSuccessAnim) Icons.Filled.CheckCircle else if (isOnlyBiometric) Icons.Outlined.Fingerprint else Icons.Filled.CheckCircle,
                        contentDescription = if (isSuccessAnim) "Success" else if (isOnlyBiometric) "Fingerprint Unlock" else "Security PIN",
                        tint = if (isSuccessAnim) Color(0xFF16A34A) else TaskFlowPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // ── Title & Subtitle ────────────────────────────────────────────────
        Text(
            text = if (isSuccessAnim) "Verified!" else title,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSuccessAnim) Color(0xFF16A34A) else Color(0xFF111827),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSuccessAnim) "Opening TaskFlow..." else subtitle,
            fontSize = 13.5.sp,
            color = if (isSuccessAnim) Color(0xFF15803D) else Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Error message if any
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage.orEmpty(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFDC2626),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (isOnlyBiometric) {
            Spacer(modifier = Modifier.weight(0.7f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    onClick = { if (!isSuccessAnim) triggerBiometricPrompt() },
                    shape = CircleShape,
                    color = if (isSuccessAnim) Color(0xFFDCFCE7) else Color(0xFFEFF6FF),
                    border = BorderStroke(1.5.dp, if (isSuccessAnim) Color(0xFF86EFAC) else Color(0xFFBFDBFE)),
                    modifier = Modifier.size(108.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSuccessAnim) Icons.Filled.CheckCircle else Icons.Outlined.Fingerprint,
                            contentDescription = "Scan Fingerprint",
                            tint = if (isSuccessAnim) Color(0xFF16A34A) else TaskFlowPrimary,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (isSuccessAnim) {
                    androidx.compose.material3.LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF16A34A),
                        trackColor = Color(0xFFDCFCE7)
                    )
                } else {
                    androidx.compose.material3.Button(
                        onClick = { triggerBiometricPrompt() },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = TaskFlowPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Scan Fingerprint",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.3f))
        } else {
            Spacer(modifier = Modifier.height(34.dp))

            // ── 4 PIN Indicator Dots with Shake Animation ───────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = if (isSuccessAnim) true else (i < enteredPin.length)
                    PinIndicatorDot(
                        isFilled = isFilled,
                        customColor = if (isSuccessAnim) Color(0xFF16A34A) else null
                    )
                    if (i < 3) {
                        Spacer(modifier = Modifier.width(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Numeric Keypad (Dialpad) Matching Wireframe ─────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Row 1: 1, 2 (ABC), 3 (DEF)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    KeypadButton(number = "1", subtext = "", onClick = { onDigitClick("1") })
                    KeypadButton(number = "2", subtext = "ABC", onClick = { onDigitClick("2") })
                    KeypadButton(number = "3", subtext = "DEF", onClick = { onDigitClick("3") })
                }

                // Row 2: 4 (GHI), 5 (JKL), 6 (MNO)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    KeypadButton(number = "4", subtext = "GHI", onClick = { onDigitClick("4") })
                    KeypadButton(number = "5", subtext = "JKL", onClick = { onDigitClick("5") })
                    KeypadButton(number = "6", subtext = "MNO", onClick = { onDigitClick("6") })
                }

                // Row 3: 7 (PQRS), 8 (TUV), 9 (WXYZ)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    KeypadButton(number = "7", subtext = "PQRS", onClick = { onDigitClick("7") })
                    KeypadButton(number = "8", subtext = "TUV", onClick = { onDigitClick("8") })
                    KeypadButton(number = "9", subtext = "WXYZ", onClick = { onDigitClick("9") })
                }

                // Row 4: Biometric, 0, Backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Biometric / Fingerprint button
                    Surface(
                        onClick = {
                            if (pinManager.isBiometricEnabled()) {
                                triggerBiometricPrompt()
                            } else {
                                errorMessage = "Biometric unlock is not enabled in Settings."
                            }
                        },
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier.size(76.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Fingerprint,
                                contentDescription = "Biometric Unlock",
                                tint = if (pinManager.isBiometricEnabled()) TaskFlowPrimary else Color(0xFF94A3B8),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // 0 Button
                    KeypadButton(number = "0", subtext = "", onClick = { onDigitClick("0") })

                    // Backspace Button
                    Surface(
                        onClick = { onBackspaceClick() },
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier.size(76.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                contentDescription = "Delete",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinIndicatorDot(isFilled: Boolean, customColor: Color? = null) {
    if (isFilled) {
        Surface(
            shape = CircleShape,
            color = customColor ?: TaskFlowPrimary,
            modifier = Modifier.size(14.dp)
        ) {}
    } else {
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(1.5.dp, Color(0xFF93C5FD)),
            modifier = Modifier.size(14.dp)
        ) {}
    }
}

@Composable
private fun KeypadButton(
    number: String,
    subtext: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFFF8FAFC),
        modifier = Modifier.size(76.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number,
                fontSize = 27.sp,
                fontWeight = FontWeight.SemiBold,
                color = TaskFlowPrimary,
                lineHeight = 30.sp
            )
            if (subtext.isNotBlank()) {
                Text(
                    text = subtext,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}
