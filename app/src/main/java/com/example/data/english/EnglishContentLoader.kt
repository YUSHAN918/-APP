package com.example.data.english

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class EnglishUnitLoadErrorDetail(
    val courseId: String,
    val unitId: String,
    val assetPath: String,
    val failureStage: String, // "FILE_NOT_FOUND" / "JSON_PARSE" / "VALIDATION" / "UNKNOWN"
    val exceptionClass: String?,
    val message: String?
)

object EnglishContentLoader {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Memory caches to guarantee zero I/O triggers on compose rendering cycles
    private val manifestCache = java.util.concurrent.ConcurrentHashMap<String, EnglishCourseManifest>()
    private val unitCache = java.util.concurrent.ConcurrentHashMap<String, EnglishUnit>()
    private val contentReadyCache = java.util.concurrent.ConcurrentHashMap<String, Boolean>()
    
    val loadErrorDetails = java.util.concurrent.ConcurrentHashMap<String, EnglishUnitLoadErrorDetail>()

    fun getLoadErrorDetail(courseId: String, unitId: String): EnglishUnitLoadErrorDetail? {
        return loadErrorDetails["$courseId:$unitId"]
    }

    fun loadManifest(context: Context, courseId: String = "english_pep_2013_g3_s1"): EnglishCourseManifest? {
        manifestCache[courseId]?.let { return it }
        val regex = Regex("english_pep_2013_g(\\d)_s(\\d)")
        val match = regex.matchEntire(courseId)
        val manifestPath = if (match != null) {
            val gradeNum = match.groupValues[1]
            val semesterNum = match.groupValues[2]
            "english/pep_2013/grade$gradeNum/semester$semesterNum/course_manifest.json"
        } else {
            "english/pep_2013/grade3/semester1/course_manifest.json"
        }
        return try {
            val jsonString = context.assets.open(manifestPath)
                .bufferedReader()
                .use { it.readText() }
            val adapter = moshi.adapter(EnglishCourseManifest::class.java)
            val manifest = adapter.fromJson(jsonString)
            if (manifest != null) {
                manifestCache[courseId] = manifest
            }
            manifest
        } catch (e: Exception) {
            e.printStackTrace()
            if (courseId != "english_pep_2013_g3_s1") {
                loadManifest(context, "english_pep_2013_g3_s1")
            } else {
                null
            }
        }
    }

    private val recycleCache = java.util.concurrent.ConcurrentHashMap<String, EnglishRecycleContent>()

    fun getUnitAssetPath(courseId: String, unitId: String): String? {
        val regex = Regex("english_pep_2013_g(\\d)_s(\\d)")
        val match = regex.matchEntire(courseId) ?: return null
        val gradeNum = match.groupValues[1]
        val semesterNum = match.groupValues[2]
        val gradeDir = "grade$gradeNum"
        val semesterDir = "semester$semesterNum"

        val filename = when {
            unitId.endsWith("_u1") -> "unit_01.json"
            unitId.endsWith("_u2") -> "unit_02.json"
            unitId.endsWith("_u3") -> "unit_03.json"
            unitId.endsWith("_u4") -> "unit_04.json"
            unitId.endsWith("_u5") -> "unit_05.json"
            unitId.endsWith("_u6") -> "unit_06.json"
            unitId.endsWith("_recycle_1") -> "recycle_01.json"
            unitId.endsWith("_recycle_2") -> "recycle_02.json"
            else -> null
        } ?: return null
        return "english/pep_2013/$gradeDir/$semesterDir/$filename"
    }

    fun loadRecycle(context: Context, courseId: String, recycleId: String): EnglishRecycleContent? {
        val cacheKey = "$courseId:$recycleId"
        if (contentReadyCache[cacheKey] == false) return null
        recycleCache[cacheKey]?.let { return it }
        val path = getUnitAssetPath(courseId, recycleId) ?: return null
        return try {
            val jsonString = context.assets.open(path)
                .bufferedReader()
                .use { it.readText() }
            val adapter = moshi.adapter(EnglishRecycleContent::class.java)
            val recycle = adapter.fromJson(jsonString)
            if (recycle != null) {
                val isValid = EnglishContentValidator.validateRecycle(context, recycle)
                if (!isValid) {
                    loadErrorDetails[cacheKey] = EnglishUnitLoadErrorDetail(
                        courseId = courseId,
                        unitId = recycleId,
                        assetPath = path,
                        failureStage = "VALIDATION",
                        exceptionClass = "ValidationError",
                        message = "EnglishContentValidator check failed for Recycle."
                    )
                    contentReadyCache[cacheKey] = false
                    return null
                }
                recycleCache[cacheKey] = recycle
                contentReadyCache[cacheKey] = true
            }
            recycle
        } catch (e: Exception) {
            e.printStackTrace()
            loadErrorDetails[cacheKey] = EnglishUnitLoadErrorDetail(
                courseId = courseId,
                unitId = recycleId,
                assetPath = path,
                failureStage = "JSON_PARSE",
                exceptionClass = e.javaClass.simpleName,
                message = e.localizedMessage
            )
            contentReadyCache[cacheKey] = false
            null
        }
    }

