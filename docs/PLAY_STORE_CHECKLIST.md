# Google Play release checklist

## Implemented

- Application ID preserved: gr.peptidetracker.app.
- Target API 36.
- Room persistence with legacy-data migration.
- Android system cloud backup disabled.
- Optional app lock and private notifications.
- In-app privacy/safety information.
- Release signing configuration wired through local keystore.properties.
- Local signing material and Android machine-specific files excluded through .gitignore.
- Local compile.bat validation for unit tests, lint and debug APK builds.
- Local build-release.bat release gate for unit tests, release lint and signed AAB creation.
- GitHub Actions workflow retained as manual-only while hosted Actions quota is unavailable.
- Current beta contains no AdMob/UMP SDK.

## Required before public production release

- Create and protect the real Play upload/release key.
- Build and inspect a signed release AAB.
- Publish a public privacy-policy URL with real controller/business name, support email and effective date.
- Add the same privacy-policy URL in Play Console.
- Complete Google Play Data safety form from the final binary.
- Complete the Health apps declaration and relevant health/medical content declarations.
- Confirm store text does not imply diagnosis, treatment or personalized dosage recommendations.
- Add final screenshots, feature graphic, category and support contact.
- Test multiple Android versions, screen sizes and large-font accessibility.
- Test legacy-data-to-Room migration using a copy of real pre-Room data.
- Test plain/encrypted backup restore and reminder rescheduling.
- Review every peptide profile and its displayed review date before submission.

## If advertising is added later

- Add production AdMob IDs through release-only configuration.
- Implement UMP consent and privacy options before requesting ads.
- Re-check Data safety and Health declarations after adding the SDK.
- Never send peptide names, tracker entries, weight, notes, inventory or calculator values as ad-targeting parameters.