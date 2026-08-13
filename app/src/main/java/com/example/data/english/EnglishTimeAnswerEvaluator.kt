package com.example.data.english

data class TimeActivityGrammarRule(
    val activityId: String,
    val canonicalGrammarMode: TimeGrammarMode,
    val expressionTemplate: String
)

object EnglishTimeAnswerEvaluator {

    val rules = listOf(
        TimeActivityGrammarRule("breakfast", TimeGrammarMode.TIME_FOR_NOUN, "It's time for breakfast."),
        TimeActivityGrammarRule("english_class", TimeGrammarMode.TIME_FOR_NOUN, "It's time for English class."),
        TimeActivityGrammarRule("lunch", TimeGrammarMode.TIME_FOR_NOUN, "It's time for lunch."),
        TimeActivityGrammarRule("music_class", TimeGrammarMode.TIME_FOR_NOUN, "It's time for music class."),
        TimeActivityGrammarRule("pe_class", TimeGrammarMode.TIME_FOR_NOUN, "It's time for PE class."),
        TimeActivityGrammarRule("dinner", TimeGrammarMode.TIME_FOR_NOUN, "It's time for dinner."),
        TimeActivityGrammarRule("get_up", TimeGrammarMode.TIME_TO_VERB, "It's time to get up."),
        TimeActivityGrammarRule("go_to_school", TimeGrammarMode.TIME_TO_VERB, "It's time to go to school."),
        TimeActivityGrammarRule("go_home", TimeGrammarMode.TIME_TO_VERB, "It's time to go home."),
        TimeActivityGrammarRule("go_to_bed", TimeGrammarMode.TIME_TO_VERB, "It's time to go to bed.")
    )

    fun evaluateGrammarSentence(userInput: String, activityId: String, chosenMode: TimeGrammarMode): Boolean {
        val normInput = userInput.trim().lowercase().replace("’", "'")
        val rule = rules.find { it.activityId.lowercase().replace("_", " ") == activityId.lowercase().replace("_", " ") || it.activityId == activityId }
            ?: return false

        if (rule.canonicalGrammarMode != chosenMode) {
            return false
        }

        // Must match either standard template (case-insensitive and allowing single quotes replacement) or key components
        val expectedTemplate = rule.expressionTemplate.lowercase().replace("’", "'")
        if (normInput == expectedTemplate) {
            return true
        }

        // Let's check for specific component errors:
        // "it's time to breakfast" is wrong
        if (rule.canonicalGrammarMode == TimeGrammarMode.TIME_FOR_NOUN) {
            if (normInput.contains("time to")) return false
            if (normInput.contains("time for") && normInput.contains(rule.activityId.replace("_", " "))) return true
        } else {
            if (normInput.contains("time for")) return false
            if (normInput.contains("time to") && normInput.contains(rule.activityId.replace("_", " "))) return true
        }

        return false
    }

    fun evaluateDigitalTimeEntry(userInput: String, expectedTime: EnglishClockTime): Boolean {
        val cleaned = userInput.trim().replace("：", ":")
        val expectedStr = EnglishTimeFormatter.formatDigital(expectedTime)
        if (cleaned == expectedStr) return true

        // Also check if they input spoken text
        val spokenStr = EnglishTimeFormatter.formatSpoken(expectedTime).lowercase().replace("’", "'")
        val inputSpoken = cleaned.lowercase().replace("’", "'")
        if (inputSpoken == spokenStr) return true

        return false
    }

    fun normalizeApostrophe(text: String): String {
        return text.trim().lowercase().replace("’", "'").replace("o'clock", "o'clock")
    }
}
