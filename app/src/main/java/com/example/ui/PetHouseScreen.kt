package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetHouseScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val activePlayer by viewModel.playerProfile.collectAsState()
    val activePetBinding by viewModel.activePet.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val equippedAccessory = inventoryItems.find { it.itemType == "PET_ACCESSORY" && it.isEquipped }
    val equippedAccessoryDef = equippedAccessory?.let { item -> com.example.data.ItemDefinition.ALL_ITEMS.find { it.itemId == item.itemId } }

    var showRenameDialog by remember { mutableStateOf(false) }
    var newPetName by remember { mutableStateOf("") }
    
    val currentPet = activePetBinding
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("字灵小屋", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentPet == null) {
                Text("当前未契约字灵", modifier = Modifier.padding(32.dp))
                return@Column
            }

            val petEmoji = when (currentPet.lifeStage) {
                "EGG" -> "🥚"
                "SOUL_SLEEP" -> "👻"
                else -> when (currentPet.petId) {
                    "小墨龙" -> "🐲"
                    "小书灵" -> "📚"
                    "小云狐" -> "🦊"
                    "小竹猫" -> "🐼"
                    else -> "✨"
                }
            }
            
            val petName = when (currentPet.lifeStage) {
                "EGG" -> "未知字灵蛋"
                "SOUL_SLEEP" -> "${currentPet.customName ?: currentPet.petName} (长眠中)"
                else -> currentPet.customName ?: currentPet.petName
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), CircleShape)
                            .border(4.dp, MaterialTheme.colorScheme.tertiary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(petEmoji, fontSize = 70.sp)
                        if (equippedAccessoryDef != null) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = CircleShape,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Text(
                                        equippedAccessoryDef.iconEmoji,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = petName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    if (equippedAccessoryDef != null) {
                        Text(
                            text = "已佩戴配饰: ${equippedAccessoryDef.iconEmoji} ${equippedAccessoryDef.itemName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Stats row
                    if (currentPet.lifeStage != "EGG" && currentPet.lifeStage != "SOUL_SLEEP") {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = "等级: Lv.${currentPet.level}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Favorite,
                                        contentDescription = "亲密度",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${currentPet.intimacy}/100",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Restaurant,
                                        contentDescription = "饱食度",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${currentPet.hunger}/100",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        LinearProgressIndicator(
                            progress = { currentPet.growthExp / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.tertiary,
                            trackColor = MaterialTheme.colorScheme.tertiaryContainer,
                        )
                        Text(
                            text = "成长值: ${currentPet.growthExp}/100",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contextual Buttons
                    when (currentPet.lifeStage) {
                        "EGG" -> {
                            Text(
                                text = "每天完成听写、背诵等任务可获得孵化能量。",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.hatchEgg() },
                                enabled = currentPet.hatchProgress >= 100,
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                if (currentPet.hatchProgress >= 100) {
                                    Text("破壳孵化 🎉", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("孵化能量: ${currentPet.hatchProgress}/100", fontSize = 16.sp)
                                }
                            }
                        }
                        "SOUL_SLEEP" -> {
                            Text(
                                text = "字灵已陷入沉睡...请在庭院中呼唤它的灵魂。",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = { viewModel.awakenPet(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Text("唤醒 (🪙100)")
                                }
                                Button(
                                    onClick = { viewModel.awakenPet(false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    enabled = currentPet.intimacy >= 100,
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Text("免消耗唤醒")
                                }
                            }
                        }
                        else -> {
                            // Active pet actions
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = { showRenameDialog = true },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("改名")
                                }
                                Button(
                                    onClick = { viewModel.feedPet("BASIC") },
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Icon(Icons.Default.Restaurant, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("喂食 (🪙10)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("为字灵改名") },
            text = {
                OutlinedTextField(
                    value = newPetName,
                    onValueChange = { newPetName = it },
                    label = { Text("新名字") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPetName.isNotBlank()) {
                        viewModel.renamePet(newPetName)
                        showRenameDialog = false
                    }
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
