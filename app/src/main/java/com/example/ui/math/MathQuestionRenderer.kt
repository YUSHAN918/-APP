package com.example.ui.math

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.example.data.math.MathQuestion
import com.example.data.math.MathQuestionType
import com.example.data.math.MathBlankSpec
import com.example.data.math.MathBlankInputType
import com.example.viewmodel.math.MathLessonUiState

@Composable
fun MathQuestionRenderer(
    question: MathQuestion,
    uiState: MathLessonUiState,
    onChoiceSelected: (String) -> Unit,
    onNumeratorFocused: (Boolean) -> Unit,
    onRatioLeftFocused: (Boolean) -> Unit = {},
    onBlankClicked: (Int) -> Unit,
    onUnitSelected: (String) -> Unit,
    onKeyPress: (String) -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Question Stem Box
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category/Knowledge Tag
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "人教版六年级上",
                            color = Color(0xFF60A5FA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = question.type.name,
                            color = Color(0xFF34D399),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Dynamic Offline Vector Diagram
                if (question.imageAsset != null) {
                    MathIllustrationRenderer(
                        imageAsset = question.imageAsset,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }

                // Question Stem text
                if (question.type != MathQuestionType.FILL_BLANK) {
                    MathRichText(
                        text = question.stem,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 26.sp,
                        modifier = Modifier.testTag("math_question_stem")
                    )
                } else {
                    val spec = question.answerSpec
                    if (spec.responseTemplate != null && spec.blankSpecs != null) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "请填入空白处的正确答案（可在下方选择方向或输入数值）：",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            CompositeAnswerBoard(
                                template = spec.responseTemplate,
                                blankSpecs = spec.blankSpecs,
                                blankAnswers = uiState.blankAnswers,
                                activeBlankIndex = uiState.activeBlankIndex,
                                isSubmitted = uiState.isSubmitted,
                                onBlankClicked = onBlankClicked
                            )
                        }
                    } else {
                        // Render Fill Blank with interactable blank blocks inline
                        val parts = question.stem.split("[blank]")
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "请点击空格并在下方输入答案（可使用快捷按钮）：",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                parts.forEachIndexed { index, part ->
                                    MathRichText(
                                        text = part,
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (index < parts.size - 1) {
                                        val currentBlankVal = uiState.blankAnswers.getOrNull(index) ?: ""
                                        // Highlight active focused blank
                                        val isFocused = index == uiState.activeBlankIndex
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .width(72.dp)
                                                .height(34.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isFocused) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFF0F172A))
                                                .border(
                                                    width = if (isFocused) 1.5.dp else 1.dp,
                                                    color = if (isFocused) Color(0xFF3B82F6) else Color(0xFF475569),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable { if (!uiState.isSubmitted) onBlankClicked(index) }
                                                .testTag("math_blank_$index"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = currentBlankVal.ifEmpty { "  " },
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Direction and Unit Quick Input Chips for FILL_BLANK questions
                            if (!uiState.isSubmitted) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "快捷点击输入：",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val chips = listOf("东", "西", "南", "北", "米", "度")
                                    chips.forEach { text ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                                                .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .clickable { onKeyPress(text) }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                .testTag("direction_chip_$text"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = text,
                                                color = Color(0xFF60A5FA),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .clickable { onDelete() }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                            .testTag("fill_blank_delete_chip"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "退格",
                                            color = Color(0xFFF87171),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Question-specific Interactive Input Panel
        when (question.type) {
            MathQuestionType.MULTIPLE_CHOICE -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    question.options.forEach { option ->
                        val optionLetter = option.take(1)
                        val isSelected = uiState.selectedChoice == optionLetter
                        MathOptionCard(
                            text = option,
                            isSelected = isSelected,
                            onClick = { if (!uiState.isSubmitted) onChoiceSelected(optionLetter) }
                        )
                    }
                }
            }

            MathQuestionType.FRACTION_INPUT -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FractionInputField(
                        numerator = uiState.userNumeratorText,
                        denominator = uiState.userDenominatorText,
                        isNumeratorFocused = uiState.isNumeratorFocused,
                        onNumeratorClick = { if (!uiState.isSubmitted) onNumeratorFocused(true) },
                        onDenominatorClick = { if (!uiState.isSubmitted) onNumeratorFocused(false) }
                    )
                }
            }

            MathQuestionType.RATIO_INPUT -> {
                RatioInputComponent(
                    leftText = uiState.userRatioLeftText,
                    rightText = uiState.userRatioRightText,
                    isLeftFocused = uiState.isRatioLeftFocused,
                    onLeftClick = { if (!uiState.isSubmitted) onRatioLeftFocused(true) },
                    onRightClick = { if (!uiState.isSubmitted) onRatioLeftFocused(false) }
                )
            }

            MathQuestionType.EXPRESSION_INPUT -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = uiState.userAnswerText.ifEmpty { "请输入化简后的结果" },
                            color = if (uiState.userAnswerText.isEmpty()) Color.Gray else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("math_expression_input")
                        )
                    }
                }
            }

            MathQuestionType.WORD_PROBLEM -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = uiState.userAnswerText.ifEmpty { "请输入数值与单位" },
                            color = if (uiState.userAnswerText.isEmpty()) Color.Gray else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("math_word_problem_input")
                        )
                    }

                    // Unit quick chips
                    Text(
                        text = "常用单位快捷输入：",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("米", "厘米", "平方厘米", "m²", "kg", "个").forEach { unit ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF334155))
                                    .clickable { onUnitSelected(unit) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("unit_chip_$unit"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unit,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            MathQuestionType.NUMERIC_INPUT -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = uiState.userAnswerText.ifEmpty { "在下方键盘输入计算结果" },
                            color = if (uiState.userAnswerText.isEmpty()) Color.Gray else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("math_numeric_input")
                        )
                    }
                }
            }

            else -> {}
        }
    }
}

