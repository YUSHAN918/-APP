package com.example.ui.english

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.EnglishContentLoader
import com.example.data.english.EnglishProgressManager
import com.example.data.english.EnglishRecycleMission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnglishRecycleHubScreen(
    courseId: String,
    recycleId: String,
    onNavigateToMission: (String) -> Unit,
    onNavigateToResult: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recycle = remember(recycleId) { EnglishContentLoader.loadRecycle(context, courseId, recycleId) }
    val progress = remember(recycleId) { EnglishProgressManager.getRecycleProgress(context, recycleId) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = recycle?.title ?: "Recycle 1 阶段复习",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("recycle_hub_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        }
    ) { innerPadding ->
        if (recycle == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFEC4899))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isRecycle2 = recycle.recycleId.contains("recycle_2")
            val isG4S2 = recycle.recycleId.contains("g4_s2")
            val isS2 = recycle.recycleId.contains("_s2")
            val isG4S1 = recycle.recycleId.contains("g4_s1")
            val bannerTitle = when {
                isG4S2 -> "🏫 校园与气象 · 四年级下册 Recycle 1"
                isG4S1 -> "🏫 校园与伙伴 · 四年级上册 Recycle 1"
                isRecycle2 -> "🎉 星语海港 · 全学期大庆典"
                isS2 -> "🌸 缤纷嘉年华 · 阶段复习 1"
                else -> "🌸 星语海港 · 阶段复习 1"
            }
            val bannerDesc = when {
                isG4S2 -> "涵盖 Unit 1 (My school) · Unit 2 (What time is it?) · Unit 3 (Weather)"
                isG4S1 -> "涵盖 Unit 1 (My classroom) · Unit 2 (My schoolbag) · Unit 3 (My friends)"
                isRecycle2 -> "涵盖 Unit 4 (Animals) · Unit 5 (Food) · Unit 6 (Numbers & Birthday)"
                isS2 -> "涵盖 Unit 1 (Welcome back to school!) · Unit 2 (My family) · Unit 3 (At the zoo)"
                else -> "涵盖 Unit 1 (Hello!) · Unit 2 (Colours) · Unit 3 (Look at me!)"
            }

            // Compact Hero Banner + Integrated Progress
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (isRecycle2) {
                                        listOf(Color(0xFF831843), Color(0xFFBE185D), Color(0xFF431407))
                                    } else {
                                        listOf(Color(0xFF065F46), Color(0xFF0D9488), Color(0xFF1E3A8A))
                                    }
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Top Meta Row: Subtitle + Progress Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFDE047),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = recycle.subtitle,
                                        color = Color(0xFFFDE047),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Black.copy(alpha = 0.35f)
                                ) {
                                    Text(
                                        text = "任务进度 ${progress.completedMissionIds.size}/${recycle.missions.size}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Banner Main Title
                            Text(
                                text = bannerTitle,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )

                            // Description
                            Text(
                                text = bannerDesc,
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                maxLines = 1
                            )

                            // Bottom Row: Covered Unit Badges + Optional Result Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    recycle.coveredUnitIds.forEach { uId ->
                                        val title = when (uId) {
                                            "english_pep_2013_g3_s1_u1" -> "U1 问候"
                                            "english_pep_2013_g3_s1_u2" -> "U2 颜色"
                                            "english_pep_2013_g3_s1_u3" -> "U3 身体"
                                            "english_pep_2013_g3_s1_u4" -> "U4 动物"
                                            "english_pep_2013_g3_s1_u5" -> "U5 食物"
                                            "english_pep_2013_g3_s1_u6" -> "U6 数字"
                                            "english_pep_2013_g3_s2_u1" -> "U1 校园"
                                            "english_pep_2013_g3_s2_u2" -> "U2 家庭"
                                            "english_pep_2013_g3_s2_u3" -> "U3 动物园"
                                            "english_pep_2013_g3_s2_u4" -> "U4 位置"
                                            "english_pep_2013_g3_s2_u5" -> "U5 水果"
                                            "english_pep_2013_g3_s2_u6" -> "U6 数量"
                                            "english_pep_2013_g4_s1_u1" -> "U1 教室"
                                            "english_pep_2013_g4_s1_u2" -> "U2 书包"
                                            "english_pep_2013_g4_s1_u3" -> "U3 朋友"
                                            "english_pep_2013_g4_s1_u4" -> "U4 家居"
                                            "english_pep_2013_g4_s1_u5" -> "U5 晚餐"
                                            "english_pep_2013_g4_s1_u6" -> "U6 家庭"
                                            else -> uId.takeLast(2).uppercase()
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.Black.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = title,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (progress.completedMissionIds.size == recycle.missions.size) {
                                    Button(
                                        onClick = onNavigateToResult,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("view_recycle_result_button")
                                    ) {
                                        Text("查看结算", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Missions Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp)
                ) {
                    Text(
                        text = "🗺️ 阶段复习任务大厅",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "共 ${recycle.missions.size} 个任务",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            // Missions List
            items(recycle.missions) { mission ->
                val isCompleted = progress.completedMissionIds.contains(mission.missionId)

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) Color(0xFF065F46).copy(alpha = 0.3f) else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isCompleted) Color(0xFF10B981) else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMission(mission.missionId) }
                        .testTag("recycle_mission_card_${mission.order}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isCompleted) Color(0xFF10B981) else Color(0xFFEC4899),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isCompleted) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "已完成", tint = Color.White)
                                } else {
                                    Text(
                                        text = "${mission.order}",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = mission.title,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF334155)
                                ) {
                                    Text(
                                        text = mission.textbookPage,
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = mission.description,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                maxLines = 2
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "进入任务",
                            tint = Color(0xFFEC4899),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
