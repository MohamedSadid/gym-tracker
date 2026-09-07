package com.bool.gymtracker.domain

enum class MuscleGroup(val label: String) {
    PUSH("Push"),
    PULL("Pull"),
    LEGS("Legs"),
    CORE("Core"),
    OTHER("Other"),
}

data class Exercise(
    val id: Long,
    val name: String,
    val muscleGroup: MuscleGroup,
    val isCustom: Boolean,
)

data class WorkoutSummary(
    val id: Long,
    val name: String,
    val exerciseCount: Int,
)

data class WorkoutExerciseItem(
    val workoutExerciseId: Long,
    val exerciseId: Long,
    val name: String,
    val muscleGroup: MuscleGroup,
    val position: Int,
)

data class WorkoutDetail(
    val id: Long,
    val name: String,
    val isBuiltIn: Boolean,
    val exercises: List<WorkoutExerciseItem>,
)

data class BuiltInProgram(
    val key: String,
    val title: String,
    val blurb: String,
)

data class HistorySession(
    val id: Long,
    val workoutName: String,
    val startedAt: Long,
    val finishedAt: Long,
    val durationMs: Long,
    val setCount: Int,
)

data class SessionSetDetail(
    val setIndex: Int,
    val reps: Int,
    val weightKg: Double,
    val isWarmup: Boolean,
)

data class SessionExerciseDetail(
    val name: String,
    val sets: List<SessionSetDetail>,
)

data class SessionDetail(
    val id: Long,
    val workoutName: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val exercises: List<SessionExerciseDetail>,
)

data class ProgressionPoint(
    val finishedAt: Long,
    val weightKg: Double,
    val reps: Int,
)

data class ExerciseProgression(
    val exercise: Exercise,
    val lastTopSet: ProgressionPoint?,
    val points: List<ProgressionPoint>,
)
