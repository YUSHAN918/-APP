package com.example.data.english

import android.content.Context

enum class EnglishAccessPolicy {
    ALL_READY_CONTENT_OPEN,
    SEQUENTIAL
}

object EnglishProgressManager {
    private const val PREFS_NAME_FORMAL = "english_progress_prefs"
    private const val PREFS_NAME_DEV = "english_dev_progress_prefs"
    
    private const val COMPLETED_LESSONS_KEY = "completed_lessons"
    private const val BYPASS_PREREQUISITES_KEY = "bypass_prerequisites"
    private const val ACCESS_POLICY_KEY = "english_access_policy"
    private const val SELECTED_COURSE_KEY = "selected_course_id"

    private var currentAccessPolicy: EnglishAccessPolicy = EnglishAccessPolicy.ALL_READY_CONTENT_OPEN

    fun getSelectedCourseId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME_FORMAL, Context.MODE_PRIVATE)
        val saved = prefs.getString(SELECTED_COURSE_KEY, "english_pep_2013_g3_s1") ?: "english_pep_2013_g3_s1"
        return if (saved == "english_pep_2013_g3_s1" || saved == "english_pep_2013_g3_s2" || saved == "english_pep_2013_g4_s1") {
            saved
        } else {
            "english_pep_2013_g3_s1"
        }
    }

    fun setSelectedCourseId(context: Context, courseId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME_FORMAL, Context.MODE_PRIVATE)
        prefs.edit().putString(SELECTED_COURSE_KEY, courseId).apply()
    }

    fun getAccessPolicy(context: Context): EnglishAccessPolicy {
        return currentAccessPolicy
    }

    fun setAccessPolicy(context: Context, policy: EnglishAccessPolicy) {
        currentAccessPolicy = policy
    }

    private fun getPrefsName(context: Context): String {
        return if (isDevBypassEnabled(context)) PREFS_NAME_DEV else PREFS_NAME_FORMAL
    }

    fun getCompletedLessons(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(getPrefsName(context), Context.MODE_PRIVATE)
        return prefs.getStringSet(COMPLETED_LESSONS_KEY, emptySet()) ?: emptySet()
    }

    fun completeLesson(context: Context, lessonId: String) {
        val prefs = context.getSharedPreferences(getPrefsName(context), Context.MODE_PRIVATE)
        val completed = getCompletedLessons(context).toMutableSet()
        if (completed.add(lessonId)) {
            prefs.edit().putStringSet(COMPLETED_LESSONS_KEY, completed).apply()
        }
    }

    fun resetLesson(context: Context, lessonId: String) {
        val prefs = context.getSharedPreferences(getPrefsName(context), Context.MODE_PRIVATE)
        val completed = getCompletedLessons(context).toMutableSet()
        if (completed.remove(lessonId)) {
            prefs.edit().putStringSet(COMPLETED_LESSONS_KEY, completed).apply()
        }
    }

    fun isLessonCompleted(context: Context, lessonId: String): Boolean {
        if (isDevBypassEnabled(context)) return true
        return getCompletedLessons(context).contains(lessonId)
    }

    fun isUnitCompleted(context: Context, courseId: String, unitId: String): Boolean {
        if (isDevBypassEnabled(context)) return true
        if (unitId.contains("recycle")) {
            val progress = getRecycleProgress(context, unitId)
            return progress.completedMissionIds.size >= 5
        }
        val unit = EnglishContentLoader.loadUnit(context, courseId, unitId) ?: return false
        if (unit.words.isEmpty()) return false
        // For simple Unit 1, let's assume the unit is completed if "lesson_english_pep_2013_g3_s1_u1" is completed.
        return getCompletedLessons(context).contains(unitId)
    }

    fun completeUnit(context: Context, unitId: String) {
        val prefs = context.getSharedPreferences(getPrefsName(context), Context.MODE_PRIVATE)
        val completed = getCompletedLessons(context).toMutableSet()
        if (completed.add(unitId)) {
            prefs.edit().putStringSet(COMPLETED_LESSONS_KEY, completed).apply()
        }
    }

    fun clearProgress(context: Context) {
        val prefsFormal = context.getSharedPreferences(PREFS_NAME_FORMAL, Context.MODE_PRIVATE)
        prefsFormal.edit().clear().apply()
        
        val prefsDev = context.getSharedPreferences(PREFS_NAME_DEV, Context.MODE_PRIVATE)
        prefsDev.edit().clear().apply()
    }

    // Developer bypass settings
    fun setDevBypassEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME_DEV, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(BYPASS_PREREQUISITES_KEY, enabled).apply()
    }

    fun isDevBypassEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME_DEV, Context.MODE_PRIVATE)
        return prefs.getBoolean(BYPASS_PREREQUISITES_KEY, false)
    }

    // Word Mastery and Challenge Detail persistence
    fun getWordMastery(context: Context, wordId: String): String {
        val prefs = context.getSharedPreferences("english_word_mastery", Context.MODE_PRIVATE)
        return prefs.getString(wordId, "LEARNING") ?: "LEARNING"
    }

    fun saveWordMastery(context: Context, wordId: String, status: String) {
        val prefs = context.getSharedPreferences("english_word_mastery", Context.MODE_PRIVATE)
        prefs.edit().putString(wordId, status).apply()
    }

    fun getWordDetailStats(context: Context, wordId: String): WordDetailStats {
        val prefs = context.getSharedPreferences("english_word_details", Context.MODE_PRIVATE)
        return WordDetailStats(
            meaningCorrect = prefs.getBoolean("${wordId}_meaning", false),
            reverseCorrect = prefs.getBoolean("${wordId}_reverse", false),
            spellingCorrect = prefs.getBoolean("${wordId}_spelling", false),
            dictationCorrect = prefs.getBoolean("${wordId}_dictation", false)
        )
    }

    fun saveWordDetailStats(context: Context, wordId: String, stats: WordDetailStats) {
        val prefs = context.getSharedPreferences("english_word_details", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("${wordId}_meaning", stats.meaningCorrect)
            .putBoolean("${wordId}_reverse", stats.reverseCorrect)
            .putBoolean("${wordId}_spelling", stats.spellingCorrect)
            .putBoolean("${wordId}_dictation", stats.dictationCorrect)
            .apply()
    }

    // Extended Practice & Challenge Persistence
    fun getExtendedPracticeDetailStats(context: Context, wordId: String): WordDetailStats {
        val prefs = context.getSharedPreferences("english_extended_practice_details", Context.MODE_PRIVATE)
        return WordDetailStats(
            meaningCorrect = prefs.getBoolean("${wordId}_meaning", false),
            reverseCorrect = prefs.getBoolean("${wordId}_reverse", false),
            spellingCorrect = prefs.getBoolean("${wordId}_spelling", false),
            dictationCorrect = prefs.getBoolean("${wordId}_dictation", false)
        )
    }

    fun saveExtendedPracticeDetailStats(context: Context, wordId: String, stats: WordDetailStats) {
        val prefs = context.getSharedPreferences("english_extended_practice_details", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("${wordId}_meaning", stats.meaningCorrect)
            .putBoolean("${wordId}_reverse", stats.reverseCorrect)
            .putBoolean("${wordId}_spelling", stats.spellingCorrect)
            .putBoolean("${wordId}_dictation", stats.dictationCorrect)
            .apply()
    }

    fun isExtendedChallengeCompleted(context: Context, unitId: String): Boolean {
        val prefs = context.getSharedPreferences(getPrefsName(context), Context.MODE_PRIVATE)
        return prefs.getBoolean("extended_challenge_completed_$unitId", false)
    }

    fun saveExtendedChallengeCompleted(context: Context, unitId: String) {
        val prefs = context.getSharedPreferences(getPrefsName(context), Context.MODE_PRIVATE)
        prefs.edit().putBoolean("extended_challenge_completed_$unitId", true).apply()
    }

    // Recycle Progress Persistence
    fun getRecycleProgress(context: Context, recycleId: String): EnglishRecycleProgress {
        val prefs = context.getSharedPreferences("english_recycle_progress", Context.MODE_PRIVATE)
        val completedMissions = prefs.getStringSet("${recycleId}_completed_missions", emptySet()) ?: emptySet()
        val boardPos = prefs.getInt("${recycleId}_board_pos", 0)
        val boardSeed = prefs.getLong("${recycleId}_board_seed", 12345L)
        val rewardClaimed = prefs.getBoolean("${recycleId}_reward_claimed", false)
        return EnglishRecycleProgress(
            recycleId = recycleId,
            completedMissionIds = completedMissions,
            boardPosition = boardPos,
            boardRandomSeed = boardSeed,
            rewardClaimed = rewardClaimed
        )
    }

    fun completeRecycleMission(context: Context, recycleId: String, missionId: String) {
        val prefs = context.getSharedPreferences("english_recycle_progress", Context.MODE_PRIVATE)
        val completed = prefs.getStringSet("${recycleId}_completed_missions", emptySet())?.toMutableSet() ?: mutableSetOf()
        completed.add(missionId)
        prefs.edit().putStringSet("${recycleId}_completed_missions", completed).apply()
    }

    fun saveBoardGamePosition(context: Context, recycleId: String, position: Int, seed: Long) {
        val prefs = context.getSharedPreferences("english_recycle_progress", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("${recycleId}_board_pos", position)
            .putLong("${recycleId}_board_seed", seed)
            .apply()
    }

    fun claimRecycleReward(context: Context, recycleId: String): Boolean {
        val prefs = context.getSharedPreferences("english_recycle_progress", Context.MODE_PRIVATE)
        val alreadyClaimed = prefs.getBoolean("${recycleId}_reward_claimed", false)
        if (!alreadyClaimed) {
            prefs.edit().putBoolean("${recycleId}_reward_claimed", true).apply()
            return true
        }
        return false
    }
}

data class WordDetailStats(
    val meaningCorrect: Boolean = false,
    val reverseCorrect: Boolean = false,
    val spellingCorrect: Boolean = false,
    val dictationCorrect: Boolean = false
)
