package com.example.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

enum class PackSourceType {
    ORIGINAL, USER_IMPORTED, USER_PRIVATE, LICENSED
}

data class ContentPack(
    val id: String,
    val name: String,
    val description: String,
    val grade: String,
    val semester: String,
    val sourceType: PackSourceType,
    val versionName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isInstalled: Boolean,
    val units: List<PackUnit> = emptyList()
)

data class PackUnit(
    val id: String,
    val packId: String,
    val unitName: String,
    val orderIndex: Int,
    val items: List<PackItem> = emptyList(),
    val lessonName: String? = null,
    val sourcePackId: String? = null
)

data class PackItem(
    val text: String,
    val type: String, // 字, 词语, 成语, 等
    val difficulty: String, // 普通, 易错, BOSS
    val promptMode: String = "FULL_WORD",
    val hiddenIndicesStr: String = "",
    val visiblePrompt: String = "",
    val ttsPrompt: String = "",
    val contextText: String = "",
    val targetAnswer: String = "",
    val clueText: String = "",
    val meaningHint: String = ""
)

object ContentPackManager {
    private const val PREFS_NAME = "content_packs_prefs"
    private const val KEY_INSTALLED_PACKS = "installed_pack_ids"
    private const val CUSTOM_PACKS_FILE = "custom_content_packs.json"

    // 1. Hardcoded Original Generic 5th Grade Pack
    val original5thGradePack = ContentPack(
        id = "original_grade_5",
        name = "五年级通用字词训练包",
        description = "本包为原创通用训练内容，不对应任何特定教材版本，可用于五年级语文字词默写练习。",
        grade = "五年级",
        semester = "通用",
        sourceType = PackSourceType.ORIGINAL,
        versionName = "1.0.0",
        createdAt = 1782638400000L, // 2026-06-28
        updatedAt = 1782638400000L,
        isInstalled = false,
        units = listOf(
            PackUnit(
                id = "original_grade_5_u1",
                packId = "original_grade_5",
                unitName = "第一单元：自然与观察",
                orderIndex = 1,
                items = listOf(
                    PackItem("清澈", "词语", "普通"),
                    PackItem("湿润", "词语", "普通"),
                    PackItem("茂密", "词语", "普通"),
                    PackItem("观察", "词语", "普通"),
                    PackItem("鸟语花香", "成语", "普通"),
                    PackItem("绿意盎然", "成语", "普通"),
                    PackItem("崇山峻岭", "成语", "普通"),
                    PackItem("浩瀚", "词语", "普通"),
                    PackItem("蔚蓝", "词语", "普通"),
                    PackItem("繁星点点", "成语", "BOSS")
                )
            ),
            PackUnit(
                id = "original_grade_5_u2",
                packId = "original_grade_5",
                unitName = "第二单元：人物与品质",
                orderIndex = 2,
                items = listOf(
                    PackItem("诚实", "词语", "普通"),
                    PackItem("谦逊", "词语", "普通"),
                    PackItem("坚毅", "词语", "普通"),
                    PackItem("善良", "词语", "普通"),
                    PackItem("舍己为人", "成语", "普通"),
                    PackItem("大公无私", "成语", "普通"),
                    PackItem("锲而不舍", "成语", "普通"),
                    PackItem("谦虚", "词语", "普通"),
                    PackItem("智慧", "词语", "普通"),
                    PackItem("彬彬有礼", "成语", "BOSS")
                )
            ),
            PackUnit(
                id = "original_grade_5_u3",
                packId = "original_grade_5",
                unitName = "第三单元：阅读与表达",
                orderIndex = 3,
                items = listOf(
                    PackItem("构思", "词语", "普通"),
                    PackItem("抒发", "词语", "普通"),
                    PackItem("描绘", "词语", "普通"),
                    PackItem("阐述", "词语", "普通"),
                    PackItem("绘声绘色", "成语", "普通"),
                    PackItem("妙笔生花", "成语", "普通"),
                    PackItem("逻辑清晰", "词语", "普通"),
                    PackItem("精彩", "词语", "普通"),
                    PackItem("润色", "词语", "普通"),
                    PackItem("引人入胜", "成语", "BOSS")
                )
            ),
            PackUnit(
                id = "original_grade_5_u4",
                packId = "original_grade_5",
                unitName = "第四单元：历史与故事",
                orderIndex = 4,
                items = listOf(
                    PackItem("传承", "词语", "普通"),
                    PackItem("典故", "词语", "普通"),
                    PackItem("沧海桑田", "成语", "普通"),
                    PackItem("精忠报国", "成语", "普通"),
                    PackItem("卧薪尝胆", "成语", "普通"),
                    PackItem("纸上谈兵", "成语", "普通"),
                    PackItem("破釜沉舟", "成语", "普通"),
                    PackItem("烽火", "词语", "普通"),
                    PackItem("岁月", "词语", "普通"),
                    PackItem("历史悠久", "词语", "BOSS")
                )
            ),
            PackUnit(
                id = "original_grade_5_u5",
                packId = "original_grade_5",
                unitName = "第五单元：想象与创造",
                orderIndex = 5,
                items = listOf(
                    PackItem("遨游", "词语", "普通"),
                    PackItem("奇妙", "词语", "普通"),
                    PackItem("畅想", "词语", "普通"),
                    PackItem("创造", "词语", "普通"),
                    PackItem("异想天开", "成语", "普通"),
                    PackItem("天马行空", "成语", "普通"),
                    PackItem("别出心裁", "成语", "普通"),
                    PackItem("独创", "词语", "普通"),
                    PackItem("灵感", "词语", "普通"),
                    PackItem("科技奇迹", "词语", "BOSS")
                )
            )
        )
    )

