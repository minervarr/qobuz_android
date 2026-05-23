package io.nava.qobuz_test

import android.content.Context
import org.json.JSONArray

data class SearchResult(
    val id: String,
    val title: String,
    val artist: String,
    val year: String,
)

object QobuzNative {
    init {
        System.loadLibrary("qobuz_test")
    }

    fun initAndroid(context: Context) {
        nativeInitAndroid(context)
    }

    fun search(
        appId: String, appSecret: String,
        userId: String, authToken: String,
        query: String, searchType: String,
    ): Result<List<SearchResult>> {
        val raw = nativeSearch(appId, appSecret, userId, authToken, query, searchType)
        return if (raw.startsWith("error:")) {
            Result.failure(RuntimeException(raw.removePrefix("error: ")))
        } else {
            runCatching {
                val arr = JSONArray(raw)
                List(arr.length()) { i ->
                    val obj = arr.getJSONObject(i)
                    SearchResult(
                        id     = obj.optString("id"),
                        title  = obj.optString("title"),
                        artist = obj.optString("artist"),
                        year   = obj.optString("year"),
                    )
                }
            }
        }
    }

    fun download(
        appId: String, appSecret: String,
        userId: String, authToken: String,
        itemId: String, itemType: String,
        outputDir: String, quality: String,
    ): Result<Unit> {
        val raw = nativeDownload(appId, appSecret, userId, authToken, itemId, itemType, outputDir, quality)
        return if (raw.startsWith("error:")) {
            Result.failure(RuntimeException(raw.removePrefix("error: ")))
        } else {
            Result.success(Unit)
        }
    }

    private external fun nativeInitAndroid(context: Context)
    private external fun nativeSearch(
        appId: String, appSecret: String,
        userId: String, authToken: String,
        query: String, searchType: String,
    ): String
    private external fun nativeDownload(
        appId: String, appSecret: String,
        userId: String, authToken: String,
        itemId: String, itemType: String,
        outputDir: String, quality: String,
    ): String
}
