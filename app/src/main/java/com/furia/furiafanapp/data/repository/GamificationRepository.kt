package com.furia.furiafanapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.furia.furiafanapp.util.KEY_LAST_SYNC
import com.furia.furiafanapp.util.KEY_POINTS
import com.furia.furiafanapp.util.gamificationPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

interface GamificationRepository {
    fun getPoints(): Flow<Int>
    fun getBadge(): Flow<String>
    suspend fun awardPoints(points: Int)
    suspend fun syncIfNeeded()
}

@Singleton
class GamificationRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : GamificationRepository {
    private val dataStore: DataStore<Preferences> = context.gamificationPrefs
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun getPoints(): Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_POINTS] ?: 0
    }

    override fun getBadge(): Flow<String> = getPoints().map { points ->
        when {
            points < 100 -> "Fan"
            points < 200 -> "SuperFan"
            else -> "UltraFan"
        }
    }

    override suspend fun awardPoints(points: Int) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_POINTS] ?: 0
            prefs[KEY_POINTS] = current + points
        }
    }

    override suspend fun syncIfNeeded() {
        val prefs = dataStore.data.first()
        val lastSync = prefs[KEY_LAST_SYNC] ?: ""
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val points = prefs[KEY_POINTS] ?: 0
        if (lastSync != today && points > 0) {
            val uid = auth.currentUser?.uid ?: return
            firestore.collection("users").document(uid)
                .update(mapOf("points" to FieldValue.increment(points.toLong())))
            dataStore.edit { p ->
                p[KEY_LAST_SYNC] = today
                p[KEY_POINTS] = 0
            }
        }
    }
}
