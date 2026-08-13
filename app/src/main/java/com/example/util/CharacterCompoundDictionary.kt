package com.example.util

import com.example.data.BuiltinChineseClueDictionary

object CharacterCompoundDictionary {
    private val compoundMap = mapOf(
"毯" to "绿毯",
        "玻" to "玻璃",
        "璃" to "玻璃",
        "裳" to "衣裳",
        "虹" to "彩虹",
        "蹄" to "马蹄",
        "腐" to "豆腐",
        "稍" to "稍微",
        "微" to "微笑",
        "缀" to "点缀",
        "窥" to "窥视",
        "幽" to "幽静",
        "雅" to "高雅",
        "案" to "图案",
        "拙" to "笨拙",
        "帘" to "窗帘",
        "薄" to "单薄",
        "糊" to "模糊",
        "蕾" to "花蕾",
        "恰" to "恰好",
        "襟" to "衣襟",
        "恍" to "恍然",
        "怨" to "埋怨",
        "德" to "道德",
        "鹊" to "喜鹊",
        "蝉" to "知了蝉",
        "律" to "纪律",
        "崖" to "悬崖",
        "渡" to "渡河",
        "索" to "探索",
        "寇" to "日寇",
        "副" to "副手",
        "榴" to "石榴",
        "抢" to "抢夺",
        "贯" to "全神贯注",
        "棋" to "下棋",
        "悬" to "悬挂",
        "沸" to "沸腾",
        "涧" to "山涧",
        "雹" to "冰雹",
        "屹" to "屹立",
        "悦" to "喜悦",
        "迈" to "迈步",
        "屈" to "屈服",
        "政" to "政府",
        "府" to "政府",
        "宾" to "宾客",
        "盏" to "一盏灯",
        "栏" to "栏杆",
        "汇" to "汇聚",
        "宣" to "宣布",
        "阅" to "阅读",
        "制" to "制度",
        "坦" to "坦克",
        "距" to "距离",
        "隆" to "隆重",
        "疙" to "疙瘩",
        "瘩" to "疙瘩",
        "棍" to "木棍",
        "裁" to "裁剪",
        "筹" to "筹备",
        "橡" to "橡皮",
        "雕" to "雕刻",
        "跺" to "跺脚",
        "颓" to "颓废",
        "沮" to "沮丧",
        "丧" to "丧失",
        "趴" to "趴下",
        "屉" to "抽屉",
        "谜" to "谜语",
        "尚" to "高尚",
        "氧" to "氧气",
        "倾" to "倾听",
        "揭" to "揭开",
        "斑" to "斑马",
        "燥" to "干燥",
        "漠" to "沙漠",
        "磁" to "磁铁",
        "素" to "朴素",
        "盗" to "强盗",
        "培" to "培养",
        "黎" to "黎明",
        "咆" to "咆哮",
        "哮" to "咆哮",
        "嗓" to "嗓子",
        "哑" to "哑巴",
        "揪" to "揪住",
        "瞪" to "瞪眼",
        "呻" to "呻吟",
        "废" to "废除",
        "汹" to "汹涌",
        "涌" to "涌现",
        "澎" to "澎湃",
        "湃" to "澎湃",
        "熄" to "熄灭",
        "掀" to "掀开",
        "困" to "困难",
        "唉" to "唉声叹气",
        "淋" to "淋雨",
        "嘿" to "嘿嘿",
        "糟" to "糟糕",
        "嘛" to "干嘛",
        "皱" to "皱纹",
        "勺" to "勺子",
        "棚" to "工棚",
        "梁" to "栋梁",
        "叭" to "喇叭",
        "苔" to "青苔",
        "藓" to "苔藓",
        "坪" to "草坪",
        "蔗" to "甘蔗",
        "瀑" to "瀑布",
        "增" to "增加",
        "缝" to "缝隙",
        "谚" to "谚语",
        "袖" to "袖子",
        "篷" to "帐篷",
        "缩" to "缩小",
        "疯" to "疯狂",
        "瓦" to "瓦片",
        "柜" to "柜子",
        "喧" to "喧闹",
        "甩" to "甩开",
        "嚷" to "大嚷",
        "蒜" to "大蒜",
        "酱" to "酱油",
        "唇" to "嘴唇",
        "蹦" to "蹦跳",
        "涯" to "天涯",
        "莺" to "夜莺",
        "莹" to "晶莹",
        "裹" to "包裹",
        "篮" to "摇篮",
        "蔼" to "和蔼",
        "资" to "资源",
        "矿" to "矿产",
        "慷" to "慷慨",
        "慨" to "慷慨",
        "贡" to "贡献",
        "滥" to "泛滥",
        "基" to "基础",
        "睹" to "目睹",
        "哉" to "善哉",
        "巍" to "巍峨",
        "弦" to "琴弦",
        "轴" to "画轴",
        "锦" to "锦囊",
        "曝" to "曝光",
        "矣" to "晚矣",
        "谱" to "曲谱",
        "莱" to "蓬莱",
        "茵" to "绿草如茵",
        "盲" to "盲人",
        "纯" to "纯洁",
        "键" to "键盘",
        "缕" to "一缕",
        "陶" to "陶醉",
        "郑" to "郑重",
        "拜" to "拜访",
        "租" to "出租",
        "厨" to "厨房",
        "毡" to "毡帽",
        "羞" to "害羞",
        "撒" to "撒谎",
        "缚" to "束缚",
        "猬" to "刺猬",
        "伶" to "伶俐",
        "俐" to "伶俐",
        "窜" to "逃窜",
        "搁" to "搁浅",
        "综" to "综合",
        "澄" to "澄清",
        "萍" to "浮萍",
        "漾" to "荡漾",
        "削" to "剥削",
        "瞬" to "瞬间",
        "凝" to "凝视",
        "骤" to "骤然",
        "掷" to "投掷",
        "陡" to "陡峭",

        // 第1课
        "毯" to "绿毯",
        "陈" to "陈列",
        "裳" to "衣裳",
        "虹" to "彩虹",
        "雅" to "幽雅",
        "缀" to "点缀",
        "幽" to "幽静",
        "案" to "图案",
        "琢" to "琢磨",
        "怨" to "愁怨",
        "襟" to "胸襟",
        "糊" to "模糊",
        "匙" to "钥匙",
        "德" to "道德",
        "宿" to "住宿",
        "渚" to "烟渚",
        // 第2课
        "巨" to "巨大",
        "洒" to "洒水",
        "拼" to "拼搏",
        "恍" to "恍然",
        "旷" to "空旷",
        "窥" to "窥视",
        // 第3课
        "笨" to "笨拙",
        "拙" to "拙劣",
        "肃" to "严肃",
        "漏" to "漏洞",
        "蕾" to "花蕾",
        "漠" to "冷漠",
        "歇" to "歇息",
        "稍" to "稍微",
        // 第7课
        "律" to "纪律",
        "渡" to "渡口",
        "索" to "铁索",
        "岷" to "岷山",
        "攀" to "攀登",
        "崖" to "悬崖",
        "踩" to "踩踏",
        "险" to "危险",
        // 第8课
        "悬" to "悬挂",
        "涯" to "天涯",
        "坠" to "坠落",
        "摔" to "摔倒",
        "撞" to "碰撞",
        "斩" to "斩首",
        "咆" to "咆哮",
        "哮" to "咆哮",
        // 第10课
        "缩" to "缩小",
        "疯" to "疯狂",
        "抖" to "发抖",
        "缝" to "缝隙",
        "甩" to "甩动",
        "趴" to "趴着",
        "嚷" to "嚷嚷",
        "酱" to "酱油",
        "啪" to "噼啪",
        // 第12课
        "吼" to "吼叫",
        "废" to "废品",
        "浑" to "浑身",
        "傻" to "傻瓜",
        "糟" to "糟糕",
        "淋" to "淋湿",
        "撕" to "撕开",
        "哑" to "哑巴",
        "奠" to "奠定",
        "瞎" to "瞎闹",
        // 第15课
        "趟" to "水趟",
        "塌" to "塌陷",
        "憋" to "憋气",
        "槽" to "水槽",
        // 第17课
        "淘" to "淘沙",
        "簸" to "颠簸",
        "篱" to "篱笆",
        "莺" to "黄莺",
        "郭" to "城郭",
        "苔" to "青苔",
        "畦" to "菜畦",
        "闼" to "排闼",
        // 第21课
        "哉" to "善哉",
        "巍" to "巍峨",
        "弦" to "琴弦",
        "绝" to "绝妙",
        "熟" to "熟悉",
        "漫" to "浪漫",
        // 第22课
        "轴" to "画轴",
        "锦" to "锦囊",
        "曝" to "曝晒",
        "谬" to "谬误",
        "耕" to "耕耘",
        "织" to "组织",
        "婢" to "奴婢"
    )

