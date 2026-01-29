package com.alert.app.base

import android.app.Application
import android.content.Context
import android.os.Build
import com.alert.app.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
public class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)

        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()
        // Crashlytics configuration
        setupCrashlytics()

        // Optional: Enable in debug if needed
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        }
    }

    companion object {

        @Volatile
        private var instance: MyApplication? = null

        fun getAppContext(): Context {
            return instance!!.applicationContext
        }

    }

    private fun setupCrashlytics() {
        // Custom keys set करें
        val crashlytics = FirebaseCrashlytics.getInstance()

        // App info
        crashlytics.setCustomKey("version_code", BuildConfig.VERSION_CODE)
        crashlytics.setCustomKey("version_name", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("build_type", BuildConfig.BUILD_TYPE)

        // Device info
        crashlytics.setCustomKey("device_model", Build.MODEL)
        crashlytics.setCustomKey("android_version", Build.VERSION.RELEASE)

        // User ID (अगर available हो)
        // crashlytics.setUserId(userId)
    }

}