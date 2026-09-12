# Exercise demo stills and loops

Use this for every built-in lift. One **character**, one **camera**, **black shorts**. **Two stills per lift** (`lockout` and `bottom`). Do **not** build the animated PNG until asked.

Do not use a new person per frame. Do not use the block 3D mannequin.

## Character (same on every PNG)

- Gray anatomical clay mannequin, faceless, oval head with a neck (not a sphere / floating ball).
- Solid **black** athletic shorts, mid-thigh, no logos or stripes. Bare torso and feet.
- Dark empty studio. No text, logos, or extra props.
- Reuse this same body for **all lifts and all frames**. Generate lockout first; later poses and other exercises must match that character, shorts, and lighting.

## Camera (same on every PNG)

- **High three-quarter from above** (about 30–40°). Chest and both implements visible. Not a low side shot, not straight top-down.
- Same angle, zoom, lighting, bench, and machine on both frames of a lift. Only the arms and implement height change.
- The generator redraws the camera each time. After stills exist, **align and crop** to the lockout frame (scale + pan). Reject if it still looks like a different shot.

## Poses (two stills for now)

Generate **only** these two files per lift. Skip the loop.

| File | Pose |
|------|------|
| `{slug}-lockout.png` | End of the concentric: arms **fully extended**. Covering the head is fine. |
| `{slug}-bottom.png` | Start of the concentric: **stretch** (see chest barbell/Smith presses below). Covering the head is fine. Both sides in the same phase. |

Holds with no path (plank, carry) stay **one still**. Custom exercises: **no demo**.

### Chest barbell / Smith presses (stretch vs lockout)

Applies to every **bar-on-chest press**: barbell bench press, incline barbell press, flat Smith bench press, incline Smith bench press, and the same family later.

**Bottom (stretch)**

- The bar sits **on the chest**, along the mid-chest line (nipple / lower-pec line across the torso — the red guide on the reference shot). Not on the neck, not floating above the chest.
- Elbows are **about 45°** from the torso (slightly tucked). Not flared to 90° (arms straight out to the sides), not pinned tight to the ribs.

**Lockout**

- Elbows are **fully extended**. No bend in either elbow.

Dumbbell chest presses still use the same two poses (stretch at the chest, lockout with straight elbows). The bar-path / red-line rule is for a **single bar** (barbell or Smith).

**Smith vs free barbell:** a Smith press uses a bar locked to **vertical rails** (sliders, catch hooks). Do not draw a free Olympic barbell on an open bench.

Stills stay **unpainted** gray clay. Do not add lime (or any) muscle highlights on lockout, bottom, or loop frames. Main / auxiliary names on the demo screen still come from `ExerciseTargetMuscles`, not from paint on the PNG.

## Reject

- Different body, face, or shorts color/cut between frames or lifts
- Zoom or angle jump after align
- Wrong equipment (e.g. incline bench for flat DB press, a **flat** bench on an incline press, or a **free barbell** on a Smith press)
- One arm lockout while the other is at the chest
- Lime or other muscle paint on the stills
- Chest barbell/Smith **bottom**: bar off the chest, on the neck, or elbows flared ~90°
- Chest press **lockout**: any visible elbow bend

## Build the loop (later)

Do not run this until we ask for a loop. When we do:

```
py -3 tools/make_exercise_loop.py --lockout LOCKOUT.png --bottom BOTTOM.png --out LOOP.png
```

Aligns bottom to lockout, crops, writes a two-frame animated PNG.

## Files in this repo

- This playbook
- `content/exercises/README.md` — where files live
- `content/exercises/_character/reference.png` — master mannequin for every new lift
- `docs/exercise-muscle-map.md` — parked body-part IDs (not used on stills for now)
- `content/exercises/_character/muscle-map.jpg` — labeled front/back chart (reference only)
- `content/exercises/barbell-bench-press/` — unpainted `lockout.png`, `bottom.png`, `loop.png`
- `content/exercises/dumbbell-bench-press/` — unpainted `lockout.png`, `bottom.png`, `loop.png`
- `content/exercises/incline-barbell-press/` — unpainted `lockout.png`, `bottom.png` (no loop yet)
- `content/exercises/smith-bench-press/` — unpainted `lockout.png`, `bottom.png` (no loop yet)
- `content/exercises/incline-smith-bench-press/` — unpainted `lockout.png`, `bottom.png` (no loop yet)
- `content/exercises/incline-dumbbell-press/` — unpainted `lockout.png`, `bottom.png`, `loop.png`
- App copies: `app/src/main/assets/exercises/` — `{slug}_lockout.png`, `{slug}_bottom.png` (and `{slug}.png` loop when it exists) for Barbell bench press, Dumbbell bench press, Smith bench press, Incline barbell press, Incline Smith bench press, and Incline dumbbell press

Save new generations under `content/exercises/{slug}/` in this repo (`lockout.png` and `bottom.png` only until loops are requested). Do not leave the only copy in Cursor’s `assets` folder.
