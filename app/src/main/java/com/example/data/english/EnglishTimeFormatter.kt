package com.example.data.english

object EnglishTimeFormatter {

    private val numberWords = mapOf(
        1 to "one", 2 to "two", 3 to "three", 4 to "four", 5 to "five",
        6 to "six", 7 to "seven", 8 to "eight", 9 to "nine", 10 to "ten",
        11 to "eleven", 12 to "twelve", 13 to "thirteen", 14 to "fourteen",
        15 to "fifteen", 16 to "sixteen", 17 to "seventeen", 18 to "eighteen",
        19 to "nineteen", 20 to "twenty", 30 to "thirty", 40 to "forty",
        50 to "fifty"
    )

    fun getWordForNumber(num: Int): String {
        if (num == 0) return ""
        numberWords[num]?.let { return it }
        val tens = (num / 10) * 10
        val ones = num % 10
        val tensWord = numberWords[tens] ?: ""
        val onesWord = numberWords[ones] ?: ""
        return if (onesWord.isNotEmpty()) "$tensWord $onesWord" else tensWord
    }

    fun formatDigital(time: EnglishClockTime): String {
        val minStr = if (time.minute < 10) "0${time.minute}" else "${time.minute}"
        return "${time.hour}:$minStr"
    }

    fun formatSpoken(time: EnglishClockTime): String {
        val hourWord = getWordForNumber(time.hour)
        return if (time.minute == 0) {
            "$hourWord o'clock"
        } else {
            val minWord = getWordForNumber(time.minute)
            "$hourWord $minWord"
        }
    }

    fun getWholeHourExpression(time: EnglishClockTime): String {
        val hourWord = getWordForNumber(time.hour)
        return "$hourWord o'clock"
    }
}
