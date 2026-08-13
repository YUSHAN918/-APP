package com.example.ui.english

import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import kotlin.math.max
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.english.*
import com.example.util.AudioRecorderHelper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.english.EnglishTTSHelper
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset

class EnglishHandwritingView(context: Context) : View(context) {
    private val strokePointsList = mutableListOf<List<com.example.ui.PointData>>()
    private val strokeBrushesList = mutableListOf<com.example.ui.BrushStyle>()
    private val strokeConfigsList = mutableListOf<com.example.data.PlayerBrushConfig?>()
    private var currentPoints = mutableListOf<com.example.ui.PointData>()

    var currentBrush: com.example.ui.BrushStyle = com.example.ui.BrushStyle.ALL_BRUSHES[0]
        set(value) {
            field = value
            invalidate()
        }

    var currentBrushConfig: com.example.data.PlayerBrushConfig? = null
        set(value) {
            field = value
            invalidate()
        }

    var onStrokeFinished: (() -> Unit)? = null

    init {
        setBackgroundColor(android.graphics.Color.parseColor("#12161A")) // deep slate
    }

    private val solidGridPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#E53935") // red outer
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val midLinePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#3B82F6") // blue inner
        style = Paint.Style.STROKE
        strokeWidth = 2f
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

        // Draw standard English 4-line 3-space guide
        val totalHeight = h * 0.5f
        val startY = h * 0.25f
        val spacing = totalHeight / 3f

        val line1 = startY
        val line2 = startY + spacing
        val line3 = startY + spacing * 2
        val line4 = startY + spacing * 3

        // Outer red lines
        canvas.drawLine(0f, line1, w, line1, solidGridPaint)
        canvas.drawLine(0f, line4, w, line4, solidGridPaint)

        // Inner blue lines
        canvas.drawLine(0f, line2, w, line2, midLinePaint)
        canvas.drawLine(0f, line3, w, line3, midLinePaint)

        // Border
        val borderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1C354A")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(0f, 0f, w, h, borderPaint)

        // Historic strokes with their respective brushes
        for (i in 0 until strokePointsList.size) {
            val points = strokePointsList[i]
            val brush = strokeBrushesList.getOrNull(i) ?: currentBrush
            val config = strokeConfigsList.getOrNull(i) ?: currentBrushConfig
            drawStrokePoints(canvas, points, brush, config)
        }

        // Current stroke
        if (currentPoints.isNotEmpty()) {
            drawStrokePoints(canvas, currentPoints, currentBrush, currentBrushConfig)
        }
    }

    private fun drawStrokePoints(
        canvas: Canvas,
        points: List<com.example.ui.PointData>,
        brush: com.example.ui.BrushStyle,
        config: com.example.data.PlayerBrushConfig?
    ) {
        if (points.isEmpty()) return

        strokePaint.reset()
        strokePaint.isAntiAlias = true
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeJoin = Paint.Join.ROUND
        strokePaint.strokeCap = Paint.Cap.ROUND

        val opacity = config?.opacity ?: 1.0f

        var drawColor = if (config != null && config.colorHex.isNotBlank() && config.colorHex != "default") {
            try {
                android.graphics.Color.parseColor(config.colorHex)
            } catch (e: Exception) { brush.baseColor }
        } else brush.baseColor

        // Dark theme color adaptation for visibility
        if (drawColor == android.graphics.Color.BLACK) {
            drawColor = android.graphics.Color.parseColor("#00E5FF") // neon cyan for black brush on dark slate
        } else if (drawColor == android.graphics.Color.parseColor("#8B5A2B")) {
            drawColor = android.graphics.Color.parseColor("#FFD700") // golden for wood brush
        }

        strokePaint.color = drawColor
        strokePaint.alpha = (opacity * 255).coerceIn(0f, 255f).toInt()

        val glowEnabled = config?.let { it.glowRadius > 0f } ?: brush.glowEnabled
        val glowRadius = config?.glowRadius?.takeIf { it > 0f } ?: if (brush.glowEnabled) 12f else 0f

        if (glowEnabled && glowRadius > 0f) {
            strokePaint.setShadowLayer(glowRadius, 0f, 0f, drawColor)
        } else {
            strokePaint.clearShadowLayer()
        }

        val minW = (config?.minWidth ?: brush.minWidth).coerceIn(6f, 22f)
        val maxW = (config?.maxWidth ?: brush.maxWidth).coerceIn(10f, 28f)

        if (points.size == 1) {
            strokePaint.strokeWidth = (minW + maxW) / 2f
            canvas.drawPoint(points[0].x, points[0].y, strokePaint)
            return
        }

        for (i in 1 until points.size) {
            val p1 = points[i - 1]
            val p2 = points[i]
            val pressure = (p1.pressure + p2.pressure) / 2f
            strokePaint.strokeWidth = minW + (maxW - minW) * pressure.coerceIn(0.1f, 1.0f)
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, strokePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        val x = event.x
        val y = event.y
        val pressure = event.pressure
        val time = event.eventTime

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentPoints = mutableListOf(com.example.ui.PointData(x, y, time, pressure))
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val historySize = event.historySize
                for (i in 0 until historySize) {
                    currentPoints.add(com.example.ui.PointData(
                        event.getHistoricalX(i),
                        event.getHistoricalY(i),
                        event.getHistoricalEventTime(i),
                        event.getHistoricalPressure(i)
                    ))
                }
                currentPoints.add(com.example.ui.PointData(x, y, time, pressure))
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                currentPoints.add(com.example.ui.PointData(x, y, time, pressure))
                if (currentPoints.isNotEmpty()) {
                    strokePointsList.add(currentPoints.toList())
                    strokeBrushesList.add(currentBrush)
                    strokeConfigsList.add(currentBrushConfig)
                }
                currentPoints = mutableListOf()
                invalidate()
                onStrokeFinished?.invoke()
            }
        }
        return true
    }

    fun undo() {
        if (strokePointsList.isNotEmpty()) {
            strokePointsList.removeAt(strokePointsList.lastIndex)
            if (strokeBrushesList.isNotEmpty()) strokeBrushesList.removeAt(strokeBrushesList.lastIndex)
            if (strokeConfigsList.isNotEmpty()) strokeConfigsList.removeAt(strokeConfigsList.lastIndex)
            invalidate()
            onStrokeFinished?.invoke()
        }
    }

    fun clear() {
        strokePointsList.clear()
        strokeBrushesList.clear()
        strokeConfigsList.clear()
        currentPoints.clear()
        invalidate()
        onStrokeFinished?.invoke()
    }

    fun getStrokes(): List<List<com.example.ui.PointData>> = strokePointsList.toList()

    fun setStrokes(strokes: List<List<com.example.ui.PointData>>) {
        strokePointsList.clear()
        strokeBrushesList.clear()
        strokeConfigsList.clear()
        strokePointsList.addAll(strokes)
        strokes.forEach {
            strokeBrushesList.add(currentBrush)
            strokeConfigsList.add(currentBrushConfig)
        }
        invalidate()
    }
}

enum class LearnStage(val title: String) {
    INTRO("导学展示"),
    LISTEN_MEANING("听音辨意"),
    READ_ALOUD("大声朗读"),
    PLAYBACK("录音回放"),
    SPELL("拼写拼图"),
    WRITE("笔迹手写"),
    DICTATION("听写核对"),
    SPATIAL_PRACTICE("空间练习")
}

fun canCompleteLesson(
    unitId: String,
    currentExpressionIndex: Int,
    totalExpressions: Int,
    currentStage: LearnStage
): Boolean {
    if (totalExpressions > 0 && currentExpressionIndex < totalExpressions - 1) {
        return false
    }
    if (unitId == "english_pep_2013_g3_s2_u4") {
        if (currentStage != LearnStage.SPATIAL_PRACTICE && currentStage != LearnStage.DICTATION && currentStage != LearnStage.SPELL) {
            return false
        }
    }
    return true
}


enum class EnglishLessonType(val title: String, val description: String) {
    LESSON1("课时 1：打招呼与自我介绍", "学会如何向他人问候、打招呼并作自我介绍"),
    LESSON2("课时 2：文具词汇A", "认识 ruler、pencil、eraser、crayon 等文具物品"),
    LESSON3("课时 3：询问姓名", "学会用 What's your name? 询问姓名并礼貌作答"),
    LESSON4("课时 4：文具词汇B", "学习 bag、pen、pencil box、book 等核心词汇"),
    LESSON5("课时 5：单元听说复习", "听懂并说出 no, your 两个非认读要求词汇"),
    LESSON6("课时 6：单词综合挑战", "精通第一单元所有文具单词的听说拼写全能挑战！")
}

fun getLessonTitle(unitId: String, lessonType: EnglishLessonType): String {
    if (unitId == "english_pep_2013_g4_s1_u6") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：我们家有几个人"
            EnglishLessonType.LESSON2 -> "课时 2：家庭成员词汇"
            EnglishLessonType.LESSON3 -> "课时 3：五个元音综合复习"
            EnglishLessonType.LESSON4 -> "课时 4：这是你的谁"
            EnglishLessonType.LESSON5 -> "课时 5：家人的职业"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 6 综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g4_s1_u5") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：今晚想吃什么"
            EnglishLessonType.LESSON2 -> "课时 2：晚餐食物词汇"
            EnglishLessonType.LESSON3 -> "课时 3：词尾 e 长元音拼读工坊"
            EnglishLessonType.LESSON4 -> "课时 4：请随便吃"
            EnglishLessonType.LESSON5 -> "课时 5：餐具与餐桌准备"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 5 综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g4_s1_u4") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：小猫藏在哪里"
            EnglishLessonType.LESSON2 -> "课时 2：家庭房间词汇"
            EnglishLessonType.LESSON3 -> "课时 3：u-e 长元音拼读工坊"
            EnglishLessonType.LESSON4 -> "课时 4：钥匙在哪里"
            EnglishLessonType.LESSON5 -> "课时 5：家具物品与房间布置"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 4 综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g4_s1_u3") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：认识我的新朋友"
            EnglishLessonType.LESSON2 -> "课时 2：性格与特征词汇"
            EnglishLessonType.LESSON3 -> "课时 3：o-e 长元音拼读工坊"
            EnglishLessonType.LESSON4 -> "课时 4：猜猜他是谁"
            EnglishLessonType.LESSON5 -> "课时 5：外貌与随身物品"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 3 综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g4_s1_u2") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：我的新书包"
            EnglishLessonType.LESSON2 -> "课时 2：书包与书本词汇"
            EnglishLessonType.LESSON3 -> "课时 3：i-e 长元音拼读工坊"
            EnglishLessonType.LESSON4 -> "课时 4：失物招领处"
            EnglishLessonType.LESSON5 -> "课时 5：书包物品与整理"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 2 综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g4_s2_u3") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：我现在可以去外面吗？"
            EnglishLessonType.LESSON2 -> "课时 2：天气与温度词汇"
            EnglishLessonType.LESSON3 -> "课时 3：ar 与 al 发音工坊"
            EnglishLessonType.LESSON4 -> "课时 4：世界天气预报"
            EnglishLessonType.LESSON5 -> "课时 5：各种气候词汇与大连线"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 3 综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g4_s2_u2") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：现在几点了"
            EnglishLessonType.LESSON2 -> "课时 2：一天中的课程与三餐"
            EnglishLessonType.LESSON3 -> "课时 3：ir / ur 发音工坊"
            EnglishLessonType.LESSON4 -> "课时 4：快起床，该上学了"
            EnglishLessonType.LESSON5 -> "课时 5：我的一天时间表"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 2 综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g4_s2_u1") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：这是我们的学校"
            EnglishLessonType.LESSON2 -> "课时 2：学校场馆与楼层词汇"
            EnglishLessonType.LESSON3 -> "课时 3：-er 词尾 Phonics 拼读工坊"
            EnglishLessonType.LESSON4 -> "课时 4：这是教师办公室吗"
            EnglishLessonType.LESSON5 -> "课时 5：多功能教室与场馆词汇"
            EnglishLessonType.LESSON6 -> "课时 6：My school 单元综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g4_s1_u1") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：走进新教室"
            EnglishLessonType.LESSON2 -> "课时 2：教室词汇 A"
            EnglishLessonType.LESSON3 -> "课时 3：a-e 长元音拼读工坊"
            EnglishLessonType.LESSON4 -> "课时 4：一起整理教室"
            EnglishLessonType.LESSON5 -> "课时 5：教室词汇 B 与空间布置"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 1 综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u1") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：欢迎返校与自我介绍"
            EnglishLessonType.LESSON2 -> "课时 2：国家词汇与国旗卡"
            EnglishLessonType.LESSON3 -> "课时 3：短元音 a 拼读工坊"
            EnglishLessonType.LESSON4 -> "课时 4：你来自哪里"
            EnglishLessonType.LESSON5 -> "课时 5：人物身份与代词 he/she"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 1 单元综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u2") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：人物介绍与称谓问答"
            EnglishLessonType.LESSON2 -> "课时 2：家庭成员词汇 A"
            EnglishLessonType.LESSON3 -> "课时 3：短元音 e 拼读工坊"
            EnglishLessonType.LESSON4 -> "课时 4：人物询问与家庭相册"
            EnglishLessonType.LESSON5 -> "课时 5：家庭成员词汇 B"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 2 单元综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u3") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：进入卡通动物园"
            EnglishLessonType.LESSON2 -> "课时 2：动物特征词汇 A"
            EnglishLessonType.LESSON3 -> "课时 3：短元音 i 拼读工坊"
            EnglishLessonType.LESSON4 -> "课时 4：它是什么样与它有什么"
            EnglishLessonType.LESSON5 -> "课时 5：动物特征词汇 B 与怪兽工坊"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 3 单元综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u6") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：风筝草地数一数"
            EnglishLessonType.LESSON2 -> "课时 2：数字单词 11—15"
            EnglishLessonType.LESSON3 -> "课时 3：五个短元音总复习"
            EnglishLessonType.LESSON4 -> "课时 4：打开盒子数一数"
            EnglishLessonType.LESSON5 -> "课时 5：数字单词 16—20 与计数工坊"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 6 单元综合挑战"
        }
    }
    if (unitId.endsWith("_u6")) {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：生日派对与数量询问"
            EnglishLessonType.LESSON2 -> "课时 2：数字单词 A"
            EnglishLessonType.LESSON3 -> "课时 3：字母 Uu—Zz Phonics"
            EnglishLessonType.LESSON4 -> "课时 4：年龄询问与生日祝福"
            EnglishLessonType.LESSON5 -> "课时 5：数字单词 B 与数量指令"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 6 单元综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u5") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：水果集市与喜好问答"
            EnglishLessonType.LESSON2 -> "课时 2：水果词汇 A"
            EnglishLessonType.LESSON3 -> "课时 3：短元音 u 拼读工坊"
            EnglishLessonType.LESSON4 -> "课时 4：水果分享与礼貌请求"
            EnglishLessonType.LESSON5 -> "课时 5：水果词汇 B 与果篮搭配"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 5 单元综合挑战"
        }
    }
    if (unitId.endsWith("_u5")) {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：饥饿情境与表达需求"
            EnglishLessonType.LESSON2 -> "课时 2：核心食物词汇 A"
            EnglishLessonType.LESSON3 -> "课时 3：字母 Oo—Tt Phonics"
            EnglishLessonType.LESSON4 -> "课时 4：礼貌索取与递交食物"
            EnglishLessonType.LESSON5 -> "课时 5：核心食物词汇 B 与动作指令"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 5 单元综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u4") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：伙伴玩具房寻宝"
            EnglishLessonType.LESSON2 -> "课时 2：位置词汇与家具"
            EnglishLessonType.LESSON3 -> "课时 3：短元音 o 拼读工坊"
            EnglishLessonType.LESSON4 -> "课时 4：它在这里吗"
            EnglishLessonType.LESSON5 -> "课时 5：玩具与物品词汇"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 4 单元综合挑战"
        }
    }
    if (unitId.endsWith("_u4")) {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：动物情境与近处问答"
            EnglishLessonType.LESSON2 -> "课时 2：动物核心词汇 A"
            EnglishLessonType.LESSON3 -> "课时 3：远处动物问答与喜好表达"
            EnglishLessonType.LESSON4 -> "课时 4：动物核心词汇 B"
            EnglishLessonType.LESSON5 -> "课时 5：动物词汇 C 与动作指令"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 4 单元综合挑战"
        }
    }
    if (unitId.endsWith("_u3")) {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：身体部位与核心情境导入"
            EnglishLessonType.LESSON2 -> "课时 2：身体词汇A"
            EnglishLessonType.LESSON3 -> "课时 3：身体部位表达与指认"
            EnglishLessonType.LESSON4 -> "课时 4：身体词汇B"
            EnglishLessonType.LESSON5 -> "动作指令与字母发音"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 3 单元综合挑战"
        }
    }
    if (unitId.endsWith("_u2")) {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "课时 1：问候与介绍他人"
            EnglishLessonType.LESSON2 -> "课时 2：颜色词汇A"
            EnglishLessonType.LESSON3 -> "课时 3：看见与辨认颜色"
            EnglishLessonType.LESSON4 -> "课时 4：颜色词汇B"
            EnglishLessonType.LESSON5 -> "课时 5：颜色指令与字母发音"
            EnglishLessonType.LESSON6 -> "课时 6：Unit 2 单元综合挑战"
        }
    }
    return lessonType.title
}

