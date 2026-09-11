package com.bool.gymtracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bool.gymtracker.data.seed.ExerciseSeed
import com.bool.gymtracker.data.seed.ProgramSeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        SessionEntity::class,
        SessionExerciseEntity::class,
        SessionSetEntity::class,
        SettingsEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun sessionDao(): SessionDao
    abstract fun sessionExerciseDao(): SessionExerciseDao
    abstract fun sessionSetDao(): SessionSetDao
    abstract fun settingsDao(): SettingsDao

    fun seedIfNeeded(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            if (exerciseDao().count() == 0) {
                exerciseDao().insertAll(ExerciseSeed.builtIn())
            } else {
                val existing = exerciseDao().listAll().map { it.name.lowercase() }.toSet()
                val missing = ExerciseSeed.builtIn().filter { it.name.lowercase() !in existing }
                if (missing.isNotEmpty()) exerciseDao().insertAll(missing)
            }
            if (workoutDao().countBuiltIn() == 0) {
                ProgramSeed.insertBuiltIns(this@AppDatabase)
            }
            if (settingsDao().get() == null) {
                settingsDao().upsert(SettingsEntity())
            }
        }
    }

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workouts ADD COLUMN isBuiltIn INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE workouts ADD COLUMN programKey TEXT")
                db.execSQL("ALTER TABLE workouts ADD COLUMN sortIndex INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ExerciseSeed.builtIn().forEach { exercise ->
                    db.execSQL(
                        "UPDATE exercises SET muscleGroup = ? WHERE name = ? AND isCustom = 0",
                        arrayOf(exercise.muscleGroup, exercise.name),
                    )
                }
                db.execSQL("UPDATE exercises SET muscleGroup = 'ABS' WHERE muscleGroup = 'CORE'")
                db.execSQL("UPDATE exercises SET muscleGroup = 'OTHER' WHERE muscleGroup IN ('PUSH', 'PULL')")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE session_exercises ADD COLUMN muscleGroupSnapshot TEXT NOT NULL DEFAULT 'OTHER'",
                )
                db.execSQL(
                    """
                    UPDATE session_exercises
                    SET muscleGroupSnapshot = COALESCE(
                        (SELECT muscleGroup FROM exercises WHERE exercises.id = session_exercises.exerciseId),
                        'OTHER'
                    )
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercises ADD COLUMN coverageRegion TEXT")
                db.execSQL("ALTER TABLE session_exercises ADD COLUMN coverageRegionSnapshot TEXT")
                ExerciseSeed.builtIn().forEach { exercise ->
                    db.execSQL(
                        "UPDATE exercises SET coverageRegion = ? WHERE name = ? AND isCustom = 0",
                        arrayOf(exercise.coverageRegion, exercise.name),
                    )
                }
                db.execSQL(
                    """
                    UPDATE session_exercises
                    SET coverageRegionSnapshot = (
                        SELECT coverageRegion FROM exercises WHERE exercises.id = session_exercises.exerciseId
                    )
                    """.trimIndent(),
                )
            }
        }

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "gym-tracker.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
    }
}
