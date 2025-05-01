package com.furia.furiafanapp.di

import android.content.Context
import com.furia.furiafanapp.data.chat.ChatRepository
import com.furia.furiafanapp.data.repository.GamificationRepository
import com.furia.furiafanapp.data.repository.MatchRepository
import com.furia.furiafanapp.domain.usecases.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides domain use case dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetUpcomingMatchesUseCase(
        repository: MatchRepository
    ): GetUpcomingMatchesUseCase = GetUpcomingMatchesUseCase(repository)

    @Provides
    @Singleton
    fun provideGetLiveMatchesUseCase(
        repository: MatchRepository
    ): GetLiveMatchesUseCase = GetLiveMatchesUseCase(repository)

    @Provides
    @Singleton
    fun provideGetClosedMatchesUseCase(
        repository: MatchRepository
    ): GetClosedMatchesUseCase = GetClosedMatchesUseCase(repository)

    @Provides
    @Singleton
    fun provideRefreshMatchesUseCase(
        repository: MatchRepository
    ): RefreshMatchesUseCase = RefreshMatchesUseCase(repository)

    @Provides
    @Singleton
    fun provideGetChatMessagesUseCase(
        chatRepository: ChatRepository
    ): GetChatMessagesUseCase = GetChatMessagesUseCase(chatRepository)

    @Provides
    @Singleton
    fun provideSendChatMessageUseCase(
        chatRepository: ChatRepository
    ): SendChatMessageUseCase = SendChatMessageUseCase(chatRepository)

    @Provides
    @Singleton
    fun provideAddPointsUseCase(
        repository: GamificationRepository
    ): AddPointsUseCase = AddPointsUseCase(repository)

    @Provides
    @Singleton
    fun provideScheduleReminderUseCase(
        @ApplicationContext context: Context
    ): ScheduleReminderUseCase = ScheduleReminderUseCase(context)

    @Provides
    @Singleton
    fun provideCancelReminderUseCase(
        @ApplicationContext context: Context
    ): CancelReminderUseCase = CancelReminderUseCase(context)
}