    fun isUnitContentReady(context: Context, courseId: String, unitId: String): Boolean {
        val cacheKey = "$courseId:$unitId"
        contentReadyCache[cacheKey]?.let { return it }
        val ready = if (unitId.contains("recycle")) {
            loadRecycle(context, courseId, unitId) != null
        } else {
            loadUnit(context, courseId, unitId) != null
        }
        contentReadyCache[cacheKey] = ready
        return ready
    }

    fun loadUnit(context: Context, courseId: String, unitId: String): EnglishUnit? {
        val cacheKey = "$courseId:$unitId"
        if (contentReadyCache[cacheKey] == false) return null
        unitCache[cacheKey]?.let { return it }
        val path = getUnitAssetPath(courseId, unitId) ?: return null
        return try {
            val jsonString = context.assets.open(path)
                .bufferedReader()
                .use { it.readText() }
            val adapter = moshi.adapter(EnglishUnit::class.java)
            val unit = adapter.fromJson(jsonString)
            if (unit != null) {
                val isValid = EnglishContentValidator.validateUnit(context, unit)
                if (!isValid) {
                    loadErrorDetails[cacheKey] = EnglishUnitLoadErrorDetail(
                        courseId = courseId,
                        unitId = unitId,
                        assetPath = path,
                        failureStage = "VALIDATION",
                        exceptionClass = "ValidationError",
                        message = "EnglishContentValidator checked failed."
                    )
                    contentReadyCache[cacheKey] = false
                    return null
                }
                unitCache[cacheKey] = unit
                contentReadyCache[cacheKey] = true
            }
            unit
        } catch (e: Exception) {
            e.printStackTrace()
            loadErrorDetails[cacheKey] = EnglishUnitLoadErrorDetail(
                courseId = courseId,
                unitId = unitId,
                assetPath = path,
                failureStage = "JSON_PARSE",
                exceptionClass = e.javaClass.simpleName,
                message = e.localizedMessage
            )
            contentReadyCache[cacheKey] = false
            null
        }
    }

    fun getUnitContentAvailability(context: Context, courseId: String, unitId: String): ContentAvailability {
        val manifest = loadManifest(context, courseId)
        val unitSummary = manifest?.units?.find { it.unitId == unitId }
        if (unitSummary?.contentStatus == "UNDER_CONSTRUCTION") {
            return ContentAvailability.NOT_IMPLEMENTED
        }
        val path = getUnitAssetPath(courseId, unitId) ?: return ContentAvailability.NOT_IMPLEMENTED
        val isReady = isUnitContentReady(context, courseId, unitId)
        if (isReady) return ContentAvailability.READY
        val err = getLoadErrorDetail(courseId, unitId)
        return if (err != null) ContentAvailability.LOAD_ERROR else ContentAvailability.NOT_IMPLEMENTED
    }

    fun getUnitLockReason(
        context: Context,
        courseId: String,
        unitSummary: EnglishUnitSummary,
        units: List<EnglishUnitSummary>,
        policy: EnglishAccessPolicy = EnglishProgressManager.getAccessPolicy(context)
    ): EnglishUnitLockReason {
        val availability = getUnitContentAvailability(context, courseId, unitSummary.unitId)
        return when (availability) {
            ContentAvailability.LOAD_ERROR -> EnglishUnitLockReason.DATA_LOAD_ERROR
            ContentAvailability.NOT_IMPLEMENTED -> EnglishUnitLockReason.CONTENT_NOT_READY
            ContentAvailability.READY -> {
                when (policy) {
                    EnglishAccessPolicy.ALL_READY_CONTENT_OPEN -> EnglishUnitLockReason.NONE
                    EnglishAccessPolicy.SEQUENTIAL -> {
                        val index = units.indexOfFirst { it.unitId == unitSummary.unitId }
                        if (index <= 0) {
                            EnglishUnitLockReason.NONE
                        } else {
                            val prevUnit = units[index - 1]
                            if (!EnglishProgressManager.isUnitCompleted(context, courseId, prevUnit.unitId)) {
                                EnglishUnitLockReason.PREVIOUS_UNIT_NOT_COMPLETED
                            } else {
                                EnglishUnitLockReason.NONE
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class ContentAvailability {
    READY,
    NOT_IMPLEMENTED,
    LOAD_ERROR
}

enum class EnglishUnitLockReason {
    NONE,
    PREVIOUS_UNIT_NOT_COMPLETED,
    CONTENT_NOT_READY,
    DATA_LOAD_ERROR
}
