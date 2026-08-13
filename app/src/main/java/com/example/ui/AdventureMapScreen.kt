package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Level
import com.example.viewmodel.GameViewModel
import com.example.data.math.*

// Chapter data class to help aggregate levels by unitName
data class MapChapter(
    val name: String,
    val levels: List<Level>,
    val completedCount: Int,
    val totalCount: Int,
    val isUnlocked: Boolean
)

private fun getEffectiveSortIndex(level: Level): Int {
    if (level.sortIndex > 0) return level.sortIndex
    val name = level.name
    return when {
        name.contains("第一单元") && name.contains("会写字") -> 101
        name.contains("第一单元") && name.contains("课后词语") -> 102
        name.contains("第一单元") && name.contains("易错字") -> 103
        name.contains("第一单元") && name.contains("易错词") -> 104
        name.contains("第二单元") && name.contains("会写字") -> 201
        name.contains("第二单元") && name.contains("课后词语") -> 202
        name.contains("第二单元") && name.contains("名著词语") -> 203
        name.contains("第二单元") && name.contains("易错字") -> 204
        name.contains("第二单元") && name.contains("BOSS") -> 205
        name.contains("第三单元") && name.contains("汉字知识识字") -> 301
        name.contains("第三单元") && name.contains("汉字文化词语") -> 302
        name.contains("第三单元") && name.contains("晏子使楚词语") -> 303
        name.contains("第三单元") && name.contains("易错") -> 304
        name.contains("第四单元") && name.contains("会写字") -> 401
        name.contains("第四单元") && name.contains("课后词语") -> 402
        name.contains("第四单元") && name.contains("军神与清贫词语") -> 403
        name.contains("第四单元") && name.contains("红色主题词语") -> 404
        name.contains("第四单元") && name.contains("易错") -> 405
        name.contains("第五单元") && name.contains("会写字") -> 501
        name.contains("第五单元") && name.contains("人物描写词语") -> 502
        name.contains("第五单元") && name.contains("刷子李词语") -> 503
        name.contains("第五单元") && name.contains("易错") -> 504
        else -> 999
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureMapScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onNavigateToLevels: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToHoliday: () -> Unit,
    onNavigateToPetHouse: () -> Unit,
    isEmbeddedInShell: Boolean = false,
    onNavigateToCodex: () -> Unit = {},
    onNavigateToMathLesson: (String, String, String) -> Unit = { _, _, _ -> },
    onNavigateToEnglishLesson: (String, String, String) -> Unit = { _, _, _ -> },
    initialWorld: String = "语文"
) {
    val context = LocalContext.current
    val player by viewModel.playerProfile.collectAsState()
    val rawLevels by viewModel.allLevels.collectAsState()
    val allWords by viewModel.allWords.collectAsState()

    val mathManifest = remember { MathContentLoader.loadManifest(context) }

    // 1. Filter and sort levels
    val sortedLevels = remember(rawLevels) {
        rawLevels.sortedWith { a, b ->
            val aSort = getEffectiveSortIndex(a)
            val bSort = getEffectiveSortIndex(b)
            aSort.compareTo(bSort)
        }
    }

    // 2. Group by chapter (unitName)
    val chapters = remember(sortedLevels) {
        sortedLevels.groupBy { it.unitName }.map { (unitName, list) ->
            val completedCount = list.count { it.isCompleted }
            val totalCount = list.size
            val isChapterUnlocked = list.any { it.isUnlocked }
            MapChapter(
                name = unitName,
                levels = list,
                completedCount = completedCount,
                totalCount = totalCount,
                isUnlocked = isChapterUnlocked
            )
        }
    }

    // 3. Selection State
    var selectedWorld by rememberSaveable { mutableStateOf(initialWorld) } // 语文, 数学, 英语, 科学
    
    // Automatically find and select the current active chapter
    var selectedChapterIndex by remember(chapters) {
        val currentActiveIdx = chapters.indexOfFirst { ch -> 
            ch.isUnlocked && ch.completedCount < ch.totalCount 
        }
        mutableStateOf(if (currentActiveIdx != -1) currentActiveIdx else 0)
    }

    // Safety bounds for selected chapter index
    val activeChapter = remember(chapters, selectedChapterIndex) {
        if (chapters.isNotEmpty() && selectedChapterIndex in chapters.indices) {
            chapters[selectedChapterIndex]
        } else if (chapters.isNotEmpty()) {
            chapters[0]
        } else {
            null
        }
    }

    // Current selected level for detailing pop-up
    var detailLevel by remember { mutableStateOf<Level?>(null) }

    // Scroll state for the world map
    val scrollState = rememberScrollState()

    var completedAnimProgress by remember { mutableStateOf(0f) }
    var pathAnimProgress by remember { mutableStateOf(0f) }
    var nextUnlockAnimProgress by remember { mutableStateOf(0f) }
    var showUnlockToast by remember { mutableStateOf(false) }

    val justCompletedId = viewModel.justCompletedLevelId
    val shouldAnimate = viewModel.shouldPlayMapAnimation
    val density = androidx.compose.ui.platform.LocalDensity.current

    LaunchedEffect(justCompletedId, shouldAnimate) {
        if (justCompletedId != null) {
            // Smoothly scroll to the node
            if (activeChapter != null) {
                val idx = activeChapter.levels.indexOfFirst { it.id == justCompletedId }
                if (idx != -1) {
                    val scrollPx = with(density) { maxOf(0.dp, (idx * 140 - 150).dp).toPx().toInt() }
                    if (!scrollState.isScrollInProgress) {
                        scrollState.animateScrollTo(scrollPx)
                    }
                }
            }

            if (shouldAnimate) {
                completedAnimProgress = 0f
                pathAnimProgress = 0f
                nextUnlockAnimProgress = 0f
                showUnlockToast = false

                // Phase 1: Current node completion anim (600ms)
                androidx.compose.animation.core.animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.tween(600, easing = androidx.compose.animation.core.LinearEasing)
                ) { value, _ ->
                    completedAnimProgress = value
                }

                // Phase 2: Path extension anim (800ms)
                androidx.compose.animation.core.animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.tween(800, easing = androidx.compose.animation.core.LinearEasing)
                ) { value, _ ->
                    pathAnimProgress = value
                }

                // Phase 3: Next node unlocking lock fade and bounce (600ms)
                showUnlockToast = true
                androidx.compose.animation.core.animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.tween(600, easing = androidx.compose.animation.core.EaseOutBack)
                ) { value, _ ->
                    nextUnlockAnimProgress = value
                }

                // Auto-hide after 2.5 seconds
                kotlinx.coroutines.delay(2500)
                showUnlockToast = false

                // Reset animation flag to prevent replaying
                viewModel.shouldPlayMapAnimation = false
            }

            // Reset completed level tracker to prevent repeated scrolling
            viewModel.justCompletedLevelId = null
        }
    }

    Scaffold(
        contentWindowInsets = if (isEmbeddedInShell) WindowInsets(0.dp) else ScaffoldDefaults.contentWindowInsets,
        topBar = {
            if (!isEmbeddedInShell) {
                TopAppBar(
                    title = {
                        Text(
                            text = "🗺️ 学科世界古域地图",
                            style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("map_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        player?.let { p ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("🪙", fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${p.coins}",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFFBBF24)),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Surface(
                                    color = Color(0xFF2563EB),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("⭐", fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "LV ${p.level}",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0F172A)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF0B132B),
                            Color(0xFF1C2541)
                        )
                    )
                )
        ) {
            // Main Map content layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 80.dp) // Leave room for floating buttons & tabs
            ) {
                // 1. World selector
                WorldSelector(
                    selectedWorld = selectedWorld,
                    onWorldSelected = { world ->
                        if (world == "语文" || world == "数学" || world == "英语") {
                            selectedWorld = world
                        } else {
                            Toast.makeText(context, "$world 正在全力开拓中，敬请期待！", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                if (selectedWorld == "语文") {
                    // 2. Chapter controller banner
                if (chapters.isNotEmpty() && activeChapter != null) {
                    ChapterPortalSelector(
                        chapters = chapters,
                        selectedIndex = selectedChapterIndex,
                        onIndexChanged = { idx ->
                            selectedChapterIndex = idx
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(Color(0xFF1E293B).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏕️ 当前暂无已开放的冒险领地，请在家长端下达讨伐令",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 3. S-curve Interactive Map
                if (activeChapter != null) {
                    val chapterLevels = activeChapter.levels
                    val nodeCount = chapterLevels.size

                    if (nodeCount > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((nodeCount * 140 + 40).dp)
                                .padding(horizontal = 16.dp)
                        ) {
                            // S-Curve paths between nodes
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                val width = size.width
                                val points = (0 until nodeCount).map { i ->
                                    val isBossNode = chapterLevels[i].name.contains("BOSS") || chapterLevels[i].name.contains("挑战") || i == nodeCount - 1
                                    val xPercent = when {
                                        isBossNode -> 0.50f
                                        i % 3 == 0 -> 0.28f
                                        i % 3 == 1 -> 0.72f
                                        else -> 0.50f
                                    }
                                    Offset(width * xPercent, (i * 140 + 70).dp.toPx())
                                }

                                // Connect adjacent points
                                for (i in 0 until points.size - 1) {
                                    val p1 = points[i]
                                    val p2 = points[i + 1]
                                    val currentLvl = chapterLevels[i]
                                    val nextLvl = chapterLevels[i + 1]
                                    val isPathUnlocked = currentLvl.isCompleted || (currentLvl.isUnlocked && nextLvl.isUnlocked)

                                    val control1 = Offset(p1.x, (p1.y + p2.y) / 2)
                                    val control2 = Offset(p2.x, (p1.y + p2.y) / 2)

                                    val path = Path().apply {
                                        moveTo(p1.x, p1.y)
                                        cubicTo(control1.x, control1.y, control2.x, control2.y, p2.x, p2.y)
                                    }

                                    val isNewlyUnlockedPath = shouldAnimate && (currentLvl.id == justCompletedId)

                                    if (isNewlyUnlockedPath) {
                                        // Draw the locked path background first
                                        drawPath(
                                            path = path,
                                            color = Color(0xFF475569).copy(alpha = 0.3f),
                                            style = Stroke(
                                                width = 14f,
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                            )
                                        )
                                        drawPath(
                                            path = path,
                                            color = Color(0xFF475569),
                                            style = Stroke(
                                                width = 6f,
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                            )
                                        )

                                        // Draw the animated glowing path on top of it up to pathAnimProgress
                                        val pm = androidx.compose.ui.graphics.PathMeasure()
                                        pm.setPath(path, false)
                                        val segmentPath = Path()
                                        pm.getSegment(0f, pm.length * pathAnimProgress, segmentPath, true)

                                        drawPath(
                                            path = segmentPath,
                                            color = Color(0xFFFBBF24).copy(alpha = 0.4f),
                                            style = Stroke(
                                                width = 14f
                                            )
                                        )
                                        drawPath(
                                            path = segmentPath,
                                            color = Color(0xFFFBBF24),
                                            style = Stroke(
                                                width = 6f
                                            )
                                        )
                                    } else {
                                        // Outer glow
                                        drawPath(
                                            path = path,
                                            color = if (isPathUnlocked) Color(0xFFFBBF24).copy(alpha = 0.4f) else Color(0xFF475569).copy(alpha = 0.3f),
                                            style = Stroke(
                                                width = 14f,
                                                pathEffect = if (isPathUnlocked) null else PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                            )
                                        )

                                        // Core bright path
                                        drawPath(
                                            path = path,
                                            color = if (isPathUnlocked) Color(0xFFFBBF24) else Color(0xFF475569),
                                            style = Stroke(
                                                width = 6f,
                                                pathEffect = if (isPathUnlocked) null else PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                            )
                                        )
                                    }
                                }
                            }

                            // Interactive Nodes Overlay
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                chapterLevels.forEachIndexed { idx, level ->
                                    val isBossNode = level.name.contains("BOSS") || level.name.contains("挑战") || idx == nodeCount - 1
                                    val alignment = when {
                                        isBossNode -> Alignment.CenterHorizontally
                                        idx % 3 == 0 -> Alignment.Start
                                        idx % 3 == 1 -> Alignment.End
                                        else -> Alignment.CenterHorizontally
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp),
                                        contentAlignment = when (alignment) {
                                            Alignment.Start -> Alignment.CenterStart
                                            Alignment.End -> Alignment.CenterEnd
                                            else -> Alignment.Center
                                        }
                                    ) {
                                        // Padding nodes dynamically for left/right positioning
                                        val horizontalPadding = if (isBossNode) 0.dp else 24.dp
                                        
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = horizontalPadding)
                                        ) {
                                            val nodeJustCompleted = shouldAnimate && (level.id == justCompletedId)
                                            val isNextNodeUnlocked = shouldAnimate && (idx > 0 && chapterLevels[idx - 1].id == justCompletedId)

                                            MapNodeComponent(
                                                level = level,
                                                idx = idx + 1,
                                                isBoss = isBossNode,
                                                completedAnimProgress = if (nodeJustCompleted) completedAnimProgress else 1f,
                                                nextUnlockAnimProgress = if (isNextNodeUnlocked) nextUnlockAnimProgress else 1f,
                                                isAnimating = shouldAnimate && (nodeJustCompleted || isNextNodeUnlocked),
                                                onClick = {
                                                    if (level.isUnlocked) {
                                                        detailLevel = level
                                                    } else {
                                                        Toast.makeText(context, "🔒 该关卡尚未解锁，请先攻克前置试炼！", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                } else if (selectedWorld == "数学") {
                    MathWorldSection(
                        manifest = mathManifest,
                        onNavigateToLesson = onNavigateToMathLesson,
                        context = context
                    )
                } else if (selectedWorld == "英语") {
                    EnglishWorldSection(
                        onNavigateToLesson = onNavigateToEnglishLesson,
                        context = context
                    )
                }
            }

            // 4. Magic Floating Crystal shortcuts on the right side
            if (selectedWorld == "语文") {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FloatingCrystalButton(
                            icon = "📜",
                            title = "声澜古诗",
                            glowColor = Color(0xFF8B5CF6),
                            onClick = onNavigateToHoliday
                        )
                        FloatingCrystalButton(
                            icon = "😈",
                            title = "错灵净化",
                            glowColor = Color(0xFFF59E0B),
                            onClick = onNavigateToReview
                        )
                        FloatingCrystalButton(
                            icon = "📋",
                            title = "暑期委托",
                            glowColor = Color(0xFFEF4444),
                            onClick = onNavigateToHoliday
                        )
                        FloatingCrystalButton(
                            icon = "🦊",
                            title = "契约神域",
                            glowColor = Color(0xFF06B6D4),
                            onClick = onNavigateToPetHouse
                        )
                    }
                }
            }

            // 5. Level Contract Dialog Popup (Dialog styled as parchment/star map)
            detailLevel?.let { level ->
                // Calculate word count for this level
                val levelWords = if (!level.wordIdsStr.isNullOrEmpty()) {
                    val ids = level.wordIdsStr.split(",").mapNotNull { it.trim().toIntOrNull() }
                    ids.size
                } else {
                    allWords.count { it.unitName == level.unitName }
                }

                LevelDetailDialog(
                    level = level,
                    wordCount = levelWords,
                    onDismiss = { detailLevel = null },
                    onStartAdventure = {
                        detailLevel = null
                        viewModel.requestNavigateToBattle(level.id)
                    }
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showUnlockToast,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            ) {
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.95f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFBBF24)),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔓", fontSize = 18.sp)
                        Text(
                            text = "新关卡已解锁！",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorldSelector(
    selectedWorld: String,
    onWorldSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val worlds = listOf(
            Triple("语文", "文字森林", Color(0xFF10B981)),
            Triple("数学", "数字机械城", Color(0xFF3B82F6)),
            Triple("英语", "星语海港", Color(0xFFEC4899)),
            Triple("科学", "科学遗迹", Color(0xFF8B5CF6))
        )

        worlds.forEach { (world, alias, color) ->
            val isSelected = selectedWorld == world
            val isUnlocked = world == "语文" || world == "数学" || world == "英语"

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF1E293B).copy(alpha = 0.6f)
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) color else Color(0xFF334155),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onWorldSelected(world) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isUnlocked) color else Color.Gray, CircleShape)
                    )
                    Column {
                        Text(
                            text = world,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isUnlocked) alias else "🔒 未开启",
                            color = if (isSelected) color.copy(alpha = 0.9f) else Color.DarkGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterPortalSelector(
    chapters: List<MapChapter>,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    enabled = selectedIndex > 0,
                    onClick = { onIndexChanged(selectedIndex - 1) }
                ) {
                    Text(
                        "◀", 
                        color = if (selectedIndex > 0) Color(0xFFFBBF24) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                val currentChapter = chapters[selectedIndex]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🏆 当前冒险章节 🏆",
                        color = Color(0xFFFBBF24),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentChapter.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    enabled = selectedIndex < chapters.size - 1,
                    onClick = { onIndexChanged(selectedIndex + 1) }
                ) {
                    Text(
                        "▶", 
                        color = if (selectedIndex < chapters.size - 1) Color(0xFFFBBF24) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            val currentChapter = chapters[selectedIndex]
            val progress = if (currentChapter.totalCount > 0) {
                currentChapter.completedCount.toFloat() / currentChapter.totalCount
            } else 0f

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    color = Color(0xFF10B981),
                    trackColor = Color(0xFF334155),
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                )

                Text(
                    text = "通关度 ${currentChapter.completedCount}/${currentChapter.totalCount}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MapNodeComponent(
    level: Level,
    idx: Int,
    isBoss: Boolean,
    completedAnimProgress: Float = 1f,
    nextUnlockAnimProgress: Float = 1f,
    isAnimating: Boolean = false,
    onClick: () -> Unit
) {
    // Dynamic completed/unlocked values based on animation progress overrides!
    val isCompleted = if (isAnimating && completedAnimProgress < 1f) {
        completedAnimProgress >= 0.95f
    } else {
        level.isCompleted
    }

    val isUnlocked = if (isAnimating && nextUnlockAnimProgress < 1f) {
        nextUnlockAnimProgress >= 0.95f
    } else {
        level.isUnlocked
    }

    // Floating offset for active node
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatingOffsetY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    // Pulsing circle scale for active node background
    val pulsingScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .wrapContentSize()
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp)
        ) {
            val isCurrentlyActive = isUnlocked && !isCompleted
            if (isCurrentlyActive) {
                // Background ripple for active nodes
                Box(
                    modifier = Modifier
                        .scale(pulsingScale)
                        .size(if (isBoss) 74.dp else 54.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFBBF24).copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // User avatar pin hovering above active node
                Text(
                    text = "🧙‍♂️",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .offset(y = (-36).dp + floatingOffsetY.dp)
                )
            }

            // Outer ring
            val borderBrush = when {
                isAnimating && completedAnimProgress < 1f -> {
                    val startColor1 = if (isBoss) Color(0xFFEF4444) else Color(0xFFFBBF24)
                    val startColor2 = if (isBoss) Color(0xFFFBBF24) else Color(0xFFD97706)
                    val endColor1 = Color(0xFF10B981)
                    val endColor2 = Color(0xFF047857)
                    Brush.linearGradient(listOf(
                        androidx.compose.ui.graphics.lerp(startColor1, endColor1, completedAnimProgress),
                        androidx.compose.ui.graphics.lerp(startColor2, endColor2, completedAnimProgress)
                    ))
                }
                isAnimating && nextUnlockAnimProgress < 1f -> {
                    val startColor1 = Color(0xFF475569)
                    val startColor2 = Color(0xFF334155)
                    val endColor1 = Color(0xFFFBBF24)
                    val endColor2 = Color(0xFFD97706)
                    Brush.linearGradient(listOf(
                        androidx.compose.ui.graphics.lerp(startColor1, endColor1, nextUnlockAnimProgress),
                        androidx.compose.ui.graphics.lerp(startColor2, endColor2, nextUnlockAnimProgress)
                    ))
                }
                !isUnlocked -> Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF334155)))
                isCompleted -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
                isBoss -> Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFFBBF24)))
                else -> Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706)))
            }

            val innerColor = when {
                isAnimating && completedAnimProgress < 1f -> {
                    val start = if (isBoss) Color(0xFF7F1D1D) else Color(0xFF78350F)
                    val end = Color(0xFF064E3B)
                    androidx.compose.ui.graphics.lerp(start, end, completedAnimProgress)
                }
                isAnimating && nextUnlockAnimProgress < 1f -> {
                    val start = Color(0xFF334155).copy(alpha = 0.8f)
                    val end = Color(0xFF78350F)
                    androidx.compose.ui.graphics.lerp(start, end, nextUnlockAnimProgress)
                }
                !isUnlocked -> Color(0xFF334155).copy(alpha = 0.8f)
                isCompleted -> Color(0xFF064E3B)
                isBoss -> Color(0xFF7F1D1D)
                else -> Color(0xFF78350F)
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(if (isBoss) 68.dp else 52.dp)
                    .clip(CircleShape)
                    .background(innerColor)
                    .border(
                        width = if (isBoss) 3.5.dp else 2.5.dp,
                        brush = borderBrush,
                        shape = CircleShape
                    )
                    .scale(
                        if (isAnimating && completedAnimProgress < 1f) {
                            1f + (completedAnimProgress * (1f - completedAnimProgress) * 0.8f)
                        } else if (isAnimating && nextUnlockAnimProgress < 1f) {
                            1f + (nextUnlockAnimProgress * (1f - nextUnlockAnimProgress) * 0.8f)
                        } else {
                            1f
                        }
                    )
            ) {
                if (isAnimating && completedAnimProgress < 1f) {
                    Box(modifier = Modifier.alpha(1f - completedAnimProgress)) {
                        if (isBoss) {
                            Text(text = "👹", fontSize = 26.sp)
                        } else {
                            Text(
                                text = "$idx",
                                color = Color(0xFFFBBF24),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                    Box(modifier = Modifier.alpha(completedAnimProgress)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "已通关",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else if (isAnimating && nextUnlockAnimProgress < 1f) {
                    Box(modifier = Modifier.alpha(1f - nextUnlockAnimProgress)) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "锁定",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Box(modifier = Modifier.alpha(nextUnlockAnimProgress)) {
                        Text(
                            text = "$idx",
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                } else {
                    when {
                        !isUnlocked -> {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "锁定",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        isCompleted -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "已通关",
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "✓",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        isBoss -> {
                            Text(text = "👹", fontSize = 26.sp)
                        }
                        else -> {
                            Text(
                                text = "$idx",
                                color = Color(0xFFFBBF24),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Level name
        val textBgColor = when {
            isAnimating && completedAnimProgress < 1f -> {
                val start = if (isBoss) Color(0xFF7F1D1D).copy(alpha = 0.7f) else Color(0xFF78350F).copy(alpha = 0.7f)
                val end = Color(0xFF064E3B).copy(alpha = 0.6f)
                androidx.compose.ui.graphics.lerp(start, end, completedAnimProgress)
            }
            isAnimating && nextUnlockAnimProgress < 1f -> {
                val start = Color(0xFF334155).copy(alpha = 0.5f)
                val end = Color(0xFF78350F).copy(alpha = 0.7f)
                androidx.compose.ui.graphics.lerp(start, end, nextUnlockAnimProgress)
            }
            !isUnlocked -> Color(0xFF334155).copy(alpha = 0.5f)
            isCompleted -> Color(0xFF064E3B).copy(alpha = 0.6f)
            else -> Color(0xFF78350F).copy(alpha = 0.7f)
        }

        Surface(
            color = textBgColor,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.widthIn(max = 120.dp)
        ) {
            Text(
                text = level.name.replace("第一单元 ", "").replace("第二单元 ", "").replace("第三单元 ", ""),
                color = if (isUnlocked) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FloatingCrystalButton(
    icon: String,
    title: String,
    glowColor: Color,
    onClick: () -> Unit
) {
    // Elegant hovering effect
    val infiniteTransition = rememberInfiniteTransition(label = "floatingCrystal")
    val crystalOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200 + (title.length * 100), easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset(y = crystalOffset.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                .border(
                    width = 1.5.dp,
                    color = glowColor.copy(alpha = 0.7f),
                    shape = CircleShape
                )
        ) {
            Text(text = icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.8f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun LevelDetailDialog(
    level: Level,
    wordCount: Int,
    onDismiss: () -> Unit,
    onStartAdventure: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = null,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                // Top header icon
                Text(
                    text = if (level.name.contains("BOSS")) "👹" else "📜",
                    fontSize = 44.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = level.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = level.unitName,
                    color = Color(0xFFFBBF24),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Detail card
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📚 学习词数", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("$wordCount 词", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🪙 完美通关奖励", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("+50 🪙", color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💖 契约加成", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("+10 魂契", color = Color(0xFF06B6D4), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Button(
                    onClick = onStartAdventure,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFBBF24),
                        contentColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (level.isCompleted) "再次挑战" else "开始挑战",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("暂不前往", color = Color.LightGray)
                }
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.border(1.5.dp, Color(0xFFFBBF24).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
    )
}

@Composable
fun MathWorldSection(
    manifest: MathCourseManifest?,
    onNavigateToLesson: (String, String, String) -> Unit,
    context: android.content.Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF3B82F6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF3B82F6), androidx.compose.foundation.shape.CircleShape)
                    )
                    Text(
                        text = "人教版六年级上册",
                        color = Color(0xFF60A5FA),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "📐 数字机械城",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "连接逻辑与运算，征服分数乘除法、位置、方向、圆与百分数的重重谜题！",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }

        if (manifest == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF3B82F6))
            }
            return@Column
        }

        // Units List
        manifest.units.forEach { unitSummary ->
            val lockReason = remember(unitSummary.unitId) {
                com.example.data.math.MathContentLoader.getUnitLockReason(context, manifest.courseId, unitSummary, manifest.units)
            }
            val isUnlocked = (lockReason == com.example.data.math.MathUnitLockReason.NONE)

            val isDevUnlocked = remember(unitSummary.unitId) {
                if (com.example.BuildConfig.DEBUG && com.example.data.math.DeveloperMathSettings.isBypassMathPrerequisites(context)) {
                    val currentOrder = unitSummary.order
                    if (currentOrder > 1) {
                        val prevUnit = manifest.units.find { it.order == currentOrder - 1 }
                        prevUnit != null && !com.example.data.math.MathProgressManager.isUnitCompleted(context, manifest.courseId, prevUnit.unitId)
                    } else false
                } else false
            }

            val badgeBg = when {
                isDevUnlocked -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                lockReason == com.example.data.math.MathUnitLockReason.NONE -> Color(0xFF10B981).copy(alpha = 0.15f)
                lockReason == com.example.data.math.MathUnitLockReason.PREVIOUS_UNIT_NOT_COMPLETED -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                lockReason == com.example.data.math.MathUnitLockReason.CONTENT_NOT_READY -> Color(0xFF475569).copy(alpha = 0.15f)
                lockReason == com.example.data.math.MathUnitLockReason.DATA_LOAD_ERROR -> Color(0xFFEF4444).copy(alpha = 0.15f)
                else -> Color(0xFF475569).copy(alpha = 0.15f)
            }
            val badgeText = when {
                isDevUnlocked -> "开发者解锁"
                lockReason == com.example.data.math.MathUnitLockReason.NONE -> "已开放"
                lockReason == com.example.data.math.MathUnitLockReason.PREVIOUS_UNIT_NOT_COMPLETED -> "待解锁"
                lockReason == com.example.data.math.MathUnitLockReason.CONTENT_NOT_READY -> "建设中"
                lockReason == com.example.data.math.MathUnitLockReason.DATA_LOAD_ERROR -> "加载失败"
                else -> ""
            }
            val badgeTextColor = when {
                isDevUnlocked -> Color(0xFFA78BFA)
                lockReason == com.example.data.math.MathUnitLockReason.NONE -> Color(0xFF34D399)
                lockReason == com.example.data.math.MathUnitLockReason.PREVIOUS_UNIT_NOT_COMPLETED -> Color(0xFFFBBF24)
                lockReason == com.example.data.math.MathUnitLockReason.CONTENT_NOT_READY -> Color.Gray
                lockReason == com.example.data.math.MathUnitLockReason.DATA_LOAD_ERROR -> Color(0xFFF87171)
                else -> Color.Gray
            }

            val unitDetail = if (lockReason == com.example.data.math.MathUnitLockReason.NONE || lockReason == com.example.data.math.MathUnitLockReason.PREVIOUS_UNIT_NOT_COMPLETED) {
                remember(unitSummary.unitId) { com.example.data.math.MathContentLoader.loadUnit(context, unitSummary.unitId) }
            } else {
                null
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFF0F172A).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isUnlocked) Color(0xFF3B82F6).copy(alpha = 0.6f) else Color(0xFF334155).copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = unitSummary.title,
                                color = if (isUnlocked) Color.White else Color.Gray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "知识点: " + unitSummary.knowledgePoints.joinToString("、"),
                                color = if (isUnlocked) Color.LightGray else Color.DarkGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Unlock badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = badgeTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Card description
                    Text(
                        text = unitSummary.description,
                        color = if (isUnlocked) Color.LightGray else Color.Gray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    // Lock reason message
                    if (lockReason != com.example.data.math.MathUnitLockReason.NONE) {
                        val lockMsg = when (lockReason) {
                            com.example.data.math.MathUnitLockReason.PREVIOUS_UNIT_NOT_COMPLETED -> "🔒 完成前一单元后解锁"
                            com.example.data.math.MathUnitLockReason.CONTENT_NOT_READY -> "🛠️ 内容建设中"
                            com.example.data.math.MathUnitLockReason.DATA_LOAD_ERROR -> "⚠️ 课程暂时无法加载"
                            else -> ""
                        }
                        if (lockMsg.isNotEmpty()) {
                            val isError = (lockReason == com.example.data.math.MathUnitLockReason.DATA_LOAD_ERROR)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeBg.copy(alpha = 0.1f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = if (isError && !com.example.BuildConfig.DEBUG) "课程暂时无法加载，请稍后重试。" else lockMsg,
                                        color = badgeTextColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (isError && com.example.BuildConfig.DEBUG) {
                                    val errorDetail = remember(unitSummary.unitId) {
                                        com.example.data.math.MathContentLoader.getLoadErrorDetail(manifest.courseId, unitSummary.unitId)
                                    }
                                    if (errorDetail != null) {
                                        var expanded by remember { mutableStateOf(false) }
                                        Text(
                                            text = if (expanded) "收起诊断信息 ▴" else "查看诊断信息 ▾",
                                            color = Color(0xFF60A5FA),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clickable { expanded = !expanded }
                                                .padding(vertical = 4.dp)
                                        )
                                        if (expanded) {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.Black.copy(alpha = 0.3f))
                                                    .padding(8.dp)
                                            ) {
                                                Text("CourseId: ${errorDetail.courseId}", color = Color.LightGray, fontSize = 11.sp)
                                                Text("UnitId: ${errorDetail.unitId}", color = Color.LightGray, fontSize = 11.sp)
                                                Text("AssetPath: ${errorDetail.assetPath}", color = Color.LightGray, fontSize = 11.sp)
                                                Text("FailureStage: ${errorDetail.failureStage}", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                errorDetail.exceptionClass?.let {
                                                    Text("Exception: $it", color = Color(0xFFF87171), fontSize = 11.sp)
                                                }
                                                errorDetail.message?.let {
                                                    Text("Message: $it", color = Color(0xFFF87171), fontSize = 11.sp)
                                                }
                                                if (errorDetail.validatorErrors.isNotEmpty()) {
                                                    Text("Validation Errors:", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    errorDetail.validatorErrors.forEach { err ->
                                                        Text("  • $err", color = Color(0xFFFCA5A5), fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Text("无可用诊断信息", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (unitDetail != null) {
                        Divider(color = Color(0xFF334155).copy(alpha = 0.5f))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val formalLessons = unitDetail.lessons.filter { it.isFormalLesson() }.sortedBy { it.order }
                            val devLessons = unitDetail.lessons.filter { it.isEngineTestLesson() }

                            formalLessons.forEach { lesson ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = lesson.title,
                                            color = if (isUnlocked) Color.White else Color.Gray,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = lesson.objective,
                                            color = if (isUnlocked) Color.Gray else Color.DarkGray,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (isUnlocked) {
                                                onNavigateToLesson(manifest.courseId, unitSummary.unitId, lesson.lessonId)
                                            } else {
                                                android.widget.Toast.makeText(context, "🔒 请先解锁当前单元以开始探险！", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isUnlocked) Color(0xFF3B82F6) else Color(0xFF475569).copy(alpha = 0.4f)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("enter_math_lesson_${lesson.lessonId}")
                                    ) {
                                        Text(
                                            text = if (isUnlocked) "开始探险" else "未解锁",
                                            color = if (isUnlocked) Color.White else Color.Gray,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Developer test course section
                            if (isUnlocked && devLessons.isNotEmpty()) {
                                Text(
                                    text = "🛠️ 开发者测试 / 引擎验证",
                                    color = Color(0xFFA5B4FC),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                )
                                devLessons.forEach { lesson ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1E1E2F).copy(alpha = 0.4f))
                                            .border(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "⚙️ " + lesson.title,
                                                color = Color(0xFFA5B4FC),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = lesson.objective,
                                                color = Color.Gray,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(top = 2.dp),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { onNavigateToLesson(manifest.courseId, unitSummary.unitId, lesson.lessonId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("enter_math_lesson_${lesson.lessonId}")
                                        ) {
                                            Text("引擎验证", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnglishWorldSection(
    onNavigateToLesson: (String, String, String) -> Unit,
    context: android.content.Context
) {
    var selectedCourseId by remember {
        mutableStateOf(com.example.data.english.EnglishProgressManager.getSelectedCourseId(context))
    }
    val manifest = remember(selectedCourseId) {
        com.example.data.english.EnglishContentLoader.loadManifest(context, selectedCourseId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Primary & Secondary Hierarchical Switcher Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A).copy(alpha = 0.95f), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Primary Level: Grade Selection Header (主层级 - 年级核心巨幕切片)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(3 to "三年级", 4 to "四年级").forEach { (grade, label) ->
                    val isGradeSelected = if (selectedCourseId.contains("_g4_")) grade == 4 else grade == 3
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .then(
                                if (isGradeSelected) Modifier.background(
                                    brush = Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFFD946EF))),
                                    shape = RoundedCornerShape(10.dp)
                                ) else Modifier
                            )
                            .clickable {
                                val currentSem = if (selectedCourseId.endsWith("_s2")) "s2" else "s1"
                                val nextCourseId = "english_pep_2013_g${grade}_$currentSem"
                                selectedCourseId = nextCourseId
                                com.example.data.english.EnglishProgressManager.setSelectedCourseId(context, nextCourseId)
                            }
                            .padding(vertical = 10.dp)
                            .testTag("grade_tab_g$grade"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isGradeSelected) Color.White.copy(alpha = 0.25f) else Color(0xFF334155),
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "G$grade",
                                        color = if (isGradeSelected) Color.White else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                            Text(
                                text = label,
                                color = if (isGradeSelected) Color.White else Color(0xFF94A3B8),
                                fontWeight = if (isGradeSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // 2. Secondary Level: Semester Sub-Filter Chips (次层级 - 学期微调切片)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "学期选择",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isGrade4 = selectedCourseId.contains("_g4_")
                    listOf(1 to "上册", 2 to "下册").forEach { (sem, label) ->
                        val isSemSelected = selectedCourseId.endsWith("_s$sem")
                        val isEnabled = true
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    when {
                                        isSemSelected -> Color(0xFFEC4899).copy(alpha = 0.15f)
                                        !isEnabled -> Color(0xFF1E293B).copy(alpha = 0.3f)
                                        else -> Color(0xFF1E293B)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = when {
                                        isSemSelected -> Color(0xFFEC4899)
                                        !isEnabled -> Color(0xFF334155).copy(alpha = 0.3f)
                                        else -> Color(0xFF334155)
                                    },
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable(enabled = isEnabled) {
                                    val nextCourseId = if (isGrade4) "english_pep_2013_g4_s$sem" else "english_pep_2013_g3_s$sem"
                                    selectedCourseId = nextCourseId
                                    com.example.data.english.EnglishProgressManager.setSelectedCourseId(context, nextCourseId)
                                }
                                .padding(vertical = 6.dp, horizontal = 10.dp)
                                .testTag("semester_tab_${if (isGrade4) "g4" else "g3"}_s$sem"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isSemSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEC4899))
                                    )
                                }
                                if (!isEnabled) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = label,
                                    color = when {
                                        isSemSelected -> Color(0xFFF472B6)
                                        !isEnabled -> Color(0xFF475569)
                                        else -> Color(0xFF94A3B8)
                                    },
                                    fontWeight = if (isSemSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hero Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEC4899)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val headerTag = when (selectedCourseId) {
                    "english_pep_2013_g4_s1" -> "PEP 2013审定版四年级上册"
                    "english_pep_2013_g4_s2" -> "PEP 2013审定版四年级下册"
                    "english_pep_2013_g3_s2" -> "PEP 2013审定版三年级下册"
                    else -> "PEP 2013审定版三年级上册"
                }
                val heroTitle = when (selectedCourseId) {
                    "english_pep_2013_g4_s1" -> "🏫 梦幻新教室 · 上册"
                    "english_pep_2013_g4_s2" -> "🏫 梦幻新校园 · 下册"
                    "english_pep_2013_g3_s2" -> "🌸 星语海港 · 下册"
                    else -> "🌸 星语海港 · 上册"
                }
                val heroDesc = when (selectedCourseId) {
                    "english_pep_2013_g4_s1" -> "走进四年级新世界！探索宽敞明亮的新教室，共同打扫、整理空间，开展长元音 a-e 的奇妙拼读之旅。"
                    "english_pep_2013_g4_s2" -> "欢迎来到我们的学校！探索操场、图书馆和美术、音乐教室，开启长元音 -er 词尾的奇妙探索之旅。"
                    "english_pep_2013_g3_s2" -> "重返校园，开启新学期冒险！学习打招呼、国家与家庭成员表达，多感官综合互动课程。"
                    else -> "探秘蔚蓝海岸，开启英语趣味冒险！掌握见面问候、文具物品等多感官综合互动课程。"
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFFEC4899), androidx.compose.foundation.shape.CircleShape)
                    )
                    Text(
                        text = headerTag,
                        color = Color(0xFFF472B6),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = heroTitle,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = heroDesc,
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }

        if (manifest == null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("⚠️ 课程数据加载失败或文件暂未建立", color = Color(0xFFF87171), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            selectedCourseId = "english_pep_2013_g3_s1"
                            com.example.data.english.EnglishProgressManager.setSelectedCourseId(context, "english_pep_2013_g3_s1")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                    ) {
                        Text("重置至三年级上册", color = Color.White)
                    }
                }
            }
            return@Column
        }

        // Units List
        manifest.units.forEach { unitSummary ->
            val lockReason = remember(selectedCourseId, unitSummary.unitId) {
                com.example.data.english.EnglishContentLoader.getUnitLockReason(context, manifest.courseId, unitSummary, manifest.units)
            }
            val isUnlocked = (lockReason == com.example.data.english.EnglishUnitLockReason.NONE)

            val isDevUnlocked = remember(selectedCourseId, unitSummary.unitId) {
                if (com.example.BuildConfig.DEBUG && com.example.data.english.EnglishProgressManager.isDevBypassEnabled(context)) {
                    val index = manifest.units.indexOfFirst { it.unitId == unitSummary.unitId }
                    if (index > 0) {
                        val prevUnit = manifest.units[index - 1]
                        !com.example.data.english.EnglishProgressManager.isUnitCompleted(context, manifest.courseId, prevUnit.unitId)
                    } else false
                } else false
            }

            val isCompleted = remember(selectedCourseId, unitSummary.unitId) {
                com.example.data.english.EnglishProgressManager.isUnitCompleted(context, manifest.courseId, unitSummary.unitId)
            }

            val isStarted = remember(selectedCourseId, unitSummary.unitId) {
                val completed = com.example.data.english.EnglishProgressManager.getCompletedLessons(context)
                completed.any { it.startsWith("${selectedCourseId}_${unitSummary.unitId}") || it.startsWith("${unitSummary.unitId}_") }
            }

            val badgeBg = when {
                isCompleted -> Color(0xFF10B981).copy(alpha = 0.15f)
                isStarted -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                isDevUnlocked -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                lockReason == com.example.data.english.EnglishUnitLockReason.NONE -> Color(0xFF10B981).copy(alpha = 0.15f)
                lockReason == com.example.data.english.EnglishUnitLockReason.PREVIOUS_UNIT_NOT_COMPLETED -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                lockReason == com.example.data.english.EnglishUnitLockReason.CONTENT_NOT_READY -> Color(0xFF475569).copy(alpha = 0.15f)
                lockReason == com.example.data.english.EnglishUnitLockReason.DATA_LOAD_ERROR -> Color(0xFFEF4444).copy(alpha = 0.15f)
                else -> Color(0xFF475569).copy(alpha = 0.15f)
            }
            val badgeText = when {
                isCompleted -> "再次学习"
                isStarted -> "继续学习"
                isDevUnlocked -> "开发者解锁"
                lockReason == com.example.data.english.EnglishUnitLockReason.NONE -> "开始学习"
                lockReason == com.example.data.english.EnglishUnitLockReason.PREVIOUS_UNIT_NOT_COMPLETED -> "待解锁"
                lockReason == com.example.data.english.EnglishUnitLockReason.CONTENT_NOT_READY -> "内容建设中"
                lockReason == com.example.data.english.EnglishUnitLockReason.DATA_LOAD_ERROR -> "课程暂时无法加载"
                else -> ""
            }
            val badgeTextColor = when {
                isCompleted -> Color(0xFF34D399)
                isStarted -> Color(0xFF60A5FA)
                isDevUnlocked -> Color(0xFFA78BFA)
                lockReason == com.example.data.english.EnglishUnitLockReason.NONE -> Color(0xFF34D399)
                lockReason == com.example.data.english.EnglishUnitLockReason.PREVIOUS_UNIT_NOT_COMPLETED -> Color(0xFFFBBF24)
                lockReason == com.example.data.english.EnglishUnitLockReason.CONTENT_NOT_READY -> Color(0xFF94A3B8)
                lockReason == com.example.data.english.EnglishUnitLockReason.DATA_LOAD_ERROR -> Color(0xFFF87171)
                else -> Color(0xFF94A3B8)
            }

            if (unitSummary.isRecycle) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF831843).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF472B6)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onNavigateToLesson(manifest.courseId, unitSummary.unitId, "recycle")
                        }
                        .testTag("english_recycle_card_${unitSummary.order}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(Color(0xFFEC4899), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎉", fontSize = 24.sp)
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = unitSummary.title,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEC4899).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "阶段综合复习",
                                        color = Color(0xFFF472B6),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "覆盖 Unit 1—Unit 3 | P32-P35 阶段庆典大厅",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "进入复习",
                            tint = Color(0xFFF472B6),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isUnlocked || isDevUnlocked) Color(0xFFEC4899) else Color(0xFF334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isUnlocked || isDevUnlocked) {
                            onNavigateToLesson(manifest.courseId, unitSummary.unitId, unitSummary.unitId)
                        }
                        .testTag("english_unit_card_${unitSummary.order}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(
                                    if (isUnlocked || isDevUnlocked) Color(0xFFEC4899).copy(alpha = 0.15f) else Color(0xFF334155),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unitSummary.unitDisplayTag,
                                color = if (isUnlocked || isDevUnlocked) Color(0xFFEC4899) else Color.Gray,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = unitSummary.title,
                                    color = if (isUnlocked || isDevUnlocked) Color.White else Color.Gray,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(badgeBg, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        color = badgeTextColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = unitSummary.description,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        Icon(
                            imageVector = if (isUnlocked || isDevUnlocked) Icons.AutoMirrored.Filled.ArrowForward else Icons.Filled.Lock,
                            contentDescription = "进入学习",
                            tint = if (isUnlocked || isDevUnlocked) Color(0xFFEC4899) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
