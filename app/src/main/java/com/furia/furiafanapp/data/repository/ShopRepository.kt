package com.furia.furiafanapp.data.repository

import com.furia.furiafanapp.data.model.ShopItem
import com.furia.furiafanapp.data.model.ShopCoupon
import com.furia.furiafanapp.data.model.ShopPurchase
import kotlinx.coroutines.flow.Flow

interface ShopRepository {
    // Obter todos os itens da loja
    fun getAllItems(): Flow<List<ShopItem>>
    
    // Obter itens por categoria
    fun getItemsByCategory(category: String): Flow<List<ShopItem>>
    
    // Obter itens exclusivos (para usuários de alto ranking)
    fun getExclusiveItems(userRank: Int): Flow<List<ShopItem>>
    
    // Obter itens em destaque
    fun getFeaturedItems(): Flow<List<ShopItem>>
    
    // Obter detalhes de um item específico
    suspend fun getItemDetails(itemId: String): ShopItem?
    
    // Comprar um item com pontos
    suspend fun purchaseItem(userId: String, itemId: String, pointsCost: Long): Result<ShopCoupon>
    
    // Obter histórico de compras do usuário
    fun getUserPurchaseHistory(userId: String): Flow<List<ShopPurchase>>
    
    // Verificar se o usuário tem pontos suficientes
    suspend fun hasEnoughPoints(userId: String, pointsCost: Long): Boolean
    
    // Verificar se o usuário tem ranking suficiente para itens exclusivos
    suspend fun hasRequiredRank(userId: String, requiredRank: Int): Boolean
    
    // Verificar cupom
    suspend fun verifyCoupon(couponCode: String): ShopCoupon?
    
    // Marcar cupom como usado
    suspend fun markCouponAsUsed(couponId: String): Boolean
    
    // Obter pontos do usuário
    fun getUserPoints(userId: String): Flow<Long>
}
