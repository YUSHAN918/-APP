package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.data.ItemDefinition
import com.example.data.PlayerInventoryItem
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackpackScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val inventoryItems by viewModel.inventoryItems.collectAsState()

    var selectedTab by remember { mutableStateOf("EQUIPMENT") } // "EQUIPMENT" or "MATERIAL"
    var subCategoryFilter by remember { mutableStateOf("ALL") } // "ALL", "BRUSH", "AVATAR_FRAME", "TITLE", "PET_ACCESSORY", "CAMP_THEME"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🎒 冒险背包仓库", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF81C784))
                        Text("查看并装备你的冒险成果和战利品碎片！", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F1410),
                    titleContentColor = Color(0xFF81C784),
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF111612)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF111612))
        ) {
            // Main tabs (Equipment vs Material)
            TabRow(
                selectedTabIndex = if (selectedTab == "EQUIPMENT") 0 else 1,
                containerColor = Color(0xFF0F1410),
                contentColor = Color(0xFF81C784),
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == "EQUIPMENT",
                    onClick = { selectedTab = "EQUIPMENT" },
                    text = { Text("🛡️ 冒险装备库", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == "MATERIAL",
                    onClick = { selectedTab = "MATERIAL" },
                    text = { Text("🧪 战利品材料仓", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == "EQUIPMENT") {
                // Subcategories scrollable filters
                ScrollableTabRow(
                    selectedTabIndex = when (subCategoryFilter) {
                        "ALL" -> 0
                        "BRUSH" -> 1
                        "AVATAR_FRAME" -> 2
                        "TITLE" -> 3
                        "PET_ACCESSORY" -> 4
                        "CAMP_THEME" -> 5
                        else -> 0
                    },
                    containerColor = Color(0xFF151D16),
                    contentColor = Color(0xFF81C784),
                    edgePadding = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val subFilters = listOf(
                        "ALL" to "全部",
                        "BRUSH" to "🖌️ 神笔",
                        "AVATAR_FRAME" to "🖼️ 头像框",
                        "TITLE" to "👑 称号",
                        "PET_ACCESSORY" to "🎀 配饰",
                        "CAMP_THEME" to "🏕️ 营地"
                    )
                    subFilters.forEach { (filter, label) ->
                        Tab(
                            selected = subCategoryFilter == filter,
                            onClick = { subCategoryFilter = filter },
                            text = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                // Owned equipment list
                val ownedEquips = inventoryItems.filter { it.isOwned && it.itemType != "MATERIAL" }
                val filteredEquips = if (subCategoryFilter == "ALL") {
                    ownedEquips
                } else {
                    ownedEquips.filter { it.itemType == subCategoryFilter }
                }

                if (filteredEquips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("空空如也，快去冒险商店兑换吧！", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredEquips) { invItem ->
                            val itemDef = ItemDefinition.ALL_ITEMS.find { it.itemId == invItem.itemId }
                            if (itemDef != null) {
                                EquipmentInventoryCard(
                                    item = itemDef,
                                    isEquipped = invItem.isEquipped,
                                    onEquipToggle = {
                                        if (invItem.isEquipped) {
                                            viewModel.unequipItem(itemDef.itemId) { res ->
                                                res.onSuccess { msg ->
                                                    Toast.makeText(context, "✅ $msg", Toast.LENGTH_SHORT).show()
                                                }.onFailure { err ->
                                                    Toast.makeText(context, "❌ ${err.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            viewModel.equipItem(itemDef.itemId) { res ->
                                                res.onSuccess { msg ->
                                                    Toast.makeText(context, "⚡ $msg", Toast.LENGTH_SHORT).show()
                                                }.onFailure { err ->
                                                    Toast.makeText(context, "❌ ${err.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Materials tab
                val materialItems = inventoryItems.filter { it.itemType == "MATERIAL" }

                val allMaterials = listOf(
                    Triple("WORD_SHARD", "🎫 字灵勋章碎片", "通过战斗或净化字魔掉落，用于兑换高级笔刷、头像框。"),
                    Triple("BRUSH_SHARD", "🥢 画道神笔碎片", "在冒险地图中概率掉落，用来兑换绝版古风神笔。"),
                    Triple("COMBO_SHARD", "⚡ 连击徽记碎片", "单场战斗达成5连击及以上掉落，用来换取尊尊称号。"),
                    Triple("JINGHUA", "💎 太清净化结晶", "净化错题魔物掉落，代表最纯净的净化之力，用于精美头像框。"),
                    Triple("BOSS_SHARD", "🐉 深渊古龙逆鳞", "成功讨伐深渊魔王掉落，极为珍贵。")
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(allMaterials) { (matId, matName, matDesc) ->
                        val ownedAmt = materialItems.find { it.itemId == matId }?.amount ?: 0
                        MaterialInventoryCard(
                            itemId = matId,
                            name = matName,
                            desc = matDesc,
                            amount = ownedAmt
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EquipmentInventoryCard(
    item: ItemDefinition,
    isEquipped: Boolean,
    onEquipToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B221C)),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .border(
                1.dp,
                if (isEquipped) Color(0xFF4CAF50) else Color(0xFF2E3D31),
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
            // Equipped Badge
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (item.itemType) {
                        "BRUSH" -> "笔刷"
                        "AVATAR_FRAME" -> "头像框"
                        "TITLE" -> "称号"
                        "PET_ACCESSORY" -> "契约配饰"
                        "CAMP_THEME" -> "营地主题"
                        else -> "道具"
                    },
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                if (isEquipped) {
                    Text(
                        text = "已装备",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(Color(0xFFE8F5E9).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Emoji Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFF121813), RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.iconEmoji, fontSize = 28.sp)
            }

            // Name
            Text(
                text = item.itemName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Desc
            Text(
                text = item.description,
                color = Color.Gray,
                fontSize = 9.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Action button
            Button(
                onClick = onEquipToggle,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEquipped) Color(0xFF3E2723) else Color(0xFF2E7D32),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                Text(
                    text = if (isEquipped) "卸下" else "装备",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MaterialInventoryCard(
    itemId: String,
    name: String,
    desc: String,
    amount: Int
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B221C)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2E3D31), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large icon representation
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(Color(0xFF121813), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val matEmoji = when (itemId) {
                        "WORD_SHARD" -> "🎫"
                        "BRUSH_SHARD" -> "🥢"
                        "COMBO_SHARD" -> "⚡"
                        "JINGHUA" -> "💎"
                        "BOSS_SHARD" -> "🐉"
                        else -> "⚙️"
                    }
                    Text(matEmoji, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(desc, color = Color.Gray, fontSize = 10.sp, lineHeight = 12.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Owned amount badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(Color(0xFF121813), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp)
                    .widthIn(min = 40.dp)
            ) {
                Text("持有", color = Color.Gray, fontSize = 9.sp)
                Text(
                    "$amount",
                    color = if (amount > 0) Color(0xFF81C784) else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
