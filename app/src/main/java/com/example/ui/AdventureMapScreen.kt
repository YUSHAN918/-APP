package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureMapScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNavigateToLevels: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToHoliday: () -> Unit,
    onNavigateToPetHouse: () -> Unit
) {
    val player by viewModel.playerProfile.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🗺️ 奇幻文字远征疆界",
                            style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("map_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    player?.let { p ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            // Coins Info
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("🪙", fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${p.coins}",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFFBBF24)),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Level Info
                            Surface(
                                color = Color(0xFF2563EB),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("⭐", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "阶位.LV ${p.level}",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF09090F)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF09090F),
                            Color(0xFF141424),
                            Color(0xFF08080D)
                        )
                    )
                )
        ) {
            // Drawn decorative connections (Star routes) in background
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(950.dp)
            ) {
                val width = size.width
                // Estimated card center positions
                val points = listOf(
                    Offset(width * 0.5f, 100.dp.toPx()),
                    Offset(width * 0.25f, 290.dp.toPx()),
                    Offset(width * 0.75f, 480.dp.toPx()),
                    Offset(width * 0.3f, 670.dp.toPx()),
                    Offset(width * 0.5f, 860.dp.toPx())
                )

                // Draw neon energy path between consecutive points
                for (i in 0 until points.size - 1) {
                    val p1 = points[i]
                    val p2 = points[i + 1]

                    val control1 = Offset(p1.x, (p1.y + p2.y) / 2)
                    val control2 = Offset(p2.x, (p1.y + p2.y) / 2)

                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(p1.x, p1.y)
                        cubicTo(control1.x, control1.y, control2.x, control2.y, p2.x, p2.y)
                    }

                    // Main glow path
                    drawPath(
                        path = path,
                        color = Color(0xFF38BDF8).copy(alpha = 0.3f),
                        style = Stroke(
                            width = 12f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 12f), 0f)
                        )
                    )

                    // Core bright path
                    drawPath(
                        path = path,
                        color = Color(0xFF06B6D4),
                        style = Stroke(
                            width = 4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 12f), 0f)
                        )
                    )
                }
            }

            // Cards in scrolling column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                // 1. 字词森林 (Word Forest)
                MapAreaCard(
                    title = "🌲 字词试炼森林",
                    description = "日常字词关卡、写字与默写冒险，击退突袭的错字魔灵！",
                    actionText = "进入战区",
                    glowColor = Color(0xFF10B981),
                    testTag = "forest_card",
                    alignment = Alignment.CenterHorizontally,
                    onClick = onNavigateToLevels
                )

                // 2. 古诗山谷 (Poetry Valley)
                MapAreaCard(
                    title = "⛰️ 声澜古诗密谷",
                    description = "朗吟千古诗词，破译墨韵密卷，开启古韵声音听写魔法！",
                    actionText = "吟诵破译",
                    glowColor = Color(0xFF8B5CF6),
                    testTag = "poetry_card",
                    alignment = Alignment.Start,
                    modifier = Modifier.padding(start = 24.dp, end = 48.dp),
                    onClick = onNavigateToHoliday
                )

                // 3. 错题魔窟 (Error Dungeon)
                MapAreaCard(
                    title = "😈 错灵净化深渊",
                    description = "狩猎并净化盘踞于识字死角的邪恶错词魔灵，解救字魂伙伴！",
                    actionText = "前往净化",
                    glowColor = Color(0xFFF59E0B),
                    testTag = "error_card",
                    alignment = Alignment.End,
                    modifier = Modifier.padding(start = 48.dp, end = 24.dp),
                    onClick = onNavigateToReview
                )

                // 4. 暑假委托所 (Summer Assignment Bureau)
                MapAreaCard(
                    title = "📋 暑期远征委托所",
                    description = "大教官专属下发暑期每日作业，签收羊皮纸契约，斩获远征巨资！",
                    actionText = "审阅委托",
                    glowColor = Color(0xFFEF4444),
                    testTag = "holiday_card",
                    alignment = Alignment.Start,
                    modifier = Modifier.padding(start = 24.dp, end = 48.dp),
                    onClick = onNavigateToHoliday
                )

                // 5. 宠物训练场 (Pet Training Ground)
                MapAreaCard(
                    title = "🐾 契约字灵神域",
                    description = "喂食、训练、抚摸你的随行小契约伙伴（小墨龙/云狐），共建魂契！",
                    actionText = "进入神域",
                    glowColor = Color(0xFF06B6D4),
                    testTag = "pet_card",
                    alignment = Alignment.CenterHorizontally,
                    onClick = onNavigateToPetHouse
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun MapAreaCard(
    title: String,
    description: String,
    actionText: String,
    glowColor: Color,
    testTag: String,
    alignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val glow = QuestAnimation.rememberGlowIntensity()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        contentAlignment = when (alignment) {
            Alignment.Start -> Alignment.CenterStart
            Alignment.End -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        Surface(
            color = Color(0xFF14141E),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp, 
                glowColor.copy(alpha = 0.4f + 0.4f * glow)
            ),
            shadowElevation = 8.dp,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth(0.85f)
                .clickable { onClick() }
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8)),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = glowColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = actionText,
                            style = MaterialTheme.typography.labelLarge.copy(color = Color.White),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
