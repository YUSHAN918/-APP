package com.example.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import android.view.MotionEvent
import android.view.View

enum class BrushType {
    DEFAULT_BLACK,
    PRESSURE_BRUSH,
    COLOR_BRUSH,
    GLOW_BRUSH,
    EFFECT_PLACEHOLDER
}

data class BrushStyle(
    val brushId: String,
    val brushName: String,
    val brushType: BrushType,
    val baseColor: Int,
    val minWidth: Float,
    val maxWidth: Float,
    val glowEnabled: Boolean = false,
    val particleEnabled: Boolean = false,
    val rarity: String = "NORMAL",
    val unlockCondition: String = "",
    val isDefault: Boolean = false
) {
    companion object {
        val ALL_BRUSHES = listOf(
            BrushStyle(
                brushId = "default_black",
                brushName = "默认黑笔",
                brushType = BrushType.DEFAULT_BLACK,
                baseColor = android.graphics.Color.BLACK,
                minWidth = 16f,
                maxWidth = 16f,
                isDefault = true,
                rarity = "NORMAL",
                unlockCondition = "初始赠送"
            ),
            BrushStyle(
                brushId = "practice_wood",
                brushName = "练习木笔",
                brushType = BrushType.DEFAULT_BLACK,
                baseColor = android.graphics.Color.parseColor("#8B5A2B"), // brown
                minWidth = 20f,
                maxWidth = 20f,
                isDefault = true,
                rarity = "NORMAL",
                unlockCondition = "初始赠送"
            ),
            BrushStyle(
                brushId = "ink_brush",
                brushName = "墨韵毛笔",
                brushType = BrushType.PRESSURE_BRUSH,
                baseColor = android.graphics.Color.BLACK,
                minWidth = 8f,
                maxWidth = 36f,
                rarity = "RARE",
                unlockCondition = "角色达到 2 级"
            ),
            BrushStyle(
                brushId = "stardust_brush",
                brushName = "星尘笔",
                brushType = BrushType.COLOR_BRUSH,
                baseColor = android.graphics.Color.parseColor("#E91E63"), // Deep Pink / Violet
                minWidth = 14f,
                maxWidth = 28f,
                particleEnabled = true,
                rarity = "EPIC",
                unlockCondition = "连续学习 3 天"
            ),
            BrushStyle(
                brushId = "fluorescent_brush",
                brushName = "荧光笔",
                brushType = BrushType.GLOW_BRUSH,
                baseColor = android.graphics.Color.parseColor("#00FFCC"), // Neon Cyan
                minWidth = 24f,
                maxWidth = 24f,
                glowEnabled = true,
                rarity = "EPIC",
                unlockCondition = "净化 10 个错题魔物"
            ),
            BrushStyle(
                brushId = "rainbow_brush",
                brushName = "彩虹笔",
                brushType = BrushType.COLOR_BRUSH,
                baseColor = android.graphics.Color.parseColor("#FF9800"), // Orange base
                minWidth = 18f,
                maxWidth = 18f,
                rarity = "LEGEND",
                unlockCondition = "限时活动解锁"
            ),
            BrushStyle(
                brushId = "pet_dragon_brush",
                brushName = "小墨龙之笔",
                brushType = BrushType.PRESSURE_BRUSH,
                baseColor = android.graphics.Color.parseColor("#E64A19"), // Darker orange-red
                minWidth = 4f,
                maxWidth = 24f,
                glowEnabled = false,
                particleEnabled = true,
                rarity = "LEGEND",
                unlockCondition = "宠物亲密度达到 50"
            )
        )

        fun getBrushById(id: String): BrushStyle {
            return ALL_BRUSHES.find { it.brushId == id } ?: ALL_BRUSHES[0]
        }
    }
}

data class PointData(
    val x: Float,
    val y: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val pressure: Float = 1.0f
)

data class StrokeData(val path: Path, val points: List<PointData> = emptyList())

