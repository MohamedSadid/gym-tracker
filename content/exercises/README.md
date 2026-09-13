# Exercise demo media

All stills, loops, the character reference, and the loop builder live **in this repo**. Do not leave generation output only in Cursor’s `assets` folder.

| Path | What |
|------|------|
| `docs/exercise-demo-criteria.md` | Look, camera, poses, reject list |
| `docs/exercise-muscle-map.md` | Parked body-part IDs (not painted on stills) |
| `tools/make_exercise_loop.py` | Align two stills → animated PNG |
| `content/exercises/_character/reference.png` | Master mannequin (use this as the look for every new lift) |
| `content/exercises/_character/muscle-map.jpg` | Labeled front/back muscle chart |
| `content/exercises/{slug}/` | `position-1.png` and `position-2.png` at **1152 × 864**, plus `loop.png` when asked |
| `app/src/main/assets/exercises/` | App copies: `{slug}.png` (loop), `{slug}_position_1.png`, `{slug}_position_2.png` |

When generating a new lift: start from `_character/reference.png`, save **`position-1.png` first**, duplicate it to **`position-2.png`**, then change only the moving body part. Do **not** run the loop script until asked. When a loop is requested, run `make_exercise_loop.py` into that folder and copy `loop.png` to `app/src/main/assets/exercises/`.
