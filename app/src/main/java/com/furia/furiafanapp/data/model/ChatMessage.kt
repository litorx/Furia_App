package com.furia.furiafanapp.data.model

/**
 * Represents a chat message in a match chat.
 */
data class ChatMessage(
    val id: String = "",
    val user: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
