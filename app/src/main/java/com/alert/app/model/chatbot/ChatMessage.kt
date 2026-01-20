package com.alert.app.model.chatbot

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSeen: Boolean = false,
    val isUser: Boolean = false
)

