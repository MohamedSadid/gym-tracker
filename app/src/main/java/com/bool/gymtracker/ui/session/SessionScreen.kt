package com.bool.gymtracker.ui.session

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bool.gymtracker.data.repository.ActiveSet
import com.bool.gymtracker.data.repository.SessionRepository
import com.bool.gymtracker.data.repository.SettingsRepository
import com.bool.gymtracker.data.repository.formatWeight
import com.bool.gymtracker.domain.RestTimerState
import com.bool.gymtracker.di.LocalAppContainer
import com.bool.gymtracker.di.RestAlerter
import com.bool.gymtracker.ui.components.GymCard
import com.bool.gymtracker.ui.components.PrimaryButton
import com.bool.gymtracker.ui.components.formatDuration
import com.bool.gymtracker.ui.theme.GymAmber
import com.bool.gymtracker.ui.theme.GymBlack
import com.bool.gymtracker.ui.theme.GymDanger
import com.bool.gymtracker.ui.theme.GymLime
import com.bool.gymtracker.ui.theme.GymMuted
import com.bool.gymtracker.ui.theme.GymSurfaceHigh
import com.bool.gymtracker.ui.theme.GymText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SetDraft(
    val id: Long,
    val sessionExerciseId: Long,
    val setIndex: Int,
    val weightText: String,
    val repsText: String,
    val isWarmup: Boolean,
    val completed: Boolean,
)

data class ExerciseDraft(
    val id: Long,
    val name: String,
    val lastLabel: String?,
    val sets: List<SetDraft>,
)

data class SessionUiState(
    val workoutName: String = "",
    val startedAt: Long = 0L,
    val elapsedMs: Long = 0L,
    val exercises: List<ExerciseDraft> = emptyList(),
    val restLeft: Int = 0,
    val restTotal: Int = 90,
    val restRunning: Boolean = false,
    val restDone: Boolean = false,
    val confirmCancel: Boolean = false,
    val confirmFinish: Boolean = false,
    val confirmLeave: Boolean = false,
)

