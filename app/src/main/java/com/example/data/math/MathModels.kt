package com.example.data.math

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MathCourseManifest(
    val courseId: String,
    val subject: String,
    val grade: Int,
    val semester: Int,
    val textbookVersion: String,
    val publisher: String,
    val title: String,
    val units: List<MathUnitSummary>
)

@JsonClass(generateAdapter = true)
data class MathUnitSummary(
    val unitId: String,
    val title: String,
    val order: Int,
    val description: String,
    val knowledgePoints: List<String>,
    val sourceReference: String
)

@JsonClass(generateAdapter = true)
data class MathUnit(
    val unitId: String,
    val title: String,
    val order: Int,
    val description: String,
    val lessons: List<MathLesson>
)

enum class MathLessonPurpose {
    FORMAL,
    ENGINE_TEST
}

@JsonClass(generateAdapter = true)
data class MathLesson(
    val lessonId: String,
    val title: String,
    val order: Int,
    val objective: String,
    val estimatedMinutes: Int,
    val sourceReference: String,
    val contentBlocks: List<MathContentBlock> = emptyList(),
    val questions: List<MathQuestion> = emptyList(), // kept for backward compatibility with test lessons
    val purpose: MathLessonPurpose = MathLessonPurpose.FORMAL,
    val isDeveloperOnly: Boolean = false,
    val isTestLesson: Boolean = false
) {
    fun isFormalLesson(): Boolean = purpose == MathLessonPurpose.FORMAL && !isDeveloperOnly && !isTestLesson
    fun isEngineTestLesson(): Boolean = purpose == MathLessonPurpose.ENGINE_TEST || isDeveloperOnly || isTestLesson
}

enum class MathContentBlockType {
    LESSON_INTRO,
    CONCEPT,
    VISUAL_EXPLANATION,
    WORKED_EXAMPLE,
    GUIDED_PRACTICE,
    INDEPENDENT_PRACTICE,
    CHECKPOINT,
    SUMMARY
}

@JsonClass(generateAdapter = true)
data class MathContentBlock(
    val blockId: String,
    val type: MathContentBlockType,
    val title: String,
    val contentText: String? = null,              // Brief text, e.g. Concept description (<= 80 chars)
    val steps: List<String>? = null,             // Worked example step-by-step
    val question: MathQuestion? = null,           // Nested exercise question if type is practice/checkpoint
    val imageAsset: String? = null
)

enum class MathQuestionType {
    NUMERIC_INPUT,
    FRACTION_INPUT,
    RATIO_INPUT,
    FILL_BLANK,
    MULTIPLE_CHOICE,
    MULTI_SELECT,
    EXPRESSION_INPUT,
    WORD_PROBLEM,
    TRUE_FALSE
}

enum class MathBlankInputType {
    CHOICE_TEXT,
    INTEGER,
    DECIMAL,
    FRACTION,
    NUMERIC_WITH_UNIT
}

@JsonClass(generateAdapter = true)
data class MathBlankSpec(
    val id: String,
    val type: MathBlankInputType,
    val label: String?,
    val acceptedAnswers: List<String>,
    val choices: List<String> = emptyList(),
    val prefix: String? = null,
    val suffix: String? = null
)

@JsonClass(generateAdapter = true)
data class MathAnswerSpec(
    val kind: String, // "INTEGER", "DECIMAL", "FRACTION", "RATIO", "MULTIPLE_BLANKS", "CHOICE", "EXPRESSION", "NUMERIC_WITH_UNIT"
    val expectedValue: String? = null,
    val numerator: Int? = null,
    val denominator: Int? = null,
    val left: String? = null,
    val right: String? = null,
    val requireSimplified: Boolean? = null,
    val requireIntegerTerms: Boolean? = null,
    val allowEquivalentRatio: Boolean? = null,
    val orderMatters: Boolean? = null,
    val expectedValues: List<String>? = null,
    val value: String? = null,
    val acceptedUnits: List<String>? = null,
    val responseTemplate: String? = null,
    val blankSpecs: List<MathBlankSpec>? = null
)

@JsonClass(generateAdapter = true)
data class MathQuestion(
    val id: String,
    val type: MathQuestionType,
    val stem: String,
    @Json(name = "choices") val options: List<String> = emptyList(),
    val explanation: String = "",
    val hints: List<String> = emptyList(),
    val sourceReference: String,
    val contentStatus: String, // "INTERNAL_TEST", "PRODUCTION"
    val answerSpec: MathAnswerSpec,
    val lessonId: String = "",
    val knowledgePoint: String = "",
    val difficulty: String = "MEDIUM",
    val imageAsset: String? = null
)

enum class MathUnitLockReason {
    NONE,
    PREVIOUS_UNIT_NOT_COMPLETED,
    CONTENT_NOT_READY,
    DATA_LOAD_ERROR
}

