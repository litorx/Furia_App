package com.furia.furiafanapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furia.furiafanapp.data.model.UserPreferences
import com.furia.furiafanapp.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val availableGames: List<String> = listOf(
        "Todas",
        "Valorant",
        "CS",
        "LoL",
        "KingsLeague",
        "R6",
        "Rocket League",
        "PUBG"
    )
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collect { preferences ->
                _uiState.update { it.copy(userPreferences = preferences) }
            }
        }
    }

    fun updateFavoriteGames(favoriteGames: Set<String>) {
        viewModelScope.launch {
            userPreferencesRepository.updateUserPreferences(
                _uiState.value.userPreferences.copy(favoriteGames = favoriteGames)
            )
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateUserPreferences(
                _uiState.value.userPreferences.copy(notificationsEnabled = enabled)
            )
        }
    }
} 