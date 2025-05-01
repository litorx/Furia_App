package com.furia.furiafanapp.domain.usecases

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.furia.furiafanapp.data.model.Match
import com.furia.furiafanapp.data.model.ChatMessage
import com.furia.furiafanapp.data.repository.MatchRepository
import com.furia.furiafanapp.data.chat.ChatRepository
import com.furia.furiafanapp.data.repository.GamificationRepository
import com.furia.furiafanapp.worker.ReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Domain use cases for matches, chat, gamification and reminders.
 */

/** Get a stream of upcoming matches. */
class GetUpcomingMatchesUseCase @Inject constructor(
    private val repository: MatchRepository
) {
    operator fun invoke(): Flow<List<Match>> = repository.getUpcomingMatches()
}

/** Get a stream of live matches. */
class GetLiveMatchesUseCase @Inject constructor(
    private val repository: MatchRepository
) {
    operator fun invoke(): Flow<List<Match>> = repository.getLiveMatches()
}

/** Get a stream of closed matches. */
class GetClosedMatchesUseCase @Inject constructor(
    private val repository: MatchRepository
) {
    operator fun invoke(): Flow<List<Match>> = repository.getClosedMatches()
}

/** Refresh match data (fetch from API and update cache). */
class RefreshMatchesUseCase @Inject constructor(
    private val repository: MatchRepository
) {
    suspend operator fun invoke() = repository.refreshMatches()
}

/** Get chat messages for a specific match. */
class GetChatMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(matchId: String): Flow<List<ChatMessage>> = repository.getMessages(matchId)
}

/** Send a chat message in a match. */
class SendChatMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(matchId: String, message: ChatMessage) = repository.sendMessage(matchId, message)
}

/** Award gamification points to the user. */
class AddPointsUseCase @Inject constructor(
    private val repository: GamificationRepository
) {
    suspend operator fun invoke(points: Int) = repository.awardPoints(points)
}

/** Schedule a reminder notification 1h before match start. */
class ScheduleReminderUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(matchId: String, matchTimeMillis: Long) {
        val delay = matchTimeMillis - System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
        if (delay <= 0) return
        val data = Data.Builder().putString("matchId", matchId).build()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(matchId)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}

/**
 * Cancel scheduled reminder by matchId.
 */
class CancelReminderUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(matchId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(matchId)
    }
}
