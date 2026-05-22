#include <android_native_app_glue.h>
#include <android/log.h>

#define LOG_TAG "qobuz_test"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {
    char* qobuz_search_grimes(
        const char* app_id,
        const char* app_secret,
        const char* user_id,
        const char* auth_token
    );
    void qobuz_free_string(char* s);
}

void android_main(android_app* app) {
    LOGI("qobuz_test started — searching Grimes albums...");

    char* result = qobuz_search_grimes(
        "798273057",
        "abb21364945c0583309667d13ca3d93a",
        "12122165",
        "TZRdEWndBfvvbMuO0F_U87LMTY5ySnC0sNPY1lB9-PGfcaHh46k0G3Wkb39SLOKwqbpULKhWlWM0JCKAq_vn3w"
    );

    if (result) {
        LOGI("%s", result);
        qobuz_free_string(result);
    }

    // Event loop — required for NativeActivity to stay alive
    while (true) {
        int events;
        android_poll_source* source;

        while (ALooper_pollOnce(0, nullptr, &events, (void**)&source) >= 0) {
            if (source) source->process(app, source);
            if (app->destroyRequested) return;
        }

        if (app->destroyRequested) return;
    }
}
