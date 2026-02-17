package com.alert.app.model.message

import com.google.firebase.Timestamp

data class ChatListItem(
    val chatId: String,
    val userId: Int,
    val fullName: String,
    val profile: String,
    val lastMessage: String?,
    val lastMessageTime: Timestamp?,
    val unreadCount: Int,
    var isLiveLocation:Boolean = false
)



