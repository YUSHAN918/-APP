package com.example.ui.math

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
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

@Composable
fun MathNumericKeyboard(
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("-", "0", ".")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .clickable { onKeyPress(key) }
                            .testTag("math_key_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Action Row: Clear, Delete, Confirm
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Clear
            Button(
                onClick = onClear,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("math_key_clear"),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Clear, contentDescription = "清除", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("清空", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            // Delete
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("math_key_delete"),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Backspace, contentDescription = "删除", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("回退", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            // Confirm/Dismiss (Hides Keyboard)
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                modifier = Modifier
                    .weight(1.2f)
                    .height(52.dp)
                    .testTag("math_key_confirm"),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "收起", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("收起键盘", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FractionInputField(
    numerator: String,
    denominator: String,
    isNumeratorFocused: Boolean,
    onNumeratorClick: () -> Unit,
    onDenominatorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(140.dp)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Numerator Input Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isNumeratorFocused) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color(0xFF1E293B))
                .border(
                    width = if (isNumeratorFocused) 2.dp else 1.dp,
                    color = if (isNumeratorFocused) Color(0xFF00E5FF) else Color(0xFF475569),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onNumeratorClick() }
                .testTag("fraction_numerator"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = numerator.ifEmpty { "分子" },
                color = if (numerator.isEmpty()) Color.Gray else Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Fraction Line
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.White)
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Denominator Input Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (!isNumeratorFocused) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color(0xFF1E293B))
                .border(
                    width = if (!isNumeratorFocused) 2.dp else 1.dp,
                    color = if (!isNumeratorFocused) Color(0xFF00E5FF) else Color(0xFF475569),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onDenominatorClick() }
                .testTag("fraction_denominator"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = denominator.ifEmpty { "分母" },
                color = if (denominator.isEmpty()) Color.Gray else Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MathOptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFF1E293B).copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF3B82F6) else Color(0xFF334155)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("math_option_${text.take(1)}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF3B82F6),
                    unselectedColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            MathRichText(
                text = text,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RatioInputComponent(
    leftText: String,
    rightText: String,
    isLeftFocused: Boolean,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left term box (前项)
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isLeftFocused) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color(0xFF1E293B))
                .border(
                    width = if (isLeftFocused) 2.dp else 1.dp,
                    color = if (isLeftFocused) Color(0xFF00E5FF) else Color(0xFF475569),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onLeftClick() }
                .testTag("ratio_left_term"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = leftText.ifEmpty { "前项" },
                color = if (leftText.isEmpty()) Color.Gray else Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Fixed Colon Symbol ( : )
        Text(
            text = ":",
            color = Color(0xFF00E5FF),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Right term box (后项)
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (!isLeftFocused) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color(0xFF1E293B))
                .border(
                    width = if (!isLeftFocused) 2.dp else 1.dp,
                    color = if (!isLeftFocused) Color(0xFF00E5FF) else Color(0xFF475569),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onRightClick() }
                .testTag("ratio_right_term"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rightText.ifEmpty { "后项" },
                color = if (rightText.isEmpty()) Color.Gray else Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
