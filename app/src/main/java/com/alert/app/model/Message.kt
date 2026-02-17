package com.alert.app.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val type: MessageType = MessageType.TEXT,
    val text: String? = null,
    val location: GeoPoint? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val isLive: Boolean = false,
    val formattedTime: String? = null,
    val seenBy: List<String> = emptyList(),
    val expiresAt: Timestamp? = null,
    val deletedFor: List<String> = emptyList()

)

enum class MessageType {
    TEXT, LOCATION
}
