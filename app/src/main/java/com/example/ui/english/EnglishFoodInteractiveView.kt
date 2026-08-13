package com.example.ui.english

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.EnglishExpression
import com.example.data.english.EnglishWord
import com.example.util.english.EnglishTTSHelper

enum class FoodInteractiveMode {
    PICNIC_TRAY,     // Lesson 1: I'd like some...
    REQUEST_DELIVER, // Lesson 4: Can I have some... ? Here you are.
    ACTION_MATCHER   // Lesson 5: Eat / Drink / Cut / Have
}

@Composable
fun EnglishFoodInteractiveView(
    mode: FoodInteractiveMode,
    currentExpression: EnglishExpression? = null,
    currentWord: EnglishWord? = null,
    ttsHelper: EnglishTTSHelper
) {
    // Core Food Database for interactive mapping
    val foods = listOf(
        FoodItem("bread", "面包", isDrink = false, defaultAction = "Eat"),
        FoodItem("juice", "果汁", isDrink = true, defaultAction = "Drink"),
        FoodItem("egg", "鸡蛋", isDrink = false, defaultAction = "Eat"),
        FoodItem("milk", "牛奶", isDrink = true, defaultAction = "Drink"),
        FoodItem("fish", "鱼肉", isDrink = false, defaultAction = "Eat"),
        FoodItem("rice", "米饭", isDrink = false, defaultAction = "Eat"),
        FoodItem("water", "水", isDrink = true, defaultAction = "Drink"),
        FoodItem("cake", "蛋糕", isDrink = false, defaultAction = "Cut")
    )

    when (mode) {
        FoodInteractiveMode.PICNIC_TRAY -> {
            PicnicTrayInteractive(currentExpression, foods, ttsHelper)
        }
        FoodInteractiveMode.REQUEST_DELIVER -> {
            RequestDeliverInteractive(currentExpression, foods, ttsHelper)
        }
        FoodInteractiveMode.ACTION_MATCHER -> {
            ActionMatcherInteractive(currentWord, foods, ttsHelper)
        }
    }
}

data class FoodItem(
    val name: String,
    val nameZh: String,
    val isDrink: Boolean,
    val defaultAction: String // Eat, Drink, Cut, Have
)

