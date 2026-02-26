package com.alert.app.chatgpt

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow




class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun observeUserOnlineStatus(
        userId: String
    ): Flow<Pair<Boolean, Timestamp?>> = callbackFlow {
        Log.d("USER_STATUS_REPO", "Observing status for userId: $userId")
        // 🔥 CRASH FIX
        if (userId.isBlank()) {
            Log.e("USER_STATUS_REPO", "userId is blank, closing flow")
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(userId) // MUST be real UID
            .addSnapshotListener { snapshot, _ ->

                if (snapshot == null || !snapshot.exists()) return@addSnapshotListener
                Log.d("USER_STATUS_REPO", "Snapshot exists: ${snapshot?.exists()}")
                Log.d("USER_STATUS_REPO", "Snapshot data: ${snapshot?.data}")
                val isOnline = snapshot.getBoolean("isOnline") ?: false
                val lastSeen = snapshot.getTimestamp("lastSeen")

                trySend(isOnline to lastSeen)
            }

        awaitClose { listener.remove() }
    }
    fun setUserOnlineStatus(userId: String, isOnline: Boolean) {
        if (userId.isBlank()) return

        val userRef = firestore.collection("users").document(userId)

        val updates = hashMapOf<String, Any>(
            "isOnline" to isOnline,
            "lastSeen" to Timestamp.now()
        )

        userRef.update(updates)
            .addOnSuccessListener {
                Log.d("USER_STATUS_REPO", "User status updated: $isOnline")
            }
            .addOnFailureListener {
                Log.e("USER_STATUS_REPO", "Failed to update status", it)
            }
    }
}
