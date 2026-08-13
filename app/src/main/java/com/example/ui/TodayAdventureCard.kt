package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TodayAdventureCard(
    title: String,
    description: String,
    progressText: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamically parse progress text (e.g. "进度 1 / 3") into visual progress bar fraction
    val progressFraction = remember(progressText) {
        try {
            if (progressText.contains("/")) {
                val clean = progressText.replace("进度", "").trim()
                val parts = clean.split("/")
                if (parts.size == 2) {
                    val current = parts[0].trim().toFloat()
                    val target = parts[1].trim().toFloat()
                    if (target > 0f) (current / target).coerceIn(0f, 1f) else 0f
                } else {
                    0f
                }
            } else {
                0f
            }
        } catch (e: Exception) {
            0f
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        label = "progress_bar_anim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GameUiTokens.Colors.SurfaceVariant.copy(alpha = 0.95f),
                        GameUiTokens.Colors.Surface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(2.dp, GameUiTokens.Colors.BorderActive.copy(alpha = 0.9f), RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        // Subtle background light beam
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GameUiTokens.Colors.Gold.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Adventure Tag & Progress Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shiny Badge-style label
                Row(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    GameUiTokens.Colors.NeonAmber,
                                    GameUiTokens.Colors.Gold
                                )
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌟 今日冒险",
                        color = GameUiTokens.Colors.DarkText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
                
                // Text status indicator
                Box(
                    modifier = Modifier
                        .background(GameUiTokens.Colors.SurfaceVariant, RoundedCornerShape(10.dp))
                        .border(1.dp, GameUiTokens.Colors.Border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = progressText,
                        color = if (progressFraction >= 1f) GameUiTokens.Colors.NeonGreen else GameUiTokens.Colors.TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Title & Description
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    color = GameUiTokens.Colors.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.2.sp
                )
                Text(
                    text = description,
                    color = GameUiTokens.Colors.TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            // Physical Progress Bar (Only visible if there is active progress to display)
            if (progressText.contains("/")) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "冒险完成度",
                            color = GameUiTokens.Colors.TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            color = GameUiTokens.Colors.NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // High quality progress track & fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(GameUiTokens.Colors.Border)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            GameUiTokens.Colors.NeonCyan,
                                            GameUiTokens.Colors.NeonGreen
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            // Main Action Button (Highly appealing golden glow button)
            GamePrimaryButton(
                text = buttonText,
                onClick = onButtonClick,
                accentColor = GameUiTokens.Colors.Gold,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
        }
    }
}
