package com.furia.furiafanapp.data.model

import java.util.Date

data class Match(
    val id: String,
    val homeTeam: Team,
    val awayTeam: Team,
    val tournament: Tournament,
    val startTime: Date,
    val status: MatchStatus,
    val score: Score? = null,
    val streams: List<Stream> = emptyList()
)

data class Team(
    val id: String,
    val name: String,
    val logoUrl: String? = null
)

data class Tournament(
    val id: String,
    val name: String,
    val game: String
)

data class Score(
    val home: Int,
    val away: Int
)

data class Stream(
    val platform: StreamPlatform,
    val url: String
)

enum class StreamPlatform {
    TWITCH,
    YOUTUBE
}

enum class MatchStatus {
    SCHEDULED,
    LIVE,
    FINISHED
} 