fun getLessonDescription(unitId: String, lessonType: EnglishLessonType): String {
    if (unitId == "english_pep_2013_g4_s2_u3") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "学习询问是否能出去活动：Can I go outside now? Yes, you can. / No, you can't. 以及 It's cold outside。"
            EnglishLessonType.LESSON2 -> "掌握 cold, cool, warm, hot 核心天气与温度形容词的听、说、认读、拼写。"
            EnglishLessonType.LESSON3 -> "探秘字母组合 ar / al 的发音规律，掌握 arm, car, card, ball, tall, wall 的读音与拼读。"
            EnglishLessonType.LESSON4 -> "学习如何询问天气情况及温度度数：What's the weather like in New York? It's 26 degrees。"
            EnglishLessonType.LESSON5 -> "掌握 sunny, windy, cloudy, snowy, rainy 核心天气形容词，并参与世界各城市天气状况的大连线探究。"
            EnglishLessonType.LESSON6 -> "精通第三单元所有天气词汇、温度、世界城市天气问答的听说拼写全能挑战！"
        }
    }
    if (unitId == "english_pep_2013_g4_s2_u2") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "认识整点与常见时间，学习 What time is it? 和 It's time for...。"
            EnglishLessonType.LESSON2 -> "正式学习 breakfast, English class, lunch, music class, PE class, dinner 6个核心词汇的听、说、读、写与标准三线格手写。"
            EnglishLessonType.LESSON3 -> "探秘字母组合 ir / ur 在单词中的发音规律，掌握 girl, bird, nurse, hamburger 等单词的读音与拼读。"
            EnglishLessonType.LESSON4 -> "学习起床、上学等时间情境，学会区分并使用 It's time for... 与 It's time to...。"
            EnglishLessonType.LESSON5 -> "正式学习 get up, go to school, go home, go to bed 核心日程活动，并完成虚拟作息时间排序挑战。"
            EnglishLessonType.LESSON6 -> "精通第二单元所有日程、时间表达、词汇与核心问答的听说拼写全能挑战！"
        }
    }
    if (unitId == "english_pep_2013_g4_s2_u1") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "走进宽阔美丽的校园，学会询问并回答图书馆等场馆的所在楼层 (Where is the library? It's on the first floor.)"
            EnglishLessonType.LESSON2 -> "掌握 teachers' office, library, first floor, second floor 核心词汇的听、说、读、写与标准三线格手写"
            EnglishLessonType.LESSON3 -> "探秘字母组合 er 在单词词尾的发音规律 /ə(r)/，掌握 sister, computer, teacher, ruler 等单词的读音与拼读"
            EnglishLessonType.LESSON4 -> "学习确认学校场馆位置的句型 (Is this/that the teachers' office? Do you have a library?)，完成场馆寻找与指引"
            EnglishLessonType.LESSON5 -> "掌握 playground, computer room, art room, music room 核心词汇，并能陈述学校拥有的设施和学生班级规模"
            EnglishLessonType.LESSON6 -> "精通第一单元所有学校场馆、楼层、位置代词词汇与核心问答的听说拼写全能挑战！"
        }
    }
    if (unitId == "english_pep_2013_g4_s1_u6") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "通过虚拟家庭学习家庭人数、people 和家庭数量表达"
            EnglishLessonType.LESSON2 -> "学习 5 个主要家庭成员名称 parents, cousin, uncle, aunt, baby brother"
            EnglishLessonType.LESSON3 -> "综合复习五个元音字母的长元音与短元音拼读规律"
            EnglishLessonType.LESSON4 -> "通过虚拟家庭学习确认家庭成员身份，练习身份确认问答"
            EnglishLessonType.LESSON5 -> "学习 5 个职业核心词 doctor, cook, driver, farmer, nurse"
            EnglishLessonType.LESSON6 -> "家庭成员、职业、人数及开闭音节拼读复习综合挑战"
        }
    }
    if (unitId == "english_pep_2013_g4_s1_u5") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "进入晚餐情境，学习 What’s for dinner?、What would you like? 和 I’d like..."
            EnglishLessonType.LESSON2 -> "学习五大主要食物名称 beef, chicken, noodles, soup, vegetable"
            EnglishLessonType.LESSON3 -> "学习词尾 e 发长元音 /iː/ 拼读规律 (me, he, she, we)"
            EnglishLessonType.LESSON4 -> "学习餐桌礼貌，提供食物与餐具以及接受或拒绝建议"
            EnglishLessonType.LESSON5 -> "学习常见餐具词汇 chopsticks, bowl, fork, knife, spoon 及指令"
            EnglishLessonType.LESSON6 -> "Unit 5 听音辨义、看图识食物与餐具、核心词汇四阶段拼写听写及餐桌关卡"
        }
    }
    if (unitId == "english_pep_2013_g4_s1_u4") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "认识家庭房间，学习 Where is she?、Is she in...? 和单数位置回答"
            EnglishLessonType.LESSON2 -> "学习五大主要房间名称 bedroom, living room, study, kitchen, bathroom"
            EnglishLessonType.LESSON3 -> "学习 u-e 字母组合长元音发音规律，对比短元音 u 的读音"
            EnglishLessonType.LESSON4 -> "学习 Where are...?、Are they...? 和复数物品的位置回答"
            EnglishLessonType.LESSON5 -> "学习常见家具词汇 bed, phone, table, sofa, fridge 及描述句子"
            EnglishLessonType.LESSON6 -> "Unit 4 听音辨义、看图识房间、核心词汇四阶段拼写听写及寻物关卡"
        }
    }
    if (unitId == "english_pep_2013_g4_s1_u3") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "学习介绍新朋友、询问姓名，并用简单特征描述朋友"
            EnglishLessonType.LESSON2 -> "学习 strong、friendly、quiet 核心词汇及性格描述"
            EnglishLessonType.LESSON3 -> "学习 nose、note、Coke、Mr Jones，掌握 o-e 长元音拼读规律"
            EnglishLessonType.LESSON4 -> "根据性别代词、外貌和物品线索猜出人物，学习 his、her、or、right"
            EnglishLessonType.LESSON5 -> "学习 hair、shoe、glasses，并掌握 is/has/his/her 人物描述"
            EnglishLessonType.LESSON6 -> "综合检测朋友性格与外貌词汇、his/her/is/has 句型及 o-e 拼读全能挑战！"
        }
    }
    if (unitId == "english_pep_2013_g4_s1_u2") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "认识新书包情境，学习 What's in your schoolbag? 和书包内容表达"
            EnglishLessonType.LESSON2 -> "学习 schoolbag、maths book、English book、Chinese book、storybook 核心词汇"
            EnglishLessonType.LESSON3 -> "学习 like、kite、five、nine、rice，认识 i-e 长元音拼读规律"
            EnglishLessonType.LESSON4 -> "学习描述遗失书包，用颜色和内容线索找回物品"
            EnglishLessonType.LESSON5 -> "学习 candy、notebook、toy、key，并完成卡通书包整理与单复数表达"
            EnglishLessonType.LESSON6 -> "综合检测书包词汇、物品数量、失物招领、i-e 拼读全能挑战！"
        }
    }
    if (unitId == "english_pep_2013_g4_s1_u1") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "认识新教室，学习 What's in the classroom?、We have... 和 Where is it?"
            EnglishLessonType.LESSON2 -> "学习 classroom、window、blackboard、light、picture、door 核心词汇"
            EnglishLessonType.LESSON3 -> "学习 cake、face、name、make，认识 a-e 长元音拼读规律"
            EnglishLessonType.LESSON4 -> "学习 Let's clean...、Let me... 和 Let me help you，完成卡通教室整理活动"
            EnglishLessonType.LESSON5 -> "学习 teacher's desk、computer、fan、wall、floor，并完成教室空间布置"
            EnglishLessonType.LESSON6 -> "综合检测教室词汇、位置表达、整理指令、a-e 拼读全能挑战！"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u1") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "学习问候语 Welcome back! / Nice to see you again. 及自我介绍"
            EnglishLessonType.LESSON2 -> "掌握 UK, Canada, USA, China 国家词汇的听、说、读、写"
            EnglishLessonType.LESSON3 -> "学习字母 a 在单词中的短元音 /æ/ 发音规则与例词 (cat, bag, dad, hand)"
            EnglishLessonType.LESSON4 -> "学会用 Where are you from? 询问对方来源及回答 I'm from China."
            EnglishLessonType.LESSON5 -> "掌握 she, student, pupil, he, teacher 及介绍他人"
            EnglishLessonType.LESSON6 -> "精通第一单元所有国家词汇、身份词汇与核心表达全能挑战！"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u2") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "在太空家庭聚会中，用 Who's that man/woman? 询问他人身份，并介绍 my father/mother"
            EnglishLessonType.LESSON2 -> "掌握 father, mother, man, woman, dad, mum 核心家庭成员词汇的认、读、听、说与拼写"
            EnglishLessonType.LESSON3 -> "学习字母 e 在单词中的短元音 /e/ 发音规则与例词 (ten, pen, leg, red)"
            EnglishLessonType.LESSON4 -> "学会用 Who's that boy/girl? Is he/she your...? 询问身份，配合虚拟相册趣味指认"
            EnglishLessonType.LESSON5 -> "掌握 grandfather, grandmother, grandpa, grandma, brother, sister, family 核心称谓词汇"
            EnglishLessonType.LESSON6 -> "精通第二单元所有家庭成员称谓 and 询问句型的听说拼写全能挑战！"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u3") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "在“星语动物园探索日”中，理解 Look at... 表达，通过观察动物初步感知形容词"
            EnglishLessonType.LESSON2 -> "正式学习 thin, fat, tall, short 四个核心动物体型形容词的听、说、认读、拼写与笔迹"
            EnglishLessonType.LESSON3 -> "探秘字母 i 在闭音节中发短元音 /ɪ/ 的拼读规则，掌握 big, pig, six, milk 的拼读与拼图"
            EnglishLessonType.LESSON4 -> "掌握 It's... 描述动物整体和 It has... 描述局部的句型，学习 long, small, big, short 细节词"
            EnglishLessonType.LESSON5 -> "通过“怪兽特征拼装工坊”生动拼装属于自己的怪兽并匹配句型描述，掌握全部核心特征词"
            EnglishLessonType.LESSON6 -> "挑战三年级下册 Unit 3 单元全能挑战，达成动物形容词、拼写、听写和 Phonics 大满贯！"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u6") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "在风筝草地嘉年华中，学习用 How many... do you see? 询问并回答看见的数量，认识 kite 和 beautiful 拓展词"
            EnglishLessonType.LESSON2 -> "掌握 eleven, twelve, thirteen, fourteen, fifteen 核心数字词汇的认、读、听、说、拼写与四线三格手写"
            EnglishLessonType.LESSON3 -> "复习 a, e, i, o, u 五个短元音拼读规则，完成 hand, legs, ten, dog, duck, big 的元音补全与分类"
            EnglishLessonType.LESSON4 -> "在打开文具盒与惊喜场景中，用 How many... do you have? 询问并用 I have / We have... 回答拥有数量"
            EnglishLessonType.LESSON5 -> "掌握 sixteen, seventeen, eighteen, nineteen, twenty 核心数字词汇与 9 字母长词拼写，并体验数字计数嘉年华"
            EnglishLessonType.LESSON6 -> "精通第六单元所有 11—20 数字单词、看见与拥有数量问答、五短元音与全能综合挑战！"
        }
    }
    if (unitId.endsWith("_u6")) {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "在星语生日派对中，学习用 How many plates? / How many candles? 询问并回答物品数量"
            EnglishLessonType.LESSON2 -> "掌握 one, two, three, four, five 等核心数字词汇的认、读、听、说与拼写"
            EnglishLessonType.LESSON3 -> "学习 Uu 至 Zz 六个字母的读音、大小写、首尾音辨析与标准四线三格手写规范"
            EnglishLessonType.LESSON4 -> "学会用 How old are you? 询问年龄，并掌握 Happy birthday! 生日情境问候"
            EnglishLessonType.LESSON5 -> "掌握 six, seven, eight, nine, ten 及 brother, plate 词汇，并在生日台匹配对应的数量指令"
            EnglishLessonType.LESSON6 -> "精通第六单元所有数字和生日单词的听说拼写全能挑战！"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u5") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "在卡通水果集市中，用 Do you like...? 询问喜好，并掌握 Yes, I do. / No, I don't. 肯定与否定回答"
            EnglishLessonType.LESSON2 -> "掌握 pear, apple, orange, banana 核心水果词汇的认、读、听、说、拼写与四线三格手写"
            EnglishLessonType.LESSON3 -> "探秘字母 u 在闭音节中发短元音 /ʌ/ 的拼读规则，掌握 fun, run, duck, under 的拼读与拼图"
            EnglishLessonType.LESSON4 -> "在水果分享会中，用 Have some... 提供水果，用 Can I have some...? 礼貌请求，掌握 Here you are. 和 Thanks."
            EnglishLessonType.LESSON5 -> "掌握 watermelon, strawberry, grape 核心水果词汇与 10 字母长词拼写，并完成果篮搭配"
            EnglishLessonType.LESSON6 -> "精通第五单元所有水果单词、单复数表达、喜好与分享句型全能挑战！"
        }
    }
    if (unitId.endsWith("_u5")) {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "在星语补给餐桌中，学习用 I’d like some... 礼貌表达对食物和饮品的需求"
            EnglishLessonType.LESSON2 -> "掌握 bread, juice, egg, milk 等核心食物词汇的认、读、听、说与拼写"
            EnglishLessonType.LESSON3 -> "学习 Oo 至 Tt 六个字母的读音、大小写、首音辨析与标准四线三格手写规范"
            EnglishLessonType.LESSON4 -> "在太空配送窗口，用 Can I have some...? 礼貌请求并掌握 Here you are. 和 You're welcome."
            EnglishLessonType.LESSON5 -> "掌握 fish, rice, water, cake 词汇，并在实验台匹配 Eat, Drink, Cut 动作指令"
            EnglishLessonType.LESSON6 -> "精通第五单元所有食物单词的听说拼写全能挑战！"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u4") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "在“伙伴玩具房寻宝”中，用 Where is...? 寻找和询问铅笔盒、书等物品，初步感知空间位置"
            EnglishLessonType.LESSON2 -> "掌握 on, in, under, chair, desk 核心位置与家具词汇的听、说、认读、拼写与笔迹"
            EnglishLessonType.LESSON3 -> "探秘字母 o 在闭音节中发短元音 /ɒ/ 的拼读规则，掌握 dog, box, orange, body 的拼读与拼图"
            EnglishLessonType.LESSON4 -> "学会用 Is it in/on/under...? 询问物品具体位置，配合 Yes, it is. / No, it isn't. 作出准确的肯定与否定回答"
            EnglishLessonType.LESSON5 -> "掌握 cap, ball, car, boat, map 核心玩具与物品词汇，并在玩具房执行物体摆放位置指令"
            EnglishLessonType.LESSON6 -> "精通第四单元所有空间方位、玩具词汇与位置指认句型的听说拼写全能挑战！"
        }
    }
    if (unitId.endsWith("_u4")) {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "理解 What's this?，结合动物轮廓或局部特征进行猜测问答"
            EnglishLessonType.LESSON2 -> "学习 cat, duck, dog, pig, bear 核心动物词汇"
            EnglishLessonType.LESSON3 -> "理解 What's that? 与远处指认，并学会使用 I like it! 和 Cool!"
            EnglishLessonType.LESSON4 -> "学习 bird, panda, tiger 核心动物词汇"
            EnglishLessonType.LESSON5 -> "学习 elephant, monkey 核心词，及 Act like a... 动作指令与 Jj-Nn 字母发音"
            EnglishLessonType.LESSON6 -> "精通第四单元所有动物单词的听说拼写全能挑战！"
        }
    }
    if (unitId.endsWith("_u3")) {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "学习 Look at me! 核心情境，认识基础身体部位"
            EnglishLessonType.LESSON2 -> "学习 face, ear, eye, nose 核心身体词汇"
            EnglishLessonType.LESSON3 -> "学会用 This is my... 介绍并指认身体部位"
            EnglishLessonType.LESSON4 -> "学习 mouth, head, hand, arm 核心身体词汇"
            EnglishLessonType.LESSON5 -> "掌握 Touch your... 等身体动作指令及 body, leg 词汇"
            EnglishLessonType.LESSON6 -> "精通第三单元所有身体单词的听说拼写全能挑战！"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u2") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "在太空家庭聚会中，用 Who's that man/woman? 询问他人身份，并介绍 my father/mother"
            EnglishLessonType.LESSON2 -> "掌握 father, mother, man, woman, dad, mum 核心家庭成员词汇的认、读、听、说与拼写"
            EnglishLessonType.LESSON3 -> "学习字母 e 在单词中的短元音 /e/ 发音规则与例词 (ten, pen, leg, red)"
            EnglishLessonType.LESSON4 -> "学会用 Who's that boy/girl? Is he/she your...? 询问身份，配合虚拟相册趣味指认"
            EnglishLessonType.LESSON5 -> "掌握 grandfather, grandmother, grandpa, grandma, brother, sister, family 核心称谓词汇"
            EnglishLessonType.LESSON6 -> "精通第二单元所有家庭成员称谓 and 询问句型的听说拼写全能挑战！"
        }
    }
    if (unitId == "english_pep_2013_g3_s2_u3") {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "在“星语动物园探索日”中，理解 Look at... 表达，通过观察动物初步感知形容词"
            EnglishLessonType.LESSON2 -> "正式学习 thin, fat, tall, short 四个核心动物体型形容词的听、说、认读、拼写与笔迹"
            EnglishLessonType.LESSON3 -> "探秘字母 i 在闭音节中发短元音 /ɪ/ 的拼读规则，掌握 big, pig, six, milk 的拼读与拼图"
            EnglishLessonType.LESSON4 -> "掌握 It's... 描述动物整体和 It has... 描述局部的句型，学习 long, small, big, short 细节词"
            EnglishLessonType.LESSON5 -> "通过“怪兽特征拼装工坊”生动拼装属于自己的怪兽并匹配句型描述，掌握全部核心特征词"
            EnglishLessonType.LESSON6 -> "挑战三年级下册 Unit 3 单元全能挑战，达成动物形容词、拼写、听写和 Phonics 大满贯！"
        }
    }
    if (unitId.endsWith("_u2")) {
        return when (lessonType) {
            EnglishLessonType.LESSON1 -> "区分问候语，学会用 This is... 介绍他人及 Nice to meet you"
            EnglishLessonType.LESSON2 -> "认识 red, yellow, green, blue 核心颜色词"
            EnglishLessonType.LESSON3 -> "理解 I see...，听到颜色选择色块并表达"
            EnglishLessonType.LESSON4 -> "认识 black, white, orange, brown 核心颜色词"
            EnglishLessonType.LESSON5 -> "掌握 Colour it... 指令及 A—D 字母基础发音"
            EnglishLessonType.LESSON6 -> "精通第二单元所有颜色单词的听说拼写全能挑战！"
        }
    }
    return lessonType.description
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnglishLessonScreen(
    courseId: String,
    unitId: String,
    lessonId: String,
    viewModel: com.example.viewmodel.GameViewModel? = null,
    onBack: () -> Unit,
    onComplete: (Int) -> Unit
) {
    val context = LocalContext.current
    val playerProfile by viewModel?.playerProfile?.collectAsStateWithLifecycle(initialValue = null) ?: remember { mutableStateOf(null) }
    val equippedBrushConfig by viewModel?.equippedBrushConfig?.collectAsStateWithLifecycle(initialValue = null) ?: remember { mutableStateOf(null) }
    val equippedBrushId = playerProfile?.equippedBrushId ?: "default_black"
    val equippedBrushStyle = remember(equippedBrushId) { com.example.ui.BrushStyle.getBrushById(equippedBrushId) }
    var isLoaded by remember { mutableStateOf(false) }
    var unitData by remember { mutableStateOf<EnglishUnit?>(null) }
    
    // State machine: if currentLessonType is null, we show UnitOverviewScreen!
    var currentLessonType by remember { mutableStateOf<EnglishLessonType?>(null) }
    var currentWordIndex by remember { mutableStateOf(0) }
    var currentExpressionIndex by remember { mutableStateOf(0) }
    var currentStage by remember { mutableStateOf(LearnStage.INTRO) }
    var showLessonSummary by remember { mutableStateOf(false) }
    
    // Media & TTS helpers
    val ttsHelper = remember { EnglishTTSHelper(context) }
    val recorderHelper = remember { AudioRecorderHelper(context) }
    
    // Clean up media on dispose
    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.release()
            recorderHelper.stopPlaying()
            recorderHelper.stopRecording()
            recorderHelper.deleteCurrentRecording()
        }
    }
    
    // Physical Back button handling
    androidx.activity.compose.BackHandler(enabled = currentLessonType != null || showLessonSummary) {
        if (showLessonSummary) {
            showLessonSummary = false
            currentLessonType = null
        } else {
            currentLessonType = null
        }
    }
    
    // Load unit data
    LaunchedEffect(courseId, unitId) {
        val loaded = EnglishContentLoader.loadUnit(context, courseId, unitId)
        if (loaded != null) {
            unitData = loaded
            isLoaded = true
        } else {
            Toast.makeText(context, "无法加载英语课程数据", Toast.LENGTH_LONG).show()
            onBack()
        }
    }
    
    if (!isLoaded || unitData == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator(color = Color(0xFFEC4899))
                Text("星语海港数据舱连接中...", color = Color.LightGray, fontSize = 14.sp)
            }
        }
        return
    }
    
    val unit = unitData!!

    if (currentLessonType == null) {
        // 1. Render the real UnitOverviewScreen as requested!
        UnitOverviewScreen(
            unit = unit,
            context = context,
            onBack = onBack,
            onStartLesson = { lessonType ->
                currentLessonType = lessonType
                currentWordIndex = 0
                currentExpressionIndex = 0
                currentStage = LearnStage.INTRO
                showLessonSummary = false
            }
        )
    } else if (showLessonSummary) {
        // 2. Render lesson Completion / Gold Reward Screen
        val lessonType = currentLessonType!!
        val isUnitChallenge = lessonType == EnglishLessonType.LESSON6
        val coinReward = if (isUnitChallenge) 20 else 5
        
        // Save state persistently
        LaunchedEffect(Unit) {
            EnglishProgressManager.completeLesson(context, "${unit.unitId}_${lessonType.name}")
            if (isUnitChallenge) {
                EnglishProgressManager.completeUnit(context, unit.unitId)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, Color(0xFFEC4899)),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "🎉 挑战成功！",
                        color = Color(0xFF00E5FF),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    
                    Text(
                        text = getLessonTitle(unit.unitId, lessonType),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "恭喜你完成本课时全部探险训练！",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEC4899).copy(alpha = 0.15f), CircleShape)
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🪙", fontSize = 24.sp)
                            Text("金币 +$coinReward", color = Color(0xFFEC4899), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Button(
                        onClick = {
                            if (isUnitChallenge) {
                                onComplete(20)
                                onBack()
                            } else {
                                onComplete(5)
                                showLessonSummary = false
                                currentLessonType = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("english_lesson_finish_button")
                    ) {
                        Text(
                            text = if (isUnitChallenge) "完美通关单元！返回" else "返回课时大厅",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    } else {
        // 3. Render lesson active learning flow
        val lessonType = currentLessonType!!
        
        if ((unit.unitId == "english_pep_2013_g4_s1_u1" || unit.unitId == "english_pep_2013_g4_s1_u2" || unit.unitId == "english_pep_2013_g4_s1_u3" || unit.unitId == "english_pep_2013_g4_s1_u4" || unit.unitId == "english_pep_2013_g4_s2_u1" || unit.unitId == "english_pep_2013_g4_s2_u2" || unit.unitId == "english_pep_2013_g4_s2_u3" || unit.unitId == "english_pep_2013_g3_s2_u1" || unit.unitId == "english_pep_2013_g3_s2_u2" || unit.unitId == "english_pep_2013_g3_s2_u3" || unit.unitId == "english_pep_2013_g3_s2_u4" || unit.unitId.endsWith("_u5") || unit.unitId.endsWith("_u6")) && lessonType == EnglishLessonType.LESSON3) {
            EnglishLetterLessonView(
                unitId = unit.unitId,
                ttsHelper = ttsHelper,
                equippedBrushStyle = equippedBrushStyle,
                equippedBrushConfig = equippedBrushConfig,
                onBack = { currentLessonType = null },
                onLessonCompleted = {
                    showLessonSummary = true
                }
            )
        } else if (lessonType == EnglishLessonType.LESSON6) {
            UnitChallengeScreen(
                unit = unit,
                context = context,
                ttsHelper = ttsHelper,
                viewModel = viewModel,
                onBack = { currentLessonType = null },
                onComplete = { coins ->
                    showLessonSummary = true
                }
            )
        } else {
            val isExpressionLesson = if (unit.unitId == "english_pep_2013_g4_s1_u1" || unit.unitId == "english_pep_2013_g4_s1_u2" || unit.unitId == "english_pep_2013_g4_s1_u3" || unit.unitId == "english_pep_2013_g4_s1_u4" || unit.unitId == "english_pep_2013_g4_s1_u5" || unit.unitId == "english_pep_2013_g4_s2_u1" || unit.unitId == "english_pep_2013_g4_s2_u2" || unit.unitId == "english_pep_2013_g4_s2_u3") {
                lessonType == EnglishLessonType.LESSON1 || lessonType == EnglishLessonType.LESSON4
            } else if (unit.unitId == "english_pep_2013_g3_s2_u1" || unit.unitId == "english_pep_2013_g3_s2_u2" || unit.unitId == "english_pep_2013_g3_s2_u3" || unit.unitId == "english_pep_2013_g3_s2_u4") {
                lessonType == EnglishLessonType.LESSON1 || lessonType == EnglishLessonType.LESSON4
            } else if (unit.unitId.endsWith("_u5") || unit.unitId.endsWith("_u6")) {
                lessonType == EnglishLessonType.LESSON1 || lessonType == EnglishLessonType.LESSON4
            } else {
                lessonType == EnglishLessonType.LESSON1 || lessonType == EnglishLessonType.LESSON3
            }
            
            if (isExpressionLesson) {
            // Render dialog / core expression learning flow
            val expressions = if (lessonType == EnglishLessonType.LESSON1) {
                if (unit.unitId == "english_pep_2013_g4_s1_u6") {
                    unit.expressions.filter { it.expressionId in listOf("g4s1_u6_exp1", "g4s1_u6_exp2", "g4s1_u6_exp3", "g4s1_u6_exp4", "g4s1_u6_exp5", "g4s1_u6_exp6") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u5") {
                    unit.expressions.filter { it.expressionId in listOf("g4s1_u5_exp1", "g4s1_u5_exp2", "g4s1_u5_exp3", "g4s1_u5_exp4", "g4s1_u5_exp5", "g4s1_u5_exp6") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u4") {
                    unit.expressions.filter { it.expressionId in listOf("g4s1_u4_exp1", "g4s1_u4_exp2", "g4s1_u4_exp3") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u3") {
                    unit.expressions.filter { it.expressionId in listOf("g4s1_u3_exp1", "g4s1_u3_exp2", "g4s1_u3_exp3", "g4s1_u3_exp4", "g4s1_u3_exp5", "g4s1_u3_exp6", "g4s1_u3_exp7", "g4s1_u3_exp8") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u2") {
                    unit.expressions.filter { it.expressionId in listOf("g4s1_u2_exp1", "g4s1_u2_exp2", "g4s1_u2_exp3", "g4s1_u2_exp4", "g4s1_u2_exp5", "g4s1_u2_exp6") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u1") {
                    unit.expressions.filter { it.expressionId in listOf("g4s1_u1_exp1", "g4s1_u1_exp2", "g4s1_u1_exp3", "g4s1_u1_exp4", "g4s1_u1_exp5", "g4s1_u1_exp6") }
                } else if (unit.unitId == "english_pep_2013_g4_s2_u2") {
                    unit.expressions.filter { it.expressionId in listOf("g4s2_u2_exp1", "g4s2_u2_exp2", "g4s2_u2_exp3", "g4s2_u2_exp4", "g4s2_u2_exp5", "g4s2_u2_exp6") || it.expressionId in listOf("english_pep_2013_g4_s2_u2_exp1", "english_pep_2013_g4_s2_u2_exp2", "english_pep_2013_g4_s2_u2_exp3", "english_pep_2013_g4_s2_u2_exp4", "english_pep_2013_g4_s2_u2_exp5", "english_pep_2013_g4_s2_u2_exp6") }
                } else if (unit.unitId == "english_pep_2013_g4_s2_u3") {
                    unit.expressions.filter { it.expressionId in listOf("english_pep_2013_g4_s2_u3_exp1", "english_pep_2013_g4_s2_u3_exp2", "english_pep_2013_g4_s2_u3_exp3", "english_pep_2013_g4_s2_u3_exp4", "english_pep_2013_g4_s2_u3_exp5", "english_pep_2013_g4_s2_u3_exp6") }
                } else if (unit.unitId == "english_pep_2013_g4_s2_u1") {
                    unit.expressions.filter { it.expressionId in listOf("g4s2_u1_exp1", "g4s2_u1_exp2", "g4s2_u1_exp3", "g4s2_u1_exp4", "g4s2_u1_exp5", "g4s2_u1_exp6") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u1") {
                    unit.expressions.filter { it.expressionId in listOf("g3s2_u1_exp1", "g3s2_u1_exp2", "g3s2_u1_exp3", "g3s2_u1_exp4", "g3s2_u1_exp5", "g3s2_u1_exp6") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u2") {
                    unit.expressions.filter { it.expressionId in listOf("g3s2_u2_exp1", "g3s2_u2_exp2", "g3s2_u2_exp3", "g3s2_u2_exp4", "g3s2_u2_exp5", "g3s2_u2_exp6") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u3") {
                    unit.expressions.filter { it.expressionId in listOf("g3s2_u3_exp1", "g3s2_u3_exp2", "g3s2_u3_exp3", "g3s2_u3_exp4", "g3s2_u3_exp5", "g3s2_u3_exp6") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u4") {
                    unit.expressions.filter { it.expressionId in listOf("g3s2_u4_exp1", "g3s2_u4_exp2", "g3s2_u4_exp3", "g3s2_u4_exp4", "g3s2_u4_exp5", "g3s2_u4_exp6", "g3s2_u4_exp7") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u5") {
                    unit.expressions.filter { it.expressionId in listOf("g3s2_u5_exp1", "g3s2_u5_exp2", "g3s2_u5_exp3", "g3s2_u5_exp4", "g3s2_u5_exp5", "g3s2_u5_exp6", "g3s2_u5_exp7") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u6") {
                    unit.expressions.filter { it.expressionId in listOf("g3s2_u6_exp1", "g3s2_u6_exp2", "g3s2_u6_exp3", "g3s2_u6_exp4", "g3s2_u6_exp5", "g3s2_u6_exp6", "g3s2_u6_exp7", "g3s2_u6_exp8") }
                } else if (unit.unitId.endsWith("_u6")) {
                    unit.expressions.filter { it.expressionId in listOf("g3s1_u6_exp2", "g3s1_u6_exp3", "g3s1_u6_exp8", "g3s1_u6_exp9", "g3s1_u6_exp11", "g3s1_u6_exp13") }
                } else if (unit.unitId.endsWith("_u5")) {
                    unit.expressions.filter { it.expressionId in listOf("g3s1_u5_exp1", "g3s1_u5_exp2", "g3s1_u5_exp3", "g3s1_u5_exp4", "g3s1_u5_exp5") }
                } else if (unit.unitId.endsWith("_u4")) {
                    unit.expressions.filter { it.expressionId in listOf("g3s1_u4_exp1", "g3s1_u4_exp2", "g3s1_u4_exp3", "g3s1_u4_exp4", "g3s1_u4_exp5") }
                } else if (unit.unitId.endsWith("_u2")) {
                    unit.expressions.filter { it.expressionId in listOf("g3s1_u2_exp1", "g3s1_u2_exp2", "g3s1_u2_exp3", "g3s1_u2_exp4", "g3s1_u2_exp5") }
                } else if (unit.unitId.endsWith("_u3")) {
                    unit.expressions.filter { it.expressionId in listOf("g3s1_u3_exp1", "g3s1_u3_exp2", "g3s1_u3_exp3", "g3s1_u3_exp4", "g3s1_u3_exp5") }
                } else {
                    unit.expressions.take(2)
                }
            } else {
                if (unit.unitId == "english_pep_2013_g4_s1_u6") {
                    unit.expressions.filter { it.expressionId in listOf("g4s1_u6_exp7", "g4s1_u6_exp8", "g4s1_u6_exp9") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u5") {
                    unit.expressions.filter { it.expressionId in listOf("g4s1_u5_exp7", "g4s1_u5_exp8", "g4s1_u5_exp9", "g4s1_u5_exp10", "g4s1_u5_exp11", "g4s1_u5_exp12") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u4") {
                    unit.expressions.filter { it.expressionId in listOf("g4s1_u4_exp9", "g4s1_u4_exp10", "g4s1_u4_exp11") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u3") {
                    unit.expressions.filter { it.expressionId in listOf("g4s1_u3_exp9", "g4s1_u3_exp10", "g4s1_u3_exp11", "g4s1_u3_exp12", "g4s1_u3_exp13", "g4s1_u3_exp14", "g4s1_u3_exp15") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u2") {
                    unit.expressions.filter { it.expressionId in listOf("g4s1_u2_exp7", "g4s1_u2_exp8", "g4s1_u2_exp9", "g4s1_u2_exp10", "g4s1_u2_exp11", "g4s1_u2_exp12", "g4s1_u2_exp13") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u1") {
                    unit.expressions.filter { it.expressionId in listOf("g4s1_u1_exp7", "g4s1_u1_exp8", "g4s1_u1_exp9", "g4s1_u1_exp10", "g4s1_u1_exp11", "g4s1_u1_exp12", "g4s1_u1_exp13") }
                } else if (unit.unitId == "english_pep_2013_g4_s2_u2") {
                    unit.expressions.filter { it.expressionId in listOf("g4s2_u2_exp7", "g4s2_u2_exp8", "g4s2_u2_exp9", "g4s2_u2_exp10", "g4s2_u2_exp11", "g4s2_u2_exp12") || it.expressionId in listOf("english_pep_2013_g4_s2_u2_exp7", "english_pep_2013_g4_s2_u2_exp8", "english_pep_2013_g4_s2_u2_exp9", "english_pep_2013_g4_s2_u2_exp10", "english_pep_2013_g4_s2_u2_exp11", "english_pep_2013_g4_s2_u2_exp12") }
                } else if (unit.unitId == "english_pep_2013_g4_s2_u3") {
                    unit.expressions.filter { it.expressionId in listOf("english_pep_2013_g4_s2_u3_exp7", "english_pep_2013_g4_s2_u3_exp8", "english_pep_2013_g4_s2_u3_exp9", "english_pep_2013_g4_s2_u3_exp10", "english_pep_2013_g4_s2_u3_exp11", "english_pep_2013_g4_s2_u3_exp12") }
                } else if (unit.unitId == "english_pep_2013_g4_s2_u1") {
                    unit.expressions.filter { it.expressionId in listOf("g4s2_u1_exp7", "g4s2_u1_exp8", "g4s2_u1_exp9", "g4s2_u1_exp10", "g4s2_u1_exp11", "g4s2_u1_exp12") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u1") {
                    unit.expressions.filter { it.expressionId in listOf("g3s2_u1_exp7", "g3s2_u1_exp8", "g3s2_u1_exp9", "g3s2_u1_exp10", "g3s2_u1_exp11", "g3s2_u1_exp12", "g3s2_u1_exp13", "g3s2_u1_exp14") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u2") {
                    unit.expressions.filter { it.expressionId in listOf("g3s2_u2_exp7", "g3s2_u2_exp8", "g3s2_u2_exp9", "g3s2_u2_exp10", "g3s2_u2_exp11", "g3s2_u2_exp12", "g3s2_u2_exp13", "g3s2_u2_exp14", "g3s2_u2_exp15") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u3") {
                    unit.expressions.filter { it.expressionId in listOf("g3s2_u3_exp7", "g3s2_u3_exp8", "g3s2_u3_exp9", "g3s2_u3_exp10", "g3s2_u3_exp11", "g3s2_u3_exp12", "g3s2_u3_exp13", "g3s2_u3_exp14") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u4") {
                    unit.expressions.filter { it.expressionId in listOf("g3s2_u4_exp8", "g3s2_u4_exp9", "g3s2_u4_exp10", "g3s2_u4_exp11", "g3s2_u4_exp12", "g3s2_u4_exp13", "g3s2_u4_exp14") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u5") {
                    unit.expressions.filter { it.expressionId in listOf("g3s2_u5_exp8", "g3s2_u5_exp9", "g3s2_u5_exp10", "g3s2_u5_exp11", "g3s2_u5_exp12") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u6") {
                    unit.expressions.filter { it.expressionId in listOf("g3s2_u6_exp9", "g3s2_u6_exp10", "g3s2_u6_exp11", "g3s2_u6_exp12", "g3s2_u6_exp13", "g3s2_u6_exp14", "g3s2_u6_exp15", "g3s2_u6_exp16") }
                } else if (unit.unitId.endsWith("_u6")) {
                    unit.expressions.filter { it.expressionId in listOf("g3s1_u6_exp1", "g3s1_u6_exp4", "g3s1_u6_exp5", "g3s1_u6_exp6", "g3s1_u6_exp7", "g3s1_u6_exp10", "g3s1_u6_exp12", "g3s1_u6_exp14", "g3s1_u6_exp15") }
                } else if (unit.unitId.endsWith("_u5")) {
                    unit.expressions.filter { it.expressionId in listOf("g3s1_u5_exp6", "g3s1_u5_exp7", "g3s1_u5_exp8") }
                } else if (unit.unitId.endsWith("_u4")) {
                    unit.expressions.filter { it.expressionId in listOf("g3s1_u4_exp6", "g3s1_u4_exp7", "g3s1_u4_exp8", "g3s1_u4_exp9") }
                } else if (unit.unitId.endsWith("_u2")) {
                    unit.expressions.filter { it.expressionId in listOf("g3s1_u2_exp6") }
                } else if (unit.unitId.endsWith("_u3")) {
                    unit.expressions.filter { it.expressionId in listOf("g3s1_u3_exp6", "g3s1_u3_exp7", "g3s1_u3_exp8", "g3s1_u3_exp9") }
                } else {
                    unit.expressions.drop(2)
                }
            }
            
            val currentExpression = expressions.getOrNull(currentExpressionIndex)
            if (currentExpression == null) {
                LaunchedEffect(Unit) {
                    showLessonSummary = true
                }
                return
            }
            
            LaunchedEffect(currentExpression.expressionId, currentStage) {
                if (currentStage == LearnStage.INTRO || currentStage == LearnStage.LISTEN_MEANING) {
                    ttsHelper.speak(currentExpression.englishText, isSlow = false)
                }
            }
            
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(text = getLessonTitle(unit.unitId, lessonType), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = unit.title, color = Color(0xFFEC4899), fontSize = 11.sp)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { currentLessonType = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A)),
                        actions = {
                            Text(
                                text = if (currentStage == LearnStage.SPATIAL_PRACTICE) "空间情境练习" else "句型 ${currentExpressionIndex + 1}/${expressions.size}",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                    )
                },
                bottomBar = {
                    EnglishLessonBottomBar(
                        enableImePadding = false,
                        hasPrevious = true,
                        previousEnabled = true,
                        nextEnabled = true,
                        nextText = when (currentStage) {
                            LearnStage.INTRO -> "下一步：跟读"
                            LearnStage.LISTEN_MEANING, LearnStage.READ_ALOUD -> "下一步：对比"
                            LearnStage.PLAYBACK -> "下一步：重组"
                            LearnStage.SPATIAL_PRACTICE -> "完成课时小结"
                            else -> if (currentExpressionIndex < expressions.size - 1) "下一句" else if (unit.unitId == "english_pep_2013_g3_s2_u4") "下一步：空间练习" else "完成本课"
                        },
                        onPrevious = {
                            if (currentStage == LearnStage.SPATIAL_PRACTICE) {
                                currentStage = LearnStage.SPELL
                            } else if (currentStage == LearnStage.SPELL || currentStage == LearnStage.WRITE || currentStage == LearnStage.DICTATION) {
                                currentStage = LearnStage.PLAYBACK
                            } else if (currentStage == LearnStage.PLAYBACK) {
                                currentStage = LearnStage.READ_ALOUD
                            } else if (currentStage == LearnStage.READ_ALOUD || currentStage == LearnStage.LISTEN_MEANING) {
                                currentStage = LearnStage.INTRO
                            } else if (currentStage == LearnStage.INTRO) {
                                if (currentExpressionIndex > 0) {
                                    currentExpressionIndex--
                                    currentStage = LearnStage.SPELL
                                } else {
                                    currentLessonType = null
                                }
                            }
                        },
                        onNext = {
                            if (currentStage == LearnStage.INTRO) {
                                currentStage = LearnStage.READ_ALOUD
                            } else if (currentStage == LearnStage.READ_ALOUD || currentStage == LearnStage.LISTEN_MEANING) {
                                currentStage = LearnStage.PLAYBACK
                            } else if (currentStage == LearnStage.PLAYBACK) {
                                currentStage = LearnStage.SPELL
                            } else if (currentStage == LearnStage.SPATIAL_PRACTICE) {
                                if (canCompleteLesson(unit.unitId, currentExpressionIndex, expressions.size, currentStage)) {
                                    showLessonSummary = true
                                } else {
                                    android.util.Log.e("EnglishLessonScreen", "PREMATURE_LESSON_COMPLETION_BLOCKED: index=$currentExpressionIndex stage=$currentStage")
                                }
                            } else {
                                if (currentExpressionIndex < expressions.size - 1) {
                                    currentExpressionIndex++
                                    currentStage = LearnStage.INTRO
                                } else {
                                    if (unit.unitId == "english_pep_2013_g3_s2_u4") {
                                        currentStage = LearnStage.SPATIAL_PRACTICE
                                    } else {
                                        if (canCompleteLesson(unit.unitId, currentExpressionIndex, expressions.size, currentStage)) {
                                            showLessonSummary = true
                                        } else {
                                            android.util.Log.e("EnglishLessonScreen", "PREMATURE_LESSON_COMPLETION_BLOCKED: index=$currentExpressionIndex stage=$currentStage")
                                        }
                                    }
                                }
                            }
                        }
                    )
                },
                containerColor = Color(0xFF0F172A)
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Stage dot tracker
                    val expressionStages = listOf(LearnStage.INTRO, LearnStage.READ_ALOUD, LearnStage.PLAYBACK, LearnStage.SPELL)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        expressionStages.forEach { stage ->
                            val effectiveStage = when (currentStage) {
                                LearnStage.LISTEN_MEANING -> LearnStage.READ_ALOUD
                                LearnStage.WRITE, LearnStage.DICTATION -> LearnStage.SPELL
                                else -> currentStage
                            }
                            val isActive = stage == effectiveStage
                            val stageIdx = expressionStages.indexOf(stage)
                            val curIdx = expressionStages.indexOf(effectiveStage)
                            val isPast = stageIdx < curIdx
                            val dotColor = when {
                                isActive -> Color(0xFFEC4899)
                                isPast -> Color(0xFF10B981)
                                else -> Color(0xFF475569)
                            }
                            val textColor = if (isActive) Color.White else Color.Gray

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(dotColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stage.title.take(2),
                                    color = textColor,
                                    fontSize = 10.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Central card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        border = BorderStroke(1.5.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (currentStage) {
                                LearnStage.INTRO -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("📖 句型学记", color = Color(0xFF94A3B8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = currentExpression.englishText,
                                            color = Color(0xFF00E5FF),
                                            fontSize = if (currentExpression.englishText.length > 15) 24.sp else 30.sp,
                                            lineHeight = if (currentExpression.englishText.length > 15) 34.sp else 40.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Serif,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = currentExpression.chineseTranslation,
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center
                                        )
                                        IconButton(
                                            onClick = { ttsHelper.speak(currentExpression.englishText) },
                                            modifier = Modifier.background(Color(0xFFEC4899).copy(alpha = 0.15f), CircleShape)
                                        ) {
                                            Icon(Icons.Filled.VolumeUp, contentDescription = "播放", tint = Color(0xFFEC4899))
                                        }
                                    }
                                }
                                LearnStage.LISTEN_MEANING, LearnStage.READ_ALOUD -> {
                                    // Handle recitation
                                    val dummyWordForRecite = EnglishWord(
                                        wordId = currentExpression.expressionId,
                                        spelling = currentExpression.englishText,
                                        displayText = currentExpression.englishText,
                                        chineseMeaning = currentExpression.chineseTranslation
                                    )
                                    ReadAloudView(dummyWordForRecite, recorderHelper) {
                                        currentStage = LearnStage.PLAYBACK
                                    }
                                }
                                LearnStage.PLAYBACK -> {
                                    val dummyWordForRecite = EnglishWord(
                                        wordId = currentExpression.expressionId,
                                        spelling = currentExpression.englishText,
                                        displayText = currentExpression.englishText,
                                        chineseMeaning = currentExpression.chineseTranslation
                                    )
                                    PlaybackView(dummyWordForRecite, ttsHelper, recorderHelper,
                                        onRestart = { currentStage = LearnStage.READ_ALOUD },
                                        onNext = { currentStage = LearnStage.SPELL }
                                    )
                                }
                                LearnStage.SPELL, LearnStage.WRITE, LearnStage.DICTATION -> {
                                    // Expression sentence reconstruction (Spell Stage)
                                    ExpressionReconstructionView(currentExpression) {
                                        // Move to next expression or complete lesson
                                        if (currentExpressionIndex < expressions.size - 1) {
                                            currentExpressionIndex++
                                            currentStage = LearnStage.INTRO
                                        } else {
                                            if (unit.unitId == "english_pep_2013_g3_s2_u4") {
                                                currentStage = LearnStage.SPATIAL_PRACTICE
                                            } else {
                                                if (canCompleteLesson(unit.unitId, currentExpressionIndex, expressions.size, currentStage)) {
                                                    showLessonSummary = true
                                                } else {
                                                    android.util.Log.e("EnglishLessonScreen", "PREMATURE_LESSON_COMPLETION_BLOCKED: index=$currentExpressionIndex stage=$currentStage")
                                                }
                                            }
                                        }
                                    }
                                }
                                LearnStage.SPATIAL_PRACTICE -> {
                                    EnglishToyRoomPlacementView(
                                        mode = if (lessonType == EnglishLessonType.LESSON1) "LESSON1" else "LESSON4",
                                        ttsHelper = ttsHelper,
                                        onInteractionStateChanged = { state ->
                                            // practice state updated
                                        },
                                        onBack = { currentLessonType = null }
                                    )
                                }
                            }
                        }
                    }
                    if (unit.unitId == "english_pep_2013_g4_s2_u2" && currentStage == LearnStage.INTRO) {
                        if (lessonType == EnglishLessonType.LESSON1) {
                            EnglishUnit2Lesson1InteractiveView(currentExpression = currentExpression, ttsHelper = ttsHelper)
                        } else if (lessonType == EnglishLessonType.LESSON4) {
                            EnglishUnit2Lesson4InteractiveView(currentExpression = currentExpression, ttsHelper = ttsHelper)
                        } else if (lessonType == EnglishLessonType.LESSON5) {
                            EnglishUnit2Lesson5InteractiveView(currentExpression = currentExpression, ttsHelper = ttsHelper)
                        }
                    }
                    if (unit.unitId == "english_pep_2013_g4_s2_u3" && currentStage == LearnStage.INTRO) {
                        if (lessonType == EnglishLessonType.LESSON1) {
                            EnglishUnit3Lesson1WeatherCard(currentExpression = currentExpression, ttsHelper = ttsHelper)
                        } else if (lessonType == EnglishLessonType.LESSON4) {
                            EnglishUnit3Lesson4WeatherCard(currentExpression = currentExpression, ttsHelper = ttsHelper)
                        }
                    }
                    if (unit.unitId == "english_pep_2013_g3_s1_u3" && currentStage == LearnStage.INTRO) {
                        EnglishBodyInteractiveView(currentExpression = currentExpression, ttsHelper = ttsHelper)
                    }
                    if (unit.unitId == "english_pep_2013_g4_s1_u3" && currentStage == LearnStage.INTRO && lessonType == EnglishLessonType.LESSON1) {
                        EnglishFriendExpressionCard(currentExpression = currentExpression, ttsHelper = ttsHelper)
                    }
                    if (unit.unitId == "english_pep_2013_g3_s2_u3" && currentStage == LearnStage.INTRO) {
                        if (lessonType == EnglishLessonType.LESSON1) {
                            EnglishZooObservationView(currentExpression = currentExpression, ttsHelper = ttsHelper)
                        } else {
                            EnglishZooMonsterBuilder(currentExpression = currentExpression, ttsHelper = ttsHelper)
                        }
                    }
                    if (unit.unitId == "english_pep_2013_g3_s1_u4" && currentStage == LearnStage.INTRO) {
                        EnglishAnimalInteractiveView(currentExpression = currentExpression, ttsHelper = ttsHelper)
                    }
                    if (unit.unitId == "english_pep_2013_g3_s2_u4" && currentStage == LearnStage.INTRO) {
                        EnglishToyRoomExpressionCard(
                            currentExpression = currentExpression,
                            ttsHelper = ttsHelper
                        )
                    }
                    if (unit.unitId == "english_pep_2013_g3_s1_u5" && currentStage == LearnStage.INTRO) {
                        val foodMode = if (lessonType == EnglishLessonType.LESSON1) {
                            FoodInteractiveMode.PICNIC_TRAY
                        } else {
                            FoodInteractiveMode.REQUEST_DELIVER
                        }
                        EnglishFoodInteractiveView(mode = foodMode, currentExpression = currentExpression, ttsHelper = ttsHelper)
                    }
                    if (unit.unitId == "english_pep_2013_g3_s1_u6" && currentStage == LearnStage.INTRO) {
                        val birthdayMode = if (lessonType == EnglishLessonType.LESSON1) {
                            BirthdayInteractiveMode.PLATES_CANDLES
                        } else {
                            BirthdayInteractiveMode.AGE_WISHES
                        }
                        EnglishBirthdayInteractiveView(mode = birthdayMode, currentExpression = currentExpression, ttsHelper = ttsHelper)
                    }
                    if (unit.unitId == "english_pep_2013_g3_s2_u2" && currentStage == LearnStage.INTRO) {
                        val albumMode = if (lessonType == EnglishLessonType.LESSON1) {
                            FamilyInteractiveMode.ALBUM_BROWSE
                        } else {
                            FamilyInteractiveMode.ALBUM_QUIZ
                        }
                        EnglishFamilyAlbumView(mode = albumMode, currentExpression = currentExpression, ttsHelper = ttsHelper)
                    }
                    if (unit.unitId == "english_pep_2013_g3_s2_u5" && currentStage == LearnStage.INTRO) {
                        val fruitMode = if (lessonType == EnglishLessonType.LESSON1) {
                            FruitMarketMode.MARKET_PREFERENCE
                        } else {
                            FruitMarketMode.PICNIC_SHARING
                        }
                        EnglishFruitMarketView(
                            mode = fruitMode,
                            currentExpression = currentExpression,
                            ttsHelper = ttsHelper
                        )
                    }
                    if (unit.unitId == "english_pep_2013_g4_s1_u3" && lessonType == EnglishLessonType.LESSON4 && currentStage == LearnStage.INTRO) {
                        EnglishFriendDetectiveView(
                            ttsHelper = ttsHelper,
                            onInteractionCompleted = {
                                currentStage = LearnStage.LISTEN_MEANING
                            }
                        )
                    }
                    if (unit.unitId == "english_pep_2013_g3_s2_u6" && currentStage == LearnStage.INTRO) {
                        EnglishQuantityExpressionCard(
                            currentExpression = currentExpression,
                            ttsHelper = ttsHelper
                        )
                    }
                }
            }
        } else {
            // Word-based lessons
            val filteredWords = when (lessonType) {
                EnglishLessonType.LESSON2 -> if (unit.unitId == "english_pep_2013_g4_s2_u3") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s2_u3_cold", "english_pep_2013_g4_s2_u3_cool", "english_pep_2013_g4_s2_u3_warm", "english_pep_2013_g4_s2_u3_hot") }
                } else if (unit.unitId == "english_pep_2013_g4_s2_u2") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s2_u2_breakfast", "english_pep_2013_g4_s2_u2_english_class", "english_pep_2013_g4_s2_u2_lunch", "english_pep_2013_g4_s2_u2_music_class", "english_pep_2013_g4_s2_u2_pe_class", "english_pep_2013_g4_s2_u2_dinner") }
                } else if (unit.unitId == "english_pep_2013_g4_s2_u1") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s2_u1_teachers_office", "english_pep_2013_g4_s2_u1_library", "english_pep_2013_g4_s2_u1_first_floor", "english_pep_2013_g4_s2_u1_second_floor") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u6") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s1_u6_parents", "english_pep_2013_g4_s1_u6_cousin", "english_pep_2013_g4_s1_u6_uncle", "english_pep_2013_g4_s1_u6_aunt", "english_pep_2013_g4_s1_u6_baby_brother") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u5") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s1_u5_beef", "english_pep_2013_g4_s1_u5_chicken", "english_pep_2013_g4_s1_u5_noodles", "english_pep_2013_g4_s1_u5_soup", "english_pep_2013_g4_s1_u5_vegetable") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u4") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s1_u4_bedroom", "english_pep_2013_g4_s1_u4_living_room", "english_pep_2013_g4_s1_u4_study", "english_pep_2013_g4_s1_u4_kitchen", "english_pep_2013_g4_s1_u4_bathroom") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u3") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s1_u3_strong", "english_pep_2013_g4_s1_u3_friendly", "english_pep_2013_g4_s1_u3_quiet") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u2") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s1_u2_schoolbag", "english_pep_2013_g4_s1_u2_maths_book", "english_pep_2013_g4_s1_u2_english_book", "english_pep_2013_g4_s1_u2_chinese_book", "english_pep_2013_g4_s1_u2_storybook") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u1") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s1_u1_classroom", "english_pep_2013_g4_s1_u1_window", "english_pep_2013_g4_s1_u1_blackboard", "english_pep_2013_g4_s1_u1_light", "english_pep_2013_g4_s1_u1_picture", "english_pep_2013_g4_s1_u1_door") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u1") {
                    unit.words.filter { it.wordId in listOf("g3s2_u1_uk", "g3s2_u1_canada", "g3s2_u1_usa", "g3s2_u1_china") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u2") {
                    unit.words.filter { it.wordId in listOf("g3s2_u2_father", "g3s2_u2_dad", "g3s2_u2_mother", "g3s2_u2_man", "g3s2_u2_woman") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u3") {
                    unit.words.filter { it.wordId in listOf("g3s2_u3_thin", "g3s2_u3_fat", "g3s2_u3_tall", "g3s2_u3_short", "g3s2_u3_giraffe", "g3s2_u3_so") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u4") {
                    unit.words.filter { it.wordId in listOf("g3s2_u4_on", "g3s2_u4_in", "g3s2_u4_under", "g3s2_u4_chair", "g3s2_u4_desk") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u5") {
                    unit.words.filter { it.wordId in listOf("g3s2_u5_pear", "g3s2_u5_apple", "g3s2_u5_orange", "g3s2_u5_banana") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u6") {
                    unit.words.filter { it.wordId in listOf("g3s2_u6_eleven", "g3s2_u6_twelve", "g3s2_u6_thirteen", "g3s2_u6_fourteen", "g3s2_u6_fifteen") }
                } else if (unit.unitId.endsWith("_u6")) {
                    unit.words.filter { it.wordId in listOf("g3s1_u6_one", "g3s1_u6_two", "g3s1_u6_three", "g3s1_u6_four", "g3s1_u6_five") }
                } else if (unit.unitId.endsWith("_u5")) {
                    unit.words.filter { it.wordId in listOf("g3s1_u5_bread", "g3s1_u5_juice", "g3s1_u5_egg", "g3s1_u5_milk") }
                } else if (unit.unitId.endsWith("_u4")) {
                    unit.words.filter { it.wordId in listOf("g3s1_u4_cat", "g3s1_u4_duck", "g3s1_u4_dog", "g3s1_u4_pig", "g3s1_u4_bear") }
                } else if (unit.unitId.endsWith("_u3")) {
                    unit.words.filter { it.wordId in listOf("g3s1_u3_face", "g3s1_u3_ear", "g3s1_u3_eye", "g3s1_u3_nose") }
                } else if (unit.unitId.endsWith("_u2")) {
                    unit.words.filter { it.wordId in listOf("g3s1_u2_red", "g3s1_u2_yellow", "g3s1_u2_green", "g3s1_u2_blue") }
                } else {
                    unit.words.filter { it.wordId in listOf("g3s1_u1_ruler", "g3s1_u1_pencil", "g3s1_u1_eraser", "g3s1_u1_crayon") }
                }
                EnglishLessonType.LESSON4 -> if (unit.unitId == "english_pep_2013_g4_s2_u3") {
                    emptyList() // LESSON4 is expression lesson for G4 S2 Unit 3
                } else if (unit.unitId == "english_pep_2013_g4_s1_u6") {
                    emptyList() // LESSON4 is expression lesson for G4 S1 Unit 6
                } else if (unit.unitId == "english_pep_2013_g4_s1_u5") {
                    emptyList() // LESSON4 is expression lesson for G4 S1 Unit 5
                } else if (unit.unitId == "english_pep_2013_g4_s1_u4") {
                    emptyList() // LESSON4 is expression lesson for G4 S1 Unit 4
                } else if (unit.unitId == "english_pep_2013_g4_s1_u3") {
                    emptyList() // LESSON4 is expression & detective lesson for G4 S1 Unit 3
                } else if (unit.unitId == "english_pep_2013_g4_s1_u2") {
                    emptyList() // LESSON4 is expression & lost and found lesson for G4 S1 Unit 2
                } else if (unit.unitId == "english_pep_2013_g4_s1_u1") {
                    emptyList()
                } else if (unit.unitId == "english_pep_2013_g3_s2_u1") {
                    emptyList() // LESSON4 is expression lesson for S2 Unit 1
                } else if (unit.unitId == "english_pep_2013_g3_s2_u2") {
                    emptyList() // LESSON4 is expression lesson for S2 Unit 2
                } else if (unit.unitId == "english_pep_2013_g3_s2_u3") {
                    emptyList() // LESSON4 is expression lesson for S2 Unit 3
                } else if (unit.unitId == "english_pep_2013_g3_s2_u4") {
                    emptyList() // LESSON4 is expression lesson for S2 Unit 4
                } else if (unit.unitId == "english_pep_2013_g3_s2_u5") {
                    emptyList() // LESSON4 is expression lesson for S2 Unit 5
                } else if (unit.unitId.endsWith("_u6")) {
                    emptyList() // LESSON4 is expression lesson for Unit 6
                } else if (unit.unitId.endsWith("_u5")) {
                    emptyList() // LESSON4 is expression lesson for Unit 5
                } else if (unit.unitId.endsWith("_u4")) {
                    unit.words.filter { it.wordId in listOf("g3s1_u4_bird", "g3s1_u4_panda", "g3s1_u4_tiger") }
                } else if (unit.unitId.endsWith("_u3")) {
                    unit.words.filter { it.wordId in listOf("g3s1_u3_mouth", "g3s1_u3_head", "g3s1_u3_hand", "g3s1_u3_arm") }
                } else if (unit.unitId == "english_pep_2013_g3_s1_u2") {
                    unit.words.filter { it.wordId in listOf("g3s1_u2_black", "g3s1_u2_white", "g3s1_u2_orange", "g3s1_u2_brown") }
                } else {
                    unit.words.filter { it.wordId in listOf("g3s1_u1_bag", "g3s1_u1_pen", "g3s1_u1_pencil_box", "g3s1_u1_book") }
                }
                EnglishLessonType.LESSON5 -> if (unit.unitId == "english_pep_2013_g4_s2_u3") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s2_u3_sunny", "english_pep_2013_g4_s2_u3_windy", "english_pep_2013_g4_s2_u3_cloudy", "english_pep_2013_g4_s2_u3_snowy", "english_pep_2013_g4_s2_u3_rainy", "english_pep_2013_g4_s2_u3_outside", "english_pep_2013_g4_s2_u3_be_careful", "english_pep_2013_g4_s2_u3_weather", "english_pep_2013_g4_s2_u3_new_york", "english_pep_2013_g4_s2_u3_how_about", "english_pep_2013_g4_s2_u3_degree", "english_pep_2013_g4_s2_u3_world", "english_pep_2013_g4_s2_u3_london", "english_pep_2013_g4_s2_u3_moscow", "english_pep_2013_g4_s2_u3_singapore", "english_pep_2013_g4_s2_u3_sydney", "english_pep_2013_g4_s2_u3_fly", "english_pep_2013_g4_s2_u3_love") }
                } else if (unit.unitId == "english_pep_2013_g4_s2_u2") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s2_u2_over", "english_pep_2013_g4_s2_u2_now", "english_pep_2013_g4_s2_u2_oclock", "english_pep_2013_g4_s2_u2_kid", "english_pep_2013_g4_s2_u2_thirty", "english_pep_2013_g4_s2_u2_hurry_up", "english_pep_2013_g4_s2_u2_come_on", "english_pep_2013_g4_s2_u2_just_a_minute") }
                } else if (unit.unitId == "english_pep_2013_g4_s2_u1") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s2_u1_playground", "english_pep_2013_g4_s2_u1_computer_room", "english_pep_2013_g4_s2_u1_art_room", "english_pep_2013_g4_s2_u1_music_room") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u6") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s1_u6_doctor", "english_pep_2013_g4_s1_u6_cook", "english_pep_2013_g4_s1_u6_driver", "english_pep_2013_g4_s1_u6_farmer", "english_pep_2013_g4_s1_u6_nurse", "english_pep_2013_g4_s1_u6_football_player", "english_pep_2013_g4_s1_u6_job") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u5") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s1_u5_chopsticks", "english_pep_2013_g4_s1_u5_bowl", "english_pep_2013_g4_s1_u5_fork", "english_pep_2013_g4_s1_u5_knife", "english_pep_2013_g4_s1_u5_spoon", "english_pep_2013_g4_s1_u5_pass", "english_pep_2013_g4_s1_u5_try") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u4") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s1_u4_bed", "english_pep_2013_g4_s1_u4_phone", "english_pep_2013_g4_s1_u4_table", "english_pep_2013_g4_s1_u4_sofa", "english_pep_2013_g4_s1_u4_fridge", "english_pep_2013_g4_s1_u4_find", "english_pep_2013_g4_s1_u4_them") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u3") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s1_u3_hair", "english_pep_2013_g4_s1_u3_shoe", "english_pep_2013_g4_s1_u3_glasses", "english_pep_2013_g4_s1_u3_hat", "english_pep_2013_g4_s1_u3_his", "english_pep_2013_g4_s1_u3_her") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u2") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s1_u2_candy", "english_pep_2013_g4_s1_u2_notebook", "english_pep_2013_g4_s1_u2_toy", "english_pep_2013_g4_s1_u2_key") }
                } else if (unit.unitId == "english_pep_2013_g4_s1_u1") {
                    unit.words.filter { it.wordId in listOf("english_pep_2013_g4_s1_u1_teachers_desk", "english_pep_2013_g4_s1_u1_computer", "english_pep_2013_g4_s1_u1_fan", "english_pep_2013_g4_s1_u1_wall", "english_pep_2013_g4_s1_u1_floor") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u1") {
                    unit.words.filter { it.wordId in listOf("g3s2_u1_she", "g3s2_u1_student", "g3s2_u1_pupil", "g3s2_u1_he", "g3s2_u1_teacher") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u2") {
                    unit.words.filter { it.wordId in listOf("g3s2_u2_grandfather", "g3s2_u2_grandpa", "g3s2_u2_grandmother", "g3s2_u2_grandma", "g3s2_u2_brother", "g3s2_u2_sister", "g3s2_u2_family") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u3") {
                    unit.words.filter { it.wordId in listOf("g3s2_u3_long", "g3s2_u3_small", "g3s2_u3_big", "g3s2_u3_tail", "g3s2_u3_children") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u4") {
                    unit.words.filter { it.wordId in listOf("g3s2_u4_cap", "g3s2_u4_ball", "g3s2_u4_car", "g3s2_u4_boat", "g3s2_u4_map") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u5") {
                    unit.words.filter { it.wordId in listOf("g3s2_u5_watermelon", "g3s2_u5_strawberry", "g3s2_u5_grape") }
                } else if (unit.unitId == "english_pep_2013_g3_s2_u6") {
                    unit.words.filter { it.wordId in listOf("g3s2_u6_sixteen", "g3s2_u6_seventeen", "g3s2_u6_eighteen", "g3s2_u6_nineteen", "g3s2_u6_twenty") }
                } else if (unit.unitId.endsWith("_u6")) {
                    unit.words.filter { it.wordId in listOf("g3s1_u6_six", "g3s1_u6_seven", "g3s1_u6_eight", "g3s1_u6_nine", "g3s1_u6_ten", "g3s1_u6_brother", "g3s1_u6_plate") }
                } else if (unit.unitId.endsWith("_u5")) {
                    unit.words.filter { it.wordId in listOf("g3s1_u5_fish", "g3s1_u5_rice", "g3s1_u5_water", "g3s1_u5_cake") }
                } else if (unit.unitId.endsWith("_u4")) {
                    unit.words.filter { it.wordId in listOf("g3s1_u4_elephant", "g3s1_u4_monkey") }
                } else if (unit.unitId.endsWith("_u3")) {
                    unit.words.filter { it.wordId in listOf("g3s1_u3_body", "g3s1_u3_leg") }
                } else if (unit.unitId.endsWith("_u2")) {
                    unit.words.filter { it.wordId in listOf("g3s1_u2_ok", "g3s1_u2_mum") }
                } else {
                    unit.words.filter { it.wordId in listOf("g3s1_u1_no", "g3s1_u1_your") }
                }
                EnglishLessonType.LESSON6 -> unit.words // Challenge contains all words!
                else -> emptyList()
            }
            
            val currentWord = filteredWords.getOrNull(currentWordIndex)
            if (currentWord == null) {
                LaunchedEffect(Unit) {
                    showLessonSummary = true
                }
                return
            }
            
            LaunchedEffect(currentWord.wordId, currentStage) {
                if (currentStage == LearnStage.INTRO || currentStage == LearnStage.LISTEN_MEANING || currentStage == LearnStage.DICTATION) {
                    ttsHelper.speak(currentWord.spelling, isSlow = false)
                }
            }
            
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(text = getLessonTitle(unit.unitId, lessonType), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = unit.title, color = Color(0xFFEC4899), fontSize = 11.sp)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { currentLessonType = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A)),
                        actions = {
                            Text(
                                text = "词汇 ${currentWordIndex + 1}/${filteredWords.size}",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                    )
                },
                bottomBar = {
                    EnglishLessonBottomBar(
                        enableImePadding = (currentStage == LearnStage.SPELL || currentStage == LearnStage.DICTATION),
                        hasPrevious = true,
                        previousEnabled = true,
                        nextEnabled = true,
                        nextText = when (currentStage) {
                            LearnStage.INTRO -> "认读完成"
                            LearnStage.LISTEN_MEANING -> "下一步: 跟读"
                            LearnStage.READ_ALOUD -> "跟读完成"
                            LearnStage.PLAYBACK -> "下一步: 拼写"
                            LearnStage.SPELL -> "拼写完成"
                            LearnStage.WRITE -> "手写完成"
                            LearnStage.DICTATION, LearnStage.SPATIAL_PRACTICE -> if (currentWordIndex < filteredWords.size - 1) "下一词" else "完成本课"
                        },
                        onPrevious = {
                            if (currentStage != LearnStage.INTRO) {
                                currentStage = LearnStage.values()[currentStage.ordinal - 1]
                            } else if (currentWordIndex > 0) {
                                currentWordIndex--
                                currentStage = LearnStage.DICTATION
                            } else {
                                currentLessonType = null
                            }
                        },
                        onNext = {
                            if (currentStage != LearnStage.DICTATION) {
                                currentStage = LearnStage.values()[currentStage.ordinal + 1]
                            } else {
                                if (currentWordIndex < filteredWords.size - 1) {
                                    currentWordIndex++
                                    currentStage = LearnStage.INTRO
                                } else {
                                    showLessonSummary = true
                                }
                            }
                        }
                    )
                },
                containerColor = Color(0xFF0F172A)
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Stage dot tracker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LearnStage.values().forEach { stage ->
                            val isActive = stage == currentStage
                            val isPast = stage.ordinal < currentStage.ordinal
                            val dotColor = when {
                                isActive -> Color(0xFFEC4899)
                                isPast -> Color(0xFF10B981)
                                else -> Color(0xFF475569)
                            }
                            val textColor = if (isActive) Color.White else Color.Gray
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(dotColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stage.title.take(2),
                                    color = textColor,
                                    fontSize = 10.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    
                    // Central Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        border = BorderStroke(1.5.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (currentStage) {
                                LearnStage.INTRO -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        IntroView(currentWord, ttsHelper)
                                        if (unit.unitId.endsWith("_u5") && !unit.unitId.contains("g3_s2") && lessonType == EnglishLessonType.LESSON5) {
                                            EnglishFoodInteractiveView(
                                                mode = FoodInteractiveMode.ACTION_MATCHER,
                                                currentWord = currentWord,
                                                ttsHelper = ttsHelper
                                            )
                                        }
                                        if (unit.unitId == "english_pep_2013_g3_s2_u5" && lessonType == EnglishLessonType.LESSON5) {
                                            EnglishFruitMarketView(
                                                mode = FruitMarketMode.BASKET_COMBO,
                                                currentWord = currentWord,
                                                ttsHelper = ttsHelper
                                            )
                                        }
                                    }
                                }
                                LearnStage.LISTEN_MEANING -> {
                                    ListenMeaningView(currentWord, unit.words, ttsHelper) {
                                        currentStage = LearnStage.READ_ALOUD
                                    }
                                }
                                LearnStage.READ_ALOUD -> {
                                    ReadAloudView(currentWord, recorderHelper) {
                                        currentStage = LearnStage.PLAYBACK
                                    }
                                }
                                LearnStage.PLAYBACK -> {
                                    PlaybackView(currentWord, ttsHelper, recorderHelper,
                                        onRestart = { currentStage = LearnStage.READ_ALOUD },
                                        onNext = { currentStage = LearnStage.SPELL }
                                    )
                                }
                                LearnStage.SPELL -> {
                                    SpellView(currentWord) {
                                        currentStage = LearnStage.WRITE
                                    }
                                }
                                LearnStage.WRITE -> {
                                    WriteView(
                                        word = currentWord,
                                        tts = ttsHelper,
                                        equippedBrushStyle = equippedBrushStyle,
                                        equippedBrushConfig = equippedBrushConfig
                                    ) {
                                        currentStage = LearnStage.DICTATION
                                    }
                                }
                                LearnStage.DICTATION, LearnStage.SPATIAL_PRACTICE -> {
                                    DictationView(currentWord, ttsHelper) { isCorrect ->
                                        if (currentWordIndex < filteredWords.size - 1) {
                                            currentWordIndex++
                                            currentStage = LearnStage.INTRO
                                        } else {
                                            showLessonSummary = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitOverviewScreen(
    unit: EnglishUnit,
    context: Context,
    onBack: () -> Unit,
    onStartLesson: (EnglishLessonType) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "${unit.title} 学习大厅", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "PEP 2013审定版 · 三年级起点", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("english_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 学习目标 Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎯 ${unit.title} 核心学习目标",
                        color = Color(0xFFEC4899),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val targets = if (unit.unitId == "english_pep_2013_g4_s2_u1") {
                        listOf(
                            "认识8个核心场所与方位词汇 (teachers' office, library, playground, computer room, art room, music room, first floor, second floor)",
                            "掌握5个拓展词汇 (next to, homework, class, forty, way)",
                            "学会使用 Where is the library? / It's on the first floor. 询问并指引场所方位",
                            "学会使用 Is this/that the teachers' office? / Yes, it is. / No, it isn't. 询问并确认学校场馆",
                            "学会使用 Do you have a library? / Yes, we do. 询问并陈述学校设施",
                            "学会使用 How many students are there in your class? / Forty students. 询问并陈述班级人数",
                            "掌握 -er 词尾发音规律及例词 (sister, computer, teacher, dinner, ruler, water, tiger)"
                        )
                    } else if (unit.unitId == "english_pep_2013_g4_s2_u2") {
                        listOf(
                            "掌握6个核心课程与餐食词汇 (breakfast, English class, lunch, music class, PE class, dinner)",
                            "掌握4个核心日常活动短语 (get up, go to school, go home, go to bed)",
                            "掌握8个拓展与情境词 (over, now, o'clock, kid, thirty, hurry up, come on, just a minute)",
                            "学会使用 What time is it? / It's ... o'clock. 询问并陈述具体时间",
                            "学会使用 It's time for... / It's time to... 区分表达该做某事了",
                            "掌握字母组合 ir / ur 的发音规律与例词 (girl, bird, nurse, hamburger)"
                        )
                    } else if (unit.unitId == "english_pep_2013_g4_s2_u3") {
                        listOf(
                            "掌握9个核心天气与温度形容词 (cold, cool, warm, hot, sunny, windy, cloudy, snowy, rainy)",
                            "掌握13个拓展与情境词汇 (outside, be careful, weather, New York, how about, degree, world, London, Moscow, Singapore, Sydney, fly, love)",
                            "学会使用 Can I go outside now? / Yes, you can. / No, you can't. 询问并做出许可",
                            "学会使用 What's the weather like in New York? / It's rainy. It's 26 degrees. 询问和表达具体地方的天气与温度",
                            "掌握字母组合 ar / al 的发音规律与例词 (arm, car, card, ball, tall, wall)"
                        )
                    } else if (unit.unitId == "english_pep_2013_g4_s1_u6") {
                        listOf(
                            "认识10个核心家庭成员与职业词汇 (parents, cousin, uncle, aunt, baby brother, doctor, cook, driver, farmer, nurse)",
                            "掌握7个拓展词汇 (people, but, little, puppy, football player, job, basketball)",
                            "学会使用 How many people are there in your family? 询问家庭人数并作答",
                            "学会使用 Is this your...? / Who's this? / This is my... 询问和介绍家庭成员身份",
                            "学会使用 What's your father's/mother's job? / He's/She's a... 询问和介绍职业",
                            "综合掌握 a-e, i-e, o-e, u-e 以及 e 长元音与短元音拼读规律"
                        )
                    } else if (unit.unitId == "english_pep_2013_g4_s1_u5") {
                        listOf(
                            "认识10个核心食物与餐具词汇 (beef, chicken, noodles, soup, vegetable, chopsticks, bowl, fork, knife, spoon)",
                            "掌握5个拓展词汇 (dinner, ready, help yourself, pass, try) 与短语用法",
                            "学会使用 What's for dinner? / What would you like? / I'd like some... 表达用餐意愿",
                            "学会使用 Dinner's ready! / Help yourself. / Would you like...? / Yes, please. / No, thanks. 热情招待与礼貌回应",
                            "学会使用 Pass me the..., please. 请求与递送餐具",
                            "掌握词尾 e 长元音 /iː/ 拼读规律及例词 (me, he, she, we)"
                        )
                    } else if (unit.unitId == "english_pep_2013_g4_s1_u4") {
                        listOf(
                            "认识10个核心房间与家具词汇 (bedroom, living room, study, kitchen, bathroom, bed, phone, table, sofa, fridge)",
                            "学会使用 Where is she? / Is she in...? / She's in the kitchen. 询问和回答单数对象位置",
                            "学会使用 Where are the keys? / Are they on/near...? / They're in... 询问和回答复数物品位置",
                            "学会区分单数与复数问答，并掌握 in / on / under / near 空间指示",
                            "掌握 u-e 长元音拼读规律及例词 (use, cute, excuse)"
                        )
                    } else if (unit.unitId == "english_pep_2013_g4_s1_u3") {
                        listOf(
                            "认识6个核心朋友性格与外貌词汇 (strong, friendly, quiet, hair, shoe, glasses)",
                            "学会使用 I have a new friend. / What's his/her name? / His/Her name is... 介绍朋友与询问姓名",
                            "学会使用 A boy or girl? / Who is he/she? / Is he...? / You're right. 进行人物辨认与确认",
                            "掌握 He/She is... 与 He/She has... 及 His/Her...is/are... 句型的确切语义分工",
                            "掌握 o-e 长元音拼读规律及例词 (nose, note, Coke, Mr Jones)"
                        )
                    } else if (unit.unitId == "english_pep_2013_g4_s1_u2") {
                        listOf(
                            "认识9个核心书包与物品词汇 (schoolbag, maths book, English book, Chinese book, storybook, candy, notebook, toy, key)",
                            "学会使用 What's in your schoolbag? / An English book, a maths book... 描述与回答书包内容",
                            "学会使用 I lost my schoolbag. / What colour is it? / What's in it? 进行失物招领",
                            "学会使用 Put your... in/on/under/near... 进行书包物品整理",
                            "掌握 i-e 长元音拼读规律及例词 (like, kite, five, nine, rice)"
                        )
                    } else if (unit.unitId == "english_pep_2013_g4_s1_u1") {
                        listOf(
                            "认识11个核心教室词汇 (classroom, window, blackboard, light, picture, door, teacher's desk, computer, fan, wall, floor)",
                            "学会使用 We have... / What's in the classroom? / Let's go and see! 描述与回答教室情况",
                            "学会使用 Where is it? / It's near the... 询问和表达具体位置",
                            "学会使用 Let's clean... / Let me clean... / Let me help you 进行教室整理与配合",
                            "掌握 a-e 长元音拼读规律及例词 (cake, face, name, make)"
                        )
                    } else if (unit.unitId == "english_pep_2013_g3_s2_u1") {
                        listOf(
                            "认识9个核心词汇 (UK, Canada, USA, China, she, student, pupil, he, teacher)",
                            "听懂并说出听说词汇 (boy, and, girl, new, friend, today)",
                            "学会问候与介绍 (Welcome back! / Nice to see you again. / Where are you from? / I'm from...)",
                            "掌握短元音 a /æ/ 发音规则与例词 (cat, bag, dad, hand)"
                        )
                    } else if (unit.unitId == "english_pep_2013_g3_s2_u2") {
                        listOf(
                            "掌握核心称谓与家庭成员词汇 (father, mother, man, woman, dad, mum, grandfather, grandmother, grandpa, grandma, brother, sister, family)",
                            "学会使用 Who's that man/woman/boy/girl? 询问他人身份",
                            "学会使用 He's/She's my... 介绍并确认家人身份",
                            "掌握字母 e 在闭音节中的短元音 /e/ 发音规则与例词 (ten, pen, leg, red)"
                        )
                    } else if (unit.unitId == "english_pep_2013_g3_s2_u3") {
                        listOf(
                            "掌握描述动物体型的核心形容词 (thin, fat, tall, short, long, small, big)",
                            "学会用 Look at... / It's... 描述动物的整体特征",
                            "学会用 It has... 描述动物的局部特征（如：It has a long neck. / It has small eyes.）",
                            "掌握字母 i 在闭音节中的短元音 /ɪ/ 发音规则与例词 (big, pig, six, milk)"
                        )
                    } else if (unit.unitId.endsWith("_u6")) {
                        listOf(
                            "掌握10个核心数字单词 (one 至 ten) 及 brother, plate 词汇",
                            "学会询问并回答生日场景下的物品数量 (How many plates? Five.)",
                            "学会询问年龄并表达生日情境祝福 (How old are you? I'm six. Happy birthday!)",
                            "掌握 Uu—Zz 字母的 Phonics 读音与标准手写规范"
                        )
                    } else if (unit.unitId.endsWith("_u5")) {
                        listOf(
                            "掌握8个核心食物与饮品词 (bread, juice, egg, milk, fish, rice, water, cake)",
                            "学会礼貌表达食物需求与动作口令 (I'd like some... / Eat some... / Drink some...)",
                            "学会礼貌索取并分发配送食物 (Can I have some...? / Here you are.)",
                            "掌握 Oo—Tt 字母的 Phonics 读音与标准手写规范"
                        )
                    } else if (unit.unitId.endsWith("_u3")) {
                        listOf(
                            "认识8个核心身体部位词 (face, ear, eye, nose, mouth, head, hand, arm)",
                            "听懂并说出单词 (body, leg)",
                            "学会用 This is my... 表达自己的身体部位",
                            "学会听懂并做出 Touch your... / Clap your hands 等身体动作指令"
                        )
                    } else if (unit.unitId.endsWith("_u2")) {
                        listOf(
                            "认识8个核心颜色词 (red, yellow, green, blue, black, white, orange, brown)",
                            "听懂并说出单词 (ok, mum)",
                            "学会问候与介绍他人 (Good morning. / Good afternoon. / This is... / Nice to meet you.)",
                            "学会描述看见的颜色与颜色指令 (I see... / Colour it...)"
                        )
                    } else {
                        listOf(
                            "认识8个核心文具词 (ruler, pencil, eraser, crayon, bag, pen, pencil box, book)",
                            "听懂并说出名词 (no, your)",
                            "学会熟练打招呼与自我介绍 (Hello, I'm Wu Binbin.)",
                            "学会礼貌询问他人姓名 (What's your name?)"
                        )
                    }
                    
                    targets.forEach { target ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("✨", color = Color(0xFF00E5FF), fontSize = 12.sp)
                            Text(text = target, color = Color.LightGray, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
            
            Text(
                text = "🛤️ 单元探险线路图",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // 6 Lessons List
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(EnglishLessonType.values().size) { index ->
                    val lessonType = EnglishLessonType.values()[index]
                    val isCompleted = EnglishProgressManager.isLessonCompleted(context, "${unit.unitId}_${lessonType.name}")
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isCompleted) Color(0xFF10B981) else Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = getLessonTitle(unit.unitId, lessonType),
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isCompleted) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF334155),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isCompleted) "已通关" else "待挑战",
                                            maxLines = 1,
                                            softWrap = false,
                                            color = if (isCompleted) Color(0xFF34D399) else Color.Gray,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                Text(
                                    text = getLessonDescription(unit.unitId, lessonType),
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Button(
                                onClick = { onStartLesson(lessonType) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCompleted) Color(0xFF1E293B) else Color(0xFFEC4899)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = if (isCompleted) BorderStroke(1.dp, Color(0xFF10B981)) else null,
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("start_lesson_${index + 1}")
                            ) {
                                Text(
                                    text = if (isCompleted) "再次练习" else "开始学习",
                                    color = if (isCompleted) Color(0xFF34D399) else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressionReconstructionView(
    expression: EnglishExpression,
    onCompleted: () -> Unit
) {
    val targetWords = remember(expression.expressionId) {
        expression.englishText
            .replace("?", "")
            .replace("!", "")
            .replace(",", "")
            .replace(".", "")
            .split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
    }
    
    // Track selected word indices in assembly order
    var selectedIndices by remember(expression.expressionId) { mutableStateOf<List<Int>>(emptyList()) }
    
    // Shuffled words with original index mapping, guaranteed shuffled order when words > 1
    val shuffledWords = remember(expression.expressionId) {
        val indexed = targetWords.mapIndexed { index, word -> index to word }
        if (indexed.size <= 1) {
            indexed
        } else {
            var shuffled = indexed.shuffled()
            var attempts = 0
            while (shuffled.map { it.second } == targetWords && attempts < 30) {
                shuffled = indexed.shuffled()
                attempts++
            }
            shuffled
        }
    }
    
    val assembledWords = selectedIndices.map { targetWords[it] }
    val isCorrect = assembledWords.size == targetWords.size && 
            assembledWords.map { it.lowercase() } == targetWords.map { it.lowercase() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("🧩 句型重组拼图", color = Color(0xFF94A3B8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        
        Box(
            modifier = Modifier
                .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(text = expression.chineseTranslation, color = Color(0xFF34D399), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        // Display panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = assembledWords.joinToString(" ").ifEmpty { "点击下方气泡进行句子重组" },
                color = if (assembledWords.isEmpty()) Color.Gray else Color(0xFF00E5FF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        
        // Word bubbles selection
        CenteredWrapRow(
            horizontalSpacing = 8.dp,
            verticalSpacing = 8.dp,
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            shuffledWords.forEach { (origIndex, word) ->
                val isSelected = origIndex in selectedIndices
                
                Button(
                    onClick = {
                        if (!isSelected) {
                            selectedIndices = selectedIndices + origIndex
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF334155) else Color(0xFFEC4899),
                        contentColor = if (isSelected) Color(0xFF64748B) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSelected
                ) {
                    Text(word, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo last word
            IconButton(
                onClick = {
                    if (selectedIndices.isNotEmpty()) {
                        selectedIndices = selectedIndices.dropLast(1)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(if (selectedIndices.isNotEmpty()) Color(0xFF475569) else Color(0xFF1E293B), CircleShape),
                enabled = selectedIndices.isNotEmpty()
            ) {
                Icon(Icons.Filled.Undo, contentDescription = "撤销", tint = if (selectedIndices.isNotEmpty()) Color.White else Color.Gray)
            }
            
            // Clear all
            IconButton(
                onClick = { selectedIndices = emptyList() },
                modifier = Modifier
                    .size(48.dp)
                    .background(if (selectedIndices.isNotEmpty()) Color(0xFF475569) else Color(0xFF1E293B), CircleShape),
                enabled = selectedIndices.isNotEmpty()
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "清空", tint = if (selectedIndices.isNotEmpty()) Color.White else Color.Gray)
            }
            
            if (isCorrect) {
                Button(
                    onClick = onCompleted,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重组正确！继续", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// FlowRow simplistic polyfill to avoid import errors on some SDKs
@Composable
fun FlowRowFallback_Unused(
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

fun getColorForWord(wordSpelling: String): Color? {
    return when (wordSpelling.lowercase().trim()) {
        "red" -> Color(0xFFEF4444)
        "yellow" -> Color(0xFFEAB308)
        "green" -> Color(0xFF10B981)
        "blue" -> Color(0xFF3B82F6)
        "black" -> Color(0xFF000000)
        "white" -> Color(0xFFFFFFFF)
        "orange" -> Color(0xFFF97316)
        "brown" -> Color(0xFF78350F)
        else -> null
    }
}

@Composable
fun IntroView(word: EnglishWord, tts: EnglishTTSHelper) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "📖 导学探索",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        val colorValue = getColorForWord(word.spelling)
        if (colorValue != null) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colorValue)
                    .border(2.5.dp, if (word.spelling.lowercase() == "black") Color(0xFF475569) else Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (word.spelling.lowercase() == "white") {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFE2E8F0)))
                }
            }
        }
        
        Text(
            text = word.spelling,
            color = Color(0xFF00E5FF),
            fontSize = if (word.spelling.length > 15) 24.sp else (if (word.spelling.length > 10) 30.sp else 40.sp),
            lineHeight = if (word.spelling.length > 15) 34.sp else (if (word.spelling.length > 10) 40.sp else 48.sp),
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = word.phonetic,
                color = Color(0xFFF472B6),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "(${word.partOfSpeech})",
                color = Color.LightGray,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic
            )
        }
        
        Text(
            text = "音节划分: ${word.syllables}  |  自然拼读: ${word.phonicsHint}",
            color = Color(0xFF64748B),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        
        Box(
            modifier = Modifier
                .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = word.chineseMeaning,
                color = Color(0xFF34D399),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Example Sentence Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = word.exampleSentence,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = word.exampleTranslation,
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                
                IconButton(
                    onClick = { tts.speak(word.exampleSentence, isSlow = false) },
                    modifier = Modifier.background(Color(0xFF3B82F6).copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = "播放例句", tint = Color(0xFF60A5FA))
                }
            }
        }
        
        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 12.dp).fillMaxWidth()
        ) {
            Button(
                onClick = { tts.speak(word.spelling, isSlow = false) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "发音")
                Spacer(modifier = Modifier.width(6.dp))
                Text("正常原声")
            }
            
            Button(
                onClick = { tts.speak(word.spelling, isSlow = true) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(Icons.Filled.SlowMotionVideo, contentDescription = "慢速")
                Spacer(modifier = Modifier.width(6.dp))
                Text("慢速跟读")
            }
        }
    }
}

@Composable
fun ListenMeaningView(
    word: EnglishWord,
    allWords: List<EnglishWord>,
    tts: EnglishTTSHelper,
    onCorrectCompleted: () -> Unit
) {
    var selectedOptionIndex by remember(word.wordId) { mutableStateOf<Int?>(null) }
    var isCorrectOptionSelected by remember(word.wordId) { mutableStateOf(false) }
    
    val options = remember(word.wordId) {
        val list = mutableListOf(word.chineseMeaning)
        val otherWords = allWords.filter { it.wordId != word.wordId }.shuffled()
        otherWords.take(3).forEach { list.add(it.chineseMeaning) }
        while (list.size < 4) {
            list.add("文具物品${list.size + 1}")
        }
        list.shuffled()
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "🔊 听音辨意",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(
            onClick = { tts.speak(word.spelling, isSlow = false) },
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFEC4899).copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(Icons.Filled.VolumeUp, contentDescription = "播放发音", tint = Color(0xFFEC4899), modifier = Modifier.size(36.dp))
        }
        
        Text("根据听到的读音，选择正确的中文含义", color = Color.LightGray, fontSize = 14.sp)
        
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = selectedOptionIndex == index
                val isOptionCorrect = option == word.chineseMeaning
                
                val btnBgColor = when {
                    isSelected && isOptionCorrect -> Color(0xFF10B981)
                    isSelected && !isOptionCorrect -> Color(0xFFEF4444)
                    isCorrectOptionSelected && isOptionCorrect -> Color(0xFF10B981).copy(alpha = 0.5f)
                    else -> Color(0xFF1E293B)
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isCorrectOptionSelected) {
                            selectedOptionIndex = index
                            if (isOptionCorrect) {
                                isCorrectOptionSelected = true
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = btnBgColor),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${'A' + index}.  $option",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (isSelected && isOptionCorrect) {
                            Icon(Icons.Filled.Check, contentDescription = "正确", tint = Color.White)
                        } else if (isSelected && !isOptionCorrect) {
                            Icon(Icons.Filled.Close, contentDescription = "错误", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReadAloudView(
    word: EnglishWord,
    recorder: AudioRecorderHelper,
    onCompleted: () -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val success = recorder.startRecording("read_word_${word.wordId}.mp4")
            if (success) {
                isRecording = true
            } else {
                Toast.makeText(context, "录音启动失败，请检查麦克风设备", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "未获得录音权限，无法开始录音", Toast.LENGTH_SHORT).show()
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "🎙️ 大声朗读",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = word.spelling,
            color = Color(0xFF00E5FF),
            fontSize = if (word.spelling.length > 15) 26.sp else (if (word.spelling.length > 10) 30.sp else 36.sp),
            lineHeight = if (word.spelling.length > 15) 36.sp else (if (word.spelling.length > 10) 42.sp else 48.sp),
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = word.phonetic,
            color = Color(0xFFF472B6),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text("点击红圈麦克风开始录音，读完后再次点击停止", color = Color.LightGray, fontSize = 13.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Microphone Pulsing animation
        val infiniteTransition = rememberInfiniteTransition()
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (isRecording) 1.25f else 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        IconButton(
            onClick = {
                if (!isRecording) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        val success = recorder.startRecording("read_word_${word.wordId}.mp4")
                        if (success) {
                            isRecording = true
                        } else {
                            Toast.makeText(context, "录音启动失败，请检查麦克风设备", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        launcher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                } else {
                    recorder.stopRecording()
                    isRecording = false
                    onCompleted()
                }
            },
            modifier = Modifier
                .size((80f * pulseScale).dp)
                .background(if (isRecording) Color(0xFFEF4444) else Color(0xFF334155), CircleShape)
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Filled.MicOff else Icons.Filled.Mic,
                contentDescription = "录音控制",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Text(
            text = if (isRecording) "正在录音中... [再次点击停止]" else "点击开始录音",
            color = if (isRecording) Color(0xFFEF4444) else Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PlaybackView(
    word: EnglishWord,
    tts: EnglishTTSHelper,
    recorder: AudioRecorderHelper,
    onRestart: () -> Unit,
    onNext: () -> Unit
) {
    var isPlayingSelf by remember(word.wordId) { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "🎧 录音回放对比",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = word.spelling,
            color = Color(0xFF00E5FF),
            fontSize = if (word.spelling.length > 15) 26.sp else (if (word.spelling.length > 10) 30.sp else 36.sp),
            lineHeight = if (word.spelling.length > 15) 36.sp else (if (word.spelling.length > 10) 42.sp else 48.sp),
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )
        
        Text("回听刚才的录音，和标准原音对比：", color = Color.LightGray, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { tts.speak(word.spelling, isSlow = false) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "标准原声")
                Spacer(modifier = Modifier.width(4.dp))
                Text("1. 标准原声")
            }
            
            Button(
                onClick = {
                    isPlayingSelf = true
                    recorder.startPlaying {
                        isPlayingSelf = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(if (isPlayingSelf) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = "我的录音")
                Spacer(modifier = Modifier.width(4.dp))
                Text("2. 我的发音")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onRestart,
            border = BorderStroke(1.dp, Color(0xFFEF4444)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "重录", tint = Color(0xFFEF4444))
            Spacer(modifier = Modifier.width(6.dp))
            Text("重新跟读", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SpellView(word: EnglishWord, onCompleted: () -> Unit) {
    val targetWord = word.spelling
    var clickedIndicesOrder by remember { mutableStateOf<List<Int>>(emptyList()) }
    var bubbleLetters by remember { mutableStateOf<List<Char>>(emptyList()) }
    
    // Spelling States
    var spellingState by remember { mutableStateOf(SpellingState.EDITING) }
    var retryCount by remember { mutableStateOf(0) }
    var hintUsed by remember { mutableStateOf(false) }
    var answerRevealed by remember { mutableStateOf(false) }

    fun resetWordPuzzle() {
        clickedIndicesOrder = emptyList()
        spellingState = SpellingState.EDITING
    }

    LaunchedEffect(word.wordId) {
        resetWordPuzzle()
        retryCount = 0
        hintUsed = false
        answerRevealed = false
        bubbleLetters = targetWord.filter { !it.isWhitespace() }.toList().shuffled()
    }

    // Reconstruct the currently assembled flat string
    val assembledLettersFlat = clickedIndicesOrder.map { bubbleLetters[it] }.joinToString("")
    
    // Format full assembled text inserting correct whitespaces
    fun getAssembledWithSpaces(): String {
        val sb = java.lang.StringBuilder()
        var flatIdx = 0
        for (char in targetWord) {
            if (char.isWhitespace()) {
                sb.append(' ')
            } else {
                if (flatIdx < assembledLettersFlat.length) {
                    sb.append(assembledLettersFlat[flatIdx])
                    flatIdx++
                } else {
                    break
                }
            }
        }
        return sb.toString()
    }

    val finalAssembledWord = getAssembledWithSpaces()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "🧩 拼写拼图",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        // Chinese Meaning Banner
        Box(
            modifier = Modifier
                .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Text(
                text = word.chineseMeaning,
                color = Color(0xFF34D399),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Assembled interactive slots area using unified EnglishWordAnswerBoard
        EnglishWordAnswerBoard(
            targetWord = targetWord,
            assembledLettersFlat = assembledLettersFlat,
            onSlotClick = { flatIndex ->
                if (flatIndex < clickedIndicesOrder.size) {
                    clickedIndicesOrder = clickedIndicesOrder.filterIndexed { idx, _ -> idx != flatIndex }
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Jumbled Letter Bubbles Pool (Flow Layout wraps without compression!)
        CenteredWrapRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalSpacing = 10.dp,
            verticalSpacing = 10.dp
        ) {
            bubbleLetters.forEachIndexed { index, char ->
                val isClicked = clickedIndicesOrder.contains(index)
                
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isClicked) Color(0xFF1E293B).copy(alpha = 0.4f) else Color(0xFFEC4899))
                        .border(
                            width = 1.dp,
                            color = if (isClicked) Color(0xFF334155) else Color(0xFFF472B6),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = !isClicked && spellingState != SpellingState.CORRECT) {
                            clickedIndicesOrder = clickedIndicesOrder + index
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char.toString(),
                        color = if (isClicked) Color(0xFF475569) else Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Panel (Delete/Check/Next)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { resetWordPuzzle() },
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFF475569), CircleShape),
                enabled = spellingState != SpellingState.CORRECT
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "清空", tint = Color.White)
            }

            IconButton(
                onClick = {
                    if (clickedIndicesOrder.isNotEmpty()) {
                        clickedIndicesOrder = clickedIndicesOrder.dropLast(1)
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFF475569), CircleShape),
                enabled = spellingState != SpellingState.CORRECT
            ) {
                Icon(Icons.Filled.Undo, contentDescription = "撤销一步", tint = Color.White)
            }

            if (spellingState == SpellingState.EDITING || spellingState == SpellingState.INCORRECT_FIRST || spellingState == SpellingState.INCORRECT_REVEALED) {
                Button(
                    onClick = {
                        val userNorm = finalAssembledWord.trim().lowercase().replace("\\s+".toRegex(), " ")
                        val targetNorm = targetWord.trim().lowercase().replace("\\s+".toRegex(), " ")
                        if (userNorm == targetNorm) {
                            spellingState = SpellingState.CORRECT
                        } else {
                            retryCount++
                            spellingState = if (retryCount == 1) {
                                SpellingState.INCORRECT_FIRST
                            } else {
                                answerRevealed = true
                                SpellingState.INCORRECT_REVEALED
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp),
                    enabled = clickedIndicesOrder.size == bubbleLetters.size
                ) {
                    Text("检查答案", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else if (spellingState == SpellingState.CORRECT) {
                Button(
                    onClick = onCompleted,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("继续", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // Assistance Panel Trigger
        if (spellingState == SpellingState.INCORRECT_FIRST || spellingState == SpellingState.INCORRECT_REVEALED) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // user clicks to see details
                    }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (spellingState == SpellingState.INCORRECT_FIRST) "❌ 拼写有误。点击查看提示" else "❌ 拼写有误。点击查看正确对比",
                        color = Color(0xFFF87171),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Filled.Help, contentDescription = "提示", tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    // Modal/Overlays
    if (spellingState == SpellingState.INCORRECT_FIRST) {
        AlertDialog(
            onDismissRequest = { spellingState = SpellingState.EDITING },
            containerColor = Color(0xFF1E293B),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Warning, contentDescription = "注意", tint = Color(0xFFF59E0B))
                    Text("拼写还差一点点！", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("输入答案: $finalAssembledWord", color = Color.LightGray, fontSize = 14.sp)
                    Text("🔊 发音提示: ${word.phonetic}", color = Color(0xFFF472B6), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    
                    if (word.syllables.isNotEmpty()) {
                        Text("🧩 音节分段: ${word.syllables}", color = Color(0xFF00E5FF), fontSize = 14.sp)
                    }
                    Text("📐 字母数量: 本词一共有 ${targetWord.length} 个字符", color = Color.LightGray, fontSize = 13.sp)
                    hintUsed = true
                }
            },
            confirmButton = {
                Button(
                    onClick = { spellingState = SpellingState.EDITING },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                ) {
                    Text("再试一次", color = Color.White)
                }
            }
        )
    } else if (spellingState == SpellingState.INCORRECT_REVEALED) {
        AlertDialog(
            onDismissRequest = { /* Must correct to continue */ },
            containerColor = Color(0xFF1E293B),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Cancel, contentDescription = "错误", tint = Color(0xFFEF4444))
                    Text("正确答案对照", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("你输入的答案与正确拼写位置对齐:", color = Color.LightGray, fontSize = 13.sp)
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in targetWord.indices) {
                            val userChar = finalAssembledWord.getOrNull(i)
                            val targetChar = targetWord[i]
                            val itemBg = when {
                                userChar == null -> Color(0xFF475569) // missing
                                userChar.lowercaseChar() == targetChar.lowercaseChar() -> Color(0xFF10B981) // correct
                                else -> Color(0xFFEF4444) // incorrect
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(itemBg, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                    Text(
                                        text = targetChar.toString(),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                            }
                        }
                    }

                    Divider(color = Color(0xFF334155))
                    Text("正确答案: $targetWord", color = Color(0xFF10B981), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("中文释义: ${word.chineseMeaning}", color = Color.LightGray, fontSize = 14.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        resetWordPuzzle()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("看着答案重新拼写一遍", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun WriteView(
    word: EnglishWord,
    tts: EnglishTTSHelper,
    equippedBrushStyle: com.example.ui.BrushStyle = com.example.ui.BrushStyle.ALL_BRUSHES[0],
    equippedBrushConfig: com.example.data.PlayerBrushConfig? = null,
    onCompleted: () -> Unit
) {
    var handwritingViewRef by remember { mutableStateOf<EnglishHandwritingView?>(null) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "✍️ 笔迹手写 (四线三格)",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = word.spelling,
                color = Color(0xFF00E5FF),
                fontSize = if (word.spelling.length > 15) 22.sp else 28.sp,
                lineHeight = if (word.spelling.length > 15) 30.sp else 38.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { tts.speak(word.spelling, isSlow = false) },
                modifier = Modifier.background(Color(0xFF3B82F6).copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "播放发音", tint = Color(0xFF60A5FA))
            }
        }
        
        Text(
            text = "${word.phonetic}  |  ${word.chineseMeaning}",
            color = Color.LightGray,
            fontSize = 14.sp
        )

        // Equipped Brush Tag
        Box(
            modifier = Modifier
                .background(Color(0xFF3B82F6).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "🖌️ 当前笔刷: ${equippedBrushStyle.brushName}",
                color = Color(0xFF60A5FA),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 四线三格手写板 Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF12161A))
        ) {
            AndroidView(
                factory = { ctx ->
                    EnglishHandwritingView(ctx).apply {
                        currentBrush = equippedBrushStyle
                        currentBrushConfig = equippedBrushConfig
                        handwritingViewRef = this
                    }
                },
                update = { view ->
                    view.currentBrush = equippedBrushStyle
                    view.currentBrushConfig = equippedBrushConfig
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { handwritingViewRef?.undo() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Undo, contentDescription = "撤销", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("撤销一步")
            }
            
            OutlinedButton(
                onClick = { handwritingViewRef?.clear() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "清空", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("清空重写")
            }
        }
    }
}

@Composable
fun DictationView(
    word: EnglishWord,
    tts: EnglishTTSHelper,
    onChecked: (Boolean) -> Unit
) {
    var canvasKey by remember(word.wordId) { mutableStateOf(0) }
    var isAnswerRevealed by remember(word.wordId) { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "🎧 听写自查",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = { tts.speak(word.spelling, isSlow = false) },
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFF3B82F6).copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "播放发音", tint = Color(0xFF60A5FA), modifier = Modifier.size(28.dp))
            }
            Text("点击听写发音，在四线三格中写下拼写", color = Color.LightGray, fontSize = 13.sp)
        }
        
        // 四线三格手写板 Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF12161A))
        ) {
            key(canvasKey) {
                AndroidView(
                    factory = { ctx ->
                        EnglishHandwritingView(ctx)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            if (!isAnswerRevealed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = "❓",
                        fontSize = 40.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        
        if (!isAnswerRevealed) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { canvasKey++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "重写")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清除手写")
                }
                
                Button(
                    onClick = { isAnswerRevealed = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("english_check_answer_button")
                ) {
                    Text("检查答案", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Answer Reveal and Comparison Block
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.2.dp, Color(0xFFEC4899)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("比对标准答案：", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        text = word.spelling,
                        color = Color(0xFF00E5FF),
                        fontSize = if (word.spelling.length > 15) 22.sp else 28.sp,
                        lineHeight = if (word.spelling.length > 15) 30.sp else 38.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "中文释义：${word.chineseMeaning}",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        isAnswerRevealed = false
                        canvasKey++
                        onChecked(false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("需要练习", color = Color.White)
                }
                
                Button(
                    onClick = {
                        isAnswerRevealed = false
                        canvasKey++
                        onChecked(true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("english_got_it_right_button")
                ) {
                    Text("我写对了！", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}





@Composable
private fun CenteredWrapRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val horizontalSpacingPx = horizontalSpacing.roundToPx()
        val verticalSpacingPx = verticalSpacing.roundToPx()

        val maxAvailableWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else 1000000

        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val rowWidths = mutableListOf<Int>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentWidth = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            if (currentWidth + placeable.width > maxAvailableWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                rowWidths.add(currentWidth - horizontalSpacingPx)
                currentRow = mutableListOf(placeable)
                currentWidth = placeable.width + horizontalSpacingPx
            } else {
                currentRow.add(placeable)
                currentWidth += placeable.width + horizontalSpacingPx
            }
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentWidth - horizontalSpacingPx)
        }

        val totalHeight = rows.sumOf { row -> row.maxOfOrNull { it.height } ?: 0 } +
                (rows.size - 1).coerceAtLeast(0) * verticalSpacingPx

        val maxRowWidth = rowWidths.maxOrNull() ?: 0
        val finalWidth = maxRowWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
        val finalHeight = totalHeight.coerceIn(constraints.minHeight, constraints.maxHeight)

        layout(finalWidth, finalHeight) {
            var y = 0
            rows.forEachIndexed { index, row ->
                val rowHeight = row.maxOfOrNull { it.height } ?: 0
                var x = (finalWidth - rowWidths[index]) / 2
                row.forEach { placeable ->
                    placeable.placeRelative(x, y + (rowHeight - placeable.height) / 2)
                    x += placeable.width + horizontalSpacingPx
                }
                y += rowHeight + verticalSpacingPx
            }
        }
    }
}

@Composable
fun EnglishAnimalInteractiveView(
    currentExpression: EnglishExpression,
    ttsHelper: EnglishTTSHelper
) {
    var currentHintStage by remember { mutableStateOf(0) } // 0 = fully shadowed, 1 = partial outline details, 2 = fully colored reveal!
    
    val expText = currentExpression.englishText.lowercase()
    val activeAnimal = remember(expText) {
        when {
            expText.contains("cat") -> "cat"
            expText.contains("duck") -> "duck"
            expText.contains("dog") -> "dog"
            expText.contains("pig") -> "pig"
            expText.contains("bear") -> "bear"
            expText.contains("bird") -> "bird"
            expText.contains("panda") -> "panda"
            expText.contains("tiger") -> "tiger"
            expText.contains("elephant") -> "elephant"
            expText.contains("monkey") -> "monkey"
            else -> "cat"
        }
    }

    // Reset when expression changes
    LaunchedEffect(currentExpression.expressionId) {
        currentHintStage = 0
    }

    // Check distance based on "this" or "that"
    val isNear = remember(expText) { expText.contains("this") || !expText.contains("that") }

    // Glow and movement animations for action imitation
    val infiniteTransition = rememberInfiniteTransition(label = "animal_anim")
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )
    
    val isActCommand = expText.contains("act like")
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isActCommand) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B0F19), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.5.dp, Color(0xFF1E293B)), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "🛸 动物全息扫描舱 (Animal Radar)",
            color = Color(0xFF00E5FF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.testTag("animal_radar_title")
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color(0xFF0F172A).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                .clickable {
                    // Tap to play sound & toggle reveal
                    ttsHelper.speak(activeAnimal, isSlow = false)
                    if (currentHintStage < 2) {
                        currentHintStage++
                    } else {
                        currentHintStage = 0
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val cx = maxWidth / 2
            val cy = maxHeight / 2
            val density = androidx.compose.ui.platform.LocalDensity.current
            
            // Render beautiful interactive HUD
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().testTag("animal_canvas")) {
                val scale = size.height / 240f
                val sizeFactor = if (isNear) 1.0f else 0.45f
                val baseAlpha = if (isNear) 1.0f else 0.6f
                
                // Concentric background scanning circles
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.04f * baseAlpha),
                    radius = 80f * scale * sizeFactor,
                    center = Offset(size.width / 2f, size.height / 2f)
                )
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.02f * baseAlpha),
                    radius = 110f * scale * sizeFactor,
                    center = Offset(size.width / 2f, size.height / 2f)
                )

                // Distance indicator lines / grids
                drawLine(
                    color = Color(0xFF334155).copy(alpha = 0.3f),
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Draw Animal vector graphics based on activeAnimal
                val animalColor = when (currentHintStage) {
                    0 -> Color(0xFF1E293B) // Pitch black silhouette
                    1 -> Color(0xFF38BDF8).copy(alpha = 0.8f) // Translucent outline hologram
                    else -> when (activeAnimal) { // Full gorgeous colored illustration
                        "cat" -> Color(0xFFFBBF24)
                        "duck" -> Color(0xFFFCD34D)
                        "dog" -> Color(0xFFF97316)
                        "pig" -> Color(0xFFF472B6)
                        "bear" -> Color(0xFF78350F)
                        "bird" -> Color(0xFF60A5FA)
                        "panda" -> Color.White
                        "tiger" -> Color(0xFFF97316)
                        "elephant" -> Color(0xFF94A3B8)
                        "monkey" -> Color(0xFFB45309)
                        else -> Color(0xFF10B981)
                    }
                }

                // Draw shapes with animations
                val centerOffset = if (isActCommand) Offset(size.width / 2f + shakeOffset, size.height / 2f) else Offset(size.width / 2f, size.height / 2f)
                val drawingRadius = 55f * scale * sizeFactor * scaleFactor
                
                // Draw specialized stylized icons using geometry
                when (activeAnimal) {
                    "cat" -> {
                        // Body
                        drawCircle(color = animalColor, radius = drawingRadius * 0.9f, center = centerOffset)
                        // Ears
                        val earPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(centerOffset.x - drawingRadius * 0.7f, centerOffset.y - drawingRadius * 0.5f)
                            lineTo(centerOffset.x - drawingRadius * 0.8f, centerOffset.y - drawingRadius * 1.1f)
                            lineTo(centerOffset.x - drawingRadius * 0.2f, centerOffset.y - drawingRadius * 0.7f)
                            close()
                            moveTo(centerOffset.x + drawingRadius * 0.7f, centerOffset.y - drawingRadius * 0.5f)
                            lineTo(centerOffset.x + drawingRadius * 0.8f, centerOffset.y - drawingRadius * 1.1f)
                            lineTo(centerOffset.x + drawingRadius * 0.2f, centerOffset.y - drawingRadius * 0.7f)
                            close()
                        }
                        drawPath(path = earPath, color = animalColor)
                        // Inner Details (Eyes/Whiskers) if revealed
                        if (currentHintStage == 2) {
                            drawCircle(color = Color.Black, radius = 5f * scale, center = Offset(centerOffset.x - 15f * scale, centerOffset.y - 10f * scale))
                            drawCircle(color = Color.Black, radius = 5f * scale, center = Offset(centerOffset.x + 15f * scale, centerOffset.y - 10f * scale))
                            // Nose
                            drawCircle(color = Color(0xFFEF4444), radius = 3.5f * scale, center = Offset(centerOffset.x, centerOffset.y + 2f * scale))
                        }
                    }
                    "duck" -> {
                        // Head & Body & Bill
                        drawCircle(color = animalColor, radius = drawingRadius * 0.8f, center = centerOffset)
                        // Bill
                        val billColor = if (currentHintStage == 2) Color(0xFFF97316) else animalColor
                        drawRoundRect(
                            color = billColor,
                            topLeft = Offset(centerOffset.x - drawingRadius * 1.1f, centerOffset.y - drawingRadius * 0.2f),
                            size = Size(drawingRadius * 0.6f, drawingRadius * 0.35f),
                            cornerRadius = CornerRadius(10f * scale, 10f * scale)
                        )
                        if (currentHintStage == 2) {
                            drawCircle(color = Color.Black, radius = 4f * scale, center = Offset(centerOffset.x - 10f * scale, centerOffset.y - 15f * scale))
                        }
                    }
                    "dog" -> {
                        // Head
                        drawCircle(color = animalColor, radius = drawingRadius * 0.85f, center = centerOffset)
                        // Droopy ears
                        drawRoundRect(
                            color = animalColor,
                            topLeft = Offset(centerOffset.x - drawingRadius * 1.0f, centerOffset.y - drawingRadius * 0.3f),
                            size = Size(drawingRadius * 0.3f, drawingRadius * 0.8f),
                            cornerRadius = CornerRadius(8f * scale, 8f * scale)
                        )
                        drawRoundRect(
                            color = animalColor,
                            topLeft = Offset(centerOffset.x + drawingRadius * 0.7f, centerOffset.y - drawingRadius * 0.3f),
                            size = Size(drawingRadius * 0.3f, drawingRadius * 0.8f),
                            cornerRadius = CornerRadius(8f * scale, 8f * scale)
                        )
                        if (currentHintStage == 2) {
                            drawCircle(color = Color.Black, radius = 4.5f * scale, center = Offset(centerOffset.x - 15f * scale, centerOffset.y - 10f * scale))
                            drawCircle(color = Color.Black, radius = 4.5f * scale, center = Offset(centerOffset.x + 15f * scale, centerOffset.y - 10f * scale))
                            drawCircle(color = Color.Black, radius = 7f * scale, center = Offset(centerOffset.x, centerOffset.y + 10f * scale))
                        }
                    }
                    "pig" -> {
                        // Big round snout and ears
                        drawCircle(color = animalColor, radius = drawingRadius * 0.95f, center = centerOffset)
                        // Snout
                        val snoutColor = if (currentHintStage == 2) Color(0xFFF472B6).copy(alpha = 0.8f) else animalColor
                        drawOval(
                            color = snoutColor,
                            topLeft = Offset(centerOffset.x - drawingRadius * 0.35f, centerOffset.y - drawingRadius * 0.15f),
                            size = Size(drawingRadius * 0.7f, drawingRadius * 0.45f)
                        )
                        if (currentHintStage == 2) {
                            // nostrils
                            drawCircle(color = Color.DarkGray, radius = 3f * scale, center = Offset(centerOffset.x - 8f * scale, centerOffset.y + 5f * scale))
                            drawCircle(color = Color.DarkGray, radius = 3f * scale, center = Offset(centerOffset.x + 8f * scale, centerOffset.y + 5f * scale))
                            // eyes
                            drawCircle(color = Color.Black, radius = 4f * scale, center = Offset(centerOffset.x - 20f * scale, centerOffset.y - 15f * scale))
                            drawCircle(color = Color.Black, radius = 4f * scale, center = Offset(centerOffset.x + 20f * scale, centerOffset.y - 15f * scale))
                        }
                    }
                    "bear" -> {
                        // Huge fuzzy circle
                        drawCircle(color = animalColor, radius = drawingRadius * 1.0f, center = centerOffset)
                        // Bear ears
                        drawCircle(color = animalColor, radius = drawingRadius * 0.3f, center = Offset(centerOffset.x - drawingRadius * 0.7f, centerOffset.y - drawingRadius * 0.7f))
                        drawCircle(color = animalColor, radius = drawingRadius * 0.3f, center = Offset(centerOffset.x + drawingRadius * 0.7f, centerOffset.y - drawingRadius * 0.7f))
                        if (currentHintStage == 2) {
                            drawCircle(color = Color.Black, radius = 5f * scale, center = Offset(centerOffset.x - 18f * scale, centerOffset.y - 10f * scale))
                            drawCircle(color = Color.Black, radius = 5f * scale, center = Offset(centerOffset.x + 18f * scale, centerOffset.y - 10f * scale))
                            // snout
                            drawCircle(color = Color(0xFFFEF08A), radius = 16f * scale, center = Offset(centerOffset.x, centerOffset.y + 15f * scale))
                            drawCircle(color = Color.Black, radius = 6f * scale, center = Offset(centerOffset.x, centerOffset.y + 10f * scale))
                        }
                    }
                    "bird" -> {
                        // Body & wings
                        drawCircle(color = animalColor, radius = drawingRadius * 0.7f, center = centerOffset)
                        // beak
                        val beakColor = if (currentHintStage == 2) Color(0xFFFBBF24) else animalColor
                        val beakPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(centerOffset.x - drawingRadius * 0.6f, centerOffset.y - drawingRadius * 0.1f)
                            lineTo(centerOffset.x - drawingRadius * 1.1f, centerOffset.y + drawingRadius * 0.1f)
                            lineTo(centerOffset.x - drawingRadius * 0.6f, centerOffset.y + drawingRadius * 0.3f)
                            close()
                        }
                        drawPath(path = beakPath, color = beakColor)
                        // Flapping wings
                        val wingPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(centerOffset.x, centerOffset.y)
                            lineTo(centerOffset.x + drawingRadius * 0.9f, centerOffset.y - drawingRadius * 0.6f * scaleFactor)
                            lineTo(centerOffset.x + drawingRadius * 0.4f, centerOffset.y + drawingRadius * 0.3f)
                            close()
                        }
                        drawPath(path = wingPath, color = animalColor)
                        if (currentHintStage == 2) {
                            drawCircle(color = Color.Black, radius = 4f * scale, center = Offset(centerOffset.x - 10f * scale, centerOffset.y - 10f * scale))
                        }
                    }
                    "panda" -> {
                        // Panda head with black ears and black eye patches
                        val pandaBaseColor = if (currentHintStage == 2) Color.White else animalColor
                        val pandaAccentColor = if (currentHintStage == 2) Color.Black else animalColor
                        
                        drawCircle(color = pandaBaseColor, radius = drawingRadius * 0.9f, center = centerOffset)
                        // ears
                        drawCircle(color = pandaAccentColor, radius = drawingRadius * 0.28f, center = Offset(centerOffset.x - drawingRadius * 0.65f, centerOffset.y - drawingRadius * 0.65f))
                        drawCircle(color = pandaAccentColor, radius = drawingRadius * 0.28f, center = Offset(centerOffset.x + drawingRadius * 0.65f, centerOffset.y - drawingRadius * 0.65f))
                        
                        if (currentHintStage == 2) {
                            // eye patches
                            drawOval(color = Color.Black, topLeft = Offset(centerOffset.x - 24f * scale, centerOffset.y - 14f * scale), size = Size(14f * scale, 20f * scale))
                            drawOval(color = Color.Black, topLeft = Offset(centerOffset.x + 10f * scale, centerOffset.y - 14f * scale), size = Size(14f * scale, 20f * scale))
                            // pupils
                            drawCircle(color = Color.White, radius = 3f * scale, center = Offset(centerOffset.x - 17f * scale, centerOffset.y - 4f * scale))
                            drawCircle(color = Color.White, radius = 3f * scale, center = Offset(centerOffset.x + 17f * scale, centerOffset.y - 4f * scale))
                            // nose
                            drawCircle(color = Color.Black, radius = 5f * scale, center = Offset(centerOffset.x, centerOffset.y + 10f * scale))
                        }
                    }
                    "tiger" -> {
                        // Orange head with black stripes
                        drawCircle(color = animalColor, radius = drawingRadius * 0.9f, center = centerOffset)
                        // Ears
                        drawCircle(color = animalColor, radius = drawingRadius * 0.25f, center = Offset(centerOffset.x - drawingRadius * 0.6f, centerOffset.y - drawingRadius * 0.6f))
                        drawCircle(color = animalColor, radius = drawingRadius * 0.25f, center = Offset(centerOffset.x + drawingRadius * 0.6f, centerOffset.y - drawingRadius * 0.6f))
                        
                        if (currentHintStage == 2) {
                            // Stripes "王" on forehead
                            drawLine(color = Color.Black, start = Offset(centerOffset.x - 12f * scale, centerOffset.y - 30f * scale), end = Offset(centerOffset.x + 12f * scale, centerOffset.y - 30f * scale), strokeWidth = 3f * scale)
                            drawLine(color = Color.Black, start = Offset(centerOffset.x - 8f * scale, centerOffset.y - 22f * scale), end = Offset(centerOffset.x + 8f * scale, centerOffset.y - 22f * scale), strokeWidth = 3f * scale)
                            drawLine(color = Color.Black, start = Offset(centerOffset.x - 15f * scale, centerOffset.y - 14f * scale), end = Offset(centerOffset.x + 15f * scale, centerOffset.y - 14f * scale), strokeWidth = 3f * scale)
                            drawLine(color = Color.Black, start = Offset(centerOffset.x, centerOffset.y - 34f * scale), end = Offset(centerOffset.x, centerOffset.y - 14f * scale), strokeWidth = 3f * scale)
                            
                            // Side stripes
                            drawLine(color = Color.Black, start = Offset(centerOffset.x - 36f * scale, centerOffset.y), end = Offset(centerOffset.x - 20f * scale, centerOffset.y), strokeWidth = 2.5f * scale)
                            drawLine(color = Color.Black, start = Offset(centerOffset.x + 20f * scale, centerOffset.y), end = Offset(centerOffset.x + 36f * scale, centerOffset.y), strokeWidth = 2.5f * scale)
                            
                            // Eyes
                            drawCircle(color = Color.Black, radius = 5f * scale, center = Offset(centerOffset.x - 15f * scale, centerOffset.y))
                            drawCircle(color = Color.Black, radius = 5f * scale, center = Offset(centerOffset.x + 15f * scale, centerOffset.y))
                        }
                    }
                    "elephant" -> {
                        // Gray head with massive ears and long trunk
                        drawCircle(color = animalColor, radius = drawingRadius * 0.8f, center = centerOffset)
                        // Massive ears
                        drawOval(color = animalColor, topLeft = Offset(centerOffset.x - drawingRadius * 1.3f, centerOffset.y - drawingRadius * 0.5f), size = Size(drawingRadius * 0.65f, drawingRadius * 1.0f))
                        drawOval(color = animalColor, topLeft = Offset(centerOffset.x + drawingRadius * 0.65f, centerOffset.y - drawingRadius * 0.5f), size = Size(drawingRadius * 0.65f, drawingRadius * 1.0f))
                        // Long Trunk
                        drawRoundRect(
                            color = animalColor,
                            topLeft = Offset(centerOffset.x - drawingRadius * 0.15f, centerOffset.y),
                            size = Size(drawingRadius * 0.3f, drawingRadius * 0.9f * scaleFactor),
                            cornerRadius = CornerRadius(6f * scale, 6f * scale)
                        )
                        if (currentHintStage == 2) {
                            drawCircle(color = Color.Black, radius = 4f * scale, center = Offset(centerOffset.x - 14f * scale, centerOffset.y - 10f * scale))
                            drawCircle(color = Color.Black, radius = 4f * scale, center = Offset(centerOffset.x + 14f * scale, centerOffset.y - 10f * scale))
                        }
                    }
                    "monkey" -> {
                        // Cheeky monkey head and round ears
                        drawCircle(color = animalColor, radius = drawingRadius * 0.85f, center = centerOffset)
                        // ears
                        drawCircle(color = animalColor, radius = drawingRadius * 0.26f, center = Offset(centerOffset.x - drawingRadius * 0.75f, centerOffset.y - drawingRadius * 0.1f))
                        drawCircle(color = animalColor, radius = drawingRadius * 0.26f, center = Offset(centerOffset.x + drawingRadius * 0.75f, centerOffset.y - drawingRadius * 0.1f))
                        
                        if (currentHintStage == 2) {
                            // Face patch (heart-shaped background)
                            drawCircle(color = Color(0xFFFFEDD5), radius = drawingRadius * 0.35f, center = Offset(centerOffset.x - 12f * scale, centerOffset.y))
                            drawCircle(color = Color(0xFFFFEDD5), radius = drawingRadius * 0.35f, center = Offset(centerOffset.x + 12f * scale, centerOffset.y))
                            drawCircle(color = Color(0xFFFFEDD5), radius = drawingRadius * 0.4f, center = Offset(centerOffset.x, centerOffset.y + 12f * scale))
                            
                            // Eyes & Smile
                            drawCircle(color = Color.Black, radius = 4.5f * scale, center = Offset(centerOffset.x - 12f * scale, centerOffset.y - 2f * scale))
                            drawCircle(color = Color.Black, radius = 4.5f * scale, center = Offset(centerOffset.x + 12f * scale, centerOffset.y - 2f * scale))
                            // mouth
                            drawArc(
                                color = Color.Black,
                                startAngle = 0f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(centerOffset.x - 12f * scale, centerOffset.y + 12f * scale),
                                size = Size(24f * scale, 12f * scale),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f * scale)
                            )
                        }
                    }
                }
            }
        }
        
        // Dynamic labels and details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "📂 目标: " + activeAnimal.uppercase(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isNear) "📍 相对距离: 近处 (What's this?)" else "🔭 相对距离: 远处 (What's that?)",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
            
            // Interaction button for hint reveal
            Button(
                onClick = {
                    if (currentHintStage < 2) {
                        currentHintStage++
                    } else {
                        currentHintStage = 0
                    }
                    ttsHelper.speak(activeAnimal, isSlow = false)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp).testTag("hint_reveal_button")
            ) {
                Text(
                    text = when (currentHintStage) {
                        0 -> "🔍 扫描轮廓"
                        1 -> "💡 显示特征"
                        else -> "⚡ 全彩呈现"
                    },
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EnglishBodyInteractiveView(
    currentExpression: EnglishExpression,
    ttsHelper: EnglishTTSHelper
) {
    var selectedPart by remember { mutableStateOf<String?>(null) }
    var selectedPartNameZh by remember { mutableStateOf("") }
    
    // Parse expression text for active highlight
    val expText = currentExpression.englishText.lowercase()
    val activeHighlight = remember(expText) {
        when {
            expText.contains("face") || expText.contains("look at me") -> "face"
            expText.contains("ear") -> "ear"
            expText.contains("eye") -> "eye"
            expText.contains("nose") -> "nose"
            expText.contains("mouth") -> "mouth"
            expText.contains("head") -> "head"
            expText.contains("hand") -> "hand"
            expText.contains("arm") -> "arm"
            expText.contains("body") -> "body"
            expText.contains("leg") -> "leg"
            else -> null
        }
    }

    // Glowing animation for breathing effect
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    // Reset tapped part when expression changes
    LaunchedEffect(currentExpression.expressionId) {
        selectedPart = null
        selectedPartNameZh = ""
    }

    val finalHighlight = selectedPart ?: activeHighlight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B0F19), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.5.dp, Color(0xFF1E293B)), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "✨ 身体探索雷达 (Tap to Explore)",
            color = Color(0xFF00E5FF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.testTag("body_explore_title")
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFF0F172A).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val x = offset.x / size.width
                        val y = offset.y / size.height
                        
                        // Map coordinates to body parts
                        val part = when {
                            y in 0.08f..0.34f -> {
                                when {
                                    x in 0.44f..0.56f && y in 0.17f..0.23f -> "nose"
                                    x in 0.42f..0.58f && y in 0.23f..0.28f -> "mouth"
                                    x in 0.42f..0.49f && y in 0.14f..0.19f -> "eye"
                                    x in 0.51f..0.58f && y in 0.14f..0.19f -> "eye"
                                    x in 0.32f..0.41f -> "ear"
                                    x in 0.59f..0.68f -> "ear"
                                    else -> "head"
                                }
                            }
                            y in 0.34f..0.68f -> {
                                when {
                                    x in 0.38f..0.62f -> "body"
                                    x in 0.18f..0.38f -> if (y > 0.54f) "hand" else "arm"
                                    x in 0.62f..0.82f -> if (y > 0.54f) "hand" else "arm"
                                    else -> null
                                }
                            }
                            y in 0.68f..0.98f -> {
                                if (x in 0.33f..0.67f) "leg" else null
                            }
                            else -> null
                        }

                        if (part != null) {
                            selectedPart = part
                            selectedPartNameZh = when (part) {
                                "head" -> "头 (head)"
                                "face" -> "脸 (face)"
                                "eye" -> "眼睛 (eye)"
                                "ear" -> "耳朵 (ear)"
                                "nose" -> "鼻子 (nose)"
                                "mouth" -> "嘴巴 (mouth)"
                                "body" -> "身体 (body)"
                                "arm" -> "胳膊 (arm)"
                                "hand" -> "手 (hand)"
                                "leg" -> "腿 (leg)"
                                else -> ""
                            }
                            ttsHelper.speak(part, isSlow = false)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val scale = size.height / 300f // coordinate system based on 300 height

                // HUD concentric scanning rings
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.05f),
                    radius = 110f * scale,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.02f),
                    radius = 140f * scale,
                    center = Offset(cx, cy)
                )

                // Grid references
                drawLine(
                    color = Color(0xFF334155).copy(alpha = 0.2f),
                    start = Offset(cx, 0f),
                    end = Offset(cx, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF334155).copy(alpha = 0.2f),
                    start = Offset(0f, 100f * scale),
                    end = Offset(size.width, 100f * scale),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF334155).copy(alpha = 0.2f),
                    start = Offset(0f, 200f * scale),
                    end = Offset(size.width, 200f * scale),
                    strokeWidth = 1.dp.toPx()
                )

                // Define character geometric variables
                val headRadius = 32f * scale
                val headY = 65f * scale
                
                val eyeY = 60f * scale
                val eyeLeftX = cx - 12f * scale
                val eyeRightX = cx + 12f * scale
                
                val noseY = 68f * scale
                val mouthY = 80f * scale

                val bodyW = 56f * scale
                val bodyH = 75f * scale
                val bodyY = 105f * scale

                // 1. Draw Legs
                val legW = 12f * scale
                val legH = 65f * scale
                val legLeftX = cx - 18f * scale
                val legRightX = cx + 6f * scale
                val legY = bodyY + bodyH

                val isLegHighlighted = finalHighlight == "leg"
                val legColor = if (isLegHighlighted) Color(0xFFEC4899) else Color(0xFF38BDF8)
                val legAlpha = if (isLegHighlighted) 0.35f + 0.15f * glowScale else 0.15f

                // Glow ring for highlighted legs
                if (isLegHighlighted) {
                    drawRoundRect(
                        color = Color(0xFFEC4899).copy(alpha = 0.15f * glowScale),
                        topLeft = Offset(legLeftX - 6f * scale, legY),
                        size = Size(legW + 12f * scale, legH + 6f * scale),
                        cornerRadius = CornerRadius(8f * scale),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFFEC4899).copy(alpha = 0.15f * glowScale),
                        topLeft = Offset(legRightX - 6f * scale, legY),
                        size = Size(legW + 12f * scale, legH + 6f * scale),
                        cornerRadius = CornerRadius(8f * scale),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                drawRoundRect(
                    color = legColor.copy(alpha = legAlpha),
                    topLeft = Offset(legLeftX, legY),
                    size = Size(legW, legH),
                    cornerRadius = CornerRadius(6f * scale)
                )
                drawRoundRect(
                    color = legColor.copy(alpha = legAlpha),
                    topLeft = Offset(legRightX, legY),
                    size = Size(legW, legH),
                    cornerRadius = CornerRadius(6f * scale)
                )
                drawRoundRect(
                    color = legColor,
                    topLeft = Offset(legLeftX, legY),
                    size = Size(legW, legH),
                    cornerRadius = CornerRadius(6f * scale),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawRoundRect(
                    color = legColor,
                    topLeft = Offset(legRightX, legY),
                    size = Size(legW, legH),
                    cornerRadius = CornerRadius(6f * scale),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // 2. Draw Body
                val isBodyHighlighted = finalHighlight == "body"
                val bodyColor = if (isBodyHighlighted) Color(0xFFEC4899) else Color(0xFF38BDF8)
                val bodyAlpha = if (isBodyHighlighted) 0.35f + 0.15f * glowScale else 0.15f

                if (isBodyHighlighted) {
                    drawRoundRect(
                        color = Color(0xFFEC4899).copy(alpha = 0.2f * glowScale),
                        topLeft = Offset(cx - bodyW / 2f - 8f * scale, bodyY - 4f * scale),
                        size = Size(bodyW + 16f * scale, bodyH + 8f * scale),
                        cornerRadius = CornerRadius(16f * scale),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                drawRoundRect(
                    color = bodyColor.copy(alpha = bodyAlpha),
                    topLeft = Offset(cx - bodyW / 2f, bodyY),
                    size = Size(bodyW, bodyH),
                    cornerRadius = CornerRadius(12f * scale)
                )
                drawRoundRect(
                    color = bodyColor,
                    topLeft = Offset(cx - bodyW / 2f, bodyY),
                    size = Size(bodyW, bodyH),
                    cornerRadius = CornerRadius(12f * scale),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // 3. Draw Arms & Hands
                val armW = 10f * scale
                val armH = 50f * scale
                val armLeftX = cx - bodyW / 2f - 14f * scale
                val armRightX = cx + bodyW / 2f + 4f * scale
                val armY = bodyY + 6f * scale

                val isArmHighlighted = finalHighlight == "arm"
                val isHandHighlighted = finalHighlight == "hand"
                
                val armColor = if (isArmHighlighted) Color(0xFFEC4899) else Color(0xFF38BDF8)
                val armAlpha = if (isArmHighlighted) 0.35f + 0.15f * glowScale else 0.15f

                if (isArmHighlighted) {
                    drawRoundRect(
                        color = Color(0xFFEC4899).copy(alpha = 0.15f * glowScale),
                        topLeft = Offset(armLeftX - 6f * scale, armY - 4f * scale),
                        size = Size(armW + 12f * scale, armH + 8f * scale),
                        cornerRadius = CornerRadius(8f * scale),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFFEC4899).copy(alpha = 0.15f * glowScale),
                        topLeft = Offset(armRightX - 6f * scale, armY - 4f * scale),
                        size = Size(armW + 12f * scale, armH + 8f * scale),
                        cornerRadius = CornerRadius(8f * scale),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                drawRoundRect(
                    color = armColor.copy(alpha = armAlpha),
                    topLeft = Offset(armLeftX, armY),
                    size = Size(armW, armH),
                    cornerRadius = CornerRadius(6f * scale)
                )
                drawRoundRect(
                    color = armColor.copy(alpha = armAlpha),
                    topLeft = Offset(armRightX, armY),
                    size = Size(armW, armH),
                    cornerRadius = CornerRadius(6f * scale)
                )
                drawRoundRect(
                    color = armColor,
                    topLeft = Offset(armLeftX, armY),
                    size = Size(armW, armH),
                    cornerRadius = CornerRadius(6f * scale),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawRoundRect(
                    color = armColor,
                    topLeft = Offset(armRightX, armY),
                    size = Size(armW, armH),
                    cornerRadius = CornerRadius(6f * scale),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Hands (circles at arm bottoms)
                val handRadius = 8f * scale
                val handY = armY + armH + 4f * scale
                val handLeftX = armLeftX + armW / 2f
                val handRightX = armRightX + armW / 2f

                val handColor = if (isHandHighlighted) Color(0xFFEC4899) else Color(0xFF38BDF8)
                val handAlpha = if (isHandHighlighted) 0.35f + 0.15f * glowScale else 0.15f

                if (isHandHighlighted) {
                    drawCircle(
                        color = Color(0xFFEC4899).copy(alpha = 0.2f * glowScale),
                        radius = handRadius + 6f * scale,
                        center = Offset(handLeftX, handY)
                    )
                    drawCircle(
                        color = Color(0xFFEC4899).copy(alpha = 0.2f * glowScale),
                        radius = handRadius + 6f * scale,
                        center = Offset(handRightX, handY)
                    )
                }

                drawCircle(
                    color = handColor.copy(alpha = handAlpha),
                    radius = handRadius,
                    center = Offset(handLeftX, handY)
                )
                drawCircle(
                    color = handColor,
                    radius = handRadius,
                    center = Offset(handLeftX, handY),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = handColor.copy(alpha = handAlpha),
                    radius = handRadius,
                    center = Offset(handRightX, handY)
                )
                drawCircle(
                    color = handColor,
                    radius = handRadius,
                    center = Offset(handRightX, handY),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // 4. Draw Ears
                val earRadius = 8f * scale
                val earLeftX = cx - headRadius - 2f * scale
                val earRightX = cx + headRadius + 2f * scale

                val isEarHighlighted = finalHighlight == "ear"
                val earColor = if (isEarHighlighted) Color(0xFFEC4899) else Color(0xFF38BDF8)
                val earAlpha = if (isEarHighlighted) 0.35f + 0.15f * glowScale else 0.15f

                if (isEarHighlighted) {
                    drawCircle(
                        color = Color(0xFFEC4899).copy(alpha = 0.2f * glowScale),
                        radius = earRadius + 6f * scale,
                        center = Offset(earLeftX, headY)
                    )
                    drawCircle(
                        color = Color(0xFFEC4899).copy(alpha = 0.2f * glowScale),
                        radius = earRadius + 6f * scale,
                        center = Offset(earRightX, headY)
                    )
                }

                drawCircle(
                    color = earColor.copy(alpha = earAlpha),
                    radius = earRadius,
                    center = Offset(earLeftX, headY)
                )
                drawCircle(
                    color = earColor,
                    radius = earRadius,
                    center = Offset(earLeftX, headY),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = earColor.copy(alpha = earAlpha),
                    radius = earRadius,
                    center = Offset(earRightX, headY)
                )
                drawCircle(
                    color = earColor,
                    radius = earRadius,
                    center = Offset(earRightX, headY),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // 5. Draw Head & Face
                val isHeadHighlighted = finalHighlight == "head" || finalHighlight == "face"
                val headColor = if (isHeadHighlighted) Color(0xFFEC4899) else Color(0xFF38BDF8)
                val headAlpha = if (isHeadHighlighted) 0.3f + 0.15f * glowScale else 0.15f

                if (isHeadHighlighted) {
                    drawCircle(
                        color = Color(0xFFEC4899).copy(alpha = 0.2f * glowScale),
                        radius = headRadius + 8f * scale,
                        center = Offset(cx, headY)
                    )
                }

                drawCircle(
                    color = headColor.copy(alpha = headAlpha),
                    radius = headRadius,
                    center = Offset(cx, headY)
                )
                drawCircle(
                    color = headColor,
                    radius = headRadius,
                    center = Offset(cx, headY),
                    style = Stroke(width = 2.dp.toPx())
                )

                // 6. Draw Eyes
                val eyeRadius = 4f * scale
                val isEyeHighlighted = finalHighlight == "eye"
                val eyeColor = if (isEyeHighlighted) Color(0xFFEC4899) else Color(0xFF00E5FF)
                
                if (isEyeHighlighted) {
                    drawCircle(
                        color = Color(0xFFEC4899).copy(alpha = 0.3f * glowScale),
                        radius = eyeRadius + 4f * scale,
                        center = Offset(eyeLeftX, eyeY)
                    )
                    drawCircle(
                        color = Color(0xFFEC4899).copy(alpha = 0.3f * glowScale),
                        radius = eyeRadius + 4f * scale,
                        center = Offset(eyeRightX, eyeY)
                    )
                }

                drawCircle(
                    color = eyeColor,
                    radius = eyeRadius,
                    center = Offset(eyeLeftX, eyeY)
                )
                drawCircle(
                    color = eyeColor,
                    radius = eyeRadius,
                    center = Offset(eyeRightX, eyeY)
                )

                // 7. Draw Nose
                val isNoseHighlighted = finalHighlight == "nose"
                val noseColor = if (isNoseHighlighted) Color(0xFFEC4899) else Color(0xFF00E5FF)

                if (isNoseHighlighted) {
                    drawCircle(
                        color = Color(0xFFEC4899).copy(alpha = 0.3f * glowScale),
                        radius = 8f * scale,
                        center = Offset(cx, noseY)
                    )
                }

                val nosePath = Path().apply {
                    moveTo(cx, noseY - 4f * scale)
                    lineTo(cx - 3f * scale, noseY + 3f * scale)
                    lineTo(cx + 3f * scale, noseY + 3f * scale)
                    close()
                }
                drawPath(
                    path = nosePath,
                    color = noseColor
                )

                // 8. Draw Mouth
                val isMouthHighlighted = finalHighlight == "mouth"
                val mouthColor = if (isMouthHighlighted) Color(0xFFEC4899) else Color(0xFF00E5FF)

                if (isMouthHighlighted) {
                    drawCircle(
                        color = Color(0xFFEC4899).copy(alpha = 0.3f * glowScale),
                        radius = 10f * scale,
                        center = Offset(cx, mouthY)
                    )
                }

                drawArc(
                    color = mouthColor,
                    startAngle = 10f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(cx - 8f * scale, mouthY - 4f * scale),
                    size = Size(16f * scale, 10f * scale),
                    style = Stroke(width = 2.dp.toPx())
                )

                // HUD pointer lines pointing to active or selected parts to make it incredibly immersive!
                val pointerTarget = when (finalHighlight) {
                    "head", "face" -> Offset(cx, headY)
                    "eye" -> Offset(eyeRightX, eyeY)
                    "ear" -> Offset(earRightX, headY)
                    "nose" -> Offset(cx, noseY)
                    "mouth" -> Offset(cx, mouthY)
                    "body" -> Offset(cx, bodyY + bodyH / 2f)
                    "arm" -> Offset(armRightX + armW / 2f, armY + armH / 2f)
                    "hand" -> Offset(handRightX, handY)
                    "leg" -> Offset(legRightX + legW / 2f, legY + legH / 2f)
                    else -> null
                }

                if (pointerTarget != null) {
                    // Line from part to a standard label HUD anchor
                    val labelAnchor = Offset(cx + 100f * scale, pointerTarget.y - 15f * scale)
                    drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                        start = pointerTarget,
                        end = labelAnchor,
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = 3f * scale,
                        center = pointerTarget
                    )
                }
            }
        }

        // Tapped / active part indicator text card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(8.dp))
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🎯 当前关联或选中的部位：",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Text(
                    text = if (selectedPart != null) selectedPartNameZh else if (activeHighlight != null) "${when(activeHighlight){
                        "head" -> "头"
                        "face" -> "脸"
                        "eye" -> "眼睛"
                        "ear" -> "耳朵"
                        "nose" -> "鼻子"
                        "mouth" -> "嘴巴"
                        "body" -> "身体"
                        "arm" -> "胳膊"
                        "hand" -> "手"
                        "leg" -> "腿"
                        else -> ""
                    }} (${activeHighlight})" else "（点击人物任意部位开始探索）",
                    color = if (selectedPart != null || activeHighlight != null) Color(0xFF00E5FF) else Color.Gray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (selectedPart != null || activeHighlight != null) {
                IconButton(
                    onClick = { ttsHelper.speak(selectedPart ?: activeHighlight ?: "", isSlow = false) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFEC4899).copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "发音",
                        tint = Color(0xFFEC4899),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
