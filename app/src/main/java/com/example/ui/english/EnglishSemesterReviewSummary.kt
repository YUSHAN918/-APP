package com.example.ui.english

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
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
import com.example.data.english.EnglishSemesterReviewEngine
import com.example.data.english.EnglishSemesterReviewSummary

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EnglishSemesterReviewSummaryView(
    summary: EnglishSemesterReviewSummary,
    onNavigateToUnit: (String) -> Unit,
    onReplayBoardGame: () -> Unit,
    onReturnToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDetailedData by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner: Celebration
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "星语海港学期庆典奖杯",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "🎉 星语海港·学期大庆典",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (summary.semesterId.contains("s2")) "PEP 2013 三年级下册 (Unit 1—Unit 6) 综合复习汇总" else "PEP 2013 三年级上册 (Unit 1—Unit 6) 综合复习汇总",
                    color = Color(0xFFA5B4FC),
                    fontSize = 13.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF312E81)
                    ) {
                        Text(
                            text = "已完成 ${summary.completedUnits.size}/6 单元",
                            color = Color(0xFFC7D2FE),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF065F46)
                    ) {
                        Text(
                            text = "Recycle 1&2 通关",
                            color = Color(0xFF6EE7B7),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Highlight 1: Learned Themes This Semester
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🌟 本学期学会的主题",
                    color = Color(0xFFF472B6),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                summary.topicSummary.forEach { topic ->
                    val statusBg = when (topic.statusLabel) {
                        "很稳定" -> Color(0xFF065F46)
                        "正在进步" -> Color(0xFF075985)
                        else -> Color(0xFF991B1B)
                    }
                    val statusTextCol = when (topic.statusLabel) {
                        "很稳定" -> Color(0xFF6EE7B7)
                        "正在进步" -> Color(0xFF38BDF8)
                        else -> Color(0xFFFCA5A5)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .clickable { onNavigateToUnit(topic.targetUnitId) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.topicTitle,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "词汇掌握: ${topic.masteredWords}/${topic.totalWords} 词",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = statusBg
                        ) {
                            Text(
                                text = topic.statusLabel,
                                color = statusTextCol,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Highlight 2: Personalized Review Entry Points
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🎯 个性化复习推荐路径",
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                summary.recommendedReviewItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0284C7))
                            .clickable { onNavigateToUnit(item.targetUnitId) }
                            .padding(12.dp)
                            .testTag("review_entry_${item.topicId}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = item.reason,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "跳转专项复习",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Expandable Detailed Evidence Area
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDetailedData = !showDetailedData }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊 详细能力与学习证据清单",
                        color = Color(0xFFFDE047),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Icon(
                        imageVector = if (showDetailedData) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "展开或折叠证据清单",
                        tint = Color.White
                    )
                }

                AnimatedVisibility(visible = showDetailedData) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("🎯 八大能力维度诊断", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        summary.skillSummary.forEach { skill ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(skill.skillName, color = Color.White, fontSize = 13.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("${skill.accuracyPercentage}%", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(skill.statusLabel, color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)

                        Text("🔤 字母与发音存证", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(summary.letterSummary, color = Color.White, fontSize = 12.sp)

                        Text("✍️ 手写与口语跟读练习记录", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("四线三格手写练习存证: ${summary.handwritingEvidence} 次 | 朗读跟读练习存证: ${summary.speakingPracticeEvidence} 次", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        // Bottom Navigation Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onReplayBoardGame,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("summary_replay_board_button")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "再玩一次棋盘", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("再玩棋盘", fontSize = 13.sp)
            }

            Button(
                onClick = onReturnToMap,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("summary_return_map_button")
            ) {
                Text("返回英语世界", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
