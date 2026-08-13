package com.example.data.english

enum class AutoDictationWordScope {
    ALL,
    CORE_ONLY,
    EXTENDED_ONLY
}

enum class AutoDictationOrder {
    SHUFFLED,
    ORIGINAL
}

enum class AutoDictationFailureCode {
    EMPTY_WORD_POOL,
    AUDIO_ENGINE_UNAVAILABLE,
    AUDIO_PLAYBACK_FAILED,
    INVALID_SETTINGS,
    SESSION_CANCELLED,
    LIFECYCLE_INTERRUPTED
}

data class EnglishAutoDictationSettings(
    val wordScope: AutoDictationWordScope = AutoDictationWordScope.ALL,
    val repeatCount: Int = 2,
    val intervalSeconds: Int = 5,
    val order: AutoDictationOrder = AutoDictationOrder.SHUFFLED,
    val preStartCountdownSeconds: Int = 3
) {
    fun clamped(): EnglishAutoDictationSettings {
        return copy(
            repeatCount = repeatCount.coerceIn(1, 5),
            intervalSeconds = intervalSeconds.coerceIn(2, 30),
            preStartCountdownSeconds = preStartCountdownSeconds.coerceIn(0, 10)
        )
    }
}

data class EnglishAutoDictationItem(
    val wordId: String,
    val lexicalKey: String,
    val spokenText: String,
    val standardDisplayText: String,
    val requirementLevel: String,
    val participation: EnglishChallengeParticipation,
    val word: EnglishWord,
    val isExtended: Boolean
)

sealed interface AutoDictationUiState {
    data object Idle : AutoDictationUiState

    data class Countdown(
        val remainingSeconds: Int,
        val totalWords: Int
    ) : AutoDictationUiState

    data class Playing(
        val currentIndex: Int,
        val totalWords: Int,
        val currentRepeat: Int,
        val totalRepeats: Int,
        val intervalRemainingSeconds: Int?,
        val paused: Boolean,
        val currentItem: EnglishAutoDictationItem
    ) : AutoDictationUiState

    data class Paused(
        val currentIndex: Int,
        val totalWords: Int,
        val currentRepeat: Int,
        val totalRepeats: Int,
        val currentItem: EnglishAutoDictationItem
    ) : AutoDictationUiState

    data class Completed(
        val totalWords: Int,
        val totalPlayedCount: Int,
        val elapsedSeconds: Long,
        val failedWordIds: List<String>,
        val itemsPlayed: List<EnglishAutoDictationItem>
    ) : AutoDictationUiState

    data class Error(
        val failureCode: AutoDictationFailureCode,
        val message: String?
    ) : AutoDictationUiState
}
