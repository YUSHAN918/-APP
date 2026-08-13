package com.example.ui.english

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.EnglishProgressManager
import com.example.data.english.EnglishReviewPoolBuilder
import com.example.data.english.ReviewQuestionItem
import kotlin.random.Random

data class BoardSquare(
    val number: Int,
    val isStart: Boolean = false,
    val isEnd: Boolean = false,
    val ladderTarget: Int? = null,
    val snakeTarget: Int? = null,
    val label: String
)

val BOARD_SQUARES = listOf(
    BoardSquare(1, isStart = true, label = "🏁 起点"),
    BoardSquare(2, label = "Unit 1 问候"),
    BoardSquare(3, ladderTarget = 8, label = "🪜 梯子+5"),
    BoardSquare(4, label = "Unit 1 文具"),
    BoardSquare(5, label = "Unit 2 颜色"),
    BoardSquare(6, label = "Unit 2 介绍"),
    BoardSquare(7, label = "Unit 3 身体"),
    BoardSquare(8, label = "Unit 3 指令"),
    BoardSquare(9, ladderTarget = 14, label = "🪜 梯子+5"),
    BoardSquare(10, label = "字母 A-D"),
    BoardSquare(11, label = "字母 E-I"),
    BoardSquare(12, snakeTarget = 6, label = "🐍 毒蛇-6"),
    BoardSquare(13, label = "字母 J-N"),
    BoardSquare(14, label = "跨单元综合"),
    BoardSquare(15, snakeTarget = 10, label = "🐍 毒蛇-5"),
    BoardSquare(16, isEnd = true, label = "🏆 终点庆典")
)

