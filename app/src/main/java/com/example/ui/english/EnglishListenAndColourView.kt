package com.example.ui.english

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.SpeechSynthesizer

data class ColourPaletteItem(
    val colorId: String,
    val colorName: String,
    val colorHex: Color
)

val RECYCLE_COLOUR_PALETTE = listOf(
    ColourPaletteItem("red", "Red 红色", Color(0xFFEF4444)),
    ColourPaletteItem("yellow", "Yellow 黄色", Color(0xFFEAB308)),
    ColourPaletteItem("green", "Green 绿色", Color(0xFF22C55E)),
    ColourPaletteItem("blue", "Blue 蓝色", Color(0xFF3B82F6)),
    ColourPaletteItem("black", "Black 黑色", Color(0xFF1E293B)),
    ColourPaletteItem("white", "White 白色", Color(0xFFF8FAFC)),
    ColourPaletteItem("orange", "Orange 橙色", Color(0xFFF97316)),
    ColourPaletteItem("brown", "Brown 棕色", Color(0xFF78350F))
)

data class ColourTaskTarget(
    val bodyPartId: String,
    val bodyPartName: String,
    val requiredColorId: String,
    val promptAudioText: String
)

val DEFAULT_RECYCLE_COLOUR_TASKS = listOf(
    ColourTaskTarget(
        bodyPartId = "nose",
        bodyPartName = "鼻子 Nose",
        requiredColorId = "red",
        promptAudioText = "Colour the nose red!"
    ),
    ColourTaskTarget(
        bodyPartId = "ear",
        bodyPartName = "耳朵 Ears",
        requiredColorId = "yellow",
        promptAudioText = "Colour the ears yellow!"
    ),
    ColourTaskTarget(
        bodyPartId = "face",
        bodyPartName = "脸庞 Face",
        requiredColorId = "brown",
        promptAudioText = "Colour the face brown!"
    ),
    ColourTaskTarget(
        bodyPartId = "arm",
        bodyPartName = "手臂 Arms",
        requiredColorId = "blue",
        promptAudioText = "Colour the arms blue!"
    )
)

