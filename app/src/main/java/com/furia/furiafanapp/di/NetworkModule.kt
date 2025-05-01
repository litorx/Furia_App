package com.furia.furiafanapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Provides network-related dependencies: HttpClient and Json.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // Removed: Json and HttpClient providers; use AppModule for network dependencies
}
