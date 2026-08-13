@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.english

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.*
import com.example.viewmodel.EnglishAutoDictationViewModel
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.util.english.EnglishTTSHelper

// Custom wrapping FlowRow layout for responsive sizing
@Composable
fun FlowRowFallback_Unused(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val horizontalSpacingPx = horizontalSpacing.roundToPx()
        val verticalSpacingPx = verticalSpacing.roundToPx()
        
        val rows = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        val rowHeights = mutableListOf<Int>()
        val rowWidths = mutableListOf<Int>()
        
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0
        
        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            if (currentRowWidth + placeable.width + (if (currentRow.isEmpty()) 0 else horizontalSpacingPx) > constraints.maxWidth) {
                rows.add(currentRow)
                rowHeights.add(currentRowHeight)
                rowWidths.add(currentRowWidth)
                
                currentRow = mutableListOf(placeable)
                currentRowWidth = placeable.width
                currentRowHeight = placeable.height
            } else {
                currentRowWidth += placeable.width + (if (currentRow.isEmpty()) 0 else horizontalSpacingPx)
                currentRowHeight = maxOf(currentRowHeight, placeable.height)
                currentRow.add(placeable)
            }
        }
        
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowHeights.add(currentRowHeight)
            rowWidths.add(currentRowWidth)
        }
        
        val totalWidth = constraints.maxWidth
        val totalHeight = rowHeights.sum() + (rowHeights.size - 1).coerceAtLeast(0) * verticalSpacingPx
        
        layout(totalWidth, totalHeight) {
            var y = 0
            rows.forEachIndexed { rowIndex, row ->
                val rowWidth = rowWidths[rowIndex]
                var x = (totalWidth - rowWidth) / 2
                row.forEach { placeable ->
                    placeable.place(x, y)
                    x += placeable.width + horizontalSpacingPx
                }
                y += rowHeights[rowIndex] + verticalSpacingPx
            }
        }
    }
}

enum class ChallengeScreenState {
    OVERVIEW,
    STAGE1, // 听英文选中文
    STAGE2, // 听中文选英文
    STAGE3, // 看中文拼英文
    STAGE4, // 听英文默写英文
    RESULT
}

enum class SpellingState {
    EDITING,
    CHECKING,
    INCORRECT_FIRST,
    INCORRECT_REVEALED,
    CORRECT
}

