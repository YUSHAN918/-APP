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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.viewmodel.GameViewModel
import com.example.data.PlayerBrushConfig
import kotlinx.coroutines.launch
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrushDetailScreen(
    viewModel: GameViewModel,
    brushId: String,
    navController: NavController,
    onBack: () -> Unit,
    onNavigateToTuning: (String) -> Unit
) {
    val context = LocalContext.current
    val playerProfile by viewModel.playerProfile.collectAsState()
    val brush = BrushStyle.getBrushById(brushId)
    val unlockedBrushes = playerProfile?.unlockedBrushIds?.split(",") ?: listOf("default_black", "practice_wood")
    val isUnlocked = unlockedBrushes.contains(brushId)
    val isCurrentlyEquipped = playerProfile?.equippedBrushId == brushId

    var config by remember { mutableStateOf<PlayerBrushConfig?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var handwritingView by remember { mutableStateOf<HandwritingView?>(null) }
    var isCanvasDarkTheme by remember { mutableStateOf(false) }
    var currentWatermark by remember { mutableStateOf("神") }
    var gridPattern by remember { mutableStateOf("4x1") } // "4x1", "2x2", "1x1"
    var strokeCount by remember { mutableStateOf(0) }

    LaunchedEffect(brushId) {
        val loaded = viewModel.getBrushConfig(brushId)
        if (loaded != null) {
            config = loaded
        } else {
            // Default config mapping
            val profile = viewModel.playerProfile.value
            config = PlayerBrushConfig(
                accountId = profile?.accountId ?: 0L,
                playerId = profile?.id ?: 0L,
                brushId = brushId,
                baseWidth = brush.maxWidth,
                minWidth = brush.minWidth,
                maxWidth = brush.maxWidth,
                pressureSensitivity = 1.0f,
                speedSensitivity = 1.2f,
                smoothing = 0.35f,
                opacity = 1.0f,
                glowRadius = if (brush.glowEnabled) 16f else 0f,
                particleDensity = if (brush.particleEnabled) 1.0f else 0f,
                effectIntensity = 1.0f,
                colorHex = "default",
                usageMode = "TEST_SAFE",
                widthMode = "AUTO"
            )
        }
        isLoading = false
    }

    // Dynamic suitabilities
    val (examStars, writeStars, artStars) = when (brushId) {
        "default_black" -> Triple(5, 4, 2)
        "practice_wood" -> Triple(5, 5, 2)
        "ink_brush" -> Triple(3, 5, 4)
        "stardust_brush" -> Triple(2, 3, 5)
        "fluorescent_brush" -> Triple(1, 3, 5)
        "rainbow_brush" -> Triple(1, 3, 5)
        "pet_dragon_brush" -> Triple(2, 4, 5)
        else -> Triple(3, 3, 3)
    }

    val attackEffectText = when (brushId) {
        "default_black" -> "💥 挥舞【默认黑笔】！基础物理打击，克制偏远生僻字！"
        "practice_wood" -> "💥 挥舞【练习木笔】！发出木头撞击的「清脆脆响」，直接击碎魔物护甲！"
        "ink_brush" -> "💥 泼洒【墨韵毛笔】！古墨香气四溢，挥出千钧「泼墨气流」重创魔物！"
        "stardust_brush" -> "💥 触发【星尘笔】！星光词气爆裂，如「烈焰熔岩」般将魔物彻底包围！"
        "fluorescent_brush" -> "💥 射出【荧光笔】！荧光折射成「冰晶碎屑」，极寒冷气刺骨斩击！"
        "rainbow_brush" -> "💥 鸣动【彩虹笔】！苍穹之上悬落「彩虹圣光」，直轰魔物核心弱点！"
        "pet_dragon_brush" -> "💥 联手【小墨龙之笔】！笔墨幻化为「小墨龙之影」，一口黑焰墨息重创魔物！"
        else -> "💥 挥舞神兵，震退错字深渊魔物！"
    }

    val tipFeatureText = when (brushId) {
        "default_black" -> "标准正姿线条，出笔稳定、粗细恒定，是日常书写的基本功表现。"
        "practice_wood" -> "原木质感，适合新手校准字形，具备平滑连贯的书写感。"
        "ink_brush" -> "模拟宣纸书写，出墨饱满，支持精细的压感变化，尽显隶行草书风骨。"
        "stardust_brush" -> "神秘星河色相，笔画中伴随不断幻灭的粉紫色宇宙碎屑，灵动飘逸。"
        "fluorescent_brush" -> "外覆霓虹电光镀层，适合深夜研习模式，笔画炫酷，极具视觉冲击力。"
        "rainbow_brush" -> "随着运笔路径渐变彩虹七色，每一笔划都是流动的彩虹画卷。"
        "pet_dragon_brush" -> "熔岩红与墨黑交融，笔法遒劲有力，伴有火红星辉颗粒拖尾特效。"
        else -> "拥有独特出笔手感，助力勇者攻克错字难关。"
    }

    val rarityColor = when (brush.rarity) {
        "NORMAL" -> Color.Gray
        "RARE" -> Color(0xFF1E88E5)
        "EPIC" -> Color(0xFF8E24AA)
        "LEGEND" -> Color(0xFFF57C00)
        else -> Color.Gray
    }

    val rarityChinese = when (brush.rarity) {
        "NORMAL" -> "普通武器"
        "RARE" -> "精良神兵"
        "EPIC" -> "史诗武装"
        "LEGEND" -> "传奇圣物"
        else -> "未知装备"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗡️ 神兵武器档案", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFFFD700)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF140F0A),
                    titleContentColor = Color(0xFFFFD700),
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1C1611)
    ) { innerPadding ->
        if (isLoading || config == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFD700))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFF1C1611))
            ) {
                // --- Elegant Compact Weapon Header ---
                val brushEmoji = when (brushId) {
                    "default_black" -> "✒️"
                    "practice_wood" -> "✏️"
                    "ink_brush" -> "🖌️"
                    "stardust_brush" -> "✨"
                    "fluorescent_brush" -> "🖍️"
                    "rainbow_brush" -> "🌈"
                    "pet_dragon_brush" -> "🐉"
                    else -> "🖋️"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF231B15)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, rarityColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji Icon
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFF16110D), CircleShape)
                                .border(1.5.dp, rarityColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(brushEmoji, fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1.5f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = brush.brushName,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(rarityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, rarityColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = rarityChinese,
                                        color = rarityColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "特性: $tipFeatureText",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Unlock status flag
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isUnlocked) "已契约" else "未解锁",
                                color = if (isUnlocked) Color(0xFF81C784) else Color(0xFFE57373),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isUnlocked) "唤醒成功" else "等待解锁",
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // --- ✍️ Centerpiece: Elegant Calligraphy trial Canvas ---
                Text(
                    text = "✍️ 临摹试练场 (即席试写)",
                    color = Color(0xFFFFD700),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Massive Canvas Container with rarity-colored glowing border
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(Color.Black)
                            .border(
                                width = 2.dp,
                                color = rarityColor.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                HandwritingView(ctx).apply {
                                    currentBrush = brush
                                    currentBrushConfig = config
                                    watermarkText = currentWatermark
                                    isDarkTheme = isCanvasDarkTheme
                                    onStrokeFinished = {
                                        strokeCount++
                                    }
                                    handwritingView = this
                                }
                            },
                            update = { view ->
                                view.currentBrush = brush
                                view.currentBrushConfig = config
                                view.watermarkText = if (currentWatermark == "空") null else currentWatermark
                                view.isDarkTheme = isCanvasDarkTheme
                                when (gridPattern) {
                                    "4x1" -> view.setGrid(4, 1)
                                    "2x2" -> view.setGrid(2, 2)
                                    "1x1" -> view.setGrid(1, 1)
                                }
                            }
                        )

                        // Float Active Telemetry Label in Top End Corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isCanvasDarkTheme) "🌌 玄铁暗格" else "📜 宣纸红格",
                                color = if (isCanvasDarkTheme) Color(0xFF00E5FF) else Color(0xFFE53935),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Control Toolbar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clear Button
                        Button(
                            onClick = { handwritingView?.clear() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("🧹 清空字迹", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Undo Button
                        Button(
                            onClick = { handwritingView?.undo() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("↩️ 撤销一笔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Theme Toggle Button
                        Button(
                            onClick = { isCanvasDarkTheme = !isCanvasDarkTheme },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2638)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (isCanvasDarkTheme) "☀️ 宣纸模式" else "🌙 暗黑模式",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCanvasDarkTheme) Color(0xFFFFB74D) else Color(0xFF81D4FA)
                            )
                        }

                        // Grid Pattern Selector Trigger
                        Button(
                            onClick = {
                                gridPattern = when (gridPattern) {
                                    "4x1" -> "2x2"
                                    "2x2" -> "1x1"
                                    else -> "4x1"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E231B)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = when (gridPattern) {
                                    "4x1" -> "📐 米字格"
                                    "2x2" -> "📐 田字格"
                                    else -> "📐 单宫格"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Beautiful Calligraphy Watermark Selection ribbon
                    Text(
                        text = "💡 点击载入临摹底稿 (大字水印导引):",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    val watermarkList = listOf("神", "墨", "锋", "魂", "书", "剑", "空")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        watermarkList.forEach { word ->
                            val isSelected = currentWatermark == word
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .background(
                                        color = if (isSelected) rarityColor.copy(alpha = 0.15f) else Color(0xFF261D15),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) rarityColor else Color.White.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        currentWatermark = word
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (word == "空") "无水印" else word,
                                    color = if (isSelected) rarityColor else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- 📊 Real-time Dynamic Telemetry HUD Box ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14191C)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📊 实时神笔墨理诊断 (动态计算)",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (strokeCount > 0) Color(0xFF00E676) else Color(0xFFFF9100), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (strokeCount > 0) "已感应笔划: $strokeCount" else "待落笔感应",
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Column 1
                            Column(modifier = Modifier.weight(1f)) {
                                val minW = handwritingView?.lastWidthMinReal ?: 0f
                                val maxW = handwritingView?.lastWidthMaxReal ?: 0f
                                val widthRangeStr = if (strokeCount == 0) "0.0 dp" else String.format("%.1f-%.1f dp", minW, maxW)
                                Text("实测笔迹跨度:", color = Color.Gray, fontSize = 10.sp)
                                Text(widthRangeStr, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            // Column 2
                            Column(modifier = Modifier.weight(1f)) {
                                val maxPressure = handwritingView?.lastPressureMax ?: 0f
                                val pStr = if (strokeCount == 0) "0.0" else String.format("%.2f", maxPressure)
                                Text("最大触屏压感:", color = Color.Gray, fontSize = 10.sp)
                                Text(pStr, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            // Column 3
                            Column(modifier = Modifier.weight(1.2f)) {
                                val maxSpd = handwritingView?.lastSpeedMax ?: 0f
                                val spdStr = if (strokeCount == 0) "0.00" else String.format("%.2f dp/ms", maxSpd)
                                Text("运笔速度极值:", color = Color.Gray, fontSize = 10.sp)
                                Text(spdStr, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- Combat Effect & Rating Suitability ---
                Text(
                    text = "🛡️ 兵刃奥义与推荐适用",
                    color = Color(0xFFFFD700),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF281E15)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "⚔️ 净化击退特效:",
                            color = Color(0xFFFFB74D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = attackEffectText,
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "🎯 契合度评级:",
                            color = Color(0xFFFFB74D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            SuitabilityColumn(label = "日常考试", stars = examStars, activeColor = Color(0xFF4CAF50))
                            SuitabilityColumn(label = "正姿练字", stars = writeStars, activeColor = Color(0xFF2196F3))
                            SuitabilityColumn(label = "艺术创作", stars = artStars, activeColor = Color(0xFFE91E63))
                        }
                    }
                }

                // --- Current Config Specs ---
                Text(
                    text = "📐 物理法理参数",
                    color = Color(0xFFFFD700),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF281E15))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SpecRow("基础粗细 (Base)", "${config!!.baseWidth.toInt()} dp")
                        SpecRow("笔锋跨度 (Min-Max)", "${config!!.minWidth.toInt()} - ${config!!.maxWidth.toInt()} dp")
                        SpecRow("速度压感估算", if (config!!.widthMode == "FORCE_SPEED") "强制速度 (强笔锋)" else "自适应自动")
                        SpecRow("透明度 (Opacity)", "${(config!!.opacity * 100).toInt()}%")
                        SpecRow("发光强度 (Glow)", "${config!!.glowRadius.toInt()} px")
                        SpecRow("粒子尘埃 (Particle)", "${(config!!.particleDensity * 100).toInt()}%")
                        SpecRow("笔迹色彩", if (config!!.colorHex == "default") "神兵本色" else "自定义五彩")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- Bottom Action Area ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isUnlocked) {
                        Button(
                            onClick = {
                                viewModel.equipBrush(brushId)
                            },
                            enabled = !isCurrentlyEquipped,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                disabledContainerColor = Color(0xFF1F1811)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (isCurrentlyEquipped) "⚔️ 已装备" else "⚔️ 立即装备",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = { onNavigateToTuning(brushId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🛠️ 进入调校", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                BrushNavigator.navigateToShop(navController)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🛒 前往商店解锁该神兵", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuitabilityColumn(label: String, stars: Int, activeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
        Row {
            for (i in 1..5) {
                Text(
                    text = if (i <= stars) "★" else "☆",
                    color = if (i <= stars) activeColor else Color.DarkGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
