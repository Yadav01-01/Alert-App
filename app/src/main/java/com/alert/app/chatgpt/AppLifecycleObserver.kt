package com.alert.app.chatgpt

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AppLifecycleObserver(
    private val userId: String
) : DefaultLifecycleObserver {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onStart(owner: LifecycleOwner) {
        updateStatus(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        updateStatus(false)
    }

    private fun updateStatus(isOnline: Boolean) {
        if (userId.isBlank()) return

        firestore.collection("users")
            .document(userId)
            .set(
                mapOf(
                    "isOnline" to isOnline,
                    "lastSeen" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
    }
}