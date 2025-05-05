package com.furia.furiafanapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log
import android.widget.Toast
import com.furia.furiafanapp.R
import com.furia.furiafanapp.data.model.Match
import com.furia.furiafanapp.data.model.MatchStatus
import com.furia.furiafanapp.data.model.StreamPlatform
import com.furia.furiafanapp.ui.viewmodel.MatchViewModel
import com.furia.furiafanapp.ui.viewmodel.MatchesUiState
import com.furia.furiafanapp.utils.DateUtils
import com.furia.furiafanapp.utils.UrlUtils
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun NextMatchesScreen(
    viewModel: MatchViewModel = hiltViewModel()
) {
    val state by viewModel.upcomingMatchesState.collectAsState()
    MatchListContent(
        state = state,
        onRefresh = viewModel::refreshMatches,
        emptyMessage = stringResource(R.string.no_matches_found)
    )
}

@Composable
fun LiveMatchesScreen(
    viewModel: MatchViewModel = hiltViewModel()
) {
    val state by viewModel.liveMatchesState.collectAsState()
    MatchListContent(
        state = state,
        onRefresh = viewModel::refreshMatches,
        emptyMessage = stringResource(R.string.no_live_matches)
    )
}

@Composable
fun ClosedMatchesScreen(
    viewModel: MatchViewModel = hiltViewModel()
) {
    val state by viewModel.closedMatchesState.collectAsState()
    MatchListContent(
        state = state,
        onRefresh = viewModel::refreshMatches,
        emptyMessage = stringResource(R.string.no_matches_found)
    )
}

@Composable
private fun MatchListContent(
    state: MatchesUiState,
    onRefresh: () -> Unit,
    emptyMessage: String
) {
    val context = LocalContext.current
    Log.d("MatchList", "MatchListContent initialized")
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = state is MatchesUiState.Loading)
    var selectedMatch by remember { mutableStateOf<Match?>(null) }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = onRefresh,
        indicator = { s, trigger ->
            SwipeRefreshIndicator(
                state = s,
                refreshTriggerDistance = trigger
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Wallpaper opaco de fundo
            Image(
                painter = painterResource(id = R.drawable.wallpaper),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    is MatchesUiState.Loading -> LoadingMatchesPlaceholder()
                    is MatchesUiState.Success -> {
                        if (state.matches.isEmpty()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = emptyMessage,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = onRefresh) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.try_again))
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(state.matches) { match ->
                                    MatchCard(match = match, onChatOpen = { selectedMatch = match })
                                }
                            }
                        }
                    }
                    is MatchesUiState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onRefresh) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.try_again))
                            }
                        }
                    }
                }
            }
            selectedMatch?.let { match ->
                ChatScreen(
                    match = match,
                    onBack = { selectedMatch = null }
                )
            }
        }
    }
}

// Skeleton placeholder for loading matches
@Composable
private fun LoadingMatchesPlaceholder(count: Int = 5) {
    Column(modifier = Modifier.padding(16.dp)) {
        repeat(count) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(vertical = 4.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchCard(
    match: Match,
    onChatOpen: (Match) -> Unit
) {
    val context = LocalContext.current
    
    // Animation for the live indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulseAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnimation"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaAnimation"
    )
    
    // ViewModel para agendar lembretes
    val viewModel: MatchViewModel = hiltViewModel()
    val scheduledReminders by viewModel.scheduledReminders.collectAsState()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Game type badge and match status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFFFCD00), 
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = match.tournament.game.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                
                // Match status indicator
                when (match.status) {
                    MatchStatus.LIVE -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Animated pulsing dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp * scale)
                                    .alpha(alpha)
                                    .background(Color.Red, CircleShape)
                            )
                            Text(
                                text = "AO VIVO",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    MatchStatus.SCHEDULED -> {
                        Text(
                            text = DateUtils.formatMatchDate(match.startTime),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    MatchStatus.FINISHED -> {
                        Text(
                            text = "ENCERRADO",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tournament name
            Text(
                text = match.tournament.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Teams and score
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Home team
                Text(
                    text = match.homeTeam.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
                
                // Score or VS
                if (match.status != MatchStatus.SCHEDULED) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "${match.score?.home ?: 0}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Text(
                            text = " - ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Text(
                            text = "${match.score?.away ?: 0}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                // Away team
                Text(
                    text = match.awayTeam.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (match.streams.isNotEmpty()) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (match.streams.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        match.streams.forEach { stream ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clickable { UrlUtils.openUrl(context, stream.url) },
                                contentAlignment = Alignment.Center
                            ) {
                                when (stream.platform) {
                                    StreamPlatform.TWITCH -> Image(
                                        painter = painterResource(id = R.drawable.twitch_icon),
                                        contentDescription = "Twitch",
                                        modifier = Modifier.size(48.dp)
                                    )
                                    StreamPlatform.YOUTUBE -> Image(
                                        painter = painterResource(id = R.drawable.youtube_icon),
                                        contentDescription = "YouTube",
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                // Botões de Chat e Lembrete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    IconButton(onClick = { onChatOpen(match) }) {
                        Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Chat", tint = Color.White)
                    }
                    if (match.status == MatchStatus.SCHEDULED) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = {
                            if (scheduledReminders.contains(match.id)) {
                                viewModel.cancelReminder(match.id)
                                Toast.makeText(context, "Lembrete cancelado", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.scheduleReminder(match.id, match.startTime.time)
                                Toast.makeText(context, "Lembrete agendado para 1h antes", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Lembrete",
                                tint = if (scheduledReminders.contains(match.id)) Color.Yellow else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchItem(match: Match) {
    MatchCard(match = match, onChatOpen = {})
}