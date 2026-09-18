# Changelog

## 4.8.18 — floating glass bottom navigation

- Restored the rounded glass background, border radius and border of the in-app bottom navigation.
- Removed the dedicated full-width Scaffold bottom-bar strip behind it by rendering the navigation as a floating overlay on the app background.
- Kept content and snackbars padded so they do not sit underneath the floating navigation.
- Increased app version to 4.8.18 (versionCode 51).

## 4.8.17 — simplified syringe units

- Removed user-facing U-100 / U-40 references and the U-100 / U-40 selector.
- Standardized current calculator and inventory UI on syringe capacity and generic syringe-unit wording.
- Updated Home, inventory, saved calculations and translations to use generic units.
- Increased app version to 4.8.17 (versionCode 50).

## 4.8.16 — transparent Android navigation area

- Removed Android's forced contrast scrim behind the system navigation buttons on supported devices.
- The bottom system navigation area can now show the app background continuously instead of a separate dark strip.
- Kept edge-to-edge rendering and the in-app rounded bottom navigation unchanged.
- Increased app version to 4.8.16 (versionCode 49).

## 4.8.15 — fix Home hero card fill

- Fixed the top calculator card leaving an unpainted dark strip at the bottom.
- Moved the 156 dp minimum height from the outer GlassCard to the hero Row so the gradient/content fills the entire rounded card.
- Kept the compact card size and tap behavior unchanged.
- Increased app version to 4.8.15 (versionCode 48).

## 4.8.14 — home safe-area visual fix

- Prevented Home content from scrolling underneath the Android status bar, which could visually clip the top header after a small scroll.
- Added a larger bottom content inset so lower Home cards have cleaner breathing room above the persistent bottom navigation.
- Increased app version to 4.8.14 (versionCode 47).

## 4.8.13 — tighter home calculator card

- Reduced the Home calculator card again from a 198 dp minimum height to 156 dp.
- Removed the redundant calculator eyebrow label from the card.
- Reduced internal padding, spacing, title scale and vial artwork size for a much tighter layout.
- Kept the full-card tap target, description and calculator shortcut intact.
- Increased app version to 4.8.13 (versionCode 46).

## 4.8.12 — resilient in-app update check

- Added a second update-check path using the public GitHub Releases web redirect when the GitHub API check fails.
- The fallback derives the latest release tag from GitHub and constructs the signed APK download URL using the release naming convention.
- Increased update-check network timeouts to reduce false failures on slower mobile connections.
- Kept APK package, version and signing-certificate validation unchanged before installation.
- Added tests for fallback release-tag parsing and APK URL generation.
- Increased app version to 4.8.12 (versionCode 45).

## 4.8.11 — complete onboarding walkthrough

- Rebuilt onboarding from 3 basic slides into a 6-step feature walkthrough.
- Added clear explanations for the calculator, saved calculations, peptide library, favorites and literature links.
- Added coverage for usage history/calendar, inventory-linked deductions, active vials, expiry metadata and calculator handoff.
- Added measurements, statistics, reminders, backups, encrypted backups, restore/export, app lock, notification privacy, language/theme, custom peptides and official updates.
- Replaced the repeated large vial artwork with a compact icon-led layout and concise feature bullets so more useful information fits on each screen.
- Added step numbering and expanded Greek/English onboarding copy.
- Increased app version to 4.8.11 (versionCode 44).

## 4.8.10 — compact home calculator card

- Reduced the oversized calculator hero card on the Home screen from a 274 dp minimum height to a compact 198 dp layout.
- Reduced hero padding, vial artwork size and internal spacing so Quick access appears substantially higher on screen.
- Shortened the calculator title and supporting copy while preserving the same tap target and navigation.
- Removed the U-100 / U-40 wording from the Home hero copy.
- Increased app version to 4.8.10 (versionCode 43).

## 4.8.9 — remove default syringe preference

- Removed the Default syringe setting and its U-100 / U-40 controls from Settings.
- Removed the persisted default-syringe preference from app state and backup/restore data.
- New calculator sessions now start from the standard U-100 / 30 U capacity baseline without a hidden user preference.
- Existing saved calculations and presets keep their own stored syringe calibration for backward compatibility.
- Increased app version to 4.8.9 (versionCode 42).

## 4.8.8 — public-source protection and product README

- Replaced the technical README with a product-focused overview of what Peptide Tracker is and what it does.
- Added a proprietary source-available LICENSE and repository-wide NOTICE covering source code, branding and original assets.
- Added copyright notices to the primary application entry points and new protection code.
- Enabled R8 code shrinking/obfuscation and Android resource shrinking for release builds.
- Added release-signature integrity verification so official builds reject installations not signed with the official release certificate.
- Updated the release workflow to derive the official signing-certificate SHA-256 fingerprint from the protected keystore and inject it into signed release builds.
- Added unit coverage for signing-identity matching.
- Increased app version to 4.8.8 (versionCode 41).

## 4.8.7 — dark-theme text contrast fix

- Fixed inherited text color on root premium backgrounds so standalone screens no longer fall back to black text in dark mode.
- Fixed GlassCard to provide the correct on-surface content color to nested text and controls.
- This covers onboarding and the app-lock screen and prevents the same contrast bug in future premium-background/card content.
- Audited the current UI source for hardcoded black text; the remaining Color.Black usages are non-text scrim/image overlays.
- Increased app version to 4.8.7 (versionCode 40).

## 4.8.6 — calculator hero cleanup

- Removed the redundant U-100 / U-40 text from the calculator hero summary.
- The hero now shows only mg · mcg · mL.
- Increased app version to 4.8.6 (versionCode 39).

## 4.8.5 — update prompt and settings priority

- Checks GitHub for a newer release whenever the app is opened after onboarding.
- Replaced the blocking update dialog with an actionable in-app snackbar/toast; tapping “Update now” opens Settings.
- Moved the app-update controls to the very top of Settings, immediately below the Settings header.
- Kept the existing secure download, signature/version validation and Android installer flow unchanged.
- Increased app version to 4.8.5 (versionCode 38).

## 4.8.4 — calculator syringe selector cleanup

- Removed the redundant U-100 / U-40 selector from Calculator step 01.
- Kept the three syringe capacity choices (0.3 mL, 0.5 mL and 1.0 mL) as the only controls in that section.
- Preserved the existing U-100 / U-40 calculation behavior from Settings defaults, presets and saved calculations.
- Increased app version to 4.8.4 (versionCode 37).

## 4.8.3 — tracker selection and reconstitution polish

- Replaced the Tracker usage-entry peptide dropdown with the same fixed-height searchable picker used by the Calculator.
- Reused the shared picker in inventory and reminder editing, including the reminder recurrence selector, so legacy dropdown menus no longer appear in the main workflows.
- Kept usage-entry and reminder date/time controls on one line with stable dd/MM/yyyy and HH:mm formatting.
- Made mg the first/default quantity unit for new tracking entries and storage fallbacks.
- New tracking entries leave Amount empty instead of carrying the old 100 mcg default into mg mode.
- Reconstitution syringe capacity choices are 0.3 mL, 0.5 mL and 1.0 mL with matching U-100 (30/50/100 U) and U-40 (12/20/40 U) markings.
- Added a reusable fixed-height selection picker component for future list selectors.
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
