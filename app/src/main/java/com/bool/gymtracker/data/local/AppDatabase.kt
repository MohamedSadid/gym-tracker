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
    version = 2,
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

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "gym-tracker.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
