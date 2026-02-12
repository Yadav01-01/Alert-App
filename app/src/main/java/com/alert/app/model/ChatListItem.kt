package com.alert.app.model

data class ChatListItem(
    val chatId: String,
    val otherUserId: Int,
    val otherUserName: String,
    val otherUserProfile: String?,
    var lastMessage: String = "",
    var lastMessageTime: Long = 0L, // derived from Message.timestamp
    var unreadCount: Int = 0
)
