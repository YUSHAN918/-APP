package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cabin
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel

@Composable
fun CompanionHubScreen(
    viewModel: GameViewModel,
    onNavigateToPetHouse: () -> Unit,
    onNavigateToShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePet by viewModel.activePet.collectAsState()
    val scrollState = rememberScrollState()

    val petEmoji = when (activePet?.lifeStage) {
        "EGG" -> "🥚"
        "SOUL_SLEEP" -> "👻"
        else -> when (activePet?.petId) {
            "小墨龙" -> "🐲"
            "小书灵" -> "📚"
            "小云狐" -> "🦊"
            "小竹猫" -> "🐼"
            else -> "✨"
        }
    }

    val petName = when (activePet?.lifeStage) {
        "EGG" -> "未知字灵蛋"
        "SOUL_SLEEP" -> "${activePet?.customName ?: activePet?.petName} (长眠中)"
        else -> activePet?.customName ?: activePet?.petName ?: "暂无契约伙伴"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameUiTokens.Colors.Background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Text(
            text = "🐾 守护字灵：伙伴圣殿",
            color = GameUiTokens.Colors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // 1. Companion Status Panel
        GamePanel(
            title = "契约相伴之守护字灵",
            borderColor = GameUiTokens.Colors.Border,
            glowColor = GameUiTokens.Colors.NeonAmber
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Avatar Circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(GameUiTokens.Colors.SurfaceVariant)
                        .border(1.5.dp, GameUiTokens.Colors.NeonAmber.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(petEmoji, fontSize = 38.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = petName,
                        color = GameUiTokens.Colors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "状态: ${if (activePet?.lifeStage == "EGG") "孵化中" else if (activePet?.lifeStage == "SOUL_SLEEP") "沉睡中" else "正常相伴"}",
                        color = GameUiTokens.Colors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GameUiTokens.Colors.Border.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Companion details
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("当前契约亲密度", color = GameUiTokens.Colors.TextSecondary, fontSize = 13.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = "亲密度", tint = Color(0xFFFF4081), modifier = Modifier.size(14.dp))
                        Text("${activePet?.intimacy ?: 0}", color = Color(0xFFFF4081), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("品种/字种", color = GameUiTokens.Colors.TextSecondary, fontSize = 13.sp)
                    Text(activePet?.petId ?: "未知荒野字灵", color = GameUiTokens.Colors.TextPrimary, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("喂养与洁净状态", color = GameUiTokens.Colors.TextSecondary, fontSize = 13.sp)
                    Text("健康饱满", color = GameUiTokens.Colors.NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Portals
        Text(
            text = "🔮 字灵交互功能入口",
            color = GameUiTokens.Colors.TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        // Portal A: Pet House (伙伴小屋)
        HubPortalCard(
            title = "🏡 前往伙伴小屋",
            description = "在这里给守护字灵喂食、重命名、更换配饰、互动并提升契约亲密度。",
            icon = Icons.Default.Cabin,
            accentColor = GameUiTokens.Colors.NeonAmber,
            onClick = onNavigateToPetHouse
        )

        // Portal B: Pet Market/egg (伙伴物资市集)
        HubPortalCard(
            title = "🛒 探访字灵物资市集",
            description = "在冒险市集中兑换全新的宠物蛋、美味零食与精美伙伴配饰。",
            icon = Icons.Default.ShoppingBag,
            accentColor = GameUiTokens.Colors.NeonGreen,
            onClick = onNavigateToShop
        )
    }
}
