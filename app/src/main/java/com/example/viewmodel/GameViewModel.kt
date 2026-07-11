package com.example.viewmodel

import android.app.Application
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.StrokeData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.coroutines.resume

enum class RecognitionSource {
    NONE, ML_KIT, SYSTEM_IME, MOCK
}

enum class ConfidenceLevel {
    HIGH, MEDIUM, LOW, UNKNOWN
}

data class RecognitionResult(
    val charIndex: Int,
    val expectedChar: String,
    val recognizedText: String,
    val candidates: List<String>,
    val confidenceLevel: ConfidenceLevel,
    val isLikelyCorrect: Boolean,
    val source: RecognitionSource,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

fun createRecognitionResult(
    charIndex: Int,
    expectedChar: String,
    candidates: List<String>,
    source: RecognitionSource,
    errorMessage: String? = null
): RecognitionResult {
    if (errorMessage != null) {
        return RecognitionResult(
            charIndex = charIndex,
            expectedChar = expectedChar,
            recognizedText = "",
            candidates = emptyList(),
            confidenceLevel = ConfidenceLevel.UNKNOWN,
            isLikelyCorrect = false,
            source = source,
            errorMessage = errorMessage
        )
    }

    val recognizedText = candidates.firstOrNull() ?: ""
    val isFirstMatch = expectedChar.isNotEmpty() && recognizedText == expectedChar
    val top3 = candidates.take(3)
    val isInTop3 = expectedChar.isNotEmpty() && top3.contains(expectedChar)

    val confidenceLevel = when {
        isFirstMatch -> ConfidenceLevel.HIGH
        isInTop3 -> ConfidenceLevel.MEDIUM
        else -> ConfidenceLevel.LOW
    }

    val isLikelyCorrect = isFirstMatch || isInTop3

    return RecognitionResult(
        charIndex = charIndex,
        expectedChar = expectedChar,
        recognizedText = recognizedText,
        candidates = candidates,
        confidenceLevel = confidenceLevel,
        isLikelyCorrect = isLikelyCorrect,
        source = source
    )
}

data class CharAnswer(
    val charIndex: Int,
    val strokes: List<StrokeData>,
    val canvasWidth: Float,
    val canvasHeight: Float,
    val isBlank: Boolean,
    val recognitionResult: RecognitionResult? = null
)

data class Answer(
    val word: WordItem,
    val charAnswers: List<CharAnswer>,
    val charCount: Int
)

data class SettlementResult(
    val title: String,
    val isClear: Boolean,
    val totalWords: Int,
    val correctCount: Int,
    val almostCount: Int,
    val wrongCount: Int,
    val coinsGained: Int,
    val expGained: Int,
    val newWrongWordsCount: Int,
    val isBossDefeated: Boolean,
    val isAutoGraded: Boolean = false,
    val intimacyGained: Int = 0,
    val maxCombo: Int = 0
)

data class DailyQuest(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val isClaimed: Boolean,
    val goldReward: Int,
    val expReward: Int,
    val intimacyReward: Int
)

data class RewardClaimResult(
    val title: String,
    val gold: Int,
    val exp: Int,
    val intimacy: Int,
    val hatchEnergy: Int = 0,
    val isLevelUp: Boolean,
    val oldLevel: Int,
    val newLevel: Int,
    val petMsg: String? = null
)

enum class GameStage {
    PREP, DICTATION, ACCEPTANCE, SETTLEMENT
}

class GameViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    private val db = AppDatabase.getDatabase(application)
    val repository = GameRepository(db)

    val allLevels = repository.allLevels.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allWords = repository.allWords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val userStats = repository.userStats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val wrongWords = repository.allWrongWords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allSessions = repository.allSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allHolidayPacks = repository.allHolidayPacks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allHolidayTasks = repository.allHolidayTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allHolidayCheckIns = repository.allHolidayCheckIns.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allHolidayWorkSessions = repository.allHolidayWorkSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allHolidayMaterials = repository.allHolidayMaterials.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allHolidayMaterialProgress = repository.allHolidayMaterialProgress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allRecitationRecords = repository.allRecitationRecords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDictationRecords = repository.allDictationRecords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val playerProfile = repository.playerProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val localAccounts = repository.localAccounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val currentSession = repository.currentSession.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentAccountPlayers = currentSession.flatMapLatest { session ->
        val accountId = session?.currentAccountId
        if (accountId != null) {
            repository.getProfilesForAccount(accountId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentAccount = currentSession.flatMapLatest { session ->
        val accountId = session?.currentAccountId
        if (accountId != null) {
            localAccounts.map { list -> list.find { it.id == accountId } }
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activePet = playerProfile.flatMapLatest { player ->
        if (player != null) {
            repository.getActivePetForPlayer(player.id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val sleepingPets = playerProfile.flatMapLatest { player ->
        if (player != null) {
            repository.getSleepingPetsForPlayer(player.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val equippedBrushConfig = playerProfile.flatMapLatest { player ->
        if (player != null && player.equippedBrushId.isNotEmpty()) {
            repository.getPlayerBrushConfigFlow(player.id, player.equippedBrushId)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val inventoryItems = playerProfile.flatMapLatest { player ->
        if (player != null) {
            repository.getInventoryForPlayerFlow(player.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dailyQuests = MutableStateFlow<List<DailyQuest>>(emptyList())
    val dailyQuests: StateFlow<List<DailyQuest>> = _dailyQuests.asStateFlow()

    private val _rewardClaimResult = MutableStateFlow<RewardClaimResult?>(null)
    val rewardClaimResult: StateFlow<RewardClaimResult?> = _rewardClaimResult.asStateFlow()

    fun clearRewardClaimResult() {
        _rewardClaimResult.value = null
    }

    fun refreshDailyQuests() {
        val player = playerProfile.value
        if (player == null) {
            _dailyQuests.value = emptyList()
            return
        }
        val playerId = player.id
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sp = getApplication<Application>().getSharedPreferences("word_crusade_quests", Context.MODE_PRIVATE)

        val quests = listOf(
            DailyQuest(
                id = "dictation",
                title = "今日字词挑战",
                description = "完成任意 1 次字词听写挑战",
                currentProgress = sp.getInt("quest_progress_dictation_${today}_${playerId}", 0),
                targetProgress = 1,
                isClaimed = sp.getBoolean("quest_claimed_dictation_${today}_${playerId}", false),
                goldReward = 20,
                expReward = 20,
                intimacyReward = 0
            ),
            DailyQuest(
                id = "holiday",
                title = "今日暑假委托",
                description = "完成任意 1 个暑假作业任务打卡或执行记录",
                currentProgress = sp.getInt("quest_progress_holiday_${today}_${playerId}", 0),
                targetProgress = 1,
                isClaimed = sp.getBoolean("quest_claimed_holiday_${today}_${playerId}", false),
                goldReward = 10,
                expReward = 15,
                intimacyReward = 0
            ),
            DailyQuest(
                id = "error_purify",
                title = "错题魔物净化",
                description = "复习并净化 3 个错题",
                currentProgress = sp.getInt("quest_progress_error_purify_${today}_${playerId}", 0),
                targetProgress = 3,
                isClaimed = sp.getBoolean("quest_claimed_error_purify_${today}_${playerId}", false),
                goldReward = 15,
                expReward = 0,
                intimacyReward = 5
            ),
            DailyQuest(
                id = "recitation",
                title = "诗词背诵试炼",
                description = "完成 1 次古诗词背诵录音或默写",
                currentProgress = sp.getInt("quest_progress_recitation_${today}_${playerId}", 0),
                targetProgress = 1,
                isClaimed = sp.getBoolean("quest_claimed_recitation_${today}_${playerId}", false),
                goldReward = 0,
                expReward = 20,
                intimacyReward = 0
            )
        )
        _dailyQuests.value = quests
    }

    fun incrementQuestProgress(questId: String, delta: Int = 1) {
        val player = playerProfile.value ?: return
        val playerId = player.id
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sp = getApplication<Application>().getSharedPreferences("word_crusade_quests", Context.MODE_PRIVATE)
        val current = sp.getInt("quest_progress_${questId}_${today}_${playerId}", 0)
        sp.edit().putInt("quest_progress_${questId}_${today}_${playerId}", current + delta).apply()
        refreshDailyQuests()
    }

    fun claimQuestReward(questId: String) {
        val player = playerProfile.value ?: return
        val playerId = player.id
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sp = getApplication<Application>().getSharedPreferences("word_crusade_quests", Context.MODE_PRIVATE)
        
        val progressKey = "quest_progress_${questId}_${today}_${playerId}"
        val claimedKey = "quest_claimed_${questId}_${today}_${playerId}"
        
        val currentProgress = sp.getInt(progressKey, 0)
        val isAlreadyClaimed = sp.getBoolean(claimedKey, false)
        
        val targetProgress = when (questId) {
            "dictation" -> 1
            "holiday" -> 1
            "error_purify" -> 3
            "recitation" -> 1
            else -> 1
        }
        
        if (currentProgress >= targetProgress && !isAlreadyClaimed) {
            sp.edit().putBoolean(claimedKey, true).apply()
            
            val goldReward = when (questId) {
                "dictation" -> 20
                "holiday" -> 10
                "error_purify" -> 15
                "recitation" -> 0
                else -> 0
            }
            val expReward = when (questId) {
                "dictation" -> 20
                "holiday" -> 15
                "error_purify" -> 0
                "recitation" -> 20
                else -> 0
            }
            val intimacyReward = when (questId) {
                "error_purify" -> 5
                else -> 0
            }
            
            viewModelScope.launch {
                val oldLevel = player.level
                
                // Add gold and exp
                if (goldReward > 0) {
                    repository.addPlayerCoins(goldReward)
                }
                if (expReward > 0) {
                    repository.addPlayerExp(expReward)
                }
                if (intimacyReward > 0) {
                    repository.addPetIntimacy(intimacyReward)
                }
                
                // Check if all daily tasks are completed and claimed for the +20 energy bonus
                val allCompletedClaimedKey = "quest_all_completed_claimed_${today}_${playerId}"
                val wasAllClaimedBefore = sp.getBoolean(allCompletedClaimedKey, false)
                if (!wasAllClaimedBefore) {
                    val allQuests = dailyQuests.value
                    val allClaimedNow = allQuests.all { it.id == questId || it.isClaimed }
                    if (allClaimedNow) {
                        sp.edit().putBoolean(allCompletedClaimedKey, true).apply()
                        gainHatchEnergy(15)
                    }
                }

                // Fetch updated profile to check level up
                val updatedPlayer = repository.getPlayerProfileDirect()
                val isLevelUp = updatedPlayer != null && updatedPlayer.level > oldLevel
                val newLevel = updatedPlayer?.level ?: oldLevel
                
                var petMsg: String? = null
                if (intimacyReward > 0) {
                    val activePetBinding = activePet.value
                    if (activePetBinding != null) {
                        petMsg = "${activePetBinding.petName}更喜欢你了！"
                    }
                }
                
                _rewardClaimResult.value = RewardClaimResult(
                    title = "任务完成！",
                    gold = goldReward,
                    exp = expReward,
                    intimacy = intimacyReward,
                    isLevelUp = isLevelUp,
                    oldLevel = oldLevel,
                    newLevel = newLevel,
                    petMsg = petMsg
                )
                
                refreshDailyQuests()
            }
        }
    }

    val autoGradingToast = MutableStateFlow<String?>(null)
    private var levelStartedAt: Long = 0L


    private var tts: TextToSpeech? = null
    var isTtsReady = MutableStateFlow(false)
    private var autoPlayJob: Job? = null
    private var currentSessionId: Int = -1
    
    // Play/Pause TTS State
    private var currentFullText: String = ""
    var ttsState = MutableStateFlow("STOPPED") // STOPPED, PLAYING, PAUSED
    var ttsActiveText = kotlinx.coroutines.flow.MutableStateFlow("")
    private var ttsTextList = emptyList<String>()
    private var ttsCurrentIndex = 0
    private var lastSpokenCharOffset = 0
    private var ttsJob: Job? = null

    private val _hatchNotification = MutableStateFlow<String?>(null)
    val hatchNotification = _hatchNotification.asStateFlow()

    fun clearHatchNotification() {
        _hatchNotification.value = null
    }

    fun gainHatchEnergy(amount: Int) {
        viewModelScope.launch {
            val resultMessage = repository.addHatchEnergy(amount)
            if (resultMessage != null) {
                _hatchNotification.value = resultMessage
            }
            checkAndUnlockBrushes()
        }
    }

    fun hatchEgg() {
        viewModelScope.launch {
            val resultMessage = repository.hatchEgg()
            if (resultMessage != null) {
                _hatchNotification.value = resultMessage
            }
        }
    }

    fun renamePet(newName: String) {
        viewModelScope.launch {
            val player = playerProfile.value ?: return@launch
            val pet = activePet.value ?: return@launch
            repository.updatePetBinding(pet.copy(customName = newName, updatedAt = System.currentTimeMillis()))
        }
    }

    fun checkAndUnlockBrushes() {
        viewModelScope.launch {
            val player = playerProfile.value ?: return@launch
            val activePetVal = activePet.value
            
            val currentUnlocked = player.unlockedBrushIds.split(",").toMutableSet()
            var changed = false
            
            if (player.level >= 2 && !currentUnlocked.contains("ink_brush")) {
                currentUnlocked.add("ink_brush")
                changed = true
            }
            if (player.totalStudyDays >= 3 && !currentUnlocked.contains("stardust_brush")) {
                currentUnlocked.add("stardust_brush")
                changed = true
            }
            val masteredCount = wrongWords.value.count { it.isMastered }
            if (masteredCount >= 10 && !currentUnlocked.contains("fluorescent_brush")) {
                currentUnlocked.add("fluorescent_brush")
                changed = true
            }
            if (activePetVal != null && activePetVal.intimacy >= 50 && !currentUnlocked.contains("pet_dragon_brush")) {
                currentUnlocked.add("pet_dragon_brush")
                changed = true
            }

            if (changed) {
                val updated = player.copy(
                    unlockedBrushIds = currentUnlocked.joinToString(","),
                    updatedAt = System.currentTimeMillis()
                )
                db.playerProfileDao().insertOrUpdateProfile(updated)
            }
        }
    }

    fun equipBrush(brushId: String) {
        viewModelScope.launch {
            val success = repository.equipBrush(brushId)
            if (success) {
                _hatchNotification.value = "已成功装备笔刷：${com.example.ui.BrushStyle.getBrushById(brushId).brushName}！🖌️"
            }
        }
    }

    fun saveBrushConfig(config: com.example.data.PlayerBrushConfig) {
        viewModelScope.launch {
            repository.savePlayerBrushConfig(config)
        }
    }

    suspend fun getBrushConfig(brushId: String): com.example.data.PlayerBrushConfig? {
        val player = playerProfile.value ?: return null
        return repository.getPlayerBrushConfig(player.id, brushId)
    }

    fun unlockBrushDirect(brushId: String) {
        viewModelScope.launch {
            repository.unlockBrush(brushId)
        }
    }

    fun feedPet(foodType: String) {
        viewModelScope.launch {
            val msg = repository.feedPet(foodType)
            _hatchNotification.value = msg
            checkAndUnlockBrushes()
        }
    }

    fun awakenPet(useCoins: Boolean) {
        viewModelScope.launch {
            val msg = repository.awakenPet(useCoins)
            _hatchNotification.value = msg
            checkAndUnlockBrushes()
        }
    }

    init {
        tts = TextToSpeech(application, this)
        viewModelScope.launch {
            repository.checkAndPerformAutoMigration()
            repository.checkAndInitDefaultData(getApplication())
        }
        viewModelScope.launch {
            playerProfile.collect { player ->
                refreshDailyQuests()
                if (player != null) {
                    val decayMsg = repository.petDailyDecay(getApplication())
                    if (decayMsg != null) {
                        _hatchNotification.value = decayMsg
                    }
                    checkAndUnlockBrushes()
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.CHINESE)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady.value = true
            }
        }
    }

    fun speak(text: String) {
        if (ttsState.value == "PLAYING") {
            if (currentFullText == text) {
                // Pause
                ttsState.value = "PAUSED"
                tts?.stop()
                ttsJob?.cancel()
            } else {
                startNewSpeech(text)
            }
        } else if (ttsState.value == "PAUSED" && currentFullText == text) {
            // Resume
            ttsState.value = "PLAYING"
            startTtsJob()
        } else {
            startNewSpeech(text)
        }
    }

    private fun startNewSpeech(text: String) {
        tts?.stop()
        ttsJob?.cancel()
        currentFullText = text
        ttsActiveText.value = text
        ttsTextList = text.split(Regex("(?<=[。！？\n])")).filter { it.isNotBlank() }
        ttsCurrentIndex = 0
        lastSpokenCharOffset = 0
        ttsState.value = "PLAYING"
        startTtsJob()
    }

    private fun startTtsJob() {
        ttsJob?.cancel()
        ttsJob = viewModelScope.launch {
            while (ttsCurrentIndex < ttsTextList.size) {
                if (ttsState.value != "PLAYING") break
                val fullChunk = ttsTextList[ttsCurrentIndex]
                val offset = lastSpokenCharOffset
                val chunkToSpeak = if (offset > 0 && offset < fullChunk.length) {
                    fullChunk.substring(offset)
                } else {
                    fullChunk
                }
                val baseOffset = if (offset > 0 && offset < fullChunk.length) offset else 0

                speakAndWait(chunkToSpeak, pauseAfterMs = 100L, initialOffset = baseOffset)
                if (ttsState.value == "PLAYING") {
                    lastSpokenCharOffset = 0
                    ttsCurrentIndex++
                }
            }
            if (ttsCurrentIndex >= ttsTextList.size && ttsState.value == "PLAYING") {
                ttsState.value = "STOPPED"
                ttsActiveText.value = ""
                ttsCurrentIndex = 0
                lastSpokenCharOffset = 0
            }
        }
    }

    fun stopSpeaking() {
        ttsState.value = "STOPPED"
        ttsActiveText.value = ""
        currentFullText = ""
        ttsCurrentIndex = 0
        lastSpokenCharOffset = 0
        tts?.stop()
        ttsJob?.cancel()
    }

    fun replay(text: String) {
        startNewSpeech(text)
    }

    suspend fun speakAndWait(text: String, pauseAfterMs: Long = 1200L, initialOffset: Int = 0) {
        if (text.isBlank()) return

        if (!isTtsReady.value) {
            withTimeoutOrNull(2000L) {
                isTtsReady.first { it }
            }
        }

        if (!isTtsReady.value || tts == null) {
            delay(pauseAfterMs)
            return
        }

        val utteranceId = "utt_${System.currentTimeMillis()}_${(0..9999).random()}"
        val maxSpeechDuration = (text.length * 350L + 2000L).coerceAtLeast(3000L)

        withTimeoutOrNull(maxSpeechDuration) {
            suspendCancellableCoroutine<Unit> { continuation ->
                val listener = object : UtteranceProgressListener() {
                    override fun onStart(uttId: String?) {}

                    override fun onDone(uttId: String?) {
                        if (uttId == utteranceId && continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(uttId: String?) {
                        if (uttId == utteranceId && continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    override fun onError(uttId: String?, errorCode: Int) {
                        if (uttId == utteranceId && continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    override fun onRangeStart(uttId: String?, start: Int, end: Int, frame: Int) {
                        if (uttId == utteranceId) {
                            lastSpokenCharOffset = initialOffset + start
                        }
                    }
                }

                tts?.setOnUtteranceProgressListener(listener)
                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                }

                val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
                if (result == TextToSpeech.ERROR && continuation.isActive) {
                    continuation.resume(Unit)
                }

                continuation.invokeOnCancellation {
                    tts?.stop()
                }
            }
        }

        delay(pauseAfterMs)
    }

    fun synthesizeMockAudio(text: String, file: java.io.File) {
        if (isTtsReady.value) {
            val params = Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "mock_audio") }
            tts?.synthesizeToFile(text, params, file, "mock_audio")
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }

    // New Game State
    private val _currentStage = MutableStateFlow(GameStage.PREP)
    val currentStage = _currentStage.asStateFlow()

    // V0.5-D Battle states
    private val _currentCombo = MutableStateFlow(0)
    val currentCombo = _currentCombo.asStateFlow()

    private val _maxComboInBattle = MutableStateFlow(0)
    val maxComboInBattle = _maxComboInBattle.asStateFlow()

    private val _correctCount = MutableStateFlow(0)
    val correctCount = _correctCount.asStateFlow()

    private val _almostCount = MutableStateFlow(0)
    val almostCount = _almostCount.asStateFlow()

    private val _wrongCount = MutableStateFlow(0)
    val wrongCount = _wrongCount.asStateFlow()

    private val _currentBattleWords = MutableStateFlow<List<WordItem>>(emptyList())
    val currentBattleWords = _currentBattleWords.asStateFlow()

    private val _currentWordIndex = MutableStateFlow(0)
    val currentWordIndex = _currentWordIndex.asStateFlow()

    private val _currentCharIndex = MutableStateFlow(0)
    val currentCharIndex = _currentCharIndex.asStateFlow()

    private val _currentCharAnswers = MutableStateFlow<List<CharAnswer>>(emptyList())
    val currentCharAnswers = _currentCharAnswers.asStateFlow()

    private val _answers = MutableStateFlow<List<Answer>>(emptyList())
    val answers = _answers.asStateFlow()
    
    // index -> "CORRECT", "ALMOST", "WRONG"
    private val _judgments = MutableStateFlow<Map<Int, String>>(emptyMap())
    val judgments = _judgments.asStateFlow()

    private val _isReviewMode = MutableStateFlow(false)
    
    // 0: Closed, 1: Mock, 2: ML_KIT (手写识别辅助)
    private val _recognitionMode = MutableStateFlow(2)
    val recognitionMode = _recognitionMode.asStateFlow()
    
    fun setRecognitionMode(mode: Int) {
        _recognitionMode.value = mode
    }
    
    private val _timeLeft = MutableStateFlow(30)
    val timeLeft = _timeLeft.asStateFlow()

    private val _isTimerPaused = MutableStateFlow(false)
    val isTimerPaused = _isTimerPaused.asStateFlow()

    fun pauseTimer() {
        _isTimerPaused.value = true
    }

    fun resumeTimer() {
        _isTimerPaused.value = false
        if (_playCount.value == 0) {
            startAutoPlay()
        }
    }

    fun startAutoPlay() {
        val word = currentBattleWords.value.getOrNull(_currentWordIndex.value) ?: return
        val stats = userStats.value
        autoPlayJob?.cancel()
        tts?.stop()
        autoPlayJob = viewModelScope.launch {
            val targetPlays = stats?.playCount ?: 2
            val textToSpeak = word.getFormattedAudioPrompt()
            for (i in 1..targetPlays) {
                if (!isActive) break
                _playCount.value = i
                speakAndWait(textToSpeak, pauseAfterMs = 1200L)
            }
        }
    }
    
    private val _playCount = MutableStateFlow(0)
    val playCount = _playCount.asStateFlow()
    
    private val _settlementResult = MutableStateFlow<SettlementResult?>(null)
    val settlementResult = _settlementResult.asStateFlow()

    private val _battleProcessResult = MutableStateFlow<com.example.data.GameRepository.BattleProcessResult?>(null)
    val battleProcessResult = _battleProcessResult.asStateFlow()
    
    private val _lastQuestionResults = MutableStateFlow<List<com.example.data.QuestionResult>>(emptyList())
    val lastQuestionResults = _lastQuestionResults.asStateFlow()
    
    private val _levelName = MutableStateFlow("")
    val levelName = _levelName.asStateFlow()

    private val _currentLevel = MutableStateFlow<Level?>(null)
    val currentLevel = _currentLevel.asStateFlow()

    private val _totalLevelWordCount = MutableStateFlow(0)
    val totalLevelWordCount = _totalLevelWordCount.asStateFlow()

    private var timerJob: Job? = null

    fun updateSettings(timePerWord: Int, playCount: Int, allowExtra: Boolean, wordsPerLevel: Int, passRate: Int, gradingMode: String) {
        viewModelScope.launch {
            repository.updateStats { 
                it.copy(
                    timePerWord = timePerWord,
                    playCount = playCount,
                    allowExtraPlay = allowExtra,
                    wordsPerLevel = wordsPerLevel,
                    passRate = passRate,
                    gradingMode = gradingMode
                )
            }
        }
    }

    fun startLevel(unitName: String, lvlName: String) {
        viewModelScope.launch {
            val levelList = allLevels.value.filter { it.unitName == unitName }
            val level = levelList.find { it.name == lvlName } ?: levelList.firstOrNull()
            if (level != null) {
                startLevelById(level.id)
            } else {
                val allWords = repository.getWordsByUnit(unitName)
                _totalLevelWordCount.value = allWords.size
                val limit = userStats.value?.wordsPerLevel ?: 8
                val selected = if (limit > 0 && limit < allWords.size) allWords.take(limit) else allWords
                if (selected.isNotEmpty()) {
                    levelStartedAt = System.currentTimeMillis()
                    _levelName.value = lvlName
                    _currentBattleWords.value = selected
                    _isReviewMode.value = false
                    _answers.value = emptyList()
                    _judgments.value = emptyMap()
                    _battleProcessResult.value = null
                    _currentStage.value = GameStage.PREP
                }
            }
        }
    }
    
    fun startReviewMode() {
        viewModelScope.launch {
            val wordsToReview = wrongWords.value.filter { !it.isMastered }
            if (wordsToReview.isNotEmpty()) {
                levelStartedAt = System.currentTimeMillis()
                val limit = userStats.value?.wordsPerLevel ?: 8
                val selected = wordsToReview.shuffled().take(if (limit > 0) limit else 100)
                val allDbWords = repository.getAllWordsList().associateBy { it.text }
                _currentBattleWords.value = selected.map { wrong ->
                    val fullWord = allDbWords[wrong.text]
                    if (fullWord != null) {
                        fullWord.copy(difficulty = "易错")
                    } else {
                        val rawWord = WordItem(wrong.id, wrong.text, wrong.type, wrong.unitName, "易错")
                        com.example.util.SmartPromptGenerator.generateSmartPrompt(rawWord, com.example.util.SmartPromptGenerator.STRATEGY_SMART_RECOMMEND)
                    }
                }
                _levelName.value = "错题复仇本"
                _isReviewMode.value = true
                _answers.value = emptyList()
                _judgments.value = emptyMap()
                _battleProcessResult.value = null
                _currentStage.value = GameStage.PREP
            }
        }
    }
    
    fun beginDictation() {
        _currentCombo.value = 0
        _maxComboInBattle.value = 0
        _correctCount.value = 0
        _almostCount.value = 0
        _wrongCount.value = 0
        _currentStage.value = GameStage.DICTATION
        _currentWordIndex.value = 0
        setupCurrentWord()
    }
    
    private fun setupCurrentWord() {
        val word = currentBattleWords.value.getOrNull(_currentWordIndex.value) ?: return
        val stats = userStats.value
        _timeLeft.value = stats?.timePerWord ?: 30
        _isTimerPaused.value = true
        _playCount.value = 0
        _currentCharIndex.value = 0
        _currentCharAnswers.value = emptyList()
        
        autoPlayJob?.cancel()
        tts?.stop()
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0) {
                delay(1000)
                if (!_isTimerPaused.value) {
                    _timeLeft.value -= 1
                }
            }
        }

        // For first word (index 0), wait for resumeTimer() from Intro dialog dismiss.
        // For subsequent words, start playing and resume timer immediately.
        if (_currentWordIndex.value > 0) {
            _isTimerPaused.value = false
            startAutoPlay()
        }
    }
    
    fun saveCurrentChar(strokes: List<StrokeData>, canvasWidth: Float, canvasHeight: Float) {
        val word = currentBattleWords.value.getOrNull(_currentWordIndex.value) ?: return
        val targetText = word.getEffectiveTargetAnswer().filter { it.isLetterOrDigit() }
        val charCount = if (targetText.isEmpty()) 1 else targetText.length
        
        val newCharAnswers = _currentCharAnswers.value.toMutableList()
        val expectedChar = targetText.getOrNull(_currentCharIndex.value)?.toString() ?: ""
        
        var recognitionResult: RecognitionResult? = null
        if (_recognitionMode.value == 1) {
            recognitionResult = createRecognitionResult(
                charIndex = _currentCharIndex.value,
                expectedChar = expectedChar,
                candidates = if (strokes.isEmpty()) emptyList() else listOf(expectedChar, "模", "拟"),
                source = RecognitionSource.MOCK
            )
        } else if (_recognitionMode.value == 2 && strokes.isNotEmpty()) {
            recognitionResult = RecognitionResult(
                charIndex = _currentCharIndex.value,
                expectedChar = expectedChar,
                recognizedText = "识别中...",
                candidates = emptyList(),
                confidenceLevel = ConfidenceLevel.UNKNOWN,
                isLikelyCorrect = false,
                source = RecognitionSource.ML_KIT,
                errorMessage = "正在识别..."
            )
            performMlKitRecognition(word.id, _currentCharIndex.value, expectedChar, strokes)
        }
        
        newCharAnswers.add(CharAnswer(
            charIndex = _currentCharIndex.value,
            strokes = strokes,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            isBlank = strokes.isEmpty(),
            recognitionResult = recognitionResult
        ))
        _currentCharAnswers.value = newCharAnswers
        
        _currentCharIndex.value += 1
        
        if (_currentCharIndex.value >= charCount) {
             timerJob?.cancel()
        }
    }

    fun rewriteCurrentWord() {
        _currentCharIndex.value = 0
        _currentCharAnswers.value = emptyList()
    }

    fun replaceCharAnswer(charIndex: Int, strokes: List<StrokeData>, canvasWidth: Float, canvasHeight: Float) {
        val word = currentBattleWords.value.getOrNull(_currentWordIndex.value) ?: return
        val targetText = word.getEffectiveTargetAnswer().filter { it.isLetterOrDigit() }
        
        val newCharAnswers = _currentCharAnswers.value.toMutableList()
        val existingIndex = newCharAnswers.indexOfFirst { it.charIndex == charIndex }
        val expectedChar = targetText.getOrNull(charIndex)?.toString() ?: ""
        
        var recognitionResult: RecognitionResult? = null
        if (_recognitionMode.value == 1) {
             recognitionResult = createRecognitionResult(
                 charIndex = charIndex,
                 expectedChar = expectedChar,
                 candidates = if (strokes.isEmpty()) emptyList() else listOf(expectedChar, "模", "拟"),
                 source = RecognitionSource.MOCK
             )
        } else if (_recognitionMode.value == 2 && strokes.isNotEmpty()) {
             recognitionResult = RecognitionResult(
                 charIndex = charIndex,
                 expectedChar = expectedChar,
                 recognizedText = "识别中...",
                 candidates = emptyList(),
                 confidenceLevel = ConfidenceLevel.UNKNOWN,
                 isLikelyCorrect = false,
                 source = RecognitionSource.ML_KIT,
                 errorMessage = "正在识别..."
             )
             performMlKitRecognition(word.id, charIndex, expectedChar, strokes)
        }
        
        val newAnswer = CharAnswer(
            charIndex = charIndex,
            strokes = strokes,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            isBlank = strokes.isEmpty(),
            recognitionResult = recognitionResult
        )
        
        if (existingIndex != -1) {
            newCharAnswers[existingIndex] = newAnswer
        } else {
            newCharAnswers.add(newAnswer)
        }
        _currentCharAnswers.value = newCharAnswers
    }

    fun submitWordAndNext(currentStrokes: List<StrokeData>? = null, canvasWidth: Float = 1f, canvasHeight: Float = 1f) {
        timerJob?.cancel()
        val word = currentBattleWords.value.getOrNull(_currentWordIndex.value) ?: return
        val targetText = word.getEffectiveTargetAnswer().filter { it.isLetterOrDigit() }
        val charCount = if (targetText.isEmpty()) 1 else targetText.length
        
        val newCharAnswers = _currentCharAnswers.value.toMutableList()
        
        if (currentStrokes != null && _currentCharIndex.value < charCount) {
             val expectedChar = targetText.getOrNull(_currentCharIndex.value)?.toString() ?: ""
             var recognitionResult: RecognitionResult? = null
             if (_recognitionMode.value == 1) {
                 recognitionResult = createRecognitionResult(
                     charIndex = _currentCharIndex.value,
                     expectedChar = expectedChar,
                     candidates = if (currentStrokes.isEmpty()) emptyList() else listOf(expectedChar, "模", "拟"),
                     source = RecognitionSource.MOCK
                 )
             } else if (_recognitionMode.value == 2 && currentStrokes.isNotEmpty()) {
                 recognitionResult = RecognitionResult(
                     charIndex = _currentCharIndex.value,
                     expectedChar = expectedChar,
                     recognizedText = "识别中...",
                     candidates = emptyList(),
                     confidenceLevel = ConfidenceLevel.UNKNOWN,
                     isLikelyCorrect = false,
                     source = RecognitionSource.ML_KIT,
                     errorMessage = "正在识别..."
                 )
                 performMlKitRecognition(word.id, _currentCharIndex.value, expectedChar, currentStrokes)
             }
             newCharAnswers.add(CharAnswer(
                charIndex = _currentCharIndex.value,
                strokes = currentStrokes.toList(),
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                isBlank = currentStrokes.isEmpty(),
                recognitionResult = recognitionResult
             ))
             _currentCharIndex.value += 1
        }
        
        while (newCharAnswers.size < charCount) {
             newCharAnswers.add(CharAnswer(
                charIndex = newCharAnswers.size,
                strokes = emptyList(),
                canvasWidth = 1f,
                canvasHeight = 1f,
                isBlank = true,
                recognitionResult = null
             ))
        }

        
        val newAnswers = _answers.value.toMutableList()
        val submittedAnswer = Answer(word, newCharAnswers, charCount)
        newAnswers.add(submittedAnswer)
        _answers.value = newAnswers
        
        val isAutoMode = userStats.value?.gradingMode == "AUTO"
        if (isAutoMode) {
            val qResult = autoGradeAnswer(submittedAnswer)
            when (qResult.autoResult) {
                "CORRECT" -> {
                    _currentCombo.value += 1
                    _maxComboInBattle.value = maxOf(_maxComboInBattle.value, _currentCombo.value)
                    _correctCount.value += 1
                }
                "PARTIAL", "NEED_REVIEW" -> {
                    _almostCount.value += 1
                }
                "WRONG" -> {
                    _currentCombo.value = 0
                    _wrongCount.value += 1
                }
                else -> {
                    _currentCombo.value = 0
                    _wrongCount.value += 1
                }
            }
        }
        
        if (_currentWordIndex.value + 1 < _currentBattleWords.value.size) {
            _currentWordIndex.value += 1
            setupCurrentWord()
        } else {
            onDictationFinished()
        }
    }

    private fun performMlKitRecognition(wordId: Int, charIndex: Int, expectedChar: String, strokes: List<StrokeData>) {
        if (strokes.isEmpty()) return
        DigitalInkRecognizerManager.recognize(strokes, expectedChar) { candidates, error ->
            val finalResult = if (error != null) {
                createRecognitionResult(
                    charIndex = charIndex,
                    expectedChar = expectedChar,
                    candidates = emptyList(),
                    source = RecognitionSource.ML_KIT,
                    errorMessage = error
                )
            } else {
                createRecognitionResult(
                    charIndex = charIndex,
                    expectedChar = expectedChar,
                    candidates = candidates ?: emptyList(),
                    source = RecognitionSource.ML_KIT
                )
            }
            updateRecognitionResultInState(wordId, charIndex, finalResult)
        }
    }

    private fun updateRecognitionResultInState(wordId: Int, charIndex: Int, result: RecognitionResult) {
        val currentWord = currentBattleWords.value.getOrNull(_currentWordIndex.value)
        if (currentWord != null && currentWord.id == wordId) {
            val currentAnswers = _currentCharAnswers.value.toMutableList()
            val index = currentAnswers.indexOfFirst { it.charIndex == charIndex }
            if (index != -1) {
                currentAnswers[index] = currentAnswers[index].copy(recognitionResult = result)
                _currentCharAnswers.value = currentAnswers
                Log.d("GameViewModel", "Updated in current: word=$wordId index=$charIndex text=${result.recognizedText}")
                return
            }
        }
        
        val submittedAnswers = _answers.value.toMutableList()
        val answerIndex = submittedAnswers.indexOfFirst { it.word.id == wordId }
        if (answerIndex != -1) {
            val ans = submittedAnswers[answerIndex]
            val charAnswers = ans.charAnswers.toMutableList()
            val charIndexInList = charAnswers.indexOfFirst { it.charIndex == charIndex }
            if (charIndexInList != -1) {
                charAnswers[charIndexInList] = charAnswers[charIndexInList].copy(recognitionResult = result)
                submittedAnswers[answerIndex] = ans.copy(charAnswers = charAnswers)
                _answers.value = submittedAnswers
                Log.d("GameViewModel", "Updated in submitted: word=$wordId index=$charIndex text=${result.recognizedText}")
            }
        }
    }
    
    fun playAudioAgain() {
        val word = currentBattleWords.value.getOrNull(_currentWordIndex.value) ?: return
        val stats = userStats.value
        if (stats?.allowExtraPlay == true) {
            val textToSpeak = word.getFormattedAudioPrompt()
            autoPlayJob?.cancel()
            tts?.stop()
            autoPlayJob = viewModelScope.launch {
                _playCount.value += 1
                speakAndWait(textToSpeak, pauseAfterMs = 500L)
            }
        }
    }
    
    fun setJudgment(index: Int, result: String) {
        val newJ = _judgments.value.toMutableMap()
        newJ[index] = result
        _judgments.value = newJ
    }
    
    fun markAllCorrect() {
        val newJ = mutableMapOf<Int, String>()
        for (i in _answers.value.indices) {
            newJ[i] = "CORRECT"
        }
        _judgments.value = newJ
    }
    
    fun submitAcceptance() {
        if (_judgments.value.size < _answers.value.size) return // Not all judged
        
        viewModelScope.launch {
            var correctCount = 0
            var almostCount = 0
            var wrongCount = 0
            var coinsGained = 0
            var expGained = 0
            var newWrongWordsCount = 0
            var bossDefeated = false
            
            for (i in _answers.value.indices) {
                val ans = _answers.value[i]
                val j = _judgments.value[i] ?: "WRONG"
                when (j) {
                    "CORRECT" -> {
                        correctCount++
                        coinsGained += 10
                        expGained += 10
                        if (ans.word.difficulty == "BOSS") {
                            coinsGained += 30
                            expGained += 30
                            bossDefeated = true
                        }
                        if (_isReviewMode.value) {
                            repository.updateWrongWordCorrect(ans.word.text)
                            incrementQuestProgress("error_purify")
                        }
                    }
                    "ALMOST" -> {
                        almostCount++
                        coinsGained += 3
                        expGained += 3
                        newWrongWordsCount++
                        repository.recordWrongWord(ans.word.text, ans.word.type, ans.word.unitName, "轻度")
                    }
                    "WRONG" -> {
                        wrongCount++
                        newWrongWordsCount++
                        repository.recordWrongWord(ans.word.text, ans.word.type, ans.word.unitName, "重点")
                    }
                }
            }
            
            val total = _answers.value.size
            val passRate = userStats.value?.passRate ?: 80
            val actualRate = if (total > 0) (correctCount * 100) / total else 0
            val isClear = actualRate >= passRate
            
            if (isClear) {
                coinsGained += 100
            }
            if (actualRate >= 90) {
                coinsGained += 50
            }
            if (actualRate == 100) {
                coinsGained += 100
            }
            
            repository.updateStats { stats ->
                stats.copy(
                    coins = stats.coins + coinsGained,
                    experience = stats.experience + expGained,
                    totalAnswered = stats.totalAnswered + total,
                    correctCount = stats.correctCount + correctCount,
                    wrongCount = stats.wrongCount + almostCount + wrongCount,
                    dailyPracticeCount = stats.dailyPracticeCount + 1
                )
            }
            gainHatchEnergy(10)
            
            if (isClear && !_isReviewMode.value) {
                _answers.value.lastOrNull()?.word?.unitName?.let {
                    repository.unlockNextLevel(it, _levelName.value)
                }
            }
            
            val finishedAt = System.currentTimeMillis()
            val gradingModeStr = userStats.value?.gradingMode ?: "ASSISTED"
            
            val questionResults = mutableListOf<com.example.data.QuestionResult>()
            var autoCorrectCount = 0
            var autoPartialCount = 0
            var autoWrongCount = 0
            var autoNeedReviewCount = 0
            
            for (ans in _answers.value) {
                val autoResultObj = autoGradeAnswer(ans)
                when (autoResultObj.autoResult) {
                    "CORRECT" -> autoCorrectCount++
                    "PARTIAL" -> autoPartialCount++
                    "WRONG" -> autoWrongCount++
                    "NEED_REVIEW" -> autoNeedReviewCount++
                }
                
                val ansIndex = _answers.value.indexOf(ans)
                val j = _judgments.value[ansIndex] ?: "WRONG"
                val manualResult = when (j) {
                    "CORRECT" -> "CORRECT"
                    "ALMOST" -> "PARTIAL"
                    else -> "WRONG"
                }
                
                questionResults.add(autoResultObj.copy(
                    finalResult = manualResult,
                    parentOverrideResult = manualResult,
                    parentReviewedAt = finishedAt
                ))
            }
            
            val session = com.example.data.PracticeSession(
                levelId = 0,
                levelName = _levelName.value,
                startedAt = levelStartedAt,
                finishedAt = finishedAt,
                gradingMode = gradingModeStr,
                totalQuestions = total,
                autoCorrectCount = autoCorrectCount,
                partialCount = autoPartialCount,
                wrongCount = autoWrongCount,
                needReviewCount = autoNeedReviewCount,
                autoAccuracy = if (total > 0) (autoCorrectCount.toDouble() / total) * 100 else 0.0,
                finalAccuracy = actualRate.toDouble(),
                rewardCoins = coinsGained,
                rewardExp = expGained,
                isPassed = isClear,
                reviewStatus = "REVIEWED",
                questionResultsJson = com.example.data.PracticeSessionConverters.fromQuestionResultsListStatic(questionResults)
            )
            val newSessionId = repository.insertSession(session)
            currentSessionId = newSessionId.toInt()
            incrementQuestProgress("dictation")
            
            if (autoCorrectCount == total && total > 0 && gradingModeStr == "AUTO") {
                gainHatchEnergy(5)
            }
            
            _lastQuestionResults.value = questionResults
            
            var currentCombo = 0
            var computedMaxCombo = 0
            for (i in _answers.value.indices) {
                val j = _judgments.value[i] ?: "WRONG"
                when (j) {
                    "CORRECT" -> {
                        currentCombo++
                        if (currentCombo > computedMaxCombo) {
                            computedMaxCombo = currentCombo
                        }
                    }
                    "ALMOST" -> {
                        // stays
                    }
                    else -> {
                        currentCombo = 0
                    }
                }
            }
            _maxComboInBattle.value = computedMaxCombo

            val intimacyGained = when {
                computedMaxCombo >= 5 -> 5
                computedMaxCombo >= 3 -> 2
                else -> 0
            }
            if (intimacyGained > 0) {
                repository.addPetIntimacy(intimacyGained)
            }

            val title = if (actualRate == 100) "完美通关！" else if (isClear) "通关成功！" else "挑战失败！"
            
            _settlementResult.value = SettlementResult(
                title = title,
                isClear = isClear,
                totalWords = total,
                correctCount = correctCount,
                almostCount = almostCount,
                wrongCount = wrongCount,
                coinsGained = coinsGained,
                expGained = expGained,
                newWrongWordsCount = newWrongWordsCount,
                isBossDefeated = bossDefeated,
                intimacyGained = intimacyGained,
                maxCombo = computedMaxCombo
            )

            repository.addPlayerCoins(coinsGained)
            repository.addPlayerExp(expGained)
            
            val currentProfile = repository.getPlayerProfileDirect()
            if (currentProfile != null) {
                val acc = if (total > 0) correctCount.toDouble() / total else 0.0
                val battleResult = repository.processBattleResult(
                    accountId = currentProfile.accountId,
                    playerId = currentProfile.id,
                    levelId = 0,
                    levelName = _levelName.value,
                    questionResults = questionResults,
                    finalAccuracy = acc,
                    rewardCoins = coinsGained,
                    rewardExp = expGained,
                    highestCombo = computedMaxCombo
                )
                _battleProcessResult.value = battleResult
            }
            
            _currentStage.value = GameStage.SETTLEMENT
        }
    }
    
    fun onDictationFinished() {
        val mode = userStats.value?.gradingMode ?: "ASSISTED"
        if (mode == "AUTO") {
            val isAvailable = DigitalInkRecognizerManager.modelStatus.value == "已下载" || 
                              DigitalInkRecognizerManager.modelStatus.value == "已预置 (离线免下载)"
            if (!isAvailable) {
                autoGradingToast.value = "请先下载或启用手写识别，已退回“家长验收模式”"
                viewModelScope.launch {
                    repository.updateStats { it.copy(gradingMode = "MANUAL") }
                }
                _currentStage.value = GameStage.ACCEPTANCE
                return
            }
            
            val answersList = _answers.value
            var failedCount = 0
            val questionResults = mutableListOf<com.example.data.QuestionResult>()
            
            for (ans in answersList) {
                val qGraded = autoGradeAnswer(ans)
                questionResults.add(qGraded)
                if (qGraded.autoResult == "NEED_REVIEW") {
                    failedCount++
                }
            }
            
            val total = answersList.size
            val failRatio = if (total > 0) failedCount.toDouble() / total else 0.0
            
            if (failRatio > 0.5) {
                autoGradingToast.value = "超过50%的字词识别失败，本次结果不稳定，建议家长手动验收"
                _currentStage.value = GameStage.ACCEPTANCE
                return
            }
            
            performAutoGradingSettlement(questionResults)
        } else {
            _currentStage.value = GameStage.ACCEPTANCE
        }
    }
    
    fun autoGradeAnswer(answer: Answer): com.example.data.QuestionResult {
        val targetText = answer.word.getEffectiveTargetAnswer().filter { it.isLetterOrDigit() }
        val charResults = mutableListOf<com.example.data.CharResult>()
        
        var correctCharCount = 0
        var likelyCorrectCharCount = 0
        var wrongCharCount = 0
        var blankCharCount = 0
        var failedCharCount = 0
        val reasons = mutableListOf<String>()
        
        for (i in 0 until answer.charCount) {
            val expectedChar = targetText.getOrNull(i)?.toString() ?: ""
            val charAns = answer.charAnswers.getOrNull(i)
            
            val isBlank = charAns?.isBlank ?: true || (charAns?.strokes?.isEmpty() ?: true)
            val rec = charAns?.recognitionResult
            
            val grade: String
            val confidence: String
            val reason: String
            val recognizedText = rec?.recognizedText ?: ""
            val candidates = rec?.candidates ?: emptyList()
            val isLikelyCorrect: Boolean
            val errorMessage = rec?.errorMessage
            
            if (isBlank) {
                grade = "BLANK"
                confidence = "LOW"
                reason = "学生未书写该字"
                isLikelyCorrect = false
                blankCharCount++
                reasons.add(reason)
            } else if (rec == null || rec.recognizedText == "识别中...") {
                grade = "RECOGNITION_FAILED"
                confidence = "UNKNOWN"
                reason = "正在识别中，无最终结果"
                isLikelyCorrect = false
                failedCharCount++
                reasons.add(reason)
            } else if (errorMessage != null && errorMessage != "正在识别...") {
                grade = "RECOGNITION_FAILED"
                confidence = "UNKNOWN"
                reason = "AI识别失败，需要家长复核"
                isLikelyCorrect = false
                failedCharCount++
                reasons.add(reason)
            } else if (expectedChar == recognizedText) {
                grade = "CORRECT"
                confidence = "HIGH"
                reason = "首选识别命中正确字‘$expectedChar’"
                isLikelyCorrect = true
                correctCharCount++
            } else if (candidates.take(3).contains(expectedChar)) {
                grade = "LIKELY_CORRECT"
                confidence = "MEDIUM"
                reason = "目标字‘$expectedChar’出现在候选中，按宽松规则判为可能正确"
                isLikelyCorrect = true
                likelyCorrectCharCount++
                reasons.add(reason)
            } else {
                grade = "WRONG"
                confidence = "LOW"
                reason = if (recognizedText.isNotBlank()) "正确答案是‘$expectedChar’，当前识别首选为‘$recognizedText’" else "目标字‘$expectedChar’未出现在识别候选中"
                isLikelyCorrect = false
                wrongCharCount++
                reasons.add(reason)
            }
            
            val pointsList = charAns?.strokes?.map { stroke ->
                stroke.points.map { pt -> com.example.ui.PointData(pt.x, pt.y, pt.timestamp) }
            } ?: emptyList()
            
            charResults.add(com.example.data.CharResult(
                charIndex = i,
                pointsList = pointsList,
                canvasWidth = charAns?.canvasWidth ?: 1f,
                canvasHeight = charAns?.canvasHeight ?: 1f,
                isBlank = isBlank,
                expectedChar = expectedChar,
                recognizedText = recognizedText,
                candidates = candidates,
                confidenceLevel = confidence,
                isLikelyCorrect = isLikelyCorrect,
                errorMessage = errorMessage
            ))
        }
        
        val autoResult: String
        val needParentReview: Boolean
        
        val totalChars = answer.charCount
        if (correctCharCount == totalChars) {
            autoResult = "CORRECT"
            needParentReview = false
        } else if (correctCharCount + likelyCorrectCharCount == totalChars) {
            autoResult = "CORRECT"
            needParentReview = false
        } else if (blankCharCount > 0) {
            autoResult = "WRONG"
            needParentReview = true
        } else if (failedCharCount > 0) {
            autoResult = "NEED_REVIEW"
            needParentReview = true
        } else if (wrongCharCount > 0 && (correctCharCount > 0 || likelyCorrectCharCount > 0)) {
            autoResult = "PARTIAL"
            needParentReview = true
        } else {
            autoResult = "WRONG"
            needParentReview = true
        }
        
        val finalErrorReason = if (reasons.isEmpty()) "无错误" else reasons.distinct().joinToString("；")

        return com.example.data.QuestionResult(
            questionId = answer.word.id,
            correctText = answer.word.text,
            charResults = charResults,
            autoResult = autoResult,
            finalResult = autoResult,
            needParentReview = needParentReview,
            parentOverrideResult = null,
            parentReviewedAt = null,
            promptMode = answer.word.promptMode,
            hiddenIndicesStr = answer.word.hiddenIndicesStr,
            visiblePrompt = answer.word.getEffectiveVisiblePrompt(),
            ttsPrompt = answer.word.getEffectiveTtsPrompt(),
            targetAnswer = answer.word.getEffectiveTargetAnswer(),
            clueText = answer.word.clueText,
            meaningHint = answer.word.meaningHint,
            errorReason = finalErrorReason
        )
    }
    
    private fun performAutoGradingSettlement(questionResults: List<com.example.data.QuestionResult>) {
        _lastQuestionResults.value = questionResults
        viewModelScope.launch {
            var correctCount = 0
            var partialCount = 0
            var wrongCount = 0
            var needReviewCount = 0
            
            var coinsGained = 0
            var expGained = 0
            var bossDefeated = false
            
            for (q in questionResults) {
                val ans = _answers.value.find { it.word.id == q.questionId }
                val isBoss = ans?.word?.difficulty == "BOSS"
                
                when (q.autoResult) {
                    "CORRECT" -> {
                        correctCount++
                        coinsGained += 10
                        expGained += 10
                        if (isBoss) {
                            coinsGained += 30
                            expGained += 30
                            bossDefeated = true
                        }
                        if (_isReviewMode.value && ans != null) {
                            repository.updateWrongWordCorrect(ans.word.text)
                            incrementQuestProgress("error_purify")
                        }
                    }
                    "PARTIAL" -> {
                        partialCount++
                        coinsGained += 3
                        expGained += 3
                        if (ans != null) {
                            repository.recordWrongWord(ans.word.text, ans.word.type, ans.word.unitName, "轻度")
                        }
                    }
                    "WRONG" -> {
                        wrongCount++
                        if (ans != null) {
                            repository.recordWrongWord(ans.word.text, ans.word.type, ans.word.unitName, "重点")
                        }
                    }
                    "NEED_REVIEW" -> {
                        needReviewCount++
                        coinsGained += 2
                        expGained += 2
                    }
                }
            }
            
            val total = _answers.value.size
            val passRate = userStats.value?.passRate ?: 80
            val actualRate = if (total > 0) (correctCount * 100) / total else 0
            val isClear = actualRate >= passRate
            
            if (isClear) {
                coinsGained += 100
            }
            if (actualRate >= 90) {
                coinsGained += 50
            }
            if (actualRate == 100) {
                coinsGained += 100
            }
            
            val hasPracticeHint = _answers.value.any { it.word.visibilityPolicy == "PRACTICE_HINT" }
            if (hasPracticeHint) {
                coinsGained = 0
                expGained = 0
            }

            if (!hasPracticeHint) {
                repository.updateStats { stats ->
                    stats.copy(
                        coins = stats.coins + coinsGained,
                        experience = stats.experience + expGained,
                        totalAnswered = stats.totalAnswered + total,
                        correctCount = stats.correctCount + correctCount,
                        wrongCount = stats.wrongCount + partialCount + wrongCount,
                        dailyPracticeCount = stats.dailyPracticeCount + 1
                    )
                }
                gainHatchEnergy(10)

                if (isClear && !_isReviewMode.value) {
                    _answers.value.lastOrNull()?.word?.unitName?.let {
                        repository.unlockNextLevel(it, _levelName.value)
                    }
                }
            }
            
            val finishedAt = System.currentTimeMillis()
            val gradingModeStr = "AUTO"
            
            val session = com.example.data.PracticeSession(
                levelId = 0,
                levelName = _levelName.value,
                startedAt = levelStartedAt,
                finishedAt = finishedAt,
                gradingMode = gradingModeStr,
                totalQuestions = total,
                autoCorrectCount = correctCount,
                partialCount = partialCount,
                wrongCount = wrongCount,
                needReviewCount = needReviewCount,
                autoAccuracy = actualRate.toDouble(),
                finalAccuracy = actualRate.toDouble(),
                rewardCoins = coinsGained,
                rewardExp = expGained,
                isPassed = isClear,
                reviewStatus = if (needReviewCount > 0 || partialCount > 0 || wrongCount > 0) "NEED_PARENT_REVIEW" else "NOT_NEEDED",
                questionResultsJson = com.example.data.PracticeSessionConverters.fromQuestionResultsListStatic(questionResults)
            )
            val newSessionId = repository.insertSession(session)
            currentSessionId = newSessionId.toInt()
            incrementQuestProgress("dictation")
            
            val intimacyGained = when {
                _maxComboInBattle.value >= 5 -> 5
                _maxComboInBattle.value >= 3 -> 2
                else -> 0
            }
            if (intimacyGained > 0) {
                repository.addPetIntimacy(intimacyGained)
            }
            
            val title = if (actualRate == 100) "完美通关！" else if (isClear) "通关成功！" else "挑战失败！"
            
            _settlementResult.value = SettlementResult(
                title = title,
                isClear = isClear,
                totalWords = total,
                correctCount = correctCount,
                almostCount = partialCount,
                wrongCount = wrongCount,
                coinsGained = coinsGained,
                expGained = expGained,
                newWrongWordsCount = partialCount + wrongCount,
                isBossDefeated = bossDefeated,
                isAutoGraded = true,
                intimacyGained = intimacyGained,
                maxCombo = _maxComboInBattle.value
            )

            repository.addPlayerCoins(coinsGained)
            repository.addPlayerExp(expGained)
            
            val currentProfile = repository.getPlayerProfileDirect()
            if (currentProfile != null) {
                val acc = if (total > 0) correctCount.toDouble() / total else 0.0
                val battleResult = repository.processBattleResult(
                    accountId = currentProfile.accountId,
                    playerId = currentProfile.id,
                    levelId = 0,
                    levelName = _levelName.value,
                    questionResults = questionResults,
                    finalAccuracy = acc,
                    rewardCoins = coinsGained,
                    rewardExp = expGained,
                    highestCombo = _maxComboInBattle.value
                )
                _battleProcessResult.value = battleResult
            }
            
            _currentStage.value = GameStage.SETTLEMENT
        }
    }

    fun updateLastQuestionResult(questionId: Int, newResult: String) {
        val current = _lastQuestionResults.value.toMutableList()
        val index = current.indexOfFirst { it.questionId == questionId }
        if (index != -1) {
            val oldQ = current[index]
            if (oldQ.finalResult == newResult) return
            val updatedQ = oldQ.copy(
                finalResult = newResult,
                parentOverrideResult = newResult,
                parentReviewedAt = System.currentTimeMillis()
            )
            current[index] = updatedQ
            _lastQuestionResults.value = current

            val total = current.size
            val correctCount = current.count { it.finalResult == "CORRECT" }
            val almostCount = current.count { it.finalResult == "PARTIAL" }
            val wrongCount = current.count { it.finalResult == "WRONG" }
            val oldSettlement = _settlementResult.value
            if (oldSettlement != null) {
                val title = if (correctCount == total) "完美通关！" else "结算完成"
                _settlementResult.value = oldSettlement.copy(
                    title = title,
                    correctCount = correctCount,
                    almostCount = almostCount,
                    wrongCount = wrongCount,
                    newWrongWordsCount = almostCount + wrongCount
                )
            }

            viewModelScope.launch {
                val text = oldQ.correctText
                if (newResult == "CORRECT") {
                    repository.deleteWrongWordByText(text)
                    gainHatchEnergy(8)
                } else if (newResult == "WRONG" || newResult == "PARTIAL") {
                    val wordItem = repository.allWords.firstOrNull()?.find { it.text == text }
                    val type = wordItem?.type ?: "词语"
                    val unitName = wordItem?.unitName ?: _levelName.value
                    val errorLevel = if (newResult == "WRONG") "重点" else "轻度"
                    repository.recordWrongWord(text, type, unitName, errorLevel)
                }

                if (currentSessionId != -1) {
                    overrideSessionResult(currentSessionId, questionId, newResult)
                }
            }
        }
    }
    
    fun overrideSessionResult(sessionId: Int, questionId: Int, newResult: String) {
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId) ?: return@launch
            val list = com.example.data.PracticeSessionConverters.toQuestionResultsListStatic(session.questionResultsJson).toMutableList()
            val qIndex = list.indexOfFirst { it.questionId == questionId }
            if (qIndex != -1) {
                val qResult = list[qIndex]
                val oldResult = qResult.finalResult
                if (oldResult == newResult) return@launch
                
                val updatedQ = qResult.copy(
                    finalResult = newResult,
                    parentOverrideResult = newResult,
                    parentReviewedAt = System.currentTimeMillis()
                )
                list[qIndex] = updatedQ
                
                val finalCorrectCount = list.count { it.finalResult == "CORRECT" }
                val finalAccuracy = (finalCorrectCount.toDouble() / list.size) * 100
                
                val stillNeedsReview = list.any { it.needParentReview && it.parentOverrideResult == null }
                val newReviewStatus = if (stillNeedsReview) "NEED_PARENT_REVIEW" else "REVIEWED"
                
                val text = qResult.correctText
                if (newResult == "CORRECT") {
                    repository.deleteWrongWordByText(text)
                    gainHatchEnergy(8)
                } else if (newResult == "WRONG" || newResult == "PARTIAL") {
                    val wordItem = repository.allWords.firstOrNull()?.find { it.text == text }
                    val type = wordItem?.type ?: "词语"
                    val unitName = wordItem?.unitName ?: session.levelName
                    val errorLevel = if (newResult == "WRONG") "重点" else "轻度"
                    repository.recordWrongWord(text, type, unitName, errorLevel)
                }
                
                val updatedSession = session.copy(
                    finalAccuracy = finalAccuracy,
                    reviewStatus = newReviewStatus,
                    questionResultsJson = com.example.data.PracticeSessionConverters.fromQuestionResultsListStatic(list)
                )
                repository.updateSession(updatedSession)
            }
        }
    }
    
    fun restartCurrentLevel() {
        if (_isReviewMode.value) {
            startReviewMode()
        } else {
            val unit = _currentBattleWords.value.firstOrNull()?.unitName ?: "第一单元"
            val name = _levelName.value
            // Try to find if we can start by ID first if there's an existing matching level
            viewModelScope.launch {
                val levels = repository.allLevels.firstOrNull() ?: emptyList()
                val lvl = levels.find { it.name == name }
                if (lvl != null) {
                    startLevelById(lvl.id)
                } else {
                    startLevel(unit, name)
                }
            }
        }
    }
    
    fun addCustomWords(textBlock: String, unitName: String, diff: String) {
        viewModelScope.launch {
            val parts = textBlock.split(Regex("[,，、\\s]+")).map { it.trim() }.filter { it.isNotEmpty() }
            val words = parts.map { WordItem(text = it, type = "词语", unitName = unitName, difficulty = diff) }
            repository.addCustomWords(words)
        }
    }

    fun createPlayerProfile(playerName: String, avatarId: Int, petId: String) {
        viewModelScope.launch {
            val newProfile = PlayerProfile(
                playerName = playerName,
                avatarId = avatarId,
                petId = petId,
                level = 1,
                exp = 0,
                coins = 100,
                totalStudyDays = 1,
                streakDays = 1,
                highestCombo = 0
            )
            repository.savePlayerProfile(newProfile)
        }
    }

    fun addPlayerExp(amount: Int) {
        viewModelScope.launch {
            repository.addPlayerExp(amount)
        }
    }

    fun addPlayerCoins(amount: Int) {
        viewModelScope.launch {
            repository.addPlayerCoins(amount)
        }
    }

    fun purchaseItem(itemId: String, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val session = currentSession.value
            val player = playerProfile.value
            if (session != null && player != null) {
                val res = repository.purchaseItem(session.currentAccountId ?: 0L, player.id, itemId)
                onResult(res)
            } else {
                onResult(Result.failure(Exception("请先登录角色账户")))
            }
        }
    }

    fun equipItem(itemId: String, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val player = playerProfile.value
            if (player != null) {
                val res = repository.equipItem(player.id, itemId)
                onResult(res)
            } else {
                onResult(Result.failure(Exception("请先登录角色账户")))
            }
        }
    }

    fun unequipItem(itemId: String, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val player = playerProfile.value
            if (player != null) {
                val res = repository.unequipItem(player.id, itemId)
                onResult(res)
            } else {
                onResult(Result.failure(Exception("请先登录角色账户")))
            }
        }
    }

    fun updateStreak(streak: Int) {
        viewModelScope.launch {
            repository.updateStreak(streak)
        }
    }

    fun startCustomBattle(levelName: String, words: List<com.example.data.WordItem>) {
        _currentLevel.value = null
        _levelName.value = levelName
        _currentBattleWords.value = words
        _totalLevelWordCount.value = words.size
        _maxComboInBattle.value = 0
        _correctCount.value = 0
        _wrongCount.value = 0
        _currentStage.value = GameStage.PREP
    }

    fun startLevelById(levelId: Int) {
        viewModelScope.launch {
            val level = repository.getLevelById(levelId) ?: return@launch
            _currentLevel.value = level

            val allLevelWords = if (!level.wordIdsStr.isNullOrEmpty()) {
                val ids = level.wordIdsStr.split(",").mapNotNull { it.trim().toIntOrNull() }
                val wordMap = repository.getWordsByIds(ids).associateBy { it.id }
                ids.mapNotNull { wordMap[it] }
            } else {
                repository.getWordsByUnit(level.unitName)
            }

            _totalLevelWordCount.value = allLevelWords.size

            val globalLimit = userStats.value?.wordsPerLevel ?: 8
            val effectiveLimit = when (level.practiceLimitMode) {
                "ALL" -> 0
                "FIXED" -> level.fixedQuestionCount ?: 0
                else -> globalLimit
            }

            val selectedWords = if (effectiveLimit > 0 && effectiveLimit < allLevelWords.size) {
                allLevelWords.take(effectiveLimit)
            } else {
                allLevelWords
            }

            if (selectedWords.isNotEmpty()) {
                levelStartedAt = System.currentTimeMillis()
                _levelName.value = level.name
                _currentBattleWords.value = selectedWords
                _isReviewMode.value = false
                _answers.value = emptyList()
                _judgments.value = emptyMap()
                _battleProcessResult.value = null
                _currentStage.value = GameStage.PREP
            }
        }
    }

    suspend fun insertCustomLevel(name: String, unitName: String, wordIds: List<Int>): Long {
        val level = Level(
            name = name,
            unitName = unitName,
            isUnlocked = true,
            isCompleted = false,
            isPreset = false,
            wordIdsStr = wordIds.joinToString(","),
            practiceLimitMode = "ALL"
        )
        return repository.insertLevel(level)
    }

    fun updateLevel(level: Level) {
        viewModelScope.launch {
            repository.updateLevel(level)
        }
    }

    fun deleteLevel(levelId: Int) {
        viewModelScope.launch {
            repository.deleteLevel(levelId)
        }
    }

    fun deleteWord(wordId: Int) {
        viewModelScope.launch {
            repository.deleteWord(wordId)
        }
    }

    fun updateWord(word: WordItem) {
        viewModelScope.launch {
            repository.insertWord(word)
        }
    }

    fun updateAllowStudentViewMeaning(mode: String) {
        viewModelScope.launch {
            val stats = userStats.value ?: UserStats()
            repository.updateUserStats(stats.copy(allowStudentViewMeaning = mode))
        }
    }

    fun batchGeneratePrompts(
        scopeType: String, // FILTERED, UNIT, LESSON, PACK, ALL
        strategy: String,
        forceOverride: Boolean,
        filteredWords: List<WordItem>,
        unitNameFilter: String = "",
        lessonNameFilter: String = "",
        packIdFilter: String = "",
        onCompleted: (BatchPromptStats, List<WordItem>) -> Unit
    ) {
        viewModelScope.launch {
            val allDbWords = repository.getAllWordsList()
            val targetWords = when (scopeType) {
                "FILTERED" -> filteredWords
                "UNIT" -> if (unitNameFilter.isNotBlank()) allDbWords.filter { it.unitName == unitNameFilter } else filteredWords
                "LESSON" -> if (lessonNameFilter.isNotBlank()) allDbWords.filter { it.sourceLesson == lessonNameFilter } else filteredWords
                "PACK" -> if (packIdFilter.isNotBlank()) {
                    val packLevels = allLevels.value.filter { it.sourcePackId == packIdFilter }
                    val levelUnitNames = packLevels.map { it.unitName }.toSet()
                    allDbWords.filter { levelUnitNames.contains(it.unitName) }
                } else filteredWords
                "ALL" -> allDbWords
                else -> filteredWords
            }

            var contextCount = 0
            var clozeCount = 0
            var meaningCount = 0
            var skippedCount = 0
            var missingContextCount = 0
            val missingContextWords = mutableListOf<WordItem>()

            for (word in targetWords) {
                if (word.promptManuallyEdited && !forceOverride) {
                    skippedCount++
                    continue
                }

                val generated = com.example.util.SmartPromptGenerator.generateSmartPrompt(word, strategy, forceOverride)
                repository.insertWord(generated)

                if (generated.promptMode == "CONTEXT_CLUE") contextCount++
                if (generated.promptMode == "CLOZE_CHAR") clozeCount++
                if (generated.meaningHint.isNotBlank()) meaningCount++
                if (generated.promptQuality == "NEED_CONTEXT") {
                    missingContextCount++
                    missingContextWords.add(generated)
                }
            }

            val stats = BatchPromptStats(
                totalProcessed = targetWords.size,
                contextClueCount = contextCount,
                clozeCharCount = clozeCount,
                fullWordCount = targetWords.size - contextCount - clozeCount,
                meaningSupplementedCount = meaningCount,
                manualSkippedCount = skippedCount,
                missingContextCount = missingContextCount,
                missingContextWords = missingContextWords
            )

            onCompleted(stats, missingContextWords)
        }
    }

    suspend fun getWordsByIds(ids: List<Int>): List<WordItem> {
        return repository.getWordsByIds(ids)
    }

    suspend fun addWordToDatabase(word: WordItem): Long {
        val autoProcessed = com.example.util.SmartPromptGenerator.generateSmartPrompt(word, com.example.util.SmartPromptGenerator.STRATEGY_SMART_RECOMMEND)
        return repository.insertWordAndGetId(autoProcessed)
    }

    private fun calculateUnitSortIndex(pack: ContentPack, unit: PackUnit, groupCounters: MutableMap<Int, Int>? = null): Int {
        val unitGroup = when {
            unit.unitName.contains("第一单元") -> 1
            unit.unitName.contains("第二单元") -> 2
            unit.unitName.contains("第三单元") -> 3
            unit.unitName.contains("第四单元") -> 4
            unit.unitName.contains("第五单元") -> 5
            unit.unitName.contains("第六单元") -> 6
            unit.unitName.contains("第七单元") -> 7
            unit.unitName.contains("第八单元") -> 8
            else -> 0
        }
        if (unitGroup > 0) {
            if (groupCounters != null) {
                val count = (groupCounters[unitGroup] ?: 0) + 1
                groupCounters[unitGroup] = count
                return unitGroup * 100 + count
            } else {
                val groupUnits = pack.units.filter { u ->
                    when (unitGroup) {
                        1 -> u.unitName.contains("第一单元")
                        2 -> u.unitName.contains("第二单元")
                        3 -> u.unitName.contains("第三单元")
                        4 -> u.unitName.contains("第四单元")
                        5 -> u.unitName.contains("第五单元")
                        6 -> u.unitName.contains("第六单元")
                        7 -> u.unitName.contains("第七单元")
                        8 -> u.unitName.contains("第八单元")
                        else -> false
                    }
                }
                val idxInGroup = groupUnits.indexOfFirst { it.id == unit.id || it.unitName == unit.unitName }
                val count = if (idxInGroup >= 0) idxInGroup + 1 else 1
                return unitGroup * 100 + count
            }
        }
        return unit.orderIndex
    }

    fun installContentPack(pack: ContentPack, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val prefix = if (pack.sourceType == PackSourceType.ORIGINAL) "五年级通用训练" else pack.name
                val groupCounters = mutableMapOf<Int, Int>()
                for (unit in pack.units) {
                    val cleanUnitName = unit.unitName.split("：").first().trim()
                    val levelName = "$prefix · $cleanUnitName"
                    
                    val existing = allLevels.value.find { it.name == levelName }
                    if (existing != null) continue
                    
                    val calculatedSortIndex = calculateUnitSortIndex(pack, unit, groupCounters)

                    val wordIds = mutableListOf<Int>()
                    for (item in unit.items) {
                        val rawWord = WordItem(
                            text = item.text,
                            type = item.type,
                            unitName = levelName,
                            difficulty = item.difficulty,
                            promptMode = item.promptMode,
                            hiddenIndicesStr = item.hiddenIndicesStr,
                            visiblePrompt = item.visiblePrompt,
                            ttsPrompt = item.ttsPrompt,
                            contextText = item.contextText,
                            targetAnswer = item.targetAnswer,
                            clueText = item.clueText,
                            meaningHint = item.meaningHint,
                            sourceLesson = unit.lessonName ?: ""
                        )
                        val smartWord = com.example.util.SmartPromptGenerator.generateSmartPrompt(rawWord, com.example.util.SmartPromptGenerator.STRATEGY_SMART_RECOMMEND)
                        val wordId = repository.insertWordAndGetId(smartWord)
                        wordIds.add(wordId.toInt())
                    }
                    
                    val level = Level(
                        name = levelName,
                        unitName = levelName,
                        isUnlocked = true,
                        isCompleted = false,
                        isPreset = false,
                        wordIdsStr = wordIds.joinToString(","),
                        sortIndex = calculatedSortIndex,
                        sourcePackId = pack.id,
                        practiceLimitMode = "ALL"
                    )
                    repository.insertLevel(level)
                }
                ContentPackManager.setPackInstalled(getApplication(), pack.id, true)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "安装失败")
            }
        }
    }

    fun generateUnitLevel(pack: ContentPack, unit: PackUnit, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val prefix = if (pack.sourceType == PackSourceType.ORIGINAL) "五年级通用训练" else pack.name
                val cleanUnitName = unit.unitName.split("：").first().trim()
                val levelName = "$prefix · $cleanUnitName"
                
                val existing = allLevels.value.find { it.name == levelName }
                if (existing != null) {
                    onError("该单元关卡已生成")
                    return@launch
                }
                
                val calculatedSortIndex = calculateUnitSortIndex(pack, unit, null)

                val wordIds = mutableListOf<Int>()
                for (item in unit.items) {
                    val rawWord = WordItem(
                        text = item.text,
                        type = item.type,
                        unitName = levelName,
                        difficulty = item.difficulty,
                        promptMode = item.promptMode,
                        hiddenIndicesStr = item.hiddenIndicesStr,
                        visiblePrompt = item.visiblePrompt,
                        ttsPrompt = item.ttsPrompt,
                        contextText = item.contextText,
                        targetAnswer = item.targetAnswer,
                        clueText = item.clueText,
                        meaningHint = item.meaningHint,
                        sourceLesson = unit.lessonName ?: ""
                    )
                    val smartWord = com.example.util.SmartPromptGenerator.generateSmartPrompt(rawWord, com.example.util.SmartPromptGenerator.STRATEGY_SMART_RECOMMEND)
                    val wordId = repository.insertWordAndGetId(smartWord)
                    wordIds.add(wordId.toInt())
                }
                
                val level = Level(
                    name = levelName,
                    unitName = levelName,
                    isUnlocked = true,
                    isCompleted = false,
                    isPreset = false,
                    wordIdsStr = wordIds.joinToString(","),
                    sortIndex = calculatedSortIndex,
                    sourcePackId = pack.id,
                    practiceLimitMode = "ALL"
                )
                repository.insertLevel(level)
                
                val allInstalledNow = pack.units.all { u ->
                    val uCleanName = u.unitName.split("：").first().trim()
                    val uLevelName = "$prefix · $uCleanName"
                    uLevelName == levelName || allLevels.value.any { it.name == uLevelName }
                }
                if (allInstalledNow) {
                    ContentPackManager.setPackInstalled(getApplication(), pack.id, true)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "生成失败")
            }
        }
    }

    fun uninstallContentPack(pack: ContentPack, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val prefix = if (pack.sourceType == PackSourceType.ORIGINAL) "五年级通用训练" else pack.name
                
                val levelsToDelete = allLevels.value.filter { it.name.startsWith("$prefix · ") || it.sourcePackId == pack.id }
                for (lvl in levelsToDelete) {
                    repository.deleteLevel(lvl.id)
                }
                
                val wordsToDelete = allWords.value.filter { it.unitName.startsWith("$prefix · ") }
                for (w in wordsToDelete) {
                    repository.deleteWord(w.id)
                }
                
                ContentPackManager.setPackInstalled(getApplication(), pack.id, false)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun repairInstallContentPack(pack: ContentPack, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val prefix = if (pack.sourceType == PackSourceType.ORIGINAL) "五年级通用训练" else pack.name
                
                // 1. Delete matching levels and words
                val levelsToDelete = allLevels.value.filter { it.name.startsWith("$prefix · ") || it.sourcePackId == pack.id }
                for (lvl in levelsToDelete) {
                    repository.deleteLevel(lvl.id)
                }
                val wordsToDelete = allWords.value.filter { it.unitName.startsWith("$prefix · ") }
                for (w in wordsToDelete) {
                    repository.deleteWord(w.id)
                }
                
                // 2. Re-install
                val groupCounters = mutableMapOf<Int, Int>()
                for (unit in pack.units) {
                    val cleanUnitName = unit.unitName.split("：").first().trim()
                    val levelName = "$prefix · $cleanUnitName"
                    
                    val calculatedSortIndex = calculateUnitSortIndex(pack, unit, groupCounters)

                    val wordIds = mutableListOf<Int>()
                    for (item in unit.items) {
                        val rawWord = WordItem(
                            text = item.text,
                            type = item.type,
                            unitName = levelName,
                            difficulty = item.difficulty,
                            promptMode = item.promptMode,
                            hiddenIndicesStr = item.hiddenIndicesStr,
                            visiblePrompt = item.visiblePrompt,
                            ttsPrompt = item.ttsPrompt,
                            contextText = item.contextText,
                            targetAnswer = item.targetAnswer,
                            clueText = item.clueText,
                            meaningHint = item.meaningHint,
                            sourceLesson = unit.lessonName ?: ""
                        )
                        val smartWord = com.example.util.SmartPromptGenerator.generateSmartPrompt(rawWord, com.example.util.SmartPromptGenerator.STRATEGY_SMART_RECOMMEND)
                        val wordId = repository.insertWordAndGetId(smartWord)
                        wordIds.add(wordId.toInt())
                    }
                    
                    val level = Level(
                        name = levelName,
                        unitName = levelName,
                        isUnlocked = true,
                        isCompleted = false,
                        isPreset = false,
                        wordIdsStr = wordIds.joinToString(","),
                        sortIndex = calculatedSortIndex,
                        sourcePackId = pack.id
                    )
                    repository.insertLevel(level)
                }
                ContentPackManager.setPackInstalled(getApplication(), pack.id, true)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "修复失败")
            }
        }
    }

    fun updateGradeTiers(mathTier: String, englishTier: String) {
        viewModelScope.launch {
            repository.updateStats { 
                it.copy(mathGradeTier = mathTier, englishGradeTier = englishTier) 
            }
        }
    }

    fun checkInHolidayTask(taskId: Long, delta: Int = 1, note: String? = null) {
        viewModelScope.launch {
            repository.checkInHolidayTask(taskId, delta, note)
            incrementQuestProgress("holiday")
            gainHatchEnergy(6)
        }
    }

    fun cancelTodayHolidayCheckIn(taskId: Long) {
        viewModelScope.launch {
            repository.cancelTodayCheckIn(taskId)
        }
    }

    fun updateTaskProgressDirect(taskId: Long, newCount: Int) {
        viewModelScope.launch {
            repository.updateTaskProgressDirect(taskId, newCount)
        }
    }

    fun recordWorkSession(session: HolidayWorkSession, deltaProgress: Int = 1, updateTaskProgress: Boolean = true) {
        viewModelScope.launch {
            repository.recordWorkSession(session, deltaProgress, updateTaskProgress)
            incrementQuestProgress("holiday")
        }
    }

    fun updateWorkSession(session: HolidayWorkSession) {
        viewModelScope.launch {
            repository.updateWorkSession(session)
        }
    }

    fun deleteWorkSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteWorkSession(sessionId)
        }
    }

    fun toggleWorkSessionParentConfirmed(session: HolidayWorkSession) {
        viewModelScope.launch {
            repository.updateWorkSession(session.copy(parentConfirmed = !session.parentConfirmed))
        }
    }

    fun updateHolidayTask(task: HolidayTask) {
        viewModelScope.launch {
            repository.updateHolidayTask(task)
        }
    }

    fun toggleTaskParentConfirmed(task: HolidayTask) {
        viewModelScope.launch {
            val updated = task.copy(isParentConfirmed = !task.isParentConfirmed)
            repository.updateHolidayTask(updated)
        }
    }

    fun installHolidayPack(packId: String) {
        viewModelScope.launch {
            repository.installHolidayPack(packId)
        }
    }

    fun repairHolidayPack(packId: String) {
        viewModelScope.launch {
            repository.repairHolidayPack(packId)
        }
    }

    fun uninstallHolidayPack(packId: String) {
        viewModelScope.launch {
            repository.uninstallHolidayPack(packId)
        }
    }

    fun updateMaterialReciteStatus(materialId: String, status: String, taskId: Long? = null) {
        viewModelScope.launch {
            repository.updateMaterialReciteStatus(materialId, status, taskId)
        }
    }

    fun updateMaterialDictationStatus(materialId: String, status: String, taskId: Long? = null) {
        viewModelScope.launch {
            repository.updateMaterialDictationStatus(materialId, status, taskId)
        }
    }

    fun toggleMaterialParentConfirmed(materialId: String) {
        viewModelScope.launch {
            repository.toggleMaterialParentConfirmed(materialId)
        }
    }

    fun insertRecitationRecord(record: com.example.data.HolidayRecitationRecord) {
        viewModelScope.launch {
            repository.insertRecitationRecord(record)
            incrementQuestProgress("recitation")
            gainHatchEnergy(8)
        }
    }

    fun updateRecitationParentStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateRecitationParentStatus(id, status)
        }
    }

    fun insertDictationRecord(record: com.example.data.HolidayDictationRecord) {
        viewModelScope.launch {
            repository.insertDictationRecord(record)
            incrementQuestProgress("recitation")
            gainHatchEnergy(8)
        }
    }

    fun updateDictationParentStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateDictationParentStatus(id, status)
        }
    }

    fun createAccount(name: String, pin: String? = null, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createAccount(name, pin)
            onSuccess(id)
        }
    }

    fun renameAccount(accountId: Long, newName: String) {
        viewModelScope.launch {
            repository.renameAccount(accountId, newName)
        }
    }

    fun deleteAccount(accountId: Long) {
        viewModelScope.launch {
            repository.deleteAccount(accountId)
        }
    }

    fun selectAccount(accountId: Long) {
        viewModelScope.launch {
            repository.selectAccount(accountId)
        }
    }

    fun createPlayer(accountId: Long, name: String, avatarId: Int, petId: String) {
        viewModelScope.launch {
            repository.createPlayer(accountId, name, avatarId, petId)
        }
    }

    fun selectPlayer(accountId: Long, playerId: Long) {
        viewModelScope.launch {
            repository.selectPlayer(accountId, playerId)
        }
    }

    fun updatePlayerProfile(playerId: Long, name: String, avatarId: Int) {
        viewModelScope.launch {
            repository.updatePlayerProfile(playerId, name, avatarId)
        }
    }

    fun deletePlayer(playerId: Long) {
        viewModelScope.launch {
            repository.deletePlayer(playerId)
        }
    }

    fun bindPet(accountId: Long, playerId: Long, petId: String, petName: String) {
        viewModelScope.launch {
            repository.bindPet(accountId, playerId, petId, petName)
        }
    }

    fun unbindPet(playerId: Long) {
        viewModelScope.launch {
            repository.unbindPet(playerId)
        }
    }

    fun loginAdminTestAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val existingAccount = db.localAccountDao().getAllAccountsDirect().firstOrNull { it.accountName == "管理员测试账号" }
            val accountId = if (existingAccount != null) {
                existingAccount.id
            } else {
                repository.createAccount("管理员测试账号", null)
            }
            
            repository.selectAccount(accountId)
            
            val players = db.playerProfileDao().getProfilesByAccountDirect(accountId)
            val playerId = if (players.isNotEmpty()) {
                players.first().id
            } else {
                repository.createPlayer(accountId, "山神测试官", 1, "xiaotiangu")
                val updatedPlayers = db.playerProfileDao().getProfilesByAccountDirect(accountId)
                updatedPlayers.first().id
            }
            
            repository.selectPlayer(accountId, playerId)
            onSuccess()
        }
    }

    fun updatePlayerProfileDirectly(level: Int, exp: Int, coins: Int, streakDays: Int, totalStudyDays: Int) {
        viewModelScope.launch {
            val session = db.localSessionDao().getSessionDirect() ?: return@launch
            val playerId = session.currentPlayerId ?: return@launch
            val profile = db.playerProfileDao().getProfileById(playerId) ?: return@launch
            db.playerProfileDao().insertOrUpdateProfile(
                profile.copy(
                    level = level,
                    exp = exp,
                    coins = coins,
                    streakDays = streakDays,
                    totalStudyDays = totalStudyDays,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun addCoinsDirectly(amount: Int) {
        viewModelScope.launch {
            val session = db.localSessionDao().getSessionDirect() ?: return@launch
            val playerId = session.currentPlayerId ?: return@launch
            db.playerProfileDao().addPlayerCoins(playerId, amount)
        }
    }

    fun addAllMaterialsAndCoinsDirectly() {
        viewModelScope.launch {
            val session = db.localSessionDao().getSessionDirect() ?: return@launch
            val playerId = session.currentPlayerId ?: return@launch
            val accountId = session.currentAccountId ?: return@launch
            db.playerProfileDao().addPlayerCoins(playerId, 100000)
            val materials = listOf("WORD_SHARD", "BRUSH_SHARD", "COMBO_SHARD", "JINGHUA", "BOSS_SHARD")
            materials.forEach { matId ->
                repository.addLootToInventory(accountId, playerId, matId, 99)
            }
        }
    }

    fun unlockAllShopItemsDirectly() {
        viewModelScope.launch {
            val session = db.localSessionDao().getSessionDirect() ?: return@launch
            val playerId = session.currentPlayerId ?: return@launch
            val accountId = session.currentAccountId ?: return@launch
            com.example.data.ItemDefinition.ALL_ITEMS.forEach { def ->
                if (def.itemType != "MATERIAL") {
                    val invItem = db.playerInventoryDao().getInventoryItem(playerId, def.itemId)
                    if (invItem == null) {
                        db.playerInventoryDao().insertOrUpdateInventoryItem(
                            com.example.data.PlayerInventoryItem(
                                accountId = accountId,
                                playerId = playerId,
                                itemId = def.itemId,
                                itemType = def.itemType,
                                amount = 1,
                                isOwned = true,
                                isEquipped = false
                            )
                        )
                    } else if (!invItem.isOwned) {
                        db.playerInventoryDao().insertOrUpdateInventoryItem(invItem.copy(isOwned = true))
                    }
                }
            }
        }
    }

    fun addLevelDirectly(amount: Int) {
        viewModelScope.launch {
            val session = db.localSessionDao().getSessionDirect() ?: return@launch
            val playerId = session.currentPlayerId ?: return@launch
            val profile = db.playerProfileDao().getProfileById(playerId) ?: return@launch
            val newLevel = (profile.level + amount).coerceAtLeast(1)
            db.playerProfileDao().insertOrUpdateProfile(
                profile.copy(
                    level = newLevel,
                    exp = 0,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun unlockAllAchievements() {
        viewModelScope.launch {
            val session = db.localSessionDao().getSessionDirect() ?: return@launch
            val accountId = session.currentAccountId ?: return@launch
            val playerId = session.currentPlayerId ?: return@launch
            
            val achievementKeys = listOf(
                "first_purification" to Triple("初次净化", "第一次击败并净化任意字词魔物", 1),
                "combo_3" to Triple("连击新手", "在一次讨伐中达成3连击", 3),
                "combo_5" to Triple("连击大师", "在一次讨伐中达成5连击", 5),
                "error_hunter_3" to Triple("错题猎手", "成功净化3个错题魔物", 3),
                "perfect_conquest" to Triple("完美讨伐", "以100%正确率完成一关", 1),
                "codex_collector_10" to Triple("图鉴收藏家", "解锁并净化10个魔物图鉴", 10)
            )
            
            achievementKeys.forEach { (key, info) ->
                val (name, desc, target) = info
                val existing = db.achievementDao().getAchievementByKey(playerId, key)
                val record = com.example.data.AchievementRecord(
                    id = existing?.id ?: 0L,
                    accountId = accountId,
                    playerId = playerId,
                    achievementKey = key,
                    achievementName = name,
                    achievementDesc = desc,
                    progressValue = target,
                    targetValue = target,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis(),
                    rewardCoins = when (key) {
                        "first_purification" -> 50
                        "combo_3" -> 100
                        "combo_5" -> 200
                        "error_hunter_3" -> 150
                        "perfect_conquest" -> 300
                        "codex_collector_10" -> 500
                        else -> 100
                    }
                )
                db.achievementDao().insertOrUpdateAchievement(record)
            }
        }
    }

    fun purifyAllMonsters() {
        viewModelScope.launch {
            val session = db.localSessionDao().getSessionDirect() ?: return@launch
            val accountId = session.currentAccountId ?: return@launch
            val playerId = session.currentPlayerId ?: return@launch
            
            val testMonsters = listOf(
                Triple("饕餮之魂", "BOSS", "LEGEND"),
                Triple("九尾妖狐", "BOSS", "EPIC"),
                Triple("执笔墨客", "POEM_GUARD", "EPIC"),
                Triple("错字之渊", "WRONG_WORD", "RARE"),
                Triple("浮夸之影", "WRONG_WORD", "COMMON"),
                Triple("风物之灵", "POEM_GUARD", "UNCOMMON"),
                Triple("混淆幽灵", "ELITE", "RARE"),
                Triple("急躁魔眼", "ELITE", "COMMON")
            )
            
            testMonsters.forEachIndexed { index, (name, type, rarity) ->
                val key = "monster_dev_${index}"
                val existing = db.monsterCodexDao().getCodexEntry(playerId, key)
                
                val wordFull = when(type) {
                    "BOSS" -> "饕餮盛宴"
                    "POEM_GUARD" -> "千山鸟飞绝"
                    "WRONG_WORD" -> "滥竽充数"
                    else -> "张冠李戴"
                }
                
                val wordMasked = "●●●●"
                
                val entry = com.example.data.MonsterCodexEntry(
                    id = existing?.id ?: 0L,
                    accountId = accountId,
                    playerId = playerId,
                    monsterKey = key,
                    monsterName = name,
                    monsterType = type,
                    rarity = rarity,
                    sourceType = when(type) {
                        "BOSS" -> "领主秘境"
                        "POEM_GUARD" -> "诗词守卫战"
                        "WRONG_WORD" -> "错题库觉醒"
                        else -> "精英遭遇战"
                    },
                    relatedWordMasked = wordMasked,
                    relatedWordFullForParent = wordFull,
                    encounterCount = 5,
                    purifiedCount = 3,
                    bestCombo = 8,
                    bestAccuracy = 1.0,
                    firstEncounterAt = System.currentTimeMillis() - 86400000 * 3,
                    lastEncounterAt = System.currentTimeMillis(),
                    isUnlocked = true,
                    isPurified = true
                )
                db.monsterCodexDao().insertOrUpdateCodexEntry(entry)
            }
        }
    }

    fun resetAllDeveloperData() {
        viewModelScope.launch {
            val session = db.localSessionDao().getSessionDirect() ?: return@launch
            val playerId = session.currentPlayerId ?: return@launch
            val profile = db.playerProfileDao().getProfileById(playerId) ?: return@launch
            
            db.playerProfileDao().insertOrUpdateProfile(
                profile.copy(
                    level = 1,
                    exp = 0,
                    coins = 100,
                    streakDays = 1,
                    totalStudyDays = 1,
                    highestCombo = 0,
                    updatedAt = System.currentTimeMillis()
                )
            )
            
            val achievements = db.achievementDao().getAchievementsForPlayerDirect(playerId)
            achievements.forEach {
                db.achievementDao().insertOrUpdateAchievement(it.copy(isUnlocked = false, progressValue = 0, unlockedAt = null))
            }
            
            val codex = db.monsterCodexDao().getCodexForPlayerDirect(playerId)
            codex.forEach {
                db.monsterCodexDao().insertOrUpdateCodexEntry(it.copy(isPurified = false, purifiedCount = 0, encounterCount = 0, bestCombo = 0))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
