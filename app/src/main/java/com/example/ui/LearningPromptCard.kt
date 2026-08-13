package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LearningPromptCard(
    visiblePrompt: String,
    subPrompt: String,
    isPractice: Boolean,
    playCount: Int,
    maxPlayCount: Int,
    onPlayAudio: () -> Unit,
    showHelpHint: Boolean,
    onViewHintClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131625)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2235)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Content: Structured information & Hint Text
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Header Label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isPractice) Color(0x3000E5FF) else Color(0x30FF9800),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isPractice) "💡 临摹秘籍" else "⚔️ 字灵讨伐词",
                            color = if (isPractice) Color(0xFF00E5FF) else Color(0xFFFF9800),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPractice) "看字临摹跟写" else "听写净化模式",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Core Prompt Display
                Text(
                    text = visiblePrompt.ifBlank { "请聆听语音并写出对应的字词" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                if (subPrompt.isNotBlank() && subPrompt != visiblePrompt) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subPrompt,
                        fontSize = 11.sp,
                        color = Color(0xFF8A99AD),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right Content: Sound/Audio Trigger and Help Hint Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Big stylish play button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFF1C213B), RoundedCornerShape(12.dp))
                        .border(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { onPlayAudio() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "播音",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Play Counter Label
                Text(
                    text = "$playCount/$maxPlayCount 次",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
