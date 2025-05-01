package com.furia.furiafanapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.furia.furiafanapp.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.hours

private val Context.dataStore by preferencesDataStore(name = "matches")

// Chaves para o DataStore
private object PreferencesKeys {
    val UPCOMING_MATCHES = stringPreferencesKey("upcoming_matches")
    val LIVE_MATCHES = stringPreferencesKey("live_matches")
    val CLOSED_MATCHES = stringPreferencesKey("closed_matches")
    val LAST_REFRESH_TIME = longPreferencesKey("last_refresh_time")
}

@Singleton
class MatchRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: HttpClient,
    private val json: Json
) : MatchRepository {
    private val dataStore: DataStore<Preferences> = context.dataStore

    // Cache Time: 1 hora
    private val CACHE_DURATION = 1.hours.inWholeMilliseconds

    private val _upcomingMatches = MutableStateFlow<List<Match>>(emptyList())
    private val _liveMatches = MutableStateFlow<List<Match>>(emptyList())
    private val _closedMatches = MutableStateFlow<List<Match>>(emptyList())

    init {
        // Carregar dados do cache ao inicializar
        loadCachedData()
    }

    private fun loadCachedData() {
        // Upcoming Matches
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.UPCOMING_MATCHES]?.let { jsonString ->
                try {
                    parseMatchesFromJson(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }.onEach { matches ->
            _upcomingMatches.value = matches
        }.launchIn(kotlinx.coroutines.GlobalScope)

        // Live Matches
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LIVE_MATCHES]?.let { jsonString ->
                try {
                    parseMatchesFromJson(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }.onEach { matches ->
            _liveMatches.value = matches
        }.launchIn(kotlinx.coroutines.GlobalScope)

        // Closed Matches
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.CLOSED_MATCHES]?.let { jsonString ->
                try {
                    parseMatchesFromJson(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }.onEach { matches ->
            _closedMatches.value = matches
        }.launchIn(kotlinx.coroutines.GlobalScope)
    }

    override fun getUpcomingMatches(): Flow<List<Match>> = _upcomingMatches.asStateFlow()

    override fun getLiveMatches(): Flow<List<Match>> = _liveMatches.asStateFlow()

    override fun getClosedMatches(): Flow<List<Match>> = _closedMatches.asStateFlow()

    override suspend fun refreshMatches() {
        try {
            // Verificar se o cache ainda é válido
            val lastRefreshTime = dataStore.data.first()[PreferencesKeys.LAST_REFRESH_TIME] ?: 0
            val currentTime = System.currentTimeMillis()
            
            // Se o cache for válido e não for uma requisição forçada, use o cache
            if (currentTime - lastRefreshTime < CACHE_DURATION) {
                // Dados já carregados do cache no init, não precisa fazer nada
                return
            }

            // Buscar partidas da API PandaScore
            val upcomingMatches = fetchUpcomingMatches()
            val liveMatches = fetchLiveMatches()
            val closedMatches = fetchClosedMatches()

            // Atualizar os flows
            _upcomingMatches.value = upcomingMatches
            _liveMatches.value = liveMatches
            _closedMatches.value = closedMatches

            // Salvar no cache
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.UPCOMING_MATCHES] = serializeMatchesToJson(upcomingMatches)
                preferences[PreferencesKeys.LIVE_MATCHES] = serializeMatchesToJson(liveMatches)
                preferences[PreferencesKeys.CLOSED_MATCHES] = serializeMatchesToJson(closedMatches)
                preferences[PreferencesKeys.LAST_REFRESH_TIME] = currentTime
            }
        } catch (e: Exception) {
            // Em caso de erro, usar os dados do cache (já carregados nos flows)
            // Se os flows estiverem vazios, usar dados mock
            if (_upcomingMatches.value.isEmpty()) {
                _upcomingMatches.value = createMockUpcomingMatches()
            }
            if (_liveMatches.value.isEmpty()) {
                _liveMatches.value = createMockLiveMatches()
            }
            if (_closedMatches.value.isEmpty()) {
                _closedMatches.value = createMockClosedMatches()
            }
        }
    }

    private suspend fun fetchUpcomingMatches(): List<Match> {
        // Buscar partidas de todos os jogos da FURIA
        try {
            println("Fetching upcoming matches...")
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.pandascore.co"
                    path("teams", "126190", "matches")
                    parameters.append("filter[status]", "not_started")
                    parameters.append("sort", "begin_at")
                    parameters.append("page[number]", "1")
                    parameters.append("page[size]", "10")
                }
            }
            
            val responseBody = response.body<String>()
            println("Upcoming matches response status: ${response.status}")
            return parsePandaScoreResponse(responseBody)
        } catch (e: Exception) {
            println("Error fetching upcoming matches: ${e.message}")
            // Em caso de erro, retornar dados do cache ou mock
            return _upcomingMatches.value.ifEmpty { createMockUpcomingMatches() }
        }
    }

    private suspend fun fetchLiveMatches(): List<Match> {
        // Buscar partidas ao vivo de todos os jogos da FURIA
        try {
            println("Fetching live matches...")
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.pandascore.co"
                    path("teams", "126190", "matches")
                    parameters.append("filter[status]", "running")
                    parameters.append("sort", "begin_at")
                    parameters.append("page[number]", "1")
                    parameters.append("page[size]", "10")
                }
            }
            
            val responseBody = response.body<String>()
            println("Live matches response status: ${response.status}")
            return parsePandaScoreResponse(responseBody)
        } catch (e: Exception) {
            println("Error fetching live matches: ${e.message}")
            // Em caso de erro, retornar dados do cache ou mock
            return _liveMatches.value.ifEmpty { createMockLiveMatches() }
        }
    }

    private suspend fun fetchClosedMatches(): List<Match> {
        // Buscar partidas encerradas de todos os jogos da FURIA
        try {
            println("Fetching closed matches...")
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.pandascore.co"
                    path("teams", "126190", "matches")
                    parameters.append("filter[status]", "finished")
                    parameters.append("sort", "-begin_at")
                    parameters.append("page[number]", "1")
                    parameters.append("page[size]", "10")
                }
            }
            
            val responseBody = response.body<String>()
            println("Closed matches response status: ${response.status}")
            return parsePandaScoreResponse(responseBody)
        } catch (e: Exception) {
            println("Error fetching closed matches: ${e.message}")
            // Em caso de erro, retornar dados do cache ou mock
            return _closedMatches.value.ifEmpty { createMockClosedMatches() }
        }
    }

    private fun parsePandaScoreResponse(responseBody: String): List<Match> {
        try {
            println("PandaScore API Response: $responseBody")
            
            val jsonArray = json.parseToJsonElement(responseBody).jsonArray
            return jsonArray.mapNotNull { jsonElement ->
                try {
                    val jsonObject = jsonElement.jsonObject
                    
                    val id = jsonObject["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    
                    // Parse match status correctly
                    val status = when (jsonObject["status"]?.jsonPrimitive?.content) {
                        "running" -> MatchStatus.LIVE
                        "finished" -> MatchStatus.FINISHED
                        "not_started" -> MatchStatus.SCHEDULED
                        else -> MatchStatus.SCHEDULED
                    }
                    
                    // Parse date correctly using ISO 8601 format
                    val beginAt = jsonObject["begin_at"]?.jsonPrimitive?.content
                    val startTime = if (!beginAt.isNullOrEmpty()) {
                        try {
                            // Format can be either "2023-05-20T15:30:00Z" or "2023-05-20T15:30:00.000Z"
                            if (beginAt.contains(".")) {
                                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                                formatter.timeZone = TimeZone.getTimeZone("UTC")
                                formatter.parse(beginAt) ?: Date()
                            } else {
                                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                                formatter.timeZone = TimeZone.getTimeZone("UTC")
                                formatter.parse(beginAt) ?: Date()
                            }
                        } catch (e: Exception) {
                            println("Date parsing error: ${e.message} for date: $beginAt")
                            Date()
                        }
                    } else {
                        Date()
                    }
                    
                    // Extract teams information correctly
                    val opponents = jsonObject["opponents"]?.jsonArray ?: return@mapNotNull null
                    val teamId = "126190"
                    // Define home/away baseado no ID da FURIA
                    val homeElem = opponents.firstOrNull {
                        it.jsonObject["opponent"]?.jsonObject
                            ?.get("id")?.jsonPrimitive?.content == teamId
                    } ?: opponents.first()
                    val awayElem = opponents.firstOrNull { it != homeElem } ?: opponents.getOrNull(1)!!
                    val homeJson = homeElem.jsonObject["opponent"]?.jsonObject ?: return@mapNotNull null
                    val awayJson = awayElem.jsonObject["opponent"]?.jsonObject ?: return@mapNotNull null
                    val homeTeam = Team(
                        id = homeJson["id"]?.jsonPrimitive?.content ?: "",
                        name = homeJson["name"]?.jsonPrimitive?.content ?: "Unknown Team",
                        logoUrl = homeJson["image_url"]?.jsonPrimitive?.contentOrNull
                    )
                    
                    val awayTeam = Team(
                        id = awayJson["id"]?.jsonPrimitive?.content ?: "",
                        name = awayJson["name"]?.jsonPrimitive?.content ?: "Unknown Team",
                        logoUrl = awayJson["image_url"]?.jsonPrimitive?.contentOrNull
                    )
                    
                    // Extract tournament information
                    val leagueJson = jsonObject["league"]?.jsonObject
                    val seriesJson = jsonObject["serie"]?.jsonObject
                    val tournamentJson = jsonObject["tournament"]?.jsonObject
                    val videogameJson = jsonObject["videogame"]?.jsonObject
                    
                    val tournamentName = tournamentJson?.get("name")?.jsonPrimitive?.contentOrNull
                        ?: seriesJson?.get("name")?.jsonPrimitive?.contentOrNull
                        ?: leagueJson?.get("name")?.jsonPrimitive?.contentOrNull
                        ?: "Unknown Tournament"
                    
                    // Get the videogame name
                    val gameName = videogameJson?.get("name")?.jsonPrimitive?.contentOrNull ?: "Unknown Game"
                    
                    val tournament = Tournament(
                        id = tournamentJson?.get("id")?.jsonPrimitive?.content ?: "",
                        name = tournamentName,
                        game = gameName
                    )
                    
                    // Extract score information
                    val score = if (status != MatchStatus.SCHEDULED) {
                        // Try different ways to get the score
                        val results = jsonObject["results"]?.jsonArray
                        if (results != null && results.size >= 2) {
                            try {
                                val homeScore = results[0].jsonObject["score"]?.jsonPrimitive?.intOrNull ?: 0
                                val awayScore = results[1].jsonObject["score"]?.jsonPrimitive?.intOrNull ?: 0
                                Score(homeScore, awayScore)
                            } catch (e: Exception) {
                                println("Error parsing score from results: ${e.message}")
                                Score(0, 0)
                            }
                        } else {
                            // Alternative way: check for score object
                            val scoreObj = jsonObject["score"]?.jsonObject
                            if (scoreObj != null) {
                                try {
                                    val homeScore = scoreObj["home"]?.jsonPrimitive?.intOrNull ?: 0
                                    val awayScore = scoreObj["away"]?.jsonPrimitive?.intOrNull ?: 0
                                    Score(homeScore, awayScore)
                                } catch (e: Exception) {
                                    println("Error parsing score from score object: ${e.message}")
                                    Score(0, 0)
                                }
                            } else {
                                Score(0, 0)
                            }
                        }
                    } else {
                        null
                    }
                    
                    // Extract stream links
                    val streams = mutableListOf<Stream>()
                    
                    // Try official streams first
                    val officialStreams = jsonObject["official_stream_url"]?.jsonPrimitive?.contentOrNull
                    if (!officialStreams.isNullOrEmpty()) {
                        val platform = when {
                            officialStreams.contains("twitch.tv") -> StreamPlatform.TWITCH
                            officialStreams.contains("youtube.com") -> StreamPlatform.YOUTUBE
                            else -> null
                        }
                        
                        if (platform != null) {
                            streams.add(Stream(platform, officialStreams))
                        }
                    }
                    
                    // Try live_embed_url if available
                    val liveEmbedUrl = jsonObject["live_embed_url"]?.jsonPrimitive?.contentOrNull
                    if (!liveEmbedUrl.isNullOrEmpty()) {
                        val platform = when {
                            liveEmbedUrl.contains("twitch.tv") -> StreamPlatform.TWITCH
                            liveEmbedUrl.contains("youtube.com") -> StreamPlatform.YOUTUBE
                            else -> null
                        }
                        
                        if (platform != null) {
                            streams.add(Stream(platform, liveEmbedUrl))
                        }
                    }
                    
                    // Try streams_list if available
                    jsonObject["streams_list"]?.jsonArray?.forEach { streamJson ->
                        try {
                            val streamObj = streamJson.jsonObject
                            val url = streamObj["raw_url"]?.jsonPrimitive?.contentOrNull
                                ?: streamObj["url"]?.jsonPrimitive?.contentOrNull
                            val language = streamObj["language"]?.jsonPrimitive?.contentOrNull
                            
                            if (!url.isNullOrEmpty() && (language == null || language == "en" || language == "pt")) {
                                val platform = when {
                                    url.contains("twitch.tv") -> StreamPlatform.TWITCH
                                    url.contains("youtube.com") -> StreamPlatform.YOUTUBE
                                    else -> null
                                }
                                
                                if (platform != null) {
                                    streams.add(Stream(platform, url))
                                }
                            }
                        } catch (e: Exception) {
                            println("Error parsing stream: ${e.message}")
                        }
                    }
                    
                    // Create the match object with all parsed data
                    Match(
                        id = id,
                        homeTeam = homeTeam,
                        awayTeam = awayTeam,
                        tournament = tournament,
                        startTime = startTime,
                        status = status,
                        score = score,
                        streams = streams
                    )
                } catch (e: Exception) {
                    println("Error parsing match: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Error parsing PandaScore response: ${e.message}")
            return emptyList()
        }
    }

    private fun parseMatchesFromJson(jsonString: String): List<Match> {
        // Implementação simplificada. Na prática, você usaria kotlinx.serialization
        return try {
            val jsonArray = json.parseToJsonElement(jsonString).jsonArray
            jsonArray.mapNotNull { jsonElement ->
                val obj = jsonElement.jsonObject
                
                val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                
                val homeTeamObj = obj["homeTeam"]?.jsonObject ?: return@mapNotNull null
                val awayTeamObj = obj["awayTeam"]?.jsonObject ?: return@mapNotNull null
                val tournamentObj = obj["tournament"]?.jsonObject ?: return@mapNotNull null
                
                val homeTeam = Team(
                    id = homeTeamObj["id"]?.jsonPrimitive?.content ?: "",
                    name = homeTeamObj["name"]?.jsonPrimitive?.content ?: "",
                    logoUrl = homeTeamObj["logoUrl"]?.jsonPrimitive?.content
                )
                
                val awayTeam = Team(
                    id = awayTeamObj["id"]?.jsonPrimitive?.content ?: "",
                    name = awayTeamObj["name"]?.jsonPrimitive?.content ?: "",
                    logoUrl = awayTeamObj["logoUrl"]?.jsonPrimitive?.content
                )
                
                val tournament = Tournament(
                    id = tournamentObj["id"]?.jsonPrimitive?.content ?: "",
                    name = tournamentObj["name"]?.jsonPrimitive?.content ?: "",
                    game = tournamentObj["game"]?.jsonPrimitive?.content ?: ""
                )
                
                val startTimeStr = obj["startTime"]?.jsonPrimitive?.content
                val startTime = if (startTimeStr != null) {
                    try {
                        Date(startTimeStr.toLong())
                    } catch (e: Exception) {
                        Date()
                    }
                } else {
                    Date()
                }
                
                val statusStr = obj["status"]?.jsonPrimitive?.content
                val status = when (statusStr) {
                    "LIVE" -> MatchStatus.LIVE
                    "FINISHED" -> MatchStatus.FINISHED
                    else -> MatchStatus.SCHEDULED
                }
                
                val scoreObj = obj["score"]?.jsonObject
                val score = if (scoreObj != null) {
                    Score(
                        home = scoreObj["home"]?.jsonPrimitive?.int ?: 0,
                        away = scoreObj["away"]?.jsonPrimitive?.int ?: 0
                    )
                } else {
                    null
                }
                
                val streamsArray = obj["streams"]?.jsonArray
                val streams = streamsArray?.mapNotNull { streamElement ->
                    val streamObj = streamElement.jsonObject
                    val platformStr = streamObj["platform"]?.jsonPrimitive?.content
                    val url = streamObj["url"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    
                    val platform = when (platformStr) {
                        "TWITCH" -> StreamPlatform.TWITCH
                        "YOUTUBE" -> StreamPlatform.YOUTUBE
                        else -> return@mapNotNull null
                    }
                    
                    Stream(platform, url)
                } ?: emptyList()
                
                Match(
                    id = id,
                    homeTeam = homeTeam,
                    awayTeam = awayTeam,
                    tournament = tournament,
                    startTime = startTime,
                    status = status,
                    score = score,
                    streams = streams
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeMatchesToJson(matches: List<Match>): String {
        // Implementação simplificada. Na prática, você usaria kotlinx.serialization
        val jsonArray = buildJsonArray {
            matches.forEach { match ->
                addJsonObject {
                    put("id", match.id)
                    
                    putJsonObject("homeTeam") {
                        put("id", match.homeTeam.id)
                        put("name", match.homeTeam.name)
                        match.homeTeam.logoUrl?.let { put("logoUrl", it) }
                    }
                    
                    putJsonObject("awayTeam") {
                        put("id", match.awayTeam.id)
                        put("name", match.awayTeam.name)
                        match.awayTeam.logoUrl?.let { put("logoUrl", it) }
                    }
                    
                    putJsonObject("tournament") {
                        put("id", match.tournament.id)
                        put("name", match.tournament.name)
                        put("game", match.tournament.game)
                    }
                    
                    put("startTime", match.startTime.time)
                    put("status", match.status.name)
                    
                    match.score?.let { score ->
                        putJsonObject("score") {
                            put("home", score.home)
                            put("away", score.away)
                        }
                    }
                    
                    putJsonArray("streams") {
                        match.streams.forEach { stream ->
                            addJsonObject {
                                put("platform", stream.platform.name)
                                put("url", stream.url)
                            }
                        }
                    }
                }
            }
        }
        
        return jsonArray.toString()
    }

    // Dados mock para fallback quando não há conexão
    private fun createMockUpcomingMatches(): List<Match> {
        return listOf(
            Match(
                id = "1",
                homeTeam = Team("furia", "FURIA"),
                awayTeam = Team("navi", "Natus Vincere"),
                tournament = Tournament("1", "ESL Pro League", "cs"),
                startTime = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000),
                status = MatchStatus.SCHEDULED,
                streams = listOf(
                    Stream(StreamPlatform.TWITCH, "https://twitch.tv/esl_csgo")
                )
            ),
            Match(
                id = "4",
                homeTeam = Team("furia", "FURIA"),
                awayTeam = Team("g2", "G2 Esports"),
                tournament = Tournament("4", "VCT Champions", "valorant"),
                startTime = Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000),
                status = MatchStatus.SCHEDULED,
                streams = listOf(
                    Stream(StreamPlatform.YOUTUBE, "https://youtube.com/valorant")
                )
            )
        )
    }

    private fun createMockLiveMatches(): List<Match> {
        // Para fins de teste, vamos gerar um placar aleatório que muda a cada chamada
        val homeScore = (0..15).random()
        val awayScore = (0..15).random()
        
        return listOf(
            Match(
                id = "2",
                homeTeam = Team("furia", "FURIA"),
                awayTeam = Team("liquid", "Team Liquid"),
                tournament = Tournament("2", "VCT Americas", "valorant"),
                startTime = Date(),
                status = MatchStatus.LIVE,
                score = Score(homeScore, awayScore),
                streams = listOf(
                    Stream(StreamPlatform.YOUTUBE, "https://youtube.com/valorant")
                )
            ),
            Match(
                id = "6",
                homeTeam = Team("furia", "FURIA"),
                awayTeam = Team("navi", "Natus Vincere"),
                tournament = Tournament("6", "ESL Pro League", "cs"),
                startTime = Date(),
                status = MatchStatus.LIVE,
                score = Score((0..30).random(), (0..30).random()),
                streams = listOf(
                    Stream(StreamPlatform.TWITCH, "https://twitch.tv/esl_csgo")
                )
            )
        )
    }

    private fun createMockClosedMatches(): List<Match> {
        return listOf(
            Match(
                id = "3",
                homeTeam = Team("furia", "FURIA"),
                awayTeam = Team("mibr", "MIBR"),
                tournament = Tournament("3", "CBLOL", "lol"),
                startTime = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000),
                status = MatchStatus.FINISHED,
                score = Score(2, 0)
            ),
            Match(
                id = "5",
                homeTeam = Team("furia", "FURIA"),
                awayTeam = Team("pain", "paiN Gaming"),
                tournament = Tournament("5", "CBLOL", "lol"),
                startTime = Date(System.currentTimeMillis() - 48 * 60 * 60 * 1000),
                status = MatchStatus.FINISHED,
                score = Score(1, 2)
            )
        )
    }
}