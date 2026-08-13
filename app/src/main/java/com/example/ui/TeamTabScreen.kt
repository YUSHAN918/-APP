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
import androidx.compose.material3.MaterialTheme
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
fun TeamTabScreen(
    viewModel: GameViewModel,
    onNavigateToPetHouse: () -> Unit,
    onNavigateToBackpack: () -> Unit,
    onNavigateToBrushLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerProfile by viewModel.playerProfile.collectAsState()
    val activePet by viewModel.activePet.collectAsState()
    val inventoryItems by viewModel.inventoryItems.collectAsState()

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

    val petEmoji = when (activePet?.lifeStage) {
        "EGG" -> "🥚"
        "SOUL_SLEEP" -> "👻"
        else -> when (activePet?.petId) {
            "小墨龙" -> "🐲"
            "小书灵" -> "📚"
            "小云狐" -> "🦊"
            "小竹猫" -> "🐼"
            else -> "✨"
        }
    }
    
    val petName = when (activePet?.lifeStage) {
        "EGG" -> "未知字灵蛋"
        "SOUL_SLEEP" -> "${activePet?.customName ?: activePet?.petName} (长眠中)"
        else -> activePet?.customName ?: activePet?.petName ?: "暂无契约伙伴"
    }

    val equippedBrushId = playerProfile?.equippedBrushId ?: "default_black"
    val brush = com.example.ui.BrushStyle.getBrushById(equippedBrushId)

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
            text = "🛡️ 队伍与神兵配置",
            color = GameUiTokens.Colors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // 1. Player & Pet Character Panel
        GamePanel(
            title = "契约行者与守护字灵",
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
                        text = "等级 $level 行者",
                        color = GameUiTokens.Colors.TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "成长值: $exp / $maxExp",
                        color = GameUiTokens.Colors.NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GameUiTokens.Colors.Border.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Active Pet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(GameUiTokens.Shapes.Panel)
                    .background(GameUiTokens.Colors.SurfaceVariant.copy(alpha = 0.5f))
                    .clickable { onNavigateToPetHouse() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GameUiTokens.Colors.Surface)
                        .border(1.dp, GameUiTokens.Colors.NeonAmber.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(petEmoji, fontSize = 28.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = petName,
                        color = GameUiTokens.Colors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "契约亲密度: ${activePet?.intimacy ?: 0}",
                        color = GameUiTokens.Colors.NeonAmber,
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("前往神殿", color = GameUiTokens.Colors.TextSecondary, fontSize = 12.sp)
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "查看伙伴",
                        tint = GameUiTokens.Colors.TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // 2. Weapons / Brush Panel
        GamePanel(
            title = "神兵画笔配置",
            borderColor = GameUiTokens.Colors.Border,
            glowColor = GameUiTokens.Colors.NeonAmber
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(GameUiTokens.Shapes.Panel)
                    .background(GameUiTokens.Colors.SurfaceVariant.copy(alpha = 0.5f))
                    .clickable { onNavigateToBrushLibrary() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Brush color circle
                val brushColor = Color(brush.baseColor)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(brushColor)
                        .border(1.5.dp, GameUiTokens.Colors.TextPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = "笔触",
                        tint = if (brushColor == Color.Black || brushColor == Color(0xFF1A1A1A)) Color.White else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = brush.brushName,
                        color = GameUiTokens.Colors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "品质: ${brush.rarity} | 笔锋粗细: ${brush.minWidth}-${brush.maxWidth}dp",
                        color = GameUiTokens.Colors.TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("神兵工坊", color = GameUiTokens.Colors.TextSecondary, fontSize = 12.sp)
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "画笔工坊",
                        tint = GameUiTokens.Colors.TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // 3. Backpack & Inventory Quick Access
        GamePrimaryButton(
            text = "💼 开启探险背包",
            onClick = onNavigateToBackpack,
            modifier = Modifier.fillMaxWidth(),
            accentColor = GameUiTokens.Colors.NeonGreen
        )
    }
}
