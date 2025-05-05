package com.furia.furiafanapp.ui.screens.Home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.furia.furiafanapp.R
import com.furia.furiafanapp.ui.theme.FuriaBlack
import com.furia.furiafanapp.ui.theme.FuriaWhite
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import com.google.firebase.auth.FirebaseAuth
import com.furia.furiafanapp.ui.navigation.Screen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import com.furia.furiafanapp.ui.screens.Profile.ProfileViewModel
import com.furia.furiafanapp.data.model.UserProfile
import com.furia.furiafanapp.ui.screens.Matches.ClosedMatchesScreen
import com.furia.furiafanapp.ui.screens.Matches.LiveMatchesScreen
import com.furia.furiafanapp.ui.screens.Matches.NextMatchesScreen
import com.furia.furiafanapp.ui.theme.FuriaFanAppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    // Usar o tema com barra de status transparente
    FuriaFanAppTheme(useTransparentStatusBar = true) {
        val pagerState = rememberPagerState(initialPage = 0) { 3 }
        val coroutineScope = rememberCoroutineScope()

        // Sincronizar o estado do pager com o tab selecionado
        var selectedTabIndex by remember { mutableStateOf(0) }

        // Update selectedTabIndex when page changes
        LaunchedEffect(pagerState.currentPage) {
            selectedTabIndex = pagerState.currentPage
            // Gamificação: 7 pontos ao assistir jogos ao vivo (1x por dia)
            if (pagerState.currentPage == 1) {
                profileViewModel.awardWatchLiveGames()
            }
        }

        // Gamificação: 5 pontos por visualizar jogos (1x por dia)
        LaunchedEffect(Unit) {
            profileViewModel.awardViewGames()
        }

        // Drawer state for settings menu
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val drawerScope = rememberCoroutineScope()

        val userProfile by profileViewModel.userProfile.collectAsState(initial = UserProfile("", "", "", emptyMap(), 0L))
        val leaderboard by profileViewModel.leaderboard.collectAsState(initial = emptyList())
        
        // Verificar se o usuário é o top 1 do leaderboard
        val isTopOne = leaderboard.isNotEmpty() && leaderboard[0].id == userProfile.id
        
        // Determinar o tier do usuário com base nos pontos e posição no leaderboard
        val points = userProfile.points.toInt()
        val tierResId = when {
            isTopOne -> R.drawable.tier_4  // Alpha (top 1)
            points < 100 -> R.drawable.tier_1  // Lobo
            points < 200 -> R.drawable.tier_2  // Fera
            else -> R.drawable.tier_3  // Lenda
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = FuriaBlack.copy(alpha = 0.95f),
                    drawerContentColor = FuriaWhite
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Perfil do usuário usando o componente reutilizável
                    com.furia.furiafanapp.ui.components.UserProfileHeader(
                        userProfile = userProfile,
                        isTopOne = leaderboard.isNotEmpty() && leaderboard[0].id == userProfile.id
                    )
                    
                    Divider(color = FuriaWhite.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Opções do menu
                    NavigationDrawerItem(
                        label = { Text("Perfil") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                navController.navigate(Screen.Profile.route)
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = FuriaBlack,
                            unselectedContainerColor = FuriaBlack,
                            selectedIconColor = FuriaWhite,
                            unselectedIconColor = FuriaWhite,
                            selectedTextColor = FuriaWhite,
                            unselectedTextColor = FuriaWhite
                        )
                    )
                    
                    NavigationDrawerItem(
                        label = { Text("Chat Bot") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                navController.navigate(Screen.ChatBot.route)
                            }
                        },
                        icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = FuriaBlack,
                            unselectedContainerColor = FuriaBlack,
                            selectedIconColor = FuriaWhite,
                            unselectedIconColor = FuriaWhite,
                            selectedTextColor = FuriaWhite,
                            unselectedTextColor = FuriaWhite
                        )
                    )
                    
                    NavigationDrawerItem(
                        label = { Text("Arena") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                navController.navigate(Screen.Arena.route)
                            }
                        },
                        icon = { Icon(painterResource(id = R.drawable.ic_arena), contentDescription = null) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = FuriaBlack,
                            unselectedContainerColor = FuriaBlack,
                            selectedIconColor = FuriaWhite,
                            unselectedIconColor = FuriaWhite,
                            selectedTextColor = FuriaWhite,
                            unselectedTextColor = FuriaWhite
                        )
                    )
                    
                    NavigationDrawerItem(
                        label = { Text("Mini Games") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                navController.navigate(Screen.MiniGames.route)
                            }
                        },
                        icon = { Icon(painterResource(id = R.drawable.ic_games), contentDescription = null) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = FuriaBlack,
                            unselectedContainerColor = FuriaBlack,
                            selectedIconColor = FuriaWhite,
                            unselectedIconColor = FuriaWhite,
                            selectedTextColor = FuriaWhite,
                            unselectedTextColor = FuriaWhite
                        )
                    )
                    
                    NavigationDrawerItem(
                        label = { Text("FURIA Shop") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                navController.navigate(Screen.Shop.route)
                            }
                        },
                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = FuriaBlack,
                            unselectedContainerColor = FuriaBlack,
                            selectedIconColor = FuriaWhite,
                            unselectedIconColor = FuriaWhite,
                            selectedTextColor = FuriaWhite,
                            unselectedTextColor = FuriaWhite
                        )
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Divider(color = FuriaWhite.copy(alpha = 0.2f))
                    
                    // Botão de logout
                    NavigationDrawerItem(
                        label = { Text("Sair") },
                        selected = false,
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            coroutineScope.launch {
                                drawerState.close()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.id) {
                                        inclusive = true
                                    }
                                }
                            }
                        },
                        icon = { Icon(painterResource(id = R.drawable.ic_logout), contentDescription = null) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = FuriaBlack,
                            unselectedContainerColor = FuriaBlack,
                            selectedIconColor = FuriaWhite,
                            unselectedIconColor = FuriaWhite,
                            selectedTextColor = FuriaWhite,
                            unselectedTextColor = FuriaWhite
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            },
            content = {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            navigationIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
                                    // Mostrar o ícone de tier do usuário
                                    Image(
                                        painter = painterResource(id = tierResId),
                                        contentDescription = "Patente do usuário",
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "${userProfile.points} FP", color = FuriaWhite, style = MaterialTheme.typography.bodyMedium)
                                }
                            },
                            title = {
                                Image(
                                    painter = painterResource(id = R.drawable.texto_logo),
                                    contentDescription = "FURIA Logo",
                                    modifier = Modifier
                                        .height(50.dp)
                                        .fillMaxWidth(0.7f),
                                    alignment = Alignment.Center
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = FuriaBlack,
                                titleContentColor = FuriaWhite
                            ),
                            actions = {
                                IconButton(onClick = { drawerScope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = FuriaWhite)
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = FuriaBlack,
                            contentColor = FuriaWhite
                        ) {
                            NavigationBarItem(
                                selected = selectedTabIndex == 0,
                                onClick = {
                                    selectedTabIndex = 0
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(0)
                                    }
                                },
                                icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                                label = { Text(stringResource(R.string.next_matches)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = FuriaWhite,
                                    selectedTextColor = FuriaWhite,
                                    indicatorColor = FuriaBlack,
                                    unselectedIconColor = FuriaWhite.copy(alpha = 0.6f),
                                    unselectedTextColor = FuriaWhite.copy(alpha = 0.6f)
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTabIndex == 1,
                                onClick = {
                                    selectedTabIndex = 1
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                },
                                icon = { Icon(Icons.Default.LiveTv, contentDescription = null) },
                                label = { Text(stringResource(R.string.live_matches)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = FuriaWhite,
                                    selectedTextColor = FuriaWhite,
                                    indicatorColor = FuriaBlack,
                                    unselectedIconColor = FuriaWhite.copy(alpha = 0.6f),
                                    unselectedTextColor = FuriaWhite.copy(alpha = 0.6f)
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTabIndex == 2,
                                onClick = {
                                    selectedTabIndex = 2
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(2)
                                    }
                                },
                                icon = { Icon(Icons.Default.History, contentDescription = null) },
                                label = { Text(stringResource(R.string.closed_matches)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = FuriaWhite,
                                    selectedTextColor = FuriaWhite,
                                    indicatorColor = FuriaBlack,
                                    unselectedIconColor = FuriaWhite.copy(alpha = 0.6f),
                                    unselectedTextColor = FuriaWhite.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.wallpaper),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (page) {
                                0 -> NextMatchesScreen()
                                1 -> LiveMatchesScreen()
                                2 -> ClosedMatchesScreen()
                            }
                        }
                    }
                }
            }
        )
    }
}