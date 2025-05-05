package com.furia.furiafanapp.ui.screens

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furia.furiafanapp.R
import com.furia.furiafanapp.data.repository.ProfileRepository
import com.furia.furiafanapp.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

enum class CurrentGame {
    NONE, QUIZ, MEMORY, CHALLENGES, WORD_SCRAMBLE
}

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

data class MemoryCardContent(val name: String, val imageResId: Int)

data class MemoryCard(
    val id: Int,
    val content: MemoryCardContent,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

enum class ChallengeActionType {
    EXTERNAL_LINK, INTERNAL_NAVIGATION, INTERNAL_GAME
}

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val points: Int,
    val actionType: ChallengeActionType,
    val actionData: String,
    val completed: Boolean = false
)

data class MiniGamesUiState(
    val currentGame: CurrentGame = CurrentGame.NONE,
    val earnedPoints: Int = 0,
    val lastChallengeDay: Int = 0,
    val userPoints: Long = 0,
    val userProfile: UserProfile? = null,
    val leaderboard: List<UserProfile> = emptyList(),
    val isTopOne: Boolean = false,
    
    // Limitadores de frequência para cada jogo
    val lastQuizDay: Int = 0,
    val lastMemoryGameDay: Int = 0,
    val lastWordScrambleDay: Int = 0,
    
    // Quiz state
    val quizAvailable: Boolean = true,
    val quizQuestions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswerIndex: Int = -1,
    val showQuizAnswerResult: Boolean = false,
    val correctAnswers: Int = 0,
    val quizCompleted: Boolean = false,
    
    // Memory game state
    val memoryCards: List<MemoryCard> = emptyList(),
    val memoryMoves: Int = 0,
    val memoryMatchedPairs: Int = 0,
    val memoryIsChecking: Boolean = false,
    val memoryGameCompleted: Boolean = false,
    
    // Daily challenges state
    val dailyChallenges: List<Challenge> = emptyList(),
    
    // Word scramble game state
    val wordScrambleOriginalWord: String = "",
    val wordScrambleScrambledWord: String = "",
    val wordScrambleUserInput: String = "",
    val wordScrambleAttempts: Int = 0,
    val wordScrambleCompleted: Boolean = false,
    val wordScrambleSuccess: Boolean = false
)

