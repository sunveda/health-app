# Native Android app

This directory contains the Kotlin and Jetpack Compose source for the native Android application. It is intentionally separate from the iOS application while sharing the repository’s contracts and security decisions.

## Project setup

Open `apps/android` in Android Studio and allow Gradle to sync. The current application ID is the placeholder `com.sunveda.healthapp`. Release signing, Play App Signing, OAuth redirect configuration, network security policy, and production service endpoints must be supplied later and are intentionally not committed.

## Native capability boundaries

Health Connect, Android Keystore, BiometricPrompt, document providers, NFC, and the approved OIDC/PKCE flow should be implemented behind typed adapters. The UI must represent unavailable, denied, restricted, and error states explicitly. No health, identity, or financial values should be written to logs.