class HandwritingView(context: Context) : View(context) {
    private val strokePointsList = mutableListOf<List<PointData>>()
    private val strokeBrushesList = mutableListOf<BrushStyle>()
    private val strokeConfigsList = mutableListOf<com.example.data.PlayerBrushConfig?>()
    
    private var currentPoints = mutableListOf<PointData>()
    var currentBrush: BrushStyle = BrushStyle.ALL_BRUSHES[0]
    var currentBrushConfig: com.example.data.PlayerBrushConfig? = null
    
    var cols = 1
    var rows = 1

    var isDarkTheme: Boolean = false
        set(value) {
            field = value
            if (value) {
                gridPaint.color = android.graphics.Color.parseColor("#1E2235")
                gridPaint.alpha = 255
                gridBorderPaint.color = android.graphics.Color.parseColor("#1E2235")
            } else {
                gridPaint.color = android.graphics.Color.LTGRAY
                gridPaint.alpha = 128
                gridBorderPaint.color = android.graphics.Color.LTGRAY
            }
            invalidate()
        }

    fun setGrid(c: Int, r: Int) {
        if (cols != c || rows != r) {
            cols = c
            rows = r
            invalidate()
        }
    }

    var lastPressureMin: Float = 0f
    var lastPressureMax: Float = 0f
    var lastPressureValid: Boolean = false
    var lastWidthMode: String = "AUTO"
    var lastSpeedMin: Float = 0f
    var lastSpeedMax: Float = 0f
    var lastWidthMinReal: Float = 0f
    var lastWidthMaxReal: Float = 0f
    var recentStrokeWidths = mutableListOf<Float>()

    private val gridPaint = Paint().apply {
        color = android.graphics.Color.LTGRAY
        alpha = 128
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }
    
