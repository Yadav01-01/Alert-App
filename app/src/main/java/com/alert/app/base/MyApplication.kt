package com.alert.app.base

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
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

    }

    companion object {

        @Volatile
        private var instance: MyApplication? = null

        fun getAppContext(): Context {
            return instance!!.applicationContext
        }

    }



}