# Coach Fitness PRO

Native Kotlin Android fitness app by **PT Deploy Dulu Pulang Nanti**.

Package: `com.deploydulupulangnanti.coachfitness` · Android 8.0+ (API 26) · Target API 36 · Version 1.0.0.

## What works

- 8 structured routines and 21 exercises with written form cues.
- Exercise countdown, 30-second rests, pause/resume, skip and completion journal.
- Unfinished sessions saved locally and paused when the app goes into the background.
- Weekly activity, total training time, session goal and current day streak.
- Daily water logging in 250 ml increments and configurable tracking target.
- Local profile preferences, complete local-data deletion, offline privacy policy and email support.
- Adaptive native Android layouts for phones, tablets and desktop-sized Android windows.

The app uses Android framework views and Kotlin; there is no WebView app shell, cloud backend, advertising, analytics SDK, subscription or network permission. Training content and interface are English. Store listing translations are provided separately.

## Build

Install JDK 17+, Android SDK Platform 36 and Build Tools 36. Create `local.properties` with `sdk.dir` if Android Studio has not created it.

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
.\gradlew.bat bundleRelease assembleRelease lintRelease
```

Release signing reads ignored `keystore.properties` or environment variables:

```properties
KEYSTORE_FILE=D:/KEYSTORE/coach_fitness_pro.jks
KEYSTORE_PASSWORD=<your password>
KEY_ALIAS=coach_fitness_pro
KEY_PASSWORD=<your password>
```

The upload key is outside this repository. Keep a secure backup of it and its credentials. Release builds fail when signing is missing. APK and AAB outputs are in `app/build/outputs/`. CI uses the run number as `versionCode`; increase it for every later Play upload.

## GitHub Actions runner

`.github/workflows/android.yml` uses GitHub-hosted Ubuntu runners. Every main-branch push runs unit tests, Android lint and a debug build, followed by a signed release AAB/APK build. Pull requests run verification without signing secrets. Download outputs from the workflow's Artifacts section.

The repository secrets are `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD`. A decoded upload key exists only in the release runner's temporary directory and is removed after the job. No self-hosted runner installation is needed.

`.github/workflows/pages.yml` publishes `docs/index.html` to GitHub Pages. The app also includes the privacy text offline. Public URL: https://seandyadryan.github.io/coach_fitness_pro/

`.github/workflows/screenshots.yml` is a manually dispatched Android emulator QA job. Supply the run ID of a successful signed Android build. It downloads that APK, boots an accelerated Linux emulator, exercises the native UI, and uploads verified screenshots, a screen recording and a QA report as an artifact.

## Play Store handoff

`store/` contains English short/full descriptions, store artwork, actual running-app screenshots, listing CSV/JSON, translation report and release guidance. See [store/TRANSLATIONS.md](store/TRANSLATIONS.md) and [store/RELEASE.md](store/RELEASE.md).

Publishing to Google Play and uploading a video to YouTube require the owner's corresponding accounts. This project does not include a Play service-account key, a YouTube URL or an automatic production publish step. A paid upfront app price is configured in Play Console, rather than in the APK. No paid-access gate is built into the app; APK sideloading does not check a Play purchase receipt.

## Development tools

`tools/prepare_assets.py` packages the original generated hero photo into store artwork and creates the geometric app mark. `tools/translate_listings.py` generates and validates listing translations. `tools/capture_screenshots.py` captures the native app from an Android emulator, exercises main flows and checks screenshot dimensions. Install `tools/requirements.txt` in Python for image processing.

Workout content is general guidance for adults, not individual medical advice. The release checklist explains the remaining store-account declarations and review work.
