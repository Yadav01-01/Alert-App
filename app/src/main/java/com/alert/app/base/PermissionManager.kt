package com.alert.app.base

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object PermissionManager {
    fun openAppSettings(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        )
        activity.startActivity(intent)
    }
}