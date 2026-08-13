package com.example.ui.english

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.AutoDictationUiState
import com.example.data.english.EnglishAutoDictationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnglishAutoDictationPlayerSheet(
    uiState: AutoDictationUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReplayCurrent: () -> Unit,
    onSkipNext: () -> Unit,
    onRestartSession: () -> Unit,
    onEndDictation: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    var showConfirmEndDialog by remember { mutableStateOf(false) }
    var showWordListAnswerView by remember { mutableStateOf(false) }

    if (showConfirmEndDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmEndDialog = false },
            title = { Text("确认结束听写？", fontWeight = FontWeight.Bold) },
            text = { Text("听写尚未全部完成。结束自测不会提交任何成绩。") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmEndDialog = false
                        onEndDictation()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("confirm_end_dictation_button")
                ) {
                    Text("确认结束", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmEndDialog = false }) {
                    Text("继续听写", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E293B),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (uiState is AutoDictationUiState.Completed) {
                onEndDictation()
            } else {
                showConfirmEndDialog = true
            }
        },
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "一键报听写 (独立自测)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        if (uiState is AutoDictationUiState.Completed) {
                            onEndDictation()
                        } else {
                            showConfirmEndDialog = true
                        }
                    },
                    modifier = Modifier.testTag("close_player_sheet_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "关闭", tint = Color.White)
                }
            }

            when (uiState) {
                is AutoDictationUiState.Countdown -> {
                    CountdownView(
                        remainingSeconds = uiState.remainingSeconds,
                        totalWords = uiState.totalWords,
                        onCancel = { showConfirmEndDialog = true }
                    )
                }

                is AutoDictationUiState.Playing -> {
                    ActivePlayerCardView(
                        currentIndex = uiState.currentIndex,
                        totalWords = uiState.totalWords,
                        currentRepeat = uiState.currentRepeat,
                        totalRepeats = uiState.totalRepeats,
                        intervalRemaining = uiState.intervalRemainingSeconds,
                        isPaused = false,
                        onPause = onPause,
                        onResume = onResume,
                        onReplayCurrent = onReplayCurrent,
                        onSkipNext = onSkipNext,
                        onEnd = { showConfirmEndDialog = true }
                    )
                }

                is AutoDictationUiState.Paused -> {
                    ActivePlayerCardView(
                        currentIndex = uiState.currentIndex,
                        totalWords = uiState.totalWords,
                        currentRepeat = uiState.currentRepeat,
                        totalRepeats = uiState.totalRepeats,
                        intervalRemaining = null,
                        isPaused = true,
                        onPause = onPause,
                        onResume = onResume,
                        onReplayCurrent = onReplayCurrent,
                        onSkipNext = onSkipNext,
                        onEnd = { showConfirmEndDialog = true }
                    )
                }

                is AutoDictationUiState.Completed -> {
                    if (showWordListAnswerView) {
                        WordListAnswerView(
                            items = uiState.itemsPlayed,
                            onBackToSummary = { showWordListAnswerView = false }
                        )
                    } else {
                        CompletedSummaryView(
                            completedState = uiState,
                            onViewWordList = { showWordListAnswerView = true },
                            onRestart = onRestartSession,
                            onExit = onEndDictation
                        )
                    }
                }

                is AutoDictationUiState.Error -> {
                    ErrorView(
                        message = uiState.message ?: "听写引擎出现异常",
                        onClose = onEndDictation
                    )
                }

                AutoDictationUiState.Idle -> {
                    // Idle state fallback
                }
            }
        }
    }
}

@Composable
fun CountdownView(
    remainingSeconds: Int,
    totalWords: Int,
    onCancel: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFEC4899)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "准备听写纸和笔",
                color = Color(0xFFEC4899),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "本单元共 $totalWords 个词汇，即将开始自动播报...",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$remainingSeconds",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            TextButton(onClick = onCancel, modifier = Modifier.testTag("cancel_countdown_button")) {
                Text("取消听写", color = Color.Gray)
            }
        }
    }
}

