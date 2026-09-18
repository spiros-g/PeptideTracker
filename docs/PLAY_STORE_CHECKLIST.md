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
- GitHub Actions validation for unit tests, Android lint and debug/release builds.
- Signed GitHub release workflow for APK/AAB publication.
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
- Run a successful CI validation build and commit the generated Room schema JSON under app/schemas.
- Test legacy-data-to-Room migration using a copy of real pre-Room data.
- Test plain/encrypted backup restore and reminder rescheduling.
- Review every peptide profile and its displayed review date before submission.

## If advertising is added later

- Add production AdMob IDs through release-only configuration.
- Implement UMP consent and privacy options before requesting ads.
- Re-check Data safety and Health declarations after adding the SDK.
- Never send peptide names, tracker entries, weight, notes, inventory or calculator values as ad-targeting parameters.