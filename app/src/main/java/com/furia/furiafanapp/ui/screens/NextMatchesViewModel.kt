package com.furia.furiafanapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furia.furiafanapp.data.model.Match
import com.furia.furiafanapp.data.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NextMatchesViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _upcomingMatches = MutableStateFlow<List<Match>>(emptyList())
    val upcomingMatches: StateFlow<List<Match>> = _upcomingMatches.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUpcomingMatches()
    }

    private fun loadUpcomingMatches() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                matchRepository.getUpcomingMatches().collectLatest { matches ->
                    _upcomingMatches.value = matches
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadUpcomingMatches()
    }
}
