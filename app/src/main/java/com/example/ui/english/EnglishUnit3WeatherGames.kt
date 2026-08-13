package com.example.ui.english

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.*
import com.example.util.english.EnglishTTSHelper

@Composable
fun EnglishUnit3Lesson1WeatherCard(
    currentExpression: EnglishExpression,
    ttsHelper: EnglishTTSHelper
) {
    // A Let's play / Let's talk interactive simulation
    var selectedTemperatureType by remember { mutableStateOf("warm") } // cold, cool, warm, hot
    var actionFeedback by remember { mutableStateOf<String?>(null) }
    var actionIsAllowed by remember { mutableStateOf<Boolean?>(null) }

    // Temperature mapping (in Celsius degrees)
    val tempDegrees = when (selectedTemperatureType) {
        "cold" -> "2°C"
        "cool" -> "15°C"
        "warm" -> "22°C"
        "hot" -> "35°C"
        else -> "22°C"
    }

    val tempCn = when (selectedTemperatureType) {
        "cold" -> "寒冷 (Cold)"
        "cool" -> "凉爽 (Cool)"
        "warm" -> "温暖 (Warm)"
        "hot" -> "炎热 (Hot)"
        else -> "温暖"
    }

    val themeColor = when (selectedTemperatureType) {
        "cold" -> Color(0xFF29B6F6)
        "cool" -> Color(0xFF26A69A)
        "warm" -> Color(0xFFFFA726)
        "hot" -> Color(0xFFEF5350)
        else -> Color(0xFFFFA726)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🌡️ 课本气象控制仪 (Let's Play)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "点击切换不同的天气温度，再尝试请求是否能出门吧！",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Weather type selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("cold", "cool", "warm", "hot").forEach { type ->
                val isSelected = selectedTemperatureType == type
                Button(
                    onClick = {
                        selectedTemperatureType = type
                        actionFeedback = null
                        actionIsAllowed = null
                        ttsHelper.speak("It's $type.", isSlow = false)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("temp_selector_$type"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) themeColor else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = when (type) {
                            "cold" -> "Cold"
                            "cool" -> "Cool"
                            "warm" -> "Warm"
                            "hot" -> "Hot"
                            else -> ""
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Status Circle showing temperature and visual state
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(80.dp))
                .background(themeColor.copy(alpha = 0.15f))
                .clickable {
                    ttsHelper.speak("It's $selectedTemperatureType. It's $tempDegrees.", isSlow = false)
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // simple visual feedback icon
                Icon(
                    imageVector = when (selectedTemperatureType) {
                        "cold" -> Icons.Default.AcUnit
                        "cool" -> Icons.Default.Cloud
                        "warm" -> Icons.Default.WbSunny
                        "hot" -> Icons.Default.WbSunny
                        else -> Icons.Default.WbSunny
                    },
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tempDegrees,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
                Text(
                    text = tempCn,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ask "Can I go outside now?" Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💬 句型问答模拟器 (Ask Mom/Dad)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val allows = selectedTemperatureType == "warm" || selectedTemperatureType == "cool"
                        actionIsAllowed = allows
                        if (allows) {
                            actionFeedback = "Yes, you can. It's $selectedTemperatureType."
                            ttsHelper.speak("Mom, can I go outside now? Yes, you can. It's $selectedTemperatureType.", isSlow = false)
                        } else {
                            if (selectedTemperatureType == "cold") {
                                actionFeedback = "No, you can't. It's cold outside. Be careful!"
                                ttsHelper.speak("Mom, can I go outside now? No, you can't. It's cold outside. Be careful!", isSlow = false)
                            } else {
                                actionFeedback = "No, you can't. It's too hot outside!"
                                ttsHelper.speak("Mom, can I go outside now? No, you can't. It's too hot outside!", isSlow = false)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ask_go_outside_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("问家长: Can I go outside now?", color = Color.White)
                }

                AnimatedVisibility(visible = actionFeedback != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(
                                color = if (actionIsAllowed == true) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (actionIsAllowed == true) "✅ 家长回答: 可以出门" else "❌ 家长回答: 无法出门",
                            fontWeight = FontWeight.Bold,
                            color = if (actionIsAllowed == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = actionFeedback ?: "",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnglishUnit3Lesson4WeatherCard(
    currentExpression: EnglishExpression,
    ttsHelper: EnglishTTSHelper
) {
    // B Let's play: World Weather Reporter
    var selectedCity by remember { mutableStateOf("Singapore") }
    
    // Five standard textbook cities (Page 27)
    val worldWeatherList = listOf(
        WorldCityWeather("Singapore", "新加坡", "sunny", "32"),
        WorldCityWeather("Sydney", "悉尼", "windy", "24"),
        WorldCityWeather("New York", "纽约", "rainy", "15"),
        WorldCityWeather("London", "伦敦", "cloudy", "12"),
        WorldCityWeather("Moscow", "莫斯科", "snowy", "-5")
    )

    val currentCityData = worldWeatherList.find { it.cityName == selectedCity } ?: worldWeatherList[0]

    // Practice Challenge Game
    var targetCity by remember { mutableStateOf(worldWeatherList.random().cityName) }
    var userWeatherGuess by remember { mutableStateOf("") }
    var userTempGuess by remember { mutableStateOf("") }
    var challengeFeedback by remember { mutableStateOf<Boolean?>(null) }

    fun refreshChallenge() {
        val nextList = worldWeatherList.filter { it.cityName != targetCity }
        targetCity = if (nextList.isNotEmpty()) nextList.random().cityName else worldWeatherList.random().cityName
        userWeatherGuess = ""
        userTempGuess = ""
        challengeFeedback = null
    }

    val targetCityData = worldWeatherList.find { it.cityName == targetCity } ?: worldWeatherList[0]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🌍 世界天气大连线 (World Weather)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "点击城市听听那里的天气播报，再完成下方小小气象员测试吧：",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Cities lazy row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(worldWeatherList) { data ->
                val isSelected = selectedCity == data.cityName
                Card(
                    modifier = Modifier
                        .width(96.dp)
                        .clickable {
                            selectedCity = data.cityName
                            ttsHelper.speak("In ${data.cityName}, it's ${data.weather} today. It's ${data.temperature} degrees.", isSlow = false)
                        }
                        .testTag("world_weather_city_${data.cityName}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = data.cityName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = data.cnName, fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = when (data.weather) {
                                "sunny" -> Icons.Default.WbSunny
                                "rainy" -> Icons.Default.Umbrella
                                "cloudy" -> Icons.Default.Cloud
                                "snowy" -> Icons.Default.AcUnit
                                "windy" -> Icons.Default.Air
                                else -> Icons.Default.WbSunny
                            },
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${data.temperature}°C",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large broadcast board for selected city
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "📡 实时天气播报",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${currentCityData.cityName} (${currentCityData.cnName})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "It's ${currentCityData.weather}.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "It's ${currentCityData.temperature} degrees.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = when (currentCityData.weather) {
                        "sunny" -> Icons.Default.WbSunny
                        "rainy" -> Icons.Default.Umbrella
                        "cloudy" -> Icons.Default.Cloud
                        "snowy" -> Icons.Default.AcUnit
                        "windy" -> Icons.Default.Air
                        else -> Icons.Default.WbSunny
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Challenge Game: Weather Reporter Test
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎤 挑战：气象播报员测试",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "课本听力提示：请选择 $targetCity (${targetCityData.cnName}) 的天气和温度：",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Weather selection buttons
                Text(text = "1. 选择天气:", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("sunny", "cloudy", "rainy", "snowy", "windy").forEach { weatherOption ->
                        val isSelected = userWeatherGuess == weatherOption
                        Button(
                            onClick = { userWeatherGuess = weatherOption },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("challenge_weather_$weatherOption"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = weatherOption,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Temperature selection buttons
                Text(text = "2. 选择温度 (摄氏度):", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("32", "24", "15", "12", "-5").forEach { tempOption ->
                        val isSelected = userTempGuess == tempOption
                        Button(
                            onClick = { userTempGuess = tempOption },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("challenge_temp_$tempOption"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$tempOption°C",
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { refreshChallenge() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("weather_challenge_refresh"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("换一个城市", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            val isCorrect = userWeatherGuess == targetCityData.weather && userTempGuess == targetCityData.temperature
                            challengeFeedback = isCorrect
                            if (isCorrect) {
                                ttsHelper.speak("Excellent! In $targetCity, it's $userWeatherGuess today. It's $userTempGuess degrees.", isSlow = false)
                            } else {
                                ttsHelper.speak("That is not correct. Check the textbook map again.", isSlow = false)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("weather_challenge_verify"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("校验播报答案", color = Color.White)
                    }
                }

                AnimatedVisibility(visible = challengeFeedback != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(
                                color = if (challengeFeedback == true) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (challengeFeedback == true) "🎉 校验成功！你是优秀的气象播报员！" else "❌ 播报错误，温度或气候与课本不匹配",
                            fontWeight = FontWeight.Bold,
                            color = if (challengeFeedback == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

data class WorldCityWeather(
    val cityName: String,
    val cnName: String,
    val weather: String,
    val temperature: String
)
