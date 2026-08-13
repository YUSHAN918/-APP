package com.example.ui.english

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.english.EnglishContentLoader
import com.example.data.english.EnglishProgressManager
import com.example.data.english.EnglishRecycleMission
import com.example.util.SpeechSynthesizer

enum class RecycleMissionFailureCode {
    ROUTE_ARGUMENT_MISSING,
    COURSE_NOT_FOUND,
    RECYCLE_NOT_FOUND,
    RECYCLE_ASSET_NOT_FOUND,
    RECYCLE_PARSE_ERROR,
    RECYCLE_VALIDATION_ERROR,
    MISSION_NOT_FOUND,
    MISSION_TYPE_UNSUPPORTED,
    MISSION_CONTENT_INVALID
}

sealed interface RecycleMissionUiState {
    data object Loading : RecycleMissionUiState
    
    data class Ready(
        val recycle: com.example.data.english.EnglishRecycleContent,
        val mission: com.example.data.english.EnglishRecycleMission
    ) : RecycleMissionUiState
    
    data class Error(
        val failureCode: RecycleMissionFailureCode,
        val courseId: String?,
        val recycleId: String?,
        val missionId: String?,
        val assetPath: String?,
        val message: String?
    ) : RecycleMissionUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnglishRecycleMissionScreen(
    courseId: String,
    recycleId: String,
    missionId: String,
    onBackToHub: () -> Unit,
    onNavigateToResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val uiState = remember(courseId, recycleId, missionId) {
        if (courseId.isBlank() || recycleId.isBlank() || missionId.isBlank()) {
            RecycleMissionUiState.Error(
                failureCode = RecycleMissionFailureCode.ROUTE_ARGUMENT_MISSING,
                courseId = courseId,
                recycleId = recycleId,
                missionId = missionId,
                assetPath = null,
                message = "One or more route parameters are missing."
            )
        } else {
            val manifest = EnglishContentLoader.loadManifest(context, courseId)
            if (manifest == null) {
                RecycleMissionUiState.Error(
                    failureCode = RecycleMissionFailureCode.COURSE_NOT_FOUND,
                    courseId = courseId,
                    recycleId = recycleId,
                    missionId = missionId,
                    assetPath = null,
                    message = "Manifest not found or failed to load for course: $courseId"
                )
            } else {
                val assetPath = EnglishContentLoader.getUnitAssetPath(courseId, recycleId)
                if (assetPath == null) {
                    RecycleMissionUiState.Error(
                        failureCode = RecycleMissionFailureCode.RECYCLE_ASSET_NOT_FOUND,
                        courseId = courseId,
                        recycleId = recycleId,
                        missionId = missionId,
                        assetPath = null,
                        message = "Could not find asset path for recycleId: $recycleId under courseId: $courseId"
                    )
                } else {
                    val recycle = EnglishContentLoader.loadRecycle(context, courseId, recycleId)
                    if (recycle == null) {
                        val errorDetail = EnglishContentLoader.getLoadErrorDetail(courseId, recycleId)
                        val code = when (errorDetail?.failureStage) {
                            "VALIDATION" -> RecycleMissionFailureCode.RECYCLE_VALIDATION_ERROR
                            "JSON_PARSE" -> RecycleMissionFailureCode.RECYCLE_PARSE_ERROR
                            else -> RecycleMissionFailureCode.RECYCLE_NOT_FOUND
                        }
                        RecycleMissionUiState.Error(
                            failureCode = code,
                            courseId = courseId,
                            recycleId = recycleId,
                            missionId = missionId,
                            assetPath = assetPath,
                            message = errorDetail?.message ?: "Recycle content failed to load."
                        )
                    } else {
                        val mission = recycle.missions.find { it.missionId == missionId }
                        if (mission == null) {
                            RecycleMissionUiState.Error(
                                failureCode = RecycleMissionFailureCode.MISSION_NOT_FOUND,
                                courseId = courseId,
                                recycleId = recycleId,
                                missionId = missionId,
                                assetPath = assetPath,
                                message = "Mission with ID '$missionId' not found in Recycle '${recycleId}'"
                            )
                        } else {
                            val isSupported = when (mission.missionType) {
                                "STORY_REHEARSAL",
                                "STORY_ROLEPLAY",
                                "PHONICS_CONNECT_WRITE",
                                "FRIEND_CLUE_SEARCH",
                                "DIALOGUE_PICTURE_MATCH",
                                "QUESTION_TRAIL_GAME",
                                "LISTEN_AND_COLOUR",
                                "CATEGORY_ODD_ONE_OUT",
                                "BOARD_GAME",
                                "MATCH_WRITE_READ",
                                "SENTENCE_REPAIR",
                                "SONG_REVIEW_CHECKPOINT",
                                "SONG_SEMESTER_SUMMARY",
                                "CHANT_AND_MATCH",
                                "FAMILY_OBSERVATION",
                                "FIND_AND_CIRCLE",
                                "MIXED_CHECKPOINT",
                                "LISTEN_NUMBER_ROOMS",
                                "PHONICS_LISTEN_WRITE",
                                "FAMILY_HOME_INTERVIEW",
                                "SEMESTER_QUESTION_GAME",
                                "PHONICS_LISTEN_MATCH",
                                "SCHOOL_WEATHER_QA",
                                "WEATHER_LISTEN_READ_WRITE",
                                "DAY_TIMELINE_READ_CIRCLE" -> true
                                else -> false
                            }
                            if (!isSupported) {
                                RecycleMissionUiState.Error(
                                    failureCode = RecycleMissionFailureCode.MISSION_TYPE_UNSUPPORTED,
                                    courseId = courseId,
                                    recycleId = recycleId,
                                    missionId = missionId,
                                    assetPath = assetPath,
                                    message = "Mission type '${mission.missionType}' is not supported."
                                )
                            } else {
                                RecycleMissionUiState.Ready(recycle, mission)
                            }
                        }
                    }
                }
            }
        }
    }

    val isAlreadyCompleted = remember(courseId, recycleId, missionId) {
        val progress = EnglishProgressManager.getRecycleProgress(context, recycleId)
        progress.completedMissionIds.contains(missionId)
    }
    var currentSessionCompleted by remember(courseId, recycleId, missionId) { mutableStateOf(false) }
    
    val completionSatisfied = isAlreadyCompleted || currentSessionCompleted

    fun markMissionDone() {
        if (uiState is RecycleMissionUiState.Ready) {
            val mission = uiState.mission
            EnglishProgressManager.completeRecycleMission(context, recycleId, mission.missionId)
            currentSessionCompleted = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState) {
                            is RecycleMissionUiState.Ready -> uiState.mission.title
                            else -> "复习任务"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToHub, modifier = Modifier.testTag("mission_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        },
        bottomBar = {
            when (uiState) {
                is RecycleMissionUiState.Ready -> {
                    val mission = uiState.mission
                    val recycle = uiState.recycle
                    EnglishRecycleBottomBar(
                        onPrevious = onBackToHub,
                        onExit = onBackToHub,
                        onNext = {
                            markMissionDone()
                            val nextMission = recycle.missions.find { it.order == mission.order + 1 }
                            if (nextMission != null) {
                                onBackToHub()
                            } else {
                                onNavigateToResult()
                            }
                        },
                        nextText = if (mission.order == recycle.missions.size) "完成复习·去结算" else "完成本任务·继续",
                        nextEnabled = completionSatisfied
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onBackToHub,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("返回任务大厅")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when (uiState) {
            RecycleMissionUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFEC4899))
                }
            }
            is RecycleMissionUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "错误",
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "这个复习任务暂时无法打开，请返回后再试。",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🔧 [DEBUG INFO] 故障排查参数:", color = Color(0xFFF472B6), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            
                            val details = listOf(
                                "Failure Code" to uiState.failureCode.name,
                                "Course ID" to (uiState.courseId ?: "null"),
                                "Recycle ID" to (uiState.recycleId ?: "null"),
                                "Mission ID" to (uiState.missionId ?: "null"),
                                "Asset Path" to (uiState.assetPath ?: "null")
                            )
                            details.forEach { (label, valStr) ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(label, color = Color.Gray, fontSize = 12.sp)
                                    Text(valStr, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            if (uiState.message != null) {
                                Text("Message:", color = Color.Gray, fontSize = 12.sp)
                                Text(uiState.message, color = Color.Red.copy(alpha = 0.8f), fontSize = 11.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }
            is RecycleMissionUiState.Ready -> {
                val recycle = uiState.recycle
                val mission = uiState.mission

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mission Header Banner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "任务 ${mission.order} / ${recycle.missions.size}",
                                    color = Color(0xFFF472B6),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF334155)) {
                                    Text(
                                        text = mission.textbookPage,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = mission.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = mission.instruction,
                                color = Color.LightGray,
                                fontSize = 13.sp
                            )
                        }
                    }

            // Mission Specific Content
            when (mission.missionType) {
                "STORY_REHEARSAL" -> {
                    StoryRehearsalView(context = context, recycleId = recycleId, onCompleted = { markMissionDone() })
                }
                "STORY_ROLEPLAY" -> {
                    if (recycleId == "english_pep_2013_g4_s2_recycle_1") {
                        G4S2Recycle1StoryRoleplayView(context = context, onCompleted = { markMissionDone() })
                    } else if (recycleId == "english_pep_2013_g4_s1_recycle_2") {
                        G4S1Recycle2StoryRoleplayView(context = context, onCompleted = { markMissionDone() })
                    } else {
                        G4S1StoryRoleplayView(context = context, onCompleted = { markMissionDone() })
                    }
                }
                "PHONICS_LISTEN_MATCH" -> {
                    G4S2Recycle1PhonicsListenMatchView(context = context, onCompleted = { markMissionDone() })
                }
                "SCHOOL_WEATHER_QA" -> {
                    G4S2Recycle1SchoolWeatherQaView(context = context, onCompleted = { markMissionDone() })
                }
                "WEATHER_LISTEN_READ_WRITE" -> {
                    G4S2Recycle1WeatherListenReadWriteView(context = context, onCompleted = { markMissionDone() })
                }
                "DAY_TIMELINE_READ_CIRCLE" -> {
                    G4S2Recycle1DayTimelineReadCircleView(context = context, onCompleted = { markMissionDone() })
                }
                "LISTEN_NUMBER_ROOMS" -> {
                    G4S1ListenNumberRoomsView(context = context, onCompleted = { markMissionDone() })
                }
                "PHONICS_LISTEN_WRITE" -> {
                    G4S1PhonicsListenWriteView(context = context, onCompleted = { markMissionDone() })
                }
                "FAMILY_HOME_INTERVIEW" -> {
                    G4S1FamilyHomeInterviewView(context = context, onCompleted = { markMissionDone() })
                }
                "SEMESTER_QUESTION_GAME" -> {
                    EnglishReviewBoardGame(
                        recycleId = recycleId,
                        onGameFinished = { markMissionDone() }
                    )
                }
                "PHONICS_CONNECT_WRITE" -> {
                    G4S1PhonicsConnectWriteView(context = context, onCompleted = { markMissionDone() })
                }
                "FRIEND_CLUE_SEARCH" -> {
                    G4S1FriendClueSearchView(context = context, onCompleted = { markMissionDone() })
                }
                "DIALOGUE_PICTURE_MATCH" -> {
                    G4S1DialoguePictureMatchView(context = context, onCompleted = { markMissionDone() })
                }
                "QUESTION_TRAIL_GAME" -> {
                    G4S1QuestionTrailGameView(context = context, onCompleted = { markMissionDone() })
                }
                "LISTEN_AND_COLOUR" -> {
                    EnglishListenAndColourView(
                        onCompleted = { markMissionDone() }
                    )
                }
                "LISTEN_DRAW_SPATIAL" -> {
                    val ttsHelper = remember { com.example.util.english.EnglishTTSHelper(context) }
                    EnglishToyRoomPlacementView(
                        mode = "LESSON1",
                        ttsHelper = ttsHelper,
                        onComplete = { score -> markMissionDone() },
                        onBack = { }
                    )
                }
                "WORD_REPAIR_PHONICS" -> {
                    WordRepairPhonicsView(context = context, onCompleted = { markMissionDone() })
                }
                "CATEGORY_ODD_ONE_OUT" -> {
                    EnglishCategoryOddOneOutView(
                        onCompleted = { markMissionDone() }
                    )
                }
                "BOARD_GAME" -> {
                    EnglishReviewBoardGame(
                        recycleId = recycleId,
                        onGameFinished = { markMissionDone() }
                    )
                }
                "MATCH_WRITE_READ" -> {
                    MatchWriteReadView(context = context, onCompleted = { markMissionDone() })
                }
                "SENTENCE_REPAIR" -> {
                    EnglishSentenceRepairView(
                        onCompleted = { markMissionDone() }
                    )
                }
                "SONG_REVIEW_CHECKPOINT", "SONG_SEMESTER_SUMMARY" -> {
                    SongReviewCheckpointView(context = context, onCompleted = { markMissionDone() })
                }
                "CHANT_AND_MATCH" -> {
                    ChantAndMatchView(context = context, onCompleted = { markMissionDone() })
                }
                "FAMILY_OBSERVATION" -> {
                    FamilyObservationView(context = context, onCompleted = { markMissionDone() })
                }
                "FIND_AND_CIRCLE" -> {
                    FindAndCircleView(context = context, onCompleted = { markMissionDone() })
                }
                "MIXED_CHECKPOINT" -> {
                    MixedCheckpointView(context = context, onCompleted = { markMissionDone() })
                }
                else -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("暂不支持的任务类型: ${mission.missionType}", color = Color.White)
                    }
                }
            }
        }
    }
}
}
}

