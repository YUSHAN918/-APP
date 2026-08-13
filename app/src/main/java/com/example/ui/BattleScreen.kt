package com.example.ui

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.viewmodel.Answer
import com.example.viewmodel.GameStage
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ConfidenceLevel
import com.example.viewmodel.RecognitionResult
import com.example.viewmodel.RecognitionSource
import com.example.data.*

fun getGridDimension(charCount: Int): Pair<Int, Int> {
    return when {
        charCount <= 4 -> Pair(charCount, 1)
        charCount in 5..6 -> Pair(3, 2)
        charCount in 7..8 -> Pair(4, 2)
        else -> Pair(4, (charCount + 3) / 4)
    }
}

@Composable
fun BattleScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = onBack,
    onNavigateToReport: () -> Unit = {},
    onNavigateToReview: () -> Unit = {}
) {
    val currentStage by viewModel.currentStage.collectAsState()
    val autoGradingToast by viewModel.autoGradingToast.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(autoGradingToast) {
        autoGradingToast?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.autoGradingToast.value = null
        }
    }

    when (currentStage) {
        GameStage.PREP -> PrepScreen(viewModel, onBack)
        GameStage.DICTATION -> DictationScreen(viewModel, onBack)
        GameStage.ACCEPTANCE -> AcceptanceScreen(viewModel, onBack)
        GameStage.SETTLEMENT -> SettlementScreen(
            viewModel = viewModel,
            onBack = onBack,
            onNavigateToReport = onNavigateToReport,
            onNavigateToReview = onNavigateToReview
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrepScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val levelName by viewModel.levelName.collectAsState()
    val words by viewModel.currentBattleWords.collectAsState()
    val stats by viewModel.userStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "关卡准备 · BATTLE TERMINAL",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF),
                            letterSpacing = 1.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color(0xFF00E5FF)
                        )
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFF00E5FF).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1225))
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // System Decors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SYS_PREPARATION_ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00E5FF).copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = levelName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF070913).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Param 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("连续听写词语", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF94A3B8))
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "${words.size} 个字词",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Param 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = Color(0xFFFFAB40),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("智能播放频次", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF94A3B8))
                            }
                            Text(
                                text = "每题播放 ${stats?.playCount ?: 2} 次",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        // Param 3
                        val totalSecs = words.size * (stats?.timePerWord ?: 30)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("预估挑战时间", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF94A3B8))
                            }
                            Text(
                                text = "${totalSecs / 60} 分 ${totalSecs % 60} 秒",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Premium Launch Button
            Button(
                onClick = { viewModel.beginDictation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF00E5FF),
                        shape = RoundedCornerShape(30.dp)
                    ),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F1225),
                    contentColor = Color(0xFF00E5FF)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "开始听写 · INITIALIZE BATTLE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

data class BattleMonsterState(
    val monsterId: String,
    val displayName: String,
    val monsterType: String,
    val maxHp: Int,
    val currentHp: Int,
    val shield: Int,
    val mood: String,
    val rarity: String,
    val emoji: String
)

@Composable
fun rememberBattleMonsterState(
    currentWord: com.example.data.WordItem?,
    currentCharAnswers: List<com.example.viewmodel.CharAnswer>,
    isAutoMode: Boolean
): BattleMonsterState? {
    if (currentWord == null) return null
    return remember(currentWord, currentCharAnswers, isAutoMode) {
        val targetAnswerText = currentWord.getEffectiveTargetAnswer()
        val wordText = targetAnswerText.filter { it.isLetterOrDigit() }
        val charCount = if (wordText.isEmpty()) 1 else wordText.length
        
        val (type, emoji, rarity) = when {
            currentWord.difficulty == "BOSS" || currentWord.type == "成语" -> Triple("BOSS", "🐲", "LEGEND")
            currentWord.difficulty == "易错" -> Triple("ELITE", "😈", "EPIC")
            currentWord.type == "古诗句" || currentWord.type == "课文重点句" -> Triple("POEM_GUARD", "📜", "RARE")
            currentWord.visibilityPolicy == "REVIEW_ONLY" -> Triple("WRONG_WORD", "🦹", "UNCOMMON")
            else -> Triple("NORMAL", "👾", "COMMON")
        }
        
        val maxHp = charCount * 100
        var currentHp = maxHp
        var hasShield = 0
        
        currentCharAnswers.forEach { charAns ->
            if (!charAns.isBlank) {
                val recog = charAns.recognitionResult
                if (recog != null) {
                    val isFirstChoice = recog.recognizedText == recog.expectedChar || recog.candidates.firstOrNull() == recog.expectedChar
                    val isAnyMatch = recog.isLikelyCorrect || recog.candidates.contains(recog.expectedChar)
                    if (isFirstChoice) {
                        currentHp -= 100
                    } else if (isAnyMatch) {
                        currentHp -= 50
                    } else {
                        hasShield = 50
                    }
                } else {
                    if (!isAutoMode) {
                        currentHp -= 100
                    }
                }
            }
        }
        currentHp = currentHp.coerceIn(0, maxHp)
        
        val isComplete = currentCharAnswers.size >= charCount
        val mood = if (isComplete) {
            if (!isAutoMode) "WAITING_REVIEW" else {
                if (currentHp == 0) "PURIFIED" else "ESCAPED"
            }
        } else {
            val lastAns = currentCharAnswers.lastOrNull()
            if (lastAns != null) {
                val recog = lastAns.recognitionResult
                if (recog == null) {
                    if (!isAutoMode) "WAITING_REVIEW" else "WAITING"
                } else {
                    val isFirstChoice = recog.recognizedText == recog.expectedChar || recog.candidates.firstOrNull() == recog.expectedChar
                    val isAnyMatch = recog.isLikelyCorrect || recog.candidates.contains(recog.expectedChar)
                    if (isFirstChoice) "HIT"
                    else if (isAnyMatch) "GRAZE"
                    else "SHIELD"
                }
            } else {
                "WAITING"
            }
        }
        
        val displayName = when (type) {
            "BOSS" -> "深渊侵蚀之龙"
            "ELITE" -> "远古易错领主"
            "POEM_GUARD" -> "千秋墨意守卫"
            "WRONG_WORD" -> "噩梦错字之灵"
            else -> "迷惘字词魔兽"
        }
        
        BattleMonsterState(
            monsterId = currentWord.id.toString(),
            displayName = displayName,
            monsterType = type,
            maxHp = maxHp,
            currentHp = currentHp,
            shield = hasShield,
            mood = mood,
            rarity = rarity,
            emoji = emoji
        )
    }
}

