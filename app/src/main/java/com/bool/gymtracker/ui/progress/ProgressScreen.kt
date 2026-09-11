package com.bool.gymtracker.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bool.gymtracker.data.repository.ExerciseRepository
import com.bool.gymtracker.data.repository.formatWeight
import com.bool.gymtracker.di.LocalAppContainer
import com.bool.gymtracker.di.appViewModel
import com.bool.gymtracker.domain.Exercise
import com.bool.gymtracker.domain.ExerciseProgression
import com.bool.gymtracker.domain.performance.ProgressEngineReport
import com.bool.gymtracker.domain.performance.formatCoverageShortfall
import com.bool.gymtracker.domain.performance.formatInsight
import com.bool.gymtracker.domain.performance.formatVolumeLine
import com.bool.gymtracker.ui.components.EmptyState
import com.bool.gymtracker.ui.components.GymCard
import com.bool.gymtracker.ui.components.formatDateTime
import com.bool.gymtracker.ui.theme.GymAmber
import com.bool.gymtracker.ui.theme.GymBlack
import com.bool.gymtracker.ui.theme.GymLime
import com.bool.gymtracker.ui.theme.GymMuted
import com.bool.gymtracker.ui.theme.GymText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModel(exercises: ExerciseRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    val items = query.flatMapLatest { exercises.observeFiltered(it, null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        query.value = value
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onOpen: (Long) -> Unit,
    viewModel: ProgressViewModel = appViewModel { ProgressViewModel(it.exercises) },
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val sessions = LocalAppContainer.current.sessions
    val report by produceState(ProgressEngineReport(emptyList(), emptyList(), emptyList())) {
        value = sessions.progressEngineReport()
    }
    val shortfalls = report.shortfalls
    val volumes = report.volumes
    val insights = report.insights
    var query by remember { mutableStateOf("") }
    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = { Text("Progress") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).padding(bottom = 80.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.setQuery(it)
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                singleLine = true,
                label = { Text("Search exercise") },
            )
            if (items.isEmpty() && shortfalls.isEmpty() && volumes.isEmpty() && insights.isEmpty()) {
                EmptyState("No exercises", "Add a custom exercise from a workout if the list is empty.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (volumes.isNotEmpty() || shortfalls.isNotEmpty()) {
                        item {
                            Text("Last week", color = GymMuted, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                    items(volumes, key = { "vol-${it.muscle}" }) { volume ->
                        GymCard(Modifier.fillMaxWidth()) {
                            Text(formatVolumeLine(volume), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    items(shortfalls, key = { "short-${it.muscle}" }) { coverage ->
                        GymCard(Modifier.fillMaxWidth()) {
                            Text(formatCoverageShortfall(coverage), color = GymAmber)
                        }
                    }
                    if (insights.isNotEmpty()) {
                        item {
                            Text("3-week trend", color = GymMuted, style = MaterialTheme.typography.titleSmall)
                        }
                        items(insights, key = { "trend-${it.muscle}" }) { insight ->
                            GymCard(Modifier.fillMaxWidth()) {
                                Text(formatInsight(insight))
                            }
                        }
                    }
                    items(items, key = { it.id }) { exercise ->
                        ExerciseProgressRow(exercise, onClick = { onOpen(exercise.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseProgressRow(exercise: Exercise, onClick: () -> Unit) {
    GymCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Text(exercise.name, style = MaterialTheme.typography.titleMedium)
        Text(exercise.muscleGroup.label, color = GymMuted)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressScreen(exerciseId: Long, onBack: () -> Unit) {
    val sessions = LocalAppContainer.current.sessions
    val data by produceState<ExerciseProgression?>(null, exerciseId) {
        value = sessions.progression(exerciseId)
    }
    Scaffold(
        containerColor = GymBlack,
        topBar = {
            TopAppBar(
                title = { Text(data?.exercise?.name ?: "Progress") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = GymText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBlack, titleContentColor = GymText),
            )
        },
    ) { padding ->
        val progression = data
        if (progression == null) {
            Text("Loading…", modifier = Modifier.padding(padding).padding(16.dp), color = GymMuted)
        } else {
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GymCard(Modifier.fillMaxWidth()) {
                    Text("Last top set", color = GymMuted)
                    val last = progression.lastTopSet
                    if (last == null) {
                        Text("No completed working sets yet", modifier = Modifier.testTag("lastTopSetValue"))
                    } else {
                        Text(
                            "${formatWeight(last.weightKg)} kg × ${last.reps}",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.testTag("lastTopSetValue"),
                        )
                    }
                }
                if (progression.points.size >= 2) {
                    GymCard(Modifier.fillMaxWidth()) {
                        Sparkline(progression.points.map { it.weightKg })
                    }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(progression.points.reversed()) { point ->
                        GymCard(Modifier.fillMaxWidth()) {
                            Text("${formatWeight(point.weightKg)} kg × ${point.reps}")
                            Text(formatDateTime(point.finishedAt), color = GymMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Sparkline(values: List<Double>) {
    Canvas(Modifier.fillMaxWidth().height(120.dp).padding(8.dp)) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0 } ?: 1.0
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = size.width * index / (values.size - 1)
            val y = size.height - ((value - min) / range * size.height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, GymLime, style = Stroke(width = 6f, cap = StrokeCap.Round))
        values.forEachIndexed { index, value ->
            val x = size.width * index / (values.size - 1)
            val y = size.height - ((value - min) / range * size.height).toFloat()
            drawCircle(GymLime, radius = 8f, center = Offset(x, y))
        }
    }
}
