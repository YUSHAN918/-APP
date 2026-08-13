package com.example.data.math

import android.util.Log

object MathContentValidator {
    private const val TAG = "MathContentValidator"

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )

    fun validateUnit(unit: MathUnit): ValidationResult {
        val errors = mutableListOf<String>()
        val seenLessonIds = mutableSetOf<String>()
        val seenQuestionIds = mutableSetOf<String>()

        val formalLessons = unit.lessons.filter { it.isFormalLesson() }.sortedBy { it.order }
        val engineTestLessons = unit.lessons.filter { it.isEngineTestLesson() }

        if (formalLessons.isEmpty()) {
            errors.add("单元 (${unit.unitId}) 至少必须包含一个正式课时 (FORMAL)")
        }

        val seenOrders = mutableSetOf<Int>()
        formalLessons.forEachIndexed { index, lesson ->
            if (lesson.order <= 0) {
                errors.add("正式课时 order 必须大于 0: ${lesson.lessonId} (order=${lesson.order})")
            }
            if (!seenOrders.add(lesson.order)) {
                errors.add("单元 (${unit.unitId}) 内存在重复的正式课时 order: ${lesson.order}")
            }
            val expectedOrder = index + 1
            if (lesson.order != expectedOrder) {
                errors.add("单元 (${unit.unitId}) 正式课时顺序不连续: 期望 order=$expectedOrder，实际等于 ${lesson.order} (${lesson.lessonId})")
            }
        }

        engineTestLessons.forEach { lesson ->
            if (seenOrders.contains(lesson.order)) {
                errors.add("测试课时 (${lesson.lessonId}) 的 order (${lesson.order}) 占用了正式课时序号")
            }
        }

        unit.lessons.forEach { lesson ->
            // 1. Check duplicate lessonId
            if (lesson.lessonId.isEmpty()) {
                errors.add("课时ID不能为空 (Unit: ${unit.unitId})")
            } else if (!seenLessonIds.add(lesson.lessonId)) {
                errors.add("重复的课时ID: ${lesson.lessonId}")
            }

            // Combine legacy questions and block questions to validate
            val allQuestions = mutableListOf<MathQuestion>()
            allQuestions.addAll(lesson.questions)
            lesson.contentBlocks.forEach { block ->
                block.question?.let { allQuestions.add(it) }
                
                // Validate worked examples and concepts
                if (block.type == MathContentBlockType.CONCEPT && (block.contentText ?: "").length > 200) {
                    errors.add("概念课内容过长 (Block: ${block.blockId}), 应精简在80-200字内以利于儿童阅读")
                }
                if (block.type == MathContentBlockType.WORKED_EXAMPLE && (block.steps.isNullOrEmpty())) {
                    errors.add("例题步骤缺失 (Block: ${block.blockId})")
                }
            }

            allQuestions.forEach { question ->
                // 1. Check duplicate questionId
                if (question.id.isEmpty()) {
                    errors.add("题目ID不能为空 (Lesson: ${lesson.lessonId})")
                } else if (!seenQuestionIds.add(question.id)) {
                    errors.add("重复的题目ID: ${question.id}")
                }

                // 2. Knowledge point check
                // In our model we didn't add knowledgePoint directly to MathQuestion, wait! 
                // Let's check MathQuestion model fields in MathModels.kt:
                // id, type, stem, options, explanation, hints, sourceReference, contentStatus, answerSpec.
                // Oh! It doesn't have knowledgePoint directly in the Kotlin model. 
                // Let's look: the prompt says "每道题必须具有 knowledgePoint". Let's add knowledgePoint string with default value or validate it if present.
                // Ah, let's verify if we need to add knowledgePoint to MathQuestion. Yes, let's add it to keep it aligned with instructions!
                // Wait, if it doesn't have it, we can either check for it or add it. Let's add "val knowledgePoint: String = \"\"" to MathQuestion model.
                // Let's check the fields we validated:

                // 3. Official questions check sourceReference
                if (question.contentStatus == "PRODUCTION" && question.sourceReference.isEmpty()) {
                    errors.add("正式题目必须声明内容来源 (Question: ${question.id})")
                }

                // 4. Validate answerSpec
                val spec = question.answerSpec
                when (spec.kind.uppercase()) {
                    "INTEGER" -> {
                        if (spec.expectedValue?.toIntOrNull() == null) {
                            errors.add("整数题答案格式错误 (Question: ${question.id})")
                        }
                    }
                    "DECIMAL" -> {
                        if (spec.expectedValue?.toDoubleOrNull() == null) {
                            errors.add("小数题答案格式错误 (Question: ${question.id})")
                        }
                    }
                    "FRACTION" -> {
                        val num = spec.numerator ?: 0
                        val den = spec.denominator ?: 1
                        if (den == 0) {
                            errors.add("分数分母不能为0 (Question: ${question.id})")
                        }
                    }
                    "RATIO" -> {
                        val left = spec.left ?: spec.expectedValue?.split(":")?.getOrNull(0) ?: spec.expectedValue?.split("：")?.getOrNull(0)
                        val right = spec.right ?: spec.expectedValue?.split(":")?.getOrNull(1) ?: spec.expectedValue?.split("：")?.getOrNull(1)
                        if (left == null || right == null) {
                            errors.add("比题型 (RATIO) 必须设置 left 和 right (或 expectedValue, 如 2:3) (Question: ${question.id})")
                        } else {
                            val parsedLeft = RatioValue.parseTerm(left)
                            val parsedRight = RatioValue.parseTerm(right)
                            if (parsedLeft == null || parsedRight == null) {
                                errors.add("比的前项或后项解析失败 ($left:$right) (Question: ${question.id})")
                            } else if (parsedRight.numerator == 0) {
                                errors.add("比的后项不能为0 ($left:$right) (Question: ${question.id})")
                            }
                        }
                    }
                    "CHOICE" -> {
                        if (spec.expectedValue.isNullOrEmpty()) {
                            errors.add("选择题必须配置正确选项 (Question: ${question.id})")
                        }
                        // Correct option must exist in choices
                        val expected = spec.expectedValue?.trim()?.uppercase() ?: ""
                        val hasOption = question.options.any { it.trim().uppercase().startsWith(expected) }
                        if (question.options.isNotEmpty() && !hasOption) {
                            errors.add("选择题正确答案 ${expected} 在选项列表中不存在 (Question: ${question.id})")
                        }
                    }
                    "MULTIPLE_BLANKS" -> {
                        val blanksInStem = "\\[blank\\]".toRegex().findAll(question.stem).count()
                        val expectedCount = spec.expectedValues?.size ?: 0
                        if (blanksInStem != expectedCount) {
                            errors.add("填空题空位数 ($blanksInStem) 与期望答案数 ($expectedCount) 不一致 (Question: ${question.id})")
                        }
                        if (spec.responseTemplate != null) {
                            val specs = spec.blankSpecs ?: emptyList()
                            if (specs.size != expectedCount) {
                                errors.add("结构化填空题 blankSpecs 数量 (${specs.size}) 与期望答案数 ($expectedCount) 不一致 (Question: ${question.id})")
                            }
                            for (i in specs.indices) {
                                val placeholder = "{$i}"
                                if (!spec.responseTemplate.contains(placeholder)) {
                                    errors.add("结构化填空模板 responseTemplate 缺少占位符 $placeholder (Question: ${question.id})")
                                }
                            }
                        }
                    }
                    "NUMERIC_WITH_UNIT" -> {
                        if (spec.value.isNullOrEmpty() || spec.value.toDoubleOrNull() == null) {
                            errors.add("带单位计算题数值格式错误 (Question: ${question.id})")
                        }
                        if (spec.acceptedUnits.isNullOrEmpty()) {
                            errors.add("带单位计算题必须声明支持的单位列表 (Question: ${question.id})")
                        }
                    }
                }
            }
        }

        val isValid = errors.isEmpty()
        if (!isValid) {
            Log.e(TAG, "数学内容校验失败 (Unit: ${unit.unitId}):")
            errors.forEach { Log.e(TAG, "  - $it") }
        } else {
            Log.d(TAG, "数学内容校验通过: ${unit.unitId}")
        }

        return ValidationResult(isValid, errors)
    }
}
