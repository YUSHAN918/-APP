package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HandwritingToolBar(
    onClear: () -> Unit,
    onUndo: () -> Unit,
    onShowTip: () -> Unit,
    userBackgroundChoice: String,
    onBackgroundChoiceChange: (String) -> Unit,
    isBlackBrush: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(0xFF0F111A), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1E2235), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left part: Clear and Undo Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo button
            Button(
                onClick = onUndo,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF131625),
                    contentColor = Color.LightGray
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "撤销",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("撤销", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Clear button
            Button(
                onClick = onClear,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF131625),
                    contentColor = Color.LightGray
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "清空",
                    tint = Color(0xFFEFF3F6),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("清空", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Center part: Custom tip/meaning reveal
        Button(
            onClick = onShowTip,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x20FF9800),
                contentColor = Color(0xFFFF9800)
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(36.dp).border(1.dp, Color(0xFFFF9800).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "释义",
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("释义秘卷", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // Right part: Canvas Background Toggle Button (Single toggle to prevent squishing)
        Button(
            onClick = {
                val nextBg = if (userBackgroundChoice == "dark") "light" else "dark"
                onBackgroundChoiceChange(nextBg)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF131625),
                contentColor = Color.LightGray
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .height(36.dp)
                .border(1.dp, Color(0xFF1E2235), RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = "切换背景",
                tint = if (userBackgroundChoice == "dark") Color(0xFF00E5FF) else Color(0xFFFFB300),
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (userBackgroundChoice == "dark") "极客暗" else "宣纸白",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
