# Signed build verification

Historical verification for version 1.0.0. See `ANIMATION-RELEASE.md` for version 1.1.0 and its native movement QA.

Release source: `941bf1eee922490e844ec8582d95de00cf90998a` (later commits package documentation, assets and capture tooling only).

GitHub Actions run: https://github.com/seandyadryan/coach_fitness_pro/actions/runs/39214476702

- `verify` job: passed unit tests, Android debug lint and debug APK build.
- `release` job: passed release lint and produced signed APK and Android App Bundle.
- Version name: `1.0.0`; version code: `3`.
- Application ID: `com.deploydulupulangnanti.coachfitness`.
- Minimum API: `26`; target and compile API: `36`.
- APK verified successfully with Android SDK `apksigner`.
- AAB verified successfully with JDK `jarsigner`. Self-signed developer-certificate and no-timestamp warnings are expected for an Android upload key; this is not a public website TLS certificate.
- Signing subject: `CN=PT Deploy Dulu Pulang Nanti, O=PT Deploy Dulu Pulang Nanti, C=ID`.
- Signing certificate SHA-256: `cca88c57dd1298d3273a9ea7bd6776ee6b6b36d286ed2ed7806839f6f337aa8c`.
- APK declares no Android permissions, including no network permission.

The signing JKS and local credentials are excluded from Git. GitHub uses repository Secrets to restore the key on the hosted release runner. No Google Play publication is performed automatically.

Native emulator QA passed at https://github.com/seandyadryan/coach_fitness_pro/actions/runs/39215767288 using the signed release APK on Android API 35. The run verified workout filtering, session start/discard/completion, manual pause/resume, automatic background pause, process-restart persistence, water logging/persistence, and cancellation/confirmation of data deletion. It captured 19 real app screenshots and an approximately 25-second MP4. The exact checks are in `qa-results.json`. Pixel dimensions, aspect ratios, file sizes, translation limits and canonical support details are independently validated in `asset-validation.json`. Physical-device coverage and Android API 36 runtime coverage remain part of the owner's pre-production testing.
