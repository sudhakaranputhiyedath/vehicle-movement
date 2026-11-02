# Vehicle Movement (Android)

Office app to record vehicle movements: vehicle number, driver name, place, out time and in time.
All phones share one Supabase database, so every phone sees the same list.

## Install
Open the **Releases** page of this repository on the phone, download `VehicleMovement.apk`, and open it.
Android will ask to allow installing apps from this source – allow it once.

## Use
- **Vehicle Out**: enter vehicle number, driver name, place → tap **VEHICLE OUT** (time is "now" unless you tap *Change time*).
- **Vehicle In**: in *Vehicles outside*, tap **IN** next to the vehicle → *Now* or *Pick time*.
- **History**: search by vehicle, driver, place or date.
- Menu → **Export to Excel (CSV)** saves all trips as a file that opens in Excel / Google Sheets.

## Build
Every push to `main` builds the APK with GitHub Actions (`.github/workflows/build-apk.yml`)
and publishes it as the `latest` release.
