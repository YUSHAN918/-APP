package com.example.data.english

import android.content.Context
import android.util.Log

object EnglishContentValidator {

    fun validateUnit(context: Context, unit: EnglishUnit): Boolean {
        if (unit.unitId == "english_pep_2013_g4_s2_u1") {
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf("teachers' office", "library", "playground", "computer room", "art room", "music room", "first floor", "second floor")
            val actualCore = coreWords.map { it.spelling.lowercase() }.toSet()
            if (actualCore != expectedCore) {
                Log.e("EnglishContentValidator", "G4 S2 Unit 1 core words do not match PEP 2013 standard: $actualCore")
                return false
            }
            val extendedWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
            val expectedExtended = setOf("next to", "homework", "class", "forty", "way")
            val actualExtended = extendedWords.map { it.spelling.lowercase() }.toSet()
            if (actualExtended != expectedExtended) {
                Log.e("EnglishContentValidator", "G4 S2 Unit 1 extended words do not match standard: $actualExtended")
                return false
            }
            val phonicsWords = unit.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
            val expectedPhonics = setOf("sister", "computer", "teacher", "dinner", "ruler", "water", "tiger")
            val actualPhonics = phonicsWords.map { it.spelling.lowercase() }.toSet()
            if (actualPhonics != expectedPhonics) {
                Log.e("EnglishContentValidator", "G4 S2 Unit 1 phonics words do not match standard: $actualPhonics")
                return false
            }
            if (unit.expressions.size < 8) {
                Log.e("EnglishContentValidator", "G4 S2 Unit 1 expressions count must be at least 8, found ${unit.expressions.size}")
                return false
            }
            return true
        }

        if (unit.unitId == "english_pep_2013_g4_s2_u2") {
            if (unit.title != "Unit 2: What time is it?") {
                Log.e("EnglishContentValidator", "G4 S2 Unit 2 title must be Unit 2: What time is it?")
                return false
            }
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf(
                "breakfast", "English class", "lunch", "music class", "PE class", "dinner",
                "get up", "go to school", "go home", "go to bed"
            )
            val actualCore = coreWords.map { it.spelling }.toSet()
            if (actualCore != expectedCore) {
                Log.e("EnglishContentValidator", "G4 S2 Unit 2 core words do not match standard: $actualCore")
                return false
            }
            // Strict case check for multi-word entries
            val englishClassWord = coreWords.find { it.spelling.equals("English class", ignoreCase = true) }
            if (englishClassWord == null || englishClassWord.spelling != "English class") {
                Log.e("EnglishContentValidator", "English class capitalization is invalid")
                return false
            }
            val peClassWord = coreWords.find { it.spelling.equals("PE class", ignoreCase = true) }
            if (peClassWord == null || peClassWord.spelling != "PE class") {
                Log.e("EnglishContentValidator", "PE class capitalization is invalid")
                return false
            }

            val extendedWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
            val expectedExtended = setOf(
                "over", "now", "o'clock", "kid", "thirty", "hurry up", "come on", "just a minute"
            )
            val actualExtended = extendedWords.map { it.spelling }.toSet()
            if (actualExtended != expectedExtended) {
                Log.e("EnglishContentValidator", "G4 S2 Unit 2 extended words do not match standard: $actualExtended")
                return false
            }

            // check o'clock standard apostrophe spelling
            val oClockWord = extendedWords.find { it.spelling.lowercase() == "o'clock" }
            if (oClockWord == null || oClockWord.spelling != "o'clock") {
                Log.e("EnglishContentValidator", "o'clock apostrophe spelling is invalid")
                return false
            }

            val phonicsWords = unit.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
            val expectedPhonics = setOf("girl", "bird", "nurse", "hamburger")
            val actualPhonics = phonicsWords.map { it.spelling.lowercase() }.toSet()
            if (actualPhonics != expectedPhonics) {
                Log.e("EnglishContentValidator", "G4 S2 Unit 2 phonics words do not match standard: $actualPhonics")
                return false
            }

            if (unit.expressions.size < 10) {
                Log.e("EnglishContentValidator", "G4 S2 Unit 2 expressions count must be at least 10, found ${unit.expressions.size}")
                return false
            }
            return true
        }

        if (unit.unitId == "english_pep_2013_g4_s2_u3") {
            if (unit.title != "Unit 3: Weather") {
                Log.e("EnglishContentValidator", "G4 S2 Unit 3 title must be Unit 3: Weather")
                return false
            }
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf("cold", "cool", "warm", "hot", "sunny", "windy", "cloudy", "snowy", "rainy")
            val actualCore = coreWords.map { it.spelling }.toSet()
            if (actualCore != expectedCore) {
                Log.e("EnglishContentValidator", "G4 S2 Unit 3 core words do not match PEP 2013 standard: $actualCore")
                return false
            }
            val extendedWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
            val expectedExtended = setOf(
                "outside", "be careful", "weather", "New York", "how about", "degree", "world",
                "London", "Moscow", "Singapore", "Sydney", "fly", "love"
            )
            val actualExtended = extendedWords.map { it.spelling }.toSet()
            if (actualExtended != expectedExtended) {
                Log.e("EnglishContentValidator", "G4 S2 Unit 3 extended words do not match standard: $actualExtended")
                return false
            }
            val phonicsWords = unit.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
            val expectedPhonics = setOf("arm", "car", "card", "ball", "tall", "wall")
            val actualPhonics = phonicsWords.map { it.spelling.lowercase() }.toSet()
            if (actualPhonics != expectedPhonics) {
                Log.e("EnglishContentValidator", "G4 S2 Unit 3 phonics words do not match standard: $actualPhonics")
                return false
            }
            if (unit.expressions.size < 12) {
                Log.e("EnglishContentValidator", "G4 S2 Unit 3 expressions count must be at least 12, found ${unit.expressions.size}")
                return false
            }
            return true
        }

        if (unit.unitId == "english_pep_2013_g4_s1_u1") {
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf("classroom", "window", "blackboard", "light", "picture", "door", "teacher's desk", "computer", "fan", "wall", "floor")
            val actualCore = coreWords.map { it.spelling.lowercase() }.toSet()
            if (actualCore != expectedCore) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 1 core words do not match PEP 2013 standard: $actualCore")
                return false
            }
            val phonicsWords = unit.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
            val expectedPhonics = setOf("cake", "face", "name", "make")
            val actualPhonics = phonicsWords.map { it.spelling.lowercase() }.toSet()
            if (actualPhonics != expectedPhonics) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 1 phonics example words do not match standard: $actualPhonics")
                return false
            }
            if (unit.expressions.size < 5) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 1 expressions count must be at least 5, found ${unit.expressions.size}")
                return false
            }
            // Ensure no stationery words from Grade 3 Unit 1
            val stationeryWords = setOf("ruler", "pencil", "eraser", "crayon")
            if (actualCore.any { it in stationeryWords }) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 1 contains Grade 3 stationery words!")
                return false
            }
            return true
        }

        if (unit.unitId == "english_pep_2013_g4_s1_u2") {
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf("schoolbag", "maths book", "english book", "chinese book", "storybook", "candy", "notebook", "toy", "key")
            val actualCore = coreWords.map { it.spelling.lowercase() }.toSet()
            if (actualCore != expectedCore) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 2 core words do not match standard: $actualCore")
                return false
            }
            val extendedWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
            val expectedExtended = setOf("wow", "lost", "so much", "cute")
            val actualExtended = extendedWords.map { it.spelling.lowercase() }.toSet()
            if (actualExtended != expectedExtended) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 2 extended words do not match standard: $actualExtended")
                return false
            }
            val phonicsWords = unit.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
            val expectedPhonics = setOf("like", "kite", "five", "nine", "rice")
            val actualPhonics = phonicsWords.map { it.spelling.lowercase() }.toSet()
            if (actualPhonics != expectedPhonics) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 2 phonics words do not match standard: $actualPhonics")
                return false
            }
            if (unit.expressions.size < 5) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 2 expressions count must be at least 5, found ${unit.expressions.size}")
                return false
            }
            return true
        }

        if (unit.unitId == "english_pep_2013_g4_s1_u3") {
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf("strong", "friendly", "quiet", "hair", "shoe", "glasses")
            val actualCore = coreWords.map { it.spelling.lowercase() }.toSet()
            if (actualCore != expectedCore) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 3 core words do not match standard: $actualCore")
                return false
            }
            val extendedWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
            val expectedExtended = setOf("his", "or", "right", "hat", "her")
            val actualExtended = extendedWords.map { it.spelling.lowercase() }.toSet()
            if (actualExtended != expectedExtended) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 3 extended words do not match standard: $actualExtended")
                return false
            }
            val phonicsWords = unit.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
            val expectedPhonics = setOf("nose", "note", "coke", "mr jones")
            val actualPhonics = phonicsWords.map { it.spelling.lowercase() }.toSet()
            if (actualPhonics != expectedPhonics) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 3 phonics words do not match standard: $actualPhonics")
                return false
            }
            if (unit.expressions.size < 10) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 3 expressions count must be at least 10, found ${unit.expressions.size}")
                return false
            }
            return true
        }

        if (unit.unitId == "english_pep_2013_g4_s1_u4") {
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf("bedroom", "living room", "study", "kitchen", "bathroom", "bed", "phone", "table", "sofa", "fridge")
            val actualCore = coreWords.map { it.spelling.lowercase() }.toSet()
            if (actualCore != expectedCore) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 4 core words do not match standard: $actualCore")
                return false
            }
            val extendedWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
            val expectedExtended = setOf("find", "them")
            val actualExtended = extendedWords.map { it.spelling.lowercase() }.toSet()
            if (actualExtended != expectedExtended) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 4 extended words do not match standard: $actualExtended")
                return false
            }
            val phonicsWords = unit.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
            val expectedPhonics = setOf("use", "cute", "excuse")
            val actualPhonics = phonicsWords.map { it.spelling.lowercase() }.toSet()
            if (actualPhonics != expectedPhonics) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 4 phonics words do not match standard: $actualPhonics")
                return false
            }
            val hasLivingRoom = unit.words.any { it.spelling.lowercase() == "living room" }
            if (!hasLivingRoom) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 4 living room must be present as a single entry")
                return false
            }
            if (unit.expressions.size < 8) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 4 expressions count must be at least 8, found ${unit.expressions.size}")
                return false
            }
            return true
        }

        if (unit.unitId == "english_pep_2013_g4_s1_u5") {
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf("beef", "chicken", "noodles", "soup", "vegetable", "chopsticks", "bowl", "fork", "knife", "spoon")
            val actualCore = coreWords.map { it.spelling.lowercase() }.toSet()
            if (actualCore != expectedCore) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 5 core words do not match standard: $actualCore")
                return false
            }
            val extendedWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
            val expectedExtended = setOf("dinner", "ready", "help yourself", "pass", "try")
            val actualExtended = extendedWords.map { it.spelling.lowercase() }.toSet()
            if (actualExtended != expectedExtended) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 5 extended words do not match standard: $actualExtended")
                return false
            }
            val phonicsWords = unit.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
            val expectedPhonics = setOf("me", "he", "she", "we")
            val actualPhonics = phonicsWords.map { it.spelling.lowercase() }.toSet()
            if (actualPhonics != expectedPhonics) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 5 phonics words do not match standard: $actualPhonics")
                return false
            }
            val hasHelpYourself = unit.words.any { it.spelling.lowercase() == "help yourself" }
            if (!hasHelpYourself) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 5 help yourself must be present as a single PHRASE entry")
                return false
            }
            if (unit.expressions.size < 10) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 5 expressions count must be at least 10, found ${unit.expressions.size}")
                return false
            }
            return true
        }

        if (unit.unitId == "english_pep_2013_g4_s1_u6") {
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf("parents", "cousin", "uncle", "aunt", "baby brother", "doctor", "cook", "driver", "farmer", "nurse")
            val actualCore = coreWords.map { it.spelling.lowercase() }.toSet()
            if (actualCore != expectedCore) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 6 core words do not match standard: $actualCore")
                return false
            }
            val extendedWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
            val expectedExtended = setOf("people", "but", "little", "puppy", "football player", "job", "basketball")
            val actualExtended = extendedWords.map { it.spelling.lowercase() }.toSet()
            if (actualExtended != expectedExtended) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 6 extended words do not match standard: $actualExtended")
                return false
            }
            val phonicsWords = unit.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
            val expectedPhonics = setOf("me", "he", "she", "we", "face", "rice", "nose", "use", "bag", "leg", "six", "dog", "mum")
            val actualPhonics = phonicsWords.map { it.spelling.lowercase() }.toSet()
            if (actualPhonics != expectedPhonics) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 6 phonics words do not match standard: $actualPhonics")
                return false
            }
            val hasBabyBrother = unit.words.any { it.spelling.lowercase() == "baby brother" }
            if (!hasBabyBrother) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 6 baby brother must be present")
                return false
            }
            if (unit.expressions.size < 10) {
                Log.e("EnglishContentValidator", "G4 S1 Unit 6 expressions count must be at least 10, found ${unit.expressions.size}")
                return false
            }
            return true
        }

        if (unit.unitId == "english_pep_2013_g3_s2_u1") {
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf("UK", "Canada", "USA", "China", "she", "student", "pupil", "he", "teacher")
            val actualCore = coreWords.map { it.spelling }.toSet()
            if (actualCore.map { it.lowercase() }.toSet() != expectedCore.map { it.lowercase() }.toSet()) {
                Log.e("EnglishContentValidator", "S2 Unit 1 core words do not match PEP 2013 standard: $actualCore")
                return false
            }
            val listenOnlyWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
            val expectedListenOnly = setOf("boy", "and", "girl", "new", "friend", "today")
            val actualListenOnly = listenOnlyWords.map { it.spelling.lowercase() }.toSet()
            if (actualListenOnly != expectedListenOnly) {
                Log.e("EnglishContentValidator", "S2 Unit 1 listen/speak only words do not match PEP 2013 standard: $actualListenOnly")
                return false
            }
            if (unit.expressions.size < 8) {
                Log.e("EnglishContentValidator", "S2 Unit 1 expressions count must be at least 8, found ${unit.expressions.size}")
                return false
            }
            return true
        }

        if (unit.unitId == "english_pep_2013_g3_s2_u2") {
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf("father", "man", "woman", "mother", "sister", "brother", "grandmother", "grandma", "grandfather", "grandpa")
            val actualCore = coreWords.map { it.spelling.lowercase() }.toSet()
            if (actualCore != expectedCore.map { it.lowercase() }.toSet()) {
                Log.e("EnglishContentValidator", "S2 Unit 2 core words do not match standard: $actualCore")
                return false
            }
            val listenOnlyWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
            val expectedListenOnly = setOf("dad", "family")
            val actualListenOnly = listenOnlyWords.map { it.spelling.lowercase() }.toSet()
            if (actualListenOnly != expectedListenOnly) {
                Log.e("EnglishContentValidator", "S2 Unit 2 listen/speak only words do not match standard: $actualListenOnly")
                return false
            }
            val phonicsWords = unit.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
            val expectedPhonics = setOf("ten", "pen", "leg", "red")
            val actualPhonics = phonicsWords.map { it.spelling.lowercase() }.toSet()
            if (actualPhonics != expectedPhonics) {
                Log.e("EnglishContentValidator", "S2 Unit 2 phonics example words do not match standard: $actualPhonics")
                return false
            }
            if (unit.expressions.size < 8) {
                Log.e("EnglishContentValidator", "S2 Unit 2 expressions count must be at least 8, found ${unit.expressions.size}")
                return false
            }
            return true
        }

        if (unit.unitId == "english_pep_2013_g3_s2_u3") {
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf("thin", "fat", "tall", "short", "long", "small", "big")
            val actualCore = coreWords.map { it.spelling.lowercase() }.toSet()
            if (actualCore != expectedCore) {
                Log.e("EnglishContentValidator", "S2 Unit 3 core words do not match standard: $actualCore")
                return false
            }
            val listenOnlyWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
            val expectedListenOnly = setOf("giraffe", "so", "children", "tail")
            val actualListenOnly = listenOnlyWords.map { it.spelling.lowercase() }.toSet()
            if (actualListenOnly != expectedListenOnly) {
                Log.e("EnglishContentValidator", "S2 Unit 3 listen/speak only words do not match standard: $actualListenOnly")
                return false
            }
            val phonicsWords = unit.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
            val expectedPhonics = setOf("pig", "six", "milk")
            val actualPhonics = phonicsWords.map { it.spelling.lowercase() }.toSet()
            if (actualPhonics != expectedPhonics) {
                Log.e("EnglishContentValidator", "S2 Unit 3 phonics example words do not match standard: $actualPhonics")
                return false
            }
            if (unit.expressions.size < 8) {
                Log.e("EnglishContentValidator", "S2 Unit 3 expressions count must be at least 8, found ${unit.expressions.size}")
                return false
            }
            return true
        }

        if (unit.unitId == "english_pep_2013_g3_s2_u4") {
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf("on", "in", "under", "chair", "desk", "cap", "ball", "car", "boat", "map")
            val actualCore = coreWords.map { it.spelling.lowercase() }.toSet()
            if (actualCore != expectedCore) {
                Log.e("EnglishContentValidator", "S2 Unit 4 core words do not match standard: $actualCore")
                return false
            }
            val phonicsWords = unit.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
            val expectedPhonics = setOf("dog", "box", "orange", "body")
            val actualPhonics = phonicsWords.map { it.spelling.lowercase() }.toSet()
            if (actualPhonics != expectedPhonics) {
                Log.e("EnglishContentValidator", "S2 Unit 4 phonics example words do not match standard: $actualPhonics")
                return false
            }
            if (unit.expressions.size < 8) {
                Log.e("EnglishContentValidator", "S2 Unit 4 expressions count must be at least 8, found ${unit.expressions.size}")
                return false
            }
            return true
        }

        if (unit.unitId == "english_pep_2013_g3_s2_u5") {
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCore = setOf("pear", "apple", "orange", "banana", "watermelon", "strawberry", "grape")
            val actualCore = coreWords.map { it.spelling.lowercase() }.toSet()
            if (actualCore != expectedCore) {
                Log.e("EnglishContentValidator", "S2 Unit 5 core words do not match standard: $actualCore")
                return false
            }
            val listenOnlyWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
            val expectedListenOnly = setOf("buy", "fruit")
            val actualListenOnly = listenOnlyWords.map { it.spelling.lowercase() }.toSet()
            if (actualListenOnly != expectedListenOnly) {
                Log.e("EnglishContentValidator", "S2 Unit 5 listen/speak only words do not match standard: $actualListenOnly")
                return false
            }
            val phonicsWords = unit.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
            val expectedPhonics = setOf("fun", "run", "duck", "under")
            val actualPhonics = phonicsWords.map { it.spelling.lowercase() }.toSet()
            if (actualPhonics != expectedPhonics) {
                Log.e("EnglishContentValidator", "S2 Unit 5 phonics example words do not match standard: $actualPhonics")
                return false
            }
            if (unit.expressions.size < 8) {
                Log.e("EnglishContentValidator", "S2 Unit 5 expressions count must be at least 8, found ${unit.expressions.size}")
                return false
            }
            return true
        }

        if (unit.unitId != "english_pep_2013_g3_s1_u1" && unit.unitId != "english_pep_2013_g3_s1_u2" && unit.unitId != "english_pep_2013_g3_s1_u3" && unit.unitId != "english_pep_2013_g3_s1_u4" && unit.unitId != "english_pep_2013_g3_s1_u5" && unit.unitId != "english_pep_2013_g3_s1_u6") {
            return true
        }

        try {
            // 1. Check core A-class words (LISTEN_SPEAK_RECOGNIZE) must be exactly 8 (except Unit 6 which has 12)
            val coreWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
            val expectedCoreCount = if (unit.unitId == "english_pep_2013_g3_s1_u6") 12 else 8
            if (coreWords.size != expectedCoreCount) {
                Log.e("EnglishContentValidator", "${unit.unitId} core words count must be exactly $expectedCoreCount, but was ${coreWords.size}")
                return false
            }

            // 2. Check B-class words (LISTEN_SPEAK_ONLY) must be exactly 2 (or 0 for Unit 5 and Unit 6)
            val listenSpeakWords = unit.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
            val expectedListenSpeakSize = if (unit.unitId == "english_pep_2013_g3_s1_u5" || unit.unitId == "english_pep_2013_g3_s1_u6") 0 else 2
            if (listenSpeakWords.size != expectedListenSpeakSize) {
                Log.e("EnglishContentValidator", "${unit.unitId} listen/speak only words count must be $expectedListenSpeakSize, but was ${listenSpeakWords.size}")
                return false
            }

            // 3. Duplicate check
            val spellingSet = mutableSetOf<String>()
            for (word in unit.words) {
                if (spellingSet.contains(word.spelling.lowercase())) {
                    Log.e("EnglishContentValidator", "Duplicate word found: ${word.spelling}")
                    return false
                }
                spellingSet.add(word.spelling.lowercase())
            }

            if (unit.unitId == "english_pep_2013_g3_s1_u1") {
                // "pencil box" as a single word entry check
                val hasPencilBox = unit.words.any { it.spelling.lowercase() == "pencil box" }
                if (!hasPencilBox) {
                    Log.e("EnglishContentValidator", "pencil box must be present as a single undivided entry")
                    return false
                }
            }

            if (unit.unitId == "english_pep_2013_g3_s1_u2") {
                // Must not contain purple or pink as core words
                val invalidWords = listOf("purple", "pink")
                if (coreWords.any { invalidWords.contains(it.spelling.lowercase()) }) {
                    Log.e("EnglishContentValidator", "Unit 2 must not contain purple or pink as core words")
                    return false
                }
                // Check 8 core colours
                val expectedColours = setOf("red", "yellow", "green", "blue", "black", "white", "orange", "brown")
                val actualColours = coreWords.map { it.spelling.lowercase() }.toSet()
                if (expectedColours != actualColours) {
                    Log.e("EnglishContentValidator", "Unit 2 core words do not match PEP 2013 standards: $actualColours")
                    return false
                }
            }

            if (unit.unitId == "english_pep_2013_g3_s1_u3") {
                val expectedParts = setOf("face", "ear", "eye", "nose", "mouth", "head", "hand", "arm")
                val actualParts = coreWords.map { it.spelling.lowercase() }.toSet()
                if (expectedParts != actualParts) {
                    Log.e("EnglishContentValidator", "Unit 3 core words do not match PEP 2013 standards: $actualParts")
                    return false
                }
            }

            if (unit.unitId == "english_pep_2013_g3_s1_u4") {
                val expectedParts = setOf("cat", "duck", "dog", "pig", "bear", "bird", "panda", "tiger")
                val actualParts = coreWords.map { it.spelling.lowercase() }.toSet()
                if (expectedParts != actualParts) {
                    Log.e("EnglishContentValidator", "Unit 4 core words do not match PEP 2013 standards: $actualParts")
                    return false
                }
                
                val expectedListenOnly = setOf("elephant", "monkey")
                val actualListenOnly = listenSpeakWords.map { it.spelling.lowercase() }.toSet()
                if (expectedListenOnly != actualListenOnly) {
                    Log.e("EnglishContentValidator", "Unit 4 LISTEN_SPEAK_ONLY words must be elephant and monkey: $actualListenOnly")
                    return false
                }
            }

            if (unit.unitId == "english_pep_2013_g3_s1_u5") {
                val expectedFoods = setOf("bread", "juice", "egg", "milk", "fish", "rice", "water", "cake")
                val actualFoods = coreWords.map { it.spelling.lowercase() }.toSet()
                if (expectedFoods != actualFoods) {
                    Log.e("EnglishContentValidator", "Unit 5 core words do not match PEP 2013 standards: $actualFoods")
                    return false
                }
            }

            if (unit.unitId == "english_pep_2013_g3_s1_u6") {
                val expectedNumbers = setOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten")
                val actualNumbers = coreWords.filter { expectedNumbers.contains(it.spelling.lowercase()) }.map { it.spelling.lowercase() }.toSet()
                if (expectedNumbers != actualNumbers) {
                    Log.e("EnglishContentValidator", "Unit 6 core number words must be exactly one to ten: $actualNumbers")
                    return false
                }
                val otherCoreWords = coreWords.filter { !expectedNumbers.contains(it.spelling.lowercase()) }.map { it.spelling.lowercase() }.toSet()
                val expectedOther = setOf("brother", "plate")
                if (expectedOther != otherCoreWords) {
                    Log.e("EnglishContentValidator", "Unit 6 other core words must be exactly brother and plate: $otherCoreWords")
                    return false
                }

                // Forbidden high numbers
                val forbiddenWords = listOf("eleven", "twelve", "thirteen", "fourteen", "fifteen", "twenty", "candle", "gift", "birthday cake")
                if (unit.words.any { forbiddenWords.contains(it.spelling.lowercase()) }) {
                    Log.e("EnglishContentValidator", "Unit 6 must not contain eleven or higher, or forbidden words like candle/gift/birthday cake")
                    return false
                }
            }

            if (unit.unitId == "english_pep_2013_g3_s2_u6") {
                val expectedCore = setOf("eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty")
                val actualCore = coreWords.map { it.spelling.lowercase() }.toSet()
                if (expectedCore != actualCore) {
                    Log.e("EnglishContentValidator", "S2 Unit 6 core number words must be eleven to twenty: $actualCore")
                    return false
                }
                val expectedListenOnly = setOf("kite", "beautiful")
                val actualListenOnly = listenSpeakWords.map { it.spelling.lowercase() }.toSet()
                if (expectedListenOnly != actualListenOnly) {
                    Log.e("EnglishContentValidator", "S2 Unit 6 LISTEN_SPEAK_ONLY words must be kite and beautiful: $actualListenOnly")
                    return false
                }
            }

            // 5. Each word must have chinese meaning, requirement level, sourceReference, textbookPage
            for (word in unit.words) {
                if (word.chineseMeaning.isEmpty()) {
                    Log.e("EnglishContentValidator", "Word ${word.spelling} is missing chineseMeaning")
                    return false
                }
                if (word.requirementLevel.isEmpty()) {
                    Log.e("EnglishContentValidator", "Word ${word.spelling} is missing requirementLevel")
                    return false
                }
                if (word.sourceReference.isEmpty()) {
                    Log.e("EnglishContentValidator", "Word ${word.spelling} is missing sourceReference")
                    return false
                }
                if (word.textbookPage.isEmpty()) {
                    Log.e("EnglishContentValidator", "Word ${word.spelling} is missing textbookPage")
                    return false
                }
                
                if (word.audioSource == "OFFICIAL_LICENSED") {
                    Log.e("EnglishContentValidator", "OFFICIAL_LICENSED audio is forbidden without license verification for ${word.spelling}")
                    return false
                }
            }

            // 9. Expressions must have source anchor
            if (unit.expressions.size < 8) {
                Log.e("EnglishContentValidator", "${unit.unitId} core expressions count must be at least 8, but was ${unit.expressions.size}")
                return false
            }

            for (exp in unit.expressions) {
                if (exp.sourceReference.isEmpty()) {
                    Log.e("EnglishContentValidator", "Expression ${exp.englishText} is missing sourceReference")
                    return false
                }
                if (exp.textbookPage.isEmpty()) {
                    Log.e("EnglishContentValidator", "Expression ${exp.englishText} is missing textbookPage")
                    return false
                }
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun validateRecycle(context: Context, recycle: EnglishRecycleContent): Boolean {
        try {
            if (recycle.recycleId != "english_pep_2013_g3_s1_recycle_1" && 
                recycle.recycleId != "english_pep_2013_g3_s1_recycle_2" &&
                recycle.recycleId != "english_pep_2013_g3_s2_recycle_1" &&
                recycle.recycleId != "english_pep_2013_g3_s2_recycle_2" &&
                recycle.recycleId != "english_pep_2013_g4_s1_recycle_1" &&
                recycle.recycleId != "english_pep_2013_g4_s1_recycle_2" &&
                recycle.recycleId != "english_pep_2013_g4_s2_recycle_1") {
                Log.e("EnglishContentValidator", "Invalid recycleId: ${recycle.recycleId}")
                return false
            }

            if (recycle.recycleId == "english_pep_2013_g4_s2_recycle_1") {
                val expectedUnits = listOf("english_pep_2013_g4_s2_u1", "english_pep_2013_g4_s2_u2", "english_pep_2013_g4_s2_u3")
                if (recycle.coveredUnitIds != expectedUnits) {
                    Log.e("EnglishContentValidator", "G4 S2 Recycle 1 must cover exactly Unit 1-3, but covered: ${recycle.coveredUnitIds}")
                    return false
                }
                if (!recycle.textbookPages.contains("P32") || !recycle.textbookPages.contains("P35")) {
                    Log.e("EnglishContentValidator", "G4 S2 Recycle 1 textbookPages must cover P32-P35")
                    return false
                }
            } else if (recycle.recycleId == "english_pep_2013_g3_s1_recycle_1") {
                val expectedUnits = listOf("english_pep_2013_g3_s1_u1", "english_pep_2013_g3_s1_u2", "english_pep_2013_g3_s1_u3")
                if (recycle.coveredUnitIds != expectedUnits) {
                    Log.e("EnglishContentValidator", "Recycle 1 must cover exactly Unit 1-3, but covered: ${recycle.coveredUnitIds}")
                    return false
                }
                if (!recycle.textbookPages.contains("P32") || !recycle.textbookPages.contains("P35")) {
                    Log.e("EnglishContentValidator", "Recycle 1 textbookPages must cover P32-P35")
                    return false
                }
            } else if (recycle.recycleId == "english_pep_2013_g4_s1_recycle_1") {
                val expectedUnits = listOf("english_pep_2013_g4_s1_u1", "english_pep_2013_g4_s1_u2", "english_pep_2013_g4_s1_u3")
                if (recycle.coveredUnitIds != expectedUnits) {
                    Log.e("EnglishContentValidator", "G4 S1 Recycle 1 must cover exactly Unit 1-3, but covered: ${recycle.coveredUnitIds}")
                    return false
                }
                if (!recycle.textbookPages.contains("P32") || !recycle.textbookPages.contains("P35")) {
                    Log.e("EnglishContentValidator", "G4 S1 Recycle 1 textbookPages must cover P32-P35")
                    return false
                }
            } else if (recycle.recycleId == "english_pep_2013_g4_s1_recycle_2") {
                val expectedUnits = listOf(
                    "english_pep_2013_g4_s1_u1",
                    "english_pep_2013_g4_s1_u2",
                    "english_pep_2013_g4_s1_u3",
                    "english_pep_2013_g4_s1_u4",
                    "english_pep_2013_g4_s1_u5",
                    "english_pep_2013_g4_s1_u6"
                )
                if (recycle.coveredUnitIds != expectedUnits) {
                    Log.e("EnglishContentValidator", "G4 S1 Recycle 2 must cover exactly Unit 1-6, but covered: ${recycle.coveredUnitIds}")
                    return false
                }
                if (!recycle.textbookPages.contains("P66") || !recycle.textbookPages.contains("P69")) {
                    Log.e("EnglishContentValidator", "G4 S1 Recycle 2 textbookPages must cover P66-P69")
                    return false
                }
            } else if (recycle.recycleId == "english_pep_2013_g3_s1_recycle_2") {
                val expectedUnits = listOf("english_pep_2013_g3_s1_u4", "english_pep_2013_g3_s1_u5", "english_pep_2013_g3_s1_u6")
                if (recycle.coveredUnitIds != expectedUnits) {
                    Log.e("EnglishContentValidator", "Recycle 2 must cover exactly Unit 4-6, but covered: ${recycle.coveredUnitIds}")
                    return false
                }
                if (!recycle.textbookPages.contains("P66") || !recycle.textbookPages.contains("P69")) {
                    Log.e("EnglishContentValidator", "Recycle 2 textbookPages must cover P66-P69")
                    return false
                }
            } else if (recycle.recycleId == "english_pep_2013_g3_s2_recycle_1") {
                val expectedUnits = listOf("english_pep_2013_g3_s2_u1", "english_pep_2013_g3_s2_u2", "english_pep_2013_g3_s2_u3")
                if (recycle.coveredUnitIds != expectedUnits) {
                    Log.e("EnglishContentValidator", "S2 Recycle 1 must cover exactly Unit 1-3, but covered: ${recycle.coveredUnitIds}")
                    return false
                }
                if (!recycle.textbookPages.contains("P32") || !recycle.textbookPages.contains("P35")) {
                    Log.e("EnglishContentValidator", "S2 Recycle 1 textbookPages must cover P32-P35")
                    return false
                }
            } else if (recycle.recycleId == "english_pep_2013_g3_s2_recycle_2") {
                val expectedUnits = listOf(
                    "english_pep_2013_g3_s2_u1",
                    "english_pep_2013_g3_s2_u2",
                    "english_pep_2013_g3_s2_u3",
                    "english_pep_2013_g3_s2_u4",
                    "english_pep_2013_g3_s2_u5",
                    "english_pep_2013_g3_s2_u6"
                )
                if (recycle.coveredUnitIds != expectedUnits) {
                    Log.e("EnglishContentValidator", "S2 Recycle 2 must cover exactly Unit 1-6, but covered: ${recycle.coveredUnitIds}")
                    return false
                }
                if (!recycle.textbookPages.contains("P66") || !recycle.textbookPages.contains("P69")) {
                    Log.e("EnglishContentValidator", "S2 Recycle 2 textbookPages must cover P66-P69")
                    return false
                }
            }

            // Check unique missionIds and mission count
            val missionIds = recycle.missions.map { it.missionId }
            if (missionIds.distinct().size != recycle.missions.size) {
                Log.e("EnglishContentValidator", "Duplicate missionId found in ${recycle.recycleId}")
                return false
            }
            if (recycle.missions.size != 5) {
                Log.e("EnglishContentValidator", "${recycle.recycleId} must have exactly 5 missions, found: ${recycle.missions.size}")
                return false
            }

            // Check mission types presence
            val types = recycle.missions.map { it.missionType }.toSet()
            if (!types.contains("STORY_REHEARSAL") && !types.contains("STORY_ROLEPLAY")) {
                Log.e("EnglishContentValidator", "Missing STORY_REHEARSAL or STORY_ROLEPLAY mission in ${recycle.recycleId}")
                return false
            }

            if (recycle.recycleId == "english_pep_2013_g3_s1_recycle_1") {
                if (!types.contains("BOARD_GAME")) {
                    Log.e("EnglishContentValidator", "Missing BOARD_GAME mission in ${recycle.recycleId}")
                    return false
                }
                if (!types.contains("LISTEN_AND_COLOUR") || !types.contains("MATCH_WRITE_READ") || !types.contains("SONG_REVIEW_CHECKPOINT")) {
                    Log.e("EnglishContentValidator", "Missing required missions in Recycle 1")
                    return false
                }
            } else if (recycle.recycleId == "english_pep_2013_g4_s1_recycle_1") {
                if (!types.contains("PHONICS_CONNECT_WRITE") || !types.contains("FRIEND_CLUE_SEARCH") ||
                    !types.contains("DIALOGUE_PICTURE_MATCH") || !types.contains("QUESTION_TRAIL_GAME")) {
                    Log.e("EnglishContentValidator", "Missing required missions in G4 S1 Recycle 1")
                    return false
                }
            } else if (recycle.recycleId == "english_pep_2013_g4_s2_recycle_1") {
                if (!types.contains("STORY_ROLEPLAY") || !types.contains("PHONICS_LISTEN_MATCH") ||
                    !types.contains("SCHOOL_WEATHER_QA") || !types.contains("WEATHER_LISTEN_READ_WRITE") ||
                    !types.contains("DAY_TIMELINE_READ_CIRCLE")) {
                    Log.e("EnglishContentValidator", "Missing required missions in G4 S2 Recycle 1")
                    return false
                }
            } else if (recycle.recycleId == "english_pep_2013_g4_s1_recycle_2") {
                if (!types.contains("LISTEN_NUMBER_ROOMS") || !types.contains("PHONICS_LISTEN_WRITE") ||
                    !types.contains("FAMILY_HOME_INTERVIEW") || !types.contains("SEMESTER_QUESTION_GAME")) {
                    Log.e("EnglishContentValidator", "Missing required missions in G4 S1 Recycle 2")
                    return false
                }
            } else if (recycle.recycleId == "english_pep_2013_g3_s1_recycle_2") {
                if (!types.contains("BOARD_GAME")) {
                    Log.e("EnglishContentValidator", "Missing BOARD_GAME mission in ${recycle.recycleId}")
                    return false
                }
                if (!types.contains("CATEGORY_ODD_ONE_OUT") || !types.contains("SENTENCE_REPAIR") || !types.contains("SONG_SEMESTER_SUMMARY")) {
                    Log.e("EnglishContentValidator", "Missing required missions in Recycle 2")
                    return false
                }
            } else if (recycle.recycleId == "english_pep_2013_g3_s2_recycle_1") {
                if (!types.contains("CHANT_AND_MATCH") || !types.contains("FAMILY_OBSERVATION") || !types.contains("FIND_AND_CIRCLE") || !types.contains("MIXED_CHECKPOINT")) {
                    Log.e("EnglishContentValidator", "Missing required missions in S2 Recycle 1")
                    return false
                }
            } else if (recycle.recycleId == "english_pep_2013_g3_s2_recycle_2") {
                if (!types.contains("STORY_REHEARSAL") || !types.contains("LISTEN_DRAW_SPATIAL") || !types.contains("BOARD_GAME") || !types.contains("WORD_REPAIR_PHONICS") || !types.contains("MIXED_CHECKPOINT")) {
                    Log.e("EnglishContentValidator", "Missing required missions in S2 Recycle 2")
                    return false
                }
            }

            // Verify covered units load successfully
            val courseId = when {
                recycle.recycleId.contains("g4_s2") -> "english_pep_2013_g4_s2"
                recycle.recycleId.contains("g4_s1") -> "english_pep_2013_g4_s1"
                recycle.recycleId.contains("_s2") -> "english_pep_2013_g3_s2"
                else -> "english_pep_2013_g3_s1"
            }
            for (uId in recycle.coveredUnitIds) {
                val unit = EnglishContentLoader.loadUnit(context, courseId, uId)
                if (unit == null) {
                    Log.e("EnglishContentValidator", "Recycle referenced unit $uId failed to load")
                    return false
                }
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
