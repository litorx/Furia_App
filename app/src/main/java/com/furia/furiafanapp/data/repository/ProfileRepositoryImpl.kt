package com.furia.furiafanapp.data.repository

import com.furia.furiafanapp.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.FieldValue
import android.util.Log
import kotlinx.coroutines.tasks.await

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProfileRepository {

    // Cache para o perfil do usuário atual
    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    
    override fun getUserProfile(userId: String): Flow<UserProfile> = callbackFlow {
        Log.d("ProfileRepository", "Iniciando observação do perfil do usuário: $userId")
        
        val docRef = firestore.collection("users").document(userId)
        val subscription: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ProfileRepository", "Erro ao observar perfil: ${error.message}")
                return@addSnapshotListener
            }
            
            if (snapshot == null || !snapshot.exists()) {
                Log.w("ProfileRepository", "Documento do usuário não existe: $userId")
                return@addSnapshotListener
            }
            
            val data = snapshot.data ?: return@addSnapshotListener
            Log.d("ProfileRepository", "Dados do perfil recebidos: $data")

            // build social links map from nested 'socialLinks' or flat fields
            val rawSocial = data["socialLinks"] as? Map<*, *>
            val socialMap = mutableMapOf<String, String>()
            rawSocial?.forEach { (k, v) -> socialMap[k.toString()] = v.toString() }
            (data["instagram"] as? String)?.let { socialMap["instagram"] = it }
            (data["twitch"] as? String)?.let { socialMap["twitch"] = it }
            (data["x"] as? String)?.let { socialMap["x"] = it }

            val points = (data["points"] as? Long) ?: 0L
            Log.d("ProfileRepository", "Pontos do usuário $userId: $points")
            
            val profile = UserProfile(
                id = snapshot.id,
                nickname = data["nickname"] as? String ?: "",
                photoUrl = data["photoUrl"] as? String ?: "",
                socialLinks = socialMap,
                points = points,
                favorites = (data["favorites"] as? List<String>) ?: emptyList()
            )
            
            // Atualiza o cache
            _currentUserProfile.value = profile
            
            trySend(profile)
        }
        awaitClose { 
            Log.d("ProfileRepository", "Fechando observação do perfil: $userId")
            subscription.remove() 
        }
    }

    override suspend fun addPoints(userId: String, points: Long) {
        try {
            Log.d("ProfileRepository", "Adicionando $points pontos para o usuário $userId")
            val docRef = firestore.collection("users").document(userId)
            
            // Verifica se o documento existe antes de tentar atualizar
            val document = docRef.get().await()
            if (!document.exists()) {
                Log.e("ProfileRepository", "Documento do usuário não existe: $userId")
                throw IllegalStateException("Usuário não encontrado no banco de dados")
            }
            
            // Obtém o valor atual de pontos
            val currentPoints = document.getLong("points") ?: 0L
            val newPoints = currentPoints + points
            
            Log.d("ProfileRepository", "Pontos atuais: $currentPoints, Novos pontos: $newPoints")
            
            // Atualiza os pontos no Firestore usando set com merge em vez de update
            val updates = hashMapOf<String, Any>(
                "points" to newPoints
            )
            
            docRef.set(updates, SetOptions.merge()).await()
            Log.d("ProfileRepository", "Pontos atualizados com sucesso para $newPoints")
            
            // Atualiza o cache local também para refletir imediatamente
            _currentUserProfile.value?.let { currentProfile ->
                if (currentProfile.id == userId) {
                    _currentUserProfile.value = currentProfile.copy(points = newPoints)
                    Log.d("ProfileRepository", "Cache local atualizado: $newPoints pontos")
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Erro ao adicionar pontos: ${e.message}", e)
            throw e
        }
    }

    override fun getLeaderboard(): Flow<List<UserProfile>> = callbackFlow {
        Log.d("ProfileRepository", "Iniciando observação do leaderboard")
        
        val query = firestore.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(10)
        val subscription: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ProfileRepository", "Erro ao observar leaderboard: ${error.message}")
                return@addSnapshotListener
            }
            
            if (snapshot == null) {
                Log.w("ProfileRepository", "Snapshot do leaderboard é nulo")
                return@addSnapshotListener
            }
            
            val list = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null

                // build social links map for leaderboard
                val rawSocial2 = data["socialLinks"] as? Map<*, *>
                val socialMap2 = mutableMapOf<String, String>()
                rawSocial2?.forEach { (k, v) -> socialMap2[k.toString()] = v.toString() }
                (data["instagram"] as? String)?.let { socialMap2["instagram"] = it }
                (data["twitch"] as? String)?.let { socialMap2["twitch"] = it }
                (data["x"] as? String)?.let { socialMap2["x"] = it }

                val points = (data["points"] as? Long) ?: 0L
                
                UserProfile(
                    id = doc.id,
                    nickname = data["nickname"] as? String ?: "",
                    photoUrl = data["photoUrl"] as? String ?: "",
                    socialLinks = socialMap2,
                    points = points,
                    favorites = (data["favorites"] as? List<String>) ?: emptyList()
                )
            }
            
            Log.d("ProfileRepository", "Leaderboard atualizado: ${list.size} usuários")
            trySend(list)
        }
        awaitClose { 
            Log.d("ProfileRepository", "Fechando observação do leaderboard")
            subscription.remove() 
        }
    }
    
    override suspend fun awardPoints(points: Long) {
        try {
            // Obter o ID do usuário atual
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                ?: throw IllegalStateException("Usuário não está logado")
            
            Log.d("ProfileRepository", "Premiando usuário $userId com $points pontos")
            
            // Adicionar pontos ao usuário atual
            addPoints(userId, points)
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Erro ao premiar pontos: ${e.message}", e)
            throw e
        }
    }
}
