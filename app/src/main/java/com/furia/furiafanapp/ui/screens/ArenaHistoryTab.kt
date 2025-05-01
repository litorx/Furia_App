package com.furia.furiafanapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.furia.furiafanapp.data.model.ArenaStats
import com.furia.furiafanapp.data.model.Bet
import com.furia.furiafanapp.data.model.BetStatus
import com.furia.furiafanapp.ui.theme.FuriaYellow
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BetHistoryTab(
    userBets: List<Bet>,
    arenaStats: ArenaStats
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Estatísticas do usuário
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, FuriaYellow),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Suas Estatísticas",
                        style = MaterialTheme.typography.titleMedium,
                        color = FuriaYellow
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total de apostas")
                            Text("Apostas ganhas")
                            Text("Taxa de acerto")
                            Text("Pontos ganhos")
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${arenaStats.totalBets}")
                            Text("${arenaStats.wonBets}")
                            Text("${(arenaStats.accuracy * 100).toInt()}%")
                            Text("${arenaStats.totalPointsWon} FP", color = FuriaYellow)
                        }
                    }
                    
                    if (arenaStats.badges.isNotEmpty()) {
                        Divider(color = Color.Gray, thickness = 1.dp)
                        Text("Conquistas", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            arenaStats.badges.forEach { badge ->
                                Surface(
                                    color = FuriaYellow.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        text = badge,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FuriaYellow,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Text(
                text = "Histórico de Apostas",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
        
        if (userBets.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.Gray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Você ainda não fez nenhuma aposta",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(userBets.sortedByDescending { it.createdAt }) { bet ->
                BetHistoryCard(bet = bet)
            }
        }
    }
}

@Composable
fun BetHistoryCard(bet: Bet) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(bet.createdAt)
    
    val statusColor = when (bet.status) {
        BetStatus.WON -> Color.Green
        BetStatus.LOST -> Color.Red
        BetStatus.REFUNDED -> Color.Gray
        BetStatus.PENDING -> FuriaYellow
    }
    
    val statusText = when (bet.status) {
        BetStatus.WON -> "Ganhou"
        BetStatus.LOST -> "Perdeu"
        BetStatus.REFUNDED -> "Reembolsado"
        BetStatus.PENDING -> "Pendente"
    }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f),
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bet.predictedWinner,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            if (bet.predictedScore.isNotEmpty()) {
                Text(
                    text = "Placar: ${bet.predictedScore}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Divider(color = Color.Gray, thickness = 0.5.dp)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Aposta")
                    Text("Odds")
                    if (bet.status == BetStatus.WON) {
                        Text("Ganho", color = Color.Green)
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("${bet.betAmount} FP")
                    Text("x${String.format("%.2f", bet.odds)}")
                    if (bet.status == BetStatus.WON) {
                        Text("${bet.potentialWinnings} FP", color = Color.Green)
                    }
                }
            }
            
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
