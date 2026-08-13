package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onNavigateToLevels: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToParent: () -> Unit,
    onNavigateToHoliday: () -> Unit,
    onNavigateToAccountCenter: () -> Unit,
    onNavigateToPetHouse: () -> Unit,
    onNavigateToMonsterCodex: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToBackpack: () -> Unit,
    onNavigateToBrushLibrary: () -> Unit,
    onNavigateToChushibiao: () -> Unit,
    isEmbeddedInShell: Boolean = false
) {
    val claimResult by viewModel.rewardClaimResult.collectAsState()
    val hatchNotification by viewModel.hatchNotification.collectAsState()

    hatchNotification?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearHatchNotification() },
            title = { Text("✨ 契约伙伴与神兵异动 ✨", fontWeight = FontWeight.Bold) },
            text = { Text(msg, fontSize = 16.sp, fontWeight = FontWeight.Medium) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearHatchNotification() }) {
                    Text("太棒了！")
                }
            }
        )
    }

    claimResult?.let { result ->
        RewardDialog(
            title = result.title,
            gold = result.gold,
            exp = result.exp,
            intimacy = result.intimacy,
            isLevelUp = result.isLevelUp,
            oldLevel = result.oldLevel,
            newLevel = result.newLevel,
            petMsg = result.petMsg,
            onDismiss = { viewModel.clearRewardClaimResult() }
        )
    }

    // Embed the new Camp Home Content
    var showCampFeedback by remember { mutableStateOf(false) }
    val shouldShowCampFeedback = viewModel.shouldShowCampFeedback

    LaunchedEffect(shouldShowCampFeedback) {
        if (shouldShowCampFeedback) {
            showCampFeedback = true
            viewModel.shouldShowCampFeedback = false
            kotlinx.coroutines.delay(2200)
            showCampFeedback = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CampHomeContent(
            viewModel = viewModel,
            onNavigateToLevels = onNavigateToLevels,
            onNavigateToPetHouse = onNavigateToPetHouse,
            onNavigateToHoliday = onNavigateToHoliday,
            onNavigateToReview = onNavigateToReview,
            onNavigateToReport = onNavigateToReport,
            onNavigateToChushibiao = onNavigateToChushibiao,
            modifier = Modifier.fillMaxSize()
        )

        androidx.compose.animation.AnimatedVisibility(
            visible = showCampFeedback,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { it / 2 }),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(horizontal = 24.dp).padding(bottom = 96.dp)
        ) {
            Surface(
                color = com.example.ui.GameUiTokens.Colors.Surface.copy(alpha = 0.95f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, com.example.ui.GameUiTokens.Colors.BorderActive),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Text(
                        text = "🏆 文字森林的试炼已完成",
                        color = com.example.ui.GameUiTokens.Colors.Gold,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.material3.Text(
                        text = "✨ 新关卡已解锁 • 今日冒险进度已更新",
                        color = com.example.ui.GameUiTokens.Colors.NeonCyan,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
