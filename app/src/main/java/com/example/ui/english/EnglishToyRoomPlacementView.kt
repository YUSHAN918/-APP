package com.example.ui.english

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.*
import com.example.util.english.EnglishTTSHelper

data class SpatialInteractionState(
    val answered: Boolean = false,
    val correct: Boolean = false,
    val completed: Boolean = false,
    val score: Int = 0,
    val totalQuestions: Int = 0
)

@Composable
fun EnglishToyRoomExpressionCard(
    currentExpression: EnglishExpression,
    ttsHelper: EnglishTTSHelper
) {
    val text = currentExpression.englishText.lowercase()
    val (emoji, bg, title, hint) = remember(currentExpression.expressionId) {
        when {
            text.contains("under") -> Quadruple("📖🏎️", Color(0xFFFEF3C7), "Under 示例 (在...下面)", "物品在书籍或座椅下方")
            text.contains("in") -> Quadruple("📥✏️", Color(0xFFE0F2FE), "In 示例 (在...里面)", "物品放置于文具盒或玩具箱内部")
            text.contains("on") -> Quadruple("🪑⚽", Color(0xFFDCFCE7), "On 示例 (在...上面)", "物品处于桌椅表面上方")
            text.contains("where") -> Quadruple("🔍🧸", Color(0xFFF3E8FF), "Where 询问位置", "在房间内寻找指定物品")
            else -> Quadruple("🧸🏡", Color(0xFFFFF7ED), "玩具房词汇表达", "伙伴玩具房空间情境")
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("toy_room_expression_card")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = emoji, fontSize = 36.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = hint,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            IconButton(
                onClick = { ttsHelper.speak(currentExpression.englishText) },
                modifier = Modifier.background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "朗读",
                    tint = Color(0xFF0284C7)
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun EnglishToyRoomPlacementView(
    mode: String = "LESSON1", // "LESSON1" (treasure hunt), "LESSON4" (guessing/yes-no), "LESSON5" (vocabulary & placement instructions)
    ttsHelper: EnglishTTSHelper,
    onInteractionStateChanged: ((SpatialInteractionState) -> Unit)? = null,
    onComplete: ((Int) -> Unit)? = null,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // 🧱 Define standard interactive anchors
    val anchors = remember {
        listOf(
            EnglishSpatialAnchor("desk", SpatialAnchorType.DESK, listOf(EnglishSpatialRelation.ON, EnglishSpatialRelation.IN, EnglishSpatialRelation.UNDER), "Desk"),
            EnglishSpatialAnchor("chair", SpatialAnchorType.CHAIR, listOf(EnglishSpatialRelation.ON, EnglishSpatialRelation.UNDER), "Chair"),
            EnglishSpatialAnchor("toy_box", SpatialAnchorType.BOX, listOf(EnglishSpatialRelation.IN), "Toy Box"),
            EnglishSpatialAnchor("bag", SpatialAnchorType.BAG, listOf(EnglishSpatialRelation.IN), "Bag")
        )
    }

    // 🧸 Defined interactive items
    val availableItems = remember {
        listOf(
            "car" to "🏎️ Toy Car",
            "ball" to "⚽ Ball",
            "cap" to "🧢 Cap",
            "map" to "🗺️ Map",
            "pencil_box" to "✏️ Pencil Box",
            "book" to "📖 Book"
        )
    }

    // Dynamic placement state
    var placements by remember {
        mutableStateOf(
            listOf(
                EnglishObjectPlacement("pencil_box", EnglishSpatialRelation.IN, "desk"),
                EnglishObjectPlacement("book", EnglishSpatialRelation.ON, "desk"),
                EnglishObjectPlacement("car", EnglishSpatialRelation.UNDER, "chair"),
                EnglishObjectPlacement("ball", EnglishSpatialRelation.ON, "chair"),
                EnglishObjectPlacement("cap", EnglishSpatialRelation.IN, "toy_box"),
                EnglishObjectPlacement("map", EnglishSpatialRelation.IN, "bag")
            )
        )
    }

    // Selected item state for Tap-to-Place
    var selectedItem by remember { mutableStateOf<String?>(null) }
    
    // Game step / target
    var targetItem by remember { mutableStateOf("car") }
    var targetRelation by remember { mutableStateOf(EnglishSpatialRelation.UNDER) }
    var targetAnchor by remember { mutableStateOf("chair") }
    var score by remember { mutableStateOf(0) }
    var totalQuestions by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMsg by remember { mutableStateOf("") }
    
    // Regenerate a target command
    val generateNextQuestion = {
        val randomItem = availableItems.random().first
        val randomAnchor = anchors.random()
        val randomRelation = randomAnchor.allowedRelations.random()
        targetItem = randomItem
        targetRelation = randomRelation
        targetAnchor = randomAnchor.anchorId
        
        val itemSpelling = when(targetItem) {
            "car" -> "car"
            "ball" -> "ball"
            "cap" -> "cap"
            "map" -> "map"
            "pencil_box" -> "pencil box"
            else -> "book"
        }
        val prep = targetRelation.name.lowercase()
        val command = "Put the $itemSpelling $prep the ${randomAnchor.anchorId}."
        ttsHelper.speak(command, isSlow = false)
    }

    // Check answer logic for Drag/Tap Placement
    val checkPlacementAnswer = { item: String, relation: EnglishSpatialRelation, anchor: String ->
        val expected = EnglishObjectPlacement(targetItem, targetRelation, targetAnchor)
        val actual = EnglishObjectPlacement(item, relation, anchor)
        val isCorrect = EnglishSpatialAnswerEvaluator.evaluatePlacement(item, relation, anchor, expected)
        
        totalQuestions++
        if (isCorrect) {
            score += 10
            dialogMsg = "🎉 Great job! You successfully placed the ${targetItem.replace("_", " ")} ${targetRelation.name.lowercase()} the $targetAnchor!"
            ttsHelper.speak("Excellent! That is correct.", isSlow = false)
        } else {
            dialogMsg = "❌ Oops! That's not correct. Let's try to put the ${targetItem.replace("_", " ")} ${targetRelation.name.lowercase()} the $targetAnchor."
            ttsHelper.speak("No, try again.", isSlow = false)
        }
        onInteractionStateChanged?.invoke(
            SpatialInteractionState(
                answered = true,
                correct = isCorrect,
                completed = score >= 10,
                score = score,
                totalQuestions = totalQuestions
            )
        )
        showDialog = true
    }

    // Initialize the first question
    LaunchedEffect(Unit) {
        generateNextQuestion()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBEB)) // warm pastel cream color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🧸 Bright Toy Room 寻宝",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF78350F)
                    )
                    Text(
                        text = "Score: $score  |  Attempt: $totalQuestions",
                        fontSize = 12.sp,
                        color = Color(0xFF92400E)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            placements = listOf(
                                EnglishObjectPlacement("pencil_box", EnglishSpatialRelation.IN, "desk"),
                                EnglishObjectPlacement("book", EnglishSpatialRelation.ON, "desk"),
                                EnglishObjectPlacement("car", EnglishSpatialRelation.UNDER, "chair"),
                                EnglishObjectPlacement("ball", EnglishSpatialRelation.ON, "chair"),
                                EnglishObjectPlacement("cap", EnglishSpatialRelation.IN, "toy_box"),
                                EnglishObjectPlacement("map", EnglishSpatialRelation.IN, "bag")
                            )
                            selectedItem = null
                            generateNextQuestion()
                        },
                        modifier = Modifier.background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "重置场景", tint = Color(0xFFD97706))
                    }
                }
            }

            // Target Command / Dialogue Bubble
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFFFCD34D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = {
                            val itemSpelling = when(targetItem) {
                                "car" -> "car"
                                "ball" -> "ball"
                                "cap" -> "cap"
                                "map" -> "map"
                                "pencil_box" -> "pencil box"
                                else -> "book"
                            }
                            val prep = targetRelation.name.lowercase()
                            val command = "Put the $itemSpelling $prep the $targetAnchor."
                            ttsHelper.speak(command, isSlow = false)
                        },
                        modifier = Modifier.background(Color(0xFFFEF3C7), CircleShape)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "朗读指令", tint = Color(0xFFD97706))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "💡 Location Command 指令:",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        val itemSpelling = when(targetItem) {
                            "car" -> "car"
                            "ball" -> "ball"
                            "cap" -> "cap"
                            "map" -> "map"
                            "pencil_box" -> "pencil box"
                            else -> "book"
                        }
                        Text(
                            text = "Put the $itemSpelling ${targetRelation.name.lowercase()} the $targetAnchor.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }

            // 🪑 📥 Room Visual Canvas (Adaptive & Responsive layout)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color(0xFFF59E0B).copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Render anchors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        anchors.take(2).forEach { anchor ->
                            AnchorView(
                                anchor = anchor,
                                placements = placements,
                                selectedItem = selectedItem,
                                isTarget = targetAnchor == anchor.anchorId,
                                onPlaceItem = { relation ->
                                    selectedItem?.let { item ->
                                        // Update placements
                                        placements = placements.filter { it.itemId != item } + EnglishObjectPlacement(item, relation, anchor.anchorId)
                                        checkPlacementAnswer(item, relation, anchor.anchorId)
                                        selectedItem = null
                                    }
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        anchors.drop(2).forEach { anchor ->
                            AnchorView(
                                anchor = anchor,
                                placements = placements,
                                selectedItem = selectedItem,
                                isTarget = targetAnchor == anchor.anchorId,
                                onPlaceItem = { relation ->
                                    selectedItem?.let { item ->
                                        placements = placements.filter { it.itemId != item } + EnglishObjectPlacement(item, relation, anchor.anchorId)
                                        checkPlacementAnswer(item, relation, anchor.anchorId)
                                        selectedItem = null
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 🏎️ ⚽ Interactive Items Dock (Selectable)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "👇 Pick a Toy / Object to place:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        availableItems.forEach { (itemId, display) ->
                            val isSelected = selectedItem == itemId
                            val currentLoc = placements.find { it.itemId == itemId }
                            
                            Box(
                                modifier = Modifier
                                    .testTag("toy_item_$itemId")
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFFF59E0B) else Color.White)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF78350F) else Color(0xFFFCD34D),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedItem = if (isSelected) null else itemId
                                        ttsHelper.speak(itemId.replace("_", " "), isSlow = false)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = display.split(" ")[0], // emoji
                                        fontSize = 24.sp
                                    )
                                    Text(
                                        text = itemId.replace("_", " "),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF78350F)
                                    )
                                    if (currentLoc != null) {
                                        Text(
                                            text = "${currentLoc.relation.name.lowercase()} ${currentLoc.anchorId}",
                                            fontSize = 8.sp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Result Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    generateNextQuestion()
                },
                icon = { Icon(Icons.Default.Info, contentDescription = "结果", tint = Color(0xFFF59E0B)) },
                title = { Text("挑战结果") },
                text = { Text(dialogMsg, fontSize = 16.sp, color = Color.DarkGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            generateNextQuestion()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                    ) {
                        Text("下一题 (Next)")
                    }
                }
            )
        }
    }
}

