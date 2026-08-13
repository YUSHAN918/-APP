package com.example.ui.english

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EnglishRecycleBottomBar(
    onPrevious: (() -> Unit)?,
    onExit: () -> Unit,
    onNext: () -> Unit,
    nextText: String = "下一步",
    nextEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0F172A),
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Step
            if (onPrevious != null) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("recycle_prev_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上一步", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("上一步", fontSize = 13.sp)
                }
            }

            // Exit Revision Button
            OutlinedButton(
                onClick = onExit,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("recycle_exit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8))
            ) {
                Icon(Icons.Filled.Close, contentDescription = "退出复习", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("退出", fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Check / Continue Button
            Button(
                onClick = onNext,
                enabled = nextEnabled,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("recycle_next_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEC4899),
                    disabledContainerColor = Color(0xFF334155)
                )
            ) {
                Text(nextText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "下一步", modifier = Modifier.size(18.dp))
            }
        }
    }
}
