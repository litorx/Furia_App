package com.furia.furiafanapp.ui.screens.Profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.furia.furiafanapp.R
import com.furia.furiafanapp.data.model.UserProfile
import com.furia.furiafanapp.ui.theme.FuriaBlack
import com.furia.furiafanapp.ui.theme.FuriaYellow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState(initial = UserProfile("", "", "", emptyMap(), 0L, emptyList()))
    val leaderboard by viewModel.leaderboard.collectAsState(initial = emptyList<UserProfile>())
    // points agora vêm do Firestore para manter consistência
    val points = userProfile.points.toInt()
    val badge by viewModel.badgeFlow.collectAsState(initial = "Fan")

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val rank = leaderboard.indexOfFirst { it.id == userProfile.id }
    val tierResId = when {
        rank == 0 -> R.drawable.tier_4
        points < 100 -> R.drawable.tier_1
        points < 200 -> R.drawable.tier_2
        else -> R.drawable.tier_3
    }
    val tierLabel = when {
        rank == 0 -> "Alpha"
        points < 100 -> "Lobo"
        points < 200 -> "Fera"
        else -> "Lenda"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.logo_perfil),
                        contentDescription = "Perfil",
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = FuriaYellow, contentColor = FuriaBlack)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.wallpaper),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(padding)
                    .padding(16.dp)
            ) {
                item { Spacer(Modifier.height(16.dp)) }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, FuriaYellow),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp, end = 8.dp, bottom = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                // Foto de perfil à esquerda
                                if (userProfile.photoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = userProfile.photoUrl,
                                        contentDescription = "Foto de perfil",
                                        modifier = Modifier.size(100.dp).clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Foto de perfil",
                                        tint = FuriaYellow,
                                        modifier = Modifier.size(100.dp)
                                    )
                                }
                                
                                // Nickname, pontos e título ao lado da foto
                                Spacer(Modifier.width(4.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = userProfile.nickname,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = FuriaYellow,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = tierResId),
                                            contentDescription = tierLabel,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(text = "$points FP", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                        Spacer(Modifier.width(4.dp))
                                        Text(text = tierLabel, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                    }
                                }
                                
                                // Redes sociais à direita
                                Spacer(Modifier.weight(1f))
                                if (userProfile.socialLinks.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        userProfile.socialLinks.forEach { (platform, url) ->
                                            val resId = when (platform.lowercase()) {
                                                "instagram" -> R.drawable.icon_insta
                                                "youtube" -> R.drawable.youtube_icon
                                                "twitch" -> R.drawable.twitch_icon
                                                "x", "twitter" -> R.drawable.icon_x
                                                else -> R.drawable.icon_furia
                                            }
                                            if (resId != 0) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.combinedClickable(
                                                        onClick = {
                                                            val target = when(platform.lowercase()) {
                                                                "instagram" -> "https://www.instagram.com/$url"
                                                                "twitch" -> "https://www.twitch.tv/$url"
                                                                "x" -> "https://www.twitter.com/$url"
                                                                else -> url
                                                            }
                                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)))
                                                        },
                                                        onLongClick = {
                                                            scope.launch { snackbarHostState.showSnackbar("@$url") }
                                                        }
                                                    )
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = resId),
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        text = "@$url",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Divider(color = Color.White, thickness = 1.dp)
                            Text(
                                text = "Jogos Favoritos",
                                style = MaterialTheme.typography.titleLarge,
                                color = FuriaYellow,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                userProfile.favorites.forEach { fav ->
                                    val resId = when (fav.lowercase()) {
                                        "valorant" -> R.drawable.icon_valorant
                                        "lol" -> R.drawable.icon_lol
                                        "cs" -> R.drawable.icon_cs2
                                        "rocket league", "rocketleague" -> R.drawable.icon_rocketleague
                                        "pubg" -> R.drawable.icon_pubg
                                        "r6" -> R.drawable.icon_r6
                                        "kingsleague" -> R.drawable.icon_kingsleague
                                        else -> R.drawable.icon_furia
                                    }
                                    if (resId != 0) {
                                        Image(
                                            painter = painterResource(id = resId),
                                            contentDescription = fav,
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
                item {
                    Text(
                        text = "Leaderboard",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                itemsIndexed(leaderboard) { idx, profile ->
                    val itemLabel = when {
                        idx == 0 -> "Alpha"
                        profile.points < 100 -> "Lobo"
                        profile.points < 200 -> "Fera"
                        else -> "Lenda"
                    }
                    val itemRes = when {
                        idx == 0 -> R.drawable.tier_4
                        profile.points < 100 -> R.drawable.tier_1
                        profile.points < 200 -> R.drawable.tier_2
                        else -> R.drawable.tier_3
                    }
                    Divider(color = Color.Gray)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Image(painter = painterResource(id = itemRes), contentDescription = itemLabel, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(text = profile.nickname, modifier = Modifier.weight(3f), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp), color = Color.White)
                        Text(text = itemLabel, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp), color = Color.White, textAlign = TextAlign.Center)
                        Text(text = "${profile.points}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp), color = Color.White, textAlign = TextAlign.End)
                    }
                }
            }
        }
    }
}
