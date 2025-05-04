package com.furia.furiafanapp.data.repository

import android.util.Log
import com.furia.furiafanapp.data.model.ChatMessage
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    @Named("openai") private val httpClient: HttpClient,
    private val json: Json
) {
    // Informações sobre a FURIA para incluir no sistema prompt
    private val furiaInfo = """
        FURIA Esports é uma organização brasileira de esportes eletrônicos fundada em 2017 por Jaime "raizen" Pádua e André Akkari.
        
        Times atuais da FURIA (2025):
        - CS2: art (capitão), yuurih, kscerato, chelo, drop, saffee (reserva)
        - Valorant: Quick, Khalil, Mazin, Nozwerr, dgzin
        - League of Legends: Ranger, Goot, Envy, Brance, RedBert
        - Rainbow Six Siege: Fntzy, Stk, Pino, Miracle, Soulz1
        - PUBG: rustyzera, killdemo, raspu, ITZZ
        - Rocket League: yanxnz, drufinho, CaioTG1
        - Kings League: Time misto com jogadores brasileiros
        
        Conquistas notáveis:
        - CS2/CSGO: Campeão da IEM Katowice 2023, ESL Pro League Season 16, BLAST Premier Spring Finals 2023
        - Valorant: Campeão do VCT Americas 2024, 3º lugar no Valorant Champions 2024
        - Rainbow Six: Campeão do Six Invitational 2023
        
        A FURIA é conhecida por seu estilo agressivo de jogo, especialmente no CS2, e por desenvolver talentos brasileiros.
        A sede da FURIA fica em São Paulo, Brasil, mas também possui instalações em Miami, EUA.
        
        Patrocinadores principais incluem: Lenovo Legion, Red Bull, Betway, HyperX.
        
        O mascote da FURIA é uma pantera negra, e as cores oficiais são preto e amarelo/dourado.
    """.trimIndent()
    
    // Instruções para o chatbot
    private val systemInstructions = """
        Você é o assistente oficial da FURIA Esports, especializado em tudo sobre a organização.
        Sempre responda como se fosse parte da equipe FURIA.
        
        Use um tom amigável, entusiasmado e profissional. Seja conciso nas respostas.
        
        Quando não souber algo específico sobre a FURIA, admita que não tem essa informação
        em vez de inventar fatos. Nunca responda perguntas que não estejam relacionadas à FURIA
        ou esports em geral.
        
        Se o usuário perguntar sobre temas não relacionados à FURIA ou esports, educadamente
        redirecione a conversa para tópicos sobre a FURIA.
        
        Aqui estão informações atualizadas sobre a FURIA que você deve usar:
        
        $furiaInfo
    """.trimIndent()

    // Inicializa a conversa com o prompt do sistema
    private val conversation = mutableListOf(
        mapOf("role" to "system", "content" to systemInstructions)
    )

    // Mensagem de boas-vindas
    val welcomeMessage = ChatMessage(
        user = "assistant",
        text = "Olá! Sou o assistente oficial da FURIA. Como posso ajudar você hoje?"
    )

    /**
     * Envia uma mensagem para a API da OpenAI e retorna a resposta
     */
    suspend fun sendMessage(text: String): ChatMessage {
        return withContext(Dispatchers.IO) {
            // Adiciona a mensagem do usuário à conversa
            conversation.add(mapOf("role" to "user", "content" to text))
            
            try {

                val body = buildJsonObject {

                    put("model", "gpt-3.5-turbo")

                    put("max_tokens", 300)

                    put("temperature", 0.7)

                    put("messages", json.encodeToJsonElement(conversation))
                }
                
                // Envia a requisição para a API
                val httpResponse: HttpResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
                
                // Verifica o status da resposta
                if (!httpResponse.status.isSuccess()) {
                    return@withContext handleErrorResponse(httpResponse)
                }
                
                // Processa a resposta
                val responseText = httpResponse.bodyAsText()
                Log.d("ChatRepository", "OpenAI response: $responseText")
                
                val jsonObj = json.parseToJsonElement(responseText).jsonObject
                
                // Verifica se há um erro no corpo da resposta
                if (jsonObj.containsKey("error")) {
                    val errorObj = jsonObj["error"]?.jsonObject
                    val errorType = errorObj?.get("type")?.jsonPrimitive?.content ?: "unknown_error"
                    val errorMessage = errorObj?.get("message")?.jsonPrimitive?.content 
                        ?: "Ocorreu um erro desconhecido."
                    
                    return@withContext handleApiError(errorType, errorMessage)
                }
                
                // Extrai o conteúdo da resposta
                val content = jsonObj["choices"]
                    ?.jsonArray?.get(0)?.jsonObject
                    ?.get("message")?.jsonObject
                    ?.get("content")?.jsonPrimitive?.content
                    ?: "Desculpe, não consegui gerar uma resposta. Tente novamente mais tarde."
                
                // Adiciona a resposta à conversa
                conversation.add(mapOf("role" to "assistant", "content" to content))
                
                // Limita o tamanho da conversa para economizar tokens
                if (conversation.size > 10) {
                    // Mantém o prompt do sistema e as 5 mensagens mais recentes
                    val systemPrompt = conversation.first()
                    conversation.clear()
                    conversation.add(systemPrompt)
                    conversation.addAll(conversation.takeLast(5))
                }
                
                ChatMessage(user = "assistant", text = content)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error sending message", e)
                handleException(e)
            }
        }
    }
    
    /**
     * Trata erros de resposta HTTP
     */
    private fun handleErrorResponse(response: HttpResponse): ChatMessage {
        val errorMessage = when (response.status) {
            HttpStatusCode.Unauthorized -> 
                "Erro de autenticação com a API. Verifique sua chave de API."
            
            HttpStatusCode.TooManyRequests -> 
                "Limite de requisições excedido. Por favor, tente novamente mais tarde."
            
            HttpStatusCode.BadRequest ->
                "Requisição inválida. Tente uma pergunta diferente."
            
            HttpStatusCode.InternalServerError, 
            HttpStatusCode.BadGateway,
            HttpStatusCode.ServiceUnavailable ->
                "Os servidores da OpenAI estão indisponíveis no momento. Tente novamente mais tarde."
            
            else -> "Erro de comunicação com a API (código ${response.status.value}). Tente novamente mais tarde."
        }
        
        Log.e("ChatRepository", "API Error: ${response.status.value} - $errorMessage")
        return ChatMessage(user = "assistant", text = errorMessage)
    }
    
    /**
     * Trata erros específicos da API da OpenAI
     */
    private fun handleApiError(errorType: String, errorMessage: String): ChatMessage {
        val userFriendlyMessage = when {
            errorType.contains("rate_limit") || errorMessage.contains("rate limit") ->
                "O serviço de chat está temporariamente indisponível. Por favor, tente novamente em alguns minutos."
            
            errorType.contains("token") || errorMessage.contains("token") ->
                "A conversa ficou muito longa. Tente limpar o histórico e começar uma nova conversa."
            
            errorType.contains("context_length") || errorMessage.contains("maximum context length") ->
                "A conversa excedeu o tamanho máximo permitido. Por favor, limpe o histórico e comece uma nova conversa."
            
            errorType.contains("billing") || errorMessage.contains("billing") ->
                "O serviço de chat está temporariamente indisponível."
            
            else -> "Erro ao processar sua mensagem: $errorMessage"
        }
        
        Log.e("ChatRepository", "OpenAI API Error: $errorType - $errorMessage")
        return ChatMessage(user = "assistant", text = userFriendlyMessage)
    }
    
    /**
     * Trata exceções gerais
     */
    private fun handleException(exception: Exception): ChatMessage {
        val errorMessage = when (exception) {
            is java.net.UnknownHostException, 
            is java.net.ConnectException,
            is java.net.SocketTimeoutException ->
                "Não foi possível conectar aos servidores. Verifique sua conexão com a internet."
            
            is java.util.concurrent.TimeoutException ->
                "A requisição demorou muito tempo. Tente novamente mais tarde."
            
            else -> "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde."
        }
        
        Log.e("ChatRepository", "Exception in chat: ${exception.javaClass.simpleName} - ${exception.message}")
        return ChatMessage(user = "assistant", text = errorMessage)
    }
    
    /**
     * Limpa a conversa atual, mantendo apenas o prompt do sistema
     */
    fun clearConversation() {
        val systemPrompt = conversation.first()
        conversation.clear()
        conversation.add(systemPrompt)
    }
}
