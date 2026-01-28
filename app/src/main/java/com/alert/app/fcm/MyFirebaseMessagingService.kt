package com.alert.app.fcm

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alert.app.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.content.edit
import com.alert.app.calling.CallActionReceiverTwo
import com.alert.app.calling.IncomingCallService

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val INCOMING_CALL_CHANNEL_ID = "incoming_call_channel"
    private val CALL_NOTIFICATION_ID = 1001


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: "Notification"
        val body = remoteMessage.notification?.body ?: "No content"

        // Show the notification
       // showNotification(title, body)

        // Increment stored count
        incrementNotificationCount()

        // If app is in foreground, use TTS via broadcast
//        if (isAppInForeground(this)) {
//            val intent = Intent("FCM_NOTIFICATION_RECEIVED")
//            sendBroadcast(intent)
//        }

        callphoneWithRing(remoteMessage)

    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun callphoneWithRing(remoteMessage: RemoteMessage) {


        //shrawan sir code


    //  startIncomingCallService("Nikunj","122")
        showIncomingCallNotification("Nikunj","122")
//        if(remoteMessage.data.isNotEmpty() && remoteMessage.data["type"] == "incoming_call") {
//            val callerName = remoteMessage.data["caller_name"] ?: "APWL Connect Calling"
//            val callSessionId = remoteMessage.data["caller_id"] ?: ""
//            Log.d("testing_notification","Call SessionId is"+callSessionId);
//            startIncomingCallService(callerName,callSessionId)
//        }

    }


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showIncomingCallNotification(callerName: String, callSessionId: String) {
        val context = applicationContext

        createNotificationChannel(context)

        val acceptIntent = Intent(context, CallActionReceiverTwo::class.java).apply {
            action = "ACTION_ACCEPT_CALL"
            putExtra("CALL_SESSION_ID", callSessionId)
        }
        val acceptPendingIntent = PendingIntent.getBroadcast(
            context, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val declineIntent = Intent(context, CallActionReceiverTwo::class.java).apply {
            action = "ACTION_DECLINE_CALL"
            putExtra("CALL_SESSION_ID", callSessionId)
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context, 1, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notificationBuilder = NotificationCompat.Builder(context, INCOMING_CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Incoming Call")
            .setContentText("Call from $callerName")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .addAction(R.drawable.notification_icon, "Decline", declinePendingIntent)
            .addAction(R.drawable.notification_icon, "Answer", acceptPendingIntent)
            .setOngoing(true)
        NotificationManagerCompat.from(context).notify(CALL_NOTIFICATION_ID, notificationBuilder.build())
    }


    private fun createNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Incoming Calls"
            val descriptionText = "Alerts for incoming VoIP calls"
            // IMPORTANCE_HIGH ज़रूरी है ताकि नोटिफ़िकेशन तेज़ी से और आवाज़ के साथ आए
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(INCOMING_CALL_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // डिफ़ॉल्ट रिंगटोन सेट करें (या custom.mp3 का Uri सेट करें)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build())
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }



    private fun startIncomingCallService(caller: String, sessionId: String) {

        stopService(Intent(this, IncomingCallService::class.java))

        val svcIntent = Intent(this, IncomingCallService::class.java).apply {
            putExtra("CALLER_NAME", caller)
            putExtra("CALL_SESSION_ID", sessionId)
        }

        ContextCompat.startForegroundService(this, svcIntent)
    }




    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        // Send token to your backend
        sendTokenToServer(token)
    }

    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        val packageName = context.packageName

        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }


    private fun incrementNotificationCount() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val current = prefs.getInt("notif_count", 0)
        prefs.edit { putInt("notif_count", current + 1) }
    }


    private fun sendTokenToServer(token: String) {
        // Send via Retrofit or any network library
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "default_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Default Channel", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.app_icon) // Your icon
            .build()

        notificationManager.notify(1, notification)
    }
}
