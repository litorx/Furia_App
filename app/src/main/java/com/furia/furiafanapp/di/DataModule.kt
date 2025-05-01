package com.furia.furiafanapp.di

import android.content.Context
import com.furia.furiafanapp.data.repository.GamificationRepository
import com.furia.furiafanapp.data.repository.GamificationRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides data-related dependencies, like the GamificationRepository.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideGamificationRepository(
        @ApplicationContext context: Context
    ): GamificationRepository = GamificationRepositoryImpl(context)
}
