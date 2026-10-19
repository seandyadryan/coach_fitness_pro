# Coach Fitness PRO 1.1.0 · Animated Movement Guides

Package: `com.deploydulupulangnanti.coachfitness` · Version code: `5`.

The update adds a native Kotlin/Canvas movement illustration for every one of the 21 exercises. The athlete, equipment, movement loops and hold/breathing poses are drawn entirely on device. The app has no new permissions, network dependency or runtime asset download.

The exercise library detail page and workout-plan exercise dialogs include play/pause controls. When Android's animator setting is disabled, illustrations stay still and **Next pose** permits manual stepping. Active-session illustrations follow the workout's play/pause state, stop in the background and disappear during rest. Detached or off-screen views do not keep rendering at the normal frame rate.

Release source: `be9918c0fcec122468c136092cfb6edab1216065`.

[Build, tests and lint](https://github.com/seandyadryan/coach_fitness_pro/actions/runs/39231980806) passed. All 9 unit tests passed, including catalog coverage, finite geometry bounds, continuous loop seams, preserved curl/jack forearm lengths, stable ground supports and forearm-plank proportions. APK signing uses the same external JKS as version 1.0.0.

[Native movement QA](https://github.com/seandyadryan/coach_fitness_pro/actions/runs/39232620137) passed reduced-motion still frames/manual stepping, actual changing athlete pixels, preview play/pause/resume, session animation synchronization, background pause and the rest state. See `movement-qa.json` for the completed run's results. Runtime coverage is Android API 35; Android 16 and physical-device checks remain part of pre-production testing.

The 21 looping GIF files in `animations/` are previews rendered from the production pose model. The native MP4 there records the actual app. The added exercise-detail screenshot and refreshed session screenshot demonstrate the new UI. Existing baseline screenshots and their original broader QA report remain in the package; they were captured for version 1.0.0.

These illustrations are schematic movement aids. Written form notes remain available for every exercise; the illustrations are not motion-captured technique assessment or personalized medical guidance.
