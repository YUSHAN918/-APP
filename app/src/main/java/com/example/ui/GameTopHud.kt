package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel

@Composable
fun GameTopHud(
    viewModel: GameViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerProfile by viewModel.playerProfile.collectAsState()
    
    val level = playerProfile?.level ?: 1
    val exp = playerProfile?.exp ?: 0
    val maxExp = level * 100
    val coins = playerProfile?.coins ?: 0
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
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GameUiTokens.Colors.Background.copy(alpha = 0.98f),
                        GameUiTokens.Colors.Background.copy(alpha = 0.90f)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Player Info Area
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Player Avatar + Level Badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(GameUiTokens.Shapes.Avatar)
                        .background(GameUiTokens.Colors.Surface)
                        .border(1.dp, GameUiTokens.Colors.BorderActive, GameUiTokens.Shapes.Avatar),
                    contentAlignment = Alignment.Center
                ) {
                    Text(avatarEmoji, fontSize = 22.sp)
                    
                    // Level badge overlapping bottom
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(GameUiTokens.Colors.Gold)
                            .border(0.5.dp, GameUiTokens.Colors.Background, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.toString(),
                            color = GameUiTokens.Colors.Background,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Name & EXP text stacked vertically
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = name,
                        color = GameUiTokens.Colors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "EXP: $exp / $maxExp",
                        color = GameUiTokens.Colors.TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Right: Resources & Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Coins Chip
                GameResourceChip(
                    icon = "🪙",
                    value = coins.toString(),
                    accentColor = GameUiTokens.Colors.Gold,
                    modifier = Modifier.widthIn(max = 100.dp)
                )

                // Settings / Parents Entry
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GameUiTokens.Colors.Surface)
                        .border(1.5.dp, GameUiTokens.Colors.Border, CircleShape)
                        .clickable { onNavigateToSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = GameUiTokens.Colors.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Full-Width Micro EXP Progress Line
        val progress = if (maxExp > 0) exp.toFloat() / maxExp else 0f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(GameUiTokens.Colors.Surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                GameUiTokens.Colors.NeonCyan,
                                GameUiTokens.Colors.NeonCyan.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }
    }
}
