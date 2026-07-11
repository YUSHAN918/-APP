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
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
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
                    onNavigateToBrushLibrary = { BrushNavigator.navigateToLibrary(navController) }
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
            composable("adventure_map") {
                AdventureMapScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToLevels = { navController.navigate("levels") },
                    onNavigateToReview = { navController.navigate("review") },
                    onNavigateToHoliday = { navController.navigate("holiday") },
                    onNavigateToPetHouse = { navController.navigate("pet_house") }
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
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
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
                    }
                )
            }
            composable("lab") {
                HandwritingLabScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
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



