package com.example.ui.english

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DiffCharType {
    MATCH,     // Green
    MISMATCH,  // Red
    MISSING,   // Gray dashed
    EXTRA,     // Orange
    SPACE      // Blue badge
}

data class DiffItem(
    val charDisplay: String,
    val type: DiffCharType
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnglishAnswerDiffView(
    userAnswer: String,
    targetWord: String,
    chineseMeaning: String,
    modifier: Modifier = Modifier
) {
    val userClean = userAnswer.trim()
    val targetClean = targetWord.trim()

    val targetParts = targetClean.split("\\s+".toRegex()).filter { it.isNotEmpty() }
    val userParts = userClean.split("\\s+".toRegex()).filter { it.isNotEmpty() }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "正确答案与输入对比",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Word-by-word diff layout (stacked vertically per word)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val maxWordCount = maxOf(targetParts.size, userParts.size)
                for (wIdx in 0 until maxWordCount) {
                    val targetPart = targetParts.getOrNull(wIdx) ?: ""
                    val userPart = userParts.getOrNull(wIdx) ?: ""

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val maxCharLen = maxOf(targetPart.length, userPart.length)
                        for (cIdx in 0 until maxCharLen) {
                            val tChar = targetPart.getOrNull(cIdx)
                            val uChar = userPart.getOrNull(cIdx)

                            val diffType = when {
                                tChar != null && uChar != null && tChar.equals(uChar, ignoreCase = true) -> DiffCharType.MATCH
                                tChar != null && uChar != null -> DiffCharType.MISMATCH
                                tChar != null && uChar == null -> DiffCharType.MISSING
                                else -> DiffCharType.EXTRA
                            }

                            val displayChar = when (diffType) {
                                DiffCharType.MATCH -> tChar.toString()
                                DiffCharType.MISMATCH -> uChar.toString()
                                DiffCharType.MISSING -> tChar.toString()
                                DiffCharType.EXTRA -> uChar.toString()
                                else -> ""
                            }

                            val boxBg = when (diffType) {
                                DiffCharType.MATCH -> Color(0xFF10B981)
                                DiffCharType.MISMATCH -> Color(0xFFEF4444)
                                DiffCharType.MISSING -> Color(0xFF334155)
                                DiffCharType.EXTRA -> Color(0xFFF59E0B)
                                else -> Color.Transparent
                            }

                            val boxBorder = when (diffType) {
                                DiffCharType.MATCH -> Color(0xFF34D399)
                                DiffCharType.MISMATCH -> Color(0xFFF87171)
                                DiffCharType.MISSING -> Color(0xFF94A3B8)
                                DiffCharType.EXTRA -> Color(0xFFFBBF24)
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(boxBg, RoundedCornerShape(6.dp))
                                    .border(1.dp, boxBorder, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayChar,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Explicit space separator if target has more words
                    if (wIdx < targetParts.lastIndex) {
                        val userHasSpace = wIdx < userParts.lastIndex
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (userHasSpace) Color(0xFF3B82F6).copy(alpha = 0.25f)
                                        else Color(0xFFEF4444).copy(alpha = 0.25f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (userHasSpace) Color(0xFF3B82F6) else Color(0xFFEF4444),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (userHasSpace) "空格 ␠" else "缺失空格!",
                                    color = if (userHasSpace) Color(0xFF60A5FA) else Color(0xFFF87171),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF334155))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("你的输入:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text(
                        text = if (userClean.isEmpty()) "（未输入）" else userClean,
                        color = Color(0xFFF87171),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("正确拼写:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text(
                        text = targetClean,
                        color = Color(0xFF34D399),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("中文释义:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Text(
                        text = chineseMeaning,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(Color(0xFF10B981), "正确")
                LegendItem(Color(0xFFEF4444), "错误")
                LegendItem(Color(0xFF334155), "缺失")
                LegendItem(Color(0xFFF59E0B), "多余")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 11.sp)
    }
}