    fun getCompoundWord(charStr: String): String? {
        val trimmed = charStr.trim()
        if (compoundMap.containsKey(trimmed)) return compoundMap[trimmed]
        val clue = BuiltinChineseClueDictionary.getClueInfo(trimmed)
        if (clue != null && clue.clueText.isNotBlank()) {
            val firstWord = clue.clueText.split("/").firstOrNull()?.trim()
            if (!firstWord.isNullOrBlank()) return firstWord
        }
        return null
    }

    fun getMaskedContextPrompt(item: String): String {
        val trimmed = item.trim()
        if (trimmed.length == 1) {
            val compound = getCompoundWord(trimmed)
            if (!compound.isNullOrBlank() && compound.contains(trimmed)) {
                return compound.replace(trimmed, "_")
            } else if (!compound.isNullOrBlank()) {
                return "$compound (_)"
            }
            return "_"
        } else {
            val clue = BuiltinChineseClueDictionary.getClueInfo(trimmed)
            if (clue != null && clue.clueText.isNotBlank() && clue.clueText.contains(trimmed)) {
                val mask = "_".repeat(trimmed.length)
                return clue.clueText.replace(trimmed, mask)
            }
            return "_".repeat(trimmed.length)
        }
    }

    fun getSpokenPrompt(item: String): String {
        val trimmed = item.trim()
        if (trimmed.length == 1) {
            val compound = getCompoundWord(trimmed)
            return if (!compound.isNullOrBlank()) {
                "${trimmed}，${compound}的${trimmed}"
            } else {
                "${trimmed}，${trimmed}字"
            }
        } else {
            return "请写词语：$trimmed"
        }
    }

    fun getDisplayText(item: String): String {
        val trimmed = item.trim()
        if (trimmed.length == 1) {
            val compound = getCompoundWord(trimmed)
            return if (!compound.isNullOrBlank()) {
                "$trimmed ($compound)"
            } else {
                trimmed
            }
        }
        return trimmed
    }
}
