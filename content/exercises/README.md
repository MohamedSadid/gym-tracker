# Exercise demo media

All stills, loops, the character reference, and the loop builder live **in this repo**. Do not leave generation output only in Cursor’s `assets` folder.

| Path | What |
|------|------|
| `docs/exercise-demo-criteria.md` | Look, camera, poses, reject list |
| `docs/exercise-muscle-map.md` | Parked body-part IDs (not painted on stills) |
| `tools/make_exercise_loop.py` | Align two stills → animated PNG |
| `content/exercises/_character/reference.png` | Master mannequin (use this as the look for every new lift) |
| `content/exercises/_character/muscle-map.jpg` | Labeled front/back muscle chart |
| `content/exercises/{slug}/` | `lockout.png`, `bottom.png`, `loop.png` |
| `app/src/main/assets/exercises/` | App copies: `{slug}.png` (loop), `{slug}_lockout.png`, `{slug}_bottom.png` |

When generating a new lift: start from `_character/reference.png`, save stills under `content/exercises/{slug}/`, then run `make_exercise_loop.py` into that folder and copy `loop.png` to `app/src/main/assets/exercises/`.
