package com.example.ui.english

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
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

data class ZooAnimalModel(
    val animalId: String,
    val displayName: String,
    val emoji: String,
    val overallTraits: List<String>, // tall, short, fat, thin, big
    val bodyFeatures: Map<String, String>, // "nose" -> "long", "eyes" -> "small", "ears" -> "big", "tail" -> "short"
    val voiceLine: String,
    val cardBg: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnglishZooObservationView(
    currentExpression: EnglishExpression? = null,
    ttsHelper: EnglishTTSHelper
) {
    val context = LocalContext.current
    
    // Structuring 4 classic animals from PEP Unit 3 (Giraffe, Bear, Monkey, Elephant)
    val zooAnimals = remember {
        listOf(
            ZooAnimalModel(
                "giraffe",
                "Giraffe (长颈鹿)",
                "🦒",
                listOf("tall", "thin"),
                mapOf("neck" to "long", "legs" to "long", "tail" to "short"),
                "Look at that giraffe. It's so tall!",
                Color(0xFFFEF3C7) // Light warm amber
            ),
            ZooAnimalModel(
                "bear",
                "Bear (熊)",
                "🐻",
                listOf("fat", "big"),
                mapOf("tail" to "short", "ears" to "small", "body" to "big"),
                "Look at the bear. It's so fat!",
                Color(0xFFECE0D1) // Light brown
            ),
            ZooAnimalModel(
                "monkey",
                "Monkey (猴子)",
                "🐒",
                listOf("thin", "short"),
                mapOf("tail" to "long", "arms" to "long"),
                "Look at that monkey. It's short and fat.",
                Color(0xFFFEF2F2) // Pale red
            ),
            ZooAnimalModel(
                "elephant",
                "Elephant (大象)",
                "🐘",
                listOf("big", "fat"),
                mapOf("nose" to "long", "eyes" to "small", "ears" to "big", "tail" to "short"),
                "Look at the elephant. It has a long nose.",
                Color(0xFFF1F5F9) // Slate gray
            )
        )
    }

    var selectedAnimalId by remember { mutableStateOf("giraffe") }
    val activeAnimal = remember(selectedAnimalId) {
        zooAnimals.find { it.animalId == selectedAnimalId } ?: zooAnimals[0]
    }

    var isLocalViewMode by remember { mutableStateOf(false) } // overall vs. local body parts

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.5.dp, Color(0xFF1E293B)), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("zoo_observation_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bright playful HUD Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "☀️ 阳光动物园探索日",
                color = Color(0xFFF59E0B),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            
            // Switch Mode Button
            Button(
                onClick = { isLocalViewMode = !isLocalViewMode },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLocalViewMode) Color(0xFF10B981) else Color(0xFF3B82F6)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp).testTag("zoo_view_mode_switch")
            ) {
                Text(
                    text = if (isLocalViewMode) "切换: 整体观察" else "切换: 局部细节",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Live Polaroid Frame (Interactive Screen)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, Color(0xFF38BDF8)), RoundedCornerShape(12.dp))
                .testTag("zoo_animal_viewer"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Large Animal Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(activeAnimal.cardBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = activeAnimal.emoji,
                            fontSize = 80.sp,
                            modifier = Modifier.testTag("viewer_animal_emoji")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeAnimal.displayName,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                // Interactive Audio Dialogue card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                        .clickable { ttsHelper.speak(activeAnimal.voiceLine, isSlow = false) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "朗读",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "点击播放标准情境朗读:",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = activeAnimal.voiceLine,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Traits and features breakdown (Non-evaluative of children)
                if (!isLocalViewMode) {
                    // Overall look traits
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("整体物理特征 (Overall physical status):", color = Color.LightGray, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            activeAnimal.overallTraits.forEach { trait ->
                                AssistChip(
                                    onClick = { ttsHelper.speak(trait, isSlow = false) },
                                    label = { Text("It's $trait.", color = Color.White) },
                                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF334155))
                                )
                            }
                        }
                    }
                } else {
                    // Local body parts
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("局部细节特征 (Body part descriptors):", color = Color.LightGray, fontSize = 12.sp)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeAnimal.bodyFeatures.forEach { (part, desc) ->
                                AssistChip(
                                    onClick = { ttsHelper.speak("It has a $desc $part.", isSlow = false) },
                                    label = { Text("It has a $desc $part.", color = Color.White) },
                                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF475569))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Animal selector Row
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "点击动物卡，指认观察目标 (Select Animal to Inspect):",
                color = Color.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                zooAnimals.forEach { animal ->
                    val isSelected = animal.animalId == selectedAnimalId
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedAnimalId = animal.animalId
                                ttsHelper.speak(animal.displayName.split(" ")[0], isSlow = false)
                            }
                            .testTag("zoo_tab_${animal.animalId}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF020617)
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155).copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = animal.emoji, fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = animal.displayName.split(" ")[0],
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
