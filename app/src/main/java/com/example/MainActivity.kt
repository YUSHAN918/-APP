package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.theme.AppTheme
import com.example.viewmodel.GameViewModel
import androidx.navigation.navArgument
import androidx.navigation.NavType

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                WordCrusadeApp(viewModel)
            }
        }
    }
}

@Composable
fun WordCrusadeApp(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val localAccounts by viewModel.localAccounts.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    val playerProfile by viewModel.playerProfile.collectAsState()
    val currentAccountPlayers by viewModel.currentAccountPlayers.collectAsState()

    var forceShowCreateAccount by remember { mutableStateOf(false) }
    var forceShowCreatePlayer by remember { mutableStateOf(false) }

    if (forceShowCreateAccount) {
        CreateAccountScreen(
            viewModel = viewModel,
            onBack = { forceShowCreateAccount = false },
            onSuccess = { forceShowCreateAccount = false }
        )
    } else if (forceShowCreatePlayer) {
        CreatePlayerScreen(
            viewModel = viewModel,
            onCompleted = { forceShowCreatePlayer = false }
        )
    } else if (localAccounts.isEmpty()) {
        CreateAccountScreen(
            viewModel = viewModel,
            onBack = null,
            onSuccess = {}
        )
    } else if (currentSession?.currentAccountId == null) {
        AccountSelectScreen(
            viewModel = viewModel,
            onCreateAccount = { forceShowCreateAccount = true }
        )
    } else if (currentAccountPlayers.isEmpty()) {
        CreatePlayerScreen(
            viewModel = viewModel,
            onCompleted = {}
        )
    } else if (currentSession?.currentPlayerId == null) {
        PlayerSelectScreen(
            viewModel = viewModel,
            onNavigateToCreatePlayer = { forceShowCreatePlayer = true },
            onBackToAccountSelect = { viewModel.logout() }
        )
    } else {
        LaunchedEffect(viewModel) {
            viewModel.navigateToBattle.collect { levelId ->
                viewModel.startLevelById(levelId)
                navController.navigate("battle")
            }
        }
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                GameAppShell(
                    viewModel = viewModel,
                    onNavigateToLevels = { navController.navigate("adventure_map") },
                    onNavigateToReview = { navController.navigate("review") },
                    onNavigateToReport = { navController.navigate("report") },
                    onNavigateToParent = { navController.navigate("parent") },
                    onNavigateToHoliday = { navController.navigate("holiday") },
                    onNavigateToAccountCenter = { navController.navigate("account_center") },
                    onNavigateToPetHouse = { navController.navigate("pet_house") },
                    onNavigateToMonsterCodex = { navController.navigate("monster_codex") },
                    onNavigateToAchievements = { navController.navigate("achievements") },
                    onNavigateToShop = { BrushNavigator.navigateToShop(navController) },
                    onNavigateToBackpack = { navController.navigate("backpack") },
                    onNavigateToBrushLibrary = { BrushNavigator.navigateToLibrary(navController) },
                    onNavigateToChushibiao = { navController.navigate("chushibiao") },
                    onNavigateToBrushTuning = { BrushNavigator.navigateToTuning(navController, it) },
                    onNavigateToBrushDetail = { BrushNavigator.navigateToDetail(navController, it) },
                    onNavigateToCodex = { navController.navigate("codex_tab") },
                    onNavigateToMathLesson = { courseId, unitId, lessonId ->
                        navController.navigate("mathLesson/$courseId/$unitId/$lessonId")
                    },
                    onNavigateToEnglishLesson = { courseId, unitId, lessonId ->
                        navController.navigate("englishLesson/$courseId/$unitId/$lessonId")
                    }
                )
            }
            composable("monster_codex") {
                MonsterCodexScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToLevels = { navController.navigate("levels") }
                )
            }
            composable("achievements") {
                AchievementsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("adventure_map") { backStackEntry ->
                val initialWorld = backStackEntry.arguments?.getString("initialWorld") ?: "语文"
                AdventureMapScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToLevels = { navController.navigate("levels") },
                    onNavigateToReview = { navController.navigate("review") },
                    onNavigateToHoliday = { navController.navigate("holiday") },
                    onNavigateToPetHouse = { navController.navigate("pet_house") },
                    onNavigateToMathLesson = { courseId, unitId, lessonId ->
                        navController.navigate("mathLesson/$courseId/$unitId/$lessonId")
                    },
                    onNavigateToEnglishLesson = { courseId, unitId, lessonId ->
                        navController.navigate("englishLesson/$courseId/$unitId/$lessonId")
                    },
                    initialWorld = initialWorld
                )
            }
            composable("chushibiao") {
                ChushibiaoScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("account_center") {
                AccountCenterScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("pet_house") {
                PetHouseScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("holiday") {
                HolidayHomeworkCenterScreen(
                    onNavigateToBattle = { navController.navigate("battle") },
                    onNavigateToDictation = { levelId ->
                        viewModel.startLevelById(levelId)
                        navController.navigate("battle")
                    },
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToQuests = { navController.navigate("quests") }
                )
            }
            composable("quests") {
                QuestsTabScreen(
                    viewModel = viewModel,
                    onNavigateToLevels = { navController.navigate("adventure_map") },
                    onNavigateToReview = { navController.navigate("review") },
                    onNavigateToReport = { navController.navigate("report") },
                    onNavigateToHoliday = { navController.navigate("holiday") }
                )
            }
            composable("codex_tab") {
                CodexTabScreen(
                    viewModel = viewModel,
                    onNavigateToMonsterCodex = { navController.navigate("monster_codex") },
                    onNavigateToAchievements = { navController.navigate("achievements") },
                    onNavigateToReport = { navController.navigate("report") },
                    onNavigateToChushibiao = { navController.navigate("chushibiao") }
                )
            }
            composable("levels") {
                LevelScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onLevelSelect = { levelId -> 
                        viewModel.startLevelById(levelId)
                        navController.navigate("battle")
                    }
                )
            }
            composable("battle") {
                BattleScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigate("home") { popUpTo("home") { inclusive = false } } },
                    onNavigateToReport = { navController.navigate("report") { popUpTo("home") } },
                    onNavigateToReview = { navController.navigate("review") { popUpTo("home") } }
                )
            }
            composable("review") {
                ReviewScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onStartReview = {
                        viewModel.startReviewMode()
                        navController.navigate("battle")
                    }
                )
            }
            composable("report") {
                ReportScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("parent") {
                ParentScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToLab = { navController.navigate("lab") },
                    onStartLevelTest = { levelId ->
                        viewModel.startLevelById(levelId)
                        navController.navigate("battle")
                    },
                    onNavigateToMathLesson = { courseId, unitId, lessonId ->
                        navController.navigate("mathLesson/$courseId/$unitId/$lessonId")
                    }
                )
            }
            composable("lab") {
                HandwritingLabScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("mathLesson/{courseId}/{unitId}/{lessonId}") { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId")
                val unitId = backStackEntry.arguments?.getString("unitId")
                val lessonId = backStackEntry.arguments?.getString("lessonId")

                if (courseId.isNullOrEmpty() || unitId.isNullOrEmpty() || lessonId.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color(0xFF0F172A)),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "❌ 路由参数缺失\n\n- courseId: $courseId\n- unitId: $unitId\n- lessonId: $lessonId",
                            color = androidx.compose.ui.graphics.Color.Red,
                            fontSize = 16.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    com.example.ui.math.MathLessonScreen(
                        courseId = courseId,
                        unitId = unitId,
                        lessonId = lessonId,
                        onBack = { navController.popBackStack() },
                        onComplete = { coins ->
                            viewModel.addPlayerCoins(coins)
                        }
                    )
                }
            }

            composable("englishLesson/{courseId}/{unitId}/{lessonId}") { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId")
                val unitId = backStackEntry.arguments?.getString("unitId")
                val lessonId = backStackEntry.arguments?.getString("lessonId")

                if (courseId.isNullOrEmpty() || unitId.isNullOrEmpty() || lessonId.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color(0xFF0F172A)),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "❌ 路由参数缺失\n\n- courseId: $courseId\n- unitId: $unitId\n- lessonId: $lessonId",
                            color = androidx.compose.ui.graphics.Color.Red,
                            fontSize = 16.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else if (lessonId == "recycle" || unitId.contains("recycle")) {
                    com.example.ui.english.EnglishRecycleHubScreen(
                        courseId = courseId,
                        recycleId = unitId,
                        onNavigateToMission = { mId ->
                            navController.navigate("english/recycle/$courseId/$unitId/mission/$mId")
                        },
                        onNavigateToResult = {
                            navController.navigate("english/recycle/$courseId/$unitId/result")
                        },
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    com.example.ui.english.EnglishLessonScreen(
                        courseId = courseId,
                        unitId = unitId,
                        lessonId = lessonId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onComplete = { coins ->
                            viewModel.addPlayerCoins(coins)
                        }
                    )
                }
            }

            // Dynamic CourseId based Recycle Routes
            composable("english/recycle/{courseId}/{recycleId}") { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: "english_pep_2013_g3_s1"
                val recycleId = backStackEntry.arguments?.getString("recycleId") ?: "english_pep_2013_g3_s1_recycle_1"
                com.example.ui.english.EnglishRecycleHubScreen(
                    courseId = courseId,
                    recycleId = recycleId,
                    onNavigateToMission = { mId ->
                        navController.navigate("english/recycle/$courseId/$recycleId/mission/$mId")
                    },
                    onNavigateToResult = {
                        navController.navigate("english/recycle/$courseId/$recycleId/result")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("english/recycle/{courseId}/{recycleId}/mission/{missionId}") { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: "english_pep_2013_g3_s1"
                val recycleId = backStackEntry.arguments?.getString("recycleId") ?: "english_pep_2013_g3_s1_recycle_1"
                val missionId = backStackEntry.arguments?.getString("missionId") ?: "recycle_1_m1"
                com.example.ui.english.EnglishRecycleMissionScreen(
                    courseId = courseId,
                    recycleId = recycleId,
                    missionId = missionId,
                    onBackToHub = { navController.popBackStack() },
                    onNavigateToResult = {
                        navController.navigate("english/recycle/$courseId/$recycleId/result")
                    }
                )
            }

            composable("english/recycle/{courseId}/{recycleId}/result") { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: "english_pep_2013_g3_s1"
                val recycleId = backStackEntry.arguments?.getString("recycleId") ?: "english_pep_2013_g3_s1_recycle_1"
                val boardMissionId = when (recycleId) {
                    "english_pep_2013_g3_s1_recycle_2" -> "recycle_2_m3"
                    "english_pep_2013_g3_s2_recycle_1" -> "english_pep_2013_g3_s2_r1_m4"
                    else -> "recycle_1_m3"
                }
                com.example.ui.english.EnglishRecycleResultScreen(
                    recycleId = recycleId,
                    onClaimReward = { coins -> viewModel.addPlayerCoins(coins) },
                    onReplayBoardGame = {
                        navController.navigate("english/recycle/$courseId/$recycleId/mission/$boardMissionId")
                    },
                    onReturnToMap = {
                        navController.navigate("adventure_map") { popUpTo("home") }
                    },
                    onNavigateToUnit = { targetUnitId ->
                        navController.navigate("englishLesson/$courseId/$targetUnitId/lesson_1")
                    }
                )
            }

            // Legacy Fallback Redirectors to prevent broken links
            composable("english/recycle/{recycleId}") { backStackEntry ->
                val recycleId = backStackEntry.arguments?.getString("recycleId") ?: "english_pep_2013_g3_s1_recycle_1"
                val computedCourseId = if (recycleId.contains("_s2")) "english_pep_2013_g3_s2" else "english_pep_2013_g3_s1"
                LaunchedEffect(recycleId) {
                    navController.navigate("english/recycle/$computedCourseId/$recycleId") {
                        popUpTo("english/recycle/$recycleId") { inclusive = true }
                    }
                }
            }

            composable("english/recycle/{recycleId}/mission/{missionId}") { backStackEntry ->
                val recycleId = backStackEntry.arguments?.getString("recycleId") ?: "english_pep_2013_g3_s1_recycle_1"
                val missionId = backStackEntry.arguments?.getString("missionId") ?: "recycle_1_m1"
                val computedCourseId = if (recycleId.contains("_s2")) "english_pep_2013_g3_s2" else "english_pep_2013_g3_s1"
                LaunchedEffect(recycleId, missionId) {
                    navController.navigate("english/recycle/$computedCourseId/$recycleId/mission/$missionId") {
                        popUpTo("english/recycle/$recycleId/mission/$missionId") { inclusive = true }
                    }
                }
            }

            composable("english/recycle/{recycleId}/result") { backStackEntry ->
                val recycleId = backStackEntry.arguments?.getString("recycleId") ?: "english_pep_2013_g3_s1_recycle_1"
                val computedCourseId = if (recycleId.contains("_s2")) "english_pep_2013_g3_s2" else "english_pep_2013_g3_s1"
                LaunchedEffect(recycleId) {
                    navController.navigate("english/recycle/$computedCourseId/$recycleId/result") {
                        popUpTo("english/recycle/$recycleId/result") { inclusive = true }
                    }
                }
            }

            composable("math_lesson/{unitId}/{lessonId}") { backStackEntry ->
                val unitId = backStackEntry.arguments?.getString("unitId") ?: ""
                val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                com.example.ui.math.MathLessonScreen(
                    courseId = "math_pep_g6_s1",
                    unitId = unitId,
                    lessonId = lessonId,
                    onBack = { navController.popBackStack() },
                    onComplete = { coins ->
                        viewModel.addPlayerCoins(coins)
                    }
                )
            }
            composable("adventure_shop") {
                AdventureShopScreen(
                    viewModel = viewModel,
                    navController = navController,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("backpack") {
                BackpackScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "brush_library?focus={focus}",
                arguments = listOf(navArgument("focus") { nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val focusBrushId = backStackEntry.arguments?.getString("focus")
                BrushLibraryScreen(
                    viewModel = viewModel,
                    focusBrushId = focusBrushId,
                    navController = navController
                )
            }
            composable(
                route = "brush_detail/{brushId}",
                arguments = listOf(navArgument("brushId") { type = NavType.StringType })
            ) { backStackEntry ->
                val brushId = backStackEntry.arguments?.getString("brushId") ?: "default_black"
                BrushDetailScreen(
                    viewModel = viewModel,
                    brushId = brushId,
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    onNavigateToTuning = { BrushNavigator.navigateToTuning(navController, it) }
                )
            }
            composable(
                route = "brush_tuning/{brushId}",
                arguments = listOf(navArgument("brushId") { type = NavType.StringType })
            ) { backStackEntry ->
                val brushId = backStackEntry.arguments?.getString("brushId") ?: "default_black"
                val brush = BrushStyle.getBrushById(brushId)
                val playerProfile by viewModel.playerProfile.collectAsState()
                val unlockedBrushes = playerProfile?.unlockedBrushIds?.split(",") ?: listOf("default_black", "practice_wood")
                val isUnlocked = unlockedBrushes.contains(brushId)
                
                BrushTuningScreen(
                    viewModel = viewModel,
                    brush = brush,
                    isUnlocked = isUnlocked,
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}



