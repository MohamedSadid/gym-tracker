# Changes log

Add a new `- [ ]` item when we agree a product/UI/behavior change (not a bug). Change it to `- [x]` after it is in the code.

## Open

<!-- copy this line:
- [ ] Short description. Where. What should happen instead.
-->

## Done

- [x] Exercise demo: larger full-width PNG; how-to and main/auxiliary muscles under it. `ExerciseDemoScreen`, `ExerciseTargetMuscles`.

- [x] Animation integration: picker/program-day thumbnails; tap opens loop + tips; picker checkbox + Add selected. First pass: Dumbbell bench press and Incline dumbbell press. `ExerciseDemos`, `ExerciseDemoScreen`, `ExercisePickerScreen`, `ProgramDayScreen`.

- [x] Built-in library: 25 extra lifts (coverage holes + StrengthLog/BurnFit staples). Missing names insert on launch. `ExerciseSeed`, `AppDatabase.seedIfNeeded`.

- [x] Progress: last-week Total-volume, 3-week flat / rise / fall cards. `Insights.kt`, `ProgressScreen`.

- [x] Coverage report always shows **2 sessions** for every required muscle (Chest, Back, Shoulders, Arms, Legs, Abs), not only Chest/Back/Abs. `formatCoverageShortfall`.

- [x] Coverage from logs only: 2× + set targets (Legs 24 with 8/8/8, Shoulders 12 with 6/3/3, Arms 18 with 6/6/3/3); ended-week Progress shortfall report. `Coverage.kt`, Room 4→5.

- [x] Volume: small-volume per exercise and Total-volume per muscle, Mon–Sun week, finished working sets only. `domain/performance/Volume.kt`.

- [x] Session logs snapshot primary muscle on Start. `SessionExerciseEntity.muscleGroupSnapshot`, Room 3→4.

- [x] Exercises: replace Push / Pull / Core with primary muscle (Chest, Back, Shoulders, Arms, Legs, Abs, Other) on the picker and cards. `MuscleGroup`, `ExerciseSeed`, Room 2→3.

- [x] Customize: **Edit** has no Start; tap the card to open and start. `WorkoutsScreen` + `ProgramDayScreen`.

- [x] Customize: leave new workout asks Save / Don't save; Don't save deletes. Start only inside opened workout. `EditWorkoutScreen`.
- [x] Program days: Start and Copy only inside the opened day; **Push Pull Leg program**. `ProgramDayScreen`.

- [x] Workouts hub: **Programs** and **Customize**. Four built-in programs are read-only; **Copy to Customize** duplicates days. `WorkoutsHubScreen`, `ProgramSeed`.
- [x] Session: warm-up only via set ⋮ menu; orange label hidden unless marked. `SessionScreen` SetRow.
- [x] Edit workout: draft until Save / Cancel; leave with unsaved edits confirms. `EditWorkoutScreen`.
- [x] Workouts list: **Delete** (light red) next to **Edit** on the card; confirm; history stays. Press-and-hold removed. `WorkoutsScreen`.
- [x] Session set ⋮: **Set as warm-up** / **Remove warm-up** in `GymAmber`. `SetRow`.
