package io.nava.qobuz_test

import android.content.Context
import android.content.SharedPreferences

private const val NAME = "qobuz_prefs"

object Prefs {
    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun appId(ctx: Context)     = prefs(ctx).getString("app_id", "") ?: ""
    fun appSecret(ctx: Context) = prefs(ctx).getString("app_secret", "") ?: ""
    fun userId(ctx: Context)    = prefs(ctx).getString("user_id", "") ?: ""
    fun authToken(ctx: Context) = prefs(ctx).getString("auth_token", "") ?: ""
    fun quality(ctx: Context)   = prefs(ctx).getString("quality", "flac") ?: "flac"
    fun downloadDir(ctx: Context) = prefs(ctx).getString("download_dir", "/sdcard/Music/Qobuz") ?: "/sdcard/Music/Qobuz"

    fun saveCredentials(ctx: Context, appId: String, appSecret: String, userId: String, authToken: String) {
        prefs(ctx).edit()
            .putString("app_id", appId)
            .putString("app_secret", appSecret)
            .putString("user_id", userId)
            .putString("auth_token", authToken)
            .apply()
    }

    fun saveSettings(ctx: Context, quality: String, downloadDir: String) {
        prefs(ctx).edit()
            .putString("quality", quality)
            .putString("download_dir", downloadDir)
            .apply()
    }

    fun hasCredentials(ctx: Context) =
        appId(ctx).isNotBlank() && authToken(ctx).isNotBlank()
}
