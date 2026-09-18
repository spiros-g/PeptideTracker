# Changelog

## 4.8.3 — tracker peptide picker polish

- Replaced the Tracker usage-entry peptide dropdown with the same fixed-height searchable picker used by the Calculator.
- Reused the shared peptide picker in inventory editing for consistent selection UX across the app.
- Kept the usage-entry date and time controls on one line with stable dd/MM/yyyy and HH:mm formatting.
- Increased app version to 4.8.3 (versionCode 36).

## 4.8.2 — calculator picker polish

- Refined the peptide selector into a compact fixed-height searchable picker.
- Keeps the peptide list inside a dedicated 360dp scroll area instead of an oversized dropdown.
- Improved selection highlighting, search behavior, empty state and spacing for mobile screens.
- Increased app version to 4.8.2 (versionCode 35).

## 4.8.1 — searchable peptide picker

- Replaced the calculator's oversized peptide dropdown with a fixed, polished picker.
- Added a persistent search field with instant filtering.
- Added a bounded scrollable peptide list that works cleanly on mobile screens.
- Highlights the currently selected peptide and keeps it in view when the picker opens.
- Added proper empty-search state and Greek/English picker copy.
- Increased app version to 4.8.1 (versionCode 34).


## 4.8.0 — multilingual cleanup

- Rebranded user-facing names from Peptide Tracker GR to Peptide Tracker while preserving the Android application ID for upgrade compatibility.
- Added Greek and English localization across the UI, notifications, updater, tracker tools and peptide library.
- Added an in-app language selector for System, Greek and English.
- Removed obsolete Windows compile/release batch scripts and dead launcher-artwork chunks.
- Cleaned the Android validation workflow and debug artifact naming.
- Increased app version to 4.8.0 (versionCode 33).


## 4.7.2 — in-app updater

- Added automatic background checks for new GitHub releases and Android notifications when an update is available.
- Added an Updates section in Settings with manual update checking and one-tap in-app download.
- Added secure APK validation before installation: package name, version, GitHub SHA-256 when available, and signing-certificate match.
- Added the Android unknown-source permission flow and FileProvider handoff so the system installer opens directly after the in-app download.
- Increased app version to 4.7.2 (versionCode 32).


## 4.7.1 — launcher icon fix

- Rebuilt the Android launcher icon as a true adaptive icon: the background and molecule foreground are now separate layers.
- Kept the molecule entirely inside the adaptive-icon safe zone so OEM circle/squircle masks cannot crop it.
- Added Android 13+ monochrome launcher support for themed icons.
- Increased app version to 4.7.1 (versionCode 31).

## 4.7.0 — release hardening

- Fixed Room v1 → v2 migration registration so existing installations can upgrade without a missing-migration crash.
- Made usage-log and linked-inventory mutations atomic in Room to prevent partial stock/history updates.
- Added flexible user-defined reminder recurrence options from one-time through 30-day intervals.
- Added home-screen warnings for expired inventory entries and inventory expiring within 30 days.
- Hardened local release validation: tests, release lint and signed AAB generation; hosted GitHub Actions remains manual-only while quota is unavailable.
- Added signing/keystore and local Android files to .gitignore.
- Increased app version to 4.7.0 (versionCode 30).

The app remains an offline-first tracking and mathematical-conversion tool. It does not generate treatment protocols or personalized dosage recommendations.
