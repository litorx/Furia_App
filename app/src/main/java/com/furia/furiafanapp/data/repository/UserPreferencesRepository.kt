package com.furia.furiafanapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.furia.furiafanapp.data.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val FAVORITE_GAMES = stringSetPreferencesKey("favorite_games")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                favoriteGames = preferences[PreferencesKeys.FAVORITE_GAMES] ?: emptySet(),
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
            )
        }

    suspend fun updateUserPreferences(userPreferences: UserPreferences) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FAVORITE_GAMES] = userPreferences.favoriteGames
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = userPreferences.notificationsEnabled
        }
    }
} 