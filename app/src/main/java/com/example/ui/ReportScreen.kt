package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CharResult
import com.example.data.PracticeSession
import com.example.data.PracticeSessionConverters
import com.example.data.QuestionResult
import com.example.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val stats by viewModel.userStats.collectAsState()
    val wrongWords by viewModel.wrongWords.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val holidayTasks by viewModel.allHolidayTasks.collectAsState()
    val holidaySessions by viewModel.allHolidayWorkSessions.collectAsState()
    val allMaterials by viewModel.allHolidayMaterials.collectAsState()
    val allMaterialProgress by viewModel.allHolidayMaterialProgress.collectAsState()
    val allRecitations by viewModel.allRecitationRecords.collectAsState()
    val allDictations by viewModel.allDictationRecords.collectAsState()

    val totalMastered = wrongWords.count { it.isMastered }
    val totalToReview = wrongWords.count { !it.isMastered }

    // Holiday Stats Calculation
    val totalReadingMins = remember(holidaySessions) {
        holidaySessions.filter { it.taskType == "READING" }.sumOf { it.durationMinutes }
    }
    val mathSessionCount = remember(holidaySessions) {
        holidaySessions.count { it.subject == "MATH" }
    }
    val englishListeningCount = remember(holidaySessions) {
        holidaySessions.count { it.subject == "ENGLISH" }
    }
    val compositionDoneCount = remember(holidayTasks) {
        holidayTasks.filter { it.taskType == "COMPOSITION" }.count { it.isFinalWritten || it.completedCount >= it.totalCount }
    }
    val lifeDaysCount = remember(holidaySessions) {
        holidaySessions.filter { it.subject in listOf("PRACTICE", "LIFE", "MOVIE") }.map {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.createdAt))
        }.distinct().size
    }
    val pendingParentConfirmCount = remember(holidayTasks) {
        holidayTasks.count { it.completedCount > 0 && !it.isParentConfirmed }
    }

    var selectedSession by remember { mutableStateOf<PracticeSession?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("学习报告 & 练习历史") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("总体数据", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("勇士等级: ${stats?.level ?: 1}")
                    Text("总计金币: ${stats?.coins ?: 0}")
                    Text("总练习题数: ${stats?.totalAnswered ?: 0}")
                    Text("最高连击记录: ${stats?.maxCorrectStreak ?: 0}")
                    val rate = if ((stats?.totalAnswered ?: 0) > 0) {
                        (stats!!.correctCount * 100) / stats!!.totalAnswered
                    } else 0
                    Text("总正确率: $rate%")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("错题情况", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("已成功复仇（掌握）: $totalMastered 个")
                    Text("待复仇错题: $totalToReview 个")
                }
            }

            // Summer Homework Report Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏖️ 暑假作业中心执行报告", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        Text("五升六 · 私有包", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val completedTasksCount = holidayTasks.count { it.completedCount >= it.totalCount || it.status == "COMPLETED" }
                    val totalTasksCount = holidayTasks.size
                    val taskPercent = if (totalTasksCount > 0) (completedTasksCount * 100) / totalTasksCount else 0

                    val recitationMaterials = allMaterials.filter { it.materialType !in listOf("WRITING_TABLE", "WORD_TABLE") }
                    val recitedCount = recitationMaterials.count { m ->
                        allMaterialProgress.find { it.materialId == m.materialId }?.reciteStatus in listOf("RECITED", "FAMILIAR")
                    }
                    val dictatedCount = recitationMaterials.count { m ->
                        allMaterialProgress.find { it.materialId == m.materialId }?.dictationStatus == "PASSED"
                    }

                    Text("📊 作业总完成度: $completedTasksCount / $totalTasksCount 项 ($taskPercent%)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Text("📖 必背古诗/课文熟读: $recitedCount / ${recitationMaterials.size} 篇")
                    Text("🎙️ 背诵录音次数: ${allRecitations.size} 次")
                    Text("✍️ 必背古诗/课文默写过关: $dictatedCount / ${recitationMaterials.size} 篇")
                    Text("📚 课后生字词表练习: 已绑定写字表与词语表")
                    Text("⏱️ 累计自主阅读时长: $totalReadingMins 分钟")
                    Text("📐 数学练习打卡/记录: $mathSessionCount 次")
                    Text("🔤 英语听力/抄写记录: $englishListeningCount 次")
                    Text("📝 大作文誊写搞定数: $compositionDoneCount 篇")
                    Text("⚽ 体育/家务/观影打卡天数: $lifeDaysCount 天")
                    
                    if (pendingParentConfirmCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("⚠️ 尚有 $pendingParentConfirmCount 项任务等待家长复核签字", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("✅ 所有已完成打卡项均已由家长签字复核", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("历史练习记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (sessions.isEmpty()) {
                        Text(
                            "暂无历史听写记录，快去开启第一场“字词讨伐”吧！",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        sessions.forEach { session ->
                            SessionItem(session = session, onClick = { selectedSession = session })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("我的徽章", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val b1 = if ((stats?.totalAnswered ?: 0) > 0) "✅" else "🔒"
                    Text("$b1 第一次完成听写")
                    
                    val b2 = if ((stats?.level ?: 1) > 1) "✅" else "🔒"
                    Text("$b2 第一次通关")
                    
                    val b3 = if ((stats?.maxCorrectStreak ?: 0) >= 5) "✅" else "🔒"
                    Text("$b3 连续答对 5 次")
                    
                    val b4 = if (totalMastered >= 3) "✅" else "🔒"
                    Text("$b4 清除 3 个错题")
                    
                    val b5 = if ((stats?.coins ?: 0) >= 50) "✅" else "🔒"
                    Text("$b5 击败第一个 BOSS")
                    
                    val b6 = if ((stats?.dailyPracticeCount ?: 0) >= 3) "✅" else "🔒"
                    Text("$b6 连续学习 3 天")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Parental Review Detail Dialog
    selectedSession?.let { session ->
        val liveSession = sessions.find { it.sessionId == session.sessionId } ?: session
        ParentReviewDialog(
            session = liveSession,
            viewModel = viewModel,
            onDismiss = { selectedSession = null }
        )
    }
}

@Composable
fun SessionItem(session: PracticeSession, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(session.startedAt))
    
    val durationMs = session.finishedAt - session.startedAt
    val durationMin = if (durationMs > 0) (durationMs / 1000) / 60 else 0
    val durationSec = if (durationMs > 0) (durationMs / 1000) % 60 else 0
    
    val modeStr = when (session.gradingMode) {
        "AUTO" -> "自动批改"
        "ASSISTED" -> "识别辅助"
        else -> "手动验收"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.levelName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Review Status Badge
                val (badgeBg, badgeText, label) = when (session.reviewStatus) {
                    "NEED_PARENT_REVIEW" -> Triple(
                        Color(0xFFFFEBEE),
                        Color(0xFFC62828),
                        "⚠️ 待家长复核"
                    )
                    "REVIEWED" -> Triple(
                        Color(0xFFE8F5E9),
                        Color(0xFF2E7D32),
                        "✅ 已过目/复核"
                    )
                    else -> Triple(
                        Color(0xFFECEFF1),
                        Color(0xFF37474F),
                        "已完成"
                    )
                }
                
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = label,
                        color = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$dateStr | 用时 ${durationMin}分${durationSec}秒 | $modeStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "正确率: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${session.finalAccuracy.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (session.isPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    if (session.finalAccuracy != session.autoAccuracy) {
                        Text(
                            text = " (初判:${session.autoAccuracy.toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentReviewDialog(
    session: PracticeSession,
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val questionResults = remember(session.questionResultsJson) {
        PracticeSessionConverters.toQuestionResultsListStatic(session.questionResultsJson)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("听写明细与改判", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "当前练习: ${session.levelName} | 正确率: ${session.finalAccuracy.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("💡 家长批改说明", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "1. 自动批改模式下，手写识别偶尔有误。您可以根据孩子在下方的真实笔迹轨迹，点击对应的判定按钮重新判分。\n" +
                                "2. 改判为“写对了”会将单词移出或标记为掌握；改判为“写错了”或“差一点”会正式将单词放入错题本。\n" +
                                "3. 金币与等级数据为一次性奖励，改判不影响当前已发放的奖励数额。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                items(questionResults) { qResult ->
                    QuestionReviewCard(
                        qResult = qResult,
                        onOverride = { newResult ->
                            viewModel.overrideSessionResult(session.sessionId, qResult.questionId, newResult)
                        }
                    )
                }
                
                item {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text("复核完毕")
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionReviewCard(
    qResult: QuestionResult,
    onOverride: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "目标词条: ${qResult.getEffectiveTargetAnswer()} (${qResult.correctText})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val visiblePrompt = qResult.getEffectiveVisiblePrompt()
                    if (visiblePrompt.isNotBlank() && visiblePrompt != qResult.correctText) {
                        Text(
                            text = "提示: $visiblePrompt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                val (color, text) = when (qResult.finalResult) {
                    "CORRECT" -> Pair(Color(0xFF2E7D32), "正确")
                    "PARTIAL" -> Pair(Color(0xFFEF6C00), "差一点")
                    else -> Pair(Color(0xFFC62828), "错误")
                }
                
                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = text,
                        color = color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            if (qResult.errorReason.isNotBlank() && qResult.errorReason != "无错误") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "错因分析: ${qResult.errorReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (qResult.finalResult == "CORRECT") Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Render Character stroke grids
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                qResult.charResults.forEach { charRes ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "字: ${charRes.expectedChar}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        MiniTianZiGe(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                        ) {
                            if (!charRes.isBlank) {
                                StrokePreview(
                                    pointsList = charRes.pointsList,
                                    canvasWidth = charRes.canvasWidth,
                                    canvasHeight = charRes.canvasHeight,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    "空白",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (charRes.isBlank) "未书写" else "识: ${charRes.recognizedText.ifEmpty { "无" }}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (charRes.isLikelyCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                        
                        if (!charRes.isBlank && charRes.candidates.isNotEmpty()) {
                            Text(
                                text = "候: ${charRes.candidates.take(3).joinToString(",")}",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))
            
            // Parent Override Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modifier = Modifier.weight(1f)
                
                OutlinedButton(
                    onClick = { onOverride("CORRECT") },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (qResult.finalResult == "CORRECT") Color(0xFFE8F5E9) else Color.Transparent,
                        contentColor = if (qResult.finalResult == "CORRECT") Color(0xFF2E7D32) else Color.Gray
                    ),
                    modifier = modifier,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (qResult.finalResult == "CORRECT") Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Text("写对了", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onOverride("PARTIAL") },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (qResult.finalResult == "PARTIAL") Color(0xFFFFF3E0) else Color.Transparent,
                        contentColor = if (qResult.finalResult == "PARTIAL") Color(0xFFEF6C00) else Color.Gray
                    ),
                    modifier = modifier,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (qResult.finalResult == "PARTIAL") Color(0xFFEF6C00) else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Text("差一点", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onOverride("WRONG") },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (qResult.finalResult == "WRONG") Color(0xFFFFEBEE) else Color.Transparent,
                        contentColor = if (qResult.finalResult == "WRONG") Color(0xFFC62828) else Color.Gray
                    ),
                    modifier = modifier,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (qResult.finalResult == "WRONG") Color(0xFFC62828) else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Text("写错了", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MiniTianZiGe(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color(0xFFFDFDFD))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            // Horizontal dashed line
            drawLine(
                color = Color.Red.copy(alpha = 0.35f),
                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                strokeWidth = 2f,
                pathEffect = dashEffect
            )
            // Vertical dashed line
            drawLine(
                color = Color.Red.copy(alpha = 0.35f),
                start = androidx.compose.ui.geometry.Offset(size.width / 2, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
                strokeWidth = 2f,
                pathEffect = dashEffect
            )
        }
        content()
    }
}

@Composable
fun StrokePreview(
    pointsList: List<List<com.example.ui.PointData>>,
    canvasWidth: Float,
    canvasHeight: Float,
    modifier: Modifier = Modifier,
    strokeColor: Color = Color(0xFF1E293B),
    strokeWidth: Float = 6f
) {
    Canvas(modifier = modifier) {
        val scaleX = if (canvasWidth > 0) size.width / canvasWidth else 1f
        val scaleY = if (canvasHeight > 0) size.height / canvasHeight else 1f
        
        for (stroke in pointsList) {
            if (stroke.isEmpty()) continue
            val path = Path()
            val first = stroke.first()
            path.moveTo(first.x * scaleX, first.y * scaleY)
            for (i in 1 until stroke.size) {
                val pt = stroke[i]
                path.lineTo(pt.x * scaleX, pt.y * scaleY)
            }
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }
    }
}
