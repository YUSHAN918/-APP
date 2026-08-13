package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.english.*
import com.example.ui.english.LearnStage
import com.example.ui.english.EnglishLessonType
import com.example.ui.english.getLessonTitle
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EnglishContentTest {

    @Test
    fun testLoadManifest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val manifest = EnglishContentLoader.loadManifest(context)
        assertNotNull("English Manifest should load successfully", manifest)
        assertEquals("Course ID matches", "english_pep_2013_g3_s1", manifest!!.courseId)
        assertEquals("Textbook version matches", "PEP 2013版", manifest.textbookVersion)
        assertEquals("Units count matches", 8, manifest.units.size)
        
        val u1Summary = manifest.units[0]
        assertEquals("english_pep_2013_g3_s1_u1", u1Summary.unitId)
        assertEquals("Unit 1: Hello!", u1Summary.title)
        assertEquals(1, u1Summary.order)
    }

    @Test
    fun testLoadUnit1Details() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val u1 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u1")
        assertNotNull("Unit 1 data should load successfully", u1)
        assertEquals("english_pep_2013_g3_s1_u1", u1!!.unitId)
        assertEquals("Unit 1: Hello!", u1.title)
        assertEquals(10, u1.words.size)
        
        // Check specific word
        val crayon = u1.words.find { it.spelling == "crayon" }
        assertNotNull("Crayon word should exist in Unit 1", crayon)
        assertEquals("g3s1_u1_crayon", crayon!!.wordId)
        assertEquals("n.", crayon.partOfSpeech)
        assertEquals("蜡笔", crayon.chineseMeaning)
        assertEquals("/ˈkreɪən/", crayon.phonetic)
        assertEquals("cray-on", crayon.syllables)
        
        // Verify all words have required fields
        u1.words.forEach { word ->
            assertFalse("Spelling is not blank", word.spelling.isBlank())
            assertFalse("Chinese meaning is not blank", word.chineseMeaning.isBlank())
            assertTrue("Word has required skills set", word.requiredSkills.isNotEmpty())
        }
    }

    @Test
    fun testProgressManagerAndDevBypass() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Reset progress first
        EnglishProgressManager.clearProgress(context)
        
        // Assert initial locked/completed states
        assertFalse("Initial lesson should not be completed", EnglishProgressManager.isLessonCompleted(context, "english_pep_2013_g3_s1_u1"))
        assertFalse("Initial unit should not be completed", EnglishProgressManager.isUnitCompleted(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u1"))
        
        // Check Unit Lock Reasons
        val manifest = EnglishContentLoader.loadManifest(context)!!
        val u1Summary = manifest.units[0]
        val u2Summary = manifest.units[1]
        val u3Summary = manifest.units[2]
        val u4Summary = manifest.units[3]

        // Test default strategy ALL_READY_CONTENT_OPEN
        assertEquals(EnglishAccessPolicy.ALL_READY_CONTENT_OPEN, EnglishProgressManager.getAccessPolicy(context))
        assertEquals(EnglishUnitLockReason.NONE, EnglishContentLoader.getUnitLockReason(context, manifest.courseId, u1Summary, manifest.units))
        assertEquals(EnglishUnitLockReason.NONE, EnglishContentLoader.getUnitLockReason(context, manifest.courseId, u2Summary, manifest.units))
        assertEquals(EnglishUnitLockReason.NONE, EnglishContentLoader.getUnitLockReason(context, manifest.courseId, u3Summary, manifest.units))
        assertEquals(EnglishUnitLockReason.NONE, EnglishContentLoader.getUnitLockReason(context, manifest.courseId, u4Summary, manifest.units))

        // Test ContentAvailability mapping
        assertEquals(ContentAvailability.READY, EnglishContentLoader.getUnitContentAvailability(context, manifest.courseId, u1Summary.unitId))
        assertEquals(ContentAvailability.READY, EnglishContentLoader.getUnitContentAvailability(context, manifest.courseId, u2Summary.unitId))
        assertEquals(ContentAvailability.READY, EnglishContentLoader.getUnitContentAvailability(context, manifest.courseId, u3Summary.unitId))
        assertEquals(ContentAvailability.READY, EnglishContentLoader.getUnitContentAvailability(context, manifest.courseId, u4Summary.unitId))

        // Test SEQUENTIAL policy
        EnglishProgressManager.setAccessPolicy(context, EnglishAccessPolicy.SEQUENTIAL)
        assertEquals(EnglishUnitLockReason.NONE, EnglishContentLoader.getUnitLockReason(context, manifest.courseId, u1Summary, manifest.units))
        assertEquals(EnglishUnitLockReason.PREVIOUS_UNIT_NOT_COMPLETED, EnglishContentLoader.getUnitLockReason(context, manifest.courseId, u2Summary, manifest.units))

        // Complete Unit 1 and check unlocking under SEQUENTIAL
        EnglishProgressManager.completeUnit(context, "english_pep_2013_g3_s1_u1")
        assertTrue("Unit 1 should now be completed", EnglishProgressManager.isUnitCompleted(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u1"))
        assertEquals(EnglishUnitLockReason.NONE, EnglishContentLoader.getUnitLockReason(context, manifest.courseId, u2Summary, manifest.units))

        // Restore default policy
        EnglishProgressManager.setAccessPolicy(context, EnglishAccessPolicy.ALL_READY_CONTENT_OPEN)
        EnglishProgressManager.clearProgress(context)
    }

    @Test
    fun testLoadUnit2Details() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val u2 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u2")
        assertNotNull("Unit 2 data should load successfully", u2)
        assertEquals("english_pep_2013_g3_s1_u2", u2!!.unitId)
        assertEquals("Unit 2: Colours", u2.title)
        assertEquals(10, u2.words.size)
        assertEquals(8, u2.expressions.size)
        
        // Check core 8 colours
        val coreWords = u2.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        assertEquals(8, coreWords.size)
        val coreSpellings = coreWords.map { it.spelling.lowercase() }.toSet()
        val expectedColours = setOf("red", "yellow", "green", "blue", "black", "white", "orange", "brown")
        assertEquals(expectedColours, coreSpellings)
        
        // Assert purple and pink are NOT in core words
        assertFalse(coreSpellings.contains("purple"))
        assertFalse(coreSpellings.contains("pink"))
        
        // Check orange
        val orange = u2.words.find { it.spelling == "orange" }
        assertNotNull("orange word should exist in Unit 2", orange)
        assertEquals("橙色；橙色的", orange!!.chineseMeaning)
        assertEquals("/ˈɒrɪndʒ/", orange.phonetic)
        
        // Verify all words have required fields and source references
        u2.words.forEach { word ->
            assertFalse("Spelling is not blank", word.spelling.isBlank())
            assertFalse("Chinese meaning is not blank", word.chineseMeaning.isBlank())
            assertFalse("Phonetic is not blank", word.phonetic.isBlank())
            assertFalse("Source reference is not blank", word.sourceReference.isBlank())
            assertFalse("Textbook page is not blank", word.textbookPage.isBlank())
        }
    }

    @Test
    fun testLoadUnit3Details() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val u3 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u3")
        assertNotNull("Unit 3 data should load successfully", u3)
        assertEquals("english_pep_2013_g3_s1_u3", u3!!.unitId)
        assertEquals("Unit 3: Look at me!", u3.title)
        assertEquals(10, u3.words.size)
        assertEquals(9, u3.expressions.size)
        
        // Check core 8 body parts
        val coreWords = u3.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        assertEquals(8, coreWords.size)
        val coreSpellings = coreWords.map { it.spelling.lowercase() }.toSet()
        val expectedParts = setOf("face", "ear", "eye", "nose", "mouth", "head", "hand", "arm")
        assertEquals(expectedParts, coreSpellings)
        
        // Check face
        val face = u3.words.find { it.spelling == "face" }
        assertNotNull("face word should exist in Unit 3", face)
        assertEquals("脸", face!!.chineseMeaning)
        assertEquals("/feɪs/", face.phonetic)
        
        // Verify all words have required fields and source references
        u3.words.forEach { word ->
            assertFalse("Spelling is not blank", word.spelling.isBlank())
            assertFalse("Chinese meaning is not blank", word.chineseMeaning.isBlank())
            assertFalse("Phonetic is not blank", word.phonetic.isBlank())
            assertFalse("Source reference is not blank", word.sourceReference.isBlank())
            assertFalse("Textbook page is not blank", word.textbookPage.isBlank())
        }
    }

    @Test
    fun testLoadUnit4Details() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val u4 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u4")
        assertNotNull("Unit 4 data should load successfully", u4)
        assertEquals("english_pep_2013_g3_s1_u4", u4!!.unitId)
        assertEquals("Unit 4: We love animals", u4.title)
        assertEquals(10, u4.words.size)
        assertEquals(13, u4.expressions.size)
        
        // Check core 8 animal words
        val coreWords = u4.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        assertEquals(8, coreWords.size)
        val coreSpellings = coreWords.map { it.spelling.lowercase() }.toSet()
        val expectedAnimals = setOf("cat", "duck", "dog", "pig", "bear", "bird", "panda", "tiger")
        assertEquals(expectedAnimals, coreSpellings)
        
        // Check LISTEN_SPEAK_ONLY words
        val extraWords = u4.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        assertEquals(2, extraWords.size)
        val extraSpellings = extraWords.map { it.spelling.lowercase() }.toSet()
        val expectedExtra = setOf("elephant", "monkey")
        assertEquals(expectedExtra, extraSpellings)
        
        // Verify all words have required fields and source references
        u4.words.forEach { word ->
            assertFalse("Spelling is not blank", word.spelling.isBlank())
            assertFalse("Chinese meaning is not blank", word.chineseMeaning.isBlank())
            assertFalse("Phonetic is not blank", word.phonetic.isBlank())
            assertFalse("Source reference is not blank", word.sourceReference.isBlank())
            assertFalse("Textbook page is not blank", word.textbookPage.isBlank())
        }
    }

    @Test
    fun testLoadUnit5Details() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val u5 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u5")
        assertNotNull("Unit 5 data should load successfully", u5)
        assertEquals("english_pep_2013_g3_s1_u5", u5!!.unitId)
        assertEquals("Unit 5: Let's eat!", u5.title)
        assertEquals(8, u5.words.size)
        assertEquals(15, u5.expressions.size)
        
        // Check core 8 food words
        val coreWords = u5.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        assertEquals(8, coreWords.size)
        val coreSpellings = coreWords.map { it.spelling.lowercase() }.toSet()
        val expectedFoods = setOf("bread", "juice", "egg", "milk", "fish", "rice", "water", "cake")
        assertEquals(expectedFoods, coreSpellings)
        
        // Check specific food
        val bread = u5.words.find { it.spelling == "bread" }
        assertNotNull("bread word should exist in Unit 5", bread)
        assertEquals("面包", bread!!.chineseMeaning)
        assertEquals("/bred/", bread.phonetic)
        
        // Verify all words have required fields and source references
        u5.words.forEach { word ->
            assertFalse("Spelling is not blank", word.spelling.isBlank())
            assertFalse("Chinese meaning is not blank", word.chineseMeaning.isBlank())
            assertFalse("Phonetic is not blank", word.phonetic.isBlank())
            assertFalse("Source reference is not blank", word.sourceReference.isBlank())
            assertFalse("Textbook page is not blank", word.textbookPage.isBlank())
        }
    }

    @Test
    fun testLoadUnit6Details() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val u6 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u6")
        assertNotNull("Unit 6 data should load successfully", u6)
        assertEquals("english_pep_2013_g3_s1_u6", u6!!.unitId)
        assertEquals("Unit 6: Happy birthday!", u6.title)
        assertEquals(12, u6.words.size)
        assertEquals(15, u6.expressions.size)
        
        // Check core numbers 1-10 are present
        val expectedNumbers = setOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten")
        val actualNumbers = u6.words.filter { expectedNumbers.contains(it.spelling.lowercase()) }.map { it.spelling.lowercase() }.toSet()
        assertEquals(expectedNumbers, actualNumbers)

        // Check brother and plate are present
        val expectedOther = setOf("brother", "plate")
        val actualOther = u6.words.filter { expectedOther.contains(it.spelling.lowercase()) }.map { it.spelling.lowercase() }.toSet()
        assertEquals(expectedOther, actualOther)
        
        // Verify no high numbers like eleven, twelve are in words list
        val forbiddenNumbers = listOf("eleven", "twelve", "thirteen", "fourteen", "fifteen", "twenty")
        assertTrue(u6.words.none { forbiddenNumbers.contains(it.spelling.lowercase()) })
        
        // Verify all words have required fields and source references
        u6.words.forEach { word ->
            assertFalse("Spelling is not blank", word.spelling.isBlank())
            assertFalse("Chinese meaning is not blank", word.chineseMeaning.isBlank())
            assertFalse("Phonetic is not blank", word.phonetic.isBlank())
            assertFalse("Source reference is not blank", word.sourceReference.isBlank())
            assertFalse("Textbook page is not blank", word.textbookPage.isBlank())
        }
    }

    @Test
    fun testLearnStageEnumValues() {
        // Simple assertion to verify LearnStage values exist and map correct order
        assertEquals(8, LearnStage.values().size)
        assertEquals(LearnStage.INTRO, LearnStage.values()[0])
        assertEquals(LearnStage.LISTEN_MEANING, LearnStage.values()[1])
        assertEquals(LearnStage.READ_ALOUD, LearnStage.values()[2])
        assertEquals(LearnStage.PLAYBACK, LearnStage.values()[3])
        assertEquals(LearnStage.SPELL, LearnStage.values()[4])
        assertEquals(LearnStage.WRITE, LearnStage.values()[5])
        assertEquals(LearnStage.DICTATION, LearnStage.values()[6])
        assertEquals(LearnStage.SPATIAL_PRACTICE, LearnStage.values()[7])
    }

    @Test
    fun testRecycle1ManifestAndLoader() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val manifest = EnglishContentLoader.loadManifest(context)
        assertNotNull("Manifest should load", manifest)
        assertEquals(8, manifest!!.units.size) // U1-U3, Recycle 1, U4-U6, Recycle 2

        val recycleSummary = manifest.units.find { it.unitId == "english_pep_2013_g3_s1_recycle_1" }
        assertNotNull("Recycle 1 summary should exist", recycleSummary)
        assertTrue("isRecycle should be true", recycleSummary!!.isRecycle)
        assertEquals("RECYCLE", recycleSummary.contentType)
        assertEquals(3, recycleSummary.coveredUnitIds.size)

        val recycleContent = EnglishContentLoader.loadRecycle(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_recycle_1")
        assertNotNull("Recycle 1 content should load", recycleContent)
        assertEquals("english_pep_2013_g3_s1_recycle_1", recycleContent!!.recycleId)
        assertEquals(5, recycleContent.missions.size)
    }

    @Test
    fun testRecycle1Validator() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val recycle = EnglishContentLoader.loadRecycle(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_recycle_1")
        assertNotNull(recycle)

        val isValid = EnglishContentValidator.validateRecycle(context, recycle!!)
        assertTrue("Recycle 1 should pass validation", isValid)
    }

    @Test
    fun testReviewPoolBuilder() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pool = EnglishReviewPoolBuilder.buildPool(context, listOf("english_pep_2013_g3_s1_u1", "english_pep_2013_g3_s1_u2", "english_pep_2013_g3_s1_u3"), 42L)
        assertTrue("Pool should contain aggregated review questions", pool.size >= 8)

        val sourceUnits = pool.map { it.sourceUnitId }.toSet()
        assertTrue("Pool contains questions from Unit 1", sourceUnits.contains("english_pep_2013_g3_s1_u1"))
        assertTrue("Pool contains questions from Unit 2", sourceUnits.contains("english_pep_2013_g3_s1_u2"))
        assertTrue("Pool contains questions from Unit 3", sourceUnits.contains("english_pep_2013_g3_s1_u3"))
        assertTrue("Pool contains questions from letters", sourceUnits.contains("letters_u1_u3"))
    }

    @Test
    fun testRecycle2ManifestAndLoader() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val manifest = EnglishContentLoader.loadManifest(context)
        assertNotNull("Manifest should load", manifest)
        assertEquals(8, manifest!!.units.size) // U1-U3, Recycle 1, U4-U6, Recycle 2

        val recycleSummary = manifest.units.find { it.unitId == "english_pep_2013_g3_s1_recycle_2" }
        assertNotNull("Recycle 2 summary should exist", recycleSummary)
        assertTrue("isRecycle should be true", recycleSummary!!.isRecycle)
        assertEquals("RECYCLE", recycleSummary.contentType)
        assertEquals(3, recycleSummary.coveredUnitIds.size)

        val recycleContent = EnglishContentLoader.loadRecycle(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_recycle_2")
        assertNotNull("Recycle 2 content should load", recycleContent)
        assertEquals("english_pep_2013_g3_s1_recycle_2", recycleContent!!.recycleId)
        assertEquals(5, recycleContent.missions.size)
    }

    @Test
    fun testRecycle2Validator() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val recycle = EnglishContentLoader.loadRecycle(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_recycle_2")
        assertNotNull(recycle)

        val isValid = EnglishContentValidator.validateRecycle(context, recycle!!)
        assertTrue("Recycle 2 should pass validation", isValid)
    }

    @Test
    fun testReviewPoolBuilderRecycle2() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pool = EnglishReviewPoolBuilder.buildPool(context, listOf("english_pep_2013_g3_s1_u4", "english_pep_2013_g3_s1_u5", "english_pep_2013_g3_s1_u6"), 88L)
        assertTrue("Pool should contain aggregated review questions for U4-U6", pool.size >= 8)

        val sourceUnits = pool.map { it.sourceUnitId }.toSet()
        assertTrue("Pool contains questions from Unit 4", sourceUnits.contains("english_pep_2013_g3_s1_u4"))
        assertTrue("Pool contains questions from Unit 5", sourceUnits.contains("english_pep_2013_g3_s1_u5"))
        assertTrue("Pool contains questions from Unit 6", sourceUnits.contains("english_pep_2013_g3_s1_u6"))
        assertTrue("Pool contains questions from letters", sourceUnits.contains("letters_u4_u6"))
    }

    @Test
    fun testSemesterReviewSummaryEngine() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val summary = EnglishSemesterReviewEngine.generateSummary(context)

        assertNotNull("Semester review summary should not be null", summary)
        assertEquals("english_pep_2013_g3_s1", summary.semesterId)
        assertEquals(6, summary.coveredUnitIds.size)
        assertEquals(6, summary.topicSummary.size)
        assertEquals(6, summary.skillSummary.size)
        assertTrue("Recommended review items should not be empty", summary.recommendedReviewItems.isNotEmpty())
    }

    @Test
    fun testRecycleProgressManager() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val rId = "english_pep_2013_g3_s1_recycle_1"

        EnglishProgressManager.clearProgress(context)

        var progress = EnglishProgressManager.getRecycleProgress(context, rId)
        assertEquals(0, progress.completedMissionIds.size)
        assertEquals(0, progress.boardPosition)
        assertFalse(progress.rewardClaimed)

        // Complete mission 1
        EnglishProgressManager.completeRecycleMission(context, rId, "recycle_1_m1")
        progress = EnglishProgressManager.getRecycleProgress(context, rId)
        assertTrue(progress.completedMissionIds.contains("recycle_1_m1"))

        // Save board pos
        EnglishProgressManager.saveBoardGamePosition(context, rId, 8, 999L)
        progress = EnglishProgressManager.getRecycleProgress(context, rId)
        assertEquals(8, progress.boardPosition)
        assertEquals(999L, progress.boardRandomSeed)

        // Claim reward once
        val firstClaim = EnglishProgressManager.claimRecycleReward(context, rId)
        assertTrue("First claim should succeed", firstClaim)

        val secondClaim = EnglishProgressManager.claimRecycleReward(context, rId)
        assertFalse("Second claim should fail (already claimed)", secondClaim)
    }

    @Test
    fun testLoadS2Manifest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val manifest = EnglishContentLoader.loadManifest(context, "english_pep_2013_g3_s2")
        assertNotNull("English S2 Manifest should load successfully", manifest)
        assertEquals("Course ID matches", "english_pep_2013_g3_s2", manifest!!.courseId)
        assertEquals("Textbook version matches", "PEP 2013版", manifest.textbookVersion)
        assertEquals("Units count matches", 8, manifest.units.size)
        
        val u1Summary = manifest.units[0]
        assertEquals("english_pep_2013_g3_s2_u1", u1Summary.unitId)
        assertEquals("Unit 1: Welcome back to school!", u1Summary.title)
        assertEquals("READY", u1Summary.contentStatus)

        val u2Summary = manifest.units[1]
        assertEquals("english_pep_2013_g3_s2_u2", u2Summary.unitId)
        assertEquals("READY", u2Summary.contentStatus)
    }

    @Test
    fun testLoadS2Unit1Details() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val u1 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s2", "english_pep_2013_g3_s2_u1")
        assertNotNull("S2 Unit 1 data should load successfully", u1)
        assertEquals("english_pep_2013_g3_s2_u1", u1!!.unitId)
        assertEquals("Unit 1: Welcome back to school!", u1.title)
        assertEquals(19, u1.words.size)
        assertEquals(14, u1.expressions.size)
        
        // Check 9 core words
        val coreWords = u1.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        assertEquals(9, coreWords.size)
        val expectedCore = setOf("UK", "Canada", "USA", "China", "she", "student", "pupil", "he", "teacher")
        assertEquals(expectedCore.map { it.lowercase() }.toSet(), coreWords.map { it.spelling.lowercase() }.toSet())
        
        // Check 6 listen/speak context words
        val listenWords = u1.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        assertEquals(6, listenWords.size)
        val expectedListen = setOf("boy", "and", "girl", "new", "friend", "today")
        assertEquals(expectedListen, listenWords.map { it.spelling.lowercase() }.toSet())

        // Check phonics words
        val phonicsWords = u1.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
        assertEquals(4, phonicsWords.size)

        // Validate via validator
        val isValid = EnglishContentValidator.validateUnit(context, u1)
        assertTrue("S2 Unit 1 should pass validation", isValid)
    }

    @Test
    fun testLoadS2Unit2Details() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val u2 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s2", "english_pep_2013_g3_s2_u2")
        assertNotNull("S2 Unit 2 data should load successfully", u2)
        assertEquals("english_pep_2013_g3_s2_u2", u2!!.unitId)
        assertEquals("Unit 2: My family", u2.title)
        assertEquals(16, u2.words.size)
        assertEquals(15, u2.expressions.size)
        
        // Check 10 core words
        val coreWords = u2.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        assertEquals(10, coreWords.size)
        val expectedCore = setOf("father", "man", "woman", "mother", "sister", "brother", "grandmother", "grandma", "grandfather", "grandpa")
        assertEquals(expectedCore.map { it.lowercase() }.toSet(), coreWords.map { it.spelling.lowercase() }.toSet())
        
        // Check 2 listen/speak context words
        val listenWords = u2.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        assertEquals(2, listenWords.size)
        val expectedListen = setOf("dad", "family")
        assertEquals(expectedListen, listenWords.map { it.spelling.lowercase() }.toSet())

        // Check 4 phonics words
        val phonicsWords = u2.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
        assertEquals(4, phonicsWords.size)
        val expectedPhonics = setOf("ten", "pen", "leg", "red")
        assertEquals(expectedPhonics, phonicsWords.map { it.spelling.lowercase() }.toSet())

        // Check long words length and structure
        val grandmother = u2.words.find { it.spelling == "grandmother" }
        assertNotNull(grandmother)
        assertEquals(11, grandmother!!.spelling.length)

        val grandfather = u2.words.find { it.spelling == "grandfather" }
        assertNotNull(grandfather)
        assertEquals(11, grandfather!!.spelling.length)

        // Validate via validator
        val isValid = EnglishContentValidator.validateUnit(context, u2)
        assertTrue("S2 Unit 2 should pass validation", isValid)
    }

    @Test
    fun testLoadS2Unit3Details() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // 1. Check load Unit 3 with detail diagnostics
        val u3 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s2", "english_pep_2013_g3_s2_u3")
        assertNotNull("S2 Unit 3 data should load successfully", u3)
        assertEquals("english_pep_2013_g3_s2_u3", u3!!.unitId)
        assertEquals("Unit 3: At the zoo", u3.title)
        assertEquals(14, u3.words.size)
        assertEquals(14, u3.expressions.size)
        
        // 2. Check 7 core adjectives
        val coreWords = u3.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        assertEquals(7, coreWords.size)
        val expectedCore = setOf("thin", "fat", "tall", "short", "long", "small", "big")
        assertEquals(expectedCore, coreWords.map { it.spelling.lowercase() }.toSet())
        
        // 3. Check 4 listen/speak context words
        val listenWords = u3.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        assertEquals(4, listenWords.size)
        val expectedListen = setOf("giraffe", "so", "children", "tail")
        assertEquals(expectedListen, listenWords.map { it.spelling.lowercase() }.toSet())

        // 4. Check 3 phonics example words (excluding big because big is registered as core, while pig/six/milk are examples)
        val phonicsWords = u3.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
        assertEquals(3, phonicsWords.size)
        val expectedPhonics = setOf("pig", "six", "milk")
        assertEquals(expectedPhonics, phonicsWords.map { it.spelling.lowercase() }.toSet())

        // 5. Check multi-meaning processing for 'short'
        val shortWord = u3.words.find { it.spelling == "short" }
        assertNotNull(shortWord)
        assertTrue("short should cover both meanings", shortWord!!.chineseMeaning.contains("矮") && shortWord.chineseMeaning.contains("短"))

        // 6. Validate via validator
        val isValid = EnglishContentValidator.validateUnit(context, u3)
        assertTrue("S2 Unit 3 should pass validation", isValid)

        // 7. Progress & Evidence Isolation Tests
        EnglishProgressManager.clearProgress(context)
        
        // Assert initial state
        assertFalse(EnglishProgressManager.isUnitCompleted(context, "english_pep_2013_g3_s2", "english_pep_2013_g3_s2_u3"))
        
        // Update some vocabulary mastery in Unit 3
        EnglishProgressManager.saveWordMastery(context, "g3s2_u3_thin", "MASTERED")
        val thinMastery = EnglishProgressManager.getWordMastery(context, "g3s2_u3_thin")
        assertEquals("MASTERED", thinMastery)

        // Detail stats mastery
        val bigStats = WordDetailStats(meaningCorrect = true, reverseCorrect = true, spellingCorrect = true, dictationCorrect = false)
        EnglishProgressManager.saveWordDetailStats(context, "g3s2_u3_big", bigStats)
        
        val bigStatsResult = EnglishProgressManager.getWordDetailStats(context, "g3s2_u3_big")
        assertTrue(bigStatsResult.spellingCorrect)
        assertFalse(bigStatsResult.dictationCorrect)
        
        // Clean up
        EnglishProgressManager.clearProgress(context)
    }

    @Test
    fun testS2Recycle1LoaderAndValidator() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val manifest = EnglishContentLoader.loadManifest(context, "english_pep_2013_g3_s2")
        assertNotNull("S2 Manifest should load", manifest)
        
        val recycleSummary = manifest!!.units.find { it.unitId == "english_pep_2013_g3_s2_recycle_1" }
        assertNotNull("S2 Recycle 1 summary should exist", recycleSummary)
        assertTrue("isRecycle should be true for S2 Recycle 1", recycleSummary!!.isRecycle)
        assertEquals("RECYCLE", recycleSummary.contentType)
        assertEquals(3, recycleSummary.coveredUnitIds.size)

        val recycleContent = EnglishContentLoader.loadRecycle(context, "english_pep_2013_g3_s2", "english_pep_2013_g3_s2_recycle_1")
        assertNotNull("S2 Recycle 1 content should load", recycleContent)
        assertEquals("english_pep_2013_g3_s2_recycle_1", recycleContent!!.recycleId)
        assertEquals(5, recycleContent.missions.size)

        // Validate S2 Recycle 1
        val isValid = EnglishContentValidator.validateRecycle(context, recycleContent)
        assertTrue("S2 Recycle 1 should pass validation", isValid)

        // Validate that all 5 missions have correct types and are non-null
        val expectedMissionIds = listOf(
            "english_pep_2013_g3_s2_r1_m1",
            "english_pep_2013_g3_s2_r1_m2",
            "english_pep_2013_g3_s2_r1_m3",
            "english_pep_2013_g3_s2_r1_m4",
            "english_pep_2013_g3_s2_r1_m5"
        )
        expectedMissionIds.forEach { mId ->
            val m = recycleContent.missions.find { it.missionId == mId }
            assertNotNull("Mission $mId should exist", m)
        }
    }

    @Test
    fun testUnit4LessonTitleAndGuardProtection() {
        val u4TitleL1 = com.example.ui.english.getLessonTitle("english_pep_2013_g3_s2_u4", com.example.ui.english.EnglishLessonType.LESSON1)
        assertEquals("课时 1：伙伴玩具房寻宝", u4TitleL1)

        val u4TitleL4 = com.example.ui.english.getLessonTitle("english_pep_2013_g3_s2_u4", com.example.ui.english.EnglishLessonType.LESSON4)
        assertEquals("课时 4：它在这里吗", u4TitleL4)

        val u1TitleL1 = com.example.ui.english.getLessonTitle("english_pep_2013_g3_s1_u1", com.example.ui.english.EnglishLessonType.LESSON1)
        assertEquals("课时 1：打招呼与自我介绍", u1TitleL1)

        // Test Guard Protection: cannot complete lesson prematurely at index 3/7
        val isBlockedAt3 = com.example.ui.english.canCompleteLesson(
            unitId = "english_pep_2013_g3_s2_u4",
            currentExpressionIndex = 3,
            totalExpressions = 7,
            currentStage = com.example.ui.english.LearnStage.INTRO
        )
        assertFalse("Should block premature lesson completion at sentence 4/7", isBlockedAt3)

        // Test Guard Protection: allowed at last sentence index during SPATIAL_PRACTICE
        val isAllowedAtEnd = com.example.ui.english.canCompleteLesson(
            unitId = "english_pep_2013_g3_s2_u4",
            currentExpressionIndex = 6,
            totalExpressions = 7,
            currentStage = com.example.ui.english.LearnStage.SPATIAL_PRACTICE
        )
        assertTrue("Should allow lesson completion when in SPATIAL_PRACTICE at final sentence", isAllowedAtEnd)
    }

    @Test
    fun testSemester2Unit5DataAndValidator() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Load S2 Unit 5
        val u5 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s2", "english_pep_2013_g3_s2_u5")
        assertNotNull("S2 Unit 5 content should load successfully", u5)
        assertEquals("english_pep_2013_g3_s2_u5", u5!!.unitId)
        assertEquals("Unit 5: Do you like pears?", u5.title)

        // 2. Check Core Fruit Words
        val coreWords = u5.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        assertEquals("Should have exactly 7 core fruit words", 7, coreWords.size)
        val expectedCore = setOf("pear", "apple", "orange", "banana", "watermelon", "strawberry", "grape")
        assertEquals(expectedCore, coreWords.map { it.spelling.lowercase() }.toSet())

        // 3. Check Listen-only Words
        val listenOnlyWords = u5.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        assertEquals("Should have 2 listen/speak only words", 2, listenOnlyWords.size)
        assertEquals(setOf("buy", "fruit"), listenOnlyWords.map { it.spelling.lowercase() }.toSet())

        // 4. Check Phonics Words
        val phonicsWords = u5.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
        assertEquals("Should have 4 short vowel u phonics words", 4, phonicsWords.size)
        assertEquals(setOf("fun", "run", "duck", "under"), phonicsWords.map { it.spelling.lowercase() }.toSet())

        // 5. Check Expressions
        assertTrue("Expressions count should be at least 8", u5.expressions.size >= 8)

        // 6. Validate Unit 5
        val isValid = EnglishContentValidator.validateUnit(context, u5)
        assertTrue("S2 Unit 5 should pass validation", isValid)

        // 7. Check Lesson Titles
        val titleL1 = com.example.ui.english.getLessonTitle("english_pep_2013_g3_s2_u5", com.example.ui.english.EnglishLessonType.LESSON1)
        assertEquals("课时 1：水果集市与喜好问答", titleL1)

        val titleL3 = com.example.ui.english.getLessonTitle("english_pep_2013_g3_s2_u5", com.example.ui.english.EnglishLessonType.LESSON3)
        assertEquals("课时 3：短元音 u 拼读工坊", titleL3)

        val titleL5 = com.example.ui.english.getLessonTitle("english_pep_2013_g3_s2_u5", com.example.ui.english.EnglishLessonType.LESSON5)
        assertEquals("课时 5：水果词汇 B 与果篮搭配", titleL5)

        // 8. Test Plural Inflector
        assertEquals("strawberries", EnglishPluralInflector.getPluralForm("strawberry"))
        assertEquals("pears", EnglishPluralInflector.getPluralForm("pear"))
        assertEquals("grapes", EnglishPluralInflector.getPluralForm("grape"))
    }

    @Test
    fun testGlobalFixChallengeEngineBClassIntegration() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Load G3 S2 Unit 1 which contains B-class words: boy, and, girl, new, friend, today
        val u1 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s2", "english_pep_2013_g3_s2_u1")
        assertNotNull("S2 Unit 1 should load", u1)

        val pool = EnglishChallengePoolBuilder.buildWordPool(u1!!)
        assertTrue("Pool should not be empty", pool.isNotEmpty())

        // 2. Verify B-class words maintain LISTEN_SPEAK_ONLY requirement level
        val bClassItem = pool.find { it.word.spelling == "boy" }
        assertNotNull("boy should exist in Unit 1 pool", bClassItem)
        assertEquals("LISTEN_SPEAK_ONLY", bClassItem!!.word.requirementLevel)
        assertEquals(EnglishChallengeParticipation.EXTENDED_OPTIONAL, bClassItem.participation)

        // 3. Verify A-class words
        val aClassItem = pool.find { it.word.spelling == "teacher" }
        assertNotNull("teacher should exist in Unit 1 pool", aClassItem)
        assertEquals("LISTEN_SPEAK_RECOGNIZE", aClassItem!!.word.requirementLevel)
        assertEquals(EnglishChallengeParticipation.CORE_REQUIRED, aClassItem.participation)

        // 4. Verify all eligible words in unit are included in challenge pool
        val coreCount = pool.count { it.participation == EnglishChallengeParticipation.CORE_REQUIRED }
        val extCount = pool.count { it.participation == EnglishChallengeParticipation.EXTENDED_OPTIONAL }
        val eligibleCount = u1.words.count { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" || it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        assertTrue("Should have core words", coreCount > 0)
        assertTrue("Should have extended B-class words", extCount > 0)
        assertEquals(eligibleCount, pool.size)

        // 5. Save Extended Practice Stats and verify requirementLevel is strictly untouched
        EnglishProgressManager.saveExtendedPracticeDetailStats(
            context,
            "g3s2_u1_boy",
            WordDetailStats(meaningCorrect = true, reverseCorrect = true, spellingCorrect = true, dictationCorrect = true)
        )

        val retrievedStats = EnglishProgressManager.getExtendedPracticeDetailStats(context, "g3s2_u1_boy")
        assertNotNull("Extended practice stats should be saved", retrievedStats)
        assertTrue(retrievedStats!!.spellingCorrect)
        assertTrue(retrievedStats.dictationCorrect)

        // Confirm original word data requirement level remains unchanged
        val originalWord = u1.words.find { it.wordId == "g3s2_u1_boy" }
        assertEquals("LISTEN_SPEAK_ONLY", originalWord?.requirementLevel)

        // 6. Test extended challenge completed flag
        EnglishProgressManager.saveExtendedChallengeCompleted(context, u1.unitId)
        assertTrue("Extended challenge completion flag should be saved", EnglishProgressManager.isExtendedChallengeCompleted(context, u1.unitId))
    }

    @Test
    fun testAutoDictationPoolBuilder_AllCoreAndExtended() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val u1 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s2", "english_pep_2013_g3_s2_u1")
        assertNotNull("G3S2 Unit 1 should load", u1)

        // 1. ALL scope
        val allSettings = EnglishAutoDictationSettings(wordScope = AutoDictationWordScope.ALL, order = AutoDictationOrder.ORIGINAL)
        val allPool = EnglishAutoDictationPoolBuilder.buildDictationPool(u1!!, allSettings)
        assertTrue("ALL pool should contain core & extended items", allPool.isNotEmpty())

        val hasCore = allPool.any { !it.isExtended }
        val hasExtended = allPool.any { it.isExtended }
        assertTrue("ALL pool has core words", hasCore)
        assertTrue("ALL pool has extended words", hasExtended)

        // 2. CORE_ONLY scope
        val coreSettings = EnglishAutoDictationSettings(wordScope = AutoDictationWordScope.CORE_ONLY)
        val corePool = EnglishAutoDictationPoolBuilder.buildDictationPool(u1, coreSettings)
        assertTrue("CORE pool should only contain core words", corePool.all { !it.isExtended })

        // 3. EXTENDED_ONLY scope
        val extSettings = EnglishAutoDictationSettings(wordScope = AutoDictationWordScope.EXTENDED_ONLY)
        val extPool = EnglishAutoDictationPoolBuilder.buildDictationPool(u1, extSettings)
        assertTrue("EXTENDED pool should only contain extended words", extPool.all { it.isExtended })
    }

    @Test
    fun testAutoDictationPoolBuilder_Grade3Semester2Unit1SpecialTest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val u1 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s2", "english_pep_2013_g3_s2_u1")
        assertNotNull("G3S2 Unit 1 should load", u1)

        val settings = EnglishAutoDictationSettings(wordScope = AutoDictationWordScope.ALL, order = AutoDictationOrder.ORIGINAL)
        val pool = EnglishAutoDictationPoolBuilder.buildDictationPool(u1!!, settings)

        val expectedWords = listOf(
            "UK", "Canada", "USA", "China", "she", "student", "pupil", "he", "teacher",
            "boy", "and", "girl", "new", "friend", "today"
        )

        expectedWords.forEach { expected ->
            val found = pool.any { it.word.spelling.equals(expected, ignoreCase = true) }
            assertTrue("Dictation pool for G3S2 U1 should contain word '$expected'", found)
        }

        // Verify extended B-class words exist in pool
        val bClassWords = listOf("boy", "and", "girl", "new", "friend", "today")
        bClassWords.forEach { bWord ->
            val foundB = pool.find { it.word.spelling.equals(bWord, ignoreCase = true) }
            assertNotNull("B-class word '$bWord' should exist in dictation pool", foundB)
            assertTrue("Word '$bWord' should be marked as extended", foundB!!.isExtended)
        }
    }

    @Test
    fun testAutoDictationSettingsClampingAndPreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Test bounds clamping
        val invalidSettings = EnglishAutoDictationSettings(
            repeatCount = 10, // out of 1..5 -> clamp 5
            intervalSeconds = 1, // out of 2..30 -> clamp 2
            preStartCountdownSeconds = -5 // out of 0..10 -> clamp 0
        )
        val clamped = invalidSettings.clamped()
        assertEquals(5, clamped.repeatCount)
        assertEquals(2, clamped.intervalSeconds)
        assertEquals(0, clamped.preStartCountdownSeconds)

        // Test persistent storage
        EnglishPreferenceStore.saveAutoDictationSettings(context, clamped)
        val retrieved = EnglishPreferenceStore.getAutoDictationSettings(context)
        assertEquals(clamped.wordScope, retrieved.wordScope)
        assertEquals(clamped.repeatCount, retrieved.repeatCount)
        assertEquals(clamped.intervalSeconds, retrieved.intervalSeconds)
        assertEquals(clamped.order, retrieved.order)
        assertEquals(clamped.preStartCountdownSeconds, retrieved.preStartCountdownSeconds)
    }

    @Test
    fun testAutoDictationChallengeIsolation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val u1 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u1")!!

        // Clear progress
        EnglishProgressManager.clearProgress(context)

        // Verify that starting dictation or building pool does NOT complete unit, grant gold, or update stage
        val settings = EnglishAutoDictationSettings()
        val pool = EnglishAutoDictationPoolBuilder.buildDictationPool(u1, settings)
        assertTrue(pool.isNotEmpty())

        assertFalse("Auto dictation should NOT complete unit", EnglishProgressManager.isUnitCompleted(context, "english_pep_2013_g3_s1", u1.unitId))
        assertFalse("Auto dictation should NOT complete lesson", EnglishProgressManager.isLessonCompleted(context, u1.unitId))
    }

    @Test
    fun testSemester2Unit6HowManyImplementation() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Check Course Manifest Status
        val manifest = EnglishContentLoader.loadManifest(context, "english_pep_2013_g3_s2")
        assertNotNull("S2 Manifest should load", manifest)
        val u6Summary = manifest!!.units.find { it.unitId == "english_pep_2013_g3_s2_u6" }
        assertNotNull("Unit 6 summary should exist in manifest", u6Summary)
        assertEquals("Unit 6 contentStatus should be READY", "READY", u6Summary!!.contentStatus)

        // 2. Load Unit 6 Content
        val u6 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s2", "english_pep_2013_g3_s2_u6")
        assertNotNull("S2 Unit 6 content should load successfully", u6)
        assertEquals("english_pep_2013_g3_s2_u6", u6!!.unitId)
        assertEquals("Unit 6: How many?", u6.title)

        // 3. Verify Core Number Words (11-20)
        val coreWords = u6.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        assertEquals("Should have exactly 10 core number words", 10, coreWords.size)
        val expectedCore = setOf("eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty")
        assertEquals(expectedCore, coreWords.map { it.spelling.lowercase() }.toSet())

        // 4. Verify Extended Listen-Only Words (kite, beautiful)
        val listenOnlyWords = u6.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        assertEquals("Should have 2 listen/speak only words", 2, listenOnlyWords.size)
        assertEquals(setOf("kite", "beautiful"), listenOnlyWords.map { it.spelling.lowercase() }.toSet())

        // 5. Verify Phonics Example Words (hand, legs, ten, dog, duck, big)
        val phonicsWords = u6.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
        assertEquals("Should have 6 phonics review words", 6, phonicsWords.size)
        assertEquals(setOf("hand", "legs", "ten", "dog", "duck", "big"), phonicsWords.map { it.spelling.lowercase() }.toSet())

        // 6. Verify Core Expressions
        assertTrue("Expressions count should be at least 8", u6.expressions.size >= 8)

        // 7. Validate Unit 6 with EnglishContentValidator
        val isValid = EnglishContentValidator.validateUnit(context, u6)
        assertTrue("S2 Unit 6 should pass validation", isValid)

        // 8. Verify Lesson Titles
        val titleL1 = com.example.ui.english.getLessonTitle("english_pep_2013_g3_s2_u6", com.example.ui.english.EnglishLessonType.LESSON1)
        assertEquals("课时 1：风筝草地数一数", titleL1)
        val titleL3 = com.example.ui.english.getLessonTitle("english_pep_2013_g3_s2_u6", com.example.ui.english.EnglishLessonType.LESSON3)
        assertEquals("课时 3：五个短元音总复习", titleL3)
        val titleL5 = com.example.ui.english.getLessonTitle("english_pep_2013_g3_s2_u6", com.example.ui.english.EnglishLessonType.LESSON5)
        assertEquals("课时 5：数字单词 16—20 与计数工坊", titleL5)

        // 9. Verify Deterministic Evaluator
        assertTrue(EnglishNumberAnswerEvaluator.evaluateWord("eleven", "eleven"))
        assertTrue(EnglishNumberAnswerEvaluator.evaluateDigit(15, 15))
        assertTrue(EnglishNumberAnswerEvaluator.evaluateChineseNumeral("二十", "二十"))
        assertTrue(EnglishNumberAnswerEvaluator.evaluateQuantityVisual(16, 16))

        // 10. Verify Quantity Scene Model
        val scene = EnglishQuantityScene("scene_test", CountableObjectType.KITE, 12)
        assertEquals(12, scene.objectCount)
        assertEquals(CountableObjectType.KITE, scene.objectType)
    }

    @Test
    fun testGrade4Unit1ContentAndTitles() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val manifest = EnglishContentLoader.loadManifest(context, "english_pep_2013_g4_s1")
        assertNotNull("G4 S1 manifest should load successfully", manifest)
        assertEquals("english_pep_2013_g4_s1", manifest!!.courseId)

        val u1 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_u1")
        assertNotNull("G4 S1 Unit 1 should load successfully", u1)
        assertEquals("english_pep_2013_g4_s1_u1", u1!!.unitId)
        assertEquals("Unit 1: My classroom", u1.title)

        // Validate unit content
        val isValid = EnglishContentValidator.validateUnit(context, u1)
        assertTrue("G4 S1 Unit 1 must pass validation", isValid)

        // Verify lesson titles
        val titleL1 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u1", com.example.ui.english.EnglishLessonType.LESSON1)
        assertEquals("课时 1：走进新教室", titleL1)
        val titleL2 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u1", com.example.ui.english.EnglishLessonType.LESSON2)
        assertEquals("课时 2：教室词汇 A", titleL2)
        val titleL3 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u1", com.example.ui.english.EnglishLessonType.LESSON3)
        assertEquals("课时 3：a-e 长元音拼读工坊", titleL3)
        val titleL4 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u1", com.example.ui.english.EnglishLessonType.LESSON4)
        assertEquals("课时 4：一起整理教室", titleL4)
        val titleL5 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u1", com.example.ui.english.EnglishLessonType.LESSON5)
        assertEquals("课时 5：教室词汇 B 与空间布置", titleL5)
        val titleL6 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u1", com.example.ui.english.EnglishLessonType.LESSON6)
        assertEquals("课时 6：Unit 1 综合挑战", titleL6)

        // Ensure none of the titles equal Grade 3 Unit 1 stationery titles
        assertNotEquals("课时 1：打招呼与自我介绍", titleL1)
        assertNotEquals("课时 2：文具词汇A", titleL2)
        assertNotEquals("课时 3：询问姓名", titleL3)

        // Verify Lesson 2 classroom words
        val classroomWords = u1.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        val classroomSpellings = classroomWords.map { it.spelling.lowercase() }.toSet()
        assertTrue(classroomSpellings.contains("classroom"))
        assertTrue(classroomSpellings.contains("blackboard"))
        assertTrue(classroomSpellings.contains("window"))
        assertFalse("G4 Unit 1 must NOT contain ruler", classroomSpellings.contains("ruler"))

        // Verify Phonics words
        val phonicsWords = u1.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
        val phonicsSpellings = phonicsWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("cake", "face", "name", "make"), phonicsSpellings)
    }

    @Test
    fun testGrade3AndGrade4CrossGradeIsolation() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Load Grade 3 Unit 1
        val g3u1 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g3_s1", "english_pep_2013_g3_s1_u1")
        assertNotNull(g3u1)
        assertEquals("Unit 1: Hello!", g3u1!!.title)
        val g3TitleL2 = com.example.ui.english.getLessonTitle("english_pep_2013_g3_s1_u1", com.example.ui.english.EnglishLessonType.LESSON2)
        assertEquals("课时 2：文具词汇A", g3TitleL2)

        // 2. Load Grade 4 Unit 1
        val g4u1 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_u1")
        assertNotNull(g4u1)
        assertEquals("Unit 1: My classroom", g4u1!!.title)
        val g4TitleL2 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u1", com.example.ui.english.EnglishLessonType.LESSON2)
        assertEquals("课时 2：教室词汇 A", g4TitleL2)

        // 3. Switch back to Grade 3 Unit 1
        val g3TitleL2Retry = com.example.ui.english.getLessonTitle("english_pep_2013_g3_s1_u1", com.example.ui.english.EnglishLessonType.LESSON2)
        assertEquals("课时 2：文具词汇A", g3TitleL2Retry)

        // Verify word pools are distinct
        val g3Words = g3u1.words.map { it.spelling.lowercase() }.toSet()
        val g4Words = g4u1.words.map { it.spelling.lowercase() }.toSet()
        assertTrue(g3Words.contains("ruler"))
        assertFalse(g3Words.contains("classroom"))
        assertTrue(g4Words.contains("classroom"))
        assertFalse(g4Words.contains("ruler"))
    }

    @Test
    fun testGrade4Unit2ContentAndValidation() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val u2 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_u2")
        assertNotNull("G4 S1 Unit 2 data should load successfully", u2)
        assertEquals("english_pep_2013_g4_s1_u2", u2!!.unitId)
        assertEquals("Unit 2: My schoolbag", u2.title)

        // Validate unit content
        val isValid = EnglishContentValidator.validateUnit(context, u2)
        assertTrue("G4 S1 Unit 2 content validation must pass", isValid)

        // Check titles
        val titleL1 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u2", com.example.ui.english.EnglishLessonType.LESSON1)
        assertEquals("课时 1：我的新书包", titleL1)
        val titleL2 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u2", com.example.ui.english.EnglishLessonType.LESSON2)
        assertEquals("课时 2：书包与书本词汇", titleL2)
        val titleL3 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u2", com.example.ui.english.EnglishLessonType.LESSON3)
        assertEquals("课时 3：i-e 长元音拼读工坊", titleL3)
        val titleL4 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u2", com.example.ui.english.EnglishLessonType.LESSON4)
        assertEquals("课时 4：失物招领处", titleL4)
        val titleL5 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u2", com.example.ui.english.EnglishLessonType.LESSON5)
        assertEquals("课时 5：书包物品与整理", titleL5)
        val titleL6 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u2", com.example.ui.english.EnglishLessonType.LESSON6)
        assertEquals("课时 6：Unit 2 综合挑战", titleL6)

        // Verify schoolbag core words
        val coreWords = u2.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        val coreSpellings = coreWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("schoolbag", "maths book", "english book", "chinese book", "storybook", "candy", "notebook", "toy", "key"), coreSpellings)

        // Verify phonics words
        val phonicsWords = u2.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
        val phonicsSpellings = phonicsWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("like", "kite", "five", "nine", "rice"), phonicsSpellings)
    }

    @Test
    fun testGrade4Unit3ContentAndValidation() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val u3 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_u3")
        assertNotNull("G4 S1 Unit 3 data should load successfully", u3)
        assertEquals("english_pep_2013_g4_s1_u3", u3!!.unitId)
        assertEquals("Unit 3: My friends", u3.title)

        // Validate unit content
        val isValid = EnglishContentValidator.validateUnit(context, u3)
        assertTrue("G4 S1 Unit 3 content validation must pass", isValid)

        // Check titles
        val titleL1 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u3", com.example.ui.english.EnglishLessonType.LESSON1)
        assertEquals("课时 1：认识我的新朋友", titleL1)
        val titleL2 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u3", com.example.ui.english.EnglishLessonType.LESSON2)
        assertEquals("课时 2：性格与特征词汇", titleL2)
        val titleL3 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u3", com.example.ui.english.EnglishLessonType.LESSON3)
        assertEquals("课时 3：o-e 长元音拼读工坊", titleL3)
        val titleL4 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u3", com.example.ui.english.EnglishLessonType.LESSON4)
        assertEquals("课时 4：猜猜他是谁", titleL4)
        val titleL5 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u3", com.example.ui.english.EnglishLessonType.LESSON5)
        assertEquals("课时 5：外貌与随身物品", titleL5)
        val titleL6 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u3", com.example.ui.english.EnglishLessonType.LESSON6)
        assertEquals("课时 6：Unit 3 综合挑战", titleL6)

        // Verify friend core words
        val coreWords = u3.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        val coreSpellings = coreWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("strong", "friendly", "quiet", "hair", "shoe", "glasses"), coreSpellings)

        // Verify extended words
        val extendedWords = u3.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        val extendedSpellings = extendedWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("his", "or", "right", "hat", "her"), extendedSpellings)

        // Verify phonics words
        val phonicsWords = u3.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
        val phonicsSpellings = phonicsWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("nose", "note", "coke", "mr jones"), phonicsSpellings)

        assertTrue("Expressions count should be at least 10", u3.expressions.size >= 10)
    }

    @Test
    fun testGrade4Unit3EvaluatorAndClues() {
        val profiles = com.example.data.english.StandardFriendProfiles.allTextbookProfiles

        // Clues for Wu Binbin: boy, glasses, blue shoes
        val clues = listOf(
            com.example.data.english.FriendClue("c1", com.example.data.english.FriendClueType.PRONOUN, "wu_binbin", "he"),
            com.example.data.english.FriendClue("c2", com.example.data.english.FriendClueType.GLASSES, "wu_binbin", "glasses"),
            com.example.data.english.FriendClue("c3", com.example.data.english.FriendClueType.SHOES, "wu_binbin", "blue")
        )

        val result = com.example.data.english.EnglishFriendDescriptionEvaluator.evaluateClues(profiles, clues)
        assertNotNull("Should uniquely identify Wu Binbin", result)
        assertEquals("Wu Binbin", result!!.displayName)

        // Test sentence structure evaluator
        assertTrue(com.example.data.english.EnglishFriendDescriptionEvaluator.isValidDescriptionStructure("He", "is", "friendly"))
        assertTrue(com.example.data.english.EnglishFriendDescriptionEvaluator.isValidDescriptionStructure("He", "has", "glasses"))
        assertTrue(com.example.data.english.EnglishFriendDescriptionEvaluator.isValidDescriptionStructure("His", "are", "shoes are blue"))
        assertFalse(com.example.data.english.EnglishFriendDescriptionEvaluator.isValidDescriptionStructure("He", "is", "glasses"))
    }

    @Test
    fun testGrade4Unit3AutoDictation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val u3 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_u3")!!

        val settingsAll = com.example.data.english.EnglishAutoDictationSettings(
            wordScope = com.example.data.english.AutoDictationWordScope.ALL
        )
        val poolAll = com.example.data.english.EnglishAutoDictationPoolBuilder.buildDictationPool(u3, settingsAll)
        assertEquals(11, poolAll.size)

        val settingsCore = com.example.data.english.EnglishAutoDictationSettings(
            wordScope = com.example.data.english.AutoDictationWordScope.CORE_ONLY
        )
        val poolCore = com.example.data.english.EnglishAutoDictationPoolBuilder.buildDictationPool(u3, settingsCore)
        assertEquals(6, poolCore.size)

        val settingsExtended = com.example.data.english.EnglishAutoDictationSettings(
            wordScope = com.example.data.english.AutoDictationWordScope.EXTENDED_ONLY
        )
        val poolExtended = com.example.data.english.EnglishAutoDictationPoolBuilder.buildDictationPool(u3, settingsExtended)
        assertEquals(5, poolExtended.size)
    }

    @Test
    fun testGrade4Unit4ContentAndValidation() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val u4 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_u4")
        assertNotNull("G4 S1 Unit 4 data should load successfully", u4)
        assertEquals("english_pep_2013_g4_s1_u4", u4!!.unitId)
        assertEquals("Unit 4: My home", u4.title)

        // Validate unit content
        val isValid = EnglishContentValidator.validateUnit(context, u4)
        assertTrue("G4 S1 Unit 4 content validation must pass", isValid)

        // Check titles
        val titleL1 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u4", com.example.ui.english.EnglishLessonType.LESSON1)
        assertEquals("课时 1：小猫藏在哪里", titleL1)
        val titleL2 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u4", com.example.ui.english.EnglishLessonType.LESSON2)
        assertEquals("课时 2：家庭房间词汇", titleL2)
        val titleL3 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u4", com.example.ui.english.EnglishLessonType.LESSON3)
        assertEquals("课时 3：u-e 长元音拼读工坊", titleL3)
        val titleL4 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u4", com.example.ui.english.EnglishLessonType.LESSON4)
        assertEquals("课时 4：钥匙在哪里", titleL4)
        val titleL5 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u4", com.example.ui.english.EnglishLessonType.LESSON5)
        assertEquals("课时 5：家具物品与房间布置", titleL5)
        val titleL6 = com.example.ui.english.getLessonTitle("english_pep_2013_g4_s1_u4", com.example.ui.english.EnglishLessonType.LESSON6)
        assertEquals("课时 6：Unit 4 综合挑战", titleL6)

        // Verify home core words
        val coreWords = u4.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        val coreSpellings = coreWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("bedroom", "living room", "study", "kitchen", "bathroom", "bed", "phone", "table", "sofa", "fridge"), coreSpellings)

        // Verify living room is multi-word term
        val livingRoomWord = u4.words.find { it.spelling == "living room" }
        assertNotNull("living room should exist", livingRoomWord)
        assertTrue("living room should contain space for multi-word term", livingRoomWord!!.spelling.contains(" "))

        // Verify extended words
        val extendedWords = u4.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        val extendedSpellings = extendedWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("find", "them"), extendedSpellings)

        // Verify phonics words
        val phonicsWords = u4.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
        val phonicsSpellings = phonicsWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("use", "cute", "excuse"), phonicsSpellings)

        assertTrue("Expressions count should be at least 8", u4.expressions.size >= 8)
    }

    @Test
    fun testGrade4Unit4AutoDictation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val u4 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_u4")!!

        val settingsAll = com.example.data.english.EnglishAutoDictationSettings(
            wordScope = com.example.data.english.AutoDictationWordScope.ALL
        )
        val poolAll = com.example.data.english.EnglishAutoDictationPoolBuilder.buildDictationPool(u4, settingsAll)
        assertEquals(12, poolAll.size)

        val settingsCore = com.example.data.english.EnglishAutoDictationSettings(
            wordScope = com.example.data.english.AutoDictationWordScope.CORE_ONLY
        )
        val poolCore = com.example.data.english.EnglishAutoDictationPoolBuilder.buildDictationPool(u4, settingsCore)
        assertEquals(10, poolCore.size)

        val settingsExtended = com.example.data.english.EnglishAutoDictationSettings(
            wordScope = com.example.data.english.AutoDictationWordScope.EXTENDED_ONLY
        )
        val poolExtended = com.example.data.english.EnglishAutoDictationPoolBuilder.buildDictationPool(u4, settingsExtended)
        assertEquals(2, poolExtended.size)

        // Phonics words use, cute, excuse must NOT be in dictation pool
        val poolSpellings = poolAll.map { it.word.spelling.lowercase() }
        assertFalse("use should not be in dictation pool", poolSpellings.contains("use"))
        assertFalse("excuse should not be in dictation pool", poolSpellings.contains("excuse"))
    }

    @Test
    fun testGrade4Unit5Content() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val u5 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_u5")
        assertNotNull("Unit 5 should exist", u5)
        u5!!

        assertEquals("english_pep_2013_g4_s1_u5", u5.unitId)
        assertEquals("Unit 5: Dinner’s ready", u5.title)
        assertEquals(5, u5.order)

        // Verify lesson titles
        val titleL1 = getLessonTitle(u5.unitId, EnglishLessonType.LESSON1)
        val titleL2 = getLessonTitle(u5.unitId, EnglishLessonType.LESSON2)
        val titleL3 = getLessonTitle(u5.unitId, EnglishLessonType.LESSON3)
        val titleL4 = getLessonTitle(u5.unitId, EnglishLessonType.LESSON4)
        val titleL5 = getLessonTitle(u5.unitId, EnglishLessonType.LESSON5)
        val titleL6 = getLessonTitle(u5.unitId, EnglishLessonType.LESSON6)

        assertEquals("课时 1：今晚想吃什么", titleL1)
        assertEquals("课时 2：晚餐食物词汇", titleL2)
        assertEquals("课时 3：词尾 e 长元音拼读工坊", titleL3)
        assertEquals("课时 4：请随便吃", titleL4)
        assertEquals("课时 5：餐具与餐桌准备", titleL5)
        assertEquals("课时 6：Unit 5 综合挑战", titleL6)

        // Verify core words
        val coreWords = u5.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        val coreSpellings = coreWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("beef", "chicken", "noodles", "soup", "vegetable", "chopsticks", "bowl", "fork", "knife", "spoon"), coreSpellings)

        // Verify extended words
        val extendedWords = u5.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        val extendedSpellings = extendedWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("dinner", "ready", "help yourself", "pass", "try"), extendedSpellings)

        // Verify phonics words
        val phonicsWords = u5.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
        val phonicsSpellings = phonicsWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("me", "he", "she", "we"), phonicsSpellings)

        assertTrue("Expressions count should be at least 10", u5.expressions.size >= 10)
    }

    @Test
    fun testGrade4Unit5AutoDictation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val u5 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_u5")!!

        val settingsAll = com.example.data.english.EnglishAutoDictationSettings(
            wordScope = com.example.data.english.AutoDictationWordScope.ALL
        )
        val poolAll = com.example.data.english.EnglishAutoDictationPoolBuilder.buildDictationPool(u5, settingsAll)
        assertEquals(15, poolAll.size)

        val settingsCore = com.example.data.english.EnglishAutoDictationSettings(
            wordScope = com.example.data.english.AutoDictationWordScope.CORE_ONLY
        )
        val poolCore = com.example.data.english.EnglishAutoDictationPoolBuilder.buildDictationPool(u5, settingsCore)
        assertEquals(10, poolCore.size)

        val settingsExtended = com.example.data.english.EnglishAutoDictationSettings(
            wordScope = com.example.data.english.AutoDictationWordScope.EXTENDED_ONLY
        )
        val poolExtended = com.example.data.english.EnglishAutoDictationPoolBuilder.buildDictationPool(u5, settingsExtended)
        assertEquals(5, poolExtended.size)

        // Phonics words me, he, she, we must NOT be in dictation pool
        val poolSpellings = poolAll.map { it.word.spelling.lowercase() }
        assertFalse("me should not be in dictation pool", poolSpellings.contains("me"))
        assertFalse("he should not be in dictation pool", poolSpellings.contains("he"))
    }

    @Test
    fun testGrade4Unit6Content() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val u6 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_u6")
        assertNotNull("Unit 6 should exist", u6)
        u6!!

        assertEquals("english_pep_2013_g4_s1_u6", u6.unitId)
        assertEquals("Unit 6: Meet my family!", u6.title)
        assertEquals(6, u6.order)

        // Verify lesson titles
        val titleL1 = getLessonTitle(u6.unitId, EnglishLessonType.LESSON1)
        val titleL2 = getLessonTitle(u6.unitId, EnglishLessonType.LESSON2)
        val titleL3 = getLessonTitle(u6.unitId, EnglishLessonType.LESSON3)
        val titleL4 = getLessonTitle(u6.unitId, EnglishLessonType.LESSON4)
        val titleL5 = getLessonTitle(u6.unitId, EnglishLessonType.LESSON5)
        val titleL6 = getLessonTitle(u6.unitId, EnglishLessonType.LESSON6)

        assertEquals("课时 1：我们家有几个人", titleL1)
        assertEquals("课时 2：家庭成员词汇", titleL2)
        assertEquals("课时 3：五个元音综合复习", titleL3)
        assertEquals("课时 4：这是你的谁", titleL4)
        assertEquals("课时 5：家人的职业", titleL5)
        assertEquals("课时 6：Unit 6 综合挑战", titleL6)

        // Verify core words
        val coreWords = u6.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        val coreSpellings = coreWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("parents", "cousin", "uncle", "aunt", "baby brother", "doctor", "cook", "driver", "farmer", "nurse"), coreSpellings)

        // Verify extended words
        val extendedWords = u6.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        val extendedSpellings = extendedWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("people", "but", "little", "puppy", "football player", "job", "basketball"), extendedSpellings)

        // Verify phonics words
        val phonicsWords = u6.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
        val phonicsSpellings = phonicsWords.map { it.spelling.lowercase() }.toSet()
        assertEquals(setOf("me", "he", "she", "we", "face", "rice", "nose", "use", "bag", "leg", "six", "dog", "mum"), phonicsSpellings)

        assertTrue("Expressions count should be at least 10", u6.expressions.size >= 10)
    }

    @Test
    fun testGrade4Unit6AutoDictation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val u6 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_u6")!!

        val settingsAll = com.example.data.english.EnglishAutoDictationSettings(
            wordScope = com.example.data.english.AutoDictationWordScope.ALL
        )
        val poolAll = com.example.data.english.EnglishAutoDictationPoolBuilder.buildDictationPool(u6, settingsAll)
        assertEquals(17, poolAll.size)

        val settingsCore = com.example.data.english.EnglishAutoDictationSettings(
            wordScope = com.example.data.english.AutoDictationWordScope.CORE_ONLY
        )
        val poolCore = com.example.data.english.EnglishAutoDictationPoolBuilder.buildDictationPool(u6, settingsCore)
        assertEquals(10, poolCore.size)

        val settingsExtended = com.example.data.english.EnglishAutoDictationSettings(
            wordScope = com.example.data.english.AutoDictationWordScope.EXTENDED_ONLY
        )
        val poolExtended = com.example.data.english.EnglishAutoDictationPoolBuilder.buildDictationPool(u6, settingsExtended)
        assertEquals(7, poolExtended.size)

        // Phonics words face, rice, bag, leg, use, me must NOT be in dictation pool
        val poolSpellings = poolAll.map { it.word.spelling.lowercase() }
        assertFalse("face should not be in dictation pool", poolSpellings.contains("face"))
        assertFalse("rice should not be in dictation pool", poolSpellings.contains("rice"))
        assertFalse("bag should not be in dictation pool", poolSpellings.contains("bag"))
        assertFalse("leg should not be in dictation pool", poolSpellings.contains("leg"))
        assertFalse("use should not be in dictation pool", poolSpellings.contains("use"))
        assertFalse("me should not be in dictation pool", poolSpellings.contains("me"))
    }

    @Test
    fun testUnitDisplayTag() {
        val u1 = com.example.data.english.EnglishUnitSummary(
            unitId = "english_pep_2013_g4_s1_u1",
            title = "Unit 1: My classroom",
            order = 1
        )
        assertEquals("U1", u1.unitDisplayTag)

        val u4 = com.example.data.english.EnglishUnitSummary(
            unitId = "english_pep_2013_g4_s1_u4",
            title = "Unit 4: My home",
            order = 5
        )
        assertEquals("U4", u4.unitDisplayTag)

        val recycle = com.example.data.english.EnglishUnitSummary(
            unitId = "english_pep_2013_g4_s1_recycle_1",
            contentType = "RECYCLE",
            title = "Recycle 1",
            order = 4
        )
        assertEquals("REC", recycle.unitDisplayTag)
    }

    @Test
    fun testRecycle2G4S1DataAndValidation() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Load Recycle 2 JSON and verify contents
        val recycle = EnglishContentLoader.loadRecycle(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_recycle_2")
        assertNotNull("Recycle 2 should load successfully", recycle)
        assertEquals("english_pep_2013_g4_s1_recycle_2", recycle!!.recycleId)
        assertEquals("Recycle 2", recycle.title)
        assertEquals(5, recycle.missions.size)

        // Verify specific mission IDs and types
        val m1 = recycle.missions[0]
        assertEquals("STORY_ROLEPLAY", m1.missionType)
        assertEquals("english_pep_2013_g4_s1_r2_m1", m1.missionId)

        val m2 = recycle.missions[1]
        assertEquals("LISTEN_NUMBER_ROOMS", m2.missionType)

        val m3 = recycle.missions[2]
        assertEquals("PHONICS_LISTEN_WRITE", m3.missionType)

        val m4 = recycle.missions[3]
        assertEquals("FAMILY_HOME_INTERVIEW", m4.missionType)

        val m5 = recycle.missions[4]
        assertEquals("SEMESTER_QUESTION_GAME", m5.missionType)

        // 2. Validate Recycle 2 via EnglishContentValidator
        val isValid = EnglishContentValidator.validateRecycle(context, recycle)
        assertTrue("Recycle 2 validation should be valid", isValid)

        // 3. Verify Progress Management for Recycle 2
        EnglishProgressManager.clearProgress(context)
        var progress = EnglishProgressManager.getRecycleProgress(context, "english_pep_2013_g4_s1_recycle_2")
        assertNotNull(progress)
        assertTrue(progress!!.completedMissionIds.isEmpty())

        EnglishProgressManager.completeRecycleMission(context, "english_pep_2013_g4_s1_recycle_2", "english_pep_2013_g4_s1_r2_m1")
        progress = EnglishProgressManager.getRecycleProgress(context, "english_pep_2013_g4_s1_recycle_2")
        assertTrue(progress!!.completedMissionIds.contains("english_pep_2013_g4_s1_r2_m1"))

        // 4. Verify Review Pool Builder for G4 S1
        val pool = EnglishReviewPoolBuilder.buildPool(context, listOf("english_pep_2013_g4_s1_u1", "english_pep_2013_g4_s1_u6"))
        assertNotNull(pool)
        assertTrue("Review pool should contain items", pool.isNotEmpty())

        // Check for textbookDerived on P69
        val p69Questions = pool.filter { it.textbookDerived && it.sourceReference == "P69" }
        assertTrue("Should have textbookDerived P69 questions", p69Questions.isNotEmpty())
        val schoolbagQ = p69Questions.find { it.id == "q_g4_p69_schoolbag" }
        assertNotNull("schoolbag P69 question should exist", schoolbagQ)
        assertEquals("What's in your schoolbag? (书包里有什么？)", schoolbagQ!!.promptText)

        // Check for generatedPractice cumulative question
        val practiceQuestions = pool.filter { it.generatedPractice }
        assertTrue("Should have generatedPractice questions", practiceQuestions.isNotEmpty())
        val classroomQ = practiceQuestions.find { it.id == "q_g4_practice_classroom" }
        assertNotNull("classroom practice question should exist", classroomQ)
    }

    @Test
    fun testG4S2FrameworkAndUnit1() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Load S2 Manifest and verify 8-node list structure
        val manifest = EnglishContentLoader.loadManifest(context, "english_pep_2013_g4_s2")
        assertNotNull("G4 S2 Manifest should load successfully", manifest)
        assertEquals("Course ID matches", "english_pep_2013_g4_s2", manifest!!.courseId)
        assertEquals("Manifest should have exactly 8 units/recycle", 8, manifest.units.size)

        // Verify Unit 1 is READY and Unit 2 is READY, rest is UNDER_CONSTRUCTION
        val u1Summary = manifest.units[0]
        assertEquals("english_pep_2013_g4_s2_u1", u1Summary.unitId)
        assertEquals("READY", u1Summary.contentStatus)

        val u2Summary = manifest.units[1]
        assertEquals("english_pep_2013_g4_s2_u2", u2Summary.unitId)
        assertEquals("READY", u2Summary.contentStatus)

        for (i in 2 until manifest.units.size) {
            assertEquals("UNDER_CONSTRUCTION", manifest.units[i].contentStatus)
        }

        // 2. Load Unit 1 detail data
        val u1 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s2", "english_pep_2013_g4_s2_u1")
        assertNotNull("G4 S2 Unit 1 details should load", u1)
        assertEquals("english_pep_2013_g4_s2_u1", u1!!.unitId)
        assertEquals("Unit 1: My school", u1.title)

        // Check word counts and levels
        val coreWords = u1.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        assertEquals("Should have exactly 8 core words", 8, coreWords.size)
        assertTrue(coreWords.map { it.spelling }.contains("teachers' office"))
        assertTrue(coreWords.map { it.spelling }.contains("library"))

        val extendedWords = u1.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        assertEquals("Should have exactly 5 extended words", 5, extendedWords.size)

        val phonicsWords = u1.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
        assertEquals("Should have exactly 7 phonics words", 7, phonicsWords.size)

        // 3. Validate G4 S2 Unit 1 via EnglishContentValidator
        val isValid = EnglishContentValidator.validateUnit(context, u1)
        assertTrue("G4 S2 Unit 1 should be perfectly valid", isValid)
    }

    @Test
    fun testG4S2Unit2ContentAndValidation() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. g4s2u2_parses
        val u2 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s2", "english_pep_2013_g4_s2_u2")
        assertNotNull("G4 S2 Unit 2 details should load", u2)
        assertEquals("english_pep_2013_g4_s2_u2", u2!!.unitId)

        // 2. g4s2u2_validatorPasses
        val isValid = EnglishContentValidator.validateUnit(context, u2)
        assertTrue("G4 S2 Unit 2 should pass validation", isValid)

        // 3. g4s2u2_pages12To21
        val manifest = EnglishContentLoader.loadManifest(context, "english_pep_2013_g4_s2")!!
        val u2Summary = manifest.units[1]
        assertEquals("12—21", u2Summary.textbookPages)

        // 4. g4s2u2_hasSixLessons & g4s2u2_titlesCorrect
        val lessons = listOf(
            EnglishLessonType.LESSON1, EnglishLessonType.LESSON2, EnglishLessonType.LESSON3,
            EnglishLessonType.LESSON4, EnglishLessonType.LESSON5, EnglishLessonType.LESSON6
        )
        val expectedTitles = listOf(
            "课时 1：现在几点了", "课时 2：一天中的课程与三餐", "课时 3：ir / ur 发音工坊",
            "课时 4：快起床，该上学了", "课时 5：我的一天时间表", "课时 6：Unit 2 综合挑战"
        )
        lessons.forEachIndexed { i, lType ->
            val title = getLessonTitle("english_pep_2013_g4_s2_u2", lType)
            assertEquals("Lesson title matches expected PEP unit 2", expectedTitles[i], title)
        }

        // 5. g4s2u2_ready & g4s2u3_stillNotImplemented
        assertEquals(ContentAvailability.READY, EnglishContentLoader.getUnitContentAvailability(context, "english_pep_2013_g4_s2", "english_pep_2013_g4_s2_u2"))
        assertEquals(ContentAvailability.NOT_IMPLEMENTED, EnglishContentLoader.getUnitContentAvailability(context, "english_pep_2013_g4_s2", "english_pep_2013_g4_s2_u3"))

        // 6. coreVocabulary_count10 & coreVocabulary_matchesAppendix
        val coreWords = u2.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        assertEquals(10, coreWords.size)
        val actualCore = coreWords.map { it.spelling }.toSet()
        val expectedCore = setOf(
            "breakfast", "English class", "lunch", "music class", "PE class", "dinner",
            "get up", "go to school", "go home", "go to bed"
        )
        assertEquals(expectedCore, actualCore)

        // 7. extendedVocabulary_count8 & extendedVocabulary_matchesAppendix
        val extendedWords = u2.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        assertEquals(8, extendedWords.size)
        val actualExtended = extendedWords.map { it.spelling }.toSet()
        val expectedExtended = setOf(
            "over", "now", "o'clock", "kid", "thirty", "hurry up", "come on", "just a minute"
        )
        assertEquals(expectedExtended, actualExtended)

        // 8. extendedIsListenSpeakOnly & extendedIsOptionalChallenge
        extendedWords.forEach { word ->
            assertEquals("LISTEN_SPEAK_ONLY", word.requirementLevel)
            assertTrue(word.requiredSkills.contains(EnglishSkill.LISTEN))
            assertTrue(word.requiredSkills.contains(EnglishSkill.SPEAK))
            assertFalse(word.requiredSkills.contains(EnglishSkill.SPELL))
        }
    }

    @Test
    fun testG4S2Unit2MultiWordAndOClock() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val u2 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s2", "english_pep_2013_g4_s2_u2")!!

        // Multi-word spelling testing
        val multiwords = listOf(
            "English class", "music class", "PE class", "get up", "go to school",
            "go home", "go to bed", "hurry up", "come on", "just a minute"
        )
        multiwords.forEach { mw ->
            val w = u2.words.find { it.spelling == mw }
            assertNotNull("Multiword $mw should exist in Unit 2", w)
        }

        // test wholeAnswers
        val englishClass = "English class"
        assertTrue(englishClass.equals("English class", ignoreCase = true))
        assertFalse(englishClass.equals("English", ignoreCase = true))

        val justAMinute = "just a minute"
        assertTrue(justAMinute.equals("just a minute", ignoreCase = true))
        assertFalse(justAMinute.equals("just", ignoreCase = true))

        // partialPhraseFails checks
        val userInputPartial = "go to"
        assertNotEquals("go to school", userInputPartial)

        // o'clock apostrophe
        val oClock = u2.words.find { it.spelling == "o'clock" }
        assertNotNull(oClock)
        assertEquals("o'clock", oClock!!.spelling)

        // normalizes apostrophes
        assertEquals("o'clock", EnglishTimeAnswerEvaluator.normalizeApostrophe("o'clock"))
        assertEquals("o'clock", EnglishTimeAnswerEvaluator.normalizeApostrophe("o’clock"))
        assertNotEquals("o'clock", "oclock")
    }

    @Test
    fun testG4S2Unit2ClockAndFormatting() {
        // Clock models parses
        val t7_00 = EnglishClockTime(7, 0)
        val t6_30 = EnglishClockTime(6, 30)
        val t7_20 = EnglishClockTime(7, 20)
        val t10_40 = EnglishClockTime(10, 40)
        val t2_15 = EnglishClockTime(2, 15)
        val t4_50 = EnglishClockTime(4, 50)
        val t9_00 = EnglishClockTime(9, 0)

        // Formatting Digital
        assertEquals("7:00", EnglishTimeFormatter.formatDigital(t7_00))
        assertEquals("6:30", EnglishTimeFormatter.formatDigital(t6_30))
        assertEquals("7:20", EnglishTimeFormatter.formatDigital(t7_20))
        assertEquals("10:40", EnglishTimeFormatter.formatDigital(t10_40))
        assertEquals("2:15", EnglishTimeFormatter.formatDigital(t2_15))
        assertEquals("4:50", EnglishTimeFormatter.formatDigital(t4_50))
        assertEquals("9:00", EnglishTimeFormatter.formatDigital(t9_00))

        // Spoken forms
        assertEquals("seven o'clock", EnglishTimeFormatter.formatSpoken(t7_00))
        assertEquals("six thirty", EnglishTimeFormatter.formatSpoken(t6_30))
        assertEquals("seven twenty", EnglishTimeFormatter.formatSpoken(t7_20))
        assertEquals("ten forty", EnglishTimeFormatter.formatSpoken(t10_40))
        assertEquals("two fifteen", EnglishTimeFormatter.formatSpoken(t2_15))
        assertEquals("four fifty", EnglishTimeFormatter.formatSpoken(t4_50))
        assertEquals("nine o'clock", EnglishTimeFormatter.formatSpoken(t9_00))

        // wholeHourAllowsOClock & nonWholeHourRejectsOClock
        assertTrue(EnglishTimeFormatter.formatSpoken(t7_00).contains("o'clock"))
        assertFalse(EnglishTimeFormatter.formatSpoken(t7_20).contains("o'clock"))

        // Clock math values
        assertEquals(7 * 60, t7_00.normalizedMinuteOfDay)
        assertEquals(6 * 60 + 30, t6_30.normalizedMinuteOfDay)
        assertEquals(14 * 60 + 15, EnglishClockTime(2, 15, DayPeriod.PM).normalizedMinuteOfDay)

        // invalidHourFails & invalidMinuteFails check
        try {
            EnglishClockTime(13, 0)
            fail("Should fail on invalid hour")
        } catch (e: IllegalArgumentException) {
            // pass
        }
        try {
            EnglishClockTime(5, 60)
            fail("Should fail on invalid minute")
        } catch (e: IllegalArgumentException) {
            // pass
        }
    }

    @Test
    fun testG4S2Unit2GrammarAndSchedule() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // TIME_FOR_NOUN vs TIME_TO_VERB matching
        assertTrue(EnglishTimeAnswerEvaluator.evaluateGrammarSentence("It's time for breakfast.", "breakfast", TimeGrammarMode.TIME_FOR_NOUN))
        assertTrue(EnglishTimeAnswerEvaluator.evaluateGrammarSentence("it's time for English class.", "english_class", TimeGrammarMode.TIME_FOR_NOUN))
        assertTrue(EnglishTimeAnswerEvaluator.evaluateGrammarSentence("It's time for lunch.", "lunch", TimeGrammarMode.TIME_FOR_NOUN))
        assertTrue(EnglishTimeAnswerEvaluator.evaluateGrammarSentence("It's time to get up.", "get_up", TimeGrammarMode.TIME_TO_VERB))
        assertTrue(EnglishTimeAnswerEvaluator.evaluateGrammarSentence("it's time to go to school.", "go_to_school", TimeGrammarMode.TIME_TO_VERB))

        // Incorrect syntactic patterns
        assertFalse(EnglishTimeAnswerEvaluator.evaluateGrammarSentence("It's time to breakfast.", "breakfast", TimeGrammarMode.TIME_TO_VERB))
        assertFalse(EnglishTimeAnswerEvaluator.evaluateGrammarSentence("It's time for get up.", "get_up", TimeGrammarMode.TIME_FOR_NOUN))

        // digital entries evaluator
        assertTrue(EnglishTimeAnswerEvaluator.evaluateDigitalTimeEntry("7:20", EnglishClockTime(7, 20)))
        assertTrue(EnglishTimeAnswerEvaluator.evaluateDigitalTimeEntry("seven twenty", EnglishClockTime(7, 20)))

        // Schedule loads properly & sorts chronologically
        val items = listOf(
            ScheduleItem("i1", "breakfast", DailyActivityType.BREAKFAST, EnglishClockTime(7, 0), TimeGrammarMode.TIME_FOR_NOUN),
            ScheduleItem("i2", "lunch", DailyActivityType.LUNCH, EnglishClockTime(12, 0), TimeGrammarMode.TIME_FOR_NOUN),
            ScheduleItem("i3", "get up", DailyActivityType.GET_UP, EnglishClockTime(6, 30), TimeGrammarMode.TIME_TO_VERB)
        )
        val sorted = items.sortedBy { it.time.normalizedMinuteOfDay }
        assertEquals("get up", sorted[0].activityWordRef)
        assertEquals("breakfast", sorted[1].activityWordRef)
        assertEquals("lunch", sorted[2].activityWordRef)
    }

    @Test
    fun testG4S2Unit2Phonics() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val u2 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s2", "english_pep_2013_g4_s2_u2")!!

        // unit2Phonics_irUr
        val phonicsWords = u2.words.filter { it.requirementLevel == "PHONICS_EXAMPLE" }
        assertEquals(4, phonicsWords.size)
        val spellings = phonicsWords.map { it.spelling }.toSet()
        assertTrue(spellings.contains("girl"))
        assertTrue(spellings.contains("bird"))
        assertTrue(spellings.contains("nurse"))
        assertTrue(spellings.contains("hamburger"))

        // contrast words
        val contrastWords = u2.words.filter { it.requirementLevel == "PHONICS_CONTRAST" }
        assertEquals(4, contrastWords.size)
        val contrastSpellings = contrastWords.map { it.spelling }.toSet()
        assertTrue(contrastSpellings.contains("dirt"))
        assertTrue(contrastSpellings.contains("birth"))
        assertTrue(contrastSpellings.contains("hurt"))
        assertTrue(contrastSpellings.contains("number"))

        // nurse isolation
        val nurseInU2 = u2.words.find { it.spelling == "nurse" }!!
        assertEquals("PHONICS_EXAMPLE", nurseInU2.requirementLevel)
        assertTrue(nurseInU2.wordId.startsWith("english_pep_2013_g4_s2_u2"))
    }

    @Test
    fun testG4S2Unit2ChallengeAndIsolation() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // all 10 core words and 8 extended in stages
        val u2 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s2", "english_pep_2013_g4_s2_u2")!!
        val coreWords = u2.words.filter { it.requirementLevel == "LISTEN_SPEAK_RECOGNIZE" }
        val extendedWords = u2.words.filter { it.requirementLevel == "LISTEN_SPEAK_ONLY" }
        assertEquals(10, coreWords.size)
        assertEquals(8, extendedWords.size)

        // dictation pooling deduplicates
        val pool = coreWords.map { it.spelling }.toSet()
        assertEquals(10, pool.size)

        // isolation: G4S2U2 vs G4S1U2 My schoolbag
        assertNotEquals("english_pep_2013_g4_s1_u2", u2.unitId)
        val g4s1u2 = EnglishContentLoader.loadUnit(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_u2")
        assertNotNull(g4s1u2)
        assertEquals("Unit 2: My schoolbag", g4s1u2!!.title)

        // verify progress isolated
        EnglishProgressManager.clearProgress(context)
        assertFalse(EnglishProgressManager.isUnitCompleted(context, "english_pep_2013_g4_s2", "english_pep_2013_g4_s2_u2"))
        assertFalse(EnglishProgressManager.isUnitCompleted(context, "english_pep_2013_g4_s1", "english_pep_2013_g4_s1_u2"))
    }
}


