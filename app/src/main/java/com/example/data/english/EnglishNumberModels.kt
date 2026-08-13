package com.example.data.english

enum class EnglishNumberCategory {
    TEEN_OR_SPECIAL,
    ROUND_TEN
}

enum class EnglishNumberAnswerType {
    WORD,
    DIGIT,
    CHINESE_NUMERAL,
    QUANTITY_VISUAL
}

data class EnglishNumberInfo(
    val numberValue: Int,
    val numberWord: String,
    val displayDigit: String,
    val chineseNumeral: String,
    val numberCategory: EnglishNumberCategory,
    val spellingPattern: String,
    val sourceReference: String,
    val textbookPage: String
)

object EnglishNumberRepository {
    val numbers11To20 = listOf(
        EnglishNumberInfo(11, "eleven", "11", "十一", EnglishNumberCategory.TEEN_OR_SPECIAL, "e-l-e-v-e-n", "PEP 2013 Grade 3 Semester 2 Unit 6 Page 59", "59"),
        EnglishNumberInfo(12, "twelve", "12", "十二", EnglishNumberCategory.TEEN_OR_SPECIAL, "t-w-e-l-v-e", "PEP 2013 Grade 3 Semester 2 Unit 6 Page 59", "59"),
        EnglishNumberInfo(13, "thirteen", "13", "十三", EnglishNumberCategory.TEEN_OR_SPECIAL, "t-h-i-r-t-e-e-n", "PEP 2013 Grade 3 Semester 2 Unit 6 Page 59", "59"),
        EnglishNumberInfo(14, "fourteen", "14", "十四", EnglishNumberCategory.TEEN_OR_SPECIAL, "f-o-u-r-t-e-e-n", "PEP 2013 Grade 3 Semester 2 Unit 6 Page 59", "59"),
        EnglishNumberInfo(15, "fifteen", "15", "十五", EnglishNumberCategory.TEEN_OR_SPECIAL, "f-i-f-t-e-e-n", "PEP 2013 Grade 3 Semester 2 Unit 6 Page 59", "59"),
        EnglishNumberInfo(16, "sixteen", "16", "十六", EnglishNumberCategory.TEEN_OR_SPECIAL, "s-i-x-t-e-e-n", "PEP 2013 Grade 3 Semester 2 Unit 6 Page 62", "62"),
        EnglishNumberInfo(17, "seventeen", "17", "十七", EnglishNumberCategory.TEEN_OR_SPECIAL, "s-e-v-e-n-t-e-e-n", "PEP 2013 Grade 3 Semester 2 Unit 6 Page 62", "62"),
        EnglishNumberInfo(18, "eighteen", "18", "十八", EnglishNumberCategory.TEEN_OR_SPECIAL, "e-i-g-h-t-e-e-n", "PEP 2013 Grade 3 Semester 2 Unit 6 Page 62", "62"),
        EnglishNumberInfo(19, "nineteen", "19", "十九", EnglishNumberCategory.TEEN_OR_SPECIAL, "n-i-n-e-t-e-e-n", "PEP 2013 Grade 3 Semester 2 Unit 6 Page 62", "62"),
        EnglishNumberInfo(20, "twenty", "20", "二十", EnglishNumberCategory.ROUND_TEN, "t-w-e-n-t-y", "PEP 2013 Grade 3 Semester 2 Unit 6 Page 62", "62")
    )

    fun getByValue(value: Int): EnglishNumberInfo? = numbers11To20.find { it.numberValue == value }
    fun getByWord(word: String): EnglishNumberInfo? = numbers11To20.find { it.numberWord.equals(word.trim(), ignoreCase = true) }
}

object EnglishNumberAnswerEvaluator {
    fun evaluateWord(input: String, expectedWord: String): Boolean {
        return input.trim().equals(expectedWord.trim(), ignoreCase = true)
    }

    fun evaluateDigit(input: Int, expectedValue: Int): Boolean {
        return input == expectedValue
    }

    fun evaluateChineseNumeral(input: String, expectedChinese: String): Boolean {
        return input.trim() == expectedChinese.trim()
    }

    fun evaluateQuantityVisual(count: Int, expectedValue: Int): Boolean {
        return count == expectedValue
    }
}

enum class CountableObjectType(val singular: String, val plural: String, val emoji: String) {
    KITE("kite", "kites", "🪁"),
    BIRD("bird", "birds", "🐦"),
    CRAYON("crayon", "crayons", "🖍️"),
    CAR("car", "cars", "🚗"),
    BALLOON("balloon", "balloons", "🎈"),
    FISH("fish", "fish", "🐟"),
    APPLE("apple", "apples", "🍎")
}

enum class QuantityQuestionMode {
    SEE,
    HAVE
}

enum class QuantityGrouping {
    INDIVIDUAL,
    TEN_PLUS_REMAINDER,
    ROWS_OF_FIVE,
    GRID
}

data class EnglishQuantityScene(
    val sceneId: String,
    val objectType: CountableObjectType,
    val objectCount: Int,
    val visualGrouping: QuantityGrouping = QuantityGrouping.ROWS_OF_FIVE,
    val questionMode: QuantityQuestionMode = QuantityQuestionMode.SEE,
    val ownerCharacterId: String? = null,
    val randomSeed: Long = 12345L,
    val sourceReference: String? = "PEP 2013 Grade 3 Semester 2 Unit 6",
    val generatedPractice: Boolean = false
)
