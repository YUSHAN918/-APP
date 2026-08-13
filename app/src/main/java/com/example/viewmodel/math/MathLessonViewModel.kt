package com.example.viewmodel.math

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.math.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MathLessonViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MathLessonUiState())
    val uiState: StateFlow<MathLessonUiState> = _uiState.asStateFlow()

    fun loadLesson(context: Context, unitId: String, lessonId: String) {
        loadLesson(context, "math_pep_g6_s1", unitId, lessonId)
    }

    fun loadLesson(context: Context, courseId: String, unitId: String, lessonId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, debugInfo = null) }
        viewModelScope.launch {
            try {
                val unit = MathContentLoader.loadUnit(context, courseId, unitId)
                if (unit == null) {
                    val debugText = "courseId: $courseId\nunitId: $unitId\nlessonId: $lessonId\n文件路径: math/pep/grade6/semester1/unit_01.json (或未映射)\n错误原因: 单元 $unitId 无法加载"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "课程内容加载失败",
                            debugInfo = debugText
                        )
                    }
                    return@launch
                }
                val lesson = unit.lessons.find { it.lessonId == lessonId }
                if (lesson != null) {
                    // Compatibility mode: if contentBlocks is empty, convert legacy questions to blocks
                    val blocks = if (lesson.contentBlocks.isNotEmpty()) {
                        lesson.contentBlocks
                    } else {
                        lesson.questions.mapIndexed { idx, q ->
                            MathContentBlock(
                                blockId = "compat_${q.id}_$idx",
                                type = MathContentBlockType.INDEPENDENT_PRACTICE,
                                title = "练习题 ${idx + 1}",
                                question = q
                            )
                        }
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lesson = lesson,
                            blocks = blocks,
                            currentBlockIndex = 0,
                            currentExampleStepIndex = 0,
                            history = emptyList(),
                            isFinished = false,
                            errorMessage = null,
                            debugInfo = null
                        )
                    }
                    resetQuestionState()
                } else {
                    val debugText = "courseId: $courseId\nunitId: $unitId\nlessonId: $lessonId\n文件路径: math/pep/grade6/semester1/unit_01.json\n错误原因: 课时 $lessonId 未找到"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lesson = null,
                            blocks = emptyList(),
                            errorMessage = "课程内容加载失败",
                            debugInfo = debugText
                        )
                    }
                }
            } catch (e: Exception) {
                val debugText = "courseId: $courseId\nunitId: $unitId\nlessonId: $lessonId\n异常信息: ${e.localizedMessage}"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lesson = null,
                        blocks = emptyList(),
                        errorMessage = "课程内容加载失败",
                        debugInfo = debugText
                    )
                }
            }
        }
    }

    private fun resetQuestionState() {
        val state = _uiState.value
        val block = state.blocks.getOrNull(state.currentBlockIndex) ?: return
        val question = block.question ?: return

        val initialBlanks = if (question.type == MathQuestionType.FILL_BLANK) {
            // Find how many blanks are in the stem
            val blankCount = "\\[blank\\]".toRegex().findAll(question.stem).count()
            List(if (blankCount > 0) blankCount else 1) { "" }
        } else {
            emptyList()
        }

        _uiState.update {
            it.copy(
                userAnswerText = "",
                userNumeratorText = "",
                userDenominatorText = "",
                userRatioLeftText = "",
                userRatioRightText = "",
                blankAnswers = initialBlanks,
                selectedChoice = "",
                isNumeratorFocused = true,
                isRatioLeftFocused = true,
                isSubmitted = false,
                evaluationResult = null,
                tryCount = 0,
                showExplanation = false,
                interactionState = MathQuestionInteractionState.EDITING,
                showHintLevel = 0,
                isAnswerRevealed = false
            )
        }
    }

    fun onKeyPress(key: String) {
        val state = _uiState.value
        val block = state.blocks.getOrNull(state.currentBlockIndex) ?: return
        val question = block.question ?: return

        when (question.type) {
            MathQuestionType.FRACTION_INPUT -> {
                if (state.isNumeratorFocused) {
                    val current = state.userNumeratorText
                    val updated = handleInputKey(current, key)
                    _uiState.update { it.copy(userNumeratorText = updated) }
                } else {
                    val current = state.userDenominatorText
                    val updated = handleInputKey(current, key)
                    _uiState.update { it.copy(userDenominatorText = updated) }
                }
            }
            MathQuestionType.RATIO_INPUT -> {
                if (state.isRatioLeftFocused) {
                    val current = state.userRatioLeftText
                    val updated = handleInputKey(current, key)
                    _uiState.update { it.copy(userRatioLeftText = updated) }
                } else {
                    val current = state.userRatioRightText
                    val updated = handleInputKey(current, key)
                    _uiState.update { it.copy(userRatioRightText = updated) }
                }
            }
            MathQuestionType.FILL_BLANK -> {
                val currentBlanks = state.blankAnswers.toMutableList()
                val targetIndex = state.activeBlankIndex
                if (targetIndex in currentBlanks.indices) {
                    currentBlanks[targetIndex] = handleInputKey(currentBlanks[targetIndex], key)
                    _uiState.update { it.copy(blankAnswers = currentBlanks) }
                }
            }
            else -> {
                val current = state.userAnswerText
                val updated = handleInputKey(current, key)
                _uiState.update { it.copy(userAnswerText = updated) }
            }
        }
    }

    private fun handleInputKey(current: String, key: String): String {
        return if (key == "-") {
            if (current.startsWith("-")) current.substring(1) else "-$current"
        } else if (key == ".") {
            if (current.contains(".")) current else "$current."
        } else {
            current + key
        }
    }

    fun onDelete() {
        _uiState.update { state ->
            val block = state.blocks.getOrNull(state.currentBlockIndex) ?: return@update state
            val question = block.question ?: return@update state

            when (question.type) {
                MathQuestionType.FRACTION_INPUT -> {
                    if (state.isNumeratorFocused) {
                        val current = state.userNumeratorText
                        val updated = if (current.isNotEmpty()) current.dropLast(1) else ""
                        state.copy(userNumeratorText = updated)
                    } else {
                        val current = state.userDenominatorText
                        val updated = if (current.isNotEmpty()) current.dropLast(1) else ""
                        state.copy(userDenominatorText = updated)
                    }
                }
                MathQuestionType.RATIO_INPUT -> {
                    if (state.isRatioLeftFocused) {
                        val current = state.userRatioLeftText
                        val updated = if (current.isNotEmpty()) current.dropLast(1) else ""
                        state.copy(userRatioLeftText = updated)
                    } else {
                        val current = state.userRatioRightText
                        val updated = if (current.isNotEmpty()) current.dropLast(1) else ""
                        state.copy(userRatioRightText = updated)
                    }
                }
                MathQuestionType.FILL_BLANK -> {
                    val currentBlanks = state.blankAnswers.toMutableList()
                    val targetIndex = state.activeBlankIndex
                    if (targetIndex in currentBlanks.indices) {
                        val currentText = currentBlanks[targetIndex]
                        currentBlanks[targetIndex] = if (currentText.isNotEmpty()) currentText.dropLast(1) else ""
                    }
                    state.copy(blankAnswers = currentBlanks)
                }
                else -> {
                    val current = state.userAnswerText
                    val updated = if (current.isNotEmpty()) current.dropLast(1) else ""
                    state.copy(userAnswerText = updated)
                }
            }
        }
    }

    fun onClear() {
        _uiState.update { state ->
            val block = state.blocks.getOrNull(state.currentBlockIndex) ?: return@update state
            val question = block.question ?: return@update state

            when (question.type) {
                MathQuestionType.FRACTION_INPUT -> {
                    if (state.isNumeratorFocused) {
                        state.copy(userNumeratorText = "")
                    } else {
                        state.copy(userDenominatorText = "")
                    }
                }
                MathQuestionType.RATIO_INPUT -> {
                    if (state.isRatioLeftFocused) {
                        state.copy(userRatioLeftText = "")
                    } else {
                        state.copy(userRatioRightText = "")
                    }
                }
                MathQuestionType.FILL_BLANK -> {
                    state.copy(blankAnswers = List(state.blankAnswers.size) { "" }, activeBlankIndex = 0)
                }
                else -> {
                    state.copy(userAnswerText = "")
                }
            }
        }
    }

    fun selectChoice(optionLetter: String) {
        _uiState.update { it.copy(selectedChoice = optionLetter) }
    }

    fun setNumeratorFocused(focused: Boolean) {
        _uiState.update { it.copy(isNumeratorFocused = focused) }
    }

    fun setRatioLeftFocused(focused: Boolean) {
        _uiState.update { it.copy(isRatioLeftFocused = focused) }
    }

    fun selectBlank(index: Int) {
        _uiState.update { state ->
            if (index in state.blankAnswers.indices) {
                state.copy(activeBlankIndex = index)
            } else {
                state
            }
        }
    }

    fun updateBlankAnswer(index: Int, text: String) {
        _uiState.update { state ->
            val currentBlanks = state.blankAnswers.toMutableList()
            if (index in currentBlanks.indices) {
                currentBlanks[index] = text
            }
            val nextIndex = if (index + 1 in currentBlanks.indices) index + 1 else index
            state.copy(blankAnswers = currentBlanks, activeBlankIndex = nextIndex)
        }
    }

    fun appendUnit(unitStr: String) {
        _uiState.update { state ->
            // Append unit directly to numerical answer text
            val current = state.userAnswerText
            // Filter digits and decimals
            val regex = "([\\d.-]*)(.*)".toRegex()
            val match = regex.matchEntire(current)
            val numericPart = match?.groups?.get(1)?.value ?: ""
            state.copy(userAnswerText = numericPart + unitStr)
        }
    }

    fun submitAnswer() {
        val state = _uiState.value
        if (state.interactionState == MathQuestionInteractionState.CHECKING ||
            state.interactionState == MathQuestionInteractionState.CORRECT ||
            state.interactionState == MathQuestionInteractionState.ANSWER_REVEALED) {
            return
        }

        val block = state.blocks.getOrNull(state.currentBlockIndex) ?: return
        val question = block.question ?: return

        // Instantly set state to CHECKING to prevent duplicate clicks
        _uiState.update { it.copy(interactionState = MathQuestionInteractionState.CHECKING) }

        val answersList = when (question.type) {
            MathQuestionType.FRACTION_INPUT -> {
                val num = state.userNumeratorText.ifEmpty { "0" }
                val den = state.userDenominatorText.ifEmpty { "1" }
                listOf("$num/$den")
            }
            MathQuestionType.RATIO_INPUT -> {
                listOf(state.userRatioLeftText, state.userRatioRightText)
            }
            MathQuestionType.FILL_BLANK -> {
                state.blankAnswers
            }
            MathQuestionType.MULTIPLE_CHOICE -> {
                listOf(state.selectedChoice)
            }
            else -> {
                listOf(state.userAnswerText)
            }
        }

        val result = MathAnswerEvaluator.evaluate(question.answerSpec, answersList)
        val isCorrect = result is MathEvaluationResult.Correct

        _uiState.update {
            val newTryCount = it.tryCount + 1
            it.copy(
                isSubmitted = true,
                evaluationResult = result,
                tryCount = newTryCount,
                showExplanation = isCorrect || newTryCount >= 2,
                interactionState = if (isCorrect) MathQuestionInteractionState.CORRECT else MathQuestionInteractionState.INCORRECT
            )
        }
    }

    private fun saveCurrentBlockState(state: MathLessonUiState): MathLessonUiState {
        val currentIdx = state.currentBlockIndex
        val currentBlock = state.blocks.getOrNull(currentIdx) ?: return state
        
        val blockState = BlockState(
            userAnswerText = state.userAnswerText,
            userNumeratorText = state.userNumeratorText,
            userDenominatorText = state.userDenominatorText,
            userRatioLeftText = state.userRatioLeftText,
            userRatioRightText = state.userRatioRightText,
            blankAnswers = state.blankAnswers,
            activeBlankIndex = state.activeBlankIndex,
            selectedChoice = state.selectedChoice,
            isNumeratorFocused = state.isNumeratorFocused,
            isRatioLeftFocused = state.isRatioLeftFocused,
            isSubmitted = state.isSubmitted,
            evaluationResult = state.evaluationResult,
            tryCount = state.tryCount,
            showExplanation = state.showExplanation,
            currentExampleStepIndex = state.currentExampleStepIndex,
            interactionState = state.interactionState,
            showHintLevel = state.showHintLevel,
            isAnswerRevealed = state.isAnswerRevealed
        )
        val updatedStates = state.blockStates.toMutableMap()
        updatedStates[currentIdx] = blockState
        return state.copy(blockStates = updatedStates)
    }

    private fun restoreBlockState(state: MathLessonUiState, targetIndex: Int): MathLessonUiState {
        val block = state.blocks.getOrNull(targetIndex) ?: return state
        val question = block.question
        val saved = state.blockStates[targetIndex]
        
        if (saved != null) {
            return state.copy(
                currentBlockIndex = targetIndex,
                currentExampleStepIndex = saved.currentExampleStepIndex,
                userAnswerText = saved.userAnswerText,
                userNumeratorText = saved.userNumeratorText,
                userDenominatorText = saved.userDenominatorText,
                userRatioLeftText = saved.userRatioLeftText,
                userRatioRightText = saved.userRatioRightText,
                blankAnswers = saved.blankAnswers,
                activeBlankIndex = saved.activeBlankIndex,
                selectedChoice = saved.selectedChoice,
                isNumeratorFocused = saved.isNumeratorFocused,
                isRatioLeftFocused = saved.isRatioLeftFocused,
                isSubmitted = saved.isSubmitted,
                evaluationResult = saved.evaluationResult,
                tryCount = saved.tryCount,
                showExplanation = saved.showExplanation,
                interactionState = saved.interactionState,
                showHintLevel = saved.showHintLevel,
                isAnswerRevealed = saved.isAnswerRevealed
            )
        } else {
            val initialBlanks = if (question?.type == MathQuestionType.FILL_BLANK) {
                val blankCount = "\\[blank\\]".toRegex().findAll(question.stem).count()
                List(if (blankCount > 0) blankCount else 1) { "" }
            } else {
                emptyList()
            }
            return state.copy(
                currentBlockIndex = targetIndex,
                currentExampleStepIndex = 0,
                userAnswerText = "",
                userNumeratorText = "",
                userDenominatorText = "",
                userRatioLeftText = "",
                userRatioRightText = "",
                blankAnswers = initialBlanks,
                activeBlankIndex = 0,
                selectedChoice = "",
                isNumeratorFocused = true,
                isRatioLeftFocused = true,
                isSubmitted = false,
                evaluationResult = null,
                tryCount = 0,
                showExplanation = false,
                interactionState = MathQuestionInteractionState.EDITING,
                showHintLevel = 0,
                isAnswerRevealed = false
            )
        }
    }

    fun goToPreviousBlock() {
        val state = _uiState.value
        if (state.currentBlockIndex > 0) {
            val withSaved = saveCurrentBlockState(state)
            val prevIndex = state.currentBlockIndex - 1
            _uiState.value = restoreBlockState(withSaved, prevIndex)
        }
    }

    fun goToNextBlock() {
        val state = _uiState.value
        val block = state.blocks.getOrNull(state.currentBlockIndex) ?: return
        val question = block.question

        val totalSteps = block.steps?.size ?: 0
        if (block.type == MathContentBlockType.WORKED_EXAMPLE && state.currentExampleStepIndex + 1 < totalSteps) {
            _uiState.update {
                it.copy(
                    currentExampleStepIndex = state.currentExampleStepIndex + 1
                )
            }
            return
        }

        var withSaved = saveCurrentBlockState(state)

        val updatedHistory = if (question != null && state.isSubmitted) {
            val answersList = when (question.type) {
                MathQuestionType.FRACTION_INPUT -> {
                    listOf("${state.userNumeratorText}/${state.userDenominatorText}")
                }
                MathQuestionType.RATIO_INPUT -> {
                    listOf(state.userRatioLeftText, state.userRatioRightText)
                }
                MathQuestionType.FILL_BLANK -> {
                    state.blankAnswers
                }
                MathQuestionType.MULTIPLE_CHOICE -> {
                    listOf(state.selectedChoice)
                }
                else -> {
                    listOf(state.userAnswerText)
                }
            }

            val record = MathQuestionRecord(
                question = question,
                userAnswers = answersList,
                isCorrect = state.interactionState == MathQuestionInteractionState.CORRECT && !state.isAnswerRevealed,
                evaluationResult = state.evaluationResult ?: MathEvaluationResult.Incorrect("未作答"),
                tryCount = state.tryCount
            )
            withSaved.history.filter { it.question.id != question.id } + record
        } else {
            withSaved.history
        }

        withSaved = withSaved.copy(history = updatedHistory)

        if (state.currentBlockIndex + 1 < state.blocks.size) {
            val nextIndex = state.currentBlockIndex + 1
            _uiState.value = restoreBlockState(withSaved, nextIndex)
        } else {
            val correctCount = updatedHistory.count { it.isCorrect }
            val earned = 50 + (correctCount * 10)

            _uiState.update {
                it.copy(
                    isFinished = true,
                    history = updatedHistory,
                    earnedCoins = earned
                )
            }
        }
    }

    fun nextQuestion() {
        goToNextBlock()
    }

    fun retryQuestion() {
        _uiState.update { state ->
            val focusNumerator = if (state.userNumeratorText.isEmpty()) {
                true
            } else if (state.userDenominatorText.isEmpty()) {
                false
            } else {
                state.isNumeratorFocused
            }
            val focusRatioLeft = if (state.userRatioLeftText.isEmpty()) {
                true
            } else if (state.userRatioRightText.isEmpty()) {
                false
            } else {
                state.isRatioLeftFocused
            }
            state.copy(
                isSubmitted = false,
                evaluationResult = null,
                interactionState = MathQuestionInteractionState.EDITING,
                isNumeratorFocused = focusNumerator,
                isRatioLeftFocused = focusRatioLeft
            )
        }
    }

    fun clearAndResetQuestion() {
        _uiState.update { state ->
            val block = state.blocks.getOrNull(state.currentBlockIndex) ?: return@update state
            val question = block.question ?: return@update state
            val initialBlanks = if (question.type == MathQuestionType.FILL_BLANK) {
                val blankCount = "\\[blank\\]".toRegex().findAll(question.stem).count()
                List(if (blankCount > 0) blankCount else 1) { "" }
            } else {
                emptyList()
            }
            state.copy(
                userAnswerText = "",
                userNumeratorText = "",
                userDenominatorText = "",
                userRatioLeftText = "",
                userRatioRightText = "",
                blankAnswers = initialBlanks,
                selectedChoice = "",
                isNumeratorFocused = true,
                isRatioLeftFocused = true,
                isSubmitted = false,
                evaluationResult = null,
                interactionState = MathQuestionInteractionState.EDITING
            )
        }
    }

    fun showNextHint() {
        _uiState.update { state ->
            val block = state.blocks.getOrNull(state.currentBlockIndex) ?: return@update state
            val question = block.question ?: return@update state
            val totalHints = question.hints.size
            val nextHintLevel = (state.showHintLevel + 1).coerceAtMost(totalHints)
            state.copy(showHintLevel = nextHintLevel)
        }
    }

    fun revealAnswer() {
        _uiState.update { state ->
            state.copy(
                interactionState = MathQuestionInteractionState.ANSWER_REVEALED,
                isAnswerRevealed = true,
                isSubmitted = true,
                showExplanation = true
            )
        }
    }

    fun reDoQuestionAfterReveal() {
        _uiState.update { state ->
            val block = state.blocks.getOrNull(state.currentBlockIndex) ?: return@update state
            val question = block.question ?: return@update state
            val initialBlanks = if (question.type == MathQuestionType.FILL_BLANK) {
                val blankCount = "\\[blank\\]".toRegex().findAll(question.stem).count()
                List(if (blankCount > 0) blankCount else 1) { "" }
            } else {
                emptyList()
            }
            state.copy(
                userAnswerText = "",
                userNumeratorText = "",
                userDenominatorText = "",
                userRatioLeftText = "",
                userRatioRightText = "",
                blankAnswers = initialBlanks,
                selectedChoice = "",
                isNumeratorFocused = true,
                isRatioLeftFocused = true,
                isSubmitted = false,
                evaluationResult = null,
                interactionState = MathQuestionInteractionState.EDITING,
                isAnswerRevealed = true // retain this flag so they cannot abuse it for scores
            )
        }
    }
}
