package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Level
import com.example.data.WordItem
import com.example.data.BatchPromptStats
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

enum class ParentView {
    DASHBOARD,
    BATCH_IMPORT,
    IMPORT_CONFIRM,
    QUESTION_BANK,
    LEVEL_MANAGEMENT,
    LEVEL_DETAIL,
    LEVEL_EDIT,
    DICTATION_SETTINGS,
    RECOGNITION_SETTINGS,
    PRESET_PACKS,
    PACK_DETAIL,
    PACK_IMPORT_CONFIRM,
    HOLIDAY_HOMEWORK_MANAGEMENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNavigateToLab: () -> Unit,
    onStartLevelTest: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentView by remember { mutableStateOf(ParentView.DASHBOARD) }

    // State from database / flow
    val allWords by viewModel.allWords.collectAsState()
    val levels by viewModel.allLevels.collectAsState()
    val stats by viewModel.userStats.collectAsState()
    
    // Sub-screen states:
    var allPacksState by remember { mutableStateOf<List<com.example.data.ContentPack>>(emptyList()) }
    var selectedPackForDetail by remember { mutableStateOf<com.example.data.ContentPack?>(null) }
    var importedPackToConfirm by remember { mutableStateOf<com.example.data.ContentPack?>(null) }
    var rawImportJsonToConfirm by remember { mutableStateOf("") }

    // 1. Batch Import text state
    var rawInputText by remember { mutableStateOf("") }
    var parsedWordsToConfirm by remember { mutableStateOf<List<WordItem>>(emptyList()) }
    
    // Batch settings for confirmation screen
    var batchUnitName by remember { mutableStateOf("第六单元") }
    var batchLessonName by remember { mutableStateOf("") }
    var batchType by remember { mutableStateOf("词语") }
    var batchDifficulty by remember { mutableStateOf("普通") }

    // 2. Level detail screen
    var selectedLevelForDetail by remember { mutableStateOf<Level?>(null) }

    // 3. Level edit screen
    var editingLevel by remember { mutableStateOf<Level?>(null) }
    var editingLevelName by remember { mutableStateOf("") }
    var editingLevelUnitName by remember { mutableStateOf("") }
    var editingLevelUnlocked by remember { mutableStateOf(true) }
    var editingLevelWords by remember { mutableStateOf<List<WordItem>>(emptyList()) }
    var editingLevelPracticeLimitMode by remember { mutableStateOf("ALL") }
    var editingLevelFixedCountStr by remember { mutableStateOf("") }
    var showAddWordsFromPoolDialog by remember { mutableStateOf(false) }
    var poolSearchQuery by remember { mutableStateOf("") }

    // 4. Word pool direct editing (Question Bank)
    var showAddWordToPoolDialog by remember { mutableStateOf(false) }
    var newWordText by remember { mutableStateOf("") }
    var newWordUnit by remember { mutableStateOf("第六单元") }
    var newWordType by remember { mutableStateOf("词语") }
    var newWordDifficulty by remember { mutableStateOf("普通") }
    var newWordPromptMode by remember { mutableStateOf("FULL_WORD") }
    var newWordHiddenIndicesStr by remember { mutableStateOf("0") }
    var newWordVisiblePrompt by remember { mutableStateOf("") }
    var newWordTtsPrompt by remember { mutableStateOf("") }
    var newWordTargetAnswer by remember { mutableStateOf("") }
    var wordPoolSearchQuery by remember { mutableStateOf("") }
    var selectedSourceTab by remember { mutableStateOf("全部") }
    var selectedUnitTab by remember { mutableStateOf("全部") }

    // Prompt Setting Dialog Global State
    var promptSettingWord by remember { mutableStateOf<WordItem?>(null) }
    var batchPromptMode by remember { mutableStateOf("FULL_WORD") }

    // 5. Dictation & Recognition Settings local inputs
    var timePerWord by remember(stats) { mutableIntStateOf(stats?.timePerWord ?: 30) }
    var playCount by remember(stats) { mutableIntStateOf(stats?.playCount ?: 2) }
    var allowExtra by remember(stats) { mutableStateOf(stats?.allowExtraPlay ?: true) }
    var wordsPerLevel by remember(stats) { mutableIntStateOf(stats?.wordsPerLevel ?: 8) }
    var passRate by remember(stats) { mutableIntStateOf(stats?.passRate ?: 80) }
    var gradingMode by remember(stats) { mutableStateOf(stats?.gradingMode ?: "ASSISTED") }
    var allowStudentViewMeaning by remember(stats) { mutableStateOf(stats?.allowStudentViewMeaning ?: "AFTER_ERROR") }
    var showModelErrorDialog by remember { mutableStateOf(false) }

    // Batch prompt dialog state
    var showBatchPromptDialog by remember { mutableStateOf(false) }
    var batchScopeType by remember { mutableStateOf("FILTERED") }
    var batchStrategy by remember { mutableStateOf("SMART_RECOMMEND") }
    var forceOverrideManual by remember { mutableStateOf(false) }
    var batchResultStats by remember { mutableStateOf<BatchPromptStats?>(null) }
    var showMissingContextDialog by remember { mutableStateOf(false) }
    var missingContextWordsList by remember { mutableStateOf<List<WordItem>>(emptyList()) }
    val modelStatus by com.example.viewmodel.DigitalInkRecognizerManager.modelStatus.collectAsState()

    LaunchedEffect(showModelErrorDialog) {
        if (showModelErrorDialog && modelStatus == "已预置 (离线免下载)") {
            gradingMode = "AUTO"
            showModelErrorDialog = false
        }
    }

    LaunchedEffect(currentView) {
        if (currentView == ParentView.PRESET_PACKS || currentView == ParentView.DASHBOARD) {
            allPacksState = com.example.data.ContentPackManager.getAllPacks(context)
        }
    }

    // Helper functions for counts and items
    fun getLevelWordCount(lvl: Level): Int {
        return if (!lvl.wordIdsStr.isNullOrEmpty()) {
            lvl.wordIdsStr.split(",").filter { it.isNotBlank() }.size
        } else {
            allWords.count { it.unitName == lvl.unitName }
        }
    }

    fun getLevelWords(lvl: Level): List<WordItem> {
        return if (!lvl.wordIdsStr.isNullOrEmpty()) {
            val ids = lvl.wordIdsStr.split(",").mapNotNull { it.trim().toIntOrNull() }
            val wordMap = allWords.associateBy { it.id }
            ids.mapNotNull { wordMap[it] }
        } else {
            allWords.filter { it.unitName == lvl.unitName }
        }
    }

    if (showModelErrorDialog) {
        AlertDialog(
            onDismissRequest = { showModelErrorDialog = false },
            title = { Text("提示") },
            text = { Text("您需要先下载中文手写识别模型，才能开启自动批改功能。请点击“手写识别实验室”下载模型包。") },
            confirmButton = {
                TextButton(onClick = { showModelErrorDialog = false }) { Text("好的") }
            }
        )
    }

