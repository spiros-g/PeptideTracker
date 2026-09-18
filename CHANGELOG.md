# Changelog

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
