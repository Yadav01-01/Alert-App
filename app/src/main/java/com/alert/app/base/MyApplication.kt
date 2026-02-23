package com.alert.app.base

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.ProcessLifecycleOwner
import com.alert.app.BuildConfig
//import com.alert.app.chatgpt.AppLifecycleObserver
import com.alert.app.AppLifecycleObserver
import com.alert.app.chatgpt.UserRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
public class MyApplication : Application() {
    private lateinit var lifecycleObserver: AppLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)
       // val userId = SessionManagement(context = applicationContext).getUserId()
        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()
        // Crashlytics configuration
        setupCrashlytics()

        // Optional: Enable in debug if needed
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        }

     //   lifecycleObserver = AppLifecycleObserver(SessionManagement(this).getUserId().toString())
        val repository = UserRepository()
        ProcessLifecycleOwner.get()
            .lifecycle
            //.addObserver(lifecycleObserver)
            .addObserver(AppLifecycleObserver(repository, SessionManagement(this).getUserId().toString()))
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