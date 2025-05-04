package com.furia.furiafanapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserVerificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun currentUserExists(): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return userExists(userId)
    }

    suspend fun userExists(userId: String): Boolean {
        val userDoc = firestore.collection("users").document(userId).get().await()
        return userDoc.exists()
    }

    suspend fun verifyCurrentUserOrLogout(): Boolean {
        if (!currentUserExists()) {
            auth.signOut()
            return false
        }
        return true
    }

    suspend fun cleanupDeletedUserData(userId: String) {
        if (!userExists(userId)) {
            firestore.collection("arenaStats").document(userId).delete().await()
            
            val bets = firestore.collection("bets")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            for (bet in bets.documents) {
                bet.reference.delete().await()
            }
        }
    }
}
