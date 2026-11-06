# Storage Access Framework (SAF) & Metadata Specification

## Overview
Android Scoped Storage prohibits arbitrary direct file path traversal across external storage. Craft Anything adopts Android's Storage Access Framework (SAF) as the primary mechanism for accessing project directories and document assets.

## Permission Acquisition Flow

1. **User Invocation**: The user requests opening a project workspace or document.
2. **Intent Launch**:
   ```kotlin
   val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
       addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
   }
   activityResultLauncher.launch(intent)
   ```
3. **Persist Permission**:
   ```kotlin
   context.contentResolver.takePersistableUriPermission(
       treeUri,
       Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
   )
   ```
4. **Metadata Caching**: Persist the `treeUri.toString()` in EncryptedSharedPreferences / Datastore alongside metadata (alias, last opened timestamp, bookmark token).

## Passing Data to Rust Core

Because the Rust engine cannot resolve Android content URIs (`content://...`) via standard `std::fs::File::open()`, we employ two interaction modes:

### Mode A: Direct ParcelFileDescriptor (Streaming)
- Kotlin opens an `AssetFileDescriptor` or `ParcelFileDescriptor` using `ContentResolver.openFileDescriptor(uri, "rw")`.
- The raw Linux file descriptor integer (`pfd.detachFd()`) is passed into Rust via JNI.
- Rust wraps the file descriptor into standard I/O:
  ```rust
  #[cfg(unix)]
  use std::os::unix::io::FromRawFd;
  let file = unsafe { std::fs::File::from_raw_fd(raw_fd) };
  ```

### Mode B: Memory Buffer / Byte Array (Small Documents & Metadata)
- For configuration files, schemas, and documents under 10MB, Kotlin reads the byte stream and passes a direct `jbyteArray` or UTF-8 `jstring` into Rust.
- Rust deserializes the JSON/binary payload directly into internal models.
