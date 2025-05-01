package com.furia.furiafanapp.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furia.furiafanapp.data.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ChatBotViewModel @Inject constructor(
    @Named("openai") private val httpClient: HttpClient,
    private val json: Json
) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    private val conversation = mutableListOf<Map<String, String>>()

    init {
        // system prompt for FURIA bot knowledge
        conversation.add(mapOf(
            "role" to "system", 
            "content" to "Você é um assistente especializado em tudo sobre a FURIA eSports atualizado até 2025."
        ))
        // welcome message
        _messages.value = listOf(
            ChatMessage(user = "assistant", text = "Olá! Eu sou o Bot FURIA. Pergunte-me qualquer coisa sobre a equipe.")
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            // add user message
            val userMsg = ChatMessage(user = "user", text = text)
            val list = _messages.value.toMutableList()
            list.add(userMsg)
            _messages.value = list
            conversation.add(mapOf("role" to "user", "content" to text))
            // call OpenAI Chat API with error handling
            var botMsg: ChatMessage
            try {
                val body = buildJsonObject {
                    put("model", "gpt-3.5-turbo")
                    put("messages", json.encodeToJsonElement(conversation))
                }
                // enviar requisição e obter resposta bruta
                val httpResponse: HttpResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
                val responseText: String = httpResponse.bodyAsText()
                Log.d("ChatBot", "OpenAI response: $responseText")
                val jsonObj = json.parseToJsonElement(responseText).jsonObject
                // extrair conteúdo de choices ou mensagem de erro
                val content = jsonObj["choices"]
                    ?.jsonArray?.get(0)?.jsonObject
                    ?.get("message")?.jsonObject
                    ?.get("content")?.jsonPrimitive?.content
                    ?: jsonObj["error"]?.jsonObject
                        ?.get("message")?.jsonPrimitive?.content
                    ?: "Desculpe, não consegui gerar a resposta."
                botMsg = ChatMessage(user = "assistant", text = content)
                conversation.add(mapOf("role" to "assistant", "content" to content))
            } catch (e: Exception) {
                Log.e("ChatBot", "Error on Chat API", e)
                botMsg = ChatMessage(user = "assistant", text = "Desculpe, ocorreu um erro ao obter resposta.")
            }
            // update messages with bot response
            list.add(botMsg)
            _messages.value = list
        }
    }
}
