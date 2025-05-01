package com.furia.furiafanapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.furia.furiafanapp.data.model.ArenaStats
import com.furia.furiafanapp.ui.theme.FuriaYellow

@Composable
fun LeaderboardTab(
    leaderboard: List<ArenaStats>,
    userStats: ArenaStats
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Cabeçalho
            Text(
                text = "Melhores Analistas",
                style = MaterialTheme.typography.titleLarge,
                color = FuriaYellow,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Baseado na precisão das apostas e pontos ganhos",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        
        // Posição do usuário
        item {
            val userPosition = leaderboard.indexOfFirst { it.userId == userStats.userId } + 1
            
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
                        text = "Sua Posição",
                        style = MaterialTheme.typography.titleMedium,
                        color = FuriaYellow
                    )
                    
                    if (userPosition > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#$userPosition",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userPosition <= 3) FuriaYellow else Color.White
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column {
                                    Text(
                                        text = userStats.username,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${(userStats.accuracy * 100).toInt()}% de acerto",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                            
                            Text(
                                text = "${userStats.totalPointsWon} FP",
                                style = MaterialTheme.typography.titleMedium,
                                color = FuriaYellow
                            )
                        }
                    } else {
                        Text(
                            text = "Faça apostas para aparecer no ranking",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        // Leaderboard
        if (leaderboard.isEmpty()) {
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
                            text = "Ainda não há dados suficientes para o ranking",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Top 3 em destaque
            if (leaderboard.size >= 3) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 2º lugar
                        if (leaderboard.size >= 2) {
                            TopAnalystItem(
                                stats = leaderboard[1],
                                position = 2,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // 1º lugar
                        TopAnalystItem(
                            stats = leaderboard[0],
                            position = 1,
                            modifier = Modifier.weight(1.2f)
                        )
                        
                        // 3º lugar
                        if (leaderboard.size >= 3) {
                            TopAnalystItem(
                                stats = leaderboard[2],
                                position = 3,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Restante do ranking
            itemsIndexed(
                items = leaderboard.drop(3),
                key = { _, item -> item.userId }
            ) { index, stats ->
                LeaderboardItem(
                    stats = stats,
                    position = index + 4
                )
            }
        }
    }
}

@Composable
fun TopAnalystItem(
    stats: ArenaStats,
    position: Int,
    modifier: Modifier = Modifier
) {
    val medalColor = when (position) {
        1 -> FuriaYellow
        2 -> Color(0xFFAAAAAA) // Prata
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color.Gray
    }
    
    Card(
        modifier = modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f),
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, medalColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (position == 1) 48.dp else 40.dp)
                    .clip(CircleShape)
                    .background(medalColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Troféu",
                    tint = medalColor,
                    modifier = Modifier.size(if (position == 1) 32.dp else 24.dp)
                )
            }
            
            Text(
                text = "#$position",
                style = MaterialTheme.typography.titleMedium,
                color = medalColor
            )
            
            Text(
                text = stats.username,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${(stats.accuracy * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "${stats.totalPointsWon} FP",
                style = MaterialTheme.typography.bodySmall
            )
            
            if (stats.badges.isNotEmpty()) {
                Surface(
                    color = medalColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = stats.badges.first(),
                        style = MaterialTheme.typography.bodySmall,
                        color = medalColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(
    stats: ArenaStats,
    position: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f),
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.Gray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#$position",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(48.dp)
                )
                
                Column {
                    Text(
                        text = stats.username,
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(stats.accuracy * 100).toInt()}% de acerto",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        
                        if (stats.badges.isNotEmpty()) {
                            Surface(
                                color = FuriaYellow.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = stats.badges.first(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FuriaYellow,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Text(
                text = "${stats.totalPointsWon} FP",
                style = MaterialTheme.typography.titleSmall,
                color = FuriaYellow
            )
        }
    }
}
