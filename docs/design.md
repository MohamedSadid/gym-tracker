# Gym Tracker — v1 product / UI design

## Visual

Dark gym floor, glanceable between sets, large targets.

- Background `#0B0D10`, cards slightly lifted, one accent: lime `#C8F542`
- Numbers (weight, reps, timer) large
- Short motion; dense **active session**, more space on home
- Empty states: one sentence + one button

## Navigation

Bottom bar: **Workouts** | **History** | **Progress**. Settings is a gear on Workouts, not a tab. Session, edit, picker, and detail hide the bar.

## Screens

1. **Workouts** — templates, **Start**, FAB new workout; empty: create first workout.
2. **Edit workout** — name, ordered list, up/down reorder, add exercise, delete with confirm. Auto-save.
3. **Exercise picker** — search, muscle-group chips, tap to add, create custom.
4. **Active session** — elapsed time, Finish; sets (warmup, kg, reps, complete); Add set; rest bar.
5. **Rest** — large countdown, pause / +15s / skip; at 0: haptic + sound.
6. **History** — finished sessions; tap for read-only detail.
7. **Progress** — exercise list; last top set + recent list/chart.
8. Confirms: delete template, cancel session, finish (including empty).

## Speed

Numeric fields, one tap to complete a set, rest does not block logging, pre-fill from last session when history exists.

## Top set rule

Heaviest working (non-warmup) completed set in a session; if same weight, more reps.
