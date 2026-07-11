package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    onNavigateToBrushLibrary: () -> Unit
) {
    val stats by viewModel.userStats.collectAsState()
    val playerProfile by viewModel.playerProfile.collectAsState()
    val wrongWords by viewModel.wrongWords.collectAsState()
    val allHolidayTasks by viewModel.allHolidayTasks.collectAsState()
    val activePetBinding by viewModel.activePet.collectAsState()
    val dailyQuests by viewModel.dailyQuests.collectAsState()
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

    var showPinDialog by remember { mutableStateOf(false) }
    var showBrushLibrary by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showDevHUDPanel by remember { mutableStateOf(false) }

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

    val name = playerProfile?.playerName ?: "小字灵"
    val avatarEmoji = when (playerProfile?.avatarId) {
        1 -> "🛡️"
        2 -> "📖"
        3 -> "🧙‍♂️"
        4 -> "🔍"
        else -> "🛡️"
    }
    val activePet = activePetBinding
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
        "SOUL_SLEEP" -> "${activePet.customName ?: activePet.petName} (长眠中)"
        else -> activePet?.customName ?: activePet?.petName ?: "暂无字灵伙伴"
    }
    val intimacy = activePet?.intimacy ?: 0

    val level = playerProfile?.level ?: 1
    val exp = playerProfile?.exp ?: 0
    val maxExp = level * 100
    val coins = playerProfile?.coins ?: 0
    val streak = playerProfile?.streakDays ?: 1

    // Real stats calculation
    val todayDictationDone = (stats?.dailyPracticeCount ?: 0) > 0
    val totalTasks = allHolidayTasks.size
    val completedTasks = allHolidayTasks.count { it.status == "COMPLETED" }
    val remainingMonsters = wrongWords.size

    val inventoryItems by viewModel.inventoryItems.collectAsState()

    val equippedFrameItem = inventoryItems.find { it.itemType == "AVATAR_FRAME" && it.isEquipped }
    val equippedFrameDef = equippedFrameItem?.let { item -> com.example.data.ItemDefinition.ALL_ITEMS.find { it.itemId == item.itemId } }

    val equippedTitleItem = inventoryItems.find { it.itemType == "TITLE" && it.isEquipped }
    val equippedTitleDef = equippedTitleItem?.let { item -> com.example.data.ItemDefinition.ALL_ITEMS.find { it.itemId == item.itemId } }

    val equippedThemeItem = inventoryItems.find { it.itemType == "CAMP_THEME" && it.isEquipped }
    val equippedThemeDef = equippedThemeItem?.let { item -> com.example.data.ItemDefinition.ALL_ITEMS.find { it.itemId == item.itemId } }

    val backgroundGradient = when (equippedThemeDef?.itemId) {
        "theme_forest" -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1B5E20).copy(alpha = 0.25f), Color(0xFF121A13))
        )
        "theme_cyberpunk" -> Brush.verticalGradient(
            colors = listOf(Color(0xFF311B92).copy(alpha = 0.35f), Color(0xFF0F081D))
        )
        "theme_castle" -> Brush.verticalGradient(
            colors = listOf(Color(0xFF455A64).copy(alpha = 0.3f), Color(0xFF15191C))
        )
        else -> Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                MaterialTheme.colorScheme.surface
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎪 冒险营地", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "《字灵冒险团》",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = { showDevHUDPanel = true },
                        modifier = Modifier.testTag("admin_panel_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build, 
                            contentDescription = "管理员测试面板", 
                            tint = Color(0xFF00E5FF)
                        )
                    }
                    IconButton(
                        onClick = onNavigateToAccountCenter
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle, 
                            contentDescription = "档案与角色管理", 
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { 
                            pinInput = ""
                            pinError = false
                            showPinDialog = true 
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings, 
                            contentDescription = "守护者管理", 
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(backgroundGradient)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. RPG Character & Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Large Avatar Frame
                    val avatarBorderColor = when (equippedFrameDef?.itemId) {
                        "frame_combo" -> Color(0xFFFF9800) // Gold orange
                        "frame_perfect" -> Color(0xFFE91E63) // Rose gold
                        "frame_dragon" -> Color(0xFF9C27B0) // Purple dragon
                        else -> MaterialTheme.colorScheme.primary
                    }
                    val avatarBorderWidth = if (equippedFrameDef != null) 3.dp else 2.dp

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                            .border(avatarBorderWidth, avatarBorderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatarEmoji, fontSize = 36.sp)
                        if (equippedFrameDef != null) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(2.dp),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                Text(equippedFrameDef.iconEmoji, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right: Stats and Info
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                if (equippedTitleDef != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = when (equippedTitleDef.rarity) {
                                            "COMMON" -> Color.Gray.copy(alpha = 0.2f)
                                            "RARE" -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                            "EPIC" -> Color(0xFF9C27B0).copy(alpha = 0.2f)
                                            "LEGENDARY" -> Color(0xFFFFD700).copy(alpha = 0.25f)
                                            else -> Color.Gray
                                        },
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            when (equippedTitleDef.rarity) {
                                                "COMMON" -> Color.Gray
                                                "RARE" -> Color(0xFF2196F3)
                                                "EPIC" -> Color(0xFF9C27B0)
                                                "LEGENDARY" -> Color(0xFFFFD700)
                                                else -> Color.Gray
                                            }
                                        )
                                    ) {
                                        Text(
                                            text = equippedTitleDef.itemName,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (equippedTitleDef.rarity) {
                                                "COMMON" -> Color.LightGray
                                                "RARE" -> Color(0xFF90CAF9)
                                                "EPIC" -> Color(0xFFE040FB)
                                                "LEGENDARY" -> Color(0xFFFFD54F)
                                                else -> Color.White
                                            },
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Lv.$level",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Exp bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "EXP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(30.dp)
                            )
                            LinearProgressIndicator(
                                progress = { exp.toFloat() / maxExp },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$exp/$maxExp",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Stats Grid Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪙", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$coins 金币",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔥", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${streak}天连击",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 2. Pet Exhibit Card
            val activePet = activePetBinding
            val bubbleHint = if (activePet != null) {
                when (activePet.lifeStage) {
                    "EGG" -> {
                        if (activePet.hatchProgress >= 100) "“能量满了！可以孵化了！”" else "“我快要听见你的字灵之力了……”"
                    }
                    "SOUL_SLEEP" -> "“灵魂正在庭院中沉睡，等待复苏……”"
                    "INFANT" -> "“每天学习，我就能快快长大啦！”"
                    "AWAKENED" -> "“我会用觉醒的字灵之力守护你！”"
                    else -> when (activePet.petId) {
                        "小墨龙" -> "“今天我也要写出像龙一样矫健的字！”"
                        "小书灵" -> "“多学一个字，我的字灵之力就强一分哦！”"
                        "小云狐" -> "“拼写全对的话，我带你在云里飞翔过关！”"
                        "小竹猫" -> "“吃饱了拼音竹子，现在浑身都是劲！”"
                        else -> "“今天也一起去冒险吧！”"
                    }
                }
            } else {
                "“点击宠物小屋，契约你的专属字灵伙伴吧！”"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPetHouse() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(petEmoji, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = petName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (activePet?.lifeStage == "EGG") {
                                Text(
                                    text = "✨ 孵化能量: ${activePet.hatchProgress}/100",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                            } else if (activePet?.lifeStage != "SOUL_SLEEP" && activePet != null) {
                                Text(
                                    text = "💖 亲密度: $intimacy/100",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bubbleHint,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 3. Today's Adventure Checklist
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📜 今日冒险任务",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    dailyQuests.forEachIndexed { index, quest ->
                        val icon = when (quest.id) {
                            "dictation" -> "⚔️"
                            "holiday" -> "📋"
                            "error_purify" -> "👾"
                            "recitation" -> "🎙️"
                            else -> "🌟"
                        }
                        val badgeColor = when (quest.id) {
                            "dictation" -> MaterialTheme.colorScheme.primary
                            "holiday" -> MaterialTheme.colorScheme.tertiary
                            "error_purify" -> MaterialTheme.colorScheme.error
                            "recitation" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.primary
                        }

                        val targetClickAction = when (quest.id) {
                            "dictation" -> onNavigateToLevels
                            "holiday" -> onNavigateToHoliday
                            "error_purify" -> onNavigateToReview
                            "recitation" -> onNavigateToReport
                            else -> onNavigateToLevels
                        }

                        AdventureTaskRow(
                            icon = icon,
                            title = quest.title,
                            description = quest.description,
                            currentProgress = quest.currentProgress,
                            targetProgress = quest.targetProgress,
                            isClaimed = quest.isClaimed,
                            badgeColor = badgeColor,
                            onClaimClick = { viewModel.claimQuestReward(quest.id) },
                            onItemClick = targetClickAction
                        )

                        if (index < dailyQuests.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Hero Action Buttons Grid (Two rows of action entry points)
            Text(
                text = "🗺️ 冒险指令面板",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Row 1: High-impact main entry buttons
            Button(
                onClick = onNavigateToLevels,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("⚔️", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text("开始字词冒险", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("勇闯听写关卡，斩获丰厚经验与金币", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                }
            }

            Button(
                onClick = onNavigateToHoliday,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Text("📋", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text("暑期委托所", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("暑假专属学习任务，契约伙伴大升级", fontSize = 11.sp, color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f))
                }
            }

            Button(
                onClick = onNavigateToBrushLibrary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("🖌️", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text("书写神兵武器库", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("收集并配备独特的笔刷效果，提升书写乐趣", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clickable { onNavigateToReview() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("👾", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("错题魔物图鉴", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clickable { onNavigateToReport() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("📜", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("冒险日志", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clickable { onNavigateToMonsterCodex() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("👾", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("魔物净化图鉴", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clickable { onNavigateToAchievements() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🏆", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("成就荣誉圣殿", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clickable { onNavigateToShop() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🛒", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("冒险藏宝商店", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clickable { onNavigateToBackpack() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🎒", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("个人背包仓库", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // BrushLibraryDialog is now a full screen screen navigated via onNavigateToBrushLibrary()

        // Parent Pin dialog
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text("🛡️ 守护者管理校验") },
                text = {
                    Column {
                        Text("此区域属于家长监护范畴，请输入校验 PIN 码 (默认: 1234)：", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { 
                                pinInput = it 
                                pinError = false
                            },
                            singleLine = true,
                            placeholder = { Text("输入4位PIN码") },
                            isError = pinError,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (pinError) {
                            Text(
                                "PIN 码错误，请重新输入",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (pinInput == "1234") {
                            showPinDialog = false
                            onNavigateToParent()
                        } else {
                            pinError = true
                        }
                    }) {
                        Text("确认授权")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        if (showDevHUDPanel) {
            DeveloperHUDPanel(
                viewModel = viewModel,
                onDismiss = { showDevHUDPanel = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperHUDPanel(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val playerProfile by viewModel.playerProfile.collectAsState()
    val profile = playerProfile ?: return

    var levelInput by remember { mutableStateOf(profile.level.toString()) }
    var coinsInput by remember { mutableStateOf(profile.coins.toString()) }
    var expInput by remember { mutableStateOf(profile.exp.toString()) }
    var streakInput by remember { mutableStateOf(profile.streakDays.toString()) }
    var totalStudyInput by remember { mutableStateOf(profile.totalStudyDays.toString()) }

    var feedbackMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val lvl = levelInput.toIntOrNull() ?: profile.level
                    val coin = coinsInput.toIntOrNull() ?: profile.coins
                    val exp = expInput.toIntOrNull() ?: profile.exp
                    val streak = streakInput.toIntOrNull() ?: profile.streakDays
                    val total = totalStudyInput.toIntOrNull() ?: profile.totalStudyDays

                    viewModel.updatePlayerProfileDirectly(
                        level = lvl,
                        exp = exp,
                        coins = coin,
                        streakDays = streak,
                        totalStudyDays = total
                    )
                    feedbackMsg = "✨ 角色核心参数已保存并同步！"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black)
            ) {
                Text("保存核心参数", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭面板", color = Color.Gray)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🛠️", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("山神・核心资源调度面板", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF00E5FF))
                    Text("管理员开发测试模式 (V2.0 精简镜像)", fontSize = 10.sp, color = Color.Gray)
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                if (feedbackMsg != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20).copy(alpha = 0.2f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = feedbackMsg!!,
                            color = Color(0xFF81C784),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Text("🎯 核心数值微调：", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = levelInput,
                        onValueChange = { levelInput = it },
                        label = { Text("等级 (Lv)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = coinsInput,
                        onValueChange = { coinsInput = it },
                        label = { Text("金币 (Coins)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = expInput,
                        onValueChange = { expInput = it },
                        label = { Text("经验 (Exp)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = streakInput,
                        onValueChange = { streakInput = it },
                        label = { Text("连续学习") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                OutlinedTextField(
                    value = totalStudyInput,
                    onValueChange = { totalStudyInput = it },
                    label = { Text("总学习天数 (TotalStudy)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                Text("⚡ 快捷调试指令：", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.addCoinsDirectly(5000)
                            coinsInput = ( (coinsInput.toIntOrNull() ?: profile.coins) + 5000 ).toString()
                            feedbackMsg = "🪙 赠送 5000 金币指令发送成功！"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107), contentColor = Color.Black),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("🪙 +5k 金币", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.addLevelDirectly(10)
                            levelInput = ( (levelInput.toIntOrNull() ?: profile.level) + 10 ).toString()
                            feedbackMsg = "⚔️ 等级直升 10 级指令发送成功！"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("⚔️ +10 等级", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        viewModel.purifyAllMonsters()
                        feedbackMsg = "👾 已经一键解锁、完全净化所有字词魔物图鉴！"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("👾 一键净化与解锁全部魔物图鉴", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.unlockAllAchievements()
                        feedbackMsg = "🏆 已经一键解锁、完全达成所有冒险荣誉勋章！"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🏆 一键达标、激活全部荣誉勋章", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.addAllMaterialsAndCoinsDirectly()
                        coinsInput = "100000"
                        feedbackMsg = "🧪 一键集齐全部背包材料（各99个）且到账10万金币！"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🧪 一键集齐全部背包材料 + 10万金币", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.unlockAllShopItemsDirectly()
                        feedbackMsg = "🛍️ 已经一键在背包中解锁并拥有全部商店里的极品装备装扮！"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800), contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🛍️ 一键解锁并拥有商店全部装扮商品", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.resetAllDeveloperData()
                        levelInput = "1"
                        coinsInput = "100"
                        expInput = "0"
                        streakInput = "1"
                        totalStudyInput = "1"
                        feedbackMsg = "♻️ 角色、图鉴、成就数据全部完成重置归零！"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("♻️ 重置并归零所有开发测试进度", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .background(Color(0xFF0F111A), RoundedCornerShape(16.dp))
            .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(16.dp))
    )
}

@Composable
fun BrushStrokePreview(brush: BrushStyle, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val h = size.height
        val w = size.width
        val path = androidx.compose.ui.graphics.Path()
        path.moveTo(10f, h / 2f)
        path.cubicTo(w * 0.3f, h * 0.2f, w * 0.7f, h * 0.8f, w - 10f, h / 2f)
        
        val strokeColor = Color(brush.baseColor)
        
        if (brush.glowEnabled) {
            drawPath(
                path = path,
                color = strokeColor.copy(alpha = 0.35f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = brush.maxWidth * 2.2f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }
        
        drawPath(
            path = path,
            color = strokeColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = brush.maxWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
        
        if (brush.particleEnabled) {
            drawCircle(color = Color(0xFFFFD700), radius = 5f, center = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.3f))
            drawCircle(color = Color.White, radius = 4f, center = androidx.compose.ui.geometry.Offset(w * 0.55f, h * 0.58f))
            drawCircle(color = Color(0xFF00FFFF), radius = 4.5f, center = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.65f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrushLibraryDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val playerProfile by viewModel.playerProfile.collectAsState()
    val activePetBinding by viewModel.activePet.collectAsState()
    val wrongWords by viewModel.wrongWords.collectAsState()
    val coins = playerProfile?.coins ?: 0
    
    val currentEquipped = playerProfile?.equippedBrushId ?: "default_black"
    val unlockedBrushes = playerProfile?.unlockedBrushIds?.split(",") ?: listOf("default_black", "practice_wood")

    var tuningBrushId by remember { mutableStateOf<String?>(null) }

    if (tuningBrushId != null) {
        val brush = BrushStyle.getBrushById(tuningBrushId!!)
        BrushTuningScreen(
            viewModel = viewModel,
            brush = brush,
            isUnlocked = unlockedBrushes.contains(tuningBrushId!!),
            onClose = { tuningBrushId = null }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🖌️ 书写神兵武器库",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "收集稀有笔刷皮肤，开启独特笔锋特效！",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("当前装备: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = BrushStyle.getBrushById(currentEquipped).brushName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("🪙 $coins 金币", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(1),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(BrushStyle.ALL_BRUSHES.size) { index ->
                        val brush = BrushStyle.ALL_BRUSHES[index]
                        val isUnlocked = unlockedBrushes.contains(brush.brushId)
                        val isEquipped = currentEquipped == brush.brushId
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isEquipped) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isEquipped) 2.dp else 1.dp,
                                color = if (isEquipped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = brush.brushName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val rarityColor = when (brush.rarity) {
                                            "NORMAL" -> Color.Gray
                                            "RARE" -> Color(0xFF1E88E5)
                                            "EPIC" -> Color(0xFF8E24AA)
                                            "LEGEND" -> Color(0xFFF57C00)
                                            else -> Color.Gray
                                        }
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(brush.rarity, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = rarityColor.copy(alpha = 0.1f),
                                                labelColor = rarityColor
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, rarityColor.copy(alpha = 0.4f)),
                                            modifier = Modifier.height(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "解锁方式: ${brush.unlockCondition}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("笔迹特效: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        BrushStrokePreview(
                                            brush = brush,
                                            modifier = Modifier
                                                .width(130.dp)
                                                .height(30.dp)
                                                .background(Color(0xFFFDF6E3), RoundedCornerShape(4.dp))
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Button(
                                        onClick = { tuningBrushId = brush.brushId },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        Text(if (isUnlocked) "试写调校" else "预览试写")
                                    }

                                    if (isEquipped) {
                                        Button(
                                            onClick = {},
                                            enabled = false,
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("已装备")
                                        }
                                    } else if (isUnlocked) {
                                        Button(
                                            onClick = { viewModel.equipBrush(brush.brushId) }
                                        ) {
                                            Text("配备")
                                        }
                                    } else {
                                        var showPurchaseButton = false
                                        var purchaseCost = 0
                                        if (brush.brushId == "rainbow_brush") {
                                            showPurchaseButton = true
                                            purchaseCost = 200
                                        }
                                        
                                        if (showPurchaseButton) {
                                            Button(
                                                onClick = {
                                                    if (coins >= purchaseCost) {
                                                        viewModel.addPlayerCoins(-purchaseCost)
                                                        viewModel.unlockBrushDirect(brush.brushId)
                                                        viewModel.equipBrush(brush.brushId)
                                                    }
                                                },
                                                enabled = coins >= purchaseCost,
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                                            ) {
                                                Text("🪙 $purchaseCost 解锁")
                                            }
                                        } else {
                                            Button(
                                                onClick = {},
                                                enabled = false,
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                            ) {
                                                Icon(Icons.Default.Lock, contentDescription = "未解锁", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("未解锁")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdventureTaskRow(
    icon: String,
    title: String,
    description: String,
    currentProgress: Int,
    targetProgress: Int,
    isClaimed: Boolean,
    badgeColor: Color,
    onClaimClick: () -> Unit,
    onItemClick: () -> Unit
) {
    val isReadyToClaim = currentProgress >= targetProgress && !isClaimed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isReadyToClaim) {
                    onClaimClick()
                } else {
                    onItemClick()
                }
            }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(badgeColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))

        if (isClaimed) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "已领取 ✅",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        } else if (isReadyToClaim) {
            Button(
                onClick = onClaimClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF57C00), // Vibrant Orange
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("claim_btn_${title}")
            ) {
                Text(
                    text = "领取 🎁",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        } else {
            Surface(
                color = badgeColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "$currentProgress / $targetProgress",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
