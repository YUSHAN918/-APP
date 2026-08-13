package com.example.ui.english

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.EnglishClockTime
import com.example.data.english.EnglishTimeFormatter
import com.example.data.english.ScheduleItem
import com.example.data.english.TimeGrammarMode

@Composable
fun EnglishScheduleView(
    items: List<ScheduleItem>,
    modifier: Modifier = Modifier,
    isSortable: Boolean = false,
    onMoveItem: ((fromIndex: Int, toIndex: Int) -> Unit)? = null,
    onVerify: (() -> Unit)? = null,
    isCorrect: Boolean? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📅 虚拟角色的一天作息表",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Timeline List of Schedule Items
            Box(modifier = Modifier.heightIn(max = 320.dp)) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(items) { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Timeline dot
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Time & Activity details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = EnglishTimeFormatter.formatDigital(item.time),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = item.activityWordRef,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (item.grammarMode == TimeGrammarMode.TIME_FOR_NOUN)
                                        "It's time for ${item.activityWordRef}."
                                    else
                                        "It's time to ${item.activityWordRef}.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            // Sort controls (Up/Down arrow buttons for perfect responsive accessibility)
                            if (isSortable && onMoveItem != null) {
                                Row {
                                    IconButton(
                                        onClick = { if (index > 0) onMoveItem(index, index - 1) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowUpward,
                                            contentDescription = "Move Up",
                                            tint = if (index > 0) MaterialTheme.colorScheme.primary else Color.LightGray
                                        )
                                    }
                                    IconButton(
                                        onClick = { if (index < items.size - 1) onMoveItem(index, index + 1) },
                                        enabled = index < items.size - 1,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDownward,
                                            contentDescription = "Move Down",
                                            tint = if (index < items.size - 1) MaterialTheme.colorScheme.primary else Color.LightGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Verify action
            if (isSortable && onVerify != null) {
                Button(
                    onClick = onVerify,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_schedule_order_button")
                ) {
                    Text("校验作息时间顺序", fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = isCorrect != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isCorrect == true) Color(0xFFDEF7EC) else Color(0xFFFDE8E8)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Result Icon",
                        tint = if (isCorrect == true) Color(0xFF0E9F6E) else Color(0xFFF05252)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCorrect == true) "🎉 棒极了！顺序完全正确！" else "❌ 哎呀，作息表顺序还需要调整，再检查看看吧！",
                        color = if (isCorrect == true) Color(0xFF03543F) else Color(0xFF9B1C1C),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
