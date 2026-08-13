package com.example.data.english

object EnglishFriendDescriptionEvaluator {

    fun evaluateClues(
        profiles: List<VirtualFriendProfile>,
        clues: List<FriendClue>
    ): VirtualFriendProfile? {
        val matching = profiles.filter { profile ->
            clues.all { clue -> matchesClue(profile, clue) }
        }
        return if (matching.size == 1) matching.first() else null
    }

    fun matchesClue(profile: VirtualFriendProfile, clue: FriendClue): Boolean {
        val expected = clue.expectedValue.lowercase().trim()
        return when (clue.clueType) {
            FriendClueType.PERSONALITY, FriendClueType.HEIGHT_OR_BUILD -> {
                profile.traits.any { it.lowercase() == expected }
            }
            FriendClueType.HAIR -> {
                if (expected == "long" || expected == "long hair") {
                    profile.hairStyle == HairStyle.LONG
                } else if (expected == "short" || expected == "short hair") {
                    profile.hairStyle == HairStyle.SHORT
                } else {
                    profile.hairColour?.lowercase() == expected
                }
            }
            FriendClueType.GLASSES -> {
                if (expected == "glasses" || expected == "has glasses" || expected == "true") {
                    profile.accessories.contains("glasses")
                } else {
                    !profile.accessories.contains("glasses")
                }
            }
            FriendClueType.SHOES -> {
                profile.shoeColour?.lowercase() == expected
            }
            FriendClueType.BAG -> {
                profile.bagColour?.lowercase() == expected
            }
            FriendClueType.PRONOUN -> {
                if (expected == "he" || expected == "his" || expected == "boy") {
                    profile.pronounSet == PronounSet.HE_HIS
                } else {
                    profile.pronounSet == PronounSet.SHE_HER
                }
            }
            FriendClueType.NAME -> {
                profile.displayName.lowercase() == expected || profile.characterId.lowercase() == expected
            }
        }
    }

    fun getSentenceType(sentence: String): FriendDescriptionType {
        val s = sentence.lowercase().trim()
        return when {
            s.startsWith("what's his name") || s.startsWith("what's her name") -> FriendDescriptionType.ASK_NAME
            s.startsWith("his name is") || s.startsWith("her name is") -> FriendDescriptionType.ANSWER_NAME
            s.contains("boy or girl") -> FriendDescriptionType.ALTERNATIVE_QUESTION
            s.startsWith("is he ") || s.startsWith("is she ") -> FriendDescriptionType.IDENTITY_CONFIRMATION
            s.startsWith("his ") || s.startsWith("her ") -> FriendDescriptionType.POSSESSIVE_DESCRIPTION
            s.contains(" has ") -> FriendDescriptionType.HAVE_FEATURE
            else -> FriendDescriptionType.BE_TRAIT
        }
    }

    fun isValidDescriptionStructure(pronoun: String, verb: String, predicate: String): Boolean {
        val p = pronoun.lowercase().trim()
        val v = verb.lowercase().trim()
        val pred = predicate.lowercase().trim()

        if ((p == "he" || p == "she") && v == "is") {
            // BE_TRAIT: predicates should be adjectives like friendly, quiet, tall, strong, thin, short
            val validTraits = setOf("friendly", "quiet", "tall", "strong", "thin", "short", "tall and strong", "tall and thin", "short and thin")
            return validTraits.contains(pred)
        }

        if ((p == "he" || p == "she") && v == "has") {
            // HAVE_FEATURE: predicates should be nouns/features like glasses, long hair, short hair, a green bag, a hat
            val validFeatures = setOf("glasses", "long hair", "short hair", "a green bag", "a blue bag", "a hat", "a red hat")
            return validFeatures.contains(pred)
        }

        if ((p == "his" || p == "her") && (v == "is" || v == "are")) {
            // POSSESSIVE_DESCRIPTION: His shoes are blue / Her bag is green
            if (pred.contains("shoes") && v == "are") return true
            if (pred.contains("glasses") && v == "are") return true
            if (pred.contains("bag") && v == "is") return true
            if (pred.contains("hair") && v == "is") return true
        }

        return false
    }
}
