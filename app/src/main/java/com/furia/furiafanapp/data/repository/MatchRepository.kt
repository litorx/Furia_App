package com.furia.furiafanapp.data.repository

import com.furia.furiafanapp.data.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    fun getUpcomingMatches(): Flow<List<Match>>
    fun getLiveMatches(): Flow<List<Match>>
    fun getClosedMatches(): Flow<List<Match>>
    suspend fun refreshMatches()
}