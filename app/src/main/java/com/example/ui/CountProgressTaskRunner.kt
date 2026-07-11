package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HolidayTask
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CountProgressTaskRunner(
    task: HolidayTask,
    viewModel: GameViewModel,
    onShowMessage: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("✅ 进度打卡确认", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("点击下一个格子进行打卡", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (index in 0 until task.totalCount) {
                    val isChecked = index < task.completedCount
                    val isNext = index == task.completedCount
                    
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isChecked) MaterialTheme.colorScheme.primary 
                                else if (isNext) MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (isChecked) MaterialTheme.colorScheme.primary else if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (isNext) {
                                    viewModel.checkInHolidayTask(task.id, 1, "完成了第 ${index + 1} ${task.unitLabel}")
                                    onShowMessage("第 ${index + 1} ${task.unitLabel} 打卡成功！")
                                } else if (isChecked && index == task.completedCount - 1) {
                                    // Let's use checkInHolidayTask to deduct 1 if clicking the latest one
                                    viewModel.checkInHolidayTask(task.id, -1, "撤销了第 ${index + 1} ${task.unitLabel}")
                                    onShowMessage("已撤销第 ${index + 1} ${task.unitLabel} 的打卡")
                                } else if (!isChecked) {
                                    onShowMessage("请按顺序完成，当前应打卡第 ${task.completedCount + 1} ${task.unitLabel}")
                                } else {
                                    onShowMessage("只能撤销最新的一次打卡")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecked) {
                            Icon(Icons.Default.Check, contentDescription = "已完成", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = "${index + 1}",
                                fontSize = 14.sp,
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNext) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
