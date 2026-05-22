cmake_minimum_required(VERSION 3.22.1)
project(qobuz_test)

set(NATIVE_APP_GLUE ${ANDROID_NDK}/sources/android/native_app_glue)

# Prebuilt Rust qobuz-api library (cross-compiled with cargo-ndk)
add_library(qobuz_api SHARED IMPORTED)
set_target_properties(qobuz_api PROPERTIES
    IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/jniLibs/${ANDROID_ABI}/libqobuz_api.so
)

add_library(qobuz_test SHARED
    cpp/main.cc
    ${NATIVE_APP_GLUE}/android_native_app_glue.c
)

target_include_directories(qobuz_test PRIVATE
    ${NATIVE_APP_GLUE}
    ${CMAKE_CURRENT_SOURCE_DIR}/cpp
)

target_link_libraries(qobuz_test
    android
    log
    qobuz_api
)
