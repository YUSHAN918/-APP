package com.example.data.english

import android.content.Context

object EnglishPreferenceStore {
    private const val PREFS_NAME = "english_autodictation_prefs"

    private const val KEY_WORD_SCOPE = "auto_dictation_word_scope"
    private const val KEY_REPEAT_COUNT = "auto_dictation_repeat_count"
    private const val KEY_INTERVAL_SECONDS = "auto_dictation_interval_seconds"
    private const val KEY_ORDER = "auto_dictation_order"
    private const val KEY_PRE_COUNTDOWN = "auto_dictation_pre_countdown"

    fun saveAutoDictationSettings(context: Context, settings: EnglishAutoDictationSettings) {
        val clamped = settings.clamped()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_WORD_SCOPE, clamped.wordScope.name)
            .putInt(KEY_REPEAT_COUNT, clamped.repeatCount)
            .putInt(KEY_INTERVAL_SECONDS, clamped.intervalSeconds)
            .putString(KEY_ORDER, clamped.order.name)
            .putInt(KEY_PRE_COUNTDOWN, clamped.preStartCountdownSeconds)
            .apply()
    }

    fun getAutoDictationSettings(context: Context): EnglishAutoDictationSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val scopeStr = prefs.getString(KEY_WORD_SCOPE, AutoDictationWordScope.ALL.name)
        val scope = try {
            AutoDictationWordScope.valueOf(scopeStr ?: AutoDictationWordScope.ALL.name)
        } catch (e: Exception) {
            AutoDictationWordScope.ALL
        }

        val orderStr = prefs.getString(KEY_ORDER, AutoDictationOrder.SHUFFLED.name)
        val order = try {
            AutoDictationOrder.valueOf(orderStr ?: AutoDictationOrder.SHUFFLED.name)
        } catch (e: Exception) {
            AutoDictationOrder.SHUFFLED
        }

        val repeatCount = prefs.getInt(KEY_REPEAT_COUNT, 2)
        val intervalSeconds = prefs.getInt(KEY_INTERVAL_SECONDS, 5)
        val preCountdown = prefs.getInt(KEY_PRE_COUNTDOWN, 3)

        return EnglishAutoDictationSettings(
            wordScope = scope,
            repeatCount = repeatCount,
            intervalSeconds = intervalSeconds,
            order = order,
            preStartCountdownSeconds = preCountdown
        ).clamped()
    }
}
