package com.furia.furiafanapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositório responsável por verificar se um usuário ainda existe no banco de dados.
 * Isso é útil para garantir que contas excluídas não possam mais acessar o aplicativo
 * e que seus dados não apareçam mais no leaderboard.
 */
@Singleton
class UserVerificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    /**
     * Verifica se o usuário atual ainda existe no banco de dados.
     * @return true se o usuário existir, false caso contrário
     */
    suspend fun currentUserExists(): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return userExists(userId)
    }

    /**
     * Verifica se um usuário específico ainda existe no banco de dados.
     * @param userId ID do usuário a ser verificado
     * @return true se o usuário existir, false caso contrário
     */
    suspend fun userExists(userId: String): Boolean {
        val userDoc = firestore.collection("users").document(userId).get().await()
        return userDoc.exists()
    }

    /**
     * Verifica se o usuário atual ainda existe no banco de dados e, caso não exista,
     * realiza o logout do usuário.
     * @return true se o usuário existir, false caso contrário (e o logout foi realizado)
     */
    suspend fun verifyCurrentUserOrLogout(): Boolean {
        if (!currentUserExists()) {
            auth.signOut()
            return false
        }
        return true
    }

    /**
     * Limpa os dados de um usuário que não existe mais no banco de dados.
     * Isso inclui remover suas estatísticas de arena, apostas, etc.
     * @param userId ID do usuário a ser limpo
     */
    suspend fun cleanupDeletedUserData(userId: String) {
        // Verificar se o usuário realmente não existe
        if (!userExists(userId)) {
            // Remover estatísticas de arena
            firestore.collection("arenaStats").document(userId).delete().await()
            
            // Remover apostas do usuário
            val bets = firestore.collection("bets")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            for (bet in bets.documents) {
                bet.reference.delete().await()
            }
            
            // Remover outras referências ao usuário que possam existir
            // (como comentários, participações em eventos, etc.)
        }
    }
}
