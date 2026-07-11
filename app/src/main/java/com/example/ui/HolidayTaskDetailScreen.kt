package com.example.ui

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HolidayTask
import com.example.data.HolidayWorkSession
import com.example.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayTaskDetailScreen(
    onNavigateToBattle: () -> Unit = {},
    taskId: Long,
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNavigateToDictation: ((Int) -> Unit)? = null
) {
    val tasks by viewModel.allHolidayTasks.collectAsState()
    val workSessions by viewModel.allHolidayWorkSessions.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val allMaterials by viewModel.allHolidayMaterials.collectAsState()
    val allMaterialProgress by viewModel.allHolidayMaterialProgress.collectAsState()

    val task = tasks.find { it.id == taskId }

    var selectedMaterialForStudy by remember { mutableStateOf<com.example.data.HolidayStudyMaterial?>(null) }
    var lastSelectedMaterialId by remember { mutableStateOf<String?>(null) }
    var showCancelCheckInDialog by remember { mutableStateOf(false) }
    var showModifyProgressDialog by remember { mutableStateOf(false) }
    var progressInputText by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val taskSessions = remember(workSessions, taskId) { workSessions.filter { it.taskId == taskId } }
    val todaySessions = remember(taskSessions, todayStr) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        taskSessions.filter { sdf.format(Date(it.createdAt)) == todayStr }
    }

    val mainScrollState = rememberScrollState()

    if (task == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("任务详情") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("未找到对应的作业任务", style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }

    if (selectedMaterialForStudy != null) {
        val mat = selectedMaterialForStudy!!
        val prog = allMaterialProgress.find { it.materialId == mat.materialId }
        MaterialStudyScreen(
            onNavigateToBattle = onNavigateToBattle,
            material = mat,
            progress = prog,
            taskId = task.id,
            viewModel = viewModel,
            onBack = { selectedMaterialForStudy = null }
        )
        return
    }

    val mathTier = userStats?.mathGradeTier ?: "ABOVE_90"
    val englishTier = userStats?.englishGradeTier ?: "ABOVE_88"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
                            maxLines = 1
                        )
                        Text(
                            text = "${task.subject} · ${task.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("task_detail_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                },
                actions = {
                    if (task.isParentConfirmed) {
                        AssistChip(
                            onClick = { viewModel.toggleTaskParentConfirmed(task) },
                            label = { Text("教官已签章", fontSize = 11.sp, color = Color(0xFF10B981)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.toggleTaskParentConfirmed(task) },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Icon(Icons.Default.BorderColor, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("大教官签章", fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0F16)
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xFF14141E),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showModifyProgressDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF60A5FA)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF60A5FA).copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("修正刻度", fontSize = 13.sp)
                    }

                    if (todaySessions.isNotEmpty() || task.completedCount > 0) {
                        OutlinedButton(
                            onClick = { showCancelCheckInDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("撤销落印", fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.checkInHolidayTask(task.id, 1, "远征试炼快速落印")
                            snackbarMessage = "已记录今日试炼落印！"
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6), contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("完成今日落印", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF070913))
                .verticalScroll(mainScrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Snackbar notice
            snackbarMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1225)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(msg, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        IconButton(onClick = { snackbarMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "关闭", modifier = Modifier.size(16.dp), tint = Color.Gray)
                        }
                    }
                }
            }

            // Task Header Overview Card
            val (themeColor, emoji, categoryLabel) = when (task.subject) {
                "CHINESE" -> Triple(Color(0xFFEF5350), "🔥", "语文赤炼")
                "MATH" -> Triple(Color(0xFF00E5FF), "⚡", "奥数密境")
                "ENGLISH" -> Triple(Color(0xFF00E676), "🟢", "英文魔典")
                "PRACTICE" -> Triple(Color(0xFFFFAB40), "⛺", "荒野求生")
                "LIFE" -> Triple(Color(0xFFAB47BC), "🌾", "生活试炼")
                else -> Triple(Color(0xFF90A4AE), "⚔️", "日常冒险")
            }
            val isDone = task.completedCount >= task.totalCount || task.status == "COMPLETED"

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1225)),
                border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = if (isDone) 0.2f else 0.7f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(text = emoji, fontSize = 26.sp, modifier = Modifier.padding(end = 10.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    lineHeight = 22.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isDone) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFF141726),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDone) Color(0xFF00E676) else themeColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (isDone) "契约达成" else "${task.completedCount}/${task.totalCount}${task.unitLabel ?: ""}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDone) Color(0xFF00E676) else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (task.requirement.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "💡 秘境法则: ${task.requirement}",
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Precise HUD Linear Progress Bar
                    Spacer(modifier = Modifier.height(16.dp))
                    val progressPercentage = if (task.totalCount > 0) task.completedCount.toFloat() / task.totalCount.toFloat() else 0f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "任务契约执行进度: ${(progressPercentage * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "${task.completedCount} / ${task.totalCount} ${task.unitLabel ?: ""}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = themeColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressPercentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = themeColor,
                        trackColor = Color(0xFF141726)
                    )

                    // Score Tier hint if applicable
                    when (task.tierRule) {
                        "MATH_90_ABOVE", "MATH_90_BELOW" -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            val isHigher = mathTier == "ABOVE_90"
                            Text(
                                text = if (isHigher) "📌 奥数分流: 90分及以上组 (奥数法典)" else "📌 奥数分流: 90分以下组 (精益求精)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        "ENG_88_ABOVE", "ENG_88_BELOW" -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            val isHigher = englishTier == "ABOVE_88"
                            Text(
                                text = if (isHigher) "📌 魔典分流: 88分及以上组 (高级密语)" else "📌 魔典分流: 88分以下组 (咒语重温)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (!task.linkedMaterialIdsStr.isNullOrBlank()) {
                val materialIds = task.linkedMaterialIdsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val linkedMaterials = allMaterials.filter { it.materialId in materialIds }
                
                if (linkedMaterials.isNotEmpty()) {
                    Text(
                        text = "📚 真实任务卷轴库（共 ${linkedMaterials.size} 项）",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold, 
                            color = Color(0xFF00E5FF),
                            letterSpacing = 0.5.sp
                        )
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (mat in linkedMaterials) {
                            val prog = allMaterialProgress.find { it.materialId == mat.materialId }
                            val isHighlighted = mat.materialId == lastSelectedMaterialId
                            MaterialItemCard(
                                material = mat,
                                progress = prog,
                                isHighlighted = isHighlighted,
                                onClick = { 
                                    selectedMaterialForStudy = mat
                                    lastSelectedMaterialId = mat.materialId 
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Executable Task Runner Box based on Task Type
            Text(
                text = "⚡ 契约执行终端",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold, 
                    color = Color(0xFFFFAB40),
                    letterSpacing = 0.5.sp
                )
            )

            when (task.taskType) {
                "READING" -> ReadingTaskRunner(task = task, viewModel = viewModel, onShowMessage = { snackbarMessage = it })
                "WRITING_PRACTICE" -> WritingPracticeRunner(task = task, viewModel = viewModel, onNavigateToDictation = onNavigateToDictation, onShowMessage = { snackbarMessage = it })
                "RECITATION_MEMORIZE" -> RecitationRunner(task = task, viewModel = viewModel, onNavigateToDictation = onNavigateToDictation, onShowMessage = { snackbarMessage = it })
                "COMPOSITION" -> CompositionRunner(task = task, viewModel = viewModel, onShowMessage = { snackbarMessage = it })
                "MATH_PRACTICE" -> MathTaskRunner(task = task, viewModel = viewModel, onShowMessage = { snackbarMessage = it })
                "ENGLISH_PRACTICE" -> EnglishTaskRunner(task = task, viewModel = viewModel, onShowMessage = { snackbarMessage = it })
                "LIFE_PRACTICE" -> LifeTaskRunner(task = task, viewModel = viewModel, onShowMessage = { snackbarMessage = it })
                "COUNT_PROGRESS" -> CountProgressTaskRunner(task = task, viewModel = viewModel, onShowMessage = { snackbarMessage = it })
                else -> GenericTaskRunner(task = task, viewModel = viewModel, onShowMessage = { snackbarMessage = it })
            }

            // Session History Log for this task
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📋 本契约执行记录 (${taskSessions.size}条)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold, 
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            )

            if (taskSessions.isEmpty()) {
                Text(
                    text = "尚未在时光卷轴上留下痕迹，请在上方执行终端刻印你的试炼。",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp)
                )
            } else {
                taskSessions.sortedByDescending { it.createdAt }.take(5).forEach { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1225)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                // Tech line accent on the left
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF00E5FF))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    val dateStr = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(session.createdAt))
                                    Text(
                                        text = "$dateStr ${(session.titleInput ?: "").ifBlank { "执行契约" }}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFE2E8F0))
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        if (session.durationMinutes > 0) {
                                            Text("⏱️ 时光: ${session.durationMinutes} 分钟", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                        }
                                        if (!session.note.isNullOrBlank()) {
                                            Text("📝 秘文: ${session.note}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                        }
                                    }
                                }
                            }
                            if (session.parentConfirmed) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF00E676).copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF00E676).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "教官落印", 
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), 
                                        color = Color(0xFF00E676),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCancelCheckInDialog) {
        AlertDialog(containerColor = Color(0xFF1E293B), titleContentColor = Color.White, textContentColor = Color(0xFFE2E8F0),
            onDismissRequest = { showCancelCheckInDialog = false },
            title = { Text("确认撤销今日打卡？") },
            text = { Text("撤销后，今天增加的进度将被减去，今日打卡状态将还原。是否继续？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelTodayHolidayCheckIn(task.id)
                        showCancelCheckInDialog = false
                        snackbarMessage = "已成功撤销今日打卡！"
                    }
                ) {
                    Text("确认撤销", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelCheckInDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showModifyProgressDialog) {
        AlertDialog(containerColor = Color(0xFF1E293B), titleContentColor = Color.White, textContentColor = Color(0xFFE2E8F0),
            onDismissRequest = { showModifyProgressDialog = false },
            title = { Text("修改完成进度") },
            text = {
                Column {
                    Text("当前进度: ${task.completedCount} / ${task.totalCount} ${task.unitLabel ?: ""}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                        value = progressInputText,
                        onValueChange = { progressInputText = it },
                        label = { Text("请输入新的已完成数值 (0 - ${task.totalCount})") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newCount = progressInputText.toIntOrNull()
                        if (newCount != null && newCount in 0..task.totalCount) {
                            viewModel.updateTaskProgressDirect(task.id, newCount)
                            showModifyProgressDialog = false
                            snackbarMessage = "已修正任务进度为 $newCount !"
                        } else if (newCount != null && newCount > task.totalCount) {
                            viewModel.updateTaskProgressDirect(task.id, task.totalCount)
                            showModifyProgressDialog = false
                            snackbarMessage = "已修正任务进度为 ${task.totalCount} !"
                        }
                    }
                ) {
                    Text("保存修正")
                }
            },
            dismissButton = {
                TextButton(onClick = { showModifyProgressDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

// ----------------------------------------------------
// Executable Task Runners
// ----------------------------------------------------

@Composable
fun ReadingTaskRunner(
    task: HolidayTask,
    viewModel: GameViewModel,
    onShowMessage: (String) -> Unit
) {
    var bookTitle by remember { mutableStateOf("") }
    var pageRange by remember { mutableStateOf("") }
    var readNotes by remember { mutableStateOf("") }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            kotlinx.coroutines.delay(1000L)
            elapsedSeconds += 1
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📚 暑假阅读计时与打卡", style = MaterialTheme.typography.titleMedium.copy(color = Color.White), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Timer display
            val mins = elapsedSeconds / 60
            val secs = elapsedSeconds % 60
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("⏱️ 本次阅读用时: %02d:%02d", mins, secs),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF38BDF8)
                )

                Row {
                    if (isTimerRunning) {
                        OutlinedButton(onClick = { isTimerRunning = false }) {
                            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("暂停")
                        }
                    } else {
                        Button(onClick = { isTimerRunning = true }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (elapsedSeconds == 0) "开始阅读" else "继续阅读")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = bookTitle,
                onValueChange = { bookTitle = it },
                label = { Text("书名 (如《童年》《鲁滨逊漂流记》)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = pageRange,
                onValueChange = { pageRange = it },
                label = { Text("阅读页码 (如 第15-32页)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = readNotes,
                onValueChange = { readNotes = it },
                label = { Text("阅读心得 / 笔记摘抄 (选填)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val finalMinutes = (elapsedSeconds / 60).coerceAtLeast(1)
                    val session = HolidayWorkSession(
                        taskId = task.id,
                        packId = task.packId,
                        subject = task.subject,
                        taskType = task.taskType,
                        startedAt = System.currentTimeMillis() - (elapsedSeconds * 1000L),
                        endedAt = System.currentTimeMillis(),
                        durationMinutes = finalMinutes,
                        progressDelta = 1,
                        note = "阅读《$bookTitle》 $pageRange | 心得: $readNotes",
                        titleInput = bookTitle.ifBlank { "阅读打卡" }
                    )
                    viewModel.recordWorkSession(session, deltaProgress = 1)
                    isTimerRunning = false
                    onShowMessage("已保存阅读记录 ($finalMinutes 分钟) 并完成今日打卡！")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("提交并完成本次阅读打卡")
            }
        }
    }
}

@Composable
fun WritingPracticeRunner(
    task: HolidayTask,
    viewModel: GameViewModel,
    onNavigateToDictation: ((Int) -> Unit)?,
    onShowMessage: (String) -> Unit
) {
    var practiceNotes by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1225)), 
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFAB40).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "⚡ 五升六字词练写与听写", 
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFFFFAB40),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "在纸质练字本或下方的字词关卡中练习，写好字词，打牢基本功。", 
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (task.linkedLevelId != null && onNavigateToDictation != null) {
                Button(
                    onClick = { onNavigateToDictation(task.linkedLevelId.toInt()) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF141726),
                        contentColor = Color(0xFF00E5FF)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Spellcheck, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF00E5FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("直接进入配套字词听写关卡", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, 
                    unfocusedTextColor = Color.White, 
                    focusedContainerColor = Color(0xFF070913), 
                    unfocusedContainerColor = Color(0xFF070913), 
                    focusedBorderColor = Color(0xFF00E5FF), 
                    unfocusedBorderColor = Color(0xFF00E5FF).copy(alpha = 0.15f), 
                    focusedLabelColor = Color(0xFF00E5FF), 
                    unfocusedLabelColor = Color(0xFF94A3B8)
                ),
                value = practiceNotes,
                onValueChange = { practiceNotes = it },
                label = { Text("练写心得 / 易错字备注 (选填)") },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val session = HolidayWorkSession(
                        taskId = task.id,
                        packId = task.packId,
                        subject = task.subject,
                        taskType = task.taskType,
                        startedAt = System.currentTimeMillis() - 600000L,
                        endedAt = System.currentTimeMillis(),
                        durationMinutes = 10,
                        progressDelta = 1,
                        note = practiceNotes.ifBlank { "完成日常字词练写" },
                        titleInput = "练写打卡"
                    )
                    viewModel.recordWorkSession(session, deltaProgress = 1)
                    onShowMessage("已标记练写完成！")
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF141726),
                    contentColor = Color(0xFF00E676)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF00E676))
                Spacer(modifier = Modifier.width(8.dp))
                Text("完成打卡", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun RecitationRunner(
    task: HolidayTask,
    viewModel: GameViewModel,
    onNavigateToDictation: ((Int) -> Unit)?,
    onShowMessage: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🗣️ 古诗文与课文背诵默写", style = MaterialTheme.typography.titleMedium.copy(color = Color.White), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = task.isRecited,
                    onClick = {
                        val newRecited = !task.isRecited
                        viewModel.updateHolidayTask(task.copy(isRecited = newRecited))
                        onShowMessage(if (newRecited) "已标记为【熟练背诵】" else "已取消【熟练背诵】标记")
                    },
                    label = { Text("口头熟练背诵") },
                    leadingIcon = {
                        Icon(
                            if (task.isRecited) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = task.isMemorized,
                    onClick = {
                        val newMemo = !task.isMemorized
                        val newCount = if (newMemo) 1 else 0
                        viewModel.updateHolidayTask(task.copy(isMemorized = newMemo, completedCount = newCount))
                        onShowMessage(if (newMemo) "已标记为【默写过关】" else "已取消【默写过关】")
                    },
                    label = { Text("默写精准过关") },
                    leadingIcon = {
                        Icon(
                            if (task.isMemorized) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (task.linkedLevelId != null && onNavigateToDictation != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onNavigateToDictation(task.linkedLevelId.toInt()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Extension, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("前往配套古诗默写听写关卡")
                }
            }
        }
    }
}

@Composable
fun CompositionRunner(
    task: HolidayTask,
    viewModel: GameViewModel,
    onShowMessage: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var wordCountStr by remember { mutableStateOf("450") }
    var notes by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📝 暑假大作文阶段跟进", style = MaterialTheme.typography.titleMedium.copy(color = Color.White), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Stage Checkboxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = task.isDraftDone,
                        onCheckedChange = { checked ->
                            viewModel.updateHolidayTask(task.copy(isDraftDone = checked))
                            onShowMessage(if (checked) "已完成草稿阶段！" else "取消草稿标记")
                        }
                    )
                    Text("1. 草稿初成")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = task.isFinalWritten,
                        onCheckedChange = { checked ->
                            val newCompleted = if (checked) task.totalCount else 0
                            viewModel.updateHolidayTask(task.copy(isFinalWritten = checked, isDraftDone = if (checked) true else task.isDraftDone, completedCount = newCompleted))
                            onShowMessage(if (checked) "🎉 誊写完成！大作文搞定" else "取消誊写标记")
                        }
                    )
                    Text("2. 正式誊写完成")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = title,
                onValueChange = { title = it },
                label = { Text("作文题目 (如《难忘的暑假》《未来的学校》)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = wordCountStr,
                onValueChange = { wordCountStr = it },
                label = { Text("预估/实际字数") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = notes,
                onValueChange = { notes = it },
                label = { Text("习作大纲 / 得意之处 / 遇到的困难") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val session = HolidayWorkSession(
                        taskId = task.id,
                        packId = task.packId,
                        subject = task.subject,
                        taskType = task.taskType,
                        startedAt = System.currentTimeMillis() - 1800000L,
                        endedAt = System.currentTimeMillis(),
                        durationMinutes = 30,
                        progressDelta = if (task.isFinalWritten) 1 else 0,
                        note = "作文《$title》 ($wordCountStr 字) | 备注: $notes",
                        titleInput = title.ifBlank { "作文阶段打卡" }
                    )
                    viewModel.recordWorkSession(session, deltaProgress = if (task.isFinalWritten) 1 else 0)
                    onShowMessage("已保存作文进展记录！")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("记录习作进展")
            }
        }
    }
}

@Composable
fun MathTaskRunner(
    task: HolidayTask,
    viewModel: GameViewModel,
    onShowMessage: (String) -> Unit
) {
    var pageInfo by remember { mutableStateOf("") }
    var wrongQuestions by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64748B).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📐 数学练习册与错题订正", style = MaterialTheme.typography.titleMedium.copy(color = Color.White), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = pageInfo,
                onValueChange = { pageInfo = it },
                label = { Text("完成页码/栏数 (如 第12-14页)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = wrongQuestions,
                onValueChange = { wrongQuestions = it },
                label = { Text("错题记录与订正情况 (如 2题错已重做)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.checkInHolidayTask(task.id, 1, "数学做题: $pageInfo | 错题: $wrongQuestions")
                        onShowMessage("已增加 1 页数学任务进度！")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+1 页/栏进度")
                }

                Button(
                    onClick = {
                        val session = HolidayWorkSession(
                            taskId = task.id,
                            packId = task.packId,
                            subject = task.subject,
                            taskType = task.taskType,
                            startedAt = System.currentTimeMillis() - 1200000L,
                            endedAt = System.currentTimeMillis(),
                            durationMinutes = 20,
                            progressDelta = 1,
                            note = "数学练习 $pageInfo | 错题订正: $wrongQuestions",
                            titleInput = "数学打卡"
                        )
                        viewModel.recordWorkSession(session, deltaProgress = 1)
                        onShowMessage("已保存数学练习与订正记录！")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("提交全套打卡")
                }
            }
        }
    }
}

@Composable
fun EnglishTaskRunner(
    task: HolidayTask,
    viewModel: GameViewModel,
    onShowMessage: (String) -> Unit
) {
    var listeningTimeMins by remember { mutableStateOf("20") }
    var noteInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🔤 英语听力 / 抄写 / 阅读", style = MaterialTheme.typography.titleMedium.copy(color = Color.White), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = listeningTimeMins,
                onValueChange = { listeningTimeMins = it },
                label = { Text("听力/朗读时长 (分钟)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = noteInput,
                onValueChange = { noteInput = it },
                label = { Text("抄写单元 / 阅读文章名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val mins = listeningTimeMins.toIntOrNull() ?: 15
                    val session = HolidayWorkSession(
                        taskId = task.id,
                        packId = task.packId,
                        subject = task.subject,
                        taskType = task.taskType,
                        startedAt = System.currentTimeMillis() - (mins * 60000L),
                        endedAt = System.currentTimeMillis(),
                        durationMinutes = mins,
                        progressDelta = 1,
                        note = "英语练习: $noteInput (时长 $mins 分钟)",
                        titleInput = "英语打卡"
                    )
                    viewModel.recordWorkSession(session, deltaProgress = 1)
                    onShowMessage("英语作业打卡完成！")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("提交打卡")
            }
        }
    }
}

@Composable
fun LifeTaskRunner(
    task: HolidayTask,
    viewModel: GameViewModel,
    onShowMessage: (String) -> Unit
) {
    var activityTitle by remember { mutableStateOf("") }
    var durationMins by remember { mutableStateOf("30") }
    var details by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("⚽ 实践、家务、运动与观影", style = MaterialTheme.typography.titleMedium.copy(color = Color.White), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = activityTitle,
                onValueChange = { activityTitle = it },
                label = { Text("活动/项目名称 (如 跳绳500下 / 整理书架 / 看《流浪地球》)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = durationMins,
                onValueChange = { durationMins = it },
                label = { Text("用时/时长 (分钟)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = details,
                onValueChange = { details = it },
                label = { Text("活动感想 / 运动成效 / 观后感") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val mins = durationMins.toIntOrNull() ?: 30
                    val session = HolidayWorkSession(
                        taskId = task.id,
                        packId = task.packId,
                        subject = task.subject,
                        taskType = task.taskType,
                        startedAt = System.currentTimeMillis() - (mins * 60000L),
                        endedAt = System.currentTimeMillis(),
                        durationMinutes = mins,
                        progressDelta = 1,
                        note = "$activityTitle | 感想/记录: $details",
                        titleInput = activityTitle.ifBlank { "生活实践打卡" }
                    )
                    viewModel.recordWorkSession(session, deltaProgress = 1)
                    onShowMessage("已记录实践/生活打卡！")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("提交打卡")
            }
        }
    }
}

@Composable
fun GenericTaskRunner(
    task: HolidayTask,
    viewModel: GameViewModel,
    onShowMessage: (String) -> Unit
) {
    var note by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("✅ 任务完成确认", style = MaterialTheme.typography.titleMedium.copy(color = Color.White), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E293B), unfocusedContainerColor = Color(0xFF1E293B), focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFF94A3B8), unfocusedLabelColor = Color(0xFF64748B)),
                value = note,
                onValueChange = { note = it },
                label = { Text("打卡备注 (选填)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.checkInHolidayTask(task.id, 1, note)
                    onShowMessage("打卡完成！")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("完成打卡 (+1)")
            }
        }
    }
}

@Composable
fun MaterialItemCard(
    material: com.example.data.HolidayStudyMaterial,
    progress: com.example.data.HolidayMaterialProgress?,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    val reciteStatus = progress?.reciteStatus ?: "NOT_STARTED"
    val dictationStatus = progress?.dictationStatus ?: "NOT_STARTED"
    val parentConfirmed = progress?.parentConfirmed ?: false

    val borderColor = if (isHighlighted) Color(0xFF00E5FF) else Color(0xFF00E5FF).copy(alpha = 0.15f)
    val borderWidth = if (isHighlighted) 1.5.dp else 1.dp
    val containerColor = if (isHighlighted) Color(0xFF141A35) else Color(0xFF0F1225)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = material.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    if (!material.author.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = material.author!!,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${material.lessonTitle} · ${material.sourceNote}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Recite Chip
                    val (reciteText, reciteColor) = when (reciteStatus) {
                        "RECITED" -> "已悟熟" to Color(0xFF00E676)
                        "FAMILIAR" -> "已初窥" to Color(0xFF00E5FF)
                        else -> "未参悟" to Color(0xFF94A3B8)
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = reciteColor.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, reciteColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = reciteText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = reciteColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Dictation Chip
                    val (dictText, dictColor) = when (dictationStatus) {
                        "PASSED" -> "已默写" to Color(0xFF00E676)
                        "NEED_RETRY" -> "需重溯" to Color(0xFFEF5350)
                        else -> "未默写" to Color(0xFF94A3B8)
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = dictColor.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, dictColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = dictText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dictColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (parentConfirmed) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF00E676).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "✍️ 大教官印章",
                                fontSize = 10.sp,
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "进入学习",
                tint = Color(0xFF00E5FF).copy(alpha = 0.6f)
            )
        }
    }
}
