package com.example.ui.english

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.EnglishExpression
import com.example.data.english.EnglishNumberRepository
import com.example.data.english.EnglishQuantityScene
import com.example.data.english.CountableObjectType
import com.example.data.english.QuantityQuestionMode
import com.example.util.english.EnglishTTSHelper

enum class QuantityCardType {
    GALLERY_VIEW,  // 情景美景展示 (如 Look at the kites!)
    QUIZ_QUESTION, // 数量互动提问 (如 How many kites do you see?)
    ANSWER_NOTICE  // 答案数量确认 (如 I see 12!)
}

data class QuantityExpressionSceneConfig(
    val titleZh: String,
    val questionText: String,
    val cardType: QuantityCardType,
    val primaryEmoji: String,
    val secondaryEmoji: String? = null,
    val primaryCount: Int,
    val secondaryCount: Int = 0,
    val targetAnswer: Int,
    val objectLabel: String,
    val primaryBgColor: Color = Color(0xFFFFF8E1)
)

fun resolveQuantitySceneConfig(expression: EnglishExpression): QuantityExpressionSceneConfig {
    val text = expression.englishText.lowercase()
    return when {
        // --- Lesson 1: Kites & Birds ---
        text.contains("look at the kites") -> QuantityExpressionSceneConfig(
            titleZh = "风筝草地 (11只风筝与1只小鸟)",
            questionText = "Look at the kites!",
            cardType = QuantityCardType.GALLERY_VIEW,
            primaryEmoji = "🪁",
            secondaryEmoji = "🐦",
            primaryCount = 11,
            secondaryCount = 1,
            targetAnswer = 12,
            objectLabel = "kites"
        )
        text.contains("so beautiful") -> QuantityExpressionSceneConfig(
            titleZh = "美景画廊 (彩虹风筝群)",
            questionText = "Wow, so beautiful!",
            cardType = QuantityCardType.GALLERY_VIEW,
            primaryEmoji = "🪁",
            primaryCount = 12,
            targetAnswer = 12,
            objectLabel = "kites"
        )
        text.contains("how many kites") -> QuantityExpressionSceneConfig(
            titleZh = "看数提问 (你能看到多少只风筝？)",
            questionText = "How many kites do you see?",
            cardType = QuantityCardType.QUIZ_QUESTION,
            primaryEmoji = "🪁",
            secondaryEmoji = "🐦",
            primaryCount = 11,
            secondaryCount = 1,
            targetAnswer = 12,
            objectLabel = "kites"
        )
        text.contains("i see 12") || text.contains("i see twelve") || text.contains("12") -> QuantityExpressionSceneConfig(
            titleZh = "数量确认 (12只风筝)",
            questionText = "1, 2 ... I see 12!",
            cardType = QuantityCardType.ANSWER_NOTICE,
            primaryEmoji = "🪁",
            primaryCount = 12,
            targetAnswer = 12,
            objectLabel = "kites"
        )
        text.contains("black one is a bird") || text.contains("bird!") -> QuantityExpressionSceneConfig(
            titleZh = "细节发现 (黑色的是一只鸟！)",
            questionText = "The black one is a bird!",
            cardType = QuantityCardType.GALLERY_VIEW,
            primaryEmoji = "🪁",
            secondaryEmoji = "🐦",
            primaryCount = 11,
            secondaryCount = 1,
            targetAnswer = 1,
            objectLabel = "birds"
        )
        text.contains("how many birds") -> QuantityExpressionSceneConfig(
            titleZh = "看数提问 (你能看到多少只鸟？)",
            questionText = "How many birds do you see?",
            cardType = QuantityCardType.QUIZ_QUESTION,
            primaryEmoji = "🪁",
            secondaryEmoji = "🐦",
            primaryCount = 11,
            secondaryCount = 1,
            targetAnswer = 1,
            objectLabel = "birds"
        )
        text.contains("how many fish") -> QuantityExpressionSceneConfig(
            titleZh = "池塘看数 (你能看到多少条鱼？)",
            questionText = "How many fish do you see?",
            cardType = QuantityCardType.QUIZ_QUESTION,
            primaryEmoji = "🐟",
            primaryCount = 15,
            targetAnswer = 15,
            objectLabel = "fish"
        )
        text.contains("fifteen") -> QuantityExpressionSceneConfig(
            titleZh = "数量确认 (15条小鱼)",
            questionText = "I see fifteen.",
            cardType = QuantityCardType.ANSWER_NOTICE,
            primaryEmoji = "🐟",
            primaryCount = 15,
            targetAnswer = 15,
            objectLabel = "fish"
        )

        // --- Lesson 4: Crayons & Toy Cars ---
        text.contains("open it and see") -> QuantityExpressionSceneConfig(
            titleZh = "惊喜盒 (文具盒里的蜡笔)",
            questionText = "Open it and see!",
            cardType = QuantityCardType.GALLERY_VIEW,
            primaryEmoji = "🖍️",
            primaryCount = 16,
            targetAnswer = 16,
            objectLabel = "crayons"
        )
        text.contains("my new crayons") -> QuantityExpressionSceneConfig(
            titleZh = "文具盒 (16支彩色蜡笔)",
            questionText = "My new crayons.",
            cardType = QuantityCardType.GALLERY_VIEW,
            primaryEmoji = "🖍️",
            primaryCount = 16,
            targetAnswer = 16,
            objectLabel = "crayons"
        )
        text.contains("how many crayons") -> QuantityExpressionSceneConfig(
            titleZh = "计数提问 (你有多少支蜡笔？)",
            questionText = "How many crayons do you have?",
            cardType = QuantityCardType.QUIZ_QUESTION,
            primaryEmoji = "🖍️",
            primaryCount = 16,
            targetAnswer = 16,
            objectLabel = "crayons"
        )
        text.contains("16") || text.contains("sixteen") -> QuantityExpressionSceneConfig(
            titleZh = "数量确认 (16支蜡笔)",
            questionText = "You have 16 crayons!",
            cardType = QuantityCardType.ANSWER_NOTICE,
            primaryEmoji = "🖍️",
            primaryCount = 16,
            targetAnswer = 16,
            objectLabel = "crayons"
        )
        text.contains("toy cars") || text.contains("look at my new toy") -> QuantityExpressionSceneConfig(
            titleZh = "车库工坊 (20辆玩具车)",
            questionText = "Look at my new toy cars.",
            cardType = QuantityCardType.GALLERY_VIEW,
            primaryEmoji = "🚗",
            primaryCount = 20,
            targetAnswer = 20,
            objectLabel = "cars"
        )
        text.contains("how many cars") -> QuantityExpressionSceneConfig(
            titleZh = "计数提问 (你有多少辆车？)",
            questionText = "How many cars do you have?",
            cardType = QuantityCardType.QUIZ_QUESTION,
            primaryEmoji = "🚗",
            primaryCount = 20,
            targetAnswer = 20,
            objectLabel = "cars"
        )
        text.contains("twenty") || text.contains("20") -> QuantityExpressionSceneConfig(
            titleZh = "数量确认 (20辆玩具车)",
            questionText = "We have twenty.",
            cardType = QuantityCardType.ANSWER_NOTICE,
            primaryEmoji = "🚗",
            primaryCount = 20,
            targetAnswer = 20,
            objectLabel = "cars"
        )

        else -> QuantityExpressionSceneConfig(
            titleZh = "数字与计数",
            questionText = expression.englishText,
            cardType = QuantityCardType.QUIZ_QUESTION,
            primaryEmoji = "🪁",
            primaryCount = 12,
            targetAnswer = 12,
            objectLabel = "items"
        )
    }
}

