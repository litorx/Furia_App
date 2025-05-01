package com.furia.furiafanapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.furia.furiafanapp.data.repository.ProfileRepository
import com.furia.furiafanapp.data.repository.GamificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import com.furia.furiafanapp.data.model.UserProfile

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val gamificationRepository: GamificationRepository
) : ViewModel() {

    private val userId: String = auth.currentUser?.uid.orEmpty()

    val userProfile: StateFlow<UserProfile> = profileRepository.getUserProfile(userId)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            UserProfile("", "", "", emptyMap(), 0L, emptyList())
        )
    val leaderboard: Flow<List<UserProfile>> = profileRepository.getLeaderboard()
    val pointsFlow = gamificationRepository.getPoints()
    val badgeFlow = gamificationRepository.getBadge()

    init {
        viewModelScope.launch { gamificationRepository.syncIfNeeded() }
    }

    // Gamificação: 5 pontos por visualizar jogos (1x por dia)
    fun awardViewGames() {
        viewModelScope.launch { gamificationRepository.awardPoints(5) }
    }

    // Gamificação: 7 pontos por assistir jogos ao vivo (1x por dia)
    fun awardWatchLiveGames() {
        viewModelScope.launch { gamificationRepository.awardPoints(7) }
    }

    // Gamificação: 2 pontos por mensagem no chat (máx 10 msgs/dia)
    fun awardChatInteraction() {
        viewModelScope.launch { gamificationRepository.awardPoints(2) }
    }
}
