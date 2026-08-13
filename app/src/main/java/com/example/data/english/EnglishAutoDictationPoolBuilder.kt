package com.example.data.english

import java.util.Random

object EnglishAutoDictationPoolBuilder {

    fun buildDictationPool(
        unit: EnglishUnit,
        settings: EnglishAutoDictationSettings,
        randomSeed: Long = System.currentTimeMillis()
    ): List<EnglishAutoDictationItem> {
        val clampedSettings = settings.clamped()

        // First build standard challenge items from unit
        val challengeItems = EnglishChallengePoolBuilder.buildWordPool(unit)

        // Filter and convert to EnglishAutoDictationItem
        val seenWordIds = mutableSetOf<String>()
        val seenLexicalKeys = mutableSetOf<String>()

        val rawItems = mutableListOf<EnglishAutoDictationItem>()

        for (chItem in challengeItems) {
            val word = chItem.word

            // Valid spelling check
            val spelling = word.spelling.trim()
            if (spelling.isEmpty()) continue

            // Exclude unwanted context tokens
            if (word.wordId.contains("generatedPractice", ignoreCase = true)) continue
            if (spelling.contains("?") || spelling.contains("!") || spelling.contains("...")) continue
            // Sentence check (more than 3 words and contains punctuation, or expression role only)
            if (word.requirementLevel == "EXPRESSION_ONLY" || word.requirementLevel == "STORY_CONTEXT" ||
                word.requirementLevel == "CHANT_CONTEXT" || word.requirementLevel == "RECYCLE_CONTEXT_ONLY"
            ) continue

            // Unique checks
            if (seenWordIds.contains(word.wordId)) continue
            val lexKey = word.spelling.lowercase().trim()
            if (seenLexicalKeys.contains(lexKey)) continue

            val isExtended = chItem.participation == EnglishChallengeParticipation.EXTENDED_OPTIONAL ||
                    word.requirementLevel == "LISTEN_SPEAK_ONLY"

            // Scope filter
            val keep = when (clampedSettings.wordScope) {
                AutoDictationWordScope.ALL -> true
                AutoDictationWordScope.CORE_ONLY -> !isExtended
                AutoDictationWordScope.EXTENDED_ONLY -> isExtended
            }

            if (!keep) continue

            seenWordIds.add(word.wordId)
            seenLexicalKeys.add(lexKey)

            val spokenText = word.spelling
            val displayTex = word.spelling

            rawItems.add(
                EnglishAutoDictationItem(
                    wordId = word.wordId,
                    lexicalKey = lexKey,
                    spokenText = spokenText,
                    standardDisplayText = displayTex,
                    requirementLevel = word.requirementLevel,
                    participation = chItem.participation,
                    word = word,
                    isExtended = isExtended
                )
            )
        }

        return if (clampedSettings.order == AutoDictationOrder.SHUFFLED) {
            val random = Random(randomSeed)
            val list = rawItems.toMutableList()
            list.shuffle(random)
            list
        } else {
            rawItems
        }
    }

    fun buildMultiUnitDictationPool(
        units: List<EnglishUnit>,
        settings: EnglishAutoDictationSettings,
        randomSeed: Long = System.currentTimeMillis()
    ): List<EnglishAutoDictationItem> {
        val clampedSettings = settings.clamped()
        val seenWordIds = mutableSetOf<String>()
        val seenLexicalKeys = mutableSetOf<String>()
        val rawItems = mutableListOf<EnglishAutoDictationItem>()

        for (unit in units) {
            val challengeItems = EnglishChallengePoolBuilder.buildWordPool(unit)
            for (chItem in challengeItems) {
                val word = chItem.word
                val spelling = word.spelling.trim()
                if (spelling.isEmpty()) continue
                if (word.wordId.contains("generatedPractice", ignoreCase = true)) continue
                if (spelling.contains("?") || spelling.contains("!") || spelling.contains("...")) continue
                if (word.requirementLevel == "EXPRESSION_ONLY" || word.requirementLevel == "STORY_CONTEXT" ||
                    word.requirementLevel == "CHANT_CONTEXT" || word.requirementLevel == "RECYCLE_CONTEXT_ONLY"
                ) continue

                if (seenWordIds.contains(word.wordId)) continue
                val lexKey = word.spelling.lowercase().trim()
                if (seenLexicalKeys.contains(lexKey)) continue

                val isExtended = chItem.participation == EnglishChallengeParticipation.EXTENDED_OPTIONAL ||
                        word.requirementLevel == "LISTEN_SPEAK_ONLY"

                val keep = when (clampedSettings.wordScope) {
                    AutoDictationWordScope.ALL -> true
                    AutoDictationWordScope.CORE_ONLY -> !isExtended
                    AutoDictationWordScope.EXTENDED_ONLY -> isExtended
                }
                if (!keep) continue

                seenWordIds.add(word.wordId)
                seenLexicalKeys.add(lexKey)

                rawItems.add(
                    EnglishAutoDictationItem(
                        wordId = word.wordId,
                        lexicalKey = lexKey,
                        spokenText = word.spelling,
                        standardDisplayText = word.spelling,
                        requirementLevel = word.requirementLevel,
                        participation = chItem.participation,
                        word = word,
                        isExtended = isExtended
                    )
                )
            }
        }

        return if (clampedSettings.order == AutoDictationOrder.SHUFFLED) {
            val random = Random(randomSeed)
            val list = rawItems.toMutableList()
            list.shuffle(random)
            list
        } else {
            rawItems
        }
    }
}
