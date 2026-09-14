# AdMob and UMP setup

The debug build uses Google's official sample App ID. Create the production Android app in AdMob with package `gr.peptidetracker.app`, create adaptive banner/native/interstitial units, and keep production IDs outside committed source. Configure the EEA consent message in AdMob Privacy & messaging and initialize UMP before requesting production ads. Test with official test IDs or registered test devices only. Ads must never receive tracker, dose, weight, vial, or note data and must never block core features when offline or unavailable.
