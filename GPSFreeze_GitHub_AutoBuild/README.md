# GPS Freeze (Android)

Android Java source project. Open in Android Studio (JDK 17, Android SDK 35), let Gradle sync, then Build > Build APK(s). Output: `app/build/outputs/apk/debug/app-debug.apk`.

## Usage
1. Install APK. Grant location permission.
2. Enable Developer options on Android and set **Select mock location app** to **GPS Freeze**.
3. Select a point on the map (requires internet) or enter latitude and longitude manually.
4. Tap **START FREEZE**. A foreground notification indicates the service is running.
5. Tap **STOP** to remove test providers.

## Limitations
- This supplies Android mock locations; it does not disable or physically freeze GPS hardware.
- Apps can detect mock locations, reject them, use IP/network information, or rely on other location sources.
- Android versions and manufacturer customizations differ; some devices may not accept network test providers.
- Foreground service may stop if the system kills it or permission is revoked. The app is not designed to evade location security checks.
- The embedded map uses Leaflet CDN and OpenStreetMap tiles and needs internet; manual coordinates work offline.
- Not compiled or tested in this environment because Android SDK/Gradle are unavailable.
