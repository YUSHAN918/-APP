package com.example.ui.english

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.SpeechSynthesizer

enum class EnglishCategoryTag(val labelName: String, val chineseName: String) {
    ANIMAL("ANIMAL", "动物类"),
    FOOD("FOOD", "食物类"),
    DRINK("DRINK", "饮品类"),
    NUMBER("NUMBER", "数字类"),
    BODY("BODY", "身体部位"),
    COLOUR("COLOUR", "颜色类"),
    SCHOOL_ITEM("SCHOOL_ITEM", "文具用品")
}

data class CategoryWordItem(
    val word: String,
    val translation: String,
    val categoryTag: EnglishCategoryTag
)

data class OddOneOutQuestion(
    val id: String,
    val categoryTarget: EnglishCategoryTag,
    val items: List<CategoryWordItem>,
    val oddWordIndex: Int, // index of item that DOES NOT belong
    val promptAudioText: String,
    val explanation: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnglishCategoryOddOneOutView(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val questions = remember {
        listOf(
            OddOneOutQuestion(
                id = "odd_1",
                categoryTarget = EnglishCategoryTag.ANIMAL,
                items = listOf(
                    CategoryWordItem("duck", "鸭子", EnglishCategoryTag.ANIMAL),
                    CategoryWordItem("dog", "狗", EnglishCategoryTag.ANIMAL),
                    CategoryWordItem("bread", "面包", EnglishCategoryTag.FOOD),
                    CategoryWordItem("tiger", "老虎", EnglishCategoryTag.ANIMAL)
                ),
                oddWordIndex = 2,
                promptAudioText = "Find the odd word that is NOT an animal!",
                explanation = "bread（面包）属于 FOOD（食物类），而其他三个都是 ANIMAL（动物类）。"
            ),
            OddOneOutQuestion(
                id = "odd_2",
                categoryTarget = EnglishCategoryTag.FOOD,
                items = listOf(
                    CategoryWordItem("egg", "鸡蛋", EnglishCategoryTag.FOOD),
                    CategoryWordItem("rice", "米饭", EnglishCategoryTag.FOOD),
                    CategoryWordItem("cake", "蛋糕", EnglishCategoryTag.FOOD),
                    CategoryWordItem("seven", "数字7", EnglishCategoryTag.NUMBER)
                ),
                oddWordIndex = 3,
                promptAudioText = "Find the word that is NOT food!",
                explanation = "seven（数字7）属于 NUMBER（数字类），而其他三个都是 FOOD（食物类）。"
            ),
            OddOneOutQuestion(
                id = "odd_3",
                categoryTarget = EnglishCategoryTag.NUMBER,
                items = listOf(
                    CategoryWordItem("one", "数字1", EnglishCategoryTag.NUMBER),
                    CategoryWordItem("monkey", "猴子", EnglishCategoryTag.ANIMAL),
                    CategoryWordItem("eight", "数字8", EnglishCategoryTag.NUMBER),
                    CategoryWordItem("ten", "数字10", EnglishCategoryTag.NUMBER)
                ),
                oddWordIndex = 1,
                promptAudioText = "Find the word that is NOT a number!",
                explanation = "monkey（猴子）属于 ANIMAL（动物类），而其他三个都是 NUMBER（数字类）。"
            )
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var completedCount by remember { mutableIntStateOf(0) }

    val currentQ = questions[currentIndex]

    fun playPrompt() {
        SpeechSynthesizer.speak(context, currentQ.promptAudioText)
    }

    LaunchedEffect(currentIndex) {
        selectedIndex = -1
        showFeedback = false
        isCorrect = false
        playPrompt()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "任务 2: 听音巡检与词汇分类 (${currentIndex + 1}/${questions.size})",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B)
            ) {
                Text(
                    text = "分类: ${currentQ.categoryTarget.chineseName} (${currentQ.categoryTarget.labelName})",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        LinearProgressIndicator(
            progress = { (currentIndex + 1) / questions.size.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF38BDF8),
            trackColor = Color(0xFF334155)
        )

        // Audio Prompt Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🔍 听音辨别：找出不属于当前分类的“异类”单词！",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = currentQ.promptAudioText,
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
                IconButton(
                    onClick = { playPrompt() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF38BDF8), CircleShape)
                        .testTag("play_category_audio_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "播放语音提示",
                        tint = Color.Black
                    )
                }
            }
        }

        // 2x2 Word Grid
        Text(
            text = "点击选中不属于 ${currentQ.categoryTarget.chineseName} 的单词：",
            color = Color.LightGray,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            currentQ.items.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                val itemBorderColor = when {
                    showFeedback && index == currentQ.oddWordIndex -> Color(0xFF10B981)
                    showFeedback && isSelected && !isCorrect -> Color(0xFFEF4444)
                    isSelected -> Color(0xFF38BDF8)
                    else -> Color(0xFF334155)
                }
                val itemBg = when {
                    showFeedback && index == currentQ.oddWordIndex -> Color(0xFF064E3B)
                    showFeedback && isSelected && !isCorrect -> Color(0xFF7F1D1D)
                    isSelected -> Color(0xFF0284C7)
                    else -> Color(0xFF1E293B)
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = itemBg),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 96.dp)
                        .border(2.dp, itemBorderColor, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            if (!showFeedback) {
                                selectedIndex = index
                                SpeechSynthesizer.speak(context, item.word)
                            }
                        }
                        .testTag("category_word_item_$index")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = item.word,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.translation,
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A)
                        ) {
                            Text(
                                text = item.categoryTag.labelName,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Functional Feedback Area
        AnimatedVisibility(
            visible = showFeedback,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) Color(0xFF065F46) else Color(0xFF991B1B)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Info,
                        contentDescription = "分类判断反馈",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCorrect) "回答正确！分类判断非常精准！" else "辨别错误，请仔细查看解析：",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = currentQ.explanation,
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Action Buttons
        Spacer(modifier = Modifier.weight(1f))

        if (!showFeedback) {
            Button(
                onClick = {
                    if (selectedIndex != -1) {
                        isCorrect = (selectedIndex == currentQ.oddWordIndex)
                        showFeedback = true
                        if (isCorrect) {
                            completedCount++
                        }
                    }
                },
                enabled = selectedIndex != -1,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_odd_one_out_button")
            ) {
                Text("提交答案", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else {
            Button(
                onClick = {
                    if (currentIndex + 1 < questions.size) {
                        currentIndex++
                    } else {
                        onCompleted()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("next_odd_one_out_button")
            ) {
                Text(
                    text = if (currentIndex + 1 < questions.size) "下一题" else "完成分类任务",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
