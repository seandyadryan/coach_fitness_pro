# Store listing translations

`listing-import.csv` (UTF-8 BOM), `listing-translations.json`, individual locale JSON files, and `fastlane/metadata/android/<locale>/` contain the same listing text. The language list follows [Google Play's supported languages](https://support.google.com/googleplay/android-developer/answer/9844778?hl=en), checked 19 October 2026. See `translation-report.json` for the exact completion count.

English is the source. Other languages are machine translations generated with Google Translate, except Romansh, which uses an AI-authored override because the endpoint does not support it. These are not human-reviewed translations or a claim of Play Console AI approval. Regional variants share base-language translations. Have native speakers review before applying them. In-app training content remains English; these files translate the store listing.

## Apply the prepared files

- In Play Console → Grow users → Store presence → Main store listing, add the desired languages and paste title, short description and full description from each locale file.
- If your Console offers a file-import dialog, export its current template first and map the CSV columns into that template. This CSV is a portable interchange format; the Console UI does not document a universal CSV schema for every import/AI workflow. Do not upload it assuming compatibility.
- For automation, the prepared `fastlane/metadata/android` layout can be used with `fastlane supply` and an authorized Google Play service account after the app is created. No Play service-account key is included. Run metadata-only uploads with APK, AAB, images and screenshots upload disabled until you intend to upload those files.
- Alternatively use Play Console → Translations → Store listings and products to generate/review its available machine translations. Google controls which languages are offered there; an import file cannot turn on all AI options.

All listing images are intentionally English and shared across locales. Source short/full descriptions are in `store/short-description.txt` and `store/full-description.txt`. Both character limits are checked during generation. Re-run `tools/translate_listings.py` after editing the source. Only public listing copy is sent to the translation service.

The final legal/contact block preserves the exact developer name and support email in every locale, because machine translation can otherwise transliterate or alter them. Both are validated in the release package checks.
