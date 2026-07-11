package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfile
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountCenterScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val currentAccount by viewModel.currentAccount.collectAsState()
    val players by viewModel.currentAccountPlayers.collectAsState()
    val activePlayer by viewModel.playerProfile.collectAsState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf("") }

    var showDeleteAccountConfirm by remember { mutableStateOf(false) }
    var pinDeleteInput by remember { mutableStateOf("") }
    var deleteAccountPinError by remember { mutableStateOf(false) }

    var playerToDelete by remember { mutableStateOf<PlayerProfile?>(null) }
    var pinPlayerDeleteInput by remember { mutableStateOf("") }
    var deletePlayerPinError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ 档案与角色管理", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.logout()
                        }
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "登出")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("退出登录", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Account Info Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📁 ", fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = currentAccount?.accountName ?: "未登录",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (currentAccount?.pinHash != null) "🔒 已加密保护" else "🔓 无密码保护",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = {
                                        renameInput = currentAccount?.accountName ?: ""
                                        showRenameDialog = true
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "重命名档案")
                                }

                                IconButton(
                                    onClick = {
                                        pinDeleteInput = ""
                                        deleteAccountPinError = false
                                        showDeleteAccountConfirm = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除账号",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Character Switching and Soft Delete Section
            item {
                Text(
                    text = "🧙 本档案下的所有角色列表：",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(players) { player ->
                val isActive = activePlayer?.id == player.id
                val avatarEmoji = when (player.avatarId) {
                    1 -> "🛡️"
                    2 -> "📖"
                    3 -> "🧙‍♂️"
                    4 -> "🔍"
                    else -> "🛡️"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isActive) {
                                viewModel.selectPlayer(player.accountId, player.id)
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isActive) 2.dp else 1.dp,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(avatarEmoji, fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = player.playerName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "等级: " + player.level + " | 金币: " + player.coins,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isActive) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "冒险中",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = {
                                    pinPlayerDeleteInput = ""
                                    deletePlayerPinError = false
                                    playerToDelete = player
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "退役角色",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("✏️ 修改档案名称") },
            text = {
                Column {
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("档案名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameInput.trim().isNotEmpty()) {
                            viewModel.renameAccount(currentAccount?.id ?: 0L, renameInput.trim())
                            showRenameDialog = false
                        }
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountConfirm) {
        val hasPin = currentAccount?.pinHash != null
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirm = false },
            title = { Text("⚠️ 警示：彻底删除档案") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "此操作将永久软删除整个冒险档案「${currentAccount?.accountName}」及其底下的所有角色、拼写历史、拼音练习和学习报告。此过程不可逆！",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (hasPin) {
                        Text("请输入当前档案的防沉迷锁 PIN 码进行安全核对：")
                        OutlinedTextField(
                            value = pinDeleteInput,
                            onValueChange = { pinDeleteInput = it },
                            label = { Text("PIN 码") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (deleteAccountPinError) {
                            Text(
                                text = "密码不正确，拒绝删除",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text("请确认你明白所有的游戏记录与冒险历史将会从此档案中退役：")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (hasPin) {
                            if (pinDeleteInput == currentAccount?.pinHash) {
                                viewModel.deleteAccount(currentAccount?.id ?: 0L)
                                showDeleteAccountConfirm = false
                            } else {
                                deleteAccountPinError = true
                            }
                        } else {
                            viewModel.deleteAccount(currentAccount?.id ?: 0L)
                            showDeleteAccountConfirm = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("确认退役删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirm = false }) {
                    Text("考虑一下")
                }
            }
        )
    }

    // Delete Player Dialog
    if (playerToDelete != null) {
        val player = playerToDelete!!
        val hasPin = currentAccount?.pinHash != null
        AlertDialog(
            onDismissRequest = { playerToDelete = null },
            title = { Text("⚠️ 警示：角色退役") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "你即将让出战角色「${player.playerName}」永久退役，该角色在冒险团里的等级（Lv.${player.level}）和金币将会一并注销。此操作不可恢复！",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )

                    if (hasPin) {
                        Text("请输入防沉迷锁密码 PIN 进行安全核实：")
                        OutlinedTextField(
                            value = pinPlayerDeleteInput,
                            onValueChange = { pinPlayerDeleteInput = it },
                            label = { Text("PIN 码") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (deletePlayerPinError) {
                            Text(
                                text = "密码不正确，操作取消",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (hasPin) {
                            if (pinPlayerDeleteInput == currentAccount?.pinHash) {
                                viewModel.deletePlayer(player.id)
                                playerToDelete = null
                            } else {
                                deletePlayerPinError = true
                            }
                        } else {
                            viewModel.deletePlayer(player.id)
                            playerToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("确认角色退役")
                }
            },
            dismissButton = {
                TextButton(onClick = { playerToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}
