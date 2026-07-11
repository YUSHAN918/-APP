package com.example.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.DigitalInkRecognizerManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandwritingLabScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    var targetWord by remember { mutableStateOf("观") }
    var resultText by remember { mutableStateOf("") }
    var candidateList by remember { mutableStateOf<List<String>>(emptyList()) }
    var judgementText by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }
    
    var handwritingView by remember { mutableStateOf<HandwritingView?>(null) }
    
    val modelStatus by DigitalInkRecognizerManager.modelStatus.collectAsState()
    val statusDetails by DigitalInkRecognizerManager.statusDetails.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("手写识别实验室") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Model Management Area
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("模型包管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("识别语言：中文 (简体)")
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("当前状态：")
                        AssistChip(
                            onClick = { },
                            label = { Text(modelStatus) },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = when (modelStatus) {
                                    "已下载" -> Color(0xFF2E7D32)
                                    "下载中" -> Color(0xFFF57C00)
                                    "下载失败" -> MaterialTheme.colorScheme.error
                                    else -> Color.Gray
                                }
                            )
                        )
                    }
                    Text(statusDetails, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { DigitalInkRecognizerManager.downloadModel() },
                            enabled = modelStatus != "下载中",
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "下载", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("下载模型", style = MaterialTheme.typography.labelSmall)
                        }
                        
                        OutlinedButton(
                            onClick = { DigitalInkRecognizerManager.checkModelStatus() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("重新检查", style = MaterialTheme.typography.labelSmall)
                        }

                        if (modelStatus == "已下载") {
                            IconButton(
                                onClick = { DigitalInkRecognizerManager.deleteModel() },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "删除模型")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Testing Inputs Area
            OutlinedTextField(
                value = targetWord,
                onValueChange = { targetWord = it.trim().take(1) },
                label = { Text("预期字 (例：观)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val playerProfile by viewModel.playerProfile.collectAsState()
            val equippedBrushConfig by viewModel.equippedBrushConfig.collectAsState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        HandwritingView(context).apply {
                            handwritingView = this
                            setGrid(1, 1)
                        }
                    },
                    update = { view ->
                        val equippedBrushId = playerProfile?.equippedBrushId ?: "default_black"
                        view.currentBrush = BrushStyle.getBrushById(equippedBrushId)
                        view.currentBrushConfig = equippedBrushConfig
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OutlinedButton(onClick = { 
                    handwritingView?.clear()
                    resultText = ""
                    candidateList = emptyList()
                    judgementText = ""
                }) {
                    Icon(Icons.Default.Clear, contentDescription = "清空")
                    Text("清空")
                }
                
                Button(
                    onClick = {
                        isTesting = true
                        val strokes = handwritingView?.getStrokes() ?: emptyList()
                        if (strokes.isEmpty()) {
                            resultText = "手写区为空，请先在大田字格内手写字"
                            candidateList = emptyList()
                            judgementText = ""
                            isTesting = false
                            return@Button
                        }
                        
                        if (modelStatus != "已下载" && modelStatus != "已预置 (离线免下载)") {
                            resultText = "识别包尚未就绪，当前状态: $modelStatus。"
                            judgementText = "请先点击上方“下载模型”下载简体中文手写识别包 (支持离线识别)。"
                            candidateList = emptyList()
                            isTesting = false
                            return@Button
                        }

                        resultText = "正在后台离线识别笔迹..."
                        DigitalInkRecognizerManager.recognize(strokes, null) { candidates, error ->
                            isTesting = false
                            if (error != null) {
                                resultText = "手写识别失败"
                                judgementText = "错误原因: $error"
                                candidateList = emptyList()
                            } else {
                                val cands = candidates ?: emptyList()
                                candidateList = cands
                                if (cands.isEmpty()) {
                                    resultText = "未匹配到任何手写候选字"
                                    judgementText = "建议书写更规整一些再试"
                                } else {
                                    resultText = "首选结果: ${cands.first()}"
                                    
                                    val matchIdx = cands.indexOf(targetWord)
                                    judgementText = when (matchIdx) {
                                        0 -> "目标字出现在首位（第 1 候选）！可能正确！"
                                        in 1..2 -> "目标字出现在前三位（第 ${matchIdx + 1} 候选）！可能正确。"
                                        in 3..4 -> "目标字出现在候选中（第 ${matchIdx + 1} 候选）。建议检查。"
                                        else -> "目标字「$targetWord」未命中（不在前 5 候选）。可能写错或识别不准，请人工检查。"
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isTesting
                ) {
                    Icon(Icons.Default.Gesture, contentDescription = "识别测试", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("识别测试")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("测试识别结果", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("识别状态: $resultText", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    if (candidateList.isNotEmpty()) {
                        Text("候选列表 (前 5):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            candidateList.take(5).forEachIndexed { idx, word ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text("${idx + 1}. $word") },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    }
                    
                    if (judgementText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("判断建议: $judgementText", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
