package com.furia.furiafanapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Bet(
    @DocumentId val id: String = "",
    val userId: String = "",
    val matchId: String = "",
    val betAmount: Int = 0,
    val betType: BetType = BetType.MVP_PREDICTION,
    val playerName: String = "",
    val statPrediction: Int = 0,
    val predictedWinner: String = "",  // Mantido para compatibilidade
    val predictedScore: String = "",   // Mantido para compatibilidade
    val odds: Double = 1.0,
    val potentialWinnings: Int = 0,
    val status: BetStatus = BetStatus.PENDING,
    val createdAt: Timestamp = Timestamp.now()
)

enum class BetStatus {
    PENDING,
    WON,
    LOST,
    REFUNDED
}

enum class BetType {
    MVP_PREDICTION,
    KILL_COUNT,
    HEADSHOT_PERCENTAGE,
    CLUTCH_ROUNDS,
    ACE_COUNT,
    FIRST_BLOOD,
    BOMB_PLANTS,
    KNIFE_KILLS,
    PISTOL_ROUND_WINS,
    TEAM_TOTAL_KILLS
}

data class BetOdds(
    // Odds para o novo sistema
    val mvpOdds: Map<String, Double> = emptyMap(),
    val killCountRanges: Map<String, Double> = emptyMap(),
    val headshotRanges: Map<String, Double> = emptyMap(),
    val clutchRanges: Map<String, Double> = emptyMap(),
    val aceCountOdds: Map<String, Double> = emptyMap(),
    val firstBloodOdds: Map<String, Double> = emptyMap(),
    val bombPlantRanges: Map<String, Double> = emptyMap(),
    val knifeKillOdds: Map<String, Double> = emptyMap(),
    val pistolRoundOdds: Map<String, Double> = emptyMap(),
    val teamKillRanges: Map<String, Double> = emptyMap(),
    
    // Odds para o sistema antigo (mantido para compatibilidade)
    val homeWin: Double = 1.0,
    val awayWin: Double = 1.0,
    val draw: Double = 1.0,
    val exactScores: Map<String, Double> = emptyMap()
)

data class ArenaStats(
    val userId: String = "",
    val username: String = "Analista", 
    val totalBets: Int = 0,
    val wonBets: Int = 0,
    val accuracy: Double = 0.0,
    val totalPointsWon: Int = 0,
    val rank: Int = 0,
    val badges: List<String> = emptyList()
)

data class PlayerStats(
    val playerId: String = "",
    val playerName: String = "",
    val teamId: String = "",
    val kills: Int = 0,
    val deaths: Int = 0,
    val assists: Int = 0,
    val headshotPercentage: Double = 0.0,
    val clutchesWon: Int = 0,
    val aces: Int = 0,
    val firstBloods: Int = 0,
    val bombPlants: Int = 0,
    val knifeKills: Int = 0,
    val mvpCount: Int = 0
)
