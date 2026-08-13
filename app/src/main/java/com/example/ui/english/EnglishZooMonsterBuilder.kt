package com.example.ui.english

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.EnglishExpression
import com.example.util.english.EnglishTTSHelper

data class MonsterFeatureModel(
    val monsterName: String = "Spooky",
    val eyeSize: String = "small",   // big, small
    val earSize: String = "big",     // big, small
    val noseLength: String = "long",  // long, short
    val armLength: String = "short",  // long, short
    val tailLength: String = "short"  // long, short
) {
    fun generateDescription(): String {
        return "It has $eyeSize eyes, $earSize ears, a $noseLength nose, $armLength arms, and a $tailLength tail."
    }
}

@Composable
fun EnglishZooMonsterBuilder(
    currentExpression: EnglishExpression? = null,
    ttsHelper: EnglishTTSHelper
) {
    val context = LocalContext.current
    
    // Target configuration for quiz
    val quizTarget = remember(currentExpression) {
        val expText = currentExpression?.englishText?.lowercase() ?: ""
        MonsterFeatureModel(
            monsterName = "Target Monster",
            eyeSize = if (expText.contains("big eyes")) "big" else "small",
            earSize = if (expText.contains("small ears")) "small" else "big",
            noseLength = if (expText.contains("short nose")) "short" else "long",
            armLength = if (expText.contains("long arms")) "long" else "short",
            tailLength = if (expText.contains("long tail")) "long" else "short"
        )
    }

    var currentMonster by remember { mutableStateOf(MonsterFeatureModel()) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

    // Reset state when expression changes
    LaunchedEffect(currentExpression) {
        currentMonster = MonsterFeatureModel()
        isSubmitted = false
        isCorrect = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.5.dp, Color(0xFF1E293B)), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("monster_builder_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Playful M3 Title
        Text(
            text = "👾 创意怪兽定制拼装工坊 (Make a Monster)",
            color = Color(0xFF10B981),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.testTag("monster_title")
        )

        // Dialogue target instructions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.2.dp, Color(0xFF10B981).copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                .clickable { ttsHelper.speak(currentExpression?.englishText ?: "", isSlow = false) }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "播放", tint = Color(0xFF10B981))
                Column {
                    Text(
                        text = "调整下方属性，拼接出符合此描述的怪兽 (Target Monster Description):",
                        color = Color(0xFF10B981),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentExpression?.englishText ?: "It has small eyes and big ears.",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentExpression?.chineseTranslation ?: "",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Live Monster Rendering (Adaptive Compose Canvas & Row representations)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Ears + Head Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Left Ear
                    Text(
                        text = "👂",
                        fontSize = if (currentMonster.earSize == "big") 36.sp else 18.sp,
                        modifier = Modifier.testTag("monster_left_ear")
                    )
                    
                    // Main Head & Face
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(Color(0xFF8B5CF6), CircleShape)
                            .border(BorderStroke(2.dp, Color.White), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Eyes
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "👁️", fontSize = if (currentMonster.eyeSize == "big") 24.sp else 12.sp)
                                Text(text = "👁️", fontSize = if (currentMonster.eyeSize == "big") 24.sp else 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Nose
                            Text(
                                text = "👃",
                                fontSize = if (currentMonster.noseLength == "long") 22.sp else 11.sp,
                                modifier = Modifier.testTag("monster_nose")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Mouth
                            Text(text = "👄", fontSize = 14.sp)
                        }
                    }

                    // Right Ear
                    Text(
                        text = "👂",
                        fontSize = if (currentMonster.earSize == "big") 36.sp else 18.sp,
                        modifier = Modifier.testTag("monster_right_ear")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Arms & Body & Tail
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left Arm
                    Text(
                        text = "💪",
                        fontSize = if (currentMonster.armLength == "long") 24.sp else 14.sp,
                        modifier = Modifier.testTag("monster_left_arm")
                    )

                    // Tail
                    Text(
                        text = "🦎",
                        fontSize = if (currentMonster.tailLength == "long") 24.sp else 14.sp,
                        modifier = Modifier.testTag("monster_tail")
                    )

                    // Right Arm
                    Text(
                        text = "💪",
                        fontSize = if (currentMonster.armLength == "long") 24.sp else 14.sp,
                        modifier = Modifier.testTag("monster_right_arm")
                    )
                }
            }
        }

        // Live Generated Sentence Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .clickable { ttsHelper.speak(currentMonster.generateDescription(), isSlow = false) }
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "播放拼装描述", tint = Color.LightGray)
                Column {
                    Text(text = "当前拼接怪兽特征描述 (Your Monster):", color = Color.Gray, fontSize = 10.sp)
                    Text(
                        text = currentMonster.generateDescription(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Selector controls - minimum touch target size 48dp
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "怪兽部位属性调整 (Monster Parameter Controls):", color = Color.LightGray, fontSize = 11.sp)
            
            // Row 1: Eyes & Ears
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Eyes Size Toggle
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clickable {
                            val newEye = if (currentMonster.eyeSize == "small") "big" else "small"
                            currentMonster = currentMonster.copy(eyeSize = newEye)
                            ttsHelper.speak("$newEye eyes", isSlow = false)
                        }
                        .testTag("monster_eye_toggle"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "👁️ 眼睛大小 (Eyes)", color = Color.White, fontSize = 11.sp)
                        Text(
                            text = if (currentMonster.eyeSize == "big") "【大 (BIG)】" else "【小 (SMALL)】",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Ears Size Toggle
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clickable {
                            val newEar = if (currentMonster.earSize == "small") "big" else "small"
                            currentMonster = currentMonster.copy(earSize = newEar)
                            ttsHelper.speak("$newEar ears", isSlow = false)
                        }
                        .testTag("monster_ear_toggle"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "👂 耳朵大小 (Ears)", color = Color.White, fontSize = 11.sp)
                        Text(
                            text = if (currentMonster.earSize == "big") "【大 (BIG)】" else "【小 (SMALL)】",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Row 2: Nose & Arms
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nose Length Toggle
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clickable {
                            val newNose = if (currentMonster.noseLength == "short") "long" else "short"
                            currentMonster = currentMonster.copy(noseLength = newNose)
                            ttsHelper.speak("$newNose nose", isSlow = false)
                        }
                        .testTag("monster_nose_toggle"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "👃 鼻子长短 (Nose)", color = Color.White, fontSize = 11.sp)
                        Text(
                            text = if (currentMonster.noseLength == "long") "【长 (LONG)】" else "【短 (SHORT)】",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Arms Length Toggle
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clickable {
                            val newArm = if (currentMonster.armLength == "short") "long" else "short"
                            currentMonster = currentMonster.copy(armLength = newArm)
                            ttsHelper.speak("$newArm arms", isSlow = false)
                        }
                        .testTag("monster_arm_toggle"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "💪 手臂长短 (Arms)", color = Color.White, fontSize = 11.sp)
                        Text(
                            text = if (currentMonster.armLength == "long") "【长 (LONG)】" else "【短 (SHORT)】",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Row 3: Tail Length
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tail Length Toggle
                Card(
                    modifier = Modifier
                        .weight(1.0f)
                        .height(54.dp)
                        .clickable {
                            val newTail = if (currentMonster.tailLength == "short") "long" else "short"
                            currentMonster = currentMonster.copy(tailLength = newTail)
                            ttsHelper.speak("$newTail tail", isSlow = false)
                        }
                        .testTag("monster_tail_toggle"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🦎 尾巴长短 (Tail)", color = Color.White, fontSize = 11.sp)
                        Text(
                            text = if (currentMonster.tailLength == "long") "【长 (LONG)】" else "【短 (SHORT)】",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quiz feedback panel
        if (isSubmitted) {
            val feedbackBg = if (isCorrect) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
            val feedbackBorder = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
            val feedbackText = if (isCorrect) "🎉 太棒了！怪兽特征完全与句子描述吻合！" else "❌ 特征不匹配。对照目标：\n${currentExpression?.englishText ?: ""}"
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(feedbackBg, RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, feedbackBorder), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = feedbackText,
                    color = feedbackBorder,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reset Button
            Button(
                onClick = {
                    currentMonster = MonsterFeatureModel()
                    isSubmitted = false
                    isCorrect = false
                    ttsHelper.speak("Reset", isSlow = false)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(0.35f).height(48.dp).testTag("monster_reset_btn")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "重置")
            }

            // Submit Button
            Button(
                onClick = {
                    isSubmitted = true
                    // Verify correct mapping against targets extracted from currentExpression
                    val expText = currentExpression?.englishText?.lowercase() ?: ""
                    var match = true
                    if (expText.contains("big eyes") && currentMonster.eyeSize != "big") match = false
                    if (expText.contains("small eyes") && currentMonster.eyeSize != "small") match = false
                    if (expText.contains("big ears") && currentMonster.earSize != "big") match = false
                    if (expText.contains("small ears") && currentMonster.earSize != "small") match = false
                    if (expText.contains("long nose") && currentMonster.noseLength != "long") match = false
                    if (expText.contains("short nose") && currentMonster.noseLength != "short") match = false
                    if (expText.contains("long arms") && currentMonster.armLength != "long") match = false
                    if (expText.contains("short arms") && currentMonster.armLength != "short") match = false
                    if (expText.contains("long tail") && currentMonster.tailLength != "long") match = false
                    if (expText.contains("short tail") && currentMonster.tailLength != "short") match = false
                    
                    isCorrect = match
                    if (isCorrect) {
                        ttsHelper.speak("Great! Perfect fit!", isSlow = false)
                    } else {
                        ttsHelper.speak("No, try again", isSlow = false)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSubmitted && isCorrect) Color(0xFF10B981) else Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(0.65f).height(48.dp).testTag("monster_submit_btn")
            ) {
                Text(
                    text = if (isSubmitted && isCorrect) "完美匹配！" else "确认怪兽拼装",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
