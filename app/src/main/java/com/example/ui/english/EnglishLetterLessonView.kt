package com.example.ui.english

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.util.english.EnglishTTSHelper

data class LetterModel(
    val upper: Char,
    val lower: Char,
    val examples: List<LetterExample>
)

data class LetterExample(
    val english: String,
    val chinese: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnglishLetterLessonView(
    unitId: String = "english_pep_2013_g3_s1_u5",
    ttsHelper: EnglishTTSHelper,
    equippedBrushStyle: com.example.ui.BrushStyle,
    equippedBrushConfig: com.example.data.PlayerBrushConfig?,
    onBack: (() -> Unit)? = null,
    onLessonCompleted: () -> Unit
) {
    val context = LocalContext.current
    val letters = remember(unitId) {
        if (unitId == "english_pep_2013_g4_s2_u1") {
            listOf(
                LetterModel('E', 'r', listOf(
                    LetterExample("sister", "姐；妹 /ˈsɪstə(r)/"),
                    LetterExample("computer", "计算机 /kəmˈpjuːtə(r)/"),
                    LetterExample("teacher", "教师 /ˈtiːtʃə(r)/"),
                    LetterExample("dinner", "晚餐 /ˈdɪnə(r)/"),
                    LetterExample("ruler", "尺子 /ˈruːlə(r)/"),
                    LetterExample("water", "水 /ˈwɔːtə(r)/"),
                    LetterExample("tiger", "老虎 /ˈtaɪɡə(r)/")
                ))
            )
        } else if (unitId == "english_pep_2013_g4_s2_u2") {
            listOf(
                LetterModel('I', 'r', listOf(
                    LetterExample("girl", "女孩 /ɡɜːl/"),
                    LetterExample("bird", "鸟 /bɜːd/"),
                    LetterExample("dirt", "泥土 /dɜːt/"),
                    LetterExample("birth", "出生 /bɜːθ/")
                )),
                LetterModel('U', 'r', listOf(
                    LetterExample("nurse", "护士 /nɜːs/"),
                    LetterExample("hamburger", "汉堡包 /ˈhæmbɜːɡə(r)/"),
                    LetterExample("hurt", "受伤 /hɜːt/"),
                    LetterExample("number", "数字 /ˈnʌmbə(r)/")
                ))
            )
        } else if (unitId == "english_pep_2013_g4_s1_u6") {
            listOf(
                LetterModel('A', 'e', listOf(
                    LetterExample("face", "脸 /feɪs/ (开音节)"),
                    LetterExample("bag", "包 /bæg/ (闭音节)")
                )),
                LetterModel('I', 'e', listOf(
                    LetterExample("rice", "米饭 /raɪs/ (开音节)"),
                    LetterExample("six", "六 /sɪks/ (闭音节)")
                )),
                LetterModel('O', 'e', listOf(
                    LetterExample("nose", "鼻子 /nəʊz/ (开音节)"),
                    LetterExample("dog", "狗 /dɒg/ (闭音节)")
                )),
                LetterModel('U', 'e', listOf(
                    LetterExample("use", "使用 /juːz/ (开音节)"),
                    LetterExample("mum", "妈妈 /mʌm/ (闭音节)")
                )),
                LetterModel('E', 'e', listOf(
                    LetterExample("me", "我 /miː/ (长音)"),
                    LetterExample("leg", "腿 /leg/ (短音)")
                ))
            )
        } else if (unitId == "english_pep_2013_g4_s1_u5") {
            listOf(
                LetterModel('E', 'e', listOf(
                    LetterExample("me", "我(宾格) /miː/"),
                    LetterExample("he", "他 /hiː/"),
                    LetterExample("she", "她 /ʃiː/"),
                    LetterExample("we", "我们 /wiː/")
                ))
            )
        } else if (unitId == "english_pep_2013_g4_s1_u4") {
            listOf(
                LetterModel('U', 'e', listOf(
                    LetterExample("use", "使用 /juːz/"),
                    LetterExample("cute", "可爱的 /kjuːt/"),
                    LetterExample("excuse", "原谅 /ɪkˈskjuːz/")
                ))
            )
        } else if (unitId == "english_pep_2013_g4_s1_u3") {
            listOf(
                LetterModel('O', 'e', listOf(
                    LetterExample("nose", "鼻子 /nəʊz/"),
                    LetterExample("note", "便条；笔记 /nəʊt/"),
                    LetterExample("Coke", "可口可乐 /kəʊk/"),
                    LetterExample("Mr Jones", "琼斯先生 /ˈmɪstə dʒəʊnz/")
                ))
            )
        } else if (unitId == "english_pep_2013_g4_s1_u2") {
            listOf(
                LetterModel('I', 'e', listOf(
                    LetterExample("like", "喜欢 /laɪk/"),
                    LetterExample("kite", "风筝 /kaɪt/"),
                    LetterExample("five", "五 /faɪv/"),
                    LetterExample("nine", "九 /naɪn/"),
                    LetterExample("rice", "米饭 /raɪs/")
                ))
            )
        } else if (unitId == "english_pep_2013_g4_s1_u1") {
            listOf(
                LetterModel('A', 'e', listOf(
                    LetterExample("cake", "蛋糕 /keɪk/"),
                    LetterExample("face", "脸 /feɪs/"),
                    LetterExample("name", "名字 /neɪm/"),
                    LetterExample("make", "制作 /meɪk/")
                ))
            )
        } else if (unitId == "english_pep_2013_g3_s2_u1") {
            listOf(
                LetterModel('A', 'a', listOf(
                    LetterExample("cat", "猫 /kæt/"),
                    LetterExample("bag", "包 /bæɡ/"),
                    LetterExample("dad", "爸爸 /dæd/"),
                    LetterExample("hand", "手 /hænd/")
                ))
            )
        } else if (unitId == "english_pep_2013_g3_s2_u2") {
            listOf(
                LetterModel('E', 'e', listOf(
                    LetterExample("ten", "十 /ten/"),
                    LetterExample("pen", "钢笔 /pen/"),
                    LetterExample("leg", "腿 /leɡ/"),
                    LetterExample("red", "红色 /red/")
                ))
            )
        } else if (unitId == "english_pep_2013_g3_s2_u3") {
            listOf(
                LetterModel('I', 'i', listOf(
                    LetterExample("big", "大的 /bɪɡ/"),
                    LetterExample("pig", "猪 /pɪɡ/"),
                    LetterExample("six", "六 /sɪks/"),
                    LetterExample("milk", "牛奶 /mɪlk/")
                ))
            )
        } else if (unitId == "english_pep_2013_g3_s2_u4") {
            listOf(
                LetterModel('O', 'o', listOf(
                    LetterExample("dog", "狗 /dɒɡ/"),
                    LetterExample("box", "盒子 /bɒks/"),
                    LetterExample("orange", "橙子 /ˈɒrɪndʒ/"),
                    LetterExample("body", "身体 /ˈbɒdi/")
                ))
            )
        } else if (unitId == "english_pep_2013_g3_s2_u5") {
            listOf(
                LetterModel('U', 'u', listOf(
                    LetterExample("fun", "乐趣 /fʌn/"),
                    LetterExample("run", "跑步 /rʌn/"),
                    LetterExample("duck", "鸭子 /dʌk/"),
                    LetterExample("under", "在...下面 /ˈʌndə/")
                ))
            )
        } else if (unitId == "english_pep_2013_g3_s2_u6") {
            listOf(
                LetterModel('A', 'a', listOf(
                    LetterExample("hand", "手 /hænd/")
                )),
                LetterModel('E', 'e', listOf(
                    LetterExample("legs", "腿 /legz/"),
                    LetterExample("ten", "十 /ten/")
                )),
                LetterModel('I', 'i', listOf(
                    LetterExample("big", "大的 /bɪɡ/")
                )),
                LetterModel('O', 'o', listOf(
                    LetterExample("dog", "狗 /dɒɡ/")
                )),
                LetterModel('U', 'u', listOf(
                    LetterExample("duck", "鸭子 /dʌk/")
                ))
            )
        } else if (unitId.endsWith("_u6")) {
            listOf(
                LetterModel('U', 'u', listOf(LetterExample("umbrella", "雨伞"), LetterExample("under", "在...下面"))),
                LetterModel('V', 'v', listOf(LetterExample("vet", "兽医"), LetterExample("vest", "背心"))),
                LetterModel('W', 'w', listOf(LetterExample("wet", "湿的"), LetterExample("water", "水"))),
                LetterModel('X', 'x', listOf(LetterExample("fox", "狐狸"), LetterExample("box", "盒子"))),
                LetterModel('Y', 'y', listOf(LetterExample("yellow", "黄色"), LetterExample("yo-yo", "溜溜球"))),
                LetterModel('Z', 'z', listOf(LetterExample("zoo", "动物园"), LetterExample("zipper", "拉链")))
            )
        } else {
            listOf(
                LetterModel('O', 'o', listOf(LetterExample("orange", "橙子"), LetterExample("on", "在...上面"))),
                LetterModel('P', 'p', listOf(LetterExample("pig", "猪"), LetterExample("pen", "钢笔"))),
                LetterModel('Q', 'q', listOf(LetterExample("queen", "女王"), LetterExample("quiet", "安静的"))),
                LetterModel('R', 'r', listOf(LetterExample("red", "红色"), LetterExample("rice", "米饭"))),
                LetterModel('S', 's', listOf(LetterExample("Sarah", "莎拉"), LetterExample("six", "六"))),
                LetterModel('T', 't', listOf(LetterExample("tiger", "老虎"), LetterExample("ten", "十")))
            )
        }
    }

    var currentLetterIndex by remember { mutableStateOf(0) }
    var currentSubStage by remember { mutableStateOf(0) } // 0 = Recognition, 1 = Pairing Quiz, 2 = Phonics Quiz, 3 = Handwriting
    
    // Quiz states
    var pairingQuizAnswer by remember { mutableStateOf<Char?>(null) }
    var phonicsQuizAnswer by remember { mutableStateOf<Char?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

    // Hand writing references
    var handwritingRef by remember { mutableStateOf<EnglishHandwritingView?>(null) }
    var handwritingKey by remember { mutableStateOf(0) }

    val activeLetter = letters[currentLetterIndex]

    // Speaks the letter name on entry/change
    LaunchedEffect(currentLetterIndex, currentSubStage) {
        if (currentSubStage == 0) {
            ttsHelper.speak(activeLetter.upper.toString(), isSlow = false)
        }
        pairingQuizAnswer = null
        phonicsQuizAnswer = null
        isSubmitted = false
        isCorrect = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = getLessonTitle(unitId, EnglishLessonType.LESSON3),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "短元音 /自然拼读工坊",
                            color = Color(0xFFEC4899),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F172A))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Upper progress indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            letters.forEachIndexed { idx, letter ->
                val isAct = idx == currentLetterIndex
                val isDone = idx < currentLetterIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isAct) Color(0xFF00E5FF)
                            else if (isDone) Color(0xFF10B981)
                            else Color(0xFF334155)
                        )
                )
            }
        }

        // Sub stages visual steps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val steps = listOf("认读认知", "字母配对", "首音辨析", "规范手写")
            steps.forEachIndexed { sIdx, sTitle ->
                val active = sIdx == currentSubStage
                Text(
                    text = sTitle,
                    color = if (active) Color(0xFFEC4899) else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // Main display card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.5.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (currentSubStage) {
                    0 -> {
                        // Sub Stage 0: Recognition
                        Text(
                            text = "💡 字母认知 (Letters Recognition)",
                            color = Color(0xFF00E5FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        // Gigantic glyph display
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeLetter.upper}${activeLetter.lower}",
                                color = Color.White,
                                fontSize = 84.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Serif
                            )
                            IconButton(
                                onClick = { ttsHelper.speak(activeLetter.upper.toString(), isSlow = false) },
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFEC4899).copy(alpha = 0.15f), CircleShape)
                                    .border(BorderStroke(1.5.dp, Color(0xFFEC4899)), CircleShape)
                            ) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = "朗读字母音", tint = Color(0xFFEC4899), modifier = Modifier.size(28.dp))
                            }
                        }

                        HorizontalDivider(color = Color(0xFF334155))

                        Text(text = "首音关联例词 (Tap to Listen):", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            activeLetter.examples.forEach { example ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    border = BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { ttsHelper.speak(example.english, isSlow = false) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = example.english,
                                            color = Color(0xFF00E5FF),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif
                                        )
                                        Text(text = example.chinese, color = Color.LightGray, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Sub Stage 1: Pairing Quiz
                        Text(
                            text = "🔗 大小写配对 (Uppercase to Lowercase)",
                            color = Color(0xFF00E5FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "请找出字母 \"${activeLetter.upper}\" 对应的小写字母:",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val options = remember(activeLetter) {
                            val wrongAnswers = letters.filter { it.lower != activeLetter.lower }.map { it.lower }.shuffled().take(2)
                            (wrongAnswers + activeLetter.lower).shuffled()
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            options.forEach { opt ->
                                val isSelected = pairingQuizAnswer == opt
                                Button(
                                    onClick = {
                                        if (!isSubmitted) {
                                            pairingQuizAnswer = opt
                                            ttsHelper.speak(opt.toString(), isSlow = false)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFFEC4899) else Color(0xFF0F172A)
                                    ),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isSelected) Color(0xFFEC4899) else Color(0xFF334155)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp)
                                ) {
                                    Text(text = opt.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    2 -> {
                        // Sub Stage 2: Phonics Quiz (首音辨析)
                        Text(
                            text = "👂 首音听音辨析 (Phonics Auditory Quiz)",
                            color = Color(0xFF00E5FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        val targetExample = remember(activeLetter) { activeLetter.examples.first() }

                        Text(
                            text = "点击播放单词，听辨其首字母 (Starting sound):",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Button(
                            onClick = { ttsHelper.speak(targetExample.english, isSlow = false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = "播放")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("朗读单词 \"${targetExample.english}\"", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val options = remember(activeLetter) {
                            val wrongUpper = letters.filter { it.upper != activeLetter.upper }.map { it.upper }.shuffled().take(2)
                            (wrongUpper + activeLetter.upper).shuffled()
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            options.forEach { opt ->
                                val isSelected = phonicsQuizAnswer == opt
                                Button(
                                    onClick = {
                                        if (!isSubmitted) {
                                            phonicsQuizAnswer = opt
                                            ttsHelper.speak(opt.toString(), isSlow = false)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF00E5FF) else Color(0xFF0F172A)
                                    ),
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp)
                                ) {
                                    Text(text = opt.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.Black else Color.White)
                                }
                            }
                        }
                    }

                    3 -> {
                        // Sub Stage 3: Four lines three grids handwriting
                        Text(
                            text = "✍️ 规范手写练字板 (Four-Line Three-Grid Canvas)",
                            color = Color(0xFF00E5FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "请临摹规范书写: \"${activeLetter.upper}${activeLetter.lower}\"",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Handwriting canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF12161A))
                        ) {
                            key(handwritingKey) {
                                AndroidView(
                                    factory = { ctx ->
                                        EnglishHandwritingView(ctx).apply {
                                            currentBrush = equippedBrushStyle
                                            currentBrushConfig = equippedBrushConfig
                                            handwritingRef = this
                                        }
                                    },
                                    update = { view ->
                                        view.currentBrush = equippedBrushStyle
                                        view.currentBrushConfig = equippedBrushConfig
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Local Undo & Clear controls
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { handwritingRef?.undo() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Undo, contentDescription = "撤销")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("撤销", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { handwritingRef?.clear() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "清空")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("清空", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Quiz feedback area
        if (isSubmitted) {
            val alertBg = if (isCorrect) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
            val alertBorder = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
            val alertText = if (isCorrect) "🎉 恭喜你回答正确！非常棒！" else "❌ 回答错误，不妨再仔细听听或认读一下喔。"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(alertBg, RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, alertBorder), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alertText,
                    color = alertBorder,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Previous/Next primary button controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentSubStage > 0 || currentLetterIndex > 0) {
                OutlinedButton(
                    onClick = {
                        if (currentSubStage > 0) {
                            currentSubStage--
                        } else {
                            currentLetterIndex--
                            currentSubStage = 3 // go to handwriting of previous letter
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("上一步 (Back)", fontSize = 14.sp)
                }
            }

            Button(
                onClick = {
                    if (currentSubStage == 1) {
                        if (pairingQuizAnswer == null) {
                            Toast.makeText(context, "请先选择一个小写字母进行配对", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!isSubmitted) {
                            isSubmitted = true
                            isCorrect = pairingQuizAnswer == activeLetter.lower
                            if (isCorrect) ttsHelper.speak("Yes", isSlow = false) else ttsHelper.speak("No", isSlow = false)
                        } else {
                            currentSubStage++
                        }
                    } else if (currentSubStage == 2) {
                        if (phonicsQuizAnswer == null) {
                            Toast.makeText(context, "请先选择词首对应的字母", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!isSubmitted) {
                            isSubmitted = true
                            isCorrect = phonicsQuizAnswer == activeLetter.upper
                            if (isCorrect) ttsHelper.speak("Yes", isSlow = false) else ttsHelper.speak("No", isSlow = false)
                        } else {
                            currentSubStage++
                        }
                    } else if (currentSubStage == 3) {
                        // Handwriting complete
                        if (currentLetterIndex < letters.size - 1) {
                            currentLetterIndex++
                            currentSubStage = 0
                        } else {
                            onLessonCompleted()
                        }
                    } else {
                        // Step 0 recognition to pairing
                        currentSubStage++
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSubmitted && !isCorrect) Color(0xFFEF4444) else Color(0xFFEC4899)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .height(52.dp)
                    .testTag("letter_next_btn")
            ) {
                val btnText = when (currentSubStage) {
                    1, 2 -> if (isSubmitted) "下一步 (Next)" else "确认答案 (Verify)"
                    3 -> if (currentLetterIndex < letters.size - 1) "下一字母 (Next Letter)" else "完成课时 (Finish)"
                    else -> "下一步 (Next)"
                }
                Text(text = btnText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
}