    private val gridBorderPaint = Paint().apply {
        color = android.graphics.Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    init {
        setBackgroundColor(android.graphics.Color.parseColor("#FDF6E3")) // light paper color
    }

    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val w = width.toFloat()
        val h = height.toFloat()
        
        val cellW = w / cols
        val cellH = h / rows

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = c * cellW
                val top = r * cellH
                val right = left + cellW
                val bottom = top + cellH
                
                // outer box
                canvas.drawRect(left, top, right, bottom, gridBorderPaint)
                
                // inner cross
                canvas.drawLine(left, top + cellH / 2, right, top + cellH / 2, gridPaint)
                canvas.drawLine(left + cellW / 2, top, left + cellW / 2, bottom, gridPaint)
            }
        }

        // Draw historic strokes
        for (i in 0 until strokePointsList.size) {
            val points = strokePointsList[i]
            val brush = strokeBrushesList.getOrNull(i) ?: BrushStyle.ALL_BRUSHES[0]
            val config = strokeConfigsList.getOrNull(i)
            drawStrokePoints(canvas, points, brush, config)
        }
        
        // Draw current stroke
        if (currentPoints.isNotEmpty()) {
            drawStrokePoints(canvas, currentPoints, currentBrush, currentBrushConfig)
        }
    }

    private fun drawStrokePoints(canvas: Canvas, points: List<PointData>, brush: BrushStyle, config: com.example.data.PlayerBrushConfig?) {
        if (points.isEmpty()) return
        
        strokePaint.reset()
        strokePaint.isAntiAlias = true
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeJoin = Paint.Join.ROUND
        strokePaint.strokeCap = Paint.Cap.ROUND

        val opacity = config?.opacity ?: 1.0f
        val smoothing = config?.smoothing ?: 0.35f

        val baseColor = if (config != null && config.colorHex.isNotBlank() && config.colorHex != "default") {
            try {
                android.graphics.Color.parseColor(config.colorHex)
            } catch (e: Exception) { brush.baseColor }
        } else brush.baseColor

        var drawColor = baseColor
        if (isDarkTheme) {
            if (drawColor == android.graphics.Color.BLACK) {
                drawColor = android.graphics.Color.parseColor("#00E5FF") // neon cyan
            } else if (drawColor == android.graphics.Color.parseColor("#8B5A2B")) {
                drawColor = android.graphics.Color.parseColor("#FFD700") // golden
            }
        }

        strokePaint.color = drawColor
        strokePaint.alpha = (opacity * 255).toInt()
        
        val glowEnabled = config?.let { it.glowRadius > 0f } ?: brush.glowEnabled
        val glowRadius = config?.glowRadius?.takeIf { it > 0f } ?: if (brush.glowEnabled) 16f else 0f

        if (glowEnabled && glowRadius > 0f) {
            strokePaint.setShadowLayer(glowRadius, 0f, 0f, drawColor)
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        } else {
            setLayerType(LAYER_TYPE_NONE, null)
        }

        val maxWidth = config?.maxWidth ?: brush.maxWidth
        val minWidth = config?.minWidth ?: brush.minWidth
        val speedSens = config?.speedSensitivity ?: 1.2f
        val pressureSens = config?.pressureSensitivity ?: 1.0f

        if (points.size == 1) {
            strokePaint.strokeWidth = maxWidth
            canvas.drawPoint(points[0].x, points[0].y, strokePaint)
            return
        }

        var pMin = Float.MAX_VALUE
        var pMax = Float.MIN_VALUE
        for (p in points) {
            if (p.pressure < pMin) pMin = p.pressure
            if (p.pressure > pMax) pMax = p.pressure
        }
        val pressureRange = pMax - pMin
        val hasValidPressure = pressureRange > 0.08f

        lastPressureMin = pMin
        lastPressureMax = pMax
        lastPressureValid = hasValidPressure

        val requestedMode = config?.widthMode ?: "AUTO"
        val effectiveMode = when (requestedMode) {
            "FORCE_SPEED" -> "SPEED"
            "FORCE_PRESSURE" -> "PRESSURE"
            "FIXED" -> "FIXED"
            else -> if (hasValidPressure) "PRESSURE" else "SPEED"
        }
        lastWidthMode = effectiveMode

        var currentWidth = maxWidth
        var minRealW = Float.MAX_VALUE
        var maxRealW = Float.MIN_VALUE
        var minSpd = Float.MAX_VALUE
        var maxSpd = Float.MIN_VALUE
        recentStrokeWidths.clear()

        for (i in 1 until points.size) {
            val p1 = points[i - 1]
            val p2 = points[i]
            
            val dx = p2.x - p1.x
            val dy = p2.y - p1.y
            var dt = p2.timestamp - p1.timestamp
            if (dt <= 0) dt = 16L
            val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            val speed = distance / dt

            if (speed < minSpd) minSpd = speed
            if (speed > maxSpd) maxSpd = speed

            val targetWidth = if (brush.brushType != BrushType.PRESSURE_BRUSH || effectiveMode == "FIXED") {
                maxWidth
            } else if (effectiveMode == "SPEED") {
                // slow = thick, fast = thin
                val normalizedSpeed = (speed * speedSens / 2.5f).coerceIn(0f, 1f)
                maxWidth - (maxWidth - minWidth) * normalizedSpeed
            } else {
                // PRESSURE
                val pT = p2.pressure.coerceIn(0f, 1f) * pressureSens
                minWidth + (maxWidth - minWidth) * pT
            }

            currentWidth = currentWidth * smoothing + targetWidth * (1f - smoothing)

            if (currentWidth < minRealW) minRealW = currentWidth
            if (currentWidth > maxRealW) maxRealW = currentWidth
            recentStrokeWidths.add(currentWidth)
            if (recentStrokeWidths.size > 5) recentStrokeWidths.removeAt(0)

            strokePaint.strokeWidth = currentWidth
            
            if (brush.brushId == "rainbow_brush") {
                val hue = (i * 12f) % 360f
                strokePaint.color = android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
                strokePaint.alpha = (opacity * 255).toInt()
            } else {
                strokePaint.color = drawColor
                strokePaint.alpha = (opacity * 255).toInt()
            }

            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, strokePaint)

            val particleEnabled = config?.let { it.particleDensity > 0f } ?: brush.particleEnabled
            val particleDensity = config?.particleDensity ?: if (brush.brushId == "pet_dragon_brush") 0.15f else 1.0f

            if (particleEnabled && i % 2 == 0 && Math.random() < particleDensity) {
                val particlePaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    color = android.graphics.Color.HSVToColor(floatArrayOf((i * 30f) % 360f, 0.6f, 1f))
                    alpha = (opacity * 255).toInt()
                }
                val radius = 4f + java.util.Random().nextFloat() * 6f
                val offsetX = -15f + java.util.Random().nextFloat() * 30f
                val offsetY = -15f + java.util.Random().nextFloat() * 30f
                canvas.drawCircle(p2.x + offsetX, p2.y + offsetY, radius, particlePaint)
            }
        }

        lastSpeedMin = if (minSpd == Float.MAX_VALUE) 0f else minSpd
        lastSpeedMax = if (maxSpd == Float.MIN_VALUE) 0f else maxSpd
        lastWidthMinReal = if (minRealW == Float.MAX_VALUE) maxWidth else minRealW
        lastWidthMaxReal = if (maxRealW == Float.MIN_VALUE) maxWidth else maxRealW
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        
        val x = event.x
        val y = event.y
        val pressure = event.pressure
        val time = event.eventTime
        
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("HandwritingDebug", "ACTION_DOWN at $x, $y")
                currentPoints = mutableListOf(PointData(x, y, time, pressure))
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val historySize = event.historySize
                for (i in 0 until historySize) {
                    val hx = event.getHistoricalX(i)
                    val hy = event.getHistoricalY(i)
                    val hp = event.getHistoricalPressure(i)
                    val ht = event.getHistoricalEventTime(i)
                    currentPoints.add(PointData(hx, hy, ht, hp))
                }
                currentPoints.add(PointData(x, y, time, pressure))
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Log.d("HandwritingDebug", "ACTION_UP/CANCEL at $x, $y")
                currentPoints.add(PointData(x, y, time, pressure))
                if (currentPoints.isNotEmpty()) {
                    strokePointsList.add(currentPoints.toList())
                    strokeBrushesList.add(currentBrush)
                    strokeConfigsList.add(currentBrushConfig)
                }
                currentPoints = mutableListOf()
                invalidate()
            }
        }
        return true
    }

    fun clear() {
        Log.d("HandwritingDebug", "clear canvas")
        strokePointsList.clear()
        strokeBrushesList.clear()
        strokeConfigsList.clear()
        currentPoints.clear()
        invalidate()
    }

    fun undo() {
        if (strokePointsList.isNotEmpty()) {
            Log.d("HandwritingDebug", "undo last stroke")
            strokePointsList.removeAt(strokePointsList.size - 1)
            if (strokeBrushesList.isNotEmpty()) {
                strokeBrushesList.removeAt(strokeBrushesList.size - 1)
            }
            if (strokeConfigsList.isNotEmpty()) {
                strokeConfigsList.removeAt(strokeConfigsList.size - 1)
            }
            invalidate()
        }
    }

    fun getStrokes(): List<StrokeData> {
        val result = mutableListOf<StrokeData>()
        for (i in 0 until strokePointsList.size) {
            val points = strokePointsList[i]
            val path = Path()
            if (points.isNotEmpty()) {
                path.moveTo(points[0].x, points[0].y)
                for (j in 1 until points.size) {
                    path.lineTo(points[j].x, points[j].y)
                }
            }
            result.add(StrokeData(path, points))
        }
        return result
    }
}