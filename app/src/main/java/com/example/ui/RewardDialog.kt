package com.example.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

@Composable
fun RewardDialog(
    title: String = "冒险奖励",
    gold: Int = 0,
    exp: Int = 0,
    intimacy: Int = 0,
    hatchEnergy: Int = 0,
    isLevelUp: Boolean = false,
    oldLevel: Int = 1,
    newLevel: Int = 2,
    petMsg: String? = null,
    onDismiss: () -> Unit
) {
    var animationTriggered by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        animationTriggered = true
    }

    val scale by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialog_scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .scale(scale)
                .fillMaxWidth()
                .background(
                    color = Color(0xFF0F111A), // Deep Command Navy
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00E5FF), // Neon Cyan
                            Color(0xFF005F73)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
                .testTag("reward_dialog_container"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Large holographic floating chest indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0x1A00E5FF), RoundedCornerShape(50.dp))
                        .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(50.dp))
                ) {
                    Text(
                        text = "🎁", 
                        fontSize = 54.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "✦ $title ✦",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00E5FF), // Neon cyan header
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "修行有成，已成功向存储终端同步物资！",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                // Reward Details List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x1000E5FF), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x1F00E5FF), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (gold > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🪙", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "能量金币",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "+$gold",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD54F) // Brilliant Gold
                            )
                        }
                    }

                    if (exp > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("✨", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "全息经验",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "+$exp",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00E676) // Radiant Green
                            )
                        }
                    }

                    if (intimacy > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💖", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "御兽谐振值",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "+$intimacy",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF4081) // Cyber Pink
                            )
                        }
                    }
                    if (hatchEnergy > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("⚡", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "孵化热能",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "+$hatchEnergy",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB388FF) // Cyber Purple
                            )
                        }
                    }
                }

                // Level Up celebration block (tactical cyber style)
                if (isLevelUp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x1400E676), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "⚡ 权限提升 AUTHORITY ASCENDED ⚡",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00E676),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "LV.$oldLevel  ➔  LV.$newLevel",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                // Pet Intimacy Message
                petMsg?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x14FF4081), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFF4081), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🐾 $msg",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF80AB),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Confirm button (glowing cyberpunk cyan button)
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color(0xFF0F111A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("reward_ok_button")
                ) {
                    Text(
                        text = "接收同步物资",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}
