package com.furia.furiafanapp.di

import com.furia.furiafanapp.data.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OpenAIModule {
    
    @Provides
    @Singleton
    fun provideChatRepository(
        @Named("openai") httpClient: HttpClient,
        json: Json
    ): ChatRepository = ChatRepository(httpClient, json)
}