    if (promptSettingWord != null) {
        PromptSettingDialog(
            word = promptSettingWord!!,
            onDismiss = { promptSettingWord = null },
            onSave = { updated ->
                coroutineScope.launch {
                    viewModel.addWordToDatabase(updated)
                    if (currentView == ParentView.IMPORT_CONFIRM) {
                        parsedWordsToConfirm = parsedWordsToConfirm.map { if (it.id == updated.id || (it.id == 0 && it.text == updated.text)) updated else it }
                    } else if (currentView == ParentView.LEVEL_EDIT) {
                        editingLevelWords = editingLevelWords.map { if (it.id == updated.id) updated else it }
                    }
                    Toast.makeText(context, "已更新“${updated.text}”的听写提示！", Toast.LENGTH_SHORT).show()
                    promptSettingWord = null
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentView) {
                            ParentView.DASHBOARD -> "家长管理中心"
                            ParentView.BATCH_IMPORT -> "批量导入字词句"
                            ParentView.IMPORT_CONFIRM -> "导入确认与属性配置"
                            ParentView.QUESTION_BANK -> "题库管理"
                            ParentView.LEVEL_MANAGEMENT -> "关卡制作管理"
                            ParentView.LEVEL_DETAIL -> "关卡详情页"
                            ParentView.LEVEL_EDIT -> "编辑关卡"
                            ParentView.DICTATION_SETTINGS -> "听写参数设置"
                            ParentView.RECOGNITION_SETTINGS -> "智能识别与批改设置"
                            ParentView.PRESET_PACKS -> "预设题库包"
                            ParentView.PACK_DETAIL -> "题库包详情"
                            ParentView.PACK_IMPORT_CONFIRM -> "导入确认"
                            ParentView.HOLIDAY_HOMEWORK_MANAGEMENT -> "暑假作业管理中心"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.testTag("parent_back_btn"),
                        onClick = {
                            if (currentView == ParentView.DASHBOARD) {
                                onBack()
                            } else if (currentView == ParentView.IMPORT_CONFIRM) {
                                currentView = ParentView.BATCH_IMPORT
                            } else if (currentView == ParentView.LEVEL_DETAIL) {
                                currentView = ParentView.LEVEL_MANAGEMENT
                            } else if (currentView == ParentView.LEVEL_EDIT) {
                                currentView = ParentView.LEVEL_MANAGEMENT
                            } else if (currentView == ParentView.PACK_DETAIL) {
                                currentView = ParentView.PRESET_PACKS
                            } else if (currentView == ParentView.PACK_IMPORT_CONFIRM) {
                                currentView = ParentView.PRESET_PACKS
                            } else {
                                currentView = ParentView.DASHBOARD
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentView) {
                // ==================== DASHBOARD ====================
                ParentView.DASHBOARD -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title Hero Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("我是 山神", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("欢迎使用 PolyForge 字词听写家长端。您可在此高效导入每日生词、量身定制孩子专属关卡。数据完全保存在本地，离线可用。", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        // Function entries
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(110.dp)
                                        .clickable { currentView = ParentView.BATCH_IMPORT }
                                        .testTag("entry_batch_import"),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "批量导入", tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("批量导入字词句", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("粘贴文本，自动拆分", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(110.dp)
                                        .clickable { currentView = ParentView.LEVEL_MANAGEMENT }
                                        .testTag("entry_levels"),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.List, contentDescription = "关卡管理", tint = MaterialTheme.colorScheme.secondary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("关卡制作管理", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("制作、编辑、复制关卡", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(110.dp)
                                        .clickable { currentView = ParentView.QUESTION_BANK }
                                        .testTag("entry_question_bank"),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Build, contentDescription = "题库管理", tint = MaterialTheme.colorScheme.tertiary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("题库管理", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("检索、追加、清除词条", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(110.dp)
                                        .clickable { currentView = ParentView.DICTATION_SETTINGS }
                                        .testTag("entry_dictation_settings"),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Settings, contentDescription = "听写设置", tint = MaterialTheme.colorScheme.outline)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("听写设置", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("次数、时限、通过率", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(110.dp)
                                        .clickable { currentView = ParentView.RECOGNITION_SETTINGS }
                                        .testTag("entry_recognition_settings"),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "智能识别", tint = Color(0xFF008080))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("智能批改设置", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("识别辅助与批改策略", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(110.dp)
                                        .clickable {
                                            Toast.makeText(context, "本地题库备份与恢复功能开发中，敬请期待！", Toast.LENGTH_SHORT).show()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "数据备份", tint = Color.Gray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("数据备份与恢复", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                                        Text("本地存储、数据导出 [占位]", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        // Preset Content Packs Entry
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { currentView = ParentView.PRESET_PACKS }
                                    .testTag("entry_preset_packs"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = "预设题库包",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "预设题库包",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "安装原创年级训练包，或导入家长自定义包",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "进入",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Holiday Homework Management Entry
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { currentView = ParentView.HOLIDAY_HOMEWORK_MANAGEMENT }
                                    .testTag("entry_holiday_homework_management"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "暑假作业管理中心",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "暑假作业管理中心",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "期末成绩分段设置、作业包修复更新与家长签字",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "进入",
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }

                        // ML Kit Download Area Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("离线手写识别模型实验室", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("自动批改与识别辅助，极力推荐在此进行模型包下载与校验。", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = onNavigateToLab) {
                                        Text("去配置")
                                    }
                                }
                            }
                        }
                    }
                }

                // ==================== BATCH IMPORT ====================
                ParentView.BATCH_IMPORT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("批量粘贴录入字词句", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        OutlinedTextField(
                            value = rawInputText,
                            onValueChange = { rawInputText = it },
                            placeholder = { Text("示例格式 1：\n清晨\n黎明\n观察\n记录\n\n示例格式 2：\n清晨、黎明、观察、记录\n\n示例格式 3：\n1. 清晨\n2. 黎明") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("batch_import_input"),
                            maxLines = 15
                        )

                        // Photographic OCR Warning block
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "拍照说明", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "拍照识别功能待后续版本接入。当前请使用批量粘贴导入，以保障离线环境的安全隐私与稳定性。",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (rawInputText.isBlank()) {
                                    Toast.makeText(context, "请输入字词内容", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Split words according to strict rules
                                    val rawTokens = rawInputText.split(Regex("[\\r\\n,，、\\s]+"))
                                    val resultWords = mutableListOf<WordItem>()
                                    rawTokens.forEach { token ->
                                        var clean = token.trim()
                                        if (clean.isNotEmpty()) {
                                            // Strip out numbering index e.g., "1.", "1、", "一、", "(一)"
                                            clean = clean.replaceFirst(Regex("^\\(?[0-9a-zA-Z一二三四五六七八九十百]+[\\.\\、\\.、:：\\s\\)]+"), "")
                                            clean = clean.trim()
                                            if (clean.isNotEmpty() && resultWords.none { it.text == clean }) {
                                                resultWords.add(
                                                    WordItem(
                                                        text = clean,
                                                        type = "词语",
                                                        unitName = "第六单元",
                                                        difficulty = "普通"
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    
                                    if (resultWords.isEmpty()) {
                                        Toast.makeText(context, "拆分后词条为空，请重新输入", Toast.LENGTH_SHORT).show()
                                    } else {
                                        parsedWordsToConfirm = resultWords
                                        currentView = ParentView.IMPORT_CONFIRM
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_auto_split")
                        ) {
                            Text("自动拆分并进入属性确认")
                        }
                    }
                }

                // ==================== IMPORT CONFIRM ====================
                ParentView.IMPORT_CONFIRM -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("批量及单独设置词条属性", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        // Top Batch Settings Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("批量属性设置 (应用到当前所有词条)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = batchUnitName,
                                        onValueChange = { batchUnitName = it },
                                        label = { Text("所属单元") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = batchLessonName,
                                        onValueChange = { batchLessonName = it },
                                        label = { Text("课文名(选填)") },
                                        placeholder = { Text("如:清晨") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Word Type selectors
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("类型", style = MaterialTheme.typography.bodySmall)
                                        Row {
                                            listOf("字", "词语", "成语", "句子").forEach { type ->
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { batchType = type }) {
                                                    RadioButton(selected = batchType == type, onClick = { batchType = type })
                                                    Text(type, style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                        }
                                    }
                                    
                                    // Difficulty selectors
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("难度", style = MaterialTheme.typography.bodySmall)
                                        Row {
                                            listOf("普通", "易错", "BOSS").forEach { diff ->
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { batchDifficulty = diff }) {
                                                    RadioButton(selected = batchDifficulty == diff, onClick = { batchDifficulty = diff })
                                                    Text(diff, style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                        }
                                    }
                                }

                                Column {
                                    Text("批量听写模式设置", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        listOf(
                                            "FULL_WORD" to "全部完整听写",
                                            "CLOZE_FIRST" to "全部只写第一个字",
                                            "CLOZE_SECOND" to "全部只写第二个字"
                                        ).forEach { (mKey, mLabel) ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable { batchPromptMode = mKey }
                                            ) {
                                                RadioButton(selected = batchPromptMode == mKey, onClick = { batchPromptMode = mKey })
                                                Text(mLabel, style = MaterialTheme.typography.bodySmall)
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        parsedWordsToConfirm = parsedWordsToConfirm.map { w ->
                                            var updated = w.copy(
                                                unitName = batchUnitName,
                                                type = batchType,
                                                difficulty = batchDifficulty
                                            )
                                            if (batchPromptMode == "FULL_WORD") {
                                                updated = updated.copy(promptMode = "FULL_WORD", visiblePrompt = "", ttsPrompt = "", targetAnswer = "")
                                            } else if (batchPromptMode == "CLOZE_FIRST") {
                                                if (w.text.length >= 2) {
                                                    updated = updated.copy(
                                                        promptMode = "CLOZE_CHAR",
                                                        hiddenIndicesStr = "0",
                                                        visiblePrompt = "__${w.text.drop(1)}",
                                                        targetAnswer = w.text.take(1),
                                                        ttsPrompt = "请写“${w.text}”的第一个字"
                                                    )
                                                }
                                            } else if (batchPromptMode == "CLOZE_SECOND") {
                                                if (w.text.length >= 2) {
                                                    updated = updated.copy(
                                                        promptMode = "CLOZE_CHAR",
                                                        hiddenIndicesStr = "1",
                                                        visiblePrompt = "${w.text.take(1)}__${w.text.drop(2)}",
                                                        targetAnswer = w.text.substring(1, 2),
                                                        ttsPrompt = "请写“${w.text}”的第二个字"
                                                    )
                                                }
                                            }
                                            updated
                                        }
                                        Toast.makeText(context, "批量属性及听写模式设置成功！", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("一键应用到全部")
                                }
                            }
                        }

                        // Word list
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(parsedWordsToConfirm) { index, word ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("词条内容: ${word.text}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            WordPromptTag(word = word, onEditPrompt = { promptSettingWord = word })
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                SuggestionChip(onClick = {}, label = { Text(word.unitName) })
                                                SuggestionChip(onClick = {}, label = { Text(word.type) })
                                                SuggestionChip(onClick = {}, label = { Text(word.difficulty) })
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                parsedWordsToConfirm = parsedWordsToConfirm.toMutableList().apply { removeAt(index) }
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentView = ParentView.BATCH_IMPORT },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("上一步")
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        // 1. Save confirmed words to the database
                                        val savedIds = mutableListOf<Int>()
                                        parsedWordsToConfirm.forEach { w ->
                                            val generatedId = viewModel.addWordToDatabase(w)
                                            savedIds.add(generatedId.toInt())
                                        }
                                        Toast.makeText(context, "成功保存 ${savedIds.size} 个词条到题库！", Toast.LENGTH_SHORT).show()
                                        currentView = ParentView.DASHBOARD
                                    }
                                },
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text("仅保存到题库")
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        // 1. Save words
                                        val savedIds = mutableListOf<Int>()
                                        parsedWordsToConfirm.forEach { w ->
                                            val generatedId = viewModel.addWordToDatabase(w)
                                            savedIds.add(generatedId.toInt())
                                        }
                                        
                                        // 2. Generate a custom level
                                        val customLevelName = if (batchLessonName.isNotBlank()) "${batchLessonName}听写" else "${batchUnitName}听写"
                                        val customLvlId = viewModel.insertCustomLevel(
                                            name = customLevelName,
                                            unitName = batchUnitName,
                                            wordIds = savedIds
                                        )
                                        
                                        Toast.makeText(context, "成功生成关卡: $customLevelName", Toast.LENGTH_SHORT).show()
                                        
                                        // 3. Load the level into the detail screen state
                                        selectedLevelForDetail = Level(
                                            id = customLvlId.toInt(),
                                            name = customLevelName,
                                            unitName = batchUnitName,
                                            isUnlocked = true,
                                            isCompleted = false,
                                            wordIdsStr = savedIds.joinToString(",")
                                        )
                                        currentView = ParentView.LEVEL_DETAIL
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("btn_save_and_level")
                            ) {
                                Text("保存并生成关卡")
                            }
                        }
                    }
                }

                // ==================== LEVEL MANAGEMENT ====================
                ParentView.LEVEL_MANAGEMENT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("关卡制作与状态管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(levels) { level ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (level.isPreset) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = level.name,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    val labelText = if (level.sourcePackId != null) "听写包关卡" else if (level.isPreset) "内置系统关" else "家长自定义"
                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                if (level.sourcePackId != null || level.isPreset) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                                                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = labelText,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = if (level.sourcePackId != null || level.isPreset) MaterialTheme.colorScheme.onPrimaryContainer
                                                                    else MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("所属单元: ${level.unitName} | 题目: ${getLevelWordCount(level)} 词", style = MaterialTheme.typography.bodyMedium)
                                            }
                                            
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(if (level.isUnlocked) "已开放" else "未开放", style = MaterialTheme.typography.bodySmall)
                                                Switch(
                                                    checked = level.isUnlocked,
                                                    onCheckedChange = { isChecked ->
                                                        viewModel.updateLevel(level.copy(isUnlocked = isChecked))
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider()
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                // Start test play button
                                                IconButton(
                                                    onClick = { onStartLevelTest(level.id) },
                                                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                                ) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = "试玩", tint = MaterialTheme.colorScheme.primary)
                                                }

                                                // Edit button
                                                IconButton(
                                                    onClick = {
                                                        editingLevel = level
                                                        editingLevelName = level.name
                                                        editingLevelUnitName = level.unitName
                                                        editingLevelUnlocked = level.isUnlocked
                                                        editingLevelWords = getLevelWords(level)
                                                        editingLevelPracticeLimitMode = level.practiceLimitMode
                                                        editingLevelFixedCountStr = level.fixedQuestionCount?.toString() ?: ""
                                                        currentView = ParentView.LEVEL_EDIT
                                                    },
                                                    modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.secondary)
                                                }

                                                // Copy level button
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            viewModel.insertCustomLevel(
                                                                name = "${level.name}(副本)",
                                                                unitName = level.unitName,
                                                                wordIds = getLevelWords(level).map { it.id }
                                                            )
                                                            Toast.makeText(context, "已成功复制关卡：${level.name}(副本)", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(8.dp))
                                                ) {
                                                    Icon(Icons.Default.Share, contentDescription = "复制关卡", tint = MaterialTheme.colorScheme.tertiary)
                                                }
                                            }

                                            // Delete level button
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteLevel(level.id)
                                                    Toast.makeText(context, "已删除关卡 ${level.name}", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "删除关卡", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        FloatingActionButton(
                            onClick = {
                                currentView = ParentView.BATCH_IMPORT
                            },
                            modifier = Modifier.align(Alignment.End),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "制作新关卡", tint = Color.White)
                        }
                    }
                }

                // ==================== LEVEL DETAIL ====================
                ParentView.LEVEL_DETAIL -> {
                    val level = selectedLevelForDetail
                    if (level != null) {
                        val levelWords = getLevelWords(level)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("🎉 关卡制作完成！", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("关卡名称: ${level.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("所属单元: ${level.unitName}", style = MaterialTheme.typography.bodyMedium)
                                    Text("题目数量: ${levelWords.size} 词", style = MaterialTheme.typography.bodyMedium)
                                    val limitText = when (level.practiceLimitMode) {
                                        "ALL" -> "本关全部练完 (${levelWords.size}题)"
                                        "FIXED" -> "固定练完 ${level.fixedQuestionCount ?: levelWords.size} 题"
                                        else -> "跟随全局设置 (${stats?.wordsPerLevel ?: 8}题)"
                                    }
                                    Text("练习模式: $limitText", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Text("题目词条列表: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(levelWords) { word ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(word.text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                WordPromptTag(word = word, onEditPrompt = { promptSettingWord = word })
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                SuggestionChip(onClick = {}, label = { Text(word.type) })
                                                SuggestionChip(onClick = {}, label = { Text(word.difficulty) })
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { currentView = ParentView.LEVEL_MANAGEMENT },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("返回管理首页")
                                }

                                Button(
                                    onClick = { onStartLevelTest(level.id) },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("btn_start_play_test")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "开始试玩")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("开始试玩听写")
                                }
                            }
                        }
                    }
                }

                // ==================== LEVEL EDIT ====================
                ParentView.LEVEL_EDIT -> {
                    val currentEditing = editingLevel
                    if (currentEditing != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("编辑家长自定义关卡信息", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                            OutlinedTextField(
                                value = editingLevelName,
                                onValueChange = { editingLevelName = it },
                                label = { Text("关卡名称") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = editingLevelUnitName,
                                onValueChange = { editingLevelUnitName = it },
                                label = { Text("所属单元") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("开放给孩子挑战", fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = editingLevelUnlocked,
                                    onCheckedChange = { editingLevelUnlocked = it }
                                )
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("关卡题量练习模式设置：", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        listOf(
                                            "ALL" to "本关全部练完",
                                            "USE_GLOBAL" to "跟随全局设置",
                                            "FIXED" to "固定题数"
                                        ).forEach { (mKey, mLabel) ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable { editingLevelPracticeLimitMode = mKey }
                                            ) {
                                                RadioButton(
                                                    selected = editingLevelPracticeLimitMode == mKey,
                                                    onClick = { editingLevelPracticeLimitMode = mKey }
                                                )
                                                Text(mLabel, style = MaterialTheme.typography.bodySmall)
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                        }
                                    }
                                    if (editingLevelPracticeLimitMode == "FIXED") {
                                        OutlinedTextField(
                                            value = editingLevelFixedCountStr,
                                            onValueChange = { editingLevelFixedCountStr = it },
                                            label = { Text("自定义固定练完题数 (如 12)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }
                                }
                            }

                            Divider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("关卡题目排序及删改", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                TextButton(
                                    onClick = { showAddWordsFromPoolDialog = true }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "添加题目")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("从题库添加词条")
                                }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(editingLevelWords) { idx, word ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(word.text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                Text("${word.unitName} | ${word.type}", style = MaterialTheme.typography.bodySmall)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                WordPromptTag(word = word, onEditPrompt = { promptSettingWord = word })
                                            }

                                            // Reordering and deletion actions
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Move Up button
                                                TextButton(
                                                    onClick = {
                                                        if (idx > 0) {
                                                            editingLevelWords = editingLevelWords.toMutableList().apply {
                                                                val temp = this[idx]
                                                                this[idx] = this[idx - 1]
                                                                this[idx - 1] = temp
                                                            }
                                                        }
                                                    },
                                                    enabled = idx > 0
                                                ) {
                                                    Text("↑", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                }

                                                // Move Down button
                                                TextButton(
                                                    onClick = {
                                                        if (idx < editingLevelWords.size - 1) {
                                                            editingLevelWords = editingLevelWords.toMutableList().apply {
                                                                val temp = this[idx]
                                                                this[idx] = this[idx + 1]
                                                                this[idx + 1] = temp
                                                            }
                                                        }
                                                    },
                                                    enabled = idx < editingLevelWords.size - 1
                                                ) {
                                                    Text("↓", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                }

                                                // Remove word from level button
                                                IconButton(
                                                    onClick = {
                                                        editingLevelWords = editingLevelWords.toMutableList().apply { removeAt(idx) }
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "从关卡移除", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { currentView = ParentView.LEVEL_MANAGEMENT },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("取消")
                                }

                                Button(
                                    onClick = {
                                        val updated = currentEditing.copy(
                                            name = editingLevelName,
                                            unitName = editingLevelUnitName,
                                            isUnlocked = editingLevelUnlocked,
                                            wordIdsStr = editingLevelWords.map { it.id }.joinToString(","),
                                            practiceLimitMode = editingLevelPracticeLimitMode,
                                            fixedQuestionCount = editingLevelFixedCountStr.toIntOrNull()
                                        )
                                        viewModel.updateLevel(updated)
                                        Toast.makeText(context, "修改保存成功！", Toast.LENGTH_SHORT).show()
                                        currentView = ParentView.LEVEL_MANAGEMENT
                                    },
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Text("保存修改")
                                }
                            }
                        }
                    }
                }

                // ==================== QUESTION BANK ====================
                ParentView.QUESTION_BANK -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("题库字词句分类管理与检索", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = wordPoolSearchQuery,
                            onValueChange = { wordPoolSearchQuery = it },
                            placeholder = { Text("搜索题库内容或单元") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 1. 第一级：题库包来源筛选 (Group by Content Pack / Source)
                        val uniqueSources = remember(allWords) {
                            val srcList = allWords.map { word ->
                                if (word.unitName.contains(" · ")) {
                                    word.unitName.substringBefore(" · ")
                                } else {
                                    "默认题库"
                                }
                            }.distinct().sorted()
                            listOf("全部") + srcList
                        }

                        // 2. 第二级：对应来源下的章节单元筛选
                        val uniqueUnits = remember(allWords, selectedSourceTab) {
                            val filteredUnits = allWords.filter { word ->
                                if (selectedSourceTab == "全部") true
                                else {
                                    val src = if (word.unitName.contains(" · ")) word.unitName.substringBefore(" · ") else "默认题库"
                                    src == selectedSourceTab
                                }
                            }.map { it.unitName }.distinct().sortedWith(compareBy<String> { extractUnitNumber(it) }.thenBy { it })
                            listOf("全部") + filteredUnits
                        }

                        // 自动调整无效的单元选中状态
                        LaunchedEffect(selectedSourceTab, uniqueUnits) {
                            if (selectedUnitTab != "全部" && !uniqueUnits.contains(selectedUnitTab)) {
                                selectedUnitTab = "全部"
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("⚡ 批量听写提示生成", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Text("一键为单字智能配语境短语，多字词自动挖空，补充词义解释", style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = { showBatchPromptDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("一键配置", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // UI Tier 1: Sources Horizontal Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("选择题库来源:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(uniqueSources) { src ->
                                    val isSelected = selectedSourceTab == src
                                    val count = if (src == "全部") allWords.size else allWords.count { word ->
                                        val s = if (word.unitName.contains(" · ")) word.unitName.substringBefore(" · ") else "默认题库"
                                        s == src
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedSourceTab = src },
                                        label = { Text("$src ($count)") },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null
                                    )
                                }
                            }
                        }

                        // UI Tier 2: Units Horizontal Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                Text("选择对应章节单元:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(uniqueUnits) { unit ->
                                    val isSelected = selectedUnitTab == unit
                                    val count = if (unit == "全部") {
                                        allWords.count { word ->
                                            if (selectedSourceTab == "全部") true
                                            else {
                                                val s = if (word.unitName.contains(" · ")) word.unitName.substringBefore(" · ") else "默认题库"
                                                s == selectedSourceTab
                                            }
                                        }
                                    } else {
                                        allWords.count { it.unitName == unit }
                                    }

                                    val displayLabel = if (unit == "全部") {
                                        "全部单元"
                                    } else if (unit.contains(" · ")) {
                                        unit.substringAfter(" · ")
                                    } else {
                                        unit
                                    }

                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedUnitTab = unit },
                                        label = { Text("$displayLabel ($count)") },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null
                                    )
                                }
                            }
                        }

                        val filteredPool = allWords.filter { word ->
                            val wordSource = if (word.unitName.contains(" · ")) word.unitName.substringBefore(" · ") else "默认题库"
                            val matchesSource = (selectedSourceTab == "全部" || wordSource == selectedSourceTab)
                            val matchesUnit = (selectedUnitTab == "全部" || word.unitName == selectedUnitTab)
                            val matchesQuery = word.text.contains(wordPoolSearchQuery, ignoreCase = true) ||
                                    word.unitName.contains(wordPoolSearchQuery, ignoreCase = true)
                            matchesSource && matchesUnit && matchesQuery
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val sourcePart = if (selectedSourceTab == "全部") "全部来源" else selectedSourceTab
                            val unitPart = if (selectedUnitTab == "全部") "全部单元" else {
                                if (selectedUnitTab.contains(" · ")) selectedUnitTab.substringAfter(" · ") else selectedUnitTab
                            }
                            Text(
                                text = "当前筛选: $sourcePart > $unitPart",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "共 ${filteredPool.size} 个条目",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredPool) { word ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(word.text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                Box(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(word.type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))

                                            val hasSeparator = word.unitName.contains(" · ")
                                            val srcLabel = if (hasSeparator) word.unitName.substringBefore(" · ") else "默认题库"
                                            val unitLabel = if (hasSeparator) word.unitName.substringAfter(" · ") else word.unitName

                                            Text(
                                                text = if (hasSeparator) "$srcLabel  ▶  $unitLabel  |  属性: ${word.difficulty}" else "$unitLabel  |  属性: ${word.difficulty}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            WordPromptTag(word = word, onEditPrompt = { promptSettingWord = word })
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.deleteWord(word.id)
                                                Toast.makeText(context, "已从题库中删除: ${word.text}", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "彻底删除", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { showAddWordToPoolDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "追加词条")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("向题库中手动追加一个词条")
                        }
                    }
                }

                // ==================== DICTATION SETTINGS ====================
                ParentView.DICTATION_SETTINGS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("系统听写核心参数配置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    
                                    Text("每题书写时间限制:", fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        listOf(15, 30, 45, 60).forEach { t ->
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { timePerWord = t }) {
                                                RadioButton(selected = timePerWord == t, onClick = { timePerWord = t })
                                                Text("${t}s")
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                        }
                                    }
                                    
                                    Divider()

                                    Text("自动语音重播遍数:", fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        listOf(1, 2, 3).forEach { c ->
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { playCount = c }) {
                                                RadioButton(selected = playCount == c, onClick = { playCount = c })
                                                Text("$c 遍")
                                                Spacer(modifier = Modifier.width(12.dp))
                                            }
                                        }
                                    }
                                    
                                    Divider()

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Text("允许学生手动重听", fontWeight = FontWeight.Bold)
                                            Text("不限制遍数，辅助孩子理解生词", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Switch(checked = allowExtra, onCheckedChange = { allowExtra = it })
                                    }
                                    
                                    Divider()

                                    Text("每关最大测试题数限额:", fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        listOf(5, 8, 10, 0).forEach { w ->
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { wordsPerLevel = w }) {
                                                RadioButton(selected = wordsPerLevel == w, onClick = { wordsPerLevel = w })
                                                Text(if (w == 0) "全部" else "$w 题")
                                                Spacer(modifier = Modifier.width(10.dp))
                                            }
                                        }
                                    }
                                    
                                    Divider()

                                    Text("评判及格与通关正确率阈值:", fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        listOf(60, 80, 90, 100).forEach { p ->
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { passRate = p }) {
                                                RadioButton(selected = passRate == p, onClick = { passRate = p })
                                                Text("$p%")
                                                Spacer(modifier = Modifier.width(10.dp))
                                            }
                                        }
                                    }

                                    Divider()

                                    Text("学生听写时允许查看字词解释/提示:", fontWeight = FontWeight.Bold)
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(
                                            "AFTER_ERROR" to "仅错题后/结算页可查看（推荐：不干扰首次听写）",
                                            "ALWAYS" to "听写界面允许点击「查看提示解释」按钮",
                                            "NEVER" to "全程关闭字词解释提示"
                                        ).forEach { (mode, label) ->
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { allowStudentViewMeaning = mode }) {
                                                RadioButton(selected = allowStudentViewMeaning == mode, onClick = { allowStudentViewMeaning = mode })
                                                Text(label, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.updateSettings(timePerWord, playCount, allowExtra, wordsPerLevel, passRate, gradingMode)
                                viewModel.updateAllowStudentViewMeaning(allowStudentViewMeaning)
                                Toast.makeText(context, "听写核心参数已保存！", Toast.LENGTH_SHORT).show()
                                currentView = ParentView.DASHBOARD
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("应用并保存此设置")
                        }
                    }
                }

                // ==================== RECOGNITION SETTINGS ====================
                ParentView.RECOGNITION_SETTINGS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("手写识别与批改策略配置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("手写识别实时辅助反馈:", fontWeight = FontWeight.Bold)
                                    val recognitionMode by viewModel.recognitionMode.collectAsState()
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(
                                            0 to "完全关闭实时识别辅助",
                                            2 to "开启手写识别辅助反馈 (默认推荐)",
                                            1 to "模拟离线识别功能 (开发调试模式)"
                                        ).forEach { (mode, label) ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { viewModel.setRecognitionMode(mode) }
                                            ) {
                                                RadioButton(selected = recognitionMode == mode, onClick = { viewModel.setRecognitionMode(mode) })
                                                Text(label, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("听写批改模式切换:", fontWeight = FontWeight.Bold)
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(
                                            "MANUAL" to "家长手工逐字验收模式",
                                            "ASSISTED" to "辅助识别模式 (家长验收并参考AI候选词) [默认]",
                                            "AUTO" to "自动智能批改模式 (模型直接评定结算) [实验版]"
                                        ).forEach { (mode, label) ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        if (mode == "AUTO" && modelStatus != "已下载") {
                                                            showModelErrorDialog = true
                                                        } else {
                                                            gradingMode = mode
                                                        }
                                                    }
                                            ) {
                                                RadioButton(
                                                    selected = gradingMode == mode,
                                                    onClick = {
                                                        if (mode == "AUTO" && modelStatus != "已下载") {
                                                            showModelErrorDialog = true
                                                        } else {
                                                            gradingMode = mode
                                                        }
                                                    }
                                                )
                                                Text(label, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }

                                    if (gradingMode == "AUTO") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "💡 自动智能批改：系统结合手写候选集实现高容错自动批改并清算金币经验。建议家长随后在「练习报告」中进行复核与改判以保证百分百准确度。",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.updateSettings(timePerWord, playCount, allowExtra, wordsPerLevel, passRate, gradingMode)
                                Toast.makeText(context, "智能识别配置已保存！", Toast.LENGTH_SHORT).show()
                                currentView = ParentView.DASHBOARD
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("应用并保存此设置")
                        }
                    }
                }

                ParentView.PRESET_PACKS -> {
                    var showImportDialog by remember { mutableStateOf(false) }
                    var importJsonText by remember { mutableStateOf("") }
                    var uninstallPackConfirm by remember { mutableStateOf<com.example.data.ContentPack?>(null) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("预设与导入题库包", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Button(
                                onClick = { showImportDialog = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("导入题库包 JSON", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // List of packs
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val installed = allPacksState.filter { it.isInstalled }
                            val uninstalled = allPacksState.filter { !it.isInstalled }

                            if (installed.isNotEmpty()) {
                                item {
                                    Text("已安装的题库包 (${installed.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                items(installed) { pack ->
                                    PackCardItem(
                                        pack = pack,
                                        onDetail = {
                                            selectedPackForDetail = pack
                                            currentView = ParentView.PACK_DETAIL
                                        },
                                        onAction = {
                                            uninstallPackConfirm = pack
                                        }
                                    )
                                }
                            }

                            if (uninstalled.isNotEmpty()) {
                                item {
                                    Text("未安装的题库包 (${uninstalled.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                items(uninstalled) { pack ->
                                    PackCardItem(
                                        pack = pack,
                                        onDetail = {
                                            selectedPackForDetail = pack
                                            currentView = ParentView.PACK_DETAIL
                                        },
                                        onAction = {
                                            viewModel.installContentPack(pack, {
                                                allPacksState = com.example.data.ContentPackManager.getAllPacks(context)
                                                Toast.makeText(context, "“${pack.name}”安装成功！关卡已自动生成。", Toast.LENGTH_LONG).show()
                                            }, { err ->
                                                Toast.makeText(context, "安装失败: $err", Toast.LENGTH_SHORT).show()
                                            })
                                        }
                                    )
                                }
                            }
                        }

                        // Bottom copyright disclaimer
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "版权与来源提示：\n内置训练包为原创通用练习内容，不对应任何特定教材版本。如需同步学校教材，请由家长根据孩子实际教材通过批量导入或授权内容包导入。",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Dialog for JSON import
                    if (showImportDialog) {
                        AlertDialog(
                            onDismissRequest = { showImportDialog = false },
                            title = { Text("导入题库包 JSON") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("请粘贴题库包的 JSON 格式文本。系统将在下一步显示预览确认：", style = MaterialTheme.typography.bodySmall)
                                    OutlinedTextField(
                                        value = importJsonText,
                                        onValueChange = { importJsonText = it },
                                        placeholder = { Text("{\n  \"name\": \"家长自定义五年级词语包\",\n  \"grade\": \"五年级\",\n  \"units\": [...]\n}") },
                                        modifier = Modifier.fillMaxWidth().height(200.dp),
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (importJsonText.isNotBlank()) {
                                            try {
                                                val root = org.json.JSONObject(importJsonText)
                                                val testName = root.getString("name")
                                                val testGrade = root.getString("grade")
                                                val unitsArray = root.getJSONArray("units")
                                                
                                                val tempPack = com.example.data.ContentPack(
                                                    id = "temp_preview",
                                                    name = testName,
                                                    description = root.optString("description", "导入的自定义包"),
                                                    grade = testGrade,
                                                    semester = root.optString("semester", "上册"),
                                                    sourceType = try {
                                                        com.example.data.PackSourceType.valueOf(root.optString("sourceType", "USER_IMPORTED"))
                                                    } catch (e: Exception) {
                                                        com.example.data.PackSourceType.USER_IMPORTED
                                                    },
                                                    versionName = "1.0.0",
                                                    createdAt = System.currentTimeMillis(),
                                                    updatedAt = System.currentTimeMillis(),
                                                    isInstalled = false,
                                                    units = emptyList()
                                                )
                                                
                                                rawImportJsonToConfirm = importJsonText
                                                importedPackToConfirm = tempPack
                                                showImportDialog = false
                                                currentView = ParentView.PACK_IMPORT_CONFIRM
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "JSON 格式解析失败: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "请输入 JSON 内容", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Text("解析预览")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showImportDialog = false }) {
                                    Text("取消")
                                }
                            }
                        )
                    }

                    // Dialog for Uninstall confirmation
                    if (uninstallPackConfirm != null) {
                        val pack = uninstallPackConfirm!!
                        AlertDialog(
                            onDismissRequest = { uninstallPackConfirm = null },
                            title = { Text("卸载题库包确认") },
                            text = {
                                Text("卸载后会移除该预设包生成的题库和关卡，但不会删除孩子已经产生的练习记录。确定卸载吗？", style = MaterialTheme.typography.bodyMedium)
                            },
                            confirmButton = {
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    onClick = {
                                        viewModel.uninstallContentPack(pack) {
                                            allPacksState = com.example.data.ContentPackManager.getAllPacks(context)
                                            Toast.makeText(context, "“${pack.name}”已成功卸载！相关词条和关卡已移除。", Toast.LENGTH_SHORT).show()
                                        }
                                        uninstallPackConfirm = null
                                    }
                                ) {
                                    Text("确定卸载", color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { uninstallPackConfirm = null }) {
                                    Text("取消")
                                }
                            }
                        )
                    }
                }

                ParentView.PACK_DETAIL -> {
                    val pack = selectedPackForDetail
                    if (pack == null) {
                        currentView = ParentView.PRESET_PACKS
                    } else {
                        val prefix = if (pack.sourceType == com.example.data.PackSourceType.ORIGINAL) "五年级通用训练" else pack.name

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Pack Details Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(pack.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        val sourceLabel = when (pack.sourceType) {
                                            com.example.data.PackSourceType.ORIGINAL -> "原创训练包"
                                            com.example.data.PackSourceType.USER_IMPORTED -> "家长导入包"
                                            com.example.data.PackSourceType.USER_PRIVATE -> "私有教材包"
                                            com.example.data.PackSourceType.LICENSED -> "授权内容包"
                                        }
                                        Text(
                                            text = sourceLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(pack.description, style = MaterialTheme.typography.bodyMedium)

                                    if (pack.sourceType == com.example.data.PackSourceType.USER_PRIVATE) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            listOf("五年级", "下册", "私有教材包", "本地内置", "第一批内容").forEach { tag ->
                                                Text(
                                                    text = tag,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text("学段年级: ${pack.grade} (${pack.semester})", style = MaterialTheme.typography.bodySmall)
                                        Text("单元总数: ${pack.units.size}", style = MaterialTheme.typography.bodySmall)
                                        Text("词条总数: ${pack.units.sumOf { it.items.size }}", style = MaterialTheme.typography.bodySmall)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Action buttons at pack-level
                                    if (pack.isInstalled) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                onClick = {
                                                    viewModel.uninstallContentPack(pack) {
                                                        allPacksState = com.example.data.ContentPackManager.getAllPacks(context)
                                                        selectedPackForDetail = allPacksState.find { it.id == pack.id }
                                                        Toast.makeText(context, "“${pack.name}”已成功卸载！相关词条和关卡已移除。", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            ) {
                                                Text("一键卸载")
                                            }

                                            var showRepairConfirm by remember { mutableStateOf(false) }
                                            Button(
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                                onClick = { showRepairConfirm = true }
                                            ) {
                                                Text("修复/重新安装")
                                            }

                                            if (showRepairConfirm) {
                                                AlertDialog(
                                                    onDismissRequest = { showRepairConfirm = false },
                                                    title = { Text("修复安装确认") },
                                                    text = { Text("重新安装将会重新生成该包的全部词条与关卡（不会删除孩子已有练习历史）。确定重新安装吗？") },
                                                    confirmButton = {
                                                        Button(
                                                            onClick = {
                                                                showRepairConfirm = false
                                                                viewModel.repairInstallContentPack(pack, {
                                                                    allPacksState = com.example.data.ContentPackManager.getAllPacks(context)
                                                                    selectedPackForDetail = allPacksState.find { it.id == pack.id }
                                                                    Toast.makeText(context, "“${pack.name}”重新生成与修复成功！", Toast.LENGTH_SHORT).show()
                                                                }, { err ->
                                                                    Toast.makeText(context, "修复失败: $err", Toast.LENGTH_SHORT).show()
                                                                })
                                                            }
                                                        ) {
                                                            Text("确定")
                                                        }
                                                    },
                                                    dismissButton = {
                                                        TextButton(onClick = { showRepairConfirm = false }) {
                                                            Text("取消")
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    } else {
                                        val anyUnitInstalled = pack.units.any { u ->
                                            val cleanU = u.unitName.split("：").first().trim()
                                            levels.any { it.name == "$prefix · $cleanU" }
                                        }
                                        val btnText = if (anyUnitInstalled) "补全安装剩余单元关卡" else "一键生成全部单元关卡"
                                        Button(
                                            modifier = Modifier.fillMaxWidth(),
                                            onClick = {
                                                viewModel.installContentPack(pack, {
                                                    allPacksState = com.example.data.ContentPackManager.getAllPacks(context)
                                                    selectedPackForDetail = allPacksState.find { it.id == pack.id }
                                                    Toast.makeText(context, "“${pack.name}”全部关卡已生成！", Toast.LENGTH_SHORT).show()
                                                }, { err ->
                                                    Toast.makeText(context, "安装失败: $err", Toast.LENGTH_SHORT).show()
                                                })
                                            }
                                        ) {
                                            Text(btnText)
                                        }
                                    }

                                    if (pack.sourceType == com.example.data.PackSourceType.USER_PRIVATE) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "该包为本地私有学习测试内容，仅用于家庭自用。请勿作为公开教材资源分发。",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Text("包含的单元与词语 (${pack.units.size} 个单元)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            // Units List
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(pack.units) { unit ->
                                    val cleanUnit = unit.unitName.split("：").first().trim()
                                    val levelName = "$prefix · $cleanUnit"
                                    val isUnitInstalled = levels.any { it.name == levelName }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(unit.unitName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

                                                if (isUnitInstalled) {
                                                    Text(
                                                        text = "已生成关卡",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFF008080),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                } else {
                                                    TextButton(
                                                        onClick = {
                                                            viewModel.generateUnitLevel(pack, unit, {
                                                                allPacksState = com.example.data.ContentPackManager.getAllPacks(context)
                                                                selectedPackForDetail = allPacksState.find { it.id == pack.id }
                                                                Toast.makeText(context, "“$cleanUnit”关卡已单独生成！", Toast.LENGTH_SHORT).show()
                                                            }, { err ->
                                                                Toast.makeText(context, "生成失败: $err", Toast.LENGTH_SHORT).show()
                                                            })
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("一键生成该单元")
                                                    }
                                                }
                                            }

                                            // Words preview
                                            val wordsStr = unit.items.joinToString("、") { it.text }
                                            Text(
                                                text = "词条: $wordsStr",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val clozeCount = unit.items.count { it.promptMode == "CLOZE_CHAR" || it.promptMode == "CONTEXT_CLUE" }
                                            if (clozeCount > 0) {
                                                Text(
                                                    text = "💡 包含 $clozeCount 个特殊填空/语境提示词条",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ParentView.PACK_IMPORT_CONFIRM -> {
                    val tempPack = importedPackToConfirm
                    if (tempPack == null) {
                        currentView = ParentView.PRESET_PACKS
                    } else {
                        val parsedPack = remember(rawImportJsonToConfirm) {
                            try {
                                com.example.data.ContentPackManager.importPackFromJson(context, rawImportJsonToConfirm)?.let {
                                    com.example.data.ContentPackManager.deleteCustomPackMetadata(context, it.id)
                                    it
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (parsedPack == null) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("JSON 内容解析异常，请返回重新检查格式。")
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { currentView = ParentView.PRESET_PACKS }) {
                                    Text("返回列表")
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("解析成功！请确认题库包详情", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("题库包名称: ${parsedPack.name}", fontWeight = FontWeight.Bold)
                                        Text("学段年级: ${parsedPack.grade} (${parsedPack.semester})")
                                        val sourceLabel = when (parsedPack.sourceType) {
                                            com.example.data.PackSourceType.ORIGINAL -> "原创训练包"
                                            com.example.data.PackSourceType.USER_IMPORTED -> "家长导入包"
                                            com.example.data.PackSourceType.USER_PRIVATE -> "私有教材包"
                                            com.example.data.PackSourceType.LICENSED -> "授权内容包"
                                        }
                                        Text("内容来源: $sourceLabel")
                                        Text("描述: ${parsedPack.description}")
                                        Text("总单元数: ${parsedPack.units.size}")
                                        Text("总词条数: ${parsedPack.units.sumOf { it.items.size }}")
                                    }
                                }

                                Text("单元预览:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(parsedPack.units) { unit ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(unit.unitName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                val wPreview = unit.items.joinToString("、") { it.text }
                                                Text(wPreview, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { currentView = ParentView.PRESET_PACKS },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("取消导入")
                                    }

                                    Button(
                                        onClick = {
                                            val finalPack = com.example.data.ContentPackManager.importPackFromJson(context, rawImportJsonToConfirm)
                                            if (finalPack != null) {
                                                allPacksState = com.example.data.ContentPackManager.getAllPacks(context)
                                                Toast.makeText(context, "题库包“${finalPack.name}”已导入至可用列表！", Toast.LENGTH_SHORT).show()
                                                currentView = ParentView.PRESET_PACKS
                                            } else {
                                                Toast.makeText(context, "导入失败，格式有误", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("确认保存导入")
                                    }
                                }
                            }
                        }
                    }
                }

                // ==================== HOLIDAY HOMEWORK MANAGEMENT ====================
                ParentView.HOLIDAY_HOMEWORK_MANAGEMENT -> {
                    HolidayHomeworkManagementView(viewModel = viewModel)
                }
            }
        }
    }

    // DIALOGS:
    // Dialog 1: Add Words from Pool (inside custom level editor)
    if (showAddWordsFromPoolDialog) {
        Dialog(onDismissRequest = { showAddWordsFromPoolDialog = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("从全局题库中点击添加词条", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = poolSearchQuery,
                        onValueChange = { poolSearchQuery = it },
                        placeholder = { Text("输入字、词、或单元筛选") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val filteredPool = allWords.filter { word ->
                        (word.text.contains(poolSearchQuery, ignoreCase = true) ||
                                word.unitName.contains(poolSearchQuery, ignoreCase = true)) &&
                                editingLevelWords.none { it.id == word.id }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredPool) { word ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editingLevelWords = editingLevelWords.toMutableList().apply { add(word) }
                                        Toast.makeText(context, "已添加：${word.text}", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(word.text, fontWeight = FontWeight.Bold)
                                        Text("单元: ${word.unitName}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Icon(Icons.Default.Add, contentDescription = "添加", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showAddWordsFromPoolDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("完成")
                    }
                }
            }
        }
    }

    // Dialog 2: Add single Word to Pool directly
    if (showAddWordToPoolDialog) {
        Dialog(onDismissRequest = { showAddWordToPoolDialog = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("手动追加词条到题库（含听写提示配置）", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = newWordText,
                        onValueChange = { 
                            newWordText = it 
                            if (newWordPromptMode == "CLOZE_CHAR" && it.length >= 2) {
                                val idx = newWordHiddenIndicesStr.toIntOrNull() ?: 0
                                if (idx < it.length) {
                                    val sb = StringBuilder(it)
                                    val charToHide = sb[idx]
                                    sb.setCharAt(idx, '_')
                                    newWordVisiblePrompt = sb.toString()
                                    newWordTtsPrompt = "请写‘$it’的第${idx + 1}个字"
                                    newWordTargetAnswer = charToHide.toString()
                                }
                            }
                        },
                        label = { Text("完整词条内容 (如: 樱桃 / 颤抖)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newWordUnit,
                        onValueChange = { newWordUnit = it },
                        label = { Text("所属单元") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row {
                        listOf("字", "词语", "成语", "句子").forEach { type ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { newWordType = type }) {
                                RadioButton(selected = newWordType == type, onClick = { newWordType = type })
                                Text(type, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Row {
                        listOf("普通", "易错", "BOSS").forEach { diff ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { newWordDifficulty = diff }) {
                                RadioButton(selected = newWordDifficulty == diff, onClick = { newWordDifficulty = diff })
                                Text(diff, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Divider()

                    Text("听写提示模式设置", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "FULL_WORD" to "完整听写 (播报整词, 书写整词)",
                            "CLOZE_CHAR" to "填空听写 (显示带下划线提示, 只书写目标字)",
                            "CONTEXT_CLUE" to "语境/同音提示 (播报特定指导语, 显示视觉提示)"
                        ).forEach { (mode, label) ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { 
                                newWordPromptMode = mode 
                                if (mode == "CLOZE_CHAR" && newWordText.length >= 2) {
                                    val idx = newWordHiddenIndicesStr.toIntOrNull() ?: 0
                                    val sb = StringBuilder(newWordText)
                                    val charToHide = if (idx < sb.length) sb[idx] else sb[0]
                                    if (idx < sb.length) sb.setCharAt(idx, '_')
                                    newWordVisiblePrompt = sb.toString()
                                    newWordTtsPrompt = "请写‘$newWordText’的第${idx + 1}个字"
                                    newWordTargetAnswer = charToHide.toString()
                                } else if (mode == "FULL_WORD") {
                                    newWordVisiblePrompt = ""
                                    newWordTtsPrompt = ""
                                    newWordTargetAnswer = ""
                                }
                            }) {
                                RadioButton(selected = newWordPromptMode == mode, onClick = { 
                                    newWordPromptMode = mode 
                                    if (mode == "CLOZE_CHAR" && newWordText.length >= 2) {
                                        val idx = newWordHiddenIndicesStr.toIntOrNull() ?: 0
                                        val sb = StringBuilder(newWordText)
                                        val charToHide = if (idx < sb.length) sb[idx] else sb[0]
                                        if (idx < sb.length) sb.setCharAt(idx, '_')
                                        newWordVisiblePrompt = sb.toString()
                                        newWordTtsPrompt = "请写‘$newWordText’的第${idx + 1}个字"
                                        newWordTargetAnswer = charToHide.toString()
                                    } else if (mode == "FULL_WORD") {
                                        newWordVisiblePrompt = ""
                                        newWordTtsPrompt = ""
                                        newWordTargetAnswer = ""
                                    }
                                })
                                Text(label, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    if (newWordPromptMode != "FULL_WORD") {
                        if (newWordPromptMode == "CLOZE_CHAR") {
                            OutlinedTextField(
                                value = newWordHiddenIndicesStr,
                                onValueChange = { 
                                    newWordHiddenIndicesStr = it 
                                    val idx = it.toIntOrNull() ?: 0
                                    if (newWordText.length >= 2 && idx < newWordText.length) {
                                        val sb = StringBuilder(newWordText)
                                        val charToHide = sb[idx]
                                        sb.setCharAt(idx, '_')
                                        newWordVisiblePrompt = sb.toString()
                                        newWordTtsPrompt = "请写‘$newWordText’的第${idx + 1}个字"
                                        newWordTargetAnswer = charToHide.toString()
                                    }
                                },
                                label = { Text("挖空位置序号 (0表示第1个字, 1表示第2个字)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        OutlinedTextField(
                            value = newWordVisiblePrompt,
                            onValueChange = { newWordVisiblePrompt = it },
                            label = { Text("孩子界面视觉提示 (如: __ 桃)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newWordTtsPrompt,
                            onValueChange = { newWordTtsPrompt = it },
                            label = { Text("TTS 朗读播报词 (如: 请写樱桃的第一个字)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newWordTargetAnswer,
                            onValueChange = { newWordTargetAnswer = it },
                            label = { Text("判分目标字 (如: 樱)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Live Preview Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📱 学生端听写效果实时预览", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            val effPrompt = if (newWordVisiblePrompt.isNotBlank()) newWordVisiblePrompt else newWordText
                            val effTts = if (newWordTtsPrompt.isNotBlank()) newWordTtsPrompt else newWordText
                            val effTarget = if (newWordTargetAnswer.isNotBlank()) newWordTargetAnswer else newWordText
                            Text("· 屏幕上方提示: $effPrompt", style = MaterialTheme.typography.bodySmall)
                            Text("· 语音播放文本: “$effTts”", style = MaterialTheme.typography.bodySmall)
                            Text("· 田字格数量 & 批改目标: $effTarget (${effTarget.length} 个字)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddWordToPoolDialog = false }) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newWordText.isBlank()) {
                                    Toast.makeText(context, "词条内容不能为空", Toast.LENGTH_SHORT).show()
                                } else {
                                    coroutineScope.launch {
                                        viewModel.addWordToDatabase(
                                            WordItem(
                                                text = newWordText,
                                                type = newWordType,
                                                unitName = newWordUnit,
                                                difficulty = newWordDifficulty,
                                                promptMode = newWordPromptMode,
                                                hiddenIndicesStr = newWordHiddenIndicesStr,
                                                visiblePrompt = newWordVisiblePrompt,
                                                ttsPrompt = newWordTtsPrompt,
                                                targetAnswer = newWordTargetAnswer
                                            )
                                        )
                                        Toast.makeText(context, "已成功添加: $newWordText", Toast.LENGTH_SHORT).show()
                                        newWordText = ""
                                        newWordVisiblePrompt = ""
                                        newWordTtsPrompt = ""
                                        newWordTargetAnswer = ""
                                        showAddWordToPoolDialog = false
                                    }
                                }
                            }
                        ) {
                            Text("确定添加")
                        }
                    }
                }
            }
        }
    }

    if (showBatchPromptDialog) {
        val filteredPool = allWords.filter { word ->
            val wordSource = if (word.unitName.contains(" · ")) word.unitName.substringBefore(" · ") else "默认题库"
            val matchesSource = (selectedSourceTab == "全部" || wordSource == selectedSourceTab)
            val matchesUnit = (selectedUnitTab == "全部" || word.unitName == selectedUnitTab)
            val matchesQuery = word.text.contains(wordPoolSearchQuery, ignoreCase = true) ||
                    word.unitName.contains(wordPoolSearchQuery, ignoreCase = true)
            matchesSource && matchesUnit && matchesQuery
        }

        BatchPromptConfigDialog(
            scopeType = batchScopeType,
            onScopeTypeChange = { batchScopeType = it },
            strategy = batchStrategy,
            onStrategyChange = { batchStrategy = it },
            forceOverride = forceOverrideManual,
            onForceOverrideChange = { forceOverrideManual = it },
            currentFilteredCount = filteredPool.size,
            onExecute = {
                viewModel.batchGeneratePrompts(
                    scopeType = batchScopeType,
                    strategy = batchStrategy,
                    forceOverride = forceOverrideManual,
                    filteredWords = filteredPool,
                    unitNameFilter = selectedUnitTab,
                    packIdFilter = selectedPackForDetail?.id ?: "",
                    onCompleted = { stats, missingWords ->
                        batchResultStats = stats
                        missingContextWordsList = missingWords
                        showBatchPromptDialog = false
                    }
                )
            },
            onDismiss = { showBatchPromptDialog = false }
        )
    }

    batchResultStats?.let { stats ->
        BatchPromptResultDialog(
            stats = stats,
            onViewMissingContext = {
                showMissingContextDialog = true
            },
            onDismiss = {
                batchResultStats = null
            }
        )
    }

    if (showMissingContextDialog) {
        MissingContextWordsDialog(
            words = missingContextWordsList,
            onEditWord = { wordToEdit ->
                promptSettingWord = wordToEdit
                showMissingContextDialog = false
            },
            onDismiss = {
                showMissingContextDialog = false
            }
        )
    }
}

private fun parseChineseNumber(cn: String): Int {
    val map = mapOf(
        '零' to 0, '一' to 1, '二' to 2, '两' to 2, '三' to 3, '四' to 4,
        '五' to 5, '六' to 6, '七' to 7, '八' to 8, '九' to 9, '十' to 10
    )
    if (cn.isEmpty()) return -1
    if (cn.length == 1) return map[cn[0]] ?: -1
    if (cn.length == 2 && cn[0] == '十') {
        val unit = map[cn[1]] ?: 0
        return 10 + unit
    }
    if (cn.length == 2 && cn[1] == '十') {
        val dec = map[cn[0]] ?: 1
        return dec * 10
    }
    if (cn.length == 3 && cn[1] == '十') {
        val dec = map[cn[0]] ?: 1
        val unit = map[cn[2]] ?: 0
        return dec * 10 + unit
    }
    return -1
}

private fun extractUnitNumber(unitName: String): Int {
    val cleanUnit = if (unitName.contains(" · ")) {
        unitName.substringAfter(" · ")
    } else {
        unitName
    }
    // 尝试直接提取阿拉伯数字 (e.g., "第1单元" -> 1)
    val digitRegex = "\\d+".toRegex()
    val digitMatch = digitRegex.find(cleanUnit)
    if (digitMatch != null) {
        return digitMatch.value.toIntOrNull() ?: 9999
    }
    
    // 尝试提取中文数字 (e.g., "第一单元" -> extract "一")
    val cnRegex = "[零一二两三四五六七八九十]+".toRegex()
    val cnMatch = cnRegex.find(cleanUnit)
    if (cnMatch != null) {
        val parsed = parseChineseNumber(cnMatch.value)
        if (parsed != -1) return parsed
    }
    return 9999 // 无法识别的排到最后
}

@Composable
fun PackCardItem(
    pack: com.example.data.ContentPack,
    onDetail: () -> Unit,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetail() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pack.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                
                val sourceLabel = when (pack.sourceType) {
                    com.example.data.PackSourceType.ORIGINAL -> "原创训练包"
                    com.example.data.PackSourceType.USER_IMPORTED -> "家长导入包"
                    com.example.data.PackSourceType.USER_PRIVATE -> "私有教材包"
                    com.example.data.PackSourceType.LICENSED -> "授权内容包"
                }
                Text(
                    text = sourceLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            if (pack.sourceType == com.example.data.PackSourceType.USER_PRIVATE) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    listOf("五年级", "下册", "私有教材包", "本地内置", "第一批内容").forEach { tag ->
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Text(
                text = pack.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "年级学段: ${pack.grade} (${pack.semester})",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "单元数量: ${pack.units.size}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "总词条数: ${pack.units.sumOf { it.items.size }}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusText = if (pack.isInstalled) "已安装" else "未安装"
                val statusColor = if (pack.isInstalled) Color(0xFF008080) else Color.Gray
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDetail,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("查看详情", style = MaterialTheme.typography.labelMedium)
                    }
                    
                    if (pack.isInstalled) {
                        Button(
                            onClick = onAction,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("卸载包", style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Button(
                            onClick = onAction,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("一键安装", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordPromptTag(word: WordItem, onEditPrompt: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val targetAns = word.getEffectiveTargetAnswer()
        val isAnswerVisible = word.visibilityPolicy == "PRACTICE_HINT" ||
                (word.promptMode != "FULL_WORD" && word.visiblePrompt.isNotBlank() && word.visiblePrompt.contains(targetAns))
        val isMissingContext = word.text.length == 1 && word.clueText.isBlank() && word.meaningHint.isBlank()

        val securityTagText = when {
            isAnswerVisible -> "⚠️ 答案可见，仅练习"
            isMissingContext -> "⚡ 缺少语境"
            else -> "🛡️ 测验安全"
        }
        val securityTagBg = when {
            isAnswerVisible -> Color(0xFFFFEBEE)
            isMissingContext -> Color(0xFFFFF8E1)
            else -> Color(0xFFE8F5E9)
        }
        val securityTagFg = when {
            isAnswerVisible -> Color(0xFFC62828)
            isMissingContext -> Color(0xFFF57F17)
            else -> Color(0xFF2E7D32)
        }

        Box(
            modifier = Modifier
                .background(securityTagBg, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(securityTagText, style = MaterialTheme.typography.labelSmall, color = securityTagFg, fontWeight = FontWeight.Bold)
        }

        val tagText = when (word.promptMode) {
            "CLOZE_CHAR" -> "填空写字: ${word.getEffectiveVisiblePrompt()} -> ${word.getEffectiveTargetAnswer()}"
            "CONTEXT_CLUE" -> "语境提示: ${word.getEffectiveVisiblePrompt()} -> ${word.getEffectiveTargetAnswer()}"
            else -> "完整听写"
        }
        val tagColor = when (word.promptMode) {
            "CLOZE_CHAR" -> MaterialTheme.colorScheme.tertiaryContainer
            "CONTEXT_CLUE" -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
        val textColor = when (word.promptMode) {
            "CLOZE_CHAR" -> MaterialTheme.colorScheme.onTertiaryContainer
            "CONTEXT_CLUE" -> MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Box(
            modifier = Modifier
                .background(tagColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(tagText, style = MaterialTheme.typography.labelSmall, color = textColor, fontWeight = FontWeight.Bold)
        }

        AssistChip(
            onClick = onEditPrompt,
            label = { Text("听写提示设置", style = MaterialTheme.typography.labelSmall) },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptSettingDialog(
    word: WordItem,
    onDismiss: () -> Unit,
    onSave: (WordItem) -> Unit
) {
    var promptMode by remember { mutableStateOf(word.promptMode.ifBlank { "FULL_WORD" }) }
    var visibilityPolicy by remember { mutableStateOf(word.visibilityPolicy.ifBlank { "TEST_SAFE" }) }
    val hiddenIndices = remember {
        mutableStateListOf<Int>().apply {
            if (word.hiddenIndicesStr.isNotBlank()) {
                word.hiddenIndicesStr.split(",").mapNotNull { it.trim().toIntOrNull() }.forEach { add(it) }
            }
        }
    }
    var visiblePrompt by remember { mutableStateOf(word.visiblePrompt) }
    var ttsPrompt by remember { mutableStateOf(word.ttsPrompt) }
    var contextText by remember { mutableStateOf(word.contextText) }
    var targetAnswer by remember { mutableStateOf(word.targetAnswer) }

    fun updateAutoFields() {
        val chars = word.text.toCharArray()
        if (promptMode == "FULL_WORD") {
            visiblePrompt = word.text
            targetAnswer = word.text
            ttsPrompt = word.text
        } else if (promptMode == "CLOZE_CHAR") {
            if (hiddenIndices.isEmpty()) {
                hiddenIndices.add(0)
            }
            val promptSb = StringBuilder()
            val answerSb = StringBuilder()
            chars.forEachIndexed { idx, ch ->
                if (hiddenIndices.contains(idx)) {
                    promptSb.append("__")
                    answerSb.append(ch)
                } else {
                    promptSb.append(ch)
                }
            }
            visiblePrompt = promptSb.toString()
            targetAnswer = answerSb.toString()
            if (ttsPrompt.isBlank() || ttsPrompt == word.text) {
                if (hiddenIndices.size == 1 && word.text.length >= 2) {
                    val idx = hiddenIndices[0]
                    val posStr = when (idx) {
                        0 -> "第一个字"
                        1 -> "第二个字"
                        2 -> "第三个字"
                        else -> "第${idx + 1}个字"
                    }
                    ttsPrompt = "请写“${word.text}”的${posStr}"
                } else {
                    ttsPrompt = "请写“${word.text}”中的隐藏汉字"
                }
            }
        } else if (promptMode == "CONTEXT_CLUE") {
            if (contextText.isBlank()) {
                contextText = "${word.text}的${word.text}"
            }
            if (visiblePrompt.isBlank() || visiblePrompt == word.text) {
                visiblePrompt = "${contextText}的 __"
            }
            targetAnswer = word.text
            if (ttsPrompt.isBlank() || ttsPrompt == word.text) {
                ttsPrompt = "请写“${contextText}”的“${word.text}”"
            }
        }
    }

    LaunchedEffect(promptMode, hiddenIndices.size, contextText) {
        updateAutoFields()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("听写提示设置 — ${word.text}", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("完整词条：${word.text}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Text("安全与可见策略：", fontWeight = FontWeight.Bold)
                Column {
                    listOf(
                        "TEST_SAFE" to "🛡️ 测验安全 (听写时遮蔽答案，正式计分)",
                        "PRACTICE_HINT" to "💡 答案可见 (仅用于预习/跟写练习，不计正式成绩)"
                    ).forEach { (pKey, pLabel) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { visibilityPolicy = pKey }
                        ) {
                            RadioButton(
                                selected = visibilityPolicy == pKey,
                                onClick = { visibilityPolicy = pKey }
                            )
                            Text(pLabel, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Text("听写模式选择：", fontWeight = FontWeight.Bold)
                Column {
                    listOf(
                        "FULL_WORD" to "完整听写 (显示提示并要求写出整词)",
                        "CLOZE_CHAR" to "填空写字 (指定位置挖空，只写挖空字)",
                        "CONTEXT_CLUE" to "语境提示 (给定语境短语提示，写目标汉字)"
                    ).forEach { (mKey, mLabel) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    promptMode = mKey
                                    if (mKey == "CLOZE_CHAR" && hiddenIndices.isEmpty()) hiddenIndices.add(0)
                                }
                        ) {
                            RadioButton(
                                selected = promptMode == mKey,
                                onClick = {
                                    promptMode = mKey
                                    if (mKey == "CLOZE_CHAR" && hiddenIndices.isEmpty()) hiddenIndices.add(0)
                                }
                            )
                            Text(mLabel, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                if (promptMode == "CLOZE_CHAR") {
                    Text("字符选择器 (点击方块切换 隐藏/显示)：", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        word.text.forEachIndexed { index, ch ->
                            val isHidden = hiddenIndices.contains(index)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isHidden) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.5.dp, if (isHidden) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .size(52.dp)
                                    .clickable {
                                        if (isHidden) {
                                            if (hiddenIndices.size > 1) hiddenIndices.remove(index)
                                        } else {
                                            hiddenIndices.add(index)
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = ch.toString(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isHidden) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = if (isHidden) "隐藏" else "显示",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isHidden) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (promptMode == "CONTEXT_CLUE") {
                    OutlinedTextField(
                        value = contextText,
                        onValueChange = { contextText = it },
                        label = { Text("语境提示短语 (如: 破晓)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = visiblePrompt,
                    onValueChange = { visiblePrompt = it },
                    label = { Text("屏幕显示提示 (visiblePrompt)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = ttsPrompt,
                    onValueChange = { ttsPrompt = it },
                    label = { Text("语音播报内容 (ttsPrompt)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetAnswer,
                    onValueChange = { targetAnswer = it },
                    label = { Text("最终目标答案 (targetAnswer)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                val tempWord = word.copy(
                    promptMode = promptMode,
                    visibilityPolicy = visibilityPolicy,
                    hiddenIndicesStr = hiddenIndices.joinToString(","),
                    visiblePrompt = visiblePrompt,
                    ttsPrompt = ttsPrompt,
                    contextText = contextText,
                    clueText = contextText.ifBlank { word.clueText },
                    targetAnswer = targetAnswer
                )
                val studentVisiblePrompt = tempWord.getEffectiveVisiblePrompt(isPracticeMode = false)
                val studentSubPrompt = if (visibilityPolicy == "PRACTICE_HINT") {
                    tempWord.getEffectiveSubPrompt(isPracticeMode = true)
                } else {
                    if (promptMode == "CONTEXT_CLUE" || promptMode == "CLOZE_CHAR" || tempWord.getEffectiveTargetAnswer().length == 1) {
                        "请根据语音写出空缺的字"
                    } else {
                        "请根据语音写出词语"
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("👨‍👩‍👧 1. 家长端完整配置 (家长视角)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        Text("• 完整语境: ${contextText.ifEmpty { word.clueText.ifEmpty { "无" } }}", style = MaterialTheme.typography.bodySmall)
                        Text("• 语音播报: ${ttsPrompt.ifEmpty { word.getFormattedAudioPrompt() }}", style = MaterialTheme.typography.bodySmall)
                        Text("• 目标答案: ${targetAnswer.ifEmpty { word.text }}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text("📱 2. 学生端正式关卡看到 (${if (visibilityPolicy == "PRACTICE_HINT") "💡练习跟写模式" else "🛡️测验安全模式"})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelLarge)
                        Text("• 屏幕主提示: $studentVisiblePrompt", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• 屏幕副提示: $studentSubPrompt", style = MaterialTheme.typography.bodySmall)
                        Text("• 书写位数: 本题需要书写 ${targetAnswer.ifEmpty { word.text }.length} 个字", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = word.copy(
                        promptMode = promptMode,
                        visibilityPolicy = visibilityPolicy,
                        hiddenIndicesStr = hiddenIndices.joinToString(","),
                        visiblePrompt = visiblePrompt,
                        ttsPrompt = ttsPrompt,
                        contextText = contextText,
                        targetAnswer = targetAnswer
                    )
                    onSave(updated)
                }
            ) {
                Text("保存设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun BatchPromptConfigDialog(
    scopeType: String,
    onScopeTypeChange: (String) -> Unit,
    strategy: String,
    onStrategyChange: (String) -> Unit,
    forceOverride: Boolean,
    onForceOverrideChange: (Boolean) -> Unit,
    currentFilteredCount: Int,
    onExecute: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("⚡ 批量生成听写提示配置", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("配置应用范围:", fontWeight = FontWeight.Bold)
                Column {
                    listOf(
                        "FILTERED" to "当前筛选范围 (${currentFilteredCount} 个词条)",
                        "UNIT" to "当前选中单元",
                        "ALL" to "全量题库 (包含所有章节与教材包)"
                    ).forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onScopeTypeChange(key) }
                        ) {
                            RadioButton(selected = scopeType == key, onClick = { onScopeTypeChange(key) })
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Divider()

                Text("生成策略:", fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        com.example.util.SmartPromptGenerator.STRATEGY_SMART_RECOMMEND to ("1. 测验安全推荐（默认）" to "不会在听写界面显示答案，适合正式听写计分。"),
                        com.example.util.SmartPromptGenerator.STRATEGY_SINGLE_CHAR_CONTEXT to ("2. 单字安全语境提示" to "单字显示挖空语境，例如“白__ / __夜”。"),
                        com.example.util.SmartPromptGenerator.STRATEGY_TWO_CHAR_CLOZE to ("3. 填空专项练习" to "显示部分词语，让孩子补空，适合练习，不作为默认正式测验。"),
                        com.example.util.SmartPromptGenerator.STRATEGY_FILL_MEANING_ONLY to ("4. 字词解释补全" to "只补充解释，不改变听写模式。"),
                        com.example.util.SmartPromptGenerator.STRATEGY_RESET_FULL_WORD to ("5. 恢复完整听写" to "所有词条恢复为只听语音完整书写。"),
                        com.example.util.SmartPromptGenerator.STRATEGY_PRACTICE_HINT_VISIBLE to ("6. 答案可见练习模式" to "会显示完整答案或强提示，仅用于预习跟写，不计正式成绩。")
                    ).forEach { (key, titleAndDesc) ->
                        val (title, desc) = titleAndDesc
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStrategyChange(key) }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(selected = strategy == key, onClick = { onStrategyChange(key) })
                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Divider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("覆盖手改提示", fontWeight = FontWeight.Bold)
                        Text("关闭时保留家长手动编辑过的提示", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(checked = forceOverride, onCheckedChange = onForceOverrideChange)
                }
            }
        },
        confirmButton = {
            Button(onClick = onExecute) {
                Text("立即应用配置")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun BatchPromptResultDialog(
    stats: BatchPromptStats,
    onViewMissingContext: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🎉 批量提示配置完成", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("• 处理总词条数: ${stats.totalProcessed} 题", fontWeight = FontWeight.Bold)
                Text("• 生成语境短语提示: ${stats.contextClueCount} 题", color = MaterialTheme.colorScheme.primary)
                Text("• 生成填空听写提示: ${stats.clozeCharCount} 题", color = MaterialTheme.colorScheme.secondary)
                Text("• 补充字词解释: ${stats.meaningSupplementedCount} 题", color = Color(0xFF008080))
                if (stats.manualSkippedCount > 0) {
                    Text("• 保留家长手工配置: ${stats.manualSkippedCount} 题", color = Color.Gray)
                }
                if (stats.missingContextCount > 0) {
                    Text("• 缺少语境回退完整听写: ${stats.missingContextCount} 题", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (stats.missingContextCount > 0) {
                    OutlinedButton(onClick = onViewMissingContext) {
                        Text("查看缺少语境的词条")
                    }
                }
                Button(onClick = onDismiss) {
                    Text("完成并返回")
                }
            }
        }
    )
}

@Composable
fun MissingContextWordsDialog(
    words: List<WordItem>,
    onEditWord: (WordItem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("⚠️ 缺少内置语境短语的单字 (${words.size} 个)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("以下单字在内置词典中尚未包含语境短语，目前自动回退为“完整听写”。您可以点击手动补充语境：", style = MaterialTheme.typography.bodySmall)
                words.forEach { w ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onEditWord(w)
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(w.text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("单元: ${w.unitName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Text("点击配置语境 ➔", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
fun HolidayHomeworkManagementView(
    viewModel: GameViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val stats by viewModel.userStats.collectAsState()
    val packs by viewModel.allHolidayPacks.collectAsState()
    val tasks by viewModel.allHolidayTasks.collectAsState()
    val workSessions by viewModel.allHolidayWorkSessions.collectAsState()

    var mathTier by remember(stats) { mutableStateOf(stats?.mathGradeTier ?: "ABOVE_90") }
    var englishTier by remember(stats) { mutableStateOf(stats?.englishGradeTier ?: "ABOVE_88") }

    val todayStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
    val todaySessions = remember(workSessions, todayStr) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        workSessions.filter { sdf.format(java.util.Date(it.createdAt)) == todayStr }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Score Tiers Config
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("1. 五年级期末成绩分段设置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("系统将根据成绩自动下发或隐去对应档位的附加习题：", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Math Tier
                    Text("📐 数学学科档位", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                mathTier = "ABOVE_90"
                                viewModel.updateGradeTiers(mathTier, englishTier)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = mathTier == "ABOVE_90", onClick = {
                            mathTier = "ABOVE_90"
                            viewModel.updateGradeTiers(mathTier, englishTier)
                        })
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text("90分及以上", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("附加《举一反三》30讲；免做《黄冈小状元》预习", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                mathTier = "BELOW_90"
                                viewModel.updateGradeTiers(mathTier, englishTier)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = mathTier == "BELOW_90", onClick = {
                            mathTier = "BELOW_90"
                            viewModel.updateGradeTiers(mathTier, englishTier)
                        })
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text("90分以下", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("不做《举一反三》；做《黄冈小状元》六上预习", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // English Tier
                    Text("🔤 英语学科档位", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                englishTier = "ABOVE_88"
                                viewModel.updateGradeTiers(mathTier, englishTier)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = englishTier == "ABOVE_88", onClick = {
                            englishTier = "ABOVE_88"
                            viewModel.updateGradeTiers(mathTier, englishTier)
                        })
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text("88分及以上", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("抄写五下单词+听力+60篇阅读", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                englishTier = "BELOW_88"
                                viewModel.updateGradeTiers(mathTier, englishTier)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = englishTier == "BELOW_88", onClick = {
                            englishTier = "BELOW_88"
                            viewModel.updateGradeTiers(mathTier, englishTier)
                        })
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text("88分以下", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("抄写五上单词+听力+30篇阅读", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }

        // Section 2: Pack Status & Repair
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("2. 私有作业包状态与修复", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    val activePack = packs.firstOrNull()
                    if (activePack != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(activePack.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("${activePack.subtitle} | 标识: ${activePack.packId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Text(
                                if (activePack.isInstalled) "已安装" else "未安装",
                                color = if (activePack.isInstalled) Color(0xFF008080) else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.repairHolidayPack(activePack.packId)
                                        Toast.makeText(context, "暑假作业包修复更新成功！", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("一键修复/更新")
                            }

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.installHolidayPack(activePack.packId)
                                        Toast.makeText(context, "已重新安装作业包基础数据！", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("重置数据")
                            }
                        }
                    } else {
                        Text("当前未检测到暑假作业包，点击下方按钮初始化内置作业包：")
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.installHolidayPack("summer_homework_grade5_to_6_2026_private_v1")
                                    Toast.makeText(context, "初始化安装成功！", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("安装五升六暑假作业包")
                        }
                    }
                }
            }
        }

        // Section 3: Today's Work Sessions & Parent Review
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("3. 今日作业执行与提交审核 (${todaySessions.size}项)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        Text("日期: $todayStr", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    if (todaySessions.isEmpty()) {
                        Text("今日孩子尚未提交详细作业日志。", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        todaySessions.forEach { session ->
                            val linkedTask = tasks.find { it.id == session.taskId }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${(session.titleInput ?: "").ifBlank { linkedTask?.title ?: "作业打卡" }}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (session.durationMinutes > 0) {
                                            Text("⏱️ 用时: ${session.durationMinutes} 分钟", style = MaterialTheme.typography.bodySmall)
                                        }
                                        if (!session.note.isNullOrBlank()) {
                                            Text("📝 细节: ${session.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        if (session.parentConfirmed) {
                                            Text("✅ 已复核签字", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                                        } else {
                                            Button(
                                                onClick = { viewModel.toggleWorkSessionParentConfirmed(session) },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("确认签字", fontSize = 11.sp)
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

        // Section 4: Student Task Sign & Confirmation
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("4. 每日任务进度与家长控制", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            var signedCount = 0
                            tasks.forEach { t ->
                                if (t.completedCount > 0 && !t.isParentConfirmed) {
                                    viewModel.toggleTaskParentConfirmed(t)
                                    signedCount++
                                }
                            }
                            Toast.makeText(context, "已为 $signedCount 项已打卡任务一键签字！", Toast.LENGTH_SHORT).show()
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("一键全选签字", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        items(tasks) { task ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (task.isParentConfirmed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, if (task.isParentConfirmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "[${task.subject}]",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "孩子进度: ${task.completedCount} / ${task.totalCount} | ${if (task.completedCount >= task.totalCount) "✅ 已完成" else "⏳ 进行中"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (task.completedCount >= task.totalCount) Color(0xFF008080) else Color.Gray
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (task.completedCount > 0) {
                                TextButton(
                                    onClick = { viewModel.cancelTodayHolidayCheckIn(task.id) },
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text("撤销打卡", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                            Checkbox(
                                checked = task.isParentConfirmed,
                                onCheckedChange = { _ ->
                                    viewModel.toggleTaskParentConfirmed(task)
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Section 5: Recitation Confirmation
        item {
            val recitations by viewModel.allRecitationRecords.collectAsState()
            val pendingRecitations = recitations.filter { it.parentStatus == "PENDING" }
            
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text("5. 背诵录音待确认 (${pendingRecitations.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                if (pendingRecitations.isEmpty()) {
                    Text("当前没有待确认的背诵录音。", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    pendingRecitations.forEach { rec ->
                        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("素材: ${rec.materialTitle}", fontWeight = FontWeight.Bold)
                                Text("模式: ${if (rec.mode == "MEMORIZE") "脱稿背诵" else "朗读练习"}")
                                
                                val audioPlayer = remember(rec.audioPath) { com.example.util.AudioRecorderHelper(context).apply { 
                                    // Hack to set currentFilePath
                                    val field = this::class.java.getDeclaredField("currentFilePath")
                                    field.isAccessible = true
                                    field.set(this, rec.audioPath)
                                }}
                                var isPlaying by remember { mutableStateOf(false) }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                                    Button(onClick = {
                                        if (isPlaying) {
                                            audioPlayer.stopPlaying()
                                            isPlaying = false
                                        } else {
                                            isPlaying = true
                                            audioPlayer.startPlaying { isPlaying = false }
                                        }
                                    }) { Text(if (isPlaying) "停止播放" else "播放录音") }
                                    
                                    Button(onClick = { viewModel.updateRecitationParentStatus(rec.id, "PASSED") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("通过") }
                                    Button(onClick = { viewModel.updateRecitationParentStatus(rec.id, "NEED_RETRY") }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("重背") }
                                }
                                
                                DisposableEffect(rec.audioPath) {
                                    onDispose { audioPlayer.release() }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
