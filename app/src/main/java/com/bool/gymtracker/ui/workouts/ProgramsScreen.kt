package com.bool.gymtracker.ui.workouts

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
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bool.gymtracker.data.repository.WorkoutRepository
import com.bool.gymtracker.data.seed.ProgramSeed
import com.bool.gymtracker.di.LocalAppContainer
import com.bool.gymtracker.domain.BuiltInProgram
import com.bool.gymtracker.domain.WorkoutSummary
import com.bool.gymtracker.ui.components.GymCard
import com.bool.gymtracker.ui.components.PrimaryButton
import com.bool.gymtracker.ui.theme.GymBlack
import com.bool.gymtracker.ui.theme.GymLime
import com.bool.gymtracker.ui.theme.GymMuted
import com.bool.gymtracker.ui.theme.GymText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsHubScreen(
    onPrograms: () -> Unit,
    onCustomize: () -> Unit,
    onSettings: () -> Unit,
) {
    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = { Text("Workouts") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = GymMuted)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GymCard(Modifier.fillMaxWidth().clickable(onClick = onPrograms)) {
                Text("Programs", style = MaterialTheme.typography.titleLarge)
                Text("Ready-made splits you can start or copy.", color = GymMuted)
            }
            GymCard(Modifier.fillMaxWidth().clickable(onClick = onCustomize)) {
                Text("Customize", style = MaterialTheme.typography.titleLarge)
                Text("Your workouts — create, edit, and start.", color = GymMuted)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramsScreen(
    onBack: () -> Unit,
    onOpenProgram: (String) -> Unit,
) {
    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = { Text("Programs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = GymText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(ProgramSeed.programs, key = { it.key }) { program ->
                GymCard(Modifier.fillMaxWidth().clickable { onOpenProgram(program.key) }) {
                    Text(program.title, style = MaterialTheme.typography.titleLarge)
                    Text(program.blurb, color = GymMuted)
                }
            }
        }
    }
}

class ProgramDetailViewModel(
    programKey: String,
    private val workouts: WorkoutRepository,
    private val startSession: suspend (Long) -> Long,
) : ViewModel() {
    val program: BuiltInProgram? = ProgramSeed.definition(programKey)
    val days: StateFlow<List<WorkoutSummary>> = workouts.observeProgramDays(programKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun start(workoutId: Long, onStarted: (Long) -> Unit) {
        viewModelScope.launch { onStarted(startSession(workoutId)) }
    }

    fun copy(onCopied: () -> Unit) {
        val key = program?.key ?: return
        viewModelScope.launch {
            workouts.copyProgram(key)
            onCopied()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramDetailScreen(
    programKey: String,
    onBack: () -> Unit,
    onCopied: () -> Unit,
    onStartSession: (Long) -> Unit,
) {
    val container = LocalAppContainer.current
    val viewModel: ProgramDetailViewModel = viewModel(
        key = "program-$programKey",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProgramDetailViewModel(programKey, container.workouts, container.sessions::startFromWorkout) as T
        },
    )
    val days by viewModel.days.collectAsStateWithLifecycle()
    val program = viewModel.program
    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = { Text(program?.title ?: "Program") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = GymText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(program?.blurb.orEmpty(), color = GymMuted, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                PrimaryButton("Copy to Customize", { viewModel.copy(onCopied) })
            }
            items(days, key = { it.id }) { day ->
                ProgramDayCard(
                    workout = day,
                    onStart = { viewModel.start(day.id, onStartSession) },
                )
            }
        }
    }
}

@Composable
private fun ProgramDayCard(
    workout: WorkoutSummary,
    onStart: () -> Unit,
) {
    val workouts = LocalAppContainer.current.workouts
    val detail by workouts.observeDetail(workout.id).collectAsStateWithLifecycle(initialValue = null)
    GymCard(Modifier.fillMaxWidth()) {
        Text(workout.name, style = MaterialTheme.typography.titleLarge)
        val names = detail?.exercises.orEmpty()
        Text(
            if (names.isEmpty()) "${workout.exerciseCount} exercises"
            else names.joinToString("\n") { it.name },
            color = GymMuted,
        )
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = GymLime, contentColor = GymBlack),
        ) { Text("Start") }
    }
}
