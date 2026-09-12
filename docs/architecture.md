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
| `domain/` | Models (`Exercise`, `HistorySession`, `RestTimerState`, …). Performance: volume, weekly rollup, coverage, advisor messages (pure functions; no I/O). |

Wiring today: `AppContainer` (simple constructor DI). Hilt can replace this later.

Timer: `RestTimerState` in domain; session ViewModel owns countdown + `RestAlerter` (vibrate/beep).

Single user: no `User` table. Add `userId` later if needed.

## Data model

- `Exercise` — id, name, **primary muscle** (`CHEST`, `BACK`, `SHOULDERS`, `ARMS`, `LEGS`, `ABS`, `OTHER`), isCustom. Optional silent **coverage region** on built-in Legs / Shoulders / Arms lifts (not a picker chip). Legs: `QUADS` / `HAMSTRINGS` / `CALVES`. Shoulders: `LATERAL` / `REAR` / `FRONT`. Arms: `BICEPS` / `TRICEPS` / `FLEXORS` / `EXTENSORS` (forearm). Replaces Push / Pull / Core / Other as the only user-facing grouping. Ambiguous built-ins pick one home (e.g. face pull → Shoulders + rear). Room **2→3** remaps stored `exercises.muscleGroup` by built-in name; leftover `CORE` → `ABS`, `PUSH`/`PULL` → `OTHER`.
- `Workout` — id, name, isBuiltIn, programKey, sortIndex
- `WorkoutExercise` — workout, exercise, position
- `Session` — snapshot of workout name, started/finished
- `SessionExercise` — snapshot of exercise name, **primary muscle**, optional **coverage region**, and position. Copied from the library on **Start**. Room **3→4** adds `muscleGroupSnapshot`; Room **4→5** adds `coverageRegion` / `coverageRegionSnapshot` and backfills from the seed map.
- `SessionSet` — reps, weightKg, warmup, completed. Every new session seeds **one** working set per exercise; weight/reps pre-fill from the last completed working set when history exists.
- `Settings` — default rest seconds

Finished sessions keep **name and primary-muscle snapshots** so renaming a template or remapping a lift does not rewrite history. Deleting a template does not delete sessions. Built-in program days cannot be changed; **Copy to Customize** on a day duplicates that day as a normal workout.

In-progress session is written on **Start**, deleted on **Cancel**, `finishedAt` set on **Finish** (including Finish from the back dialog). System back on the session screen is intercepted (`BackHandler`) and does not pop until Finish or the user already left via Cancel.

## Packages

```
com.bool.gymtracker
  ui/           workouts, session, history, progress, settings, theme
  domain/       models, ExerciseDemos, ExerciseTargetMuscles, RestTimerState, performance (volume / coverage / advisor)
  data/local/
  data/repository/
  data/seed/    exercises (missing built-ins inserted on launch) + 4 built-in programs
  di/           AppContainer
```

Exercise how-to stills and loops live in the repo under `content/exercises/` (character reference + per-lift `lockout` / `bottom` / `loop` PNGs). The Android copies are `app/src/main/assets/exercises/` (`{slug}_lockout.png` / `{slug}_bottom.png`, plus the APNG loop). `ExerciseDemos` maps built-in names to those assets and tip strings; Compose swaps the two stills (~700 ms). `ExerciseTargetMuscles` maps every built-in name to main and auxiliary labels for the demo screen; custom lifts fall back to the chosen primary muscle. Custom lifts are not in the demo-asset map. Rules: `docs/exercise-demo-criteria.md`. Stills are unpainted; region IDs in `docs/exercise-muscle-map.md` are parked. Builder: `tools/make_exercise_loop.py`.

Performance stays in-process: repositories load finished sets → `domain/performance` computes small-volume, weekly Total-volume (Mon–Sun, device local), coverage vs targets, and 3-week trend messages. Coverage never writes workouts or programs. Progress shows last-week volume, shortfalls, and trend cards. No backend, no extra services.

## Stack

Kotlin, Jetpack Compose, single Activity, Compose Navigation, Room, ViewModel + StateFlow, Material 3 (dark + lime accent). No backend.

## UI (v1)

Bottom tabs: **Workouts** (hub), **History**, **Progress**. Active session is full-screen (no tabs). Settings from the gear on the Workouts hub.

Core loop: Workouts → Customize or a program day → Start → log sets → rest → Finish → History / Progress.

## Build order (done)

Empty app → templates + exercises → session logging → rest timer → history + progression → polish.

## Tests

- `ExerciseDemoTest` — shipped name map for Barbell bench press, Dumbbell bench press, Smith bench press, Incline barbell press, Incline Smith bench press, and Incline dumbbell press; unknown names have no demo; every built-in seed name has main/auxiliary targets
- `V1WorkoutFlowTest` — create workout, log set, finish, history, progression, cancel, duplicate custom name, rename blank keeps name, add/remove set, reject duplicate workout exercise, new session always one set, delete template keeps history, built-in program locked + copy one day, session keeps primary muscle if the library remaps, coverage region snapshot, weekly volume from finished working sets, finish leaves incomplete sets out of volume
- `RestTimerStateTest` — countdown, pause/resume/skip, +15s
- `VolumeTest` — working-set small-volume, Total-volume per muscle, Mon–Sun week in local time, warm-ups / incomplete / unfinished excluded
- `CoverageTest` — 2× sessions, set targets, Legs/Shoulders/Arms regions, unclassified skip, Other ignored, ended week only
- `InsightsTest` — 3-week flat (±2%), rise (+10% both steps), fall (−12%), no band without three weeks of volume
- `V1UiFlowTest` — Compose path through screens (Robolectric; no physical device), including picker checkbox + **Add selected**, Delete from Customize, copy one program day, Don't save on a new workout