    // 2. Load Installed IDs
    fun getInstalledPackIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_INSTALLED_PACKS, emptySet()) ?: emptySet()
    }

    // 3. Mark Installed
    fun setPackInstalled(context: Context, packId: String, installed: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_INSTALLED_PACKS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (installed) {
            current.add(packId)
        } else {
            current.remove(packId)
        }
        prefs.edit().putStringSet(KEY_INSTALLED_PACKS, current).apply()
    }

    // 4. Save Custom (User Imported / Licensed) Packs
    fun saveCustomPacks(context: Context, packs: List<ContentPack>) {
        val file = File(context.filesDir, CUSTOM_PACKS_FILE)
        val jsonArray = JSONArray()
        for (pack in packs) {
            val packObj = JSONObject()
            packObj.put("id", pack.id)
            packObj.put("name", pack.name)
            packObj.put("description", pack.description)
            packObj.put("grade", pack.grade)
            packObj.put("semester", pack.semester)
            packObj.put("sourceType", pack.sourceType.name)
            packObj.put("versionName", pack.versionName)
            packObj.put("createdAt", pack.createdAt)
            packObj.put("updatedAt", pack.updatedAt)

            val unitsArray = JSONArray()
            for (unit in pack.units) {
                val unitObj = JSONObject()
                unitObj.put("id", unit.id)
                unitObj.put("packId", unit.packId)
                unitObj.put("unitName", unit.unitName)
                unitObj.put("orderIndex", unit.orderIndex)
                unit.lessonName?.let { unitObj.put("lessonName", it) }
                unit.sourcePackId?.let { unitObj.put("sourcePackId", it) }

                val itemsArray = JSONArray()
                for (item in unit.items) {
                    val itemObj = JSONObject()
                    itemObj.put("text", item.text)
                    itemObj.put("type", item.type)
                    itemObj.put("difficulty", item.difficulty)
                    itemsArray.put(itemObj)
                }
                unitObj.put("items", itemsArray)
                unitsArray.put(unitObj)
            }
            packObj.put("units", unitsArray)
            jsonArray.put(packObj)
        }
        file.writeText(jsonArray.toString())
    }

    // 5. Load Custom Packs
    fun getCustomPacks(context: Context): List<ContentPack> {
        val file = File(context.filesDir, CUSTOM_PACKS_FILE)
        if (!file.exists()) return emptyList()

        return try {
            val jsonStr = file.readText()
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<ContentPack>()
            for (i in 0 until jsonArray.length()) {
                val packObj = jsonArray.getJSONObject(i)
                val id = packObj.getString("id")
                val name = packObj.getString("name")
                val description = packObj.optString("description", "")
                val grade = packObj.getString("grade")
                val semester = packObj.getString("semester")
                val sourceTypeStr = packObj.getString("sourceType")
                val sourceType = PackSourceType.valueOf(sourceTypeStr)
                val versionName = packObj.optString("versionName", "1.0.0")
                val createdAt = packObj.optLong("createdAt", System.currentTimeMillis())
                val updatedAt = packObj.optLong("updatedAt", System.currentTimeMillis())

                val unitsArray = packObj.getJSONArray("units")
                val units = mutableListOf<PackUnit>()
                for (j in 0 until unitsArray.length()) {
                    val unitObj = unitsArray.getJSONObject(j)
                    val unitId = unitObj.getString("id")
                    val packId = unitObj.getString("packId")
                    val unitName = unitObj.getString("unitName")
                    val orderIndex = unitObj.getInt("orderIndex")

                    val itemsArray = unitObj.getJSONArray("items")
                    val items = mutableListOf<PackItem>()
                    for (k in 0 until itemsArray.length()) {
                        val itemObj = itemsArray.getJSONObject(k)
                        val text = itemObj.getString("text")
                        val type = itemObj.optString("type", "词语")
                        val difficulty = itemObj.optString("difficulty", "普通")
                        items.add(PackItem(text, type, difficulty))
                    }
                    val lessonName = if (unitObj.has("lessonName")) unitObj.getString("lessonName") else null
                    val sourcePackId = if (unitObj.has("sourcePackId")) unitObj.getString("sourcePackId") else null
                    units.add(PackUnit(unitId, packId, unitName, orderIndex, items, lessonName, sourcePackId))
                }
                list.add(
                    ContentPack(
                        id = id,
                        name = name,
                        description = description,
                        grade = grade,
                        semester = semester,
                        sourceType = sourceType,
                        versionName = versionName,
                        createdAt = createdAt,
                        updatedAt = updatedAt,
                        isInstalled = false,
                        units = units
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 6. Get All Packs (with dynamic isInstalled state)
    fun getAllPacks(context: Context): List<ContentPack> {
        val installedIds = getInstalledPackIds(context)
        val builtinPrivate = BuiltinPrivatePacks.packs.map {
            it.copy(isInstalled = installedIds.contains(it.id))
        }
        val custom = getCustomPacks(context).map {
            it.copy(isInstalled = installedIds.contains(it.id))
        }
        return builtinPrivate + custom
    }

    // 7. Parse and Import JSON
    fun importPackFromJson(context: Context, jsonStr: String): ContentPack? {
        return try {
            val root = JSONObject(jsonStr)
            val name = root.getString("name")
            val grade = root.optString("grade", "五年级")
            val semester = root.optString("semester", "通用")
            val sourceTypeStr = root.optString("sourceType", "USER_IMPORTED")
            val sourceType = try {
                PackSourceType.valueOf(sourceTypeStr)
            } catch (e: Exception) {
                PackSourceType.USER_IMPORTED
            }
            val description = root.optString("description", "导入的自定义字词包")
            val packId = "imported_" + System.currentTimeMillis()

            val unitsArray = root.getJSONArray("units")
            val units = mutableListOf<PackUnit>()
            for (i in 0 until unitsArray.length()) {
                val unitObj = unitsArray.getJSONObject(i)
                val unitName = unitObj.getString("unitName")
                val unitId = "${packId}_u${i + 1}"

                val itemsArray = unitObj.getJSONArray("items")
                val items = mutableListOf<PackItem>()
                for (j in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(j)
                    val text = itemObj.getString("text")
                    val type = itemObj.optString("type", "词语")
                    val difficulty = itemObj.optString("difficulty", "普通")
                    items.add(PackItem(text, type, difficulty))
                }
                units.add(
                    PackUnit(
                        id = unitId,
                        packId = packId,
                        unitName = unitName,
                        orderIndex = i + 1,
                        items = items,
                        lessonName = if (unitObj.has("lessonName")) unitObj.getString("lessonName") else null,
                        sourcePackId = if (unitObj.has("sourcePackId")) unitObj.getString("sourcePackId") else null
                    )
                )
            }

            val newPack = ContentPack(
                id = packId,
                name = name,
                description = description,
                grade = grade,
                semester = semester,
                sourceType = sourceType,
                versionName = "1.0.0",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isInstalled = false,
                units = units
            )

            val currentCustom = getCustomPacks(context).toMutableList()
            currentCustom.add(newPack)
            saveCustomPacks(context, currentCustom)
            newPack
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 8. Delete Custom Pack Metadata (from our json file)
    fun deleteCustomPackMetadata(context: Context, packId: String) {
        val currentCustom = getCustomPacks(context).toMutableList()
        val index = currentCustom.indexOfFirst { it.id == packId }
        if (index != -1) {
            currentCustom.removeAt(index)
            saveCustomPacks(context, currentCustom)
        }
        setPackInstalled(context, packId, false)
    }
}
