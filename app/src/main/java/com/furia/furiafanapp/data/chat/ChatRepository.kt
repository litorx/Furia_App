package com.furia.furiafanapp.data.chat

import com.furia.furiafanapp.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat messages per match.
 */
interface ChatRepository {
    /**
     * Returns a flow of messages for the given match ID, ordered by timestamp ascending.
     */
    fun getMessages(matchId: String): Flow<List<ChatMessage>>

    /**
     * Sends a message to the given match chat.
     */
    suspend fun sendMessage(matchId: String, message: ChatMessage)
}