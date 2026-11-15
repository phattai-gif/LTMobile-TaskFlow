package com.team.taskmanagementapp.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team.taskmanagementapp.R
import com.team.taskmanagementapp.ui.theme.TaskFlowPrimary

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Decorative Background Curves ──────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top soft curved background accent
            val topCurve = Path().apply {
                moveTo(width * 0.35f, 0f)
                cubicTo(
                    width * 0.3f, height * 0.12f,
                    width * 0.9f, height * 0.22f,
                    width, height * 0.32f
                )
            }
            drawPath(
                path = topCurve,
                color = Color(0xFFF1F5F9),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Bottom left subtle geometric accent line
            val bottomCurve = Path().apply {
                moveTo(0f, height * 0.72f)
                lineTo(width * 0.28f, height * 0.85f)
                lineTo(0f, height * 0.96f)
            }
            drawPath(
                path = bottomCurve,
                color = Color(0xFFF8FAFC),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // ── Main Content Column ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ── Top Illustration (Person with laptop, mug & plant) ──────────
            Image(
                painter = painterResource(id = R.drawable.img_welcome_hero),
                contentDescription = "Welcome to TaskFlow",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .padding(horizontal = 8.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Title & Subtitle (Matching Image 2) ─────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.welcome_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TaskFlowPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = stringResource(R.string.welcome_subtitle),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ── Action Buttons: Login (Blue Pill) & Register (Text) ─────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Login Button: Solid Blue with subtle shadow
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = TaskFlowPrimary.copy(alpha = 0.45f)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskFlowPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.welcome_btn_login),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Register Button: Clean Dark Text Button aligned with Login
                TextButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.welcome_btn_register),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }
    }
}
