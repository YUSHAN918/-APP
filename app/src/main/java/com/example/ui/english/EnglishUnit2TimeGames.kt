package com.example.ui.english

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.*
import com.example.util.english.EnglishTTSHelper

@Composable
fun EnglishUnit2Lesson1InteractiveView(
    currentExpression: EnglishExpression,
    ttsHelper: EnglishTTSHelper
) {
    var activeCity by remember { mutableStateOf("Beijing") }
    var practiceTime by remember { mutableStateOf<EnglishClockTime>(EnglishClockTime(7, 0)) }
    var practiceFeedback by remember { mutableStateOf<Boolean?>(null) }

    // Textbook world clock cities (Static fixed data - strictly from textbook Page 13)
    val cities = listOf(
        Triple("Beijing", "北京", EnglishClockTime(8, 0, DayPeriod.PM)),
        Triple("London", "伦敦", EnglishClockTime(12, 0, DayPeriod.AM)),
        Triple("New York", "纽约", EnglishClockTime(7, 0, DayPeriod.AM)),
        Triple("Sydney", "悉尼", EnglishClockTime(10, 0, DayPeriod.PM))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🌍 教材世界时间卡 (Let's Play)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "点击城市，听听课本中各城市对应的时间问答：",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Horizontal list of city cards
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cities) { (city, cnName, time) ->
                val isSelected = activeCity == city
                Card(
                    modifier = Modifier
                        .width(100.dp)
                        .clickable {
                            activeCity = city
                            ttsHelper.speak("In $city, it's ${EnglishTimeFormatter.formatSpoken(time)}.", isSlow = false)
                        }
                        .testTag("world_city_card_$city"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = city, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = cnName, fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = EnglishTimeFormatter.formatDigital(time),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Analog Clock face linked to active city
        val activeCityTime = cities.find { it.first == activeCity }?.third ?: EnglishClockTime(8, 0)
        EnglishClockView(
            time = activeCityTime,
            isEditable = false,
            modifier = Modifier.width(240.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Time Match Challenge Section (Interactive Learning)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎯 钟面魔法调整师",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "请调整下方时钟，使其显示为: 7:20",
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                EnglishClockView(
                    time = practiceTime,
                    isEditable = true,
                    onTimeChanged = {
                        practiceTime = it
                        practiceFeedback = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val isCorrect = practiceTime.hour == 7 && practiceTime.minute == 20
                        practiceFeedback = isCorrect
                        if (isCorrect) {
                            ttsHelper.speak("Seven twenty. It's seven twenty. Excellent!", isSlow = false)
                        } else {
                            ttsHelper.speak("That is not correct. Try again.", isSlow = false)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_clock_match_button")
                ) {
                    Text("校验答案", fontWeight = FontWeight.Bold)
                }

                AnimatedVisibility(visible = practiceFeedback != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (practiceFeedback == true) Color(0xFFDEF7EC) else Color(0xFFFDE8E8)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (practiceFeedback == true) Color(0xFF0E9F6E) else Color(0xFFF05252)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (practiceFeedback == true) "🎉 完全正确！时针与分针对齐了！" else "❌ 时间还没调对，继续试试看吧！",
                            color = if (practiceFeedback == true) Color(0xFF03543F) else Color(0xFF9B1C1C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnglishUnit2Lesson4InteractiveView(
    currentExpression: EnglishExpression,
    ttsHelper: EnglishTTSHelper
) {
    var selectedGrammar by remember { mutableStateOf<TimeGrammarMode?>(null) }
    var activeStep by remember { mutableStateOf(0) }
    var stepFeedback by remember { mutableStateOf<Boolean?>(null) }

    // Sequential Morning routines (B Let's Talk P16)
    val steps = listOf(
        Triple("get_up", "get up (起床)", TimeGrammarMode.TIME_TO_VERB),
        Triple("breakfast", "breakfast (吃早餐)", TimeGrammarMode.TIME_FOR_NOUN),
        Triple("go_to_school", "go to school (去上学)", TimeGrammarMode.TIME_TO_VERB)
    )

    val activeRoutine = steps[activeStep]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⏰ 晨间时间与语法冒险",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LinearProgressIndicator(
            progress = (activeStep + 1).toFloat() / steps.size,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "情境描述: ${activeRoutine.second}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "It's time ____________ ${activeRoutine.first.replace("_", " ")}.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            selectedGrammar = TimeGrammarMode.TIME_FOR_NOUN
                            ttsHelper.speak("for", isSlow = false)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedGrammar == TimeGrammarMode.TIME_FOR_NOUN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (selectedGrammar == TimeGrammarMode.TIME_FOR_NOUN) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                            .testTag("for_noun_button")
                    ) {
                        Text("for (接名词/课程/三餐)", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            selectedGrammar = TimeGrammarMode.TIME_TO_VERB
                            ttsHelper.speak("to", isSlow = false)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedGrammar == TimeGrammarMode.TIME_TO_VERB) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (selectedGrammar == TimeGrammarMode.TIME_TO_VERB) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                            .testTag("to_verb_button")
                    ) {
                        Text("to (接动词/日常活动)", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val isCorrect = selectedGrammar == activeRoutine.third
                        stepFeedback = isCorrect
                        if (isCorrect) {
                            ttsHelper.speak("It's time ${if (isCorrect && selectedGrammar == TimeGrammarMode.TIME_TO_VERB) "to" else "for"} ${activeRoutine.first.replace("_", " ")}.", isSlow = false)
                        } else {
                            ttsHelper.speak("Oops! Try again.", isSlow = false)
                        }
                    },
                    enabled = selectedGrammar != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_grammar_choice_button")
                ) {
                    Text("验证并提交", fontWeight = FontWeight.Bold)
                }

                AnimatedVisibility(visible = stepFeedback != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (stepFeedback == true) Color(0xFFDEF7EC) else Color(0xFFFDE8E8)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (stepFeedback == true) Color(0xFF0E9F6E) else Color(0xFFF05252)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (stepFeedback == true) "🎉 选对啦！完美符合英语语法！" else "❌ 哎呀，搭配不正确！请区分 noun 与 verb 的搭配关系。",
                                color = if (stepFeedback == true) Color(0xFF03543F) else Color(0xFF9B1C1C),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (stepFeedback == true) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (activeStep < steps.size - 1) {
                                        activeStep++
                                        selectedGrammar = null
                                        stepFeedback = null
                                    } else {
                                        // Reset to first
                                        activeStep = 0
                                        selectedGrammar = null
                                        stepFeedback = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text(if (activeStep < steps.size - 1) "下一关 (Next)" else "重新开始 (Restart)")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnglishUnit2Lesson5InteractiveView(
    currentExpression: EnglishExpression,
    ttsHelper: EnglishTTSHelper
) {
    // Standard virtual daily schedule based on textbook (Page 17)
    val initialItems: List<ScheduleItem> = remember {
        listOf(
            ScheduleItem("item1", "get up (起床)", DailyActivityType.GET_UP, EnglishClockTime(6, 30), TimeGrammarMode.TIME_TO_VERB),
            ScheduleItem("item2", "breakfast (吃早餐)", DailyActivityType.BREAKFAST, EnglishClockTime(7, 0), TimeGrammarMode.TIME_FOR_NOUN),
            ScheduleItem("item3", "go to school (去上学)", DailyActivityType.GO_TO_SCHOOL, EnglishClockTime(7, 20), TimeGrammarMode.TIME_TO_VERB),
            ScheduleItem("item4", "English class (英语课)", DailyActivityType.ENGLISH_CLASS, EnglishClockTime(10, 40), TimeGrammarMode.TIME_FOR_NOUN),
            ScheduleItem("item5", "lunch (吃午餐)", DailyActivityType.LUNCH, EnglishClockTime(12, 0), TimeGrammarMode.TIME_FOR_NOUN),
            ScheduleItem("item6", "music class (音乐课)", DailyActivityType.MUSIC_CLASS, EnglishClockTime(2, 15), TimeGrammarMode.TIME_FOR_NOUN),
            ScheduleItem("item7", "PE class (体育课)", DailyActivityType.PE_CLASS, EnglishClockTime(3, 0), TimeGrammarMode.TIME_FOR_NOUN),
            ScheduleItem("item8", "go home (回家)", DailyActivityType.GO_HOME, EnglishClockTime(4, 50), TimeGrammarMode.TIME_TO_VERB),
            ScheduleItem("item9", "go to bed (睡觉)", DailyActivityType.GO_TO_BED, EnglishClockTime(9, 0), TimeGrammarMode.TIME_TO_VERB)
        ).shuffled()
    }

    var scheduleItems: List<ScheduleItem> by remember { mutableStateOf(initialItems) }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📋 虚拟角色作息整理工坊",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "请点击 ↑ 或 ↓ 箭头，按从早到晚的真实作息时间顺序排列日程：",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        EnglishScheduleView(
            items = scheduleItems,
            isSortable = true,
            onMoveItem = { from, to ->
                val list = scheduleItems.toMutableList()
                val item = list.removeAt(from)
                list.add(to, item)
                scheduleItems = list
                isCorrect = null
            },
            onVerify = {
                // Verify chronological order based on normalized minute of day
                var correct = true
                for (i in 0 until scheduleItems.size - 1) {
                    if (scheduleItems[i].time.normalizedMinuteOfDay > scheduleItems[i+1].time.normalizedMinuteOfDay) {
                        correct = false
                        break
                    }
                }
                isCorrect = correct
                if (correct) {
                    ttsHelper.speak("Great job! The virtual schedule order is perfectly correct.", isSlow = false)
                } else {
                    ttsHelper.speak("The schedule is not in correct order. Please check again.", isSlow = false)
                }
            },
            isCorrect = isCorrect
        )
    }
}
