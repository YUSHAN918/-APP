package com.example.data.english

import com.squareup.moshi.JsonClass

enum class EnglishContentType {
    UNIT,
    RECYCLE
}

enum class RecycleMissionType {
    STORY_REHEARSAL,
    LISTEN_AND_COLOUR,
    BOARD_GAME,
    MATCH_WRITE_READ,
    SONG_REVIEW_CHECKPOINT
}

@JsonClass(generateAdapter = true)
data class EnglishRecycleContent(
    val recycleId: String,
    val title: String,
    val subtitle: String,
    val coveredUnitIds: List<String>,
    val textbookPages: String,
    val sourceReference: String,
    val missions: List<EnglishRecycleMission>,
    val contentVersion: String = "1.0"
)

@JsonClass(generateAdapter = true)
data class EnglishRecycleMission(
    val missionId: String,
    val title: String,
    val description: String,
    val missionType: String, // "STORY_REHEARSAL" / "LISTEN_AND_COLOUR" / "BOARD_GAME" / "MATCH_WRITE_READ" / "SONG_REVIEW_CHECKPOINT"
    val order: Int,
    val textbookPage: String,
    val sourceReference: String,
    val instruction: String,
    val estimatedMinutes: Int = 5,
    val completionRule: String = "AUTO"
)

data class EnglishRecycleProgress(
    val recycleId: String,
    val completedMissionIds: Set<String> = emptySet(),
    val currentMissionId: String = "",
    val boardPosition: Int = 0,
    val boardRandomSeed: Long = 12345L,
    val rewardClaimed: Boolean = false,
    val completedAt: Long? = null
)

data class ReviewQuestionItem(
    val id: String,
    val sourceUnitId: String,
    val promptText: String,
    val promptTranslation: String,
    val promptAudio: String = "",
    val questionType: String, // "LISTEN_MEANING", "COLOR_IDENTIFY", "BODY_ACTION", "LETTER_MATCH", "EXPRESSION_CHOICE"
    val options: List<String>,
    val correctIndex: Int,
    val sourceWordId: String? = null,
    val sourceExpressionId: String? = null,
    val explanation: String = "",
    val textbookDerived: Boolean = false,
    val sourceReference: String = "",
    val generatedPractice: Boolean = false
)
