# Play Area
Hamleys play-zone session tracker for Android, built with Kotlin, Jetpack Compose and Material 3.

## Build
Requirements: JDK 17, Gradle 8.7, Android SDK 34.
```bash
gradle assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions
Push to `main`, or use Actions → Build Play Area APK → Run workflow. Download the `PlayArea-debug-apk` artifact.

## Runtime notes
- Sessions persist as epoch-millisecond timestamps in local SharedPreferences and are restored after reboot.
- AlarmManager uses exact-and-idle alarms when Android permits exact alarms; otherwise it falls back to inexact-and-idle alarms.
- Android 13+ notification permission is requested at launch.
- Android notification permission and exact-alarm access are controlled by device settings. Alerts can be delayed if exact alarms are not allowed.
