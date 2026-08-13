package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel

@Composable
fun CampHomeContent(
    viewModel: GameViewModel,
    onNavigateToLevels: () -> Unit,
    onNavigateToPetHouse: () -> Unit,
    onNavigateToHoliday: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToChushibiao: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerProfile by viewModel.playerProfile.collectAsState()
    val activePetBinding by viewModel.activePet.collectAsState()
    val allHolidayTasks by viewModel.allHolidayTasks.collectAsState()
    val dailyQuests by viewModel.dailyQuests.collectAsState()
    
    val name = playerProfile?.playerName ?: "小字灵"
    val playerLevel = playerProfile?.level ?: 1
    
    val isPetEgg = activePetBinding?.lifeStage == "EGG"
    
    val petName = when (activePetBinding?.lifeStage) {
        "EGG" -> "未知字灵蛋"
        "SOUL_SLEEP" -> "${activePetBinding?.customName ?: activePetBinding?.petName} (长眠)"
        else -> activePetBinding?.customName ?: activePetBinding?.petName ?: "伙伴"
    }

    // Daily task completion stats
    val totalTasks = allHolidayTasks.size
    val completedTasks = allHolidayTasks.count { it.status == "COMPLETED" }
    
    // Pick the most relevant daily quest as "Today's Adventure"
    val mainQuest = dailyQuests.firstOrNull { !it.isClaimed } ?: dailyQuests.firstOrNull()
    val mainQuestTitle = mainQuest?.title ?: "字词试炼森林"
    val mainQuestDesc = mainQuest?.description ?: "探索未知的字词区域，收集经验与金币！"
    val mainQuestProgress = if (mainQuest != null) "进度 ${mainQuest.currentProgress} / ${mainQuest.targetProgress}" else "推荐"
    
    val onMainQuestClick = {
        when (mainQuest?.id) {
            "dictation" -> onNavigateToLevels()
            "holiday" -> onNavigateToHoliday()
            "error_purify" -> onNavigateToReview()
            "recitation" -> onNavigateToReport()
            else -> onNavigateToLevels()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GameUiTokens.Colors.Background,
                        Color(0xFF212A3A)
                    )
                )
            )
            .drawBehind {
                // High-altitude ambient light 1 (Soft ice-blue top-left corner)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GameUiTokens.Colors.NeonCyan.copy(alpha = 0.08f), Color.Transparent),
                        radius = size.width * 0.8f
                    ),
                    radius = size.width * 0.8f,
                    center = androidx.compose.ui.geometry.Offset(0f, 0f)
                )
                // Ambient light 2 (Warm sunset-amber bottom-right corner)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GameUiTokens.Colors.Gold.copy(alpha = 0.06f), Color.Transparent),
                        radius = size.width * 1.0f
                    ),
                    radius = size.width * 1.0f,
                    center = androidx.compose.ui.geometry.Offset(size.width, size.height)
                )
            }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            
            // 1. Hero Stage (Center Avatar & Pet)
            CampHeroStage(
                playerName = name,
                playerLevel = playerLevel,
                isPetEgg = isPetEgg,
                petName = if (activePetBinding != null) petName else null,
                onPetClick = onNavigateToPetHouse,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 240.dp)
                    .padding(vertical = 12.dp)
            )
            
            // Fixed small spacer instead of weight(1f) to prevent large gaps
            Spacer(modifier = Modifier.height(12.dp))

            // 2. Today's Adventure Main Card
            TodayAdventureCard(
                title = mainQuestTitle,
                description = mainQuestDesc,
                progressText = mainQuestProgress,
                buttonText = if (mainQuest?.currentProgress == mainQuest?.targetProgress && mainQuest?.isClaimed == false) "领取奖励" else "开始冒险",
                onButtonClick = {
                    if (mainQuest?.currentProgress == mainQuest?.targetProgress && mainQuest?.isClaimed == false) {
                        viewModel.claimQuestReward(mainQuest.id)
                    } else {
                        onMainQuestClick()
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Secondary Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CampSecondaryCard(
                    icon = Icons.Default.List,
                    title = "暑期委托所",
                    subtitle = "进度 $completedTasks / ${if(totalTasks>0) totalTasks else 3}",
                    onClick = onNavigateToHoliday,
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                )
                CampSecondaryCard(
                    icon = Icons.Default.Pets,
                    title = "伙伴小屋",
                    subtitle = petName,
                    onClick = onNavigateToPetHouse,
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // 4. Custom 武侯诵读阁 《出师表》 Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(GameUiTokens.Shapes.Panel)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                GameUiTokens.Colors.Surface,
                                Color(0xFF2E394A)
                            )
                        )
                    )
                    .border(1.dp, GameUiTokens.Colors.Gold.copy(alpha = 0.5f), GameUiTokens.Shapes.Panel)
                    .clickable { onNavigateToChushibiao() }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GameUiTokens.Colors.Gold.copy(alpha = 0.12f))
                            .border(1.dp, GameUiTokens.Colors.Gold.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "出师表",
                            tint = GameUiTokens.Colors.Gold,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📜 武侯诵读阁",
                                color = GameUiTokens.Colors.TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(GameUiTokens.Colors.NeonRed, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "出师表",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "少儿专属品读、音频诵读跟读、背诵大挑战！获双倍奖励",
                            color = GameUiTokens.Colors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "打开",
                        tint = GameUiTokens.Colors.TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
