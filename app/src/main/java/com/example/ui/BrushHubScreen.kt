package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel
import com.example.data.ItemDefinition

@Composable
fun BrushHubScreen(
    viewModel: GameViewModel,
    onNavigateToBrushLibrary: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToBrushTuning: (String) -> Unit,
    onNavigateToBrushDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playerProfile by viewModel.playerProfile.collectAsState()
    val inventoryItems by viewModel.inventoryItems.collectAsState()

    val currentCoins = playerProfile?.coins ?: 0
    val equippedBrushId = playerProfile?.equippedBrushId ?: "default_black"
    val unlockedBrushes = playerProfile?.unlockedBrushIds?.split(",") ?: listOf("default_black", "practice_wood")

    // Active sub-tab state (装备 / 笔库 / 获取)
    var selectedSubTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("装备", "笔库", "获取")

    // Non-blocking purchase success context
    var justUnlockedBrushId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.checkAndUnlockBrushes()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameUiTokens.Colors.Background)
    ) {
        // 1. Orbital Header with Dynamic Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🖌️ 神笔中心",
                    color = GameUiTokens.Colors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "墨池探星，试锋调墨解乾坤",
                    color = GameUiTokens.Colors.TextMuted,
                    fontSize = 11.sp
                )
            }

            // High Tech Coin display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .border(1.dp, GameUiTokens.Colors.Border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("🪙", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$currentCoins",
                    color = Color(0xFFFFD700),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. Tab Selectors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabTitles.forEachIndexed { index, title ->
                val isSelected = selectedSubTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) GameUiTokens.Colors.Surface else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) GameUiTokens.Colors.Border.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            selectedSubTab = index
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) GameUiTokens.Colors.TextPrimary else GameUiTokens.Colors.TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedSubTab) {
                0 -> {
                    // ------------------ Tab 1: 装备 ------------------
                    val brush = BrushStyle.getBrushById(equippedBrushId)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // High-tech Aura equipped card (Clickable to Details!)
                        GamePanel(
                            title = "当前契约·执掌神兵",
                            borderColor = GameUiTokens.Colors.NeonAmber,
                            glowColor = GameUiTokens.Colors.NeonAmber.copy(alpha = 0.4f),
                            modifier = Modifier.clickable {
                                onNavigateToBrushDetail(equippedBrushId)
                            }
                        ) {
                            val brushColor = Color(brush.baseColor)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Circular Aura indicator
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(brushColor.copy(alpha = 0.15f))
                                            .border(2.dp, brushColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Brush,
                                            contentDescription = brush.brushName,
                                            tint = brushColor,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = brush.brushName,
                                            color = GameUiTokens.Colors.TextPrimary,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = when (brush.rarity) {
                                                    "LEGEND" -> Color(0xFFFF3D00).copy(alpha = 0.15f)
                                                    "EPIC" -> Color(0xFFD500F9).copy(alpha = 0.15f)
                                                    "RARE" -> Color(0xFF00E5FF).copy(alpha = 0.15f)
                                                    else -> Color.Gray.copy(alpha = 0.15f)
                                                },
                                                border = BorderStroke(
                                                    1.dp,
                                                    when (brush.rarity) {
                                                        "LEGEND" -> Color(0xFFFF3D00)
                                                        "EPIC" -> Color(0xFFD500F9)
                                                        "RARE" -> Color(0xFF00E5FF)
                                                        else -> Color.Gray
                                                    }
                                                )
                                            ) {
                                                Text(
                                                    text = when (brush.rarity) {
                                                        "LEGEND" -> "传奇圣物"
                                                        "EPIC" -> "史诗武装"
                                                        "RARE" -> "精良神兵"
                                                        else -> "普通武器"
                                                    },
                                                    color = when (brush.rarity) {
                                                        "LEGEND" -> Color(0xFFFF3D00)
                                                        "EPIC" -> Color(0xFFD500F9)
                                                        "RARE" -> Color(0xFF00E5FF)
                                                        else -> Color.LightGray
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                text = "• 已装备在身",
                                                color = GameUiTokens.Colors.NeonGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.align(Alignment.CenterVertically)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Realistic brushstroke simulation curve
                                Text(
                                    text = "📐 物理墨意预览：",
                                    color = GameUiTokens.Colors.TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CosmicBrushStrokePreview(brush = brush)

                                Spacer(modifier = Modifier.height(12.dp))

                                // Essential core stats summary
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatBlock(label = "笔锋跨度", value = "${brush.minWidth.toInt()}-${brush.maxWidth.toInt()}dp", modifier = Modifier.weight(1f))
                                    StatBlock(label = "发光强度", value = if (brush.glowEnabled) "🌟 高灵" else "无特效", color = if (brush.glowEnabled) GameUiTokens.Colors.NeonCyan else GameUiTokens.Colors.TextMuted, modifier = Modifier.weight(1f))
                                    StatBlock(label = "粒子风暴", value = if (brush.particleEnabled) "✨ 尘埃" else "无特效", color = if (brush.particleEnabled) GameUiTokens.Colors.NeonGreen else GameUiTokens.Colors.TextMuted, modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        // Prominent primary test action & secondary configurations
                        Button(
                            onClick = { onNavigateToBrushTuning(equippedBrushId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GameUiTokens.Colors.NeonAmber,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Brush, contentDescription = "试写", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("✍️ 试写当前神笔", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onNavigateToBrushTuning(equippedBrushId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GameUiTokens.Colors.Surface,
                                    contentColor = GameUiTokens.Colors.TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GameUiTokens.Colors.Border.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Tune, contentDescription = "调校", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🛠️ 调校参数", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { selectedSubTab = 1 }, // Change page-tab to 1 (Library)
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GameUiTokens.Colors.Surface,
                                    contentColor = GameUiTokens.Colors.TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GameUiTokens.Colors.Border.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Palette, contentDescription = "更换", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🔄 更换神笔", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                1 -> {
                    // ------------------ Tab 2: 笔库 ------------------
                    val ownedBrushes = BrushStyle.ALL_BRUSHES.filter { unlockedBrushes.contains(it.brushId) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Total count indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "已解封神兵武器",
                                color = GameUiTokens.Colors.TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "已拥有 ${ownedBrushes.size} / ${BrushStyle.ALL_BRUSHES.size}",
                                color = GameUiTokens.Colors.NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ownedBrushes.forEach { brush ->
                                val isEquipped = brush.brushId == equippedBrushId
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isEquipped) Color(0xFF2C2219) else GameUiTokens.Colors.Surface
                                    ),
                                    border = BorderStroke(
                                        width = if (isEquipped) 1.5.dp else 1.dp,
                                        color = if (isEquipped) GameUiTokens.Colors.NeonAmber else GameUiTokens.Colors.Border.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToBrushDetail(brush.brushId) }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(brush.baseColor).copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Brush,
                                                        contentDescription = "画笔",
                                                        tint = Color(brush.baseColor),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                Column {
                                                    Text(
                                                        text = brush.brushName,
                                                        color = GameUiTokens.Colors.TextPrimary,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "稀有度: ${brush.rarity}",
                                                        color = when (brush.rarity) {
                                                            "LEGEND" -> Color(0xFFFF9800)
                                                            "EPIC" -> Color(0xFFE040FB)
                                                            "RARE" -> Color(0xFF00E5FF)
                                                            else -> GameUiTokens.Colors.TextMuted
                                                        },
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }

                                            // Equipped status label or action button
                                            if (isEquipped) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "已装备",
                                                        tint = GameUiTokens.Colors.NeonGreen,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "已装备",
                                                        color = GameUiTokens.Colors.NeonGreen,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            } else {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.equipBrush(brush.brushId)
                                                            Toast.makeText(context, "⚔️ 已装备神兵 ${brush.brushName}", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFF382A1C),
                                                            contentColor = GameUiTokens.Colors.TextPrimary
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                                        modifier = Modifier.height(28.dp)
                                                    ) {
                                                        Text("装备", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    Button(
                                                        onClick = { onNavigateToBrushDetail(brush.brushId) },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color.Transparent,
                                                            contentColor = GameUiTokens.Colors.TextSecondary
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(1.dp, GameUiTokens.Colors.Border.copy(alpha = 0.4f)),
                                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                                        modifier = Modifier.height(28.dp)
                                                    ) {
                                                        Text("详情", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        CosmicBrushStrokePreview(brush = brush)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                2 -> {
                    // ------------------ Tab 3: 获取 ------------------
                    val displayBrushes = BrushStyle.ALL_BRUSHES
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "神墨殿堂·全谱系契约神笔",
                            color = GameUiTokens.Colors.TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            displayBrushes.forEach { brush ->
                                val isUnlocked = unlockedBrushes.contains(brush.brushId)
                                val shopItemId = when (brush.brushId) {
                                    "stardust_brush" -> "star_brush"
                                    "fluorescent_brush" -> "glow_brush"
                                    "pet_dragon_brush" -> "pet_brush_molong"
                                    "ink_brush" -> "ink_brush"
                                    else -> null
                                }

                                val shopItem = if (shopItemId != null) {
                                    ItemDefinition.ALL_ITEMS.find { it.itemId == shopItemId }
                                } else null

                                val isPurchaseable = shopItem != null && !isUnlocked
                                val cost = shopItem?.priceCoins ?: 0
                                val canAfford = currentCoins >= cost

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUnlocked) Color(0xFF1E2320) else GameUiTokens.Colors.Surface
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isUnlocked) GameUiTokens.Colors.NeonGreen.copy(alpha = 0.3f) else GameUiTokens.Colors.Border.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Black.copy(alpha = 0.4f))
                                                        .border(
                                                            1.dp,
                                                            if (isUnlocked) GameUiTokens.Colors.NeonGreen.copy(alpha = 0.6f) else Color(brush.baseColor).copy(alpha = 0.6f),
                                                            CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                                        contentDescription = if (isUnlocked) "已解封" else "未解锁",
                                                        tint = if (isUnlocked) GameUiTokens.Colors.NeonGreen else Color(brush.baseColor).copy(alpha = 0.8f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                Column {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = brush.brushName,
                                                            color = GameUiTokens.Colors.TextPrimary,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        if (isUnlocked) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(GameUiTokens.Colors.NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                                    .border(0.5.dp, GameUiTokens.Colors.NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                                            ) {
                                                                Text("已契约", color = GameUiTokens.Colors.NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = when (brush.rarity) {
                                                            "LEGEND" -> "传奇圣物"
                                                            "EPIC" -> "史诗武装"
                                                            "RARE" -> "精良神兵"
                                                            else -> "普通武器"
                                                        },
                                                        color = when (brush.rarity) {
                                                            "LEGEND" -> Color(0xFFFF3D00)
                                                            "EPIC" -> Color(0xFFD500F9)
                                                            "RARE" -> Color(0xFF00E5FF)
                                                            else -> GameUiTokens.Colors.TextMuted
                                                        },
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // Price display
                                            if (isPurchaseable) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("🪙", fontSize = 12.sp)
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = "$cost",
                                                        color = Color(0xFFFFD700),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = if (isUnlocked) "解封状态: 已成功契约并唤醒神兵" else "解封条件: ${brush.unlockCondition}",
                                            color = if (isUnlocked) GameUiTokens.Colors.TextPrimary.copy(alpha = 0.8f) else GameUiTokens.Colors.TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (isUnlocked) {
                                                val isEquipped = brush.brushId == equippedBrushId
                                                if (isEquipped) {
                                                    Button(
                                                        onClick = { },
                                                        enabled = false,
                                                        colors = ButtonDefaults.buttonColors(
                                                            disabledContainerColor = Color(0xFF1B291C),
                                                            disabledContentColor = GameUiTokens.Colors.NeonGreen
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier
                                                            .weight(1.5f)
                                                            .height(34.dp)
                                                    ) {
                                                        Text("⚔️ 当前装备中", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Button(
                                                        onClick = {
                                                            viewModel.equipBrush(brush.brushId)
                                                            Toast.makeText(context, "⚔️ 已装备神兵 ${brush.brushName}", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFF2C1E15),
                                                            contentColor = GameUiTokens.Colors.TextPrimary
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier
                                                            .weight(1.5f)
                                                            .height(34.dp)
                                                    ) {
                                                        Text("装备此神兵", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else if (isPurchaseable && shopItem != null) {
                                                Button(
                                                    onClick = {
                                                        viewModel.purchaseItem(shopItem.itemId) { result ->
                                                            result.onSuccess {
                                                                viewModel.unlockBrushDirect(brush.brushId)
                                                                justUnlockedBrushId = brush.brushId
                                                                Toast.makeText(context, "🎉 成功解锁神笔: ${brush.brushName}！", Toast.LENGTH_SHORT).show()
                                                            }.onFailure { err ->
                                                                Toast.makeText(context, "❌ 无法解封: ${err.message}", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                    },
                                                    enabled = canAfford,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = GameUiTokens.Colors.NeonGreen,
                                                        contentColor = Color.Black,
                                                        disabledContainerColor = Color(0xFF1F241E)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .weight(1.5f)
                                                        .height(34.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text(
                                                        text = if (canAfford) "🛒 立即金币购买" else "金币不足",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            } else {
                                                // Adventure gameplay unlock tips
                                                Button(
                                                    onClick = {
                                                        Toast.makeText(context, "⚔️ 请前往冒险关卡、完成任务挑战解锁！", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF2C1E15),
                                                        contentColor = GameUiTokens.Colors.TextSecondary
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .weight(1.5f)
                                                        .height(34.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text("⚔️ 查看关卡来源", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            // View details
                                            Button(
                                                onClick = { onNavigateToBrushDetail(brush.brushId) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.Transparent,
                                                    contentColor = GameUiTokens.Colors.TextSecondary
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, GameUiTokens.Colors.Border.copy(alpha = 0.4f)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(34.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("查看档案", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    // ------------------ Non-Blocking Interactive Success Overlay ------------------
    AnimatedVisibility(
        visible = justUnlockedBrushId != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        val bId = justUnlockedBrushId
        if (bId != null) {
            val brush = BrushStyle.getBrushById(bId)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { justUnlockedBrushId = null }, // dismiss on backdrop click
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1610)),
                    border = BorderStroke(2.dp, GameUiTokens.Colors.NeonAmber),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) {} // block backdrop click
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 契约解封成功！",
                            color = GameUiTokens.Colors.NeonAmber,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "你已喜得写神传世圣物 ── [${brush.brushName}]",
                            color = GameUiTokens.Colors.TextPrimary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        CosmicBrushStrokePreview(brush = brush, modifier = Modifier.height(40.dp))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.equipBrush(bId)
                                    justUnlockedBrushId = null
                                    selectedSubTab = 0 // back to equipped info
                                    Toast.makeText(context, "⚔️ 已立即装备 ${brush.brushName}", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GameUiTokens.Colors.NeonGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Text("立即装备", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    justUnlockedBrushId = null
                                    onNavigateToBrushTuning(bId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GameUiTokens.Colors.NeonAmber, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Text("立即试写", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    justUnlockedBrushId = null
                                    onNavigateToBrushDetail(bId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = GameUiTokens.Colors.TextPrimary),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GameUiTokens.Colors.Border),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Text("查看档案", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = GameUiTokens.Colors.TextPrimary
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .border(1.dp, GameUiTokens.Colors.Border.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = GameUiTokens.Colors.TextMuted, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun CosmicBrushStrokePreview(
    brush: BrushStyle,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp)
    ) {
        val width = size.width
        val height = size.height
        val path = androidx.compose.ui.graphics.Path()

        // Elegantly drawn sine wave curve to mimic authentic brush weight!
        val points = 50
        val baseColor = Color(brush.baseColor)
        val strokeWidth = brush.maxWidth.dp.toPx()

        for (i in 0..points) {
            val x = (width / points) * i
            val y = height / 2 + Math.sin(i * 0.22).toFloat() * (height * 0.25f)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        if (brush.glowEnabled) {
            // Inner aura blur emission
            drawPath(
                path = path,
                color = baseColor.copy(alpha = 0.35f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth * 1.5f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }

        drawPath(
            path = path,
            color = baseColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth * 0.7f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )

        if (brush.particleEnabled) {
            // Emits shiny particle stardusts on canvas preview!
            val random = java.util.Random(brush.brushId.hashCode().toLong())
            for (i in 0..12) {
                val px = random.nextFloat() * width
                val py = height / 2 + (random.nextFloat() - 0.5f) * height * 0.6f
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 2.5f,
                    center = androidx.compose.ui.geometry.Offset(px, py)
                )
            }
        }
    }
}

/**
 * Public portal card reusable for CompanionHubScreen and GrowthHubScreen to prevent compile errors.
 */
@Composable
fun HubPortalCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, GameUiTokens.Colors.Border.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GameUiTokens.Colors.Surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(24.dp))
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
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "进入",
                tint = GameUiTokens.Colors.TextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