/**
 * 针对 INTRO 句型学习页面的专有场景图画卡片。
 * 随 currentExpression 动态改变背景、情景和图形数量，图画大且清晰。
 */
@Composable
fun EnglishQuantityExpressionCard(
    currentExpression: EnglishExpression,
    ttsHelper: EnglishTTSHelper
) {
    val config = remember(currentExpression.expressionId) { resolveQuantitySceneConfig(currentExpression) }
    var countedSet by remember(currentExpression.expressionId) { mutableStateOf(setOf<Int>()) }
    var selectedOpt by remember(currentExpression.expressionId) { mutableStateOf<Int?>(null) }
    var isCorrectAnswer by remember(currentExpression.expressionId) { mutableStateOf<Boolean?>(null) }

    val totalItems = config.primaryCount + config.secondaryCount
    val targetNumberInfo = remember(config.targetAnswer) { EnglishNumberRepository.getByValue(config.targetAnswer) }

    val options = remember(currentExpression.expressionId) {
        val list = mutableListOf(config.targetAnswer)
        val candidates = (11..20).filter { it != config.targetAnswer }.shuffled(java.util.Random(currentExpression.expressionId.hashCode().toLong()))
        list.addAll(candidates.take(3))
        list.shuffled(java.util.Random(currentExpression.expressionId.hashCode().toLong() + 1))
    }

    val primaryColor = Color(0xFFFF8F00)
    val cardBg = Color(0xFFFFFBEB)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("quantity_expression_card_${currentExpression.expressionId}")
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🎨 ${config.titleZh}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = "点击可声光计数 (${countedSet.size}/$totalItems)",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // High Resolution Flat Object Grid (Flow / Row Grid)
            // 解决旧版 LazyVerticalGrid 被压扁不见的问题
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE0B2), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Render items in rows of 5
                    val rowCount = (totalItems + 4) / 5
                    for (r in 0 until rowCount) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (c in 0 until 5) {
                                val idx = r * 5 + c
                                if (idx < totalItems) {
                                    val isSecondary = idx >= config.primaryCount
                                    val emoji = if (isSecondary) (config.secondaryEmoji ?: "🐦") else config.primaryEmoji
                                    val isCounted = countedSet.contains(idx)
                                    val itemScale by animateFloatAsState(targetValue = if (isCounted) 1.1f else 1f, label = "itemScale")

                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .scale(itemScale)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    isSecondary && isCounted -> Color(0xFFE1F5FE)
                                                    isCounted -> Color(0xFFFFECB3)
                                                    else -> Color(0xFFFAFAFA)
                                                }
                                            )
                                            .border(
                                                width = if (isCounted) 2.dp else 1.dp,
                                                color = when {
                                                    isSecondary -> Color(0xFF0288D1)
                                                    isCounted -> primaryColor
                                                    else -> Color(0xFFE0E0E0)
                                                },
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                val next = if (isCounted) countedSet - idx else countedSet + idx
                                                countedSet = next
                                                ttsHelper.speak("${next.size}")
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = emoji,
                                            fontSize = 24.sp
                                        )

                                        if (isCounted) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .align(Alignment.TopEnd)
                                                    .background(
                                                        if (isSecondary) Color(0xFF0288D1) else Color(0xFFE65100),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${countedSet.toList().sorted().indexOf(idx) + 1}",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(44.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Answers / Question Options if CardType == QUIZ_QUESTION
            if (config.cardType == QuantityCardType.QUIZ_QUESTION) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "❓ ${config.questionText}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    options.forEach { optVal ->
                        val optInfo = EnglishNumberRepository.getByValue(optVal)
                        val isSel = selectedOpt == optVal
                        val btnColor = when {
                            isSel && isCorrectAnswer == true -> Color(0xFF4CAF50)
                            isSel && isCorrectAnswer == false -> Color(0xFFF44336)
                            else -> primaryColor
                        }

                        Button(
                            onClick = {
                                selectedOpt = optVal
                                val correct = (optVal == config.targetAnswer)
                                isCorrectAnswer = correct
                                if (correct) {
                                    ttsHelper.speak("That's right! ${optInfo?.numberWord ?: optVal}")
                                } else {
                                    ttsHelper.speak("Try again!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("expr_opt_$optVal")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$optVal",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = optInfo?.numberWord ?: "",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                if (isCorrectAnswer == true) {
                    Text(
                        text = "🎉 回答正确！数量是 ${config.targetAnswer} (${targetNumberInfo?.numberWord})",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            } else if (config.cardType == QuantityCardType.ANSWER_NOTICE) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "✨ 目标数量：${config.targetAnswer}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${targetNumberInfo?.numberWord} / ${targetNumberInfo?.chineseNumeral})",
                        fontSize = 14.sp,
                        color = Color(0xFF5D4037)
                    )
                }
            }
        }
    }
}

