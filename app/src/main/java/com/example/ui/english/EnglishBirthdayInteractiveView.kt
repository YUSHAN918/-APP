package com.example.ui.english

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.EnglishExpression
import com.example.util.english.EnglishTTSHelper

enum class BirthdayInteractiveMode {
    PLATES_CANDLES, // Lesson 1: How many plates? / How many candles?
    AGE_WISHES      // Lesson 4: How old are you? / Happy birthday!
}

enum class NumberAnswerType {
    WORD,
    DIGIT,
    CHINESE_MEANING,
    QUANTITY_VISUAL
}

data class NumberFact(
    val word: String,
    val value: Int,
    val displayObjects: List<String>
)

data class GalaxyAnswerOption(
    val type: NumberAnswerType,
    val text: String,
    val isCorrect: Boolean
)

data class BirthdayAvatar(
    val id: String,
    val name: String,
    val role: String,
    val age: Int,
    val introSpeech: String,
    val description: String,
    val color: Color
)

@Composable
fun EnglishBirthdayInteractiveView(
    mode: BirthdayInteractiveMode,
    currentExpression: EnglishExpression? = null,
    ttsHelper: EnglishTTSHelper
) {
    val context = LocalContext.current
    
    // Core Number Facts Database (strictly treating numbers as NumberFact instead of simple strings!)
    val numberFacts = remember {
        listOf(
            NumberFact("one", 1, listOf("🎂")),
            NumberFact("two", 2, listOf("🎁", "🎁")),
            NumberFact("three", 3, listOf("🎈", "🎈", "🎈")),
            NumberFact("four", 4, listOf("🧁", "🧁", "🧁", "🧁")),
            NumberFact("five", 5, listOf("🍽️", "🍽️", "🍽️", "🍽️", "🍽️")),
            NumberFact("six", 6, listOf("🕯️", "🕯️", "🕯️", "🕯️", "🕯️", "🕯️")),
            NumberFact("seven", 7, listOf("🍪", "🍪", "🍪", "🍪", "🍪", "🍪", "🍪")),
            NumberFact("eight", 8, listOf("🍓", "🍓", "🍓", "🍓", "🍓", "🍓", "🍓", "🍓")),
            NumberFact("nine", 9, listOf("🍬", "🍬", "🍬", "🍬", "🍬", "🍬", "🍬", "🍬", "🍬")),
            NumberFact("ten", 10, listOf("✨", "✨", "✨", "✨", "✨", "✨", "✨", "✨", "✨", "✨"))
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.5.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (mode) {
                BirthdayInteractiveMode.PLATES_CANDLES -> {
                    PlatesCandlesInteractive(currentExpression, numberFacts, ttsHelper)
                }
                BirthdayInteractiveMode.AGE_WISHES -> {
                    AgeWishesInteractive(numberFacts, ttsHelper)
                }
            }
        }
    }
}

