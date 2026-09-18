# Peptide Tracker GR

Native, offline-first Android application in Greek, built with Kotlin, Jetpack Compose, Material 3, Room and WorkManager.

## Current beta

- Searchable peptide library with favorites, content review dates and PubMed / ClinicalTrials.gov search links.
- Reconstitution and mathematical amount-to-volume / reverse calculators for U-100 and U-40 syringes.
- Usage log with monthly calendar view, repeat-last workflow, delete undo and statistics.
- Inventory with sealed / active / empty state, remaining amount, reconstitution data, lot, vendor/source, purchase date, expiry and notes.
- Home-screen visibility for expired inventory and inventory expiring within 30 days.
- Reversible inventory accounting for linked usage logs.
- Body-weight / waist progress tracking.
- User-defined reminders with one-time or flexible 1–30 day recurrence options plus 15-minute snooze.
- Optional private notification mode.
- Optional biometric / device-credential app lock.
- Custom peptide names for tracking without generating scientific claims.
- Room database with non-destructive migration from legacy SharedPreferences data.
- JSON backup / restore with preview plus password-encrypted AES-GCM backup.
- CSV history export.
- Local validation scripts for unit tests, Android lint, debug APK builds and signed release AAB builds. The GitHub Actions workflow is manual-only while hosted Actions quota is unavailable.

The calculator performs mathematical conversions only. The app does not recommend doses, treatment protocols or personalized medical decisions.

## Data and privacy

Tracker data is stored locally in Room. Android system cloud backup is disabled. Plain JSON/CSV exports are user-initiated; encrypted backups are available for sensitive exports.

## Build

Requirements: JDK 17, Android SDK 36, Gradle 8.11.1.

Windows:

    compile.bat

compile.bat executes:

    gradle testDebugUnitTest
    gradle lintDebug
    gradle assembleDebug

The repository also keeps a manual-only GitHub Actions workflow for future use when hosted Actions quota is available.

Debug APK:

    app/build/outputs/apk/debug/app-debug.apk

## Signed release

Copy keystore.properties.example to keystore.properties and point it to your private release keystore. Never commit the real keystore or passwords.

The Gradle release build loads signing credentials only when the local keystore.properties file exists.

For a production candidate, run build-release.bat. It runs unit tests, release lint and then creates the signed AAB at app/build/outputs/bundle/release/app-release.aab.

## Advertising

The current beta intentionally contains no AdMob / UMP SDK and no advertising IDs. Monetization should be reintroduced only as a complete production integration with consent handling and production IDs.

## Scientific content

Generic PubMed and ClinicalTrials.gov links are labelled as literature/search sources rather than individual curated studies. Scientific and regulatory text includes a visible content-review date and should be re-checked periodically.