# Exercise demo stills and loops

Use this for every built-in lift. One **character**, one **camera**, **black shorts**. Two stills per lift, then one **animated PNG**.

Do not use a new person per frame. Do not use the block 3D mannequin.

## Character (same on every PNG)

- Gray anatomical clay mannequin, faceless, oval head with a neck (not a sphere / floating ball).
- Solid **black** athletic shorts, mid-thigh, no logos or stripes. Bare torso and feet.
- Dark empty studio. No text, logos, or extra props.
- Reuse this same body for **all lifts and all frames**. Generate lockout first; later poses and other exercises must match that character, shorts, and lighting.

## Camera (same on every PNG)

- **High three-quarter from above** (about 30–40°). Chest and both implements visible. Not a low side shot, not straight top-down.
- **Same angle and zoom** on every frame of a lift, and the same crop family across the library.
- The generator redraws the camera each time. After stills exist, **align and crop** to the lockout frame (scale + pan). Reject if it still looks like a different shot.

## Poses (two stills → animated PNG)

| File | Pose |
|------|------|
| `{slug}-lockout.png` | End of the concentric: arms **fully extended**. Covering the head is fine. |
| `{slug}-bottom.png` | Start of the concentric: implements at the stretch (press: DBs on the chest). Covering the head is fine. Both sides in the same phase. |

Playback: **lockout ↔ bottom**, looping (~700 ms each). File: `{slug}-loop.png` (APNG).

Holds with no path (plank, carry) stay **one still**. Custom exercises: **no demo**.

## Muscle highlights

Paint on the gray clay. Do not change pose, camera, or shorts. Keep `_character/reference.png` **unpainted**.

Region IDs (C1, D1, D3, B5, …) are in **`docs/exercise-muscle-map.md`**. Chart: `content/exercises/_character/muscle-map.jpg`. When a request names IDs, paint **only those IDs**.

**Color convention**

| Role | Color | Opacity |
|------|--------|---------|
| **Primary** (main working muscles) | Solid lime `#B8F000` | **High** — almost opaque (~90%). Must read clearly against gray clay. |
| **Auxiliary** (helpers) | Same hue, paler `#D4E88A` | **Low** — see-through wash (~40%) so it never matches primary. |
| Other muscles | Gray clay | None |

Paint **only** the muscles in the table. Abs, obliques, biceps, forearms, hips, and legs stay gray clay. Do not flood the whole arm or whole torso.

**Muscle focus (per lift)**

| Lift | Primary | Auxiliary |
|------|---------|-----------|
| Flat dumbbell bench press | Chest, front delts | Triceps |
| Incline dumbbell press | Chest, front delts | Triceps |

Add a row here when a new lift is painted.

## Reject

- Different body, face, or shorts color/cut between frames or lifts
- Zoom or angle jump after align
- Wrong equipment (e.g. incline bench for flat DB press)
- One arm lockout while the other is at the chest
- Primary and auxiliary highlights the same opacity or brightness

## Build the loop

```
py -3 tools/make_exercise_loop.py --lockout LOCKOUT.png --bottom BOTTOM.png --out LOOP.png
```

Aligns bottom to lockout, crops, writes a two-frame animated PNG.

## Files in this repo

- This playbook
- `content/exercises/README.md` — where files live
- `content/exercises/_character/reference.png` — master mannequin for every new lift
- `docs/exercise-muscle-map.md` — region IDs for painting (D3 = side / lateral delt)
- `content/exercises/_character/muscle-map.jpg` — labeled front/back chart
- `content/exercises/dumbbell-bench-press/` — `lockout.png`, `bottom.png`, `loop.png`
- `content/exercises/incline-dumbbell-press/` — `lockout-plain.png`, `bottom-plain.png`, painted `lockout.png` / `bottom.png`, `loop.png`
- App copies: `app/src/main/assets/exercises/` — `{slug}.png` (loop), `{slug}_lockout.png`, `{slug}_bottom.png` for Dumbbell bench press and Incline dumbbell press

Save new generations under `content/exercises/{slug}/` in this repo. Do not leave the only copy in Cursor’s `assets` folder.
