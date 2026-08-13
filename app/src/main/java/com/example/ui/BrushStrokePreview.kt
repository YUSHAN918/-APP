package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BrushStrokePreview(brush: BrushStyle, modifier: Modifier = Modifier) {
    val brushColor = Color(brush.baseColor)
    Box(
        modifier = modifier
            .width(60.dp)
            .height(((brush.minWidth + brush.maxWidth) / 2).dp.coerceAtLeast(2.dp).coerceAtMost(24.dp))
            .clip(RoundedCornerShape(50))
            .background(brushColor)
    )
}
