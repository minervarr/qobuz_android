# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build commands

### Rust FFI library (cross-compile for Android)

```bash
cd qobuz_ffi

# arm64-v8a (physical devices)
cargo build --release --target aarch64-linux-android

# x86_64 (emulator)
cargo build --release --target x86_64-linux-android
```

After building, copy the `.so` outputs to `app/src/main/jniLibs/<abi>/libqobuz_ffi.so`.

### Android app

Build via Android Studio or Gradle:

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

NDK version required: `29.0.14206865` (set in `app/build.gradle`).

## Architecture

This is an Android app that downloads and searches Qobuz music via a Rust FFI layer.

### Call chain

```
Kotlin (MainActivity / QobuzNative.kt)
  → JNI (app/src/main/cpp/jni_bridge.cc)
    → Rust cdylib (qobuz_ffi/src/lib.rs)
      → qobuz-api (streamer/qobuz-api) — HTTP, auth, download, metadata
```

### Key components

- **`qobuz_ffi/`** — Rust `cdylib` that exposes three C functions: `qobuz_init_android`, `qobuz_search`, `qobuz_download`. All return `*mut c_char` (JSON or `"ok"` on success, `"error: ..."` on failure); caller frees with `qobuz_free_string`. Panics are caught and returned as error strings.
- **`app/src/main/cpp/jni_bridge.cc`** — Thin C++ shim that translates JNI types to C strings and calls the Rust functions. The shared library loaded by Kotlin is `qobuz_test` (not `qobuz_ffi`); CMake links `qobuz_ffi` as a prebuilt `.so`.
- **`app/src/main/java/io/nava/qobuz_test/QobuzNative.kt`** — Kotlin singleton wrapping the JNI calls. Returns `Result<T>`; checks for `"error:"` prefix in the raw string before parsing JSON.
- **`streamer/qobuz-api/`** — Local library crate (git submodule) containing all Qobuz API logic. See `streamer/CLAUDE.md` for its internals.

### Credentials flow

Credentials (`app_id`, `app_secret`, `user_id`, `auth_token`) are stored in `SharedPreferences` (see `Prefs.kt`) and passed on every FFI call — there is no persistent session in the Rust layer. `qobuz_init_android` must be called once at startup (from `App.kt`) to initialize TLS on Android via `rustls-platform-verifier`.

### Quality values

`"mp3"` | `"flac"` | `"flac-hi"` | `"flac-ultra"` — passed to `qobuz_download` and mapped to Qobuz format IDs (5, 6, 7, 27) in `qobuz_ffi/src/lib.rs`.

### ABI splits

The app is built as per-ABI APKs (`arm64-v8a` and `x86_64`). The prebuilt `.so` files in `app/src/main/jniLibs/` must match the Rust build outputs.
