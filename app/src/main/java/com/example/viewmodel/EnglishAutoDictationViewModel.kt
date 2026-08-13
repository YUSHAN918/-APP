package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.english.AutoDictationFailureCode
import com.example.data.english.AutoDictationUiState
import com.example.data.english.EnglishAutoDictationItem
import com.example.data.english.EnglishAutoDictationPoolBuilder
import com.example.data.english.EnglishAutoDictationSettings
import com.example.data.english.EnglishPreferenceStore
import com.example.data.english.EnglishUnit
import com.example.util.english.EnglishTTSHelper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EnglishAutoDictationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AutoDictationUiState>(AutoDictationUiState.Idle)
    val uiState: StateFlow<AutoDictationUiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow(EnglishAutoDictationSettings())
    val settings: StateFlow<EnglishAutoDictationSettings> = _settings.asStateFlow()

    private var dictationJob: Job? = null
    private var ttsHelper: EnglishTTSHelper? = null

    private var currentItems = listOf<EnglishAutoDictationItem>()
    private var currentIndex = 0
    private var currentRepeat = 1
    private var isPaused = false
    private var startTimeMillis = 0L
    private var totalPlayedCount = 0
    private val failedWordIds = mutableListOf<String>()

    fun loadSettings(context: Context) {
        val loaded = EnglishPreferenceStore.getAutoDictationSettings(context)
        _settings.value = loaded
    }

    fun updateSettings(context: Context, newSettings: EnglishAutoDictationSettings) {
        val clamped = newSettings.clamped()
        _settings.value = clamped
        EnglishPreferenceStore.saveAutoDictationSettings(context, clamped)
    }

    fun startDictation(
        context: Context,
        unit: EnglishUnit,
        customSettings: EnglishAutoDictationSettings? = null,
        tts: EnglishTTSHelper? = null
    ) {
        stopAndEnd()

        val activeSettings = (customSettings ?: _settings.value).clamped()
        _settings.value = activeSettings
        EnglishPreferenceStore.saveAutoDictationSettings(context, activeSettings)

        ttsHelper = tts ?: EnglishTTSHelper(context)

        currentItems = EnglishAutoDictationPoolBuilder.buildDictationPool(unit, activeSettings)
        if (currentItems.isEmpty()) {
            _uiState.value = AutoDictationUiState.Error(
                failureCode = AutoDictationFailureCode.EMPTY_WORD_POOL,
                message = "当前选定的范围内没有可用于听写的词汇"
            )
            return
        }

        currentIndex = 0
        currentRepeat = 1
        isPaused = false
        startTimeMillis = System.currentTimeMillis()
        totalPlayedCount = 0
        failedWordIds.clear()

        dictationJob = viewModelScope.launch {
            runDictationLoop(activeSettings)
        }
    }

    private suspend fun runDictationLoop(activeSettings: EnglishAutoDictationSettings) {
        // Countdown phase if preStartCountdownSeconds > 0
        if (activeSettings.preStartCountdownSeconds > 0) {
            for (sec in activeSettings.preStartCountdownSeconds downTo 1) {
                while (isPaused) {
                    delay(200)
                }
                _uiState.value = AutoDictationUiState.Countdown(
                    remainingSeconds = sec,
                    totalWords = currentItems.size
                )
                delay(1000)
            }
        }

        while (currentIndex < currentItems.size) {
            val item = currentItems[currentIndex]

            // Play word repetitions
            while (currentRepeat <= activeSettings.repeatCount) {
                while (isPaused) {
                    _uiState.value = AutoDictationUiState.Paused(
                        currentIndex = currentIndex,
                        totalWords = currentItems.size,
                        currentRepeat = currentRepeat,
                        totalRepeats = activeSettings.repeatCount,
                        currentItem = item
                    )
                    delay(200)
                }

                _uiState.value = AutoDictationUiState.Playing(
                    currentIndex = currentIndex,
                    totalWords = currentItems.size,
                    currentRepeat = currentRepeat,
                    totalRepeats = activeSettings.repeatCount,
                    intervalRemainingSeconds = null,
                    paused = false,
                    currentItem = item
                )

                // Play audio with TTS
                val success = playWordAudio(item.spokenText)
                if (!success) {
                    if (!failedWordIds.contains(item.wordId)) {
                        failedWordIds.add(item.wordId)
                    }
                }
                totalPlayedCount++

                currentRepeat++

                if (currentRepeat <= activeSettings.repeatCount) {
                    // Short intra-word pause between repetitions (800ms)
                    var intraPause = 800
                    while (intraPause > 0) {
                        while (isPaused) {
                            delay(200)
                        }
                        delay(100)
                        intraPause -= 100
                    }
                }
            }

            // Word completed, reset repeat for next word
            currentRepeat = 1

            // Interval countdown phase before next word
            if (currentIndex < currentItems.size - 1) {
                for (sec in activeSettings.intervalSeconds downTo 1) {
                    while (isPaused) {
                        _uiState.value = AutoDictationUiState.Paused(
                            currentIndex = currentIndex,
                            totalWords = currentItems.size,
                            currentRepeat = activeSettings.repeatCount,
                            totalRepeats = activeSettings.repeatCount,
                            currentItem = item
                        )
                        delay(200)
                    }

                    _uiState.value = AutoDictationUiState.Playing(
                        currentIndex = currentIndex,
                        totalWords = currentItems.size,
                        currentRepeat = activeSettings.repeatCount,
                        totalRepeats = activeSettings.repeatCount,
                        intervalRemainingSeconds = sec,
                        paused = false,
                        currentItem = item
                    )
                    delay(1000)
                }
            }

            currentIndex++
        }

        // All completed!
        val elapsedSec = ((System.currentTimeMillis() - startTimeMillis) / 1000).coerceAtLeast(1)
        _uiState.value = AutoDictationUiState.Completed(
            totalWords = currentItems.size,
            totalPlayedCount = totalPlayedCount,
            elapsedSeconds = elapsedSec,
            failedWordIds = failedWordIds.toList(),
            itemsPlayed = currentItems
        )
    }

    private suspend fun playWordAudio(text: String): Boolean {
        ttsHelper?.stop()
        val deferred = CompletableDeferred<Boolean>()

        ttsHelper?.speakWithCompletion(text) {
            if (!deferred.isCompleted) {
                deferred.complete(true)
            }
        }

        // Safety fallback timeout: max 3.5 seconds
        viewModelScope.launch {
            delay(3500)
            if (!deferred.isCompleted) {
                deferred.complete(false)
            }
        }

        return deferred.await()
    }

    fun pause() {
        if (_uiState.value is AutoDictationUiState.Playing || _uiState.value is AutoDictationUiState.Countdown) {
            isPaused = true
            ttsHelper?.stop()
            val state = _uiState.value
            if (state is AutoDictationUiState.Playing) {
                _uiState.value = AutoDictationUiState.Paused(
                    currentIndex = state.currentIndex,
                    totalWords = state.totalWords,
                    currentRepeat = state.currentRepeat,
                    totalRepeats = state.totalRepeats,
                    currentItem = state.currentItem
                )
            }
        }
    }

    fun resume() {
        if (isPaused) {
            isPaused = false
        }
    }

    fun replayCurrent() {
        ttsHelper?.stop()
        currentRepeat = 1
        isPaused = false
    }

    fun skipNext() {
        ttsHelper?.stop()
        if (currentIndex < currentItems.size - 1) {
            currentIndex++
            currentRepeat = 1
            isPaused = false
        } else {
            // Reached end
            val elapsedSec = ((System.currentTimeMillis() - startTimeMillis) / 1000).coerceAtLeast(1)
            _uiState.value = AutoDictationUiState.Completed(
                totalWords = currentItems.size,
                totalPlayedCount = totalPlayedCount,
                elapsedSeconds = elapsedSec,
                failedWordIds = failedWordIds.toList(),
                itemsPlayed = currentItems
            )
        }
    }

    fun onAppBackgrounded() {
        pause()
    }

    fun stopAndEnd() {
        ttsHelper?.stop()
        dictationJob?.cancel()
        dictationJob = null
        isPaused = false
        _uiState.value = AutoDictationUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        stopAndEnd()
        ttsHelper = null
    }
}
