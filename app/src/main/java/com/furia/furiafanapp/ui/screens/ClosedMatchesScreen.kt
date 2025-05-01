package com.furia.furiafanapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.furia.furiafanapp.ui.theme.FuriaWhite
import com.furia.furiafanapp.ui.theme.FuriaYellow
import com.furia.furiafanapp.data.model.Match
import com.furia.furiafanapp.ui.viewmodels.MatchesViewModel

@Composable
fun ClosedMatchesTab(viewModel: MatchesViewModel = hiltViewModel()) {
    val matches by viewModel.closedMatches.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            text = "Partidas Encerradas",
            style = MaterialTheme.typography.titleLarge,
            color = FuriaWhite
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { viewModel.refresh() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = FuriaYellow)
        ) {
            Text("Atualizar", color = FuriaWhite)
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (isLoading) {
            CircularProgressIndicator(color = FuriaYellow)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(matches) { match ->
                    MatchItem(match)
                }
            }
        }
    }
}
