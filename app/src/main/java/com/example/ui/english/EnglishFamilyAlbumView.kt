package com.example.ui.english

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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

enum class FamilyInteractiveMode {
    ALBUM_BROWSE, // Lesson 1: Introductions and family members
    ALBUM_QUIZ    // Lesson 4: Ask and answer quiz: Is he/she your...? Who's that...?
}

data class VirtualFamilyMember(
    val memberId: String,
    val displayName: String,
    val relationType: String, // father, mother, sister, brother, grandfather, grandmother
    val pronoun: String,      // he, she
    val wordId: String,
    val emoji: String,
    val avatarBg: Color
)

@Composable
fun EnglishFamilyAlbumView(
    mode: FamilyInteractiveMode,
    currentExpression: EnglishExpression? = null,
    ttsHelper: EnglishTTSHelper
) {
    val context = LocalContext.current
    
    // Privacy compliant virtual members (no real PII or photo uploads)
    val familyMembers = remember {
        listOf(
            VirtualFamilyMember("m1", "Grandfather (爷爷/外公)", "grandfather", "he", "g3s2_u2_grandfather", "👴", Color(0xFF3B82F6).copy(alpha = 0.15f)),
            VirtualFamilyMember("m2", "Grandmother (奶奶/外婆)", "grandmother", "she", "g3s2_u2_grandmother", "👵", Color(0xFFEC4899).copy(alpha = 0.15f)),
            VirtualFamilyMember("m3", "Father (爸爸)", "father", "he", "g3s2_u2_father", "👨", Color(0xFF10B981).copy(alpha = 0.15f)),
            VirtualFamilyMember("m4", "Mother (妈妈)", "mother", "she", "g3s2_u2_mother", "👩", Color(0xFFF59E0B).copy(alpha = 0.15f)),
            VirtualFamilyMember("m5", "Brother (兄弟)", "brother", "he", "g3s2_u2_brother", "👦", Color(0xFF8B5CF6).copy(alpha = 0.15f)),
            VirtualFamilyMember("m6", "Sister (姐妹)", "sister", "she", "g3s2_u2_sister", "👧", Color(0xFF06B6D4).copy(alpha = 0.15f))
        )
    }

    when (mode) {
        FamilyInteractiveMode.ALBUM_BROWSE -> {
            AlbumBrowseInteractive(currentExpression, familyMembers, ttsHelper)
        }
        FamilyInteractiveMode.ALBUM_QUIZ -> {
            AlbumQuizInteractive(currentExpression, familyMembers, ttsHelper)
        }
    }
}

