package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class GameTab(val label: String, val icon: ImageVector) {
    CAMP("营地", Icons.Default.Home),
    ADVENTURE("冒险", Icons.Default.Explore),
    BRUSH("神笔", Icons.Default.Brush),
    COMPANION("伙伴", Icons.Default.Pets),
    GROWTH("成长", Icons.Default.Stars)
}

@Composable
fun GameBottomNavigation(
    selectedTab: GameTab,
    onTabSelected: (GameTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(GameUiTokens.Colors.Surface)
            .navigationBarsPadding()
            .height(68.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GameTab.values().forEach { tab ->
            val isSelected = tab == selectedTab
            
            val yOffset by animateFloatAsState(
                targetValue = if (isSelected) -4f else 0f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                label = "yOffset"
            )
            
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.08f else 1.0f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                label = "iconScale"
            )

            val tintColor by animateColorAsState(
                targetValue = if (isSelected) GameUiTokens.Colors.DarkText else GameUiTokens.Colors.TextSecondary,
                label = "tintColor"
            )

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) GameUiTokens.Colors.Parchment else Color.Transparent,
                label = "bgColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .offset(y = yOffset.dp)
                        .clip(GameUiTokens.Shapes.Button)
                        .background(bgColor)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = tintColor,
                        modifier = Modifier
                            .scale(iconScale)
                            .size(28.dp)
                    )
                    Text(
                        text = tab.label,
                        color = tintColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
