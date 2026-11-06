# Craft Anything — Architecture Design Document (ADR 001)

## Status
Approved (Phase 0)

## Context
Craft Anything requires a high-performance, modular runtime that operates on modern Android devices (Android 10+ / API 29+). Android development on mobile tablets (via AntiGem) benefits from separating responsibilities between local authoring/testing and cloud CI compilation.

## Architectural Principles

### 1. Engine Separation (Kotlin UI + Rust Core)
- **Kotlin / Jetpack Compose UI**:
  - Handles the UI lifecycle, Material 3 design, Android lifecycle events, and OS integration (SAF, system intents, notifications).
  - Interacts with the core engine solely via a thin, asynchronous JNI bridge (`NativeBridge`).
- **Rust Core (`craft-core`)**:
  - Encapsulates domain logic: document parsing, file tree indexing, metadata extraction, serialization, and state mutations.
  - Zero Android dependencies inside the core logic; pure Rust guarantees cross-platform testability without needing an Android emulator or device runtime for unit tests.
  - Exposes C-compatible JNI entry points for Android native integration.

### 2. File Access via Storage Access Framework (SAF)
- Directly accessing `/storage/emulated/0` via POSIX `java.io.File` paths is restricted by Android Scoped Storage.
- Craft Anything interacts with external user storage via `Intent.ACTION_OPEN_DOCUMENT_TREE` and persistent URI permissions (`takePersistableUriPermission`).
- File descriptors are passed across the JNI boundary via Android `ParcelFileDescriptor.detachFd()` or stream buffers to allow Rust to read/write without needing raw filesystem paths.

### 3. Cloud Build Pipeline (GitHub Actions)
- Building an Android APK requires Java 17+, Android SDK Platform 34+, Build-Tools, NDK, and CMake.
- To avoid battery drain, memory exhaustion, and Android 12 Phantom Process Killer termination on mobile devices, all APK artifact building and NDK multi-ABI linking are executed in GitHub Actions.
- Local mobile development runs `cargo test` for the core engine and verifies syntax.

## Data Flow Diagram

```
User Action (Compose UI)
          │
          ▼
StorageManager (SAF URI Resolution)
          │ File Descriptor / Byte Buffers
          ▼
NativeBridge (JNI Binding)
          │
          ▼
craft-core (Rust Engine)
  ├── Parser & Validator
  ├── In-Memory State Cache
  └── Serializer
          │ Result / Event
          ▼
StateFlow / Compose State Update
```
