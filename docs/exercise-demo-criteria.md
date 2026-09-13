# Exercise demo stills and loops

Use this for every built-in lift. One **character**, one **camera**, **black shorts**. **Two stills per lift** (`position-1` and `position-2`). Do **not** build the animated PNG until asked.

Do not use a new person per frame. Do not use the block 3D mannequin.

## Character (same on every PNG)

- Gray anatomical clay mannequin, faceless, oval head with a neck (not a sphere / floating ball).
- Solid **black** athletic shorts, mid-thigh, no logos or stripes. Bare torso and feet.
- Dark empty studio. No text, logos, or extra props.
- Reuse this same body for **all lifts and all frames**. Later poses and other exercises must match that character, shorts, and lighting.

## Camera (same on every PNG)

- **High three-quarter from above** (about 30–40°). Chest and both implements visible. Not a low side shot, not straight top-down.
- Same angle, zoom, lighting, bench, and machine on both frames of a lift.
- Both stills are **1152 × 864** (4:3). Export or crop to that size before saving. Do not leave a square frame on one pose and 4:3 on the other.
- The generator redraws the camera each time. After stills exist, **align and crop** to the locked frame (usually position 1; for Push-up, position 2). Reject if it still looks like a different shot.

## How to make the two stills

Always work in this order. Do not generate position 2 from a blank prompt.

1. Create **position 1** (start of the concentric / stretch). Lock camera, zoom, crop, lighting, shadows, machine, seat, torso, hips, and legs.
2. **Duplicate** that file as **position 2**.
3. Change **only** the body part that moves for this lift, and the implement that has to follow it (bar, dumbbells, handles, cables). Everything else stays identical: camera, zoom, **lighting, brightness, contrast, shadows**, mannequin body, posture of the unmoving parts, and setup.

The two photos must look like the same frame with one motion applied. If the camera, zoom, shadows, **exposure**, or torso jumped, reject and redo step 2–3 from the position-1 file. **Do not** add a darker overlay, vignette, or extra shadow pass on position 2.

## Poses (two stills for now)

Generate **only** these two files per lift. Skip the loop.

| File | Pose |
|------|------|
| `{slug}-position-1.png` | Start of the concentric: **stretch**. Covering the head is fine. Both sides in the same phase. |
| `{slug}-position-2.png` | End of the concentric: peak / arms **fully extended** where the lift calls for it. Covering the head is fine. Duplicate of position 1 except the moving parts. |

Holds with no path (plank, carry) stay **one still**. Custom exercises: **no demo**.

### Chest barbell / Smith presses (stretch vs peak)

Applies to every **bar-on-chest press**: barbell bench press, incline barbell press, flat Smith bench press, incline Smith bench press, and the same family later.

**Position 1 (stretch)**

- The bar sits **on the chest**, along the mid-chest line (nipple / lower-pec line across the torso — the red guide on the reference shot). Not on the neck, not floating above the chest.
- Elbows are **about 45°** from the torso (slightly tucked). Not flared to 90° (arms straight out to the sides), not pinned tight to the ribs.

**Position 2 (peak)**

- Duplicate of position 1. Change only the arms and bar height.
- Elbows are **fully extended**. No bend in either elbow.

Dumbbell chest presses still use the same two poses (stretch at the chest, peak with straight elbows). The bar-path / red-line rule is for a **single bar** (barbell or Smith).

**Smith vs free barbell:** a Smith press uses a bar locked to **vertical rails** (sliders, catch hooks). Do not draw a free Olympic barbell on an open bench.

### Pec deck / machine fly

Machine: **overhead-pivot pec deck** — two arms hang from cams at the top of the frame, ending in vertical **handles** (not large forearm pads, not a chest-press machine). Weight stack on the side.

**Position 1 (stretch):** seated; handles open wide to the sides; slight elbow bend at about shoulder height.

**Position 2 (peak):** **copy position 1**. Change **only** the arms and handles: bring them **together in front of the chest** with elbows **fully straight** (no bend). On the stack, **4–5 plates** lift to about **shoulder height**; the **remaining plates stay down** on the base. Chrome guide rods stay visible between the lifted plates and the plates left behind. Do not change camera, lighting, shadows, seat, or machine.

### Cable fly (seated dual-cable)

Machine: **dual-pulley / functional-trainer** frame with an **upright 90° seat** between the stacks (horizontal seat pad + vertical back pad, like a chair — not a flat bench, not an incline). D-handles on cables at about mid-chest height. Not a pec deck, not a chest-press machine.

**Position 1 (stretch):** seated tall against the 90° back pad; arms open wide to the sides at about shoulder height; slight elbow bend; cables pulled from the sides.

**Position 2 (peak):** **copy position 1**. Change **only** the arms, handles, and cable lines: both arms reach **straight forward** at **mid-chest height** with elbows **fully extended** (no bend). Each **hand grips a D-handle** (handles are in the fists, not at the elbows). Hands meet in front of the chest. Seat, torso, legs, camera, zoom, lighting, and shadows stay identical.

### Chest press machine

