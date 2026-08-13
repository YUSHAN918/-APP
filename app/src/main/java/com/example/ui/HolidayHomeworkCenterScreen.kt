package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HolidayTask
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayHomeworkCenterScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNavigateToDictation: ((Int) -> Unit)? = null,
    onNavigateToBattle: () -> Unit = {},
    onNavigateToQuests: () -> Unit = {}
) {
    val tasks by viewModel.allHolidayTasks.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val allMaterials by viewModel.allHolidayMaterials.collectAsState()
    val allMaterialProgress by viewModel.allHolidayMaterialProgress.collectAsState()

    var selectedSubject by rememberSaveable { mutableStateOf("ALL") }
    var detailTaskId by rememberSaveable { mutableStateOf<Long?>(null) }

    val mathTier = userStats?.mathGradeTier ?: "ABOVE_90"
    val englishTier = userStats?.englishGradeTier ?: "ABOVE_88"

    // Filter tasks based on score tier rules
    val activeTasks = remember(tasks, mathTier, englishTier) {
        tasks.filter { task ->
            when (task.tierRule) {
                "MATH_90_ABOVE" -> mathTier == "ABOVE_90"
                "MATH_90_BELOW" -> mathTier == "BELOW_90"
                "ENG_88_ABOVE" -> englishTier == "ABOVE_88"
                "ENG_88_BELOW" -> englishTier == "BELOW_88"
                else -> true
            }
        }
    }

    val filteredTasks = remember(activeTasks, selectedSubject) {
        if (selectedSubject == "ALL") {
            activeTasks
        } else if (selectedSubject == "PRACTICE_LIFE") {
            activeTasks.filter { it.subject in listOf("PRACTICE", "LIFE", "MOVIE") }
        } else {
            activeTasks.filter { it.subject == selectedSubject }
        }
    }

    val totalCount = activeTasks.size
    val completedCount = activeTasks.count { it.status == "COMPLETED" || it.completedCount >= it.totalCount }
    val overallProgress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "暑假远征试炼中心",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text(
                            text = "五年级升六年级 · 私有契约任务包",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0F16)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0F0F16))
        ) {
            // Top Overview Card (远征圣印)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.BeachAccess,
                                    contentDescription = null,
                                    tint = Color(0xFFE2E8F0),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "2026 暑期远征净化进度",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF059669),
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "${(overallProgress * 100).toInt()}%",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = Color(0xFF10B981),
                            trackColor = Color(0xFF334155)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "已解密卷轴: $completedCount / $totalCount 项",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = if (overallProgress >= 1f) "远征宣告大胜利！🎉" else "不积跬步无以至千里，加油！",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tier Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val mathLabel = if (mathTier == "ABOVE_90") "数学: 90分+ (奥数法典)" else "数学: 90分- (精益求精)"
                            val engLabel = if (englishTier == "ABOVE_88") "英语: 88分+ (高级密语)" else "英语: 88分- (咒语重温)"

                            Surface(
                                color = Color(0xFF334155),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(mathLabel, color = Color(0xFFE2E8F0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                color = Color(0xFF334155),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Translate, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(engLabel, color = Color(0xFFE2E8F0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 🚀 Navigation to Complete Daily Quests
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onNavigateToQuests() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "每日委托",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "前往行者每日委托",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "查看每日历练、日常修行及委托奖励明细",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "前往",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Subject Filter Tabs
            val subjects = listOf(
                "ALL" to "全部任务",
                "CHINESE" to "📚 语文赤炼",
                "MATH" to "📐 奥数密境",
                "ENGLISH" to "🔤 英文咒语",
                "PRACTICE_LIFE" to "⛺ 实践与生存"
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subjects) { (key, label) ->
                    val isSelected = selectedSubject == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubject = key },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF1E1E2F),
                            labelColor = Color.Gray,
                            selectedContainerColor = Color(0xFF2563EB),
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.White.copy(alpha = 0.1f),
                            selectedBorderColor = Color(0xFF3B82F6)
                        ),
                        modifier = Modifier.testTag("filter_chip_$key")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("当前试炼密境下暂无任务", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        HolidayTaskCard(
                            task = task,
                            materials = allMaterials,
                            materialProgressList = allMaterialProgress,
                            onCheckIn = { delta -> viewModel.checkInHolidayTask(task.id, delta) },
                            onCancelCheckIn = { viewModel.cancelTodayHolidayCheckIn(task.id) },
                            onUpdateTask = { updated -> viewModel.updateHolidayTask(updated) },
                            onOpenDetail = { detailTaskId = task.id }
                        )
                    }
                }
            }
        }
    }

    // Task detail overlay
    detailTaskId?.let { id ->
        BackHandler {
            detailTaskId = null
        }
        Surface(modifier = Modifier.fillMaxSize()) {
            HolidayTaskDetailScreen(
                onNavigateToBattle = onNavigateToBattle,
                taskId = id,
                viewModel = viewModel,
                onBack = { detailTaskId = null },
                onNavigateToDictation = { levelId ->
                    onNavigateToDictation?.invoke(levelId)
                }
            )
        }
    }
}

