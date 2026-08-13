package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel

@Composable
fun QuestsTabScreen(
    viewModel: GameViewModel,
    onNavigateToLevels: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToHoliday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyQuests by viewModel.dailyQuests.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameUiTokens.Colors.Background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Title
        Text(
            text = "📜 今日冒险任务",
            color = GameUiTokens.Colors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // Intro
        Text(
            text = "完成每日修习与魔物净化挑战，领取金币、经历经验与契约伙伴亲密度奖励！",
            color = GameUiTokens.Colors.TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        // Quests List
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            dailyQuests.forEach { quest ->
                val icon = when (quest.id) {
                    "dictation" -> "⚔️"
                    "holiday" -> "📋"
                    "error_purify" -> "👾"
                    "recitation" -> "🎙️"
                    else -> "🌟"
                }
                
                val badgeColor = when (quest.id) {
                    "dictation" -> GameUiTokens.Colors.NeonCyan
                    "holiday" -> GameUiTokens.Colors.NeonAmber
                    "error_purify" -> GameUiTokens.Colors.NeonRed
                    "recitation" -> GameUiTokens.Colors.NeonGreen
                    else -> GameUiTokens.Colors.BorderActive
                }

                val targetClickAction = when (quest.id) {
                    "dictation" -> onNavigateToLevels
                    "holiday" -> onNavigateToHoliday
                    "error_purify" -> onNavigateToReview
                    "recitation" -> onNavigateToReport
                    else -> onNavigateToLevels
                }

                QuestItemCard(
                    icon = icon,
                    title = quest.title,
                    description = quest.description,
                    currentProgress = quest.currentProgress,
                    targetProgress = quest.targetProgress,
                    isClaimed = quest.isClaimed,
                    badgeColor = badgeColor,
                    onClaimClick = { viewModel.claimQuestReward(quest.id) },
                    onNavigateClick = targetClickAction
                )
            }
        }
    }
}

@Composable
fun QuestItemCard(
    icon: String,
    title: String,
    description: String,
    currentProgress: Int,
    targetProgress: Int,
    isClaimed: Boolean,
    badgeColor: Color,
    onClaimClick: () -> Unit,
    onNavigateClick: () -> Unit
) {
    val isCompleted = currentProgress >= targetProgress
    val isReadyToClaim = isCompleted && !isClaimed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GameUiTokens.Shapes.Panel)
            .background(GameUiTokens.Colors.Surface)
            .border(
                width = 1.dp,
                color = if (isReadyToClaim) GameUiTokens.Colors.BorderActive.copy(alpha = 0.6f) else GameUiTokens.Colors.Border.copy(alpha = 0.4f),
                shape = GameUiTokens.Shapes.Panel
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon circular badge
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.15f))
                .border(1.dp, badgeColor.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 22.sp)
        }

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = GameUiTokens.Colors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = GameUiTokens.Colors.TextSecondary,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            val progressFraction = if (targetProgress > 0) currentProgress.toFloat() / targetProgress else 0f
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progressFraction },
                    color = badgeColor,
                    trackColor = GameUiTokens.Colors.SurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = "$currentProgress/$targetProgress",
                    color = if (isCompleted) badgeColor else GameUiTokens.Colors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Action Button
        Box(modifier = Modifier.width(90.dp)) {
            if (isClaimed) {
                Text(
                    text = "已领取",
                    color = GameUiTokens.Colors.TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (isReadyToClaim) {
                GamePrimaryButton(
                    text = "领取",
                    onClick = onClaimClick,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    accentColor = GameUiTokens.Colors.Gold,
                    fontSize = 13.sp,
                    contentPadding = PaddingValues(0.dp)
                )
            } else {
                GamePrimaryButton(
                    text = "前往",
                    onClick = onNavigateClick,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    accentColor = GameUiTokens.Colors.NeonCyan,
                    fontSize = 13.sp,
                    contentPadding = PaddingValues(0.dp)
                )
            }
        }
    }
}
