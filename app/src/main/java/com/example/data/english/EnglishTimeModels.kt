package com.example.data.english

enum class DayPeriod {
    AM,
    PM,
    UNSPECIFIED
}

data class EnglishClockTime(
    val hour: Int,
    val minute: Int,
    val period: DayPeriod? = DayPeriod.UNSPECIFIED
) {
    init {
        require(hour in 1..12) { "Hour must be between 1 and 12" }
        require(minute in 0..59) { "Minute must be between 0 and 59" }
    }

    val normalizedMinuteOfDay: Int
        get() {
            val h = if (period == DayPeriod.PM && hour < 12) hour + 12
                    else if (period == DayPeriod.AM && hour == 12) 0
                    else hour
            return h * 60 + minute
        }
}

enum class DailyActivityType {
    BREAKFAST,
    ENGLISH_CLASS,
    LUNCH,
    MUSIC_CLASS,
    PE_CLASS,
    DINNER,
    GET_UP,
    GO_TO_SCHOOL,
    GO_HOME,
    GO_TO_BED,
    OTHER
}

enum class TimeGrammarMode {
    TIME_FOR_NOUN,
    TIME_TO_VERB
}

data class ScheduleItem(
    val itemId: String,
    val activityWordRef: String,
    val activityType: DailyActivityType,
    val time: EnglishClockTime,
    val grammarMode: TimeGrammarMode,
    val sourceReference: String? = null,
    val generatedPractice: Boolean = false
)

data class VirtualDailySchedule(
    val scheduleId: String,
    val ownerCharacterId: String,
    val items: List<ScheduleItem>,
    val randomSeed: Long? = null,
    val sourceType: ScheduleSource = ScheduleSource.TEXTBOOK
)

enum class ScheduleSource {
    TEXTBOOK,
    APP_VIRTUAL,
    GENERATED_PRACTICE
}
