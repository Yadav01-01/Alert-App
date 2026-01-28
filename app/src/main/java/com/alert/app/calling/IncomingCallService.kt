package com.alert.app.calling

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.Service.START_NOT_STICKY
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.alert.app.R
import com.alert.app.activity.InCallActivity
import com.alert.app.activity.IncomingCallActivity

class IncomingCallService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val STOP_AFTER_MS = 60_000L
    private val handler = Handler(Looper.getMainLooper())
    private val stopRunnable = Runnable { stopRingtone() }

    private var callerName = "Unknown Caller"
    private var sessionId = ""

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        callerName = intent?.getStringExtra("CALLER_NAME") ?: "Unknown Caller"
        sessionId = intent?.getStringExtra("CALL_SESSION_ID") ?: ""

        val notification = buildIncomingCallNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startRingtone()


        //new code added

        val callIntent = Intent(this, IncomingCallActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("CALLER_NAME", callerName)
            putExtra("CALL_SESSION_ID", sessionId)
        }

        startActivity(callIntent)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopRingtone()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null


    private fun buildIncomingCallNotification(): Notification {

        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("CALLER_NAME", callerName)
            putExtra("CALL_SESSION_ID", sessionId)
        }

        val fullScreenPending = PendingIntent.getActivity(
            this, 100, fullScreenIntent, pendingFlags()
        )

        val acceptIntent = Intent(this, InCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("CALLER_NAME", callerName)
            putExtra("CALL_SESSION_ID", sessionId)
            putExtra("ACTION_TYPE", "ANSWER")
        }

        val acceptPending = PendingIntent.getActivity(this, 101, acceptIntent, pendingFlags())

        val declineIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_DECLINE
            putExtra("CALL_SESSION_ID", sessionId)
        }
        val declinePending = PendingIntent.getBroadcast(this, 102, declineIntent, pendingFlags())

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Incoming Alert")
            .setContentText("Alert")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPending, true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // show on lock screen
            .addAction(R.drawable.join_call   , "Answer", acceptPending)
            .addAction(R.drawable.ic_call_cut , "Decline", declinePending)
            .build()
    }

    // -------------------------------
    // CREATE NOTIFICATION CHANNEL (API 26+)
    // -------------------------------

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Incoming Calls",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Incoming call notifications"
            setSound(null, null) // custom ringtone
            enableVibration(true)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun pendingFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else
            PendingIntent.FLAG_UPDATE_CURRENT
    }

    // -------------------------------
    // RINGTONE (single instance)
    // -------------------------------
    private fun startRingtone() {
        // Double ringing fix
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) return

        stopRingtone() // stop any previous player

        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(this@IncomingCallService, ringtoneUri)
                isLooping = true
                prepare()
                start()
            }
            handler.removeCallbacks(stopRunnable)
            handler.postDelayed(stopRunnable, STOP_AFTER_MS)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun stopRingtone() {
        handler.removeCallbacks(stopRunnable)
        try { mediaPlayer?.stop() } catch (_: Exception) {}
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "incoming_call_channel"
    }
}