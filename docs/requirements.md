# Gym Tracker — v1 requirements

Single-user Android app for logging gym workouts on the phone: pick exercises, enter sets/reps/weight, rest between sets, then review history and simple progression.

v1 is done when you can use it for a real session without an account or internet.

## User

- One person, one phone.
- Typical gym sets: **weight (kg) + reps**.
- Fast logging between sets.

## In scope

### Exercises
- Starter list of common lifts (name + muscle group).
- Search / filter.
- Add a custom exercise (name + muscle group).
- Built-in exercises are not deleted in v1.

### Workout templates
- Create a named workout (e.g. Push A).
- Add exercises in order; rename; add/remove/reorder; delete template.
- Deleting a template does **not** delete finished history.

### Active session
- Start from a template.
- Log sets: set number, **reps**, **weight (kg)**, optional **warmup**.
- Add or remove a set; skip an exercise (no sets).
- **Finish** → saved with date/time and duration.
- **Cancel** → confirm; **not saved**.

### Rest timer
- Starts after a completed set (default **90s**, adjustable 60 / 90 / 120 / 180 in Settings).
- Pause / +15s / skip.
- At 0: vibrate + sound when the OS allows.
- Perfect background timer if the app is killed is **not** required in v1.

### History
- List of finished sessions: date, name, duration.
- Open a session: exercises + sets (read-only in v1).

### Progression
- Per exercise: last **top set** (heaviest weight; if tie, more reps).
- Recent top sets (about last 12) as a list; chart when there are 2+ points.
- No AI suggested next weight.

### Home
- Templates, **Start** in one or two taps, shortcut to history.
- Empty states when there are no workouts or no history.

## Out of scope (v1)

Accounts, cloud sync, social, videos, AI coaching, wearables, nutrition, subscriptions, multi-user, RPE/drop-set types, cardio mode, supersets as a type, kg/lb toggle, light theme, onboarding tutorial.

## Quality

- Offline-first; data on device (uninstall deletes data).
- English; dark theme; large tap targets.
- Finish must persist; no silent data loss.
- Code structured so accounts/sync can be added later.

## Locked decisions

- Android only, min API 26.
- Weight unit: kg.
- Cancelled session: not saved.
- Past sessions: read-only.
- Default rest: 90s.

## Definition of done

Create a workout, run a session with sets + rest timer, see it in history, see simple progression, all offline with no account.
