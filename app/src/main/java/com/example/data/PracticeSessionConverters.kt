package com.example.data

import androidx.room.TypeConverter
import com.example.ui.PointData
import org.json.JSONArray
import org.json.JSONObject

class PracticeSessionConverters {
    @TypeConverter
    fun fromQuestionResultsList(value: List<QuestionResult>?): String {
        return fromQuestionResultsListStatic(value)
    }

    @TypeConverter
    fun toQuestionResultsList(value: String?): List<QuestionResult> {
        return toQuestionResultsListStatic(value)
    }

    companion object {
        fun fromQuestionResultsListStatic(value: List<QuestionResult>?): String {
            if (value == null) return "[]"
            val arr = JSONArray()
            for (q in value) {
                val qObj = JSONObject().apply {
                    put("questionId", q.questionId)
                    put("correctText", q.correctText)
                    put("autoResult", q.autoResult)
                    put("finalResult", q.finalResult)
                    put("needParentReview", q.needParentReview)
                    put("parentOverrideResult", q.parentOverrideResult ?: JSONObject.NULL)
                    put("parentReviewedAt", q.parentReviewedAt ?: JSONObject.NULL)
                    put("promptMode", q.promptMode)
                    put("hiddenIndicesStr", q.hiddenIndicesStr)
                    put("visiblePrompt", q.visiblePrompt)
                    put("ttsPrompt", q.ttsPrompt)
                    put("targetAnswer", q.targetAnswer)
                    put("errorReason", q.errorReason)
                    
                    val charArr = JSONArray()
                    for (c in q.charResults) {
                        val cObj = JSONObject().apply {
                            put("charIndex", c.charIndex)
                            put("canvasWidth", c.canvasWidth.toDouble())
                            put("canvasHeight", c.canvasHeight.toDouble())
                            put("isBlank", c.isBlank)
                            put("expectedChar", c.expectedChar)
                            put("recognizedText", c.recognizedText)
                            
                            val candArr = JSONArray()
                            c.candidates.forEach { candArr.put(it) }
                            put("candidates", candArr)
                            
                            put("confidenceLevel", c.confidenceLevel)
                            put("isLikelyCorrect", c.isLikelyCorrect)
                            put("errorMessage", c.errorMessage ?: JSONObject.NULL)
                            
                            val ptsListArr = JSONArray()
                            for (stroke in c.pointsList) {
                                val strokeArr = JSONArray()
                                for (pt in stroke) {
                                    val ptObj = JSONObject().apply {
                                        put("x", pt.x.toDouble())
                                        put("y", pt.y.toDouble())
                                        put("t", pt.timestamp)
                                    }
                                    strokeArr.put(ptObj)
                                }
                                ptsListArr.put(strokeArr)
                            }
                            put("pointsList", ptsListArr)
                        }
                        charArr.put(cObj)
                    }
                    put("charResults", charArr)
                }
                arr.put(qObj)
            }
            return arr.toString()
        }

        fun toQuestionResultsListStatic(value: String?): List<QuestionResult> {
            if (value.isNullOrEmpty()) return emptyList()
            val list = mutableListOf<QuestionResult>()
            try {
                val arr = JSONArray(value)
                for (i in 0 until arr.length()) {
                    val qObj = arr.getJSONObject(i)
                    
                    val charResults = mutableListOf<CharResult>()
                    val charArr = qObj.getJSONArray("charResults")
                    for (j in 0 until charArr.length()) {
                        val cObj = charArr.getJSONObject(j)
                        
                        val cands = mutableListOf<String>()
                        val candArr = cObj.getJSONArray("candidates")
                        for (k in 0 until candArr.length()) {
                            cands.add(candArr.getString(k))
                        }
                        
                        val ptsList = mutableListOf<List<PointData>>()
                        val ptsListArr = cObj.getJSONArray("pointsList")
                        for (k in 0 until ptsListArr.length()) {
                            val strokeArr = ptsListArr.getJSONArray(k)
                            val strokePts = mutableListOf<PointData>()
                            for (m in 0 until strokeArr.length()) {
                                val ptObj = strokeArr.getJSONObject(m)
                                strokePts.add(PointData(
                                    x = ptObj.getDouble("x").toFloat(),
                                    y = ptObj.getDouble("y").toFloat(),
                                    timestamp = ptObj.optLong("t", System.currentTimeMillis())
                                ))
                            }
                            ptsList.add(strokePts)
                        }
                        
                        charResults.add(CharResult(
                            charIndex = cObj.getInt("charIndex"),
                            pointsList = ptsList,
                            canvasWidth = cObj.getDouble("canvasWidth").toFloat(),
                            canvasHeight = cObj.getDouble("canvasHeight").toFloat(),
                            isBlank = cObj.getBoolean("isBlank"),
                            expectedChar = cObj.getString("expectedChar"),
                            recognizedText = cObj.getString("recognizedText"),
                            candidates = cands,
                            confidenceLevel = cObj.getString("confidenceLevel"),
                            isLikelyCorrect = cObj.getBoolean("isLikelyCorrect"),
                            errorMessage = if (cObj.isNull("errorMessage")) null else cObj.getString("errorMessage")
                        ))
                    }
                    
                    list.add(QuestionResult(
                        questionId = qObj.getInt("questionId"),
                        correctText = qObj.getString("correctText"),
                        charResults = charResults,
                        autoResult = qObj.getString("autoResult"),
                        finalResult = qObj.getString("finalResult"),
                        needParentReview = qObj.getBoolean("needParentReview"),
                        parentOverrideResult = if (qObj.isNull("parentOverrideResult")) null else qObj.getString("parentOverrideResult"),
                        parentReviewedAt = if (qObj.isNull("parentReviewedAt")) null else qObj.getLong("parentReviewedAt"),
                        promptMode = qObj.optString("promptMode", "FULL_WORD"),
                        hiddenIndicesStr = qObj.optString("hiddenIndicesStr", ""),
                        visiblePrompt = qObj.optString("visiblePrompt", ""),
                        ttsPrompt = qObj.optString("ttsPrompt", ""),
                        targetAnswer = qObj.optString("targetAnswer", ""),
                        errorReason = qObj.optString("errorReason", "")
                    ))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return list
        }
    }
}
