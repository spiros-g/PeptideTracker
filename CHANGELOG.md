# Changelog

## 4.7.2 — in-app updates

- Added automatic background checks for new GitHub releases on sideload installations.
- Added Android notifications when a newer signed release is available.
- Added a manual update checker and Update button in Settings.
- Added in-app APK download, SHA-256 integrity validation, package/version validation and signing-certificate verification.
- Added a secure FileProvider handoff to the Android package installer, so no browser or manual APK download is required.
- Added first-use handling for Android's "Install unknown apps" permission.
- Increased app version to 4.7.2 (versionCode 32).

## 4.7.1 — launcher icon fix

- Fixed Android adaptive launcher icon cropping on OEM launchers by moving the full artwork into a safe inset foreground layer.
- Added a dedicated legacy launcher composition so older/non-adaptive launchers keep the artwork centered with margin.
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
