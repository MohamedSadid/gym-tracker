package com.bool.gymtracker.domain

import com.bool.gymtracker.data.seed.ExerciseSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseDemoTest {
    @Test
    fun mapsTheShippedLifts() {
        val names = listOf(
            "Barbell bench press" to "exercises/barbell_bench_press_lockout.png",
            "Dumbbell bench press" to "exercises/dumbbell_bench_press_lockout.png",
            "Smith bench press" to "exercises/smith_bench_press_lockout.png",
            "Incline barbell press" to "exercises/incline_barbell_press_lockout.png",
            "Incline Smith bench press" to "exercises/incline_smith_bench_press_lockout.png",
            "Incline dumbbell press" to "exercises/incline_dumbbell_press_lockout.png",
            "Pec deck fly" to "exercises/pec_deck_fly_lockout.png",
        )
        names.forEach { (name, lockout) ->
            val demo = ExerciseDemos.forName(name)!!
            assertEquals(lockout, demo.lockoutAsset)
            assertEquals(3, demo.tips.size)
        }
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
