package com.example.util

import com.example.data.BuiltinChineseClueDictionary

object CharacterCompoundDictionary {
    private val compoundMap = mapOf(
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
