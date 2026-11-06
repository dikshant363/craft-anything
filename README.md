# Craft Anything

Craft Anything is an open, modular creation and development platform built for Android. It combines a native Jetpack Compose Android UI, a high-performance cross-platform Rust core engine via JNI, and robust Storage Access Framework (SAF) integration.

## Architecture Overview

```
┌────────────────────────────────────────────────────────┐
│            Android Application (Kotlin / Compose)       │
│  - UI Presentation Layer (Material 3)                  │
│  - Storage Access Framework (SAF) URI & Permission Mgr │
│  - JNI Native Bridge (`NativeBridge.kt`)               │
└───────────────────────────┬────────────────────────────┘
                            │ JNI Interface
                            ▼
┌────────────────────────────────────────────────────────┐
│              Craft Core Engine (Rust crate)            │
│  - Document parsing, transformation, and metadata      │
│  - In-memory workspace cache & state manager           │
│  - Extensible plugin / action pipeline                 │
└────────────────────────────────────────────────────────┘
```

- **Local Tablet Development**: Author code, run unit tests, and manage git using the AntiGem mobile IDE.
- **Continuous Integration**: Cloud-based reproducible builds, linting, and APK generation via **GitHub Actions**.

## Directory Structure

- `android/` — Android application source code (Gradle, Kotlin, Jetpack Compose).
- `craft-core/` — High-performance Rust core library and JNI bindings.
- `docs/` — Architecture specifications, SAF integration guides, and CI documentation.
- `.github/workflows/` — Automated build and test workflows for GitHub Actions.

## Development & Testing

### Running Rust Core Tests Locally
```bash
cargo test --workspace
```

### Building the Android APK
Building the Android application is offloaded to GitHub Actions on each push and pull request to `main`. Download the generated APK artifacts directly from the **Actions** tab in GitHub.

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.