@HiltViewModel
class MiniGamesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val auth: FirebaseAuth,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MiniGamesUiState())
    val uiState: StateFlow<MiniGamesUiState> = _uiState.asStateFlow()
    
    init {
        loadQuizQuestions()
        initializeMemoryGame()
        loadDailyChallenges()
        
        // Carregar as datas da última vez que cada jogo foi jogado
        loadLastPlayedDates()
        
        // Observar os pontos do usuário atual
        observeUserPoints()
        
        // Carregar o leaderboard
        loadLeaderboard()
    }
    
    private fun observeUserPoints() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    Log.e("MiniGamesViewModel", "Usuário não está logado")
                    return@launch
                }
                
                Log.d("MiniGamesViewModel", "Iniciando observação de pontos para usuário: $userId")
                
                profileRepository.getUserProfile(userId).collectLatest { userProfile ->
                    val points = userProfile.points
                    Log.d("MiniGamesViewModel", "Pontos atualizados do usuário: $points")
                    
                    _uiState.update { it.copy(userPoints = points, userProfile = userProfile) }
                }
            } catch (e: Exception) {
                Log.e("MiniGamesViewModel", "Erro ao observar pontos do usuário: ${e.message}", e)
            }
        }
    }
    
    fun setCurrentGame(game: CurrentGame) {
        _uiState.update { it.copy(currentGame = game) }
        
        // Reset game-specific state when switching games
        when (game) {
            CurrentGame.QUIZ -> resetQuiz()
            CurrentGame.MEMORY -> initializeMemoryGame()
            CurrentGame.CHALLENGES -> loadDailyChallenges()
            CurrentGame.WORD_SCRAMBLE -> initializeWordScrambleGame()
            else -> {}
        }
    }
    
    // QUIZ FUNCTIONS
    
    private fun loadQuizQuestions() {
        val questions = listOf(
            QuizQuestion(
                "Qual é o jogo principal da FURIA?",
                listOf("CS2", "Valorant", "League of Legends", "Rainbow Six"),
                0
            ),
            QuizQuestion(
                "Quem é o capitão atual da equipe de CS2 da FURIA?",
                listOf("arT", "KSCERATO", "yuurih", "drop"),
                0
            ),
            QuizQuestion(
                "Em que ano a FURIA foi fundada?",
                listOf("2015", "2016", "2017", "2018"),
                2
            ),
            QuizQuestion(
                "Qual foi o primeiro Major de CS:GO que a FURIA participou?",
                listOf("Katowice 2019", "IEM Katowice 2019", "StarLadder Berlin 2019", "IEM Rio 2022"),
                2
            ),
            QuizQuestion(
                "Qual jogador da FURIA ficou conhecido como 'The Undertaker'?",
                listOf("arT", "KSCERATO", "yuurih", "HEN1"),
                0
            ),
            QuizQuestion(
                "Qual é a nacionalidade predominante dos jogadores da FURIA?",
                listOf("Brasileira", "Argentina", "Colombiana", "Chilena"),
                0
            ),
            QuizQuestion(
                "Qual destes jogadores NÃO faz parte do atual elenco de CS2 da FURIA?",
                listOf("KSCERATO", "yuurih", "FalleN", "chelo"),
                2
            ),
            QuizQuestion(
                "Qual a cor predominante no logo da FURIA?",
                listOf("Preto", "Amarelo", "Vermelho", "Azul"),
                1
            ),
            QuizQuestion(
                "Quem é o CEO e co-fundador da FURIA?",
                listOf("Jaime Pádua", "André Akkari", "Cris Guedes", "Guilherme Barbosa"),
                1
            ),
            QuizQuestion(
                "Em qual torneio a FURIA conquistou seu primeiro título internacional de CS:GO?",
                listOf("BLAST Pro Series", "ESL One", "DreamHack", "ESEA MDL Season 30"),
                3
            ),
            QuizQuestion(
                "Qual jogador abaixo NÃO jogou pela FURIA?",
                listOf("honda", "saffee", "coldzera", "drop"),
                2
            ),
            QuizQuestion(
                "Qual é o nome do centro de treinamento da FURIA nos EUA?",
                listOf("FURIA HQ", "FURIA House", "FURIA Center", "FURIA Academy"),
                0
            ),
            QuizQuestion(
                "Em qual cidade brasileira a FURIA foi fundada?",
                listOf("Rio de Janeiro", "São Paulo", "Belo Horizonte", "Salvador"),
                1
            ),
            QuizQuestion(
                "Qual destes jogos a FURIA NÃO possui uma equipe profissional?",
                listOf("Valorant", "CS2", "Dota 2", "League of Legends"),
                2
            ),
            QuizQuestion(
                "Qual é o mascote da FURIA?",
                listOf("Águia", "Lobo", "Pantera", "Leão"),
                1
            )
        )
        
        _uiState.update { it.copy(quizQuestions = questions.shuffled().take(5)) }
    }
    
    private fun resetQuiz() {
        _uiState.update { 
            it.copy(
                currentQuestionIndex = 0,
                selectedAnswerIndex = -1,
                showQuizAnswerResult = false,
                correctAnswers = 0,
                earnedPoints = 0,
                quizCompleted = false
            )
        }
    }
    
    fun selectAnswer(index: Int) {
        val currentState = _uiState.value
        val currentQuestion = currentState.quizQuestions[currentState.currentQuestionIndex]
        val isCorrect = index == currentQuestion.correctAnswerIndex
        
        _uiState.update { 
            it.copy(
                selectedAnswerIndex = index,
                showQuizAnswerResult = true,
                correctAnswers = it.correctAnswers + if (isCorrect) 1 else 0
            )
        }
    }
    
    fun nextQuestion() {
        val currentState = _uiState.value
        
        // Verificar se já é a última questão
        if (currentState.currentQuestionIndex >= currentState.quizQuestions.size - 1) {
            // Se for a última questão, finalizar o quiz
            finishQuiz()
            return
        }
        
        // Se não for a última, avançar para a próxima
        _uiState.update { 
            it.copy(
                currentQuestionIndex = it.currentQuestionIndex + 1,
                selectedAnswerIndex = -1,
                showQuizAnswerResult = false
            )
        }
    }
    
    fun finishQuiz() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    Log.e("MiniGamesViewModel", "Usuário não está logado")
                    return@launch
                }
                
                if (!_uiState.value.quizAvailable) {
                    Log.d("MiniGamesViewModel", "Quiz já foi completado hoje")
                    return@launch
                }
                
                // Calcular pontos baseado no número de respostas corretas
                val points = calculateQuizPoints()
                
                // Atualizar o estado da UI
                _uiState.update { 
                    it.copy(
                        quizCompleted = true,
                        earnedPoints = it.earnedPoints + points
                    )
                }
                
                // Adicionar pontos ao perfil do usuário
                profileRepository.awardPoints(points.toLong())
                
                // Atualizar o leaderboard para verificar se o usuário se tornou top 1
                loadLeaderboard()
                
                // Salvar a data da última vez que o quiz foi jogado
                saveLastPlayedDate("quiz")
                
                // Marcar o desafio diário relacionado ao quiz como completo
                val quizChallenge = _uiState.value.dailyChallenges.find { it.actionData == "QUIZ" }
                if (quizChallenge != null && !quizChallenge.completed) {
                    completeChallenge(quizChallenge)
                }
                
                Log.d("MiniGamesViewModel", "Quiz finalizado, +$points pontos")
            } catch (e: Exception) {
                Log.e("MiniGamesViewModel", "Erro ao finalizar quiz: ${e.message}")
            }
        }
    }
    
    fun calculateQuizPoints(): Int {
        val correctAnswers = _uiState.value.correctAnswers
        val totalQuestions = _uiState.value.quizQuestions.size
        
        return when {
            correctAnswers == totalQuestions -> 50 // Perfect score
            correctAnswers >= totalQuestions * 0.8 -> 40 // 80%+
            correctAnswers >= totalQuestions * 0.6 -> 30 // 60%+
            correctAnswers >= totalQuestions * 0.4 -> 20 // 40%+
            correctAnswers > 0 -> 10 // At least one correct
            else -> 5 // Participation points
        }
    }
    
    fun restartQuiz() {
        viewModelScope.launch {
            try {
                // Inicializar o quiz novamente
                loadQuizQuestions()
                
                // Resetar o estado do quiz
                _uiState.update { 
                    it.copy(
                        currentQuestionIndex = 0,
                        selectedAnswerIndex = -1,
                        showQuizAnswerResult = false,
                        correctAnswers = 0,
                        earnedPoints = 0,
                        quizCompleted = false,
                        quizAvailable = true
                    )
                }
            } catch (e: Exception) {
                Log.e("MiniGamesViewModel", "Erro ao reiniciar quiz: ${e.message}")
            }
        }
    }
    
    // MEMORY GAME FUNCTIONS
    
    private fun initializeMemoryGame() {
        val furiaPlayers = listOf(
            MemoryCardContent("art", R.drawable.ic_arena),
            MemoryCardContent("kscerato", R.drawable.ic_arena),
            MemoryCardContent("yuurih", R.drawable.ic_arena),
            MemoryCardContent("chelo", R.drawable.ic_arena),
            MemoryCardContent("drop", R.drawable.ic_arena),
            MemoryCardContent("saffee", R.drawable.ic_arena),
            MemoryCardContent("honda", R.drawable.ic_arena),
            MemoryCardContent("guerri", R.drawable.ic_arena)
        )
        
        // Criar uma lista com pares exatos para garantir que todos os pares existam
        val firstSet = furiaPlayers.toList()
        val secondSet = furiaPlayers.toList()
        val cardPairs = (firstSet + secondSet).shuffled()
        
        Log.d("MiniGamesViewModel", "Inicializando jogo da memória com ${cardPairs.size} cartas (${cardPairs.size/2} pares)")
        
        val cards = cardPairs.mapIndexed { index, content ->
            MemoryCard(id = index, content = content)
        }
        
        _uiState.update { 
            it.copy(
                memoryCards = cards,
                memoryMoves = 0,
                memoryMatchedPairs = 0,
                memoryIsChecking = false,
                memoryGameCompleted = false,
                earnedPoints = 0
            )
        }
    }
    
    fun flipCard(index: Int) {
        val currentState = _uiState.value
        val cards = currentState.memoryCards.toMutableList()
        
        // Não permite virar cartas durante a verificação ou se o jogo estiver completo
        if (currentState.memoryIsChecking || currentState.memoryGameCompleted) {
            return
        }
        
        // Não permite virar uma carta já virada ou já combinada
        if (cards[index].isFlipped || cards[index].isMatched) {
            return
        }
        
        // Flip the selected card
        cards[index] = cards[index].copy(isFlipped = true)
        
        // Count flipped but unmatched cards
        val flippedCards = cards.filter { it.isFlipped && !it.isMatched }
        
        if (flippedCards.size == 2) {
            // Increment moves
            _uiState.update { 
                it.copy(
                    memoryCards = cards,
                    memoryMoves = it.memoryMoves + 1,
                    memoryIsChecking = true
                )
            }
            
            // Check for match
            viewModelScope.launch {
                delay(1000) // Give player time to see the cards
                
                val card1 = flippedCards[0]
                val card2 = flippedCards[1]
                
                if (card1.content.name == card2.content.name) {
                    // Match found
                    cards[cards.indexOfFirst { it.id == card1.id }] = card1.copy(isMatched = true, isFlipped = false)
                    cards[cards.indexOfFirst { it.id == card2.id }] = card2.copy(isMatched = true, isFlipped = false)
                    
                    val newMatchedPairs = currentState.memoryMatchedPairs + 1
                    val totalPairs = cards.size / 2
                    
                    Log.d("MiniGamesViewModel", "Par encontrado! $newMatchedPairs de $totalPairs pares")
                    
                    val isGameCompleted = newMatchedPairs >= totalPairs
                    
                    if (isGameCompleted) {
                        Log.d("MiniGamesViewModel", "Jogo da memória completado! $newMatchedPairs/$totalPairs pares")
                        // Marcar todos os cartões como matched para destacá-los
                        val updatedCards = cards.map { card -> 
                            card.copy(isMatched = true) 
                        }
                        
                        // Atualizar o estado para mostrar que o jogo foi completado
                        _uiState.update { 
                            it.copy(
                                memoryCards = updatedCards,
                                memoryMatchedPairs = newMatchedPairs,
                                memoryIsChecking = false,
                                memoryGameCompleted = true
                            )
                        }
                        
                        // Chamar a função de finalização para lidar com pontos e desafios
                        finishMemoryGame()
                    } else {
                        _uiState.update { 
                            it.copy(
                                memoryCards = cards,
                                memoryMatchedPairs = newMatchedPairs,
                                memoryIsChecking = false
                            )
                        }
                    }
                } else {
                    // No match, flip cards back
                    cards[cards.indexOfFirst { it.id == card1.id }] = card1.copy(isFlipped = false)
                    cards[cards.indexOfFirst { it.id == card2.id }] = card2.copy(isFlipped = false)
                    
                    _uiState.update { 
                        it.copy(
                            memoryCards = cards,
                            memoryIsChecking = false
                        )
                    }
                }
            }
        } else {
            // Just update the flipped card
            _uiState.update { it.copy(memoryCards = cards) }
        }
    }
    
    fun restartMemoryGame() {
        _uiState.update { 
            it.copy(
                memoryGameCompleted = false,
                memoryMoves = 0,
                memoryMatchedPairs = 0,
                memoryIsChecking = false
            )
        }
        initializeMemoryGame()
    }
    
    fun finishMemoryGame() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    Log.e("MiniGamesViewModel", "Usuário não está logado")
                    return@launch
                }
                
                // Calcular pontos baseado no número de movimentos
                val points = calculateMemoryPoints(_uiState.value.memoryMoves)
                
                // Adicionar pontos ao perfil do usuário
                profileRepository.awardPoints(points.toLong())
                
                // Atualizar o leaderboard para verificar se o usuário se tornou top 1
                loadLeaderboard()
                
                // Atualizar o estado da UI
                _uiState.update { 
                    it.copy(
                        earnedPoints = points
                    )
                }
                
                // Salvar a data da última vez que o jogo da memória foi jogado
                saveLastPlayedDate("memory")
                
                // Marcar o desafio diário relacionado ao jogo da memória como completo
                val memoryChallenge = _uiState.value.dailyChallenges.find { it.actionData == "MEMORY" }
                if (memoryChallenge != null && !memoryChallenge.completed) {
                    completeChallenge(memoryChallenge)
                }
                
                Log.d("MiniGamesViewModel", "Jogo da memória finalizado, +$points pontos")
            } catch (e: Exception) {
                Log.e("MiniGamesViewModel", "Erro ao finalizar jogo da memória: ${e.message}")
            }
        }
    }
    
    fun calculateMemoryPoints(moves: Int): Int {
        val cardPairs = _uiState.value.memoryCards.size / 2
        val perfectScore = cardPairs * 2 // Perfect play would be exactly 2 moves per pair
        
        return when {
            moves <= perfectScore -> 30 // Perfect or near perfect
            moves <= perfectScore * 1.5 -> 25 // Very good
            moves <= perfectScore * 2 -> 20 // Good
            moves <= perfectScore * 2.5 -> 15 // Average
            else -> 10 // Completed but took many moves
        }
    }
    
    // DAILY CHALLENGES FUNCTIONS
    
    private fun loadDailyChallenges() {
        viewModelScope.launch {
            try {
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext)
                
                // Lista de desafios diários
                val challenges = listOf(
                    Challenge(
                        id = "daily_instagram",
                        title = "Visitar Instagram da FURIA",
                        description = "Confira as últimas novidades no Instagram oficial",
                        points = 15,
                        actionType = ChallengeActionType.EXTERNAL_LINK,
                        actionData = "https://www.instagram.com/furiagg/",
                        completed = sharedPrefs.getBoolean("challenge_daily_instagram_completed", false)
                    ),
                    Challenge(
                        id = "daily_twitter",
                        title = "Visitar Twitter da FURIA",
                        description = "Acompanhe as atualizações no Twitter/X oficial",
                        points = 15,
                        actionType = ChallengeActionType.EXTERNAL_LINK,
                        actionData = "https://twitter.com/FURIA",
                        completed = sharedPrefs.getBoolean("challenge_daily_twitter_completed", false)
                    ),
                    Challenge(
                        id = "daily_youtube",
                        title = "Assistir vídeo no YouTube",
                        description = "Assista um vídeo no canal oficial da FURIA",
                        points = 20,
                        actionType = ChallengeActionType.EXTERNAL_LINK,
                        actionData = "https://www.youtube.com/c/furiagg",
                        completed = sharedPrefs.getBoolean("challenge_daily_youtube_completed", false)
                    ),
                    Challenge(
                        id = "daily_twitch",
                        title = "Assistir stream na Twitch",
                        description = "Acompanhe uma transmissão ao vivo na Twitch",
                        points = 20,
                        actionType = ChallengeActionType.EXTERNAL_LINK,
                        actionData = "https://www.twitch.tv/furia",
                        completed = sharedPrefs.getBoolean("challenge_daily_twitch_completed", false)
                    ),
                    Challenge(
                        id = "daily_shop",
                        title = "Visitar loja oficial",
                        description = "Confira os produtos oficiais da FURIA",
                        points = 15,
                        actionType = ChallengeActionType.EXTERNAL_LINK,
                        actionData = "https://furiastore.com.br/",
                        completed = sharedPrefs.getBoolean("challenge_daily_shop_completed", false)
                    ),
                    Challenge(
                        id = "daily_quiz",
                        title = "Complete o Quiz",
                        description = "Responda o quiz diário sobre a FURIA",
                        points = 30,
                        actionType = ChallengeActionType.INTERNAL_GAME,
                        actionData = "QUIZ",
                        completed = sharedPrefs.getBoolean("challenge_daily_quiz_completed", false)
                    ),
                    Challenge(
                        id = "daily_memory",
                        title = "Jogo da Memória",
                        description = "Complete o jogo da memória",
                        points = 25,
                        actionType = ChallengeActionType.INTERNAL_GAME,
                        actionData = "MEMORY",
                        completed = sharedPrefs.getBoolean("challenge_daily_memory_completed", false)
                    ),
                    Challenge(
                        id = "daily_word",
                        title = "Palavras Embaralhadas",
                        description = "Descubra a palavra embaralhada",
                        points = 20,
                        actionType = ChallengeActionType.INTERNAL_GAME,
                        actionData = "WORD_SCRAMBLE",
                        completed = sharedPrefs.getBoolean("challenge_daily_word_completed", false)
                    )
                )
                
                _uiState.update { 
                    it.copy(dailyChallenges = challenges)
                }
                
                Log.d("MiniGamesViewModel", "Desafios diários carregados: ${challenges.size}")
            } catch (e: Exception) {
                Log.e("MiniGamesViewModel", "Erro ao carregar desafios: ${e.message}")
                // Em caso de erro, carregar uma lista vazia para evitar carregamento infinito
                _uiState.update { 
                    it.copy(dailyChallenges = emptyList())
                }
            }
        }
    }
    
    fun completeChallenge(challenge: Challenge) {
        viewModelScope.launch {
            try {
                if (challenge.completed) {
                    // Desafio já foi completado, não fazer nada
                    return@launch
                }
                
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    Log.e("MiniGamesViewModel", "Usuário não está logado")
                    return@launch
                }
                
                // Atualizar o estado da UI para marcar o desafio como concluído
                val updatedChallenges = _uiState.value.dailyChallenges.map {
                    if (it.id == challenge.id) it.copy(completed = true) else it
                }
                
                _uiState.update { 
                    it.copy(
                        dailyChallenges = updatedChallenges,
                        earnedPoints = it.earnedPoints + challenge.points
                    )
                }
                
                // Adicionar pontos ao perfil do usuário
                profileRepository.awardPoints(challenge.points.toLong())
                
                // Atualizar o leaderboard para verificar se o usuário se tornou top 1
                loadLeaderboard()
                
                // Salvar o estado do desafio em SharedPreferences
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext)
                val editor = sharedPrefs.edit()
                editor.putBoolean("challenge_${challenge.id}_completed", true)
                editor.apply()
                
                Log.d("MiniGamesViewModel", "Desafio ${challenge.id} completado, +${challenge.points} pontos")
            } catch (e: Exception) {
                Log.e("MiniGamesViewModel", "Erro ao completar desafio: ${e.message}")
            }
        }
    }
    
    fun finishChallenges() {
        // Retorna para a tela de seleção de jogos
        _uiState.update { 
            it.copy(
                currentGame = CurrentGame.NONE
            )
        }
    }
    
    // WORD SCRAMBLE GAME FUNCTIONS
    
    fun initializeWordScrambleGame() {
        val words = listOf(
            "FURIA", "BRASIL", "CSGO", "VALORANT", "ESPORTS", 
            "GAMING", "AKKARI", "KSCERATO", "YUURIH", "ART", 
            "CHELO", "DROP", "VITORIA", "CAMPEAO", "LOBO"
        )
        
        val selectedWord = words.random()
        val scrambledWord = selectedWord.toCharArray().apply { shuffle() }.joinToString("")
        
        Log.d("MiniGamesViewModel", "Iniciando jogo de palavras. Palavra: $selectedWord, Embaralhada: $scrambledWord")
        
        _uiState.update {
            it.copy(
                wordScrambleOriginalWord = selectedWord,
                wordScrambleScrambledWord = scrambledWord,
                wordScrambleUserInput = "",
                wordScrambleAttempts = 0,
                wordScrambleCompleted = false,
                wordScrambleSuccess = false,
                earnedPoints = 0
            )
        }
    }
    
    fun updateWordScrambleInput(input: String) {
        _uiState.update {
            it.copy(wordScrambleUserInput = input.uppercase())
        }
    }
    
    fun checkWordScrambleSolution() {
        viewModelScope.launch {
            try {
                val userInput = _uiState.value.wordScrambleUserInput.trim().lowercase()
                val originalWord = _uiState.value.wordScrambleOriginalWord.lowercase()
                
                if (userInput.isEmpty()) {
                    return@launch
                }
                
                val attempts = _uiState.value.wordScrambleAttempts + 1
                val success = userInput == originalWord
                
                _uiState.update { 
                    it.copy(
                        wordScrambleAttempts = attempts
                    )
                }
                
                if (success || attempts >= 5) {
                    // Se acertou ou atingiu o número máximo de tentativas, finaliza o jogo
                    val points = if (success) {
                        // Pontos baseados no número de tentativas
                        when (attempts) {
                            1 -> 50
                            2 -> 40
                            3 -> 30
                            4 -> 20
                            else -> 10
                        }
                    } else {
                        0 // Sem pontos se não acertou
                    }
                    
                    val userId = auth.currentUser?.uid
                    if (userId != null && success) {
                        // Adicionar pontos ao perfil do usuário apenas se acertou
                        profileRepository.awardPoints(points.toLong())
                    }
                    
                    // Atualizar o estado da UI
                    _uiState.update {
                        it.copy(
                            wordScrambleCompleted = true,
                            wordScrambleSuccess = success,
                            earnedPoints = if (success) it.earnedPoints + points else it.earnedPoints
                        )
                    }
                    
                    // Salvar a data da última vez que o jogo de palavras embaralhadas foi jogado
                    if (success) {
                        saveLastPlayedDate("word_scramble")
                        
                        // Marcar o desafio diário relacionado ao jogo de palavras embaralhadas como completo
                        val wordChallenge = _uiState.value.dailyChallenges.find { it.actionData == "WORD_SCRAMBLE" }
                        if (wordChallenge != null && !wordChallenge.completed) {
                            completeChallenge(wordChallenge)
                        }
                    }
                    
                    Log.d("MiniGamesViewModel", "Jogo de palavras embaralhadas finalizado, sucesso: $success, +$points pontos")
                } else {
                    // Feedback para o usuário
                    Log.d("MiniGamesViewModel", "Tentativa incorreta: $userInput != $originalWord, tentativas: $attempts")
                }
                
                // Limpar o campo de entrada
                _uiState.update { 
                    it.copy(wordScrambleUserInput = "")
                }
            } catch (e: Exception) {
                Log.e("MiniGamesViewModel", "Erro ao verificar solução: ${e.message}")
            }
        }
    }
    
    fun resetWordScrambleGame() {
        _uiState.update { 
            it.copy(
                wordScrambleCompleted = false,
                wordScrambleSuccess = false,
                wordScrambleAttempts = 0,
                wordScrambleUserInput = ""
            )
        }
        initializeWordScrambleGame()
    }
    
    fun finishWordScrambleGame() {
        _uiState.update { 
            it.copy(
                wordScrambleCompleted = true
            )
        }
    }
    
    /**
     * Função de teste para adicionar pontos diretamente.
     * Isso permite verificar se o sistema de pontos está funcionando corretamente.
     */
    suspend fun testAddPoints(points: Long) {
        try {
            Log.d("MiniGamesViewModel", "Teste: Adicionando $points pontos")
            profileRepository.awardPoints(points)
            
            // Forçar uma atualização da UI após adicionar pontos
            val userId = auth.currentUser?.uid ?: return
            
            // Atualizar o leaderboard para verificar se o usuário se tornou top 1
            loadLeaderboard()
            
            // Apenas um log para confirmar que os pontos foram adicionados
            Log.d("MiniGamesViewModel", "Pontos adicionados com sucesso, atualizando UI")
        } catch (e: Exception) {
            Log.e("MiniGamesViewModel", "Erro ao testar adição de pontos: ${e.message}", e)
            throw e
        }
    }
    
    private fun loadLastPlayedDates() {
        viewModelScope.launch {
            try {
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext)
                
                val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
                
                val lastQuizWeek = sharedPrefs.getInt("last_quiz_week", 0)
                val lastMemoryGameDay = sharedPrefs.getInt("last_memory_game_day", 0)
                val lastWordScrambleDay = sharedPrefs.getInt("last_word_scramble_day", 0)
                val lastChallengeDay = sharedPrefs.getInt("last_challenge_day", 0)
                
                _uiState.update { 
                    it.copy(
                        lastQuizDay = lastQuizWeek,
                        lastMemoryGameDay = lastMemoryGameDay,
                        lastWordScrambleDay = lastWordScrambleDay,
                        lastChallengeDay = lastChallengeDay,
                        quizAvailable = lastQuizWeek != currentWeek
                    )
                }
                
                Log.d("MiniGamesViewModel", "Datas carregadas: Quiz semana $lastQuizWeek (atual: $currentWeek), " +
                        "Memória dia $lastMemoryGameDay, Palavras dia $lastWordScrambleDay, Desafios dia $lastChallengeDay (hoje: $today)")
            } catch (e: Exception) {
                Log.e("MiniGamesViewModel", "Erro ao carregar datas: ${e.message}")
            }
        }
    }
    
    private fun saveLastPlayedDate(gameType: String) {
        viewModelScope.launch {
            try {
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext)
                val editor = sharedPrefs.edit()
                
                val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
                
                when (gameType) {
                    "quiz" -> {
                        editor.putInt("last_quiz_week", currentWeek)
                        _uiState.update { it.copy(lastQuizDay = currentWeek, quizAvailable = false) }
                    }
                    "memory" -> {
                        editor.putInt("last_memory_game_day", today)
                        _uiState.update { it.copy(lastMemoryGameDay = today) }
                    }
                    "word_scramble" -> {
                        editor.putInt("last_word_scramble_day", today)
                        _uiState.update { it.copy(lastWordScrambleDay = today) }
                    }
                    "challenge" -> {
                        editor.putInt("last_challenge_day", today)
                        _uiState.update { it.copy(lastChallengeDay = today) }
                    }
                }
                
                editor.apply()
                Log.d("MiniGamesViewModel", "Data salva para $gameType")
            } catch (e: Exception) {
                Log.e("MiniGamesViewModel", "Erro ao salvar data: ${e.message}")
            }
        }
    }
    
    // Verifica se um jogo está disponível
    fun isGameAvailable(game: CurrentGame): Boolean {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        
        return when (game) {
            CurrentGame.QUIZ -> _uiState.value.lastQuizDay != currentWeek
            CurrentGame.MEMORY -> _uiState.value.lastMemoryGameDay != today
            CurrentGame.WORD_SCRAMBLE -> _uiState.value.lastWordScrambleDay != today
            CurrentGame.CHALLENGES -> true // Desafios diários sempre disponíveis
            else -> true
        }
    }
    
    private fun loadLeaderboard() {
        viewModelScope.launch {
            try {
                profileRepository.getLeaderboard().collectLatest { leaderboard ->
                    val userId = auth.currentUser?.uid ?: return@collectLatest
                    val isTopOne = leaderboard.isNotEmpty() && leaderboard.first().id == userId
                    
                    _uiState.update { 
                        it.copy(
                            leaderboard = leaderboard,
                            isTopOne = isTopOne
                        )
                    }
                    
                    Log.d("MiniGamesViewModel", "Leaderboard carregado: ${leaderboard.size} usuários")
                }
            } catch (e: Exception) {
                Log.e("MiniGamesViewModel", "Erro ao carregar leaderboard: ${e.message}", e)
            }
        }
    }
}
