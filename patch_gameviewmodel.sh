sed -i '/fun startLevelById(levelId: Int) {/i \
    fun startCustomBattle(levelName: String, words: List<com.example.data.WordItem>) {\
        _currentLevel.value = null\
        _levelName.value = levelName\
        _currentBattleWords.value = words\
        _totalLevelWordCount.value = words.size\
        _maxComboInBattle.value = 0\
        _correctCount.value = 0\
        _wrongCount.value = 0\
        _currentStage.value = GameStage.PREP\
    }\
' app/src/main/java/com/example/viewmodel/GameViewModel.kt
