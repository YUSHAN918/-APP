package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WrongWordItem
import com.example.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onStartReview: () -> Unit
) {
    val wrongWords by viewModel.wrongWords.collectAsState()
    val allWords by viewModel.allWords.collectAsState()
    val wordsToReview = wrongWords.filter { !it.isMastered }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("深渊魔物封印本", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("净化字词心魔，重铸神兵契约", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
        },
        floatingActionButton = {
            if (wordsToReview.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onStartReview,
                    containerColor = Color(0xFFC62828),
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White) },
                    text = { Text("开启深渊净化", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F16))
                .padding(padding)
        ) {
            if (wordsToReview.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✨", fontSize = 48.sp)
                        Text("心魔尽退，灵识清明！", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("当前的错题深渊已全部净化完毕", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
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
                    // Custom stats header
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF3E2723), Color(0xFF1A0C08))
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "👹 错题深渊第 5 层",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "宿主写错的生字在此化为实体魔物。需通过连续 3 次成功拼写以彻底封印净化！",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                    Surface(
                                        color = Color(0xFFC62828).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935)),
                                        modifier = Modifier.padding(start = 12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("待净化", color = Color(0xFFFF8A80), fontSize = 10.sp)
                                            Text("${wordsToReview.size}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    items(wordsToReview) { word ->
                        MonsterCard(word = word, allWords = allWords)
                    }
                }
            }
        }
    }
}

@Composable
fun MonsterCard(
    word: WrongWordItem,
    allWords: List<com.example.data.WordItem>,
    modifier: Modifier = Modifier
) {
    val isCritical = word.errorLevel == "重点"
    val cardBg = if (isCritical) Color(0xFF1E1112) else Color(0xFF14141E)
    val glowColor = if (isCritical) Color(0xFFE53935) else Color(0xFF78909C)
    val emoji = if (isCritical) "💀" else "👻"
    val errorLabel = if (isCritical) "核心心魔" else "普通幽魂"

    Surface(
        color = cardBg,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, glowColor.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title & Difficulty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(emoji, fontSize = 24.sp)
                    Column {
                        Text(
                            text = word.text,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = glowColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = errorLabel,
                                color = glowColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "肆虐强度: ${word.errorCount} 级",
                        color = Color(0xFFFF8A80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "累积错字 ${word.errorCount} 次",
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(10.dp))

            // Details & Streak Progress
            val hasActiveWord = allWords.any { it.text == word.text && it.unitName == word.unitName }
            val displaySource = if (!hasActiveWord && word.unitName.contains("五年级下册私有教材听写包")) {
                "历史教材包记录"
            } else if (word.unitName.contains(" · ")) {
                val prefix = word.unitName.substringBefore(" · ")
                if (prefix == "五年级下册私有教材听写包") "私有教材包 / 五年级下册" else prefix
            } else {
                word.unitName
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "出没密境: $displaySource",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    val date = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(word.lastErrorTime))
                    Text(
                        text = "最近肆虐: $date",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                // 净化进度条 (3次答对即可封印)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "净化进度: ${word.correctStreak}/3",
                        color = if (word.correctStreak >= 2) Color(0xFF81C784) else Color(0xFFFFD54F),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        for (i in 1..3) {
                            val active = i <= word.correctStreak
                            Box(
                                modifier = Modifier
                                    .size(width = 16.dp, height = 5.dp)
                                    .background(
                                        color = if (active) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

