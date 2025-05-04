package com.furia.furiafanapp.data.repository

import com.furia.furiafanapp.data.model.ArenaStats
import com.furia.furiafanapp.data.model.Bet
import com.furia.furiafanapp.data.model.BetOdds
import com.furia.furiafanapp.data.model.BetStatus
import com.furia.furiafanapp.data.model.Match
import com.furia.furiafanapp.data.model.Team
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import java.util.Date

interface ArenaRepository {
    fun getUserBets(): Flow<List<Bet>>
    fun getArenaStats(): Flow<ArenaStats>
    fun getLeaderboard(): Flow<List<ArenaStats>>
    suspend fun calculateOdds(match: Match): BetOdds
    suspend fun placeBet(matchId: String, amount: Int, predictedWinner: String, predictedScore: String? = null): Result<Bet>
    suspend fun placeBet(bet: Bet): Result<Bet>
    suspend fun settleBet(betId: String, actualWinner: String, actualScore: String): Result<Bet>
    suspend fun getUserAvailablePoints(): Int
}

@Singleton
class ArenaRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val gamificationRepository: GamificationRepository
) : ArenaRepository {

    private val userId: String get() = auth.currentUser?.uid ?: ""
    
    private val _userBets = MutableStateFlow<List<Bet>>(emptyList())
    private val _arenaStats = MutableStateFlow(ArenaStats())
    private val _leaderboard = MutableStateFlow<List<ArenaStats>>(emptyList())
    
    override fun getUserBets(): Flow<List<Bet>> = _userBets
    override fun getArenaStats(): Flow<ArenaStats> = _arenaStats
    override fun getLeaderboard(): Flow<List<ArenaStats>> = _leaderboard

    init {
        if (userId.isNotEmpty()) {
            loadUserBets()
            loadArenaStats()
            loadLeaderboard()
        }
    }

    private fun loadUserBets() {
        firestore.collection("bets")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val bets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Bet::class.java)
                } ?: emptyList()
                _userBets.value = bets
            }
    }

    private fun loadArenaStats() {
        firestore.collection("arenaStats")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val stats = snapshot?.toObject(ArenaStats::class.java) ?: ArenaStats(userId = userId)
                _arenaStats.value = stats
            }
    }

    private fun loadLeaderboard() {
        firestore.collection("arenaStats")
            .orderBy("accuracy", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                val statsDocuments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ArenaStats::class.java)
                } ?: emptyList()
                
                // Filtramos apenas os usuários que ainda existem no banco de dados
                firestore.runTransaction { transaction ->
                    val validStats = mutableListOf<ArenaStats>()
                    
                    for (stats in statsDocuments) {
                        val userRef = firestore.collection("users").document(stats.userId)
                        val userDoc = transaction.get(userRef)
                        
                        if (userDoc.exists()) {
                            // Usuário existe, adicionar às estatísticas válidas
                            validStats.add(stats)
                        } else {
                            // Usuário não existe mais, remover suas estatísticas
                            firestore.collection("arenaStats").document(stats.userId).delete()
                        }
                    }
                    
                    _leaderboard.value = validStats
                }
            }
    }

    override suspend fun calculateOdds(match: Match): BetOdds {
        // Sistema avançado de balanceamento de odds baseado em múltiplos fatores
        
        // 1. Obter dados históricos (simulado para este exemplo)
        val homeTeamStrength = calculateTeamStrength(match.homeTeam)
        val awayTeamStrength = calculateTeamStrength(match.awayTeam)
        
        // 2. Calcular odds base usando a força relativa das equipes
        val strengthDifference = homeTeamStrength - awayTeamStrength
        
        // Odds base - quanto mais próximo de 1.0, maior a chance de vitória
        var homeOdds = 2.5 - (strengthDifference * 0.5).coerceIn(-0.7, 0.7)
        var awayOdds = 2.5 + (strengthDifference * 0.5).coerceIn(-0.7, 0.7)
        
        // 3. Ajustar com base no tipo de jogo/torneio
        val tournamentFactor = when {
            match.tournament.name.contains("Major", ignoreCase = true) -> 0.9 // Jogos de Major são mais imprevisíveis
            match.tournament.name.contains("Final", ignoreCase = true) -> 0.85 // Finais são mais imprevisíveis
            else -> 1.0
        }
        
        homeOdds *= tournamentFactor
        awayOdds *= tournamentFactor
        
        // 4. Ajustar com base no jogo (CS:GO, Valorant, etc)
        val gameFactor = when (match.tournament.game) {
            "CS2" -> 1.0
            "Valorant" -> 0.95 // Valorant tem mais variabilidade
            "League of Legends" -> 0.9 // LoL favorece mais os times fortes
            else -> 1.0
        }
        
        homeOdds *= gameFactor
        awayOdds *= gameFactor
        
        // 5. Calcular odds de empate (relevante para alguns jogos)
        val drawPossible = match.tournament.game != "Valorant" && match.tournament.game != "League of Legends"
        val drawOdds = if (drawPossible) {
            // Quanto mais próximas as forças das equipes, maior a chance de empate
            val drawFactor = 1.0 - Math.abs(strengthDifference).coerceIn(0.0, 0.5)
            3.0 + (drawFactor * 2.0)
        } else {
            15.0 // Odds muito altas para jogos onde empate é quase impossível
        }
        
        // 6. Gerar odds para placares exatos
        val exactScores = generateExactScores(match, homeTeamStrength, awayTeamStrength)
        
        // 7. Garantir que as odds sejam razoáveis (limites mínimos e máximos)
        homeOdds = homeOdds.coerceIn(1.2, 5.0)
        awayOdds = awayOdds.coerceIn(1.2, 5.0)
        
        return BetOdds(
            homeWin = homeOdds,
            awayWin = awayOdds,
            draw = drawOdds,
            exactScores = exactScores
        )
    }
    
    private fun calculateTeamStrength(team: Team): Double {
        // Em um sistema real, isso viria de um banco de dados com estatísticas históricas
        // Para este exemplo, usamos alguns valores fixos para times conhecidos
        // e valores aleatórios consistentes para outros times
        
        return when (team.name) {
            "FURIA" -> 0.85 // FURIA é muito forte
            "Liquid" -> 0.82
            "NAVI" -> 0.88
            "G2" -> 0.84
            "Cloud9" -> 0.80
            "FaZe" -> 0.83
            "Astralis" -> 0.79
            "MIBR" -> 0.75
            "paiN" -> 0.76
            "Imperial" -> 0.77
            else -> {
                // Para times desconhecidos, gerar um valor baseado no hash do nome
                // para que seja consistente entre chamadas
                val hash = team.name.hashCode()
                val random = Random(hash)
                0.65 + (random.nextDouble() * 0.2) // Entre 0.65 e 0.85
            }
        }
    }
    
    private fun generateExactScores(match: Match, homeStrength: Double, awayStrength: Double): Map<String, Double> {
        val result = mutableMapOf<String, Double>()
        
        // Diferentes jogos têm diferentes formatos de placar
        when (match.tournament.game) {
            "CS2" -> {
                // Formatos comuns para CS2: 2-0, 2-1, 1-2, 0-2 (Bo3) ou 3-0, 3-1, 3-2, 2-3, 1-3, 0-3 (Bo5)
                val isBo5 = match.tournament.name.contains("Final", ignoreCase = true) || 
                            match.tournament.name.contains("Playoff", ignoreCase = true)
                
                if (isBo5) {
                    // Bo5 - Odds baseadas na força relativa
                    val strengthDiff = homeStrength - awayStrength
                    
                    // Home win scenarios
                    result["3-0"] = (10.0 - (strengthDiff * 3.0)).coerceIn(5.0, 15.0)
                    result["3-1"] = (7.0 - (strengthDiff * 2.0)).coerceIn(4.0, 10.0)
                    result["3-2"] = (8.0 - (strengthDiff * 1.0)).coerceIn(5.0, 12.0)
                    
                    // Away win scenarios
                    result["2-3"] = (8.0 + (strengthDiff * 1.0)).coerceIn(5.0, 12.0)
                    result["1-3"] = (7.0 + (strengthDiff * 2.0)).coerceIn(4.0, 10.0)
                    result["0-3"] = (10.0 + (strengthDiff * 3.0)).coerceIn(5.0, 15.0)
                } else {
                    // Bo3 - Odds baseadas na força relativa
                    val strengthDiff = homeStrength - awayStrength
                    
                    // Home win scenarios
                    result["2-0"] = (4.0 - (strengthDiff * 1.5)).coerceIn(2.5, 7.0)
                    result["2-1"] = (3.5 - (strengthDiff * 0.5)).coerceIn(2.5, 5.0)
                    
                    // Away win scenarios
                    result["1-2"] = (3.5 + (strengthDiff * 0.5)).coerceIn(2.5, 5.0)
                    result["0-2"] = (4.0 + (strengthDiff * 1.5)).coerceIn(2.5, 7.0)
                }
            }
            "Valorant" -> {
                // Formatos para Valorant: 2-0, 2-1, 1-2, 0-2 (Bo3) ou 3-0, 3-1, 3-2, 2-3, 1-3, 0-3 (Bo5)
                val isBo5 = match.tournament.name.contains("Final", ignoreCase = true) || 
                            match.tournament.name.contains("Playoff", ignoreCase = true)
                
                if (isBo5) {
                    // Bo5 - Odds similares ao CS2 mas com ajustes para Valorant
                    val strengthDiff = homeStrength - awayStrength
                    
                    // Home win scenarios
                    result["3-0"] = (9.0 - (strengthDiff * 3.0)).coerceIn(5.0, 15.0)
                    result["3-1"] = (6.5 - (strengthDiff * 2.0)).coerceIn(4.0, 10.0)
                    result["3-2"] = (7.5 - (strengthDiff * 1.0)).coerceIn(5.0, 12.0)
                    
                    // Away win scenarios
                    result["2-3"] = (7.5 + (strengthDiff * 1.0)).coerceIn(5.0, 12.0)
                    result["1-3"] = (6.5 + (strengthDiff * 2.0)).coerceIn(4.0, 10.0)
                    result["0-3"] = (9.0 + (strengthDiff * 3.0)).coerceIn(5.0, 15.0)
                } else {
                    // Bo3 - Odds similares ao CS2 mas com ajustes para Valorant
                    val strengthDiff = homeStrength - awayStrength
                    
                    // Home win scenarios
                    result["2-0"] = (3.8 - (strengthDiff * 1.5)).coerceIn(2.5, 7.0)
                    result["2-1"] = (3.3 - (strengthDiff * 0.5)).coerceIn(2.5, 5.0)
                    
                    // Away win scenarios
                    result["1-2"] = (3.3 + (strengthDiff * 0.5)).coerceIn(2.5, 5.0)
                    result["0-2"] = (3.8 + (strengthDiff * 1.5)).coerceIn(2.5, 7.0)
                }
            }
            "League of Legends" -> {
                // Formatos para LoL: 2-0, 2-1, 1-2, 0-2 (Bo3) ou 3-0, 3-1, 3-2, 2-3, 1-3, 0-3 (Bo5)
                val isBo5 = match.tournament.name.contains("Final", ignoreCase = true) || 
                            match.tournament.name.contains("Playoff", ignoreCase = true)
                
                if (isBo5) {
                    // Bo5 - Odds para LoL (favorece mais os times fortes)
                    val strengthDiff = homeStrength - awayStrength
                    
                    // Home win scenarios
                    result["3-0"] = (8.0 - (strengthDiff * 4.0)).coerceIn(4.0, 12.0)
                    result["3-1"] = (6.0 - (strengthDiff * 3.0)).coerceIn(3.5, 9.0)
                    result["3-2"] = (7.0 - (strengthDiff * 2.0)).coerceIn(4.5, 10.0)
                    
                    // Away win scenarios
                    result["2-3"] = (7.0 + (strengthDiff * 2.0)).coerceIn(4.5, 10.0)
                    result["1-3"] = (6.0 + (strengthDiff * 3.0)).coerceIn(3.5, 9.0)
                    result["0-3"] = (8.0 + (strengthDiff * 4.0)).coerceIn(4.0, 12.0)
                } else {
                    // Bo3 - Odds para LoL (favorece mais os times fortes)
                    val strengthDiff = homeStrength - awayStrength
                    
                    // Home win scenarios
                    result["2-0"] = (3.5 - (strengthDiff * 2.0)).coerceIn(2.0, 6.0)
                    result["2-1"] = (3.0 - (strengthDiff * 1.0)).coerceIn(2.0, 4.5)
                    
                    // Away win scenarios
                    result["1-2"] = (3.0 + (strengthDiff * 1.0)).coerceIn(2.0, 4.5)
                    result["0-2"] = (3.5 + (strengthDiff * 2.0)).coerceIn(2.0, 6.0)
                }
            }
            else -> {
                // Para outros jogos, usar um formato genérico
                result["2-0"] = 4.0
                result["2-1"] = 3.5
                result["1-2"] = 3.5
                result["0-2"] = 4.0
            }
        }
        
        return result
    }

    override suspend fun placeBet(
        matchId: String, 
        amount: Int, 
        predictedWinner: String, 
        predictedScore: String?
    ): Result<Bet> {
        try {
            // Verificar se o usuário tem pontos suficientes
            val availablePoints = getUserAvailablePoints()
            if (availablePoints < amount) {
                return Result.failure(Exception("Pontos insuficientes"))
            }
            
            // Buscar a partida para calcular as odds
            val matchDoc = firestore.collection("matches").document(matchId).get().await()
            val match = matchDoc.toObject(Match::class.java) ?: 
                return Result.failure(Exception("Partida não encontrada"))
            
            // Verificar se a partida ainda não começou
            if (match.startTime.time < System.currentTimeMillis()) {
                return Result.failure(Exception("Apostas encerradas para esta partida"))
            }
            
            // Calcular odds
            val odds = calculateOdds(match)
            
            // Determinar a odd específica para esta aposta
            val betOdds = when {
                predictedScore != null -> odds.exactScores[predictedScore] ?: 10.0
                predictedWinner == match.homeTeam.name -> odds.homeWin
                predictedWinner == match.awayTeam.name -> odds.awayWin
                else -> odds.draw
            }
            
            // Calcular ganhos potenciais
            val potentialWinnings = (amount * betOdds).toInt()
            
            // Criar a aposta
            val bet = Bet(
                userId = userId,
                matchId = matchId,
                betAmount = amount,
                predictedWinner = predictedWinner,
                predictedScore = predictedScore ?: "",
                odds = betOdds,
                potentialWinnings = potentialWinnings,
                status = BetStatus.PENDING
            )
            
            // Salvar a aposta no Firestore
            val betRef = firestore.collection("bets").document()
            betRef.set(bet.copy(id = betRef.id)).await()
            
            // Deduzir os pontos do usuário
            firestore.collection("users").document(userId)
                .update("points", com.google.firebase.firestore.FieldValue.increment(-amount.toLong()))
                .await()
            
            return Result.success(bet.copy(id = betRef.id))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun placeBet(bet: Bet): Result<Bet> {
        try {
            // Verificar se o usuário tem pontos suficientes
            val availablePoints = getUserAvailablePoints()
            if (availablePoints < bet.betAmount) {
                return Result.failure(Exception("Pontos insuficientes"))
            }
            
            // Buscar a partida para calcular as odds
            val matchDoc = firestore.collection("matches").document(bet.matchId).get().await()
            val match = matchDoc.toObject(Match::class.java) ?: 
                return Result.failure(Exception("Partida não encontrada"))
            
            // Verificar se a partida ainda não começou
            if (match.startTime.time < System.currentTimeMillis()) {
                return Result.failure(Exception("Apostas encerradas para esta partida"))
            }
            
            // Calcular odds
            val odds = calculateOdds(match)
            
            // Determinar a odd específica para esta aposta
            val betOdds = when {
                bet.predictedScore.isNotEmpty() -> odds.exactScores[bet.predictedScore] ?: 10.0
                bet.predictedWinner == match.homeTeam.name -> odds.homeWin
                bet.predictedWinner == match.awayTeam.name -> odds.awayWin
                else -> odds.draw
            }
            
            // Calcular ganhos potenciais
            val potentialWinnings = (bet.betAmount * betOdds).toInt()
            
            // Criar a aposta
            val newBet = bet.copy(
                userId = userId,
                odds = betOdds,
                potentialWinnings = potentialWinnings,
                status = BetStatus.PENDING
            )
            
            // Salvar a aposta no Firestore
            val betRef = firestore.collection("bets").document()
            betRef.set(newBet.copy(id = betRef.id)).await()
            
            // Deduzir os pontos do usuário
            firestore.collection("users").document(userId)
                .update("points", com.google.firebase.firestore.FieldValue.increment(-bet.betAmount.toLong()))
                .await()
            
            return Result.success(newBet.copy(id = betRef.id))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun settleBet(betId: String, actualWinner: String, actualScore: String): Result<Bet> {
        try {
            // Buscar a aposta
            val betDoc = firestore.collection("bets").document(betId).get().await()
            val bet = betDoc.toObject(Bet::class.java) ?: 
                return Result.failure(Exception("Aposta não encontrada"))
            
            // Verificar se a aposta já foi liquidada
            if (bet.status != BetStatus.PENDING) {
                return Result.failure(Exception("Aposta já foi liquidada"))
            }
            
            // Determinar o resultado da aposta
            val status = when {
                // Se apostou no placar exato e acertou
                bet.predictedScore.isNotEmpty() && bet.predictedScore == actualScore -> BetStatus.WON
                // Se apostou apenas no vencedor e acertou
                bet.predictedScore.isEmpty() && bet.predictedWinner == actualWinner -> BetStatus.WON
                // Caso contrário, perdeu
                else -> BetStatus.LOST
            }
            
            // Atualizar o status da aposta
            val updatedBet = bet.copy(status = status)
            firestore.collection("bets").document(betId).set(updatedBet).await()
            
            // Se ganhou, creditar os pontos
            if (status == BetStatus.WON) {
                firestore.collection("users").document(userId)
                    .update("points", com.google.firebase.firestore.FieldValue.increment(bet.potentialWinnings.toLong()))
                    .await()
                
                // Atualizar estatísticas
                updateArenaStats(won = true, pointsWon = bet.potentialWinnings)
            } else {
                // Atualizar estatísticas de derrota
                updateArenaStats(won = false, pointsWon = 0)
            }
            
            return Result.success(updatedBet)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun updateArenaStats(won: Boolean, pointsWon: Int) {
        val statsRef = firestore.collection("arenaStats").document(userId)
        val statsDoc = statsRef.get().await()
        
        // Obter nome de usuário
        val userDoc = firestore.collection("users").document(userId).get().await()
        val username = userDoc.getString("username") ?: "Analista"
        
        if (statsDoc.exists()) {
            // Atualizar estatísticas existentes
            statsRef.update(
                "totalBets", com.google.firebase.firestore.FieldValue.increment(1),
                "wonBets", com.google.firebase.firestore.FieldValue.increment(if (won) 1 else 0),
                "totalPointsWon", com.google.firebase.firestore.FieldValue.increment(pointsWon.toLong()),
                "username", username
            ).await()
            
            // Recalcular precisão
            val stats = statsDoc.toObject(ArenaStats::class.java) ?: return
            val newTotalBets = stats.totalBets + 1
            val newWonBets = stats.wonBets + (if (won) 1 else 0)
            val newAccuracy = newWonBets.toDouble() / newTotalBets
            
            // Atualizar badges com base no desempenho
            val badges = updateBadges(stats.badges, newAccuracy, newTotalBets, newWonBets)
            
            statsRef.update(
                "accuracy", newAccuracy,
                "badges", badges
            ).await()
        } else {
            // Criar novas estatísticas
            val newStats = ArenaStats(
                userId = userId,
                username = username,
                totalBets = 1,
                wonBets = if (won) 1 else 0,
                accuracy = if (won) 1.0 else 0.0,
                totalPointsWon = pointsWon,
                badges = if (won) listOf("Primeira Vitória") else emptyList()
            )
            statsRef.set(newStats).await()
        }
    }
    
    private fun updateBadges(currentBadges: List<String>, accuracy: Double, totalBets: Int, wonBets: Int): List<String> {
        val badges = currentBadges.toMutableList()
        
        // Badges baseadas em precisão
        if (accuracy >= 0.7 && totalBets >= 10 && !badges.contains("Analista de Elite")) {
            badges.add("Analista de Elite")
        }
        
        if (accuracy >= 0.6 && totalBets >= 5 && !badges.contains("Bom Palpiteiro")) {
            badges.add("Bom Palpiteiro")
        }
        
        // Badges baseadas em volume
        if (totalBets >= 50 && !badges.contains("Veterano")) {
            badges.add("Veterano")
        }
        
        if (totalBets >= 20 && !badges.contains("Frequente")) {
            badges.add("Frequente")
        }
        
        // Badges baseadas em vitórias
        if (wonBets >= 25 && !badges.contains("Mestre das Apostas")) {
            badges.add("Mestre das Apostas")
        }
        
        if (wonBets >= 10 && !badges.contains("Vencedor Consistente")) {
            badges.add("Vencedor Consistente")
        }
        
        return badges
    }

    override suspend fun getUserAvailablePoints(): Int {
        val userDoc = firestore.collection("users").document(userId).get().await()
        return userDoc.getLong("points")?.toInt() ?: 0
    }
}