class SessionViewModel(
    private val sessionId: Long,
    private val sessions: SessionRepository,
    private val settings: SettingsRepository,
    private val alerter: RestAlerter,
) : ViewModel() {
    private val _state = MutableStateFlow(SessionUiState())
    val state = _state.asStateFlow()
    private var restJob: Job? = null
    private var clockJob: Job? = null

    private var rest = RestTimerState.start(90).pause()

    init {
        viewModelScope.launch {
            reload()
            val rest = settings.restSeconds()
            _state.update { it.copy(restTotal = rest) }
            clockJob = viewModelScope.launch {
                while (isActive) {
                    val started = _state.value.startedAt
                    if (started > 0) {
                        _state.update { ui -> ui.copy(elapsedMs = System.currentTimeMillis() - started) }
                    }
                    delay(1000)
                }
            }
        }
    }

    private suspend fun reload() {
        val active = sessions.loadActive(sessionId) ?: return
        val drafts = active.exercises.map { ex ->
            ExerciseDraft(
                id = ex.id,
                name = ex.name,
                lastLabel = sessions.lastTopSetLabel(ex.exerciseId),
                sets = ex.sets.map { it.toDraft() },
            )
        }
        _state.update {
            it.copy(workoutName = active.workoutName, startedAt = active.startedAt, exercises = drafts)
        }
    }

    fun onWeight(setId: Long, text: String) {
        if (!text.matches(Regex("""^\d*\.?\d*$"""))) return
        patchSet(setId) { it.copy(weightText = text) }
        persist(setId)
    }

    fun onReps(setId: Long, text: String) {
        if (!text.matches(Regex("""^\d*$"""))) return
        patchSet(setId) { it.copy(repsText = text) }
        persist(setId)
    }

    fun onWarmup(setId: Long, value: Boolean) {
        patchSet(setId) { it.copy(isWarmup = value) }
        persist(setId)
    }

    fun completeSet(setId: Long) {
        patchSet(setId) { it.copy(completed = true) }
        persist(setId)
        startRest()
    }

    fun addSet(exerciseId: Long) {
        viewModelScope.launch {
            sessions.addSet(exerciseId)
            reload()
        }
    }

    fun removeSet(setId: Long, exerciseId: Long) {
        viewModelScope.launch {
            sessions.removeSet(setId, exerciseId)
            reload()
        }
    }

    fun startRest(seconds: Int? = null) {
        restJob?.cancel()
        rest = RestTimerState.start(seconds ?: _state.value.restTotal)
        publishRest()
        restJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!rest.running) break
                rest = rest.tick()
                publishRest()
                if (rest.finished) {
                    alerter.ping()
                    break
                }
            }
        }
    }

    fun pauseRest() {
        rest = rest.pause()
        restJob?.cancel()
        publishRest()
    }

    fun resumeRest() {
        rest = rest.resume()
        if (rest.running) startRest(rest.remainingSeconds)
    }

    fun skipRest() {
        restJob?.cancel()
        rest = rest.skip()
        publishRest()
    }

    fun addRest(seconds: Int) {
        rest = rest.addSeconds(seconds)
        if (rest.running) startRest(rest.remainingSeconds) else publishRest()
    }

    private fun publishRest() {
        _state.update {
            it.copy(
                restLeft = rest.remainingSeconds,
                restTotal = rest.totalSeconds,
                restRunning = rest.running,
                restDone = rest.finished,
            )
        }
    }

    fun askCancel(show: Boolean) = _state.update { it.copy(confirmCancel = show) }
    fun askFinish(show: Boolean) = _state.update { it.copy(confirmFinish = show) }
    fun askLeave(show: Boolean) = _state.update { it.copy(confirmLeave = show) }

    fun cancel(onDone: () -> Unit) {
        viewModelScope.launch {
            sessions.cancel(sessionId)
            onDone()
        }
    }

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            persistAll()
            sessions.finish(sessionId)
            onDone()
        }
    }

    private fun persist(setId: Long) {
        viewModelScope.launch {
            val set = _state.value.exercises.flatMap { it.sets }.find { it.id == setId } ?: return@launch
            sessions.saveSet(set.toActive())
        }
    }

    private suspend fun persistAll() {
        _state.value.exercises.flatMap { it.sets }.forEach { sessions.saveSet(it.toActive()) }
    }

    private fun patchSet(setId: Long, transform: (SetDraft) -> SetDraft) {
        _state.update { ui ->
            ui.copy(
                exercises = ui.exercises.map { ex ->
                    ex.copy(sets = ex.sets.map { set -> if (set.id == setId) transform(set) else set })
                },
            )
        }
    }

    private fun ActiveSet.toDraft() = SetDraft(
        id = id,
        sessionExerciseId = sessionExerciseId,
        setIndex = setIndex,
        weightText = if (weightKg == 0.0) "" else formatWeight(weightKg),
        repsText = if (reps == 0) "" else reps.toString(),
        isWarmup = isWarmup,
        completed = completed,
    )

    private fun SetDraft.toActive() = ActiveSet(
        id = id,
        sessionExerciseId = sessionExerciseId,
        setIndex = setIndex,
        reps = repsText.toIntOrNull() ?: 0,
        weightKg = weightText.toDoubleOrNull() ?: 0.0,
        isWarmup = isWarmup,
        completed = completed,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(sessionId: Long, onExit: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: SessionViewModel = viewModel(
        key = "session-$sessionId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SessionViewModel(sessionId, container.sessions, container.settings, container.restAlerter) as T
        },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { /* keep composition subscribed */ }
    BackHandler { viewModel.askLeave(true) }

    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.workoutName.ifBlank { "Session" })
                        Text(formatDuration(state.elapsedMs), style = MaterialTheme.typography.bodyMedium, color = GymMuted)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.askCancel(true) }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Cancel", tint = GymText)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.askFinish(true) }) { Text("Finish", color = GymLime) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (state.restRunning || state.restDone || state.restLeft > 0) {
                RestBar(
                    left = state.restLeft,
                    running = state.restRunning,
                    done = state.restDone,
                    onPause = viewModel::pauseRest,
                    onResume = viewModel::resumeRest,
                    onSkip = viewModel::skipRest,
                    onPlus = { viewModel.addRest(15) },
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(state.exercises, key = { it.id }) { exercise ->
                    ExerciseCard(exercise, viewModel)
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    if (state.confirmCancel) {
        AlertDialog(
            onDismissRequest = { viewModel.askCancel(false) },
            title = { Text("Leave without saving?") },
            text = { Text("This session will be discarded.") },
            confirmButton = { TextButton(onClick = { viewModel.cancel(onExit) }) { Text("Discard", color = GymDanger) } },
            dismissButton = { TextButton(onClick = { viewModel.askCancel(false) }) { Text("Keep logging", color = GymText) } },
        )
    }
    if (state.confirmLeave) {
        AlertDialog(
            onDismissRequest = { viewModel.askLeave(false) },
            title = { Text("Leave session?") },
            text = { Text("Finish saves completed sets for history and weekly volume. Incomplete sets count as 0. Resume keeps logging.") },
            confirmButton = { TextButton(onClick = { viewModel.finish(onExit) }) { Text("Finish", color = GymLime) } },
            dismissButton = { TextButton(onClick = { viewModel.askLeave(false) }) { Text("Resume", color = GymText) } },
        )
    }
    if (state.confirmFinish) {
        val empty = state.exercises.none { ex -> ex.sets.any { it.completed } }
        AlertDialog(
            onDismissRequest = { viewModel.askFinish(false) },
            title = { Text(if (empty) "Finish empty workout?" else "Finish workout?") },
            confirmButton = { TextButton(onClick = { viewModel.finish(onExit) }) { Text("Finish", color = GymLime) } },
            dismissButton = { TextButton(onClick = { viewModel.askFinish(false) }) { Text("Back") } },
        )
    }
}

@Composable
private fun RestBar(
    left: Int,
    running: Boolean,
    done: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onPlus: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(GymSurfaceHigh, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            if (done) "Rest done" else "%d:%02d".format(left / 60, left % 60),
            color = GymLime,
            fontSize = 40.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            TextButton(onClick = if (running) onPause else onResume) {
                Text(if (running) "Pause" else "Resume", color = GymText)
            }
            TextButton(onClick = onPlus) { Text("+15s", color = GymText) }
            TextButton(onClick = onSkip) { Text("Skip", color = GymLime) }
        }
    }
}