@Composable
fun EnglishReviewBoardGame(
    recycleId: String,
    onGameFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedProgress = remember { EnglishProgressManager.getRecycleProgress(context, recycleId) }
    var currentPosition by remember { mutableStateOf(savedProgress.boardPosition.coerceAtLeast(1)) }
    var seed by remember { mutableStateOf(savedProgress.boardRandomSeed) }

    val pool = remember(recycleId, seed) {
        val unitIds = when {
            recycleId.contains("_s2_recycle_2") -> listOf(
                "english_pep_2013_g3_s2_u1",
                "english_pep_2013_g3_s2_u2",
                "english_pep_2013_g3_s2_u3",
                "english_pep_2013_g3_s2_u4",
                "english_pep_2013_g3_s2_u5",
                "english_pep_2013_g3_s2_u6"
            )
            recycleId.contains("_s2_recycle_1") -> listOf(
                "english_pep_2013_g3_s2_u1",
                "english_pep_2013_g3_s2_u2",
                "english_pep_2013_g3_s2_u3"
            )
            recycleId.contains("_s1_recycle_2") -> listOf(
                "english_pep_2013_g3_s1_u4",
                "english_pep_2013_g3_s1_u5",
                "english_pep_2013_g3_s1_u6"
            )
            else -> listOf(
                "english_pep_2013_g3_s1_u1",
                "english_pep_2013_g3_s1_u2",
                "english_pep_2013_g3_s1_u3"
            )
        }
        EnglishReviewPoolBuilder.buildPool(context, unitIds, seed)
    }

    var diceValue by remember { mutableStateOf<Int?>(null) }
    var currentQuestion by remember { mutableStateOf<ReviewQuestionItem?>(null) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var questionFeedback by remember { mutableStateOf<String?>(null) }
    var isQuestionCorrect by remember { mutableStateOf<Boolean?>(null) }
    var gameCompleted by remember { mutableStateOf(currentPosition >= 16) }

    fun saveProgress(pos: Int) {
        EnglishProgressManager.saveBoardGamePosition(context, recycleId, pos, seed)
    }

    fun rollDice() {
        if (currentQuestion != null || gameCompleted) return
        val roll = Random.nextInt(1, 4) // 1, 2, or 3
        diceValue = roll
        val newPos = (currentPosition + roll).coerceAtMost(16)
        currentPosition = newPos
        saveProgress(currentPosition)

        if (currentPosition >= 16) {
            gameCompleted = true
            onGameFinished()
        } else {
            // Pick a question from pool for this position
            val qIndex = (newPos - 1) % pool.size
            currentQuestion = pool[qIndex]
            selectedOptionIndex = null
            questionFeedback = null
            isQuestionCorrect = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Game Banner Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎲 梯子与蛇·跨单元复习棋",
                        color = Color(0xFFF472B6),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "答对前进，遇到梯子飞升，遇到蛇要下滑！",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEC4899),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$currentPosition/16",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Board Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BOARD_SQUARES.size) { index ->
                val sq = BOARD_SQUARES[index]
                val isPlayerHere = (sq.number == currentPosition)

                val bgColor = when {
                    isPlayerHere -> Color(0xFFEC4899)
                    sq.ladderTarget != null -> Color(0xFF10B981).copy(alpha = 0.2f)
                    sq.snakeTarget != null -> Color(0xFFEF4444).copy(alpha = 0.2f)
                    sq.isEnd -> Color(0xFFEAB308).copy(alpha = 0.2f)
                    else -> Color(0xFF1E293B)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isPlayerHere) 2.dp else 1.dp,
                        color = if (isPlayerHere) Color.White else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("board_square_${sq.number}")
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${sq.number}",
                                color = if (isPlayerHere) Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isPlayerHere) {
                                Text("📍", fontSize = 12.sp)
                            }
                        }

                        Text(
                            text = sq.label,
                            color = if (isPlayerHere) Color.White else Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Dice Roll Control Bar
        if (!gameCompleted && currentQuestion == null) {
            Button(
                onClick = { rollDice() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("roll_dice_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Casino, contentDescription = "掷骰子")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (diceValue == null) "掷骰子前进" else "已掷出 $diceValue 点！再次点击",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Question Dialog / Card
        currentQuestion?.let { q ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEC4899)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "❓ 棋盘复习关卡 (${q.promptTranslation})",
                        color = Color(0xFFF472B6),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = q.promptText,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    q.options.forEachIndexed { idx, opt ->
                        val isSelected = selectedOptionIndex == idx
                        val optionBg = when {
                            selectedOptionIndex == null -> Color(0xFF0F172A)
                            isSelected && idx == q.correctIndex -> Color(0xFF065F46)
                            isSelected && idx != q.correctIndex -> Color(0xFF991B1B)
                            else -> Color(0xFF0F172A)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = optionBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = selectedOptionIndex == null) {
                                    selectedOptionIndex = idx
                                    val correct = (idx == q.correctIndex)
                                    isQuestionCorrect = correct

                                    val sq = BOARD_SQUARES.find { it.number == currentPosition }
                                    if (correct) {
                                        if (sq?.ladderTarget != null) {
                                            currentPosition = sq.ladderTarget
                                            questionFeedback = "🎉 答对！触发梯子上升到了第 ${sq.ladderTarget} 格！"
                                        } else {
                                            questionFeedback = "✅ 回答正确！稳步前进！"
                                        }
                                    } else {
                                        if (sq?.snakeTarget != null) {
                                            currentPosition = sq.snakeTarget
                                            questionFeedback = "❌ 答错了哦！遭遇毒蛇下滑至第 ${sq.snakeTarget} 格。解析: ${q.explanation}"
                                        } else {
                                            questionFeedback = "❌ 答错啦。解析: ${q.explanation}"
                                        }
                                    }
                                    saveProgress(currentPosition)
                                }
                                .testTag("board_option_$idx")
                        ) {
                            Text(
                                text = opt,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    questionFeedback?.let { fb ->
                        Text(
                            text = fb,
                            color = if (isQuestionCorrect == true) Color(0xFF34D399) else Color(0xFFF87171),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = {
                                if (currentPosition >= 16) {
                                    gameCompleted = true
                                    onGameFinished()
                                }
                                currentQuestion = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Text("继续冒险", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Completion Card
        if (gameCompleted) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF065F46)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "完成",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "🏆 恭喜到达棋盘终点！",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "你成功完成了跨单元冒险棋盘任务！",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
