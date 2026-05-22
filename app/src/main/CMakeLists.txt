cmake_minimum_required(VERSION 3.22.1)
project(qobuz_test)

set(NATIVE_APP_GLUE ${ANDROID_NDK}/sources/android/native_app_glue)

add_library(qobuz_test SHARED
    cpp/main.cc
    ${NATIVE_APP_GLUE}/android_native_app_glue.c
)

target_include_directories(qobuz_test PRIVATE
    ${NATIVE_APP_GLUE}
    ${CMAKE_CURRENT_SOURCE_DIR}/cpp
)

# Search jniLibs for libqobuz_ffi.so by name — linker stores only the filename
# in DT_NEEDED (not the full path), so Android can find it in the APK at runtime.
# Build: cd qobuz_ffi && cargo ndk -t arm64-v8a -t x86_64 -o ../app/src/main/jniLibs build --release
target_link_directories(qobuz_test PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/jniLibs/${ANDROID_ABI}
)

target_link_libraries(qobuz_test
    android
    log
    qobuz_ffi
)
