package com.bool.gymtracker.di

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.bool.gymtracker.data.local.AppDatabase
import com.bool.gymtracker.data.repository.ExerciseRepository
import com.bool.gymtracker.data.repository.SessionRepository
import com.bool.gymtracker.data.repository.SettingsRepository
import com.bool.gymtracker.data.repository.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AppContainer(
    context: Context,
    database: AppDatabase? = null,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob())
    val database: AppDatabase = (database ?: AppDatabase.create(appContext)).also { it.seedIfNeeded(scope) }
    val exercises = ExerciseRepository(this.database)
    val workouts = WorkoutRepository(this.database)
    val sessions = SessionRepository(this.database)
    val settings = SettingsRepository(this.database)
    val restAlerter = RestAlerter(appContext)
}

class RestAlerter(private val context: Context) {
    fun ping() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 250, 120, 250), -1))
        runCatching {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
                .startTone(ToneGenerator.TONE_PROP_BEEP, 500)
        }
    }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}

@Composable
inline fun <reified VM : ViewModel> appViewModel(
    crossinline create: (AppContainer) -> VM,
): VM {
    val container = LocalAppContainer.current
    return viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return create(container) as T
            }
        },
    )
}
