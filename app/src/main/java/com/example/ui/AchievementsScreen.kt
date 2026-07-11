package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AchievementRecord
import com.example.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val playerProfile by viewModel.playerProfile.collectAsState()
    val playerId = playerProfile?.id ?: 0L
    
    // Fetch achievements list from repository flow
    val rawAchievementsList by viewModel.repository.getAchievementsForPlayerFlow(playerId).collectAsState(initial = emptyList())
    
    val totalCount = rawAchievementsList.size
    val unlockedCount = rawAchievementsList.count { it.isUnlocked }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("🏆 冒险荣誉圣殿", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFFF9800))
                        Text("荣誉勋章: $unlockedCount / $totalCount", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF140F0A),
                    titleContentColor = Color(0xFFFF9800),
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0C0D14))
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Overall progress bar
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1810)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏆 勋章总进度",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = Color(0xFFFF9800),
                            trackColor = Color(0xFF382918)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF9800),
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "每一次完美的书写，每一只被净化的魔物，都将化为至高荣誉刻在殿堂的圣碑之上！",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        lineHeight = 16.sp
                    )
                }
            }
            
            // Achievements List
            if (rawAchievementsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏆", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("圣殿中暂无荣誉勋章记录", color = Color.Gray, fontSize = 14.sp)
                        Text("完成第一次「字词冒险」结算即可觉醒图鉴与成就系统！", color = Color.DarkGray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(rawAchievementsList.sortedByDescending { it.isUnlocked }) { record ->
                        val medalEmoji = when {
                            !record.isUnlocked -> "🔒"
                            record.achievementKey.contains("perfect") -> "🥇"
                            record.achievementKey.contains("streak") -> "🔥"
                            record.achievementKey.contains("pet") -> "🐾"
                            record.achievementKey.contains("boss") -> "🐉"
                            else -> "🏅"
                        }
                        
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (record.isUnlocked) Color(0xFF1C1610) else Color(0xFF16161C)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp, 
                                if (record.isUnlocked) Color(0xFFFF9800).copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Medal slot
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(
                                            if (record.isUnlocked) Color(0xFFFF9800).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.05f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            1.dp, 
                                            if (record.isUnlocked) Color(0xFFFF9800).copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.15f), 
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(medalEmoji, fontSize = 30.sp)
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.achievementName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (record.isUnlocked) Color.White else Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = record.achievementDesc,
                                        fontSize = 11.sp,
                                        color = if (record.isUnlocked) Color.LightGray else Color.Gray,
                                        lineHeight = 15.sp
                                    )
                                    
                                    val unlockedAtVal = record.unlockedAt ?: 0L
                                    if (record.isUnlocked && unlockedAtVal > 0L) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "达成时间: ${sdf.format(Date(unlockedAtVal))}",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    if (record.isUnlocked) {
                                        Text(
                                            text = "已达标",
                                            color = Color(0xFFFF9800),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "🪙 +${record.rewardCoins}",
                                            color = Color(0xFFFFC107),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        )
                                    } else {
                                        Text(
                                            text = "未解锁",
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "🪙 +${record.rewardCoins}",
                                            color = Color.DarkGray,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
