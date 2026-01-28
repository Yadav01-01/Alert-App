package com.alert.app.calling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat


class CallActionReceiverTwo : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val callSessionId = intent.getStringExtra("CALL_SESSION_ID")

        // 1. नोटिफ़िकेशन को हटाएँ
        NotificationManagerCompat.from(context).cancel(1001) // CALL_NOTIFICATION_ID

        when (intent.action) {
            "ACTION_ACCEPT_CALL" -> {
                // कॉल स्वीकार करने का लॉजिक
                // YourVoIPManager.acceptCall(callSessionId)

                // कॉल UI Activity खोलें
//                val callIntent = Intent(context, Home::class.java).apply {
//                    putExtra("CALL_SESSION_ID", callSessionId)
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                }
//                context.startActivity(callIntent)
            }
            "ACTION_DECLINE_CALL" -> {
                // कॉल अस्वीकार करने का लॉजिक
                // YourVoIPManager.declineCall(callSessionId)
            }
        }

        // सिस्टम डायलॉग (जैसे लॉक स्क्रीन) को बंद करने का प्रयास करें
        //   context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    }
}