@Composable
fun EnglishListenAndColourView(
    tasks: List<ColourTaskTarget> = DEFAULT_RECYCLE_COLOUR_TASKS,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTaskIndex by remember { mutableIntStateOf(0) }
    val currentTask = tasks.getOrElse(currentTaskIndex) { tasks.first() }

    var selectedColor by remember { mutableStateOf<ColourPaletteItem?>(null) }
    val filledColors = remember { mutableStateMapOf<String, String>() }
    val actionHistory = remember { mutableStateListOf<Pair<String, String>>() }
    var isSubmittedStep by remember { mutableStateOf(false) }
    var isCorrectStep by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var lastTappedPart by remember { mutableStateOf<String?>(null) }

    // Play audio prompt when switching task
    LaunchedEffect(currentTaskIndex) {
        isSubmittedStep = false
        isCorrectStep = false
        errorMessage = ""
        SpeechSynthesizer.speak(context, currentTask.promptAudioText)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Round & Progress Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFFEC4899).copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEC4899))
            ) {
                Text(
                    text = "🎯 关卡 ${currentTaskIndex + 1} / ${tasks.size}",
                    color = Color(0xFFF472B6),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Step Indicator Dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tasks.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == currentTaskIndex) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    idx < currentTaskIndex -> Color(0xFF10B981)
                                    idx == currentTaskIndex -> Color(0xFFEC4899)
                                    else -> Color(0xFF475569)
                                }
                            )
                    )
                }
            }
        }

        // Audio Prompt Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🔊 第 ${currentTaskIndex + 1} 关指令 (点击喇叭可重听):",
                        color = Color(0xFFF472B6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentTask.promptAudioText,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "目标部位: ${currentTask.bodyPartName}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = { SpeechSynthesizer.speak(context, currentTask.promptAudioText) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFEC4899), CircleShape)
                        .testTag("colour_audio_play_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "播放语音",
                        tint = Color.White
                    )
                }
            }
        }

        // Cartoon Body Canvas with High-Definition Bear Artwork & Explicit Labels
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF334155)),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Interactive Cartoon Teddy Bear Character Canvas
                val faceColor = RECYCLE_COLOUR_PALETTE.find { it.colorId == filledColors["face"] }?.colorHex ?: Color(0xFFFED7AA)
                val earColor = RECYCLE_COLOUR_PALETTE.find { it.colorId == filledColors["ear"] }?.colorHex ?: Color(0xFFFDBA74)
                val noseColor = RECYCLE_COLOUR_PALETTE.find { it.colorId == filledColors["nose"] }?.colorHex ?: Color(0xFFF43F5E)
                val armColor = RECYCLE_COLOUR_PALETTE.find { it.colorId == filledColors["arm"] }?.colorHex ?: Color(0xFFFDBA74)

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val sel = selectedColor
                                val width = size.width
                                val height = size.height

                                // Body part hit zones (Bear Layout)
                                val noseCenter = Offset(width * 0.5f, height * 0.45f)
                                val earLeftCenter = Offset(width * 0.25f, height * 0.28f)
                                val earRightCenter = Offset(width * 0.75f, height * 0.28f)
                                val faceCenter = Offset(width * 0.5f, height * 0.44f)
                                val armLeftCenter = Offset(width * 0.22f, height * 0.70f)
                                val armRightCenter = Offset(width * 0.78f, height * 0.70f)

                                val clickedPart = when {
                                    (offset - noseCenter).getDistance() < 45f -> "nose"
                                    (offset - earLeftCenter).getDistance() < 60f || (offset - earRightCenter).getDistance() < 60f -> "ear"
                                    (offset - armLeftCenter).getDistance() < 65f || (offset - armRightCenter).getDistance() < 65f -> "arm"
                                    (offset - faceCenter).getDistance() < 130f -> "face"
                                    else -> "face"
                                }

                                lastTappedPart = clickedPart

                                if (sel != null) {
                                    filledColors[clickedPart] = sel.colorId
                                    actionHistory.add(clickedPart to sel.colorId)
                                    errorMessage = ""
                                } else {
                                    errorMessage = "💡 请先从下方调色盘中点击选择一种颜色！"
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    val earLeftCenter = Offset(w * 0.25f, h * 0.28f)
                    val earRightCenter = Offset(w * 0.75f, h * 0.28f)
                    val faceCenter = Offset(w * 0.5f, h * 0.44f)
                    val noseCenter = Offset(w * 0.5f, h * 0.45f)

                    // 1. EAR LEFT & RIGHT
                    drawCircle(color = earColor, radius = 52f, center = earLeftCenter)
                    drawCircle(color = Color(0xFF334155), radius = 52f, center = earLeftCenter, style = Stroke(width = 6f))
                    drawCircle(color = earColor.copy(alpha = 0.7f), radius = 30f, center = earLeftCenter)

                    drawCircle(color = earColor, radius = 52f, center = earRightCenter)
                    drawCircle(color = Color(0xFF334155), radius = 52f, center = earRightCenter, style = Stroke(width = 6f))
                    drawCircle(color = earColor.copy(alpha = 0.7f), radius = 30f, center = earRightCenter)

                    // Highlight border if current task is ear
                    if (currentTask.bodyPartId == "ear") {
                        drawCircle(color = Color(0xFFEC4899), radius = 60f, center = earLeftCenter, style = Stroke(width = 4f))
                        drawCircle(color = Color(0xFFEC4899), radius = 60f, center = earRightCenter, style = Stroke(width = 4f))
                    }

                    // 2. ARMS LEFT & RIGHT
                    drawRoundRect(
                        color = armColor,
                        topLeft = Offset(w * 0.12f, h * 0.62f),
                        size = Size(w * 0.20f, h * 0.22f),
                        cornerRadius = CornerRadius(28f, 28f)
                    )
                    drawRoundRect(
                        color = Color(0xFF334155),
                        topLeft = Offset(w * 0.12f, h * 0.62f),
                        size = Size(w * 0.20f, h * 0.22f),
                        cornerRadius = CornerRadius(28f, 28f),
                        style = Stroke(width = 6f)
                    )

                    drawRoundRect(
                        color = armColor,
                        topLeft = Offset(w * 0.68f, h * 0.62f),
                        size = Size(w * 0.20f, h * 0.22f),
                        cornerRadius = CornerRadius(28f, 28f)
                    )
                    drawRoundRect(
                        color = Color(0xFF334155),
                        topLeft = Offset(w * 0.68f, h * 0.62f),
                        size = Size(w * 0.20f, h * 0.22f),
                        cornerRadius = CornerRadius(28f, 28f),
                        style = Stroke(width = 6f)
                    )

                    if (currentTask.bodyPartId == "arm") {
                        drawRoundRect(
                            color = Color(0xFFEC4899),
                            topLeft = Offset(w * 0.10f, h * 0.60f),
                            size = Size(w * 0.24f, h * 0.26f),
                            cornerRadius = CornerRadius(32f, 32f),
                            style = Stroke(width = 4f)
                        )
                        drawRoundRect(
                            color = Color(0xFFEC4899),
                            topLeft = Offset(w * 0.66f, h * 0.60f),
                            size = Size(w * 0.24f, h * 0.26f),
                            cornerRadius = CornerRadius(32f, 32f),
                            style = Stroke(width = 4f)
                        )
                    }

                    // 3. FACE (MAIN HEAD)
                    drawCircle(color = faceColor, radius = 125f, center = faceCenter)
                    drawCircle(color = Color(0xFF334155), radius = 125f, center = faceCenter, style = Stroke(width = 7f))

                    if (currentTask.bodyPartId == "face") {
                        drawCircle(color = Color(0xFFEC4899), radius = 135f, center = faceCenter, style = Stroke(width = 4f))
                    }

                    // Muzzle Snout Area
                    drawOval(
                        color = Color.White.copy(alpha = 0.85f),
                        topLeft = Offset(w * 0.38f, h * 0.40f),
                        size = Size(w * 0.24f, h * 0.18f)
                    )

                    // Eyes
                    drawCircle(color = Color.Black, radius = 12f, center = Offset(w * 0.42f, h * 0.38f))
                    drawCircle(color = Color.White, radius = 4f, center = Offset(w * 0.40f, h * 0.36f)) // Highlight

                    drawCircle(color = Color.Black, radius = 12f, center = Offset(w * 0.58f, h * 0.38f))
                    drawCircle(color = Color.White, radius = 4f, center = Offset(w * 0.56f, h * 0.36f)) // Highlight

                    // Rosy Cheeks
                    drawCircle(color = Color(0xFFFF8A8A).copy(alpha = 0.5f), radius = 14f, center = Offset(w * 0.35f, h * 0.46f))
                    drawCircle(color = Color(0xFFFF8A8A).copy(alpha = 0.5f), radius = 14f, center = Offset(w * 0.65f, h * 0.46f))

                    // 4. NOSE
                    drawCircle(color = noseColor, radius = 26f, center = noseCenter)
                    drawCircle(color = Color.Black, radius = 26f, center = noseCenter, style = Stroke(width = 4f))
                    drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 6f, center = Offset(w * 0.48f, h * 0.43f))

                    if (currentTask.bodyPartId == "nose") {
                        drawCircle(color = Color(0xFFEC4899), radius = 34f, center = noseCenter, style = Stroke(width = 4f))
                    }

                    // Mouth
                    drawArc(
                        color = Color(0xFFDC2626),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.44f, h * 0.49f),
                        size = Size(w * 0.12f, h * 0.06f),
                        style = Stroke(width = 6f)
                    )
                }

                // Explicit Body Part Floating Badges (Labels for crystal-clear recognition)
                // Ear Label (Top Left)
                BodyPartLabelBadge(
                    text = "👂 耳朵 Ear",
                    isCurrentTarget = currentTask.bodyPartId == "ear",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 16.dp)
                )

                // Face Label (Top Right)
                BodyPartLabelBadge(
                    text = "🐱 脸庞 Face",
                    isCurrentTarget = currentTask.bodyPartId == "face",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp, top = 16.dp)
                )

                // Nose Label (Center Indicator)
                BodyPartLabelBadge(
                    text = "👃 鼻子 Nose",
                    isCurrentTarget = currentTask.bodyPartId == "nose",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-30).dp)
                )

                // Arm Label (Bottom)
                BodyPartLabelBadge(
                    text = "💪 手臂 Arms",
                    isCurrentTarget = currentTask.bodyPartId == "arm",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                )

                // Bottom Hint Bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "💡 点击调色盘选颜色，然后点击角色部位上色",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Palette Selector Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎨 调色盘 (请选择颜色，然后点击上方角色):",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            if (selectedColor != null) {
                Text(
                    text = "已选: ${selectedColor?.colorName}",
                    color = Color(0xFFF472B6),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Palette Grid (Row 1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RECYCLE_COLOUR_PALETTE.take(4).forEach { item ->
                PaletteButton(
                    item = item,
                    isSelected = selectedColor?.colorId == item.colorId,
                    onSelect = {
                        selectedColor = item
                        errorMessage = ""
                    }
                )
            }
        }

        // Palette Grid (Row 2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RECYCLE_COLOUR_PALETTE.takeLast(4).forEach { item ->
                PaletteButton(
                    item = item,
                    isSelected = selectedColor?.colorId == item.colorId,
                    onSelect = {
                        selectedColor = item
                        errorMessage = ""
                    }
                )
            }
        }

        // Undo & Clear Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    if (actionHistory.isNotEmpty()) {
                        val last = actionHistory.removeAt(actionHistory.size - 1)
                        filledColors.remove(last.first)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("colour_undo_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Undo, contentDescription = "撤销", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("撤销上色", fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = {
                    filledColors.clear()
                    actionHistory.clear()
                    isSubmittedStep = false
                    isCorrectStep = false
                    errorMessage = ""
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("colour_reset_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "重置", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("全部重置", fontSize = 13.sp)
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color(0xFFEF4444),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Submit & Next Step Buttons Flow
        if (!isCorrectStep) {
            Button(
                onClick = {
                    val actualColor = filledColors[currentTask.bodyPartId]
                    if (actualColor == currentTask.requiredColorId) {
                        isCorrectStep = true
                        isSubmittedStep = true
                        errorMessage = ""
                        SpeechSynthesizer.speak(context, "Great job! ${currentTask.promptAudioText} Correct!")
                    } else {
                        isCorrectStep = false
                        isSubmittedStep = true
                        errorMessage = "❌ 涂色不对哦！指令要求: ${currentTask.promptAudioText}，请仔细重试！"
                        SpeechSynthesizer.speak(context, "Try again!")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("colour_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "提交答案")
                Spacer(modifier = Modifier.width(8.dp))
                Text("确认提交第 ${currentTaskIndex + 1} 关涂色答案", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            // Correct Answer Banner & Next Stage Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF065F46)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF34D399))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎉 第 ${currentTaskIndex + 1} 关完全正确！词汇涂色完美！",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Next Step or Complete All Tasks Button
                Button(
                    onClick = {
                        if (currentTaskIndex < tasks.size - 1) {
                            currentTaskIndex++
                            selectedColor = null
                        } else {
                            onCompleted()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("colour_next_step_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (currentTaskIndex < tasks.size - 1) "进入下一关 (${currentTaskIndex + 2}/${tasks.size}) ➡️" else "🎉 完成全套听音涂色·点击结账",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun BodyPartLabelBadge(
    text: String,
    isCurrentTarget: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isCurrentTarget) Color(0xFFEC4899) else Color(0xFF1E293B).copy(alpha = 0.85f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isCurrentTarget) 2.dp else 1.dp,
            color = if (isCurrentTarget) Color.White else Color(0xFF475569)
        ),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = if (isCurrentTarget) 12.sp else 11.sp,
            fontWeight = if (isCurrentTarget) FontWeight.ExtraBold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PaletteButton(
    item: ColourPaletteItem,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(76.dp, 46.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color(0xFFEC4899) else Color(0xFF475569),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .testTag("color_palette_${item.colorId}"),
        color = item.colorHex
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = item.colorName.split(" ").first(),
                color = if (item.colorId == "white" || item.colorId == "yellow") Color.Black else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
