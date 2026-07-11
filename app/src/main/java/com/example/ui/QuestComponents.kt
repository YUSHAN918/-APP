package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Quest UI 统一动画辅助器
 */
object QuestAnimation {
    @Composable
    fun rememberBreathingScale(minScale: Float = 0.98f, maxScale: Float = 1.02f): Float {
        val infiniteTransition = rememberInfiniteTransition(label = "breathing")
        val scale by infiniteTransition.animateFloat(
            initialValue = minScale,
            targetValue = maxScale,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        return scale
    }

    @Composable
    fun rememberFloatingOffset(minOffset: Float = -4f, maxOffset: Float = 4f): Float {
        val infiniteTransition = rememberInfiniteTransition(label = "floating")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = minOffset,
            targetValue = maxOffset,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offsetY"
        )
        return offsetY
    }

    @Composable
    fun rememberGlowIntensity(minGlow: Float = 0.2f, maxGlow: Float = 0.8f): Float {
        val infiniteTransition = rememberInfiniteTransition(label = "glow")
        val intensity by infiniteTransition.animateFloat(
            initialValue = minGlow,
            targetValue = maxGlow,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "intensity"
        )
        return intensity
    }
}

/**
 * 缩放点击效果 Modifier
 */
@Composable
fun Modifier.questClickScale(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "pressScale")
    return this
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = onClick
        )
}

/**
 * 呼吸动效 Modifier
 */
fun Modifier.questBreathing(enabled: Boolean = true): Modifier = composed {
    if (!enabled) return@composed this
    val scale = QuestAnimation.rememberBreathingScale()
    this.graphicsLayer(scaleX = scale, scaleY = scale)
}

/**
 * 悬浮抖动 Modifier
 */
fun Modifier.questFloat(enabled: Boolean = true): Modifier = composed {
    if (!enabled) return@composed this
    val offsetY = QuestAnimation.rememberFloatingOffset()
    this.graphicsLayer(translationY = offsetY)
}

/**
 * 动态根据关卡属性生成的冒险元数据
 */
data class QuestMetadata(
    val typeName: String,
    val bossName: String,
    val bossQuality: String, // 普通, 精英, 极危
    val bossColor: Color,
    val bossEmoji: String,
    val difficultyStars: Int,
    val difficultyName: String,
    val difficultyColor: Color,
    val estTimeMinutes: Int,
    val goldReward: Int,
    val expReward: Int,
    val petExpReward: Int,
    val energyReward: Int,
    val weaponExpReward: Int,
    val dropInfo: String,
    val themeColor: Color
)

