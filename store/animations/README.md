# Fitness movement illustrations

All 21 exercise IDs have their own offline native Kotlin/Canvas movement illustration. The examples use a simplified articulated athlete, equipment where relevant, and slow repeating motion. Plank and child's pose demonstrate a steady hold with gentle breathing.

The app displays these in exercise details, the workout-plan exercise dialog, and active exercise sessions. Pausing a session also pauses its illustration. Rest periods do not display an active exercise. Guide previews have independent play/pause controls. When Android animations are disabled, previews stay still and the **Next pose** button permits manual inspection. Background/detached/hidden views stop their frame loop.

The GIF files are preview assets rendered from geometry exported directly from the production Kotlin model, using `exportMovementFrames` and `tools/render_movement_previews.py`. They are illustrations rather than screenshots of the app. `movement-demo.mp4` is a native Android recording of the actual app. Geometry tests and native playback/reduced-motion tests are documented in the release verification report.

These are schematic movement aids, not motion-captured demonstrations or personalized technique assessment. The existing written form cues and comfortable range-of-motion guidance remain part of each exercise.