@Composable
fun HolidayTaskCard(
    task: HolidayTask,
    materials: List<com.example.data.HolidayStudyMaterial> = emptyList(),
    materialProgressList: List<com.example.data.HolidayMaterialProgress> = emptyList(),
    onCheckIn: (Int) -> Unit,
    onCancelCheckIn: () -> Unit,
    onUpdateTask: (HolidayTask) -> Unit,
    onOpenDetail: () -> Unit
) {
    val isDone = task.completedCount >= task.totalCount || task.status == "COMPLETED"
    
    // Subject themed configuration
    val (themeColor, emoji, categoryLabel) = when (task.subject) {
        "CHINESE" -> Triple(Color(0xFFEF4444), "🔥", "语文赤炼")
        "MATH" -> Triple(Color(0xFF3B82F6), "⚡", "奥数密境")
        "ENGLISH" -> Triple(Color(0xFF10B981), "🟢", "英文魔典")
        "PRACTICE" -> Triple(Color(0xFFF59E0B), "⛺", "荒野求生")
        "LIFE" -> Triple(Color(0xFF8B5CF6), "🌾", "生活试炼")
        else -> Triple(Color(0xFF64748B), "⚔️", "日常冒险")
    }

    val cardBg = if (isDone) Color(0xFF161622).copy(alpha = 0.5f) else Color(0xFF14141E)
    val glow = if (isDone) 0f else QuestAnimation.rememberGlowIntensity()

    Surface(
        color = cardBg,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp, 
            if (isDone) themeColor.copy(alpha = 0.3f) else themeColor.copy(alpha = 0.5f + 0.3f * glow)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("holiday_task_card_${task.id}")
    ) {
        Column(
            modifier = Modifier
                .clickable { onOpenDetail() }
                .padding(16.dp)
        ) {
            // Header Row: Category Badge, Title, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = emoji, fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = themeColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = categoryLabel,
                            color = themeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = task.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status chip
                Surface(
                    shape = CircleShape,
                    color = if (isDone) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF334155),
                    contentColor = if (isDone) Color(0xFF34D399) else Color(0xFFCBD5E1)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        if (isDone) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (isDone) "契约达成" else if (task.completedCount > 0) "解密中" else "未落款",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description and Requirements
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0)
            )

            if (task.requirement.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(top = 2.dp),
                        tint = themeColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "秘境法则: ${task.requirement}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Linked study materials
            if (!task.linkedMaterialIdsStr.isNullOrBlank()) {
                val matIds = task.linkedMaterialIdsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val linkedMats = materials.filter { it.materialId in matIds }
                if (linkedMats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val recitedCount = linkedMats.count { m ->
                        materialProgressList.find { it.materialId == m.materialId }?.reciteStatus in listOf("RECITED", "FAMILIAR")
                    }
                    val dictatedCount = linkedMats.count { m ->
                        materialProgressList.find { it.materialId == m.materialId }?.dictationStatus == "PASSED"
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = themeColor.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LibraryBooks, contentDescription = null, modifier = Modifier.size(14.dp), tint = themeColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "绑定秘卷 ${linkedMats.size} 篇  |  已悟熟 $recitedCount  |  已默写过 $dictatedCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            val runnerButtonText = when (task.taskType) {
                "READING" -> "📚 研读秘卷"
                "WRITING_PRACTICE" -> "✍️ 剑笔磨砺"
                "RECITATION_MEMORIZE" -> "🗣️ 默念引吭"
                "COMPOSITION" -> "📝 撰写密卷"
                "MATH_PRACTICE" -> "📐 破解奥数"
                "ENGLISH_PRACTICE" -> "🔤 咒语复诵"
                "LIFE_PRACTICE" -> "⚽ 绝境求生"
                else -> "⚡ 契约突入"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenDetail,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(runnerButtonText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (task.completedCount > 0) {
                    OutlinedButton(
                        onClick = onCancelCheckIn,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("撤销落印", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { onCheckIn(1) },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF334155),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("契约落印", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Parent confirmation indicator on task card
            if (task.isParentConfirmed) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "大教官（家长）已印章确认",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

