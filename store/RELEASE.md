# Play Console release handoff

Developer: **PT Deploy Dulu Pulang Nanti**  
Support: **seandyadryan2@gmail.com**  
Title: **Coach Fitness PRO**  
Package: **com.deploydulupulangnanti.coachfitness**  
Category: **Health & Fitness**  
Default language: **English (United States)**

## Uploads

- Signed Android App Bundle: `app/build/outputs/bundle/release/app-release.aab` or GitHub Actions release artifact.
- Icon: `graphics/app-icon-512.png` (512 × 512 PNG).
- Feature graphic: `graphics/feature-graphic-1024x500.png` (1024 × 500 PNG).
- Screenshots: `screenshots/phone/`, `screenshots/tablet-7/`, `screenshots/tablet-10/`, `screenshots/desktop/`.
- Copy: `short-description.txt` and `full-description.txt`.
- Translations: `listing-import.csv`, `listing-translations.json`, `translations/`, and Fastlane metadata. See `TRANSLATIONS.md` for import limitations and review status.
- Privacy: `docs/index.html`; hosted URL https://seandyadryan.github.io/coach_fitness_pro/.
- Optional promotional video: local `video/coach-fitness-pro-preview.mp4`. Upload to your YouTube channel, set public or unlisted, turn advertising off and ensure it has no age restriction; paste the actual resulting URL into Play Console. There is no invented YouTube URL in this package.

## Account-side setup

Create the app as a paid app before publishing if you intend an upfront purchase. Set your own price, countries and tax/payment details. The APK contains no subscription or in-app purchase. Enroll in Play App Signing and use the signed AAB as the upload artifact. The supplied JKS is the upload/signing key; retain it outside Git and make a secure backup.

Complete app access (no login required), ads declaration (no ads), target audience (adults 18+), content rating, health-app declaration and Data safety. The app offers exercise/fitness features, not medical features. Do not claim medical-device certification or guaranteed outcomes.

For this build, workout data, hydration totals and preferences are processed only on device. There is no account, advertising, analytics, network permission or backend. The privacy policy separately explains user-initiated support email and browser access. Use those facts when completing the Data safety form; inspect any future SDK or feature change before reusing the declaration. See [Google's Data safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en).

Review the AI/machine translations with native speakers. The app content itself is English and each listing discloses this. English graphics are shared for all locales. Desktop images show Android running in a large landscape window, not a Windows or web application; upload to device categories your Console actually offers.

Run internal testing on physical devices before requesting production review, including Android 8, Android 16+, large fonts, TalkBack, tablet rotation, battery/background behavior and uninstall/data deletion. Automated and emulator checks in this repository do not constitute Play review approval or medical validation. Meet any testing requirements shown for your particular developer account.

The project targets API 36 under the [current target API requirements](https://developer.android.com/google/play/requirements/target-sdk). It packages no native `.so` libraries.
