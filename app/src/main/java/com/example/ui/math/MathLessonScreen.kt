package com.example.ui.math

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.math.MathEvaluationResult
import com.example.data.math.MathQuestionType
import com.example.data.math.MathBlankInputType
import com.example.data.math.MathBlankSpec
import com.example.viewmodel.math.MathLessonViewModel
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.example.viewmodel.math.MathQuestionInteractionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathLessonScreen(
    courseId: String = "math_pep_g6_s1",
    unitId: String,
    lessonId: String,
    onBack: () -> Unit,
    onComplete: (Int) -> Unit, // rewards coins
    modifier: Modifier = Modifier,
    viewModel: MathLessonViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    var isKeyboardDismissed by remember { mutableStateOf(false) }
    var isSheetOpen by remember { mutableStateOf(false) }

    // Auto open sheet when state changes to final evaluation
    LaunchedEffect(uiState.interactionState) {
        if (uiState.interactionState == MathQuestionInteractionState.CORRECT ||
            uiState.interactionState == MathQuestionInteractionState.INCORRECT ||
            uiState.interactionState == MathQuestionInteractionState.ANSWER_REVEALED
        ) {
            isSheetOpen = true
        } else {
            isSheetOpen = false
        }
    }

    // Reset keyboard dismiss state on block change
    LaunchedEffect(uiState.currentBlockIndex) {
        isKeyboardDismissed = false
    }

    // Load once
    LaunchedEffect(courseId, unitId, lessonId) {
        viewModel.loadLesson(context, courseId, unitId, lessonId)
    }

    // Intercept back actions
    BackHandler(enabled = !uiState.isFinished && !uiState.isLoading) {
        if (isSheetOpen) {
            isSheetOpen = false
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("退出本课", fontWeight = FontWeight.Bold) },
            text = { Text("本课尚未完成，现在退出将不会保存当前进度和奖励哦，确定要离开吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onBack()
                    }
                ) {
                    Text("确定退出", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("继续学习", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E293B),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (uiState.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF3B82F6))
        }
        return
    }

    val lesson = uiState.lesson
    val errorMsg = uiState.errorMessage ?: if (lesson == null) "课程内容加载失败" else null
    if (errorMsg != null || lesson == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠️ $errorMsg",
                    color = Color(0xFFEF4444),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                val debugText = uiState.debugInfo ?: "courseId: $courseId\nunitId: $unitId\nlessonId: $lessonId\n文件路径: math/pep/grade6/semester1/unit_01.json\n错误原因: 课时数据未加载成功"
                
                if (com.example.BuildConfig.DEBUG) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🖥️ 调试信息 (仅开发者可见):",
                                color = Color(0xFF60A5FA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = debugText,
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
                
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("返回数字机械城", color = Color.White)
                }
            }
        }
        return
    }

    if (uiState.isFinished) {
        MathResultScreen(
            uiState = uiState,
            onRetry = { viewModel.loadLesson(context, courseId, unitId, lessonId) },
            onBackToMap = {
                com.example.data.math.MathProgressManager.completeLesson(context, lessonId)
                onComplete(uiState.earnedCoins)
                onBack()
            }
        )
        return
    }

    val blocks = uiState.blocks
    val currentBlock = blocks.getOrNull(uiState.currentBlockIndex)

    if (currentBlock == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("没有可加载的内容块数据", color = Color.White, fontSize = 16.sp)
        }
        return
    }

    val currentQuestion = currentBlock.question

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        MathRichText(
                            text = lesson.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "数学 · 数字机械城",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            val isDevMode = remember {
                                com.example.BuildConfig.DEBUG && (
                                    com.example.data.math.DeveloperMathSettings.isBypassMathPrerequisites(context) ||
                                    com.example.data.math.DeveloperMathSettings.isUseSimulatedProgress(context)
                                )
                            }
                            if (isDevMode) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "DEBUG体验",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA78BFA)
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSheetOpen) {
                                isSheetOpen = false
                            } else {
                                showExitDialog = true
                            }
                        },
                        modifier = Modifier.testTag("math_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Help dialog
                    }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "帮助",
                            tint = Color.LightGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            if (!isSheetOpen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                ) {
                    // Keyboard or Choice Tool Panel: Show based on active blank type
                    if (currentQuestion != null && currentQuestion.type != MathQuestionType.MULTIPLE_CHOICE && 
                        uiState.interactionState == MathQuestionInteractionState.EDITING && !isKeyboardDismissed) {
                        val blankSpecs = currentQuestion.answerSpec.blankSpecs
                        val activeBlankIndex = uiState.activeBlankIndex
                        val activeSpec = blankSpecs?.getOrNull(activeBlankIndex)
                        if (activeSpec != null && activeSpec.type == MathBlankInputType.CHOICE_TEXT) {
                            MathChoiceToolPanel(
                                choices = activeSpec.choices,
                                onChoiceSelected = { text ->
                                    viewModel.updateBlankAnswer(activeBlankIndex, text)
                                },
                                onDelete = { viewModel.onDelete() }
                            )
                        } else {
                            MathNumericKeyboard(
                                onKeyPress = { viewModel.onKeyPress(it) },
                                onDelete = { viewModel.onDelete() },
                                onClear = { viewModel.onClear() },
                                onConfirm = { isKeyboardDismissed = true }
                            )
                        }
                    }

                    // Unified Fixed Dual-Action Bottom Bar
                    MathLessonBottomBar(
                        currentBlockIndex = uiState.currentBlockIndex,
                        totalBlocks = blocks.size,
                        currentBlock = currentBlock,
                        uiState = uiState,
                        onPrevious = { viewModel.goToPreviousBlock() },
                        onNext = { viewModel.goToNextBlock() },
                        onSubmit = { viewModel.submitAnswer() },
                        onClear = { viewModel.retryQuestion() }, // Align onClear click to retryQuestion
                        onReopenSheet = { isSheetOpen = true }
                    )
                }
            }
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF0B132B)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress bar
                val progress = (uiState.currentBlockIndex + 1).toFloat() / blocks.size
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MathRichText(
                            text = "课堂环节：${currentBlock.title}",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.currentBlockIndex + 1} / ${blocks.size}",
                            color = Color(0xFF00E5FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        color = Color(0xFF00E5FF),
                        trackColor = Color(0xFF1E293B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }

                if (currentQuestion != null) {
                    // Question block
                    MathQuestionRenderer(
                        question = currentQuestion,
                        uiState = uiState,
                        onChoiceSelected = { viewModel.selectChoice(it) },
                        onNumeratorFocused = { 
                            isKeyboardDismissed = false
                            viewModel.setNumeratorFocused(it) 
                        },
                        onRatioLeftFocused = {
                            isKeyboardDismissed = false
                            viewModel.setRatioLeftFocused(it)
                        },
                        onBlankClicked = { index ->
                            isKeyboardDismissed = false
                            viewModel.selectBlank(index)
                        },
                        onUnitSelected = { viewModel.appendUnit(it) },
                        onKeyPress = { viewModel.onKeyPress(it) },
                        onDelete = { viewModel.onDelete() }
                    )

                    // Lightweight feedback entry point
                    if (!isSheetOpen && uiState.interactionState != MathQuestionInteractionState.EDITING) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when (uiState.interactionState) {
                                    MathQuestionInteractionState.CORRECT -> Color(0xFF10B981).copy(alpha = 0.1f)
                                    MathQuestionInteractionState.ANSWER_REVEALED -> Color(0xFF3B82F6).copy(alpha = 0.1f)
                                    else -> Color(0xFFEF4444).copy(alpha = 0.1f)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { isSheetOpen = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val icon = when (uiState.interactionState) {
                                        MathQuestionInteractionState.CORRECT -> "🌟"
                                        MathQuestionInteractionState.ANSWER_REVEALED -> "📖"
                                        else -> "❌"
                                    }
                                    val text = when (uiState.interactionState) {
                                        MathQuestionInteractionState.CORRECT -> "回答正确！点击重新打开反馈与解析"
                                        MathQuestionInteractionState.ANSWER_REVEALED -> "已查看标准答案与解析。点击再次查看"
                                        else -> "答案不正确。点击重新打开反馈与查看提示"
                                    }
                                    val textColor = when (uiState.interactionState) {
                                        MathQuestionInteractionState.CORRECT -> Color(0xFF34D399)
                                        MathQuestionInteractionState.ANSWER_REVEALED -> Color(0xFF60A5FA)
                                        else -> Color(0xFFF87171)
                                    }
                                    Text("$icon $text", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                Text("点击展开 ↗", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    // Render Concept Card / Worked Example / Lesson Intro / Summary
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = when (currentBlock.type) {
                                com.example.data.math.MathContentBlockType.LESSON_INTRO -> Color(0xFF3B82F6).copy(alpha = 0.5f)
                                com.example.data.math.MathContentBlockType.CONCEPT -> Color(0xFFEC4899).copy(alpha = 0.5f)
                                com.example.data.math.MathContentBlockType.WORKED_EXAMPLE -> Color(0xFFFBBF24).copy(alpha = 0.5f)
                                com.example.data.math.MathContentBlockType.SUMMARY -> Color(0xFF10B981).copy(alpha = 0.5f)
                                else -> Color(0xFF3B82F6).copy(alpha = 0.5f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Badge
                            val typeLabel = when (currentBlock.type) {
                                com.example.data.math.MathContentBlockType.LESSON_INTRO -> "🏁 关卡导读"
                                com.example.data.math.MathContentBlockType.CONCEPT -> "📐 核心概念学堂"
                                com.example.data.math.MathContentBlockType.WORKED_EXAMPLE -> "🔍 教材分步例题精讲"
                                com.example.data.math.MathContentBlockType.SUMMARY -> "🏆 核心本课结语"
                                else -> "📖 知识卡片"
                            }
                            val badgeColor = when (currentBlock.type) {
                                com.example.data.math.MathContentBlockType.LESSON_INTRO -> Color(0xFF3B82F6)
                                com.example.data.math.MathContentBlockType.CONCEPT -> Color(0xFFEC4899)
                                com.example.data.math.MathContentBlockType.WORKED_EXAMPLE -> Color(0xFFFBBF24)
                                com.example.data.math.MathContentBlockType.SUMMARY -> Color(0xFF10B981)
                                else -> Color(0xFF3B82F6)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = typeLabel,
                                    color = badgeColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            MathRichText(
                                text = currentBlock.title,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            currentBlock.contentText?.let { text ->
                                MathRichText(
                                    text = text,
                                    color = Color.LightGray,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp
                                )
                            }

                            currentBlock.imageAsset?.let { asset ->
                                MathIllustrationRenderer(
                                    imageAsset = asset,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )
                            }

                            // Show step-by-step for WORKED_EXAMPLE
                            if (currentBlock.type == com.example.data.math.MathContentBlockType.WORKED_EXAMPLE) {
                                val steps = currentBlock.steps ?: emptyList()
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    steps.take(uiState.currentExampleStepIndex + 1).forEachIndexed { index, stepText ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.8f)),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .background(Color(0xFF3B82F6), androidx.compose.foundation.shape.CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${index + 1}",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                MathRichText(
                                                    text = stepText,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    lineHeight = 20.sp
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

            if (currentQuestion != null) {
                MathFeedbackSheet(
                    isOpen = isSheetOpen,
                    onDismiss = { isSheetOpen = false },
                    uiState = uiState,
                    currentQuestion = currentQuestion,
                    viewModel = viewModel,
                    onNext = { viewModel.goToNextBlock() },
                    onClear = { viewModel.retryQuestion() }
                )
            }
        }
    }
}

@Composable
fun MathLessonBottomBar(
    currentBlockIndex: Int,
    totalBlocks: Int,
    currentBlock: com.example.data.math.MathContentBlock,
    uiState: com.example.viewmodel.math.MathLessonUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    onReopenSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isQuestion = currentBlock.question != null
    val hasMoreSteps = currentBlock.type == com.example.data.math.MathContentBlockType.WORKED_EXAMPLE &&
            uiState.currentExampleStepIndex + 1 < (currentBlock.steps?.size ?: 0)

    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = Color(0xFF1E293B)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Button: Previous Step
            Button(
                onClick = onPrevious,
                enabled = currentBlockIndex > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.LightGray,
                    disabledContainerColor = Color(0xFF0F172A),
                    disabledContentColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("math_prev_step_button")
            ) {
                Text("上一步", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            // Right Button: Action Button
            if (isQuestion) {
                when (uiState.interactionState) {
                    MathQuestionInteractionState.CHECKING -> {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981).copy(alpha = 0.5f),
                                disabledContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("判断中...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    MathQuestionInteractionState.CORRECT, MathQuestionInteractionState.ANSWER_REVEALED -> {
                        val isLastBlock = currentBlockIndex + 1 >= totalBlocks
                        val btnText = if (isLastBlock) "完成本课并结算" else "我明白了，继续"
                        Button(
                            onClick = onNext,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("math_next_question_button")
                        ) {
                            Text(btnText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    MathQuestionInteractionState.INCORRECT -> {
                        Button(
                            onClick = onReopenSheet,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("math_retry_button")
                        ) {
                            Text("查看反馈", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    else -> { // MathQuestionInteractionState.EDITING
                        val canSubmit = when (currentBlock.question.type) {
                            MathQuestionType.MULTIPLE_CHOICE -> uiState.selectedChoice.isNotEmpty()
                            MathQuestionType.FRACTION_INPUT -> uiState.userNumeratorText.isNotEmpty() && uiState.userDenominatorText.isNotEmpty()
                            MathQuestionType.RATIO_INPUT -> uiState.userRatioLeftText.isNotEmpty() && uiState.userRatioRightText.isNotEmpty()
                            MathQuestionType.FILL_BLANK -> uiState.blankAnswers.all { it.isNotEmpty() }
                            else -> uiState.userAnswerText.isNotEmpty()
                        }
                        Button(
                            onClick = onSubmit,
                            enabled = canSubmit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                disabledContainerColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("choice_submit_button")
                        ) {
                            Text("检查答案", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                val btnText = if (hasMoreSteps) "查看下一步步骤" else if (currentBlock.type == com.example.data.math.MathContentBlockType.SUMMARY) "完成本课并结算" else "我明白了，继续"
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("math_block_continue_button")
                ) {
                    Text(btnText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ReadOnlyFractionDisplay(numerator: String, denominator: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(60.dp)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = numerator,
            color = Color(0xFF00E5FF),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.White)
        )
        Text(
            text = denominator,
            color = Color(0xFF00E5FF),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

fun formatCorrectAnswer(spec: com.example.data.math.MathAnswerSpec): String {
    val template = spec.responseTemplate
    val expected = spec.expectedValues
    if (template != null && expected != null) {
        var sentence = template!!
        expected.forEachIndexed { index, s ->
            sentence = sentence.replace("{$index}", s)
        }
        return sentence
    }
    return when (spec.kind) {
        "FRACTION" -> "${spec.numerator ?: 0}/${spec.denominator ?: 1}"
        "DECIMAL" -> spec.value ?: spec.expectedValue ?: ""
        "INTEGER" -> spec.value ?: spec.expectedValue ?: ""
        "CHOICE" -> spec.expectedValue ?: ""
        "MULTIPLE_BLANKS" -> spec.expectedValues?.joinToString("、") ?: ""
        "EXPRESSION" -> spec.expectedValue ?: ""
        "NUMERIC_WITH_UNIT" -> "${spec.value ?: ""}${spec.acceptedUnits?.firstOrNull() ?: ""}"
        else -> spec.expectedValue ?: spec.value ?: ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathFeedbackSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    uiState: com.example.viewmodel.math.MathLessonUiState,
    currentQuestion: com.example.data.math.MathQuestion,
    viewModel: MathLessonViewModel,
    onNext: () -> Unit,
    onClear: () -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showRevealConfirmInSheet by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E293B),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF475569)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showRevealConfirmInSheet) {
                // Confirm Reveal block inside sheet
                Text(
                    text = "🔑 确认要揭示答案吗？",
                    color = Color(0xFFF59E0B),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "揭示标准答案和思路后，本题将被标记为“需要复习”，且本题将无法获得金币奖励哦。确定要看吗？",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showRevealConfirmInSheet = false },
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("再想想", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showRevealConfirmInSheet = false
                            viewModel.revealAnswer()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.1f).height(44.dp)
                    ) {
                        Text("确定查看", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else if (uiState.interactionState == MathQuestionInteractionState.ANSWER_REVEALED) {
                // ANSWER_REVEALED
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📖 标准答案与思路", color = Color(0xFF60A5FA), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "关闭", tint = Color.Gray)
                    }
                }

                Text("标准答案是：", color = Color.Gray, fontSize = 13.sp)

                val spec = currentQuestion.answerSpec
                if (spec.kind == "FRACTION") {
                    ReadOnlyFractionDisplay(
                        numerator = (spec.numerator ?: 0).toString(),
                        denominator = (spec.denominator ?: 1).toString()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = formatCorrectAnswer(spec),
                            color = Color(0xFF00E5FF),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text("解题步骤与思路：", color = Color(0xFF60A5FA), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                MathRichText(
                    text = currentQuestion.explanation.ifEmpty { "根据题意，按照基本运算法则计算即可。" },
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Divider(color = Color(0xFF334155))

                Text(
                    text = "⚠️ 这道题已标记为需要复习。建议你自主背默并手动清空重做一遍，以加深理解！",
                    color = Color(0xFFFBBF24),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.reDoQuestionAfterReveal()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("手脑重构：再做一次", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onNext()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("跳过此题，下一关", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                // Correct / Incorrect / Feedback
                val result = uiState.evaluationResult
                if (result != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val headerText = when (result) {
                            is MathEvaluationResult.Correct -> "🌟 完全正确！"
                            is MathEvaluationResult.NotSimplified -> "📐 分数需要约分哦"
                            is MathEvaluationResult.UnitMissing -> "📏 别漏掉单位呀"
                            is MathEvaluationResult.InvalidInput -> "❓ 格式输入不完整"
                            is MathEvaluationResult.Incorrect -> "❌ 答案不正确"
                        }
                        val headerColor = when (result) {
                            is MathEvaluationResult.Correct -> Color(0xFF34D399)
                            is MathEvaluationResult.NotSimplified -> Color(0xFFFBBF24)
                            is MathEvaluationResult.UnitMissing -> Color(0xFFFBBF24)
                            else -> Color(0xFFF87171)
                        }
                        Text(
                            text = headerText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = headerColor
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "关闭", tint = Color.Gray)
                        }
                    }

                    val descText = when (result) {
                        is MathEvaluationResult.Correct -> "做得太棒了！继续保持！"
                        is MathEvaluationResult.NotSimplified -> "你的数值计算完全正确，但是分数还可以继续化简为最简分数，请约分后再试一次。"
                        is MathEvaluationResult.UnitMissing -> "计算数值完全正确，但请记得在尾部写上正确的单位（如 米、厘米 等）。"
                        is MathEvaluationResult.InvalidInput -> result.message
                        is MathEvaluationResult.Incorrect -> result.reason
                    }
                    MathRichText(
                        text = descText,
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        lineHeight = 20.sp
                    )

                    if (currentQuestion.type == MathQuestionType.FILL_BLANK && currentQuestion.answerSpec.responseTemplate != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "你的组合答案拼装效果：",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        TemplateWithDiffDisplay(
                            template = currentQuestion.answerSpec.responseTemplate,
                            expectedValues = currentQuestion.answerSpec.expectedValues ?: emptyList(),
                            userAnswers = uiState.blankAnswers
                        )
                    }

                    if (uiState.showExplanation && currentQuestion.explanation.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("解题步骤：", color = Color(0xFF60A5FA), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                MathRichText(text = currentQuestion.explanation, color = Color.LightGray, fontSize = 13.sp)
                            }
                        }
                    }

                    // Render hints if active in uiState.showHintLevel
                    if (uiState.showHintLevel > 0 && currentQuestion.hints.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💡 提示助手 (分步解锁)", color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("${uiState.showHintLevel} / ${currentQuestion.hints.size}", color = Color.Gray, fontSize = 11.sp)
                                }
                                currentQuestion.hints.take(uiState.showHintLevel).forEachIndexed { idx, hintText ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(text = "• ", color = Color(0xFFFBBF24), fontSize = 13.sp)
                                        Text(text = hintText, color = Color.LightGray, fontSize = 12.sp, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (uiState.interactionState == MathQuestionInteractionState.CORRECT) {
                        Button(
                            onClick = {
                                onDismiss()
                                onNext()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("我明白了，继续", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        // Incorrect or NotSimplified, etc.
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onClear()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1.2f).height(44.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "再试一次", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("原答案修改", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.clearAndResetQuestion()
                                        onDismiss()
                                    },
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(44.dp)
                                ) {
                                    Text("清空重做", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (currentQuestion.hints.isNotEmpty()) {
                                    Button(
                                        onClick = { viewModel.showNextHint() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                        enabled = uiState.showHintLevel < currentQuestion.hints.size,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Text(
                                            if (uiState.showHintLevel == 0) "💡 查看提示" else "💡 展开更多提示",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Button(
                                    onClick = { showRevealConfirmInSheet = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Text("🔑 查看答案", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
fun MathChoiceToolPanel(
    choices: List<String>,
    onChoiceSelected: (String) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        choices.forEach { choice ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF00E5FF).copy(alpha = 0.12f))
                    .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    .clickable { onChoiceSelected(choice) }
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = choice,
                    color = Color(0xFF00E5FF),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Delete button
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "退格",
                color = Color(0xFFF72585),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TemplateWithDiffDisplay(
    template: String,
    expectedValues: List<String>,
    userAnswers: List<String>
) {
    val segments = remember(template) { parseTemplate(template) }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalArrangement = Arrangement.Center
    ) {
        segments.forEach { segment ->
            when (segment) {
                is TemplateSegment.Text -> {
                    Text(
                        text = segment.content,
                        color = Color.LightGray,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp),
                        lineHeight = 24.sp
                    )
                }
                is TemplateSegment.Placeholder -> {
                    val index = segment.index
                    val expected = expectedValues.getOrNull(index) ?: ""
                    val actual = userAnswers.getOrNull(index) ?: ""
                    val isCorrect = actual.trim() == expected.trim()

                    val bgColor = if (actual.isEmpty()) {
                        Color(0xFF334155).copy(alpha = 0.4f)
                    } else if (isCorrect) {
                        Color(0xFF10B981).copy(alpha = 0.15f)
                    } else {
                        Color(0xFFEF4444).copy(alpha = 0.15f)
                    }

                    val borderColor = if (actual.isEmpty()) {
                        Color(0xFF475569)
                    } else if (isCorrect) {
                        Color(0xFF10B981)
                    } else {
                        Color(0xFFEF4444)
                    }

                    val textColor = if (actual.isEmpty()) {
                        Color.Gray
                    } else if (isCorrect) {
                        Color(0xFF34D399)
                    } else {
                        Color(0xFFF87171)
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .background(bgColor, RoundedCornerShape(4.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = actual.ifEmpty { "空" },
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

