package com.furia.furiafanapp.data.repository

import com.furia.furiafanapp.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserProfile(userId: String): Flow<UserProfile>
    suspend fun addPoints(userId: String, points: Long)
    fun getLeaderboard(): Flow<List<UserProfile>>
    suspend fun awardPoints(points: Long)
}
