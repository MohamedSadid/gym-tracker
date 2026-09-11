# Gym Tracker — v1 product / UI design

## Visual

Dark gym floor, glanceable between sets, large targets.

- Background `#0B0D10`, cards slightly lifted, one accent: lime `#C8F542`
- Numbers (weight, reps, timer) large
- Short motion; dense **active session**, more space on home
- Empty states: one sentence + one button

## Navigation

Bottom bar: **Workouts** | **History** | **Progress**. Settings is a gear on the Workouts hub, not a tab. Programs, Customize, session, edit, picker, and detail hide the bar.

## Screens

1. **Workouts hub** — **Programs** and **Customize**; settings gear.
2. **Programs** — four built-in splits; tap a program.
3. **Program days** — tap a day to open it.
4. **Program day** — read-only exercises, **Start**, **Copy to Customize** (that day only).
5. **Customize** — user templates; FAB new workout; empty: create first workout. Tap a card to open and **Start**. **Edit** is edit-only (no Start). Light-red **Delete** on the card (confirm; history kept).
6. **Edit workout** — name, ordered list, up/down reorder, add exercise, delete with confirm. No **Start**. **Save** / **Cancel**; leaving a new unsaved workout asks Save or Don't save (Don't save deletes it). Leaving with unsaved edits on an existing workout asks Save or Don't save (Don't save discards). Tap the name to clear it and type a new one; leaving it empty keeps the last saved name.
7. **Exercise picker** — search, **primary muscle** chips: Chest, Back, Shoulders, Arms, Legs, Abs, Other (light text on dark chips). Tap to add; create custom with the same list. Duplicate on the workout: message **This exercise is already added** for 3s. No Push / Pull / Core chips.
8. **Active session** — elapsed time, Finish; sets (⋮ → orange **Set as warm-up**, one-line orange **warm-up** label only if marked, compact kg/reps, complete, remove set); Add set; rest bar. System back asks **Finish** / **Resume**.
9. **Rest** — large countdown, pause / +15s / skip (light / lime labels on the dark bar); at 0: haptic + sound.
10. **History** — finished sessions; tap for read-only detail.
11. **Progress** — exercise list; last top set + recent list/chart. After a week ends: **Last week** Total-volume per muscle (`kg × reps`) and a **coverage report** of short muscles (always includes **2 sessions**). After **3 full weeks** of volume for a muscle: **3-week trend** cards (flat / rise / fall). No engine popups on the active session. Programs and session UI stay as they are.
12. Confirms: delete template, cancel session, finish (including empty), session back Finish / Resume.

## Performance copy (tone)

Short insights, not medical coaching. Prefer “below 10 back sets this week” / “volume dropped” over “overtrained.” Coverage and trend cards do not tell the user which workout to start.

## Speed

Numeric fields, one tap to complete a set, rest does not block logging, pre-fill from last session when history exists.

## Top set rule

Heaviest working (non-warmup) completed set in a session; if same weight, more reps.
