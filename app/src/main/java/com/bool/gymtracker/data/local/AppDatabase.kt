package com.bool.gymtracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bool.gymtracker.data.seed.ExerciseSeed
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
    version = 1,
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
            if (settingsDao().get() == null) {
                settingsDao().upsert(SettingsEntity())
            }
        }
    }

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "gym-tracker.db")
                .build()
    }
}
