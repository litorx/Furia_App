package com.furia.furiafanapp.di

import android.content.Context
import com.furia.furiafanapp.BuildConfig
import com.furia.furiafanapp.data.repository.MatchRepository
import com.furia.furiafanapp.data.repository.MatchRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Chave de API PandaScore (normalmente seria armazenada em BuildConfig ou variáveis de ambiente)
    private const val PANDASCORE_API_KEY = "l83LuRUeYQDDYMbKMVUpJlZiT1yqcaUcaW_Xv2fvN_QXHUhiF2o"
    // Chave de API OpenAI definida em local.properties e exposta via BuildConfig
    private val OPENAI_API_KEY: String = BuildConfig.OPENAI_API_KEY

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json)
            }
            
            // Configurar cabeçalhos padrão para todas as requisições
            install(DefaultRequest) {
                headers {
                    append("Authorization", "Bearer $PANDASCORE_API_KEY")
                }
            }
        }
    }

    @Provides
    @Singleton
    @Named("openai")
    fun provideOpenAiHttpClient(json: Json): HttpClient {
        return HttpClient(OkHttp) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }
            install(ContentNegotiation) { json(json) }
            install(DefaultRequest) {
                headers {
                    append("Authorization", "Bearer $OPENAI_API_KEY")
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideMatchRepository(
        @ApplicationContext context: Context,
        httpClient: HttpClient,
        json: Json
    ): MatchRepository {
        return MatchRepositoryImpl(context, httpClient, json)
    }
}