# Changelog

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
