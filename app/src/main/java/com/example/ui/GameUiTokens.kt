package com.example.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object GameUiTokens {
    object Colors {
        val Background = Color(0xFF161C26) // 现代游戏深邃藏蓝底色
        val Surface = Color(0xFF232D3B)    // 高质感深蓝面板
        val SurfaceVariant = Color(0xFF2E3B4E) // 稍浅的蓝灰面板，用于卡片与容器
        val Border = Color(0xFF0F141D)     // 极高对比度的暗色轮廓
        val BorderActive = Color(0xFFFFAE19) // 鎏金流光高亮边框
        
        val NeonCyan = Color(0xFF4AC2F4)   // 冰晶霓虹蓝
        val NeonAmber = Color(0xFFF4B73E)  // 暖阳金
        val Gold = Color(0xFFFFAE19)       // 耀眼流金
        val NeonGreen = Color(0xFF67C46A)  // 极光绿
        val NeonRed = Color(0xFFF25C5C)    // 警示红
        
        val TextPrimary = Color(0xFFF7F4EA)   // 香槟白，极佳易读性
        val TextSecondary = Color(0xFFBAC3D0) // 次级灰蓝色文字
        val TextMuted = Color(0xFF7E8A9C)     // 暗部说明文字
        
        val Parchment = Color(0xFFF2E2BB) // 羊皮香槟金浅色
        val DarkText = Color(0xFF1A1F26)  // 适配羊皮香槟金的深色文字
    }

    object Shapes {
        val Panel = RoundedCornerShape(20.dp)
        val Button = RoundedCornerShape(14.dp)
        val Chip = RoundedCornerShape(24.dp)
        val Avatar = RoundedCornerShape(16.dp)
    }

    object Spacing {
        val Xs = 4.dp
        val Sm = 8.dp
        val Md = 16.dp
        val Lg = 24.dp
        val Xl = 32.dp
    }

    object Elevation {
        val Normal = 4.dp
        val Pressed = 1.dp
        val Active = 8.dp
    }
}

