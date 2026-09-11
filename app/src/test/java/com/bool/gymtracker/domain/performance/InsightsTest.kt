package com.bool.gymtracker.domain.performance

import com.bool.gymtracker.domain.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class InsightsTest {
    private val zone = ZoneId.of("Europe/London")
    private val today = LocalDate.of(2026, 9, 11)
    private val week1 = at("2026-08-19T12:00:00")
    private val week2 = at("2026-08-26T12:00:00")
    private val week3 = at("2026-09-02T12:00:00")

    @Test
    fun noBandUntilThreeWeeksWithVolume() {
        val rows = chest(week1, 100.0, 5) + chest(week3, 100.0, 5)
        assertTrue(insightsForEndedWeeks(rows, zone, today).isEmpty())
        assertNull(trendBand(100.0, 0.0, 100.0))
    }

    @Test
    fun flatWhenAllWeeksWithinTwoPercentOfAverage() {
        assertEquals(TrendBand.FLAT, trendBand(100.0, 101.0, 99.0))
        val rows = chest(week1, 100.0, 5) + chest(week2, 100.0, 5) + chest(week3, 100.0, 5)
        val insight = insightsForEndedWeeks(rows, zone, today).single { it.muscle == MuscleGroup.CHEST }
        assertEquals(TrendBand.FLAT, insight.band)
        assertTrue(insight.belowCoverage)
        assertTrue(formatInsight(insight).contains("weak"))
    }

    @Test
    fun risingNeedsTenPercentBothSteps() {
        assertEquals(TrendBand.RISING, trendBand(100.0, 110.0, 121.0))
        assertNull(trendBand(100.0, 110.0, 115.0))
        val same = chest(week1, 100.0, 5, exerciseId = 1) +
            chest(week2, 110.0, 5, exerciseId = 1) +
            chest(week3, 121.0, 5, exerciseId = 1)
        val sameInsight = insightsForEndedWeeks(same, zone, today).single { it.muscle == MuscleGroup.CHEST }
        assertEquals(TrendBand.RISING, sameInsight.band)
        assertTrue(formatInsight(sameInsight).contains("8–12"))
        val changed = chest(week1, 100.0, 5, exerciseId = 1) +
            chest(week2, 110.0, 5, exerciseId = 2) +
            chest(week3, 121.0, 5, exerciseId = 2)
        val changedInsight = insightsForEndedWeeks(changed, zone, today).single { it.muscle == MuscleGroup.CHEST }
        assertTrue(changedInsight.liftsChanged)
        assertTrue(formatInsight(changedInsight).contains("lifts changed"))
    }

    @Test
    fun fallingIsTwelvePercentOrWorseVsPreviousWeek() {
        assertEquals(TrendBand.FALLING, trendBand(100.0, 100.0, 88.0))
        assertNull(trendBand(100.0, 100.0, 89.0))
        val rows = chest(week1, 100.0, 5) + chest(week2, 100.0, 5) + chest(week3, 80.0, 5)
        val insight = insightsForEndedWeeks(rows, zone, today).single { it.muscle == MuscleGroup.CHEST }
        assertEquals(TrendBand.FALLING, insight.band)
        assertTrue(formatInsight(insight).contains("dropped"))
        assertTrue(formatInsight(insight).contains("deload"))
    }

    @Test
    fun lastWeekVolumeIgnoresCurrentWeek() {
        val rows = chest(week3, 80.0, 5) + chest(at("2026-09-10T12:00:00"), 200.0, 10)
        val volumes = lastWeekVolumes(rows, zone, today)
        assertEquals(400.0, volumes.single { it.muscle == MuscleGroup.CHEST }.totalVolume, 0.0)
    }

    private fun at(local: String): Long =
        LocalDateTime.parse(local).atZone(zone).toInstant().toEpochMilli()

    private fun chest(
        finishedAt: Long,
        weightKg: Double,
        reps: Int,
        exerciseId: Long = 1,
        sessionId: Long = 1,
    ) = listOf(
        SessionSetLog(
            exerciseId = exerciseId,
            muscle = MuscleGroup.CHEST,
            weightKg = weightKg,
            reps = reps,
            finishedAt = finishedAt,
            completed = true,
            isWarmup = false,
            sessionId = sessionId,
        ),
    )
}
