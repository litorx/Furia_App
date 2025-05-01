package com.furia.furiafanapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furia.furiafanapp.data.model.Match
import com.furia.furiafanapp.data.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val upcomingMatches: Flow<List<Match>> = repository.getUpcomingMatches()
    val liveMatches: Flow<List<Match>> = repository.getLiveMatches()
    val closedMatches: Flow<List<Match>> = repository.getClosedMatches()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.refreshMatches()
            _isLoading.value = false
        }
    }
}
