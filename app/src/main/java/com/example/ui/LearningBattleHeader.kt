package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LearningBattleHeader(
    levelName: String,
    currentIndex: Int,
    totalCount: Int,
    timeLeft: Int,
    onBack: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clockColor = when {
        timeLeft <= 5 -> Color(0xFFFF1744)
        timeLeft <= 10 -> Color(0xFFFF9100)
        else -> Color(0xFF00E5FF)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0C0D14))
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Back button in HUD Style
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF131625), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF1E2235), RoundedCornerShape(10.dp))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(20.dp)
            )
        }

        // Center: High-contrast Title and Level progress
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
        ) {
            Text(
                text = levelName.ifBlank { "字词讨伐战" },
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF00E5FF), RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "关卡进度: ${currentIndex + 1} / $totalCount",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
            }
        }

        // Right: Help Info & Countdown Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Help button
            IconButton(
                onClick = onHelpClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF131625), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF1E2235), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "提示",
                    tint = Color.LightGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Countdown Pill
            Surface(
                color = clockColor.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, clockColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = "⌛ ${timeLeft}s",
                        color = clockColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
