package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.math.MathContentLoader
import com.example.data.math.MathContentValidator
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MathContentTest {

    @Test
    fun testLoadAndValidateAllUnits() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // 1. Load manifest
        val manifest = MathContentLoader.loadManifest(context)
        assertNotNull("Manifest should not be null", manifest)
        println("--- Manifest Loaded successfully ---")
        println("Course ID: ${manifest!!.courseId}")
        println("Units in Manifest:")
        manifest.units.forEach { unitSummary ->
            println("  - Unit ID: ${unitSummary.unitId}, Title: ${unitSummary.title}, Order: ${unitSummary.order}, AssetPath: ${MathContentLoader.getUnitAssetPath(manifest.courseId, unitSummary.unitId)}")
        }

        // 2. Validate unit_01.json
        println("\n=== Validating Unit 1 ===")
        val u1 = MathContentLoader.loadUnit(context, "math_pep_g6_s1", "math_pep_g6_s1_u1")
        assertNotNull("Unit 1 should load", u1)
        val u1Formal = u1!!.lessons.filter { it.isFormalLesson() }
        val u1Test = u1.lessons.filter { it.isEngineTestLesson() }
        assertEquals("Unit 1 formal lessons count", 6, u1Formal.size)
        assertEquals("Unit 1 test lessons count", 1, u1Test.size)
        val u1Val = MathContentValidator.validateUnit(u1)
        assertTrue("Unit 1 validation errors: ${u1Val.errors}", u1Val.isValid)

        // 3. Validate unit_02.json
        println("\n=== Validating Unit 2 ===")
        val u2 = MathContentLoader.loadUnit(context, "math_pep_g6_s1", "math_pep_g6_s1_u2")
        assertNotNull("Unit 2 should load", u2)
        val u2Formal = u2!!.lessons.filter { it.isFormalLesson() }
        val u2Test = u2.lessons.filter { it.isEngineTestLesson() }
        assertEquals("Unit 2 formal lessons count", 4, u2Formal.size)
        assertEquals("Unit 2 test lessons count", 0, u2Test.size)
        val u2Val = MathContentValidator.validateUnit(u2)
        assertTrue("Unit 2 validation errors: ${u2Val.errors}", u2Val.isValid)

        // 4. Validate unit_03.json
        println("\n=== Validating Unit 3 ===")
        val u3 = MathContentLoader.loadUnit(context, "math_pep_g6_s1", "math_pep_g6_s1_u3")
        assertNotNull("Unit 3 should load", u3)
        val u3Formal = u3!!.lessons.filter { it.isFormalLesson() }.sortedBy { it.order }
        val u3Test = u3.lessons.filter { it.isEngineTestLesson() }
        assertEquals("Unit 3 formal lessons count", 6, u3Formal.size)
        assertEquals("Unit 3 test lessons count", 0, u3Test.size)
        
        // Assert Lesson 1 of Unit 3
        assertEquals("math_pep_g6_s1_u3_l1", u3Formal[0].lessonId)
        assertEquals(1, u3Formal[0].order)
        assertTrue(u3Formal[0].isFormalLesson())
        assertFalse(u3Formal[0].isEngineTestLesson())
        assertTrue("Lesson 1 title should contain 倒数的认识", u3Formal[0].title.contains("倒数的认识"))

        val u3Val = MathContentValidator.validateUnit(u3)
        assertTrue("Unit 3 validation errors: ${u3Val.errors}", u3Val.isValid)

        // 5. Validate unit_04.json
        println("\n=== Validating Unit 4 ===")
        val u4 = MathContentLoader.loadUnit(context, "math_pep_g6_s1", "math_pep_g6_s1_u4")
        assertNotNull("Unit 4 should load", u4)
        val u4Formal = u4!!.lessons.filter { it.isFormalLesson() }.sortedBy { it.order }
        assertEquals("Unit 4 formal lessons count", 6, u4Formal.size)
        val u4Val = MathContentValidator.validateUnit(u4)
        assertTrue("Unit 4 validation errors: ${u4Val.errors}", u4Val.isValid)
    }
}
