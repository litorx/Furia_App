package com.furia.furiafanapp.ui.screens.Arena

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furia.furiafanapp.data.model.*
import com.furia.furiafanapp.ui.theme.FuriaWhite
import com.furia.furiafanapp.ui.theme.FuriaYellow
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UpcomingMatchesTab(
    availablePoints: Int,
    upcomingMatches: List<Match>,
    selectedMatch: Match?,
    matchOdds: BetOdds?,
    betAmount: String,
    selectedBetType: BetType?,
    selectedPlayer: String?,
    statPrediction: Int?,
    potentialWinnings: Long,
    onMatchSelected: (Match) -> Unit,
    onBetAmountChanged: (String) -> Unit,
    onBetTypeSelected: (BetType) -> Unit,
    onPlayerSelected: (String) -> Unit,
    onStatPredictionChanged: (Int) -> Unit,
    onPlaceBet: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                ),
                border = BorderStroke(1.dp, FuriaYellow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pontos Disponíveis",
                        color = FuriaWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$availablePoints",
                        color = FuriaYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Próximas Partidas",
                color = FuriaWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(upcomingMatches) { match ->
                    MatchCard(
                        match = match,
                        isSelected = selectedMatch?.id == match.id,
                        onClick = { onMatchSelected(match) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (selectedMatch != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Faça sua Aposta",
                            color = FuriaYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        BetForm(
                            match = selectedMatch,
                            matchOdds = matchOdds,
                            betAmount = betAmount,
                            selectedBetType = selectedBetType,
                            selectedPlayer = selectedPlayer,
                            statPrediction = statPrediction,
                            potentialWinnings = potentialWinnings,
                            availablePoints = availablePoints,
                            onBetAmountChanged = onBetAmountChanged,
                            onBetTypeSelected = onBetTypeSelected,
                            onPlayerSelected = onPlayerSelected,
                            onStatPredictionChanged = onStatPredictionChanged,
                            onPlaceBet = onPlaceBet
                        )
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selecione uma partida para fazer sua aposta",
                        color = FuriaWhite,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun MatchCard(
    match: Match,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM - HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(match.startTime)
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2E2E2E) else Color(0xFF1E1E1E)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) FuriaYellow else FuriaWhite.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = match.tournament.name,
                color = FuriaWhite,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = match.homeTeam.name,
                        color = if (match.homeTeam.name.contains("FURIA", ignoreCase = true)) 
                            FuriaYellow else FuriaWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "VS",
                    color = FuriaWhite,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = match.awayTeam.name,
                        color = if (match.awayTeam.name.contains("FURIA", ignoreCase = true)) 
                            FuriaYellow else FuriaWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = formattedDate,
                color = FuriaWhite.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BetForm(
    match: Match,
    matchOdds: BetOdds?,
    betAmount: String,
    selectedBetType: BetType?,
    selectedPlayer: String?,
    statPrediction: Int?,
    potentialWinnings: Long,
    availablePoints: Int,
    onBetAmountChanged: (String) -> Unit,
    onBetTypeSelected: (BetType) -> Unit,
    onPlayerSelected: (String) -> Unit,
    onStatPredictionChanged: (Int) -> Unit,
    onPlaceBet: () -> Unit
) {
    val furiaTeam = if (match.homeTeam.name.contains("FURIA", ignoreCase = true)) 
        match.homeTeam else match.awayTeam
    
    val furiaPlayers = listOf("art", "yuurih", "kscerato", "chelo", "drop", "saffee")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Faça sua Aposta",
            color = FuriaYellow,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tipo de Aposta",
            color = FuriaWhite,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BetType.values()) { betType ->
                val betTypeName = when(betType) {
                    BetType.MVP_PREDICTION -> "MVP da Partida"
                    BetType.KILL_COUNT -> "Total de Kills"
                    BetType.HEADSHOT_PERCENTAGE -> "% Headshots"
                    BetType.CLUTCH_ROUNDS -> "Clutches"
                    BetType.ACE_COUNT -> "Número de Aces"
                    BetType.FIRST_BLOOD -> "Primeiro Abate"
                    BetType.BOMB_PLANTS -> "Bombas Plantadas"
                    BetType.KNIFE_KILLS -> "Kills com Faca"
                    BetType.PISTOL_ROUND_WINS -> "Rounds de Pistol"
                    BetType.TEAM_TOTAL_KILLS -> "Kills da Equipe"
                }
                
                FilterChip(
                    selected = selectedBetType == betType,
                    onClick = { onBetTypeSelected(betType) },
                    label = { Text(betTypeName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FuriaYellow,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (selectedBetType != null && selectedBetType != BetType.TEAM_TOTAL_KILLS) {
            Text(
                text = "Jogador",
                color = FuriaWhite,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(furiaPlayers) { player ->
                    FilterChip(
                        selected = selectedPlayer == player,
                        onClick = { onPlayerSelected(player) },
                        label = { Text(player) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FuriaYellow,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (selectedBetType != null && selectedBetType != BetType.MVP_PREDICTION && 
            selectedBetType != BetType.FIRST_BLOOD) {
            
            val statName = when(selectedBetType) {
                BetType.KILL_COUNT -> "Número de Kills"
                BetType.HEADSHOT_PERCENTAGE -> "Porcentagem de Headshots"
                BetType.CLUTCH_ROUNDS -> "Número de Clutches"
                BetType.ACE_COUNT -> "Número de Aces"
                BetType.BOMB_PLANTS -> "Bombas Plantadas"
                BetType.KNIFE_KILLS -> "Kills com Faca"
                BetType.PISTOL_ROUND_WINS -> "Rounds de Pistol Vencidos"
                BetType.TEAM_TOTAL_KILLS -> "Total de Kills da Equipe"
                else -> ""
            }
            
            Text(
                text = statName,
                color = FuriaWhite,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val predictionValue = statPrediction ?: 0
            val maxValue = when(selectedBetType) {
                BetType.KILL_COUNT -> 40
                BetType.HEADSHOT_PERCENTAGE -> 100
                BetType.CLUTCH_ROUNDS -> 10
                BetType.ACE_COUNT -> 5
                BetType.BOMB_PLANTS -> 15
                BetType.KNIFE_KILLS -> 3
                BetType.PISTOL_ROUND_WINS -> 6
                BetType.TEAM_TOTAL_KILLS -> 100
                else -> 100
            }
            
            Column {
                Text(
                    text = "$predictionValue",
                    color = FuriaYellow,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Slider(
                    value = predictionValue.toFloat(),
                    onValueChange = { onStatPredictionChanged(it.toInt()) },
                    valueRange = 0f..maxValue.toFloat(),
                    steps = maxValue,
                    colors = SliderDefaults.colors(
                        thumbColor = FuriaYellow,
                        activeTrackColor = FuriaYellow
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Text(
            text = "Valor da Aposta",
            color = FuriaWhite,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = betAmount,
            onValueChange = { 
                val newValue = it.filter { char -> char.isDigit() }
                val intValue = newValue.toIntOrNull() ?: 0
                if (intValue <= availablePoints) {
                    onBetAmountChanged(newValue)
                }
            },
            label = { Text("Pontos") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FuriaYellow,
                unfocusedBorderColor = FuriaWhite.copy(alpha = 0.5f),
                focusedLabelColor = FuriaYellow,
                unfocusedLabelColor = FuriaWhite.copy(alpha = 0.5f),
                cursorColor = FuriaYellow,
                focusedTextColor = FuriaWhite,
                unfocusedTextColor = FuriaWhite
            ),
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                Text(
                    text = "Máximo: $availablePoints pontos",
                    color = FuriaWhite.copy(alpha = 0.7f)
                )
            },
            isError = betAmount.isNotEmpty() && (betAmount.toIntOrNull() ?: 0) > availablePoints
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ganhos Potenciais",
                color = FuriaWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$potentialWinnings pontos",
                color = FuriaYellow,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val betAmountInt = betAmount.toIntOrNull() ?: 0
        val hasSufficientPoints = betAmountInt > 0 && betAmountInt <= availablePoints
        val isFormValid = selectedBetType != null && 
                 (selectedBetType == BetType.TEAM_TOTAL_KILLS || selectedPlayer != null) &&
                 (selectedBetType == BetType.MVP_PREDICTION || 
                  selectedBetType == BetType.FIRST_BLOOD || 
                  statPrediction != null) &&
                 betAmount.isNotEmpty() && hasSufficientPoints
        
        Button(
            onClick = onPlaceBet,
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = FuriaYellow,
                contentColor = Color.Black,
                disabledContainerColor = FuriaYellow.copy(alpha = 0.3f),
                disabledContentColor = Color.Black.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Apostar",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        if (betAmount.isNotEmpty() && betAmountInt > availablePoints) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Você não tem pontos suficientes para esta aposta",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