@Composable
fun PlatesCandlesInteractive(
    expression: EnglishExpression?,
    numberFacts: List<NumberFact>,
    ttsHelper: EnglishTTSHelper
) {
    val context = LocalContext.current
    val expText = expression?.englishText?.lowercase() ?: ""
    
    // Extract target quantity from the expression (e.g. plates / candles, five / six)
    val targetFact = remember(expText) {
        val found = numberFacts.find { expText.contains(it.word) }
        found ?: numberFacts[4] // default to five
    }
    
    val targetItemName = remember(expText) {
        if (expText.contains("candle")) "candles" else "plates"
    }
    
    val itemEmoji = remember(targetItemName) {
        if (targetItemName == "candles") "🕯️" else "🍽️"
    }

    var selectedOptionText by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var itemsFilledCount by remember { mutableStateOf(0) }

    // Reset when expression changes
    LaunchedEffect(expression) {
        selectedOptionText = null
        isSubmitted = false
        isSuccess = false
        itemsFilledCount = 0
    }

    // Generate Galaxy Answer Options (6 options in a clean 2-column layout)
    val galaxyOptions = remember(targetFact) {
        val corrects = listOf(
            GalaxyAnswerOption(NumberAnswerType.WORD, targetFact.word, true),
            GalaxyAnswerOption(NumberAnswerType.DIGIT, targetFact.value.toString(), true),
            GalaxyAnswerOption(NumberAnswerType.CHINESE_MEANING, "${targetFact.value}个", true)
        )
        
        val wrongFacts = numberFacts.filter { it.value != targetFact.value }.shuffled().take(3)
        val wrongs = listOf(
            GalaxyAnswerOption(NumberAnswerType.WORD, wrongFacts[0].word, false),
            GalaxyAnswerOption(NumberAnswerType.DIGIT, wrongFacts[1].value.toString(), false),
            GalaxyAnswerOption(NumberAnswerType.CHINESE_MEANING, "${wrongFacts[2].value}个", false)
        )
        
        (corrects + wrongs).shuffled()
    }

    // Plate packing animation logic
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            ttsHelper.speak("Great! ${targetFact.word} $targetItemName!", isSlow = false)
            for (i in 1..targetFact.value) {
                kotlinx.coroutines.delay(200)
                itemsFilledCount = i
            }
        }
    }

    Text(
        text = "🌌 星语生日补给站 - 数量解算仪",
        color = Color(0xFFEC4899),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
    )

    if (expression != null) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = expression.englishText,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = expression.chineseTranslation,
                    color = Color(0xFF00E5FF),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    // Top Section: Physical Item Assembly Panel
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📦 所需 $targetItemName：${targetFact.value} 个",
                color = Color(0xFF00E5FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,
                maxItemsInEachRow = 6
            ) {
                for (i in 0 until targetFact.value) {
                    val isFilled = i < itemsFilledCount
                    val scale by animateFloatAsState(
                        targetValue = if (isFilled) 1.25f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(38.dp)
                            .scale(scale)
                            .background(
                                if (isFilled) Color(0xFF10B981).copy(alpha = 0.25f)
                                else Color(0xFF334155).copy(alpha = 0.4f),
                                CircleShape
                            )
                            .border(
                                1.5.dp,
                                if (isFilled) Color(0xFF10B981) else Color(0xFF475569),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isFilled && targetItemName == "plates") "🍰" else itemEmoji,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }

    // Bottom Section: Galactic Code Decoder Console (2-column responsive layout)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🌟 请选择正确的星河解算密码：",
                color = Color(0xFF00E5FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                galaxyOptions.forEachIndexed { idx, opt ->
                    val isSel = selectedOptionText == opt.text
                    val buttonColor = if (isSubmitted) {
                        if (opt.isCorrect) Color(0xFF10B981)
                        else if (isSel) Color(0xFFEF4444)
                        else Color(0xFF334155)
                    } else {
                        if (isSel) Color(0xFFEC4899) else Color(0xFF1E293B)
                    }

                    Button(
                        onClick = {
                            if (!isSubmitted) {
                                selectedOptionText = opt.text
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isSel) Color(0xFFF472B6) else Color(0xFF334155)),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 42.dp)
                            .testTag("galaxy_option_${idx + 1}"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = opt.text,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = opt.type.name,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (!isSubmitted) {
        Button(
            onClick = {
                if (selectedOptionText == null) {
                    Toast.makeText(context, "请先选择一个星河解算按钮！", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isSubmitted = true
                val matched = galaxyOptions.find { it.text == selectedOptionText }
                if (matched?.isCorrect == true) {
                    isSuccess = true
                    Toast.makeText(context, "💡 解算成功！开始装配生日蛋糕！", Toast.LENGTH_SHORT).show()
                } else {
                    isSuccess = false
                    ttsHelper.speak("Oh, try again!", isSlow = false)
                    Toast.makeText(context, "❌ 解算密码错误，请重新点击或进行下一题", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("submit_galaxy_answer")
        ) {
            Text("🛰️ 提交星河解算密码", fontWeight = FontWeight.Bold, color = Color.White)
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    selectedOptionText = null
                    isSubmitted = false
                    isSuccess = false
                    itemsFilledCount = 0
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("retry_galaxy_answer")
            ) {
                Text("🔄 重新解算", color = Color.White)
            }

            Button(
                onClick = {
                    // Simulates automatic progression inside parent
                    Toast.makeText(context, "点击右下角『下一关』继续星河挑战！", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier
                    .weight(1.2f)
                    .height(44.dp)
                    .testTag("next_galaxy_stage")
            ) {
                Text("🚀 解算成功 · 前往下一步", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun AgeWishesInteractive(
    numberFacts: List<NumberFact>,
    ttsHelper: EnglishTTSHelper
) {
    val context = LocalContext.current
    
    // Virtual birthday partners (strictly complying with textbook pages Sam, John, Wu Binbin)
    val avatars = remember {
        listOf(
            BirthdayAvatar("sam", "Sam", "弟弟 (Brother)", 6, "This is my brother, Sam. I'm six years old.", "Sam 的6岁生日宴会", Color(0xFF00E5FF)),
            BirthdayAvatar("john", "John", "朋友 (Friend)", 9, "I'm nine. Happy birthday to you!", "John 的9岁生日宴会", Color(0xFFEC4899)),
            BirthdayAvatar("wubinbin", "Wu Binbin", "朋友 (Friend)", 10, "How old are you, John? I'm ten.", "Wu Binbin 的10岁大蛋糕", Color(0xFF10B981))
        )
    }

    var selectedAvatarIndex by remember { mutableStateOf(0) }
    val activeAvatar = avatars[selectedAvatarIndex]
    
    // Candle igniter count
    var currentCandlesOnCake by remember { mutableStateOf(0) }
    var showParticleOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(selectedAvatarIndex) {
        currentCandlesOnCake = 0
        showParticleOverlay = false
        ttsHelper.speak(activeAvatar.introSpeech, isSlow = false)
    }

    Text(
        text = "🎂 星际生日宴会厅 - 蜡烛点燃仪",
        color = Color(0xFF00E5FF),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
    )

    // Character Selector Tabs
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        avatars.forEachIndexed { index, avatar ->
            val isSelected = index == selectedAvatarIndex
            OutlinedButton(
                onClick = { selectedAvatarIndex = index },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) avatar.color.copy(alpha = 0.2f) else Color.Transparent
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isSelected) avatar.color else Color(0xFF334155)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("avatar_tab_${avatar.id}")
            ) {
                Text(
                    text = avatar.name,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Interactive Arena Panel
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, activeAvatar.color.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Character Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(activeAvatar.color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activeAvatar.name.take(1),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeAvatar.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(activeAvatar.color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = activeAvatar.role,
                                color = activeAvatar.color,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "🎂 目标年龄: ${activeAvatar.age} 岁 (Years Old)",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = { ttsHelper.speak(activeAvatar.introSpeech, isSlow = false) },
                    modifier = Modifier
                        .background(Color(0xFF0F172A), CircleShape)
                        .testTag("avatar_speak_${activeAvatar.id}")
                ) {
                    Icon(Icons.Default.Star, contentDescription = "朗读", tint = Color.Yellow)
                }
            }

            Divider(color = Color(0xFF334155))

            // The Birthday Cake Canvas with Candle Slots
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF0F172A).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (showParticleOverlay) {
                    // Fullscreen celebration overlay
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✨🌟 STAR LIGHT 🌟✨", color = Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Happy Birthday!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("🎶 Happy birthday to you! 🎶", color = Color(0xFF00E5FF), fontSize = 11.sp)
                    }
                } else {
                    // Real graphic display of the birthday cake
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Display candles
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            for (c in 0 until currentCandlesOnCake) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🔥", fontSize = 14.sp)
                                    Box(
                                        modifier = Modifier
                                            .width(6.dp)
                                            .height(24.dp)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Yellow, Color.Red)
                                                ),
                                                RoundedCornerShape(3.dp)
                                            )
                                    )
                                }
                            }
                        }

                        // Big Cake emoji
                        Icon(
                            imageVector = Icons.Default.Cake,
                            contentDescription = "Cake",
                            tint = activeAvatar.color,
                            modifier = Modifier.size(54.dp)
                        )

                        Text(
                            text = "已点燃 $currentCandlesOnCake / ${activeAvatar.age} 支星能蜡烛",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Star Candle Igniter Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (currentCandlesOnCake < activeAvatar.age) {
                            currentCandlesOnCake++
                            ttsHelper.speak("candle $currentCandlesOnCake", isSlow = false)
                            if (currentCandlesOnCake == activeAvatar.age) {
                                showParticleOverlay = true
                                ttsHelper.speak("Happy birthday to you!", isSlow = false)
                                Toast.makeText(context, "🎉 生日快乐！星能蜡烛全部点燃！", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "蜡烛已经全部插好并点燃啦！", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = activeAvatar.color),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(44.dp)
                        .testTag("ignite_candle_button")
                ) {
                    Text("🕯️ 点燃1支星能蜡烛", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        currentCandlesOnCake = 0
                        showParticleOverlay = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("reset_candle_button")
                ) {
                    Text("🔄 重置", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
