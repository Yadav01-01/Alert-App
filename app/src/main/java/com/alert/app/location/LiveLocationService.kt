package com.alert.app.location

import com.alert.app.base.LocationNotification


import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

import androidx.core.app.NotificationCompat
import com.alert.app.R

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class LiveLocationService : Service() {

    companion object {
        private const val CHANNEL_ID = "live_location_channel"
        private const val NOTIFICATION_ID = 1001
        private const val PREF_NAME = "live_location_prefs"
    }

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var chatId: String
    private lateinit var messageId: String
    private var durationMillis: Long = 0L

    private var stopHandler: Handler? = null

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        fusedClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        chatId = intent?.getStringExtra("chatId")
            ?: prefs.getString("chatId", null)
                    ?: return stopSafely()

        messageId = intent?.getStringExtra("messageId")
            ?: prefs.getString("messageId", null)
                    ?: return stopSafely()

        durationMillis = intent?.getIntExtra("duration", -1)
            ?.takeIf { it > 0 }
            ?.let { it * 60 * 1000L }
            ?: prefs.getLong("duration", 0L)

        if (durationMillis <= 0) {
            return stopSafely()
        }

        persistSession(chatId, messageId, durationMillis)

        startLocationUpdates()
        startAutoStopTimer()

        return START_STICKY
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            15_000L
        ).setMinUpdateDistanceMeters(30f).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                FirebaseFirestore.getInstance()
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .document(messageId)
                    .update(
                        mapOf(
                            "location" to GeoPoint(
                                location.latitude,
                                location.longitude
                            ),
                            "timestamp" to Timestamp.now()
                        )
                    )
            }
        }

        fusedClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun startAutoStopTimer() {
        stopHandler = Handler(Looper.getMainLooper())
        stopHandler?.postDelayed({
            stopLiveLocation()
        }, durationMillis)
    }

    private fun stopLiveLocation() {
        FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .update(
                mapOf(
                    "isLive" to false,
                    "timestamp" to Timestamp.now()
                )
            )

        stopSelf()
    }

    private fun persistSession(
        chatId: String,
        messageId: String,
        duration: Long
    ) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit()
            .putString("chatId", chatId)
            .putString("messageId", messageId)
            .putLong("duration", duration)
            .apply()
    }

    private fun stopSafely(): Int {
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        fusedClient.removeLocationUpdates(locationCallback)
        stopHandler?.removeCallbacksAndMessages(null)

        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Live Location Sharing",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Live location sharing")
            .setContentText("Your location is being shared")
            .setSmallIcon(R.drawable.ic_location_icon)
            .setOngoing(true)
            .build()
    }



}