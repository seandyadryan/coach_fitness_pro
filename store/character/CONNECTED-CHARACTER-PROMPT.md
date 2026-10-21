# Connected character correction — 1.2.1

Generated with the built-in OpenAI ImageGen tool. Reference: the original `training_hero.png`, the same athlete as the user's screenshot. Final source: `athlete-connected-upper-body.png`. The native `athlete_body_front.webp` and `athlete_body_side.webp` each contain a connected head, neck, shoulders, torso and pelvis. Mechanical crop coordinates are recorded in `textures.json` and reproduced by `tools/pack_character.py`.

## Prompt

Use case: identity-preserve.
Asset type: continuous upper-body photographic textures for a native fitness animation.
Use the reference gym photograph ONLY as the identity, clothing and lighting reference: preserve the same adult Asian athletic woman, her face, tan skin, dark ponytail, graphite racerback cropped athletic top and high-waisted charcoal leggings.
Make ONE landscape transparent PNG containing exactly TWO isolated upper-body cutouts, with a wide transparent gutter between them. LEFT: straight-on front view. RIGHT: strict right-facing profile. Each cutout extends continuously from the top of her hair through her head, neck, shoulders, entire torso, midriff and pelvis to the upper thighs. Keep the head upright in a relaxed neutral posture. Natural adult anatomy and proportions.
CRITICAL: each head, neck, chest and pelvis is ONE physically connected photographic image, with a smooth natural neck-to-head and neck-to-shoulder connection. No detached head, no seams, no gaps, no separate neck pieces, no unnaturally long neck. These textures will bend as one continuous animation mesh.
For animation layering, omit the arms beyond the shoulder sockets, and omit the legs below the upper thighs. Rounded overlap-friendly shoulder and hip silhouettes, ordinary animation character cutout, no injury details. No arm obscuring the torso in the profile view. Preserve the cropped sports top and visible midriff.
Keep the full hair and full pelvis inside each cutout, ample transparent margin around each. Both cutouts use the same scale and vertical alignment, same natural studio light with subtle emerald rim, detailed realistic skin and fabric. No dumbbells, no scene, no background, no shadows, no text, no watermark, no labels, no mannequin styling. Actual alpha transparency.