sealed class TemplateSegment {
    data class Text(val content: String) : TemplateSegment()
    data class Placeholder(val index: Int) : TemplateSegment()
}

fun parseTemplate(template: String): List<TemplateSegment> {
    val regex = "\\{(\\d+)\\}".toRegex()
    val matches = regex.findAll(template)
    val segments = mutableListOf<TemplateSegment>()
    var lastIndex = 0
    for (match in matches) {
        if (match.range.first > lastIndex) {
            segments.add(TemplateSegment.Text(template.substring(lastIndex, match.range.first)))
        }
        val index = match.groupValues[1].toInt()
        segments.add(TemplateSegment.Placeholder(index))
        lastIndex = match.range.last + 1
    }
    if (lastIndex < template.length) {
        segments.add(TemplateSegment.Text(template.substring(lastIndex)))
    }
    return segments
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompositeAnswerBoard(
    template: String,
    blankSpecs: List<MathBlankSpec>,
    blankAnswers: List<String>,
    activeBlankIndex: Int,
    isSubmitted: Boolean,
    onBlankClicked: (Int) -> Unit
) {
    val segments = remember(template) { parseTemplate(template) }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalArrangement = Arrangement.Center
    ) {
        segments.forEach { segment ->
            when (segment) {
                is TemplateSegment.Text -> {
                    Text(
                        text = segment.content,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp),
                        lineHeight = 28.sp
                    )
                }
                is TemplateSegment.Placeholder -> {
                    val index = segment.index
                    val spec = blankSpecs.getOrNull(index)
                    val value = blankAnswers.getOrNull(index) ?: ""
                    val isFocused = index == activeBlankIndex && !isSubmitted

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .widthIn(min = 64.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isFocused) Color(0xFF00E5FF).copy(alpha = 0.15f)
                                else Color(0xFF1E293B)
                            )
                            .border(
                                width = if (isFocused) 1.5.dp else 1.dp,
                                color = if (isFocused) Color(0xFF00E5FF) else Color(0xFF475569),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { if (!isSubmitted) onBlankClicked(index) }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value.ifEmpty { spec?.label ?: " " },
                            color = if (value.isEmpty()) Color.Gray else Color(0xFF00E5FF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
