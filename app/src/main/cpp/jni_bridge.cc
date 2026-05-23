#include <jni.h>
#include <android/log.h>
#include <string>

#define LOG_TAG "QobuzJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {
    void qobuz_init_android(void* vm, void* context);
    char* qobuz_search(
        const char* app_id, const char* app_secret,
        const char* user_id, const char* auth_token,
        const char* query, const char* search_type
    );
    char* qobuz_download(
        const char* app_id, const char* app_secret,
        const char* user_id, const char* auth_token,
        const char* item_id, const char* item_type,
        const char* output_dir, const char* quality
    );
    void qobuz_free_string(char* s);
}

static std::string jstr(JNIEnv* env, jstring s) {
    if (!s) return {};
    const char* p = env->GetStringUTFChars(s, nullptr);
    std::string r(p);
    env->ReleaseStringUTFChars(s, p);
    return r;
}

static jstring rust_to_jstr(JNIEnv* env, char* p) {
    jstring result = env->NewStringUTF(p ? p : "");
    qobuz_free_string(p);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_io_nava_qobuz_1test_QobuzNative_nativeInitAndroid(
        JNIEnv* env, jobject /*thiz*/, jobject context)
{
    JavaVM* vm = nullptr;
    env->GetJavaVM(&vm);
    qobuz_init_android(vm, context);
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_nava_qobuz_1test_QobuzNative_nativeSearch(
        JNIEnv* env, jobject /*thiz*/,
        jstring appId, jstring appSecret,
        jstring userId, jstring authToken,
        jstring query, jstring searchType)
{
    std::string q = jstr(env, query);
    std::string st = jstr(env, searchType);
    LOGI("search start: query=\"%s\" type=%s", q.c_str(), st.c_str());
    char* raw = qobuz_search(
        jstr(env, appId).c_str(),
        jstr(env, appSecret).c_str(),
        jstr(env, userId).c_str(),
        jstr(env, authToken).c_str(),
        q.c_str(), st.c_str()
    );
    if (raw && std::string(raw).rfind("error:", 0) == 0)
        LOGE("search error: %s", raw);
    else
        LOGI("search done");
    return rust_to_jstr(env, raw);
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_nava_qobuz_1test_QobuzNative_nativeDownload(
        JNIEnv* env, jobject /*thiz*/,
        jstring appId, jstring appSecret,
        jstring userId, jstring authToken,
        jstring itemId, jstring itemType,
        jstring outputDir, jstring quality)
{
    std::string id = jstr(env, itemId);
    std::string it = jstr(env, itemType);
    std::string ql = jstr(env, quality);
    LOGI("download start: id=%s type=%s quality=%s", id.c_str(), it.c_str(), ql.c_str());
    char* raw = qobuz_download(
        jstr(env, appId).c_str(),
        jstr(env, appSecret).c_str(),
        jstr(env, userId).c_str(),
        jstr(env, authToken).c_str(),
        id.c_str(), it.c_str(),
        jstr(env, outputDir).c_str(),
        ql.c_str()
    );
    if (raw && std::string(raw).rfind("error:", 0) == 0)
        LOGE("download error: %s", raw);
    else
        LOGI("download ok");
    return rust_to_jstr(env, raw);
}
