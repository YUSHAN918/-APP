package com.example.ui.english

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.EnglishClockTime
import com.example.data.english.EnglishTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EnglishClockView(
    time: EnglishClockTime,
    modifier: Modifier = Modifier,
    isEditable: Boolean = false,
    onTimeChanged: ((EnglishClockTime) -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Analog Clock Face Canvas
            Box(
                modifier = Modifier
                    .size(if (isEditable) 155.dp else 180.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .testTag("analog_clock_face"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2

                    // Draw outer border ring
                    drawCircle(
                        color = Color(0xFF6366F1),
                        radius = radius - 4.dp.toPx(),
                        center = center,
                        style = Stroke(width = 6.dp.toPx())
                    )

                    // Draw Hour ticks/labels
                    for (i in 1..12) {
                        val angleInRadians = Math.toRadians((i * 30 - 90).toDouble())
                        val textRadius = radius - 26.dp.toPx()
                        val tickX = center.x + textRadius * cos(angleInRadians).toFloat()
                        val tickY = center.y + textRadius * sin(angleInRadians).toFloat()

                        // Simple dot or small line for hour positions
                        drawCircle(
                            color = Color(0xFF4F46E5),
                            radius = 3.dp.toPx(),
                            center = Offset(tickX, tickY)
                        )
                    }

                    // Calculate hand angles
                    val hourAngle = Math.toRadians(((time.hour % 12) * 30 + time.minute * 0.5 - 90))
                    val minuteAngle = Math.toRadians((time.minute * 6 - 90).toDouble())

                    // Draw Hour hand (Thicker and shorter)
                    val hourHandLen = radius * 0.5f
                    val hourEnd = Offset(
                        center.x + hourHandLen * cos(hourAngle).toFloat(),
                        center.y + hourHandLen * sin(hourAngle).toFloat()
                    )
                    drawLine(
                        color = Color(0xFF1E1B4B),
                        start = center,
                        end = hourEnd,
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Draw Minute hand (Thinner and longer)
                    val minuteHandLen = radius * 0.75f
                    val minuteEnd = Offset(
                        center.x + minuteHandLen * cos(minuteAngle).toFloat(),
                        center.y + minuteHandLen * sin(minuteAngle).toFloat()
                    )
                    drawLine(
                        color = Color(0xFF4F46E5),
                        start = center,
                        end = minuteEnd,
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Center pin point
                    drawCircle(
                        color = Color(0xFFEC4899),
                        radius = 6.dp.toPx(),
                        center = center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Digital Time display
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = EnglishTimeFormatter.formatDigital(time),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            Text(
                text = "It's " + EnglishTimeFormatter.formatSpoken(time) + ".",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Edit controls (if isEditable)
            if (isEditable && onTimeChanged != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("小时 (Hour)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val newHour = if (time.hour == 1) 12 else time.hour - 1
                                onTimeChanged(time.copy(hour = newHour))
                            }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease hour")
                            }
                            Text("${time.hour}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = {
                                val newHour = if (time.hour == 12) 1 else time.hour + 1
                                onTimeChanged(time.copy(hour = newHour))
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase hour")
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("分钟 (Minute)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val newMinute = if (time.minute == 0) 55 else (time.minute - 5) % 60
                                onTimeChanged(time.copy(minute = newMinute))
                            }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease minute")
                            }
                            Text(String.format("%02d", time.minute), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = {
                                val newMinute = (time.minute + 5) % 60
                                onTimeChanged(time.copy(minute = newMinute))
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase minute")
                            }
                        }
                    }
                }
            }
        }
    }
}
