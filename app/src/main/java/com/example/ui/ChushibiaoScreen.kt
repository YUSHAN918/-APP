package com.example.ui

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChushiParagraph(
    val id: Int,
    val original: String,
    val translation: String,
    val annotations: List<Pair<String, String>> // Highlight word -> Explanation
)

object ChushiData {
    val paragraphs = listOf(
        ChushiParagraph(
            id = 1,
            original = "先帝创业未半而中道崩殂，今天下三分，益州疲弊，此诚危急存亡之秋也。然侍卫之臣不懈于内，忠志之士忘身于外者，盖追先帝之殊遇，欲报之于陛下也。诚宜开张圣听，以光先帝遗德，恢弘志士之气，不宜妄自菲薄，引喻失义，以塞忠谏之路也。",
            translation = "先帝刘备创办大业还没完成一半，却在中途去世了。现在天下分裂为三国，益州土地民力疲劳困顿，这实在是危急存亡的关键时刻。但是宫廷内侍卫的臣子不敢松懈，忠心有志的将士在外面舍身忘死，这大概是追念先帝给予的特别厚待，想要报答在陛下您的身上啊。确实应该广泛听取臣民的意见，以此发扬光大先帝遗留的美德，振奋有志之士的勇气，不应该随随便便看轻自己，说话不合道理，以致堵塞了忠臣劝谏的道路。",
            annotations = listOf(
                "中道崩殂" to "半路去世。崩：古代指皇帝死亡；殂：死亡。",
                "三分" to "魏、蜀、吴三分天下。",
                "疲弊" to "疲劳困顿，国力空虚。",
                "之秋" to "的时候。秋常表示重要的节点或危机之秋。",
                "殊遇" to "特别优厚的待遇。",
                "开张圣听" to "广泛倾听臣民意见。圣听指君王的听觉。",
                "恢弘" to "发扬光大。",
                "妄自菲薄" to "毫无根据地看轻自己。",
                "引喻失义" to "说话不合情理，比喻不恰当。"
            )
        ),
        ChushiParagraph(
            id = 2,
            original = "宫中府中，俱为一体，陟罚臧否，不宜异同。若有作奸犯科及为忠善者，宜付有司论其刑赏，以昭陛下平明之理，不宜偏私，使内外异法也。",
            translation = "皇宫里和丞相府里，都是一个整体。晋升、惩罚、赞扬、批评，不应当标准不同。如果有做坏事触犯法律、或者尽忠做善事的，应该交给主管部门判定他们受罚或受赏，以此来彰显陛下公平清明的国家治理，不应该偏心自私，导致皇宫内外执行着不同的法律法度。",
            annotations = listOf(
                "陟罚臧否" to "陟：提升；罚：惩罚；臧：赞赏；否：批评。指官吏的升降考核。",
                "作奸犯科" to "奸邪做坏事，触犯国家的法律条规。",
                "有司" to "专门管理和负责具体部门的官员官吏。",
                "平明之理" to "公平清明的治理治理政策。",
                "内外异法" to "宫中（皇室）和府中（丞相府）执行不同的法律标准。"
            )
        ),
        ChushiParagraph(
            id = 3,
            original = "侍中、侍郎郭攸之、费祎、董允等，此皆良实，志虑忠纯，是以先帝简拔以遗陛下。愚以为宫中之事，事无大小，悉以咨之，然后施行，必能裨补阙漏，有所广益。",
            translation = "侍中郭攸之、费祎，侍郎董允等人，这些都是善良忠实的人，他们的心志思虑极为忠诚纯正，所以先帝在世时把他们选拔出来留给陛下。我认为皇宫里的事务，不论大小，都完全应该向他们商量咨询，然后去执行。这一定能够弥补漏洞和过失，得到更多的成效和极大的益处。",
            annotations = listOf(
                "良实" to "善良诚实的人。",
                "简拔" to "挑选并提拔。简：挑选。",
                "遗陛下" to "遗留给陛下。遗（wèi）：留给、赠送。",
                "裨补阙漏" to "裨：弥补、帮助；阙（quē）：缺点、漏洞。意为弥补一切疏忽和漏洞。"
            )
        ),
        ChushiParagraph(
            id = 4,
            original = "将军向宠，性行淑均，晓畅军事，试用于昔日，先帝称之曰能，是以众议举宠为督。愚以为营中之事，悉以咨之，必能使行阵和睦，优劣得所。",
            translation = "将军向宠，品德作风善良公正，通晓军事。在过去尝试任用他时，先帝多次夸赞他很有才能，所以大家一致商议推举向宠担任中部督。我认为军营中的一切防务大小事务，都完全可以去咨询他，必定能够使军中队伍团结和睦，优秀或才能稍弱的士兵都能得到妥善的安排与重用。",
            annotations = listOf(
                "性行淑均" to "性情、言行、品格善良而公正（均：公正）。",
                "晓畅" to "非常通晓、熟悉。",
                "行阵" to "指军营、行伍，特指军事队伍。",
                "优劣得所" to "指优秀和一般的人都能找到合适自己的岗位，各得其所。"
            )
        ),
        ChushiParagraph(
            id = 5,
            original = "亲贤臣，远小人，此先汉所以兴隆也；亲小人，远贤臣，此后汉所以倾颓也。先帝在时，每与臣论此事，未尝不叹息痛恨于桓、灵也。侍中、尚书、长史、参军，此悉贞良死节之臣，愿陛下亲之信之，则汉室之隆，可计日而待也。",
            translation = "亲近有贤德的臣子，疏远奸诈的小人，这是西汉之所以能够兴旺昌盛的原因；亲近小人，疏远贤臣，这是东汉之所以中途衰落败亡的原因。先帝在世的时候，每次与我讨论起这个历史教训，没有一次不对汉桓帝、汉灵帝感到叹息、痛心与遗憾的。侍中、尚书、长史、参军，这些全都是坚贞优秀、愿意为道义和国家赴死的忠臣。希望陛下能够多多亲近他们、信任他们，那么汉室的再次兴盛，简直就可以数着日子来等待实现了。",
            annotations = listOf(
                "倾颓" to "衰败、倾覆、倒下。",
                "痛恨" to "感到痛心与非常遗憾。不同于现代汉语的极度怨恨。",
                "死节" to "为了国家、忠贞的节操和气节而死。",
                "计日而待" to "数着日子就能等得到。指指日可待、非常快就能实现。"
            )
        ),
        ChushiParagraph(
            id = 6,
            original = "臣本布衣，躬耕于南阳，苟全性命于乱世，不求闻达于诸侯。先帝不以臣卑鄙，猥自枉屈，三顾臣于草庐之中，咨臣以当世之事，由是感激，遂许先帝以驱驰。后值倾覆，受任于败军之际，奉命于危难之间，尔来二十有一年矣。",
            translation = "我本来就是一个老百姓，亲自在南阳种田过活，只希望在兵荒马乱的世道中苟且保全性命，从没想过在诸侯争霸中谋求出名和高官厚禄。先帝没有因为我地位低下、见识浅陋而嫌弃我，反而委屈了自己，接连三次来到草屋来看望我，向我询问当今天下的局势大事，我因为深受感动，于是答应为先帝奔走奔波。后来遇到兵败受挫，在战事覆灭的关头我接受了重托，在危急和困难的关键时刻奉命出使，从那时到现在，已经整整过了二十一年了啊。",
            annotations = listOf(
                "布衣" to "老百姓。古代百姓穿麻布制衣，因此代指平民。",
                "躬耕" to "亲自下田劳动耕作。",
                "闻达" to "出名、声名显赫、发迹闻名。",
                "卑鄙" to "地位卑微，见识粗浅（古代褒贬中性，非现在卑劣不耻的意思）。",
                "猥自枉屈" to "委屈自己，降低了身份。",
                "三顾臣" to "三次拜访我。即著名的“三顾茅庐”典故。",
                "驱驰" to "奔走效劳、听从调遣使唤。",
                "尔来" to "自那以来、从那时到现在。"
            )
        ),
        ChushiParagraph(
            id = 7,
            original = "先帝知臣谨慎，故临崩寄臣以大事也。受命以来，夙夜忧叹，恐托付不效，以伤先帝之明，故五月渡泸，深入不毛。今南方已定，兵甲已足，当奖率三军，北定中原，庶竭驽钝，攘除奸凶，兴复汉室，还于旧都。此臣所以报先帝而忠陛下之职分也。至于斟酌损益，进尽忠言，则攸之、祎、允之任也。",
            translation = "先帝深知我做事极其细心谨慎，所以在临终病危前将国家大事托付托付给我。自接受遗诏以来，我日夜叹息发愁，生怕辜负了重托，损害了先帝知人善任的圣明，所以我在酷热的五月渡过泸水，深入到荒凉贫瘠的蛮荒之地。现在南方反叛已平定，军备武器也已充沛。应该鼓励并率领大军平定北方中原！希望尽到我平庸愚钝的毕生精力，铲除天下邪恶凶险的奸贼，重振汉朝江山，返回当年的故都洛阳。这是我报答先帝、效忠陛下应该尽到的神圣本分。至于处理政务中加减取舍，提出最忠心的建言，那就是郭攸之、费祎、董允他们的职责了。",
            annotations = listOf(
                "临崩寄臣" to "刘备临死前在白帝城嘱托诸葛亮，即“白帝城托孤”。",
                "夙夜" to "从早到晚，日夜不停。夙：早晨；夜：夜晚。",
                "不毛" to "连毛发草木都不生长的贫瘠蛮荒地带。",
                "驽钝" to "比喻才能愚钝、低下。驽：资质差的马；钝：刀不锋利。",
                "攘除" to "排除、彻底扫除、铲除。",
                "损益" to "损：减少，指不合理的政务；益：增加。这里指政务的裁决权衡。"
            )
        ),
        ChushiParagraph(
            id = 8,
            original = "愿陛下托臣以讨贼兴复之效，不效，则治臣之罪，以告先帝之灵。若无兴德之言，则责攸之、祎、允等之慢，以彰其咎；陛下亦宜自谋，以咨诹善道，察纳雅言，深追先帝遗诏。臣不胜受恩感激。今当远离，临表涕零，不知所言。",
            translation = "希望陛下能交付给我讨伐曹贼、复兴汉朝的战斗成效和任务。如果不成功，就惩治我监督不力的重罪，来告慰先帝的在天之灵！如果没有进献兴隆德行方面的良好建议，就请陛下斥责惩罚郭攸之、董允、费祎他们的懒惰怠慢，来揭示他们的过错。陛下您也应当亲自多加思考谋求好策略，向德才兼备的人询问、征求治国的良方，考察并采纳正确的建议，深深追念先帝白帝城中遗留的旨言。我承受如此深恩，真是受宠若惊，不胜感激。今天我将率军远离成都出征，面对这份临行的奏表我止不住流下热泪，不知道到底都说了些什么话了。",
            annotations = listOf(
                "效" to "这里指交付的重要使命和工作任务。",
                "慢" to "怠慢、懒散疏忽。",
                "其咎" to "他们的过错和不称职。咎：罪责、过失。",
                "咨诹善道" to "向智者商讨询问治国安邦的好办法。诹（zōu）：询问。",
                "察纳雅言" to "考察采纳正确的建言。雅言：正确高尚的言论。",
                "临表涕零" to "面对这份奏表禁不住流下眼泪。零：落下。涕：眼泪。"
            )
        )
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChushibiaoScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: 诵读馆, 1: 听书阁, 2: 背诵坛
    val scope = rememberCoroutineScope()

    // Screen Layout
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(GameUiTokens.Colors.Surface)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = GameUiTokens.Colors.TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "诸葛武侯 • 《出师表》",
                        color = GameUiTokens.Colors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // Small decorative seal
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .border(1.dp, GameUiTokens.Colors.NeonRed, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "出师",
                            color = GameUiTokens.Colors.NeonRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Tabs Custom HUD-style
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(width = 1.dp, color = GameUiTokens.Colors.Border, shape = RoundedCornerShape(0.dp))
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabNames = listOf("诵读馆", "听书阁", "背诵坛")
                    tabNames.forEachIndexed { index, name ->
                        val isSelected = selectedTab == index
                        val borderAlpha by animateFloatAsState(if (isSelected) 1f else 0f)
                        val textColor by animateColorAsState(if (isSelected) GameUiTokens.Colors.Gold else GameUiTokens.Colors.TextSecondary)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { selectedTab = index }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                                .drawBehind {
                                    if (isSelected) {
                                        drawLine(
                                            color = GameUiTokens.Colors.Gold,
                                            start = androidx.compose.ui.geometry.Offset(0f, size.height),
                                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                                            strokeWidth = 6f
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        containerColor = GameUiTokens.Colors.Background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ReadingSection()
                1 -> ListeningSection()
                2 -> RecitingSection(viewModel)
            }
        }
    }
}

// ======================= 1. READING SECTION =======================
@Composable
fun ReadingSection() {
    val scrollState = rememberScrollState()
    var selectedAnnotation by remember { mutableStateOf<Pair<String, String>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tips box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(GameUiTokens.Shapes.Panel)
                .background(GameUiTokens.Colors.Surface)
                .border(1.dp, GameUiTokens.Colors.Border, GameUiTokens.Shapes.Panel)
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("💡", fontSize = 18.sp)
                Text(
                    text = "点击文章中的高亮加粗词汇，即可在下方查看少儿专属详细注释哦！",
                    color = GameUiTokens.Colors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Reading view scroll
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .background(GameUiTokens.Colors.Surface.copy(alpha = 0.5f), GameUiTokens.Shapes.Panel)
                .border(1.dp, GameUiTokens.Colors.Border, GameUiTokens.Shapes.Panel)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ChushiData.paragraphs.forEach { paragraph ->
                ParagraphCard(
                    paragraph = paragraph,
                    onAnnotationClick = { word, explanation ->
                        selectedAnnotation = word to explanation
                    }
                )
            }
        }

        // Expanded Annotation View at bottom
        AnimatedVisibility(
            visible = selectedAnnotation != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedAnnotation?.let { (word, expl) ->
                Surface(
                    color = GameUiTokens.Colors.Surface,
                    shape = GameUiTokens.Shapes.Panel,
                    border = BorderStroke(1.dp, GameUiTokens.Colors.NeonCyan.copy(alpha = 0.6f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(GameUiTokens.Colors.NeonCyan.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = "注释",
                                    tint = GameUiTokens.Colors.NeonCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "词汇注释 • $word",
                                color = GameUiTokens.Colors.Gold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { selectedAnnotation = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭",
                                    tint = GameUiTokens.Colors.TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = expl,
                            color = GameUiTokens.Colors.TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParagraphCard(
    paragraph: ChushiParagraph,
    onAnnotationClick: (String, String) -> Unit
) {
    var showTranslation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // draw small vertical left accent line for each paragraph
                drawLine(
                    color = GameUiTokens.Colors.NeonCyan.copy(alpha = 0.4f),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, size.height),
                    strokeWidth = 4f
                )
            }
            .padding(start = 12.dp)
    ) {
        // Paragraph original with annotated text highlights
        val annotatedOriginal = buildAnnotatedString {
            var remaining = paragraph.original
            while (remaining.isNotEmpty()) {
                // Find first occurrence of any annotation key
                val match = paragraph.annotations.map { ann ->
                    val index = remaining.indexOf(ann.first)
                    Triple(ann.first, ann.second, index)
                }.filter { it.third != -1 }.minByOrNull { it.third }

                if (match != null) {
                    val (word, explanation, index) = match
                    // Append text before word
                    append(remaining.substring(0, index))
                    // Push annotation link and custom style
                    pushStringAnnotation(tag = word, annotation = explanation)
                    withStyle(
                        style = SpanStyle(
                            color = GameUiTokens.Colors.NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            background = GameUiTokens.Colors.NeonCyan.copy(alpha = 0.12f)
                        )
                    ) {
                        append(word)
                    }
                    pop()
                    remaining = remaining.substring(index + word.length)
                } else {
                    append(remaining)
                    remaining = ""
                }
            }
        }

        // Display ClickableText using modern ClickableText block or basic SelectionContainer/Gesture
        androidx.compose.foundation.text.ClickableText(
            text = annotatedOriginal,
            style = LocalTextStyle.current.copy(
                color = GameUiTokens.Colors.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontFamily = FontFamily.Serif
            ),
            onClick = { offset ->
                annotatedOriginal.getStringAnnotations(offset, offset).firstOrNull()?.let { annotation ->
                    onAnnotationClick(annotation.tag, annotation.item)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Row containing Translation toggle and speaker icon placeholder
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Paragraph Id tag
            Box(
                modifier = Modifier
                    .background(GameUiTokens.Colors.SurfaceVariant, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "段落 ${paragraph.id}",
                    color = GameUiTokens.Colors.TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = { showTranslation = !showTranslation },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(
                    imageVector = if (showTranslation) Icons.Default.VisibilityOff else Icons.Default.Translate,
                    contentDescription = "译文",
                    tint = GameUiTokens.Colors.Gold,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (showTranslation) "隐藏译文" else "查看解析译文",
                    color = GameUiTokens.Colors.Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Animated Translation Details
        AnimatedVisibility(
            visible = showTranslation,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GameUiTokens.Colors.SurfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp)
            ) {
                Text(
                    text = "【白话译文】",
                    color = GameUiTokens.Colors.Gold.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = paragraph.translation,
                    color = GameUiTokens.Colors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}


// ======================= 2. LISTENING SECTION =======================
data class SpeechSegment(
    val id: Int,
    val text: String,
    val paragraphIndex: Int,
    val textOffsetInParagraph: Int,
    val sentenceIndexInParagraph: Int
)

fun splitIntoSentences(text: String): List<String> {
    val sentences = mutableListOf<String>()
    var current = StringBuilder()
    for (char in text) {
        current.append(char)
        if (char == '。' || char == '；' || char == '！' || char == '？') {
            sentences.add(current.toString().trim())
            current = StringBuilder()
        }
    }
    if (current.isNotEmpty()) {
        val remaining = current.toString().trim()
        if (remaining.isNotEmpty()) {
            sentences.add(remaining)
        }
    }
    return sentences
}

@Composable
fun ListeningSection() {
    val paragraphs = ChushiData.paragraphs
    var currentPlayingIndex by remember { mutableStateOf(-1) }
    var currentSegmentIndex by remember { mutableStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    var playSpeed by remember { mutableStateOf(1.0f) } // 0.75f, 1.0f, 1.25f
    var progressSeconds by remember { mutableStateOf(0) }

    // Flatten all paragraphs into sequential sentences (segments)
    val allSegments = remember(paragraphs) {
        val list = mutableListOf<SpeechSegment>()
        var segmentId = 0
        paragraphs.forEachIndexed { pIdx, paragraph ->
            val sentences = splitIntoSentences(paragraph.original)
            var offset = 0
            sentences.forEachIndexed { sIdx, text ->
                list.add(
                    SpeechSegment(
                        id = segmentId++,
                        text = text,
                        paragraphIndex = pIdx,
                        textOffsetInParagraph = offset,
                        sentenceIndexInParagraph = sIdx
                    )
                )
                offset += text.length
            }
        }
        list
    }

    // Estimate realistic speaking duration for each sentence in seconds
    val segmentDurations = remember(allSegments) {
        allSegments.map { segment ->
            val chars = segment.text.count { it != '，' && it != '。' && it != '；' && it != '！' && it != '？' }
            val duration = (chars * 0.35f + 0.8f).toInt().coerceAtLeast(2)
            duration
        }
    }

    // Calculate start time in seconds for each sentence
    val segmentStartTimes = remember(segmentDurations) {
        val starts = IntArray(segmentDurations.size)
        var currentStart = 0
        for (i in segmentDurations.indices) {
            starts[i] = currentStart
            currentStart += segmentDurations[i]
        }
        starts
    }

    val computedTotalSeconds = remember(segmentDurations) {
        segmentDurations.sum()
    }

    fun getSegmentIndexForProgress(progress: Int): Int {
        for (i in segmentStartTimes.indices.reversed()) {
            if (progress >= segmentStartTimes[i]) {
                return i
            }
        }
        return 0
    }

    val listScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    // Speak a specific segment
    val speakSegment = { index: Int ->
        if (isTtsReady && tts != null && index in allSegments.indices) {
            val segment = allSegments[index]
            currentSegmentIndex = index
            currentPlayingIndex = segment.paragraphIndex
            
            tts?.setSpeechRate(playSpeed)
            val params = android.os.Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "chushibiao_seg_$index")
            tts?.speak(segment.text, TextToSpeech.QUEUE_FLUSH, params, "chushibiao_seg_$index")
        }
    }
    val speakFromCurrentProgress = {
        if (isTtsReady && tts != null && currentSegmentIndex in allSegments.indices) {
            val segment = allSegments[currentSegmentIndex]
            val segStart = segmentStartTimes[currentSegmentIndex]
            val segDuration = segmentDurations[currentSegmentIndex]
            val elapsed = (progressSeconds - segStart).coerceIn(0, segDuration)
            
            val totalChars = segment.text.length
            val elapsedFraction = if (segDuration > 0) elapsed.toFloat() / segDuration else 0f
            val charsSpoken = (totalChars * elapsedFraction).toInt().coerceIn(0, totalChars)
            
            val remainingText = segment.text.substring(charsSpoken)
            
            tts?.setSpeechRate(playSpeed)
            val params = android.os.Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "chushibiao_seg_${currentSegmentIndex}")
            
            if (remainingText.isNotBlank()) {
                tts?.speak(remainingText, TextToSpeech.QUEUE_FLUSH, params, "chushibiao_seg_${currentSegmentIndex}")
            } else {
                val nextIdx = currentSegmentIndex + 1
                if (nextIdx < allSegments.size) {
                    currentSegmentIndex = nextIdx
                    progressSeconds = segmentStartTimes[nextIdx]
                    speakSegment(nextIdx)
                } else {
                    isPlaying = false
                    currentSegmentIndex = -1
                    currentPlayingIndex = -1
                    progressSeconds = 0
                }
            }
        }
    }

    val startPlayback = { index: Int ->
        isPlaying = true
        speakSegment(index)
    }

    val startPlaybackForParagraph = { paragraphIndex: Int ->
        val firstSegIdx = allSegments.indexOfFirst { it.paragraphIndex == paragraphIndex }
        if (firstSegIdx != -1) {
            progressSeconds = segmentStartTimes[firstSegIdx]
            currentSegmentIndex = firstSegIdx
            isPlaying = true
            speakSegment(firstSegIdx)
        }
    }

    val pausePlayback = {
        tts?.stop()
        isPlaying = false
    }

    // Initialize TTS Engine
    DisposableEffect(Unit) {
        val speechListener = object : TextToSpeech.OnInitListener {
            override fun onInit(status: Int) {
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.CHINESE)
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        isTtsReady = true
                    }
                }
            }
        }
        val textToSpeech = TextToSpeech(context, speechListener)
        tts = textToSpeech

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    // Set Utterance Progress Listener for synchronization
    LaunchedEffect(tts, isTtsReady) {
        if (isTtsReady && tts != null) {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Handler(Looper.getMainLooper()).post {
                        isPlaying = true
                    }
                }

                override fun onDone(utteranceId: String?) {
                    val completedIdx = utteranceId?.substringAfter("chushibiao_seg_")?.toIntOrNull() ?: -1
                    if (completedIdx != -1) {
                        val nextIdx = completedIdx + 1
                        Handler(Looper.getMainLooper()).post {
                            if (nextIdx < allSegments.size) {
                                currentSegmentIndex = nextIdx
                                progressSeconds = segmentStartTimes[nextIdx]
                                speakSegment(nextIdx)
                            } else {
                                isPlaying = false
                                currentSegmentIndex = -1
                                currentPlayingIndex = -1
                                progressSeconds = 0
                            }
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    Handler(Looper.getMainLooper()).post {
                        isPlaying = false
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Handler(Looper.getMainLooper()).post {
                        isPlaying = false
                    }
                }
            })
        }
    }

    // Auto-scroll current reading line into focus
    LaunchedEffect(currentPlayingIndex) {
        if (currentPlayingIndex in 0 until paragraphs.size) {
            // Smoothly scrolls paragraphs during playback
            val offsetPx = (currentPlayingIndex * 240)
            listScrollState.animateScrollTo(offsetPx)
        }
    }

    // Audio progress visual counter synchronized with TTS segment bounds
    LaunchedEffect(isPlaying, currentSegmentIndex, playSpeed) {
        if (isPlaying && currentSegmentIndex in allSegments.indices) {
            var currentProgress = progressSeconds
            val segmentEndTime = segmentStartTimes[currentSegmentIndex] + segmentDurations[currentSegmentIndex]
            
            while (isPlaying && currentProgress < segmentEndTime && currentProgress < computedTotalSeconds) {
                progressSeconds = currentProgress
                delay((1000 / playSpeed).toLong())
                currentProgress++
            }
        }
    }

    // Custom spin vinyl record animation for children
    val rotationAngle = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            rotationAngle.animateTo(
                targetValue = rotationAngle.value + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 6000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rotationAngle.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Classic Wooden Vinyl Record Player Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(GameUiTokens.Shapes.Panel)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GameUiTokens.Colors.SurfaceVariant, GameUiTokens.Colors.Surface)
                    )
                )
                .border(1.5.dp, GameUiTokens.Colors.Border, GameUiTokens.Shapes.Panel),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Spinning vinyl record visualizer
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D1117))
                        .border(4.dp, Color(0xFF1E2530), CircleShape)
                        .border(6.dp, GameUiTokens.Colors.Border, CircleShape)
                        .rotate(rotationAngle.value),
                    contentAlignment = Alignment.Center
                ) {
                    // Radial tracks
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    )
                    // Core historical emblem
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GameUiTokens.Colors.Gold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "蜀",
                            color = GameUiTokens.Colors.Background,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }

                // Audio Info & Stats
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "《出师表》配乐诵读",
                        color = GameUiTokens.Colors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "讲读者：少儿专属 AI 书童",
                        color = GameUiTokens.Colors.TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Live paragraph text snippet
                    val playingText = if (currentPlayingIndex in paragraphs.indices) {
                        "“" + paragraphs[currentPlayingIndex].original.take(12) + "...”"
                    } else {
                        "点击播放，沉浸式品读经典"
                    }
                    Text(
                        text = playingText,
                        color = GameUiTokens.Colors.NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }

        // Center: Auto Scrollable Transcript highlights
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(listScrollState)
                .clip(GameUiTokens.Shapes.Panel)
                .background(GameUiTokens.Colors.Surface.copy(alpha = 0.4f))
                .border(1.dp, GameUiTokens.Colors.Border, GameUiTokens.Shapes.Panel)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            paragraphs.forEachIndexed { index, paragraph ->
                val isCurrentlyCurrent = index == currentPlayingIndex
                val scale by animateFloatAsState(if (isCurrentlyCurrent) 1.02f else 1.0f)
                val bgAlpha by animateFloatAsState(if (isCurrentlyCurrent) 0.15f else 0.0f)
                val cardBorderColor by animateColorAsState(if (isCurrentlyCurrent) GameUiTokens.Colors.NeonCyan else Color.Transparent)
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GameUiTokens.Colors.NeonCyan.copy(alpha = bgAlpha))
                        .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
                        .scale(scale)
                        .clickable {
                            startPlaybackForParagraph(index)
                        }
                        .padding(12.dp)
                ) {
                    Text(
                        text = "第 ${index + 1} 自然段",
                        color = if (isCurrentlyCurrent) GameUiTokens.Colors.Gold else GameUiTokens.Colors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = paragraph.original,
                        color = if (isCurrentlyCurrent) GameUiTokens.Colors.TextPrimary else GameUiTokens.Colors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
 
        // Bottom Controls HUD Player
        Surface(
            color = GameUiTokens.Colors.Surface,
            shape = GameUiTokens.Shapes.Panel,
            border = BorderStroke(1.dp, GameUiTokens.Colors.Border)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Slider & Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val minutes = progressSeconds / 60
                    val seconds = progressSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        color = GameUiTokens.Colors.TextSecondary,
                        fontSize = 11.sp
                    )
 
                    Slider(
                        value = progressSeconds.coerceIn(0, computedTotalSeconds).toFloat(),
                        onValueChange = {
                            val newProgress = it.toInt().coerceIn(0, computedTotalSeconds)
                            progressSeconds = newProgress
                            val segIdx = getSegmentIndexForProgress(newProgress)
                            currentSegmentIndex = segIdx
                            currentPlayingIndex = allSegments[segIdx].paragraphIndex
                        },
                        onValueChangeFinished = {
                            if (isPlaying) {
                                speakSegment(currentSegmentIndex)
                            }
                        },
                        valueRange = 0f..computedTotalSeconds.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = GameUiTokens.Colors.NeonCyan,
                            inactiveTrackColor = GameUiTokens.Colors.Border,
                            thumbColor = GameUiTokens.Colors.Gold
                        ),
                        modifier = Modifier.weight(1f)
                    )
 
                    val totMin = computedTotalSeconds / 60
                    val totSec = computedTotalSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", totMin, totSec),
                        color = GameUiTokens.Colors.TextMuted,
                        fontSize = 11.sp
                    )
                }
 
                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Speed Changer Button
                    TextButton(
                        onClick = {
                            playSpeed = when (playSpeed) {
                                0.75f -> 1.0f
                                1.0f -> 1.25f
                                else -> 0.75f
                            }
                            if (isPlaying && currentSegmentIndex in allSegments.indices) {
                                speakSegment(currentSegmentIndex)
                            }
                        }
                    ) {
                        Text(
                            text = "倍速 ${playSpeed}x",
                            color = GameUiTokens.Colors.NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
 
                    // Main Play/Pause Button
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                pausePlayback()
                            } else {
                                val targetIdx = if (currentSegmentIndex == -1) 0 else currentSegmentIndex
                                startPlayback(targetIdx)
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(GameUiTokens.Colors.Gold, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "播放暂停",
                            tint = GameUiTokens.Colors.Background,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Reset Button
                    IconButton(
                        onClick = {
                            pausePlayback()
                            currentPlayingIndex = -1
                            progressSeconds = 0
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "重置",
                            tint = GameUiTokens.Colors.TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}


// ======================= 3. RECITATION SECTION =======================
@Composable
fun RecitingSection(viewModel: GameViewModel) {
    val paragraphs = ChushiData.paragraphs
    var currentParagraphIdx by remember { mutableStateOf(0) }
    var hideDifficulty by remember { mutableStateOf(1) } // 1: 初学乍练 (20%), 2: 略有建树 (50%), 3: 倒背如流 (100%)
    var showFullText by remember { mutableStateOf(false) }
    var userScoredCoins by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    val currentPara = paragraphs[currentParagraphIdx]

    // Create a deterministic masked text based on difficulty setting
    val maskedText = remember(currentPara, hideDifficulty) {
        val words = currentPara.original
        val charArray = words.toCharArray()
        
        // Pick character masks deterministically or pseudo-randomly to keep it consistent
        val gapRate = when (hideDifficulty) {
            1 -> 5  // Hide 1 in every 5 characters
            2 -> 2  // Hide 1 in every 2 characters
            else -> 1 // Hide all
        }

        buildAnnotatedString {
            for (i in charArray.indices) {
                val c = charArray[i]
                val isPunctuation = c == '，' || c == '。' || c == '、' || c == '；' || c == '：' || c == '！' || c == '？' || c == '“' || c == '”' || c == '✓'
                
                if (!isPunctuation && i % gapRate == 0) {
                    pushStringAnnotation(tag = "masked_$i", annotation = c.toString())
                    withStyle(
                        style = SpanStyle(
                            color = GameUiTokens.Colors.Gold,
                            background = GameUiTokens.Colors.Gold.copy(alpha = 0.2f),
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(" █ ")
                    }
                    pop()
                } else {
                    append(c.toString())
                }
            }
        }
    }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Selection Row: Paragraph Choice + Difficulty Choice
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(GameUiTokens.Shapes.Panel)
                .background(GameUiTokens.Colors.Surface)
                .border(1.dp, GameUiTokens.Colors.Border, GameUiTokens.Shapes.Panel)
                .padding(12.dp)
        ) {
            // Difficulty selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("关卡难度:", color = GameUiTokens.Colors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                val levels = listOf(1 to "初学乍练", 2 to "略有建树", 3 to "倒背如流")
                levels.forEach { (lvl, title) ->
                    val isSelected = hideDifficulty == lvl
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) GameUiTokens.Colors.Gold else GameUiTokens.Colors.SurfaceVariant)
                            .border(1.dp, if (isSelected) GameUiTokens.Colors.Gold else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable {
                                hideDifficulty = lvl
                                showFullText = false
                                feedbackMessage = null
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) GameUiTokens.Colors.Background else GameUiTokens.Colors.TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Paragraph Choice Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("段落选择:", color = GameUiTokens.Colors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paragraphs.forEach { p ->
                        val isSelected = currentParagraphIdx == p.id - 1
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) GameUiTokens.Colors.NeonCyan else GameUiTokens.Colors.SurfaceVariant)
                                .border(1.dp, if (isSelected) GameUiTokens.Colors.NeonCyan else Color.Transparent, CircleShape)
                                .clickable {
                                    currentParagraphIdx = p.id - 1
                                    showFullText = false
                                    feedbackMessage = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${p.id}",
                                color = if (isSelected) GameUiTokens.Colors.Background else GameUiTokens.Colors.TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Recitation Card Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(GameUiTokens.Shapes.Panel)
                .background(GameUiTokens.Colors.Surface.copy(alpha = 0.5f))
                .border(1.5.dp, GameUiTokens.Colors.Border, GameUiTokens.Shapes.Panel)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📖 默背区 (段落 ${currentPara.id})",
                        color = GameUiTokens.Colors.Gold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // Tap a gap tip
                    Text(
                        text = "提示：点击黑色遮挡块可显示字",
                        color = GameUiTokens.Colors.TextMuted,
                        fontSize = 11.sp
                    )
                }

                // Main Reading content
                var tappedGapText by remember { mutableStateOf<String?>(null) }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (showFullText) {
                            // Show full text clearly
                            Text(
                                text = currentPara.original,
                                color = GameUiTokens.Colors.TextPrimary,
                                fontSize = 15.sp,
                                lineHeight = 24.sp,
                                fontFamily = FontFamily.Serif
                            )
                        } else {
                            // Show masked interactive text
                            androidx.compose.animation.Crossfade(targetState = maskedText) { text ->
                                androidx.compose.foundation.text.ClickableText(
                                    text = text,
                                    style = LocalTextStyle.current.copy(
                                        color = GameUiTokens.Colors.TextPrimary,
                                        fontSize = 15.sp,
                                        lineHeight = 24.sp,
                                        fontFamily = FontFamily.Serif
                                    ),
                                    onClick = { offset ->
                                        text.getStringAnnotations(offset, offset).firstOrNull()?.let { annotation ->
                                            tappedGapText = "遮挡处的汉字是: 【 ${annotation.item} 】"
                                        }
                                    }
                                )
                            }
                        }

                        // Display transient single gap hint when tapped
                        AnimatedVisibility(visible = tappedGapText != null) {
                            tappedGapText?.let { hint ->
                                Surface(
                                    color = GameUiTokens.Colors.SurfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, GameUiTokens.Colors.Gold.copy(alpha = 0.5f)),
                                    modifier = Modifier.padding(top = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("💡", fontSize = 14.sp)
                                        Text(hint, color = GameUiTokens.Colors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(onClick = { tappedGapText = null }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, "关闭", tint = GameUiTokens.Colors.TextMuted, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive check & reward system
                feedbackMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GameUiTokens.Colors.NeonGreen.copy(alpha = 0.15f))
                            .border(1.dp, GameUiTokens.Colors.NeonGreen, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = msg,
                            color = GameUiTokens.Colors.NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Action controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Action 1: Toggle show/hide answer
            Button(
                onClick = { showFullText = !showFullText },
                colors = ButtonDefaults.buttonColors(containerColor = GameUiTokens.Colors.SurfaceVariant),
                shape = GameUiTokens.Shapes.Button,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (showFullText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "核对全文",
                    tint = GameUiTokens.Colors.TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = if (showFullText) "隐藏全文" else "核对原文", color = GameUiTokens.Colors.TextPrimary, fontSize = 13.sp)
            }

            // Action 2: Recited successfully reward trigger!
            Button(
                onClick = {
                    val coinReward = 50 * hideDifficulty
                    val expReward = 30 * hideDifficulty
                    viewModel.addPlayerCoins(coinReward)
                    viewModel.addPlayerExp(expReward)
                    
                    feedbackMessage = "🎉 太棒了！背诵通过！恭喜获得金币 +$coinReward，行者经验 +$expReward！继续加油吧！"
                    showFullText = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = GameUiTokens.Colors.Gold),
                shape = GameUiTokens.Shapes.Button,
                modifier = Modifier.weight(1.2f)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "我背完了",
                    tint = GameUiTokens.Colors.Background
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "我已背熟这段", color = GameUiTokens.Colors.Background, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
