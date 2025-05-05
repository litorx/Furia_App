package com.furia.furiafanapp.ui.screens.Chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log
import com.furia.furiafanapp.R
import com.furia.furiafanapp.data.model.Match
import com.furia.furiafanapp.ui.theme.FuriaBlack
import com.furia.furiafanapp.ui.theme.FuriaWhite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.furia.furiafanapp.ui.screens.Profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    match: Match,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    Log.d("ChatScreen", "open for ${match.id}")
    val msgs by viewModel.getMessages(match.id).collectAsState(initial = emptyList())
    var text by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    val nameMap = remember { mutableStateMapOf<String, String>() }

    Scaffold(
        containerColor = FuriaBlack,
        topBar = {
            Box(
                modifier = Modifier
                    .background(FuriaBlack)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = FuriaWhite
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = match.tournament.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = FuriaWhite.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${match.homeTeam.name} ${match.score?.home ?: 0} - ${match.score?.away ?: 0} ${match.awayTeam.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = FuriaWhite
                    )
                }
            }
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FuriaBlack)
                .padding(pad)
        ) {
            // Wallpaper background with dark overlay
            Image(
                painter = painterResource(id = R.drawable.wallpaper),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(msgs) { msg ->
                        val userId = msg.user
                        LaunchedEffect(userId) {
                            if (!nameMap.containsKey(userId)) {
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(userId)
                                    .get()
                                    .addOnSuccessListener { doc ->
                                        doc.getString("nickname")?.let { nameMap[userId] = it }
                                    }
                            }
                        }
                        val displayName = nameMap[userId] ?: ""
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FuriaWhite.copy(alpha = 0.2f),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = if (displayName.isNotBlank()) "$displayName: ${msg.text}" else msg.text,
                                    color = Color.White,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
                if (showEmojiPicker) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        val emojis = listOf("😊","😂","❤️","👍","🔥","🎉","😎")
                        items(emojis) { e ->
                            Text(
                                text = e,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clickable {
                                        text += e
                                        showEmojiPicker = false
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showEmojiPicker = !showEmojiPicker }) {
                        Icon(
                            Icons.Default.EmojiEmotions,
                            contentDescription = "Emojis",
                            tint = Color.White
                        )
                    }
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Digite...", color = Color.White.copy(alpha = 0.6f)) },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(color = Color.White),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Black.copy(alpha = 0.55f),
                            cursorColor = Color.White
                        )
                    )
                    IconButton(onClick = {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        viewModel.send(match.id, text, uid)
                        profileViewModel.awardChatInteraction()
                        text = ""
                    }) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
