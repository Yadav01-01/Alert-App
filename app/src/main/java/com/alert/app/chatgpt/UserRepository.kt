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

                val isOnline = snapshot.getBoolean("isOnline") ?: false
                val lastSeen = snapshot.getTimestamp("lastSeen")

                trySend(isOnline to lastSeen)
            }

        awaitClose { listener.remove() }
    }
}
