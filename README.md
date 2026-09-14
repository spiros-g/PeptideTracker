# Peptide Tracker GR

Native offline-first Android application in Greek, built with Kotlin, Jetpack Compose and Material 3.

## Current v1

- Local peptide reference list and instant search
- Reconstitution, dosage-to-volume and reverse calculators
- U-100 and U-40 syringe conversions
- Persistent local tracker records
- Greek decimal comma support
- AdMob test application configuration (no production IDs)
- Unit tests, lint and debug APK build in GitHub Actions

The calculator performs mathematical conversions only. It does not recommend doses or treatment protocols.

## Build

Requirements: JDK 17, Android SDK 36 and Gradle 8.11.1. Open with Android Studio or run `compile.bat` on Windows. GitHub Actions runs `gradle testDebugUnitTest`, `gradle lintDebug`, and `gradle assembleDebug`.

Debug output: `app/build/outputs/apk/debug/app-debug.apk`.

## Architecture

UI is under the application package, calculation logic is isolated under `domain`, and local persistence under `data`. Keep future database migrations non-destructive and preserve `gr.peptidetracker.app` for updates.

## Production configuration

Replace Google test ad identifiers only for a signed production build. Never commit signing credentials. Copy `keystore.properties.example`, keep the real file outside version control, and configure signing before running `build-release.bat`.

## Adding peptides

Add reviewed entries to the structured peptide source. Regulatory and scientific statements require authoritative references and a review date. A later version should move the starter list to a JSON asset without coupling it to Compose UI.
