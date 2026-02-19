package com.alert.app.base


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
class NotificationPermissionHelper(private val activity: Activity) {

    /** Pass launcher from activity */
    fun checkAndRequestPermission(permissionLauncher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        when {
            // Already granted
            ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> return

            // Denied once → explain rationale
            activity.shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                showPermissionDialog()
            }

            // First time → request via launcher
            else -> permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun showPermissionDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Notification Permission Required")
            .setMessage(
                "Notifications are required to receive incoming call alerts and voice calls. " +
                        "Please enable notifications in settings to continue using calling features."
            )
            .setCancelable(false)
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${activity.packageName}")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
    }

    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(activity).areNotificationsEnabled()
    }
}