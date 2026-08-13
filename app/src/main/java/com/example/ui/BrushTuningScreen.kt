package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.viewmodel.GameViewModel
import com.example.data.PlayerBrushConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrushTuningScreen(
    viewModel: GameViewModel,
    brush: BrushStyle,
    isUnlocked: Boolean,
    onClose: () -> Unit
) {
    val playerProfile by viewModel.playerProfile.collectAsState()
    val scope = rememberCoroutineScope()
    
    var config by remember { mutableStateOf<PlayerBrushConfig?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(brush.brushId) {
        val loaded = viewModel.getBrushConfig(brush.brushId)
        if (loaded != null) {
            config = loaded
        } else {
            // default config mapped from brush
            if (brush.brushId == "pet_dragon_brush") {
                config = PlayerBrushConfig(
                    accountId = playerProfile?.accountId ?: 0L,
                    playerId = playerProfile?.id ?: 0L,
                    brushId = brush.brushId,
                    baseWidth = 24f,
                    minWidth = 4f,
                    maxWidth = 24f,
                    pressureSensitivity = 1.0f,
                    speedSensitivity = 1.2f,
                    smoothing = 0.35f,
                    opacity = 1.0f,
                    glowRadius = 0f,
                    particleDensity = 0.15f,
                    effectIntensity = 0.25f,
                    colorHex = "default",
                    usageMode = "TEST_SAFE",
                    widthMode = "AUTO"
                )
            } else {
                config = PlayerBrushConfig(
                    accountId = playerProfile?.accountId ?: 0L,
                    playerId = playerProfile?.id ?: 0L,
                    brushId = brush.brushId,
                    baseWidth = brush.maxWidth,
                    minWidth = brush.minWidth,
                    maxWidth = brush.maxWidth,
                    pressureSensitivity = 1.0f,
                    speedSensitivity = 1.2f,
                    smoothing = 0.35f,
                    opacity = 1.0f,
                    glowRadius = if (brush.glowEnabled) 16f else 0f,
                    particleDensity = if (brush.particleEnabled) 1.0f else 0f,
                    effectIntensity = 1.0f,
                    colorHex = "default",
                    usageMode = "TEST_SAFE",
                    widthMode = "AUTO"
                )
            }
        }
        isLoading = false
    }

    if (isLoading || config == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var handwritingView by remember { mutableStateOf<HandwritingView?>(null) }
    var showDebug by remember { mutableStateOf(false) }

    // Colors list
    val colorOptions = listOf(
        "default" to "默认",
        "#000000" to "黑色",
        "#F44336" to "红色",
        "#2196F3" to "蓝色",
        "#4CAF50" to "绿色",
        "#9C27B0" to "紫色",
        "#FF9800" to "橙色"
    )

    fun applyPreset(presetType: Int) {
        config = when (presetType) {
            1 -> config!!.copy(
                opacity = 1.0f,
                glowRadius = 0f,
                particleDensity = 0f,
                minWidth = brush.minWidth.coerceAtMost(8f),
                maxWidth = brush.maxWidth.coerceAtMost(24f),
                usageMode = "TEST_SAFE"
            )
            2 -> config!!.copy(
                opacity = 0.9f,
                glowRadius = if (brush.glowEnabled) 8f else 0f,
                particleDensity = if (brush.particleEnabled) 0.5f else 0f,
                minWidth = brush.minWidth,
                maxWidth = brush.maxWidth,
                usageMode = "PRACTICE_FUN"
            )
            3 -> config!!.copy(
                opacity = 1.0f,
                glowRadius = if (brush.glowEnabled) 24f else 0f,
                particleDensity = if (brush.particleEnabled) 1.0f else 0f,
                minWidth = brush.minWidth,
                maxWidth = brush.maxWidth * 1.2f,
                usageMode = "SHOWCASE"
            )
            4 -> config!!.copy(
                minWidth = 3f,
                maxWidth = 36f,
                speedSensitivity = 1.8f,
                smoothing = 0.2f,
                opacity = 1.0f,
                glowRadius = 0f,
                particleDensity = 0f,
                widthMode = "FORCE_SPEED"
            )
            else -> config!!
        }
        handwritingView?.currentBrushConfig = config
        handwritingView?.invalidate()
    }

    AlertDialog(
        onDismissRequest = onClose,
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.95f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Info with Back Arrow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "书写武器试炼场",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "当前测试: ${brush.brushName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val isEquipped = playerProfile?.equippedBrushId == brush.brushId
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isEquipped) Color(0xFFE8F5E9) else Color(0xFFECEFF1),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (isEquipped) "已装备" else "未装备",
                                    color = if (isEquipped) Color(0xFF2E7D32) else Color(0xFF546E7A),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Info Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "💡 提示：此处的调校与试写仅作用于显示效果的微调，不影响底层的文字及拼音智能识别精度。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE65100)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Handwriting Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(horizontal = 16.dp)
                        .background(Color(0xFFFDF6E3), RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            HandwritingView(ctx).apply {
                                setGrid(4, 1)
                                currentBrush = brush
                                currentBrushConfig = config
                                handwritingView = this
                            }
                        },
                        update = { view ->
                            view.currentBrush = brush
                            view.currentBrushConfig = config
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { applyPreset(1) }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text("清晰考试", fontSize = 12.sp) }
                    Button(onClick = { applyPreset(2) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text("好看练习", fontSize = 12.sp) }
                    Button(onClick = { applyPreset(3) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text("炫酷特效", fontSize = 12.sp) }
                }

                // Tuning Sliders
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("粗细调校", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                    TuningSlider("最小粗细 (minWidth)", config!!.minWidth, 2f, 20f) { config = config!!.copy(minWidth = it); handwritingView?.currentBrushConfig = config; handwritingView?.invalidate() }
                    TuningSlider("最大粗细 (maxWidth)", config!!.maxWidth, 8f, 48f) { config = config!!.copy(maxWidth = it); handwritingView?.currentBrushConfig = config; handwritingView?.invalidate() }
                    
                    Text("灵敏度调校", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                    TuningSlider("压力灵敏度", config!!.pressureSensitivity, 0f, 2f) { config = config!!.copy(pressureSensitivity = it); handwritingView?.currentBrushConfig = config; handwritingView?.invalidate() }
                    TuningSlider("速度估算灵敏度", config!!.speedSensitivity, 0f, 2f) { config = config!!.copy(speedSensitivity = it); handwritingView?.currentBrushConfig = config; handwritingView?.invalidate() }
                    
                    Text("视觉特效", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                    TuningSlider("透明度 (opacity)", config!!.opacity, 0.3f, 1.0f) { config = config!!.copy(opacity = it); handwritingView?.currentBrushConfig = config; handwritingView?.invalidate() }
                    TuningSlider("发光强度", config!!.glowRadius, 0f, 32f) { config = config!!.copy(glowRadius = it); handwritingView?.currentBrushConfig = config; handwritingView?.invalidate() }
                    TuningSlider("粒子密度", config!!.particleDensity, 0f, 1.0f) { config = config!!.copy(particleDensity = it); handwritingView?.currentBrushConfig = config; handwritingView?.invalidate() }

                    Text("颜色", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        colorOptions.forEach { (hex, name) ->
                            val color = if (hex == "default") brush.baseColor else android.graphics.Color.parseColor(hex)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(color), CircleShape)
                                    .border(
                                        width = if (config!!.colorHex == hex) 3.dp else 1.dp,
                                        color = if (config!!.colorHex == hex) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        config = config!!.copy(colorHex = hex)
                                        handwritingView?.currentBrushConfig = config
                                        handwritingView?.invalidate()
                                    }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("强制速度笔锋测试", fontWeight = FontWeight.Bold)
                        Switch(
                            checked = config!!.widthMode == "FORCE_SPEED",
                            onCheckedChange = { 
                                config = config!!.copy(widthMode = if (it) "FORCE_SPEED" else "AUTO")
                                handwritingView?.currentBrushConfig = config
                                handwritingView?.invalidate()
                            }
                        )
                    }
                    if (config!!.widthMode == "FORCE_SPEED") {
                        Text("当前正在测试普通手机也能生效的速度笔锋。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(onClick = { applyPreset(4) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))) {
                        Text("应用夸张笔锋测试参数")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showDebug = !showDebug }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (showDebug) "隐藏调试信息" else "显示调试信息")
                    }
                    if (showDebug) {
                        val strokes = handwritingView?.getStrokes() ?: emptyList()
                        val pointsCount = strokes.sumOf { it.points.size }
                        val drawingCanvas = handwritingView
                        Text("笔画数: ${strokes.size}, 总点数: $pointsCount", style = MaterialTheme.typography.bodySmall)
                        if (drawingCanvas != null) {
                            Text("pressureMin: ${String.format("%.3f", drawingCanvas.lastPressureMin)}", style = MaterialTheme.typography.bodySmall)
                            Text("pressureMax: ${String.format("%.3f", drawingCanvas.lastPressureMax)}", style = MaterialTheme.typography.bodySmall)
                            Text("pressureRange: ${String.format("%.3f", drawingCanvas.lastPressureMax - drawingCanvas.lastPressureMin)}", style = MaterialTheme.typography.bodySmall)
                            Text("pressure 是否有效: ${drawingCanvas.lastPressureValid}", style = MaterialTheme.typography.bodySmall)
                            Text("当前 widthMode: ${drawingCanvas.lastWidthMode}", style = MaterialTheme.typography.bodySmall)
                            Text("speedMin: ${String.format("%.3f", drawingCanvas.lastSpeedMin)}", style = MaterialTheme.typography.bodySmall)
                            Text("speedMax: ${String.format("%.3f", drawingCanvas.lastSpeedMax)}", style = MaterialTheme.typography.bodySmall)
                            Text("widthMinReal: ${String.format("%.3f", drawingCanvas.lastWidthMinReal)}", style = MaterialTheme.typography.bodySmall)
                            Text("widthMaxReal: ${String.format("%.3f", drawingCanvas.lastWidthMaxReal)}", style = MaterialTheme.typography.bodySmall)
                            Text("最近笔画粗细: ${drawingCanvas.recentStrokeWidths.joinToString { String.format("%.1f", it) }}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        TextButton(onClick = { handwritingView?.clear() }, contentPadding = PaddingValues(horizontal = 4.dp)) { Text("清空", fontSize = 12.sp) }
                        TextButton(onClick = { handwritingView?.undo() }, contentPadding = PaddingValues(horizontal = 4.dp)) { Text("撤销", fontSize = 12.sp) }
                    }
                    
                    Row {
                        if (isUnlocked) {
                            Button(onClick = {
                                viewModel.saveBrushConfig(config!!)
                                onClose()
                            }, modifier = Modifier.padding(end = 4.dp), contentPadding = PaddingValues(horizontal = 8.dp)) { Text("保存", fontSize = 12.sp) }

                            Button(onClick = {
                                viewModel.saveBrushConfig(config!!)
                                viewModel.equipBrush(brush.brushId)
                                onClose()
                            }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("保存并装备", fontSize = 12.sp) }
                        } else {
                            Button(onClick = onClose) { Text("完成试写") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TuningSlider(label: String, value: Float, min: Float, max: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(String.format("%.2f", value), style = MaterialTheme.typography.bodyMedium)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max
        )
    }
}
