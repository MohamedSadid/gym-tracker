package com.bool.gymtracker.domain.performance

import com.bool.gymtracker.domain.CoverageRegion
import com.bool.gymtracker.domain.MuscleGroup
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class SessionSetLog(
    val exerciseId: Long,
    val muscle: MuscleGroup,
    val weightKg: Double,
    val reps: Int,
    val finishedAt: Long?,
    val completed: Boolean,
    val isWarmup: Boolean,
    val sessionId: Long = 0,
    val region: CoverageRegion? = null,
)

data class WorkingSetLog(
    val exerciseId: Long,
    val muscle: MuscleGroup,
    val weightKg: Double,
    val reps: Int,
    val finishedAt: Long,
    val sessionId: Long = 0,
    val region: CoverageRegion? = null,
)

data class WeeklyExerciseVolume(
    val weekStart: LocalDate,
    val exerciseId: Long,
    val muscle: MuscleGroup,
    val smallVolume: Double,
)

data class WeeklyMuscleVolume(
    val weekStart: LocalDate,
    val muscle: MuscleGroup,
    val totalVolume: Double,
)

fun setVolume(weightKg: Double, reps: Int): Double = weightKg * reps

fun smallVolume(sets: List<WorkingSetLog>): Double =
    sets.sumOf { setVolume(it.weightKg, it.reps) }

fun workingSetLogs(rows: List<SessionSetLog>): List<WorkingSetLog> =
    rows.mapNotNull { row ->
        val finishedAt = row.finishedAt ?: return@mapNotNull null
        if (!row.completed || row.isWarmup) return@mapNotNull null
        WorkingSetLog(
            exerciseId = row.exerciseId,
            muscle = row.muscle,
            weightKg = row.weightKg,
            reps = row.reps,
            finishedAt = finishedAt,
            sessionId = row.sessionId,
            region = row.region,
        )
    }

fun weekMonday(finishedAt: Long, zone: ZoneId): LocalDate =
    Instant.ofEpochMilli(finishedAt)
        .atZone(zone)
        .toLocalDate()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

fun weeklyExerciseVolumes(
    rows: List<SessionSetLog>,
    zone: ZoneId,
): List<WeeklyExerciseVolume> =
    workingSetLogs(rows)
        .groupBy { Triple(weekMonday(it.finishedAt, zone), it.exerciseId, it.muscle) }
        .map { (key, group) ->
            WeeklyExerciseVolume(
                weekStart = key.first,
                exerciseId = key.second,
                muscle = key.third,
                smallVolume = smallVolume(group),
            )
        }
        .sortedWith(compareBy({ it.weekStart }, { it.exerciseId }, { it.muscle }))

fun weeklyMuscleVolumes(
    rows: List<SessionSetLog>,
    zone: ZoneId,
): List<WeeklyMuscleVolume> =
    weeklyExerciseVolumes(rows, zone)
        .groupBy { it.weekStart to it.muscle }
        .map { (key, group) ->
            WeeklyMuscleVolume(
                weekStart = key.first,
                muscle = key.second,
                totalVolume = group.sumOf { it.smallVolume },
            )
        }
        .sortedWith(compareBy({ it.weekStart }, { it.muscle }))
