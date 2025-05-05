package com.furia.furiafanapp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.MutablePreferences
import androidx.hilt.navigation.compose.hiltViewModel
import com.furia.furiafanapp.R
import com.furia.furiafanapp.ui.theme.FuriaBlack
import com.furia.furiafanapp.ui.theme.FuriaYellow
import com.furia.furiafanapp.util.ONBOARDING_COMPLETED_KEY
import com.furia.furiafanapp.util.onboardingPrefs
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class Game(
    val id: String,
    val name: String,
    val icon: Int
)

val games = listOf(
    Game("all", "Todos", R.drawable.icon_furia),
    Game("valorant", "Valorant", R.drawable.icon_valorant),
    Game("cs", "CS2", R.drawable.icon_cs2),
    Game("lol", "League of Legends", R.drawable.icon_lol),
    Game("kingsleague", "Kings League", R.drawable.icon_kingsleague),
    Game("r6", "Rainbow Six", R.drawable.icon_r6),
    Game("rocketleague", "Rocket League", R.drawable.icon_rocketleague),
    Game("pubg", "PUBG", R.drawable.icon_pubg)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onOnboardingComplete: () -> Unit
) {
    var selectedGames by remember { mutableStateOf(setOf<String>()) }
    val pagerState = rememberPagerState(initialPage = 0) { games.size }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_furiav_semtexto),
                contentDescription = "FURIA Logo",
                modifier = Modifier
                    .size(140.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A2A)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(games) { game ->
                        GameSelectionItem(
                            game = game,
                            isSelected = selectedGames.contains(game.id),
                            onGameSelected = {
                                selectedGames = if (game.id == "all") {
                                    if (selectedGames.contains("all")) emptySet() else setOf("all")
                                } else {
                                    if (selectedGames.contains(game.id)) {
                                        selectedGames - game.id
                                    } else {
                                        (selectedGames - "all") + game.id
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        context.onboardingPrefs.edit { prefs: MutablePreferences ->
                            prefs[ONBOARDING_COMPLETED_KEY] = true
                        }
                    }
                    authViewModel.saveFavoriteGames(selectedGames)
                    onOnboardingComplete()
                },
                enabled = selectedGames.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FuriaYellow,
                    contentColor = FuriaBlack,
                    disabledContainerColor = FuriaYellow.copy(alpha = 0.5f),
                    disabledContentColor = FuriaBlack.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = stringResource(R.string.continue_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSelectionItem(
    game: Game,
    isSelected: Boolean,
    onGameSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) FuriaYellow.copy(alpha = 0.2f) else Color(0xFF333333)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) {
            ButtonDefaults.outlinedButtonBorder.copy(
                width = 2.dp,
                brush = SolidColor(FuriaYellow)
            )
        } else null,
        onClick = onGameSelected
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = game.icon),
                contentDescription = game.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = game.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = FuriaYellow,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}