package com.furia.furiafanapp.data.chat

import com.furia.furiafanapp.data.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore implementation of ChatRepository.
 */
@Singleton
class FirestoreChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {
    override fun getMessages(matchId: String): Flow<List<ChatMessage>> = callbackFlow {
        val coll = firestore.collection("matches")
            .document(matchId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
        val sub = coll.addSnapshotListener { snap, err ->
            if (err != null) {
                // Ignora erros do Firestore para evitar crash
                return@addSnapshotListener
            } else {
                val items = snap?.documents
                    ?.mapNotNull { it.toObject(ChatMessage::class.java)?.copy(id = it.id) }
                    ?: emptyList()
                trySend(items).isSuccess
            }
        }
        awaitClose { sub.remove() }
    }

    override suspend fun sendMessage(matchId: String, message: ChatMessage) {
        firestore.collection("matches")
            .document(matchId)
            .collection("messages")
            .add(message)
    }
}
