package com.example.data.math

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class MathUnitLoadErrorDetail(
    val courseId: String,
    val unitId: String,
    val assetPath: String,
    val failureStage: String, // "FILE_NOT_FOUND" / "JSON_PARSE" / "VALIDATION" / "UNKNOWN"
    val exceptionClass: String?,
    val message: String?,
    val validatorErrors: List<String> = emptyList()
)

object MathContentLoader {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // 内存缓存以确保 Compose 渲染循环中零 I/O 触发
    private var cachedManifest: MathCourseManifest? = null
    private val unitCache = java.util.concurrent.ConcurrentHashMap<String, MathUnit>()
    private val contentReadyCache = java.util.concurrent.ConcurrentHashMap<String, Boolean>()
    
    val loadErrorDetails = java.util.concurrent.ConcurrentHashMap<String, MathUnitLoadErrorDetail>()

    fun getLoadErrorDetail(courseId: String, unitId: String): MathUnitLoadErrorDetail? {
        return loadErrorDetails["$courseId:$unitId"]
    }

    fun loadManifest(context: Context): MathCourseManifest? {
        if (cachedManifest != null) return cachedManifest
        return try {
            val jsonString = context.assets.open("math/pep/grade6/semester1/course_manifest.json")
                .bufferedReader()
                .use { it.readText() }
            val adapter = moshi.adapter(MathCourseManifest::class.java)
            val manifest = adapter.fromJson(jsonString)
            if (manifest != null) {
                cachedManifest = manifest
            }
            manifest
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getUnitAssetPath(courseId: String, unitId: String): String? {
        if (courseId != "math_pep_g6_s1") return null
        val filename = when (unitId) {
            "math_pep_g6_s1_u1" -> "unit_01.json"
            "math_pep_g6_s1_u2" -> "unit_02.json"
            "math_pep_g6_s1_u3" -> "unit_03.json"
            "math_pep_g6_s1_u4" -> "unit_04.json"
            "math_pep_g6_s1_u5" -> "unit_05.json"
            "math_pep_g6_s1_u6" -> "unit_06.json"
            "math_pep_g6_s1_u7" -> "unit_07.json"
            "math_pep_g6_s1_u8" -> "unit_08.json"
            "math_pep_g6_s1_u9" -> "unit_09.json"
            else -> null
        } ?: return null
        return "math/pep/grade6/semester1/$filename"
    }

    fun isUnitContentReady(context: Context, courseId: String, unitId: String): Boolean {
        val cacheKey = "$courseId:$unitId"
        contentReadyCache[cacheKey]?.let { return it }
        val path = getUnitAssetPath(courseId, unitId) ?: return false
        val ready = try {
            context.assets.open(path).use { true }
        } catch (e: Exception) {
            loadErrorDetails[cacheKey] = MathUnitLoadErrorDetail(
                courseId = courseId,
                unitId = unitId,
                assetPath = path,
                failureStage = "FILE_NOT_FOUND",
                exceptionClass = e.javaClass.name,
                message = e.message
            )
            false
        }
        contentReadyCache[cacheKey] = ready
        return ready
    }

    fun getUnitLockReason(
        context: Context,
        courseId: String,
        unitSummary: MathUnitSummary,
        units: List<MathUnitSummary>
    ): MathUnitLockReason {
        // 1. Check if content is ready
        val isReady = isUnitContentReady(context, courseId, unitSummary.unitId)
        if (!isReady) {
            return MathUnitLockReason.CONTENT_NOT_READY
        }

        // 2. Check if there is a predecessor unit and if it is completed
        val currentOrder = unitSummary.order
        if (currentOrder > 1) {
            val prevUnit = units.find { it.order == currentOrder - 1 }
            if (prevUnit != null) {
                val isPrevCompleted = MathProgressManager.isUnitCompleted(context, courseId, prevUnit.unitId)
                if (!isPrevCompleted) {
                    if (!(com.example.BuildConfig.DEBUG && (
                        DeveloperMathSettings.isBypassMathPrerequisites(context) ||
                        DeveloperMathSettings.isUnitIndividuallyBypassed(context, unitSummary.unitId)
                    ))) {
                        return MathUnitLockReason.PREVIOUS_UNIT_NOT_COMPLETED
                    }
                }
            }
        }

        // 3. Try loading it. If it fails, return DATA_LOAD_ERROR
        try {
            val unit = loadUnit(context, courseId, unitSummary.unitId)
            if (unit == null) {
                return MathUnitLockReason.DATA_LOAD_ERROR
            }
        } catch (e: Exception) {
            return MathUnitLockReason.DATA_LOAD_ERROR
        }

        return MathUnitLockReason.NONE
    }

    fun loadUnit(context: Context, unitId: String): MathUnit? {
        return loadUnit(context, "math_pep_g6_s1", unitId)
    }

    fun loadCourse(context: Context, courseId: String): MathCourseManifest? {
        val path = when (courseId) {
            "math_pep_g6_s1" -> "math/pep/grade6/semester1/course_manifest.json"
            else -> null
        } ?: return null
        return try {
            val jsonString = context.assets.open(path)
                .bufferedReader()
                .use { it.readText() }
            val adapter = moshi.adapter(MathCourseManifest::class.java)
            adapter.fromJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun enrichUnit(unit: MathUnit): MathUnit {
        val enrichedLessons = unit.lessons.map { lesson ->
            val enrichedBlocks = lesson.contentBlocks.map { block ->
                val q = block.question
                if (q != null && q.type == MathQuestionType.FILL_BLANK) {
                    block.copy(question = MathQuestionEnricher.enrich(q))
                } else {
                    block
                }
            }
            val enrichedQuestions = lesson.questions.map { q ->
                if (q.type == MathQuestionType.FILL_BLANK) {
                    MathQuestionEnricher.enrich(q)
                } else {
                    q
                }
            }
            lesson.copy(contentBlocks = enrichedBlocks, questions = enrichedQuestions)
        }
        return unit.copy(lessons = enrichedLessons)
    }

    fun loadUnit(context: Context, courseId: String, unitId: String): MathUnit? {
        val cacheKey = "$courseId:$unitId"
        unitCache[cacheKey]?.let { return it }
        val path = getUnitAssetPath(courseId, unitId) ?: run {
            loadErrorDetails[cacheKey] = MathUnitLoadErrorDetail(
                courseId = courseId,
                unitId = unitId,
                assetPath = "unknown",
                failureStage = "FILE_NOT_FOUND",
                exceptionClass = null,
                message = "Asset path not configured for unit $unitId"
            )
            return null
        }
        return try {
            val jsonString = context.assets.open(path)
                .bufferedReader()
                .use { it.readText() }
            val adapter = moshi.adapter(MathUnit::class.java)
            val unit = try {
                adapter.fromJson(jsonString)
            } catch (je: Exception) {
                loadErrorDetails[cacheKey] = MathUnitLoadErrorDetail(
                    courseId = courseId,
                    unitId = unitId,
                    assetPath = path,
                    failureStage = "JSON_PARSE",
                    exceptionClass = je.javaClass.name,
                    message = je.message
                )
                throw je
            }
            if (unit != null) {
                val validation = MathContentValidator.validateUnit(unit)
                if (!validation.isValid) {
                    loadErrorDetails[cacheKey] = MathUnitLoadErrorDetail(
                        courseId = courseId,
                        unitId = unitId,
                        assetPath = path,
                        failureStage = "VALIDATION",
                        exceptionClass = null,
                        message = "Content validation failed with ${validation.errors.size} error(s)",
                        validatorErrors = validation.errors
                    )
                    null
                } else {
                    val enriched = enrichUnit(unit)
                    unitCache[cacheKey] = enriched
                    loadErrorDetails.remove(cacheKey)
                    enriched
                }
            } else {
                loadErrorDetails[cacheKey] = MathUnitLoadErrorDetail(
                    courseId = courseId,
                    unitId = unitId,
                    assetPath = path,
                    failureStage = "JSON_PARSE",
                    exceptionClass = null,
                    message = "Parsed JSON resulted in null object"
                )
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (loadErrorDetails[cacheKey] == null) {
                loadErrorDetails[cacheKey] = MathUnitLoadErrorDetail(
                    courseId = courseId,
                    unitId = unitId,
                    assetPath = path,
                    failureStage = "UNKNOWN",
                    exceptionClass = e.javaClass.name,
                    message = e.message
                )
            }
            null
        }
    }

    fun loadLesson(context: Context, courseId: String, unitId: String, lessonId: String): MathLesson? {
        val unit = loadUnit(context, courseId, unitId) ?: return null
        return unit.lessons.find { it.lessonId == lessonId }
    }

    fun preWarmCache(context: Context) {
        try {
            val manifest = loadManifest(context) ?: return
            manifest.units.forEach { unitSummary ->
                isUnitContentReady(context, manifest.courseId, unitSummary.unitId)
                loadUnit(context, manifest.courseId, unitSummary.unitId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
