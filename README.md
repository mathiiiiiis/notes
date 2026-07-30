# Notes

A minimal Android notes app. Markdown, images, one note per page, stored in
sqlite. Everything stays on device, the app has no internet permission.

Stack: Kotlin, Jetpack Compose, Material 3 Expressive (light and dark, dynamic
colour on Android 12+), Room.

## Features

- Markdown with a live preview and a floating formatting toolbar
- Attach images from the system photo picker
- Pin notes, search across everything, swipe to delete with undo
- Share text from any app to create a note

## Build

```
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Release bundle, see `RELEASE.md` for the signing setup:

```
./gradlew bundleRelease -PversionName=1.0.0 -PversionCode=1
```

by mathis
