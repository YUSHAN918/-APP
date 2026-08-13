package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel

@Composable
fun CodexTabScreen(
    viewModel: GameViewModel,
    onNavigateToMonsterCodex: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToChushibiao: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wrongWords by viewModel.wrongWords.collectAsState()
    val stats by viewModel.userStats.collectAsState()

    val totalMonstersPurified = stats?.totalAnswered ?: 0
    val totalPerfectPurifications = stats?.correctCount ?: 0
    val totalAchievementsCount = stats?.correctCount ?: 0 // Mock indicator or real achievements count if any

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
            text = "📖 冒险图鉴与成就",
            color = GameUiTokens.Colors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // 1. Core Learning Achievements Statistics Panel
        GamePanel(
            title = "行者净化档案",
            borderColor = GameUiTokens.Colors.Border,
            glowColor = GameUiTokens.Colors.NeonCyan
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stat 1
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "👾 ${wrongWords.size}",
                        color = GameUiTokens.Colors.NeonRed,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "残留魔物",
                        color = GameUiTokens.Colors.TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Divider
                Box(modifier = Modifier.size(width = 1.dp, height = 32.dp).background(GameUiTokens.Colors.Border))

                // Stat 2
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚔️ $totalMonstersPurified",
                        color = GameUiTokens.Colors.NeonCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "累计净化",
                        color = GameUiTokens.Colors.TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Divider
                Box(modifier = Modifier.size(width = 1.dp, height = 32.dp).background(GameUiTokens.Colors.Border))

                // Stat 3
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⭐ $totalPerfectPurifications",
                        color = GameUiTokens.Colors.Gold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "完美无瑕",
                        color = GameUiTokens.Colors.TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 2. Entries Grid (List of Codex Screens)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Codex Card 1: Monster Codex
            CodexRowCard(
                icon = "👾",
                title = "字灵魔物图鉴",
                description = "记录所有相遇的错词魔物与进化状态",
                accentColor = GameUiTokens.Colors.NeonRed,
                onClick = onNavigateToMonsterCodex
            )

            // Codex Card 2: Achievements
            CodexRowCard(
                icon = "🏆",
                title = "行者荣耀成就",
                description = "行者一路上的不凡成就和稀有称号",
                accentColor = GameUiTokens.Colors.Gold,
                onClick = onNavigateToAchievements
            )

            // Codex Card 3: Report / Studies History
            CodexRowCard(
                icon = "📊",
                title = "修习报告与卷轴",
                description = "详细记录您的净化历史与高频错词热力图",
                accentColor = GameUiTokens.Colors.NeonCyan,
                onClick = onNavigateToReport
            )

            // Codex Card 4: Chushibiao Classic Reading
            CodexRowCard(
                icon = "📜",
                title = "诸葛武侯《出师表》",
                description = "品读国学传世经典，练习朗读、精读与少儿背诵挑战",
                accentColor = GameUiTokens.Colors.NeonAmber,
                onClick = onNavigateToChushibiao
            )
        }
    }
}

@Composable
fun CodexRowCard(
    icon: String,
    title: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GameUiTokens.Shapes.Panel)
            .background(GameUiTokens.Colors.Surface)
            .border(1.dp, GameUiTokens.Colors.Border.copy(alpha = 0.5f), GameUiTokens.Shapes.Panel)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f))
                .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 22.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = GameUiTokens.Colors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = GameUiTokens.Colors.TextSecondary,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = "打开",
            tint = GameUiTokens.Colors.TextSecondary.copy(alpha = 0.6f),
            modifier = Modifier.size(12.dp)
        )
    }
}
