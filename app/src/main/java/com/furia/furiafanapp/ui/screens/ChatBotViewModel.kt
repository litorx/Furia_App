package com.furia.furiafanapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furia.furiafanapp.data.model.ChatMessage
import com.furia.furiafanapp.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatBotViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        _messages.value = listOf(chatRepository.welcomeMessage)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            val userMsg = ChatMessage(user = "user", text = text)
            val currentMessages = _messages.value.toMutableList()
            currentMessages.add(userMsg)
            _messages.value = currentMessages
            
            _isLoading.value = true
            
            try {
                val botResponse = chatRepository.sendMessage(text)
                
                currentMessages.add(botResponse)
                _messages.value = currentMessages
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearChat() {
        chatRepository.clearConversation()
        _messages.value = listOf(chatRepository.welcomeMessage)
    }
}
