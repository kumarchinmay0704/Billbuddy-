data class ChatMessage(
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val message: String = "",
    val messageType: String = "text", // "text" or "image"
    val timestamp: Long = 0
) 