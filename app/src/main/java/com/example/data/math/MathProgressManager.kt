package com.example.data.math

import android.content.Context

object MathProgressManager {
    private const val PREFS_NAME = "math_progress_prefs"
    private const val COMPLETED_LESSONS_KEY = "completed_lessons"

    fun getCompletedLessons(context: Context): Set<String> {
        if (com.example.BuildConfig.DEBUG && DeveloperMathSettings.isUseSimulatedProgress(context)) {
            return DeveloperMathSettings.getCompletedLessons(context)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(COMPLETED_LESSONS_KEY, emptySet()) ?: emptySet()
    }

    fun completeLesson(context: Context, lessonId: String) {
        if (com.example.BuildConfig.DEBUG && (DeveloperMathSettings.isUseSimulatedProgress(context) || DeveloperMathSettings.isBypassMathPrerequisites(context))) {
            DeveloperMathSettings.completeLesson(context, lessonId)
            return
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val completed = getCompletedLessons(context).toMutableSet()
        if (completed.add(lessonId)) {
            prefs.edit().putStringSet(COMPLETED_LESSONS_KEY, completed).apply()
        }
    }

    fun isLessonCompleted(context: Context, lessonId: String): Boolean {
        return getCompletedLessons(context).contains(lessonId)
    }

    fun isUnitCompleted(context: Context, courseId: String, unitId: String): Boolean {
        if (com.example.BuildConfig.DEBUG && DeveloperMathSettings.isUseSimulatedProgress(context)) {
            return DeveloperMathSettings.isUnitCompleted(context, courseId, unitId)
        }
        val unit = MathContentLoader.loadUnit(context, courseId, unitId) ?: return false
        val formalLessons = unit.lessons.filter { it.isFormalLesson() }
        if (formalLessons.isEmpty()) return false
        val completed = getCompletedLessons(context)
        return formalLessons.all { completed.contains(it.lessonId) }
    }

    fun clearProgress(context: Context) {
        if (com.example.BuildConfig.DEBUG && DeveloperMathSettings.isUseSimulatedProgress(context)) {
            DeveloperMathSettings.clearProgress(context)
            return
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(COMPLETED_LESSONS_KEY).apply()
    }
}

