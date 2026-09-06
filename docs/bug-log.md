# Bug log

Add a new `- [ ]` item when we find a bug. Change it to `- [x]` after it is fixed (and note the date or commit if useful).

## Open

<!-- copy this line:
- [ ] Short description. Where it happens. What you expected.
-->

## Fixed

- [x] Muscle group (e.g. Legs) drawn on top of the exercise name — cards used a `Box`, so texts stacked. Fixed by using a `Column` in `GymCard`.
- [x] **Add set** did nothing — taps hit the overlapping complete-set control. Same card layout fix.
- [x] **Add set** cramped between the check control and the exercise name — same overlap. Button is now full-width under the sets.
- [x] Unresolved reference `Spacer` on the session screen — missing import. Commit `54edae2`.
