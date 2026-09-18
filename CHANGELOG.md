# Changelog

## 4.7.0 — release hardening

- Fixed Room v1 → v2 migration registration so existing installations can upgrade without a missing-migration crash.
- Made usage-log and linked-inventory mutations atomic in Room to prevent partial stock/history updates.
- Added flexible user-defined reminder recurrence options from one-time through 30-day intervals.
- Added home-screen warnings for expired inventory entries and inventory expiring within 30 days.
- Added pull-request CI and release-variant compilation to the Android workflow.
- Added signing/keystore and local Android files to .gitignore.
- Increased app version to 4.7.0 (versionCode 30).

The app remains an offline-first tracking and mathematical-conversion tool. It does not generate treatment protocols or personalized dosage recommendations.
