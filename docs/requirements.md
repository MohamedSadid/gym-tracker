# Gym Tracker — v1 requirements

Single-user Android app for logging gym workouts on the phone: pick exercises, enter sets/reps/weight, rest between sets, then review history and simple progression.

v1 is done when you can use it for a real session without an account or internet.

## User

- One person, one phone.
- Typical gym sets: **weight (kg) + reps**.
- Fast logging between sets.

## In scope

### Exercises
- Starter list of common lifts (name + **primary muscle**).
- Primary muscle is one of: **Chest, Back, Shoulders, Arms, Legs, Abs**, or **Other**.
- Search / filter by that same list (not Push / Pull / Core).
- Add a custom exercise (name + primary muscle).
- Built-in exercises are not deleted in v1.
- **Demos (first pass):** **Dumbbell bench press** and **Incline dumbbell press** have a two-frame loop. Other built-ins show the same cards with an empty thumbnail until stills exist. Custom exercises have no demo.
- Opened demo: large loop (or empty placeholder), then **Main** and **Auxiliary** muscles, then how-to tips. Built-ins have a main/auxiliary list; custom uses the chosen primary muscle only.
- Picker: thumbnail; tap row opens demo; checkbox + **Add selected** (or **Add** on the demo). Program / opened-day lists: thumbnail; tap opens demo; no checkboxes.

### Workout templates
- Create a named workout (e.g. Push A). Default name is **New workout**.
- Add exercises in order; rename; add/remove/reorder; delete template (from Edit, or tap the workout then **Delete**, or **Delete** on the Customize card, then confirm). An exercise can appear only once; adding it again shows **This exercise is already added** for 3 seconds.
- Edits are a draft until **Save**. Leaving a **new** workout without saving asks **Save** / **Don't save**; Don't save deletes it. Leaving an existing workout with unsaved edits asks the same; Don't save restores the last saved version.
- **Start** is on the opened workout (tap the card), not on **Edit**. Edit is name and exercises only.
- Rename: tap the name field to clear it, then type the new name. Leave it empty and the previous name stays (default **New workout** if it was never renamed).
- Deleting a template does **not** delete finished history.

### Programs
- Workouts tab opens a hub: **Programs** or **Customize**.
- **Programs** lists four built-in splits: Full Body, Push Pull Leg program, Upper Lower, Pro Split. Each program is a set of day templates.
- Open a day to **Start** or **Copy to Customize** (that day only; names end with **(copy)**). Built-in days cannot be renamed, edited, or deleted.

### Active session
- Start from a template. Each exercise starts with **one** set (weight/reps pre-filled from the last working set when history exists); add or remove sets as needed; skip an exercise (no sets).
- Log sets: set number, **reps**, **weight (kg)**, optional **warmup** (set ⋮ menu → orange **Set as warm-up**; compact orange **warm-up** label only when marked).
- **Finish** → saved with date/time and duration. Incomplete sets are not completed (they count as **0** toward volume and coverage).
- **Cancel** (close) → confirm; **not saved**.
- System / gesture **Back** on an active session asks **Finish** or **Resume**. Resume stays in the session. Finish saves as above.

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
- Workouts hub, shortcut to history.
- Empty states when there are no custom workouts or no history.

### Performance engine (next increment)

Local insights from **finished session logs** only. No backend. Warm-up sets are excluded from volume and set counts. Food, sleep, and “enough rest” are not inputs. Each logged exercise keeps the **primary muscle from Start** (not the live library).

