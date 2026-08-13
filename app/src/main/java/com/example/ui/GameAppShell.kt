package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameAppShell(
    viewModel: GameViewModel,
    onNavigateToLevels: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToParent: () -> Unit,
    onNavigateToHoliday: () -> Unit,
    onNavigateToAccountCenter: () -> Unit,
    onNavigateToPetHouse: () -> Unit,
    onNavigateToMonsterCodex: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToBackpack: () -> Unit,
    onNavigateToBrushLibrary: () -> Unit,
    onNavigateToChushibiao: () -> Unit,
    onNavigateToBrushTuning: (String) -> Unit,
    onNavigateToBrushDetail: (String) -> Unit,
    onNavigateToCodex: () -> Unit,
    onNavigateToMathLesson: (String, String, String) -> Unit = { _, _, _ -> },
    onNavigateToEnglishLesson: (String, String, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(GameTab.CAMP) }

    // Intercept Back button if we are not on the Camp tab to go back to Camp tab
    BackHandler(enabled = selectedTab != GameTab.CAMP) {
        selectedTab = GameTab.CAMP
    }

    Scaffold(
        topBar = {
            GameTopHud(
                viewModel = viewModel,
                onNavigateToSettings = onNavigateToParent
            )
        },
        bottomBar = {
            GameBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = GameUiTokens.Colors.Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(150)) with fadeOut(animationSpec = tween(150))
                },
                label = "tabTransition"
            ) { tab ->
                when (tab) {
                    GameTab.CAMP -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToLevels = onNavigateToLevels,
                            onNavigateToReview = onNavigateToReview,
                            onNavigateToReport = onNavigateToReport,
                            onNavigateToParent = onNavigateToParent,
                            onNavigateToHoliday = onNavigateToHoliday,
                            onNavigateToAccountCenter = onNavigateToAccountCenter,
                            onNavigateToPetHouse = onNavigateToPetHouse,
                            onNavigateToMonsterCodex = onNavigateToMonsterCodex,
                            onNavigateToAchievements = onNavigateToAchievements,
                            onNavigateToShop = onNavigateToShop,
                            onNavigateToBackpack = onNavigateToBackpack,
                            onNavigateToBrushLibrary = onNavigateToBrushLibrary,
                            onNavigateToChushibiao = onNavigateToChushibiao,
                            isEmbeddedInShell = true
                        )
                    }
                    GameTab.ADVENTURE -> {
                        AdventureMapScreen(
                            viewModel = viewModel,
                            onBack = { selectedTab = GameTab.CAMP },
                            onNavigateToLevels = onNavigateToLevels,
                            onNavigateToReview = onNavigateToReview,
                            onNavigateToHoliday = onNavigateToHoliday,
                            onNavigateToPetHouse = onNavigateToPetHouse,
                            isEmbeddedInShell = true,
                            onNavigateToCodex = onNavigateToCodex,
                            onNavigateToMathLesson = onNavigateToMathLesson,
                            onNavigateToEnglishLesson = onNavigateToEnglishLesson
                        )
                    }
                    GameTab.BRUSH -> {
                        BrushHubScreen(
                            viewModel = viewModel,
                            onNavigateToBrushLibrary = onNavigateToBrushLibrary,
                            onNavigateToShop = onNavigateToShop,
                            onNavigateToBrushTuning = onNavigateToBrushTuning,
                            onNavigateToBrushDetail = onNavigateToBrushDetail
                        )
                    }
                    GameTab.COMPANION -> {
                        CompanionHubScreen(
                            viewModel = viewModel,
                            onNavigateToPetHouse = onNavigateToPetHouse,
                            onNavigateToShop = onNavigateToShop
                        )
                    }
                    GameTab.GROWTH -> {
                        GrowthHubScreen(
                            viewModel = viewModel,
                            onNavigateToBackpack = onNavigateToBackpack,
                            onNavigateToAchievements = onNavigateToAchievements,
                            onNavigateToReport = onNavigateToReport,
                            onNavigateToAccountCenter = onNavigateToAccountCenter,
                            onNavigateToParent = onNavigateToParent
                        )
                    }
                }
            }
        }
    }
}
