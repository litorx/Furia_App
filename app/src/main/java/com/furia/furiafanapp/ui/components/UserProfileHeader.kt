package com.furia.furiafanapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.furia.furiafanapp.R
import com.furia.furiafanapp.data.model.UserProfile
import com.furia.furiafanapp.ui.theme.FuriaWhite

/**
 * Componente reutilizável para exibir o perfil do usuário com tier, nickname e pontos (FP)
 */
@Composable
fun UserProfileHeader(
    userProfile: UserProfile,
    isTopOne: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Determinar o tier do usuário com base nos pontos
        val points = userProfile.points?.toInt() ?: 0
        val tierResId = when {
            isTopOne -> R.drawable.tier_4  // Alpha (top 1)
            points < 100 -> R.drawable.tier_1  // Lobo
            points < 200 -> R.drawable.tier_2  // Fera
            else -> R.drawable.tier_3  // Lenda
        }
        
        Image(
            painter = painterResource(id = tierResId),
            contentDescription = "Patente do usuário",
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = userProfile.nickname ?: "Usuário",
                style = MaterialTheme.typography.titleMedium,
                color = FuriaWhite
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${userProfile.points ?: 0} FP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFD700)
                )
            }
        }
    }
}
