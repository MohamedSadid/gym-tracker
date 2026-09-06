package com.bool.gymtracker.data.repository

import androidx.room.withTransaction
import com.bool.gymtracker.data.local.AppDatabase
import com.bool.gymtracker.data.local.ExerciseEntity
import com.bool.gymtracker.data.local.SessionEntity
import com.bool.gymtracker.data.local.SessionExerciseEntity
import com.bool.gymtracker.data.local.SessionSetEntity
import com.bool.gymtracker.data.local.SettingsEntity
import com.bool.gymtracker.data.local.WorkoutEntity
import com.bool.gymtracker.data.local.WorkoutExerciseEntity
import com.bool.gymtracker.domain.Exercise
import com.bool.gymtracker.domain.ExerciseProgression
import com.bool.gymtracker.domain.HistorySession
import com.bool.gymtracker.domain.MuscleGroup
import com.bool.gymtracker.domain.ProgressionPoint
import com.bool.gymtracker.domain.SessionDetail
import com.bool.gymtracker.domain.SessionExerciseDetail
import com.bool.gymtracker.domain.SessionSetDetail
import com.bool.gymtracker.domain.WorkoutDetail
import com.bool.gymtracker.domain.WorkoutExerciseItem
import com.bool.gymtracker.domain.WorkoutSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private fun muscleGroupOf(raw: String): MuscleGroup =
    MuscleGroup.entries.find { it.name == raw } ?: MuscleGroup.OTHER

private fun ExerciseEntity.toModel() = Exercise(
    id = id,
    name = name,
    muscleGroup = muscleGroupOf(muscleGroup),
    isCustom = isCustom,
)

class ExerciseRepository(private val db: AppDatabase) {
    private val dao = db.exerciseDao()

    fun observeFiltered(query: String, group: MuscleGroup?): Flow<List<Exercise>> =
        dao.observeFiltered(query.trim(), group?.name ?: "").map { list -> list.map { it.toModel() } }

    fun observeAll(): Flow<List<Exercise>> = dao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun get(id: Long): Exercise? = dao.get(id)?.toModel()

    suspend fun addCustom(name: String, group: MuscleGroup): Result<Long> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("Name is required"))
        if (dao.countByName(trimmed) > 0) {
            return Result.failure(IllegalArgumentException("An exercise with that name already exists"))
        }
        val id = dao.insert(ExerciseEntity(name = trimmed, muscleGroup = group.name, isCustom = true))
        return Result.success(id)
    }
}

class WorkoutRepository(private val db: AppDatabase) {
    private val workouts = db.workoutDao()
    private val items = db.workoutExerciseDao()
    private val exercises = db.exerciseDao()

    fun observeSummaries(): Flow<List<WorkoutSummary>> =
        workouts.observeSummaries().map { rows ->
            rows.map { WorkoutSummary(it.id, it.name, it.exerciseCount) }
        }

    fun observeDetail(workoutId: Long): Flow<WorkoutDetail?> =
        combine(workouts.observe(workoutId), items.observeForWorkout(workoutId), exercises.observeAll()) { workout, rows, all ->
            workout ?: return@combine null
            val byId = all.associateBy { it.id }
            WorkoutDetail(
                id = workout.id,
                name = workout.name,
                exercises = rows.mapNotNull { row ->
                    val ex = byId[row.exerciseId] ?: return@mapNotNull null
                    WorkoutExerciseItem(
                        workoutExerciseId = row.id,
                        exerciseId = ex.id,
                        name = ex.name,
                        muscleGroup = muscleGroupOf(ex.muscleGroup),
                        position = row.position,
                    )
                },
            )
        }

    suspend fun create(name: String = "New workout"): Long =
        workouts.insert(WorkoutEntity(name = name, createdAt = System.currentTimeMillis()))

    suspend fun rename(id: Long, name: String) {
        val current = workouts.get(id) ?: return
        workouts.update(current.copy(name = name.ifBlank { current.name }))
    }

    suspend fun delete(id: Long) = workouts.delete(id)

    suspend fun addExercise(workoutId: Long, exerciseId: Long): Boolean {
        val existing = items.forWorkout(workoutId)
        if (existing.any { it.exerciseId == exerciseId }) return false
        items.insert(
            WorkoutExerciseEntity(
                workoutId = workoutId,
                exerciseId = exerciseId,
                position = existing.size,
            ),
        )
        return true
    }

    suspend fun replaceExercises(workoutId: Long, exerciseIds: List<Long>) {
        db.withTransaction {
            items.deleteForWorkout(workoutId)
            exerciseIds.forEachIndexed { index, exerciseId ->
                items.insert(
                    WorkoutExerciseEntity(
                        workoutId = workoutId,
                        exerciseId = exerciseId,
                        position = index,
                    ),
                )
            }
        }
    }

