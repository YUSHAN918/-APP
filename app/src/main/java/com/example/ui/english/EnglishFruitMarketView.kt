package com.example.ui.english

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.*
import com.example.util.english.EnglishTTSHelper

enum class FruitMarketMode {
    MARKET_PREFERENCE, // Lesson 1: Do you like...? Yes, I do / No, I don't
    PICNIC_SHARING,    // Lesson 4: Have some... / Can I have some...? / Here you are / Thanks
    BASKET_COMBO       // Lesson 5: Fruit Basket combo
}

data class FruitItemDisplay(
    val id: String,
    val spelling: String,
    val pluralSpelling: String,
    val nameZh: String,
    val emoji: String,
    val color: Color
)

@Composable
fun EnglishFruitMarketView(
    mode: FruitMarketMode,
    currentExpression: EnglishExpression? = null,
    currentWord: EnglishWord? = null,
    ttsHelper: EnglishTTSHelper,
    onInteractionCompleted: (() -> Unit)? = null
) {
    val fruitList = remember {
        listOf(
            FruitItemDisplay("pear", "pear", "pears", "梨", "🍐", Color(0xFFFEF08A)),
            FruitItemDisplay("apple", "apple", "apples", "苹果", "🍎", Color(0xFFFECACA)),
            FruitItemDisplay("orange", "orange", "oranges", "橙子", "🍊", Color(0xFFFFEDD5)),
            FruitItemDisplay("banana", "banana", "bananas", "香蕉", "🍌", Color(0xFFFEF9C3)),
            FruitItemDisplay("watermelon", "watermelon", "watermelons", "西瓜", "🍉", Color(0xFFDCFCE7)),
            FruitItemDisplay("strawberry", "strawberry", "strawberries", "草莓", "🍓", Color(0xFFFFE4E6)),
            FruitItemDisplay("grape", "grape", "grapes", "葡萄", "🍇", Color(0xFFF3E8FF))
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, Color(0xFFFDE68A)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("fruit_market_view")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (mode) {
                FruitMarketMode.MARKET_PREFERENCE -> {
                    MarketPreferenceInteractive(currentExpression, fruitList, ttsHelper, onInteractionCompleted)
                }
                FruitMarketMode.PICNIC_SHARING -> {
                    PicnicSharingInteractive(currentExpression, fruitList, ttsHelper, onInteractionCompleted)
                }
                FruitMarketMode.BASKET_COMBO -> {
                    BasketComboInteractive(currentWord, fruitList, ttsHelper, onInteractionCompleted)
                }
            }
        }
    }
}

@Composable
private fun MarketPreferenceInteractive(
    expression: EnglishExpression?,
    fruitList: List<FruitItemDisplay>,
    ttsHelper: EnglishTTSHelper,
    onInteractionCompleted: (() -> Unit)?
) {
    val text = expression?.englishText ?: "Do you like apples?"
    val id = expression?.expressionId

    val isGeneralFruit = remember(expression) {
        id == "g3s2_u5_exp1" || text.lowercase().contains("buy some fruit")
    }

    val targetFruit = remember(expression) {
        val resolved = fruitList.find { text.lowercase().contains(it.spelling) || text.lowercase().contains(it.pluralSpelling) }
        if (resolved != null) {
            resolved
        } else {
            when (id) {
                "g3s2_u5_exp3" -> fruitList.find { it.id == "orange" } ?: fruitList.first()
                "g3s2_u5_exp5", "g3s2_u5_exp6", "g3s2_u5_exp7" -> fruitList.find { it.id == "apple" } ?: fruitList.first()
                else -> fruitList.first()
            }
        }
    }

    val isDisliked = remember(expression) {
        text.lowercase().contains("don't like") || text.lowercase().contains("don’t like") || id == "g3s2_u5_exp3"
    }

    val isLiked = remember(expression) {
        text.lowercase().contains("yes, i do") || id == "g3s2_u5_exp5" || id == "g3s2_u5_exp6" || id == "g3s2_u5_exp7"
    }

    var userSelectedChoice by remember(expression) { mutableStateOf<String?>(null) }
    var isAnswered by remember(expression) { mutableStateOf(false) }

    val isAsking = text.lowercase().contains("do you like")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "🧺", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "卡通水果集市 · 喜好问答",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF92400E)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cartoon Fruit Display Box
        val boxModifier = if (isGeneralFruit) {
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFFD3B6))))
                .padding(16.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(targetFruit.color)
                .padding(16.dp)
        }

        Box(
            modifier = boxModifier,
            contentAlignment = Alignment.Center
        ) {
            if (isGeneralFruit) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🍎🍊🍇🍌🍓🍒🍐🍉", fontSize = 36.sp, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "fruit (水果)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78350F)
                    )
                    Text(
                        text = "各种各样美味丰盛的水果摊 🛒",
                        fontSize = 13.sp,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = targetFruit.emoji, fontSize = 64.sp)
                        if (isDisliked) {
                            Text(
                                text = "😢❌",
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 12.dp, y = 12.dp)
                            )
                        } else if (isLiked) {
                            Text(
                                text = "😋❤️",
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 12.dp, y = 12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${targetFruit.spelling} (${targetFruit.nameZh})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = if (isDisliked) "不喜欢这个水果 😢 ❌" else if (isLiked) "非常喜欢这个水果！ 😋 ❤️" else "复数: ${targetFruit.pluralSpelling}",
                        fontSize = 13.sp,
                        color = if (isDisliked) Color(0xFFDC2626) else if (isLiked) Color(0xFF16A34A) else Color(0xFF64748B),
                        fontWeight = if (isDisliked || isLiked) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Question / Dialogue Box
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFFCD34D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expression?.englishText ?: "Do you like apples?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    Text(
                        text = expression?.chineseTranslation ?: "你喜欢苹果吗？",
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563)
                    )
                }
                IconButton(
                    onClick = { ttsHelper.speak(expression?.englishText ?: "Do you like apples?") },
                    modifier = Modifier.background(Color(0xFFFEF3C7), CircleShape)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "朗读", tint = Color(0xFFD97706))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isAsking) {
            Text(
                text = "请根据情境选择回答：",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF78350F)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        userSelectedChoice = "Yes, I do."
                        isAnswered = true
                        ttsHelper.speak("Yes, I do.")
                        onInteractionCompleted?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("fruit_market_yes_button")
                ) {
                    Text("Yes, I do. (喜欢)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        userSelectedChoice = "No, I don't."
                        isAnswered = true
                        ttsHelper.speak("No, I don't.")
                        onInteractionCompleted?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("fruit_market_no_button")
                ) {
                    Text("No, I don't. (不喜欢)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isAnswered) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFDCFCE7))
                    .padding(10.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "回答已提交：$userSelectedChoice",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D)
                )
            }
        }
    }
}

