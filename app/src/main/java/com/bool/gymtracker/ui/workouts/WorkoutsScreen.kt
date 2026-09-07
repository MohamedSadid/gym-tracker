package com.bool.gymtracker.ui.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bool.gymtracker.data.repository.WorkoutRepository
import com.bool.gymtracker.di.appViewModel
import com.bool.gymtracker.domain.WorkoutSummary
import com.bool.gymtracker.ui.components.EmptyState
import com.bool.gymtracker.ui.components.GymCard
import com.bool.gymtracker.ui.theme.GymBlack
import com.bool.gymtracker.ui.theme.GymDanger
import com.bool.gymtracker.ui.theme.GymLime
import com.bool.gymtracker.ui.theme.GymMuted
import com.bool.gymtracker.ui.theme.GymText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutsViewModel(
    private val workouts: WorkoutRepository,
    private val startSession: suspend (Long) -> Long,
) : ViewModel() {
    val items: StateFlow<List<WorkoutSummary>> = workouts.observeSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun create(onCreated: (Long) -> Unit) {
        viewModelScope.launch { onCreated(workouts.create()) }
    }

    fun start(workoutId: Long, onStarted: (Long) -> Unit) {
        viewModelScope.launch { onStarted(startSession(workoutId)) }
    }

    fun delete(workoutId: Long) {
        viewModelScope.launch { workouts.delete(workoutId) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    onOpenWorkout: (Long) -> Unit,
    onStartSession: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: WorkoutsViewModel = appViewModel { WorkoutsViewModel(it.workouts, it.sessions::startFromWorkout) },
) {
    val workouts by viewModel.items.collectAsStateWithLifecycle()
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = { Text("Customize") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = GymText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.create(onOpenWorkout) }, containerColor = GymLime, contentColor = GymBlack) {
                Icon(Icons.Outlined.Add, contentDescription = "New workout")
            }
        },
    ) { padding ->
        if (workouts.isEmpty()) {
            EmptyState(
                title = "No workouts yet",
                body = "Create a workout to log your first session.",
                actionLabel = "Create workout",
                onAction = { viewModel.create(onOpenWorkout) },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(workouts, key = { it.id }) { workout ->
                    WorkoutCard(
                        workout = workout,
                        onEdit = { onOpenWorkout(workout.id) },
                        onStart = { viewModel.start(workout.id, onStartSession) },
                        onDelete = { pendingDeleteId = workout.id },
                    )
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    val deleteTarget = pendingDeleteId
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Delete workout?") },
            text = { Text("Past sessions stay in history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(deleteTarget)
                        pendingDeleteId = null
                    },
                ) { Text("Delete", color = GymDanger) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun WorkoutCard(
    workout: WorkoutSummary,
    onEdit: () -> Unit,
    onStart: () -> Unit,
    onDelete: () -> Unit,
) {
    GymCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(workout.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    "${workout.exerciseCount} exercise${if (workout.exerciseCount == 1) "" else "s"}",
                    color = GymMuted,
                )
                Row {
                    TextButton(onClick = onEdit) { Text("Edit", color = GymLime) }
                    TextButton(onClick = onDelete) { Text("Delete", color = GymDanger) }
                }
            }
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = GymLime, contentColor = GymBlack),
            ) { Text("Start") }
        }
    }
}
