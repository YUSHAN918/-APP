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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MonsterCodexEntry
import com.example.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterCodexScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNavigateToLevels: () -> Unit = {}
) {
    val playerProfile by viewModel.playerProfile.collectAsState()
    val playerId = playerProfile?.id ?: 0L
    
    // Fetch codex list from repository flow
    val rawCodexList by viewModel.repository.getCodexForPlayerFlow(playerId).collectAsState(initial = emptyList())
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedRarityFilter by remember { mutableStateOf("ALL") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    
    var selectedMonsterForDetail by remember { mutableStateOf<MonsterCodexEntry?>(null) }
    
    // Filtered entries
    val filteredCodexList = rawCodexList.filter { entry ->
        val matchesSearch = searchQuery.isBlank() || 
                entry.monsterName.contains(searchQuery, ignoreCase = true) || 
                entry.relatedWordFullForParent.contains(searchQuery, ignoreCase = true)
        
        val matchesRarity = selectedRarityFilter == "ALL" || entry.rarity == selectedRarityFilter
        val matchesType = selectedTypeFilter == "ALL" || entry.monsterType == selectedTypeFilter
        
        matchesSearch && matchesRarity && matchesType
    }.sortedWith(compareByDescending<MonsterCodexEntry> { it.isPurified }.thenByDescending { it.lastEncounterAt })
    
    val totalUnlocks = rawCodexList.size
    val totalPurified = rawCodexList.count { it.isPurified }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("👾 字词魔物净化图鉴", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF00E5FF))
                        Text("净化进度: $totalPurified / $totalUnlocks", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F111A),
                    titleContentColor = Color(0xFF00E5FF),
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
            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("搜索魔物名称或字词...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
            
            // Filter Tabs - Rarity
            Text("稀有度过滤:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val rarities = listOf(
                    "ALL" to "全部",
                    "LEGEND" to "传奇",
                    "EPIC" to "史诗",
                    "RARE" to "稀有",
                    "UNCOMMON" to "精良",
                    "COMMON" to "普通"
                )
                
                rarities.forEach { (key, label) ->
                    val isSelected = selectedRarityFilter == key
                    val activeColor = when (key) {
                        "LEGEND" -> Color(0xFFFFD700)
                        "EPIC" -> Color(0xFFD500F9)
                        "RARE" -> Color(0xFF00B0FF)
                        "UNCOMMON" -> Color(0xFF00E676)
                        "COMMON" -> Color(0xFF9E9E9E)
                        else -> Color(0xFF00E5FF)
                    }
                    
                    Surface(
                        onClick = { selectedRarityFilter = key },
                        color = if (isSelected) activeColor.copy(alpha = 0.25f) else Color(0xFF141724),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            if (isSelected) activeColor else Color.Gray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isSelected) activeColor else Color.LightGray,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            
            // Filter Tabs - Type
            Text("魔物类型过滤:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf(
                    "ALL" to "全部类型",
                    "BOSS" to "🐲 领主BOSS",
                    "ELITE" to "😈 易错精英",
                    "POEM_GUARD" to "📜 千秋守卫",
                    "WRONG_WORD" to "🦹 错字之灵"
                )
                
                types.forEach { (key, label) ->
                    val isSelected = selectedTypeFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTypeFilter = key },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF00E5FF),
                            containerColor = Color(0xFF141724),
                            labelColor = Color.LightGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.Gray.copy(alpha = 0.4f),
                            selectedBorderColor = Color(0xFF00E5FF)
                        )
                    )
                }
            }
            
            // List of Monsters
            if (filteredCodexList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👾", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("没有找到符合过滤条件的净化记录", color = Color.Gray, fontSize = 14.sp)
                        Text("快去「字词冒险」中讨伐并净化更多魔物吧！", color = Color.DarkGray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCodexList) { entry ->
                        val borderCol = when (entry.rarity) {
                            "LEGEND" -> Color(0xFFFFD700)
                            "EPIC" -> Color(0xFFD500F9)
                            "RARE" -> Color(0xFF00B0FF)
                            "UNCOMMON" -> Color(0xFF00E676)
                            else -> Color(0xFF9E9E9E)
                        }
                        
                        val monsterEmoji = when (entry.monsterType) {
                            "BOSS" -> "🐲"
                            "ELITE" -> "😈"
                            "POEM_GUARD" -> "📜"
                            "WRONG_WORD" -> "🦹"
                            else -> "👾"
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (entry.isPurified) Color(0xFF131726) else Color(0xFF16161C)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                if (entry.isPurified) borderCol.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMonsterForDetail = entry }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(borderCol.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .border(1.dp, borderCol.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(monsterEmoji, fontSize = 30.sp)
                                }
                                
                                Spacer(modifier = Modifier.width(14.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = entry.monsterName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (entry.isPurified) Color.White else Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when (entry.rarity) {
                                                "LEGEND" -> "传奇"
                                                "EPIC" -> "史诗"
                                                "RARE" -> "稀有"
                                                "UNCOMMON" -> "精良"
                                                else -> "普通"
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = borderCol,
                                            modifier = Modifier
                                                .background(borderCol.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "遭遇: ${entry.encounterCount}次",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "代表字词: ${if (entry.isPurified) entry.relatedWordFullForParent else entry.relatedWordMasked}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (entry.isPurified) Color(0xFF00E676) else Color.Gray
                                        )
                                    }
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    if (entry.isPurified) {
                                        Surface(
                                            color = Color(0xFF2E7D32).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                "✨ 已净化",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF4CAF50),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            color = Color(0xFFFF5722).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5722).copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                "💀 逃逸中",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFF7043),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
        
        // Detail Dialog
        selectedMonsterForDetail?.let { entry ->
            val borderCol = when (entry.rarity) {
                "LEGEND" -> Color(0xFFFFD700)
                "EPIC" -> Color(0xFFD500F9)
                "RARE" -> Color(0xFF00B0FF)
                "UNCOMMON" -> Color(0xFF00E676)
                else -> Color(0xFF9E9E9E)
            }
            val monsterEmoji = when (entry.monsterType) {
                "BOSS" -> "🐲"
                "ELITE" -> "😈"
                "POEM_GUARD" -> "📜"
                "WRONG_WORD" -> "🦹"
                else -> "👾"
            }
            
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            
            AlertDialog(
                onDismissRequest = { selectedMonsterForDetail = null },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedMonsterForDetail = null
                            onNavigateToLevels()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = borderCol)
                    ) {
                        Text("⚔️ 前往讨伐战斗", fontWeight = FontWeight.Bold, color = if (entry.rarity == "LEGEND") Color.Black else Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedMonsterForDetail = null }) {
                        Text("关闭", color = Color.Gray)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(monsterEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(entry.monsterName, fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                            Text(
                                text = when (entry.rarity) {
                                    "LEGEND" -> "传奇魔物"
                                    "EPIC" -> "史诗魔物"
                                    "RARE" -> "稀有魔物"
                                    "UNCOMMON" -> "精良魔物"
                                    else -> "普通魔物"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = borderCol
                            )
                        }
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("代表字词 (学名):", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                text = if (entry.isPurified) entry.relatedWordFullForParent else entry.relatedWordMasked,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (entry.isPurified) Color(0xFF00E676) else Color.LightGray,
                                fontSize = 14.sp
                            )
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("魔物形态 (来源):", color = Color.Gray, fontSize = 12.sp)
                            Text(entry.sourceType, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("遭遇次数:", color = Color.Gray, fontSize = 12.sp)
                            Text("${entry.encounterCount}次", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("成功净化次数:", color = Color.Gray, fontSize = 12.sp)
                            Text("${entry.purifiedCount}次", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 12.sp)
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("对战最高连击:", color = Color.Gray, fontSize = 12.sp)
                            Text("${entry.bestCombo} 连击", fontWeight = FontWeight.Bold, color = Color(0xFFFF5722), fontSize = 12.sp)
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("首次遭遇时间:", color = Color.Gray, fontSize = 12.sp)
                            Text(sdf.format(Date(entry.firstEncounterAt)), color = Color.LightGray, fontSize = 12.sp)
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("最近遭遇时间:", color = Color.Gray, fontSize = 12.sp)
                            Text(sdf.format(Date(entry.lastEncounterAt)), color = Color.LightGray, fontSize = 12.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        if (entry.isPurified) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF141724)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("💡 净化之光带来的字词启示：", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "已经成功净化该魔物！该代表字词字形端正、笔画规整。多多复习可使该魔物彻底驯服，成为您冒险路上的字灵之力！",
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1512)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5722).copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("⚠️ 受到深渊魔气侵蚀：", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF7043))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "该魔物尚未被完美净化（遭遇了但书写不正确或尚未开始听写）。标准答案目前被魔气隐蔽。请前往关卡战斗，将该词语完美书写，即可完成净化！",
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color(0xFF0F111A), RoundedCornerShape(16.dp))
                    .border(2.dp, borderCol, RoundedCornerShape(16.dp))
            )
        }
    }
}
