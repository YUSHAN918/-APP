package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
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
fun GrowthHubScreen(
    viewModel: GameViewModel,
    onNavigateToBackpack: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToAccountCenter: () -> Unit,
    onNavigateToParent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerProfile by viewModel.playerProfile.collectAsState()
    val scrollState = rememberScrollState()

    val level = playerProfile?.level ?: 1
    val exp = playerProfile?.exp ?: 0
    val maxExp = level * 100
    val name = playerProfile?.playerName ?: "小字灵"
    val avatarEmoji = when (playerProfile?.avatarId) {
        1 -> "🛡️"
        2 -> "📖"
        3 -> "🧙‍♂️"
        4 -> "🔍"
        else -> "🛡️"
    }

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
            text = "🛡️ 契约行者：成长大厅",
            color = GameUiTokens.Colors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // 1. Player Character Profile Panel
        GamePanel(
            title = "当前契约行者档案",
            borderColor = GameUiTokens.Colors.Border,
            glowColor = GameUiTokens.Colors.NeonCyan
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Character Avatar Box
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(GameUiTokens.Shapes.Avatar)
                        .background(GameUiTokens.Colors.SurfaceVariant)
                        .border(1.5.dp, GameUiTokens.Colors.BorderActive, GameUiTokens.Shapes.Avatar),
                    contentAlignment = Alignment.Center
                ) {
                    Text(avatarEmoji, fontSize = 36.sp)
                }

                // Details Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        color = GameUiTokens.Colors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "等级 $level 契约行者",
                        color = GameUiTokens.Colors.TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "成长灵气: $exp / $maxExp",
                        color = GameUiTokens.Colors.NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(GameUiTokens.Colors.SurfaceVariant)
            ) {
                val progressFraction = if (maxExp > 0) (exp.toFloat() / maxExp).coerceIn(0f, 1f) else 0f
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GameUiTokens.Colors.NeonCyan)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Growth Features list
        Text(
            text = "🏅 行者成长与配置功能",
            color = GameUiTokens.Colors.TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        // Backpack Portal
        HubPortalCard(
            title = "💼 探险背包",
            description = "查看在探险中获得的各种药水、字灵礼物、装扮背景及契约道具。",
            icon = Icons.Default.Work,
            accentColor = GameUiTokens.Colors.NeonGreen,
            onClick = onNavigateToBackpack
        )

        // Achievements Portal
        HubPortalCard(
            title = "🏆 荣誉勋章与行者成就",
            description = "在这里查看你累积的净化记录、连对印记及解锁的各种荣誉勋章。",
            icon = Icons.Default.EmojiEvents,
            accentColor = GameUiTokens.Colors.NeonAmber,
            onClick = onNavigateToAchievements
        )

        // Report Portal
        HubPortalCard(
            title = "📊 成长与学习报告",
            description = "查看暑期写字听写的统计数据、高频错字以及词频攻克深度分析。",
            icon = Icons.Default.Assessment,
            accentColor = GameUiTokens.Colors.NeonCyan,
            onClick = onNavigateToReport
        )

        // Account Portal
        HubPortalCard(
            title = "👤 契约账户管理中心",
            description = "在此切换行者身份、编辑契约名字和绑定多账户信息。",
            icon = Icons.Default.AccountCircle,
            accentColor = Color(0xFFFF4081),
            onClick = onNavigateToAccountCenter
        )

        // Settings / Parent Portal
        HubPortalCard(
            title = "⚙️ 家长中心与系统设置",
            description = "设置词语范围、查看手写调优设置、进入家长防沉迷限制配置。",
            icon = Icons.Default.Settings,
            accentColor = Color(0xFF90A4AE),
            onClick = onNavigateToParent
        )
    }
}
