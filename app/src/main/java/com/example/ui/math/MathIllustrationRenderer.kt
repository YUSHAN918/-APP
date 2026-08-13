package com.example.ui.math

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MathIllustrationRenderer(
    imageAsset: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f
            val radius = minOf(width, height) * 0.35f

            // Prepare native paints
            val textPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 34f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val anglePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#FBBF24") // Amber 400
                textSize = 28f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val accentPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#38BDF8") // Sky 400
                textSize = 32f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            // Draw HUD Grid background (Rings)
            drawCircle(
                color = Color(0x1F38BDF8),
                radius = radius * 0.4f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            )
            drawCircle(
                color = Color(0x2E38BDF8),
                radius = radius * 0.8f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f))
            )
            drawCircle(
                color = Color(0x3D38BDF8),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Draw Base Cardinal Axes (North-South, East-West)
            drawLine(
                color = Color(0x33475569),
                start = Offset(centerX - radius * 1.1f, centerY),
                end = Offset(centerX + radius * 1.1f, centerY),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0x33475569),
                start = Offset(centerX, centerY - radius * 1.1f),
                end = Offset(centerX, centerY + radius * 1.1f),
                strokeWidth = 1.dp.toPx()
            )

            // Draw Compass Direction Indicators (N, S, E, W)
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText("北 (N)", centerX, centerY - radius * 1.12f, textPaint)
                canvas.nativeCanvas.drawText("南 (S)", centerX, centerY + radius * 1.25f, textPaint)
                canvas.nativeCanvas.drawText("东 (E)", centerX + radius * 1.2f, centerY + 10f, textPaint)
                canvas.nativeCanvas.drawText("西 (W)", centerX - radius * 1.2f, centerY + 10f, textPaint)
            }

            // Draw Scale bar in top-right
            val scaleUnitText = when {
                imageAsset.contains("l3") -> "比例尺 1cm = 1km"
                imageAsset.contains("u2_l1_q4") -> "比例尺 1cm = 200m"
                else -> "比例尺 1cm = 100m"
            }
            drawIntoCanvas { canvas ->
                val p = Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 24f
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                }
                canvas.nativeCanvas.drawText(scaleUnitText, width - 20f, 40f, p)
            }

            // Draw specific diagrams
            when (imageAsset) {
                "diagram_u2_l1_concept", "diagram_u2_l2_concept" -> {
                    // Center: School
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("学校 (观测点)", centerX, centerY + 40f, accentPaint.apply { color = android.graphics.Color.parseColor("#00E5FF") })
                    }
                    // Target A: North偏东30 degrees, 200m (distance = 2 units = radius * 0.8f)
                    val angleRad = Math.toRadians(30.0)
                    // Angle in mathematical standard polar coords: north is -90deg.
                    // North偏东30 means standard polar angle is -90 + 30 = -60 degrees.
                    val targetX = centerX + radius * 0.8f * cos(Math.toRadians(-60.0)).toFloat()
                    val targetY = centerY + radius * 0.8f * sin(Math.toRadians(-60.0)).toFloat()

                    // Angle line from center
                    drawLine(
                        color = Color(0xFFF59E0B),
                        start = Offset(centerX, centerY),
                        end = Offset(targetX, targetY),
                        strokeWidth = 2.dp.toPx()
                    )
                    // Draw target circle & label
                    drawCircle(
                        color = Color(0xFFEF4444),
                        radius = 6.dp.toPx(),
                        center = Offset(targetX, targetY)
                    )
                    drawCircle(
                        color = Color(0xFFEF4444).copy(alpha = 0.3f),
                        radius = 12.dp.toPx(),
                        center = Offset(targetX, targetY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("少年宫 (30°，200米)", targetX, targetY - 20f, textPaint)
                        // Arc angle indicator from North axis (N is at top, x=centerX, y=centerY-r)
                        // Display angle arc
                        canvas.nativeCanvas.drawText("30°", centerX + 25f, centerY - 60f, anglePaint)
                    }
                }

                "diagram_u2_l1_q1" -> {
                    // Center: Island
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("海岛 (观测点)", centerX, centerY + 40f, accentPaint.apply { color = android.graphics.Color.parseColor("#34D399") })
                    }
                    // Target: North偏东45 degrees, 600m
                    val targetX = centerX + radius * 0.9f * cos(Math.toRadians(-45.0)).toFloat()
                    val targetY = centerY + radius * 0.9f * sin(Math.toRadians(-45.0)).toFloat()

                    drawLine(color = Color(0xFF10B981), start = Offset(centerX, centerY), end = Offset(targetX, targetY), strokeWidth = 2.dp.toPx())
                    drawCircle(color = Color(0xFF3B82F6), radius = 6.dp.toPx(), center = Offset(targetX, targetY))
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("灯塔 (45°，600米)", targetX, targetY - 20f, textPaint)
                        canvas.nativeCanvas.drawText("45°", centerX + 35f, centerY - 50f, anglePaint)
                    }
                }

                "diagram_u2_l1_q2" -> {
                    // Center: School
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("学校 (观测点)", centerX, centerY + 40f, accentPaint)
                    }
                    // Target: South偏西30 degrees, 400m
                    // South is +90. South偏西30 is +90 + 30 = 120 degrees
                    val targetX = centerX + radius * 0.75f * cos(Math.toRadians(120.0)).toFloat()
                    val targetY = centerY + radius * 0.75f * sin(Math.toRadians(120.0)).toFloat()

                    drawLine(color = Color(0xFFF59E0B), start = Offset(centerX, centerY), end = Offset(targetX, targetY), strokeWidth = 2.dp.toPx())
                    drawCircle(color = Color(0xFFEF4444), radius = 6.dp.toPx(), center = Offset(targetX, targetY))
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("图书馆 (南偏西30°，400米)", targetX - 50f, targetY + 35f, textPaint)
                        canvas.nativeCanvas.drawText("30°", centerX - 25f, centerY + 65f, anglePaint)
                    }
                }

                "diagram_u2_l1_q3" -> {
                    // Center: Square
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("市民广场", centerX, centerY + 40f, accentPaint)
                    }
                    // Target: South偏东60 degrees, 300m
                    // South is +90. South偏东60 is +90 - 60 = 30 degrees
                    val targetX = centerX + radius * 0.7f * cos(Math.toRadians(30.0)).toFloat()
                    val targetY = centerY + radius * 0.7f * sin(Math.toRadians(30.0)).toFloat()

                    drawLine(color = Color(0xFFF59E0B), start = Offset(centerX, centerY), end = Offset(targetX, targetY), strokeWidth = 2.dp.toPx())
                    drawCircle(color = Color(0xFFEF4444), radius = 6.dp.toPx(), center = Offset(targetX, targetY))
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("体育馆 (南偏东60°，300米)", targetX + 40f, targetY + 35f, textPaint)
                        canvas.nativeCanvas.drawText("60°", centerX + 20f, centerY + 50f, anglePaint)
                    }
                }

                "diagram_u2_l1_q4" -> {
                    // Center: Radar
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("雷达站", centerX, centerY + 40f, accentPaint)
                    }
                    // Target: North偏西40 degrees, 800m
                    // North is -90. North偏西40 is -90 - 40 = -130 degrees
                    val targetX = centerX + radius * 0.95f * cos(Math.toRadians(-130.0)).toFloat()
                    val targetY = centerY + radius * 0.95f * sin(Math.toRadians(-130.0)).toFloat()

                    drawLine(color = Color(0xFF00E5FF), start = Offset(centerX, centerY), end = Offset(targetX, targetY), strokeWidth = 2.dp.toPx())
                    drawCircle(color = Color(0xFFE11D48), radius = 6.dp.toPx(), center = Offset(targetX, targetY))
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("潜艇 (北偏西40°，800米)", targetX - 30f, targetY - 20f, textPaint)
                        canvas.nativeCanvas.drawText("40°", centerX - 30f, centerY - 60f, anglePaint)
                    }
                }

                "diagram_u2_l2_q1" -> {
                    // Center: Gymnasium
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("体育馆 (观测点)", centerX, centerY + 40f, accentPaint)
                    }
                    // Target: East偏北30 degrees, 600m
                    // East is 0. East偏北30 is -30 degrees
                    val targetX = centerX + radius * 0.85f * cos(Math.toRadians(-30.0)).toFloat()
                    val targetY = centerY + radius * 0.85f * sin(Math.toRadians(-30.0)).toFloat()

                    drawLine(color = Color(0xFF00E5FF), start = Offset(centerX, centerY), end = Offset(targetX, targetY), strokeWidth = 2.dp.toPx())
                    drawCircle(color = Color(0xFFE11D48), radius = 6.dp.toPx(), center = Offset(targetX, targetY))
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("科技馆 (东偏北30°，600米)", targetX, targetY - 20f, textPaint)
                        canvas.nativeCanvas.drawText("30°", centerX + 60f, centerY - 15f, anglePaint)
                    }
                }

                "diagram_u2_l2_q2" -> {
                    // Center: Town Hall
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("市政厅", centerX, centerY + 40f, accentPaint)
                    }
                    // Target: West偏北60 degrees, 400m
                    // West is 180. West偏北60 is 180 - 60 = 120 or -120? Standard Polar: West is 180, West偏北60 is 180 + 60 = 240 or 180 - 60 = 120?
                    // North is -90. West is 180. West偏北60 is standard angle -150 degrees (or 210 degrees).
                    val targetX = centerX + radius * 0.8f * cos(Math.toRadians(-150.0)).toFloat()
                    val targetY = centerY + radius * 0.8f * sin(Math.toRadians(-150.0)).toFloat()

                    drawLine(color = Color(0xFFFBBF24), start = Offset(centerX, centerY), end = Offset(targetX, targetY), strokeWidth = 2.dp.toPx())
                    drawCircle(color = Color(0xFFEC4899), radius = 6.dp.toPx(), center = Offset(targetX, targetY))
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("百货大楼 (西偏北60°，400米)", targetX - 10f, targetY - 20f, textPaint)
                        canvas.nativeCanvas.drawText("60°", centerX - 60f, centerY - 25f, anglePaint)
                    }
                }

                "diagram_u2_l2_q3" -> {
                    // Center: Port
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("港口", centerX, centerY + 40f, accentPaint)
                    }
                    // Target: North偏东20 degrees, 500m (North is -90, -90 + 20 = -70)
                    val targetX = centerX + radius * 0.75f * cos(Math.toRadians(-70.0)).toFloat()
                    val targetY = centerY + radius * 0.75f * sin(Math.toRadians(-70.0)).toFloat()

                    drawLine(color = Color(0xFF00E5FF), start = Offset(centerX, centerY), end = Offset(targetX, targetY), strokeWidth = 2.dp.toPx())
                    drawCircle(color = Color(0xFFE11D48), radius = 6.dp.toPx(), center = Offset(targetX, targetY))
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("灯塔A (北偏东20°，500米)", targetX, targetY - 20f, textPaint)
                        canvas.nativeCanvas.drawText("20°", centerX + 20f, centerY - 60f, anglePaint)
                    }
                }

                "diagram_u2_l3_concept" -> {
                    // Simple Route Map: Home -(East, 200m)-> Library -(North偏东45°, 300m)-> Park -(South偏东30°, 150m)-> School
                    // Draw path
                    val pt1 = Offset(centerX - radius, centerY + radius * 0.4f) // Home
                    val pt2 = Offset(centerX - radius * 0.3f, centerY + radius * 0.4f) // Library
                    val pt3 = Offset(centerX + radius * 0.2f, centerY - radius * 0.2f) // Park
                    val pt4 = Offset(centerX + radius * 0.8f, centerY + radius * 0.1f) // School

                    drawLine(color = Color(0xFF38BDF8), start = pt1, end = pt2, strokeWidth = 3.dp.toPx())
                    drawLine(color = Color(0xFF38BDF8), start = pt2, end = pt3, strokeWidth = 3.dp.toPx())
                    drawLine(color = Color(0xFF38BDF8), start = pt3, end = pt4, strokeWidth = 3.dp.toPx())

                    drawCircle(color = Color(0xFF10B981), radius = 6.dp.toPx(), center = pt1)
                    drawCircle(color = Color(0xFFF59E0B), radius = 6.dp.toPx(), center = pt2)
                    drawCircle(color = Color(0xFFF59E0B), radius = 6.dp.toPx(), center = pt3)
                    drawCircle(color = Color(0xFFEF4444), radius = 6.dp.toPx(), center = pt4)

                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("小明家", pt1.x, pt1.y + 35f, textPaint)
                        canvas.nativeCanvas.drawText("图书馆", pt2.x, pt2.y + 35f, textPaint)
                        canvas.nativeCanvas.drawText("公园", pt3.x, pt3.y - 15f, textPaint)
                        canvas.nativeCanvas.drawText("学校", pt4.x, pt4.y + 35f, textPaint)
                        
                        // Path specs
                        canvas.nativeCanvas.drawText("正东 200米", (pt1.x + pt2.x) / 2f, pt1.y - 10f, anglePaint)
                        canvas.nativeCanvas.drawText("北偏东45° 300米", (pt2.x + pt3.x) / 2f - 20f, (pt2.y + pt3.y) / 2f - 10f, anglePaint)
                        canvas.nativeCanvas.drawText("南偏东30° 150米", (pt3.x + pt4.x) / 2f + 40f, (pt3.y + pt4.y) / 2f + 15f, anglePaint)
                    }
                }

                "diagram_u2_l3_q1" -> {
                    // Route of a bus. Bus Stop A -> Stop B -> Stop C.
                    // Stop A -> Stop B: North偏东30°, 3km.
                    // Stop B -> Stop C: East, 4km.
                    val pt1 = Offset(centerX - radius * 0.8f, centerY + radius * 0.5f) // Stop A
                    val pt2 = Offset(centerX - radius * 0.2f, centerY - radius * 0.5f) // Stop B
                    val pt3 = Offset(centerX + radius * 0.8f, centerY - radius * 0.5f) // Stop C

                    drawLine(color = Color(0xFF38BDF8), start = pt1, end = pt2, strokeWidth = 3.dp.toPx())
                    drawLine(color = Color(0xFF38BDF8), start = pt2, end = pt3, strokeWidth = 3.dp.toPx())

                    drawCircle(color = Color(0xFF10B981), radius = 6.dp.toPx(), center = pt1)
                    drawCircle(color = Color(0xFFF59E0B), radius = 6.dp.toPx(), center = pt2)
                    drawCircle(color = Color(0xFFEF4444), radius = 6.dp.toPx(), center = pt3)

                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("起点站A", pt1.x, pt1.y + 35f, textPaint)
                        canvas.nativeCanvas.drawText("中转站B", pt2.x - 30f, pt2.y - 15f, textPaint)
                        canvas.nativeCanvas.drawText("终点站C", pt3.x, pt3.y + 35f, textPaint)
                        
                        canvas.nativeCanvas.drawText("北偏东30° 3千米", (pt1.x + pt2.x) / 2f - 50f, (pt1.y + pt2.y) / 2f, anglePaint)
                        canvas.nativeCanvas.drawText("正东 4千米", (pt2.x + pt3.x) / 2f, pt2.y - 15f, anglePaint)
                    }
                }

                "diagram_u2_l3_q2" -> {
                    // Hiking route. Base camp -> Peak -> Valley.
                    // Camp to Peak: North偏西45°, 2km.
                    // Peak to Valley: South偏西30°, 1.5km.
                    val pt1 = Offset(centerX + radius * 0.2f, centerY + radius * 0.5f) // Camp
                    val pt2 = Offset(centerX - radius * 0.5f, centerY - radius * 0.5f) // Peak
                    val pt3 = Offset(centerX - radius * 0.9f, centerY + radius * 0.1f) // Valley

                    drawLine(color = Color(0xFF00E5FF), start = pt1, end = pt2, strokeWidth = 3.dp.toPx())
                    drawLine(color = Color(0xFF00E5FF), start = pt2, end = pt3, strokeWidth = 3.dp.toPx())

                    drawCircle(color = Color(0xFF10B981), radius = 6.dp.toPx(), center = pt1)
                    drawCircle(color = Color(0xFFFBBF24), radius = 6.dp.toPx(), center = pt2)
                    drawCircle(color = Color(0xFFEF4444), radius = 6.dp.toPx(), center = pt3)

                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("大本营", pt1.x, pt1.y + 35f, textPaint)
                        canvas.nativeCanvas.drawText("一号营地(山顶)", pt2.x + 50f, pt2.y - 15f, textPaint)
                        canvas.nativeCanvas.drawText("二号营地(山谷)", pt3.x, pt3.y + 35f, textPaint)
                        
                        canvas.nativeCanvas.drawText("北偏西45° 2km", (pt1.x + pt2.x) / 2f + 40f, (pt1.y + pt2.y) / 2f, anglePaint)
                        canvas.nativeCanvas.drawText("南偏西30° 1.5km", (pt2.x + pt3.x) / 2f - 50f, (pt2.y + pt3.y) / 2f, anglePaint)
                    }
                }

                "diagram_u2_l4_concept" -> {
                    // Double Compass return trip representation
                    // Draw Observatory (Center Left) -> Lighthouse (Center Right)
                    val ptA = Offset(centerX - radius * 0.6f, centerY)
                    val ptB = Offset(centerX + radius * 0.6f, centerY - radius * 0.4f)

                    drawLine(color = Color(0x33475569), start = Offset(ptA.x - 30f, ptA.y), end = Offset(ptA.x + 30f, ptA.y), strokeWidth = 1.dp.toPx())
                    drawLine(color = Color(0x33475569), start = Offset(ptA.x, ptA.y - 30f), end = Offset(ptA.x, ptA.y + 30f), strokeWidth = 1.dp.toPx())
                    drawLine(color = Color(0x33475569), start = Offset(ptB.x - 30f, ptB.y), end = Offset(ptB.x + 30f, ptB.y), strokeWidth = 1.dp.toPx())
                    drawLine(color = Color(0x33475569), start = Offset(ptB.x, ptB.y - 30f), end = Offset(ptB.x, ptB.y + 30f), strokeWidth = 1.dp.toPx())

                    drawLine(color = Color(0xFF38BDF8), start = ptA, end = ptB, strokeWidth = 2.dp.toPx())
                    drawCircle(color = Color(0xFF10B981), radius = 6.dp.toPx(), center = ptA)
                    drawCircle(color = Color(0xFFEF4444), radius = 6.dp.toPx(), center = ptB)

                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("北", ptA.x, ptA.y - 35f, Paint().apply { color = android.graphics.Color.GRAY; textSize = 22f; textAlign = Paint.Align.CENTER })
                        canvas.nativeCanvas.drawText("北", ptB.x, ptB.y - 35f, Paint().apply { color = android.graphics.Color.GRAY; textSize = 22f; textAlign = Paint.Align.CENTER })
                        
                        canvas.nativeCanvas.drawText("观测点A (学校)", ptA.x - 20f, ptA.y + 35f, textPaint)
                        canvas.nativeCanvas.drawText("观测点B (书店)", ptB.x + 20f, ptB.y + 35f, textPaint)
                        
                        canvas.nativeCanvas.drawText("A到B: 北偏东30°", centerX, centerY - 40f, anglePaint)
                        canvas.nativeCanvas.drawText("B到A: 南偏西30°", centerX, centerY + 20f, anglePaint)
                    }
                }

                "diagram_u2_l4_q1" -> {
                    // School Center, Library (North偏东45°, 400m), Park (West偏南30°, 300m)
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("学校 (中心)", centerX, centerY + 30f, accentPaint)
                    }
                    val libX = centerX + radius * 0.8f * cos(Math.toRadians(-45.0)).toFloat()
                    val libY = centerY + radius * 0.8f * sin(Math.toRadians(-45.0)).toFloat()

                    val parkX = centerX + radius * 0.65f * cos(Math.toRadians(150.0)).toFloat()
                    val parkY = centerY + radius * 0.65f * sin(Math.toRadians(150.0)).toFloat()

                    drawLine(color = Color(0xFF38BDF8), start = Offset(centerX, centerY), end = Offset(libX, libY), strokeWidth = 2.dp.toPx())
                    drawLine(color = Color(0xFFF59E0B), start = Offset(centerX, centerY), end = Offset(parkX, parkY), strokeWidth = 2.dp.toPx())

                    drawCircle(color = Color(0xFFEF4444), radius = 5.dp.toPx(), center = Offset(libX, libY))
                    drawCircle(color = Color(0xFF10B981), radius = 5.dp.toPx(), center = Offset(parkX, parkY))

                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("图书馆 (45°，400米)", libX, libY - 20f, textPaint)
                        canvas.nativeCanvas.drawText("西偏南30°公园 (300米)", parkX - 40f, parkY + 30f, textPaint)
                    }
                }

                else -> {
                    // Default Fallback diagram: A beautiful geometric grid
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText("方位雷达就绪 (G6S1U2)", centerX, centerY + 10f, accentPaint)
                    }
                }
            }
        }
    }
}
