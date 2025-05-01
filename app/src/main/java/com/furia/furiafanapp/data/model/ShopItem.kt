package com.furia.furiafanapp.data.model

data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val pointsCost: Long,
    val category: ShopCategory,
    val discountPercentage: Int = 0, // Para itens que são descontos
    val isExclusive: Boolean = false, // Itens exclusivos para usuários de alto ranking
    val isLimited: Boolean = false, // Edições limitadas
    val availableUntil: Long? = null // Timestamp para itens por tempo limitado
)

enum class ShopCategory {
    APPAREL, // Roupas e acessórios
    TICKETS, // Ingressos para eventos
    EXPERIENCES, // Experiências como conhecer o time
    DISCOUNTS, // Descontos na loja oficial
    DIGITAL, // Itens digitais como wallpapers, avatares
    COLLECTIBLES // Itens colecionáveis
}

// Modelo para representar um cupom gerado
data class ShopCoupon(
    val id: String,
    val userId: String,
    val itemId: String,
    val code: String,
    val createdAt: Long,
    val expiresAt: Long,
    val isUsed: Boolean = false,
    val usedAt: Long? = null
)

// Modelo para representar o histórico de compras do usuário
data class ShopPurchase(
    val id: String,
    val userId: String,
    val itemId: String,
    val itemName: String,
    val pointsCost: Long,
    val purchasedAt: Long,
    val couponId: String? = null
)

// Modelo para resultado de compra
data class PurchaseResult(
    val success: Boolean,
    val couponCode: String? = null,
    val errorMessage: String? = null
)
