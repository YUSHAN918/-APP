package com.example.data.english

import android.content.Context
import kotlin.random.Random

object EnglishReviewPoolBuilder {

    fun buildPool(
        context: Context,
        coveredUnitIds: List<String>,
        randomSeed: Long = 42L
    ): List<ReviewQuestionItem> {
        val random = Random(randomSeed)
        val pool = mutableListOf<ReviewQuestionItem>()

        val isG4S1 = coveredUnitIds.any { it.contains("g4_s1") }
        if (isG4S1) {
            val courseId = "english_pep_2013_g4_s1"
            
            // 1. TextbookDerived Questions from P69 theme (Real textbook questions on family, rooms, food, schoolbag, friend)
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_p69_schoolbag",
                    sourceUnitId = "english_pep_2013_g4_s1_u1",
                    promptText = "What's in your schoolbag? (书包里有什么？)",
                    promptTranslation = "真实课本P69原题衍生",
                    questionType = "EXPRESSION_CHOICE",
                    options = listOf(
                        "An English book, a maths book and a pencil box.",
                        "It is near the window.",
                        "His name is Zhang Peng.",
                        "No, they aren't."
                    ),
                    correctIndex = 0,
                    explanation = "P69 提问书包里装了什么，正确回答书本和铅笔盒等物品。",
                    textbookDerived = true,
                    sourceReference = "P69"
                )
            )
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_p69_friend_name",
                    sourceUnitId = "english_pep_2013_g4_s1_u3",
                    promptText = "What's her name? (她的名字是什么？)",
                    promptTranslation = "真实课本P69原题衍生",
                    questionType = "EXPRESSION_CHOICE",
                    options = listOf(
                        "Her name is Chen Jie.",
                        "His name is Zhang Peng.",
                        "Yes, she is.",
                        "She is a doctor."
                    ),
                    correctIndex = 0,
                    explanation = "her 提问女性名字，用 Her name is Chen Jie.",
                    textbookDerived = true,
                    sourceReference = "P69"
                )
            )
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_p69_friend_look",
                    sourceUnitId = "english_pep_2013_g4_s1_u3",
                    promptText = "He has short hair and glasses. Who is he? (他留短发、戴眼镜，他是谁？)",
                    promptTranslation = "真实课本P69原题衍生",
                    questionType = "EXPRESSION_CHOICE",
                    options = listOf(
                        "He is Zhang Peng.",
                        "She is Sarah.",
                        "He is a driver.",
                        "It is near the phone."
                    ),
                    correctIndex = 0,
                    explanation = "短发和眼镜是 Zhang Peng 的典型外貌特征。",
                    textbookDerived = true,
                    sourceReference = "P69"
                )
            )
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_p69_keys",
                    sourceUnitId = "english_pep_2013_g4_s1_u4",
                    promptText = "Where is the key? (钥匙在哪里？)",
                    promptTranslation = "真实课本P69原题衍生",
                    questionType = "EXPRESSION_CHOICE",
                    options = listOf(
                        "It's near the phone.",
                        "They're on the fridge.",
                        "Yes, it is.",
                        "No, they aren't."
                    ),
                    correctIndex = 0,
                    explanation = "Where is the key? 单数提问，用 It's near the phone. 回答。",
                    textbookDerived = true,
                    sourceReference = "P69"
                )
            )
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_p69_keys_plural",
                    sourceUnitId = "english_pep_2013_g4_s1_u4",
                    promptText = "Where are the keys? (钥匙（复数）在哪里？)",
                    promptTranslation = "真实课本P69原题衍生",
                    questionType = "EXPRESSION_CHOICE",
                    options = listOf(
                        "They're on the fridge.",
                        "It's near the phone.",
                        "Yes, they are.",
                        "No, it isn't."
                    ),
                    correctIndex = 0,
                    explanation = "Where are the keys? 复数提问，用 They're... 回答。",
                    textbookDerived = true,
                    sourceReference = "P69"
                )
            )
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_p69_dinner",
                    sourceUnitId = "english_pep_2013_g4_s1_u5",
                    promptText = "What would you like for dinner? (晚饭你想吃什么？)",
                    promptTranslation = "真实课本P69原题衍生",
                    questionType = "EXPRESSION_CHOICE",
                    options = listOf(
                        "I'd like some soup and bread, please.",
                        "Help yourself.",
                        "I'd like some beef, please.",
                        "Yes, please."
                    ),
                    correctIndex = 0,
                    explanation = "dinner 提问晚饭想吃什么，回答一些食物。",
                    textbookDerived = true,
                    sourceReference = "P69"
                )
            )
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_p69_family_count",
                    sourceUnitId = "english_pep_2013_g4_s1_u6",
                    promptText = "How many people are there in your family? (你家有几口人？)",
                    promptTranslation = "真实课本P69原题衍生",
                    questionType = "EXPRESSION_CHOICE",
                    options = listOf(
                        "Three. My parents and me.",
                        "He's a doctor.",
                        "No, they aren't.",
                        "They're in the study."
                    ),
                    correctIndex = 0,
                    explanation = "How many people 提问家庭人数，用数字和成员来回答。",
                    textbookDerived = true,
                    sourceReference = "P69"
                )
            )
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_p69_father_job",
                    sourceUnitId = "english_pep_2013_g4_s1_u6",
                    promptText = "What is your father's job? (你爸爸的职业是什么？)",
                    promptTranslation = "真实课本P69原题衍生",
                    questionType = "EXPRESSION_CHOICE",
                    options = listOf(
                        "He's a doctor.",
                        "She's a nurse.",
                        "My father and me.",
                        "Yes, he is."
                    ),
                    correctIndex = 0,
                    explanation = "father 职业提问，回答 He's a doctor.",
                    textbookDerived = true,
                    sourceReference = "P69"
                )
            )

            // 2. Generated Cumulative Practice Questions (U1-U3 and U4-U6)
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_practice_classroom",
                    sourceUnitId = "english_pep_2013_g4_s1_u1",
                    promptText = "Let's clean the classroom. (我们打扫教室吧。)",
                    promptTranslation = "APP全册累计扩展",
                    questionType = "EXPRESSION_CHOICE",
                    options = listOf(
                        "OK. Let me clean the teacher's desk.",
                        "It is near the door.",
                        "Thank you.",
                        "Nice to meet you."
                    ),
                    correctIndex = 0,
                    explanation = "建议打扫教室时，可以说 OK 并主动分工打扫某处。",
                    generatedPractice = true
                )
            )
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_practice_friend_traits",
                    sourceUnitId = "english_pep_2013_g4_s1_u3",
                    promptText = "My friend is tall and thin. She has ______.",
                    promptTranslation = "APP全册累计扩展",
                    questionType = "EXPRESSION_CHOICE",
                    options = listOf(
                        "long hair",
                        "a driver",
                        "under the desk",
                        "some beef"
                    ),
                    correctIndex = 0,
                    explanation = "描述朋友的外貌特征用 has long hair (有长发)。",
                    generatedPractice = true
                )
            )
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_practice_study_books",
                    sourceUnitId = "english_pep_2013_g4_s1_u4",
                    promptText = "Where are the books? (书在哪里？)",
                    promptTranslation = "APP全册累计扩展",
                    questionType = "EXPRESSION_CHOICE",
                    options = listOf(
                        "They are in the study.",
                        "It is in the kitchen.",
                        "No, they aren't.",
                        "She's a teacher."
                    ),
                    correctIndex = 0,
                    explanation = "books 是复数，用 They are... 回答，且书房 (study) 最适合放书。",
                    generatedPractice = true
                )
            )
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_practice_knife_fork",
                    sourceUnitId = "english_pep_2013_g4_s1_u5",
                    promptText = "Would you like some beef? (你想吃些牛肉吗？)",
                    promptTranslation = "APP全册累计扩展",
                    questionType = "EXPRESSION_CHOICE",
                    options = listOf(
                        "Yes, please. Pass me the knife and fork.",
                        "No, it isn't.",
                        "He is tall.",
                        "Yes, he is."
                    ),
                    correctIndex = 0,
                    explanation = "吃牛肉通常需要刀叉 (knife and fork)。",
                    generatedPractice = true
                )
            )

            // Phonics Long Vowel Review Questions
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_phonics_ae",
                    sourceUnitId = "phonics_g4_s1",
                    promptText = "下列哪组单词中，元音字母发长音 /eɪ/ (a-e)？",
                    promptTranslation = "APP全册累计扩展",
                    questionType = "LETTER_MATCH",
                    options = listOf(
                        "cake, face, lake",
                        "cat, bag, dad",
                        "me, he, she",
                        "kite, nine, five"
                    ),
                    correctIndex = 0,
                    explanation = "cake, face, lake 符合 a-e 结构，发长元音 /eɪ/。",
                    generatedPractice = true
                )
            )
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_phonics_ie",
                    sourceUnitId = "phonics_g4_s1",
                    promptText = "下列哪组单词中，元音字母发长音 /aɪ/ (i-e)？",
                    promptTranslation = "APP全册累计扩展",
                    questionType = "LETTER_MATCH",
                    options = listOf(
                        "kite, nine, five",
                        "big, pig, six",
                        "home, nose, rose",
                        "cute, use, tube"
                    ),
                    correctIndex = 0,
                    explanation = "kite, nine, five 符合 i-e 结构，发长元音 /aɪ/。",
                    generatedPractice = true
                )
            )
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_phonics_oe",
                    sourceUnitId = "phonics_g4_s1",
                    promptText = "下列哪组单词中，元音字母发长音 /əʊ/ (o-e)？",
                    promptTranslation = "APP全册累计扩展",
                    questionType = "LETTER_MATCH",
                    options = listOf(
                        "home, nose, rose",
                        "dog, box, hot",
                        "red, pen, ten",
                        "duck, run, fun"
                    ),
                    correctIndex = 0,
                    explanation = "home, nose, rose 符合 o-e 结构，发长元音 /əʊ/。",
                    generatedPractice = true
                )
            )
            pool.add(
                ReviewQuestionItem(
                    id = "q_g4_phonics_ue",
                    sourceUnitId = "phonics_g4_s1",
                    promptText = "下列哪组单词中，元音字母发长音 /ju:/ (u-e)？",
                    promptTranslation = "APP全册累计扩展",
                    questionType = "LETTER_MATCH",
                    options = listOf(
                        "cute, use, tube",
                        "duck, run, up",
                        "face, name, make",
                        "like, rice, bite"
                    ),
                    correctIndex = 0,
                    explanation = "cute, use, tube 符合 u-e 结构，发长元音 /ju:/。",
                    generatedPractice = true
                )
            )

            return pool.shuffled(random)
        }

        val isS2 = coveredUnitIds.any { it.contains("_s2") }
        if (isS2) {
            val courseId = "english_pep_2013_g3_s2"
            val u1 = EnglishContentLoader.loadUnit(context, courseId, "english_pep_2013_g3_s2_u1")
            val u2 = EnglishContentLoader.loadUnit(context, courseId, "english_pep_2013_g3_s2_u2")
            val u3 = EnglishContentLoader.loadUnit(context, courseId, "english_pep_2013_g3_s2_u3")
            val u4 = EnglishContentLoader.loadUnit(context, courseId, "english_pep_2013_g3_s2_u4")
            val u5 = EnglishContentLoader.loadUnit(context, courseId, "english_pep_2013_g3_s2_u5")
            val u6 = EnglishContentLoader.loadUnit(context, courseId, "english_pep_2013_g3_s2_u6")

            // Unit 1
            if (u1 != null) {
                pool.add(ReviewQuestionItem("qs2_u1_1", u1.unitId, "Where are you from?", "你来自哪里？", questionType = "EXPRESSION_CHOICE", options = listOf("I'm from China.", "He's a teacher.", "She's a pupil.", "No, she isn't."), correctIndex = 0, explanation = "Where are you from? 提问来自哪个国家，答语通常是 I'm from..."))
                pool.add(ReviewQuestionItem("qs2_u1_2", u1.unitId, "He is a student. 它的汉语意思是？", "He is a student.", questionType = "LISTEN_MEANING", options = listOf("他是一名学生。", "她是一名学生。", "他是一名教师。", "她是一名教师。"), correctIndex = 0, explanation = "He 表示传统意义男性单数“他”，student 表示“学生”。"))
            }
            // Unit 2
            if (u2 != null) {
                pool.add(ReviewQuestionItem("qs2_u2_1", u2.unitId, "Who's that man? — He's my ______.", "那个人是谁？— 他是我...", questionType = "EXPRESSION_CHOICE", options = listOf("father", "mother", "sister", "grandma"), correctIndex = 0, explanation = "man 指男性，对应家庭成员中的 father。"))
                pool.add(ReviewQuestionItem("qs2_u2_2", u2.unitId, "grandmother 的亲切口语称呼是？", "grandmother", questionType = "LISTEN_MEANING", options = listOf("grandma", "grandpa", "dad", "sister"), correctIndex = 0, explanation = "grandmother 的口语和日常称呼是 grandma。"))
            }
            // Unit 3
            if (u3 != null) {
                pool.add(ReviewQuestionItem("qs2_u3_1", u3.unitId, "It has a long nose and a short tail. 描述的是什么动物？", "它有长鼻子和短尾巴。", questionType = "LISTEN_MEANING", options = listOf("elephant (大象)", "giraffe (长颈鹿)", "monkey (猴子)", "pig (猪)"), correctIndex = 0, explanation = "大象 elephant 拥有长长的鼻子 (long nose) 和短尾巴 (short tail)。"))
                pool.add(ReviewQuestionItem("qs2_u3_2", u3.unitId, "Look at the giraffe. Wow! It's so ______!", "看那只长颈鹿。哇！它好...", questionType = "EXPRESSION_CHOICE", options = listOf("tall", "short", "fat", "small"), correctIndex = 0, explanation = "长颈鹿的典型特征是高 (tall)。"))
            }
            // Unit 4
            if (u4 != null) {
                pool.add(ReviewQuestionItem("qs2_u4_1", u4.unitId, "Where is my car? — It's ______ the chair.", "我的小汽车在哪里？— 在椅子...", questionType = "EXPRESSION_CHOICE", options = listOf("under", "cap", "desk", "map"), correctIndex = 0, explanation = "under 是方位介词“在...下面”。chair（椅子）通常和 under 搭配表示在椅子下。"))
                pool.add(ReviewQuestionItem("qs2_u4_2", u4.unitId, "Is it in your bag?", "它在你的包里吗？", questionType = "EXPRESSION_CHOICE", options = listOf("No, it isn't.", "Yes, he is.", "No, she isn't.", "Thank you."), correctIndex = 0, explanation = "Is it...? 提问的否定回答是 No, it isn't.，肯定回答是 Yes, it is."))
            }
            // Unit 5
            if (u5 != null) {
                pool.add(ReviewQuestionItem("qs2_u5_1", u5.unitId, "Do you like pears? 正确的否定回答是？", "你喜欢梨吗？", questionType = "EXPRESSION_CHOICE", options = listOf("No, I don't.", "Yes, I do.", "Yes, he does.", "No, she doesn't."), correctIndex = 0, explanation = "Do you...? 提问的否定回答是 No, I don't."))
                pool.add(ReviewQuestionItem("qs2_u5_2", u5.unitId, "Have some grapes, please. 的汉语翻译是？", "请吃点葡萄吧。", questionType = "LISTEN_MEANING", options = listOf("请吃点葡萄吧。", "请吃些苹果吧。", "请吃些梨吧。", "请吃些香蕉吧。"), correctIndex = 0, explanation = "grapes 意为葡萄，Have some... 表示吃点或喝点。"))
            }
            // Unit 6
            if (u6 != null) {
                pool.add(ReviewQuestionItem("qs2_u6_1", u6.unitId, "How many kites do you see? — I see ______.", "你看到了多少只风筝？— 我看到...", questionType = "EXPRESSION_CHOICE", options = listOf("twelve", "beautiful", "kite", "under"), correctIndex = 0, explanation = "How many 提问数量，回答应用数字词，例如 twelve (12)。"))
                pool.add(ReviewQuestionItem("qs2_u6_2", u6.unitId, "How many crayons do you have? — I have ______.", "你有多少支蜡笔？— 我有...", questionType = "EXPRESSION_CHOICE", options = listOf("twenty", "crayon", "on", "he"), correctIndex = 0, explanation = "How many 提问数量，回答应用数字词，例如 twenty (20)。"))
            }

            // Short Vowels Review
            pool.add(ReviewQuestionItem("qs2_phonics_1", "phonics_s2", "单词 ten, pen 中的元音字母 e 发什么音？", "短元音发音", questionType = "LETTER_MATCH", options = listOf("短元音 /e/", "短元音 /i/", "短元音 /æ/", "短元音 /ɒ/"), correctIndex = 0, explanation = "元音字母 e 在闭音节词 ten, pen 中发短元音 /e/。"))
            pool.add(ReviewQuestionItem("qs2_phonics_2", "phonics_s2", "下列哪个单词中元音字母 i 发短元音 /i/ 且拼写正确？", "短元音发音", questionType = "LETTER_MATCH", options = listOf("six", "ten", "dog", "duck"), correctIndex = 0, explanation = "six 中的元音字母 i 发短元音 /i/。"))
            pool.add(ReviewQuestionItem("qs2_phonics_3", "phonics_s2", "单词 dog, box 中的元音字母 o 发什么音？", "短元音发音", questionType = "LETTER_MATCH", options = listOf("短元音 /ɒ/", "短元音 /u/", "短元音 /e/", "短元音 /i/"), correctIndex = 0, explanation = "dog 和 box 中的元音字母 o 发短元音 /ɒ/。"))
            pool.add(ReviewQuestionItem("qs2_phonics_4", "phonics_s2", "下列哪个单词中的元音字母 u 发短元音 /ʌ/ (如 fun, duck)？", "短元音发音", questionType = "LETTER_MATCH", options = listOf("run", "pig", "red", "cat"), correctIndex = 0, explanation = "run 中的元音字母 u 发短元音 /ʌ/。"))

            return pool.shuffled(random)
        }

        val coversRecycle1 = coveredUnitIds.any { it.endsWith("u1") || it.endsWith("u2") || it.endsWith("u3") }
        val coversRecycle2 = coveredUnitIds.any { it.endsWith("u4") || it.endsWith("u5") || it.endsWith("u6") }

        if (coversRecycle1 || coveredUnitIds.isEmpty()) {
            val u1 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u1")
            val u2 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u2")
            val u3 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u3")

            // 1. Unit 1 Questions (Stationery & Greetings)
            if (u1 != null) {
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u1_1",
                        sourceUnitId = u1.unitId,
                        promptText = "Hello, I'm Mike! What's your name?",
                        promptTranslation = "你好，我是迈克！你叫什么名字？",
                        questionType = "EXPRESSION_CHOICE",
                        options = listOf("My name's Sarah.", "I have an eraser.", "Good afternoon!", "Colour it red."),
                        correctIndex = 0,
                        explanation = "当询问姓名时，回答应表达 My name's..."
                    )
                )
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u1_2",
                        sourceUnitId = u1.unitId,
                        promptText = "I have a pencil box. 它的中文意思是？",
                        promptTranslation = "我有一个...",
                        questionType = "LISTEN_MEANING",
                        options = listOf("铅笔盒", "尺子", "橡皮", "书包"),
                        correctIndex = 0,
                        sourceWordId = "u1_w7",
                        explanation = "pencil box 意为铅笔盒。"
                    )
                )
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u1_3",
                        sourceUnitId = u1.unitId,
                        promptText = "当你想和朋友道别时，应该说：",
                        promptTranslation = "表达再见",
                        questionType = "EXPRESSION_CHOICE",
                        options = listOf("Goodbye! / Bye!", "Hello!", "Good morning!", "Nice to meet you!"),
                        correctIndex = 0,
                        explanation = "Goodbye! / Bye! 用于告别。"
                    )
                )
            }

            // 2. Unit 2 Questions (Colours & Introductions)
            if (u2 != null) {
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u2_1",
                        sourceUnitId = u2.unitId,
                        promptText = "Mr Jones, this is Miss Green. 这句话用于：",
                        promptTranslation = "琼斯先生，这是格林小姐。",
                        questionType = "EXPRESSION_CHOICE",
                        options = listOf("介绍他人认识", "询问对方身体状况", "邀请对方去上学", "告别与说再见"),
                        correctIndex = 0,
                        explanation = "This is... 用于向他人介绍第三者。"
                    )
                )
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u2_2",
                        sourceUnitId = u2.unitId,
                        promptText = "I see red and yellow. 你看到了哪两种颜色？",
                        promptTranslation = "我看见...",
                        questionType = "COLOR_IDENTIFY",
                        options = listOf("红色和黄色", "蓝色和绿色", "黑色和白色", "棕色和橙色"),
                        correctIndex = 0,
                        sourceWordId = "u2_w1",
                        explanation = "red 为红色，yellow 为黄色。"
                    )
                )
            }

            // 3. Unit 3 Questions (Body Parts & Actions)
            if (u3 != null) {
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u3_1",
                        sourceUnitId = u3.unitId,
                        promptText = "How are you? 正确而有礼貌的回答是：",
                        promptTranslation = "你好吗？",
                        questionType = "EXPRESSION_CHOICE",
                        options = listOf("I'm fine, thank you.", "My name's John.", "Good morning!", "Nice to meet you."),
                        correctIndex = 0,
                        explanation = "How are you? 应回答 I'm fine, thank you."
                    )
                )
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u3_2",
                        sourceUnitId = u3.unitId,
                        promptText = "Touch your nose and ear. 请指出对应身体部位：",
                        promptTranslation = "摸摸你的...",
                        questionType = "BODY_ACTION",
                        options = listOf("鼻子和耳朵", "眼睛和嘴巴", "脸庞和手臂", "头部和手掌"),
                        correctIndex = 0,
                        sourceWordId = "u3_w4",
                        explanation = "nose 是鼻子，ear 是耳朵。"
                    )
                )
            }

            // Letter Review Questions (A-N)
            val letters1 = listOf("Aa", "Bb", "Cc", "Dd", "Ee", "Ff", "Gg", "Hh", "Ii", "Jj", "Kk", "Ll", "Mm", "Nn")
            letters1.shuffled(random).take(2).forEachIndexed { idx, letter ->
                val first = letter.first().toString()
                pool.add(
                    ReviewQuestionItem(
                        id = "q_letter_r1_$idx",
                        sourceUnitId = "letters_u1_u3",
                        promptText = "字母 '$first' 的正确大写/小写配对是：",
                        promptTranslation = "字母匹配",
                        questionType = "LETTER_MATCH",
                        options = listOf(letter, "${first}${first.lowercase()}", "${first.lowercase()}${first.lowercase()}", "AZ"),
                        correctIndex = 0,
                        explanation = "$letter 为标准大小写配对。"
                    )
                )
            }
        }

        if (coversRecycle2 || coveredUnitIds.isEmpty()) {
            val u4 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u4")
            val u5 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u5")
            val u6 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u6")

            // Unit 4 Questions (Animals & What's this/that)
            if (u4 != null) {
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u4_1",
                        sourceUnitId = u4.unitId,
                        promptText = "What's this? — It's a duck. 表达的汉语意思是：",
                        promptTranslation = "近处问答",
                        questionType = "EXPRESSION_CHOICE",
                        options = listOf("这是什么？— 它是一只鸭子。", "那是什么？— 它是一只小狗。", "你喜欢大象吗？— 喜欢。", "你看那是什么？— 一只老虎。"),
                        correctIndex = 0,
                        explanation = "What's this? 询问近处事物，duck 是鸭子。"
                    )
                )
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u4_2",
                        sourceUnitId = u4.unitId,
                        promptText = "What's that? — It's a bear. 用于询问：",
                        promptTranslation = "远处问答",
                        questionType = "EXPRESSION_CHOICE",
                        options = listOf("远处事物（那是什么？— 是一只熊。）", "近处事物（这是什么？— 是一只猫。）", "年龄数量", "身体部位"),
                        correctIndex = 0,
                        explanation = "What's that? 用于询问较远距离的事物。"
                    )
                )
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u4_3",
                        sourceUnitId = u4.unitId,
                        promptText = "Act like a panda and a tiger! 指令要求模仿什么动物？",
                        promptTranslation = "动物模仿",
                        questionType = "LISTEN_MEANING",
                        options = listOf("熊猫和老虎", "小猪和小狗", "大象和猴子", "小猫和小鸭"),
                        correctIndex = 0,
                        sourceWordId = "u4_w7",
                        explanation = "panda 是熊猫，tiger 是老虎。"
                    )
                )
            }

            // Unit 5 Questions (Food/Drink & Requests)
            if (u5 != null) {
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u5_1",
                        sourceUnitId = u5.unitId,
                        promptText = "I'd like some juice and bread, please. 表达请求：",
                        promptTranslation = "点餐请求",
                        questionType = "EXPRESSION_CHOICE",
                        options = listOf("请给我一些果汁和面包。", "请给我一些牛奶和鸡蛋。", "我想吃米饭和鱼。", "你可以喝水吗？"),
                        correctIndex = 0,
                        sourceWordId = "u5_w2",
                        explanation = "juice 是果汁，bread 是面包。"
                    )
                )
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u5_2",
                        sourceUnitId = u5.unitId,
                        promptText = "Can I have some water, please? — Here you are. 的意思是：",
                        promptTranslation = "礼貌请求与应答",
                        questionType = "EXPRESSION_CHOICE",
                        options = listOf("我可以喝些水吗？— 给你。", "我想吃蛋糕！— 谢谢。", "你要吃米饭吗？— 不用。", "这是你的鱼吗？— 是的。"),
                        correctIndex = 0,
                        explanation = "Can I have some...? 表达礼貌请求，Here you are 意为给你。"
                    )
                )
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u5_3",
                        sourceUnitId = u5.unitId,
                        promptText = "Drink the milk and eat the cake! 对应的动作是：",
                        promptTranslation = "动作命令",
                        questionType = "LISTEN_MEANING",
                        options = listOf("喝牛奶，吃蛋糕", "切蛋糕，吃米饭", "喝水，吃面包", "吃鱼，喝果汁"),
                        correctIndex = 0,
                        sourceWordId = "u5_w4",
                        explanation = "Drink 意为喝，eat 意为吃。"
                    )
                )
            }

            // Unit 6 Questions (Numbers & Age & Birthday)
            if (u6 != null) {
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u6_1",
                        sourceUnitId = u6.unitId,
                        promptText = "How many plates? — Five. 表达的问答是：",
                        promptTranslation = "数量问答",
                        questionType = "EXPRESSION_CHOICE",
                        options = listOf("多少个盘子？— 5个。", "你几岁了？— 5岁。", "有多少只小狗？— 10只。", "祝你生日快乐！— 谢谢。"),
                        correctIndex = 0,
                        sourceWordId = "u6_w12",
                        explanation = "How many 用于询问数量，five 表示5，plate 是盘子。"
                    )
                )
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u6_2",
                        sourceUnitId = u6.unitId,
                        promptText = "How old are you? — I'm six years old. 表达的问答是：",
                        promptTranslation = "年龄问答",
                        questionType = "EXPRESSION_CHOICE",
                        options = listOf("你几岁了？— 我6岁了。", "你有多少个盘子？— 6个。", "你是哪个班的？— 6班。", "这是什么？— 一只小狗。"),
                        correctIndex = 0,
                        explanation = "How old are you? 询问年龄。"
                    )
                )
                pool.add(
                    ReviewQuestionItem(
                        id = "q_u6_3",
                        sourceUnitId = u6.unitId,
                        promptText = "Happy birthday! — Thank you. 表达的意思是：",
                        promptTranslation = "生日祝福与感谢",
                        questionType = "EXPRESSION_CHOICE",
                        options = listOf("生日快乐！— 谢谢你。", "早上好！— 早上好。", "很高兴见到你！— 我也是。", "再见！— 再见！"),
                        correctIndex = 0,
                        explanation = "Happy birthday! 用于表达生日祝福。"
                    )
                )
            }

            // Letter Review Questions (Oo-Zz)
            val letters2 = listOf("Oo", "Pp", "Qq", "Rr", "Ss", "Tt", "Uu", "Vv", "Ww", "Xx", "Yy", "Zz")
            letters2.shuffled(random).take(2).forEachIndexed { idx, letter ->
                val first = letter.first().toString()
                pool.add(
                    ReviewQuestionItem(
                        id = "q_letter_r2_$idx",
                        sourceUnitId = "letters_u4_u6",
                        promptText = "字母 '$first' 的正确大写/小写配对是：",
                        promptTranslation = "字母匹配",
                        questionType = "LETTER_MATCH",
                        options = listOf(letter, "${first}${first.lowercase()}", "${first.lowercase()}${first.lowercase()}", "OZ"),
                        correctIndex = 0,
                        explanation = "$letter 为标准大小写配对。"
                    )
                )
            }
        }

        return pool.shuffled(random)
    }
}
