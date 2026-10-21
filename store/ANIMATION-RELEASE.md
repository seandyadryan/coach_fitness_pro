# Coach Fitness PRO 1.2.1 · Connected Character

Package: `com.deploydulupulangnanti.coachfitness` · Version code: `9`.

This correction addresses visible gaps and exposed cut edges between the photo character's head and neck. Each camera view now uses one connected photograph for the head, neck, shoulders, torso and pelvis. The face remains rigid while a smooth neck region and torso share one native bitmap mesh. Limb roots attach to points inside the photograph and overlap at shoulders, elbows and hips. The existing exercise pose model is unchanged.

Ten transparent WebP textures are bundled for offline use. The four old separate head/torso resources have been removed. The update adds no permissions, network use, account flow or data-collection behavior. Playback, reduced-motion manual stepping and session/background pause remain available.

Application source: `5702df6952c16312599beb9c7c12688624ae20bf`. Later commits package tools, documentation and exported assets.

[Build, tests and lint](https://github.com/seandyadryan/coach_fitness_pro/actions/runs/39575595276) passed. All 11 unit tests pass, including a new face-rigidity regression and the existing catalog, geometry, loop, support and proportion checks. The signed APK and AAB build successfully. Their upload key remains the existing JKS; the APK signing certificate SHA-256 is `cca88c57dd1298d3273a9ea7bd6776ee6b6b36d286ed2ed7806839f6f337aa8c`.

[Native Android verification](https://github.com/seandyadryan/coach_fitness_pro/actions/runs/39577952062) passed on API 35. Instrumentation rendered the production Android Canvas character implementation from the debug test build at eight phases of all 21 exercises: **168 poses passed with no disconnected head/neck/chest or limb anchors**. Pixel connectivity is checked on actual native bitmap output. The same CI run produces the debug and release builds. Signed-release UI checks separately pass reduced-motion/manual stepping, changing character pixels, preview pause/resume, session synchronization, background pause and hiding movement during rest.

`character/native-checks/connectivity.json` records the installed version and the per-pose results. Its PNGs preserve each tested native pose. `character/neck-connection-comparison.png` compares the old defect with the corrected native rendering at the same push-up phase, 0.37. This is diagnostic artwork rather than a Play Store UI screenshot. See `character/CONNECTION-REVIEW.md` for the procedure and `character/CONNECTED-CHARACTER-PROMPT.md` for the built-in ImageGen source prompt.

The session, exercise-demo and profile screenshots have been refreshed for 1.2.1. Other baseline screenshots and the main promotional video were captured for 1.2.0 and show the same unchanged layouts. `animations/movement-demo.mp4` records this release's actual native playback. The 21 GIF previews use the production mesh vertices and bundled textures; they are composited previews, not app screenshots. Dimensions, native connectivity and media limits are recorded in `asset-validation.json`.

This is a 2D animated photographic character. This fix targets visual assembly; it does not claim motion capture or clinical validation. Existing written exercise cues and the physical-device release checklist remain available.
