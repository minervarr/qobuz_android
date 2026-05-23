package io.nava.qobuz_test

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        QobuzNative.initAndroid(this)
    }
}
