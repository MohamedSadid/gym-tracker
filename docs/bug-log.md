# Bug log

Add a new `- [ ]` item when we find a bug. Change it to `- [x]` after it is fixed (and note the date or commit if useful).

## Open

<!-- copy this line:
- [ ] Short description. Where it happens. What you expected.
-->

## Fixed

- [x] Edit workout name fought backspaces / last letter. Local draft; tap clears; empty on leave keeps last saved name.
- [x] New exercise started with 3 sets. Now 1; add/remove in the session UI.
- [x] Warmup was “W” + checkbox. Tappable **warm-up** label (dim → solid orange).
- [x] Exercise picker group chips (Push/Pull) had black labels on dark chips. Forced light/contrast chip colors.
- [x] Muscle group (e.g. Legs) drawn on top of the exercise name — cards used a `Box`, so texts stacked. Fixed by using a `Column` in `GymCard`.
- [x] **Add set** did nothing — taps hit the overlapping complete-set control. Same card layout fix.
- [x] **Add set** cramped between the check control and the exercise name — same overlap. Button is now full-width under the sets.
- [x] Unresolved reference `Spacer` on the session screen — missing import. Commit `54edae2`.
