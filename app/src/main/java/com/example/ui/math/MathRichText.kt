package com.example.ui.math

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class MathToken {
    data class Text(val content: String) : MathToken()
    data class Fraction(val numerator: String, val denominator: String) : MathToken()
}

fun parseMathText(text: String): List<MathToken> {
    val tokens = mutableListOf<MathToken>()
    val regex = """(-?\d+)/(\d+)""".toRegex()
    var lastIndex = 0
    
    regex.findAll(text).forEach { matchResult ->
        val matchStart = matchResult.range.first
        val matchEnd = matchResult.range.last // inclusive
        
        // Check if there is preceding text before this match
        if (matchStart > lastIndex) {
            val precedingText = text.substring(lastIndex, matchStart)
            tokens.add(MathToken.Text(precedingText))
        }
        
        val numerator = matchResult.groupValues[1]
        val denominator = matchResult.groupValues[2]
        
        // Safety checks to avoid paths, URLs, dates, or denominator 0
        val isPrecededBySlash = matchStart > 0 && text[matchStart - 1] == '/'
        val isFollowedBySlash = matchEnd + 1 < text.length && text[matchEnd + 1] == '/'
        val isValidFraction = numerator.length <= 3 && denominator.length <= 3 && 
                              denominator != "0" && !isPrecededBySlash && !isFollowedBySlash
        
        if (isValidFraction) {
            tokens.add(MathToken.Fraction(numerator, denominator))
        } else {
            tokens.add(MathToken.Text(matchResult.value))
        }
        
        lastIndex = matchEnd + 1
    }
    
    if (lastIndex < text.length) {
        tokens.add(MathToken.Text(text.substring(lastIndex)))
    }
    
    return tokens
}

@Composable
fun InlineFraction(
    numerator: String,
    denominator: String,
    fontSize: TextUnit,
    color: Color,
    modifier: Modifier = Modifier
) {
    val fractionFontSize = (fontSize.value * 0.85f).sp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(horizontal = 3.dp)
            .width(IntrinsicSize.Max)
            .defaultMinSize(minWidth = 14.dp)
    ) {
        Text(
            text = numerator,
            color = color,
            fontSize = fractionFontSize,
            fontWeight = FontWeight.SemiBold,
            lineHeight = fractionFontSize,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(color)
        )
        Text(
            text = denominator,
            color = color,
            fontSize = fractionFontSize,
            fontWeight = FontWeight.SemiBold,
            lineHeight = fractionFontSize,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MathRichText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    lineHeight: TextUnit = 24.sp,
    textAlign: TextAlign = TextAlign.Start
) {
    val tokens = remember(text) { parseMathText(text) }
    
    if (tokens.size == 1 && tokens[0] is MathToken.Text) {
        // Fallback for simple performance optimization
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            lineHeight = lineHeight,
            textAlign = textAlign,
            modifier = modifier
        )
    } else {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.Center
        ) {
            tokens.forEach { token ->
                when (token) {
                    is MathToken.Text -> {
                        // Intelligent sub-token character/word split for perfect flow wrapping
                        val content = token.content
                        var currentWord = StringBuilder()
                        
                        for (char in content) {
                            if (char.isWhitespace()) {
                                if (currentWord.isNotEmpty()) {
                                    Text(
                                        text = currentWord.toString(),
                                        color = color,
                                        fontSize = fontSize,
                                        fontWeight = fontWeight,
                                        lineHeight = lineHeight
                                    )
                                    currentWord = StringBuilder()
                                }
                                Text(
                                    text = char.toString(),
                                    color = color,
                                    fontSize = fontSize,
                                    fontWeight = fontWeight,
                                    lineHeight = lineHeight
                                )
                            } else if (char.code in 0x4E00..0x9FFF) { // Chinese character block
                                if (currentWord.isNotEmpty()) {
                                    Text(
                                        text = currentWord.toString(),
                                        color = color,
                                        fontSize = fontSize,
                                        fontWeight = fontWeight,
                                        lineHeight = lineHeight
                                    )
                                    currentWord = StringBuilder()
                                }
                                Text(
                                    text = char.toString(),
                                    color = color,
                                    fontSize = fontSize,
                                    fontWeight = fontWeight,
                                    lineHeight = lineHeight
                                )
                            } else {
                                currentWord.append(char)
                            }
                        }
                        if (currentWord.isNotEmpty()) {
                            Text(
                                text = currentWord.toString(),
                                color = color,
                                fontSize = fontSize,
                                fontWeight = fontWeight,
                                lineHeight = lineHeight
                            )
                        }
                    }
                    is MathToken.Fraction -> {
                        InlineFraction(
                            numerator = token.numerator,
                            denominator = token.denominator,
                            fontSize = fontSize,
                            color = color
                        )
                    }
                }
            }
        }
    }
}