@Composable
fun ActivePlayerCardView(
    currentIndex: Int,
    totalWords: Int,
    currentRepeat: Int,
    totalRepeats: Int,
    intervalRemaining: Int?,
    isPaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReplayCurrent: () -> Unit,
    onSkipNext: () -> Unit,
    onEnd: () -> Unit
) {
    val progress = if (totalWords > 0) (currentIndex + 1).toFloat() / totalWords.toFloat() else 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFF00E5FF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress Bar
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "进度: ${currentIndex + 1} / $totalWords 词",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isPaused) "已暂停" else "正在报听写...",
                        color = if (isPaused) Color(0xFFEF4444) else Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF00E5FF),
                    trackColor = Color(0xFF334155)
                )
            }

            // Word Prompt Box (Answers Strictly Hidden)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "第 ${currentIndex + 1} 个词",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    if (isPaused) {
                        Text(
                            text = "⏸️ 已暂停，请点击继续",
                            color = Color(0xFFFCA5A5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (intervalRemaining != null) {
                        Text(
                            text = "✍️ 请在纸上听写 (等待下一个: ${intervalRemaining}s)",
                            color = Color(0xFFFBBF24),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "🔊 正在播报第 $currentRepeat / $totalRepeats 次",
                            color = Color(0xFF34D399),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Controls Row (Min touch target >= 48dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Replay current
                IconButton(
                    onClick = onReplayCurrent,
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFF334155), CircleShape)
                        .testTag("replay_current_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "重播当前词", tint = Color.White)
                }

                // 2. Play / Pause
                IconButton(
                    onClick = { if (isPaused) onResume() else onPause() },
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            if (isPaused) Color(0xFF10B981) else Color(0xFFEC4899),
                            CircleShape
                        )
                        .testTag("toggle_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "继续" else "暂停",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // 3. Skip next
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFF334155), CircleShape)
                        .testTag("skip_next_button")
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "跳到下一个词", tint = Color.White)
                }

                // 4. End dictation
                IconButton(
                    onClick = onEnd,
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFEF4444).copy(alpha = 0.2f), CircleShape)
                        .testTag("end_dictation_button")
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "结束听写", tint = Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
fun CompletedSummaryView(
    completedState: AutoDictationUiState.Completed,
    onViewWordList: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    val minutes = completedState.elapsedSeconds / 60
    val seconds = completedState.elapsedSeconds % 60
    val timeFormatted = if (minutes > 0) "${minutes}分${seconds}秒" else "${seconds}秒"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFF10B981)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🎉 听写自测完成！",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF34D399)
            )

            Text(
                text = "本次共播报 ${completedState.totalWords} 个词汇 | 用时 $timeFormatted",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            if (completedState.failedWordIds.isNotEmpty()) {
                Text(
                    text = "⚠️ 注意: 有 ${completedState.failedWordIds.size} 个词发音失败或跳过",
                    color = Color(0xFFFCA5A5),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action: View word list with answers
            Button(
                onClick = onViewWordList,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("view_word_list_answers_button")
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = Color.White)
                    Text("核对听写词单 (查看拼写与中文)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("restart_dictation_button"),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("再听一遍", color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onExit,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("finish_and_return_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("返回复习页", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WordListAnswerView(
    items: List<EnglishAutoDictationItem>,
    onBackToSummary: () -> Unit
) {
    val coreItems = items.filter { !it.isExtended }
    val extItems = items.filter { it.isExtended }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📋 本次听写词单 (用于核对)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF00E5FF))
            TextButton(onClick = onBackToSummary, modifier = Modifier.testTag("back_to_summary_button")) {
                Text("返回总结", color = Color.LightGray)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (coreItems.isNotEmpty()) {
                item {
                    Text(
                        text = "📚 核心词汇 (${coreItems.size}词)",
                        color = Color(0xFF00E5FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(coreItems) { item ->
                    WordAnswerRow(item = item)
                }
            }

            if (extItems.isNotEmpty()) {
                item {
                    Text(
                        text = "🗣️ 拓展听说词汇 (${extItems.size}词)",
                        color = Color(0xFFF472B6),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(extItems) { item ->
                    WordAnswerRow(item = item)
                }
            }
        }
    }
}

@Composable
fun WordAnswerRow(item: EnglishAutoDictationItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = item.word.spelling,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.word.chineseMeaning,
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }
            Text(
                text = if (item.isExtended) "B类·拓展" else "A类·核心",
                color = if (item.isExtended) Color(0xFFF472B6) else Color(0xFF00E5FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(
                        if (item.isExtended) Color(0xFFF472B6).copy(alpha = 0.15f) else Color(0xFF00E5FF).copy(alpha = 0.15f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ErrorView(message: String, onClose: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFEF4444)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("⚠️ 播报遇到异常", color = Color(0xFFEF4444), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(message, color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center)
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("返回", color = Color.White)
            }
        }
    }
}
