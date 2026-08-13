package com.example.data.math

import java.util.Locale

object MathAnswerEvaluator {

    fun evaluate(spec: MathAnswerSpec, userAnswers: List<String>): MathEvaluationResult {
        if (userAnswers.isEmpty()) {
            return MathEvaluationResult.InvalidInput("答案不能为空哦")
        }
        val firstAnswer = userAnswers.first().trim()

        return when (spec.kind.uppercase(Locale.ROOT)) {
            "INTEGER" -> {
                if (firstAnswer.contains(":") || firstAnswer.contains("：")) {
                    return MathEvaluationResult.Incorrect("你写成了化简比，但题目要求【求比值】。求比值的结果是一个数（可写成分数、小数或整数）。")
                }
                val userInt = firstAnswer.toIntOrNull()
                val expectedInt = spec.expectedValue?.trim()?.toIntOrNull()
                if (userInt == null) {
                    MathEvaluationResult.InvalidInput("请输入有效的整数")
                } else if (userInt == expectedInt) {
                    MathEvaluationResult.Correct
                } else {
                    MathEvaluationResult.Incorrect("计算不太对，再检查一下吧")
                }
            }

            "DECIMAL" -> {
                if (firstAnswer.contains(":") || firstAnswer.contains("：")) {
                    return MathEvaluationResult.Incorrect("你写成了化简比，但题目要求【求比值】。求比值的结果是一个数（可写成分数、小数或整数）。")
                }
                val userDouble = firstAnswer.toDoubleOrNull()
                val expectedDouble = spec.expectedValue?.trim()?.toDoubleOrNull()
                if (userDouble == null) {
                    MathEvaluationResult.InvalidInput("请输入有效的小数")
                } else if (expectedDouble == null) {
                    MathEvaluationResult.Incorrect("题目配置异常")
                } else if (kotlin.math.abs(userDouble - expectedDouble) < 0.0001) {
                    MathEvaluationResult.Correct
                } else {
                    MathEvaluationResult.Incorrect("结果有偏差，再算一算")
                }
            }

            "FRACTION" -> {
                if (firstAnswer.contains(":") || firstAnswer.contains("：")) {
                    return MathEvaluationResult.Incorrect("你写成了化简比，但题目要求【求比值】。求比值的结果是一个数（可写成分数、小数或整数）。")
                }
                val userFraction = FractionValue.parse(firstAnswer)
                if (userFraction == null) {
                    return MathEvaluationResult.InvalidInput("请输入格式正确的数或分数，如 1/2")
                }
                val expNumerator = spec.numerator ?: 1
                val expDenominator = spec.denominator ?: 1
                val targetFraction = FractionValue(expNumerator, expDenominator)

                val userSimp = userFraction.simplify()
                val targetSimp = targetFraction.simplify()

                if (userSimp.numerator == targetSimp.numerator && userSimp.denominator == targetSimp.denominator) {
                    if (spec.requireSimplified == true && !userFraction.isSimplified()) {
                        MathEvaluationResult.NotSimplified(firstAnswer)
                    } else {
                        MathEvaluationResult.Correct
                    }
                } else {
                    MathEvaluationResult.Incorrect("分数计算不太对，检查通分和分子相乘吧")
                }
            }

            "RATIO" -> {
                if (userAnswers.size == 1 && (firstAnswer.contains("/") || (firstAnswer.contains(".") && !firstAnswer.contains(":"))) && !firstAnswer.contains(":") && !firstAnswer.contains("：")) {
                    return MathEvaluationResult.Incorrect("你求出了比值，但题目要求【化简比】。化简比的结果应写成前项:后项的形式（如 2:3）。")
                }

                val userRatio = if (userAnswers.size >= 2) {
                    RatioValue.parse(userAnswers[0], userAnswers[1])
                } else {
                    RatioValue.parse(firstAnswer)
                }

                if (userRatio == null) {
                    return MathEvaluationResult.InvalidInput("请填入有效的前项和后项（如 2:3）")
                }

                if (userRatio.right.numerator == 0) {
                    return MathEvaluationResult.InvalidInput("比的后项不能为0哦")
                }

                val expectedLeft = spec.left ?: spec.expectedValue?.split(":")?.getOrNull(0) ?: spec.expectedValue?.split("：")?.getOrNull(0)
                val expectedRight = spec.right ?: spec.expectedValue?.split(":")?.getOrNull(1) ?: spec.expectedValue?.split("：")?.getOrNull(1)

                val targetRatio = if (expectedLeft != null && expectedRight != null) {
                    RatioValue.parse(expectedLeft, expectedRight)
                } else {
                    null
                }

                if (targetRatio == null) {
                    return MathEvaluationResult.Incorrect("题目配置异常：缺失目标比")
                }

                val swappedUserRatio = RatioValue(userRatio.right, userRatio.left)
                val userFrac = userRatio.toFraction()
                val targetFrac = targetRatio.toFraction()
                val swappedFrac = swappedUserRatio.toFraction()

                if (userFrac != targetFrac && swappedFrac == targetFrac) {
                    return MathEvaluationResult.Incorrect("前后项顺序写反了！题目比较的前后项顺序不能颠倒。")
                }

                if (userFrac != targetFrac) {
                    return MathEvaluationResult.Incorrect("计算不正确，请重新检查前项和后项的比值关系")
                }

                val requireIntegerTerms = spec.requireIntegerTerms ?: (spec.requireSimplified != false)
                if (requireIntegerTerms && !userRatio.hasIntegerTerms()) {
                    return MathEvaluationResult.Incorrect("请将小数或分数比化成最简整数比（前后项均为整数）。")
                }

                val requireSimplified = spec.requireSimplified != false
                if (requireSimplified && !userRatio.isSimplestIntegerRatio()) {
                    return MathEvaluationResult.Incorrect("这个比还可以继续化简（如前后项有公因数），请写成最简整数比。")
                }

                MathEvaluationResult.Correct
            }

            "CHOICE" -> {
                val userChoice = firstAnswer.uppercase(Locale.ROOT)
                val expectedChoice = spec.expectedValue?.trim()?.uppercase(Locale.ROOT)
                val extractedUser = if (userChoice.isNotEmpty()) userChoice.first().toString() else ""
                val extractedExpected = if (expectedChoice != null && expectedChoice.isNotEmpty()) expectedChoice.first().toString() else ""
                if (extractedUser == extractedExpected) {
                    MathEvaluationResult.Correct
                } else {
                    MathEvaluationResult.Incorrect("这个选项不太对，请再读一遍题目")
                }
            }

            "MULTIPLE_BLANKS" -> {
                val expectedList = spec.expectedValues ?: emptyList()
                if (userAnswers.size < expectedList.size) {
                    return MathEvaluationResult.InvalidInput("还有空格没有填完哦")
                }
                var allCorrect = true
                for (i in expectedList.indices) {
                    val userAns = userAnswers.getOrNull(i)?.trim() ?: ""
                    val expAns = expectedList[i].trim()
                    if (userAns != expAns) {
                        allCorrect = false
                        break
                    }
                }
                if (allCorrect) {
                    MathEvaluationResult.Correct
                } else {
                    MathEvaluationResult.Incorrect("某些空格里的答案不太对，再检查一下")
                }
            }

            "EXPRESSION" -> {
                val userExpr = firstAnswer.replace("\\s+".toRegex(), "")
                val expectedExpr = (spec.expectedValue ?: "").trim().replace("\\s+".toRegex(), "")
                if (userExpr == expectedExpr) {
                    return MathEvaluationResult.Correct
                }
                val userFrac = FractionValue.parse(userExpr)
                val expFrac = FractionValue.parse(expectedExpr)
                if (userFrac != null && expFrac != null) {
                    val us = userFrac.simplify()
                    val es = expFrac.simplify()
                    if (us.numerator == es.numerator && us.denominator == es.denominator) {
                        return MathEvaluationResult.Correct
                    }
                }
                MathEvaluationResult.Incorrect("表达式不匹配，请按标准简写形式输入")
            }

            "NUMERIC_WITH_UNIT" -> {
                val regex = "([\\d.-]+)(.*)".toRegex()
                val matchResult = regex.matchEntire(firstAnswer)
                if (matchResult == null) {
                    return MathEvaluationResult.InvalidInput("请输入数值和单位，如 6米")
                }
                val numStr = matchResult.groups[1]?.value ?: ""
                val unitStr = matchResult.groups[2]?.value?.trim() ?: ""

                val userVal = numStr.toDoubleOrNull()
                val expectedVal = spec.value?.trim()?.toDoubleOrNull()

                if (userVal == null || expectedVal == null) {
                    return MathEvaluationResult.InvalidInput("数值格式不正确")
                }

                if (kotlin.math.abs(userVal - expectedVal) > 0.0001) {
                    return MathEvaluationResult.Incorrect("数值计算不正确，请重新计算")
                }

                if (unitStr.isEmpty()) {
                    return MathEvaluationResult.UnitMissing(numStr)
                }

                val accepted = spec.acceptedUnits ?: emptyList()
                val isUnitAccepted = accepted.any { it.trim().lowercase(Locale.ROOT) == unitStr.lowercase(Locale.ROOT) }

                if (isUnitAccepted) {
                    MathEvaluationResult.Correct
                } else {
                    MathEvaluationResult.Incorrect("单位不太对，请使用题目要求的单位，例如: " + accepted.joinToString("、"))
                }
            }

            else -> {
                MathEvaluationResult.InvalidInput("未知的题型判断规则")
            }
        }
    }
}