/**
 * 通用全功能交互数数板 (用于练习环节 / 单元挑战)
 */
@Composable
fun EnglishQuantityCountingView(
    scene: EnglishQuantityScene,
    ttsHelper: EnglishTTSHelper,
    onInteractionCompleted: (Boolean) -> Unit,
    onInteractionStateChanged: (Boolean) -> Unit = {}
) {
    var countedIndices by remember(scene) { mutableStateOf(setOf<Int>()) }
    var selectedAnswer by remember(scene) { mutableStateOf<Int?>(null) }
    var isCorrect by remember(scene) { mutableStateOf<Boolean?>(null) }

    val totalCount = scene.objectCount
    val targetNumberInfo = remember(totalCount) { EnglishNumberRepository.getByValue(totalCount) }

    val questionText = remember(scene) {
        val plural = scene.objectType.plural
        if (scene.questionMode == QuantityQuestionMode.SEE) {
            "How many $plural do you see?"
        } else {
            "How many $plural do you have?"
        }
    }

    val options = remember(scene) {
        val list = mutableListOf(totalCount)
        val candidates = (11..20).filter { it != totalCount }.shuffled(java.util.Random(scene.randomSeed))
        list.addAll(candidates.take(3))
        list.shuffled(java.util.Random(scene.randomSeed + 1))
    }

    val themeBg = Color(0xFFFFF8E1)
    val cardBg = Color(0xFFFFF3E0)
    val primaryAmber = Color(0xFFFF8F00)
    val accentOrange = Color(0xFFE65100)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeBg)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Question Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .border(2.dp, primaryAmber, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (scene.questionMode == QuantityQuestionMode.SEE) "观察看数 (See)" else "数数拥有 (Have)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryAmber
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = questionText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                }
                IconButton(
                    onClick = { ttsHelper.speak(questionText) },
                    modifier = Modifier.testTag("speak_question_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "朗读问句",
                        tint = primaryAmber
                    )
                }
            }
        }

        // Object Grid Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .border(1.dp, Color(0xFFFFE0B2), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "点击物品点数 (${countedIndices.size}/$totalCount)",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // High clarity Flow Row
                val rowCount = (totalCount + 4) / 5
                for (r in 0 until rowCount) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (c in 0 until 5) {
                            val index = r * 5 + c
                            if (index < totalCount) {
                                val isCounted = countedIndices.contains(index)
                                val scale by animateFloatAsState(targetValue = if (isCounted) 1.08f else 1f, label = "scale")

                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .scale(scale)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isCounted) Color(0xFFFFECB3) else Color(0xFFFAFAFA))
                                        .border(
                                            width = if (isCounted) 2.dp else 1.dp,
                                            color = if (isCounted) primaryAmber else Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            val newSet = if (isCounted) countedIndices - index else countedIndices + index
                                            countedIndices = newSet
                                            ttsHelper.speak("${newSet.size}")
                                        }
                                        .semantics {
                                            contentDescription = "第 ${index + 1} 个${scene.objectType.singular}${if (isCounted) "已选择" else "未选择"}"
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = scene.objectType.emoji,
                                        fontSize = 26.sp
                                    )

                                    if (isCounted) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .align(Alignment.TopEnd)
                                                .background(accentOrange, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${countedIndices.toList().sorted().indexOf(index) + 1}",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(46.dp))
                            }
                        }
                    }
                }
            }
        }

        // Answer Selection Options
        Text(
            text = "请选择正确数量：",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { optValue ->
                val info = EnglishNumberRepository.getByValue(optValue)
                val isSelected = selectedAnswer == optValue
                val btnColor = when {
                    isSelected && isCorrect == true -> Color(0xFF4CAF50)
                    isSelected && isCorrect == false -> Color(0xFFF44336)
                    else -> primaryAmber
                }

                Button(
                    onClick = {
                        selectedAnswer = optValue
                        val correct = optValue == totalCount
                        isCorrect = correct
                        onInteractionStateChanged(correct)
                        onInteractionCompleted(correct)
                        if (correct) {
                            ttsHelper.speak("That's right! ${info?.numberWord ?: optValue}")
                        } else {
                            ttsHelper.speak("Try again!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quantity_option_$optValue")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$optValue",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = info?.numberWord ?: "",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Feedback Banner
        AnimatedVisibility(
            visible = isCorrect != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .background(
                        if (isCorrect == true) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isCorrect == true) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCorrect == true) "太棒了！答案是 $totalCount (${targetNumberInfo?.numberWord})" else "不完全正确，再仔细数一数哦！",
                    color = if (isCorrect == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
