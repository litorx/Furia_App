@file:OptIn(
    androidx.compose.material.ExperimentalMaterialApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.furia.furiafanapp.ui.screens.Settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.edit
import com.furia.furiafanapp.util.settingsPrefs
import com.furia.furiafanapp.util.KEY_NOTIFICATIONS_ENABLED
import com.furia.furiafanapp.util.KEY_BADGE_NOTIFICATIONS_ENABLED

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsFlow = context.settingsPrefs.data
    val notificationsEnabled by prefsFlow.map { it[KEY_NOTIFICATIONS_ENABLED] ?: true }.collectAsState(initial = true)
    val badgeEnabled by prefsFlow.map { it[KEY_BADGE_NOTIFICATIONS_ENABLED] ?: true }.collectAsState(initial = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Notificações")
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        scope.launch {
                            context.settingsPrefs.edit { prefs ->
                                prefs[KEY_NOTIFICATIONS_ENABLED] = it
                            }
                        }
                    }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Badge de pontuação")
                Switch(
                    checked = badgeEnabled,
                    onCheckedChange = {
                        scope.launch {
                            context.settingsPrefs.edit { prefs ->
                                prefs[KEY_BADGE_NOTIFICATIONS_ENABLED] = it
                            }
                        }
                    }
                )
            }
            Button(
                onClick = {
                    scope.launch {
                        context.settingsPrefs.edit { it.clear() }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Limpar cache")
            }
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(
                        "login"
                    ) {
                        popUpTo("home") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sair")
            }
        }
    }
}