@Composable
fun PicnicTrayInteractive(
    expression: EnglishExpression?,
    foods: List<FoodItem>,
    ttsHelper: EnglishTTSHelper
) {
    val context = LocalContext.current
    val expText = expression?.englishText?.lowercase() ?: ""
    val targetFood = remember(expText) {
        foods.find { expText.contains(it.name) } ?: foods.first()
    }

    var selectedFoodName by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    // Reset state when expression changes
    LaunchedEffect(expression) {
        selectedFoodName = null
        isSubmitted = false
        isSuccess = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B0F19), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.5.dp, Color(0xFF1E293B)), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🌌 星语补给站 (Picnic Tray)",
            color = Color(0xFF00E5FF),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.testTag("picnic_tray_title")
        )

        // Customer voice / request prompt
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                .clickable { ttsHelper.speak(expression?.englishText ?: "", isSlow = false) }
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "顾客需求 (Click to Listen):",
                    color = Color(0xFFEC4899),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = expression?.englishText ?: "I'd like some...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = expression?.chineseTranslation ?: "",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }
        }

        // Picnic Tray plate visualizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Visual circles representing a physical tray/plate and glass
            Canvas(modifier = Modifier.fillMaxSize().testTag("picnic_canvas")) {
                // Outer tray
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    size = Size(size.width * 0.9f, size.height * 0.9f),
                    topLeft = Offset(size.width * 0.05f, size.height * 0.05f),
                    cornerRadius = CornerRadius(16f, 16f),
                    style = Stroke(width = 4f)
                )

                // Plate circle (left side)
                drawCircle(
                    color = Color(0xFF334155).copy(alpha = 0.4f),
                    radius = 55.dp.toPx(),
                    center = Offset(size.width * 0.35f, size.height * 0.5f)
                )
                drawCircle(
                    color = Color(0xFF334155).copy(alpha = 0.6f),
                    radius = 45.dp.toPx(),
                    center = Offset(size.width * 0.35f, size.height * 0.5f),
                    style = Stroke(width = 2f)
                )

                // Cup circle (right side)
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.08f),
                    radius = 35.dp.toPx(),
                    center = Offset(size.width * 0.75f, size.height * 0.5f)
                )
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.3f),
                    radius = 30.dp.toPx(),
                    center = Offset(size.width * 0.75f, size.height * 0.5f),
                    style = Stroke(width = 2f)
                )
            }

            // Food item placed on tray
            if (selectedFoodName != null) {
                val selectedFood = foods.first { it.name == selectedFoodName }
                val isDrink = selectedFood.isDrink
                Box(
                    modifier = Modifier
                        .align(if (isDrink) Alignment.CenterEnd else Alignment.CenterStart)
                        .padding(horizontal = if (isDrink) 48.dp else 40.dp)
                        .scale(1.2f)
                        .background(
                            if (isDrink) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFFEC4899).copy(alpha = 0.2f),
                            CircleShape
                        )
                        .border(
                            BorderStroke(
                                1.5.dp,
                                if (isDrink) Color(0xFF00E5FF) else Color(0xFFEC4899)
                            ), CircleShape
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (isDrink) "🥤" else "🍞",
                        fontSize = 24.sp
                    )
                }
            } else {
                Text(
                    text = "请将所需食物或饮品放入托盘中",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        // Selection Items FlowRow-like grids
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "食物与饮品补给舱 (Select Item):", color = Color.LightGray, fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                foods.take(4).forEach { item ->
                    val isSel = selectedFoodName == item.name
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) Color(0xFF1E293B) else Color(0xFF0B1220)
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            if (isSel) Color(0xFF00E5FF) else Color(0xFF334155).copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedFoodName = item.name
                                ttsHelper.speak(item.name, isSlow = false)
                            }
                            .padding(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = getFoodEmoji(item.name), fontSize = 20.sp)
                            Text(text = item.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = item.nameZh, color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                foods.drop(4).forEach { item ->
                    val isSel = selectedFoodName == item.name
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) Color(0xFF1E293B) else Color(0xFF0B1220)
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            if (isSel) Color(0xFF00E5FF) else Color(0xFF334155).copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedFoodName = item.name
                                ttsHelper.speak(item.name, isSlow = false)
                            }
                            .padding(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = getFoodEmoji(item.name), fontSize = 20.sp)
                            Text(text = item.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = item.nameZh, color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        // Action Buttons & Feedback loop
        if (isSubmitted) {
            val alertBg = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
            val alertBorder = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
            val alertText = if (isSuccess) "🎉 成功交付食物！ 顾客回应: \"Thank you!\"" else "❌ 分配食物错误，顾客表示不符合需求。"
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(alertBg, RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, alertBorder), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alertText,
                    color = alertBorder,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = {
                if (selectedFoodName == null) {
                    Toast.makeText(context, "请先选择需要补给的食物", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isSubmitted = true
                isSuccess = (selectedFoodName == targetFood.name)
                if (isSuccess) {
                    ttsHelper.speak("Here you are.", isSlow = false)
                } else {
                    ttsHelper.speak("no", isSlow = false)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSubmitted && isSuccess) Color(0xFF10B981) else Color(0xFFEC4899)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("picnic_submit_btn")
        ) {
            Text(
                text = if (isSubmitted && isSuccess) "交付完成 (Next)" else "递交食物 (Deliver)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RequestDeliverInteractive(
    expression: EnglishExpression?,
    foods: List<FoodItem>,
    ttsHelper: EnglishTTSHelper
) {
    val context = LocalContext.current
    val expText = expression?.englishText?.lowercase() ?: ""
    val targetFood = remember(expText) {
        foods.find { expText.contains(it.name) } ?: foods.first()
    }

    var selectedFoodName by remember { mutableStateOf<String?>(null) }
    var selectedResponseText by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    val responseOptions = listOf("Here you are.", "You're welcome.", "No, thanks.")

    // Reset state when expression changes
    LaunchedEffect(expression) {
        selectedFoodName = null
        selectedResponseText = null
        isSubmitted = false
        isSuccess = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B0F19), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.5.dp, Color(0xFF1E293B)), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📬 补给递交窗口 (Request & Deliver)",
            color = Color(0xFF00E5FF),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.testTag("deliver_window_title")
        )

        // Customer request prompt
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                .clickable { ttsHelper.speak(expression?.englishText ?: "", isSlow = false) }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🙋‍♂️", fontSize = 28.sp)
                Column {
                    Text(
                        text = "莎拉请求 (Sarah's Request):",
                        color = Color(0xFFEC4899),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = expression?.englishText ?: "Can I have some...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Horizontal Tray & Interactive bubble
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(text = "配送托盘 (Delivery Tray)", color = Color.Gray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (selectedFoodName != null) "已配给: ${getFoodEmoji(selectedFoodName!!)} $selectedFoodName" else "⚠️ 暂无食物",
                    color = if (selectedFoodName != null) Color(0xFF00E5FF) else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (selectedResponseText != null) "已选择答复: \"$selectedResponseText\"" else "⚠️ 暂无答复语",
                    color = if (selectedResponseText != null) Color(0xFFEC4899) else Color.Gray,
                    fontSize = 13.sp
                )
            }
            
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFF1E293B), CircleShape)
                    .border(BorderStroke(1.dp, Color(0xFF00E5FF)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛸", fontSize = 28.sp)
            }
        }

        // Selection 1: Choose Food
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "第一步：选择对应的食物", color = Color.LightGray, fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                foods.take(4).forEach { item ->
                    val isSel = selectedFoodName == item.name
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) Color(0xFF1E293B) else Color(0xFF0B1220)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSel) Color(0xFF00E5FF) else Color(0xFF334155).copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedFoodName = item.name
                                ttsHelper.speak(item.name, isSlow = false)
                            }
                    ) {
                        Box(modifier = Modifier.padding(6.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = getFoodEmoji(item.name), fontSize = 16.sp)
                                Text(text = item.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                foods.drop(4).forEach { item ->
                    val isSel = selectedFoodName == item.name
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) Color(0xFF1E293B) else Color(0xFF0B1220)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSel) Color(0xFF00E5FF) else Color(0xFF334155).copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedFoodName = item.name
                                ttsHelper.speak(item.name, isSlow = false)
                            }
                    ) {
                        Box(modifier = Modifier.padding(6.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = getFoodEmoji(item.name), fontSize = 16.sp)
                                Text(text = item.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Selection 2: Choose Phrase
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "第二步：选择合乎礼貌的英文配语", color = Color.LightGray, fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                responseOptions.forEach { opt ->
                    val isSel = selectedResponseText == opt
                    Button(
                        onClick = {
                            selectedResponseText = opt
                            ttsHelper.speak(opt, isSlow = false)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSel) Color(0xFFEC4899) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = opt, color = Color.White, fontSize = 10.sp, maxLines = 1)
                    }
                }
            }
        }

        // Feedback & Action buttons
        if (isSubmitted) {
            val isS = (selectedFoodName == targetFood.name && selectedResponseText == "Here you are.")
            val alertBg = if (isS) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
            val alertBorder = if (isS) Color(0xFF10B981) else Color(0xFFEF4444)
            val alertText = if (isS) "🎉 配送成功！ 对方说: \"Thank you!\" -> 回应: \"You're welcome!\"" else "❌ 配给失败！ 确保配送正确的食物且配对 \"Here you are.\""
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(alertBg, RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, alertBorder), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alertText,
                    color = alertBorder,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = {
                if (selectedFoodName == null || selectedResponseText == null) {
                    Toast.makeText(context, "请先选择食物和礼貌配配语", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isSubmitted = true
                isSuccess = (selectedFoodName == targetFood.name && selectedResponseText == "Here you are.")
                if (isSuccess) {
                    ttsHelper.speak("Here you are. Thank you. You're welcome.", isSlow = false)
                } else {
                    ttsHelper.speak("No", isSlow = false)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSubmitted && isSuccess) Color(0xFF10B981) else Color(0xFF00E5FF)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("deliver_submit_btn")
        ) {
            Text(
                text = if (isSubmitted && isSuccess) "全部配送完成" else "验证递交 (Verify Delivery)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSubmitted && isSuccess) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun ActionMatcherInteractive(
    word: EnglishWord?,
    foods: List<FoodItem>,
    ttsHelper: EnglishTTSHelper
) {
    val context = LocalContext.current
    val activeWordSpelling = word?.spelling?.lowercase() ?: ""
    val activeFoodItem = foods.find { activeWordSpelling.contains(it.name) } ?: foods.first()

    var selectedAction by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    val actionOptions = listOf("Eat", "Drink", "Cut")

    // Reset when word changes
    LaunchedEffect(word) {
        selectedAction = null
        isSubmitted = false
        isSuccess = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B0F19), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.5.dp, Color(0xFF1E293B)), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🧪 食物动作实验台 (Action Matcher)",
            color = Color(0xFF00E5FF),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.testTag("action_matcher_title")
        )

        // Target word presentation card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = getFoodEmoji(activeFoodItem.name), fontSize = 48.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = activeFoodItem.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = activeFoodItem.nameZh, color = Color.LightGray, fontSize = 14.sp)
            }
        }

        // Action Options Selector
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "选择匹配动作 (Select Action Instruction):", color = Color.LightGray, fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                actionOptions.forEach { action ->
                    val isSel = selectedAction == action
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) Color(0xFFEC4899) else Color(0xFF0F172A)
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            if (isSel) Color(0xFFEC4899) else Color(0xFF334155).copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedAction = action
                                ttsHelper.speak(action, isSlow = false)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = getActionEmoji(action), fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = action, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = getActionZh(action), color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Verify/Feedback panel
        if (isSubmitted) {
            val alertBg = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
            val alertBorder = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
            val alertText = if (isSuccess) {
                val sentence = if (activeFoodItem.name == "cake") "Cut the cake." else if (activeFoodItem.isDrink) "Drink some ${activeFoodItem.name}." else "Eat some ${activeFoodItem.name}."
                "🎉 完美匹配！ 动作口令: \"$sentence\""
            } else {
                "❌ 动作匹配错误！ 液体通常是 Drink，大口吃 is Eat，蛋糕是 Cut 喔。"
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(alertBg, RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, alertBorder), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alertText,
                    color = alertBorder,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = {
                if (selectedAction == null) {
                    Toast.makeText(context, "请先选择一个匹配动作", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isSubmitted = true
                isSuccess = (selectedAction == activeFoodItem.defaultAction)
                if (isSuccess) {
                    val sentence = if (activeFoodItem.name == "cake") "Cut the cake." else if (activeFoodItem.isDrink) "Drink some ${activeFoodItem.name}." else "Eat some ${activeFoodItem.name}."
                    ttsHelper.speak(sentence, isSlow = false)
                } else {
                    ttsHelper.speak("No", isSlow = false)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSubmitted && isSuccess) Color(0xFF10B981) else Color(0xFFEC4899)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("action_submit_btn")
        ) {
            Text(
                text = if (isSubmitted && isSuccess) "匹配成功" else "提交校验 (Match Action)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getFoodEmoji(name: String): String {
    return when (name.lowercase()) {
        "bread" -> "🍞"
        "juice" -> "🥤"
        "egg" -> "🥚"
        "milk" -> "🥛"
        "fish" -> "🐟"
        "rice" -> "🍚"
        "water" -> "💧"
        "cake" -> "🍰"
        else -> "🍔"
    }
}

fun getActionEmoji(action: String): String {
    return when (action.lowercase()) {
        "eat" -> "😋"
        "drink" -> "🥤"
        "cut" -> "🔪"
        else -> "👋"
    }
}

fun getActionZh(action: String): String {
    return when (action.lowercase()) {
        "eat" -> "吃"
        "drink" -> "喝"
        "cut" -> "切"
        else -> "做"
    }
}
