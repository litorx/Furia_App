package com.furia.furiafanapp.data.model

data class UserProfile(
    val id: String,
    val nickname: String,
    val photoUrl: String = "",
    val socialLinks: Map<String, String> = emptyMap(),
    val points: Long = 0L,
    val favorites: List<String> = emptyList()
)
