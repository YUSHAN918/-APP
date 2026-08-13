package com.example.ui.english

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.EnglishProgressManager
import com.example.data.english.EnglishSemesterReviewEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnglishRecycleResultScreen(
    recycleId: String,
    onClaimReward: (Int) -> Unit,
    onReplayBoardGame: () -> Unit,
    onReturnToMap: () -> Unit,
    onNavigateToUnit: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val progress = remember { EnglishProgressManager.getRecycleProgress(context, recycleId) }
    var rewardClaimed by remember { mutableStateOf(progress.rewardClaimed) }

    val isSemester2 = recycleId == "english_pep_2013_g3_s2_recycle_2"
    if (recycleId == "english_pep_2013_g3_s1_recycle_2" || isSemester2) {
        val courseId = if (isSemester2) "english_pep_2013_g3_s2" else "english_pep_2013_g3_s1"
        val summary = remember { EnglishSemesterReviewEngine.generateSummary(context, courseId) }
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color(0xFF0F172A),
            topBar = {
                TopAppBar(
                    title = { Text("Recycle 2 & 学期综合总结成果", color = Color.White, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Reward Banner Top
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF831843)),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🎁 学期大庆典金币通关奖励", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("奖励：+100 冒险金币 (只可领取一次)", color = Color.LightGray, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (!rewardClaimed) {
                                    val success = EnglishProgressManager.claimRecycleReward(context, recycleId)
                                    if (success) {
                                        rewardClaimed = true
                                        onClaimReward(100)
                                    }
                                }
                            },
                            enabled = !rewardClaimed,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEAB308),
                                disabledContainerColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.testTag("claim_recycle_reward_button")
                        ) {
                            Text(if (rewardClaimed) "已领取" else "领取 100 金币", color = if (rewardClaimed) Color.Gray else Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                EnglishSemesterReviewSummaryView(
                    summary = summary,
                    onNavigateToUnit = onNavigateToUnit,
                    onReplayBoardGame = onReplayBoardGame,
                    onReturnToMap = onReturnToMap,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A),
        topBar = {
            TopAppBar(
                title = { Text("Recycle 1 阶段复习成果卡", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stage Trophy Header
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "奖杯",
                        tint = Color(0xFFEAB308),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "🏆 阶段复习大获全胜！",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = when (recycleId) {
                            "english_pep_2013_g4_s2_recycle_1" -> "PEP 2013 四年级下册 Unit 1-Unit 3 综合复习完成"
                            "english_pep_2013_g4_s1_recycle_1" -> "PEP 2013 四年级上册 Unit 1-Unit 3 综合复习完成"
                            "english_pep_2013_g3_s2_recycle_1" -> "PEP 2013 三年级下册 Unit 1-Unit 3 综合复习完成"
                            else -> "PEP 2013 三年级上册 Unit 1-Unit 3 综合复习完成"
                        },
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
            }

            // Theme Dimension Mastery
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("📚 主题维度掌握情况", color = Color(0xFFF472B6), fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    val unitThemes = when (recycleId) {
                        "english_pep_2013_g4_s2_recycle_1" -> listOf(
                            "Unit 1: My school (学校场馆与楼层指引)" to "96% 稳定掌握",
                            "Unit 2: What time is it? (日常作息与时间安排)" to "95% 稳定掌握",
                            "Unit 3: Weather (世界城市天气与穿着指南)" to "97% 稳定掌握"
                        )
                        "english_pep_2013_g4_s1_recycle_1" -> listOf(
                            "Unit 1: My classroom (教室设施与打扫打理)" to "96% 稳定掌握",
                            "Unit 2: My schoolbag (书包物品与科目书籍)" to "95% 稳定掌握",
                            "Unit 3: My friends (伙伴外貌与性格特征描述)" to "97% 稳定掌握"
                        )
                        "english_pep_2013_g3_s2_recycle_1" -> listOf(
                            "Unit 1: Welcome back to school! (国家与新伙伴介绍)" to "96% 稳定掌握",
                            "Unit 2: My family (家庭成员与称谓描述)" to "94% 稳定掌握",
                            "Unit 3: At the zoo (动物认知与外貌特征描述)" to "95% 稳定掌握"
                        )
                        else -> listOf(
                            "Unit 1: Hello! (打招呼、自我介绍与文具)" to "95% 稳定掌握",
                            "Unit 2: Colours (常见颜色与人名介绍)" to "92% 稳定掌握",
                            "Unit 3: Look at me! (身体部位与动作指令)" to "96% 稳定掌握"
                        )
                    }

                    unitThemes.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pair.first, color = Color.White, fontSize = 13.sp)
                            Text(pair.second, color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Skill Dimension Mastery
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🎯 能力维度掌握情况", color = Color(0xFFF472B6), fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    listOf(
                        "听音辨义" to "98%",
                        "情境理解" to "94%",
                        "词汇认读" to "90%",
                        "字母认知 A-N" to "96%",
                        "四线三格手写" to "92%"
                    ).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pair.first, color = Color.LightGray, fontSize = 13.sp)
                            Text(pair.second, color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Reward Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF831843)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🎁 阶段庆典丰厚奖励", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("奖励：+100 冒险金币 (只可领取一次)", color = Color.LightGray, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (!rewardClaimed) {
                                val success = EnglishProgressManager.claimRecycleReward(context, recycleId)
                                if (success) {
                                    rewardClaimed = true
                                    onClaimReward(100)
                                }
                            }
                        },
                        enabled = !rewardClaimed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEAB308),
                            disabledContainerColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.testTag("claim_recycle_reward_button")
                    ) {
                        Text(if (rewardClaimed) "已领取" else "领取 100 金币", color = if (rewardClaimed) Color.Gray else Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val replayText = if (recycleId == "english_pep_2013_g3_s2_recycle_1") "重温庆典寻踪" else "再玩一次棋盘"
                OutlinedButton(
                    onClick = onReplayBoardGame,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("replay_board_button")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = replayText, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(replayText, fontSize = 13.sp)
                }

                Button(
                    onClick = onReturnToMap,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("return_english_map_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                ) {
                    Text("返回英语世界", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