@Composable
fun BattleMonsterHUD(
    monsterState: BattleMonsterState,
    currentCombo: Int,
    isAutoMode: Boolean,
    playerProfile: com.example.data.PlayerProfile?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF131625).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, when (monsterState.monsterType) {
            "BOSS" -> Color(0xFFFF1744)
            "ELITE" -> Color(0xFFFF9100)
            "POEM_GUARD" -> Color(0xFF00E5FF)
            "WRONG_WORD" -> Color(0xFFE040FB)
            else -> Color(0xFF00E676).copy(alpha = 0.4f)
        }),
        modifier = Modifier.fillMaxWidth().testTag("battle_monster_hud")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${monsterState.emoji} ${monsterState.displayName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = when (monsterState.rarity) {
                            "LEGEND" -> Color(0xFFFF1744).copy(alpha = 0.15f)
                            "EPIC" -> Color(0xFFFF9100).copy(alpha = 0.15f)
                            "RARE" -> Color(0xFF00E5FF).copy(alpha = 0.15f)
                            else -> Color.Gray.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = monsterState.rarity,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (monsterState.rarity) {
                                "LEGEND" -> Color(0xFFFF1744)
                                "EPIC" -> Color(0xFFFF9100)
                                "RARE" -> Color(0xFF00E5FF)
                                else -> Color.LightGray
                            },
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                
                if (isAutoMode && currentCombo > 0) {
                    Surface(
                        color = Color(0xFFFF3D00).copy(alpha = 0.15f),
                        contentColor = Color(0xFFFF3D00),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "🔥 连击 $currentCombo",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val hpPercent = monsterState.currentHp.toFloat() / monsterState.maxHp.toFloat()
            val hpBarColor = when {
                hpPercent <= 0.3f -> Color(0xFFFF1744)
                hpPercent <= 0.6f -> Color(0xFFFF9100)
                else -> Color(0xFF00E676)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "生命值 ",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(Color(0xFF1E2235), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(hpPercent)
                            .background(hpBarColor, RoundedCornerShape(4.dp))
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${monsterState.currentHp}/${monsterState.maxHp}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                
                if (monsterState.shield > 0 && monsterState.currentHp > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        color = Color(0xFF2979FF).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text(
                            text = "🛡️ ${monsterState.shield}",
                            color = Color(0xFF2979FF),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

fun getWeaponAttackFeedback(brushId: String, mood: String): String {
    val weaponName = when (brushId) {
        "practice_wood" -> "练习木笔"
        "ink_brush" -> "墨韵毛笔"
        "stardust_brush" -> "星尘笔"
        "fluorescent_brush" -> "荧光笔"
        "rainbow_brush" -> "彩虹笔"
        "pet_dragon_brush" -> "小墨龙之笔"
        else -> "默认黑笔"
    }
    
    return when (mood) {
        "HIT" -> when (brushId) {
            "practice_wood" -> "💥 挥舞【练习木笔】！发出木头撞击的「清脆脆响」，直接击碎魔物护甲！"
            "ink_brush" -> "💥 泼洒【墨韵毛笔】！古墨香气四溢，挥出千钧「泼墨气流」重创魔物！"
            "stardust_brush" -> "💥 触发【星尘笔】！星光词气爆裂，如「烈焰熔岩」般将魔物彻底包围！"
            "fluorescent_brush" -> "💥 挥动【荧光笔】！魔幻光芒四射，直击魔物要害！"
            "rainbow_brush" -> "💥 挥洒【彩虹笔】！绚丽的彩虹洪流冲刷净化了魔物的执念！"
            "pet_dragon_brush" -> "💥 唤醒【小墨龙之笔】！墨气化作神龙呼啸，彻底瓦解魔物核心！"
            else -> "💥 挥动【默认黑笔】！墨黑如铁，稳健重击击中魔物！"
        }
        "GRAZE" -> "🩹 擦边一击！墨力稍有分散，字灵阵有些微波动，快调整笔画！"
        "SHIELD" -> "🛡️ 被护盾弹回！魔物汉字弱点护盾仍存，需要注入更精确的书写墨力！"
        "PURIFIED" -> "✨ 彻底净化！古文字气回归清明，魔物化作字灵符纸！"
        else -> "👾 魔物受到震慑，其要害弱点正在闪烁，准备最后一击！"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictationScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val words by viewModel.currentBattleWords.collectAsState()
    val currentIndex by viewModel.currentWordIndex.collectAsState()
    val currentCharIndex by viewModel.currentCharIndex.collectAsState()
    val currentCharAnswers by viewModel.currentCharAnswers.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val playCount by viewModel.playCount.collectAsState()
    val stats by viewModel.userStats.collectAsState()
    val playerProfile by viewModel.playerProfile.collectAsState()
    val equippedBrushConfig by viewModel.equippedBrushConfig.collectAsState()

    val currentWord = words.getOrNull(currentIndex)
    val targetAnswerText = currentWord?.getEffectiveTargetAnswer() ?: currentWord?.text ?: ""
    val wordText = targetAnswerText.filter { it.isLetterOrDigit() }
    val charCount = if (wordText.isEmpty()) 1 else wordText.length
    
    val isCurrentQuestionComplete = currentCharAnswers.size >= charCount
    val isLastQuestion = currentIndex == words.size - 1

    var handwritingView by remember { mutableStateOf<HandwritingView?>(null) }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var showNextWarning by remember { mutableStateOf(false) }
    
    var reviewSelectedIndex by remember { mutableIntStateOf(0) }
    var isRewriting by remember { mutableStateOf(false) }
    var showMeaningDialog by remember { mutableStateOf(false) }
    var userBackgroundChoice by remember { mutableStateOf("dark") } // "dark" or "light"
    var showMonsterIntro by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var feedbackState by remember { mutableStateOf<DictationFeedbackState>(DictationFeedbackState.Idle) }
    var isSubmittingWord by remember { mutableStateOf(false) }

    LaunchedEffect(currentIndex) {
        keyboardController?.hide()
        Log.d("HandwritingDebug", "Entering questionIndex: $currentIndex")
        handwritingView?.clear()
        reviewSelectedIndex = 0
        isRewriting = false
        if (currentIndex == 0) {
            showMonsterIntro = true
        } else {
            showMonsterIntro = false
        }
    }
    
    LaunchedEffect(isCurrentQuestionComplete) {
        if (isCurrentQuestionComplete && !isRewriting) {
             reviewSelectedIndex = 0
        }
    }
    
    LaunchedEffect(currentCharIndex) {
        Log.d("HandwritingDebug", "Entering currentCharIndex: $currentCharIndex")
        handwritingView?.clear()
    }
    
    LaunchedEffect(timeLeft) {
        if (timeLeft == 0) {
            Log.d("HandwritingDebug", "Time is up, saving answer")
            val strokes = handwritingView?.getStrokes() ?: emptyList()
            val w = handwritingView?.width?.toFloat() ?: 1f
            val h = handwritingView?.height?.toFloat() ?: 1f
            viewModel.submitWordAndNext(strokes, w, h)
        }
    }

    val activePet by viewModel.activePet.collectAsState()
    val currentCombo by viewModel.currentCombo.collectAsState()
    val isAutoMode = stats?.gradingMode == "AUTO"

    val monsterState = rememberBattleMonsterState(currentWord, currentCharAnswers, isAutoMode)

    if (currentWord == null || monsterState == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0C0D14)),
            contentAlignment = Alignment.Center
        ) {
            Text("关卡数据加载中...", color = Color.Gray, fontSize = 16.sp)
        }
        return
    }

    val isPractice = currentWord.visibilityPolicy == "PRACTICE_HINT"
    val currentMonsterName = currentWord.text
    val displayMonsterName = if (isPractice) {
        currentMonsterName
    } else {
        currentMonsterName.map { if (it.isLetterOrDigit()) '?' else it }.joinToString("")
    }

    // Dialog: Monster Intro
    if (showMonsterIntro) {
        AlertDialog(
            onDismissRequest = { 
                showMonsterIntro = false 
                viewModel.resumeTimer()
            },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            containerColor = Color(0xFF0F111A),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "⚔️ 关卡魔物登场 ⚔️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00E5FF),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF131625)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, when (monsterState.monsterType) {
                            "BOSS" -> Color(0xFFFF1744)
                            "ELITE" -> Color(0xFFFF9100)
                            "POEM_GUARD" -> Color(0xFF00E5FF)
                            "WRONG_WORD" -> Color(0xFFE040FB)
                            else -> Color(0xFF00E676).copy(alpha = 0.5f)
                        }),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${monsterState.emoji} ${monsterState.displayName}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = when (monsterState.rarity) {
                                            "LEGEND" -> Color(0xFFFF1744).copy(alpha = 0.2f)
                                            "EPIC" -> Color(0xFFFF9100).copy(alpha = 0.2f)
                                            "RARE" -> Color(0xFF00E5FF).copy(alpha = 0.2f)
                                            else -> Color.Gray.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = monsterState.rarity,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (monsterState.rarity) {
                                                "LEGEND" -> Color(0xFFFF1744)
                                                "EPIC" -> Color(0xFFFF9100)
                                                "RARE" -> Color(0xFF00E5FF)
                                                else -> Color.LightGray
                                            },
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (isAutoMode && currentCombo > 0) {
                                        Surface(
                                            color = Color(0xFFFF3D00).copy(alpha = 0.15f),
                                            contentColor = Color(0xFFFF3D00),
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF3D00))
                                        ) {
                                            Text(
                                                text = "🔥 COMBO $currentCombo",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val hpPercent = monsterState.currentHp.toFloat() / monsterState.maxHp.toFloat()
                            val hpBarColor = when {
                                hpPercent <= 0.3f -> Color(0xFFFF1744)
                                hpPercent <= 0.6f -> Color(0xFFFF9100)
                                else -> Color(0xFF00E676)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HP ",
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(12.dp)
                                        .background(Color(0xFF1E2235), RoundedCornerShape(6.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(hpPercent)
                                            .background(hpBarColor, RoundedCornerShape(6.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${monsterState.currentHp} / ${monsterState.maxHp}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                
                                if (monsterState.shield > 0 && monsterState.currentHp > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF2979FF).copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2979FF)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "🛡️ 护盾",
                                            color = Color(0xFF2979FF),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val equippedBrushId = playerProfile?.equippedBrushId ?: "default_black"
                            val weaponFeedback = getWeaponAttackFeedback(equippedBrushId, monsterState.mood)
                            
                            val actionLogText = when (monsterState.mood) {
                                "PURIFIED" -> "✨ 净化成功！字字端正，魔物已被字灵之力完美洗涤！"
                                "ESCAPED" -> "🩹 魔物负伤，遁入暗影。在最终结算时，家长将进行灵魂判定！"
                                "WAITING_REVIEW" -> "⌛ 攻击已记录。字灵处于待命状态，等待守护者（家长）进行魂之判定！"
                                "HIT", "GRAZE", "SHIELD" -> weaponFeedback
                                else -> "👾 魔物发出沙哑的咆哮：「你能写对『$displayMonsterName』吗？」快出手攻击它的核心弱点！"
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E2235).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = actionLogText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when (monsterState.mood) {
                                        "PURIFIED" -> Color(0xFF00E676)
                                        "HIT" -> Color(0xFFFFD700)
                                        "GRAZE" -> Color(0xFF00E5FF)
                                        "SHIELD" -> Color(0xFFFF5252)
                                        "WAITING_REVIEW" -> Color(0xFFB0BEC5)
                                        else -> Color.LightGray
                                    },
                                    lineHeight = 16.sp
                                )
                            }

                            activePet?.let { pet ->
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                val petEmoji = when (pet.petId) {
                                    "小墨龙" -> "🐲"
                                    "小书灵" -> "📚"
                                    "小云狐" -> "🦊"
                                    "小竹猫" -> "🐼"
                                    else -> "🍃"
                                }
                                
                                val petDialogue = when (pet.lifeStage) {
                                    "EGG" -> "🥚 灵蛋正静静躺在角落，吸收了你的字灵书写能量 (字灵能量 +5)！"
                                    "SOUL_SLEEP" -> "👻 【字灵伙伴】正在灵魂庭院深处长眠，呼唤它的名字以期待它的复苏！"
                                    else -> {
                                        val petNameStr = pet.customName ?: pet.petName
                                        val petAssistDialogue = when (pet.petId) {
                                            "小墨龙" -> "🐲『神龙墨息』喷吐！一口龙焰墨息瞬间扫荡了战场！"
                                            "小书灵" -> "📚『古籍真解』翻开！看穿了魔物汉字最脆弱的一笔！"
                                            "小云狐" -> "🦊『幻影狐击』突袭！轻轻一扑，直接踩中魔物的错字拼音！"
                                            "小竹猫" -> "🐼『竹竹重锤』猛挥！舞动青翠神竹，给予魔物迎头痛击！"
                                            else -> "✨『字灵祝福』释放！温暖的光芒围绕在你身边！"
                                        }
                                        
                                        when {
                                            currentCombo >= 5 -> "🐾 【$petNameStr】腾空而起触发助攻！「$petAssistDialogue」"
                                            currentCombo >= 3 -> "🐾 【$petNameStr】开心呼喊: 『连击好棒！主人加油，我们就要打赢啦！』"
                                            else -> "🐾 【$petNameStr】在一旁认真为你掠阵护法，亲密度为 ${pet.intimacy}！"
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F111A).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(6.dp)
                                ) {
                                    Text(text = petEmoji, fontSize = 20.sp)
                                    Text(
                                        text = petDialogue,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (pet.lifeStage == "SOUL_SLEEP") Color(0xFFFF5252) else Color(0xFF00E5FF),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showMonsterIntro = false 
                        viewModel.resumeTimer()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Text("挥笔迎战 ⚔️", color = Color(0xFF0F111A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        )
    }

    if (showMeaningDialog) {
        val targetAns = currentWord.getEffectiveTargetAnswer()
        val safeClue = if (isPractice) currentWord.clueText else com.example.util.PromptSecurityUtil.maskTargetAnswerInPrompt(currentWord.clueText, targetAns)
        val safeMeaning = if (isPractice) currentWord.meaningHint else {
            var m = currentWord.meaningHint
            targetAns.forEach { ch -> m = m.replace(ch.toString(), "_") }
            m
        }

        AlertDialog(
            onDismissRequest = { showMeaningDialog = false },
            title = { Text("💡 字灵秘义", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (safeClue.isNotBlank()) {
                        Text("【语境提示】: $safeClue", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("【详细释义】: ${safeMeaning.ifBlank { "暂无释义" }}", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showMeaningDialog = false }) { Text("领悟") }
            }
        )
    }

    val visiblePrompt = currentWord.getEffectiveVisiblePrompt(isPracticeMode = isPractice)
    val subPrompt = currentWord.getEffectiveSubPrompt(isPracticeMode = isPractice)
    val allowViewMeaning = stats?.allowStudentViewMeaning ?: "AFTER_ERROR"
    val canShowMeaningBtn = (allowViewMeaning == "ALWAYS") && (currentWord.meaningHint.isNotBlank() || currentWord.clueText.isNotBlank())

    val levelName by viewModel.levelName.collectAsState()

    val equippedBrushId = playerProfile?.equippedBrushId ?: "default_black"
    val isBlackBrush = equippedBrushId == "default_black" || equippedBrushId == "ink_brush"
    val actualCanvasBg = userBackgroundChoice
    val isDarkBackground = actualCanvasBg == "dark"

    Scaffold(
        topBar = {
            LearningBattleHeader(
                levelName = levelName,
                currentIndex = currentIndex,
                totalCount = words.size,
                timeLeft = timeLeft,
                onBack = onBack,
                onHelpClick = { showMeaningDialog = true }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070913))
                .padding(padding)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 0. Monster HUD (Battle Arena Panel)
                BattleMonsterHUD(
                    monsterState = monsterState,
                    currentCombo = currentCombo,
                    isAutoMode = isAutoMode,
                    playerProfile = playerProfile
                )

                // 1. Prompt Area
                LearningPromptCard(
                    visiblePrompt = visiblePrompt,
                    subPrompt = if (subPrompt.isNotBlank() && subPrompt != visiblePrompt) subPrompt else "根据念读或提示进行书写",
                    isPractice = isPractice,
                    playCount = playCount,
                    maxPlayCount = stats?.playCount ?: 2,
                    onPlayAudio = { viewModel.playAudioAgain() },
                    showHelpHint = canShowMeaningBtn,
                    onViewHintClick = { showMeaningDialog = true }
                )

                // 2. Slots grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "字阵法槽: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until charCount) {
                            val isCurrent = if (!isCurrentQuestionComplete) {
                                i == currentCharIndex
                            } else {
                                i == reviewSelectedIndex
                            }
                            val ans = currentCharAnswers.find { it.charIndex == i }
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (isCurrent) Color(0x3500E5FF) else (if (isDarkBackground) Color(0xFF131625) else Color(0xFFFDF6E3)),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = if (isCurrent) 1.8.dp else 1.dp,
                                        color = if (isCurrent) Color(0xFF00E5FF) else (if (isDarkBackground) Color(0xFF1E2235) else Color(0xFFD4C5A9)),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = isCurrentQuestionComplete && !isRewriting) {
                                        reviewSelectedIndex = i
                                    }
                            ) {
                                if (ans != null && !ans.isBlank) {
                                    MiniCanvas(
                                        strokes = ans.strokes,
                                        originalWidth = ans.canvasWidth,
                                        originalHeight = ans.canvasHeight,
                                        cols = 1,
                                        rows = 1,
                                        isDarkTheme = isDarkBackground,
                                        brushId = equippedBrushId
                                    )
                                } else {
                                    Text(
                                        text = "${i + 1}",
                                        color = if (isDarkBackground) Color.Gray else Color.DarkGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Main Adaptive Handwriting Area
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    val padSize = minOf(maxWidth, maxHeight)
                    if (!isCurrentQuestionComplete || isRewriting) {
                        val canvasBorderColor = when (feedbackState) {
                            is DictationFeedbackState.Correct -> Color(0xFF00E676)
                            is DictationFeedbackState.Wrong, is DictationFeedbackState.NeedsImprovement -> Color(0xFFFF1744)
                            DictationFeedbackState.RecognitionFailed -> Color(0xFFFF9100)
                            else -> if (isDarkBackground) Color(0xFF1E2235) else Color(0xFFD4C5A9)
                        }
                        Box(
                            modifier = Modifier
                                .size(padSize)
                                .background(
                                    if (isDarkBackground) Color(0xFF131625) else Color(0xFFFDF6E3),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 2.dp,
                                    color = canvasBorderColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(4.dp)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    HandwritingView(ctx).apply {
                                        handwritingView = this
                                        setGrid(1, 1)
                                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                        isDarkTheme = isDarkBackground
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                                update = { view ->
                                    view.setGrid(1, 1)
                                    view.isDarkTheme = isDarkBackground
                                    val equippedBrushId = playerProfile?.equippedBrushId ?: "default_black"
                                    view.currentBrush = BrushStyle.getBrushById(equippedBrushId)
                                    val safeConfig = equippedBrushConfig?.copy(
                                        opacity = 1f,
                                        glowRadius = 0f,
                                        particleDensity = 0f,
                                        usageMode = "TEST_SAFE"
                                    )
                                    view.currentBrushConfig = safeConfig
                                }
                            )
                            
                            // Corner indicator
                            Surface(
                                color = if (isDarkBackground) Color(0xFF1E2235) else Color(0xFFD4C5A9),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF00E676), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "WEAPON LINK",
                                        color = if (isDarkBackground) Color.White else Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Dynamic non-blocking feedback overlay inside canvas box
                            AnswerFeedbackOverlay(
                                feedbackState = feedbackState,
                                onDismiss = {
                                    if (feedbackState is DictationFeedbackState.Wrong || feedbackState is DictationFeedbackState.NeedsImprovement) {
                                        viewModel.removeLastCharAnswer()
                                        handwritingView?.clear()
                                    }
                                    feedbackState = DictationFeedbackState.Idle
                                },
                                onShowTip = { showMeaningDialog = true },
                                modifier = Modifier.clip(RoundedCornerShape(16.dp))
                            )
                        }
                    } else {
                        // Answer Review Mode
                        Column(
                            modifier = Modifier
                                .size(padSize)
                                .background(Color(0xFF0F1225), shape = RoundedCornerShape(16.dp))
                                .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), shape = RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 1. Title & Dynamic Non-blocking Status Badge
                            val badgeColor = when (feedbackState) {
                                is DictationFeedbackState.Correct -> Color(0xFF00E676)
                                is DictationFeedbackState.NeedsImprovement -> Color(0xFFFF9100)
                                is DictationFeedbackState.Wrong -> Color(0xFFFF1744)
                                is DictationFeedbackState.Submitting -> Color(0xFF2979FF)
                                else -> Color(0xFF94A3B8)
                            }
                            
                            val badgeText = when (feedbackState) {
                                is DictationFeedbackState.Correct -> if (isAutoMode) "识别正确 ✨" else "书写完成"
                                is DictationFeedbackState.NeedsImprovement -> "建议调整 🩹"
                                is DictationFeedbackState.Wrong -> "未击中弱点 👾"
                                is DictationFeedbackState.Submitting -> "灵力析出中... ⚡"
                                else -> "书写完成 (待确认)"
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚔️ 字灵确认对照",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                
                                Surface(
                                    color = badgeColor.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        color = badgeColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            
                            // 2. Dynamic Evaluation / Concise Description Text
                            val evaluationText = when (feedbackState) {
                                is DictationFeedbackState.Correct -> "笔迹端正，完美契合字灵之气！确认无误即可推进。"
                                is DictationFeedbackState.NeedsImprovement -> (feedbackState as DictationFeedbackState.NeedsImprovement).reason.ifBlank { "笔迹有些微偏差，建议点击左下「重新书写」重载该字。" }
                                is DictationFeedbackState.Wrong -> (feedbackState as DictationFeedbackState.Wrong).reason.ifBlank { "未击中魔兽弱点，建议对照标准字结构重新铭刻。" }
                                is DictationFeedbackState.Submitting -> "正在凝聚念力，评测字灵笔法中..."
                                else -> "请对照标准字，点击「确认并继续」推进，或点击下方字槽重新书写。"
                            }

                            Text(
                                text = evaluationText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (feedbackState is DictationFeedbackState.Wrong) Color(0xFFFF5252) else Color(0xFF94A3B8),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 2,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // 3. Comparison Grid Row
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until charCount) {
                                    val isSelected = i == reviewSelectedIndex
                                    val ans = currentCharAnswers.find { it.charIndex == i }
                                    val expectedChar = wordText.getOrNull(i)?.toString() ?: ""
                                    val recognizedChar = ans?.recognitionResult?.recognizedText ?: ""
                                    
                                    Card(
                                        modifier = Modifier
                                            .weight(1f, fill = charCount > 1)
                                            .sizeIn(maxWidth = 100.dp, maxHeight = 100.dp)
                                            .aspectRatio(1f)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E2235),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { 
                                                reviewSelectedIndex = i 
                                                isRewriting = true
                                                feedbackState = DictationFeedbackState.Idle
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0x1F00E5FF) else Color(0xFF070913)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // User handwriting strokes
                                            if (ans != null && !ans.isBlank) {
                                                MiniCanvas(
                                                    strokes = ans.strokes,
                                                    originalWidth = ans.canvasWidth,
                                                    originalHeight = ans.canvasHeight,
                                                    cols = 1,
                                                    rows = 1,
                                                    isDarkTheme = true,
                                                    brushId = equippedBrushId
                                                )
                                            } else {
                                                Text("空阵", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                            }
                                            
                                            // Top Left Index
                                            Surface(
                                                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E2235),
                                                shape = RoundedCornerShape(bottomEnd = 6.dp),
                                                modifier = Modifier.align(Alignment.TopStart)
                                            ) {
                                                Text(
                                                    text = "${i + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.Black else Color.Gray,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "💡 选定下方字槽进行覆写，当前：${reviewSelectedIndex + 1} 号字槽",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // 4. Canvas ToolBar
                HandwritingToolBar(
                    onClear = { handwritingView?.clear() },
                    onUndo = { handwritingView?.undo() },
                    onShowTip = { showMeaningDialog = true },
                    userBackgroundChoice = userBackgroundChoice,
                    onBackgroundChoiceChange = { userBackgroundChoice = it },
                    isBlackBrush = isBlackBrush
                )

                // 5. Large Steady Action Button Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isButtonsEnabled = feedbackState == DictationFeedbackState.Idle
                    
                    if (!isCurrentQuestionComplete) {
                        Button(
                            enabled = isButtonsEnabled,
                            onClick = {
                                val strokes = handwritingView?.getStrokes() ?: emptyList()
                                if (strokes.isEmpty()) {
                                    android.widget.Toast.makeText(context, "请先完成书写 ✍️", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    val isLastChar = currentCharIndex == charCount - 1
                                    val w = handwritingView?.width?.toFloat() ?: 1f
                                    val h = handwritingView?.height?.toFloat() ?: 1f
                                    
                                    if (!isLastChar) {
                                        viewModel.saveCurrentChar(strokes, w, h)
                                        handwritingView?.clear()
                                    } else {
                                        coroutineScope.launch {
                                            feedbackState = DictationFeedbackState.Submitting
                                            viewModel.saveCurrentChar(strokes, w, h)
                                            delay(150)
                                            feedbackState = DictationFeedbackState.Idle
                                            handwritingView?.clear()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .fillMaxHeight()
                                .testTag("btn_save_char")
                        ) {
                            Text(
                                text = "💾 铭刻第 ${currentCharIndex + 1} 字",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                    } else if (isRewriting) {
                        Button(
                            enabled = isButtonsEnabled,
                            onClick = {
                                val strokes = handwritingView?.getStrokes() ?: emptyList()
                                if (strokes.isEmpty()) {
                                    android.widget.Toast.makeText(context, "请先完成书写 ✍️", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    coroutineScope.launch {
                                        feedbackState = DictationFeedbackState.Submitting
                                        val w = handwritingView?.width?.toFloat() ?: 1f
                                        val h = handwritingView?.height?.toFloat() ?: 1f
                                        viewModel.replaceCharAnswer(reviewSelectedIndex, strokes, w, h)
                                        isRewriting = false
                                        delay(150)
                                        feedbackState = DictationFeedbackState.Idle
                                        handwritingView?.clear()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9100),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .fillMaxHeight()
                        ) {
                            Text("保存替换并覆写 💾", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        
                        OutlinedButton(
                            enabled = isButtonsEnabled,
                            onClick = { 
                                isRewriting = false 
                                feedbackState = DictationFeedbackState.Idle
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFF5252)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Text("取消重写", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // All chars complete - ready for final submit (Review Mode Bottom Buttons)
                        val isSubmitButtonsEnabled = !isSubmittingWord && feedbackState != DictationFeedbackState.Submitting
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Left button: 重新书写 (Calls removeLastCharAnswer and reverts back to canvas)
                            OutlinedButton(
                                enabled = isSubmitButtonsEnabled,
                                onClick = {
                                    viewModel.removeLastCharAnswer()
                                    feedbackState = DictationFeedbackState.Idle
                                    handwritingView?.clear()
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    Color(0xFFFF5252).copy(alpha = 0.8f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFFF5252)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .testTag("btn_rewrite_word")
                            ) {
                                Text("🔄 重新书写", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            // Right button: 确认并继续 / 确认并完成
                            Button(
                                enabled = isSubmitButtonsEnabled,
                                onClick = {
                                    if (!isSubmittingWord) {
                                        isSubmittingWord = true
                                        coroutineScope.launch {
                                            viewModel.submitWordAndNext(null, 1f, 1f)
                                            feedbackState = DictationFeedbackState.Idle
                                            isSubmittingWord = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E676),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .fillMaxHeight()
                                    .testTag("btn_confirm_continue")
                            ) {
                                Text(
                                    text = if (isLastQuestion) "🔮 确认并完成" else "🔮 确认并继续",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                // 6. Inline Skip / Avoid Feature
                if (!isCurrentQuestionComplete) {
                    TextButton(
                        enabled = feedbackState == DictationFeedbackState.Idle,
                        onClick = { showNextWarning = true },
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("⏩ 规避当前魔兽 (不计入本次净化)", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }

        if (showNextWarning) {
            AlertDialog(
                onDismissRequest = { showNextWarning = false },
                title = { Text("规避确认") },
                text = { Text("字阵尚未完全铭刻，确定规避该魔物吗？(此题将被视为未命中)") },
                confirmButton = {
                    TextButton(onClick = {
                        showNextWarning = false
                        val strokes = handwritingView?.getStrokes() ?: emptyList()
                        val w = handwritingView?.width?.toFloat() ?: 1f
                        val h = handwritingView?.height?.toFloat() ?: 1f
                        viewModel.submitWordAndNext(strokes, w, h)
                    }) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNextWarning = false }) { Text("取消") }
                }
            )
        }
    }
}

fun getAnswerRecognitionSuggestion(answer: Answer): String {
    val nonNullResults = answer.charAnswers.mapNotNull { it.recognitionResult }
    if (nonNullResults.isEmpty()) return "识别未启用"
    
    val isAnyRecognizing = nonNullResults.any { it.recognizedText == "识别中..." }
    if (isAnyRecognizing) return "正在识别中..."
    
    val isAnyFailed = nonNullResults.any { it.errorMessage != null && it.errorMessage != "正在识别..." }
    if (isAnyFailed) return "识别建议：识别不完整，请人工检查"
    
    val isAnyUnknown = nonNullResults.any { it.confidenceLevel == ConfidenceLevel.UNKNOWN }
    if (isAnyUnknown) return "识别建议：识别不完整，请人工检查"
    
    val isAnyLow = nonNullResults.any { it.confidenceLevel == ConfidenceLevel.LOW }
    if (isAnyLow) {
        return "识别建议：需要家长检查"
    }
    
    return "识别建议：疑似正确"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptanceScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val levelName by viewModel.levelName.collectAsState()
    val answers by viewModel.answers.collectAsState()
    val judgments by viewModel.judgments.collectAsState()
    var showIncompleteWarning by remember { mutableStateOf(false) }
    
    var zoomAnswer by remember { mutableStateOf<Answer?>(null) }
    var zoomIndex by remember { mutableStateOf(-1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统一验收 - $levelName") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("已判定: ${judgments.size} / ${answers.size}")
                    Button(onClick = { 
                        if (judgments.size < answers.size) {
                            showIncompleteWarning = true
                        } else {
                            viewModel.submitAcceptance()
                        }
                    }) {
                        Text("提交验收")
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.markAllCorrect() }, 
                    modifier = Modifier.weight(1.5f)
                ) {
                    Text("一键全标为【写对了】", fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        answers.forEachIndexed { idx, ans ->
                            val suggestion = getAnswerRecognitionSuggestion(ans)
                            if (suggestion == "识别建议：疑似正确") {
                                viewModel.setJudgment(idx, "CORRECT")
                            }
                        }
                    },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("按识别结果预填判定", fontSize = 12.sp)
                }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(answers.size) { index ->
                    val answer = answers[index]
                    val judgment = judgments[index]
                    
                    AnswerReviewCard(
                        index = index,
                        answer = answer,
                        judgment = judgment,
                        onJudgment = { res -> viewModel.setJudgment(index, res) },
                        onClick = {
                            zoomIndex = index
                            zoomAnswer = answer
                        }
                    )
                }
            }
        }
        
        if (showIncompleteWarning) {
            AlertDialog(
                onDismissRequest = { showIncompleteWarning = false },
                title = { Text("提示") },
                text = { Text("还有题目没有验收，请全部判定后再提交。") },
                confirmButton = { TextButton(onClick = { showIncompleteWarning = false }) { Text("好的") } }
            )
        }
        
        zoomAnswer?.let { ans ->
            AnswerZoomDialog(
                index = zoomIndex,
                answer = ans,
                judgment = judgments[zoomIndex],
                onJudgment = { res -> 
                    viewModel.setJudgment(zoomIndex, res)
                    zoomAnswer = null
                    zoomIndex = -1
                },
                onDismiss = {
                    zoomAnswer = null
                    zoomIndex = -1
                }
            )
        }
    }
}

@Composable
fun AnswerReviewCard(index: Int, answer: Answer, judgment: String?, onJudgment: (String) -> Unit, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("第 ${index + 1} 题", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                
                val suggestion = getAnswerRecognitionSuggestion(answer)
                if (suggestion.isNotEmpty()) {
                    val color = when (suggestion) {
                        "识别建议：疑似正确" -> Color(0xFF2E7D32)
                        "识别建议：需要家长检查" -> Color(0xFFE65100)
                        "识别建议：识别不完整，请人工检查", "识别建议：识别不完整，请人工检查" -> Color(0xFFC62828)
                        else -> Color.Gray
                    }
                    Text(
                        text = suggestion,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = color
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            val wordText = answer.word.text.filter { it.isLetterOrDigit() }
            val cols = minOf(wordText.length, 4) // max 4 cols per row
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val chunks = wordText.chunked(cols)
                val charAnsChunks = answer.charAnswers.chunked(cols)
                
                for (rowIndex in chunks.indices) {
                    val wordRow = chunks[rowIndex]
                    val ansRow = charAnsChunks.getOrNull(rowIndex) ?: emptyList()
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        for (cIndex in wordRow.indices) {
                            val char = wordRow[cIndex]
                            val ans = ansRow.getOrNull(cIndex)
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.size(40.dp).background(Color(0xFFE8F5E9)).border(1.dp, Color(0xFF4CAF50)), contentAlignment = Alignment.Center) {
                                    Text(char.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .background(Color(0xFFFDF6E3))
                                        .border(1.dp, Color.LightGray)
                                        .clickable(onClick = onClick)
                                ) {
                                    if (ans == null || ans.isBlank) {
                                        Text("空白", modifier = Modifier.align(Alignment.Center), color = Color.Gray, fontSize = 12.sp)
                                    } else {
                                        MiniCanvas(ans.strokes, ans.canvasWidth, ans.canvasHeight, 1, 1)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val rec = ans?.recognitionResult
                                if (rec == null) {
                                    Text("识别未启用", fontSize = 10.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                } else if (rec.recognizedText == "识别中...") {
                                    Text("AI识别：识别中", fontSize = 10.sp, color = Color(0xFFF57C00), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                } else if (rec.errorMessage != null) {
                                    Text("识别失败，请检查", fontSize = 10.sp, color = Color(0xFFF44336), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    Text("(${rec.errorMessage})", fontSize = 8.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                } else {
                                    Text("AI识别: ${rec.recognizedText}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (rec.isLikelyCorrect) Color(0xFF2E7D32) else Color(0xFFF44336))
                                    Text(
                                        text = if (rec.isLikelyCorrect) "可能正确" else "需要检查",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (rec.isLikelyCorrect) Color(0xFF2E7D32) else Color(0xFFF44336)
                                    )
                                    if (!rec.isLikelyCorrect && rec.candidates.isNotEmpty()) {
                                        Text("候选: ${rec.candidates.take(3).joinToString("/")}", fontSize = 8.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }
                        }
                        for (i in wordRow.length until cols) {
                             Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(
                    selected = judgment == "CORRECT",
                    onClick = { onJudgment("CORRECT") },
                    label = { Text("写对了") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4CAF50), selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = judgment == "ALMOST",
                    onClick = { onJudgment("ALMOST") },
                    label = { Text("差一点") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFC107), selectedLabelColor = Color.Black)
                )
                FilterChip(
                    selected = judgment == "WRONG",
                    onClick = { onJudgment("WRONG") },
                    label = { Text("写错了") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFF44336), selectedLabelColor = Color.White)
                )
            }
        }
    }
}

@Composable
fun AnswerZoomDialog(index: Int, answer: Answer, judgment: String?, onJudgment: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("第 ${index + 1} 题大图验收")
                val suggestion = getAnswerRecognitionSuggestion(answer)
                if (suggestion.isNotEmpty()) {
                    val color = when (suggestion) {
                        "识别建议：疑似正确" -> Color(0xFF2E7D32)
                        "识别建议：需要家长检查" -> Color(0xFFE65100)
                        "识别建议：识别不完整，请人工检查", "识别建议：识别不完整，请人工检查" -> Color(0xFFC62828)
                        else -> Color.Gray
                    }
                    Text(suggestion, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                val wordText = answer.word.text.filter { it.isLetterOrDigit() }
                val cols = minOf(wordText.length, 3)
                
                val chunks = wordText.chunked(cols)
                val charAnsChunks = answer.charAnswers.chunked(cols)
                
                for (rowIndex in chunks.indices) {
                    val wordRow = chunks[rowIndex]
                    val ansRow = charAnsChunks.getOrNull(rowIndex) ?: emptyList()
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        for (cIndex in wordRow.indices) {
                            val char = wordRow[cIndex]
                            val ans = ansRow.getOrNull(cIndex)
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.size(56.dp).background(Color(0xFFE8F5E9)).border(1.dp, Color(0xFF4CAF50)), contentAlignment = Alignment.Center) {
                                    Text(char.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .background(Color(0xFFFDF6E3))
                                        .border(1.dp, Color.LightGray)
                                ) {
                                    if (ans == null || ans.isBlank) {
                                        Text("空白", modifier = Modifier.align(Alignment.Center), color = Color.Gray, fontSize = 16.sp)
                                    } else {
                                        MiniCanvas(ans.strokes, ans.canvasWidth, ans.canvasHeight, 1, 1)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val rec = ans?.recognitionResult
                                if (rec == null) {
                                    Text("识别未启用", fontSize = 12.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                } else if (rec.recognizedText == "识别中...") {
                                    Text("AI识别：识别中", fontSize = 12.sp, color = Color(0xFFF57C00), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                } else if (rec.errorMessage != null) {
                                    Text("识别失败，请检查", fontSize = 12.sp, color = Color(0xFFF44336), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    Text("(${rec.errorMessage})", fontSize = 10.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                } else {
                                    Text("AI识别: ${rec.recognizedText}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (rec.isLikelyCorrect) Color(0xFF2E7D32) else Color(0xFFF44336))
                                    Text(
                                        text = if (rec.isLikelyCorrect) "可能正确" else "需要检查",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (rec.isLikelyCorrect) Color(0xFF2E7D32) else Color(0xFFF44336)
                                    )
                                    if (!rec.isLikelyCorrect && rec.candidates.isNotEmpty()) {
                                        Text("候选: ${rec.candidates.take(5).joinToString("/")}", fontSize = 10.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }
                        }
                        for (i in wordRow.length until cols) {
                             Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { onJudgment("CORRECT") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("写对了") }
                    Button(onClick = { onJudgment("ALMOST") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))) { Text("差一点") }
                    Button(onClick = { onJudgment("WRONG") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))) { Text("写错了") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
fun MiniCanvas(
    strokes: List<StrokeData>,
    originalWidth: Float,
    originalHeight: Float,
    cols: Int,
    rows: Int,
    isDarkTheme: Boolean = true,
    brushId: String? = null
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val scaleX = size.width / originalWidth
        val scaleY = size.height / originalHeight
        val scaleFactor = minOf(scaleX, scaleY)
        
        // Center the scaled canvas
        val dx = (size.width - originalWidth * scaleFactor) / 2
        val dy = (size.height - originalHeight * scaleFactor) / 2

        val gridLineColor = if (isDarkTheme) Color(0xFF1E2235) else Color.LightGray.copy(alpha = 0.5f)
        val gridBorderColor = if (isDarkTheme) Color(0xFF1E2235) else Color.LightGray

        translate(left = dx, top = dy) {
            scale(scaleFactor, scaleFactor, pivot = Offset.Zero) {
                // draw grid
                val cellW = originalWidth / cols
                val cellH = originalHeight / rows

                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val left = c * cellW
                        val top = r * cellH
                        val right = left + cellW
                        val bottom = top + cellH
                        
                        // drawRect uses float coordinates in Compose Canvas directly
                        drawRect(
                            color = gridBorderColor,
                            topLeft = Offset(left, top),
                            size = androidx.compose.ui.geometry.Size(cellW, cellH),
                            style = Stroke(width = 4f)
                        )
                        
                        drawLine(
                            color = gridLineColor,
                            start = Offset(left, top + cellH / 2),
                            end = Offset(right, top + cellH / 2),
                            strokeWidth = 2f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawLine(
                            color = gridLineColor,
                            start = Offset(left + cellW / 2, top),
                            end = Offset(left + cellW / 2, bottom),
                            strokeWidth = 2f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }
                }

                // draw strokes
                val strokeStyle = Stroke(
                    width = 24f, // original handwriting stroke width
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
                
                val isBlackBrush = brushId == "default_black" || brushId == "ink_brush"
                val strokeColor = if (isBlackBrush) {
                    if (isDarkTheme) Color(0xFF00E5FF) else Color.Black
                } else {
                    val brush = brushId?.let { BrushStyle.getBrushById(it) }
                    if (brush != null) {
                        if (isDarkTheme && brush.baseColor == android.graphics.Color.BLACK) {
                            Color(0xFF00E5FF)
                        } else {
                            Color(brush.baseColor)
                        }
                    } else {
                        if (isDarkTheme) Color(0xFF00E5FF) else Color.Black
                    }
                }

                strokes.forEach { strokeData ->
                    drawPath(
                        path = strokeData.path.asComposePath(),
                        color = strokeColor,
                        style = strokeStyle
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNavigateToReport: () -> Unit = {},
    onNavigateToReview: () -> Unit = {}
) {
    val result by viewModel.settlementResult.collectAsState()
    val activePet by viewModel.activePet.collectAsState()
    val battleProcessResult by viewModel.battleProcessResult.collectAsState()
    val lastResults by viewModel.lastQuestionResults.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val rewardClaimState by viewModel.rewardClaimState.collectAsState()
    val sessionId = viewModel.currentSessionIdExposed
    
    LaunchedEffect(sessionId) {
        if (sessionId != -1) {
            val claimed = viewModel.isSessionRewardClaimed(sessionId)
            if (claimed) {
                viewModel.autoClaimSessionRewardIfPending(sessionId, result?.coinsGained ?: 0, result?.expGained ?: 0, result?.intimacyGained ?: 0)
            } else {
                viewModel.rewardClaimState.value = RewardClaimState.Idle
            }
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    var showDetails by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚔️ 战役结算战报 ⚔️", fontWeight = FontWeight.Black, color = Color(0xFF00E5FF)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F111A),
                    titleContentColor = Color(0xFF00E5FF)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0C0D14))
                .padding(paddingValues)
        ) {
            // 1. Scrollable Main Content (Scrolling block)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 110.dp), // Large bottom spacing for fixed bottom bar
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // 第一层：完成反馈 (Completion Header)
                val isCleared = result?.isClear == true
                val titleText = if (isCleared) {
                    if (result?.correctCount == result?.totalWords) "🏆 完美全歼魔物 🏆" else "⚔️ 战役大捷 ⚔️"
                } else {
                    "💀 战役溃败 💀"
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCleared) Color(0xFF00E676) else Color(0xFFFF1744)
                    )
                    
                    if (result?.title?.isNotBlank() == true) {
                        Text(
                            text = result?.title ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    }
                }
                
                // 第二层：本次成绩与奖励 (Rewards & Loot Card)
                val isClaimed = rewardClaimState is RewardClaimState.Claimed
                val cardBorderColor = when (rewardClaimState) {
                    is RewardClaimState.Claimed -> Color(0xFF4CAF50)
                    is RewardClaimState.Claiming -> Color(0xFF00E5FF)
                    else -> Color(0xFFFFB300).copy(alpha = borderAlpha)
                }
                val cardBgColor = when (rewardClaimState) {
                    is RewardClaimState.Claimed -> Color(0xFF0F1E15)
                    else -> Color(0xFF141724)
                }
                val contentAlpha = when (rewardClaimState) {
                    is RewardClaimState.Claimed -> 1.0f
                    is RewardClaimState.Claiming -> 0.6f
                    else -> 0.8f
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, cardBorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎁 本次战利品与奖励",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isClaimed) Color(0xFF81C784) else Color(0xFFFFC107)
                            )
                            
                            // Status Tag
                            when (rewardClaimState) {
                                is RewardClaimState.Claimed -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("✨", fontSize = 12.sp)
                                        Text("已存入冒险行囊", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                is RewardClaimState.Claiming -> {
                                    Text("⚡ 注入灵力中...", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                else -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("🔒 待领取", color = Color(0xFFFF9800), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().alpha(contentAlpha),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Coins Reward
                            val coinBg = if (isClaimed) Color(0xFF1B3B24) else Color(0xFF3B2F0F)
                            val coinBorder = if (isClaimed) Color(0xFF81C784) else Color(0xFFFBBF24)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(coinBg, RoundedCornerShape(12.dp))
                                    .border(1.dp, coinBorder, RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🪙", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("金币", fontSize = 11.sp, color = Color.White)
                                    Text("+${result?.coinsGained ?: 0}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = coinBorder)
                                }
                            }
                            
                            // EXP Reward
                            val expBg = if (isClaimed) Color(0xFF143B33) else Color(0xFF14302E)
                            val expBorder = if (isClaimed) Color(0xFF4DB6AC) else Color(0xFF2DD4BF)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(expBg, RoundedCornerShape(12.dp))
                                    .border(1.dp, expBorder, RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("✨", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("经验值", fontSize = 11.sp, color = Color.White)
                                    Text("+${result?.expGained ?: 0}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = expBorder)
                                }
                            }
                            
                            // Pet Intimacy Reward (if applicable)
                            val intimacyGained = result?.intimacyGained ?: 0
                            if (intimacyGained > 0 && activePet != null) {
                                val intimacyBg = if (isClaimed) Color(0xFF3C1E2F) else Color(0xFF2E1020)
                                val intimacyBorder = if (isClaimed) Color(0xFFF48FB1) else Color(0xFFF472B6)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(intimacyBg, RoundedCornerShape(12.dp))
                                        .border(1.dp, intimacyBorder, RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🐾", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("亲密度", fontSize = 11.sp, color = Color.White)
                                        Text("+$intimacyGained", fontSize = 14.sp, fontWeight = FontWeight.Black, color = intimacyBorder)
                                    }
                                }
                            }
                        }
                        
                        // Active pet detail label
                        val intimacyGainedVal = result?.intimacyGained ?: 0
                        if (intimacyGainedVal > 0 && activePet != null) {
                            Text(
                                text = "🐾 伙伴「${activePet?.petName}」(Lv.${activePet?.level ?: 1}) 亲密度增至: ${activePet?.intimacy ?: 100}/100",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        
                        // Boss defeat special chests
                        if (result?.isBossDefeated == true) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "👹 领主 BOSS 侵蚀魔兽已被彻底击破！额外宝箱奖励已发放！",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        
                        // Extra Honor Titles gained (Title System MVP)
                        val isPerfect = result != null && result!!.correctCount == result!!.totalWords && result!!.totalWords > 0
                        val isComboMaster = result != null && result!!.maxCombo >= 5
                        
                        if (isPerfect || isComboMaster) {
                            Text(
                                text = "👑 荣获荣誉称号",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFFF9800)
                            )
                            
                            if (isPerfect) {
                                Surface(
                                    color = Color(0xFF1B3B24),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "🏆 获得称号：【完美净化者】",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF81C784),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "✨ 达成 100% 满分通关！已发放至您的冒险称号库！",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFB9F6CA)
                                        )
                                    }
                                }
                            }
                            
                            if (isComboMaster) {
                                Surface(
                                    color = Color(0xFF4E2C15),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "🔥 获得称号：【连击大师】",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFB74D),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "✨ 达成 5 连击或以上！已发放至您的冒险称号库！",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFFFE0B2)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Loot Drops Cards (Only display if drops exist)
                        if (battleProcessResult != null && !battleProcessResult!!.drops.isEmpty()) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            Text(
                                text = "🎁 本次额外掉落：",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFC107)
                            )
                            
                            val chunks = battleProcessResult!!.drops.chunked(3)
                            chunks.forEach { chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    chunk.forEach { drop ->
                                        val (bg, border, emoji) = when (drop.lootType) {
                                            "WORD_SHARD" -> Triple(Color(0xFF1E293B), Color(0xFF94A3B8), "🎖️")
                                            "GOLD" -> Triple(Color(0xFF3B2F0F), Color(0xFFFBBF24), "🪙")
                                            "EXP" -> Triple(Color(0xFF14302E), Color(0xFF2DD4BF), "✨")
                                            "HATCH_ENERGY" -> Triple(Color(0xFF2E1065), Color(0xFFA78BFA), "🔮")
                                            "BRUSH_SHARD" -> Triple(Color(0xFF1F2937), Color(0xFFFBBF24), "🖌️")
                                            "BOSS_SHARD" -> Triple(Color(0xFF451A03), Color(0xFFF97316), "🐉")
                                            "JINGHUA" -> Triple(Color(0xFF172554), Color(0xFF60A5FA), "💎")
                                            else -> Triple(Color(0xFF1F2937), Color(0xFF9CA3AF), "📦")
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(bg, RoundedCornerShape(10.dp))
                                                .border(1.dp, border.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(emoji, fontSize = 18.sp)
                                                Spacer(modifier = Modifier.height(1.dp))
                                                Text(
                                                    text = drop.lootName,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                                Text(
                                                    text = "+${drop.amount}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = border
                                                )
                                            }
                                        }
                                    }
                                    for (i in chunk.size until 3) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Newly Unlocked Monsters and Codex (Render dynamically below Rewards Card if any)
                if (battleProcessResult != null && !battleProcessResult!!.newMonstersUnlocked.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF101322)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "😈 击退并净化字词魔物",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF00E5FF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            battleProcessResult!!.newMonstersUnlocked.forEach { monster ->
                                val borderCol = when (monster.rarity) {
                                    "LEGEND" -> Color(0xFFFFD700)
                                    "EPIC" -> Color(0xFFD500F9)
                                    "RARE" -> Color(0xFF00B0FF)
                                    "UNCOMMON" -> Color(0xFF00E676)
                                    else -> Color(0xFF9E9E9E)
                                }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(Color(0xFF0D0F1A), RoundedCornerShape(10.dp))
                                        .border(1.dp, borderCol.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val emoji = when (monster.monsterType) {
                                        "BOSS" -> "🐲"
                                        "ELITE" -> "😈"
                                        "POEM_GUARD" -> "📜"
                                        "WRONG_WORD" -> "🦹"
                                        else -> "👾"
                                    }
                                    
                                    Text(emoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = monster.monsterName,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = when (monster.rarity) {
                                                    "LEGEND" -> "传奇"
                                                    "EPIC" -> "史诗"
                                                    "RARE" -> "稀有"
                                                    "UNCOMMON" -> "精良"
                                                    else -> "普通"
                                                },
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = borderCol,
                                                modifier = Modifier
                                                    .background(borderCol.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Text(
                                            text = "来源: ${monster.sourceType}",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        if (monster.isPurified) {
                                            Text("✨ 净化成功", color = Color(0xFF4CAF50), fontWeight = FontWeight.Black, fontSize = 11.sp)
                                            Text("代表: ${monster.relatedWordFullForParent}", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("💀 逃逸 (仅遇见)", color = Color(0xFFFF5722), fontWeight = FontWeight.Black, fontSize = 11.sp)
                                            Text("代表: ${monster.relatedWordMasked}", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Newly Unlocked Achievements (Render dynamically below Rewards Card if any)
                if (battleProcessResult != null && !battleProcessResult!!.newlyUnlockedAchievements.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1B12)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF9800)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🏆 达成全新荣誉成就！",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            battleProcessResult!!.newlyUnlockedAchievements.forEach { ach ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(Color(0xFF14110A), RoundedCornerShape(10.dp))
                                        .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🥇", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ach.achievementName,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = ach.achievementDesc,
                                            fontSize = 10.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("已达成", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        Text("🪙 +${ach.rewardCoins}", color = Color(0xFFFFC107), fontWeight = FontWeight.Black, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 第三层：本次课程结果摘要 (This Course Summary)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "📊 本课战斗大捷摘要",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val purifyRate = if (result != null && result!!.totalWords > 0) (result!!.correctCount * 100) / result!!.totalWords else 0
                            
                            Column {
                                Text("总题数/字词魔物: ${result?.totalWords ?: 0} 题", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
                                Text("🔥 最高连击 (Combo): ${result?.maxCombo ?: 0} 次", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFF5722), fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("净化率: $purifyRate%", fontWeight = FontWeight.ExtraBold, color = if (purifyRate >= 80) Color(0xFF4CAF50) else Color(0xFFFF9800), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF143B1B), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("✨ 完美", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("${result?.correctCount ?: 0} 只", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF3F3B1A), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🩹 受伤", color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("${result?.almostCount ?: 0} 只", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF451E1A), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💀 逃逸", color = Color(0xFFE57373), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("${result?.wrongCount ?: 0} 只", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
                
                // 第五层：独立错题成长档案卡 (Independent Wrong Words Entry)
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("settlement_wrong_word_archive_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C24)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFA000).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("📝 错题成长档案", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            val wrongCount = result?.newWrongWordsCount ?: 0
                            Text(
                                text = if (wrongCount > 0) {
                                    "本次听写有 ${wrongCount} 只字词魔物慢入「错题魔窟」"
                                } else {
                                    "本次字词全部消灭净化，未产生任何心魔！"
                                },
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                        
                        Button(
                            onClick = {
                                viewModel.autoClaimSessionRewardIfPending(sessionId, result?.coinsGained ?: 0, result?.expGained ?: 0, result?.intimacyGained ?: 0)
                                onNavigateToReview()
                            },
                            modifier = Modifier.height(36.dp).testTag("settlement_btn_wrong_book"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("进入错题本", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                
                // AI Auto-Graded Info Card
                if (result?.isAutoGraded == true) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🤖 智能助手自动机器批改",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "已由 AI 墨迹识别初判，家长可在练习历史中随时复核孩子手写笔迹。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray,
                                    lineHeight = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    viewModel.autoClaimSessionRewardIfPending(sessionId, result?.coinsGained ?: 0, result?.expGained ?: 0, result?.intimacyGained ?: 0)
                                    onNavigateToReport()
                                },
                                modifier = Modifier.testTag("settlement_btn_review_history")
                            ) {
                                Text("去复核", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                            }
                        }
                    }
                }
                
                // 第四层：可折叠的详细手写对照 (Collapsible Handwriting Comparison Details)
                if (lastResults.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Collapsible trigger header
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDetails = !showDetails }
                                .testTag("settlement_details_collapse_header"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✍️", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (showDetails) "收起本次书写对照" else "展开本次书写对照",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E5FF),
                                        fontSize = 13.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.rotate(if (showDetails) 180f else 0f)
                                )
                            }
                        }
                        
                        // Expanded body (rendered only when showDetails is true)
                        if (showDetails) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF121420))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🔍 本次听写错因对比与笔迹", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                        Text("共 ${lastResults.size} 题", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    
                                    lastResults.forEach { qRes ->
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (qRes.finalResult == "CORRECT") Color(0xFF1B3B24).copy(alpha = 0.3f) else Color(0xFF451E1A).copy(alpha = 0.3f)
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, if (qRes.finalResult == "CORRECT") Color(0xFF4CAF50).copy(alpha = 0.3f) else Color(0xFFF44336).copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "正确答案: ${qRes.getEffectiveTargetAnswer()} (${qRes.correctText})",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color.White
                                                    )
                                                    val (statusColor, statusText) = when (qRes.finalResult) {
                                                        "CORRECT" -> Pair(Color(0xFF81C784), "正确")
                                                        "PARTIAL" -> Pair(Color(0xFFFFB74D), "差一点")
                                                        else -> Pair(Color(0xFFE57373), "写错/未写")
                                                    }
                                                    Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                                }
                                                
                                                if (qRes.getEffectiveVisiblePrompt().isNotBlank() && qRes.getEffectiveVisiblePrompt() != qRes.correctText) {
                                                    Text("提示词: ${qRes.getEffectiveVisiblePrompt()}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF80DEEA))
                                                }
                                                if (qRes.clueText.isNotBlank()) {
                                                    Text("语境短语: ${qRes.clueText}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFD54F))
                                                }
                                                if (qRes.meaningHint.isNotBlank()) {
                                                    Text("字词含义: ${qRes.meaningHint}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                                }
                                                
                                                // --- Handwritten Strokes Canvas vs Reference Character Grid ---
                                                if (qRes.charResults.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "✍️ 孩子笔迹与标准字对照：",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.LightGray
                                                    )
                                                    
                                                    val cols = minOf(qRes.charResults.size, 4)
                                                    val charChunks = qRes.charResults.chunked(cols)
                                                    
                                                    for (row in charChunks) {
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.Top
                                                        ) {
                                                            for (charRes in row) {
                                                                Column(
                                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                                    modifier = Modifier.weight(1f)
                                                                ) {
                                                                    // Top: Standard reference box
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(36.dp)
                                                                            .background(Color(0xFF1B3B24), RoundedCornerShape(4.dp))
                                                                            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(4.dp)),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        Text(
                                                                            text = charRes.expectedChar,
                                                                            fontSize = 20.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = Color(0xFF81C784)
                                                                        )
                                                                    }
                                                                    Text("标准字", fontSize = 8.sp, color = Color(0xFF81C784))
                                                                    
                                                                    Spacer(modifier = Modifier.height(2.dp))
                                                                    
                                                                    // Bottom: Child's handwritten stroke canvas
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .aspectRatio(1f)
                                                                            .background(Color(0xFFFDF6E3), RoundedCornerShape(4.dp))
                                                                            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                                                    ) {
                                                                        if (charRes.isBlank || charRes.pointsList.isEmpty()) {
                                                                            Text(
                                                                                "空白",
                                                                                modifier = Modifier.align(Alignment.Center),
                                                                                color = Color.Gray,
                                                                                fontSize = 10.sp
                                                                            )
                                                                        } else {
                                                                            StrokePreview(
                                                                                pointsList = charRes.pointsList,
                                                                                canvasWidth = charRes.canvasWidth,
                                                                                canvasHeight = charRes.canvasHeight,
                                                                                modifier = Modifier.fillMaxSize().padding(2.dp)
                                                                            )
                                                                        }
                                                                    }
                                                                    
                                                                    Spacer(modifier = Modifier.height(2.dp))
                                                                    
                                                                    Text(
                                                                        text = if (charRes.isBlank) "未书写" else "AI: ${charRes.recognizedText.ifEmpty { "无" }}",
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = if (charRes.isLikelyCorrect) Color(0xFF4CAF50) else Color(0xFFE57373),
                                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                                    )
                                                                    
                                                                    if (!charRes.isBlank && charRes.candidates.isNotEmpty()) {
                                                                        Text(
                                                                            text = "候选: ${charRes.candidates.take(2).joinToString("/")}",
                                                                            fontSize = 7.sp,
                                                                            color = Color.Gray,
                                                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                                            maxLines = 1
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                            
                                                            for (i in row.size until cols) {
                                                                Spacer(modifier = Modifier.weight(1f))
                                                            }
                                                        }
                                                    }
                                                }
                                                
                                                val candidateSummary = qRes.charResults.flatMap { it.candidates }.distinct().take(5).joinToString(", ")
                                                if (candidateSummary.isNotBlank()) {
                                                    Text("AI识别候选集: $candidateSummary", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                                }
                                                
                                                Text("错因说明: ${qRes.errorReason}", style = MaterialTheme.typography.bodySmall, color = if (qRes.finalResult == "CORRECT") Color(0xFF81C784) else Color(0xFFE57373), fontWeight = FontWeight.Medium)
                                                
                                                // --- Parent Quick Correction Actions ---
                                                Spacer(modifier = Modifier.height(2.dp))
                                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "家长批核修正：",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.LightGray
                                                    )
                                                    
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        val currentRes = qRes.finalResult
                                                        
                                                        FilterChip(
                                                            selected = currentRes == "CORRECT",
                                                            onClick = { viewModel.updateLastQuestionResult(qRes.questionId, "CORRECT") },
                                                            label = { Text("写对了", fontSize = 10.sp) },
                                                            colors = FilterChipDefaults.filterChipColors(
                                                                selectedContainerColor = Color(0xFF1B3B24),
                                                                selectedLabelColor = Color(0xFF81C784),
                                                                containerColor = Color.Transparent,
                                                                labelColor = Color.Gray
                                                            )
                                                        )
                                                        FilterChip(
                                                            selected = currentRes == "PARTIAL",
                                                            onClick = { viewModel.updateLastQuestionResult(qRes.questionId, "PARTIAL") },
                                                            label = { Text("差一点", fontSize = 10.sp) },
                                                            colors = FilterChipDefaults.filterChipColors(
                                                                selectedContainerColor = Color(0xFF4E2C15),
                                                                selectedLabelColor = Color(0xFFFFB74D),
                                                                containerColor = Color.Transparent,
                                                                labelColor = Color.Gray
                                                            )
                                                        )
                                                        FilterChip(
                                                            selected = currentRes == "WRONG",
                                                            onClick = { viewModel.updateLastQuestionResult(qRes.questionId, "WRONG") },
                                                            label = { Text("写错了", fontSize = 10.sp) },
                                                            colors = FilterChipDefaults.filterChipColors(
                                                                selectedContainerColor = Color(0xFF451E1A),
                                                                selectedLabelColor = Color(0xFFE57373),
                                                                containerColor = Color.Transparent,
                                                                labelColor = Color.Gray
                                                            )
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
                }
            }
            
            // 固定层：底部操作栏 (Fixed Bottom Action Bar)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF0C0D14).copy(alpha = 0.95f), Color(0xFF0C0D14))
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Secondary: "再练一次" (Solid background with outline stylish button)
                    Button(
                        onClick = {
                            viewModel.autoClaimSessionRewardIfPending(sessionId, result?.coinsGained ?: 0, result?.expGained ?: 0, result?.intimacyGained ?: 0)
                            viewModel.restartCurrentLevel()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("settlement_btn_retry"),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF141724),
                            contentColor = Color(0xFF00E5FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("再练一次", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    // Primary button: changes based on reward claim state
                    when (rewardClaimState) {
                        is RewardClaimState.Claimed -> {
                            Button(
                                onClick = onBack,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("settlement_btn_back"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("返回关卡首页", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                        is RewardClaimState.Claiming -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("settlement_btn_claim_rewards"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF555555),
                                    contentColor = Color.LightGray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("正在注入灵力...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            Button(
                                onClick = {
                                    viewModel.claimSessionReward(sessionId, result?.coinsGained ?: 0, result?.expGained ?: 0, result?.intimacyGained ?: 0)
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("settlement_btn_claim_rewards"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E5FF),
                                    contentColor = Color(0xFF0C0D14)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🎁", fontSize = 16.sp)
                                    Text("一键领取战利品", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
