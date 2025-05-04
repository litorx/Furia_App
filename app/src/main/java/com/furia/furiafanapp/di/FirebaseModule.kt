package com.furia.furiafanapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.furia.furiafanapp.data.repository.ProfileRepository
import com.furia.furiafanapp.data.repository.ProfileRepositoryImpl
import com.furia.furiafanapp.data.repository.UserVerificationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideProfileRepository(firestore: FirebaseFirestore): ProfileRepository =
        ProfileRepositoryImpl(firestore)
        
    @Provides
    @Singleton
    fun provideUserVerificationRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): UserVerificationRepository = UserVerificationRepository(firestore, auth)
}
