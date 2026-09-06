package com.bool.gymtracker.ui.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bool.gymtracker.data.repository.ExerciseRepository
import com.bool.gymtracker.data.repository.WorkoutRepository
import com.bool.gymtracker.di.LocalAppContainer
import com.bool.gymtracker.domain.WorkoutExerciseItem
import com.bool.gymtracker.ui.components.GymCard
import com.bool.gymtracker.ui.components.PrimaryButton
import com.bool.gymtracker.ui.theme.GymBlack
import com.bool.gymtracker.ui.theme.GymDanger
import com.bool.gymtracker.ui.theme.GymLime
import com.bool.gymtracker.ui.theme.GymMuted
import com.bool.gymtracker.ui.theme.GymText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class EditWorkoutUiState(
    val name: String = "",
    val exercises: List<WorkoutExerciseItem> = emptyList(),
    val dirty: Boolean = false,
    val confirmLeave: Boolean = false,
    val confirmDelete: Boolean = false,
)

class EditWorkoutViewModel(
    private val workoutId: Long,
    private val workouts: WorkoutRepository,
    private val exercises: ExerciseRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(EditWorkoutUiState())
    val state = _state.asStateFlow()

    private var savedName = ""
    private var savedExercises: List<WorkoutExerciseItem> = emptyList()

    private val persistLock = Mutex()

    init {
        viewModelScope.launch {
            workouts.observeDetail(workoutId).filterNotNull().collect { detail ->
                savedName = detail.name
                savedExercises = detail.exercises
                if (!_state.value.dirty) {
                    _state.update { it.copy(name = detail.name, exercises = detail.exercises) }
                }
            }
        }
    }

    fun setName(name: String) {
        _state.update { it.copy(name = name, dirty = true) }
    }

    suspend fun addExercise(exerciseId: Long) {
        if (_state.value.exercises.any { it.exerciseId == exerciseId }) return
        val ex = exercises.get(exerciseId) ?: return
        _state.update { ui ->
            ui.copy(
                dirty = true,
                exercises = ui.exercises + WorkoutExerciseItem(
                    workoutExerciseId = 0,
                    exerciseId = ex.id,
                    name = ex.name,
                    muscleGroup = ex.muscleGroup,
                    position = ui.exercises.size,
                ),
            )
        }
    }

    fun remove(exerciseId: Long) {
        _state.update { ui ->
            ui.copy(
                dirty = true,
                exercises = ui.exercises.filter { it.exerciseId != exerciseId }
                    .mapIndexed { index, item -> item.copy(position = index) },
            )
        }
    }

    fun move(exerciseId: Long, delta: Int) {
        _state.update { ui ->
            val rows = ui.exercises.toMutableList()
            val index = rows.indexOfFirst { it.exerciseId == exerciseId }
            val target = index + delta
            if (index < 0 || target !in rows.indices) return@update ui
            val moved = rows.removeAt(index)
            rows.add(target, moved)
            ui.copy(
                dirty = true,
                exercises = rows.mapIndexed { i, item -> item.copy(position = i) },
            )
        }
    }

    fun save(onSaved: (() -> Unit)? = null) {
        viewModelScope.launch {
            persistLock.withLock { persistNow() }
            onSaved?.invoke()
        }
    }

    fun leave(onBack: () -> Unit) {
        viewModelScope.launch {
            persistLock.withLock { }
            if (_state.value.dirty) askLeave(true) else onBack()
        }
    }

    private suspend fun persistNow() {
        val current = _state.value
        val name = current.name.trim().ifBlank { savedName }
        workouts.rename(workoutId, name)
        workouts.replaceExercises(workoutId, current.exercises.map { it.exerciseId })
        _state.update { it.copy(name = name, dirty = false, confirmLeave = false) }
    }

    fun discard(onDiscarded: (() -> Unit)? = null) {
        _state.update {
            it.copy(name = savedName, exercises = savedExercises, dirty = false, confirmLeave = false)
        }
        onDiscarded?.invoke()
    }

    fun askLeave(show: Boolean) = _state.update { it.copy(confirmLeave = show) }
    fun askDelete(show: Boolean) = _state.update { it.copy(confirmDelete = show) }

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
    pendingExerciseId: Long,
    onPendingConsumed: () -> Unit,
    onBack: () -> Unit,
    onAddExercise: (List<Long>) -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: EditWorkoutViewModel = viewModel(
        key = "edit-$workoutId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EditWorkoutViewModel(workoutId, container.workouts, container.exercises) as T
        },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var nameFocused by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf("") }

    LaunchedEffect(pendingExerciseId) {
        if (pendingExerciseId > 0L) {
            viewModel.addExercise(pendingExerciseId)
            onPendingConsumed()
        }
    }

    fun commitName() {
        if (!nameFocused) return
        nameFocused = false
        focusManager.clearFocus()
        val next = nameDraft.trim().ifBlank { state.name }
        if (next != state.name) viewModel.setName(next)
    }

    fun leave() {
        commitName()
        viewModel.leave(onBack)
    }

    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = { Text("Edit workout") },
                navigationIcon = {
                    IconButton(onClick = { leave() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = GymText)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            nameFocused = false
                            viewModel.discard()
                        },
                        enabled = state.dirty,
                    ) {
                        Text("Cancel", color = if (state.dirty) GymMuted else GymMuted.copy(alpha = 0.4f))
                    }
                    TextButton(
                        onClick = {
                            commitName()
                            viewModel.save()
                        },
                        enabled = state.dirty,
                    ) {
                        Text("Save", color = if (state.dirty) GymLime else GymMuted.copy(alpha = 0.4f))
                    }
                    IconButton(onClick = { viewModel.askDelete(true) }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = GymDanger)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = if (nameFocused) nameDraft else state.name,
                onValueChange = { nameDraft = it },
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focus ->
                        if (focus.isFocused) {
                            if (!nameFocused) {
                                nameFocused = true
                                nameDraft = ""
                            }
                        } else if (nameFocused) {
                            nameFocused = false
                            val next = nameDraft.trim().ifBlank { state.name }
                            if (next != state.name) viewModel.setName(next)
                        }
                    },
            )
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(state.exercises, key = { _, item -> item.exerciseId }) { index, item ->
                    GymCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleMedium)
                                Text(item.muscleGroup.label, color = GymMuted)
                            }
                            IconButton(onClick = { viewModel.move(item.exerciseId, -1) }, enabled = index > 0) {
                                Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = "Move up")
                            }
                            IconButton(
                                onClick = { viewModel.move(item.exerciseId, 1) },
                                enabled = index < state.exercises.lastIndex,
                            ) {
                                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Move down")
                            }
                            IconButton(onClick = { viewModel.remove(item.exerciseId) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = GymDanger)
                            }
                        }
                    }
                }
            }
            PrimaryButton("Add exercise", { onAddExercise(state.exercises.map { it.exerciseId }) })
        }
    }

    if (state.confirmLeave) {
        AlertDialog(
            onDismissRequest = { viewModel.askLeave(false) },
            title = { Text("Save changes?") },
            text = { Text("Cancel discards edits and keeps the last saved workout.") },
            confirmButton = {
                TextButton(onClick = {
                    commitName()
                    viewModel.save(onBack)
                }) { Text("Save", color = GymLime) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.discard(onBack) }) { Text("Cancel") }
            },
        )
    }

    if (state.confirmDelete) {
        AlertDialog(
            onDismissRequest = { viewModel.askDelete(false) },
            title = { Text("Delete workout?") },
            text = { Text("Past sessions stay in history.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(onBack) }) { Text("Delete", color = GymDanger) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.askDelete(false) }) { Text("Cancel") }
            },
        )
    }
}