    suspend fun removeExercise(workoutExerciseId: Long, workoutId: Long) {
        items.delete(workoutExerciseId)
        items.forWorkout(workoutId).sortedBy { it.position }.forEachIndexed { index, row ->
            items.updatePosition(row.id, index)
        }
    }

    suspend fun moveExercise(workoutId: Long, workoutExerciseId: Long, delta: Int) {
        val rows = items.forWorkout(workoutId).sortedBy { it.position }.toMutableList()
        val index = rows.indexOfFirst { it.id == workoutExerciseId }
        val target = index + delta
        if (index < 0 || target !in rows.indices) return
        val moved = rows.removeAt(index)
        rows.add(target, moved)
        rows.forEachIndexed { i, row -> items.updatePosition(row.id, i) }
    }
}

class SettingsRepository(private val db: AppDatabase) {
    private val dao = db.settingsDao()

    fun observeRestSeconds(): Flow<Int> = dao.observe().map { it?.defaultRestSeconds ?: 90 }

    suspend fun restSeconds(): Int = dao.get()?.defaultRestSeconds ?: 90

    suspend fun setRestSeconds(seconds: Int) {
        dao.upsert(SettingsEntity(defaultRestSeconds = seconds.coerceIn(15, 600)))
    }
}

class SessionRepository(private val db: AppDatabase) {
    private val sessions = db.sessionDao()
    private val sessionExercises = db.sessionExerciseDao()
    private val sets = db.sessionSetDao()
    private val workouts = db.workoutDao()
    private val workoutExercises = db.workoutExerciseDao()
    private val exercises = db.exerciseDao()

    suspend fun startFromWorkout(workoutId: Long): Long = db.withTransaction {
        val workout = workouts.get(workoutId) ?: error("Workout not found")
        val sessionId = sessions.insert(
            SessionEntity(
                workoutId = workout.id,
                workoutNameSnapshot = workout.name,
                startedAt = System.currentTimeMillis(),
                finishedAt = null,
            ),
        )
        workoutExercises.forWorkout(workoutId).sortedBy { it.position }.forEachIndexed { position, we ->
            val exercise = exercises.get(we.exerciseId)
            val seId = sessionExercises.insert(
                SessionExerciseEntity(
                    sessionId = sessionId,
                    exerciseId = we.exerciseId,
                    exerciseNameSnapshot = exercise?.name ?: "Exercise",
                    position = position,
                ),
            )
            val lastSessionId = sets.lastFinishedSessionId(we.exerciseId)
            val lastWorking = if (lastSessionId != null) {
                sets.setsForSessionExercise(lastSessionId, we.exerciseId)
                    .filter { it.completed && !it.isWarmup }
                    .lastOrNull()
            } else {
                null
            }
            sets.insert(
                SessionSetEntity(
                    sessionExerciseId = seId,
                    setIndex = 0,
                    reps = lastWorking?.reps ?: 0,
                    weightKg = lastWorking?.weightKg ?: 0.0,
                    isWarmup = false,
                    completed = false,
                ),
            )
        }
        sessionId
    }

    fun observeSession(sessionId: Long): Flow<ActiveSession?> =
        combine(
            sessions.observe(sessionId),
            sessionExercises.observeForSession(sessionId),
        ) { session, exerciseRows ->
            session to exerciseRows
        }.combine(
            // Re-read sets whenever exercises change; per-exercise flows would be more precise later.
            sessionExercises.observeForSession(sessionId),
        ) { pair, exerciseRows ->
            val session = pair.first ?: return@combine null
            ActiveSession(
                id = session.id,
                workoutName = session.workoutNameSnapshot,
                startedAt = session.startedAt,
                finishedAt = session.finishedAt,
                exercises = exerciseRows.map { row ->
                    ActiveExercise(
                        id = row.id,
                        exerciseId = row.exerciseId,
                        name = row.exerciseNameSnapshot,
                        sets = emptyList(),
                    )
                },
            )
        }

    suspend fun loadActive(sessionId: Long): ActiveSession? {
        val session = sessions.get(sessionId) ?: return null
        val exerciseRows = sessionExercises.forSession(sessionId)
        return ActiveSession(
            id = session.id,
            workoutName = session.workoutNameSnapshot,
            startedAt = session.startedAt,
            finishedAt = session.finishedAt,
            exercises = exerciseRows.map { row ->
                ActiveExercise(
                    id = row.id,
                    exerciseId = row.exerciseId,
                    name = row.exerciseNameSnapshot,
                    sets = sets.forExercise(row.id).map { it.toActive() },
                )
            },
        )
    }

