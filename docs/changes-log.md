# Changes log

Add a new `- [ ]` item when we agree a product/UI/behavior change (not a bug). Change it to `- [x]` after it is in the code.

## Open

<!-- copy this line:
- [ ] Short description. Where. What should happen instead.
-->

## Done

- [x] Customize: **Edit** has no Start; tap the card to open and start. `WorkoutsScreen` + `ProgramDayScreen`.

- [x] Customize: leave new workout asks Save / Don't save; Don't save deletes. Start only inside opened workout. `EditWorkoutScreen`.
- [x] Program days: Start and Copy only inside the opened day; **Push Pull Leg program**. `ProgramDayScreen`.

- [x] Workouts hub: **Programs** and **Customize**. Four built-in programs are read-only; **Copy to Customize** duplicates days. `WorkoutsHubScreen`, `ProgramSeed`.
- [x] Session: warm-up only via set ⋮ menu; orange label hidden unless marked. `SessionScreen` SetRow.
- [x] Edit workout: draft until Save / Cancel; leave with unsaved edits confirms. `EditWorkoutScreen`.
- [x] Workouts list: **Delete** (light red) next to **Edit** on the card; confirm; history stays. Press-and-hold removed. `WorkoutsScreen`.
- [x] Session set ⋮: **Set as warm-up** / **Remove warm-up** in `GymAmber`. `SetRow`.
