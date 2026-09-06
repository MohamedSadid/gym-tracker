# Gym Tracker — v1 architecture

## Layers

Screens never talk to the database. Later we can add sync inside repositories without rewriting the logger.

```
UI (Compose) → ViewModels → repositories → Room (SQLite on device)
```

| Layer | Role |
|--------|------|
| `ui/` | Screens, theme, navigation |
| ViewModels | Screen state, rest timer, survive rotation |
| `data/repository/` | Load/save workouts, sessions, exercises |
| `data/local/` | Room entities, DAOs, `AppDatabase` |
| `domain/` | Models (`Exercise`, `HistorySession`, `RestTimerState`, …) |

Wiring today: `AppContainer` (simple constructor DI). Hilt can replace this later.

Timer: `RestTimerState` in domain; session ViewModel owns countdown + `RestAlerter` (vibrate/beep).

Single user: no `User` table. Add `userId` later if needed.

## Data model

- `Exercise` — id, name, muscle group, isCustom
- `Workout` — id, name
- `WorkoutExercise` — workout, exercise, position
- `Session` — snapshot of workout name, started/finished
- `SessionExercise` — snapshot of exercise name, position
- `SessionSet` — reps, weightKg, warmup, completed. First session of an exercise seeds **one** working set; later sessions copy last completed working sets.
- `Settings` — default rest seconds

Finished sessions keep **name snapshots** so renaming a template does not rewrite history. Deleting a template does not delete sessions.

In-progress session is written on **Start**, deleted on **Cancel**, `finishedAt` set on **Finish**.

## Packages

```
com.bool.gymtracker
  ui/           workouts, session, history, progress, settings, theme
  domain/
  data/local/
  data/repository/
  data/seed/
  di/           AppContainer
```

## Stack

Kotlin, Jetpack Compose, single Activity, Compose Navigation, Room, ViewModel + StateFlow, Material 3 (dark + lime accent). No backend.

## UI (v1)

Bottom tabs: **Workouts**, **History**, **Progress**. Active session is full-screen (no tabs). Settings from the gear on Workouts.

Core loop: Workouts → Start → log sets → rest → Finish → History / Progress.

## Build order (done)

Empty app → templates + exercises → session logging → rest timer → history + progression → polish.

## Tests

- `V1WorkoutFlowTest` — create workout, log set, finish, history, progression, cancel, duplicate custom name, rename blank keeps name, add/remove set
- `RestTimerStateTest` — countdown, pause/resume/skip, +15s
- `V1UiFlowTest` — Compose path through screens (Robolectric; no physical device)
