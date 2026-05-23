cmake_minimum_required(VERSION 3.22.1)
project(qobuz_test)

add_library(qobuz_test SHARED
    cpp/jni_bridge.cc
)

target_link_directories(qobuz_test PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/jniLibs/${ANDROID_ABI}
)

target_link_libraries(qobuz_test
    android
    log
    qobuz_ffi
)