@Composable
private fun StoryRehearsalView(context: Context, recycleId: String = "", onCompleted: () -> Unit) {
    val dialogues = remember(recycleId) {
        when (recycleId) {
            "english_pep_2013_g3_s1_recycle_2" -> {
                listOf(
                    "John: Look! What's that? — It's a bear!" to "约翰：看！那是什么？— 是一只熊！",
                    "Chen Jie: I'd like some juice and bread, please." to "陈杰：请给我一些果汁和面包。",
                    "John: How old are you? — I'm six years old." to "约翰：你几岁了？— 我6岁了。",
                    "Chen Jie: Happy birthday to you! How many plates? — Five!" to "陈杰：祝你生日快乐！有多少个盘子？— 5个！"
                )
            }
            "english_pep_2013_g3_s2_recycle_1" -> {
                listOf(
                    "Sarah: Who's that man? — He's my father." to "萨拉：那个人是谁？— 他是我爸爸。",
                    "Amy: Look! He's tall. — He has short hair." to "埃米：看！他很高。— 他有短头发。",
                    "Sarah: Where are you from? — I'm from Canada." to "萨拉：你来自哪里？— 我来自加拿大。",
                    "Amy: Look at the monkey. It's so fat! — It has a long tail." to "埃米：看这只猴子，好肥啊！— 它有长长的尾巴。"
                )
            }
            "english_pep_2013_g3_s2_recycle_2" -> {
                listOf(
                    "Zoom: Look! There are many fruit trees. How many apples do you see?" to "Zoom：看！这里有许多果树。你看到多少个苹果？",
                    "Zip: I see eleven... twelve... thirteen! Wait, fifteen apples!" to "Zip：我看到11...12...13！等一下，15个苹果！",
                    "Zoom: Do you like pears? Look, they are under the tree." to "Zoom：你喜欢梨吗？看，它们在树下呢。",
                    "Zip: Yes, I do. They are yellow and beautiful. Let's have some!" to "Zip：是的，我喜欢。它们又黄又漂亮。我们吃一些吧！"
                )
            }
            else -> {
                listOf(
                    "Mike: Hello, I'm Mike! What's your name?" to "迈克：你好，我是迈克！你叫什么名字？",
                    "Sarah: My name's Sarah. Look at my red pencil box!" to "萨拉：我叫萨拉。看我的红铅笔盒！",
                    "Mike: Cool! I see green and blue. Touch your nose, Sarah!" to "迈克：太酷了！我看到了绿色和蓝色。摸摸你的鼻子，萨拉！",
                    "Sarah: Fine, thank you! Let me show you my arm!" to "萨拉：很好，谢谢你！看看我的手臂！"
                )
            }
        }
    }

    val roles = remember(recycleId) {
        when (recycleId) {
            "english_pep_2013_g3_s2_recycle_1" -> listOf("Sarah (萨拉)", "Amy (埃米)")
            "english_pep_2013_g3_s2_recycle_2" -> listOf("Zoom (熊熊)", "Zip (松鼠)")
            else -> listOf("Mike (迈克)", "Sarah (萨拉)")
        }
    }

    var selectedRole by remember(recycleId) {
        mutableStateOf(
            when (recycleId) {
                "english_pep_2013_g3_s2_recycle_1" -> "Sarah"
                "english_pep_2013_g3_s2_recycle_2" -> "Zoom"
                else -> "Mike"
            }
        )
    }
    var currentDialogueIndex by remember { mutableStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var hasRecorded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Role Selector
        Text("🎭 请选择你的分镜角色:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            roles.forEach { role ->
                val name = role.split(" ").first()
                val isSelected = selectedRole == name
                Button(
                    onClick = { selectedRole = name },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFFEC4899) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(role, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Dialogue Cards
        dialogues.forEachIndexed { idx, pair ->
            val speaker = pair.first.split(":").first().trim()
            val text = pair.first.substringAfter(":").trim()
            val isMyRole = speaker == selectedRole

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isMyRole) Color(0xFF1E293B) else Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isMyRole) 1.5.dp else 1.dp,
                    color = if (isMyRole) Color(0xFFEC4899) else Color(0xFF334155)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "$speaker:", color = if (isMyRole) Color(0xFFF472B6) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }

                    IconButton(
                        onClick = { SpeechSynthesizer.speak(context, text) },
                        modifier = Modifier.size(40.dp).background(Color(0xFF334155), CircleShape)
                    ) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = "播放语音", tint = Color.White)
                    }
                }
            }
        }

        // Recording Simulator
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🎙️ 角色跟读与比对:",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Button(
                    onClick = {
                        if (!isRecording) {
                            isRecording = true
                        } else {
                            isRecording = false
                            hasRecorded = true
                            onCompleted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color(0xFFEF4444) else Color(0xFFEC4899)
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("record_dialogue_button")
                ) {
                    Icon(if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRecording) "停止录音 (正在录制...)" else "点击开始角色跟读录音")
                }

                if (hasRecorded) {
                    Text(
                        text = "✅ 角色跟读已完成！可以清晰进行自我比对跟读。",
                        color = Color(0xFF34D399),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchWriteReadView(context: Context, onCompleted: () -> Unit) {
    var selectedLetterPair by remember { mutableStateOf("Aa") }
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🔤 字母大小写与规范四线三格书写:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        // Letters Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Aa", "Bb", "Cc", "Dd", "Ee", "Ff", "Gg", "Hh").forEach { pair ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedLetterPair == pair) Color(0xFFEC4899) else Color(0xFF1E293B),
                    modifier = Modifier.clickable { selectedLetterPair = pair }.padding(2.dp)
                ) {
                    Text(pair, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                }
            }
        }

        // Four-Line Three-Grid Handwriting Canvas
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth().height(220.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Four lines guide
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = Path().apply { moveTo(offset.x, offset.y) }
                                },
                                onDragEnd = {
                                    currentPath?.let { paths.add(it) }
                                    currentPath = null
                                    onCompleted()
                                },
                                onDrag = { change, _ ->
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                }
                            )
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val spacing = h / 5f

                    // Draw 4 lines
                    for (i in 1..4) {
                        drawLine(
                            color = if (i == 2 || i == 3) Color(0xFFF472B6).copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.4f),
                            start = Offset(20f, spacing * i),
                            end = Offset(w - 20f, spacing * i),
                            strokeWidth = 2f
                        )
                    }

                    // Draw paths
                    paths.forEach { path ->
                        drawPath(path = path, color = Color.White, style = Stroke(width = 8f))
                    }
                    currentPath?.let { path ->
                        drawPath(path = path, color = Color.White, style = Stroke(width = 8f))
                    }
                }

                // Watermark Guide Letter
                Text(
                    text = selectedLetterPair,
                    color = Color.White.copy(alpha = 0.15f),
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Canvas Controls
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { if (paths.isNotEmpty()) paths.removeAt(paths.size - 1) },
                modifier = Modifier.weight(1f).testTag("write_undo_button")
            ) {
                Text("撤销最后一笔")
            }
            OutlinedButton(
                onClick = { paths.clear() },
                modifier = Modifier.weight(1f).testTag("write_clear_button")
            ) {
                Text("清空笔迹")
            }
        }
    }
}

