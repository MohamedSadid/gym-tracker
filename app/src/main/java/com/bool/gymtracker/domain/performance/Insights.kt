package com.bool.gymtracker.domain.performance

import com.bool.gymtracker.domain.MuscleGroup
import java.time.LocalDate
import java.time.ZoneId

enum class TrendBand { FLAT, RISING, FALLING }

data class MuscleInsight(
    val muscle: MuscleGroup,
    val band: TrendBand,
    val week1: Double,
    val week2: Double,
    val week3: Double,
    val liftsChanged: Boolean,
    val belowCoverage: Boolean,
)

data class ProgressEngineReport(
    val volumes: List<WeeklyMuscleVolume>,
    val shortfalls: List<MuscleCoverage>,
    val insights: List<MuscleInsight>,
)

fun threeEndedWeekStarts(today: LocalDate): List<LocalDate> {
    val last = lastEndedWeekStart(today)
    return listOf(last.minusWeeks(2), last.minusWeeks(1), last)
}

fun lastWeekVolumes(
    rows: List<SessionSetLog>,
    zone: ZoneId,
    today: LocalDate,
): List<WeeklyMuscleVolume> {
    val week = lastEndedWeekStart(today)
    val byMuscle = weeklyMuscleVolumes(rows, zone)
        .filter { it.weekStart == week && it.totalVolume > 0 }
        .associateBy { it.muscle }
    val order = coverageTargets.map { it.muscle } + MuscleGroup.OTHER
    return order.mapNotNull { byMuscle[it] }
}

fun insightsForEndedWeeks(
    rows: List<SessionSetLog>,
    zone: ZoneId,
    today: LocalDate,
): List<MuscleInsight> {
    val weeks = threeEndedWeekStarts(today)
    val lastCoverage = coverageForWeek(rows, zone, weeks[2]).associateBy { it.muscle }
    val muscles = (coverageTargets.map { it.muscle } + MuscleGroup.OTHER).distinct()
    return muscles.mapNotNull { muscle ->
        val volumes = weeks.map { week ->
            weeklyMuscleVolumes(rows, zone)
                .find { it.weekStart == week && it.muscle == muscle }
                ?.totalVolume ?: 0.0
        }
        if (volumes.any { it <= 0.0 }) return@mapNotNull null
        val band = trendBand(volumes[0], volumes[1], volumes[2]) ?: return@mapNotNull null
        val below = lastCoverage[muscle]?.covered == false
        MuscleInsight(
            muscle = muscle,
            band = band,
            week1 = volumes[0],
            week2 = volumes[1],
            week3 = volumes[2],
            liftsChanged = liftsChangedAcrossWeeks(rows, zone, weeks, muscle),
            belowCoverage = below,
        )
    }
}

fun progressEngineReport(
    rows: List<SessionSetLog>,
    zone: ZoneId,
    today: LocalDate,
) = ProgressEngineReport(
    volumes = lastWeekVolumes(rows, zone, today),
    shortfalls = coverageShortfallsForEndedWeek(rows, zone, today),
    insights = insightsForEndedWeeks(rows, zone, today),
)

fun trendBand(week1: Double, week2: Double, week3: Double): TrendBand? {
    if (week1 <= 0.0 || week2 <= 0.0 || week3 <= 0.0) return null
    val average = (week1 + week2 + week3) / 3.0
    val flat = listOf(week1, week2, week3).all { volume ->
        kotlin.math.abs(volume - average) <= average * 0.02 + 1e-9
    }
    if (flat) return TrendBand.FLAT
    val rising = percentChange(week2, week1) >= 0.10 && percentChange(week3, week2) >= 0.10
    if (rising) return TrendBand.RISING
    if (percentChange(week3, week2) <= -0.12) return TrendBand.FALLING
    return null
}

private fun percentChange(current: Double, previous: Double): Double =
    (current - previous) / previous

fun formatVolumeLine(volume: WeeklyMuscleVolume): String =
    "${volume.muscle.label} ${volumeLabel(volume.totalVolume)}"

fun formatInsight(insight: MuscleInsight): String {
    val name = insight.muscle.label
    return when (insight.band) {
        TrendBand.FLAT -> if (insight.belowCoverage) {
            "$name volume is flat — this muscle is weak. Start with it first if you want to improve it, or add an extra set with lower weight."
        } else {
            "$name volume has not changed for 3 weeks. An extra light set is optional."
        }
        TrendBand.RISING -> if (insight.liftsChanged) {
            "$name volume went up, but the lifts changed."
        } else {
            "$name volume is rising. Consider a weight you can control for about 8–12 reps."
        }
        TrendBand.FALLING ->
            "$name volume dropped. That can be missed sessions or a deload — not a cue to cut weight."
    }
}

private fun volumeLabel(value: Double): String {
    val text = if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
    return "$text volume"
}

private fun liftsChangedAcrossWeeks(
    rows: List<SessionSetLog>,
    zone: ZoneId,
    weeks: List<LocalDate>,
    muscle: MuscleGroup,
): Boolean {
    val ids = weeks.map { week ->
        workingSetLogs(rows)
            .filter { weekMonday(it.finishedAt, zone) == week && it.muscle == muscle }
            .map { it.exerciseId }
            .toSet()
    }
    return ids.distinct().size > 1
}
