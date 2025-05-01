package com.furia.furiafanapp.di

import com.furia.furiafanapp.data.chat.ChatRepository
import com.furia.furiafanapp.data.chat.FirestoreChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger module for chat dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object ChatModule {
    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore
    ): ChatRepository = FirestoreChatRepository(firestore)
}
