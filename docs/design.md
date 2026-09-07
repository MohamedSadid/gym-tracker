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
3. **Program days** — read-only day list with exercises, **Start**, **Copy to Customize**.
4. **Customize** — user templates, **Start**, FAB new workout; empty: create first workout. **Edit** and light-red **Delete** on the card (confirm; history kept).
5. **Edit workout** — name, ordered list, up/down reorder, add exercise, delete with confirm. **Save** / **Cancel**; leaving with unsaved edits asks Save or Cancel (discard). Tap the name to clear it and type a new one; leaving it empty keeps the last saved name.
6. **Exercise picker** — search, muscle-group chips (light text on dark chips), tap to add, create custom. Duplicate on the workout: message **This exercise is already added** for 3s.
7. **Active session** — elapsed time, Finish; sets (⋮ → orange **Set as warm-up**, one-line orange **warm-up** label only if marked, compact kg/reps, complete, remove set); Add set; rest bar.
8. **Rest** — large countdown, pause / +15s / skip; at 0: haptic + sound.
9. **History** — finished sessions; tap for read-only detail.
10. **Progress** — exercise list; last top set + recent list/chart.
11. Confirms: delete template, cancel session, finish (including empty).

## Speed

Numeric fields, one tap to complete a set, rest does not block logging, pre-fill from last session when history exists.

## Top set rule

Heaviest working (non-warmup) completed set in a session; if same weight, more reps.