data class WordChallengeState(
    val word: EnglishWord,
    var meaningCorrect: Boolean = true,
    var reverseCorrect: Boolean = true,
    var spellingCorrect: Boolean = true,
    var dictationCorrect: Boolean = true,
    var retryCount: Int = 0,
    var hintUsed: Boolean = false,
    var answerRevealed: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitChallengeScreen(
    unit: EnglishUnit,
    context: Context,
    ttsHelper: EnglishTTSHelper,
    viewModel: com.example.viewmodel.GameViewModel? = null,
    onBack: () -> Unit,
    onComplete: (Int) -> Unit
) {
    val playerProfile by viewModel?.playerProfile?.collectAsStateWithLifecycle(initialValue = null) ?: remember { mutableStateOf(null) }
    val equippedBrushConfig by viewModel?.equippedBrushConfig?.collectAsStateWithLifecycle(initialValue = null) ?: remember { mutableStateOf(null) }
    val equippedBrushId = playerProfile?.equippedBrushId ?: "default_black"
    val equippedBrushStyle = remember(equippedBrushId) { com.example.ui.BrushStyle.getBrushById(equippedBrushId) }
    // We build the challenge pool dynamically using EnglishChallengePoolBuilder
    val challengeItems = remember(unit) { EnglishChallengePoolBuilder.buildWordPool(unit) }
    val classAItems = remember(challengeItems) {
        challengeItems.filter { it.participation == EnglishChallengeParticipation.CORE_REQUIRED }
    }
    val classBItems = remember(challengeItems) {
        challengeItems.filter { it.participation == EnglishChallengeParticipation.EXTENDED_OPTIONAL }
    }
    val allWords = remember(challengeItems) { challengeItems.map { it.word } }
    val classAWords = remember(classAItems) { classAItems.map { it.word } }
    val classBWords = remember(classBItems) { classBItems.map { it.word } }

    var currentState by remember { mutableStateOf(ChallengeScreenState.OVERVIEW) }
    
    // Active challenge words state (can be subset to weak words on retry)
    var activeWords by remember { mutableStateOf<List<EnglishWord>>(allWords) }
    var wordStates by remember { mutableStateOf<Map<String, WordChallengeState>>(emptyMap()) }
    var isRetryOnlyWeak by remember { mutableStateOf(false) }

    // Init word states
    fun initChallenge(wordsToTest: List<EnglishWord>) {
        activeWords = wordsToTest
        wordStates = wordsToTest.associate { it.wordId to WordChallengeState(it) }
    }

    LaunchedEffect(unit) {
        initChallenge(allWords)
    }

    // Navigation and sub-stage step indices
    var currentWordIndex by remember { mutableStateOf(0) }

    when (currentState) {
        ChallengeScreenState.OVERVIEW -> {
            UnitChallengeOverviewScreen(
                unit = unit,
                classA = classAWords,
                classB = classBWords,
                context = context,
                ttsHelper = ttsHelper,
                onBack = onBack,
                onStartChallenge = {
                    initChallenge(allWords)
                    isRetryOnlyWeak = false
                    currentWordIndex = 0
                    currentState = ChallengeScreenState.STAGE1
                }
            )
        }
        ChallengeScreenState.STAGE1 -> {
            // Stage 1: Listen to English, choose Chinese (All active words)
            val currentWord = activeWords.getOrNull(currentWordIndex)
            if (currentWord == null) {
                // Done with Stage 1, move to Stage 2
                currentWordIndex = 0
                currentState = ChallengeScreenState.STAGE2
            } else {
                Stage1ListenChooseChineseScreen(
                    word = currentWord,
                    allWordsPool = allWords,
                    ttsHelper = ttsHelper,
                    onCorrect = { wasCorrectFirstTry ->
                        wordStates[currentWord.wordId]?.meaningCorrect = wasCorrectFirstTry
                        if (currentWordIndex < activeWords.size - 1) {
                            currentWordIndex++
                        } else {
                            currentWordIndex = 0
                            currentState = ChallengeScreenState.STAGE2
                        }
                    },
                    onBack = { currentState = ChallengeScreenState.OVERVIEW }
                )
            }
        }
        ChallengeScreenState.STAGE2 -> {
            // Stage 2: Listen to Chinese, choose English (All active words)
            val currentWord = activeWords.getOrNull(currentWordIndex)
            if (currentWord == null) {
                currentWordIndex = 0
                currentState = ChallengeScreenState.STAGE3
            } else {
                Stage2ListenChooseEnglishScreen(
                    word = currentWord,
                    allWordsPool = allWords,
                    ttsHelper = ttsHelper,
                    onCorrect = { wasCorrectFirstTry ->
                        wordStates[currentWord.wordId]?.reverseCorrect = wasCorrectFirstTry
                        if (currentWordIndex < activeWords.size - 1) {
                            currentWordIndex++
                        } else {
                            currentWordIndex = 0
                            currentState = ChallengeScreenState.STAGE3
                        }
                    },
                    onBack = { currentState = ChallengeScreenState.OVERVIEW }
                )
            }
        }
        ChallengeScreenState.STAGE3 -> {
            // Stage 3: Look at Chinese, Spell English (Class A core words FIRST, then Class B extended words!)
            val activeCore = activeWords.filter { w -> classAWords.any { it.wordId == w.wordId } }
            val activeExt = activeWords.filter { w -> classBWords.any { it.wordId == w.wordId } }
            val spellingPool = activeCore + activeExt
            val currentWord = spellingPool.getOrNull(currentWordIndex)

            if (currentWord == null) {
                currentWordIndex = 0
                currentState = ChallengeScreenState.STAGE4
            } else {
                val isExtended = classBWords.any { it.wordId == currentWord.wordId }
                val groupTitle = if (!isExtended) {
                    val coreIdx = activeCore.indexOf(currentWord) + 1
                    "核心词拼写 ($coreIdx/${activeCore.size.coerceAtLeast(1)})"
                } else {
                    val extIdx = activeExt.indexOf(currentWord) + 1
                    "拓展词拼写 ($extIdx/${activeExt.size.coerceAtLeast(1)})"
                }

                Stage3LookSpellEnglishScreen(
                    word = currentWord,
                    ttsHelper = ttsHelper,
                    groupTitle = groupTitle,
                    isExtended = isExtended,
                    onCorrect = { spellingCorrect, retryCount, hintUsed, answerRevealed ->
                        wordStates[currentWord.wordId]?.let {
                            it.spellingCorrect = spellingCorrect
                            it.retryCount += retryCount
                            if (hintUsed) it.hintUsed = true
                            if (answerRevealed) it.answerRevealed = true
                        }
                        if (currentWordIndex < spellingPool.size - 1) {
                            currentWordIndex++
                        } else {
                            currentWordIndex = 0
                            currentState = ChallengeScreenState.STAGE4
                        }
                    },
                    onBack = { currentState = ChallengeScreenState.OVERVIEW }
                )
            }
        }
        ChallengeScreenState.STAGE4 -> {
            // Stage 4: Dictate (Class A core words FIRST, then Class B extended words!)
            val activeCore = activeWords.filter { w -> classAWords.any { it.wordId == w.wordId } }
            val activeExt = activeWords.filter { w -> classBWords.any { it.wordId == w.wordId } }
            val dictationPool = activeCore + activeExt
            val currentWord = dictationPool.getOrNull(currentWordIndex)

            if (currentWord == null) {
                currentState = ChallengeScreenState.RESULT
            } else {
                val isExtended = classBWords.any { it.wordId == currentWord.wordId }
                val groupTitle = if (!isExtended) {
                    val coreIdx = activeCore.indexOf(currentWord) + 1
                    "核心词听写 ($coreIdx/${activeCore.size.coerceAtLeast(1)})"
                } else {
                    val extIdx = activeExt.indexOf(currentWord) + 1
                    "拓展词听写 ($extIdx/${activeExt.size.coerceAtLeast(1)})"
                }

                Stage4DictationScreen(
                    word = currentWord,
                    ttsHelper = ttsHelper,
                    equippedBrushStyle = equippedBrushStyle,
                    equippedBrushConfig = equippedBrushConfig,
                    groupTitle = groupTitle,
                    isExtended = isExtended,
                    onCorrect = { dictationCorrect, retryCount, hintUsed, answerRevealed ->
                        wordStates[currentWord.wordId]?.let {
                            it.dictationCorrect = dictationCorrect
                            it.retryCount += retryCount
                            if (hintUsed) it.hintUsed = true
                            if (answerRevealed) it.answerRevealed = true
                        }
                        if (currentWordIndex < dictationPool.size - 1) {
                            currentWordIndex++
                        } else {
                            currentState = ChallengeScreenState.RESULT
                        }
                    },
                    onBack = { currentState = ChallengeScreenState.OVERVIEW }
                )
            }
        }
        ChallengeScreenState.RESULT -> {
            ChallengeResultScreen(
                unit = unit,
                wordStates = wordStates,
                classA = classAWords,
                classB = classBWords,
                context = context,
                onCompleteAndExit = { coins ->
                    var corePassedAll = true
                    wordStates.forEach { (wordId, state) ->
                        val isClassA = classAWords.any { it.wordId == wordId }
                        val isMastered = state.meaningCorrect && state.reverseCorrect && state.spellingCorrect && state.dictationCorrect && !state.answerRevealed
                        
                        if (isClassA) {
                            if (!isMastered) {
                                corePassedAll = false
                            }
                            val statusStr = if (isMastered) "MASTERED" else "NEEDS_REVIEW"
                            EnglishProgressManager.saveWordMastery(context, wordId, statusStr)
                            EnglishProgressManager.saveWordDetailStats(context, wordId, WordDetailStats(
                                meaningCorrect = state.meaningCorrect,
                                reverseCorrect = state.reverseCorrect,
                                spellingCorrect = state.spellingCorrect,
                                dictationCorrect = state.dictationCorrect
                            ))
                        } else {
                            // Class B word: Save extended practice details without altering core mastery or requirement level!
                            EnglishProgressManager.saveExtendedPracticeDetailStats(context, wordId, WordDetailStats(
                                meaningCorrect = state.meaningCorrect,
                                reverseCorrect = state.reverseCorrect,
                                spellingCorrect = state.spellingCorrect,
                                dictationCorrect = state.dictationCorrect
                            ))
                        }
                    }

                    if (corePassedAll) {
                        EnglishProgressManager.completeUnit(context, unit.unitId)
                    }
                    EnglishProgressManager.saveExtendedChallengeCompleted(context, unit.unitId)
                    onComplete(coins)
                },
                onRetryWeak = { weakWords ->
                    isRetryOnlyWeak = true
                    initChallenge(weakWords)
                    currentWordIndex = 0
                    currentState = ChallengeScreenState.STAGE1
                }
            )
        }
    }
}

// ==========================================
// SCREEN 1: UnitChallengeOverviewScreen
// ==========================================
@Composable
fun UnitChallengeOverviewScreen(
    unit: EnglishUnit,
    classA: List<EnglishWord>,
    classB: List<EnglishWord>,
    context: Context,
    ttsHelper: EnglishTTSHelper,
    onBack: () -> Unit,
    onStartChallenge: () -> Unit
) {
    val dictationViewModel: EnglishAutoDictationViewModel = viewModel()
    val dictationUiState by dictationViewModel.uiState.collectAsStateWithLifecycle()
    val settings by dictationViewModel.settings.collectAsStateWithLifecycle()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showPlayerSheet by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        dictationViewModel.loadSettings(context)
        onDispose {
            dictationViewModel.stopAndEnd()
        }
    }

    if (showSettingsSheet) {
        EnglishAutoDictationSettingsSheet(
            initialSettings = settings,
            hasExtendedWords = classB.isNotEmpty(),
            onDismissRequest = { showSettingsSheet = false },
            onStartDictation = { newSettings ->
                showSettingsSheet = false
                showPlayerSheet = true
                dictationViewModel.startDictation(context, unit, newSettings, ttsHelper)
            }
        )
    }

    if (showPlayerSheet) {
        EnglishAutoDictationPlayerSheet(
            uiState = dictationUiState,
            onPause = { dictationViewModel.pause() },
            onResume = { dictationViewModel.resume() },
            onReplayCurrent = { dictationViewModel.replayCurrent() },
            onSkipNext = { dictationViewModel.skipNext() },
            onRestartSession = {
                dictationViewModel.startDictation(context, unit, settings, ttsHelper)
            },
            onEndDictation = {
                dictationViewModel.stopAndEnd()
                showPlayerSheet = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${unit.title} 单词终极挑战", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回大厅", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFEC4899))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = unit.title,
                            color = Color(0xFFEC4899),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "本单元终极词汇挑战专为检验词汇拼写、发音和多维识记度设计。你需要完成 听英文识中文、听中文识英文、看中文拼英文、听音默默写 四个深度维度的挑战，形成完美词汇掌握闭环！",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "📚 核心认读词汇 (A类 - 需完成听、认读、拼写和听写挑战)",
                    color = Color(0xFF00E5FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(classA) { word ->
                WordOverviewRow(word, context, ttsHelper, isCore = true)
            }

            if (classB.isNotEmpty()) {
                item {
                    Text(
                        text = "🗣️ 听说拓展词汇 (B类 - 教材以听说为主，本挑战增加拼写和听写练习)",
                        color = Color(0xFFF472B6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(classB) { word ->
                    WordOverviewRow(word, context, ttsHelper, isCore = false)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 本次挑战包含核心词和拓展听说词。",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 1. Secondary Button: 一键报听写 (Auto Dictation)
                OutlinedButton(
                    onClick = {
                        dictationViewModel.loadSettings(context)
                        showSettingsSheet = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("auto_dictation_entry_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF00E5FF))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("🎧 一键报听写", color = Color(0xFF00E5FF), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("自动连续播放本单元单词 (纸笔自测)", color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Primary Button: 开始终极挑战
                Button(
                    onClick = onStartChallenge,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_ultimate_challenge_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "开始", tint = Color.White)
                        Text("开始终极挑战", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WordOverviewRow(
    word: EnglishWord,
    context: Context,
    ttsHelper: EnglishTTSHelper,
    isCore: Boolean
) {
    val mastery = remember(word.wordId) { EnglishProgressManager.getWordMastery(context, word.wordId) }
    val badgeColor = when (mastery) {
        "MASTERED" -> Color(0xFF10B981)
        "NEEDS_REVIEW" -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B)
    }
    val badgeText = when (mastery) {
        "MASTERED" -> "已掌握"
        "NEEDS_REVIEW" -> "待复习"
        else -> "学习中"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = { ttsHelper.speak(word.spelling) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFEC4899).copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "发音", tint = Color(0xFFEC4899), modifier = Modifier.size(18.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = word.spelling,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = word.phonetic,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = word.chineseMeaning,
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = badgeText, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isCore) "核心拼写" else "听说词",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}


// ==========================================
// STAGE 1: Stage1ListenChooseChineseScreen
// ==========================================
@Composable
fun Stage1ListenChooseChineseScreen(
    word: EnglishWord,
    allWordsPool: List<EnglishWord>,
    ttsHelper: EnglishTTSHelper,
    onCorrect: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var wasIncorrectPressed by remember { mutableStateOf(false) }
    val options = remember(word.wordId) {
        val wrongChoices = allWordsPool.filter { it.wordId != word.wordId }.shuffled().take(3)
        (wrongChoices + word).shuffled()
    }
    var selectedOption by remember { mutableStateOf<EnglishWord?>(null) }
    var isAnsweredCorrectly by remember { mutableStateOf(false) }

    LaunchedEffect(word.wordId) {
        selectedOption = null
        isAnsweredCorrectly = false
        wasIncorrectPressed = false
        ttsHelper.speak(word.spelling)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("第一阶段：听义辨词", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "退出", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("听英文，选择正确的中文释义", color = Color(0xFF94A3B8), fontSize = 14.sp)
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEC4899).copy(alpha = 0.15f))
                    .clickable { ttsHelper.speak(word.spelling) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "播放", tint = Color(0xFFEC4899), modifier = Modifier.size(44.dp))
            }
            Text("点击圆圈再次发音", color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                options.forEach { opt ->
                    val isSelected = opt == selectedOption
                    val isCorrect = opt.wordId == word.wordId
                    val itemBgColor = when {
                        isSelected && isCorrect -> Color(0xFF10B981)
                        isSelected && !isCorrect -> Color(0xFFEF4444)
                        else -> Color(0xFF1E293B)
                    }

                    Card(
                        onClick = {
                            if (!isAnsweredCorrectly) {
                                selectedOption = opt
                                if (isCorrect) {
                                    isAnsweredCorrectly = true
                                    ttsHelper.speak(word.spelling)
                                } else {
                                    wasIncorrectPressed = true
                                }
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = itemBgColor),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt.chineseMeaning,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isAnsweredCorrectly) {
                Button(
                    onClick = { onCorrect(!wasIncorrectPressed) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("继续", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}


// ==========================================
// STAGE 2: Stage2ListenChooseEnglishScreen
// ==========================================
@Composable
fun Stage2ListenChooseEnglishScreen(
    word: EnglishWord,
    allWordsPool: List<EnglishWord>,
    ttsHelper: EnglishTTSHelper,
    onCorrect: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var wasIncorrectPressed by remember { mutableStateOf(false) }
    val options = remember(word.wordId) {
        val wrongChoices = allWordsPool.filter { it.wordId != word.wordId }.shuffled().take(3)
        (wrongChoices + word).shuffled()
    }
    var selectedOption by remember { mutableStateOf<EnglishWord?>(null) }
    var isAnsweredCorrectly by remember { mutableStateOf(false) }

    LaunchedEffect(word.wordId) {
        selectedOption = null
        isAnsweredCorrectly = false
        wasIncorrectPressed = false
        ttsHelper.speak(word.chineseMeaning)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("第二阶段：中文释词", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "退出", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("听中文发音，选择正确的英文单词", color = Color(0xFF94A3B8), fontSize = 14.sp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .clickable { ttsHelper.speak(word.chineseMeaning) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = "播放", tint = Color(0xFF00E5FF))
                    Text(word.chineseMeaning, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text("点击文字框再次播音", color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                options.forEach { opt ->
                    val isSelected = opt == selectedOption
                    val isCorrect = opt.wordId == word.wordId
                    val itemBgColor = when {
                        isSelected && isCorrect -> Color(0xFF10B981)
                        isSelected && !isCorrect -> Color(0xFFEF4444)
                        else -> Color(0xFF1E293B)
                    }

                    Card(
                        onClick = {
                            if (!isAnsweredCorrectly) {
                                selectedOption = opt
                                if (isCorrect) {
                                    isAnsweredCorrectly = true
                                    ttsHelper.speak(word.spelling)
                                } else {
                                    wasIncorrectPressed = true
                                }
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = itemBgColor),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opt.spelling,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isAnsweredCorrectly) {
                Button(
                    onClick = { onCorrect(!wasIncorrectPressed) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("继续", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}


// ==========================================
// STAGE 3: Stage3LookSpellEnglishScreen
// ==========================================
@Composable
fun Stage3LookSpellEnglishScreen(
    word: EnglishWord,
    ttsHelper: EnglishTTSHelper,
    groupTitle: String = "第三阶段：拼写拼图",
    isExtended: Boolean = false,
    onCorrect: (spellingCorrect: Boolean, retryCount: Int, hintUsed: Boolean, answerRevealed: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val targetWord = word.spelling
    var clickedIndicesOrder by remember { mutableStateOf<List<Int>>(emptyList()) }
    var bubbleLetters by remember { mutableStateOf<List<Char>>(emptyList()) }
    
    // Spelling States
    var spellingState by remember { mutableStateOf(SpellingState.EDITING) }
    var retryCount by remember { mutableStateOf(0) }
    var hintUsed by remember { mutableStateOf(false) }
    var answerRevealed by remember { mutableStateOf(false) }
    var initialCorrectFirstTry by remember { mutableStateOf(true) }

    fun resetWordPuzzle() {
        clickedIndicesOrder = emptyList()
        spellingState = SpellingState.EDITING
    }

    LaunchedEffect(word.wordId) {
        resetWordPuzzle()
        retryCount = 0
        hintUsed = false
        answerRevealed = false
        initialCorrectFirstTry = true
        bubbleLetters = targetWord.filter { !it.isWhitespace() }.toList().shuffled()
    }

    // Reconstruct the currently assembled flat string
    val assembledLettersFlat = clickedIndicesOrder.map { bubbleLetters[it] }.joinToString("")
    
    // Format full assembled text inserting correct whitespaces
    fun getAssembledWithSpaces(): String {
        val sb = java.lang.StringBuilder()
        var flatIdx = 0
        for (char in targetWord) {
            if (char.isWhitespace()) {
                sb.append(' ')
            } else {
                if (flatIdx < assembledLettersFlat.length) {
                    sb.append(assembledLettersFlat[flatIdx])
                    flatIdx++
                } else {
                    break
                }
            }
        }
        return sb.toString()
    }

    val finalAssembledWord = getAssembledWithSpaces()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(groupTitle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        if (isExtended) {
                            Text("听说拓展词 · 增加拼写练习 (不影响核心通关)", color = Color(0xFFF472B6), fontSize = 11.sp)
                        } else {
                            Text("核心词 · 必考拼写", color = Color(0xFF00E5FF), fontSize = 11.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "退出", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("看中文，组装正确的英文单词", color = Color(0xFF94A3B8), fontSize = 14.sp)

            // Chinese Meaning Banner
            Box(
                modifier = Modifier
                    .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = word.chineseMeaning,
                    color = Color(0xFF34D399),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Assembled interactive slots area using unified EnglishWordAnswerBoard
            EnglishWordAnswerBoard(
                targetWord = targetWord,
                assembledLettersFlat = assembledLettersFlat,
                onSlotClick = { flatIndex ->
                    if (flatIndex < clickedIndicesOrder.size) {
                        clickedIndicesOrder = clickedIndicesOrder.filterIndexed { idx, _ -> idx != flatIndex }
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Jumbled Letter Bubbles Pool (Flow Layout wraps without compression!)
            CenteredWrapRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalSpacing = 10.dp,
                verticalSpacing = 10.dp
            ) {
                bubbleLetters.forEachIndexed { index, char ->
                    val isClicked = clickedIndicesOrder.contains(index)
                    
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isClicked) Color(0xFF1E293B).copy(alpha = 0.4f) else Color(0xFFEC4899))
                            .border(
                                width = 1.dp,
                                color = if (isClicked) Color(0xFF334155) else Color(0xFFF472B6),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = !isClicked && spellingState != SpellingState.CORRECT) {
                                clickedIndicesOrder = clickedIndicesOrder + index
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            color = if (isClicked) Color(0xFF475569) else Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Panel (Delete/Check/Next)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        resetWordPuzzle()
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0xFF475569), CircleShape),
                    enabled = spellingState != SpellingState.CORRECT
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "清空", tint = Color.White)
                }

                IconButton(
                    onClick = {
                        if (clickedIndicesOrder.isNotEmpty()) {
                            clickedIndicesOrder = clickedIndicesOrder.dropLast(1)
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0xFF475569), CircleShape),
                    enabled = spellingState != SpellingState.CORRECT
                ) {
                    Icon(Icons.Filled.Undo, contentDescription = "撤销一步", tint = Color.White)
                }

                if (spendingStateCheck(spellingState)) {
                    Button(
                        onClick = {
                            val userNorm = finalAssembledWord.trim().lowercase().replace("\\s+".toRegex(), " ")
                            val targetNorm = targetWord.trim().lowercase().replace("\\s+".toRegex(), " ")
                            if (userNorm == targetNorm) {
                                spellingState = SpellingState.CORRECT
                                ttsHelper.speak(targetWord)
                            } else {
                                retryCount++
                                initialCorrectFirstTry = false
                                spellingState = if (retryCount == 1) {
                                    SpellingState.INCORRECT_FIRST
                                } else {
                                    answerRevealed = true
                                    SpellingState.INCORRECT_REVEALED
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(50.dp),
                        enabled = clickedIndicesOrder.size == bubbleLetters.size
                    ) {
                        Text("检查答案", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else if (spellingState == SpellingState.CORRECT) {
                    Button(
                        onClick = {
                            onCorrect(initialCorrectFirstTry, retryCount, hintUsed, answerRevealed)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("继续", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Persistence Feedback Entry Point for user assistance
            if (spellingState == SpellingState.INCORRECT_FIRST || spellingState == SpellingState.INCORRECT_REVEALED) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Re-trigger visual assistant panel
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (spellingState == SpellingState.INCORRECT_FIRST) "❌ 拼写有误。点击此处查看提示" else "❌ 拼写有误。点击查看正确对比",
                            color = Color(0xFFF87171),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Filled.Help, contentDescription = "协助", tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // --- SECTION FIVE: Spelling Feedback Modal Sheet overlay ---
        if (spellingState == SpellingState.INCORRECT_FIRST) {
            AlertDialog(
                onDismissRequest = { spellingState = SpellingState.EDITING },
                containerColor = Color(0xFF1E293B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Warning, contentDescription = "注意", tint = Color(0xFFF59E0B))
                        Text("拼写还差一点点！", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("输入答案: $finalAssembledWord", color = Color.LightGray, fontSize = 14.sp)
                        Text("🔊 发音提示: ${word.phonetic}", color = Color(0xFFF472B6), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        
                        if (word.syllables.isNotEmpty()) {
                            Text("🧩 音节分段: ${word.syllables}", color = Color(0xFF00E5FF), fontSize = 14.sp)
                        }
                        Text("📐 字母数量: 本词一共有 ${targetWord.length} 个字符", color = Color.LightGray, fontSize = 13.sp)
                        hintUsed = true
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { spellingState = SpellingState.EDITING },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                    ) {
                        Text("再试一次", color = Color.White)
                    }
                }
            )
        } else if (spellingState == SpellingState.INCORRECT_REVEALED) {
            AlertDialog(
                onDismissRequest = { /* Must read before closing */ },
                containerColor = Color(0xFF1E293B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Cancel, contentDescription = "错误", tint = Color(0xFFEF4444))
                        Text("正确答案对照", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("你输入的答案与正确拼写位置对齐:", color = Color.LightGray, fontSize = 13.sp)
                        
                        // Letter comparative visualizer (Red incorrect, Green correct, Gray missing)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (i in targetWord.indices) {
                                val userChar = finalAssembledWord.getOrNull(i)
                                val targetChar = targetWord[i]
                                val itemBg = when {
                                    userChar == null -> Color(0xFF475569) // missing
                                    userChar.lowercaseChar() == targetChar.lowercaseChar() -> Color(0xFF10B981) // correct
                                    else -> Color(0xFFEF4444) // incorrect
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(itemBg, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = targetChar.toString(),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("绿色: 正确 | 红色: 错误/多余 | 灰色: 缺失", color = Color.Gray, fontSize = 10.sp)
                        }

                        Divider(color = Color(0xFF334155))
                        Text("正确答案: $targetWord", color = Color(0xFF10B981), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("中文释义: ${word.chineseMeaning}", color = Color.LightGray, fontSize = 14.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            resetWordPuzzle() // Reset user jumbled bubbles so they MUST spell correctly now
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("看着答案重新拼写一遍", color = Color.White)
                    }
                }
            )
        }
    }
}

private fun spendingStateCheck(state: SpellingState): Boolean {
    return state == SpellingState.EDITING || state == SpellingState.INCORRECT_FIRST || state == SpellingState.INCORRECT_REVEALED
}


// ==========================================
// STAGE 4: Stage4DictationScreen (Dual Mode: HANDWRITING & KEYBOARD)
// ==========================================
@Composable
fun Stage4DictationScreen(
    word: EnglishWord,
    ttsHelper: EnglishTTSHelper,
    equippedBrushStyle: com.example.ui.BrushStyle = com.example.ui.BrushStyle.ALL_BRUSHES[0],
    equippedBrushConfig: com.example.data.PlayerBrushConfig? = null,
    groupTitle: String = "第四阶段：听写默写",
    isExtended: Boolean = false,
    onCorrect: (dictationCorrect: Boolean, retryCount: Int, hintUsed: Boolean, answerRevealed: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val targetWord = word.spelling
    var dictationMode by remember { mutableStateOf(DictationMode.HANDWRITING) }
    var typedWord by remember { mutableStateOf("") }
    var strokeCount by remember { mutableStateOf(0) }
    var handwritingViewRef by remember { mutableStateOf<EnglishHandwritingView?>(null) }

    var dictationState by remember { mutableStateOf(SpellingState.EDITING) }
    var retryCount by remember { mutableStateOf(0) }
    var hintUsed by remember { mutableStateOf(false) }
    var answerRevealed by remember { mutableStateOf(false) }
    var initialCorrectFirstTry by remember { mutableStateOf(true) }

    fun resetDictation() {
        typedWord = ""
        strokeCount = 0
        handwritingViewRef?.clear()
        dictationState = SpellingState.EDITING
    }

    LaunchedEffect(word.wordId) {
        resetDictation()
        retryCount = 0
        hintUsed = false
        answerRevealed = false
        initialCorrectFirstTry = true
        ttsHelper.speak(targetWord)
    }

    val feedbackState = when (dictationState) {
        SpellingState.CORRECT -> FeedbackBannerState.CORRECT
        SpellingState.INCORRECT_FIRST, SpellingState.INCORRECT_REVEALED -> FeedbackBannerState.INCORRECT
        else -> FeedbackBannerState.NONE
    }

    val feedbackText = when (dictationState) {
        SpellingState.CORRECT -> "✅ 默写完全正确！"
        SpellingState.INCORRECT_FIRST -> "❌ 默写存在偏差，查看提示后再试"
        SpellingState.INCORRECT_REVEALED -> "❌ 答案与标准拼写对比"
        else -> ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(groupTitle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isExtended) {
                                if (dictationMode == DictationMode.HANDWRITING) "听说拓展词 · 手写默写 (不影响核心通关)" else "听说拓展词 · 键盘默写 (不影响核心通关)"
                            } else {
                                if (dictationMode == DictationMode.HANDWRITING) "核心词 · 手写默写" else "核心词 · 键盘默写"
                            },
                            color = if (isExtended) Color(0xFFF472B6) else Color(0xFF00E5FF),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "退出", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        bottomBar = {
            EnglishChallengeBottomBar(
                enableImePadding = (dictationMode == DictationMode.KEYBOARD),
                hasPrevious = true,
                previousText = "退出挑战",
                previousEnabled = true,
                actionText = when (dictationState) {
                    SpellingState.EDITING -> "提交答案"
                    SpellingState.CORRECT -> "继续"
                    else -> "再试一次"
                },
                actionEnabled = when (dictationMode) {
                    DictationMode.HANDWRITING -> strokeCount > 0 || dictationState != SpellingState.EDITING
                    DictationMode.KEYBOARD -> typedWord.isNotBlank() || dictationState != SpellingState.EDITING
                },
                actionColor = when (dictationState) {
                    SpellingState.CORRECT -> Color(0xFF10B981)
                    SpellingState.INCORRECT_FIRST, SpellingState.INCORRECT_REVEALED -> Color(0xFFEF4444)
                    else -> Color(0xFF00E5FF)
                },
                onPrevious = onBack,
                onAction = {
                    if (dictationState == SpellingState.CORRECT) {
                        onCorrect(initialCorrectFirstTry, retryCount, hintUsed, answerRevealed)
                    } else if (dictationState == SpellingState.INCORRECT_FIRST || dictationState == SpellingState.INCORRECT_REVEALED) {
                        dictationState = SpellingState.EDITING
                    } else {
                        // Validate user answer in KEYBOARD mode, or self-check/check in HANDWRITING mode
                        if (dictationMode == DictationMode.KEYBOARD) {
                            val userNorm = typedWord.trim().lowercase().replace("\\s+".toRegex(), " ")
                            val targetNorm = targetWord.trim().lowercase().replace("\\s+".toRegex(), " ")
                            if (userNorm == targetNorm || userNorm.replace(" ", "") == targetNorm.replace(" ", "")) {
                                dictationState = SpellingState.CORRECT
                            } else {
                                retryCount++
                                initialCorrectFirstTry = false
                                if (retryCount == 1) {
                                    dictationState = SpellingState.INCORRECT_FIRST
                                } else {
                                    answerRevealed = true
                                    dictationState = SpellingState.INCORRECT_REVEALED
                                }
                            }
                        } else {
                            // Handwriting mode validation - reveals answer diff for self checking & records strokes
                            dictationState = SpellingState.CORRECT
                        }
                    }
                },
                feedbackState = feedbackState,
                feedbackText = feedbackText
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Toggle Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (dictationMode == DictationMode.HANDWRITING) Color(0xFF00E5FF) else Color.Transparent)
                        .clickable { dictationMode = DictationMode.HANDWRITING },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✍️ 手写默写",
                        color = if (dictationMode == DictationMode.HANDWRITING) Color(0xFF0F172A) else Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (dictationMode == DictationMode.KEYBOARD) Color(0xFF00E5FF) else Color.Transparent)
                        .clickable { dictationMode = DictationMode.KEYBOARD },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⌨️ 键盘默写",
                        color = if (dictationMode == DictationMode.KEYBOARD) Color(0xFF0F172A) else Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Audio Controls (Normal & Slow)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { ttsHelper.speak(targetWord, isSlow = false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = "标准速", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("标准速发音", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { ttsHelper.speak(targetWord, isSlow = true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Speed, contentDescription = "慢速", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("1/2 慢速发音", fontSize = 13.sp)
                }
            }

            if (dictationState == SpellingState.INCORRECT_FIRST || dictationState == SpellingState.INCORRECT_REVEALED) {
                EnglishAnswerDiffView(
                    userAnswer = typedWord,
                    targetWord = targetWord,
                    chineseMeaning = word.chineseMeaning
                )
            } else if (dictationMode == DictationMode.HANDWRITING) {
                // HANDWRITING MODE
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Structure & space position hint (e.g., "pencil box" -> 2 词，10 个字符)
                    val wordParts = targetWord.split("\\s+".toRegex()).filter { it.isNotEmpty() }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "结构提示: ${wordParts.size} 个单词 (${targetWord.length} 字符)",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF3B82F6).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "装备笔刷: ${equippedBrushStyle.brushName}",
                                color = Color(0xFF60A5FA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Four-line Three-space Guide Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF12161A))
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                EnglishHandwritingView(ctx).apply {
                                    currentBrush = equippedBrushStyle
                                    currentBrushConfig = equippedBrushConfig
                                    onStrokeFinished = {
                                        strokeCount = getStrokes().size
                                    }
                                    handwritingViewRef = this
                                }
                            },
                            update = { view ->
                                view.currentBrush = equippedBrushStyle
                                view.currentBrushConfig = equippedBrushConfig
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Action buttons (Undo & Clear ONLY inside card, NO duplicate "Next" button!)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { handwritingViewRef?.undo() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Undo, contentDescription = "撤销", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("撤销一步")
                        }

                        OutlinedButton(
                            onClick = { handwritingViewRef?.clear() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "清空", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清空重写")
                        }
                    }

                    Text(
                        text = "（手写墨迹已记录供核对；无自动识别引擎时，判分依赖键盘模式或对齐查验）",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // KEYBOARD MODE
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EnglishWordAnswerBoard(
                        targetWord = targetWord,
                        assembledLettersFlat = typedWord,
                        isReadOnly = false,
                        onSlotClick = { index ->
                            if (index < typedWord.length) {
                                typedWord = typedWord.removeRange(index, index + 1)
                            }
                        }
                    )

                    // Virtual Keyboard
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val row1 = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p')
                        val row2 = listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l')
                        val row3 = listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row1.forEach { char ->
                                VirtualKey(char = char.toString(), onClick = { typedWord += char })
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row2.forEach { char ->
                                VirtualKey(char = char.toString(), onClick = { typedWord += char })
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Backspace
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 40.dp)
                                    .background(Color(0xFF475569), RoundedCornerShape(6.dp))
                                    .clickable { if (typedWord.isNotEmpty()) typedWord = typedWord.dropLast(1) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Backspace, contentDescription = "退格", tint = Color.White, modifier = Modifier.size(16.dp))
                            }

                            row3.forEach { char ->
                                VirtualKey(char = char.toString(), onClick = { typedWord += char })
                            }

                            // Explicit Space Key ("空格")
                            Box(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 40.dp)
                                    .background(Color(0xFF3B82F6), RoundedCornerShape(6.dp))
                                    .clickable { typedWord += " " },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("空格", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VirtualKey(
    char: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 30.dp, height = 44.dp)
            .background(Color(0xFF334155), RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = char, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}


// ==========================================
// SCREEN 5: ChallengeResultScreen
// ==========================================
@Composable
fun ChallengeResultScreen(
    unit: EnglishUnit,
    wordStates: Map<String, WordChallengeState>,
    classA: List<EnglishWord>,
    classB: List<EnglishWord>,
    context: Context,
    onCompleteAndExit: (Int) -> Unit,
    onRetryWeak: (List<EnglishWord>) -> Unit
) {
    // Core words calculations (A-class)
    val coreStates = remember(wordStates) {
        wordStates.filterKeys { wId -> classA.any { it.wordId == wId } }
    }
    val coreMasteredWords = remember(coreStates) {
        classA.filter { w ->
            val st = coreStates[w.wordId]
            st != null && st.meaningCorrect && st.reverseCorrect && st.spellingCorrect && st.dictationCorrect && !st.answerRevealed
        }
    }
    val coreWeakWords = remember(coreStates, coreMasteredWords) {
        classA.filter { it !in coreMasteredWords }
    }
    val corePassed = coreWeakWords.isEmpty()

    // Extended words calculations (B-class)
    val extStates = remember(wordStates) {
        wordStates.filterKeys { wId -> classB.any { it.wordId == wId } }
    }
    val extMasteredWords = remember(extStates) {
        classB.filter { w ->
            val st = extStates[w.wordId]
            st != null && st.meaningCorrect && st.reverseCorrect && st.spellingCorrect && st.dictationCorrect && !st.answerRevealed
        }
    }
    val extWeakWords = remember(extStates, extMasteredWords) {
        classB.filter { it !in extMasteredWords }
    }

    val totalMasteredCount = coreMasteredWords.size + extMasteredWords.size
    val totalWordsCount = classA.size + classB.size
    val goldEarned = remember(coreMasteredWords, extMasteredWords) {
        coreMasteredWords.size * 3 + extMasteredWords.size * 1
    }

    val allWeakWords = remember(coreWeakWords, extWeakWords) {
        coreWeakWords + extWeakWords
    }

    Scaffold(
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "🏆 终极挑战阶段性结算",
                    color = Color(0xFFEC4899),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "星语海港 · ${unit.title} 词汇验收报告",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            // Core Status Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (corePassed) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (corePassed) Color(0xFF10B981) else Color(0xFFEF4444),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (corePassed) "✅ 单元核心挑战合格通过！" else "❌ 核心词仍需巩固 (核心错误将阻止通关)",
                        color = if (corePassed) Color(0xFF34D399) else Color(0xFFFCA5A5),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Coin Rewards Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFEC4899))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("获得金币奖励", color = Color.LightGray, fontSize = 14.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🪙", fontSize = 32.sp)
                            Text(
                                text = "+$goldEarned",
                                color = Color(0xFFF59E0B),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        val pct = if (totalWordsCount > 0) (totalMasteredCount * 100) / totalWordsCount else 0
                        Text("全词总掌握率: $totalMasteredCount/$totalWordsCount ($pct%)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SECTION 1: CORE WORDS (A-Class)
            item {
                Text(
                    text = "📚 核心词汇表现 (A类 - 决定单元通关)",
                    color = Color(0xFF00E5FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val meaningScore = coreStates.values.count { it.meaningCorrect }
                        val reverseScore = coreStates.values.count { it.reverseCorrect }
                        val spellingScore = coreStates.values.count { it.spellingCorrect }
                        val dictationScore = coreStates.values.count { it.dictationCorrect }

                        ResultMetricRow("听英文辨中文 (词义识记)", meaningScore, classA.size, Color(0xFF34D399))
                        ResultMetricRow("听中文辨英文 (拼写关联)", reverseScore, classA.size, Color(0xFFF472B6))
                        ResultMetricRow("看中文拼英文 (核心拼写)", spellingScore, classA.size, Color(0xFF00E5FF))
                        ResultMetricRow("听英文默写 (核心听写)", dictationScore, classA.size, Color(0xFFA78BFA))
                    }
                }
            }

            if (coreMasteredWords.isNotEmpty()) {
                items(coreMasteredWords) { word ->
                    WordResultRow(word, isMastered = true)
                }
            }

            if (coreWeakWords.isNotEmpty()) {
                items(coreWeakWords) { word ->
                    WordResultRow(word, isMastered = false)
                }
            }

            // SECTION 2: EXTENDED WORDS (B-Class)
            if (classB.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🗣️ 拓展听说词汇表现 (B类 - 拓展练习，不影响核心通关)",
                        color = Color(0xFFF472B6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val meaningScore = extStates.values.count { it.meaningCorrect }
                            val reverseScore = extStates.values.count { it.reverseCorrect }
                            val spellingScore = extStates.values.count { it.spellingCorrect }
                            val dictationScore = extStates.values.count { it.dictationCorrect }

                            ResultMetricRow("听英文辨中文", meaningScore, classB.size, Color(0xFF34D399))
                            ResultMetricRow("听中文辨英文", reverseScore, classB.size, Color(0xFFF472B6))
                            ResultMetricRow("看中文拼英文 (拓展拼写)", spellingScore, classB.size, Color(0xFF00E5FF))
                            ResultMetricRow("听英文默写 (拓展听写)", dictationScore, classB.size, Color(0xFFA78BFA))
                        }
                    }
                }

                if (extMasteredWords.isNotEmpty()) {
                    items(extMasteredWords) { word ->
                        WordResultRow(word, isMastered = true)
                    }
                }

                if (extWeakWords.isNotEmpty()) {
                    items(extWeakWords) { word ->
                        WordResultRow(word, isMastered = false)
                    }
                }
            }

            // Operational Bottom Row
            item {
                Spacer(modifier = Modifier.height(12.dp))
                if (allWeakWords.isNotEmpty()) {
                    Button(
                        onClick = { onRetryWeak(allWeakWords) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("retry_weak_words_button")
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Refresh, contentDescription = "重测")
                            Text("再次挑战薄弱词 (${allWeakWords.size}个词)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = { onCompleteAndExit(goldEarned) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("exit_challenge_button")
                ) {
                    Text("完成挑战并保存成绩", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ResultMetricRow(
    label: String,
    score: Int,
    total: Int,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.LightGray, fontSize = 13.sp)
            Text("$score / $total", color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        // Small progress indicator bar
        val progress = if (total > 0) score.toFloat() / total else 0f
        LinearProgressIndicator(
            progress = progress,
            color = color,
            trackColor = Color(0xFF1E293B),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
fun WordResultRow(
    word: EnglishWord,
    isMastered: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isMastered) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(word.spelling, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(word.chineseMeaning, color = Color.LightGray, fontSize = 13.sp)
            }

            Box(
                modifier = Modifier
                    .background(
                        if (isMastered) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isMastered) "MASTERED" else "NEED REVIEW",
                    color = if (isMastered) Color(0xFF34D399) else Color(0xFFF87171),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CenteredWrapRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val horizontalSpacingPx = horizontalSpacing.roundToPx()
        val verticalSpacingPx = verticalSpacing.roundToPx()

        val maxAvailableWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else 1000000

        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val rowWidths = mutableListOf<Int>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentWidth = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            if (currentWidth + placeable.width > maxAvailableWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                rowWidths.add(currentWidth - horizontalSpacingPx)
                currentRow = mutableListOf(placeable)
                currentWidth = placeable.width + horizontalSpacingPx
            } else {
                currentRow.add(placeable)
                currentWidth += placeable.width + horizontalSpacingPx
            }
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentWidth - horizontalSpacingPx)
        }

        val totalHeight = rows.sumOf { row -> row.maxOfOrNull { it.height } ?: 0 } +
                (rows.size - 1).coerceAtLeast(0) * verticalSpacingPx

        val maxRowWidth = rowWidths.maxOrNull() ?: 0
        val finalWidth = maxRowWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
        val finalHeight = totalHeight.coerceIn(constraints.minHeight, constraints.maxHeight)

        layout(finalWidth, finalHeight) {
            var y = 0
            rows.forEachIndexed { index, row ->
                val rowHeight = row.maxOfOrNull { it.height } ?: 0
                var x = (finalWidth - rowWidths[index]) / 2
                row.forEach { placeable ->
                    placeable.placeRelative(x, y + (rowHeight - placeable.height) / 2)
                    x += placeable.width + horizontalSpacingPx
                }
                y += rowHeight + verticalSpacingPx
            }
        }
    }
}

