package com.furia.furiafanapp.data.repository

import android.util.Log
import com.furia.furiafanapp.data.model.ShopCategory
import com.furia.furiafanapp.data.model.ShopCoupon
import com.furia.furiafanapp.data.model.ShopItem
import com.furia.furiafanapp.data.model.ShopPurchase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ShopRepository {

    private val shopCollection = firestore.collection("shop_items")
    private val couponsCollection = firestore.collection("shop_coupons")
    private val purchasesCollection = firestore.collection("shop_purchases")
    
    override fun getAllItems(): Flow<List<ShopItem>> = callbackFlow {
        val listener = shopCollection
            .orderBy("pointsCost", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ShopRepository", "Error getting shop items", error)
                    return@addSnapshotListener
                }
                
                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val items = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.id
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val description = doc.getString("description") ?: ""
                        val imageUrl = doc.getString("imageUrl") ?: ""
                        val pointsCost = doc.getLong("pointsCost") ?: 0L
                        val categoryStr = doc.getString("category") ?: ShopCategory.APPAREL.name
                        val category = try {
                            ShopCategory.valueOf(categoryStr)
                        } catch (e: Exception) {
                            ShopCategory.APPAREL
                        }
                        val discountPercentage = doc.getLong("discountPercentage")?.toInt() ?: 0
                        val isExclusive = doc.getBoolean("isExclusive") ?: false
                        val isLimited = doc.getBoolean("isLimited") ?: false
                        val availableUntil = doc.getLong("availableUntil")
                        
                        ShopItem(
                            id = id,
                            name = name,
                            description = description,
                            imageUrl = imageUrl,
                            pointsCost = pointsCost,
                            category = category,
                            discountPercentage = discountPercentage,
                            isExclusive = isExclusive,
                            isLimited = isLimited,
                            availableUntil = availableUntil
                        )
                    } catch (e: Exception) {
                        Log.e("ShopRepository", "Error parsing shop item", e)
                        null
                    }
                }
                
                trySend(items)
            }
            
        awaitClose { listener.remove() }
    }
    
    override fun getItemsByCategory(category: String): Flow<List<ShopItem>> = callbackFlow {
        val listener = shopCollection
            .whereEqualTo("category", category)
            .orderBy("pointsCost", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ShopRepository", "Error getting items by category", error)
                    return@addSnapshotListener
                }
                
                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val items = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.id
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val description = doc.getString("description") ?: ""
                        val imageUrl = doc.getString("imageUrl") ?: ""
                        val pointsCost = doc.getLong("pointsCost") ?: 0L
                        val categoryStr = doc.getString("category") ?: ShopCategory.APPAREL.name
                        val itemCategory = try {
                            ShopCategory.valueOf(categoryStr)
                        } catch (e: Exception) {
                            ShopCategory.APPAREL
                        }
                        val discountPercentage = doc.getLong("discountPercentage")?.toInt() ?: 0
                        val isExclusive = doc.getBoolean("isExclusive") ?: false
                        val isLimited = doc.getBoolean("isLimited") ?: false
                        val availableUntil = doc.getLong("availableUntil")
                        
                        ShopItem(
                            id = id,
                            name = name,
                            description = description,
                            imageUrl = imageUrl,
                            pointsCost = pointsCost,
                            category = itemCategory,
                            discountPercentage = discountPercentage,
                            isExclusive = isExclusive,
                            isLimited = isLimited,
                            availableUntil = availableUntil
                        )
                    } catch (e: Exception) {
                        Log.e("ShopRepository", "Error parsing shop item", e)
                        null
                    }
                }
                
                trySend(items)
            }
            
        awaitClose { listener.remove() }
    }
    
    override fun getExclusiveItems(userRank: Int): Flow<List<ShopItem>> = callbackFlow {
        val listener = shopCollection
            .whereEqualTo("isExclusive", true)
            .orderBy("pointsCost", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ShopRepository", "Error getting exclusive items", error)
                    return@addSnapshotListener
                }
                
                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val items = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.id
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val description = doc.getString("description") ?: ""
                        val imageUrl = doc.getString("imageUrl") ?: ""
                        val pointsCost = doc.getLong("pointsCost") ?: 0L
                        val categoryStr = doc.getString("category") ?: ShopCategory.APPAREL.name
                        val category = try {
                            ShopCategory.valueOf(categoryStr)
                        } catch (e: Exception) {
                            ShopCategory.APPAREL
                        }
                        val discountPercentage = doc.getLong("discountPercentage")?.toInt() ?: 0
                        val isExclusive = doc.getBoolean("isExclusive") ?: false
                        val isLimited = doc.getBoolean("isLimited") ?: false
                        val availableUntil = doc.getLong("availableUntil")
                        
                        ShopItem(
                            id = id,
                            name = name,
                            description = description,
                            imageUrl = imageUrl,
                            pointsCost = pointsCost,
                            category = category,
                            discountPercentage = discountPercentage,
                            isExclusive = isExclusive,
                            isLimited = isLimited,
                            availableUntil = availableUntil
                        )
                    } catch (e: Exception) {
                        Log.e("ShopRepository", "Error parsing shop item", e)
                        null
                    }
                }
                
                trySend(items)
            }
            
        awaitClose { listener.remove() }
    }
    
    override fun getFeaturedItems(): Flow<List<ShopItem>> = callbackFlow {
        val listener = shopCollection
            .whereEqualTo("isFeatured", true)
            .orderBy("pointsCost", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ShopRepository", "Error getting featured items", error)
                    return@addSnapshotListener
                }
                
                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val items = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.id
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val description = doc.getString("description") ?: ""
                        val imageUrl = doc.getString("imageUrl") ?: ""
                        val pointsCost = doc.getLong("pointsCost") ?: 0L
                        val categoryStr = doc.getString("category") ?: ShopCategory.APPAREL.name
                        val category = try {
                            ShopCategory.valueOf(categoryStr)
                        } catch (e: Exception) {
                            ShopCategory.APPAREL
                        }
                        val discountPercentage = doc.getLong("discountPercentage")?.toInt() ?: 0
                        val isExclusive = doc.getBoolean("isExclusive") ?: false
                        val isLimited = doc.getBoolean("isLimited") ?: false
                        val availableUntil = doc.getLong("availableUntil")
                        
                        ShopItem(
                            id = id,
                            name = name,
                            description = description,
                            imageUrl = imageUrl,
                            pointsCost = pointsCost,
                            category = category,
                            discountPercentage = discountPercentage,
                            isExclusive = isExclusive,
                            isLimited = isLimited,
                            availableUntil = availableUntil
                        )
                    } catch (e: Exception) {
                        Log.e("ShopRepository", "Error parsing shop item", e)
                        null
                    }
                }
                
                trySend(items)
            }
            
        awaitClose { listener.remove() }
    }
    
    override suspend fun getItemDetails(itemId: String): ShopItem? {
        return try {
            val doc = shopCollection.document(itemId).get().await()
            if (!doc.exists()) return null
            
            val name = doc.getString("name") ?: return null
            val description = doc.getString("description") ?: ""
            val imageUrl = doc.getString("imageUrl") ?: ""
            val pointsCost = doc.getLong("pointsCost") ?: 0L
            val categoryStr = doc.getString("category") ?: ShopCategory.APPAREL.name
            val category = try {
                ShopCategory.valueOf(categoryStr)
            } catch (e: Exception) {
                ShopCategory.APPAREL
            }
            val discountPercentage = doc.getLong("discountPercentage")?.toInt() ?: 0
            val isExclusive = doc.getBoolean("isExclusive") ?: false
            val isLimited = doc.getBoolean("isLimited") ?: false
            val availableUntil = doc.getLong("availableUntil")
            
            ShopItem(
                id = doc.id,
                name = name,
                description = description,
                imageUrl = imageUrl,
                pointsCost = pointsCost,
                category = category,
                discountPercentage = discountPercentage,
                isExclusive = isExclusive,
                isLimited = isLimited,
                availableUntil = availableUntil
            )
        } catch (e: Exception) {
            Log.e("ShopRepository", "Error getting item details", e)
            null
        }
    }
    
    override suspend fun purchaseItem(userId: String, itemId: String, pointsCost: Long): Result<ShopCoupon> {
        return try {
            // Verificar se o usuário tem pontos suficientes
            if (!hasEnoughPoints(userId, pointsCost)) {
                return Result.failure(Exception("Pontos insuficientes"))
            }
            
            // Obter detalhes do item
            val item = getItemDetails(itemId) ?: return Result.failure(Exception("Item não encontrado"))
            
            // Verificar se o item é exclusivo e se o usuário tem ranking suficiente
            if (item.isExclusive) {
                val requiredRank = 3 // Rank mínimo para itens exclusivos (pode ser ajustado)
                if (!hasRequiredRank(userId, requiredRank)) {
                    return Result.failure(Exception("Ranking insuficiente para este item exclusivo"))
                }
            }
            
            // Deduzir pontos do usuário
            firestore.runTransaction { transaction ->
                val userRef = firestore.collection("users").document(userId)
                val userSnapshot = transaction.get(userRef)
                val currentPoints = userSnapshot.getLong("points") ?: 0L
                
                if (currentPoints < pointsCost) {
                    throw Exception("Pontos insuficientes")
                }
                
                transaction.update(userRef, "points", currentPoints - pointsCost)
            }.await()
            
            // Gerar código de cupom
            val couponCode = generateCouponCode()
            val currentTime = System.currentTimeMillis()
            val expirationTime = currentTime + (30L * 24L * 60L * 60L * 1000L) // 30 dias
            
            // Criar cupom
            val couponId = UUID.randomUUID().toString()
            val coupon = ShopCoupon(
                id = couponId,
                userId = userId,
                itemId = itemId,
                code = couponCode,
                createdAt = currentTime,
                expiresAt = expirationTime
            )
            
            // Salvar cupom no Firestore
            couponsCollection.document(couponId).set(coupon).await()
            
            // Registrar a compra
            val purchaseId = UUID.randomUUID().toString()
            val purchase = ShopPurchase(
                id = purchaseId,
                userId = userId,
                itemId = itemId,
                itemName = item.name,
                pointsCost = pointsCost,
                purchasedAt = currentTime,
                couponId = couponId
            )
            
            purchasesCollection.document(purchaseId).set(purchase).await()
            
            Result.success(coupon)
        } catch (e: Exception) {
            Log.e("ShopRepository", "Error purchasing item", e)
            Result.failure(e)
        }
    }
    
    override fun getUserPurchaseHistory(userId: String): Flow<List<ShopPurchase>> = callbackFlow {
        val listener = purchasesCollection
            .whereEqualTo("userId", userId)
            .orderBy("purchasedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ShopRepository", "Error getting purchase history", error)
                    return@addSnapshotListener
                }
                
                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val purchases = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.id
                        val itemId = doc.getString("itemId") ?: return@mapNotNull null
                        val itemName = doc.getString("itemName") ?: ""
                        val pointsCost = doc.getLong("pointsCost") ?: 0L
                        val purchasedAt = doc.getLong("purchasedAt") ?: 0L
                        val couponId = doc.getString("couponId")
                        
                        ShopPurchase(
                            id = id,
                            userId = userId,
                            itemId = itemId,
                            itemName = itemName,
                            pointsCost = pointsCost,
                            purchasedAt = purchasedAt,
                            couponId = couponId
                        )
                    } catch (e: Exception) {
                        Log.e("ShopRepository", "Error parsing purchase", e)
                        null
                    }
                }
                
                trySend(purchases)
            }
            
        awaitClose { listener.remove() }
    }
    
    override suspend fun hasEnoughPoints(userId: String, pointsCost: Long): Boolean {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val currentPoints = userDoc.getLong("points") ?: 0L
            currentPoints >= pointsCost
        } catch (e: Exception) {
            Log.e("ShopRepository", "Error checking points", e)
            false
        }
    }
    
    override suspend fun hasRequiredRank(userId: String, requiredRank: Int): Boolean {
        return try {
            // Obter a posição do usuário no ranking
            val users = firestore.collection("users")
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()
                .documents
                .mapIndexedNotNull { index, doc ->
                    if (doc.id == userId) index + 1 else null
                }
                .firstOrNull() ?: Int.MAX_VALUE
            
            // Verificar se o ranking do usuário é menor ou igual ao requerido
            // (menor é melhor, 1 é o primeiro lugar)
            users <= requiredRank
        } catch (e: Exception) {
            Log.e("ShopRepository", "Error checking rank", e)
            false
        }
    }
    
    override suspend fun verifyCoupon(couponCode: String): ShopCoupon? {
        return try {
            val snapshot = couponsCollection
                .whereEqualTo("code", couponCode)
                .get()
                .await()
            
            if (snapshot.isEmpty) return null
            
            val doc = snapshot.documents.first()
            val id = doc.id
            val userId = doc.getString("userId") ?: return null
            val itemId = doc.getString("itemId") ?: return null
            val code = doc.getString("code") ?: return null
            val createdAt = doc.getLong("createdAt") ?: 0L
            val expiresAt = doc.getLong("expiresAt") ?: 0L
            val isUsed = doc.getBoolean("isUsed") ?: false
            val usedAt = doc.getLong("usedAt")
            
            // Verificar se o cupom já foi usado
            if (isUsed) return null
            
            // Verificar se o cupom expirou
            val currentTime = System.currentTimeMillis()
            if (currentTime > expiresAt) return null
            
            ShopCoupon(
                id = id,
                userId = userId,
                itemId = itemId,
                code = code,
                createdAt = createdAt,
                expiresAt = expiresAt,
                isUsed = isUsed,
                usedAt = usedAt
            )
        } catch (e: Exception) {
            Log.e("ShopRepository", "Error verifying coupon", e)
            null
        }
    }
    
    override suspend fun markCouponAsUsed(couponId: String): Boolean {
        return try {
            val currentTime = System.currentTimeMillis()
            couponsCollection.document(couponId)
                .update(
                    mapOf(
                        "isUsed" to true,
                        "usedAt" to currentTime
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            Log.e("ShopRepository", "Error marking coupon as used", e)
            false
        }
    }
    
    override fun getUserPoints(userId: String): Flow<Long> = callbackFlow {
        val userRef = firestore.collection("users").document(userId)
        
        val listener = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            if (snapshot != null && snapshot.exists()) {
                val points = snapshot.getLong("points") ?: 0L
                trySend(points)
            } else {
                trySend(0L)
            }
        }
        
        awaitClose { listener.remove() }
    }
    
    private fun generateCouponCode(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..12)
            .map { allowedChars.random() }
            .joinToString("")
            .chunked(4)
            .joinToString("-")
    }
}
