# Photographic athlete textures

Generated with the built-in OpenAI ImageGen tool on 21 October 2026. The identity reference is the existing full-resolution `training_hero.png`, which depicts the same athlete as the user's attached app screenshot. These are AI-derived photographic textures for a 2D puppet, not recorded footage of a person performing exercises.

Final atlas: `athlete-atlas.png`. Native textures: `app/src/main/res/drawable-nodpi/athlete_*.webp`. `tools/pack_character.py` performs only mechanical cropping and lossless texture packaging; `textures.json` records the crop coordinates.

## Initial generation prompt

Use case: identity-preserve.
Asset type: photorealistic 2D skeletal animation texture atlas for a native Android fitness app.
Reference image: the attached full-resolution gym photograph is the identity and clothing reference. Preserve this same adult athletic Asian woman's face, tan skin, dark tied-back ponytail, charcoal racerback cropped sports top and high-waisted charcoal leggings. Neutral studio lighting with subtle emerald rim highlight, clear photographic skin and fabric, no illustration.
Create ONE transparent 4-column by 3-row sprite atlas, landscape 2048x1536. Exactly twelve separate isolated anatomical animation cutout pieces in equal cells; NO labels, borders, shadows, background, or extra objects. Each piece centered, with generous transparent gutters on all sides. These are clean animation puppet parts, not an injury depiction.
ROW 1 left to right: (1) head with neck, straight-on face, ponytail, looking toward camera; (2) head with neck, right-facing profile, same ponytail; (3) front torso only, from base of neck across both shoulders down to hip/pelvis in leggings, charcoal sports top and exposed midriff, NO head or arms or legs; (4) right-facing side-profile torso only, same shoulder-to-pelvis extent, NO head/arms/legs.
ROW 2 left to right: (1) single bare upper arm, straight vertical, rounded shoulder cap at top and elbow at bottom; (2) single bare forearm, straight vertical, elbow at top, wrist at bottom, NO hand; (3) single upper leg in charcoal leggings, straight vertical, rounded hip at top, knee at bottom; (4) single lower leg in charcoal leggings, straight vertical, knee at top, ankle at bottom, NO foot.
ROW 3 left to right: (1) single relaxed hand, wrist at top, fingertips at bottom, natural five fingers; (2) single fist/closed grip hand, wrist at top; (3) single dark athletic sneaker with light gray sole, seen front-on, ankle at top, toes bottom; (4) single dark athletic sneaker with light gray sole, right-facing side view with ankle on left/top and toes toward right.
Maintain realistic anatomical proportions within each part. Parts independent and not touching, no stump details, flat rounded overlap-friendly ends. Every part completely inside its own cell. Clothing plain no logos. Most important: face visibly matches the woman in reference, textures are photographic, transparent background.

## Final correction prompt

Use case: precise-object-edit. Edit the attached transparent animation atlas. Keep all twelve cells, the same woman's identity, all photographic textures, outfit, lighting and all other parts unchanged. Correct ONLY row 1 column 4, the right-facing SIDE TORSO piece: remove the entire visible arm (shoulder through elbow), and reconstruct an armless side torso silhouette with the racerback crop sports top, bare midriff and leggings pelvis. The arm must be entirely absent so this piece can be animated independently with separate arm sprites. The shoulder ends at its rounded shoulder socket. This is a normal 2D animation puppet texture, no injury or stump details. Keep it in the same cell. Also ensure transparent gutters around all objects, no background, no text.

