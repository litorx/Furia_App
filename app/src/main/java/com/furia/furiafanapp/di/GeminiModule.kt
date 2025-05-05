package com.furia.furiafanapp.di

import android.content.Context
import com.furia.furiafanapp.data.repository.GeminiChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeminiModule {
    @Provides
    @Singleton
    fun provideGeminiChatRepository(
        @ApplicationContext context: Context
    ): GeminiChatRepository {
        return GeminiChatRepository(context)
    }
}