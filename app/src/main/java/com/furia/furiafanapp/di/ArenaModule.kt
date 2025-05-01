package com.furia.furiafanapp.di

import com.furia.furiafanapp.data.repository.ArenaRepository
import com.furia.furiafanapp.data.repository.ArenaRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ArenaModule {

    @Provides
    @Singleton
    fun provideArenaRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        gamificationRepository: com.furia.furiafanapp.data.repository.GamificationRepository
    ): ArenaRepository {
        return ArenaRepositoryImpl(firestore, auth, gamificationRepository)
    }
}
