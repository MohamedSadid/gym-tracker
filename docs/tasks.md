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

## Later (not v1)

- [ ] kg / lb toggle
- [ ] Edit or delete old sessions
- [ ] Backup / cloud sync
- [ ] Accounts
- [ ] Rest timer still firing if the app is fully killed

## Already done

- [x] v1 requirements (`docs/requirements.md`)
- [x] v1 design (`docs/design.md`)
- [x] v1 architecture (`docs/architecture.md`)
- [x] Android app: workouts, exercises, logging, rest timer, history, progression
- [x] Debug APK build
- [x] Automated tests (data flow, rest timer, UI smoke)
- [x] Session always starts with one set; duplicate exercises blocked; warm-up via set menu; edit workout Save / Cancel
