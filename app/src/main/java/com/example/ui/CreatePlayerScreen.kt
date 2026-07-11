package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlayerScreen(
    viewModel: GameViewModel,
    onCompleted: () -> Unit
) {
    val currentSession by viewModel.currentSession.collectAsState()
    var step by remember { mutableStateOf(1) }
    var playerName by remember { mutableStateOf("") }
    var selectedAvatarId by remember { mutableStateOf(1) }
    var selectedPetId by remember { mutableStateOf("小墨龙") }

    val avatars = listOf(
        AvatarOption(1, "勇者", "🛡️", MaterialTheme.colorScheme.primary),
        AvatarOption(2, "书童", "📖", MaterialTheme.colorScheme.secondary),
        AvatarOption(3, "魔法师", "🧙‍♂️", MaterialTheme.colorScheme.tertiary),
        AvatarOption(4, "小侦探", "🔍", MaterialTheme.colorScheme.error)
    )

    val pets = listOf(
        PetOption("小墨龙", "🐲", "吐墨如虹，相伴书山。拥有极高文字领悟力！"),
        PetOption("小书灵", "📚", "饱读诗书，指点迷津。古文阅读时会给予额外指引。"),
        PetOption("小云狐", "🦊", "踏云追字，聪慧灵动。能带你避开陷阱。"),
        PetOption("小竹猫", "🐼", "墨竹为伴，憨厚温顺。最喜欢吃用拼音做的竹子。")
    )

    val randomNames = listOf(
        "小墨勇者", "小书侠", "小字灵", "笔尖小侠", "砚台小仙", "墨香小剑客", "诗书守护者", "错题猎人"
    )

    val keyboardController = LocalSoftwareKeyboardController.current

    val mainGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("字灵冒险团", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(mainGradient)
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "CreatePlayerWizard"
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (currentStep) {
                        1 -> {
                            // Step 1: Welcome
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Visual Hero Icon Group with circular breathing feel
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚔️",
                                    fontSize = 80.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Text(
                                text = "《字灵冒险团》",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "开始你的字词冒险之旅",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "在汉字与诗词的奇幻大陆上，你将与可爱的字灵伙伴一起踏上讨伐错题魔物的神奇旅程！",
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(48.dp))

                            Button(
                                onClick = { step = 2 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("创建我的勇者", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            }
                        }

                        2 -> {
                            // Step 2: Name Input
                            Text(
                                text = "第一步：勇者唤名",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "进入字灵世界，你需要一个响亮的名字",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            OutlinedTextField(
                                value = playerName,
                                onValueChange = { if (it.length <= 12) playerName = it },
                                label = { Text("输入勇者名 (12字以内)") },
                                placeholder = { Text("例如：小墨勇者") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                trailingIcon = {
                                    if (playerName.isNotEmpty()) {
                                        IconButton(onClick = { playerName = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "清除")
                                        }
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val randomIdx = Random.nextInt(randomNames.size)
                                    playerName = randomNames[randomIdx]
                                    keyboardController?.hide()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Casino, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("随机一个好名")
                            }

                            Spacer(modifier = Modifier.height(60.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { step = 1 },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("返回")
                                }
                                Button(
                                    onClick = { if (playerName.isNotBlank()) step = 3 },
                                    enabled = playerName.isNotBlank(),
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("下一步")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                                }
                            }
                        }

                        3 -> {
                            // Step 3: Avatar Selection
                            Text(
                                text = "第二步：选择勇者职业",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "选择代表你独特冒险形象的外观",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Grid of Avatar Choices
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                avatars.forEach { avatar ->
                                    val isSelected = selectedAvatarId == avatar.id
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedAvatarId = avatar.id },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                avatar.color.copy(alpha = 0.15f)
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) avatar.color else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .background(avatar.color.copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(avatar.emoji, fontSize = 28.sp)
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = avatar.name,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) avatar.color else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "专属冒险勇者职业：${avatar.name}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = "已选择",
                                                    tint = avatar.color,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { step = 2 },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("上一步")
                                }
                                Button(
                                    onClick = {
                                        val accountId = currentSession?.currentAccountId ?: 0L
                                        viewModel.createPlayer(
                                            accountId = accountId,
                                            name = playerName,
                                            avatarId = selectedAvatarId,
                                            petId = "unknown_egg"
                                        )
                                        onCompleted()
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("完成契约", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        4 -> {
                            // Step 4: Pet Selection
                            Text(
                                text = "第三步：契约字灵宠物",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "选择一个与你心灵相通的初始字灵宠物",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                pets.forEach { pet ->
                                    val isSelected = selectedPetId == pet.name
                                    val accentColor = when (pet.name) {
                                        "小墨龙" -> MaterialTheme.colorScheme.primary
                                        "小书灵" -> MaterialTheme.colorScheme.secondary
                                        "小云狐" -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedPetId = pet.name },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                accentColor.copy(alpha = 0.12f)
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .background(accentColor.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(pet.emoji, fontSize = 32.sp)
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = pet.name,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = pet.description,
                                                    fontSize = 12.sp,
                                                    lineHeight = 16.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = "已选择",
                                                    tint = accentColor,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { step = 3 },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("上一步")
                                }
                                Button(
                                    onClick = {
                                        val accountId = currentSession?.currentAccountId ?: 0L
                                        viewModel.createPlayer(
                                            accountId = accountId,
                                            name = playerName,
                                            avatarId = selectedAvatarId,
                                            petId = selectedPetId
                                        )
                                        onCompleted()
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("完成契约", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class AvatarOption(
    val id: Int,
    val name: String,
    val emoji: String,
    val color: Color
)

data class PetOption(
    val name: String,
    val emoji: String,
    val description: String
)
