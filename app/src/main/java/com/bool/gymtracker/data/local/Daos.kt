package com.bool.gymtracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun get(id: Long): ExerciseEntity?

    @Query(
        """
        SELECT * FROM exercises
        WHERE (:query = '' OR name LIKE '%' || :query || '%')
          AND (:group = '' OR muscleGroup = :group)
        ORDER BY name COLLATE NOCASE
        """,
    )
    fun observeFiltered(query: String, group: String): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM exercises WHERE name = :name COLLATE NOCASE")
    suspend fun countByName(name: String): Int

    @Insert
    suspend fun insert(entity: ExerciseEntity): Long

    @Insert
    suspend fun insertAll(entities: List<ExerciseEntity>)
}

@Dao
interface WorkoutDao {
    @Query(
        """
        SELECT workouts.id AS id, workouts.name AS name,
               COUNT(workout_exercises.id) AS exerciseCount
        FROM workouts
        LEFT JOIN workout_exercises ON workout_exercises.workoutId = workouts.id
        GROUP BY workouts.id
        ORDER BY workouts.name COLLATE NOCASE
        """,
    )
    fun observeSummaries(): Flow<List<WorkoutSummaryRow>>

    @Query("SELECT * FROM workouts ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun observe(id: Long): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun get(id: Long): WorkoutEntity?

    @Query("SELECT COUNT(*) FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun exerciseCount(workoutId: Long): Int

    @Insert
    suspend fun insert(entity: WorkoutEntity): Long

    @Update
    suspend fun update(entity: WorkoutEntity)

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface WorkoutExerciseDao {
    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY position")
    fun observeForWorkout(workoutId: Long): Flow<List<WorkoutExerciseEntity>>

    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY position")
    suspend fun forWorkout(workoutId: Long): List<WorkoutExerciseEntity>

    @Insert
    suspend fun insert(entity: WorkoutExerciseEntity): Long

    @Query("DELETE FROM workout_exercises WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteForWorkout(workoutId: Long)

    @Query("UPDATE workout_exercises SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: Long, position: Int)
}

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(entity: SessionEntity): Long

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun observe(id: Long): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun get(id: Long): SessionEntity?

    @Query("UPDATE sessions SET finishedAt = :finishedAt WHERE id = :id")
    suspend fun finish(id: Long, finishedAt: Long)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query(
        """
        SELECT sessions.id AS id,
               sessions.workoutNameSnapshot AS workoutNameSnapshot,
               sessions.startedAt AS startedAt,
               sessions.finishedAt AS finishedAt,
               (
                   SELECT COUNT(*) FROM session_sets
                   INNER JOIN session_exercises ON session_exercises.id = session_sets.sessionExerciseId
                   WHERE session_exercises.sessionId = sessions.id
                     AND session_sets.completed = 1
               ) AS setCount
        FROM sessions
        WHERE sessions.finishedAt IS NOT NULL
        ORDER BY sessions.finishedAt DESC
        """,
    )
    fun observeHistory(): Flow<List<HistoryRow>>
}

data class HistoryRow(
    val id: Long,
    val workoutNameSnapshot: String,
    val startedAt: Long,
    val finishedAt: Long,
    val setCount: Int,
)

@Dao
interface SessionExerciseDao {
    @Insert
    suspend fun insert(entity: SessionExerciseEntity): Long

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY position")
    fun observeForSession(sessionId: Long): Flow<List<SessionExerciseEntity>>

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY position")
    suspend fun forSession(sessionId: Long): List<SessionExerciseEntity>
}

@Dao
interface SessionSetDao {
    @Insert
    suspend fun insert(entity: SessionSetEntity): Long

    @Update
    suspend fun update(entity: SessionSetEntity)

    @Query("SELECT * FROM session_sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY setIndex")
    fun observeForExercise(sessionExerciseId: Long): Flow<List<SessionSetEntity>>

    @Query("SELECT * FROM session_sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY setIndex")
    suspend fun forExercise(sessionExerciseId: Long): List<SessionSetEntity>

    @Query("DELETE FROM session_sets WHERE id = :id")
    suspend fun delete(id: Long)

    @Query(
        """
        SELECT COUNT(*) FROM session_sets
        INNER JOIN session_exercises ON session_exercises.id = session_sets.sessionExerciseId
        WHERE session_exercises.sessionId = :sessionId AND session_sets.completed = 1
        """,
    )
    suspend fun completedCount(sessionId: Long): Int

    @Query(
        """
        SELECT sessions.id AS sessionId,
               sessions.finishedAt AS finishedAt,
               session_sets.reps AS reps,
               session_sets.weightKg AS weightKg,
               session_sets.isWarmup AS isWarmup
        FROM session_sets
        INNER JOIN session_exercises ON session_exercises.id = session_sets.sessionExerciseId
        INNER JOIN sessions ON sessions.id = session_exercises.sessionId
        WHERE session_exercises.exerciseId = :exerciseId
          AND sessions.finishedAt IS NOT NULL
          AND session_sets.completed = 1
        ORDER BY sessions.finishedAt DESC, session_sets.setIndex
        """,
    )
    suspend fun completedHistoryForExercise(exerciseId: Long): List<CompletedSetRow>

    @Query(
        """
        SELECT sessions.id FROM sessions
        INNER JOIN session_exercises ON session_exercises.sessionId = sessions.id
        WHERE session_exercises.exerciseId = :exerciseId
          AND sessions.finishedAt IS NOT NULL
        ORDER BY sessions.finishedAt DESC
        LIMIT 1
        """,
    )
    suspend fun lastFinishedSessionId(exerciseId: Long): Long?

    @Query(
        """
        SELECT session_sets.* FROM session_sets
        INNER JOIN session_exercises ON session_exercises.id = session_sets.sessionExerciseId
        WHERE session_exercises.sessionId = :sessionId
          AND session_exercises.exerciseId = :exerciseId
        ORDER BY session_sets.setIndex
        """,
    )
    suspend fun setsForSessionExercise(sessionId: Long, exerciseId: Long): List<SessionSetEntity>
}

data class WorkoutSummaryRow(
    val id: Long,
    val name: String,
    val exerciseCount: Int,
)

data class CompletedSetRow(
    val sessionId: Long,
    val finishedAt: Long,
    val reps: Int,
    val weightKg: Double,
    val isWarmup: Boolean,
)

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun observe(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun get(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SettingsEntity)
}
