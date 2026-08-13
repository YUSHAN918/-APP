package com.example.data.english

enum class EnglishChallengeParticipation {
    CORE_REQUIRED,
    EXTENDED_OPTIONAL,
    EXCLUDED
}

enum class EnglishEvidenceScope {
    CORE_CURRICULUM,
    EXTENDED_PRACTICE,
    PHONICS,
    EXPRESSION
}

data class EnglishUnitChallengePolicy(
    val includeListenSpeakOnlyInRecognition: Boolean = true,
    val includeListenSpeakOnlyInSpelling: Boolean = true,
    val includeListenSpeakOnlyInDictation: Boolean = true,
    val extendedWordsAffectCorePass: Boolean = false,
    val extendedWordsAffectCoreMastery: Boolean = false,
    val extendedWordsRequiredForChallengeCompletion: Boolean = true,
    val showExtendedWordBadges: Boolean = true
)

data class EnglishChallengeWordItem(
    val word: EnglishWord,
    val participation: EnglishChallengeParticipation,
    val sourceUnitId: String,
    val requirementLevel: String
)

data class ChallengeSectionResult(
    val totalCount: Int = 0,
    val meaningScore: Int = 0,
    val reverseScore: Int = 0,
    val spellingScore: Int = 0,
    val dictationScore: Int = 0,
    val passed: Boolean = true,
    val weakWordIds: List<String> = emptyList()
)

data class EnglishChallengeResult(
    val coreResult: ChallengeSectionResult,
    val extendedResult: ChallengeSectionResult,
    val phonicsResult: ChallengeSectionResult? = null,
    val expressionResult: ChallengeSectionResult? = null,
    val overallCompletion: Boolean,
    val corePassed: Boolean
)

object EnglishChallengePoolBuilder {
    val defaultPolicy = EnglishUnitChallengePolicy()

    fun determineParticipation(word: EnglishWord): EnglishChallengeParticipation {
        return when {
            word.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" -> EnglishChallengeParticipation.CORE_REQUIRED
            word.requirementLevel == "LISTEN_SPEAK_ONLY" && word.spelling.isNotBlank() -> EnglishChallengeParticipation.EXTENDED_OPTIONAL
            else -> EnglishChallengeParticipation.EXCLUDED
        }
    }

    fun buildWordPool(unit: EnglishUnit): List<EnglishChallengeWordItem> {
        val items = mutableListOf<EnglishChallengeWordItem>()
        val seenLexicalKeys = mutableSetOf<String>()

        unit.words.forEach { word ->
            val participation = determineParticipation(word)
            if (participation != EnglishChallengeParticipation.EXCLUDED) {
                val key = word.spelling.trim().lowercase()
                if (key !in seenLexicalKeys) {
                    seenLexicalKeys.add(key)
                    items.add(
                        EnglishChallengeWordItem(
                            word = word,
                            participation = participation,
                            sourceUnitId = unit.unitId,
                            requirementLevel = word.requirementLevel
                        )
                    )
                }
            }
        }
        return items
    }
}
