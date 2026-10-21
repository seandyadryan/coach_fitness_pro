# Fitness photo-character animations

All 21 exercise IDs have their own offline native Kotlin/Canvas movement animation. Version 1.2.0 uses photographic textures based on the same female athlete as the home-page hero image: matching face, dark ponytail, charcoal sports top and leggings. Equipment appears where relevant. Plank and child's pose demonstrate a steady hold with gentle breathing.

The app displays these in exercise details, the workout-plan exercise dialog, and active exercise sessions. Pausing a session also pauses its illustration. Rest periods do not display an active exercise. Guide previews have independent play/pause controls. When Android animations are disabled, previews stay still and the **Next pose** button permits manual inspection. Background/detached/hidden views stop their frame loop.

The GIF files are preview assets composited from texture placements exported directly from the production Kotlin character rig, using `exportMovementFrames` and `tools/render_movement_previews.py`. They use the APK's photographic textures. They are animation previews rather than screenshots of the app. `movement-demo.mp4` is a native Android recording of the actual app. Geometry tests and native playback/reduced-motion tests are documented in the release verification report. See `../character/IMAGEGEN-PROMPTS.md` for source artwork provenance.

These are 2D animated photo-character aids, not motion-captured demonstrations or personalized technique assessment. The existing written form cues and comfortable range-of-motion guidance remain part of each exercise.