@Composable
private fun PicnicSharingInteractive(
    expression: EnglishExpression?,
    fruitList: List<FruitItemDisplay>,
    ttsHelper: EnglishTTSHelper,
    onInteractionCompleted: (() -> Unit)?
) {
    val text = expression?.englishText ?: "Have some grapes."
    val id = expression?.expressionId

    val targetFruit = remember(expression) {
        val resolved = fruitList.find { text.lowercase().contains(it.spelling) || text.lowercase().contains(it.pluralSpelling) }
        if (resolved != null) {
            resolved
        } else {
            when (id) {
                "g3s2_u5_exp12" -> fruitList.find { it.id == "watermelon" } ?: fruitList.first()
                else -> fruitList.first()
            }
        }
    }

    val isDisliked = remember(expression) {
        text.lowercase().contains("don't like") || text.lowercase().contains("don’t like") || id == "g3s2_u5_exp9" || id == "g3s2_u5_exp11" || id == "g3s2_u5_exp12"
    }

    val isLiked = remember(expression) {
        text.lowercase().contains("have some") || text.lowercase().contains("can i have") || id == "g3s2_u5_exp8" || id == "g3s2_u5_exp10"
    }

    var actionState by remember(expression) { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "🍉", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "伙伴野餐会 · 礼貌分享",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF065F46)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Picnic Cartoon Fruit Display Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(targetFruit.color)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = targetFruit.emoji, fontSize = 64.sp)
                    if (isDisliked) {
                        Text(
                            text = "😢❌",
                            fontSize = 28.sp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 12.dp, y = 12.dp)
                        )
                    } else if (isLiked) {
                        Text(
                            text = "😋✨",
                            fontSize = 28.sp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 12.dp, y = 12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${targetFruit.spelling} (${targetFruit.nameZh})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = if (isDisliked) "野餐会小伙伴：不喜欢这个水果 ❌" else if (isLiked) "野餐会小伙伴：非常乐意分享此水果！ ✨" else "复数: ${targetFruit.pluralSpelling}",
                    fontSize = 13.sp,
                    color = if (isDisliked) Color(0xFFDC2626) else if (isLiked) Color(0xFF047857) else Color(0xFF64748B),
                    fontWeight = if (isDisliked || isLiked) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expression?.englishText ?: "Have some grapes.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )
                    Text(
                        text = expression?.chineseTranslation ?: "吃些葡萄吧。",
                        fontSize = 13.sp,
                        color = Color(0xFF047857)
                    )
                }
                IconButton(
                    onClick = { ttsHelper.speak(expression?.englishText ?: "Have some grapes.") },
                    modifier = Modifier.background(Color(0xFFD1FAE5), CircleShape)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "朗读", tint = Color(0xFF059669))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    actionState = "Here you are."
                    ttsHelper.speak("Here you are.")
                    onInteractionCompleted?.invoke()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Here you are. (给你)", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    actionState = "Thanks."
                    ttsHelper.speak("Thanks.")
                    onInteractionCompleted?.invoke()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Thanks. (谢谢)", fontSize = 12.sp)
            }
        }

        if (actionState != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "互动回应: $actionState",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF047857)
            )
        }
    }
}

@Composable
private fun BasketComboInteractive(
    word: EnglishWord?,
    fruitList: List<FruitItemDisplay>,
    ttsHelper: EnglishTTSHelper,
    onInteractionCompleted: (() -> Unit)?
) {
    var selectedFruitIds by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "🧺", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "虚拟果篮搭配工坊",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB45309)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "轻触水果放入果篮，学习 10 字母单词与单复数变化：",
            fontSize = 13.sp,
            color = Color(0xFF78350F),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            items(fruitList) { fruit ->
                val isSelected = selectedFruitIds.contains(fruit.id)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFFEF08A) else Color.White
                    ),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) Color(0xFFD97706) else Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clickable {
                            ttsHelper.speak(fruit.spelling)
                            selectedFruitIds = if (isSelected) {
                                selectedFruitIds - fruit.id
                            } else {
                                selectedFruitIds + fruit.id
                            }
                            if (selectedFruitIds.isNotEmpty()) {
                                onInteractionCompleted?.invoke()
                            }
                        }
                        .padding(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = fruit.emoji, fontSize = 28.sp)
                        Text(
                            text = fruit.spelling,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = fruit.pluralSpelling,
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        if (selectedFruitIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "果篮内已包含: ${selectedFruitIds.joinToString(", ") { id -> fruitList.find { it.id == id }?.pluralSpelling ?: id }}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB45309)
            )
        }
    }
}
