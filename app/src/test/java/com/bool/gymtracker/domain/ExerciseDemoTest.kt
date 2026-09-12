package com.bool.gymtracker.domain

import com.bool.gymtracker.data.seed.ExerciseSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseDemoTest {
    @Test
    fun mapsTheShippedLifts() {
        val barbell = ExerciseDemos.forName("Barbell bench press")!!
        assertEquals("exercises/barbell_bench_press_lockout.png", barbell.lockoutAsset)
        assertEquals(3, barbell.tips.size)

        val bench = ExerciseDemos.forName("Dumbbell bench press")!!
        assertEquals("exercises/dumbbell_bench_press_lockout.png", bench.lockoutAsset)
        assertEquals(3, bench.tips.size)

        val smith = ExerciseDemos.forName("Smith bench press")!!
        assertEquals("exercises/smith_bench_press_lockout.png", smith.lockoutAsset)
        assertEquals(3, smith.tips.size)

        val inclineBarbell = ExerciseDemos.forName("Incline barbell press")!!
        assertEquals("exercises/incline_barbell_press_lockout.png", inclineBarbell.lockoutAsset)
        assertEquals(3, inclineBarbell.tips.size)

        val incline = ExerciseDemos.forName("Incline dumbbell press")!!
        assertEquals("exercises/incline_dumbbell_press_bottom.png", incline.bottomAsset)
        assertEquals(3, incline.tips.size)
    }

    @Test
    fun unknownAndCustomHaveNoDemo() {
        assertNull(ExerciseDemos.forName("Chest fly"))
        assertNull(ExerciseDemos.forName("My custom fly"))
    }

    @Test
    fun everyBuiltInHasMainAndAuxiliaryTargets() {
        val seedNames = ExerciseSeed.builtIn().map { it.name }.toSet()
        assertEquals(seedNames, ExerciseTargetMuscles.listedNames())
        val bench = ExerciseTargetMuscles.forName("Dumbbell bench press", MuscleGroup.CHEST)
        assertEquals(listOf("Chest, front delts"), bench.primary)
        assertEquals(listOf("Triceps"), bench.auxiliary)
    }

    @Test
    fun customFallsBackToPrimaryMuscleOnly() {
        val custom = ExerciseTargetMuscles.forName("My fly", MuscleGroup.CHEST)
        assertEquals(listOf("Chest"), custom.primary)
        assertTrue(custom.auxiliary.isEmpty())
    }
}
