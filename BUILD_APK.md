# Khannum Android — Build APK

This project uses Android Gradle Plugin 8.7.3 and Gradle 8.9.

## Android Studio
Open this folder as an existing Gradle project. Android Studio can sync the project and use `gradlew.bat`.

## Windows terminal
Run:

    gradlew.bat assembleRelease

APK output:

    app\build\outputs\apk\release\app-release.apk

## Appcircle
Use the repository root as the build directory and build the `release` variant. The included launcher downloads Gradle 8.9 if the build environment does not already provide it.

The release signing configuration reads these properties from `gradle.properties`:
- KHANNUM_KEYSTORE
- KHANNUM_KEY_ALIAS
- KHANNUM_KEYSTORE_PASSWORD
- KHANNUM_KEY_PASSWORD

Keep the keystore and passwords private for a real production app.