@Composable
private fun ExerciseCard(exercise: ExerciseDraft, viewModel: SessionViewModel) {
    GymCard(Modifier.fillMaxWidth()) {
        Text(exercise.name, style = MaterialTheme.typography.titleLarge)
        if (exercise.lastLabel != null) {
            Text("Last top set ${exercise.lastLabel}", color = GymMuted, modifier = Modifier.padding(bottom = 8.dp))
        }
        exercise.sets.forEach { set ->
            SetRow(set, exercise.id, viewModel)
        }
        TextButton(
            onClick = { viewModel.addSet(exercise.id) },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            Text("Add set", color = GymLime)
        }
    }
}

@Composable
private fun SetRow(set: SetDraft, exerciseId: Long, viewModel: SessionViewModel) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(end = 4.dp)
                .wrapContentWidth(),
        ) {
            Text("${set.setIndex + 1}", color = GymMuted, fontSize = 13.sp)
            if (set.isWarmup) {
                Text(
                    "warm-up",
                    color = GymAmber,
                    fontSize = 8.sp,
                    lineHeight = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Visible,
                    textAlign = TextAlign.Center,
                )
            }
        }
        CompactSetField(
            value = set.weightText,
            onValueChange = { viewModel.onWeight(set.id, it) },
            label = "kg",
            enabled = !set.completed,
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.testTag("weightField").weight(1f).padding(horizontal = 2.dp),
        )
        CompactSetField(
            value = set.repsText,
            onValueChange = { viewModel.onReps(set.id, it) },
            label = "reps",
            enabled = !set.completed,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.testTag("repsField").weight(1f).padding(horizontal = 2.dp),
        )
        FilledIconButton(
            onClick = { viewModel.completeSet(set.id) },
            modifier = Modifier.size(40.dp),
            enabled = !set.completed,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (set.completed) GymLime else GymSurfaceHigh,
                contentColor = if (set.completed) GymBlack else GymLime,
                disabledContainerColor = GymLime,
                disabledContentColor = GymBlack,
            ),
        ) {
            Icon(Icons.Outlined.Check, contentDescription = "Complete set")
        }
        Box {
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "Set options", tint = GymMuted)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = {
                        Text(
                            if (set.isWarmup) "Remove warm-up" else "Set as warm-up",
                            color = GymAmber,
                        )
                    },
                    onClick = {
                        viewModel.onWarmup(set.id, !set.isWarmup)
                        menuOpen = false
                    },
                )
            }
        }
        IconButton(onClick = { viewModel.removeSet(set.id, exerciseId) }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Outlined.Close, contentDescription = "Remove set", tint = GymMuted)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactSetField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .defaultMinSize(minWidth = 0.dp, minHeight = 46.dp)
            .height(46.dp),
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(fontSize = 17.sp, color = GymText),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        interactionSource = interaction,
        decorationBox = { inner ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = inner,
                enabled = enabled,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interaction,
                label = { FieldLabel(label) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                colors = OutlinedTextFieldDefaults.colors(),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = enabled,
                        isError = false,
                        interactionSource = interaction,
                    )
                },
            )
        },
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        fontSize = 11.sp,
    )
}
