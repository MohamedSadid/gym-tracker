# Bug log

Add a new `- [ ]` item when we find a bug. Change it to `- [x]` after it is fixed (and note the date or commit if useful).

## Open

<!-- copy this line:
- [ ] Short description. Where it happens. What you expected.
-->

## Fixed

- [x] Session started with last session’s set count (e.g. Back squat → 2 sets). Now always 1 set; last working weight/reps still pre-fill. `SessionRepository.startFromWorkout`.
- [x] Exercise picker added the same exercise twice. Blocked; 3s “This exercise is already added”. `ExercisePickerScreen` + `WorkoutRepository.addExercise`.
- [x] Edit workout name fought backspaces / last letter. Local draft; tap clears; empty on leave keeps last saved name.
- [x] New exercise started with 3 sets. Now 1; add/remove in the session UI.
- [x] Warmup was “W” + checkbox. Tappable **warm-up** label (dim → solid orange).
- [x] Exercise picker group chips (Push/Pull) had black labels on dark chips. Forced light/contrast chip colors.
- [x] Muscle group (e.g. Legs) drawn on top of the exercise name — cards used a `Box`, so texts stacked. Fixed by using a `Column` in `GymCard`.
- [x] **Add set** did nothing — taps hit the overlapping complete-set control. Same card layout fix.
- [x] **Add set** cramped between the check control and the exercise name — same overlap. Button is now full-width under the sets.
- [x] Unresolved reference `Spacer` on the session screen — missing import. Commit `54edae2`.
- [x] Session set row: warm-up widened the index column so **kg** / **reps** stacked. Fixed-width index, 8sp **warm-up**, one-line labels, tighter padding. `SetRow`.
- [x] **reps** wrapped (**s** under **Rep**). Label is `maxLines = 1`, 11sp. `SetRow`.
- [x] Workouts press-and-hold never opened Delete. Hold now uses a 1.5s timer on the **full card** (Edit/Start included); after it fires, that tap is eaten so Start does not run. `WorkoutsScreen`.
- [x] Session kg/reps numbers too small / fields too tall. Input text 17sp; fields 54dp. `SetRow`.
- [x] Hold-to-delete looked like a no-op: menu opened on 1.5s then the finger-up dismissed it. Menu now opens **after** release; haptic. `WorkoutsScreen`.
- [x] kg/reps boxes still 56dp (Material min height). Compact fields 48dp via `DecorationBox`. `CompactSetField`.
- [x] Session kg/reps fields still large. Boxes 46dp, tighter padding. `CompactSetField`.
- [x] Orange **warm-up** wrapped (`warm-u` / `p`) in a 28dp column. One line, width follows the text. `SetRow`.
- [x] Workouts press-and-hold delete still failed on device. Replaced with a light-red **Delete** button next to **Edit**. `WorkoutsScreen`.
