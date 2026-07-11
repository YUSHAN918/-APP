package com.example.data

data class ClueInfo(
    val clueText: String,
    val meaningHint: String,
    val exampleText: String = "",
    val sourceLesson: String = ""
)

object BuiltinChineseClueDictionary {
    private val dictionary = mapOf(
        // 第一单元常用字
        "昼" to ClueInfo("白昼 / 昼夜", "白天"),
        "耘" to ClueInfo("耕耘", "除草、耕作"),
        "桑" to ClueInfo("桑树", "一种树，叶子可以养蚕"),
        "晓" to ClueInfo("破晓", "天刚亮的时候"),
        "蝴" to ClueInfo("蝴蝶", "昆虫名，常和“蝶”组成“蝴蝶”"),
        "蝶" to ClueInfo("蝴蝶", "昆虫名，蝴蝶的蝶"),
        "蚂" to ClueInfo("蚂蚱", "昆虫名，常和“蚱”组成“蚂蚱”"),
        "蚱" to ClueInfo("蚂蚱", "昆虫名，蚂蚱的蚱"),
        "樱" to ClueInfo("樱桃", "樱桃的樱，也可指樱花、樱树"),
        "拔" to ClueInfo("拔草", "把东西向外拉出"),
        "瞎" to ClueInfo("瞎闹", "胡乱、没有道理地做事"),
        "铲" to ClueInfo("铲土", "用铲子清除或挖起"),
        "割" to ClueInfo("收割", "用刀切断或割下"),
        "承" to ClueInfo("承认", "认可事实或错误"),
        "拴" to ClueInfo("拴住", "用绳子系住"),
        "瓢" to ClueInfo("水瓢", "舀水用的器具"),
        "逛" to ClueInfo("闲逛", "到处走走、游玩"),

        // 第二单元常用字
        "妒" to ClueInfo("妒忌", "因别人好而心里不舒服"),
        "忌" to ClueInfo("妒忌", "忌妒、不愿别人比自己好"),
        "督" to ClueInfo("都督", "古代官职，也有监督的意思"),
        "惩" to ClueInfo("惩罚", "处罚"),
        "鲁" to ClueInfo("鲁肃", "人名用字，也可指鲁国、鲁莽"),
        "寨" to ClueInfo("山寨", "防守用的村寨或营寨"),
        "擂" to ClueInfo("擂鼓", "敲打鼓"),
        "呐" to ClueInfo("呐喊", "大声喊叫"),
        "冈" to ClueInfo("山冈", "不高的山"),
        "俺" to ClueInfo("俺们", "我们，方言说法"),
        "榜" to ClueInfo("榜文", "张贴出来的公告"),
        "杖" to ClueInfo("拐杖", "拄着走路的棍子"),
        "勿" to ClueInfo("切勿", "不要"),
        "膛" to ClueInfo("胸膛", "胸部"),
        "截" to ClueInfo("截断", "切断或拦住"),

        // 更多高频字词补充
        "神机妙算" to ClueInfo("", "形容计谋高明，预测极准确"),
        "阴谋诡计" to ClueInfo("", "暗中策划的坏主意"),
        "手疾眼快" to ClueInfo("", "动作敏捷，眼光敏锐"),
        "精神抖擞" to ClueInfo("", "精神振作，非常有干劲"),
        "美妙" to ClueInfo("美妙绝伦", "美好，让人感到愉悦"),
        "蝴蝶" to ClueInfo("彩色的蝴蝶", "一种美丽的昆虫，常在花丛中飞舞"),
        "樱桃" to ClueInfo("红彤彤的樱桃", "一种鲜红酸甜的果实"),
        "圆滚滚" to ClueInfo("圆滚滚的身体", "形容非常圆润可爱"),
        "破晓" to ClueInfo("清晨破晓", "早晨天刚发亮")
    )

    fun getClueInfo(text: String): ClueInfo? {
        val trimmed = text.trim()
        dictionary[trimmed]?.let { return it }
        // 如果是多字词，且没有直接映射，尝试提取其中的字组词
        if (trimmed.length == 1) {
            return ClueInfo(clueText = "${trimmed}字", meaningHint = "汉字：$trimmed")
        }
        return null
    }
}
