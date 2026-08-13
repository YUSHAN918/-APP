package com.example.ui.english

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EnglishWordAnswerBoard(
    targetWord: String,
    assembledLettersFlat: String,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false,
    slotSize: Dp = 44.dp,
    onSlotClick: ((flatIndex: Int) -> Unit)? = null
) {
    // Filter out spaces from assembledLettersFlat for clean slot filling
    val cleanLetters = assembledLettersFlat.replace(" ", "")
    val wordParts = targetWord.split("\\s+".toRegex()).filter { it.isNotEmpty() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        wordParts.forEachIndexed { wordIndex, wordPart ->
            val actualSlotSize = when {
                wordPart.length >= 10 -> 30.dp
                wordPart.length >= 8 -> 34.dp
                else -> slotSize
            }

            val previousLengthSum = wordParts.take(wordIndex).sumOf { it.length }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                wordPart.forEachIndexed { charIndex, _ ->
                    val flatIndex = previousLengthSum + charIndex
                    val userChar = cleanLetters.getOrNull(flatIndex)
                    val isFilled = userChar != null

                    Box(
                        modifier = Modifier
                            .size(actualSlotSize)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isFilled) Color(0xFF1E293B) else Color(0xFF334155).copy(alpha = 0.4f)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isFilled) Color(0xFF00E5FF) else Color(0xFF475569),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .then(
                                if (!isReadOnly && isFilled && onSlotClick != null) {
                                    Modifier.clickable { onSlotClick(flatIndex) }
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userChar?.toString() ?: "",
                            color = Color.White,
                            fontSize = (actualSlotSize.value * 0.45f).sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
