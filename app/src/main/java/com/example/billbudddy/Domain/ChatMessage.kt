package com.example.billbudddy.Domain

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val message: String = "",
    val messageType: String = "text",
    val timestamp: Long = 0,
    val chatId: String = ""
) 