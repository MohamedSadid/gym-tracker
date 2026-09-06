package com.bool.gymtracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bool.gymtracker.ui.history.HistoryScreen
import com.bool.gymtracker.ui.history.SessionDetailScreen
import com.bool.gymtracker.ui.progress.ExerciseProgressScreen
import com.bool.gymtracker.ui.progress.ProgressScreen
import com.bool.gymtracker.ui.session.SessionScreen
import com.bool.gymtracker.ui.settings.SettingsScreen
import com.bool.gymtracker.ui.theme.GymBlack
import com.bool.gymtracker.ui.theme.GymLime
import com.bool.gymtracker.ui.theme.GymMuted
import com.bool.gymtracker.ui.workouts.EditWorkoutScreen
import com.bool.gymtracker.ui.workouts.ExercisePickerScreen
import com.bool.gymtracker.ui.workouts.WorkoutsScreen

object Routes {
    const val Workouts = "workouts"
    const val History = "history"
    const val Progress = "progress"
    const val Settings = "settings"
    const val EditWorkout = "workout/{workoutId}"
    const val Picker = "picker/{workoutId}"
    const val Session = "session/{sessionId}"
    const val SessionDetail = "history/{sessionId}"
    const val ExerciseProgress = "progress/{exerciseId}"

    fun edit(id: Long) = "workout/$id"
    fun picker(id: Long) = "picker/$id"
    fun session(id: Long) = "session/$id"
    fun historyDetail(id: Long) = "history/$id"
    fun exerciseProgress(id: Long) = "progress/$id"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun GymTrackerRoot() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBar = route in setOf(Routes.Workouts, Routes.History, Routes.Progress)
    val tabs = listOf(
        Tab(Routes.Workouts, "Workouts", Icons.Outlined.FitnessCenter),
        Tab(Routes.History, "History", Icons.Outlined.DateRange),
        Tab(Routes.Progress, "Progress", Icons.AutoMirrored.Outlined.ShowChart),
    )

    Scaffold(
        containerColor = GymBlack,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = GymBlack) {
                    tabs.forEach { tab ->
                        val selected = route == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GymBlack,
                                selectedTextColor = GymLime,
                                indicatorColor = GymLime,
                                unselectedIconColor = GymMuted,
                                unselectedTextColor = GymMuted,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.Workouts,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Workouts) {
                WorkoutsScreen(
                    onOpenWorkout = { nav.navigate(Routes.edit(it)) },
                    onStartSession = { nav.navigate(Routes.session(it)) },
                    onSettings = { nav.navigate(Routes.Settings) },
                )
            }
            composable(Routes.History) {
                HistoryScreen(onOpen = { nav.navigate(Routes.historyDetail(it)) })
            }
            composable(Routes.Progress) {
                ProgressScreen(onOpen = { nav.navigate(Routes.exerciseProgress(it)) })
            }
            composable(Routes.Settings) {
                SettingsScreen(onBack = { nav.popBackStack() })
            }
            composable(
                Routes.EditWorkout,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("workoutId") ?: return@composable
                val pendingExerciseId by entry.savedStateHandle
                    .getStateFlow("pickedExerciseId", 0L)
                    .collectAsStateWithLifecycle()
                EditWorkoutScreen(
                    workoutId = id,
                    pendingExerciseId = pendingExerciseId,
                    onPendingConsumed = { entry.savedStateHandle["pickedExerciseId"] = 0L },
                    onBack = { nav.popBackStack() },
                    onAddExercise = { ids ->
                        entry.savedStateHandle["draftExerciseIds"] = ids.toLongArray()
                        nav.navigate(Routes.picker(id))
                    },
                )
            }
            composable(
                Routes.Picker,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("workoutId") ?: return@composable
                val alreadyAdded = nav.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<LongArray>("draftExerciseIds")
                    ?.toList()
                    .orEmpty()
                ExercisePickerScreen(
                    workoutId = id,
                    alreadyAddedIds = alreadyAdded,
                    onBack = { nav.popBackStack() },
                    onPicked = { exerciseId ->
                        nav.previousBackStackEntry?.savedStateHandle?.set("pickedExerciseId", exerciseId)
                        nav.popBackStack()
                    },
                )
            }
            composable(
                Routes.Session,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("sessionId") ?: return@composable
                SessionScreen(
                    sessionId = id,
                    onExit = { nav.popBackStack() },
                )
            }
            composable(
                Routes.SessionDetail,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("sessionId") ?: return@composable
                SessionDetailScreen(sessionId = id, onBack = { nav.popBackStack() })
            }
            composable(
                Routes.ExerciseProgress,
                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("exerciseId") ?: return@composable
                ExerciseProgressScreen(exerciseId = id, onBack = { nav.popBackStack() })
            }
        }
    }
}
