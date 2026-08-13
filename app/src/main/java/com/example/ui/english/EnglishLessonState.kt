package com.example.ui.english

import com.example.ui.PointData

enum class DictationMode {
    HANDWRITING,
    KEYBOARD
}

enum class LessonStageType(val title: String, val subtitle: String) {
    INTRO("导学展示", "听发音，看图意，跟读词汇"),
    LISTEN_MEANING("听音辨意", "听英文发音，选择正确的中文释义"),
    READ_ALOUD("大声朗读", "朗读英文词汇，检验发音准确度"),
    PLAYBACK("录音回放", "听自己的朗读发音并对比标准原音"),
    SPELL("拼写拼图", "点击字母气泡组装正确的单词拼写"),
    WRITE("笔迹手写", "在四线三格中手写英文字母进行书写练习"),
    DICTATION("听写默写", "听英文发音，默写出正确的单词")
}

data class EnglishLessonNavigationState(
    val currentWordIndex: Int = 0,
    val currentStageIndex: Int = 0,
    val selectedAnswers: Map<String, Int> = emptyMap(),
    val spellingOrders: Map<Int, List<Int>> = emptyMap(),
    val typedAnswers: Map<String, String> = emptyMap(),
    val handwritingStrokes: Map<String, List<List<PointData>>> = emptyMap(),
    val recordingPaths: Map<Int, String?> = emptyMap(),
    val completedStages: Set<String> = emptySet(),
    val feedbackStates: Map<String, Boolean?> = emptyMap()
)

data class EnglishChallengeNavigationState(
    val currentStageIndex: Int = 0,
    val currentQuestionIndex: Int = 0,
    val dictationMode: DictationMode = DictationMode.HANDWRITING,
    val submittedQuestions: Set<String> = emptySet(),
    val keyboardDrafts: Map<String, String> = emptyMap(),
    val handwritingDrafts: Map<String, List<List<PointData>>> = emptyMap(),
    val answerRevealedSet: Set<String> = emptySet(),
    val stageScores: Map<Int, Int> = emptyMap(),
    val isCompleted: Boolean = false
)
