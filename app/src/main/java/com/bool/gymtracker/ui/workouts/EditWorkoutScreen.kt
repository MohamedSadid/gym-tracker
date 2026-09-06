package com.bool.gymtracker.ui.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bool.gymtracker.data.repository.WorkoutRepository
import com.bool.gymtracker.di.LocalAppContainer
import com.bool.gymtracker.domain.WorkoutDetail
import com.bool.gymtracker.ui.components.GymCard
import com.bool.gymtracker.ui.components.PrimaryButton
import com.bool.gymtracker.ui.theme.GymBlack
import com.bool.gymtracker.ui.theme.GymDanger
import com.bool.gymtracker.ui.theme.GymMuted
import com.bool.gymtracker.ui.theme.GymText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditWorkoutViewModel(
    private val workoutId: Long,
    private val workouts: WorkoutRepository,
) : ViewModel() {
    val detail = workouts.observeDetail(workoutId)
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkoutDetail(workoutId, "", emptyList()))

    fun rename(name: String) {
        viewModelScope.launch { workouts.rename(workoutId, name) }
    }

    fun remove(itemId: Long) {
        viewModelScope.launch { workouts.removeExercise(itemId, workoutId) }
    }

    fun move(itemId: Long, delta: Int) {
        viewModelScope.launch { workouts.moveExercise(workoutId, itemId, delta) }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            workouts.delete(workoutId)
            onDeleted()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutScreen(
    workoutId: Long,
    onBack: () -> Unit,
    onAddExercise: () -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: EditWorkoutViewModel = viewModel(
        key = "edit-$workoutId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EditWorkoutViewModel(workoutId, container.workouts) as T
        },
    )
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    var confirmDelete by rememberDeleteState()

    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = { Text("Edit workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = GymText)
                    }
                },
                actions = {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = GymDanger)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = detail.name,
                onValueChange = viewModel::rename,
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(detail.exercises, key = { _, item -> item.workoutExerciseId }) { index, item ->
                    GymCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleMedium)
                                Text(item.muscleGroup.label, color = GymMuted)
                            }
                            IconButton(onClick = { viewModel.move(item.workoutExerciseId, -1) }, enabled = index > 0) {
                                Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = "Move up")
                            }
                            IconButton(onClick = { viewModel.move(item.workoutExerciseId, 1) }, enabled = index < detail.exercises.lastIndex) {
                                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Move down")
                            }
                            IconButton(onClick = { viewModel.remove(item.workoutExerciseId) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = GymDanger)
                            }
                        }
                    }
                }
            }
            PrimaryButton("Add exercise", onAddExercise)
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete workout?") },
            text = { Text("Past sessions stay in history.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(onBack) }) { Text("Delete", color = GymDanger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun rememberDeleteState() = androidx.compose.runtime.remember { mutableStateOf(false) }
