package com.furia.furiafanapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import com.furia.furiafanapp.datastore.Fixtures
import com.furia.furiafanapp.datastore.FixturesSerializer

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    fun provideFixturesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Fixtures> = DataStoreFactory.create(
        serializer = FixturesSerializer,
        produceFile = { context.dataStoreFile("fixtures.pb") }
    )
}
