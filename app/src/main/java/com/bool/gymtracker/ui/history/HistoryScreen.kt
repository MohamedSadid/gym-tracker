package com.bool.gymtracker.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bool.gymtracker.data.repository.SessionRepository
import com.bool.gymtracker.di.appViewModel
import com.bool.gymtracker.domain.HistorySession
import com.bool.gymtracker.domain.SessionDetail
import com.bool.gymtracker.ui.components.EmptyState
import com.bool.gymtracker.ui.components.GymCard
import com.bool.gymtracker.ui.components.formatDateTime
import com.bool.gymtracker.ui.components.formatDuration
import com.bool.gymtracker.data.repository.formatWeight
import com.bool.gymtracker.di.LocalAppContainer
import com.bool.gymtracker.ui.theme.GymAmber
import com.bool.gymtracker.ui.theme.GymBlack
import com.bool.gymtracker.ui.theme.GymMuted
import com.bool.gymtracker.ui.theme.GymText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(sessions: SessionRepository) : ViewModel() {
    val items = sessions.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onOpen: (Long) -> Unit,
    viewModel: HistoryViewModel = appViewModel { HistoryViewModel(it.sessions) },
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = { Text("History") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
            )
        },
    ) { padding ->
        if (items.isEmpty()) {
            EmptyState("No history yet", "Finished workouts will show up here.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items, key = { it.id }) { session ->
                    HistoryRow(session, onClick = { onOpen(session.id) })
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(session: HistorySession, onClick: () -> Unit) {
    GymCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Text(session.workoutName, style = MaterialTheme.typography.titleLarge)
        Text(formatDateTime(session.finishedAt), color = GymMuted)
        Text("${formatDuration(session.durationMs)} · ${session.setCount} sets", color = GymMuted)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(sessionId: Long, onBack: () -> Unit) {
    val sessions = LocalAppContainer.current.sessions
    val detail by produceState<SessionDetail?>(null, sessionId) {
        value = sessions.detail(sessionId)
    }
    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = { Text(detail?.workoutName ?: "Session") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = GymText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
            )
        },
    ) { padding ->
        val data = detail
        if (data == null) {
            Text("Loading…", modifier = Modifier.padding(padding).padding(16.dp), color = GymMuted)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                data.finishedAt?.let { item { Text(formatDateTime(it), color = GymMuted) } }
                items(data.exercises) { exercise ->
                    GymCard(Modifier.fillMaxWidth()) {
                        Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                        if (exercise.sets.isEmpty()) {
                            Text("No completed sets", color = GymMuted)
                        } else {
                            exercise.sets.forEach { set ->
                                val warmup = if (set.isWarmup) " warmup" else ""
                                Text(
                                    "Set ${set.setIndex + 1}: ${formatWeight(set.weightKg)} kg × ${set.reps}$warmup",
                                    color = if (set.isWarmup) GymAmber else GymText,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
