package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GamePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color = GameUiTokens.Colors.NeonCyan,
    fontSize: TextUnit = 16.sp,
    contentPadding: PaddingValues = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
    icon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        label = "scale"
    )

    val backgroundColor = if (enabled) {
        accentColor
    } else {
        GameUiTokens.Colors.SurfaceVariant
    }

    val finalBorderColor = if (enabled) {
        GameUiTokens.Colors.Border
    } else {
        GameUiTokens.Colors.Border.copy(alpha = 0.5f)
    }

    val textColor = if (enabled) {
        GameUiTokens.Colors.DarkText
    } else {
        GameUiTokens.Colors.TextSecondary.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .scale(buttonScale)
            .clip(GameUiTokens.Shapes.Button)
            .background(backgroundColor)
            .border(2.dp, finalBorderColor, GameUiTokens.Shapes.Button)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple()
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
