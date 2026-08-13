package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AnswerFeedbackOverlay(
    feedbackState: DictationFeedbackState,
    onDismiss: () -> Unit,
    onShowTip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (feedbackState is DictationFeedbackState.Idle) return

    val config = remember(feedbackState) {
        when (feedbackState) {
            is DictationFeedbackState.Correct -> FeedbackConfig(
                bgColor = Color(0xE61B5E20), // deep semantic forest green
                borderColor = Color(0xFF00E676), // bright neon green
                textColor = Color(0xFFE8F5E9),
                icon = Icons.Default.CheckCircle,
                title = "✨ 完美净化！",
                desc = "字字端正，字灵净化进度推进！"
            )
            is DictationFeedbackState.NeedsImprovement -> FeedbackConfig(
                bgColor = Color(0xE6E65100), // deep semantic amber
                borderColor = Color(0xFFFF9100), // bright neon amber
                textColor = Color(0xFFFFF3E0),
                icon = Icons.Default.Warning,
                title = "🩹 擦肩而过！",
                desc = feedbackState.reason.ifBlank { "笔迹略有偏差，要再试一次吗？" }
            )
            is DictationFeedbackState.Wrong -> FeedbackConfig(
                bgColor = Color(0xE6B71C1C), // deep semantic crimson
                borderColor = Color(0xFFFF1744), // bright neon red
                textColor = Color(0xFFFFEBEE),
                icon = Icons.Default.Error,
                title = "👾 魔物抵挡！",
                desc = feedbackState.reason.ifBlank { "字灵能量未击中核心，建议再试一次" }
            )
            is DictationFeedbackState.RecognitionFailed -> FeedbackConfig(
                bgColor = Color(0xE637474F), // deep slate blue-gray
                borderColor = Color(0xFF90A4AE), // silver gray
                textColor = Color(0xFFECEFF1),
                icon = Icons.Default.Warning,
                title = "⌛ 感应未响应",
                desc = "没有识别到有效笔迹，请再写一次。"
            )
            is DictationFeedbackState.Submitting -> FeedbackConfig(
                bgColor = Color(0xE60D47A1), // deep blue
                borderColor = Color(0xFF2979FF), // bright neon blue
                textColor = Color(0xFFE3F2FD),
                icon = Icons.Default.Warning,
                title = "⚡ 灵脉析出中",
                desc = "正在评测笔迹能量，请稍候..."
            )
            else -> FeedbackConfig(
                bgColor = Color(0xE6263238),
                borderColor = Color(0xFF78909C),
                textColor = Color(0xFFECEFF1),
                icon = Icons.Default.Warning,
                title = "提示",
                desc = ""
            )
        }
    }

    // Auto dismiss only for RecognitionFailed
    LaunchedEffect(feedbackState) {
        when (feedbackState) {
            is DictationFeedbackState.RecognitionFailed -> {
                delay(1200)
                onDismiss()
            }
            else -> { /* Stay until explicit user action */ }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .testTag("dictation_feedback_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(config.bgColor)
                .border(1.5.dp, config.borderColor, RoundedCornerShape(16.dp))
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (feedbackState is DictationFeedbackState.Submitting) {
                    CircularProgressIndicator(
                        color = config.borderColor,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                } else {
                    Icon(
                        imageVector = config.icon,
                        contentDescription = null,
                        tint = config.borderColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = config.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = config.textColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = config.desc,
                    fontSize = 11.sp,
                    color = config.textColor.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Render actions for Wrong or NeedsImprovement
                if (feedbackState is DictationFeedbackState.Wrong || feedbackState is DictationFeedbackState.NeedsImprovement) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onShowTip,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("查看提示 💡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = config.borderColor,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f).height(36.dp).testTag("feedback_btn_retry"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("再写一次 🔄", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

private data class FeedbackConfig(
    val bgColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val desc: String
)
