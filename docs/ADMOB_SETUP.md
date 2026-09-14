# Future AdMob / UMP setup

The current Peptide Tracker GR beta intentionally does **not** include the Google Mobile Ads SDK, UMP SDK, AdMob App ID or ad-unit IDs.

Advertising should only be reintroduced as a complete production integration after the privacy policy, Google Play Health apps declaration and Data safety disclosures are finalized.

If advertising is added later:

- Create the production Android app in AdMob for package `gr.peptidetracker.app`.
- Keep production App IDs and ad-unit IDs outside committed source and inject them through release configuration.
- Implement UMP consent and privacy options before any ad request where required.
- Use official test IDs/test devices during development.
- Never send peptide names, tracker records, calculated amounts, weight, inventory, notes, reminders or other user-entered health-related data as ad request or targeting parameters.
- Ads must not block calculator, tracker, reminder, backup or other core features.
- Re-review the Play Console Data safety form, Health apps declaration and privacy policy before distributing the advertising-enabled build.

Do not add only the SDK or a sample App ID without the consent/privacy flow; the integration should be complete or absent.