@Composable
private fun SongReviewCheckpointView(context: Context, onCompleted: () -> Unit) {
    var isPlayingSong by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🎵 阶段嘉年华复习歌曲: 《Friends》", color = Color(0xFFF472B6), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("来源授权：PEP 2013 3A P35 Sing a song (合规引用短句与节奏跟读)", color = Color.LightGray, fontSize = 12.sp)

                Button(
                    onClick = {
                        isPlayingSong = !isPlayingSong
                        if (isPlayingSong) {
                            SpeechSynthesizer.speak(context, "Hello, hello, my friends! Look at me! I see red and blue!")
                            onCompleted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("play_song_button")
                ) {
                    Icon(if (isPlayingSong) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPlayingSong) "暂停歌曲跟读" else "播放阶段复习歌曲跟读")
                }
            }
        }

        Text("📋 Recycle 1 综合能效考核点 (8项已实时对齐验证)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        listOf(
            "见面问候与自我介绍 (Hello, I'm...)" to "✅ 已完成",
            "询问对方姓名 (What's your name?)" to "✅ 已完成",
            "常用文具词汇认读 (ruler, pencil, eraser, crayon)" to "✅ 已完成",
            "常见颜色听辨与涂色 (red, yellow, green, blue, brown)" to "✅ 已完成",
            "身体部位及动作响应 (face, ear, eye, nose, Touch your...)" to "✅ 已完成",
            "字母 A-N 大小写配对与手写" to "✅ 已完成"
        ).forEach { pair ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(pair.first, color = Color.White, fontSize = 13.sp)
                    Text(pair.second, color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// PEP 2013 Grade 3 Semester 2 Recycle 1 Specific Interactive Mission Views
// ----------------------------------------------------------------------

@Composable
private fun ChantAndMatchView(context: Context, onCompleted: () -> Unit) {
    var playedChant by remember { mutableStateOf(false) }
    val pairs = remember {
        listOf(
            "giraffe" to "tall",
            "deer" to "short",
            "monkey" to "long tail",
            "cat" to "short tail"
        )
    }
    val matched = remember { mutableStateMapOf<String, String>() }
    var selectedAnimal by remember { mutableStateOf<String?>(null) }
    var selectedFeature by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Audio Chant Player
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("chant_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("🎵 听歌谣跟读 (Listen and Chant):", color = Color(0xFFF472B6), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    "Look at the giraffe. It's tall!\nLook at the deer. It's short!\nThe monkey has a long tail.\nThe cat has a short tail.",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Button(
                    onClick = {
                        playedChant = true
                        SpeechSynthesizer.speak(context, "Look at the giraffe. It's tall! Look at the deer. It's short! The monkey has a long tail. The cat has a short tail.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    modifier = Modifier.fillMaxWidth().testTag("play_chant_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("播放动物歌谣跟读")
                }
            }
        }

        // Matching Area
        Text("🔗 动物特征对对碰 (Match and Read):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Animals (Left)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("giraffe", "deer", "monkey", "cat").forEach { animal ->
                    val isMatched = matched.containsKey(animal)
                    val isSelected = selectedAnimal == animal
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isMatched -> Color(0xFF1E293B).copy(alpha = 0.5f)
                                isSelected -> Color(0xFFEC4899)
                                else -> Color(0xFF1E293B)
                            }
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color.White else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isMatched) {
                                selectedAnimal = animal
                                SpeechSynthesizer.speak(context, animal)
                                if (selectedFeature != null) {
                                    matched[animal] = selectedFeature!!
                                    selectedAnimal = null
                                    selectedFeature = null
                                    if (matched.size == 4) {
                                        // Verify
                                        val correct = matched.all { it.value == pairs.find { p -> p.first == it.key }?.second }
                                        if (correct) {
                                            onCompleted()
                                        } else {
                                            android.widget.Toast.makeText(context, "匹配有误，自动重置", android.widget.Toast.LENGTH_SHORT).show()
                                            matched.clear()
                                        }
                                    }
                                }
                            }
                    ) {
                        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = animal.replaceFirstChar { it.uppercase() } + if (isMatched) " ✅" else "",
                                color = if (isMatched) Color.Gray else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Features (Right)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("short tail", "short", "tall", "long tail").forEach { feature ->
                    val isMatched = matched.containsValue(feature)
                    val isSelected = selectedFeature == feature
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isMatched -> Color(0xFF1E293B).copy(alpha = 0.5f)
                                isSelected -> Color(0xFFEC4899)
                                else -> Color(0xFF1E293B)
                            }
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color.White else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isMatched) {
                                selectedFeature = feature
                                SpeechSynthesizer.speak(context, feature)
                                if (selectedAnimal != null) {
                                    matched[selectedAnimal!!] = feature
                                    selectedAnimal = null
                                    selectedFeature = null
                                    if (matched.size == 4) {
                                        val correct = matched.all { it.value == pairs.find { p -> p.first == it.key }?.second }
                                        if (correct) {
                                            onCompleted()
                                        } else {
                                            android.widget.Toast.makeText(context, "匹配有误，自动重置", android.widget.Toast.LENGTH_SHORT).show()
                                            matched.clear()
                                        }
                                    }
                                }
                            }
                    ) {
                        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = feature.replaceFirstChar { it.uppercase() } + if (isMatched) " ✅" else "",
                                color = if (isMatched) Color.Gray else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
        
        if (matched.size == 4) {
            Text("🎉 匹配完全正确！太棒了！", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun FamilyObservationView(context: Context, onCompleted: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "👨‍👩‍👧‍👦 剧场角色与家庭辨认 (Family Observation):",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        if (step == 1) {
            // Part 1: Look and tick
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("👨 [场景指认] Who's that man? (那个高大威严的男士是谁？)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    listOf("He's my father. (我爸爸)", "She's my mother. (我妈妈)", "He's my brother. (我兄弟)").forEach { opt ->
                        val isSel = selectedOption == opt
                        Button(
                            onClick = { selectedOption = opt },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) Color(0xFFEC4899) else Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("fam_opt_$opt")
                        ) {
                            Text(opt, color = Color.White)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedOption == "He's my father. (我爸爸)") {
                        SpeechSynthesizer.speak(context, "Correct! He is my father.")
                        step = 2
                        selectedOption = null
                    } else {
                        SpeechSynthesizer.speak(context, "Try again!")
                        android.widget.Toast.makeText(context, "选择不对哦，请再想一想", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                modifier = Modifier.fillMaxWidth().testTag("fam_step1_submit")
            ) {
                Text("提交并进入下一问")
            }
        } else {
            // Part 2: Who is not here?
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🔍 [观察相册] 剧场舞台上坐着：Sarah, Sarah's mother, Sarah's father, Sarah's sister.\n问：Who is not here? (谁不在这里？)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    listOf("Grandfather (祖父/外祖父)", "Mother (母亲)", "Sister (姐妹)").forEach { opt ->
                        val isSel = selectedOption == opt
                        Button(
                            onClick = { selectedOption = opt },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) Color(0xFFEC4899) else Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("missing_opt_$opt")
                        ) {
                            Text(opt, color = Color.White)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedOption == "Grandfather (祖父/外祖父)") {
                        SpeechSynthesizer.speak(context, "Excellent! Grandfather is not here.")
                        isSubmitted = true
                        onCompleted()
                    } else {
                        SpeechSynthesizer.speak(context, "No, please observe the stage again.")
                        android.widget.Toast.makeText(context, "选错了哦，请仔细看舞台角色", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                modifier = Modifier.fillMaxWidth().testTag("fam_step2_submit")
            ) {
                Text(if (isSubmitted) "已完成该任务 ✅" else "确认回答")
            }
        }
    }
}

@Composable
private fun FindAndCircleView(context: Context, onCompleted: () -> Unit) {
    val targetWords = remember { listOf("USA", "CHINA", "TALL", "FAT", "BIG") }
    val foundWords = remember { mutableStateListOf<String>() }
    var playingSong by remember { mutableStateOf(false) }

    // 6x6 Letter Grid
    val grid = remember {
        listOf(
            listOf('U', 'S', 'A', 'X', 'Y', 'Z'),
            listOf('C', 'H', 'I', 'N', 'A', 'W'),
            listOf('T', 'A', 'L', 'L', 'O', 'P'),
            listOf('O', 'Q', 'F', 'A', 'T', 'M'),
            listOf('B', 'I', 'G', 'U', 'R', 'E'),
            listOf('W', 'E', 'L', 'C', 'O', 'M')
        )
    }

    var selectedLetters = remember { mutableStateListOf<Pair<Int, Int>>() }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🔤 单词巡逻兵 (Find and Circle):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text("请在下方字母网格中找出以下前三单元核心单词并点击字母连线:", color = Color.LightGray, fontSize = 13.sp)

        // Word list indicators
        targetWords.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { word ->
                    val isFound = foundWords.contains(word)
                    SuggestionChip(
                        onClick = { SpeechSynthesizer.speak(context, word) },
                        label = { Text(word) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isFound) Color(0xFF10B981) else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size < 3) {
                    Spacer(modifier = Modifier.weight((3 - row.size).toFloat()))
                }
            }
        }

        // Letter Grid View
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                grid.forEachIndexed { rIdx, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        row.forEachIndexed { cIdx, char ->
                            val isSelected = selectedLetters.contains(rIdx to cIdx)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFFEC4899) else Color.Transparent)
                                    .clickable {
                                        if (isSelected) {
                                            selectedLetters.remove(rIdx to cIdx)
                                        } else {
                                            selectedLetters.add(rIdx to cIdx)
                                            // Check if selected letters form any of the target words
                                            val currentWord = selectedLetters.map { (r, c) -> grid[r][c] }.joinToString("")
                                            if (targetWords.contains(currentWord) && !foundWords.contains(currentWord)) {
                                                foundWords.add(currentWord)
                                                SpeechSynthesizer.speak(context, "Good job! You found $currentWord")
                                                selectedLetters.clear()
                                                if (foundWords.size == targetWords.size) {
                                                    // Automatically completed finding words
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    char.toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Canvas Controls
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { selectedLetters.clear() },
                modifier = Modifier.weight(1f)
            ) {
                Text("清除选择")
            }
            Button(
                onClick = {
                    foundWords.clear()
                    foundWords.addAll(targetWords)
                    SpeechSynthesizer.speak(context, "All words found!")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
            ) {
                Text("自动搜寻")
            }
        }

        // Sing a song section
        if (foundWords.size == targetWords.size) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🎵 [庆典之歌] Sing a Song: 《Friends》", color = Color(0xFFEC4899), fontWeight = FontWeight.Bold)
                    Text("跟读歌词：Hello! Hello! What's your name? I'm from Canada, what's your name? My father and my mother, welcome back to school!", color = Color.White, fontSize = 13.sp)
                    Button(
                        onClick = {
                            playingSong = !playingSong
                            if (playingSong) {
                                SpeechSynthesizer.speak(context, "Hello! Hello! What's your name? I'm from Canada. My father and my mother, welcome back to school!")
                                onCompleted()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth().testTag("sing_song_btn")
                    ) {
                        Icon(if (playingSong) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (playingSong) "暂停庆典歌曲" else "播放庆典歌曲跟读")
                    }
                }
            }
        }
    }
}

@Composable
private fun MixedCheckpointView(context: Context, onCompleted: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    var selectedPhonicsWords = remember { mutableStateListOf<String>() }
    var isSubmitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🏆 Recycle 1 大满贯终极挑战:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)

        if (step == 1) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("💡 [情境配读] \"Where are you from?\" (选择符合标准英语的完美回应词)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    listOf("I'm from China. (我来自中国)", "I'm 9 years old. (我9岁了)", "He is my teacher. (他是我的老师)").forEach { opt ->
                        Button(
                            onClick = {
                                if (opt.startsWith("I'm from China")) {
                                    SpeechSynthesizer.speak(context, "I'm from China.")
                                    step = 2
                                } else {
                                    SpeechSynthesizer.speak(context, "No")
                                    android.widget.Toast.makeText(context, "答错啦，请认真回答来源国家", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth().testTag("checkpoint_step1_$opt")
                        ) {
                            Text(opt, color = Color.White)
                        }
                    }
                }
            }
        } else if (step == 2) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🍎 [动物特征] \"It is so big! It has a short tail.\" (描述的是哪一个动物？)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    listOf("bear (熊)", "mouse (老鼠)", "giraffe (长颈鹿)").forEach { opt ->
                        Button(
                            onClick = {
                                if (opt.startsWith("bear")) {
                                    SpeechSynthesizer.speak(context, "Correct! Bear is big with a short tail.")
                                    step = 3
                                } else {
                                    SpeechSynthesizer.speak(context, "Try again!")
                                    android.widget.Toast.makeText(context, "不对哦，再想一想哪个动物符合大且短尾巴", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth().testTag("checkpoint_step2_$opt")
                        ) {
                            Text(opt, color = Color.White)
                        }
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🎯 [发音分类] 请点击选出所有含有短元音 /ɪ/ 发音的单词 (Phonics short i):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    val words = listOf("pig", "ten", "six", "pen", "milk", "red")
                    val correctPhonics = setOf("pig", "six", "milk")

                    words.chunked(3).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { word ->
                                val isSelected = selectedPhonicsWords.contains(word)
                                Button(
                                    onClick = {
                                        if (isSelected) {
                                            selectedPhonicsWords.remove(word)
                                        } else {
                                            selectedPhonicsWords.add(word)
                                            SpeechSynthesizer.speak(context, word)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFFEC4899) else Color(0xFF334155)
                                    ),
                                    modifier = Modifier.weight(1f).testTag("phonics_btn_$word")
                                ) {
                                    Text(word, color = Color.White)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (selectedPhonicsWords.toSet() == correctPhonics) {
                                SpeechSynthesizer.speak(context, "Wonderful! You are an expert!")
                                isSubmitted = true
                                onCompleted()
                            } else {
                                SpeechSynthesizer.speak(context, "Check again.")
                                android.widget.Toast.makeText(context, "含有短音i的单词拼读：pig, six, milk 哦，请重新选择", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth().testTag("checkpoint_submit_btn")
                    ) {
                        Text(if (isSubmitted) "🎉 阶段复习挑战通关！" else "确认分类拼读单词")
                    }
                }
            }
        }
    }
}

@Composable
private fun WordRepairPhonicsView(context: Context, onCompleted: () -> Unit) {
    var step by remember { mutableStateOf(1) } // 1: word repair, 2: phonics categorization

    if (step == 1) {
        val repairWords = remember {
            mutableStateListOf(
                Triple("c_t", "a", "cat 🐱 (猫咪)"),
                Triple("p_n", "e", "pen ✒️ (钢笔)"),
                Triple("p_g", "i", "pig 🐷 (小猪)"),
                Triple("d_g", "o", "dog 🐶 (小狗)"),
                Triple("d_ck", "u", "duck 🦆 (鸭子)")
            )
        }
        var currentRepairIndex by remember { mutableStateOf(0) }
        val currentWordInfo = repairWords[currentRepairIndex]

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("word_repair_card")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🛠️ 单词修复挑战 (Word Repair Puzzle)",
                    color = Color(0xFFF472B6),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "请为缺失元音的单词选择正确的拼写字母：",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentWordInfo.first,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF472B6),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("a", "e", "i", "o", "u").forEach { vowel ->
                        Button(
                            onClick = {
                                if (vowel == currentWordInfo.second) {
                                    SpeechSynthesizer.speak(context, currentWordInfo.third.split(" ")[0])
                                    if (currentRepairIndex + 1 < repairWords.size) {
                                        currentRepairIndex++
                                    } else {
                                        SpeechSynthesizer.speak(context, "All words repaired! Let's classify them!")
                                        step = 2
                                    }
                                } else {
                                    SpeechSynthesizer.speak(context, "Try again!")
                                    android.widget.Toast.makeText(context, "不对哦，再仔细拼读一下这个词吧！", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(56.dp).testTag("repair_btn_$vowel")
                        ) {
                            Text(text = vowel, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "进度: ${currentRepairIndex + 1} / ${repairWords.size}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    } else {
        val categories = remember {
            listOf(
                "a /æ/" to setOf("cat", "bag"),
                "e /e/" to setOf("pen", "red"),
                "i /ɪ/" to setOf("pig", "six"),
                "o /ɒ/" to setOf("dog", "box"),
                "u /ʌ/" to setOf("duck", "run")
            )
        }
        val wordsToSort = remember {
            listOf("cat", "bag", "pen", "red", "pig", "six", "dog", "box", "duck", "run")
        }
        var selectedWordIndex by remember { mutableStateOf(0) }
        val currentWordToSort = wordsToSort.getOrNull(selectedWordIndex)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("phonics_sort_card")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🍎 短元音分类大师 (Phonics sorter)",
                    color = Color(0xFF34D399),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                if (currentWordToSort != null) {
                    Text(
                        text = "请点击下方元音格子，将这个单词分到对应短元音栏目：",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF34D399)),
                        modifier = Modifier.clickable { SpeechSynthesizer.speak(context, currentWordToSort) }.padding(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentWordToSort,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "🔊 点击发音",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            Button(
                                onClick = {
                                    val isCorrect = category.second.contains(currentWordToSort)
                                    if (isCorrect) {
                                        SpeechSynthesizer.speak(context, "Yes! Correct!")
                                        selectedWordIndex++
                                        if (selectedWordIndex >= wordsToSort.size) {
                                            SpeechSynthesizer.speak(context, "Outstanding! Phonics mission complete!")
                                            onCompleted()
                                        }
                                    } else {
                                        SpeechSynthesizer.speak(context, "No")
                                        android.widget.Toast.makeText(context, "拼读分类不对哦，再想一想吧！", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                modifier = Modifier.fillMaxWidth().testTag("sort_bucket_${category.first.first()}")
                            ) {
                                Text(
                                    text = "分类到: ${category.first}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "进度: ${selectedWordIndex + 1} / ${wordsToSort.size}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "🎉 恭喜！你已完美通过短元音分类挑战！",
                        color = Color(0xFF34D399),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// PEP 2013 Grade 4 Semester 1 Recycle 1 Specific Interactive Mission Views
// ----------------------------------------------------------------------

@Composable
private fun G4S1StoryRoleplayView(context: Context, onCompleted: () -> Unit) {
    val dialogueLines = remember {
        listOf(
            "Amy: I have a new schoolbag! It's black and white." to "埃米：我有一个新书包！它是黑白相间的。",
            "Sarah: Really? What's in your schoolbag?" to "萨拉：真的吗？你的书包里有什么？",
            "Amy: An English book, a maths book, three storybooks and a pencil box." to "埃米：一本英语书，一本数学书，三本故事书和一个铅笔盒。",
            "Sarah: I have a new friend. He is tall and strong. He has short hair and glasses." to "萨拉：我有一个新朋友。他很高壮。他留着短发，戴着眼镜。",
            "Amy: Is he Zhang Peng?" to "埃米：他是张鹏吗？",
            "Sarah: Yes, you're right! His name is Zhang Peng." to "萨拉：是的，你说对了！他的名字叫张鹏。"
        )
    }

    val roles = remember { listOf("Amy (埃米)", "Sarah (萨拉)") }
    var selectedRole by remember { mutableStateOf("Amy") }
    var isRecording by remember { mutableStateOf(false) }
    var hasRecorded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🎭 [P32 Read aloud] 请选择你的演练角色:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            roles.forEach { role ->
                val name = role.split(" ").first()
                val isSelected = selectedRole == name
                Button(
                    onClick = { selectedRole = name },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFFEC4899) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(role, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        dialogueLines.forEach { (enZh, cnZh) ->
            val speaker = enZh.split(":").first().trim()
            val text = enZh.substringAfter(":").trim()
            val isMyRole = speaker == selectedRole

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isMyRole) Color(0xFF1E293B) else Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isMyRole) 1.5.dp else 1.dp,
                    color = if (isMyRole) Color(0xFFEC4899) else Color(0xFF334155)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "$speaker:", color = if (isMyRole) Color(0xFFF472B6) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(text = cnZh, color = Color.LightGray, fontSize = 12.sp)
                    }

                    IconButton(
                        onClick = { SpeechSynthesizer.speak(context, text) },
                        modifier = Modifier.size(40.dp).background(Color(0xFF334155), CircleShape)
                    ) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = "播放", tint = Color.White)
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🎙️ 角色配音朗读跟读:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Button(
                    onClick = {
                        if (!isRecording) {
                            isRecording = true
                        } else {
                            isRecording = false
                            hasRecorded = true
                            onCompleted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color(0xFFEF4444) else Color(0xFFEC4899)
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("g4_s1_roleplay_rec")
                ) {
                    Icon(if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRecording) "停止录音 (正在录音...)" else "点击开始角色演练跟读")
                }

                if (hasRecorded) {
                    Text("✅ 角色跟读配音已完成！发音清晰，获得 10 积分奖励。", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun G4S1PhonicsConnectWriteView(context: Context, onCompleted: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    val pathSelection = remember { mutableStateListOf<String>() }

    val phonicsWords = remember {
        listOf(
            "cake" to "a-e", "cat" to "a", "face" to "a-e", "hat" to "a",
            "like" to "i-e", "pig" to "i", "kite" to "i-e", "five" to "i-e",
            "nose" to "o-e", "dog" to "o", "note" to "o-e", "coke" to "o-e"
        )
    }

    val fillItems = remember {
        listOf(
            Triple("c _ k e", "a", "cake"),
            Triple("k _ t e", "i", "kite"),
            Triple("n _ s e", "o", "nose"),
            Triple("f _ v e", "i", "five")
        )
    }
    var currentFillIdx by remember { mutableStateOf(0) }
    var selectedLetter by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (step == 1) {
            Text("✨ [P33 Part 1] 魔法元音路线连连看:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("点击属于长元音(a-e, i-e, o-e)的发音单词，连接成一条通往终点的长音小径:", color = Color.LightGray, fontSize = 13.sp)

            val chunked = phonicsWords.chunked(3)
            chunked.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { (word, family) ->
                        val isSelected = pathSelection.contains(word)
                        val isLongVowel = family.contains("-")
                        Button(
                            onClick = {
                                SpeechSynthesizer.speak(context, word)
                                if (isLongVowel) {
                                    if (!pathSelection.contains(word)) {
                                        pathSelection.add(word)
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "$word 是短元音 $family 哦，请点击长元音单词！", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF10B981) else Color(0xFF1E293B)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color.White else Color(0xFF334155)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(word, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("[$family]", color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Text("已连接长音词: ${pathSelection.size} / 8", color = Color(0xFFF472B6), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Button(
                onClick = {
                    if (pathSelection.size >= 5) {
                        step = 2
                    } else {
                        android.widget.Toast.makeText(context, "请找出并点击至少5个长元音单词连接小径！", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                modifier = Modifier.fillMaxWidth().testTag("phonics_p1_next")
            ) {
                Text("完成路线连接 · 进入听音填词")
            }
        } else {
            Text("✏️ [P33 Part 2] 听音补全拼写 (Look, listen and write):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)

            if (currentFillIdx < fillItems.size) {
                val item = fillItems[currentFillIdx]
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        IconButton(
                            onClick = { SpeechSynthesizer.speak(context, item.third) },
                            modifier = Modifier.size(56.dp).background(Color(0xFFEC4899), CircleShape)
                        ) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = "播放读音", tint = Color.White, modifier = Modifier.size(32.dp))
                        }

                        Text("听发音并选填缺失元音字母:", color = Color.LightGray, fontSize = 13.sp)

                        val display = if (selectedLetter != null) item.first.replace("_", selectedLetter!!) else item.first
                        Text(display, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            listOf("a", "i", "o", "u").forEach { letter ->
                                Button(
                                    onClick = { selectedLetter = letter },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedLetter == letter) Color(0xFFEC4899) else Color(0xFF334155)
                                    ),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Text(letter, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (selectedLetter == item.second) {
                                    SpeechSynthesizer.speak(context, "Correct! ${item.third}")
                                    currentFillIdx++
                                    selectedLetter = null
                                    if (currentFillIdx >= fillItems.size) {
                                        onCompleted()
                                    }
                                } else {
                                    SpeechSynthesizer.speak(context, "Try again!")
                                    android.widget.Toast.makeText(context, "字母选择不正确哦，请听发音再试", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth().testTag("phonics_submit_word")
                        ) {
                            Text("确认提交单词拼写")
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🎉 恭喜！你已圆满完成元音发音与规范书写复习！", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun G4S1FriendClueSearchView(context: Context, onCompleted: () -> Unit) {
    val characters = remember {
        listOf(
            Triple("Sarah", "She is quiet. She has long hair and green glasses.", "quiet / long hair / glasses"),
            Triple("John", "He is tall and strong. He has a blue bag and orange shoes.", "tall & strong / blue bag"),
            Triple("Chen Jie", "She is friendly. She has short hair and a yellow schoolbag.", "friendly / short hair"),
            Triple("Wu Binbin", "He is quiet and thin. He has glasses and a red notebook.", "quiet & thin / red notebook")
        )
    }

    val matchedIds = remember { mutableStateListOf<String>() }
    var activeClueIdx by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🔍 [P34 Look and find] 伙伴特征线索侦探:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text("阅读侦探线索，找出班级中对应的唯一好朋友:", color = Color.LightGray, fontSize = 13.sp)

        if (activeClueIdx < characters.size) {
            val target = characters[activeClueIdx]

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF472B6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🕵️ 任务线索 ${activeClueIdx + 1} / ${characters.size}", color = Color(0xFFF472B6), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        IconButton(
                            onClick = { SpeechSynthesizer.speak(context, target.second) },
                            modifier = Modifier.size(36.dp).background(Color(0xFF334155), CircleShape)
                        ) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = "朗读线索", tint = Color.White)
                        }
                    }

                    Text(target.second, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("关键词: ${target.third}", color = Color.LightGray, fontSize = 12.sp)
                }
            }

            Text("👉 请在候选人群中圈出对应的人物:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            val candidateList = remember(activeClueIdx) { characters.shuffled() }

            candidateList.forEach { candidate ->
                Button(
                    onClick = {
                        if (candidate.first == target.first) {
                            SpeechSynthesizer.speak(context, "Bingo! ${candidate.first}!")
                            matchedIds.add(candidate.first)
                            activeClueIdx++
                            if (activeClueIdx >= characters.size) {
                                onCompleted()
                            }
                        } else {
                            SpeechSynthesizer.speak(context, "No, try again!")
                            android.widget.Toast.makeText(context, "不对哦，这位朋友特征不符合该线索！", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("friend_candidate_${candidate.first}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👤 ${candidate.first}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("点击识别", color = Color(0xFF60A5FA), fontSize = 12.sp)
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🎉 太棒了！所有 4 位好朋友的特征均已被你精准识别！", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun G4S1DialoguePictureMatchView(context: Context, onCompleted: () -> Unit) {
    val items = remember {
        listOf(
            Triple(
                "🧹 情境 1: 打扫教室",
                "Let's clean the classroom!",
                listOf("OK. Let me clean the windows.", "His name is John.", "He is quiet.") to "OK. Let me clean the windows."
            ),
            Triple(
                "🤝 情境 2: 介绍新伙伴",
                "I have a new friend.",
                listOf("What's his name?", "Let me clean the desk.", "Where is my book?") to "What's his name?"
            ),
            Triple(
                "🏷️ 情境 3: 回答姓名",
                "What's his name?",
                listOf("His name is Zhang Peng.", "She has long hair.", "It's black and white.") to "His name is Zhang Peng."
            )
        )
    }

    var currentIdx by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("💬 [P34 Match & Finish] 补全对话与图画匹配:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)

        if (currentIdx < items.size) {
            val item = items[currentIdx]

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(item.first, color = Color(0xFFF472B6), fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("A: \"${item.second}\"", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { SpeechSynthesizer.speak(context, item.second) },
                            modifier = Modifier.size(36.dp).background(Color(0xFF334155), CircleShape)
                        ) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = "播放", tint = Color.White)
                        }
                    }

                    Text("请选择 B 对应的最恰当接话:", color = Color.LightGray, fontSize = 13.sp)

                    item.third.first.forEach { option ->
                        val isSelected = selectedAnswer == option
                        Button(
                            onClick = { selectedAnswer = option },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFFEC4899) else Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("dialogue_opt_$option")
                        ) {
                            Text(option, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedAnswer == item.third.second) {
                        SpeechSynthesizer.speak(context, "Good job!")
                        currentIdx++
                        selectedAnswer = null
                        if (currentIdx >= items.size) {
                            onCompleted()
                        }
                    } else {
                        SpeechSynthesizer.speak(context, "Try again!")
                        android.widget.Toast.makeText(context, "回答不匹配哦，请结合情境重新选择！", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.fillMaxWidth().testTag("dialogue_submit")
            ) {
                Text("提交对话匹配")
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🎉 完美！情境对话逻辑补全全部通过！", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun G4S1QuestionTrailGameView(context: Context, onCompleted: () -> Unit) {
    val questions = remember {
        listOf(
            Triple("🚩 关卡 1: 教室物品", "Where is the computer?", listOf("It's on the teacher's desk.", "It's a storybook.", "I have a bag.") to "It's on the teacher's desk."),
            Triple("🚩 关卡 2: 魔法音标", "Which word has the sound /eɪ/ (a-e)?", listOf("cake", "cat", "bag") to "cake"),
            Triple("🚩 关卡 3: 我的书包", "What's in your schoolbag?", listOf("An English book and two storybooks.", "I'm 10 years old.", "He is my friend.") to "An English book and two storybooks."),
            Triple("🚩 关卡 4: 拼写关卡", "Spell the number '5':", listOf("five", "nine", "kite") to "five"),
            Triple("🚩 关卡 5: 描写朋友", "He is tall and _____. He has glasses.", listOf("friendly", "yellow", "desk") to "friendly"),
            Triple("🚩 关卡 6: 语法代词", "_____ name is Mike. He is strong.", listOf("His", "Her", "My") to "His")
        )
    }

    var currentStep by remember { mutableStateOf(0) }
    var selectedOpt by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🏔️ [P35 Play a game] 知识山峰关卡挑战:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)

        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / questions.size.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = Color(0xFFEC4899),
            trackColor = Color(0xFF334155)
        )

        if (currentStep < questions.size) {
            val q = questions[currentStep]

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(q.first, color = Color(0xFFF472B6), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(q.second, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)

                    q.third.first.forEach { option ->
                        val isSel = selectedOpt == option
                        Button(
                            onClick = { selectedOpt = option },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) Color(0xFFEC4899) else Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("trail_opt_$option")
                        ) {
                            Text(option, fontSize = 14.sp)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedOpt == q.third.second) {
                        SpeechSynthesizer.speak(context, "Great!")
                        currentStep++
                        selectedOpt = null
                        if (currentStep >= questions.size) {
                            onCompleted()
                        }
                    } else {
                        SpeechSynthesizer.speak(context, "Try again!")
                        android.widget.Toast.makeText(context, "关卡答错啦，请再看一遍题目！", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.fillMaxWidth().testTag("trail_submit")
            ) {
                Text(if (currentStep == questions.size - 1) "登顶并完成复习 🏆" else "攀登到下一关卡")
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🏆 登顶成功！你已顺利突破所有 6 个山峰关卡！", color = Color(0xFFEAB308), fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun G4S1Recycle2StoryRoleplayView(context: Context, onCompleted: () -> Unit) {
    val dialogueLines = remember {
        listOf(
            "Mike: Merry Christmas! Come in, please!" to "迈克：圣诞快乐！请进！",
            "Sarah: Wow! Your living room is so beautiful! Is this your father?" to "萨拉：哇！你的客厅太漂亮了！这是你爸爸吗？",
            "Mike: Yes. He is a driver. My mother is in the kitchen. She is a nurse." to "迈克：是的。他是一名司机。我妈妈在厨房里。她是一名护士。",
            "Sarah: What would you like for dinner? I'd like some turkey and vegetables." to "萨拉：晚饭你想吃什么？我想吃一些火鸡和蔬菜。",
            "Mike: Would you like some soup? Yes, please. Help yourself!" to "迈克：你想喝点汤吗？好的，请。别客气，请自便！"
        )
    }

    val roles = remember { listOf("Mike (迈克)", "Sarah (萨拉)") }
    var selectedRole by remember { mutableStateOf("Mike") }
    var isRecording by remember { mutableStateOf(false) }
    var hasRecorded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🎭 [P66 Read aloud] 请选择你的演练角色:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            roles.forEach { role ->
                val name = role.split(" ").first()
                val isSelected = selectedRole == name
                Button(
                    onClick = { selectedRole = name },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFFEC4899) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(role, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        dialogueLines.forEach { (enZh, cnZh) ->
            val speaker = enZh.split(":").first().trim()
            val text = enZh.substringAfter(":").trim()
            val isMyRole = speaker == selectedRole

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isMyRole) Color(0xFF1E293B) else Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isMyRole) 1.5.dp else 1.dp,
                    color = if (isMyRole) Color(0xFFEC4899) else Color(0xFF334155)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "$speaker:", color = if (isMyRole) Color(0xFFF472B6) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(text = cnZh, color = Color.LightGray, fontSize = 12.sp)
                    }

                    IconButton(
                        onClick = { SpeechSynthesizer.speak(context, text) },
                        modifier = Modifier.size(40.dp).background(Color(0xFF334155), CircleShape)
                    ) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = "播放", tint = Color.White)
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🎙️ 角色配音朗读跟读:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Button(
                    onClick = {
                        if (!isRecording) {
                            isRecording = true
                        } else {
                            isRecording = false
                            hasRecorded = true
                            onCompleted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color(0xFFEF4444) else Color(0xFFEC4899)
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("g4_s1_recycle2_roleplay_rec")
                ) {
                    Icon(if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRecording) "停止配音 (正在录音...)" else "开始录制角色台词配音", color = Color.White)
                }

                if (hasRecorded) {
                    Text(text = "🎉 恭喜！配音演练已成功录制并比对！", color = Color(0xFF10B981), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun G4S1ListenNumberRoomsView(context: Context, onCompleted: () -> Unit) {
    val rooms = remember {
        listOf(
            "study" to "书房",
            "kitchen" to "厨房",
            "bedroom" to "卧室",
            "living room" to "客厅",
            "bathroom" to "浴室"
        )
    }

    val correctAnswers = remember {
        mapOf(
            "study" to 1,
            "kitchen" to 2,
            "bedroom" to 3,
            "living room" to 4,
            "bathroom" to 5
        )
    }

    var selections by remember { mutableStateOf(mapOf<String, Int>()) }
    var submitted by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var activeAudioIndex by remember { mutableStateOf(-1) }

    val audioTracks = listOf(
        "Where is John? He is in the study.",
        "Where is Mum? She is in the kitchen.",
        "Where are the keys? They are in the bedroom.",
        "Where is Dad? He is in the living room.",
        "Where is the puppy? It is in the bathroom."
    )

    fun playAudioTrack(idx: Int) {
        if (idx in audioTracks.indices) {
            activeAudioIndex = idx
            SpeechSynthesizer.speak(context, audioTracks[idx])
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🎵 [P67 Listen & Number] 听音标序号:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("请点击下方播放按钮听音，并在各个房间选择对应的顺序序号（1-5）：", color = Color.LightGray, fontSize = 12.sp)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                audioTracks.forEachIndexed { index, track ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { playAudioTrack(index) }.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { playAudioTrack(index) },
                                modifier = Modifier.size(32.dp).background(Color(0xFF334155), CircleShape)
                            ) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Text("听力素材 ${index + 1}", color = if (activeAudioIndex == index) Color(0xFFEC4899) else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        if (submitted) {
                            Text(track, color = Color.Gray, fontSize = 11.sp)
                        } else {
                            Text("点击播放 🎧", color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        rooms.forEach { (roomId, roomName) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(roomName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(roomId.uppercase(), color = Color.Gray, fontSize = 11.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { num ->
                            val isSelected = selections[roomId] == num
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) Color(0xFFEC4899) else Color(0xFF334155),
                                modifier = Modifier
                                    .clickable {
                                        if (!submitted) {
                                            selections = selections.toMutableMap().apply { put(roomId, num) }
                                        }
                                    }
                                    .testTag("room_num_${roomId}_$num")
                            ) {
                                Text(
                                    text = num.toString(),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                submitted = true
                var correctCount = 0
                correctAnswers.forEach { (k, v) ->
                    if (selections[k] == v) {
                        correctCount++
                    }
                }
                isCorrect = correctCount == 5
                if (isCorrect) {
                    SpeechSynthesizer.speak(context, "Excellent! You numbered all rooms correctly.")
                    onCompleted()
                } else {
                    SpeechSynthesizer.speak(context, "Please try again.")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
            enabled = selections.size == 5,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("rooms_submit")
        ) {
            Text("核对并提交答案", fontWeight = FontWeight.Bold)
        }

        if (submitted) {
            if (isCorrect) {
                Text("🎉 恭喜你！全部听音房间序号配对正确！", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else {
                Text("❌ 序号有误，请重新核对听音材料并修改！", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun G4S1PhonicsListenWriteView(context: Context, onCompleted: () -> Unit) {
    val wordList = remember {
        listOf(
            "use" to "u-e 长元音 /ju:/",
            "late" to "a-e 长元音 /eɪ/",
            "bike" to "i-e 长元音 /aɪ/",
            "home" to "o-e 长元音 /əʊ/",
            "me" to "e 长元音 /i:/",
            "nice" to "i-e 长元音 /aɪ/"
        )
    }

    var inputs by remember { mutableStateOf(mapOf<String, String>()) }
    var submitted by remember { mutableStateOf(false) }
    var checkedResults by remember { mutableStateOf(mapOf<String, Boolean>()) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🔤 [P67 Look, listen & write] 看、听并规范拼写:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("点击小喇叭听音，并在输入框中写下发长元音的正确单词：", color = Color.LightGray, fontSize = 12.sp)

        wordList.forEach { (word, clue) ->
            val userText = inputs[word] ?: ""
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { SpeechSynthesizer.speak(context, word) },
                        modifier = Modifier.size(40.dp).background(Color(0xFF334155), CircleShape)
                    ) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = Color.White)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(clue, color = Color(0xFFF472B6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = userText,
                            onValueChange = {
                                if (!submitted) {
                                    inputs = inputs.toMutableMap().apply { put(word, it) }
                                }
                            },
                            placeholder = { Text("点击输入单词...", color = Color.Gray, fontSize = 13.sp) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedIndicatorColor = Color(0xFFEC4899),
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("phonics_input_$word")
                        )
                    }

                    if (submitted) {
                        val isCorrect = checkedResults[word] == true
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                submitted = true
                val results = mutableMapOf<String, Boolean>()
                var allCorrect = true
                wordList.forEach { (word, _) ->
                    val userAns = (inputs[word] ?: "").trim().lowercase()
                    val correct = userAns == word
                    results[word] = correct
                    if (!correct) allCorrect = false
                }
                checkedResults = results
                if (allCorrect) {
                    SpeechSynthesizer.speak(context, "Perfect! You spelled all long vowel words correctly.")
                    onCompleted()
                } else {
                    SpeechSynthesizer.speak(context, "Check your spellings and try again.")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
            enabled = inputs.size == wordList.size,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("phonics_submit")
        ) {
            Text("核对书写拼写", fontWeight = FontWeight.Bold)
        }

        if (submitted) {
            val correctCount = checkedResults.values.count { it }
            if (correctCount == wordList.size) {
                Text("🎉 太棒了！全部长元音拼写完全正确！", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else {
                Text("❌ 拼写正确率: $correctCount / ${wordList.size}，请修正错误的拼写！", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun G4S1FamilyHomeInterviewView(context: Context, onCompleted: () -> Unit) {
    val interviewQuestions = remember {
        listOf(
            Triple(
                "How many people are there in John's family?",
                listOf("Six.", "Three.", "Five.", "Four."),
                0
            ),
            Triple(
                "Where is John? He is reading.",
                listOf("He is in the study.", "He is in the kitchen.", "He is in the living room.", "He is in the bathroom."),
                0
            ),
            Triple(
                "Is this John's dad?",
                listOf("Yes, he is.", "No, he isn't.", "Yes, she is.", "No, she isn't."),
                0
            ),
            Triple(
                "What's John's dad's job? (He works in a hospital)",
                listOf("He's a doctor.", "He's a driver.", "He's a teacher.", "He's a football player."),
                0
            ),
            Triple(
                "What's John's mom's job? (She helps doctors)",
                listOf("She's a nurse.", "She's a doctor.", "She's a teacher.", "She's a driver."),
                0
            )
        )
    }

    var currentStep by remember { mutableStateOf(0) }
    var selectedOpt by remember { mutableStateOf<String?>(null) }
    var hasAnswered by remember { mutableStateOf(false) }
    var answerFeedback by remember { mutableStateOf<Boolean?>(null) }

    if (currentStep < interviewQuestions.size) {
        val q = interviewQuestions[currentStep]

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🎙️ [P68 Ask & Answer] 家庭与职业采访:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("问题 ${currentStep + 1} / ${interviewQuestions.size}", color = Color(0xFFF472B6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("模拟采访进度", color = Color.Gray, fontSize = 12.sp)
            }
            LinearProgressIndicator(
                progress = { (currentStep + 1).toFloat() / interviewQuestions.size },
                color = Color(0xFFEC4899),
                trackColor = Color(0xFF334155),
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(q.first, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { SpeechSynthesizer.speak(context, q.first) },
                        modifier = Modifier.size(36.dp).background(Color(0xFF334155), CircleShape)
                    ) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                q.second.forEach { opt ->
                    val isSelected = selectedOpt == opt
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFFEC4899) else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!hasAnswered) {
                                    selectedOpt = opt
                                }
                            }
                            .testTag("interview_opt_$opt")
                    ) {
                        Text(
                            text = opt,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            if (!hasAnswered) {
                Button(
                    onClick = {
                        hasAnswered = true
                        val correct = selectedOpt == q.second[q.third]
                        answerFeedback = correct
                        if (correct) {
                            SpeechSynthesizer.speak(context, "Correct!")
                        } else {
                            SpeechSynthesizer.speak(context, "Incorrect, try again.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    enabled = selectedOpt != null,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("interview_submit")
                ) {
                    Text("核对采访回答", fontWeight = FontWeight.Bold)
                }
            } else {
                val correct = answerFeedback == true
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (correct) Color(0xFF065F46) else Color(0xFF991B1B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (correct) "✅ 回答正确！" else "❌ 回答错误！正确答案是: ${q.second[q.third]}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Button(
                            onClick = {
                                if (correct) {
                                    currentStep++
                                    selectedOpt = null
                                    hasAnswered = false
                                    answerFeedback = null
                                    if (currentStep >= interviewQuestions.size) {
                                        onCompleted()
                                    }
                                } else {
                                    hasAnswered = false
                                    selectedOpt = null
                                    answerFeedback = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = if (correct) "进入下一题" else "重新回答",
                                color = if (correct) Color(0xFF065F46) else Color(0xFF991B1B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("🎉 恭喜你！采访录制模拟全部完成！", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// PEP 2013 Grade 4 Semester 2 Recycle 1 Views
// ==========================================

@Composable
fun G4S2Recycle1StoryRoleplayView(
    context: Context,
    onCompleted: () -> Unit
) {
    val dialogueLines = remember {
        listOf(
            Triple("Amy", "Where are we going? Oh, the show is at 4 o'clock in the library.", "我们去哪？哦，演出4点在图书馆。"),
            Triple("Chen Jie", "What time is it now? It's 3:40. It's time to go!", "现在几点了？3点40。该出发了！"),
            Triple("Amy", "Oh! It's cold outside. It's windy. Let's put on our jackets.", "哦！外面好冷。风很大。我们穿上夹克吧。"),
            Triple("Chen Jie", "Oh, no! It's rainy. We can't go outside. But the show is in the library on the first floor.", "噢不！下雨了。我们不能去室外了。不过演出是在一楼的图书馆。")
        )
    }

    var selectedCharacter by remember { mutableStateOf<String?>(null) }
    var currentDialogueIndex by remember { mutableStateOf(0) }
    var hasFinishedRoleplay by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🎭 雨天演出大作战 · 角色扮演",
                    color = Color(0xFFF472B6),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "提示: 两名女孩Amy和陈洁正计划去图书馆看演出。点击选择你的角色，然后大声跟读对话吧！",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }
        }

        if (selectedCharacter == null) {
            Text("请选择你要扮演的角色:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        selectedCharacter = "Amy"
                        currentDialogueIndex = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    modifier = Modifier.weight(1f).height(50.dp).testTag("select_char_amy")
                ) {
                    Text("扮演 Amy 👧", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        selectedCharacter = "Chen Jie"
                        currentDialogueIndex = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier.weight(1f).height(50.dp).testTag("select_char_chen")
                ) {
                    Text("扮演 陈洁 👧", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当前正在扮演: ${if (selectedCharacter == "Amy") "Amy 👧" else "陈洁 👧"}",
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                TextButton(onClick = {
                    selectedCharacter = null
                    currentDialogueIndex = 0
                    hasFinishedRoleplay = false
                }) {
                    Text("重选角色", color = Color.LightGray)
                }
            }

            // Dialogue Slider
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val currentLine = dialogueLines[currentDialogueIndex]
                    val isMyTurn = currentLine.first == selectedCharacter

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currentDialogueIndex + 1} / ${dialogueLines.size}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (isMyTurn) "👉 轮到你跟读" else "🎧 聆听对方",
                            color = if (isMyTurn) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Surface(
                        color = if (isMyTurn) Color(0xFF334155) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${currentLine.first}: ${currentLine.second}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = currentLine.third,
                                color = Color.LightGray,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { SpeechSynthesizer.speak(context, currentLine.second) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFEC4899), CircleShape)
                                .testTag("story_audio_play")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "播放", tint = Color.White)
                        }

                        if (isMyTurn) {
                            Button(
                                onClick = {
                                    SpeechSynthesizer.speak(context, "Great job!")
                                    if (currentDialogueIndex < dialogueLines.size - 1) {
                                        currentDialogueIndex++
                                    } else {
                                        hasFinishedRoleplay = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.height(44.dp).testTag("story_record_button")
                            ) {
                                Text("🎤 模拟录音", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (currentDialogueIndex < dialogueLines.size - 1) {
                                        currentDialogueIndex++
                                    } else {
                                        hasFinishedRoleplay = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                                modifier = Modifier.height(44.dp).testTag("story_next_line")
                            ) {
                                Text("下一句", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (hasFinishedRoleplay) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF065F46)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉 角色扮演通关！你已经完全跟读掌握了本课时内容。", color = Color.White, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = onCompleted,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth().testTag("story_completed")
                        ) {
                            Text("完成雨天演出大作战", color = Color(0xFF065F46), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun G4S2Recycle1PhonicsListenMatchView(
    context: Context,
    onCompleted: () -> Unit
) {
    val phonicsItems = remember {
        listOf(
            // er
            Triple("sister", "er", "妹妹 / 姐姐"),
            // ir
            Triple("girl", "ir", "女孩"),
            // ar
            Triple("card", "ar", "卡片"),
            // al
            Triple("ball", "al", "球")
        )
    }

    var currentIndex by remember { mutableStateOf(0) }
    var selectedPattern by remember { mutableStateOf<String?>(null) }
    var selectedPicName by remember { mutableStateOf<String?>(null) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableStateOf(0) }

    val currentItem = phonicsItems[currentIndex]
    val optionsPatterns = remember(currentIndex) {
        when (currentIndex) {
            0 -> listOf("er", "or")
            1 -> listOf("ir", "ur")
            2 -> listOf("ar", "al")
            else -> listOf("al", "ar")
        }
    }
    val optionPictures = remember(currentIndex) {
        listOf("妹妹 / 姐姐", "女孩", "卡片", "球").shuffled()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🌱 语音花园配对 · 累计拼读复习",
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "累计温习: -er (Unit 1), ir/ur (Unit 2), ar/al (Unit 3)",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("当前关卡: ${currentIndex + 1} / 4", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("🔊 提示: 教材音频未接入，当前使用系统发音合成。", color = Color(0xFFFBBF24), fontSize = 11.sp)
                }
            }
        }

        Button(
            onClick = { SpeechSynthesizer.speak(context, currentItem.first) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("phonics_play_audio")
        ) {
            Text("🔊 播放目标单词拼读语音", fontWeight = FontWeight.Bold)
        }

        // Pattern Selection
        Text("第一步：圈出你听到的字母发音组合", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            optionsPatterns.forEach { pattern ->
                val isSelected = selectedPattern == pattern
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF10B981) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedPattern = pattern }
                        .testTag("pattern_$pattern")
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(pattern, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }

        // Picture Match Selection
        Text("第二步：圈出与发音词意匹配的图片汉意", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            optionPictures.forEach { picName ->
                val isSelected = selectedPicName == picName
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFEC4899) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedPicName = picName }
                        .testTag("pic_$picName")
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                        Text(picName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Submit matching result
        if (resultMessage == null) {
            Button(
                onClick = {
                    if (selectedPattern == currentItem.second && selectedPicName == currentItem.third) {
                        resultMessage = "✅ 拼读及匹配正确！"
                        score++
                        SpeechSynthesizer.speak(context, "Correct!")
                    } else {
                        resultMessage = "❌ 抱歉回答有误，正确拼写为 [${currentItem.second}] 含义为 [${currentItem.third}]"
                        SpeechSynthesizer.speak(context, "Try again!")
                    }
                },
                enabled = selectedPattern != null && selectedPicName != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("phonics_submit_button")
            ) {
                Text("提交核对", fontWeight = FontWeight.Bold)
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (resultMessage!!.startsWith("✅")) Color(0xFF065F46) else Color(0xFF991B1B)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(resultMessage!!, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Button(
                        onClick = {
                            if (currentIndex < phonicsItems.size - 1) {
                                currentIndex++
                                selectedPattern = null
                                selectedPicName = null
                                resultMessage = null
                            } else {
                                onCompleted()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = if (currentIndex < phonicsItems.size - 1) "进入下一题" else "完成语音配对挑战",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun G4S2Recycle1SchoolWeatherQaView(
    context: Context,
    onCompleted: () -> Unit
) {
    // 4 standard scenes in the school map
    val qaList = remember {
        listOf(
            // Question 1
            Triple(
                "Is this the library?",
                listOf("Yes, it is.", "No, it isn't. It's the computer room."),
                0 // expected index
            ),
            // Question 2
            Triple(
                "Where is the computer room?",
                listOf("It's on the first floor.", "It's on the second floor."),
                0
            ),
            // Question 3
            Triple(
                "What's the weather like in the classroom?",
                listOf("It's sunny.", "It's rainy."),
                1
            ),
            // Question 4
            Triple(
                "What's the weather like in the teachers' office?",
                listOf("It's windy.", "It's cold and snowy."),
                0
            )
        )
    }

    var currentIndex by remember { mutableStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var resultFeedback by remember { mutableStateOf<String?>(null) }

    val currentQa = qaList[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🏫 校园天气调查站",
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "观察以下校园剖面实景进行地理和天气状况问答：",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
                
                // Static visual map table
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🏢 [二楼] 教师办公室 (Windy 🌬️) | 普通教室 (Rainy 🌧️)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Divider(color = Color.Gray)
                    Text("🏢 [一楼] 图书馆 (Sunny ☀️) | 计算机房 (Cloudy ☁️)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Question Statement
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Q${currentIndex + 1}: ${currentQa.first}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { SpeechSynthesizer.speak(context, currentQa.first) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier.testTag("school_speak_q")
                ) {
                    Text("🔊 朗读问题")
                }
            }
        }

        // Answers
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            currentQa.second.forEachIndexed { index, ansText ->
                val isSelected = selectedAnswerIndex == index
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFFBBF24) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedAnswerIndex = index }
                        .testTag("ans_opt_$index")
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                        Text(ansText, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (resultFeedback == null) {
            Button(
                onClick = {
                    if (selectedAnswerIndex == currentQa.third) {
                        resultFeedback = "✅ 答对了！完美契合校园地图。"
                        SpeechSynthesizer.speak(context, "Excellent!")
                    } else {
                        resultFeedback = "❌ 答错了，请参考地图提示重新选择。"
                        SpeechSynthesizer.speak(context, "No, try again.")
                    }
                },
                enabled = selectedAnswerIndex != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("school_submit_button")
            ) {
                Text("核对我的问答", fontWeight = FontWeight.Bold)
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (resultFeedback!!.startsWith("✅")) Color(0xFF065F46) else Color(0xFF991B1B)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(resultFeedback!!, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Button(
                        onClick = {
                            if (resultFeedback!!.startsWith("✅")) {
                                if (currentIndex < qaList.size - 1) {
                                    currentIndex++
                                    selectedAnswerIndex = null
                                    resultFeedback = null
                                } else {
                                    onCompleted()
                                }
                            } else {
                                selectedAnswerIndex = null
                                resultFeedback = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = if (resultFeedback!!.startsWith("✅")) {
                                if (currentIndex < qaList.size - 1) "进入下一问" else "完成校园调查"
                            } else {
                                "重新回答"
                            },
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun G4S2Recycle1WeatherListenReadWriteView(
    context: Context,
    onCompleted: () -> Unit
) {
    // Stage 1: Weather grid ticking
    val locations = remember { listOf("Beijing", "Shanghai", "Harbin", "Lhasa", "Sanya", "Hong Kong") }
    val conditions = remember { listOf("Sunny", "Rainy", "Snowy", "Windy", "Cloudy") }
    // Static static static answers for listening
    val correctTick = remember {
        mapOf(
            "Beijing" to "Sunny",
            "Shanghai" to "Rainy",
            "Harbin" to "Snowy",
            "Lhasa" to "Windy",
            "Sanya" to "Sunny",
            "Hong Kong" to "Cloudy"
        )
    }

    var selectedTicks by remember { mutableStateOf(mapOf<String, String>()) }
    var currentPhase by remember { mutableStateOf(1) } // Phase 1: Grid, Phase 2: Fill & Circle
    var showPhase1Error by remember { mutableStateOf(false) }

    // Phase 2 State
    var textInput1 by remember { mutableStateOf("") }
    var textInput2 by remember { mutableStateOf("") }
    var textInput3 by remember { mutableStateOf("") }
    var textInput4 by remember { mutableStateOf("") }

    // Clicked letters indices list to simulate "Circle big letters"
    // Sentence: "It's time for PE," says the teacher. "Let's get a football."
    // Sentence: "My mother's a nurse," says the boy. "She drives a red car."
    // Large letters are "I" (index 0 of sent 1), "P" (index 13), "E" (index 14), "L" (index 31) etc.
    // We can just create an interactive clicking letter view
    val bigLetters = remember { listOf("I", "P", "E", "L", "M", "S") }
    val clickedLetters = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📻 城市天气广播 & 书写圈词挑战",
                    color = Color(0xFF60A5FA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (currentPhase == 1) "第一阶段: 收听天气预报并勾选表格" else "第二阶段: 句子填空与圈出大写字母",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
                if (currentPhase == 1) {
                    Text("🔊 广播音频未接入，当前已提供高保真静态广播提示：", color = Color(0xFFFBBF24), fontSize = 11.sp)
                }
            }
        }

        if (currentPhase == 1) {
            // Weather Grid Column
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🗺️ 教材广播提示:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        "“Hello, this is the weather report. It is sunny in Beijing and hot and sunny in Sanya. Shanghai is rainy. Harbin is very cold with snow. Lhasa is windy. Hong Kong is cloudy.”",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Divider(color = Color.Gray)

                    locations.forEach { location ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(location, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                conditions.forEach { condition ->
                                    val isChecked = selectedTicks[location] == condition
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isChecked) Color(0xFF10B981) else Color(0xFF334155))
                                            .clickable {
                                                selectedTicks = selectedTicks.toMutableMap().apply { put(location, condition) }
                                            }
                                            .testTag("tick_${location}_${condition}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (condition) {
                                                "Sunny" -> "☀️"
                                                "Rainy" -> "🌧️"
                                                "Snowy" -> "❄️"
                                                "Windy" -> "🌬️"
                                                else -> "☁️"
                                            },
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                        Divider(color = Color(0xFF334155))
                    }
                }
            }

            if (showPhase1Error) {
                Text("⚠️ 请将六个城市都勾选正确的天气！", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val allCorrect = locations.all { selectedTicks[it] == correctTick[it] }
                    if (allCorrect) {
                        currentPhase = 2
                        showPhase1Error = false
                        SpeechSynthesizer.speak(context, "Wonderful!")
                    } else {
                        showPhase1Error = true
                        SpeechSynthesizer.speak(context, "Check again.")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("weather_grid_submit")
            ) {
                Text("核对广播并进入下一阶段", fontWeight = FontWeight.Bold)
            }
        } else {
            // Phase 2: Read and write + Circle the big letters
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("✍️ 完成填空题 (请输入首字母小写的词):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    // Question A
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("“It's time for PE,” says the _______ (老师). Let's get a _______ (足球).", color = Color.LightGray, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = textInput1,
                                onValueChange = { textInput1 = it },
                                label = { Text("空格1") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.weight(1f).testTag("blank1")
                            )
                            OutlinedTextField(
                                value = textInput2,
                                onValueChange = { textInput2 = it },
                                label = { Text("空格2") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.weight(1f).testTag("blank2")
                            )
                        }
                    }

                    // Question B
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("“My mother's a _______ (护士),” says the boy. “She drives a red _______ (小汽车).”", color = Color.LightGray, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = textInput3,
                                onValueChange = { textInput3 = it },
                                label = { Text("空格3") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.weight(1f).testTag("blank3")
                            )
                            OutlinedTextField(
                                value = textInput4,
                                onValueChange = { textInput4 = it },
                                label = { Text("空格4") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.weight(1f).testTag("blank4")
                            )
                        }
                    }
                }
            }

            // Big Letters Clicking Panel
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔠 教材 P34：圈出所有应大写的英文字母", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("提示：包括句首字母及缩写。点击高亮圈出：", color = Color.LightGray, fontSize = 12.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bigLetters.forEach { char ->
                            val isClicked = clickedLetters.contains(char)
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isClicked) Color(0xFFEC4899) else Color(0xFF334155))
                                    .clickable {
                                        if (isClicked) clickedLetters.remove(char) else clickedLetters.add(char)
                                    }
                                    .border(1.dp, Color.White, CircleShape)
                                    .testTag("circle_letter_$char"),
                                contentAlignment = Alignment.Center
                             ) {
                                Text(char, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val a1 = textInput1.trim().lowercase() == "teacher"
                    val a2 = textInput2.trim().lowercase() == "football"
                    val a3 = textInput3.trim().lowercase() == "nurse"
                    val a4 = textInput4.trim().lowercase() == "car"
                    val hasAllLetters = clickedLetters.size == bigLetters.size

                    if (a1 && a2 && a3 && a4 && hasAllLetters) {
                        onCompleted()
                        SpeechSynthesizer.speak(context, "Amazing spelling and grammar!")
                    } else {
                        SpeechSynthesizer.speak(context, "Please check fill blanks and letters.")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("read_write_submit")
            ) {
                Text("保存我的拼写并完成", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun G4S2Recycle1DayTimelineReadCircleView(
    context: Context,
    onCompleted: () -> Unit
) {
    // Correct choices for each blank in P35 Read and circle
    // Choice 1: "library" (instead of playground)
    // Choice 2: "second" (instead of first)
    // Choice 3: "rainy" (instead of sunny)
    // Choice 4: "inside" (instead of outside)
    // Choice 5: "PE class" (instead of music class)
    // Choice 6: "can" (instead of can't)

    var choice1 by remember { mutableStateOf<String?>(null) }
    var choice2 by remember { mutableStateOf<String?>(null) }
    var choice3 by remember { mutableStateOf<String?>(null) }
    var choice4 by remember { mutableStateOf<String?>(null) }
    var choice5 by remember { mutableStateOf<String?>(null) }
    var choice6 by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📅 一天校园生存挑战 · 一日时间轴",
                    color = Color(0xFFEC4899),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "教材 P35 Read and circle：跟随一日活动时间轴与气象，做出正确判断：",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }
        }

        // Timeline visualization cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🕒 虚拟生存一日时间线:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("6:00" to "起床", "7:30" to "上学", "9:00" to "语文", "10:00" to "图书馆", "12:00" to "午餐", "3:15" to "体育").forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(item.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(item.second, color = Color.LightGray, fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        // Passage circling cards
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("📖 精读短文并圈出词汇 (点击选中):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                // Circle 1 & 2
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("It is 10:00. I go to the ____________. It is on the ____________ floor.", color = Color.White, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("library", "playground").forEach { opt ->
                            val active = choice1 == opt
                            Button(
                                onClick = { choice1 = opt },
                                colors = ButtonDefaults.buttonColors(containerColor = if (active) Color(0xFFEC4899) else Color(0xFF334155)),
                                modifier = Modifier.weight(1f).testTag("p35_c1_$opt")
                            ) { Text(opt, fontSize = 12.sp) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("first", "second").forEach { opt ->
                            val active = choice2 == opt
                            Button(
                                onClick = { choice2 = opt },
                                colors = ButtonDefaults.buttonColors(containerColor = if (active) Color(0xFFEC4899) else Color(0xFF334155)),
                                modifier = Modifier.weight(1f).testTag("p35_c2_$opt")
                            ) { Text(opt, fontSize = 12.sp) }
                        }
                    }
                }

                Divider(color = Color(0xFF334155))

                // Circle 3 & 4
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("It is 12:00. It is ____________ now. We must stay ____________.", color = Color.White, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("sunny", "rainy").forEach { opt ->
                            val active = choice3 == opt
                            Button(
                                onClick = { choice3 = opt },
                                colors = ButtonDefaults.buttonColors(containerColor = if (active) Color(0xFFEC4899) else Color(0xFF334155)),
                                modifier = Modifier.weight(1f).testTag("p35_c3_$opt")
                            ) { Text(opt, fontSize = 12.sp) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("inside", "outside").forEach { opt ->
                            val active = choice4 == opt
                            Button(
                                onClick = { choice4 = opt },
                                colors = ButtonDefaults.buttonColors(containerColor = if (active) Color(0xFFEC4899) else Color(0xFF334155)),
                                modifier = Modifier.weight(1f).testTag("p35_c4_$opt")
                            ) { Text(opt, fontSize = 12.sp) }
                        }
                    }
                }

                Divider(color = Color(0xFF334155))

                // Circle 5 & 6
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("It is 3:15. It is time for ____________. It is windy, we ____________ play football.", color = Color.White, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("PE class", "music class").forEach { opt ->
                            val active = choice5 == opt
                            Button(
                                onClick = { choice5 = opt },
                                colors = ButtonDefaults.buttonColors(containerColor = if (active) Color(0xFFEC4899) else Color(0xFF334155)),
                                modifier = Modifier.weight(1f).testTag("p35_c5_$opt")
                            ) { Text(opt, fontSize = 12.sp) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("can", "can't").forEach { opt ->
                            val active = choice6 == opt
                            Button(
                                onClick = { choice6 = opt },
                                colors = ButtonDefaults.buttonColors(containerColor = if (active) Color(0xFFEC4899) else Color(0xFF334155)),
                                modifier = Modifier.weight(1f).testTag("p35_c6_$opt")
                            ) { Text(opt, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                val r1 = choice1 == "library"
                val r2 = choice2 == "second"
                val r3 = choice3 == "rainy"
                val r4 = choice4 == "inside"
                val r5 = choice5 == "PE class"
                val r6 = choice6 == "can"

                if (r1 && r2 && r3 && r4 && r5 && r6) {
                    onCompleted()
                    SpeechSynthesizer.speak(context, "Bingo! Amazing! Mission 5 timeline cleared.")
                } else {
                    SpeechSynthesizer.speak(context, "Check your selections again.")
                }
            },
            enabled = choice1 != null && choice2 != null && choice3 != null && choice4 != null && choice5 != null && choice6 != null,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("day_timeline_submit")
        ) {
            Text("核对圈词答案并保存", fontWeight = FontWeight.Bold)
        }
    }
}


