package com.furia.furiafanapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furia.furiafanapp.data.model.Match
import com.furia.furiafanapp.data.model.MatchStatus
import com.furia.furiafanapp.data.model.Score
import com.furia.furiafanapp.data.model.Stream
import com.furia.furiafanapp.data.model.StreamPlatform
import com.furia.furiafanapp.data.model.Team
import com.furia.furiafanapp.data.model.Tournament
import com.furia.furiafanapp.data.repository.MatchRepository
import com.furia.furiafanapp.domain.usecases.CancelReminderUseCase
import com.furia.furiafanapp.domain.usecases.ScheduleReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Date

sealed class MatchesUiState {
    object Loading : MatchesUiState()
    data class Success(val matches: List<Match>) : MatchesUiState()
    data class Error(val message: String) : MatchesUiState()
}

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val repository: MatchRepository,
    private val scheduleReminderUseCase: ScheduleReminderUseCase,
    private val cancelReminderUseCase: CancelReminderUseCase
) : ViewModel() {

    private val _upcomingMatchesState = MutableStateFlow<MatchesUiState>(MatchesUiState.Loading)
    val upcomingMatchesState = _upcomingMatchesState.asStateFlow()

    private val _liveMatchesState = MutableStateFlow<MatchesUiState>(MatchesUiState.Loading)
    val liveMatchesState = _liveMatchesState.asStateFlow()

    private val _closedMatchesState = MutableStateFlow<MatchesUiState>(MatchesUiState.Loading)
    val closedMatchesState = _closedMatchesState.asStateFlow()

    // Estado de lembretes agendados
    private val _scheduledReminders = MutableStateFlow<Set<String>>(emptySet())
    val scheduledReminders: StateFlow<Set<String>> = _scheduledReminders.asStateFlow()

    private var isPollingActive = false
    private val pollingInterval = 15000L // 15 segundos

    init {
        refreshMatches()
        startLiveMatchesPolling()
    }

    fun refreshMatches() {
        _upcomingMatchesState.value = MatchesUiState.Loading
        _liveMatchesState.value = MatchesUiState.Loading
        _closedMatchesState.value = MatchesUiState.Loading
        viewModelScope.launch {
            try {
                repository.refreshMatches()

                // Coletar partidas próximas
                launch {
                    repository.getUpcomingMatches().collect { matches ->
                        if (matches.isNotEmpty()) {
                            _upcomingMatchesState.value = MatchesUiState.Success(matches)
                        } else {
                            // Se não houver partidas reais, usar dados de teste
                            _upcomingMatchesState.value = MatchesUiState.Success(createMockUpcomingMatches())
                        }
                    }
                }

                // Coletar partidas ao vivo
                launch {
                    repository.getLiveMatches().collect { matches ->
                        if (matches.isNotEmpty()) {
                            _liveMatchesState.value = MatchesUiState.Success(matches)
                        } else {
                            // Se não houver partidas reais, usar dados de teste
                            _liveMatchesState.value = MatchesUiState.Success(createMockLiveMatches())
                        }
                    }
                }

                // Coletar partidas encerradas
                launch {
                    repository.getClosedMatches().collect { matches ->
                        if (matches.isNotEmpty()) {
                            _closedMatchesState.value = MatchesUiState.Success(matches)
                        } else {
                            // Se não houver partidas reais, usar dados de teste
                            _closedMatchesState.value = MatchesUiState.Success(createMockClosedMatches())
                        }
                    }
                }
            } catch (e: Exception) {
                // Em caso de erro, forçar a atualização para usar os dados mock
                _upcomingMatchesState.value = MatchesUiState.Success(createMockUpcomingMatches())
                _liveMatchesState.value = MatchesUiState.Success(createMockLiveMatches())
                _closedMatchesState.value = MatchesUiState.Success(createMockClosedMatches())
            }
        }
    }

    private fun startLiveMatchesPolling() {
        if (isPollingActive) return
        
        isPollingActive = true
        viewModelScope.launch {
            while (isActive && isPollingActive) {
                try {
                    repository.refreshMatches()
                } catch (e: Exception) {
                    // Falha silenciosa no polling, continuamos tentando
                }
                delay(pollingInterval)
            }
        }
    }

    fun stopLiveMatchesPolling() {
        isPollingActive = false
    }

    override fun onCleared() {
        super.onCleared()
        stopLiveMatchesPolling()
    }
    
    // Dados mock para garantir que sempre haja conteúdo para exibir
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

    fun scheduleReminder(matchId: String, matchTimeMillis: Long) {
        scheduleReminderUseCase(matchId, matchTimeMillis)
        _scheduledReminders.value = _scheduledReminders.value + matchId
    }

    /**
     * Cancela um lembrete agendado.
     */
    fun cancelReminder(matchId: String) {
        cancelReminderUseCase(matchId)
        _scheduledReminders.value = _scheduledReminders.value - matchId
    }
}