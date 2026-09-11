package com.bool.gymtracker.domain.performance

import com.bool.gymtracker.domain.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class VolumeTest {
    private val zone = ZoneId.of("Europe/London")

    @Test
    fun smallVolumeIsWeightTimesRepsForWorkingSets() {
        val rows = listOf(
            log(exerciseId = 1, muscle = MuscleGroup.CHEST, weightKg = 80.0, reps = 5),
            log(exerciseId = 1, muscle = MuscleGroup.CHEST, weightKg = 80.0, reps = 5, isWarmup = true),
            log(exerciseId = 1, muscle = MuscleGroup.CHEST, weightKg = 90.0, reps = 3, completed = false),
            log(exerciseId = 1, muscle = MuscleGroup.CHEST, weightKg = 70.0, reps = 8, finishedAt = null),
        )
        assertEquals(400.0, smallVolume(workingSetLogs(rows)), 0.0)
    }

    @Test
    fun totalVolumeSumsSmallVolumesPerMuscle() {
        val sunday = at("2026-09-13T18:00:00")
        val rows = listOf(
            log(1, MuscleGroup.CHEST, 80.0, 5, sunday),
            log(2, MuscleGroup.CHEST, 20.0, 10, sunday),
            log(3, MuscleGroup.BACK, 60.0, 8, sunday),
        )
        val totals = weeklyMuscleVolumes(rows, zone)
        assertEquals(600.0, totals.single { it.muscle == MuscleGroup.CHEST }.totalVolume, 0.0)
        assertEquals(480.0, totals.single { it.muscle == MuscleGroup.BACK }.totalVolume, 0.0)
        assertEquals(LocalDate.of(2026, 9, 7), totals.first().weekStart)
    }

    @Test
    fun weekIsMondayThroughSundayInLocalTime() {
        val sundayNight = at("2026-09-13T23:30:00")
        val mondayMorning = at("2026-09-14T00:00:00")
        val rows = listOf(
            log(1, MuscleGroup.LEGS, 100.0, 5, sundayNight),
            log(1, MuscleGroup.LEGS, 100.0, 5, mondayMorning),
        )
        val totals = weeklyMuscleVolumes(rows, zone)
        assertEquals(2, totals.size)
        assertEquals(LocalDate.of(2026, 9, 7), totals[0].weekStart)
        assertEquals(500.0, totals[0].totalVolume, 0.0)
        assertEquals(LocalDate.of(2026, 9, 14), totals[1].weekStart)
        assertEquals(500.0, totals[1].totalVolume, 0.0)
    }

    @Test
    fun emptyLogsYieldNoWeeks() {
        assertTrue(weeklyMuscleVolumes(emptyList(), zone).isEmpty())
        assertTrue(weeklyExerciseVolumes(emptyList(), zone).isEmpty())
    }

    private fun at(local: String): Long =
        LocalDateTime.parse(local).atZone(zone).toInstant().toEpochMilli()

    private fun log(
        exerciseId: Long,
        muscle: MuscleGroup,
        weightKg: Double,
        reps: Int,
        finishedAt: Long? = at("2026-09-11T12:00:00"),
        completed: Boolean = true,
        isWarmup: Boolean = false,
    ) = SessionSetLog(
        exerciseId = exerciseId,
        muscle = muscle,
        weightKg = weightKg,
        reps = reps,
        finishedAt = finishedAt,
        completed = completed,
        isWarmup = isWarmup,
    )
}
