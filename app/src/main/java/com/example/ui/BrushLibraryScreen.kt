package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrushLibraryScreen(
    viewModel: GameViewModel,
    focusBrushId: String? = null,
    navController: NavController
) {
    val playerProfile by viewModel.playerProfile.collectAsState()
    val coins = playerProfile?.coins ?: 0
    val currentEquipped = playerProfile?.equippedBrushId ?: "default_black"
    val unlockedBrushes = playerProfile?.unlockedBrushIds?.split(",") ?: listOf("default_black", "practice_wood")

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.checkAndUnlockBrushes()
    }

    // Automatic focus scrolling
    LaunchedEffect(focusBrushId) {
        if (!focusBrushId.isNullOrEmpty()) {
            val index = BrushStyle.ALL_BRUSHES.indexOfFirst { it.brushId == focusBrushId }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🖌️ 书写神兵武器库", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFFFD700))
                        Text("配备精妙画道圣笔，执神兵征讨顽固错字魔物！", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF140F0A),
                    titleContentColor = Color(0xFFFFD700),
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1C1611)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF1C1611))
        ) {
            // --- Equipped Status & Coins Panel ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF281E15)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("当前已装备: ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(4.dp))
                        val currentBrush = BrushStyle.getBrushById(currentEquipped)
                        Text(
                            text = currentBrush.brushName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$coins 金币",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFB74D),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- Weapon Cards List ---
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(BrushStyle.ALL_BRUSHES.size) { index ->
                    val brush = BrushStyle.ALL_BRUSHES[index]
                    val isUnlocked = unlockedBrushes.contains(brush.brushId)
                    val isEquipped = currentEquipped == brush.brushId
                    val isFocused = brush.brushId == focusBrushId

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                BrushNavigator.navigateToDetail(navController, brush.brushId)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEquipped) {
                                Color(0xFF382A1C)
                            } else if (isFocused) {
                                Color(0xFF2D2319)
                            } else {
                                Color(0xFF231A13)
                            }
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isEquipped) 2.dp else if (isFocused) 1.5.dp else 1.dp,
                            color = if (isEquipped) {
                                Color(0xFFFFD700)
                            } else if (isFocused) {
                                Color(0xFFFFB74D)
                            } else {
                                Color(0xFF3D2E20)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Weapon Emoji Icon Frame
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF140F0A), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFF3D2E20), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val brushEmoji = when (brush.brushId) {
                                    "default_black" -> "✒️"
                                    "practice_wood" -> "✏️"
                                    "ink_brush" -> "🖌️"
                                    "stardust_brush" -> "✨"
                                    "fluorescent_brush" -> "🖍️"
                                    "rainbow_brush" -> "🌈"
                                    "pet_dragon_brush" -> "🐉"
                                    else -> "🖋️"
                                }
                                Text(brushEmoji, fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Core Metadata
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = brush.brushName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isEquipped) Color(0xFFFFD700) else Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))

                                    val rarityColor = when (brush.rarity) {
                                        "NORMAL" -> Color.Gray
                                        "RARE" -> Color(0xFF1E88E5)
                                        "EPIC" -> Color(0xFF8E24AA)
                                        "LEGEND" -> Color(0xFFF57C00)
                                        else -> Color.Gray
                                    }
                                    val rarityName = when (brush.rarity) {
                                        "NORMAL" -> "普通"
                                        "RARE" -> "精良"
                                        "EPIC" -> "史诗"
                                        "LEGEND" -> "传奇"
                                        else -> "未知"
                                    }
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(rarityName, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = rarityColor.copy(alpha = 0.15f),
                                            labelColor = rarityColor
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, rarityColor),
                                        modifier = Modifier.height(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Lock condition description
                                Text(
                                    text = "解锁方式: ${brush.unlockCondition}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isUnlocked) Color(0xFF81C784) else Color(0xFFE57373),
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Mini stroke preview
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("笔迹特效: ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    BrushStrokePreview(
                                        brush = brush,
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(24.dp)
                                            .background(Color(0xFFFDF6E3), RoundedCornerShape(4.dp))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Interactive Options Column
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isUnlocked) {
                                    // Primary Action button: Equipped / Equip
                                    if (isEquipped) {
                                        Button(
                                            onClick = {},
                                            enabled = false,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4CAF50),
                                                disabledContainerColor = Color(0xFF354B38)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.height(30.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("已装备", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.equipBrush(brush.brushId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.height(30.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("装备", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Button(
                                            onClick = { BrushNavigator.navigateToTuning(navController, brush.brushId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                            contentPadding = PaddingValues(horizontal = 6.dp),
                                            modifier = Modifier.height(26.dp),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("调校", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { BrushNavigator.navigateToDetail(navController, brush.brushId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                                            contentPadding = PaddingValues(horizontal = 6.dp),
                                            modifier = Modifier.height(26.dp),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("试写", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    // Unowned block
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lock, contentDescription = "未解锁", tint = Color.Gray, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("未拥有", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    }

                                    // Direct link to Shop
                                    Button(
                                        onClick = {
                                            BrushNavigator.navigateToShop(navController)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.height(30.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("前往商店", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
