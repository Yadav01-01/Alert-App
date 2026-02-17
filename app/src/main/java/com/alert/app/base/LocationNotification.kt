package com.alert.app.base

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.alert.app.R

object LocationNotification {

    val CHANNEL_ID = "live_location_channel"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Live Location Sharing",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun notification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Live location sharing")
            .setContentText("Your location is being shared")
            .setSmallIcon(R.drawable.ic_location_icon)
            .setOngoing(true)
            .build()
    }
}