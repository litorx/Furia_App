package com.furia.furiafanapp.utils

import android.util.Log
import com.furia.furiafanapp.data.model.ShopCategory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopDataInitializer @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun initializeShopData() {
        try {
            // Verificar se já existem itens na loja
            val existingItems = firestore.collection("shop_items").get().await()
            if (!existingItems.isEmpty) {
                Log.d("ShopDataInitializer", "Loja já possui ${existingItems.size()} itens. Pulando inicialização.")
                return
            }

            // Adicionar itens à loja
            val shopItems = createShopItems()
            
            // Adicionar cada item ao Firestore
            shopItems.forEach { item ->
                firestore.collection("shop_items").document(item["id"] as String)
                    .set(item)
                    .await()
            }
            
            Log.d("ShopDataInitializer", "Loja inicializada com ${shopItems.size} itens.")
        } catch (e: Exception) {
            Log.e("ShopDataInitializer", "Erro ao inicializar loja", e)
        }
    }
    
    private fun createShopItems(): List<Map<String, Any>> {
        val items = mutableListOf<Map<String, Any>>()
        
        // APPAREL - Roupas e acessórios
        items.add(mapOf(
            "id" to "apparel_1",
            "name" to "Camisa Oficial FURIA 2025",
            "description" to "Camisa oficial do time FURIA para a temporada 2025. Material de alta qualidade com tecnologia de respirabilidade.",
            "imageUrl" to "https://furiashop.com.br/cdn/shop/files/JERSEY_FRENTE_FURIA_2023_800x.jpg",
            "pointsCost" to 5000L,
            "category" to ShopCategory.APPAREL.name,
            "discountPercentage" to 0,
            "isExclusive" to false,
            "isLimited" to false,
            "isFeatured" to true
        ))
        
        items.add(mapOf(
            "id" to "apparel_2",
            "name" to "Moletom FURIA Black",
            "description" to "Moletom oficial FURIA na cor preta com capuz. Perfeito para os dias mais frios.",
            "imageUrl" to "https://furiashop.com.br/cdn/shop/products/moletom-furia-preto-2022-1_800x.jpg",
            "pointsCost" to 3500L,
            "category" to ShopCategory.APPAREL.name,
            "discountPercentage" to 0,
            "isExclusive" to false,
            "isLimited" to false,
            "isFeatured" to false
        ))
        
        items.add(mapOf(
            "id" to "apparel_3",
            "name" to "Boné FURIA Snapback",
            "description" to "Boné oficial da FURIA modelo snapback. Ajustável e confortável.",
            "imageUrl" to "https://furiashop.com.br/cdn/shop/products/bone-furia-preto-2022-1_800x.jpg",
            "pointsCost" to 1800L,
            "category" to ShopCategory.APPAREL.name,
            "discountPercentage" to 0,
            "isExclusive" to false,
            "isLimited" to false,
            "isFeatured" to false
        ))
        
        items.add(mapOf(
            "id" to "apparel_4",
            "name" to "Camisa Pro Player Edição Limitada",
            "description" to "Camisa exclusiva usada pelos jogadores profissionais em campeonatos. Edição limitada com autógrafos.",
            "imageUrl" to "https://furiashop.com.br/cdn/shop/products/camisa-furia-preta-2022-1_800x.jpg",
            "pointsCost" to 8000L,
            "category" to ShopCategory.APPAREL.name,
            "discountPercentage" to 0,
            "isExclusive" to true,
            "isLimited" to true,
            "isFeatured" to true
        ))
        
        // TICKETS - Ingressos para eventos
        items.add(mapOf(
            "id" to "tickets_1",
            "name" to "Ingresso Major CS2 - Categoria Premium",
            "description" to "Ingresso para o próximo Major de CS2 com a FURIA. Inclui acesso à área VIP e meet & greet com os jogadores.",
            "imageUrl" to "https://s2.glbimg.com/4Ek8CnZSuYxRQQJKGxqWKuYmRIw=/0x0:1280x720/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2023/7/a/MNfOxASxA3AAGk2FRPww/furia-blast.jpg",
            "pointsCost" to 7500L,
            "category" to ShopCategory.TICKETS.name,
            "discountPercentage" to 0,
            "isExclusive" to true,
            "isLimited" to true,
            "isFeatured" to true
        ))
        
        items.add(mapOf(
            "id" to "tickets_2",
            "name" to "Ingresso CBLOL - Final",
            "description" to "Ingresso para a final do CBLOL com a FURIA. Categoria padrão com ótima visibilidade.",
            "imageUrl" to "https://s2.glbimg.com/ySUzWKcLg5gYe_iLXgWfHEFe15I=/0x0:1200x675/984x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_bc8228b6673f488aa253bbcb03c80ec5/internal_photos/bs/2022/U/A/8oB9BdTAqAZRGPBgB6Aw/furia-cblol.jpg",
            "pointsCost" to 4500L,
            "category" to ShopCategory.TICKETS.name,
            "discountPercentage" to 0,
            "isExclusive" to false,
            "isLimited" to true,
            "isFeatured" to false
        ))
        
        items.add(mapOf(
            "id" to "tickets_3",
            "name" to "Ingresso Rainbow Six - Six Major",
            "description" to "Ingresso para o Six Major com a FURIA. Categoria padrão.",
            "imageUrl" to "https://s2.glbimg.com/ySLtXLfYHQiEBiEEiL9-ZLYdTPo=/0x0:1200x800/984x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_bc8228b6673f488aa253bbcb03c80ec5/internal_photos/bs/2022/9/P/oBDJDLQBiAg77AAgA0JQ/furia-r6.jpg",
            "pointsCost" to 3800L,
            "category" to ShopCategory.TICKETS.name,
            "discountPercentage" to 0,
            "isExclusive" to false,
            "isLimited" to true,
            "isFeatured" to false
        ))
        
        // EXPERIENCES - Experiências exclusivas
        items.add(mapOf(
            "id" to "experiences_1",
            "name" to "Visita à Gaming House FURIA",
            "description" to "Experiência exclusiva de visitar a Gaming House da FURIA e conhecer os jogadores. Inclui tour completo e sessão de fotos.",
            "imageUrl" to "https://s2.glbimg.com/Ks8YnwWQQUdkicZIj9YKUzIuq_k=/0x0:1280x720/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2020/B/A/Abq4wDRaA7tGLCC1zUGw/furia-gaming-house.jpg",
            "pointsCost" to 10000L,
            "category" to ShopCategory.EXPERIENCES.name,
            "discountPercentage" to 0,
            "isExclusive" to true,
            "isLimited" to true,
            "isFeatured" to true
        ))
        
        items.add(mapOf(
            "id" to "experiences_2",
            "name" to "Treino com Pro Player",
            "description" to "Sessão de treino online de 1 hora com um jogador profissional da FURIA. Receba dicas e melhore seu gameplay.",
            "imageUrl" to "https://s2.glbimg.com/7Yd9-c-LCQgqnvLqKJGHiQYWmPs=/0x0:1280x720/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2022/A/A/NfTgBVRGaB3gA9AAgOZA/furia-cs.jpg",
            "pointsCost" to 6000L,
            "category" to ShopCategory.EXPERIENCES.name,
            "discountPercentage" to 0,
            "isExclusive" to true,
            "isLimited" to false,
            "isFeatured" to false
        ))
        
        items.add(mapOf(
            "id" to "experiences_3",
            "name" to "Jantar com Time FURIA",
            "description" to "Experiência única de jantar com o time da FURIA após um evento. Converse com os jogadores em um ambiente descontraído.",
            "imageUrl" to "https://s2.glbimg.com/wYRR3-zAGqLCNnqYYN9i7sFZ3xM=/0x0:1280x720/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2022/B/A/AbqnBZQAqABfA9A9BASA/furia-time.jpg",
            "pointsCost" to 12000L,
            "category" to ShopCategory.EXPERIENCES.name,
            "discountPercentage" to 0,
            "isExclusive" to true,
            "isLimited" to true,
            "isFeatured" to false
        ))
        
        // DISCOUNTS - Descontos na loja oficial
        items.add(mapOf(
            "id" to "discounts_1",
            "name" to "Cupom 10% FURIA Shop",
            "description" to "Cupom de 10% de desconto em qualquer produto da loja oficial da FURIA. Válido por 30 dias.",
            "imageUrl" to "https://furiashop.com.br/cdn/shop/files/JERSEY_FRENTE_FURIA_2023_800x.jpg",
            "pointsCost" to 800L,
            "category" to ShopCategory.DISCOUNTS.name,
            "discountPercentage" to 10,
            "isExclusive" to false,
            "isLimited" to false,
            "isFeatured" to false
        ))
        
        items.add(mapOf(
            "id" to "discounts_2",
            "name" to "Cupom 25% FURIA Shop",
            "description" to "Cupom de 25% de desconto em qualquer produto da loja oficial da FURIA. Válido por 30 dias.",
            "imageUrl" to "https://furiashop.com.br/cdn/shop/files/JERSEY_FRENTE_FURIA_2023_800x.jpg",
            "pointsCost" to 2000L,
            "category" to ShopCategory.DISCOUNTS.name,
            "discountPercentage" to 25,
            "isExclusive" to false,
            "isLimited" to false,
            "isFeatured" to true
        ))
        
        items.add(mapOf(
            "id" to "discounts_3",
            "name" to "Cupom 50% FURIA Shop",
            "description" to "Cupom de 50% de desconto em qualquer produto da loja oficial da FURIA. Válido por 30 dias. Exclusivo para fãs de elite.",
            "imageUrl" to "https://furiashop.com.br/cdn/shop/files/JERSEY_FRENTE_FURIA_2023_800x.jpg",
            "pointsCost" to 5000L,
            "category" to ShopCategory.DISCOUNTS.name,
            "discountPercentage" to 50,
            "isExclusive" to true,
            "isLimited" to false,
            "isFeatured" to true
        ))
        
        // DIGITAL - Itens digitais
        items.add(mapOf(
            "id" to "digital_1",
            "name" to "Pacote de Wallpapers FURIA",
            "description" to "Pacote com 10 wallpapers exclusivos da FURIA em alta resolução para desktop e mobile.",
            "imageUrl" to "https://s2.glbimg.com/ySLtXLfYHQiEBiEEiL9-ZLYdTPo=/0x0:1200x800/984x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_bc8228b6673f488aa253bbcb03c80ec5/internal_photos/bs/2022/9/P/oBDJDLQBiAg77AAgA0JQ/furia-r6.jpg",
            "pointsCost" to 300L,
            "category" to ShopCategory.DIGITAL.name,
            "discountPercentage" to 0,
            "isExclusive" to false,
            "isLimited" to false,
            "isFeatured" to false
        ))
        
        items.add(mapOf(
            "id" to "digital_2",
            "name" to "Avatar Exclusivo FURIA",
            "description" to "Avatar exclusivo da FURIA para uso em redes sociais e plataformas de jogos.",
            "imageUrl" to "https://s2.glbimg.com/4Ek8CnZSuYxRQQJKGxqWKuYmRIw=/0x0:1280x720/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2023/7/a/MNfOxASxA3AAGk2FRPww/furia-blast.jpg",
            "pointsCost" to 500L,
            "category" to ShopCategory.DIGITAL.name,
            "discountPercentage" to 0,
            "isExclusive" to false,
            "isLimited" to false,
            "isFeatured" to false
        ))
        
        items.add(mapOf(
            "id" to "digital_3",
            "name" to "Pack de Emotes FURIA",
            "description" to "Pacote com 5 emotes exclusivos da FURIA para uso em plataformas de streaming.",
            "imageUrl" to "https://s2.glbimg.com/7Yd9-c-LCQgqnvLqKJGHiQYWmPs=/0x0:1280x720/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2022/A/A/NfTgBVRGaB3gA9AAgOZA/furia-cs.jpg",
            "pointsCost" to 800L,
            "category" to ShopCategory.DIGITAL.name,
            "discountPercentage" to 0,
            "isExclusive" to false,
            "isLimited" to false,
            "isFeatured" to false
        ))
        
        // COLLECTIBLES - Itens colecionáveis
        items.add(mapOf(
            "id" to "collectibles_1",
            "name" to "Miniatura Jogador FURIA",
            "description" to "Miniatura colecionável de um jogador da FURIA. Peça de alta qualidade com 15cm de altura.",
            "imageUrl" to "https://s2.glbimg.com/Ks8YnwWQQUdkicZIj9YKUzIuq_k=/0x0:1280x720/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2020/B/A/Abq4wDRaA7tGLCC1zUGw/furia-gaming-house.jpg",
            "pointsCost" to 2500L,
            "category" to ShopCategory.COLLECTIBLES.name,
            "discountPercentage" to 0,
            "isExclusive" to false,
            "isLimited" to true,
            "isFeatured" to false
        ))
        
        items.add(mapOf(
            "id" to "collectibles_2",
            "name" to "Poster Autografado FURIA",
            "description" to "Poster oficial da FURIA autografado por todo o time. Tamanho A2 em papel de alta qualidade.",
            "imageUrl" to "https://s2.glbimg.com/wYRR3-zAGqLCNnqYYN9i7sFZ3xM=/0x0:1280x720/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2022/B/A/AbqnBZQAqABfA9A9BASA/furia-time.jpg",
            "pointsCost" to 3000L,
            "category" to ShopCategory.COLLECTIBLES.name,
            "discountPercentage" to 0,
            "isExclusive" to false,
            "isLimited" to true,
            "isFeatured" to false
        ))
        
        items.add(mapOf(
            "id" to "collectibles_3",
            "name" to "Mousepad FURIA Edição Campeão",
            "description" to "Mousepad edição especial comemorativa de campeonato. Tamanho XL com bordas costuradas.",
            "imageUrl" to "https://s2.glbimg.com/4Ek8CnZSuYxRQQJKGxqWKuYmRIw=/0x0:1280x720/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_08fbf48bc0524877943fe86e43087e7a/internal_photos/bs/2023/7/a/MNfOxASxA3AAGk2FRPww/furia-blast.jpg",
            "pointsCost" to 1800L,
            "category" to ShopCategory.COLLECTIBLES.name,
            "discountPercentage" to 0,
            "isExclusive" to false,
            "isLimited" to true,
            "isFeatured" to true
        ))
        
        return items
    }
}
