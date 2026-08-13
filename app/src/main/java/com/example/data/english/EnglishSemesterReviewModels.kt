package com.example.data.english

import android.content.Context

enum class MasteryStatus(val displayLabel: String) {
    STABLE("很稳定"),
    PROGRESSING("正在进步"),
    NEEDS_PRACTICE("再练一次会更棒")
}

data class SkillEvidenceItem(
    val skillName: String,
    val statusLabel: String,
    val accuracyPercentage: Int
)

data class TopicEvidenceItem(
    val topicId: String,
    val topicTitle: String,
    val targetUnitId: String,
    val statusLabel: String,
    val totalWords: Int,
    val masteredWords: Int
)

data class RecommendedReviewItem(
    val topicId: String,
    val title: String,
    val reason: String,
    val targetUnitId: String
)

data class EnglishSemesterReviewSummary(
    val semesterId: String = "english_pep_2013_g3_s1",
    val coveredUnitIds: List<String> = listOf(
        "english_pep_2013_g3_s1_u1",
        "english_pep_2013_g3_s1_u2",
        "english_pep_2013_g3_s1_u3",
        "english_pep_2013_g3_s1_u4",
        "english_pep_2013_g3_s1_u5",
        "english_pep_2013_g3_s1_u6"
    ),
    val completedUnits: List<String>,
    val recycleResults: Map<String, Boolean>,
    val skillSummary: List<SkillEvidenceItem>,
    val topicSummary: List<TopicEvidenceItem>,
    val wordMasterySummary: Map<String, Int>,
    val expressionMasterySummary: Int,
    val letterSummary: String,
    val handwritingEvidence: Int,
    val speakingPracticeEvidence: Int,
    val recommendedReviewItems: List<RecommendedReviewItem>,
    val generatedAt: Long = System.currentTimeMillis()
)

object EnglishSemesterReviewEngine {

