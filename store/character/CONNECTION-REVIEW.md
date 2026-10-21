# Character connection correction

The reported defect was a visible cut between the character's head and neck. Version 1.2.0 rotated separate photographic head and torso pieces. Their cut edges did not share a surface. `before-neck-gap.png` records that defect in the actual Android view.

Version 1.2.1 uses one continuous upper-body photograph per camera view, covering the head, neck, shoulders and pelvis. The face remains rigid. A smooth neck region transitions into the torso on a shared native bitmap mesh. Limbs attach to opaque points on the photograph with additional overlap at shoulders, elbows and hips. The existing exercise pose model is unchanged.

`CharacterRenderInstrumentation` renders the actual Android Canvas implementation on a transparent bitmap at eight phases of each of the 21 exercises. It checks that the head, neck, chest and limb anchors belong to the same connected region of visible pixels. The report is `native-checks/connectivity.json`; PNGs preserve every tested pose. This targets the user's visual assembly defect rather than only testing that playback changes pixels.

The unit regression also checks that the eye-to-chin distance stays constant while the body and neck bend, preventing the face from stretching with the torso. Release UI checks separately exercise native playback, manual pose stepping and session pause/resume.

The visible head socket is calibrated closer to the shoulders for consistent neck proportions. Vertex opacity blends the lower photographic pelvis into the legs, avoiding an exposed cut edge when the body rotates during floor exercises.

`CONNECTED-CHARACTER-PROMPT.md` records the built-in ImageGen prompt and source image. The GIF previews use the production mesh vertices and source textures. They are composited previews, while the PNGs under `native-checks/` are native Android renders.
