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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
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

data class DetectiveMysteryCase(
    val caseId: String,
    val title: String,
    val cluesText: List<String>,
    val targetProfile: VirtualFriendProfile,
    val candidateProfiles: List<VirtualFriendProfile>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnglishFriendDetectiveView(
    ttsHelper: EnglishTTSHelper,
    onInteractionCompleted: () -> Unit
) {
    val cases = remember {
        listOf(
            DetectiveMysteryCase(
                caseId = "case_1",
                title = "侦探线索 1：谁是高高壮壮的新朋友？",
                cluesText = listOf(
                    "A boy or girl? -> A boy.",
                    "He is tall and strong.",
                    "His name is Zhang Peng."
                ),
                targetProfile = StandardFriendProfiles.zhangPeng,
                candidateProfiles = listOf(
                    StandardFriendProfiles.zhangPeng,
                    StandardFriendProfiles.wuBinbin,
                    StandardFriendProfiles.john,
                    StandardFriendProfiles.lucy
                )
            ),
            DetectiveMysteryCase(
                caseId = "case_2",
                title = "侦探线索 2：戴眼镜、穿蓝鞋的好朋友",
                cluesText = listOf(
                    "A boy. He is tall and thin.",
                    "He has glasses and his shoes are blue.",
                    "Is he Wu Binbin? -> Yes. You're right!"
                ),
                targetProfile = StandardFriendProfiles.wuBinbin,
                candidateProfiles = listOf(
                    StandardFriendProfiles.zhangPeng,
                    StandardFriendProfiles.wuBinbin,
                    StandardFriendProfiles.amy,
                    StandardFriendProfiles.sarah
                )
            ),
            DetectiveMysteryCase(
                caseId = "case_3",
                title = "侦探线索 3：留长发、人很友好的女孩",
                cluesText = listOf(
                    "What's her name?",
                    "Her name is Lucy.",
                    "She has long hair and she is very friendly."
                ),
                targetProfile = StandardFriendProfiles.lucy,
                candidateProfiles = listOf(
                    StandardFriendProfiles.amy,
                    StandardFriendProfiles.lucy,
                    StandardFriendProfiles.sarah,
                    StandardFriendProfiles.john
                )
            )
        )
    }

    var currentCaseIndex by remember { mutableStateOf(0) }
    var selectedProfileId by remember { mutableStateOf<String?>(null) }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }

    val currentCase = cases[currentCaseIndex]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Detective Banner Header
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Detective",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "🕵️ 伙伴侦探社 (${currentCaseIndex + 1}/${cases.size})",
                        color = Color(0xFF60A5FA),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentCase.title,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Clues Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔎 收集到的声音与特征线索：",
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                currentCase.cluesText.forEach { clue ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable {
                            ttsHelper.speak(clue)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak Clue",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = clue,
                            color = Color(0xFFE2E8F0),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Text(
            text = "👇 根据线索，点击选择正确的朋友：",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Candidate Cards Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            items(currentCase.candidateProfiles) { profile ->
                val isSelected = selectedProfileId == profile.characterId
                val isTarget = profile.characterId == currentCase.targetProfile.characterId

                val cardBg = when {
                    isSelected && isCorrect == true -> Color(0xFF065F46)
                    isSelected && isCorrect == false -> Color(0xFF991B1B)
                    else -> Color(0xFF1E293B)
                }

                val borderColour = when {
                    isSelected && isCorrect == true -> Color(0xFF10B981)
                    isSelected && isCorrect == false -> Color(0xFFEF4444)
                    else -> Color(0xFF334155)
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, borderColour),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("friend_profile_card_${profile.characterId}")
                        .clickable {
                            selectedProfileId = profile.characterId
                            if (isTarget) {
                                isCorrect = true
                                ttsHelper.speak("Yes, you're right! ${profile.displayName}")
                            } else {
                                isCorrect = false
                                ttsHelper.speak("Try again!")
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = profile.displayName,
                                tint = if (profile.pronounSet == PronounSet.HE_HIS) Color(0xFF60A5FA) else Color(0xFFF472B6),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = profile.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = if (profile.pronounSet == PronounSet.HE_HIS) "Boy" else "Girl",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Feedback & Next Case
        if (isCorrect == true) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Correct",
                    tint = Color(0xFF10B981)
                )
                Text(
                    text = "回答正确！你说对了！",
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Button(
                onClick = {
                    if (currentCaseIndex + 1 < cases.size) {
                        currentCaseIndex++
                        selectedProfileId = null
                        isCorrect = null
                    } else {
                        onInteractionCompleted()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("friend_detective_next_button")
            ) {
                Text(
                    text = if (currentCaseIndex + 1 < cases.size) "进入下一关" else "完成伙伴侦探研习",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        } else if (isCorrect == false) {
            Text(
                text = "再仔细对比一下性别人称或特征线索哦！",
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}
