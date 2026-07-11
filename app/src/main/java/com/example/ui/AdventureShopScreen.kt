package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.widget.Toast
import androidx.navigation.NavController
import com.example.data.ItemDefinition
import com.example.data.PlayerInventoryItem
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureShopScreen(
    viewModel: GameViewModel,
    navController: NavController,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val playerProfile by viewModel.playerProfile.collectAsState()
    val inventoryItems by viewModel.inventoryItems.collectAsState()

    val currentCoins = playerProfile?.coins ?: 0

    // Tab configuration
    var selectedTab by remember { mutableStateOf("BRUSH") }
    val tabs = listOf(
        "BRUSH" to "🖌️ 神兵画道",
        "AVATAR_FRAME" to "🖼️ 尊贵装扮",
        "TITLE" to "👑 尊荣称号",
        "PET_ACCESSORY" to "🎀 契约兽配饰",
        "CAMP_THEME" to "🏕️ 营地装扮"
    )

    // Material totals
    val wordShards = inventoryItems.find { it.itemId == "WORD_SHARD" }?.amount ?: 0
    val brushShards = inventoryItems.find { it.itemId == "BRUSH_SHARD" }?.amount ?: 0
    val comboShards = inventoryItems.find { it.itemId == "COMBO_SHARD" }?.amount ?: 0
    val jinghuaShards = inventoryItems.find { it.itemId == "JINGHUA" }?.amount ?: 0
    val bossShards = inventoryItems.find { it.itemId == "BOSS_SHARD" }?.amount ?: 0

    // For keeping track of recently purchased items in this screen session
    var sessionPurchasedItemIds by remember { mutableStateOf(setOf<String>()) }
    var animatingItem by remember { mutableStateOf<ItemDefinition?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🛒 冒险藏宝商店", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFFFD700))
                        Text("用金币与冒险勋章兑换绝版装备！", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            // --- Player Resources Status Bar ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF281E15)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("当前财富:", color = Color.Gray, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 18.sp, modifier = Modifier.padding(end = 4.dp))
                            Text(
                                text = "$currentCoins 金币",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF3D2E20), thickness = 1.dp)

                    // Shards Grid
                    Text("冒险材料徽记:", color = Color.Gray, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MaterialBadge(emoji = "🎫", count = wordShards, label = "字灵", color = Color(0xFF4CAF50))
                        MaterialBadge(emoji = "🥢", count = brushShards, label = "神笔", color = Color(0xFF2196F3))
                        MaterialBadge(emoji = "⚡", count = comboShards, label = "连击", color = Color(0xFFFF9800))
                        MaterialBadge(emoji = "💎", count = jinghuaShards, label = "太清", color = Color(0xFF9C27B0))
                        MaterialBadge(emoji = "🐉", count = bossShards, label = "逆鳞", color = Color(0xFFE91E63))
                    }
                }
            }

            // --- Category Tabs ---
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0),
                containerColor = Color(0xFF140F0A),
                contentColor = Color(0xFFFFD700),
                edgePadding = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEach { (type, title) ->
                    Tab(
                        selected = selectedTab == type,
                        onClick = { selectedTab = type },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == type) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == type) Color(0xFFFFD700) else Color.LightGray
                            )
                        }
                    )
                }
            }

            // --- Items Grid ---
            val filteredItems = ItemDefinition.ALL_ITEMS.filter { it.itemType == selectedTab }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("该栏目下暂无货架商品...", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredItems) { item ->
                        val originallyOwned = inventoryItems.find { it.itemId == item.itemId }?.isOwned ?: false
                        val isOwned = originallyOwned || sessionPurchasedItemIds.contains(item.itemId)
                        val isRecentlyPurchased = sessionPurchasedItemIds.contains(item.itemId)

                        ShopItemCard(
                            item = item,
                            isOwned = isOwned,
                            isRecentlyPurchased = isRecentlyPurchased,
                            navController = navController,
                            onPurchase = {
                                viewModel.purchaseItem(item.itemId) { res ->
                                    res.onSuccess { msg ->
                                        // If it is a brush, trigger the direct brush unlocking in the database/profile
                                        if (item.itemType == "BRUSH") {
                                            val brushId = mapItemIdToBrushId(item.itemId)
                                            viewModel.unlockBrushDirect(brushId)
                                        }
                                        // Trigger animation state
                                        animatingItem = item
                                        sessionPurchasedItemIds = sessionPurchasedItemIds + item.itemId
                                    }.onFailure { err ->
                                        Toast.makeText(context, "❌ ${err.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // --- Obtained Animation Overlay ---
    animatingItem?.let { item ->
        WeaponObtainedAnimation(
            item = item,
            onDismiss = { animatingItem = null }
        )
    }
}

@Composable
fun MaterialBadge(emoji: String, count: Int, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFF1F1811), RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .width(52.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(2.dp))
            Text("$count", color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, color = Color.Gray, fontSize = 9.sp)
    }
}

@Composable
fun ShopItemCard(
    item: ItemDefinition,
    isOwned: Boolean,
    isRecentlyPurchased: Boolean,
    navController: NavController,
    onPurchase: () -> Unit
) {
    val brushId = mapItemIdToBrushId(item.itemId)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF281E15)),
        modifier = Modifier
            .fillMaxWidth()
            .height(245.dp)
            .border(
                1.dp,
                when (item.rarity) {
                    "COMMON" -> Color.LightGray.copy(alpha = 0.3f)
                    "RARE" -> Color(0xFF2196F3).copy(alpha = 0.5f)
                    "EPIC" -> Color(0xFF9C27B0).copy(alpha = 0.6f)
                    "LEGENDARY" -> Color(0xFFFFD700).copy(alpha = 0.8f)
                    else -> Color.Gray
                },
                RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Rarity Label
            Text(
                text = when (item.rarity) {
                    "COMMON" -> "普通"
                    "RARE" -> "精良"
                    "EPIC" -> "史诗"
                    "LEGENDARY" -> "传说"
                    else -> "未知"
                },
                color = when (item.rarity) {
                    "COMMON" -> Color.LightGray
                    "RARE" -> Color(0xFF2196F3)
                    "EPIC" -> Color(0xFF9C27B0)
                    "LEGENDARY" -> Color(0xFFFFD700)
                    else -> Color.Gray
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.End)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )

            // Icon/Emoji Display
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFF1F1811), RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.iconEmoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Item Name
            Text(
                text = item.itemName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            // Description
            Text(
                text = item.description,
                color = Color.LightGray,
                fontSize = 9.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Purchase Price display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                if (item.priceCoins > 0) {
                    Text("🪙", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "${item.priceCoins}",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                if (item.priceCoins > 0 && item.priceShardType != null) {
                    Text("+", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 2.dp))
                }

                if (item.priceShardType != null) {
                    val shardEmoji = when (item.priceShardType) {
                        "WORD_SHARD" -> "🎫"
                        "BRUSH_SHARD" -> "🥢"
                        "COMBO_SHARD" -> "⚡"
                        "JINGHUA" -> "💎"
                        else -> "⚙️"
                    }
                    Text(shardEmoji, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "${item.priceShardAmount}",
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            // Purchase Button / Navigation Actions
            if (item.itemType == "BRUSH") {
                if (isRecentlyPurchased) {
                    Button(
                        onClick = {
                            BrushNavigator.navigateToLibrary(navController, brushId)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    ) {
                        Text("前往武器库", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else if (isOwned) {
                    Button(
                        onClick = {
                            BrushNavigator.navigateToLibrary(navController, brushId)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF382A1C)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    ) {
                        Text("已拥有 (前往库)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                    }
                } else {
                    Button(
                        onClick = onPurchase,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    ) {
                        Text("兑换", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                Button(
                    onClick = onPurchase,
                    enabled = !isOwned,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOwned) Color(0xFF1F1811) else Color(0xFFE65100),
                        disabledContainerColor = Color(0xFF1F1811),
                        contentColor = Color.White,
                        disabledContentColor = Color.Gray
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {
                    Text(
                        text = if (isOwned) "已拥有" else "兑换",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 获得的武器动画全屏弹窗 Overlay
 */
@Composable
fun WeaponObtainedAnimation(
    item: ItemDefinition,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Background Glowing Sunburst/Rings
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer(rotationZ = rotateAngle)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFFD700).copy(alpha = 0.4f),
                                    Color(0xFFFFB74D).copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Item Emoji Scaling inside the glow
                    Text(
                        text = item.iconEmoji,
                        fontSize = 54.sp,
                        modifier = Modifier.graphicsLayer(
                            scaleX = scale,
                            scaleY = scale
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "🎉 恭喜获得神兵武器！",
                    color = Color(0xFFFFD700),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.itemName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = item.description,
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(160.dp)
                        .height(44.dp)
                ) {
                    Text(
                        text = "收下神兵",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

/**
 * 将商品 Item 物理映射至底层的笔刷配置 ID
 */
private fun mapItemIdToBrushId(itemId: String): String {
    return when (itemId) {
        "star_brush" -> "stardust_brush"
        "glow_brush" -> "fluorescent_brush"
        "pet_brush_molong" -> "pet_dragon_brush"
        else -> itemId
    }
}
