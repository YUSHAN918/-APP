package com.example.ui.english

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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

data class SentenceRepairQuestion(
    val id: String,
    val contextPrompt: String,
    val promptAudioText: String,
    val sentencePrefix: String, // Text before blank
    val sentenceSuffix: String, // Text after blank
    val options: List<String>,
    val correctIndex: Int,
    val fullSentence: String,
    val functionalExplanation: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnglishSentenceRepairView(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val questions = remember {
        listOf(
            SentenceRepairQuestion(
                id = "sr_1",
                contextPrompt = "情境1: 指着远处的小狗询问朋友",
                promptAudioText = "What's that? It's a dog.",
                sentencePrefix = "What's ",
                sentenceSuffix = "? — It's a dog.",
                options = listOf("that", "this", "name", "your"),
                correctIndex = 0,
                fullSentence = "What's that? — It's a dog.",
                functionalExplanation = "that 用于询问较远距离的东西（远处的小狗），而 this 用于询问近处。"
            ),
            SentenceRepairQuestion(
                id = "sr_2",
                contextPrompt = "情境2: 询问生日蛋糕上有几支蜡笔/几个盘子",
                promptAudioText = "How many plates? Five.",
                sentencePrefix = "",
                sentenceSuffix = " plates? — Five.",
                options = listOf("How many", "How old", "What's", "How are"),
                correctIndex = 0,
                fullSentence = "How many plates? — Five.",
                functionalExplanation = "How many 用于询问物品或动物的数量，而 How old 用于询问年龄。"
            ),
            SentenceRepairQuestion(
                id = "sr_3",
                contextPrompt = "情境3: 在餐桌上礼貌地请求喝果汁",
                promptAudioText = "Can I have some juice, please?",
                sentencePrefix = "Can I have some ",
                sentenceSuffix = ", please?",
                options = listOf("juice", "pencil", "duck", "two"),
                correctIndex = 0,
                fullSentence = "Can I have some juice, please?",
                functionalExplanation = "Can I have some... 是礼貌表达饮品与食物请求的标准句式，juice 意为果汁。"
            ),
            SentenceRepairQuestion(
                id = "sr_4",
                contextPrompt = "情境4: 询问对方的年龄",
                promptAudioText = "How old are you? I'm six years old.",
                sentencePrefix = "",
                sentenceSuffix = " are you? — I'm six years old.",
                options = listOf("How old", "How many", "What", "Where"),
                correctIndex = 0,
                fullSentence = "How old are you? — I'm six years old.",
                functionalExplanation = "How old 用于询问年龄；回答 I'm six years old 表达“我6岁了”。"
            )
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

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
        // Header Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "任务 4: 句子修复站 (${currentIndex + 1}/${questions.size})",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B)
            ) {
                Text(
                    text = "句型精准修复",
                    color = Color(0xFFA7F3D0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        LinearProgressIndicator(
            progress = { (currentIndex + 1) / questions.size.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF10B981),
            trackColor = Color(0xFF334155)
        )

        // Context Banner
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
                        text = currentQ.contextPrompt,
                        color = Color(0xFFFDE047),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "选择最恰当的词块，补全下方缺失的句子：",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
                IconButton(
                    onClick = { playPrompt() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF10B981), CircleShape)
                        .testTag("play_sentence_audio_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "朗读例句",
                        tint = Color.White
                    )
                }
            }
        }

        // Sentence Canvas Box
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🛠️ 正在修复的句子：",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )

                // Wrapped Row showing prefix + [Blank/Filled Option] + suffix
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (currentQ.sentencePrefix.isNotEmpty()) {
                        Text(
                            text = currentQ.sentencePrefix,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Slot
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedIndex != -1) Color(0xFFFDE047) else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .border(
                                2.dp,
                                if (selectedIndex != -1) Color.White else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Text(
                            text = if (selectedIndex != -1) currentQ.options[selectedIndex] else " [  ?  ] ",
                            color = if (selectedIndex != -1) Color.Black else Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    if (currentQ.sentenceSuffix.isNotEmpty()) {
                        Text(
                            text = currentQ.sentenceSuffix,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Option Chips (Min Touch Target >= 48dp)
        Text(
            text = "点击词块填入句子：",
            color = Color.LightGray,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            currentQ.options.forEachIndexed { index, option ->
                val isSelected = selectedIndex == index
                val optionBg = if (isSelected) Color(0xFFF59E0B) else Color(0xFF1E293B)
                val borderCol = if (isSelected) Color.White else Color(0xFF475569)

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = optionBg,
                    border = BorderStroke(2.dp, borderCol),
                    modifier = Modifier
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (!showFeedback) {
                                selectedIndex = index
                                SpeechSynthesizer.speak(context, option)
                            }
                        }
                        .testTag("sentence_option_chip_$index")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                        contentDescription = "句子修复反馈",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCorrect) "修复成功！完整的句子是：" else "需要调整，请查看语境与语用差异：",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = currentQ.fullSentence,
                            color = Color(0xFFFDE047),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentQ.functionalExplanation,
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
                        isCorrect = (selectedIndex == currentQ.correctIndex)
                        showFeedback = true
                    }
                },
                enabled = selectedIndex != -1,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_sentence_repair_button")
            ) {
                Text("提交修复", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("next_sentence_repair_button")
            ) {
                Text(
                    text = if (currentIndex + 1 < questions.size) "下一句" else "完成句子修复任务",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
