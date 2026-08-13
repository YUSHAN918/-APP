package com.example.ui.math

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.example.viewmodel.math.MathLessonUiState
import com.example.viewmodel.math.MathQuestionRecord
import com.example.data.math.MathEvaluationResult

@Composable
fun MathResultScreen(
    uiState: MathLessonUiState,
    onRetry: () -> Unit,
    onBackToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedDetails by remember { mutableStateOf(false) }

    val correctCount = uiState.history.count { it.isCorrect }
    val totalCount = uiState.history.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF0B132B),
                        Color(0xFF1C2541)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
        ) {
            // Header Space
            Spacer(modifier = Modifier.height(36.dp))

            // Main Info Column
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "🎉 挑战完成！",
                        color = Color(0xFF00E5FF),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Text(
                        text = uiState.lesson?.title ?: "数学关卡",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Stats Dashboard Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Circular Accuracy Indicator or Score text
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("正确题数", color = Color.Gray, fontSize = 13.sp)
                                    Text(
                                        text = "$correctCount / $totalCount",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(1.dp, 40.dp)
                                        .background(Color(0xFF475569))
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("正确率", color = Color.Gray, fontSize = 13.sp)
                                    val percent = if (totalCount > 0) (correctCount * 100 / totalCount) else 0
                                    Text(
                                        text = "$percent%",
                                        color = if (percent >= 80) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Divider(color = Color(0xFF334155), thickness = 1.dp)

                            // Rewards Display
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val isDevMode = remember {
                                com.example.BuildConfig.DEBUG && (
                                    com.example.data.math.DeveloperMathSettings.isBypassMathPrerequisites(context) ||
                                    com.example.data.math.DeveloperMathSettings.isUseSimulatedProgress(context)
                                )
                            }
                            val coinsToDisplay = if (isDevMode) 0 else uiState.earnedCoins

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                        .padding(vertical = 12.dp, horizontal = 16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = "金币奖励",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "获得齿轮金币：+$coinsToDisplay",
                                        color = Color(0xFFFBBF24),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                if (isDevMode) {
                                    Text(
                                        text = "开发测试，本次奖励不入账",
                                        color = Color(0xFFF87171),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Collapsible detail list trigger
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                            .clickable { expandedDetails = !expandedDetails }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "查看本次答题详情",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (expandedDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "展开详情",
                            tint = Color.White
                        )
                    }
                }

                // Question Details list
                if (expandedDetails) {
                    itemsIndexed(uiState.history) { index, record ->
                        MathQuestionDetailCard(index = index, record = record)
                    }
                }
            }
        }

        // Fixed bottom CTA controls
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Return to Map
                Button(
                    onClick = onBackToMap,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("result_back_to_map"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("返回数字机械城", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                // Retry
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("result_retry"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("再练一次", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MathQuestionDetailCard(index: Int, record: MathQuestionRecord) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
                    text = "第 ${index + 1} 题",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (record.isCorrect) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (record.isCorrect) "正确" else "错误",
                        color = if (record.isCorrect) Color(0xFF34D399) else Color(0xFFF87171),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            MathRichText(
                text = record.question.stem,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Divider(color = Color(0xFF334155).copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("你的作答", color = Color.Gray, fontSize = 12.sp)
                    MathRichText(
                        text = record.userAnswers.firstOrNull()?.ifEmpty { "无" } ?: "无",
                        color = if (record.isCorrect) Color(0xFF34D399) else Color(0xFFF87171),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("正确答案", color = Color.Gray, fontSize = 12.sp)
                    val spec = record.question.answerSpec
                    val ansText = when (spec.kind) {
                        "FRACTION" -> "${spec.numerator}/${spec.denominator}"
                        "MULTIPLE_BLANKS" -> spec.expectedValues?.joinToString("，") ?: ""
                        "NUMERIC_WITH_UNIT" -> "${spec.value}${spec.acceptedUnits?.firstOrNull() ?: ""}"
                        else -> spec.expectedValue ?: spec.value ?: ""
                    }
                    MathRichText(
                        text = ansText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!record.isCorrect) {
                val errorMsg = when (record.evaluationResult) {
                    is MathEvaluationResult.NotSimplified -> "💡 提示：分数未约分为最简分数！"
                    is MathEvaluationResult.UnitMissing -> "💡 提示：数值正确，但遗漏了单位！"
                    is MathEvaluationResult.InvalidInput -> "💡 提示：输入格式不正确！"
                    is MathEvaluationResult.Incorrect -> "💡 提示：${(record.evaluationResult as MathEvaluationResult.Incorrect).reason}"
                    else -> "💡 提示：计算结果不太对，需要多加练习"
                }
                Text(
                    text = errorMsg,
                    color = Color(0xFFF59E0B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Explanation
            if (record.question.explanation.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("解析：", color = Color(0xFF60A5FA), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        MathRichText(text = record.question.explanation, color = Color.LightGray, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
