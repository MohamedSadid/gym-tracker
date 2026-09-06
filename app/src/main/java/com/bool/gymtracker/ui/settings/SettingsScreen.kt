package com.bool.gymtracker.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bool.gymtracker.data.repository.SettingsRepository
import com.bool.gymtracker.di.appViewModel
import com.bool.gymtracker.ui.theme.GymBlack
import com.bool.gymtracker.ui.theme.GymMuted
import com.bool.gymtracker.ui.theme.GymText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settings: SettingsRepository) : ViewModel() {
    val rest = settings.observeRestSeconds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 90)

    fun setRest(seconds: Int) {
        viewModelScope.launch { settings.setRestSeconds(seconds) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = appViewModel { SettingsViewModel(it.settings) },
) {
    val rest by viewModel.rest.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = GymText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Default rest", color = GymText)
            Text("Used when you complete a set.", color = GymMuted, modifier = Modifier.padding(bottom = 12.dp))
            listOf(60, 90, 120, 180).forEach { seconds ->
                FilterChip(
                    selected = rest == seconds,
                    onClick = { viewModel.setRest(seconds) },
                    label = { Text("${seconds}s") },
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
    }
}