    suspend fun lastTopSetLabel(exerciseId: Long): String? {
        val points = progressionPoints(exerciseId)
        val last = points.lastOrNull() ?: return null
        return "${formatWeight(last.weightKg)} kg × ${last.reps}"
    }

    suspend fun saveSet(set: ActiveSet) {
        sets.update(
            SessionSetEntity(
                id = set.id,
                sessionExerciseId = set.sessionExerciseId,
                setIndex = set.setIndex,
                reps = set.reps,
                weightKg = set.weightKg,
                isWarmup = set.isWarmup,
                completed = set.completed,
            ),
        )
    }

    suspend fun addSet(sessionExerciseId: Long) {
        val existing = sets.forExercise(sessionExerciseId)
        val last = existing.lastOrNull()
        sets.insert(
            SessionSetEntity(
                sessionExerciseId = sessionExerciseId,
                setIndex = existing.size,
                reps = last?.reps ?: 0,
                weightKg = last?.weightKg ?: 0.0,
                isWarmup = false,
                completed = false,
            ),
        )
    }

    suspend fun removeSet(setId: Long, sessionExerciseId: Long) {
        sets.delete(setId)
        sets.forExercise(sessionExerciseId).sortedBy { it.setIndex }.forEachIndexed { index, row ->
            sets.update(row.copy(setIndex = index))
        }
    }

    suspend fun finish(sessionId: Long) {
        sessions.finish(sessionId, System.currentTimeMillis())
    }

    suspend fun cancel(sessionId: Long) {
        sessions.delete(sessionId)
    }

    fun observeHistory(): Flow<List<HistorySession>> = sessions.observeHistory().map { list ->
        list.map { session ->
            HistorySession(
                id = session.id,
                workoutName = session.workoutNameSnapshot,
                startedAt = session.startedAt,
                finishedAt = session.finishedAt,
                durationMs = (session.finishedAt - session.startedAt).coerceAtLeast(0),
                setCount = session.setCount,
            )
        }
    }

    suspend fun detail(sessionId: Long): SessionDetail? {
        val session = sessions.get(sessionId) ?: return null
        val exerciseRows = sessionExercises.forSession(sessionId)
        return SessionDetail(
            id = session.id,
            workoutName = session.workoutNameSnapshot,
            startedAt = session.startedAt,
            finishedAt = session.finishedAt,
            exercises = exerciseRows.map { row ->
                SessionExerciseDetail(
                    name = row.exerciseNameSnapshot,
                    sets = sets.forExercise(row.id).filter { it.completed }.map {
                        SessionSetDetail(it.setIndex, it.reps, it.weightKg, it.isWarmup)
                    },
                )
            },
        )
    }

    suspend fun progression(exerciseId: Long): ExerciseProgression? {
        val exercise = exercises.get(exerciseId)?.toModel() ?: return null
        val points = progressionPoints(exerciseId)
        return ExerciseProgression(
            exercise = exercise,
            lastTopSet = points.lastOrNull(),
            points = points.takeLast(12),
        )
    }

    private suspend fun progressionPoints(exerciseId: Long): List<ProgressionPoint> {
        val rows = sets.completedHistoryForExercise(exerciseId).filter { !it.isWarmup }
        return rows.groupBy { it.sessionId }.map { (_, sessionSets) ->
            val top = sessionSets.maxWith(compareBy<com.bool.gymtracker.data.local.CompletedSetRow> { it.weightKg }.thenBy { it.reps })
            ProgressionPoint(top.finishedAt, top.weightKg, top.reps)
        }.sortedBy { it.finishedAt }
    }

    private fun SessionSetEntity.toActive() = ActiveSet(
        id = id,
        sessionExerciseId = sessionExerciseId,
        setIndex = setIndex,
        reps = reps,
        weightKg = weightKg,
        isWarmup = isWarmup,
        completed = completed,
    )
}

data class ActiveSession(
    val id: Long,
    val workoutName: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val exercises: List<ActiveExercise>,
)

data class ActiveExercise(
    val id: Long,
    val exerciseId: Long,
    val name: String,
    val sets: List<ActiveSet>,
    val lastTopSetLabel: String? = null,
)

data class ActiveSet(
    val id: Long,
    val sessionExerciseId: Long,
    val setIndex: Int,
    val reps: Int,
    val weightKg: Double,
    val isWarmup: Boolean,
    val completed: Boolean,
)

fun formatWeight(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
