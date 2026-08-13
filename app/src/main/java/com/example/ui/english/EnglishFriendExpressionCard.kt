package com.example.ui.english

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.EnglishExpression
import com.example.data.english.PronounSet
import com.example.data.english.StandardFriendProfiles
import com.example.data.english.VirtualFriendProfile
import com.example.util.english.EnglishTTSHelper

@Composable
fun EnglishFriendExpressionCard(
    currentExpression: EnglishExpression?,
    ttsHelper: EnglishTTSHelper
) {
    val matchedProfile: VirtualFriendProfile = when {
        currentExpression?.englishText?.contains("Zhang Peng", ignoreCase = true) == true ||
                currentExpression?.englishText?.contains("friendly", ignoreCase = true) == true ||
                currentExpression?.englishText?.contains("tall and strong", ignoreCase = true) == true -> StandardFriendProfiles.zhangPeng

        currentExpression?.englishText?.contains("Lucy", ignoreCase = true) == true ||
                currentExpression?.englishText?.contains("her name", ignoreCase = true) == true -> StandardFriendProfiles.lucy

        currentExpression?.englishText?.contains("Wu Binbin", ignoreCase = true) == true ||
                currentExpression?.englishText?.contains("glasses", ignoreCase = true) == true ||
                currentExpression?.englishText?.contains("shoes are blue", ignoreCase = true) == true -> StandardFriendProfiles.wuBinbin

        currentExpression?.englishText?.contains("John", ignoreCase = true) == true -> StandardFriendProfiles.john

        else -> StandardFriendProfiles.zhangPeng
    }

    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏷️ 朋友特征名片 (Friend Profile Card)",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = if (matchedProfile.pronounSet == PronounSet.HE_HIS) Color(0xFF1E3A8A) else Color(0xFF831843),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (matchedProfile.pronounSet == PronounSet.HE_HIS) "Boy / He" else "Girl / She",
                        color = if (matchedProfile.pronounSet == PronounSet.HE_HIS) Color(0xFF93C5FD) else Color(0xFFFBCFE8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(
                            2.dp,
                            if (matchedProfile.pronounSet == PronounSet.HE_HIS) Color(0xFF3B82F6) else Color(0xFFEC4899),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = matchedProfile.displayName,
                        tint = if (matchedProfile.pronounSet == PronounSet.HE_HIS) Color(0xFF60A5FA) else Color(0xFFF472B6),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = matchedProfile.displayName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Trait tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchedProfile.traits.forEach { trait ->
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF475569))
                            ) {
                                Text(
                                    text = trait,
                                    color = Color(0xFFF59E0B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (matchedProfile.accessories.contains("glasses")) {
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8))
                            ) {
                                Text(
                                    text = "glasses 👓",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // TTS Play button for friend description
                IconButton(
                    onClick = {
                        val descriptionText = "This is ${matchedProfile.displayName}. ${if (matchedProfile.pronounSet == PronounSet.HE_HIS) "He" else "She"} is ${matchedProfile.traits.joinToString(" and ")}."
                        ttsHelper.speak(descriptionText)
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speak friend info",
                        tint = Color(0xFF38BDF8)
                    )
                }
            }
        }
    }
}
