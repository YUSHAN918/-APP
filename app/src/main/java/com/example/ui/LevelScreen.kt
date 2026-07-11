package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Level
import com.example.data.WordItem
import com.example.viewmodel.GameViewModel

private fun getEffectiveSortIndex(level: Level): Int {
    if (level.sortIndex > 0) return level.sortIndex
    val name = level.name
    return when {
        name.contains("第一单元") && name.contains("会写字") -> 101
        name.contains("第一单元") && name.contains("课后词语") -> 102
        name.contains("第一单元") && name.contains("易错字") -> 103
        name.contains("第一单元") && name.contains("易错词") -> 104
        name.contains("第二单元") && name.contains("会写字") -> 201
        name.contains("第二单元") && name.contains("课后词语") -> 202
        name.contains("第二单元") && name.contains("名著词语") -> 203
        name.contains("第二单元") && name.contains("易错字") -> 204
        name.contains("第二单元") && name.contains("BOSS") -> 205
        name.contains("第三单元") && name.contains("汉字知识识字") -> 301
        name.contains("第三单元") && name.contains("汉字文化词语") -> 302
        name.contains("第三单元") && name.contains("晏子使楚词语") -> 303
        name.contains("第三单元") && name.contains("易错") -> 304
        name.contains("第四单元") && name.contains("会写字") -> 401
        name.contains("第四单元") && name.contains("课后词语") -> 402
        name.contains("第四单元") && name.contains("军神与清贫词语") -> 403
        name.contains("第四单元") && name.contains("红色主题词语") -> 404
        name.contains("第四单元") && name.contains("易错") -> 405
        name.contains("第五单元") && name.contains("会写字") -> 501
        name.contains("第五单元") && name.contains("人物描写词语") -> 502
        name.contains("第五单元") && name.contains("刷子李词语") -> 503
        name.contains("第五单元") && name.contains("易错") -> 504
        else -> 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onLevelSelect: (Int) -> Unit
) {
    val levels by viewModel.allLevels.collectAsState()
    val allWords by viewModel.allWords.collectAsState()
    val wrongWords by viewModel.wrongWords.collectAsState()
    val stats by viewModel.userStats.collectAsState()
    val globalWordsPerLevel = stats?.wordsPerLevel ?: 8

    // Filter out unlocked levels for student screen
    val studentLevels = levels.filter { it.isUnlocked }.sortedWith { a, b ->
        val aSort = getEffectiveSortIndex(a)
        val bSort = getEffectiveSortIndex(b)
        
        val aIsPack = a.sourcePackId != null || aSort > 0 || a.name.contains("私有教材") || a.name.contains("五年级")
        val bIsPack = b.sourcePackId != null || bSort > 0 || b.name.contains("私有教材") || b.name.contains("五年级")
        
        if (aIsPack && bIsPack) {
            aSort.compareTo(bSort)
        } else if (aIsPack) {
            -1
        } else if (bIsPack) {
            1
        } else {
            b.id.compareTo(a.id)
        }
    }

    val completedCount = studentLevels.count { it.isCompleted }
    val totalCount = studentLevels.size
    val completionRate = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    
    val gradeName = studentLevels.firstOrNull { l ->
        listOf("一年级", "二年级", "三年级", "四年级", "五年级", "六年级").any { l.name.contains(it) || l.unitName.contains(it) }
    }?.let { l ->
        listOf("一年级", "二年级", "三年级", "四年级", "五年级", "六年级").first { l.name.contains(it) || l.unitName.contains(it) }
    } ?: "五年级"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("日常试炼营地", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("字词自测与契约挑战", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0F16)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F16))
                .padding(padding)
        ) {
            if (studentLevels.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏕️ 当前暂无开放的冒险委托", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("请让教官在“家长管理”中下达全新讨伐令", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    // Top Chapter Banner decoration
                    item {
                        QuestChapterBanner(
                            gradeName = gradeName,
                            completionRate = completionRate
                        )
                    }

                    items(studentLevels, key = { it.id }) { level ->
                        // Calculate words in this level
                        val levelWords = if (!level.wordIdsStr.isNullOrEmpty()) {
                            val ids = level.wordIdsStr.split(",").mapNotNull { it.trim().toIntOrNull() }
                            val wordMap = allWords.associateBy { it.id }
                            ids.mapNotNull { wordMap[it] }
                        } else {
                            allWords.filter { it.unitName == level.unitName }
                        }

                        // Check if there are active wrong words in this level
                        val levelHasWrongWords = wrongWords.any { ww ->
                            !ww.isMastered && levelWords.any { lw -> lw.text == ww.text }
                        }

                        QuestCard(
                            title = level.name,
                            unitName = level.unitName,
                            wordCount = levelWords.size,
                            isCompleted = level.isCompleted,
                            isSystemPreset = level.isPreset,
                            hasWrongWords = levelHasWrongWords,
                            buttonText = if (level.isCompleted) {
                                if (level.name.contains("BOSS") || level.name.contains("挑战")) "再次斩杀" else "再次讨伐"
                            } else {
                                if (level.name.contains("BOSS") || level.name.contains("挑战")) "首领讨伐" else "开启讨伐"
                            },
                            buttonType = if (level.name.contains("BOSS") || level.name.contains("挑战")) "BOSS" else "NORMAL",
                            onClick = {
                                if (level.isUnlocked) {
                                    onLevelSelect(level.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
