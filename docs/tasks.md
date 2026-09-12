# Tasks

Mark a box `[x]` when that item is done.

## Next

- [ ] Open `c:\Bool\gym-tracker` in Android Studio and wait for Gradle sync to finish
- [ ] Create an emulator (Device Manager → Create Device) **or** enable USB debugging on your phone and plug it in
- [ ] Press **Run** and confirm the app launches (Workouts / History / Progress tabs)
- [ ] Create a workout, add a few exercises, start it, log weight/reps, use the rest timer, finish
- [ ] Check History shows that session and Progress shows the exercise trend
- [ ] Change default rest in Settings and confirm the next session uses it
- [ ] Try cancel: start a workout, leave without saving, confirm it is **not** in History
- [ ] First git commit of the project (only when you ask — or you do it yourself)
- [ ] Enhance in-app animation (screen transitions and session UI)
- [x] Extend the built-in exercise library (more lifts, including forearm extensors and better region coverage)

### Performance engine (do in order)

- [x] Switch exercises to primary muscle (Chest, Back, Shoulders, Arms, Legs, Abs, Other); remap seed + picker; drop Push / Pull / Core chips
- [x] Snapshot primary muscle on session exercises
- [x] Volume math: small-volume per exercise, Total-volume per muscle; working sets only
- [x] Weekly rollup (Mon–Sun) per primary muscle
- [x] Coverage from logs only (do not change programs, picker, or session): 2 sessions/week + set targets (Chest 10, Back 10, Abs 4, Shoulders 12 with 6/3/3 delts, Arms 18 with 6/6/3/3, Legs 24 with 8/8/8); after each ended week, Progress report of insufficient muscles
- [x] Progress UI: weekly Total-volume (coverage shortfall report is the previous task; no session popups)
- [x] Insights after 3 weeks: flat ±2%, rise ≥+10% both weeks, fall ≤−12%
- [x] Unit tests for volume, coverage, and the three trend bands

## Later (not v1)

- [ ] kg / lb toggle
- [ ] Edit or delete old sessions
- [ ] Backup / cloud sync
- [ ] Accounts
- [ ] Rest timer still firing if the app is fully killed
- [ ] App character for empty states / branding (separate from exercise how-to stills)

### Exercise demos (remaining lifts)

Two stills per lift (`position-1` + `position-2`); loop when asked. Generate position 1, duplicate, then change only the moving body part. Stills stay unpainted (`docs/exercise-demo-criteria.md`).

- [ ] **Chest**
  - [x] Barbell bench press (stills + loop)
  - [x] Dumbbell bench press (stills + loop)
  - [x] Incline barbell press (stills + loop)
  - [x] Incline dumbbell press (stills + loop)
  - [x] Smith bench press (stills + loop)
  - [x] Incline Smith bench press (stills + loop)
  - [x] Pec deck fly (stills + loop)
  - [x] Chest press machine (stills + loop)
  - [ ] Dips
  - [ ] Push-up
  - [ ] Chest fly
  - [x] Cable fly (stills + loop)
- [ ] **Back**
- [ ] **Shoulders**
- [ ] **Arms**
- [ ] **Legs**
- [ ] **Abs**
- [ ] **Other**

### Animation integration

Integrate exercise characters/loops in the app (built-in lifts only; custom exercises stay without a demo).

- [x] **Chest:** all finished demos in the app (Barbell bench press, Dumbbell bench press, Incline barbell press, Incline dumbbell press, Smith bench press, Incline Smith bench press, Pec deck fly, Cable fly, Chest press machine). Unpainted stills + two-frame playback; picker/program-day thumbnail and demo screen. Loop when asked.

**Remaining muscles:** other cards get the same chrome and main/auxiliary muscles; thumbnail/animation/how-to stay empty until their demos exist.

**Customize (exercise picker)**

- [x] Show an animation thumbnail on each exercise while picking
- [x] Tap the exercise card to open a detail view with the animation
- [x] Show how-to tips and main/auxiliary muscles under the animation on that detail view
- [x] Add a checkbox on each exercise so the user can select several and add them without opening the card

**Fixed programs**

- [x] Show an animation thumbnail on every exercise (user does not pick exercises here)
- [x] Tap the exercise card to open a detail view with the animation
- [x] Show how-to tips and main/auxiliary muscles under the animation on that detail view

## Already done

- [x] v1 requirements (`docs/requirements.md`)
- [x] v1 design (`docs/design.md`)
- [x] v1 architecture (`docs/architecture.md`)
- [x] Android app: workouts, exercises, logging, rest timer, history, progression
- [x] Debug APK build
- [x] Automated tests (data flow, rest timer, UI smoke)
- [x] Workouts hub, built-in programs, copy into Customize
- [x] Leave-without-save for new custom workouts; Start/Copy inside opened day