@Composable
fun AlbumBrowseInteractive(
    expression: EnglishExpression?,
    members: List<VirtualFamilyMember>,
    ttsHelper: EnglishTTSHelper
) {
    val expText = expression?.englishText?.lowercase() ?: ""
    
    // Find member mentioned in the current expression, default to father
    val activeMember = remember(expText) {
        members.find { expText.contains(it.relationType) } ?: members[2] // father
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
            text = "📖 虚拟航天家庭相册 (Family Album)",
            color = Color(0xFF00E5FF),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.testTag("family_album_browse_title")
        )

        // Polaroid Frame representing the active family member
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .testTag("polaroid_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Photo Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(activeMember.avatarBg, RoundedCornerShape(4.dp))
                        .border(BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = activeMember.emoji, fontSize = 72.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Polaroid Label
                Text(
                    text = activeMember.displayName,
                    color = Color.DarkGray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Relation: ${activeMember.relationType.uppercase()}",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Voice instruction card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                .clickable { ttsHelper.speak(expression?.englishText ?: "", isSlow = false) }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "播放", tint = Color(0xFFEC4899))
                Column {
                    Text(
                        text = "点击播放教材对话场景 (Scene Dialogue):",
                        color = Color(0xFFEC4899),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = expression?.englishText ?: "Who's that man?",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = expression?.chineseTranslation ?: "",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Browse grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "点击其他家人浏览电子相册 (Browse Album):", color = Color.LightGray, fontSize = 12.sp)
            
            val chunked = members.chunked(3)
            chunked.forEach { rowMembers ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowMembers.forEach { member ->
                        val isActive = member.memberId == activeMember.memberId
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) Color(0xFF1E293B) else Color(0xFF0B1220)
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (isActive) Color(0xFF00E5FF) else Color(0xFF334155).copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    ttsHelper.speak(member.relationType, isSlow = false)
                                }
                                .padding(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = member.emoji, fontSize = 24.sp)
                                Text(text = member.relationType, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumQuizInteractive(
    expression: EnglishExpression?,
    members: List<VirtualFamilyMember>,
    ttsHelper: EnglishTTSHelper
) {
    val context = LocalContext.current
    val expText = expression?.englishText?.lowercase() ?: ""
    
    // Quiz Goal: Find which member's relationType is matching the expression text
    // E.g. "Is she your mother? Yes, she is." -> Target is mother
    // E.g. "Who's that boy? He's my brother." -> Target is brother
    val targetMember = remember(expText) {
        members.find { expText.contains(it.relationType) } ?: members[4] // brother/sister default
    }

    var selectedMemberId by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    // Reset when expression changes
    LaunchedEffect(expression) {
        selectedMemberId = null
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
            text = "🎯 相册智力大考验 (Album Quiz Game)",
            color = Color(0xFF00E5FF),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.testTag("family_album_quiz_title")
        )

        // Dialogue Challenge Question
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
                    text = "听问句，找出相册中对应的家人 (Who matches this?):",
                    color = Color(0xFFEC4899),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = expression?.englishText ?: "Who's that boy?",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = expression?.chineseTranslation ?: "",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }
        }

        // Selected Card Polaroid Frame Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedMemberId != null) {
                val selMember = members.first { it.memberId == selectedMemberId }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(selMember.avatarBg, CircleShape)
                            .border(BorderStroke(2.dp, Color(0xFF00E5FF)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selMember.emoji, fontSize = 40.sp)
                    }
                    Column {
                        Text(text = "已选定家人卡片:", color = Color.Gray, fontSize = 11.sp)
                        Text(text = selMember.displayName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Relation: ${selMember.relationType.uppercase()}", color = Color(0xFF00E5FF), fontSize = 12.sp)
                    }
                }
            } else {
                Text(
                    text = "请点击下方成员卡片，指认正确的照片",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        // Six Selection Cards Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "选择要指认的家人卡 (Select Member Card):", color = Color.LightGray, fontSize = 12.sp)
            val chunked = members.chunked(3)
            chunked.forEach { rowMembers ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowMembers.forEach { member ->
                        val isSel = selectedMemberId == member.memberId
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) Color(0xFF1E293B) else Color(0xFF0B1220)
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSel) Color(0xFFEC4899) else Color(0xFF334155).copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedMemberId = member.memberId
                                    ttsHelper.speak(member.relationType, isSlow = false)
                                }
                                .padding(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = member.emoji, fontSize = 24.sp)
                                Text(text = member.relationType, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quiz feedback alert
        if (isSubmitted) {
            val alertBg = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
            val alertBorder = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
            val alertText = if (isSuccess) "🎉 恭喜！完美指认！ 句子配对完全正确！" else "❌ 指认有误。请仔细看问句是 He 还是 She，找对应的关系哦。"
            
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

        // Submit Button
        Button(
            onClick = {
                if (selectedMemberId == null) {
                    Toast.makeText(context, "请先选择需要指认的家人", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isSubmitted = true
                isSuccess = (selectedMemberId == targetMember.memberId)
                if (isSuccess) {
                    ttsHelper.speak("Correct! " + expression?.englishText, isSlow = false)
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
                .testTag("family_quiz_submit_btn")
        ) {
            Text(
                text = if (isSubmitted && isSuccess) "答对啦！(Next)" else "确认指认 (Submit)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
