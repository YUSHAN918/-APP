package com.example.ui.english

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.AutoDictationOrder
import com.example.data.english.AutoDictationWordScope
import com.example.data.english.EnglishAutoDictationSettings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EnglishAutoDictationSettingsSheet(
    initialSettings: EnglishAutoDictationSettings,
    hasExtendedWords: Boolean,
    onDismissRequest: () -> Unit,
    onStartDictation: (EnglishAutoDictationSettings) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    var wordScope by remember { mutableStateOf(initialSettings.wordScope) }
    var repeatCount by remember { mutableIntStateOf(initialSettings.repeatCount) }
    var intervalSeconds by remember { mutableIntStateOf(initialSettings.intervalSeconds) }
    var order by remember { mutableStateOf(initialSettings.order) }
    var preStartCountdownSeconds by remember { mutableIntStateOf(initialSettings.preStartCountdownSeconds) }

    // If unit has no extended words and extended scope was selected, fallback to ALL
    if (!hasExtendedWords && wordScope == AutoDictationWordScope.EXTENDED_ONLY) {
        wordScope = AutoDictationWordScope.ALL
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎧 一键报听写设置",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    Text(
                        text = "自动连续播报单元单词，进行纸笔自测",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
                IconButton(onClick = onDismissRequest, modifier = Modifier.testTag("close_settings_button")) {
                    Icon(Icons.Default.Close, contentDescription = "关闭设置", tint = Color.White)
                }
            }

            // 1. 词汇范围
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. 听写词汇范围", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = wordScope == AutoDictationWordScope.ALL,
                            onClick = { wordScope = AutoDictationWordScope.ALL },
                            label = { Text("全部单词 (核心+拓展)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("scope_all_chip")
                        )
                        FilterChip(
                            selected = wordScope == AutoDictationWordScope.CORE_ONLY,
                            onClick = { wordScope = AutoDictationWordScope.CORE_ONLY },
                            label = { Text("核心词 (A类)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("scope_core_chip")
                        )
                        FilterChip(
                            selected = wordScope == AutoDictationWordScope.EXTENDED_ONLY,
                            onClick = { if (hasExtendedWords) wordScope = AutoDictationWordScope.EXTENDED_ONLY },
                            enabled = hasExtendedWords,
                            label = {
                                Text(if (hasExtendedWords) "拓展词 (B类)" else "拓展词 (本单元无)")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF472B6),
                                selectedLabelColor = Color.White,
                                disabledContainerColor = Color(0xFF334155),
                                disabledLabelColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("scope_extended_chip")
                        )
                    }
                }
            }

            // 2. 每个单词播报次数
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. 每个单词播报次数", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 3, 4, 5).forEach { count ->
                            val isSelected = repeatCount == count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF10B981) else Color(0xFF334155))
                                    .clickable { repeatCount = count }
                                    .testTag("repeat_count_${count}_chip"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${count}次",
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // 3. 单词间隔时间
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. 单词间隔时间 (听写等待)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 5, 8, 10, 15).forEach { sec ->
                            val isSelected = intervalSeconds == sec
                            FilterChip(
                                selected = isSelected,
                                onClick = { intervalSeconds = sec },
                                label = { Text("${sec}秒") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6366F1),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("interval_${sec}s_chip")
                            )
                        }
                    }

                    // Stepper for custom seconds (2-30s)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("微调等待间隔 (${intervalSeconds} 秒):", fontSize = 13.sp, color = Color.LightGray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (intervalSeconds > 2) intervalSeconds-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF334155), CircleShape)
                                    .testTag("interval_minus_button")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "减少1秒", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${intervalSeconds}s",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = { if (intervalSeconds < 30) intervalSeconds++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF334155), CircleShape)
                                    .testTag("interval_plus_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "增加1秒", tint = Color.White)
                            }
                        }
                    }
                }
            }

            // 4. 播放顺序
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("4. 播放顺序", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = order == AutoDictationOrder.SHUFFLED,
                            onClick = { order = AutoDictationOrder.SHUFFLED },
                            label = { Text("🔀 随机顺序 (推荐)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF8B5CF6),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("order_shuffled_chip")
                        )
                        FilterChip(
                            selected = order == AutoDictationOrder.ORIGINAL,
                            onClick = { order = AutoDictationOrder.ORIGINAL },
                            label = { Text("➡️ 顺序播放") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF8B5CF6),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("order_original_chip")
                        )
                    }
                }
            }

            // 5. 开始前准备倒计时
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("5. 开始前准备时间", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "关闭", 3 to "3秒", 5 to "5秒").forEach { (sec, label) ->
                            val isSelected = preStartCountdownSeconds == sec
                            FilterChip(
                                selected = isSelected,
                                onClick = { preStartCountdownSeconds = sec },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFEC4899),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("pre_countdown_${sec}s_chip")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Start Action Button
            Button(
                onClick = {
                    val settings = EnglishAutoDictationSettings(
                        wordScope = wordScope,
                        repeatCount = repeatCount,
                        intervalSeconds = intervalSeconds,
                        order = order,
                        preStartCountdownSeconds = preStartCountdownSeconds
                    ).clamped()
                    onStartDictation(settings)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_dictation_now_button")
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Text(
                        text = "保存偏好并开始听写",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
