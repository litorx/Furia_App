package com.furia.furiafanapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val favoriteGames: Set<String> = emptySet(),
    val notificationsEnabled: Boolean = true
) 