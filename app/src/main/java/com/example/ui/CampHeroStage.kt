package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

@Composable
fun CampHeroStage(
    playerName: String,
    playerLevel: Int,
    isPetEgg: Boolean,
    petName: String?,
    onPetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Elegant floating breathing motion (+/- 1.5dp) strictly compliant with constraints
    val infiniteTransition = rememberInfiniteTransition(label = "lobby_stage_breath")
    
    val playerFloatOffset by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PlayerFloat"
    )

    val petFloatOffset by infiniteTransition.animateFloat(
        initialValue = 1.2f,
        targetValue = -1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PetFloat"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Cosmic backdrop glow ring
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(240.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GameUiTokens.Colors.Gold.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 2. Dual-Layer Floating Stage / Scenic Pedestal
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main glowing magic circle base
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(28.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GameUiTokens.Colors.SurfaceVariant.copy(alpha = 0.9f),
                                GameUiTokens.Colors.Surface.copy(alpha = 0.95f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(1.5.dp, GameUiTokens.Colors.BorderActive.copy(alpha = 0.8f), CircleShape)
            ) {
                // Shiny inner energy ring
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.7f)
                        .border(1.dp, GameUiTokens.Colors.NeonCyan.copy(alpha = 0.4f), CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Shadow cast on the stage (interactive elements float above this shadow)
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = (-14).dp)
        ) {
            // Player Shadow
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(14.dp)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
            )
            
            if (petName != null) {
                Spacer(modifier = Modifier.width(56.dp))
                // Pet Shadow
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(10.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                )
            }
        }

        // 3. Characters Layout
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            // --- PLAYER ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    translationY = playerFloatOffset.dp.toPx()
                }
            ) {
                // Highly polished avatar ring with golden crown/crest vibe
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    GameUiTokens.Colors.SurfaceVariant,
                                    GameUiTokens.Colors.Surface
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(3.dp, GameUiTokens.Colors.BorderActive, CircleShape)
                        .border(
                            width = 6.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner soft blue light
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.85f)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        GameUiTokens.Colors.NeonCyan.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // Grand high-contrast emoji representing the Player Character/Word Scholar
                    Text(
                        text = "🧙‍♂️",
                        fontSize = 58.sp,
                        modifier = Modifier.scale(1.05f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // RPG Level Badge & Name Banner
                Row(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    GameUiTokens.Colors.Surface,
                                    GameUiTokens.Colors.SurfaceVariant,
                                    GameUiTokens.Colors.Surface
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.5.dp, GameUiTokens.Colors.BorderActive.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(GameUiTokens.Colors.NeonAmber, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Lv.$playerLevel",
                            color = GameUiTokens.Colors.DarkText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = playerName,
                        color = GameUiTokens.Colors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (petName != null) {
                Spacer(modifier = Modifier.width(42.dp))

                // --- COMPANION / PET ---
                val petInteractionSource = remember { MutableInteractionSource() }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = petFloatOffset.dp.toPx()
                        }
                        .clickable(
                            interactionSource = petInteractionSource,
                            indication = null,
                            onClick = onPetClick
                        )
                ) {
                    // Speech/Interaction Bubble (Hologram look)
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        GameUiTokens.Colors.Surface,
                                        GameUiTokens.Colors.SurfaceVariant
                                    )
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(1.dp, GameUiTokens.Colors.NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .offset(y = (-6).dp)
                    ) {
                        Text(
                            text = if (isPetEgg) "✨ 待孵化" else "🐾 伴读中",
                            color = GameUiTokens.Colors.NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Pet Portrait Sphere
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        GameUiTokens.Colors.SurfaceVariant,
                                        GameUiTokens.Colors.Surface
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(2.dp, GameUiTokens.Colors.NeonGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Soft green glow backdrop
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.8f)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            GameUiTokens.Colors.NeonGreen.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        if (isPetEgg) {
                            // Beautiful hatching egg composition with sparkles
                            Text("🥚", fontSize = 42.sp)
                        } else {
                            // Growing companion pet emoji with lively feel
                            Text("🦊", fontSize = 42.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Companion Name tag
                    Box(
                        modifier = Modifier
                            .background(GameUiTokens.Colors.Surface, RoundedCornerShape(8.dp))
                            .border(1.dp, GameUiTokens.Colors.Border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = petName,
                            color = GameUiTokens.Colors.TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
