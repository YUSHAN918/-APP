package com.example.ui

sealed interface DictationFeedbackState {
    object Idle : DictationFeedbackState
    object Submitting : DictationFeedbackState
    data class Correct(val isLast: Boolean) : DictationFeedbackState
    data class NeedsImprovement(val reason: String) : DictationFeedbackState
    data class Wrong(val reason: String) : DictationFeedbackState
    object RecognitionFailed : DictationFeedbackState
}
