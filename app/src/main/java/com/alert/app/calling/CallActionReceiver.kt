package com.alert.app.calling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alert.app.activity.InCallActivity
import com.alert.app.base.AppConstant
import com.alert.app.base.BaseApplication
import com.google.firebase.firestore.FirebaseFirestore

class CallActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ACCEPT = "com.example.voip.ACTION_ACCEPT"
        const val ACTION_DECLINE = "com.example.voip.ACTION_DECLINE"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        context.stopService(Intent(context, IncomingCallService::class.java))
        when (intent?.action) {
            ACTION_ACCEPT -> {
                // Stop ringtone service and open in-call UI
                context.stopService(Intent(context, IncomingCallService::class.java))
                val i = Intent(context, InCallActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(i)

            }
            ACTION_DECLINE -> {
                // Stop ringtone and send decline to server
                val token = intent.getStringExtra(AppConstant.TOKEN).orEmpty()
                if (token.isBlank()) return

                val safeToken = BaseApplication.safeDocIdFromToken(token)

                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("calls")
                    .document(safeToken)
                    .update("status", "ended")
                    .addOnSuccessListener {
                        Log.d("CallFirestore", "Call declined")
                    }
                    .addOnFailureListener {
                        Log.e("CallFirestore", "Decline failed", it)
                    }

                // 🔕 Stop ringing service
                context.stopService(
                    Intent(context, IncomingAudioCallService::class.java)
                )


                // TODO: send decline signal to your server
            }
            else -> {}
        }
    }
}