package com.example.data.english

enum class EnglishPluralRule {
    ADD_S,
    Y_TO_IES,
    IRREGULAR,
    SAME_FORM
}

object EnglishPluralInflector {
    fun getPluralForm(spelling: String): String {
        val lower = spelling.lowercase().trim()
        return when (lower) {
            "strawberry" -> "strawberries"
            "pear" -> "pears"
            "apple" -> "apples"
            "orange" -> "oranges"
            "banana" -> "bananas"
            "watermelon" -> "watermelons"
            "grape" -> "grapes"
            else -> {
                if (lower.endsWith("y") && !lower.endsWith("ay") && !lower.endsWith("ey") && !lower.endsWith("oy") && !lower.endsWith("uy")) {
                    lower.dropLast(1) + "ies"
                } else if (lower.endsWith("s") || lower.endsWith("sh") || lower.endsWith("ch") || lower.endsWith("x") || lower.endsWith("z")) {
                    lower + "es"
                } else {
                    lower + "s"
                }
            }
        }
    }

    fun getPluralRule(spelling: String): EnglishPluralRule {
        val lower = spelling.lowercase().trim()
        return when (lower) {
            "strawberry" -> EnglishPluralRule.Y_TO_IES
            else -> EnglishPluralRule.ADD_S
        }
    }
}

enum class FruitPreference {
    LIKE,
    DISLIKE,
    UNKNOWN
}

enum class FruitDialogueAction {
    ASK_PREFERENCE,
    ANSWER_LIKE,
    ANSWER_DISLIKE,
    EXPRESS_LIKE,
    EXPRESS_DISLIKE,
    OFFER,
    REQUEST,
    HAND_OVER,
    THANK,
    AGREE_DISLIKE
}

data class VirtualFruitPreference(
    val characterId: String,
    val fruitId: String,
    val preference: FruitPreference
)

data class FruitDialogueTurn(
    val action: FruitDialogueAction,
    val speakerId: String,
    val targetId: String? = null,
    val fruitId: String? = null,
    val expectedExpressionId: String = ""
)
