package com.bool.gymtracker

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bool.gymtracker.data.local.AppDatabase
import com.bool.gymtracker.data.repository.ActiveSet
import com.bool.gymtracker.data.repository.ExerciseRepository
import com.bool.gymtracker.data.repository.SessionRepository
import com.bool.gymtracker.data.repository.WorkoutRepository
import com.bool.gymtracker.data.seed.ExerciseSeed
import com.bool.gymtracker.data.seed.ProgramSeed
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
    fun addingSameExerciseTwiceIsRejected() = runBlocking {
        val workoutId = workouts.create("Legs")
        val squat = exercises.observeAll().first().first { it.name == "Back squat" }
        assertTrue(workouts.addExercise(workoutId, squat.id))
        assertEquals(false, workouts.addExercise(workoutId, squat.id))
        assertEquals(1, workouts.observeDetail(workoutId).first()?.exercises?.size)
    }

    @Test
    fun newSessionAlwaysStartsWithOneSet() = runBlocking {
        val workoutId = workouts.create("Legs")
        val squat = exercises.observeAll().first().first { it.name == "Back squat" }
        workouts.addExercise(workoutId, squat.id)
        val firstId = sessions.startFromWorkout(workoutId)
        val exerciseId = sessions.loadActive(firstId)!!.exercises.first().id
        sessions.addSet(exerciseId)
        val two = sessions.loadActive(firstId)!!.exercises.first().sets
        assertEquals(2, two.size)
        two.forEachIndexed { index, set ->
            sessions.saveSet(set.copy(reps = 5 + index, weightKg = 100.0, completed = true))
        }
        sessions.finish(firstId)

        val secondId = sessions.startFromWorkout(workoutId)
        val started = sessions.loadActive(secondId)!!.exercises.first().sets
        assertEquals(1, started.size)
        assertEquals(100.0, started.first().weightKg, 0.0)
        assertEquals(6, started.first().reps)
    }

    @Test
    fun deleteWorkoutKeepsHistory() = runBlocking {
        val workoutId = workouts.create("Push A")
        val bench = exercises.observeAll().first().first { it.name == "Barbell bench press" }
        workouts.addExercise(workoutId, bench.id)
        val sessionId = sessions.startFromWorkout(workoutId)
        val set = sessions.loadActive(sessionId)!!.exercises.first().sets.first()
        sessions.saveSet(set.copy(reps = 5, weightKg = 80.0, completed = true))
        sessions.finish(sessionId)
        workouts.delete(workoutId)
        assertTrue(workouts.observeSummaries().first().isEmpty())
        val history = sessions.observeHistory().first()
        assertEquals(1, history.size)
        assertEquals("Push A", history.first().workoutName)
    }

    @Test
    fun builtInProgramCannotBeEditedAndCopyGoesToCustomize() = runBlocking {
        ProgramSeed.insertBuiltIns(db)
        val days = workouts.observeProgramDays("full_body").first()
        assertEquals(3, days.size)
        val first = days.first()
        assertEquals("Full Body — Day A", first.name)
        assertEquals(6, first.exerciseCount)

        workouts.rename(first.id, "Hacked")
        workouts.delete(first.id)
        val fly = exercises.observeAll().first().first { it.name == "Pec deck fly" }
        assertEquals(false, workouts.addExercise(first.id, fly.id))
        assertEquals("Full Body — Day A", workouts.observeDetail(first.id).first()?.name)
        assertEquals(3, workouts.observeProgramDays("full_body").first().size)
        assertTrue(workouts.observeSummaries().first().isEmpty())

        workouts.copyWorkout(first.id)
        val custom = workouts.observeSummaries().first()
        assertEquals(1, custom.size)
        assertTrue(custom.first().name.endsWith("(copy)"))
        assertEquals(3, workouts.observeProgramDays("full_body").first().size)
    }

    @Test
    fun customExerciseRejectsDuplicateName() = runBlocking {
        val first = exercises.addCustom("Band chest fly", MuscleGroup.CHEST)
        assertTrue(first.isSuccess)
        val second = exercises.addCustom("band chest fly", MuscleGroup.CHEST)
        assertTrue(second.isFailure)
    }

    @Test
    fun builtInExercisesUsePrimaryMuscle() {
        val byName = ExerciseSeed.builtIn().associate { it.name to it.muscleGroup }
        assertEquals(MuscleGroup.CHEST.name, byName["Barbell bench press"])
        assertEquals(MuscleGroup.SHOULDERS.name, byName["Face pull"])
        assertEquals(MuscleGroup.ARMS.name, byName["Barbell curl"])
        assertEquals(MuscleGroup.ABS.name, byName["Plank"])
        assertEquals(MuscleGroup.ARMS.name, byName["Wrist extension"])
        assertEquals("EXTENSORS", ExerciseSeed.builtIn().first { it.name == "Reverse curl" }.coverageRegion)
        assertEquals("CALVES", ExerciseSeed.builtIn().first { it.name == "Seated calf raise" }.coverageRegion)
        assertTrue(byName.containsKey("Chest press machine"))
        assertTrue(byName.containsKey("Pec deck fly"))
        assertTrue(byName.containsKey("Hack squat"))
        assertEquals(66, byName.size)
        assertTrue(byName.values.none { it in setOf("PUSH", "PULL", "CORE") })
    }

    @Test
    fun sessionKeepsPrimaryMuscleIfLibraryRemaps() = runBlocking {
        val workoutId = workouts.create("Push A")
        val bench = exercises.observeAll().first().first { it.name == "Barbell bench press" }
        workouts.addExercise(workoutId, bench.id)
        val sessionId = sessions.startFromWorkout(workoutId)
        assertEquals(MuscleGroup.CHEST, sessions.detail(sessionId)!!.exercises.first().muscleGroup)

        val stored = db.exerciseDao().get(bench.id)!!
        db.exerciseDao().update(stored.copy(muscleGroup = MuscleGroup.BACK.name))
        assertEquals(MuscleGroup.BACK, exercises.get(bench.id)!!.muscleGroup)
        assertEquals(MuscleGroup.CHEST, sessions.detail(sessionId)!!.exercises.first().muscleGroup)
    }

    @Test
    fun sessionSnapshotsCoverageRegion() = runBlocking {
        val workoutId = workouts.create("Push A")
        val raise = exercises.observeAll().first().first { it.name == "Lateral raise" }
        workouts.addExercise(workoutId, raise.id)
        val sessionId = sessions.startFromWorkout(workoutId)
        val stored = db.sessionExerciseDao().forSession(sessionId).first()
        assertEquals("LATERAL", stored.coverageRegionSnapshot)
    }

    @Test
    fun weeklyVolumeUsesFinishedWorkingSetsOnly() = runBlocking {
        val zone = java.time.ZoneId.of("UTC")
        val workoutId = workouts.create("Push A")
        val all = exercises.observeAll().first()
        val bench = all.first { it.name == "Barbell bench press" }
        val fly = all.first { it.name == "Pec deck fly" }
        val row = all.first { it.name == "Barbell row" }
        workouts.addExercise(workoutId, bench.id)
        workouts.addExercise(workoutId, fly.id)
        workouts.addExercise(workoutId, row.id)

        val sessionId = sessions.startFromWorkout(workoutId)
        val active = sessions.loadActive(sessionId)!!
        val benchEx = active.exercises.first { it.exerciseId == bench.id }
        val flyEx = active.exercises.first { it.exerciseId == fly.id }
        val rowEx = active.exercises.first { it.exerciseId == row.id }
        sessions.saveSet(benchEx.sets.first().copy(reps = 5, weightKg = 80.0, completed = true))
        sessions.addSet(benchEx.id)
        val warmup = sessions.loadActive(sessionId)!!.exercises.first { it.exerciseId == bench.id }.sets.last()
        sessions.saveSet(warmup.copy(reps = 10, weightKg = 40.0, isWarmup = true, completed = true))
        sessions.saveSet(flyEx.sets.first().copy(reps = 10, weightKg = 20.0, completed = true))
        sessions.saveSet(rowEx.sets.first().copy(reps = 8, weightKg = 60.0, completed = true))
        sessions.finish(sessionId)

        val openId = sessions.startFromWorkout(workoutId)
        val openBench = sessions.loadActive(openId)!!.exercises.first { it.exerciseId == bench.id }
        sessions.saveSet(openBench.sets.first().copy(reps = 5, weightKg = 100.0, completed = true))

        val small = sessions.weeklyExerciseVolumes(zone)
        assertEquals(400.0, small.single { it.exerciseId == bench.id }.smallVolume, 0.0)
        assertEquals(200.0, small.single { it.exerciseId == fly.id }.smallVolume, 0.0)
        val totals = sessions.weeklyMuscleVolumes(zone)
        assertEquals(600.0, totals.single { it.muscle == MuscleGroup.CHEST }.totalVolume, 0.0)
        assertEquals(480.0, totals.single { it.muscle == MuscleGroup.BACK }.totalVolume, 0.0)
    }

    @Test
    fun finishKeepsIncompleteSetsOutOfVolume() = runBlocking {
        val zone = java.time.ZoneId.of("UTC")
        val workoutId = workouts.create("Push A")
        val bench = exercises.observeAll().first().first { it.name == "Barbell bench press" }
        workouts.addExercise(workoutId, bench.id)
        val sessionId = sessions.startFromWorkout(workoutId)
        val set = sessions.loadActive(sessionId)!!.exercises.first().sets.first()
        sessions.saveSet(set.copy(reps = 5, weightKg = 80.0, completed = false))
        sessions.finish(sessionId)
        assertEquals(1, sessions.observeHistory().first().size)
        assertTrue(sessions.weeklyMuscleVolumes(zone).none { it.muscle == MuscleGroup.CHEST })
        assertEquals(0, sessions.detail(sessionId)!!.exercises.first().sets.size)
    }
}
