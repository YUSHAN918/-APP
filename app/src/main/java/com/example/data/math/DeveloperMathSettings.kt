package com.example.data.math

import android.content.Context

object DeveloperMathSettings {
    private const val DEV_PREFS_NAME = "math_dev_progress_prefs"
    private const val KEY_BYPASS_PREREQUISITES = "dev_bypass_math_prerequisites"
    private const val KEY_USE_SIMULATED_PROGRESS = "dev_use_simulated_progress"
    private const val COMPLETED_LESSONS_KEY = "completed_lessons"

    fun isBypassMathPrerequisites(context: Context): Boolean {
        if (!com.example.BuildConfig.DEBUG) return false
        val prefs = context.getSharedPreferences(DEV_PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BYPASS_PREREQUISITES, false)
    }

    fun setBypassMathPrerequisites(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(DEV_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BYPASS_PREREQUISITES, enabled).apply()
    }

    fun isUseSimulatedProgress(context: Context): Boolean {
        if (!com.example.BuildConfig.DEBUG) return false
        val prefs = context.getSharedPreferences(DEV_PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_USE_SIMULATED_PROGRESS, false)
    }

    fun setUseSimulatedProgress(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(DEV_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_USE_SIMULATED_PROGRESS, enabled).apply()
    }

    fun getCompletedLessons(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(DEV_PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(COMPLETED_LESSONS_KEY, emptySet()) ?: emptySet()
    }

    fun completeLesson(context: Context, lessonId: String) {
        val prefs = context.getSharedPreferences(DEV_PREFS_NAME, Context.MODE_PRIVATE)
        val completed = getCompletedLessons(context).toMutableSet()
        if (completed.add(lessonId)) {
            prefs.edit().putStringSet(COMPLETED_LESSONS_KEY, completed).apply()
        }
    }

    fun isUnitCompleted(context: Context, courseId: String, unitId: String): Boolean {
        val unit = MathContentLoader.loadUnit(context, courseId, unitId) ?: return false
        val formalLessons = unit.lessons.filter { it.isFormalLesson() }
        if (formalLessons.isEmpty()) return false
        val completed = getCompletedLessons(context)
        return formalLessons.all { completed.contains(it.lessonId) }
    }

    fun isUnitIndividuallyBypassed(context: Context, unitId: String): Boolean {
        if (!com.example.BuildConfig.DEBUG) return false
        val prefs = context.getSharedPreferences(DEV_PREFS_NAME, Context.MODE_PRIVATE)
        val bypassed = prefs.getStringSet("individually_bypassed_units", emptySet()) ?: emptySet()
        return bypassed.contains(unitId)
    }

    fun setUnitIndividuallyBypassed(context: Context, unitId: String, bypassed: Boolean) {
        val prefs = context.getSharedPreferences(DEV_PREFS_NAME, Context.MODE_PRIVATE)
        val set = (prefs.getStringSet("individually_bypassed_units", emptySet()) ?: emptySet()).toMutableSet()
        if (bypassed) {
            set.add(unitId)
        } else {
            set.remove(unitId)
        }
        prefs.edit().putStringSet("individually_bypassed_units", set).apply()
    }

    fun clearProgress(context: Context) {
        val prefs = context.getSharedPreferences(DEV_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(COMPLETED_LESSONS_KEY)
            .remove("individually_bypassed_units")
            .putBoolean(KEY_BYPASS_PREREQUISITES, false)
            .putBoolean(KEY_USE_SIMULATED_PROGRESS, false)
            .apply()
    }
}
