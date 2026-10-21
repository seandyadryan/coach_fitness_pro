# Coach Fitness PRO 1.2.0 · Photo Character

Package: `com.deploydulupulangnanti.coachfitness` · Version code: `6`.

All 21 exercise animations now use an articulated photographic character derived from the athlete in the user's reference screenshot and the original home-screen hero photograph. The face, dark ponytail, charcoal sports top and leggings match that reference. Twelve transparent lossless WebP textures are bundled in the APK and decoded once into a shared cache. Kotlin positions and rotates the limbs/head and deforms a continuous torso texture mesh. No permission, network dependency, asset download or data-collection behavior was added.

The exercise library detail page and workout-plan exercise dialogs retain play/pause controls. When Android animations are disabled, the character stays still and **Next pose** permits manual stepping. Active-session animations follow the workout's play/pause state, stop in the background and disappear during rest. Off-screen or detached views stop regular rendering.

Application source: `b5bc88362806dd11727dbedb4d5e8fb9bdf70c2c`. The later packaging commits change tools, documentation and exported assets only.

[Build, tests and lint](https://github.com/seandyadryan/coach_fitness_pro/actions/runs/39564388808) passed. All 10 unit tests passed: catalog coverage, finite geometry, continuous loop seams, curl/jack forearm lengths, stable hold supports, forearm-plank proportions, and photo-texture placements within the stage across 121 samples of every exercise. The debug APK and signed release APK/AAB build successfully. APK and AAB certificates match the existing upload key, SHA-256 `cca88c57dd1298d3273a9ea7bd6776ee6b6b36d286ed2ed7806839f6f337aa8c`.

[Full native Android QA](https://github.com/seandyadryan/coach_fitness_pro/actions/runs/39564960554) passed on Android API 35. The movement checks cover reduced-motion still frames/manual stepping for squat, push-up and curl; changing athlete pixels during playback; preview pause/resume; session animation synchronization; background pause; and hiding the character during rest. The full baseline also verifies routine filtering, timer controls, saved sessions, completion journaling, water persistence and data deletion.

The eight phone screenshots and four screenshots for each tablet/desktop size were captured from this release. `animations/native-checks/` contains actual Android view crops; `animations/movement-demo.mp4` records actual native playback. `movement-qa.json` records the installed version name and code. The refreshed main promotional recording has an encoded duration of 25.137 seconds. Image/video dimensions and limits are recorded in `asset-validation.json`.

The 21 GIF previews use 48 production pose samples per loop and the same photographic textures as the APK. They are animation previews, not fabricated Android screenshots. See `character/IMAGEGEN-PROMPTS.md` for the complete built-in ImageGen prompt set and source-atlas provenance, and `character/textures.json` for the mechanical crop manifest.

These are 2D animated photographic puppets, not live-action or motion-captured technique demonstrations. Written form cues remain part of every exercise. Physical-device and additional Android-version testing remain part of the pre-production checklist in `RELEASE.md`.
