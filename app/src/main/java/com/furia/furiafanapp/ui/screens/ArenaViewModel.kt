package com.furia.furiafanapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furia.furiafanapp.data.model.ArenaStats
import com.furia.furiafanapp.data.model.Bet
import com.furia.furiafanapp.data.model.BetOdds
import com.furia.furiafanapp.data.model.BetStatus
import com.furia.furiafanapp.data.model.BetType
import com.furia.furiafanapp.data.model.Match
import com.furia.furiafanapp.data.model.MatchStatus
import com.furia.furiafanapp.data.model.Team
import com.furia.furiafanapp.data.model.Tournament
import com.furia.furiafanapp.data.model.UserProfile
import com.furia.furiafanapp.data.repository.ArenaRepository
import com.furia.furiafanapp.data.repository.MatchRepository
import com.furia.furiafanapp.data.repository.UserVerificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ArenaViewModel @Inject constructor(
    private val arenaRepository: ArenaRepository,
    private val matchRepository: MatchRepository,
    private val userVerificationRepository: UserVerificationRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // Estados para a UI
    private val _uiState = MutableStateFlow<ArenaUiState>(ArenaUiState.Loading)
    val uiState: StateFlow<ArenaUiState> = _uiState.asStateFlow()
    
    private val _userBets = MutableStateFlow<List<Bet>>(emptyList())
    val userBets: StateFlow<List<Bet>> = _userBets.asStateFlow()
    
    private val _arenaStats = MutableStateFlow(ArenaStats())
    val arenaStats: StateFlow<ArenaStats> = _arenaStats.asStateFlow()
    
    private val _leaderboard = MutableStateFlow<List<ArenaStats>>(emptyList())
    val leaderboard: StateFlow<List<ArenaStats>> = _leaderboard.asStateFlow()
    
    private val _availablePoints = MutableStateFlow(0)
    val availablePoints: StateFlow<Int> = _availablePoints.asStateFlow()
    
    private val _upcomingMatches = MutableStateFlow<List<Match>>(emptyList())
    val upcomingMatches: StateFlow<List<Match>> = _upcomingMatches.asStateFlow()
    
    private val _selectedMatch = MutableStateFlow<Match?>(null)
    val selectedMatch: StateFlow<Match?> = _selectedMatch.asStateFlow()
    
    private val _matchOdds = MutableStateFlow<BetOdds?>(null)
    val matchOdds: StateFlow<BetOdds?> = _matchOdds.asStateFlow()
    
    private val _betAmount = MutableStateFlow("10")
    val betAmount: StateFlow<String> = _betAmount.asStateFlow()
    
    private val _selectedBetType = MutableStateFlow<BetType?>(null)
    val selectedBetType: StateFlow<BetType?> = _selectedBetType.asStateFlow()
    
    private val _selectedPlayer = MutableStateFlow<String?>(null)
    val selectedPlayer: StateFlow<String?> = _selectedPlayer.asStateFlow()
    
    private val _statPrediction = MutableStateFlow<Int?>(null)
    val statPrediction: StateFlow<Int?> = _statPrediction.asStateFlow()
    
    private val _potentialWinnings = MutableStateFlow(0L)
    val potentialWinnings: StateFlow<Long> = _potentialWinnings.asStateFlow()
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        loadUserProfile()
        loadUpcomingMatches()
        loadUserBets()
        loadLeaderboard()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                
                // Obter dados do usuário do Firestore
                val userRef = firestore.collection("users").document(userId)
                val snapshot = userRef.get().await()
                
                if (snapshot.exists()) {
                    val nickname = snapshot.getString("nickname") ?: "Usuário"
                    val photoUrl = snapshot.getString("photoUrl") ?: ""
                    val points = snapshot.getLong("points") ?: 0L
                    
                    val userProfile = UserProfile(
                        id = userId,
                        nickname = nickname,
                        photoUrl = photoUrl,
                        points = points
                    )
                    
                    _userProfile.value = userProfile
                    
                    // Atualizar pontos disponíveis para apostas
                    _availablePoints.value = points.toInt()
                    
                    // Atualizar as estatísticas da arena com os pontos corretos
                    val currentStats = _arenaStats.value
                    if (currentStats.userId == userId) {
                        _arenaStats.value = currentStats.copy(
                            totalPointsWon = points.toInt(),
                            username = nickname
                        )
                        
                        // Atualizar também no Firestore para manter a consistência
                        firestore.collection("arenaStats").document(userId)
                            .update(
                                "totalPointsWon", points.toInt(),
                                "username", nickname
                            )
                            .await()
                    }
                    
                    // Recarregar o leaderboard para refletir as alterações
                    loadLeaderboard()
                }
            } catch (e: Exception) {
                // Log.e("ArenaViewModel", "Erro ao carregar perfil do usuário", e)
            }
        }
    }
    
    private fun loadUpcomingMatches() {
        viewModelScope.launch {
            try {
                // Carregar próximas partidas
                val matches = matchRepository.getUpcomingMatches().first()
                
                // Se não houver partidas, adicionar uma partida de exemplo para testes
                if (matches.isEmpty()) {
                    val exampleTeam1 = Team(
                        id = "furia-id",
                        name = "FURIA",
                        logoUrl = "https://furiashop.com.br/cdn/shop/files/JERSEY_FRENTE_FURIA_2023_800x.jpg"
                    )
                    
                    val exampleTeam2 = Team(
                        id = "navi-id",
                        name = "NAVI",
                        logoUrl = "https://s2.glbimg.com/4Ek8CnZSuYxRQQJKGxqWKuYmRIw=/0x0:1280x720/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2023/7/a/MNfOxASxA3AAGk2FRPww/furia-blast.jpg"
                    )
                    
                    val exampleTournament = Tournament(
                        id = "major-cs2-2025",
                        name = "Major CS2 2025",
                        game = "CS2"
                    )
                    
                    val exampleMatch = Match(
                        id = "example-match-1",
                        homeTeam = exampleTeam1,
                        awayTeam = exampleTeam2,
                        tournament = exampleTournament,
                        startTime = Date(System.currentTimeMillis() + 86400000), // Amanhã
                        status = MatchStatus.SCHEDULED
                    )
                    _upcomingMatches.value = listOf(exampleMatch)
                } else {
                    _upcomingMatches.value = matches
                }
                
                _uiState.value = ArenaUiState.Success
            } catch (e: Exception) {
                _uiState.value = ArenaUiState.Error("Erro ao carregar dados: ${e.message}")
            }
        }
    }
    
    private fun loadUserBets() {
        viewModelScope.launch {
            try {
                // Carregar apostas do usuário
                arenaRepository.getUserBets().collectLatest { bets ->
                    _userBets.value = bets
                }
            } catch (e: Exception) {
                _uiState.value = ArenaUiState.Error("Erro ao carregar apostas: ${e.message}")
            }
        }
    }
    
    private fun loadLeaderboard() {
        viewModelScope.launch {
            try {
                // Coletar estatísticas do repositório
                arenaRepository.getLeaderboard().collectLatest { leaderboardStats ->
                    // Filtrar apenas usuários que ainda existem no banco de dados
                    val validStats = mutableListOf<ArenaStats>()
                    
                    for (stats in leaderboardStats) {
                        if (userVerificationRepository.userExists(stats.userId)) {
                            validStats.add(stats)
                        } else {
                            // Usuário não existe mais, limpar seus dados
                            viewModelScope.launch {
                                userVerificationRepository.cleanupDeletedUserData(stats.userId)
                            }
                        }
                    }
                    
                    _leaderboard.value = validStats
                    
                    // Verificar se o usuário atual ainda existe
                    if (auth.currentUser != null) {
                        val userExists = userVerificationRepository.verifyCurrentUserOrLogout()
                        if (!userExists) {
                            _uiState.value = ArenaUiState.Error("Sua conta foi excluída. Faça login novamente.")
                        }
                    }
                    
                    _uiState.value = ArenaUiState.Success
                }
            } catch (e: Exception) {
                _uiState.value = ArenaUiState.Error("Erro ao carregar leaderboard: ${e.message}")
            }
        }
    }
    
    fun selectMatch(match: Match) {
        viewModelScope.launch {
            _selectedMatch.value = match
            _selectedBetType.value = null
            _selectedPlayer.value = null
            _statPrediction.value = null
            
            try {
                val odds = arenaRepository.calculateOdds(match)
                _matchOdds.value = odds
                calculatePotentialWinnings()
            } catch (e: Exception) {
                _uiState.value = ArenaUiState.Error("Erro ao calcular odds: ${e.message}")
            }
        }
    }
    
    fun updateBetAmount(amount: String) {
        _betAmount.value = amount
        calculatePotentialWinnings()
    }
    
    fun selectBetType(betType: BetType) {
        _selectedBetType.value = betType
        // Resetar valores relacionados quando mudar o tipo de aposta
        if (betType == BetType.TEAM_TOTAL_KILLS) {
            _selectedPlayer.value = null
        }
        
        if (betType == BetType.MVP_PREDICTION || betType == BetType.FIRST_BLOOD) {
            _statPrediction.value = null
        } else {
            // Definir um valor padrão inicial para a previsão de estatística
            _statPrediction.value = when(betType) {
                BetType.KILL_COUNT -> 20
                BetType.HEADSHOT_PERCENTAGE -> 50
                BetType.CLUTCH_ROUNDS -> 3
                BetType.ACE_COUNT -> 1
                BetType.BOMB_PLANTS -> 5
                BetType.KNIFE_KILLS -> 1
                BetType.PISTOL_ROUND_WINS -> 2
                BetType.TEAM_TOTAL_KILLS -> 60
                else -> 0
            }
        }
        
        calculatePotentialWinnings()
    }
    
    fun selectPlayer(player: String) {
        _selectedPlayer.value = player
        calculatePotentialWinnings()
    }
    
    fun updateStatPrediction(value: Int) {
        _statPrediction.value = value
        calculatePotentialWinnings()
    }
    
    private fun calculatePotentialWinnings() {
        val betType = _selectedBetType.value ?: return
        val odds = _matchOdds.value ?: return
        val amount = _betAmount.value.toIntOrNull() ?: 10
        
        // Calcular odds com base no tipo de aposta e valores selecionados
        val betOdds = when(betType) {
            BetType.MVP_PREDICTION -> {
                val player = _selectedPlayer.value ?: return
                odds.mvpOdds[player] ?: 5.0
            }
            BetType.KILL_COUNT -> {
                val prediction = _statPrediction.value ?: return
                val range = when(prediction) {
                    in 0..10 -> "0-10"
                    in 11..20 -> "11-20"
                    in 21..30 -> "21-30"
                    else -> "31+"
                }
                odds.killCountRanges[range] ?: 3.0
            }
            BetType.HEADSHOT_PERCENTAGE -> {
                val prediction = _statPrediction.value ?: return
                val range = when(prediction) {
                    in 0..40 -> "0-40"
                    in 41..60 -> "41-60"
                    in 61..80 -> "61-80"
                    else -> "81-100"
                }
                odds.headshotRanges[range] ?: 4.0
            }
            BetType.CLUTCH_ROUNDS -> {
                val prediction = _statPrediction.value ?: return
                val range = when(prediction) {
                    0 -> "0"
                    in 1..2 -> "1-2"
                    in 3..4 -> "3-4"
                    else -> "5+"
                }
                odds.clutchRanges[range] ?: 3.5
            }
            BetType.ACE_COUNT -> {
                val prediction = _statPrediction.value ?: return
                odds.aceCountOdds[prediction.toString()] ?: 10.0
            }
            BetType.FIRST_BLOOD -> {
                val player = _selectedPlayer.value ?: return
                odds.firstBloodOdds[player] ?: 4.0
            }
            BetType.BOMB_PLANTS -> {
                val prediction = _statPrediction.value ?: return
                val range = when(prediction) {
                    in 0..3 -> "0-3"
                    in 4..6 -> "4-6"
                    in 7..10 -> "7-10"
                    else -> "11+"
                }
                odds.bombPlantRanges[range] ?: 3.0
            }
            BetType.KNIFE_KILLS -> {
                val prediction = _statPrediction.value ?: return
                odds.knifeKillOdds[prediction.toString()] ?: 15.0
            }
            BetType.PISTOL_ROUND_WINS -> {
                val prediction = _statPrediction.value ?: return
                odds.pistolRoundOdds[prediction.toString()] ?: 4.0
            }
            BetType.TEAM_TOTAL_KILLS -> {
                val prediction = _statPrediction.value ?: return
                val range = when(prediction) {
                    in 0..40 -> "0-40"
                    in 41..60 -> "41-60"
                    in 61..80 -> "61-80"
                    else -> "81+"
                }
                odds.teamKillRanges[range] ?: 2.5
            }
        }
        
        _potentialWinnings.value = (amount * betOdds).toLong()
    }
    
    fun placeBet() {
        val match = _selectedMatch.value ?: return
        val betType = _selectedBetType.value ?: return
        val amount = _betAmount.value.toIntOrNull() ?: return
        val player = _selectedPlayer.value
        val statPrediction = _statPrediction.value
        
        // Validar que temos todos os dados necessários para a aposta
        if (betType != BetType.TEAM_TOTAL_KILLS && player == null) return
        if (betType != BetType.MVP_PREDICTION && betType != BetType.FIRST_BLOOD && statPrediction == null) return
        
        viewModelScope.launch {
            _uiState.value = ArenaUiState.Loading
            try {
                // Criar o objeto de aposta
                val bet = Bet(
                    userId = auth.currentUser?.uid ?: "",
                    matchId = match.id,
                    betAmount = amount,
                    betType = betType,
                    playerName = player ?: "",
                    statPrediction = statPrediction ?: 0,
                    odds = _potentialWinnings.value.toDouble() / amount,
                    potentialWinnings = _potentialWinnings.value.toInt(),
                    status = BetStatus.PENDING
                )
                
                // Salvar a aposta
                val result = arenaRepository.placeBet(bet)
                
                if (result.isSuccess) {
                    _uiState.value = ArenaUiState.BetPlaced(result.getOrThrow())
                    // Atualizar pontos disponíveis
                    _availablePoints.value = arenaRepository.getUserAvailablePoints()
                    // Limpar seleção
                    clearSelection()
                } else {
                    _uiState.value = ArenaUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao fazer aposta")
                }
            } catch (e: Exception) {
                _uiState.value = ArenaUiState.Error("Erro ao fazer aposta: ${e.message}")
            }
        }
    }
    
    private fun clearSelection() {
        _selectedMatch.value = null
        _matchOdds.value = null
        _betAmount.value = "10"
        _selectedBetType.value = null
        _selectedPlayer.value = null
        _statPrediction.value = null
        _potentialWinnings.value = 0L
    }
    
    fun refresh() {
        _uiState.value = ArenaUiState.Loading
        loadUserProfile()
        loadUpcomingMatches()
        loadUserBets()
        loadLeaderboard()
    }
}

sealed class ArenaUiState {
    object Loading : ArenaUiState()
    object Success : ArenaUiState()
    data class Error(val message: String) : ArenaUiState()
    data class BetPlaced(val bet: Bet) : ArenaUiState()
}
