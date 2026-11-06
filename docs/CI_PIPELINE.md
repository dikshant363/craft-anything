# Continuous Integration & Build Pipeline Specification

## Principles
1. **Zero Device Burden**: All CPU/RAM-heavy Android compilation and multi-ABI NDK builds occur on GitHub Actions runners.
2. **Deterministic Artifacts**: Builds run against fixed JDK, Android NDK, and Rust versions to avoid toolchain divergence.
3. **Artifact Availability**: Every commit or pull request to `main` produces a debug APK artifact available for immediate download and installation on the user's tablet.

## Workflow Pipeline Architecture

```
GitHub Push / PR
       │
       ├── Step 1: Rust Lint & Unit Tests (`cargo clippy`, `cargo test`)
       ├── Step 2: Cross-compile Rust for Android ABIs (`cargo-ndk`)
       │     ├── aarch64-linux-android
       │     ├── armv7-linux-androideabi
       │     └── x86_64-linux-android
       ├── Step 3: Set up JDK 17 & Android SDK
       ├── Step 4: Assemble Android APK (`./gradlew assembleDebug`)
       └── Step 5: Upload APK artifact to GitHub Run Artifacts
```
