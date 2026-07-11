package com.example.util

object PromptSecurityUtil {

    const val POLICY_TEST_SAFE = "TEST_SAFE"         // 正式测验安全模式（严禁泄露答案）
    const val POLICY_PRACTICE_HINT = "PRACTICE_HINT" // 练习提示模式（答案可见，显示练习模式标识）
    const val POLICY_REVIEW_ONLY = "REVIEW_ONLY"     // 复盘模式（结算页、错因对比显示完整答案）

    /**
     * 构建单字或短语的安全语境提示（把 targetAnswer 替换为 __）
     * 规则：
     * 1. clueText = "白昼 / 昼夜", targetAnswer = "昼" -> "白__ / __夜"
     * 2. clueText = "破晓", targetAnswer = "晓" -> "破__"
     * 3. clueText = "耕耘", targetAnswer = "耘" -> "耕__"
     * 4. clueText = "妒忌", targetAnswer = "妒" -> "__忌"
     * 5. 如果 clueText 不包含 targetAnswer：输出 clueText，未遮蔽则追加说明
     */
    fun buildSafeCluePrompt(clueText: String, targetAnswer: String): String {
        val trimmedClue = clueText.trim()
        val trimmedTarget = targetAnswer.trim()

        if (trimmedClue.isBlank()) return "请根据语音写出词语"
        if (trimmedTarget.isBlank()) return trimmedClue

        // 如果 clueText 已经被遮蔽（包含 __），且不泄露答案，直接返回
        if ((trimmedClue.contains("__") || trimmedClue.contains("_")) && !isPromptLeakingAnswer(trimmedClue, trimmedTarget)) {
            return trimmedClue
        }

        var result = trimmedClue
        var hasReplaced = false

        trimmedTarget.forEach { ch ->
            if (ch.toString().isNotBlank() && result.contains(ch)) {
                result = result.replace(ch.toString(), "__")
                hasReplaced = true
            }
        }

        if (!hasReplaced) {
            return "$trimmedClue（请根据语音写出目标字）"
        }

        return result
    }

    /**
     * 遮蔽提示文本中的目标字答案
     */
    fun maskTargetAnswerInPrompt(prompt: String, targetAnswer: String): String {
        val trimmedTarget = targetAnswer.trim()
        val trimmedPrompt = prompt.trim()

        if (trimmedPrompt.isBlank()) {
            return "请根据语音写出词语"
        }

        if (trimmedTarget.isBlank()) {
            return trimmedPrompt
        }

        // 如果 prompt 完全等于 targetAnswer
        if (trimmedPrompt == trimmedTarget) {
            return if (trimmedTarget.length == 1) "请根据语音写出这个字" else "请根据语音写出词语"
        }

        // 如果 prompt 已经做过遮蔽（包含 __ 或 _），并且不再裸露包含 targetAnswer，说明是已生成的安全提示，直接返回
        if ((trimmedPrompt.contains("__") || trimmedPrompt.contains("_")) && !isPromptLeakingAnswer(trimmedPrompt, trimmedTarget)) {
            return trimmedPrompt
        }

        var result = trimmedPrompt
        var maskedCount = 0

        // 逐字替换 targetAnswer 中的每个汉字为 "__"
        trimmedTarget.forEach { ch ->
            if (ch.toString().isNotBlank() && result.contains(ch)) {
                result = result.replace(ch.toString(), "__")
                maskedCount++
            }
        }

        // 如果替换后没有产生任何遮蔽（即 prompt 中不含 targetAnswer，且 prompt 本身是类似语境词）
        if (maskedCount == 0 && !result.contains("__") && !result.contains("_")) {
            return result
        }

        // 如果全被替换成了下划线或空格，或者与 targetAnswer 相同
        if (result == trimmedTarget || result.all { it == '_' || it.isWhitespace() }) {
            return if (trimmedTarget.length == 1) "请根据语音写出这个字" else "请根据语音写出词语"
        }

        return result
    }

    /**
     * 判断提示词在正式听写时是否会泄露答案
     */
    fun isPromptLeakingAnswer(prompt: String, targetAnswer: String): Boolean {
        if (targetAnswer.isBlank() || prompt.isBlank()) return false
        val target = targetAnswer.trim()
        val p = prompt.trim()
        if (p == target) return true
        return target.any { ch -> p.contains(ch) }
    }
}
