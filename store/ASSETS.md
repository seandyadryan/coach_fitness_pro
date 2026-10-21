# Artwork and capture provenance

The original gym hero photograph was generated for this project with OpenAI ImageGen on 19 October 2026. It is used as illustrative lifestyle artwork, not as a representation of an actual trainer or a before/after claim. Source: `app/src/main/res/drawable-nodpi/training_hero.png`.

The lime dumbbell mark is an original geometric design implemented as an Android vector/adaptive icon and deterministic PNG artwork. The feature graphic combines the hero photograph and original typography/layout. `tools/prepare_assets.py` reproduces the graphic packaging.

Phone, tablet and desktop-sized screenshots are direct Android emulator screen captures from the actual Kotlin application. No invented app UI or progress graph is painted into the screenshots. The progress screen can include a short QA workout completed using the app's supported skip controls. Its duration reflects the recorded session, not an invented training duration. Desktop screenshots depict an Android app on a large landscape display; there is no separate Windows app.

The silent promotional MP4 is an approximately 25-second Android screen recording of the actual app, taken during a 30-second capture window. Its encoded duration is validated in `asset-validation.json`. All visible product and promotional copy is English. The photograph contains no text or third-party logos.

Version 1.1.0 adds 21 illustrated looping GIF previews generated from the production Kotlin pose model, plus a separate native Android recording of the movement demo. The GIFs are illustration assets rather than app screenshots. `animations/native-checks/` contains actual cropped Android view captures used to check manual pose stepping. The refreshed session screenshot and the eighth phone screenshot show the new animation UI. See `ANIMATION-RELEASE.md` and `movement-qa.json` for the corresponding verification.

Version 1.2.0 replaces the schematic athlete with photographic character textures derived from the same hero athlete, matching the user's reference screenshot. The built-in OpenAI ImageGen tool produced the transparent atlas on 21 October 2026. The source atlas, complete prompts and mechanical crop manifest are in `character/`. Twelve lossless WebP textures are bundled in the APK; no remote service is used by the app. Kotlin positions and rotates the limbs and head and bends the torso with a texture mesh. This is a 2D animated photographic puppet, not live-action footage or motion capture. The GIF previews now use exported production texture placements and the same textures.

Image requirements are checked by the capture script and summarized in `asset-validation.json`. Store listing icon is 512×512 PNG; feature artwork is 1024×500 PNG; screenshots use exact 9:16 or 16:9 aspect ratios.
