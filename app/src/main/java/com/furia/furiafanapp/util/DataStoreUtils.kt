package com.furia.furiafanapp.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Preferences DataStore para flags de Onboarding e Gamificação
val Context.onboardingPrefs by preferencesDataStore(name = "onboarding_prefs")
val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")

// DataStore Preferences para Gamificação
val Context.gamificationPrefs by preferencesDataStore(name = "gamification_prefs")
val KEY_POINTS = intPreferencesKey("points")
val KEY_LAST_SYNC = stringPreferencesKey("last_sync_date")

// DataStore Preferences para Settings
val Context.settingsPrefs by preferencesDataStore(name = "settings_prefs")
val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
val KEY_BADGE_NOTIFICATIONS_ENABLED = booleanPreferencesKey("badge_notifications_enabled")
