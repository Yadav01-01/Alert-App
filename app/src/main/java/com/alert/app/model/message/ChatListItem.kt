package com.alert.app.model.message

data class ChatListItem(
    val chatId: String,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserImage: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int
)



