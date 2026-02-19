package com.alert.app.calling

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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
import com.alert.app.activity.IncomingAudioCallActivity
import com.alert.app.activity.IncomingCallActivity
import com.alert.app.base.AppConstant

class IncomingAudioCallService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val STOP_AFTER_MS = 60_000L
    private val handler = Handler(Looper.getMainLooper())
    private val stopRunnable = Runnable { stopRingtone() }

    // 🔹 New parameters
    private var channelName = ""
    private var agoraToken = ""
    private var appId = ""
    private var callerName = "Unknown Caller"
    private var image = ""

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // 🔹 Read values from intent
        channelName = intent?.getStringExtra("channel_name") ?: ""
        agoraToken = intent?.getStringExtra("agora_token") ?: ""
        appId = intent?.getStringExtra("agora_app_id") ?: ""
        callerName = intent?.getStringExtra("caller_name") ?: "Unknown Caller"
        image = intent?.getStringExtra("image") ?: ""

        val notification = buildIncomingCallNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startRingtone()

        // 🔹 Launch Incoming Call Screen
        val callIntent = Intent(this, IncomingAudioCallActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("channel_name", channelName)
            putExtra("agora_token", agoraToken)
            putExtra("agora_app_id", appId)
            putExtra("caller_name", callerName)
            putExtra("image", image)
        }
        startActivity(callIntent)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopRingtone()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ----------------------------------
    // NOTIFICATION
    // ----------------------------------
    private fun buildIncomingCallNotification(): Notification {

        // Full screen intent (lock screen)
        val fullScreenIntent = Intent(this, IncomingAudioCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AppConstant.CHANNEL, channelName)
            putExtra(AppConstant.TOKEN, agoraToken)
            putExtra(AppConstant.APPiD, appId)
            putExtra(AppConstant.NAME, callerName)
            putExtra(AppConstant.IMAGE, image)
        }

        val fullScreenPending = PendingIntent.getActivity(
            this, 100, fullScreenIntent, pendingFlags()
        )

        // Answer button → InCallActivity
        val acceptIntent = Intent(this, InCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(AppConstant.CHANNEL, channelName)
            putExtra(AppConstant.TOKEN, agoraToken)
            putExtra(AppConstant.APPiD, appId)
            putExtra(AppConstant.NAME, callerName)
            putExtra(AppConstant.IMAGE, image)
            putExtra("ACTION_TYPE", "ANSWER")
        }

        val acceptPending = PendingIntent.getActivity(
            this, 101, acceptIntent, pendingFlags()
        )

        // Decline button
        val declineIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_DECLINE
        }

        val declinePending = PendingIntent.getBroadcast(
            this, 102, declineIntent, pendingFlags()
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Incoming Call")
            .setContentText(callerName)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPending, true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.join_call, "Answer", acceptPending)
            .addAction(R.drawable.ic_call_cut, "Decline", declinePending)
            .build()
    }

    // ----------------------------------
    // NOTIFICATION CHANNEL
    // ----------------------------------
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Incoming Calls",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Incoming call notifications"
            setSound(null, null)
            enableVibration(true)
        }

        getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    private fun pendingFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else
            PendingIntent.FLAG_UPDATE_CURRENT
    }

    // ----------------------------------
    // RINGTONE
    // ----------------------------------
    private fun startRingtone() {
        if (mediaPlayer?.isPlaying == true) return

        stopRingtone()

        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(this@IncomingAudioCallService, ringtoneUri)
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