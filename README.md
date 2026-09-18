# Peptide Tracker

Native, offline-first Android app built with Kotlin, Jetpack Compose, Material 3, Room and WorkManager.

## Highlights

- Greek and English UI based on the device language.
- Searchable peptide library with favorites, review dates and literature links.
- Reconstitution and mathematical conversion tools for U-100 and U-40 syringes.
- Usage log, calendar, inventory, body measurements, statistics and reminders.
- Local-first storage with Room and non-destructive migrations.
- JSON, encrypted AES-GCM backup/restore and CSV export.
- Optional biometric/device-credential lock.
- Signed GitHub releases with in-app update checks and installer handoff.
- No dose recommendations, treatment protocols or personalized medical decisions.

## Build

Requirements:

- JDK 17
- Android SDK 36
- Gradle 8.11.1

Validation:

    gradle testDebugUnitTest
    gradle lintDebug
    gradle assembleDebug

Release validation:

    gradle testDebugUnitTest
    gradle lintRelease
    gradle assembleRelease bundleRelease

The signed release workflow uses GitHub Actions secrets and publishes APK/AAB assets to GitHub Releases.

## Signing

Copy `keystore.properties.example` to `keystore.properties` only for local signed builds and point it to the private release keystore.

Never commit the real keystore or passwords.

## Data and privacy

Tracker data stays on-device. Android cloud backup is disabled. Plain JSON/CSV exports are user initiated, and encrypted backups are available for sensitive exports.

## Advertising

The app currently contains no AdMob/UMP SDK and no advertising IDs.

## Scientific content

Literature links are provided as research references. Scientific and regulatory summaries include review dates and should be re-checked periodically.
