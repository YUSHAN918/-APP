package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HolidayMaterialProgress
import com.example.data.HolidayStudyMaterial
import com.example.data.HolidayWorkSession
import com.example.viewmodel.GameViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.util.AudioRecorderHelper
import com.example.util.TTSHelper
import com.example.data.HolidayRecitationRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialStudyScreen(
    onNavigateToBattle: () -> Unit = {},
    material: HolidayStudyMaterial,
    progress: HolidayMaterialProgress?,
    taskId: Long?,
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    var displayMode by remember { mutableStateOf("FULL") } // FULL, MASKED, FIRST_CHAR, SENTENCE
    var isDictationMode by remember { mutableStateOf(false) }
    var selectedDictationLesson by remember { mutableStateOf<String?>(null) }
    var isRecordingMode by remember { mutableStateOf(false) }
    var studentInputText by remember { mutableStateOf("") }
    val ttsState by viewModel.ttsState.collectAsState()
    val ttsActiveText by viewModel.ttsActiveText.collectAsState()
    var showDictationResult by remember { mutableStateOf(false) }
    var showAnnotations by remember { mutableStateOf(true) }
    
    val reciteStatus = progress?.reciteStatus ?: "NOT_STARTED"
    val dictationStatus = progress?.dictationStatus ?: "NOT_STARTED"
    val parentConfirmed = progress?.parentConfirmed ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        if (isDictationMode) {
                            Text(
                                text = "默写进行中...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else if (isRecordingMode) {
                            Text(
                                text = "录音进行中...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = material.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${material.lessonTitle} · ${material.sourceNote}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isDictationMode) {
                            isDictationMode = false
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.speak(material.fullText)
                    }) {
                        if (ttsState == "PLAYING" && ttsActiveText == material.fullText) {
                            Icon(Icons.Default.Pause, contentDescription = "暂停", tint = Color(0xFF38BDF8))
                        } else {
                            Icon(Icons.Default.VolumeUp, contentDescription = "朗读", tint = Color(0xFF38BDF8))
                        }
                    }
                    IconButton(onClick = {
                        viewModel.replay(material.fullText)
                    }) {
                        Icon(Icons.Default.Replay, contentDescription = "重播", tint = MaterialTheme.colorScheme.secondary)
                    }
                    if (ttsState != "STOPPED") {
                        IconButton(onClick = {
                            viewModel.stopSpeaking()
                        }) {
                            Icon(Icons.Default.Stop, contentDescription = "停止", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF070913)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070913))
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Status Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F1225),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("背诵状态：", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            val (reciteText, reciteColor) = when (reciteStatus) {
                                "RECITED" -> "已会背" to Color(0xFF00E676)
                                "FAMILIAR" -> "已熟读" to Color(0xFF00E5FF)
                                else -> "未开始" to Color(0xFF94A3B8)
                            }
                            Text(reciteText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = reciteColor)
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text("默写状态：", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            val (dictText, dictColor) = when (dictationStatus) {
                                "PASSED" -> "已过关" to Color(0xFF00E676)
                                "NEED_RETRY" -> "需重默" to Color(0xFFEF5350)
                                else -> "未默写" to Color(0xFF94A3B8)
                            }
                            Text(dictText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dictColor)
                        }
                    }
                    
                    FilterChip(
                        selected = parentConfirmed,
                        onClick = {
                            viewModel.toggleMaterialParentConfirmed(material.materialId)
                        },
                        label = { Text(if (parentConfirmed) "已签字" else "待签字", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (parentConfirmed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (parentConfirmed) Color(0xFF00E676) else Color(0xFF00E5FF)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF141726),
                            selectedContainerColor = Color(0xFF00E676).copy(alpha = 0.15f),
                            labelColor = Color(0xFF94A3B8),
                            selectedLabelColor = Color(0xFF00E676)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = parentConfirmed,
                            borderColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            selectedBorderColor = Color(0xFF00E676).copy(alpha = 0.5f)
                        )
                    )
                }
            }

            if (material.materialType == "WRITING_TABLE" || material.materialType == "WORD_TABLE") {
                // Table view for writing or vocabulary table
                if (isDictationMode) {
                    TableDictationRunnerView(
                        material = material,
                        selectedLessonFilter = selectedDictationLesson,
                        viewModel = viewModel,
                        taskId = taskId,
                        modifier = Modifier.weight(1f),
                        onCancel = { isDictationMode = false },
                        onComplete = { isDictationMode = false }
                    )
                } else {
                    TableViewContent(
                        material = material,
                        viewModel = viewModel,
                        taskId = taskId,
                        modifier = Modifier.weight(1f),
                        onStartDictation = { lessonTitle ->
                            val result = mutableListOf<com.example.data.WordItem>()
                            val lessonLines = material.fullText.split("\n").filter { it.isNotBlank() }
                            for (line in lessonLines) {
                                val parts = line.split("：")
                                val title = parts.getOrNull(0)?.trim() ?: ""
                                if (lessonTitle != null && title != lessonTitle) continue
                                val itemsInLesson = parts.getOrNull(1)?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
                                for (item in itemsInLesson) {
                                    val rawWord = com.example.data.WordItem(id = 0, text = item, type = "dictation", unitName = "", difficulty = "普通")
                                    val smartWord = com.example.util.SmartPromptGenerator.generateSmartPrompt(rawWord, com.example.util.SmartPromptGenerator.STRATEGY_SMART_RECOMMEND)
                                    result.add(smartWord)
                                }
                            }
                            viewModel.startCustomBattle(lessonTitle ?: material.title, result)
                            onNavigateToBattle()
                        }
                    )
                }
            } else {
                // Text view for poems, texts, daily accumulations
                if (isDictationMode) {
                    HandwritingDictationRunnerView(
                        material = material,
                        viewModel = viewModel,
                        taskId = taskId,
                        modifier = Modifier.weight(1f),
                        onCancel = { isDictationMode = false },
                        onComplete = { isDictationMode = false }
                    )
                } else if (isRecordingMode) {
                    RecitationRecordingView(
                        material = material,
                        viewModel = viewModel,
                        taskId = taskId,
                        modifier = Modifier.weight(1f),
                        onCancel = { isRecordingMode = false },
                        onComplete = { isRecordingMode = false }
                    )
                } else {
                    // Display Mode Selector Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = displayMode == "FULL",
                            onClick = { displayMode = "FULL" },
                            label = { Text("全显正文", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = displayMode == "MASKED",
                            onClick = { displayMode = "MASKED" },
                            label = { Text("遮挡背诵", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = displayMode == "FIRST_CHAR",
                            onClick = { displayMode = "FIRST_CHAR" },
                            label = { Text("首字提示", fontSize = 12.sp) }
                        )
                        val hasAnnotation = remember(material.materialId) {
                            com.example.util.PoemAnnotationProvider.getAnnotation(material.materialId) != null
                        }
                        if (hasAnnotation) {
                            FilterChip(
                                selected = showAnnotations,
                                onClick = { showAnnotations = !showAnnotations },
                                label = { Text("💡 释义与鉴赏", fontSize = 12.sp) }
                            )
                        }
                    }

                    // Main Text Content Display Area
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = material.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (!material.author.isNullOrBlank()) {
                                    Text(
                                        text = material.author,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp, bottom = 12.dp),
                                        color = Color.Gray
                                    )
                                }
                            }

                            item {
                                val textLines = material.fullText.split("\n")
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    for (line in textLines) {
                                        val formattedLine = when (displayMode) {
                                            "MASKED" -> line.replace(Regex("[\u4e00-\u9fa5]"), "█")
                                            "FIRST_CHAR" -> formatFirstCharLine(line)
                                            else -> line
                                        }
                                        Text(
                                            text = formattedLine,
                                            fontSize = 18.sp,
                                            lineHeight = 28.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = if (displayMode == "MASKED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            val annotation = com.example.util.PoemAnnotationProvider.getAnnotation(material.materialId)
                            if (annotation != null && showAnnotations) {
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                        thickness = 1.dp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "古诗词注释与理解",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // 1. 译文
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF38BDF8).copy(alpha = 0.15f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Translate,
                                                    contentDescription = null,
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "【白话译文】",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = annotation.translation,
                                                fontSize = 14.sp,
                                                lineHeight = 22.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                // 2. 重点注释
                                if (annotation.annotations.isNotEmpty()) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFF1E293B).copy(alpha = 0.4f)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.FormatListBulleted,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "【重点注释】",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                annotation.annotations.forEachIndexed { index, pair ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 3.dp),
                                                        verticalAlignment = Alignment.Top
                                                    ) {
                                                        Text(
                                                            text = "${pair.first}：",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.secondary,
                                                            modifier = Modifier.widthIn(min = 60.dp)
                                                        )
                                                        Text(
                                                            text = pair.second,
                                                            fontSize = 13.sp,
                                                            lineHeight = 18.sp,
                                                            color = Color.Gray,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    if (index < annotation.annotations.size - 1) {
                                                        HorizontalDivider(
                                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                                            thickness = 0.5.dp,
                                                            modifier = Modifier.padding(vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 3. 意境赏析
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Book,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "【意境赏析】",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = annotation.appreciation,
                                                fontSize = 14.sp,
                                                lineHeight = 22.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Action Toolbar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.updateMaterialReciteStatus(material.materialId, "FAMILIAR", taskId)
                                    if (taskId != null) {
                                        viewModel.recordWorkSession(
                                            HolidayWorkSession(
                                                taskId = taskId,
                                                subject = material.subject,
                                                taskType = "RECITATION_MEMORIZE",
                                                titleInput = material.title,
                                                note = "标记《${material.title}》为已熟读"
                                            ),
                                            deltaProgress = 0
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("标记已熟读")
                            }

                            Button(
                                onClick = {
                                    viewModel.updateMaterialReciteStatus(material.materialId, "RECITED", taskId)
                                    if (taskId != null) {
                                        viewModel.recordWorkSession(
                                            HolidayWorkSession(
                                                taskId = taskId,
                                                subject = material.subject,
                                                taskType = "RECITATION_MEMORIZE",
                                                titleInput = material.title,
                                                note = "标记《${material.title}》为已会背"
                                            ),
                                            deltaProgress = 1
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("标记已会背")
                            }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { isRecordingMode = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("开始背诵录音", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { isDictationMode = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.Default.EditNote, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("开始手写默写", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
}

private fun formatFirstCharLine(line: String): String {
    val sb = StringBuilder()
    var isNextCharFirst = true
    for (ch in line) {
        if (ch in '\u4e00'..'\u9fa5') {
            if (isNextCharFirst) {
                sb.append(ch)
                isNextCharFirst = false
            } else {
                sb.append("＿")
            }
        } else {
            sb.append(ch)
            if (ch == '，' || ch == '。' || ch == '！' || ch == '？' || ch == '；' || ch == '、') {
                isNextCharFirst = true
            }
        }
    }
    return sb.toString()
}

@Composable
private fun RecitationRecordingView(
    material: HolidayStudyMaterial,
    viewModel: GameViewModel,
    taskId: Long?,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val audioRecorder = remember { AudioRecorderHelper(context) }
    var recordingState by remember { mutableStateOf("IDLE") } // IDLE, RECORDING, PLAYING, SAVED
    var mode by remember { mutableStateOf("MEMORIZE") } // PRACTICE, MEMORIZE
    val ttsState by viewModel.ttsState.collectAsState()
    val ttsActiveText by viewModel.ttsActiveText.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val fileName = "${material.materialId}_${System.currentTimeMillis()}_recitation.m4a"
            if (audioRecorder.startRecording(fileName)) {
                recordingState = "RECORDING"
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { audioRecorder.release() }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("🎙️ 背诵录音", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Row {
                IconButton(onClick = { viewModel.speak(material.fullText) }) {
                    if (ttsState == "PLAYING" && ttsActiveText == material.fullText) {
                        Icon(Icons.Default.Pause, contentDescription = "暂停", tint = Color(0xFF38BDF8))
                    } else {
                        Icon(Icons.Default.VolumeUp, contentDescription = "示范朗读", tint = Color(0xFF38BDF8))
                    }
                }
                IconButton(onClick = { viewModel.replay(material.fullText) }) {
                    Icon(Icons.Default.Replay, contentDescription = "重播", tint = MaterialTheme.colorScheme.secondary)
                }
                if (ttsState != "STOPPED") {
                    IconButton(onClick = { viewModel.stopSpeaking() }) {
                        Icon(Icons.Default.Stop, contentDescription = "停止朗读", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = mode == "MEMORIZE", onClick = { mode = "MEMORIZE" }, label = { Text("脱稿背诵") })
            FilterChip(selected = mode == "PRACTICE", onClick = { mode = "PRACTICE" }, label = { Text("朗读练习") })
        }

        if (mode == "PRACTICE") {
            Surface(modifier = Modifier.weight(1f).fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item { Text(material.fullText, fontSize = 16.sp, lineHeight = 24.sp) }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
            Text("请闭眼背诵全文，录音将提交家长确认。", modifier = Modifier.align(Alignment.CenterHorizontally), color = Color.Gray)
            Spacer(modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (recordingState == "IDLE") {
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            val fileName = "${material.materialId}_${System.currentTimeMillis()}_recitation.m4a"
                            if (audioRecorder.startRecording(fileName)) {
                                recordingState = "RECORDING"
                            }
                        } else {
                            launcher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("开始录音") }
            } else if (recordingState == "RECORDING") {
                Button(
                    onClick = {
                        audioRecorder.stopRecording()
                        val path = audioRecorder.currentFilePath
                        if (path != null) {
                            val file = java.io.File(path)
                            if (!file.exists() || file.length() < 1000) {
                                viewModel.synthesizeMockAudio(material.fullText, file)
                            }
                        }
                        recordingState = "SAVED"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) { Text("停止录音") }
            } else if (recordingState == "SAVED" || recordingState == "PLAYING") {
                Button(onClick = {
                    if (recordingState == "PLAYING") {
                        audioRecorder.stopPlaying()
                        recordingState = "SAVED"
                    } else {
                        recordingState = "PLAYING"
                        audioRecorder.startPlaying { recordingState = "SAVED" }
                    }
                }) { Text(if (recordingState == "PLAYING") "停止播放" else "播放回听") }
                
                Button(onClick = {
                    val path = audioRecorder.currentFilePath
                    if (path != null) {
                        viewModel.insertRecitationRecord(
                            HolidayRecitationRecord(
                                materialId = material.materialId,
                                materialTitle = material.title,
                                audioPath = path,
                                durationMs = 0,
                                mode = mode,
                                parentStatus = "PENDING"
                            )
                        )
                        viewModel.updateMaterialReciteStatus(material.materialId, "RECITED", taskId)
                    }
                    onComplete()
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("提交家长确认") }
            }
        }
        
        OutlinedButton(onClick = {
            audioRecorder.deleteCurrentRecording()
            onCancel()
        }, modifier = Modifier.fillMaxWidth()) { Text("退出") }
    }
}

data class DictationSavedChar(
    val strokes: List<com.example.ui.StrokeData>,
    val width: Float,
    val height: Float
)

@Composable
private fun HandwritingDictationRunnerView(
    material: HolidayStudyMaterial,
    viewModel: GameViewModel,
    taskId: Long?,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onComplete: () -> Unit
) {
    var step by remember { mutableStateOf(0) } // 0: writing, 1: reviewing
    val ttsState by viewModel.ttsState.collectAsState()
    val ttsActiveText by viewModel.ttsActiveText.collectAsState()
    
    val sentences = remember(material) { 
        val list = mutableListOf<Pair<String, String>>()
        if (material.title.isNotBlank()) {
            val t = material.title.replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9]"), "")
            if (t.isNotEmpty()) list.add("【标题】" to t)
        }
        if (!material.author.isNullOrBlank()) {
            val authorStr = material.author
            if (authorStr.startsWith("【") && authorStr.contains("】")) {
                val dynasty = authorStr.substringAfter("【").substringBefore("】").replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9]"), "")
                val name = authorStr.substringAfter("】").replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9]"), "")
                if (dynasty.isNotEmpty()) list.add("【朝代】" to dynasty)
                if (name.isNotEmpty()) list.add("【作者】" to name)
            } else {
                val a = authorStr.replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9]"), "")
                if (a.isNotEmpty()) list.add("【作者】" to a)
            }
        }
        
        val bodySentences = material.fullText.split(Regex("(?<=[。！？；])\\s*")).filter { it.isNotBlank() }
        var sentenceCount = 1
        bodySentences.forEach { s ->
            val clean = s.replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9]"), "")
            if (clean.isNotEmpty()) {
                list.add("【正文: 第 $sentenceCount 句】" to clean)
                sentenceCount++
            }
        }
        list
    }
    
    var currentSentenceIndex by remember { mutableStateOf(0) }
    val currentSentenceData = sentences.getOrElse(currentSentenceIndex) { "" to "" }
    val currentSentence = currentSentenceData.second
    val currentType = currentSentenceData.first
    
    var charIndex by remember { mutableStateOf(0) }
    var strokesList by remember { mutableStateOf(mutableListOf<DictationSavedChar>()) }
    var allStrokesList by remember { mutableStateOf(mutableListOf<List<DictationSavedChar>>()) }
    var handwritingView by remember { mutableStateOf<HandwritingView?>(null) }
    
    val context = LocalContext.current
    
    if (sentences.isEmpty()) {
        Column(modifier = modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("无可默写内容", modifier = Modifier.padding(16.dp))
            Button(onClick = onCancel) { Text("返回") }
        }
        return
    }

    Column(modifier = modifier.fillMaxSize().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("📝 手写默写: $currentType", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("进度: 第 ${currentSentenceIndex + 1} / ${sentences.size} 项", fontSize = 14.sp, color = Color.Gray)
            }
            Row {
                IconButton(onClick = { viewModel.speak(currentSentence) }) {
                    if (ttsState == "PLAYING" && ttsActiveText == currentSentence) {
                        Icon(Icons.Default.Pause, contentDescription = "暂停", tint = Color(0xFF38BDF8))
                    } else {
                        Icon(Icons.Default.VolumeUp, contentDescription = "朗读本句", tint = Color(0xFF38BDF8))
                    }
                }
                IconButton(onClick = { viewModel.replay(currentSentence) }) {
                    Icon(Icons.Default.Replay, contentDescription = "重播", tint = MaterialTheme.colorScheme.secondary)
                }
                if (ttsState != "STOPPED") {
                    IconButton(onClick = { viewModel.stopSpeaking() }) {
                        Icon(Icons.Default.Stop, contentDescription = "停止朗读", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        
        if (step == 0) {
            Text("当前默写第 ${charIndex + 1} / ${currentSentence.length} 个字", color = MaterialTheme.colorScheme.secondary)
            
            // Slots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in currentSentence.indices) {
                    Box(modifier = Modifier.size(40.dp).background(Color.White).border(1.dp, if (i == charIndex) MaterialTheme.colorScheme.primary else Color.Gray).padding(2.dp), contentAlignment = Alignment.Center) {
                        if (i < strokesList.size) {
                            val saved = strokesList[i]
                            MiniCanvas(strokes = saved.strokes, originalWidth = saved.width, originalHeight = saved.height, cols = 1, rows = 1)
                        }
                    }
                }
            }
            
            val playerProfile by viewModel.playerProfile.collectAsState()
            val equippedBrushConfig by viewModel.equippedBrushConfig.collectAsState()
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp).border(2.dp, MaterialTheme.colorScheme.outline)) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        HandwritingView(ctx).apply { handwritingView = this }
                    },
                    update = { view ->
                        val equippedBrushId = playerProfile?.equippedBrushId ?: "default_black"
                        view.currentBrush = BrushStyle.getBrushById(equippedBrushId)
                        val safeConfig = equippedBrushConfig?.copy(
                            opacity = 1f,
                            glowRadius = 0f,
                            particleDensity = 0f,
                            usageMode = "TEST_SAFE"
                        )
                        view.currentBrushConfig = safeConfig
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedButton(
                    onClick = { handwritingView?.clear() },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) { Text("清空", fontSize = 13.sp) }
                OutlinedButton(
                    onClick = { handwritingView?.undo() },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) { Text("撤销", fontSize = 13.sp) }
                Button(
                    onClick = {
                        val currentStrokes = handwritingView?.getStrokes() ?: emptyList()
                        val w = handwritingView?.width?.toFloat() ?: 1000f
                        val h = handwritingView?.height?.toFloat() ?: 1000f
                        val newList = strokesList.toMutableList()
                        val savedChar = DictationSavedChar(currentStrokes, w, h)
                        
                        if (charIndex < newList.size) {
                            newList[charIndex] = savedChar
                        } else {
                            newList.add(savedChar)
                        }
                        strokesList = newList
                        handwritingView?.clear()
                        
                        if (charIndex < currentSentence.length - 1) {
                            charIndex++
                        } else {
                            // Current sentence finished
                            val newAllStrokes = allStrokesList.toMutableList()
                            if (currentSentenceIndex < newAllStrokes.size) {
                                newAllStrokes[currentSentenceIndex] = newList
                            } else {
                                newAllStrokes.add(newList)
                            }
                            allStrokesList = newAllStrokes
                            
                            if (currentSentenceIndex < sentences.size - 1) {
                                currentSentenceIndex++
                                charIndex = 0
                                strokesList = mutableListOf()
                            } else {
                                step = 1
                            }
                        }
                    },
                    modifier = Modifier.weight(2f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (charIndex < currentSentence.length - 1) "保存这个字" else "保存并下一句",
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }
        } else if (step == 1) {
            Text("【全文对照复盘】", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("请滑动检查您的默写结果，没问题即可提交。", fontSize = 13.sp, color = Color.Gray)
            
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(sentences.size) { sIdx ->
                    val sData = sentences[sIdx]
                    val sType = sData.first
                    val sText = sData.second
                    val sStrokes = allStrokesList.getOrNull(sIdx) ?: emptyList()
                    
                    Column(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)).padding(8.dp)) {
                        Text(sType, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(sText.indices.toList()) { i ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(sText[i].toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    Box(modifier = Modifier.size(60.dp).background(Color.White).border(1.dp, Color.Gray).padding(2.dp)) {
                                        if (i < sStrokes.size) {
                                            val saved = sStrokes[i]
                                            MiniCanvas(strokes = saved.strokes, originalWidth = saved.width, originalHeight = saved.height, cols = 1, rows = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = { 
                        step = 0
                        currentSentenceIndex = 0
                        charIndex = 0
                        strokesList = mutableListOf()
                        allStrokesList = mutableListOf()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("全部重写") }
                
                Button(
                    onClick = {
                        // Save full dictation record
                        viewModel.insertDictationRecord(
                            com.example.data.HolidayDictationRecord(
                                materialId = material.materialId,
                                materialTitle = material.title,
                                sentenceIndex = -1, // -1 means full text
                                standardText = material.fullText,
                                handwrittenStrokesJson = "[]" // In real app, serialize allStrokesList to JSON
                            )
                        )
                        
                        viewModel.updateMaterialDictationStatus(material.materialId, "PASSED", taskId)
                        onComplete()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("完成默写提交") }
            }
        }
    }
}

@Composable
private fun TableViewContent(
    material: HolidayStudyMaterial,
    viewModel: GameViewModel,
    taskId: Long?,
    modifier: Modifier = Modifier,
    onStartDictation: (String?) -> Unit
) {
    val ttsState by viewModel.ttsState.collectAsState()
    val ttsActiveText by viewModel.ttsActiveText.collectAsState()
    val lessons = remember(material.fullText) {
        material.fullText.split("\n").filter { it.isNotBlank() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1225)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (material.materialType == "WRITING_TABLE") "✍️ 六年级上册写字表练习（带组词）" else "📚 六年级上册词语表练习",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "点击单个字词可带组词示范朗读；点击“听写本课”或“全册听写”发起大田字格手写默写。",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onStartDictation(null) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF141726),
                            contentColor = Color(0xFF00E5FF)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("全册听写", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(lessons) { lessonLine ->
            val parts = lessonLine.split("：")
            val title = parts.getOrNull(0) ?: ""
            val items = parts.getOrNull(1)?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1225)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFAB40),
                            letterSpacing = 0.5.sp
                        )
                        Button(
                            onClick = { onStartDictation(title) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF141726),
                                contentColor = Color(0xFFFFAB40)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFAB40).copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFAB40))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("听写本课", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items.forEach { item ->
                            val spokenPrompt = remember(item) { com.example.util.CharacterCompoundDictionary.getSpokenPrompt(item) }
                            val compound = remember(item) { com.example.util.CharacterCompoundDictionary.getCompoundWord(item) }
                            val isPlayingThis = ttsState == "PLAYING" && ttsActiveText == spokenPrompt

                            Surface(
                                modifier = Modifier
                                    .clickable {
                                        viewModel.speak(spokenPrompt)
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF141726),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isPlayingThis) Color(0xFF00E5FF) else Color(0xFF00E5FF).copy(alpha = 0.15f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (!compound.isNullOrBlank() && material.materialType == "WRITING_TABLE") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "($compound)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color(0xFFFFAB40),
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFF00E5FF)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class DictationTableItem(
    val lessonTitle: String,
    val charOrWord: String
)

@Composable
private fun TableDictationRunnerView(
    material: HolidayStudyMaterial,
    selectedLessonFilter: String?,
    viewModel: GameViewModel,
    taskId: Long?,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onComplete: () -> Unit
) {
    var step by remember { mutableStateOf(0) } // 0: writing, 1: reviewing
    val ttsState by viewModel.ttsState.collectAsState()
    val ttsActiveText by viewModel.ttsActiveText.collectAsState()

    val dictationItems = remember(material, selectedLessonFilter) {
        val result = mutableListOf<DictationTableItem>()
        val lessonLines = material.fullText.split("\n").filter { it.isNotBlank() }
        for (line in lessonLines) {
            val parts = line.split("：")
            val title = parts.getOrNull(0)?.trim() ?: ""
            if (selectedLessonFilter != null && title != selectedLessonFilter) {
                continue
            }
            val itemsInLesson = parts.getOrNull(1)?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
            for (item in itemsInLesson) {
                result.add(DictationTableItem(lessonTitle = title, charOrWord = item))
            }
        }
        result
    }

    var currentIndex by remember { mutableStateOf(0) }
    var charIndex by remember { mutableStateOf(0) }
    var wordStrokesList by remember { mutableStateOf(mutableListOf<DictationSavedChar>()) }
    var allSavedStrokes by remember { mutableStateOf(mutableListOf<List<DictationSavedChar>>()) }
    var handwritingView by remember { mutableStateOf<HandwritingView?>(null) }
    var showAnswer by remember(currentIndex) { mutableStateOf(false) }

    val currentItem = dictationItems.getOrNull(currentIndex)
    val charOrWord = currentItem?.charOrWord ?: ""
    val lessonTitle = currentItem?.lessonTitle ?: ""
    val compoundWord = remember(charOrWord) { com.example.util.CharacterCompoundDictionary.getCompoundWord(charOrWord) }
    val spokenPrompt = remember(charOrWord) { com.example.util.CharacterCompoundDictionary.getSpokenPrompt(charOrWord) }

    LaunchedEffect(currentIndex, step) {
        if (step == 0 && charOrWord.isNotEmpty()) {
            viewModel.speak(spokenPrompt)
        }
    }

    if (dictationItems.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("暂无可听写字词", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onCancel) { Text("返回") }
        }
        return
    }

    Column(modifier = modifier.fillMaxSize().padding(8.dp)) {
        if (step == 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📝 生字/词语听写: $lessonTitle",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "进度: 第 ${currentIndex + 1} / ${dictationItems.size} 个",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Row {
                    IconButton(onClick = { viewModel.speak(spokenPrompt) }) {
                        if (ttsState == "PLAYING" && ttsActiveText == spokenPrompt) {
                            Icon(Icons.Default.Pause, contentDescription = "暂停", tint = Color(0xFF38BDF8))
                        } else {
                            Icon(Icons.Default.VolumeUp, contentDescription = "朗读", tint = Color(0xFF38BDF8))
                        }
                    }
                    IconButton(onClick = { viewModel.replay(spokenPrompt) }) {
                        Icon(Icons.Default.Replay, contentDescription = "重播", tint = MaterialTheme.colorScheme.secondary)
                    }
                    if (ttsState != "STOPPED") {
                        IconButton(onClick = { viewModel.stopSpeaking() }) {
                            Icon(Icons.Default.Stop, contentDescription = "停止", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "语境提示",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val maskedPrompt = remember(charOrWord) { com.example.util.CharacterCompoundDictionary.getMaskedContextPrompt(charOrWord) }
                    Text(
                        text = if (showAnswer) (compoundWord ?: charOrWord) else maskedPrompt,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "请根据语音写出空缺字词",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { showAnswer = !showAnswer },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (showAnswer) "🙈 隐藏答案" else "👁️ 显示答案",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text("本题需要书写 ${charOrWord.length} 个字", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Text("当前第 ${charIndex + 1} / ${charOrWord.length} 个字", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            // Slots display
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                for (i in 0 until charOrWord.length) {
                    val isCurrent = (i == charIndex)
                    val saved = wordStrokesList.getOrNull(i)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(if (isCurrent) Color(0xFFFFF9C4) else Color(0xFFFDF6E3))
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = RoundedCornerShape(4.dp)
                            )
                    ) {
                        if (saved != null) {
                            MiniCanvas(saved.strokes, saved.width, saved.height, 1, 1)
                        }
                    }
                }
            }

            val tablePlayerProfile by viewModel.playerProfile.collectAsState()
            val tableEquippedBrushConfig by viewModel.equippedBrushConfig.collectAsState()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        HandwritingView(ctx).apply { handwritingView = this }
                    },
                    update = { view ->
                        view.setGrid(1, 1)
                        val equippedBrushId = tablePlayerProfile?.equippedBrushId ?: "default_black"
                        view.currentBrush = BrushStyle.getBrushById(equippedBrushId)
                        val safeConfig = tableEquippedBrushConfig?.copy(
                            opacity = 1f,
                            glowRadius = 0f,
                            particleDensity = 0f,
                            usageMode = "TEST_SAFE"
                        )
                        view.currentBrushConfig = safeConfig
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedButton(
                    onClick = { handwritingView?.clear() },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("清空", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = { handwritingView?.undo() },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("撤销", fontSize = 13.sp)
                }
                Button(
                    onClick = {
                        val currentStrokes = handwritingView?.getStrokes() ?: emptyList()
                        val w = handwritingView?.width?.toFloat() ?: 1000f
                        val h = handwritingView?.height?.toFloat() ?: 1000f
                        val savedChar = DictationSavedChar(currentStrokes, w, h)
                        val newList = wordStrokesList.toMutableList()
                        if (charIndex < newList.size) {
                            newList[charIndex] = savedChar
                        } else {
                            newList.add(savedChar)
                        }
                        wordStrokesList = newList
                        handwritingView?.clear()
                        if (charIndex < charOrWord.length - 1) {
                            charIndex++
                        } else {
                            val newAllStrokes = allSavedStrokes.toMutableList()
                            if (currentIndex < newAllStrokes.size) {
                                newAllStrokes[currentIndex] = newList
                            } else {
                                newAllStrokes.add(newList)
                            }
                            allSavedStrokes = newAllStrokes
                            if (currentIndex < dictationItems.size - 1) {
                                currentIndex++
                                charIndex = 0
                                wordStrokesList = mutableListOf()
                            } else {
                                step = 1
                            }
                        }
                    },
                    modifier = Modifier.weight(2f),
                ) {
                    Text(
                        text = if (charIndex < charOrWord.length - 1) "保存这个字" else "保存并下一题",
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }
        } else {
            Text("🎉 听写默写完成对照复盘", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("共默写 ${dictationItems.size} 项，请对照检查手写痕迹。", fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dictationItems.size) { idx ->
                    val item = dictationItems[idx]
                    val cWord = com.example.util.CharacterCompoundDictionary.getCompoundWord(item.charOrWord)
                    val sStrokes = allSavedStrokes.getOrNull(idx) ?: emptyList()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxWidth()
                        ) {
                            Text("${item.lessonTitle} · 第 ${idx + 1} 个", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = item.charOrWord,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (!cWord.isNullOrBlank()) {
                                Text("组词: $cWord", fontSize = 13.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            androidx.compose.foundation.lazy.LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(item.charOrWord.indices.toList()) { i ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(modifier = Modifier.size(60.dp).background(Color.White).border(1.dp, Color.Gray).padding(2.dp)) {
                                            if (i < sStrokes.size) {
                                                val saved = sStrokes[i]
                                                MiniCanvas(strokes = saved.strokes, originalWidth = saved.width, originalHeight = saved.height, cols = 1, rows = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        step = 0
                        currentIndex = 0
                        charIndex = 0
                        wordStrokesList = mutableListOf()
                        allSavedStrokes = mutableListOf()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("重新听写") }

                Button(
                    onClick = {
                        viewModel.insertDictationRecord(
                            com.example.data.HolidayDictationRecord(
                                materialId = material.materialId,
                                materialTitle = "${material.title}${if (selectedLessonFilter != null) " - $selectedLessonFilter" else ""}",
                                sentenceIndex = -1,
                                standardText = dictationItems.joinToString(" ") { it.charOrWord },
                                handwrittenStrokesJson = "[]"
                            )
                        )
                        viewModel.updateMaterialDictationStatus(material.materialId, "PASSED", taskId)
                        onComplete()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("提交听写成绩") }
            }
        }
    }
}
@Composable
private fun FlowRowLayout(
    items: List<String>,
    content: @Composable (String) -> Unit
) {
    Column {
        var currentIndex = 0
        while (currentIndex < items.size) {
            val chunk = items.subList(currentIndex, (currentIndex + 4).coerceAtMost(items.size))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                for (item in chunk) {
                    content(item)
                }
            }
            currentIndex += 4
        }
    }
}
