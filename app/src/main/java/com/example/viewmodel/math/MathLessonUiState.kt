package com.example.viewmodel.math

import com.example.data.math.MathLesson
import com.example.data.math.MathQuestion
import com.example.data.math.MathContentBlock
import com.example.data.math.MathEvaluationResult

data class MathQuestionRecord(
    val question: MathQuestion,
    val userAnswers: List<String>,
    val isCorrect: Boolean,
    val evaluationResult: MathEvaluationResult,
    val tryCount: Int
)

enum class MathQuestionInteractionState {
    EDITING,
    CHECKING,
    INCORRECT,
    CORRECT,
    ANSWER_REVEALED
}

data class BlockState(
    val userAnswerText: String = "",
    val userNumeratorText: String = "",
    val userDenominatorText: String = "",
    val userRatioLeftText: String = "",
    val userRatioRightText: String = "",
    val blankAnswers: List<String> = emptyList(),
    val activeBlankIndex: Int = 0,
    val selectedChoice: String = "",
    val isNumeratorFocused: Boolean = true,
    val isRatioLeftFocused: Boolean = true,
    val isSubmitted: Boolean = false,
    val evaluationResult: MathEvaluationResult? = null,
    val tryCount: Int = 0,
    val showExplanation: Boolean = false,
    val currentExampleStepIndex: Int = 0,
    val interactionState: MathQuestionInteractionState = MathQuestionInteractionState.EDITING,
    val showHintLevel: Int = 0,
    val isAnswerRevealed: Boolean = false
)

data class MathLessonUiState(
    val isLoading: Boolean = true,
    val lesson: MathLesson? = null,
    val blocks: List<MathContentBlock> = emptyList(),
    val currentBlockIndex: Int = 0,
    val currentExampleStepIndex: Int = 0, // For WORKED_EXAMPLE step-by-step display
    // Active answer states
    val userAnswerText: String = "",
    val userNumeratorText: String = "",
    val userDenominatorText: String = "",
    val userRatioLeftText: String = "",
    val userRatioRightText: String = "",
    val blankAnswers: List<String> = emptyList(),
    val activeBlankIndex: Int = 0,
    val selectedChoice: String = "",
    val isNumeratorFocused: Boolean = true, // for Fraction
    val isRatioLeftFocused: Boolean = true, // for Ratio
    // Evaluation states
    val isSubmitted: Boolean = false,
    val evaluationResult: MathEvaluationResult? = null,
    val tryCount: Int = 0,
    val showExplanation: Boolean = false,
    // Final completion states
    val isFinished: Boolean = false,
    val history: List<MathQuestionRecord> = emptyList(),
    val earnedCoins: Int = 0,
    val errorMessage: String? = null,
    val debugInfo: String? = null,
    val blockStates: Map<Int, BlockState> = emptyMap(),
    // Added fields for unified answer interactions
    val interactionState: MathQuestionInteractionState = MathQuestionInteractionState.EDITING,
    val showHintLevel: Int = 0,
    val isAnswerRevealed: Boolean = false
)
