package com.bool.gymtracker.domain.performance

import com.bool.gymtracker.domain.CoverageRegion
import com.bool.gymtracker.domain.MuscleGroup
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class MuscleTarget(
    val muscle: MuscleGroup,
    val setTarget: Int,
    val sessionTarget: Int = 2,
    val regionTargets: Map<CoverageRegion, Int> = emptyMap(),
)

val coverageTargets: List<MuscleTarget> = listOf(
    MuscleTarget(MuscleGroup.CHEST, setTarget = 10, sessionTarget = 2),
    MuscleTarget(MuscleGroup.BACK, setTarget = 10, sessionTarget = 2),
    MuscleTarget(MuscleGroup.ABS, setTarget = 4, sessionTarget = 2),
    MuscleTarget(
        MuscleGroup.SHOULDERS,
        setTarget = 12,
        sessionTarget = 2,
        regionTargets = mapOf(
            CoverageRegion.LATERAL to 6,
            CoverageRegion.REAR to 3,
            CoverageRegion.FRONT to 3,
        ),
    ),
    MuscleTarget(
        MuscleGroup.ARMS,
        setTarget = 18,
        sessionTarget = 2,
        regionTargets = mapOf(
            CoverageRegion.BICEPS to 6,
            CoverageRegion.TRICEPS to 6,
            CoverageRegion.FLEXORS to 3,
            CoverageRegion.EXTENSORS to 3,
        ),
    ),
    MuscleTarget(
        MuscleGroup.LEGS,
        setTarget = 24,
        sessionTarget = 2,
        regionTargets = mapOf(
            CoverageRegion.QUADS to 8,
            CoverageRegion.HAMSTRINGS to 8,
            CoverageRegion.CALVES to 8,
        ),
    ),
)

data class MuscleCoverage(
    val weekStart: LocalDate,
    val muscle: MuscleGroup,
    val sessionCount: Int,
    val setCount: Int,
    val sessionTarget: Int,
    val setTarget: Int,
    val regionCounts: Map<CoverageRegion, Int>,
    val regionTargets: Map<CoverageRegion, Int>,
    val applyRegions: Boolean,
) {
    val sessionsOk: Boolean get() = sessionCount >= sessionTarget
    val setsOk: Boolean get() = setCount >= setTarget
    val regionsOk: Boolean
        get() = !applyRegions || regionTargets.all { (region, target) ->
            (regionCounts[region] ?: 0) >= target
        }
    val covered: Boolean get() = sessionsOk && setsOk && regionsOk
}

fun lastEndedWeekStart(today: LocalDate): LocalDate =
    today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1)

fun coverageForWeek(
    rows: List<SessionSetLog>,
    zone: ZoneId,
    weekStart: LocalDate,
): List<MuscleCoverage> {
    val working = workingSetLogs(rows).filter { weekMonday(it.finishedAt, zone) == weekStart }
    return coverageTargets.map { target ->
        val sets = working.filter { it.muscle == target.muscle }
        val applyRegions = target.regionTargets.isNotEmpty() && sets.any { it.region != null }
        MuscleCoverage(
            weekStart = weekStart,
            muscle = target.muscle,
            sessionCount = sets.map { it.sessionId }.toSet().size,
            setCount = sets.size,
            sessionTarget = target.sessionTarget,
            setTarget = target.setTarget,
            regionCounts = target.regionTargets.keys.associateWith { region ->
                sets.count { it.region == region }
            },
            regionTargets = target.regionTargets,
            applyRegions = applyRegions,
        )
    }
}

fun coverageShortfallsForEndedWeek(
    rows: List<SessionSetLog>,
    zone: ZoneId,
    today: LocalDate,
): List<MuscleCoverage> =
    coverageForWeek(rows, zone, lastEndedWeekStart(today)).filter { !it.covered }

fun formatCoverageShortfall(coverage: MuscleCoverage): String {
    val parts = mutableListOf(
        "${coverage.muscle.label} ${coverage.setCount}/${coverage.setTarget}",
        "${coverage.sessionCount}/${coverage.sessionTarget} sessions",
    )
    if (coverage.applyRegions) {
        val shortRegions = coverage.regionTargets.mapNotNull { (region, target) ->
            val count = coverage.regionCounts[region] ?: 0
            if (count < target) "${region.reportLabel} $count/$target" else null
        }
        if (shortRegions.isNotEmpty()) parts += shortRegions.joinToString(", ")
    }
    return parts.joinToString(" — ")
}
