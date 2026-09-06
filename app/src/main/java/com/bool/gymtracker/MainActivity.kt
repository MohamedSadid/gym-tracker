package com.bool.gymtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.bool.gymtracker.di.LocalAppContainer
import com.bool.gymtracker.ui.GymTrackerRoot
import com.bool.gymtracker.ui.theme.GymTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as GymTrackerApp
        setContent {
            CompositionLocalProvider(LocalAppContainer provides app.container) {
                GymTrackerTheme {
                    GymTrackerRoot()
                }
            }
        }
    }
}
