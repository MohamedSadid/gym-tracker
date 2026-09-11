package com.bool.gymtracker.domain.performance

import com.bool.gymtracker.domain.CoverageRegion
import com.bool.gymtracker.domain.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class CoverageTest {
    private val zone = ZoneId.of("Europe/London")
    private val today = LocalDate.of(2026, 9, 11)
    private val lastWeek = at("2026-09-03T12:00:00")
    private val thisWeek = at("2026-09-10T12:00:00")

    @Test
    fun everyRequiredMuscleNeedsTwoSessions() {
        assertTrue(coverageTargets.all { it.sessionTarget == 2 })
        assertEquals(
            setOf(
                MuscleGroup.CHEST,
                MuscleGroup.BACK,
                MuscleGroup.SHOULDERS,
                MuscleGroup.ARMS,
                MuscleGroup.LEGS,
                MuscleGroup.ABS,
            ),
            coverageTargets.map { it.muscle }.toSet(),
        )
    }

    @Test
    fun lastEndedWeekIsPreviousMondayToSunday() {
        assertEquals(LocalDate.of(2026, 8, 31), lastEndedWeekStart(today))
    }

    @Test
    fun emptyLastWeekReportsAllRequiredMuscles() {
        val short = coverageShortfallsForEndedWeek(emptyList(), zone, today)
        assertEquals(coverageTargets.map { it.muscle }, short.map { it.muscle })
        assertTrue(short.none { it.muscle == MuscleGroup.OTHER })
    }

    @Test
    fun warmupAndUnfinishedDoNotCount() {
        val rows = listOf(
            log(MuscleGroup.CHEST, lastWeek, sessionId = 1, completed = false),
            log(MuscleGroup.CHEST, lastWeek, sessionId = 1, isWarmup = true),
            log(MuscleGroup.CHEST, null, sessionId = 1),
        )
        val chest = coverageForWeek(rows, zone, lastEndedWeekStart(today)).single { it.muscle == MuscleGroup.CHEST }
        assertEquals(0, chest.setCount)
        assertEquals(0, chest.sessionCount)
    }

    @Test
    fun twoSessionsRequiredEvenWhenSetCountIsMet() {
        val rows = (1..10).map { log(MuscleGroup.CHEST, lastWeek, sessionId = 1) }
        val chest = coverageForWeek(rows, zone, lastEndedWeekStart(today)).single { it.muscle == MuscleGroup.CHEST }
        assertTrue(chest.setsOk)
        assertFalse(chest.sessionsOk)
        assertFalse(chest.covered)
    }

    @Test
    fun chestCoveredWithTwoSessionsAndTenSets() {
        val rows = (1..5).map { log(MuscleGroup.CHEST, lastWeek, sessionId = 1) } +
            (1..5).map { log(MuscleGroup.CHEST, lastWeek, sessionId = 2) }
        val chest = coverageForWeek(rows, zone, lastEndedWeekStart(today)).single { it.muscle == MuscleGroup.CHEST }
        assertTrue(chest.covered)
        assertTrue(coverageShortfallsForEndedWeek(rows, zone, today).none { it.muscle == MuscleGroup.CHEST })
    }

    @Test
    fun thisWeekDoesNotCountTowardEndedWeekReport() {
        val rows = (1..10).map { log(MuscleGroup.CHEST, thisWeek, sessionId = 1) } +
            (1..10).map { log(MuscleGroup.CHEST, thisWeek, sessionId = 2) }
        assertTrue(coverageShortfallsForEndedWeek(rows, zone, today).any { it.muscle == MuscleGroup.CHEST })
    }

    @Test
    fun unclassifiedLegsSkipRegionCheck() {
        val rows = (1..12).map { log(MuscleGroup.LEGS, lastWeek, sessionId = 1) } +
            (1..12).map { log(MuscleGroup.LEGS, lastWeek, sessionId = 2) }
        val legs = coverageForWeek(rows, zone, lastEndedWeekStart(today)).single { it.muscle == MuscleGroup.LEGS }
        assertFalse(legs.applyRegions)
        assertTrue(legs.covered)
    }

    @Test
    fun shouldersShortWhenRearDeltsBelowTarget() {
        val rows = listOf(
            *Array(6) { log(MuscleGroup.SHOULDERS, lastWeek, 1, CoverageRegion.LATERAL) },
            *Array(3) { log(MuscleGroup.SHOULDERS, lastWeek, 1, CoverageRegion.FRONT) },
            log(MuscleGroup.SHOULDERS, lastWeek, 1, CoverageRegion.REAR),
            *Array(6) { log(MuscleGroup.SHOULDERS, lastWeek, 2, CoverageRegion.LATERAL) },
            *Array(3) { log(MuscleGroup.SHOULDERS, lastWeek, 2, CoverageRegion.FRONT) },
            log(MuscleGroup.SHOULDERS, lastWeek, 2, CoverageRegion.REAR),
        )
        val shoulders = coverageForWeek(rows, zone, lastEndedWeekStart(today))
            .single { it.muscle == MuscleGroup.SHOULDERS }
        assertTrue(shoulders.setsOk)
        assertTrue(shoulders.sessionsOk)
        assertFalse(shoulders.regionsOk)
        assertEquals(
            "Shoulders 20/12 — 2/2 sessions — rear delts 2/3",
            formatCoverageShortfall(shoulders),
        )
    }

    @Test
    fun otherNeverAppearsInCoverage() {
        val rows = (1..20).map { log(MuscleGroup.OTHER, lastWeek, sessionId = 1) }
        assertTrue(coverageForWeek(rows, zone, lastEndedWeekStart(today)).none { it.muscle == MuscleGroup.OTHER })
    }

    private fun at(local: String): Long =
        LocalDateTime.parse(local).atZone(zone).toInstant().toEpochMilli()

    private fun log(
        muscle: MuscleGroup,
        finishedAt: Long?,
        sessionId: Long = 1,
        region: CoverageRegion? = null,
        completed: Boolean = true,
        isWarmup: Boolean = false,
    ) = SessionSetLog(
        exerciseId = 1,
        muscle = muscle,
        weightKg = 20.0,
        reps = 10,
        finishedAt = finishedAt,
        completed = completed,
        isWarmup = isWarmup,
        sessionId = sessionId,
        region = region,
    )
}
