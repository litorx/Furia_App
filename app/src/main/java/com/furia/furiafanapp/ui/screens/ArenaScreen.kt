package com.furia.furiafanapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.furia.furiafanapp.data.model.ArenaStats
import com.furia.furiafanapp.data.model.Bet
import com.furia.furiafanapp.data.model.BetStatus
import com.furia.furiafanapp.data.model.Match
import com.furia.furiafanapp.data.model.UserProfile
import com.furia.furiafanapp.ui.theme.FuriaBlack
import com.furia.furiafanapp.ui.theme.FuriaWhite
import com.furia.furiafanapp.ui.theme.FuriaYellow
import com.furia.furiafanapp.ui.components.UserProfileHeader
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArenaScreen(
    navController: NavHostController,
    viewModel: ArenaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userBets by viewModel.userBets.collectAsState()
    val arenaStats by viewModel.arenaStats.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val availablePoints by viewModel.availablePoints.collectAsState()
    val upcomingMatches by viewModel.upcomingMatches.collectAsState()
    val selectedMatch by viewModel.selectedMatch.collectAsState()
    val matchOdds by viewModel.matchOdds.collectAsState()
    val betAmount by viewModel.betAmount.collectAsState()
    val selectedBetType by viewModel.selectedBetType.collectAsState()
    val selectedPlayer by viewModel.selectedPlayer.collectAsState()
    val statPrediction by viewModel.statPrediction.collectAsState()
    val potentialWinnings by viewModel.potentialWinnings.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    
    var selectedTab by remember { mutableStateOf(ArenaTab.UPCOMING) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ArenaUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            is ArenaUiState.BetPlaced -> {
                snackbarHostState.showSnackbar("Aposta realizada com sucesso!")
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FURIA Arena", color = FuriaWhite) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = FuriaWhite)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = FuriaBlack
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black
            ) {
                NavigationBarItem(
                    selected = selectedTab == ArenaTab.UPCOMING,
                    onClick = { selectedTab = ArenaTab.UPCOMING },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Próximos Jogos") },
                    label = { Text("Apostar") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FuriaYellow,
                        selectedTextColor = FuriaYellow,
                        indicatorColor = Color.Black,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.White
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == ArenaTab.HISTORY,
                    onClick = { selectedTab = ArenaTab.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = "Histórico") },
                    label = { Text("Histórico") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FuriaYellow,
                        selectedTextColor = FuriaYellow,
                        indicatorColor = Color.Black,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.White
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == ArenaTab.LEADERBOARD,
                    onClick = { selectedTab = ArenaTab.LEADERBOARD },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Ranking") },
                    label = { Text("Ranking") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FuriaYellow,
                        selectedTextColor = FuriaYellow,
                        indicatorColor = Color.Black,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.White
                    )
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = FuriaYellow,
                    contentColor = Color.Black
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Perfil do usuário
            userProfile?.let { profile ->
                UserProfileHeader(
                    userProfile = profile,
                    isTopOne = leaderboard.isNotEmpty() && leaderboard.firstOrNull()?.userId == profile.id
                )
            }
            
            // Tabs
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (uiState) {
                    is ArenaUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = FuriaYellow
                        )
                    }
                    is ArenaUiState.Error -> {
                        // Erro já exibido no Snackbar
                    }
                    else -> {
                        when (selectedTab) {
                            ArenaTab.UPCOMING -> UpcomingMatchesTab(
                                availablePoints = availablePoints,
                                upcomingMatches = upcomingMatches,
                                selectedMatch = selectedMatch,
                                matchOdds = matchOdds,
                                betAmount = betAmount,
                                selectedBetType = selectedBetType,
                                selectedPlayer = selectedPlayer,
                                statPrediction = statPrediction,
                                potentialWinnings = potentialWinnings,
                                onMatchSelected = { viewModel.selectMatch(it) },
                                onBetAmountChanged = { viewModel.updateBetAmount(it) },
                                onBetTypeSelected = { viewModel.selectBetType(it) },
                                onPlayerSelected = { viewModel.selectPlayer(it) },
                                onStatPredictionChanged = { viewModel.updateStatPrediction(it) },
                                onPlaceBet = { viewModel.placeBet() }
                            )
                            ArenaTab.HISTORY -> BetHistoryTab(
                                userBets = userBets,
                                arenaStats = arenaStats
                            )
                            ArenaTab.LEADERBOARD -> LeaderboardTab(
                                leaderboard = leaderboard,
                                userStats = arenaStats
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class ArenaTab {
    UPCOMING, HISTORY, LEADERBOARD
}