fun resolveQuestMetadata(name: String, unitName: String = ""): QuestMetadata {
    val lowerName = name.uppercase()
    val combined = (name + " " + unitName).uppercase()
    return when {
        // BOSS / 挑战 / 终极
        combined.contains("BOSS") || combined.contains("挑战") || combined.contains("终极") -> {
            QuestMetadata(
                typeName = "深渊讨伐战",
                bossName = if (combined.contains("成语")) "深渊侵蚀之龙" else "终极错字霸主",
                bossQuality = "极危·主宰",
                bossColor = Color(0xFFE53935),
                bossEmoji = "🐉",
                difficultyStars = 5,
                difficultyName = "传说",
                difficultyColor = Color(0xFFE53935),
                estTimeMinutes = 15,
                goldReward = 50,
                expReward = 50,
                petExpReward = 40,
                energyReward = 30,
                weaponExpReward = 35,
                dropInfo = "⭐⭐⭐ 神秘宝箱",
                themeColor = Color(0xFFD32F2F)
            )
        }
        // 易错字词 / 错题本
        combined.contains("易错") || combined.contains("错题") || combined.contains("复仇") || combined.contains("净化") -> {
            QuestMetadata(
                typeName = "净化秘境",
                bossName = if (combined.contains("错题") || combined.contains("复仇")) "噩梦错字之灵" else "远古易错领主",
                bossQuality = "高危·统领",
                bossColor = Color(0xFFFF9800),
                bossEmoji = "💀",
                difficultyStars = 4,
                difficultyName = "大师",
                difficultyColor = Color(0xFFFF9800),
                estTimeMinutes = 10,
                goldReward = 35,
                expReward = 35,
                petExpReward = 25,
                energyReward = 15,
                weaponExpReward = 20,
                dropInfo = "⭐⭐ 星尘笔碎片",
                themeColor = Color(0xFFF57C00)
            )
        }
        // 古诗 / 成语
        combined.contains("古诗") || combined.contains("诗词") || combined.contains("诵") || combined.contains("成语") -> {
            QuestMetadata(
                typeName = "圣殿试炼",
                bossName = if (combined.contains("成语")) "墨渊成语幻龙" else "千秋墨意守卫",
                bossQuality = "精英·守护",
                bossColor = Color(0xFF9C27B0),
                bossEmoji = if (combined.contains("成语")) "🐲" else "🧙‍♂️",
                difficultyStars = 3,
                difficultyName = "专家",
                difficultyColor = Color(0xFF9C27B0),
                estTimeMinutes = 8,
                goldReward = 25,
                expReward = 25,
                petExpReward = 20,
                energyReward = 10,
                weaponExpReward = 15,
                dropInfo = "⭐ 墨能精华",
                themeColor = Color(0xFF7B1FA2)
            )
        }
        // 课后词语 / 文言文 / 课文 / 数学 / 英语 / 实践
        combined.contains("词语") || combined.contains("文言文") || combined.contains("课文") || combined.contains("MATH") || combined.contains("ENG") || combined.contains("ENGLISH") || combined.contains("PRACTICE") || combined.contains("LIFE") -> {
            val isMath = combined.contains("MATH") || combined.contains("数学")
            val isEng = combined.contains("ENG") || combined.contains("英语")
            val isPractice = combined.contains("PRACTICE") || combined.contains("LIFE") || combined.contains("实践")
            QuestMetadata(
                typeName = if (isMath) "奥数殿堂" else if (isEng) "圣剑语域" else if (isPractice) "实践委托" else "荒野远征",
                bossName = if (isMath) "逻辑齿轮魔怪" else if (isEng) "幽灵语语法怪" else if (isPractice) "惰性黏土魔兽" else "词汇狂澜魔兽",
                bossQuality = "精英·悍将",
                bossColor = Color(0xFF1E88E5),
                bossEmoji = if (isMath) "⚙️" else if (isEng) "🔤" else if (isPractice) "🧹" else "🦁",
                difficultyStars = 3,
                difficultyName = "专家",
                difficultyColor = Color(0xFF1E88E5),
                estTimeMinutes = 6,
                goldReward = 20,
                expReward = 20,
                petExpReward = 15,
                energyReward = 8,
                weaponExpReward = 10,
                dropInfo = "暂无特殊掉落",
                themeColor = Color(0xFF1976D2)
            )
        }
        // 会写字 / 识字 / 基础
        combined.contains("会写字") || combined.contains("识字") || combined.contains("基础") || combined.contains("自测") -> {
            QuestMetadata(
                typeName = "前哨扫荡",
                bossName = "迷惘字词魔兽",
                bossQuality = "普通·喽啰",
                bossColor = Color(0xFF43A047),
                bossEmoji = "👺",
                difficultyStars = 2,
                difficultyName = "困难",
                difficultyColor = Color(0xFF43A047),
                estTimeMinutes = 5,
                goldReward = 15,
                expReward = 15,
                petExpReward = 10,
                energyReward = 5,
                weaponExpReward = 8,
                dropInfo = "暂无特殊掉落",
                themeColor = Color(0xFF388E3C)
            )
        }
        // 其它
        else -> {
            QuestMetadata(
                typeName = "冒险委托",
                bossName = "字界游荡史莱姆",
                bossQuality = "普通·游荡者",
                bossColor = Color(0xFF78909C),
                bossEmoji = "💧",
                difficultyStars = 1,
                difficultyName = "普通",
                difficultyColor = Color(0xFF78909C),
                estTimeMinutes = 4,
                goldReward = 10,
                expReward = 10,
                petExpReward = 8,
                energyReward = 3,
                weaponExpReward = 5,
                dropInfo = "暂无特殊掉落",
                themeColor = Color(0xFF455A64)
            )
        }
    }
}

/**
 * 难度系统星级徽章
 */
