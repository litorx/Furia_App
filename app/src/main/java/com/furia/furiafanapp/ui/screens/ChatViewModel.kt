package com.furia.furiafanapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furia.furiafanapp.data.chat.ChatRepository
import com.furia.furiafanapp.data.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling chat messages per match.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository
) : ViewModel() {

    /**
     * Observes messages for a match.
     */
    fun getMessages(matchId: String): Flow<List<ChatMessage>> =
        repo.getMessages(matchId)

    /**
     * Sends a message to the given match chat.
     */
    fun send(matchId: String, text: String, user: String = "torcedor") {
        if (text.isBlank()) return
        viewModelScope.launch {
            repo.sendMessage(matchId, ChatMessage(user = user, text = text))
        }
    }
}