Machine: **seated lever chest press** — upright 90° seat. Two **horizontal** press handles on front lever arms. Weight stack beside the seat, fully down on position 1. **Not a pec deck**.

**Position 1:** settle / stretch. Machine down and relaxed. Mannequin sitting, holding both **horizontal** handles at the chest, elbows bent. Stack fully down.

**Position 2:** **copy position 1**. Same camera, zoom, lighting, brightness, and shadows — no darker overlay. The overhead **lever arms rotate up a little** around their pivot until the handles sit about **head height** (the marked end of the axis), not above the head. Elbows **fully extended**. Hands stay on both horizontal handles. Stack rises with the levers. Do not draw guide lines on the PNG.

### Push-up

No equipment. Floor only. Same gray clay mannequin, black shorts, high three-quarter camera.

For this lift the **position 2** still is the locked camera. **Position 1** is a duplicate of that frame with only the arms and body height changed.

**Position 1 (stretch):** copy of position 2. Hands and feet stay planted. The body line (heels → hips → chest) drops along the same diagonal as the locked plank, until the chest is **almost touching the floor**. Upper arms **45° from the torso** (tucked, not flared 90°). Elbow **joint** about **45°** (more bent than a 90° push-up). Hips neither sagging off that line nor piked.

**Position 2 (peak):** high plank, elbows **fully extended** (~180°). Same camera, zoom, lighting, brightness, and shadows — no darker overlay.

Stills stay **unpainted** gray clay. Do not add lime (or any) muscle highlights on position 1, position 2, or loop frames. Main / auxiliary names on the demo screen still come from `ExerciseTargetMuscles`, not from paint on the PNG.

## Reject

- Different body, face, or shorts color/cut between frames or lifts
- Zoom or angle jump after align
- Wrong canvas size (not **1152 × 864**)
- Position 2 redrawn as a new scene instead of a duplicate of position 1
- Position 2 darker, more contrasty, or a different shadow pass than position 1
- Wrong equipment (e.g. incline bench for flat DB press, a **flat** bench on an incline press, a **free barbell** on a Smith press, a **chest-press pad machine** posed as a pec deck, a **pec deck** posed as a cable fly, a **flat bench** on cable fly instead of a 90° upright seat, or a **pec deck / cable fly** posed as a chest press machine)
- One arm at peak while the other is at the stretch
- Lime or other muscle paint on the stills
- Chest barbell/Smith **position 1**: bar off the chest, on the neck, or elbows flared ~90°
- Chest press **position 2**: any visible elbow bend
- Cable fly **position 2**: any elbow bend, or D-handles not gripped in the hands (e.g. stuck on the elbows)
- Push-up **position 2**: any visible elbow bend, knees on the floor, or a pike hip

## Build the loop (later)

Do not run this until we ask for a loop. When we do:

```
py -3 tools/make_exercise_loop.py --position-1 POSITION-1.png --position-2 POSITION-2.png --out LOOP.png
```

Aligns position 2 to position 1, crops, writes a two-frame animated PNG.

## Files in this repo

- This playbook
- `content/exercises/README.md` — where files live
- `content/exercises/_character/reference.png` — master mannequin for every new lift
- `docs/exercise-muscle-map.md` — parked body-part IDs (not used on stills for now)
- `content/exercises/_character/muscle-map.jpg` — labeled front/back chart (reference only)
- `content/exercises/barbell-bench-press/` — unpainted `position-1.png`, `position-2.png`, `loop.png`
- `content/exercises/dumbbell-bench-press/` — unpainted `position-1.png`, `position-2.png`, `loop.png`
- `content/exercises/incline-barbell-press/` — unpainted `position-1.png`, `position-2.png`, `loop.png`
- `content/exercises/smith-bench-press/` — unpainted `position-1.png`, `position-2.png`, `loop.png`
- `content/exercises/incline-smith-bench-press/` — unpainted `position-1.png`, `position-2.png`, `loop.png`
- `content/exercises/incline-dumbbell-press/` — unpainted `position-1.png`, `position-2.png`, `loop.png`
- `content/exercises/pec-deck-fly/` — unpainted `position-1.png`, `position-2.png`, `loop.png`
- `content/exercises/cable-fly/` — unpainted `position-1.png`, `position-2.png`, `loop.png`
- `content/exercises/chest-press-machine/` — unpainted `position-1.png`, `position-2.png`, `loop.png`
- `content/exercises/push-up/` — unpainted `position-1.png`, `position-2.png`, `loop.png`
- App copies: `app/src/main/assets/exercises/` — `{slug}_position_1.png`, `{slug}_position_2.png` (and `{slug}.png` loop when it exists) for Barbell bench press, Dumbbell bench press, Smith bench press, Incline barbell press, Incline Smith bench press, Incline dumbbell press, Pec deck fly, Cable fly, Chest press machine, and Push-up

Save new generations under `content/exercises/{slug}/` in this repo (`position-1.png` and `position-2.png` only until loops are requested). Do not leave the only copy in Cursor’s `assets` folder.