- **Small-volume** (per exercise, per week): sum of `weightKg × reps` for completed working sets.
- **Total-volume** (per primary muscle, per week): sum of small-volumes for exercises with that primary muscle.
- **Week:** Monday 00:00 through Sunday 23:59 (device local time).
- **Speak:** no trend advice (flat / rise / fall) until **3 full weeks** of data for that muscle. Coverage shortfalls may show as soon as a week has ended.
- **Flat:** all three weekly Total-volumes within **±2% of the 3-week average**. If also **below** weekly set/frequency targets: *this muscle is weak — start with it first if you want to improve it, or add an extra set with lower weight.* If targets are already met: volume has not changed for 3 weeks (optional extra light set). Do not call it weak.
- **Rising:** week 2 ≥ **+10%** vs week 1 **and** week 3 ≥ **+10%** vs week 2. If the lifts for that muscle **changed**, say volume went up but exercises changed. If the same lifts: consider a weight you can control for about **8–12** reps. Not a hard 12-rep rule.
- **Falling:** **−12% or worse** vs the previous week. Say volume dropped; do not auto-prescribe “reduce weight” (missed sessions or a deload are possible).
- **Neglected / weak:** below frequency or set targets for that muscle (see coverage). After the week ends, list those muscles on Progress so they get attention.
- **Coverage (logs only, end of week):** statistics from **finished working sets**. Does **not** change Programs, Customize, the exercise picker, or how a session is logged. **Every** required muscle — Chest, Back, Shoulders, Arms, Legs, and Abs — needs **≥ 2 sessions** that week **and** at least these **hard (working) sets per week:** Back **10** (10–12 is on target; below 10 is short), Chest **10**, Abs **4**, Shoulders **12**, Arms **18**, Legs **24**. Both frequency and sets must hold (all work for a muscle on one day still fails 2×).
- **Region splits (counting only, picker unchanged):** built-in lifts have one silent region. Custom / unclassified sets count toward the **muscle total** but not a region. If every set for that muscle that week is unclassified, skip the region check and require **total + 2 sessions** only.
  - **Legs (24):** 8 quads (squats / press / lunges / extensions) + 8 hamstrings (deadlift / RDL / leg curl / hip thrust) + 8 calves (calf raise).
  - **Shoulders (12):** 6 lateral delts (lateral raise) + 3 rear delts (face pull, rear delt fly) + 3 front delts (OHP, DB shoulder press, front raise).
  - **Arms (18):** 6 biceps (barbell / DB / incline / cable / preacher curl) + 6 triceps (pushdown, rope pushdown, skull crusher, close-grip bench, overhead extension, bench dip) + 3 forearm flexors (hammer curl, wrist curl) + 3 forearm extensors (reverse curl, wrist extension).
- **Required muscles** for “full body”: Chest, Back, Shoulders, Arms, Legs, Abs. **Other** does not fail the check.
- **Delivery:** after a week ends (local Monday looking at the previous Mon–Sun), Progress shows last-week **Total-volume** and a **coverage report** of muscles that missed targets. After **3 full weeks** of volume for a muscle, Progress also shows flat / rise / fall cards. No cards during the live session. No OS / system notification in this increment.
- Template / Customize / program audit is **out** of this increment (logs only). The engine never reorders, hides, or recommends a workout.

## Out of scope (v1)

Accounts, cloud sync, social, how-to videos (loops/stills only), AI coaching, wearables, nutrition, subscriptions, multi-user, RPE/drop-set types, cardio mode, supersets as a type, kg/lb toggle, light theme, onboarding tutorial, in-app character/mascot for empty states or branding (v2).

## Out of scope (Performance increment)

Food/sleep logging, medical or “overtrained” claims, system notifications, splitting one lift across several primary muscles (e.g. bench as Chest+Arms), PR engine, entertainment engine, fatigue as a separate engine, advising from templates instead of logs, changing which program or workout the user should do.

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
- Exercise **primary muscle** (not Push/Pull) drives picker, labels, volume, and coverage.
- Performance engine is on-device; one engine (volume + coverage + messages), not services.

## Definition of done

Create a workout, run a session with sets + rest timer, see it in history, see simple progression, all offline with no account.

**Performance increment:** remap the library to primary muscles; see weekly Total-volume; after each ended week see which muscles were short vs targets (including Legs / Shoulders / Arms region splits); after three weeks see flat / rise / fall from logs only.
