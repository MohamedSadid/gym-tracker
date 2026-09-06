package com.bool.gymtracker.ui.workouts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bool.gymtracker.data.repository.ExerciseRepository
import com.bool.gymtracker.di.LocalAppContainer
import com.bool.gymtracker.domain.Exercise
import com.bool.gymtracker.domain.MuscleGroup
import com.bool.gymtracker.ui.components.GymCard
import com.bool.gymtracker.ui.components.PrimaryButton
import com.bool.gymtracker.ui.theme.GymAmber
import com.bool.gymtracker.ui.theme.GymBlack
import com.bool.gymtracker.ui.theme.GymLime
import com.bool.gymtracker.ui.theme.GymMuted
import com.bool.gymtracker.ui.theme.GymSurfaceHigh
import com.bool.gymtracker.ui.theme.GymText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisePickerViewModel(
    private val exercises: ExerciseRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val group = MutableStateFlow<MuscleGroup?>(null)
    var error by mutableStateOf<String?>(null)
        private set
    var notice by mutableStateOf<String?>(null)
        private set
    var noticeNonce by mutableIntStateOf(0)
        private set

    val selectedGroup = group.asStateFlow()

    val items = combine(query, group) { q, g -> q to g }
        .flatMapLatest { (q, g) -> exercises.observeFiltered(q, g) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        query.value = value
    }

    fun setGroup(value: MuscleGroup?) {
        group.value = if (group.value == value) null else value
    }

    fun add(exerciseId: Long, alreadyAdded: Set<Long>, onAdded: (Long) -> Unit) {
        if (exerciseId in alreadyAdded) {
            showAlreadyAdded()
            return
        }
        onAdded(exerciseId)
    }

    fun createCustom(name: String, group: MuscleGroup, alreadyAdded: Set<Long>, onAdded: (Long) -> Unit) {
        viewModelScope.launch {
            exercises.addCustom(name, group)
                .onSuccess { id ->
                    error = null
                    add(id, alreadyAdded, onAdded)
                }
                .onFailure { error = it.message }
        }
    }

    fun clearNotice() {
        notice = null
    }

    private fun showAlreadyAdded() {
        notice = "This exercise is already added"
        noticeNonce++
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerScreen(
    workoutId: Long,
    alreadyAddedIds: List<Long>,
    onBack: () -> Unit,
    onPicked: (Long) -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: ExercisePickerViewModel = viewModel(
        key = "picker-$workoutId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ExercisePickerViewModel(container.exercises) as T
        },
    )
    val exercises by viewModel.items.collectAsStateWithLifecycle()
    val selectedGroup by viewModel.selectedGroup.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }
    val alreadyAdded = remember(alreadyAddedIds) { alreadyAddedIds.toSet() }

    LaunchedEffect(viewModel.noticeNonce) {
        if (viewModel.notice == null) return@LaunchedEffect
        delay(3_000)
        viewModel.clearNotice()
    }

    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = { Text("Add exercise") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = GymText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.setQuery(it)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search") },
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MuscleGroup.entries) { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { viewModel.setGroup(group) },
                        label = { Text(group.label, color = if (selectedGroup == group) GymBlack else GymText) },
                        colors = groupChipColors(),
                    )
                }
            }
            if (viewModel.notice != null) {
                Text(viewModel.notice!!, color = GymAmber, style = MaterialTheme.typography.bodyMedium)
            }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(exercises, key = { it.id }) { exercise ->
                    ExerciseRow(exercise) { viewModel.add(exercise.id, alreadyAdded, onPicked) }
                }
            }
            PrimaryButton("Create custom", { showCreate = true })
        }
    }

    if (showCreate) {
        CustomExerciseDialog(
            error = viewModel.error,
            onDismiss = { showCreate = false },
            onCreate = { name, group -> viewModel.createCustom(name, group, alreadyAdded, onPicked) },
        )
    }
}

@Composable
private fun groupChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = GymSurfaceHigh,
    labelColor = GymText,
    selectedContainerColor = GymLime,
    selectedLabelColor = GymBlack,
)

@Composable
private fun ExerciseRow(exercise: Exercise, onClick: () -> Unit) {
    GymCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Text(exercise.name, style = MaterialTheme.typography.titleMedium)
        Text(exercise.muscleGroup.label, color = GymMuted)
    }
}

@Composable
private fun CustomExerciseDialog(
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (String, MuscleGroup) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf(MuscleGroup.OTHER) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MuscleGroup.entries) { item ->
                        FilterChip(
                            selected = group == item,
                            onClick = { group = item },
                            label = { Text(item.label, color = if (group == item) GymBlack else GymText) },
                            colors = groupChipColors(),
                        )
                    }
                }
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = { onCreate(name, group) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
