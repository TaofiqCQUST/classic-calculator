# Classic Calculator for Android

Source project, not a compiled APK. Classic iPhone-inspired portrait layout, not a pixel-perfect reproduction of every iOS release. No Apple assets or fonts included.

## Build
1. Open this project folder in Android Studio.
2. Use JDK 17 and Gradle 8.9 (compatible with Android Gradle Plugin 8.7.3). Install Android SDK 35 if prompted. Gradle wrapper files are not included; use a local Gradle installation or generate a wrapper with `gradle wrapper --gradle-version 8.9`.
3. Sync, run on an Android 6+ device, or use Build > Build App Bundle(s) / APK(s) > Build APK(s).
   Command line after wrapper generation: `./gradlew assembleDebug`.

## Features
Black background, round keys, orange operations, wide zero key, decimal entry, sign toggle, context-aware percentage, chained operations, repeat equals, divide-by-zero error handling, and saved state across activity recreation.
C clears the current entry and retains any pending operation. Hold the clear key for 600 ms to reset everything (AC). Sequential evaluation, not algebraic operator precedence. Arithmetic uses 16 significant digits; manually entered numbers are limited to 12 digits. Exponential display for large/small results.

## Privacy
No permissions, no network access, no ads, no analytics, no messaging, no hidden functions. Only in-memory/activity state; no persistent history. Android screenshot and device access protections are not added.

## Limitations
Source has not been compiled or device-tested in the generation environment. Custom-drawn keys do not provide individual TalkBack nodes; accessibility needs further work before release. Portrait only, no scientific mode, no history. System font substitutes for Apple's font. Exact layout varies by device.

## Suggested checks
2 + 3 = => 5; = => 8
0.1 + 0.2 = => 0.3
100 + 15 % = => 115
9 / 0 = => Error
Launch and return; background/recreate activity; test on small displays.
