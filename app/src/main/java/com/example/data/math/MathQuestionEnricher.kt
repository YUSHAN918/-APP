package com.example.data.math

object MathQuestionEnricher {
    fun enrich(question: MathQuestion): MathQuestion {
        val spec = question.answerSpec
        if (question.type != MathQuestionType.FILL_BLANK) return question

        val (template, specs) = when (question.id) {
            "q_u2_l1_q2" -> {
                "以学校为观测点，图书馆在学校的 {0} 偏 {1} {2}° 方向上，距离学校 {3} 米。" to listOf(
                    MathBlankSpec("0", MathBlankInputType.CHOICE_TEXT, "基准方向", listOf("南"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("1", MathBlankInputType.CHOICE_TEXT, "偏转方向", listOf("西"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("2", MathBlankInputType.INTEGER, "角度", listOf("30"), suffix = "°"),
                    MathBlankSpec("3", MathBlankInputType.INTEGER, "距离", listOf("400"), suffix = "米")
                )
            }
            "q_u2_l1_q3" -> {
                "体育馆在市民广场的 {0} 偏 {1} {2}° 方向上，距离是 {3} 米。" to listOf(
                    MathBlankSpec("0", MathBlankInputType.CHOICE_TEXT, "基准方向", listOf("南"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("1", MathBlankInputType.CHOICE_TEXT, "偏转方向", listOf("东"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("2", MathBlankInputType.INTEGER, "角度", listOf("60"), suffix = "°"),
                    MathBlankSpec("3", MathBlankInputType.INTEGER, "距离", listOf("300"), suffix = "米")
                )
            }
            "q_u2_l2_q2" -> {
                "以市政厅为观测点，百货大楼在市政厅的 {0} 偏 {1} {2}° 方向上，实际距离是 {3} 米。" to listOf(
                    MathBlankSpec("0", MathBlankInputType.CHOICE_TEXT, "基准方向", listOf("西"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("1", MathBlankInputType.CHOICE_TEXT, "偏转方向", listOf("北"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("2", MathBlankInputType.INTEGER, "角度", listOf("60"), suffix = "°"),
                    MathBlankSpec("3", MathBlankInputType.INTEGER, "距离", listOf("400"), suffix = "米")
                )
            }
            "q_u2_l2_q3" -> {
                "灯塔A在港口的 {0} 偏 {1} {2}° 方向上，实际距离是 {3} 米。" to listOf(
                    MathBlankSpec("0", MathBlankInputType.CHOICE_TEXT, "基准方向", listOf("北"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("1", MathBlankInputType.CHOICE_TEXT, "偏转方向", listOf("东"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("2", MathBlankInputType.INTEGER, "角度", listOf("20"), suffix = "°"),
                    MathBlankSpec("3", MathBlankInputType.INTEGER, "距离", listOf("500"), suffix = "米")
                )
            }
            "q_u2_l3_q2" -> {
                "徒步队从大本营出发，向 {0} 偏 {1} {2}° 方向行进 {3} 千米到达山顶，再向 {4} 偏 {5} {6}° 方向行进 {7} 千米到达山谷。" to listOf(
                    MathBlankSpec("0", MathBlankInputType.CHOICE_TEXT, "第一段基准方向", listOf("北"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("1", MathBlankInputType.CHOICE_TEXT, "第一段偏转方向", listOf("西"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("2", MathBlankInputType.INTEGER, "第一段角度", listOf("45"), suffix = "°"),
                    MathBlankSpec("3", MathBlankInputType.INTEGER, "第一段距离", listOf("2"), suffix = "千米"),
                    MathBlankSpec("4", MathBlankInputType.CHOICE_TEXT, "第二段基准方向", listOf("东"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("5", MathBlankInputType.CHOICE_TEXT, "第二段偏转方向", listOf("南"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("6", MathBlankInputType.INTEGER, "第二段角度", listOf("30"), suffix = "°"),
                    MathBlankSpec("7", MathBlankInputType.INTEGER, "第二段距离", listOf("3"), suffix = "千米")
                )
            }
            "q_u2_l4_q2" -> {
                "一艘轮船朝南偏东40°方向航行了50海里。由于风暴需要原路返回，它应该朝 {0} 偏 {1} {2}° 方向航行 {3} 海里才能回到港口。" to listOf(
                    MathBlankSpec("0", MathBlankInputType.CHOICE_TEXT, "基准方向", listOf("北"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("1", MathBlankInputType.CHOICE_TEXT, "偏转方向", listOf("西"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("2", MathBlankInputType.INTEGER, "角度", listOf("40"), suffix = "°"),
                    MathBlankSpec("3", MathBlankInputType.INTEGER, "距离", listOf("50"), suffix = "海里")
                )
            }
            "q_u2_l4_q5" -> {
                "已知点A在观测点O的东偏北15°方向上，距离300米。那么以A为观测点时，点O在点A的 {0} 偏 {1} {2}° 方向上，距离是 {3} 米。" to listOf(
                    MathBlankSpec("0", MathBlankInputType.CHOICE_TEXT, "基准方向", listOf("西"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("1", MathBlankInputType.CHOICE_TEXT, "偏转方向", listOf("南"), listOf("东", "西", "南", "北")),
                    MathBlankSpec("2", MathBlankInputType.INTEGER, "角度", listOf("15"), suffix = "°"),
                    MathBlankSpec("3", MathBlankInputType.INTEGER, "距离", listOf("300"), suffix = "米")
                )
            }
            else -> return question
        }

        val enrichedSpec = spec.copy(
            responseTemplate = template,
            blankSpecs = specs
        )
        return question.copy(answerSpec = enrichedSpec)
    }
}
