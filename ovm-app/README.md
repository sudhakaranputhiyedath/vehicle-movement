# Office Vehicle Manager – update channel

This branch only holds updates for the **Office Vehicle Manager** Android app.
It is separate from `main` (the Vehicle Movement app), so pushing here does not run that app's build.

- `manifest.json` – latest web version (with file checksums) and latest APK.
- `web/` – the app's screens and logic. Phones download these and show **Update now**.
- `apk/OfficeVehicleManager.apk` – latest full app. Phones with an older one show **Download & install**.
