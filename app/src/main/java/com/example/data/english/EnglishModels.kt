package com.example.data.english

import com.squareup.moshi.JsonClass

enum class EnglishSkill {
    LISTEN, SPEAK, READ, SPELL, WRITE
}

@JsonClass(generateAdapter = true)
data class EnglishCourseManifest(
    val courseId: String,
    val subject: String,
    val grade: Int,
    val semester: Int,
    val textbookVersion: String,
    val publisher: String,
    val title: String,
    val units: List<EnglishUnitSummary>,
    val editionId: String = "pep_2012_2013",
    val startingGrade: Int = 3
)

@JsonClass(generateAdapter = true)
data class EnglishUnitSummary(
    val unitId: String = "",
    val contentId: String = "",
    val contentType: String = "UNIT", // "UNIT" or "RECYCLE"
    val title: String,
    val subtitle: String = "",
    val order: Int,
    val description: String = "",
    val assetFile: String = "",
    val contentStatus: String = "READY",
    val coveredUnitIds: List<String> = emptyList(),
    val textbookPages: String = "",
    val themeId: String = "rainbow_carnival"
) {
    val realId: String get() = contentId.ifEmpty { unitId }
    val isRecycle: Boolean get() = contentType.uppercase() == "RECYCLE" || realId.contains("recycle")

    val unitNumber: Int? get() {
        if (isRecycle) return null
        val match = Regex("""_u(\d+)$""", RegexOption.IGNORE_CASE).find(unitId)
            ?: Regex("""Unit\s*(\d+)""", RegexOption.IGNORE_CASE).find(title)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    val unitDisplayTag: String get() {
        if (isRecycle) return "REC"
        val num = unitNumber
        return if (num != null) "U$num" else "U$order"
    }
}

@JsonClass(generateAdapter = true)
data class EnglishUnit(
    val unitId: String,
    val title: String,
    val order: Int,
    val words: List<EnglishWord>,
    val expressions: List<EnglishExpression> = emptyList()
)

@JsonClass(generateAdapter = true)
data class EnglishWord(
    val wordId: String,
    val spelling: String,
    val displayText: String,
    val chineseMeaning: String,
    val partOfSpeech: String = "",
    val phonetic: String = "",
    val syllables: String = "",
    val phonicsHint: String = "",
    val audioAssetPath: String = "",
    val imageAssetPath: String = "",
    val exampleSentence: String = "",
    val exampleTranslation: String = "",
    val exampleAudioAssetPath: String = "",
    val requiredSkills: List<EnglishSkill> = emptyList(),
    val difficulty: Int = 1,
    val sourceReference: String = "",
    val contentStatus: String = "NOT_STARTED", // "NOT_STARTED" / "SOURCE_COLLECTED" / "SOURCE_VERIFIED" / "IMPLEMENTED" / "RUNTIME_VERIFIED" / "PRODUCTION_READY"
    val requirementLevel: String = "LISTEN_SPEAK_RECOGNIZE", // "LISTEN_SPEAK_RECOGNIZE" / "LISTEN_SPEAK_ONLY"
    val textbookPage: String = "",
    val audioSource: String = "ANDROID_TTS" // "OFFICIAL_LICENSED" / "LOCAL_RECORDED" / "ANDROID_TTS" / "PLACEHOLDER"
)

@JsonClass(generateAdapter = true)
data class EnglishExpression(
    val expressionId: String,
    val englishText: String,
    val chineseTranslation: String,
    val textbookPage: String = "",
    val sourceReference: String = "",
    val derivedPractice: Boolean = false,
    val contentStatus: String = "NOT_STARTED"
)