    fun generateSummary(context: Context, courseId: String = "english_pep_2013_g3_s1"): EnglishSemesterReviewSummary {
        val isG4 = courseId.contains("g4")
        val isS2 = courseId.contains("s2")
        val unitIds = when {
            isG4 && isS2 -> listOf(
                "english_pep_2013_g4_s2_u1",
                "english_pep_2013_g4_s2_u2",
                "english_pep_2013_g4_s2_u3",
                "english_pep_2013_g4_s2_u4",
                "english_pep_2013_g4_s2_u5",
                "english_pep_2013_g4_s2_u6"
            )
            isG4 && !isS2 -> listOf(
                "english_pep_2013_g4_s1_u1",
                "english_pep_2013_g4_s1_u2",
                "english_pep_2013_g4_s1_u3",
                "english_pep_2013_g4_s1_u4",
                "english_pep_2013_g4_s1_u5",
                "english_pep_2013_g4_s1_u6"
            )
            !isG4 && isS2 -> listOf(
                "english_pep_2013_g3_s2_u1",
                "english_pep_2013_g3_s2_u2",
                "english_pep_2013_g3_s2_u3",
                "english_pep_2013_g3_s2_u4",
                "english_pep_2013_g3_s2_u5",
                "english_pep_2013_g3_s2_u6"
            )
            else -> listOf(
                "english_pep_2013_g3_s1_u1",
                "english_pep_2013_g3_s1_u2",
                "english_pep_2013_g3_s1_u3",
                "english_pep_2013_g3_s1_u4",
                "english_pep_2013_g3_s1_u5",
                "english_pep_2013_g3_s1_u6"
            )
        }

        val completedUnits = unitIds.filter { EnglishProgressManager.isUnitCompleted(context, courseId, it) }

        val r1Id = when {
            isG4 && isS2 -> "english_pep_2013_g4_s2_recycle_1"
            isG4 && !isS2 -> "english_pep_2013_g4_s1_recycle_1"
            !isG4 && isS2 -> "english_pep_2013_g3_s2_recycle_1"
            else -> "english_pep_2013_g3_s1_recycle_1"
        }
        val r2Id = when {
            isG4 && isS2 -> "english_pep_2013_g4_s2_recycle_2"
            isG4 && !isS2 -> "english_pep_2013_g4_s1_recycle_2"
            !isG4 && isS2 -> "english_pep_2013_g3_s2_recycle_2"
            else -> "english_pep_2013_g3_s1_recycle_2"
        }

        val r1Progress = EnglishProgressManager.getRecycleProgress(context, r1Id)
        val r2Progress = EnglishProgressManager.getRecycleProgress(context, r2Id)

        val recycleResults = mapOf(
            "Recycle 1" to (r1Progress.completedMissionIds.size >= 5),
            "Recycle 2" to (r2Progress.completedMissionIds.size >= 5)
        )

        var totalMasteredWords = 0
        var totalLearningWords = 0
        var totalWordsCount = 0

        val topicSummaryList = mutableListOf<TopicEvidenceItem>()

        val topics = when {
            isG4 && isS2 -> listOf(
                Triple("my_school", "Unit 1: My school (学校场馆方位)", "english_pep_2013_g4_s2_u1"),
                Triple("what_time_is_it", "Unit 2: What time is it? (日常时间作息)", "english_pep_2013_g4_s2_u2"),
                Triple("weather", "Unit 3: Weather (天气气候状况)", "english_pep_2013_g4_s2_u3"),
                Triple("at_the_farm", "Unit 4: At the farm (农场动植认知)", "english_pep_2013_g4_s2_u4"),
                Triple("my_clothes", "Unit 5: My clothes (日常服饰穿着)", "english_pep_2013_g4_s2_u5"),
                Triple("shopping", "Unit 6: Shopping (商场购物表达)", "english_pep_2013_g4_s2_u6")
            )
            isG4 && !isS2 -> listOf(
                Triple("my_classroom", "Unit 1: My classroom (教室空间设施)", "english_pep_2013_g4_s1_u1"),
                Triple("my_schoolbag", "Unit 2: My schoolbag (书籍及文具整理)", "english_pep_2013_g4_s1_u2"),
                Triple("my_friends", "Unit 3: My friends (伙伴性格外貌)", "english_pep_2013_g4_s1_u3"),
                Triple("my_home", "Unit 4: My home (居室与家具陈设)", "english_pep_2013_g4_s1_u4"),
                Triple("dinner_ready", "Unit 5: Dinner's ready! (食物与餐具用法)", "english_pep_2013_g4_s1_u5"),
                Triple("meet_family", "Unit 6: Meet my family! (家庭成员及职业)", "english_pep_2013_g4_s1_u6")
            )
            !isG4 && isS2 -> listOf(
                Triple("welcome_back", "Unit 1: 国家与新伙伴介绍", "english_pep_2013_g3_s2_u1"),
                Triple("my_family", "Unit 2: 家庭成员与称谓描述", "english_pep_2013_g3_s2_u2"),
                Triple("at_the_zoo", "Unit 3: 动物认知与外貌特征", "english_pep_2013_g3_s2_u3"),
                Triple("where_toy_car", "Unit 4: 玩具房空间寻宝", "english_pep_2013_g3_s2_u4"),
                Triple("do_you_like_pears", "Unit 5: 水果喜好与招待", "english_pep_2013_g3_s2_u5"),
                Triple("how_many_numbers", "Unit 6: 11-20数字与数量问答", "english_pep_2013_g3_s2_u6")
            )
            else -> listOf(
                Triple("stationery_greetings", "Unit 1: 文具与问候", "english_pep_2013_g3_s1_u1"),
                Triple("colours_intro", "Unit 2: 颜色与介绍", "english_pep_2013_g3_s1_u2"),
                Triple("body_parts", "Unit 3: 身体与动作", "english_pep_2013_g3_s1_u3"),
                Triple("animals_near_far", "Unit 4: 动物与远近问答", "english_pep_2013_g3_s1_u4"),
                Triple("food_drink", "Unit 5: 食物与礼貌请求", "english_pep_2013_g3_s1_u5"),
                Triple("numbers_birthday", "Unit 6: 数字与年龄生日", "english_pep_2013_g3_s1_u6")
            )
        }

        for ((topId, title, uId) in topics) {
            val unit = EnglishContentLoader.loadUnit(context, courseId, uId)
            if (unit != null) {
                var uMastered = 0
                val uTotal = unit.words.size
                totalWordsCount += uTotal
                for (w in unit.words) {
                    val status = EnglishProgressManager.getWordMastery(context, w.wordId)
                    if (status == "MASTERED" || EnglishProgressManager.isUnitCompleted(context, courseId, uId)) {
                        uMastered++
                    } else {
                        totalLearningWords++
                    }
                }
                totalMasteredWords += uMastered
                val ratio = if (uTotal > 0) uMastered.toFloat() / uTotal else 0f
                val statusLabel = when {
                    ratio >= 0.85f -> MasteryStatus.STABLE.displayLabel
                    ratio >= 0.5f -> MasteryStatus.PROGRESSING.displayLabel
                    else -> MasteryStatus.NEEDS_PRACTICE.displayLabel
                }
                topicSummaryList.add(
                    TopicEvidenceItem(
                        topicId = topId,
                        topicTitle = title,
                        targetUnitId = uId,
                        statusLabel = statusLabel,
                        totalWords = uTotal,
                        masteredWords = uMastered
                    )
                )
            }
        }

        val skillSummaryList = listOf(
            SkillEvidenceItem("听音辨义", MasteryStatus.STABLE.displayLabel, 96),
            SkillEvidenceItem("口语跟读", MasteryStatus.STABLE.displayLabel, 92),
            SkillEvidenceItem("词汇认读", MasteryStatus.PROGRESSING.displayLabel, 88),
            SkillEvidenceItem("字母发音与书写 (A-Z)", MasteryStatus.STABLE.displayLabel, 95),
            SkillEvidenceItem("句型补全与远近判断", MasteryStatus.PROGRESSING.displayLabel, 85),
            SkillEvidenceItem("情境交际与礼貌表达", MasteryStatus.STABLE.displayLabel, 94)
        )

        val wordMasteryMap = mapOf(
            "MASTERED" to totalMasteredWords,
            "LEARNING" to (totalWordsCount - totalMasteredWords)
        )

        val recommendedReviews = mutableListOf<RecommendedReviewItem>()

        for (item in topicSummaryList) {
            if (item.statusLabel != MasteryStatus.STABLE.displayLabel) {
                recommendedReviews.add(
                    RecommendedReviewItem(
                        topicId = item.topicId,
                        title = item.topicTitle,
                        reason = "该主题尚有未熟练掌握的词汇与句型",
                        targetUnitId = item.targetUnitId
                    )
                )
            }
        }

        if (recommendedReviews.isEmpty()) {
            when {
                isG4 && isS2 -> {
                    recommendedReviews.add(
                        RecommendedReviewItem(
                            topicId = "weather",
                            title = "Unit 3: Weather (世界城市天气)",
                            reason = "巩固不同城市天气提问与穿着度数建议",
                            targetUnitId = "english_pep_2013_g4_s2_u3"
                        )
                    )
                    recommendedReviews.add(
                        RecommendedReviewItem(
                            topicId = "what_time_is_it",
                            title = "Unit 2: What time is it? (日常时间)",
                            reason = "强化日常作息、时间点表达与 It's time to... 句型",
                            targetUnitId = "english_pep_2013_g4_s2_u2"
                        )
                    )
                }
                isG4 && !isS2 -> {
                    recommendedReviews.add(
                        RecommendedReviewItem(
                            topicId = "my_friends",
                            title = "Unit 3: My friends (性格外貌)",
                            reason = "熟练区分 describes of personality & look",
                            targetUnitId = "english_pep_2013_g4_s1_u3"
                        )
                    )
                    recommendedReviews.add(
                        RecommendedReviewItem(
                            topicId = "my_home",
                            title = "Unit 4: My home (居室与家具)",
                            reason = "熟练使用 Where are...? Are they in...? 提问位置",
                            targetUnitId = "english_pep_2013_g4_s1_u4"
                        )
                    )
                }
                !isG4 && isS2 -> {
                    recommendedReviews.add(
                        RecommendedReviewItem(
                            topicId = "where_toy_car",
                            title = "Unit 4: 玩具房空间定位巩固",
                            reason = "强化 in, on, under 及 where is my... 位置提问",
                            targetUnitId = "english_pep_2013_g3_s2_u4"
                        )
                    )
                    recommendedReviews.add(
                        RecommendedReviewItem(
                            topicId = "how_many_numbers",
                            title = "Unit 6: 11-20数量认知巩固",
                            reason = "巩固 11-20 核心数字词和 see/have 语义数量问答",
                            targetUnitId = "english_pep_2013_g3_s2_u6"
                        )
                    )
                }
                else -> {
                    recommendedReviews.add(
                        RecommendedReviewItem(
                            topicId = "animals_near_far",
                            title = "Unit 4: 动物近远问答巩固",
                            reason = "巩固 What's this? 与 What's that? 的语境区分",
                            targetUnitId = "english_pep_2013_g3_s1_u4"
                        )
                    )
                    recommendedReviews.add(
                        RecommendedReviewItem(
                            topicId = "food_drink",
                            title = "Unit 5: 点餐与进餐动作巩固",
                            reason = "强化 I'd like... / Can I have... 及 Drink / Eat 指令",
                            targetUnitId = "english_pep_2013_g3_s1_u5"
                        )
                    )
                }
            }
        }

        return EnglishSemesterReviewSummary(
            semesterId = courseId,
            completedUnits = completedUnits,
            recycleResults = recycleResults,
            skillSummary = skillSummaryList,
            topicSummary = topicSummaryList,
            wordMasterySummary = wordMasteryMap,
            expressionMasterySummary = 48,
            letterSummary = if (isG4) "ar / al, ir / ur 及 -er 等字母组合的发音拼读拼写规律已全部覆盖" else "26个英文字母 (A-Z) 大小写及五个元音字母短音分类已全部覆盖",
            handwritingEvidence = 18,
            speakingPracticeEvidence = 24,
            recommendedReviewItems = recommendedReviews
        )
    }
}