@Composable
fun QuestDifficultyBadge(
    stars: Int,
    name: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "★".repeat(stars),
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = name,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 守关 BOSS 动态头像
 */
@Composable
fun QuestBossBadge(
    bossName: String,
    bossQuality: String,
    bossColor: Color,
    bossEmoji: String,
    modifier: Modifier = Modifier
) {
    val floatingY = QuestAnimation.rememberFloatingOffset()
    Surface(
        color = Color(0xFF16161E),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, bossColor.copy(alpha = 0.8f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 漂浮的 Boss 头像
            Text(
                text = bossEmoji,
                fontSize = 22.sp,
                modifier = Modifier.graphicsLayer(translationY = floatingY)
            )
            Column {
                Text(
                    text = bossName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = bossColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = bossQuality,
                        color = bossColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

/**
 * 奖励预览栏
 */
@Composable
fun QuestRewardRow(
    gold: Int,
    exp: Int,
    petExp: Int,
    energy: Int,
    weaponExp: Int,
    dropInfo: String,
    modifier: Modifier = Modifier
) {
    val glowIntensity = QuestAnimation.rememberGlowIntensity()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A24), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "🎁 探索预计奖励:",
            color = Color(0xFFFFD700),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RewardChip(emoji = "🪙", text = "+$gold")
            RewardChip(emoji = "✨", text = "+${exp}经验")
            RewardChip(emoji = "🐾", text = "+${petExp}宠物")
            if (energy > 0) {
                RewardChip(emoji = "🔋", text = "+${energy}孵化")
            }
            if (weaponExp > 0) {
                RewardChip(emoji = "⚔️", text = "+${weaponExp}武器")
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Text(
                text = "💎 特殊概率掉落:",
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dropInfo,
                color = if (dropInfo != "暂无特殊掉落") Color(0xFFFFD700) else Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = if (dropInfo != "暂无特殊掉落") {
                    Modifier.graphicsLayer(alpha = 0.7f + 0.3f * glowIntensity)
                } else Modifier
            )
        }
    }
}

@Composable
private fun RewardChip(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .background(Color(0xFF2B2B3A), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(emoji, fontSize = 11.sp)
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * 勋章样式完成徽章
 */
@Composable
fun QuestCompletionBadge(
    badgeType: String,
    modifier: Modifier = Modifier
) {
    val (emoji, label, color) = when (badgeType) {
        "THREE_STAR" -> Triple("🏆", "三星通关", Color(0xFFFFD700))
        "PERFECT" -> Triple("💠", "完美净化", Color(0xFF00E5FF))
        "BOSS_DEFEAT" -> Triple("⚔️", "魔王击破", Color(0xFFFF1744))
        "DAILY_DONE" -> Triple("🗓️", "委托完成", Color(0xFF00E676))
        else -> Triple("🎖️", "首次完成", Color(0xFFECEFF1))
    }

    val glow = QuestAnimation.rememberGlowIntensity()

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.6f + 0.3f * glow)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 13.sp)
            Text(
                text = label,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class ChapterBannerConfig(
    val bgGradient: Brush,
    val bannerTitle: String,
    val bannerDesc: String,
    val bannerEmoji: String,
    val themeColor: Color
)

/**
 * 章节 Header & 插图 Banner 区域
 */
@Composable
fun QuestChapterBanner(
    gradeName: String,
    completionRate: Float,
    modifier: Modifier = Modifier
) {
    val config = when {
        gradeName.contains("一年级") -> ChapterBannerConfig(
            bgGradient = Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))),
            bannerTitle = "🌲 绿野森林·新手试炼",
            bannerDesc = "万物复苏的启蒙森林，藏匿着初生的字词魔物。",
            bannerEmoji = "🌳",
            themeColor = Color(0xFF4CAF50)
        )
        gradeName.contains("二年级") -> ChapterBannerConfig(
            bgGradient = Brush.verticalGradient(listOf(Color(0xFF004D40), Color(0xFF00796B))),
            bannerTitle = "🎋 墨韵竹林·修心之道",
            bannerDesc = "风吹竹林，沙沙作响，字里行间领悟古朴风华。",
            bannerEmoji = "🐼",
            themeColor = Color(0xFF009688)
        )
        gradeName.contains("三年级") -> ChapterBannerConfig(
            bgGradient = Brush.verticalGradient(listOf(Color(0xFF006064), Color(0xFF0097A7))),
            bannerTitle = "❄️ 寒霜雪原·意志淬炼",
            bannerDesc = "极寒的风雪冻结了字形，在这严苛考验中战胜自我。",
            bannerEmoji = "🏔️",
            themeColor = Color(0xFF00BCD4)
        )
        gradeName.contains("四年级") -> ChapterBannerConfig(
            bgGradient = Brush.verticalGradient(listOf(Color(0xFF3E2723), Color(0xFF5D4037))),
            bannerTitle = "🏯 荒芜古城·历史回响",
            bannerDesc = "沉睡千年的青砖黛瓦，字词在时光斑驳中苏醒。",
            bannerEmoji = "⛩️",
            themeColor = Color(0xFF795548)
        )
        gradeName.contains("五年级") -> ChapterBannerConfig(
            bgGradient = Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF1A237E))),
            bannerTitle = "🌌 浩瀚墨海·终极对决",
            bannerDesc = "狂澜万丈的泼墨深渊，唯有千锤百炼的剑笔能划破黑暗。",
            bannerEmoji = "🪐",
            themeColor = Color(0xFF3F51B5)
        )
        gradeName.contains("六年级") -> ChapterBannerConfig(
            bgGradient = Brush.verticalGradient(listOf(Color(0xFF311B92), Color(0xFF4A148C))),
            bannerTitle = "⚡ 天空神殿·超凡入圣",
            bannerDesc = "云端之上的至高王座，迎接净化试炼的终极勇士。",
            bannerEmoji = "🏛️",
            themeColor = Color(0xFF9C27B0)
        )
        else -> ChapterBannerConfig(
            bgGradient = Brush.verticalGradient(listOf(Color(0xFF212121), Color(0xFF37474F))),
            bannerTitle = "⚔️ 异界虚空·魔物乱入",
            bannerDesc = "来自未知维度的混沌深渊，净化战斗与冒险永无止境。",
            bannerEmoji = "🔮",
            themeColor = Color(0xFF607D8B)
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(config.bgGradient)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = config.bannerTitle,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = config.bannerDesc,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                    Text(
                        text = config.bannerEmoji,
                        fontSize = 38.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                // 章节进度条
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "章节冒险进度",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(completionRate * 100).toInt()}%",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { completionRate },
                        color = Color(0xFFFFD700),
                        trackColor = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

/**
 * 统一关卡 Quest Card 卡片
 */
@Composable
fun QuestCard(
    title: String,
    unitName: String,
    wordCount: Int,
    isCompleted: Boolean,
    isSystemPreset: Boolean,
    hasWrongWords: Boolean,
    buttonText: String = if (isCompleted) "再次冒险" else "开始冒险",
    buttonType: String = "NORMAL", // NORMAL, BOSS, DAILY, CHALLENGE
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val meta = resolveQuestMetadata(title, unitName)
    val breathingScale = QuestAnimation.rememberBreathingScale()
    val glow = QuestAnimation.rememberGlowIntensity()

    // 决定按钮色彩
    val buttonColor = when (buttonType) {
        "BOSS" -> Color(0xFFD32F2F)
        "DAILY" -> Color(0xFF388E3C)
        "CHALLENGE" -> Color(0xFF7B1FA2)
        else -> meta.themeColor
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFF16161E) else Color(0xFF1E1E26)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (hasWrongWords) 1.5.dp else 1.dp,
            color = if (hasWrongWords) Color(0xFFE53935) else if (isCompleted) Color(0xFF30303A) else meta.themeColor.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .questClickScale(onClick)
            .graphicsLayer(
                scaleX = if (hasWrongWords) breathingScale else 1f,
                scaleY = if (hasWrongWords) breathingScale else 1f
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 上半部分：Boss 怪物 + 信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // 左侧：守关 Boss 头像和品质
                QuestBossBadge(
                    bossName = meta.bossName,
                    bossQuality = meta.bossQuality,
                    bossColor = meta.bossColor,
                    bossEmoji = meta.bossEmoji
                )

                // 右侧：关卡标题与详情
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        // 预设/系统标识
                        Surface(
                            color = if (isSystemPreset) Color(0xFF37474F) else Color(0xFF5D4037),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = if (isSystemPreset) "系统" else "自制",
                                color = if (isSystemPreset) Color(0xFFCFD8DC) else Color(0xFFFFD54F),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = "🏰 $unitName",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚔️ $wordCount 词",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "⏳ ${meta.estTimeMinutes} 分钟",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }

                    // 难度星级徽章
                    QuestDifficultyBadge(
                        stars = meta.difficultyStars,
                        name = meta.difficultyName,
                        color = meta.difficultyColor
                    )
                }
            }

            // 中间部分：预计收益预览
            QuestRewardRow(
                gold = meta.goldReward,
                exp = meta.expReward,
                petExp = meta.petExpReward,
                energy = meta.energyReward,
                weaponExp = meta.weaponExpReward,
                dropInfo = meta.dropInfo
            )

            // 下半部分：游戏勋章 + 闪烁冒险大按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCompleted) {
                    QuestCompletionBadge(badgeType = if (buttonType == "BOSS") "BOSS_DEFEAT" else "THREE_STAR")
                } else if (hasWrongWords) {
                    Surface(
                        color = Color(0xFFFFEBEE).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350))
                    ) {
                        Text(
                            text = "⚠️ 需完美净化",
                            color = Color(0xFFE53935),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // 【开始冒险】大按钮
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.3f + 0.3f * glow),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