@Composable
fun AnchorView(
    anchor: EnglishSpatialAnchor,
    placements: List<EnglishObjectPlacement>,
    selectedItem: String?,
    isTarget: Boolean,
    onPlaceItem: (EnglishSpatialRelation) -> Unit
) {
    val anchorEmoji = when (anchor.anchorId) {
        "desk" -> "🪑📚 Desk"
        "chair" -> "🪑 Chair"
        "toy_box" -> "📥 Toy Box"
        else -> "🎒 School Bag"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isTarget && selectedItem != null) Color(0xFFFEF3C7) else Color(0xFFF8FAFC)
        ),
        border = BorderStroke(
            width = if (isTarget && selectedItem != null) 3.dp else 1.5.dp,
            color = if (isTarget && selectedItem != null) Color(0xFFF59E0B) else Color(0xFFE2E8F0)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(140.dp)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = anchorEmoji,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B)
            )

            // Dynamic relations drop zones
            anchor.allowedRelations.forEach { rel ->
                val placedItemsHere = placements.filter { it.anchorId == anchor.anchorId && it.relation == rel }
                val isDropActive = selectedItem != null
                
                Box(
                    modifier = Modifier
                        .testTag("drop_zone_${anchor.anchorId}_${rel.name}")
                        .fillMaxWidth()
                        .heightIn(min = 36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isDropActive) Color(0xFF10B981).copy(alpha = 0.15f)
                            else Color(0xFFF1F5F9)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isDropActive) Color(0xFF10B981) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = isDropActive) {
                            onPlaceItem(rel)
                        }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = rel.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDropActive) Color(0xFF047857) else Color.Gray
                        )
                        
                        if (placedItemsHere.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                placedItemsHere.forEach { p ->
                                    val emoji = when (p.itemId) {
                                        "car" -> "🏎️"
                                        "ball" -> "⚽"
                                        "cap" -> "Cap"
                                        "map" -> "🗺️"
                                        "pencil_box" -> "✏️"
                                        else -> "📖"
                                    }
                                    Text(text = emoji, fontSize = 14.sp)
                                }
                            }
                        } else if (isDropActive) {
                            Text(text = "+ Place", fontSize = 9.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
