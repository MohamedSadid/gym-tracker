package com.bool.gymtracker

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bool.gymtracker.data.local.AppDatabase
import com.bool.gymtracker.data.repository.ActiveSet
import com.bool.gymtracker.data.repository.ExerciseRepository
import com.bool.gymtracker.data.repository.SessionRepository
import com.bool.gymtracker.data.repository.WorkoutRepository
import com.bool.gymtracker.data.seed.ExerciseSeed
import com.bool.gymtracker.domain.MuscleGroup
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class V1WorkoutFlowTest {
    private lateinit var db: AppDatabase
    private lateinit var exercises: ExerciseRepository
    private lateinit var workouts: WorkoutRepository
    private lateinit var sessions: SessionRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        runBlocking { db.exerciseDao().insertAll(ExerciseSeed.builtIn()) }
        exercises = ExerciseRepository(db)
        workouts = WorkoutRepository(db)
        sessions = SessionRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createWorkoutLogSetFinishHistoryAndProgress() = runBlocking {
        val workoutId = workouts.create("Push A")
        val bench = exercises.observeAll().first().first { it.name == "Barbell bench press" }
        workouts.addExercise(workoutId, bench.id)

        val detail = workouts.observeDetail(workoutId).first()
        assertEquals("Push A", detail?.name)
        assertEquals(1, detail?.exercises?.size)

        val sessionId = sessions.startFromWorkout(workoutId)
        val active = sessions.loadActive(sessionId)!!
        assertEquals(1, active.exercises.first().sets.size)
        val firstSet = active.exercises.first().sets.first()
        sessions.saveSet(
            ActiveSet(
                id = firstSet.id,
                sessionExerciseId = firstSet.sessionExerciseId,
                setIndex = firstSet.setIndex,
                reps = 5,
                weightKg = 80.0,
                isWarmup = false,
                completed = true,
            ),
        )
        sessions.finish(sessionId)

        val history = sessions.observeHistory().first()
        assertEquals(1, history.size)
        assertEquals("Push A", history.first().workoutName)
        assertEquals(1, history.first().setCount)

        val progress = sessions.progression(bench.id)!!
        assertEquals(80.0, progress.lastTopSet!!.weightKg, 0.0)
        assertEquals(5, progress.lastTopSet!!.reps)
        assertEquals(1, progress.points.size)
    }

    @Test
    fun renameBlankKeepsCurrentName() = runBlocking {
        val workoutId = workouts.create()
        workouts.rename(workoutId, "")
        assertEquals("New workout", workouts.observeDetail(workoutId).first()?.name)
        workouts.rename(workoutId, "Push A")
        workouts.rename(workoutId, "   ")
        assertEquals("Push A", workouts.observeDetail(workoutId).first()?.name)
    }

    @Test
    fun sessionCanAddAndRemoveSets() = runBlocking {
        val workoutId = workouts.create("Push A")
        val bench = exercises.observeAll().first().first { it.name == "Barbell bench press" }
        workouts.addExercise(workoutId, bench.id)
        val sessionId = sessions.startFromWorkout(workoutId)
        val exerciseId = sessions.loadActive(sessionId)!!.exercises.first().id
        sessions.addSet(exerciseId)
        val withTwo = sessions.loadActive(sessionId)!!.exercises.first().sets
        assertEquals(2, withTwo.size)
        sessions.removeSet(withTwo.last().id, exerciseId)
        assertEquals(1, sessions.loadActive(sessionId)!!.exercises.first().sets.size)
    }

    @Test
    fun cancelSessionIsNotSaved() = runBlocking {
        val workoutId = workouts.create("Pull")
        val row = exercises.observeAll().first().first { it.name == "Barbell row" }
        workouts.addExercise(workoutId, row.id)
        val sessionId = sessions.startFromWorkout(workoutId)
        sessions.cancel(sessionId)
        assertTrue(sessions.observeHistory().first().isEmpty())
        assertEquals(null, sessions.loadActive(sessionId))
    }

    @Test
    fun customExerciseRejectsDuplicateName() = runBlocking {
        val first = exercises.addCustom("Cable fly", MuscleGroup.PUSH)
        assertTrue(first.isSuccess)
        val second = exercises.addCustom("cable fly", MuscleGroup.PUSH)
        assertTrue(second.isFailure)
    }
}
