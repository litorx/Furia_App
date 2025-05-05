package com.furia.furiafanapp.ui.screens.shop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.furia.furiafanapp.R
import com.furia.furiafanapp.data.model.ShopCategory
import com.furia.furiafanapp.data.model.ShopItem
import com.furia.furiafanapp.ui.components.UserProfileHeader
import com.furia.furiafanapp.ui.theme.FuriaBlack
import com.furia.furiafanapp.ui.theme.FuriaWhite
import com.furia.furiafanapp.ui.theme.FuriaYellow
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    navController: NavHostController,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedCategory by remember { mutableStateOf<ShopCategory?>(null) }
    var showItemDetails by remember { mutableStateOf<ShopItem?>(null) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FURIA Shop", color = FuriaWhite) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = FuriaWhite)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = FuriaBlack
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background
            Image(
                painter = painterResource(id = R.drawable.wallpaper),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Conteúdo principal
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Perfil do usuário
                uiState.userProfile?.let { profile ->
                    UserProfileHeader(
                        userProfile = profile,
                        isTopOne = uiState.isTopOne
                    )
                }
                
                // Exibir os pontos do usuário
                if (uiState.userProfile == null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Pontos",
                            tint = FuriaYellow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${uiState.userPoints} FP",
                            color = FuriaYellow,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                // Content
                if (showItemDetails != null) {
                    ItemDetailsScreen(
                        item = showItemDetails!!,
                        userPoints = uiState.userPoints,
                        onBackClick = { showItemDetails = null },
                        onPurchaseClick = { item ->
                            viewModel.purchaseItem(item)
                        },
                        isPurchasing = uiState.isPurchasing,
                        purchaseResult = uiState.purchaseResult
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Título da seção
                        item {
                            Text(
                                text = "Loja Oficial FURIA",
                                style = MaterialTheme.typography.headlineMedium,
                                color = FuriaYellow,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        
                        // Categorias
                        item {
                            Text(
                                text = "Categorias",
                                style = MaterialTheme.typography.titleMedium,
                                color = FuriaWhite,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                item {
                                    CategoryChip(
                                        category = null,
                                        isSelected = selectedCategory == null,
                                        onClick = { selectedCategory = null }
                                    )
                                }
                                
                                items(ShopCategory.values()) { category ->
                                    CategoryChip(
                                        category = category,
                                        isSelected = selectedCategory == category,
                                        onClick = { selectedCategory = category }
                                    )
                                }
                            }
                        }
                        
                        // Itens em destaque
                        if (selectedCategory == null) {
                            item {
                                Text(
                                    text = "Itens em Destaque",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FuriaWhite,
                                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                                )
                            }
                            
                            items(uiState.featuredItems) { item ->
                                FeaturedItemCard(
                                    item = item,
                                    onClick = { showItemDetails = item }
                                )
                            }
                            
                            // Exclusivos
                            item {
                                Text(
                                    text = "Exclusivos para Fãs de Elite",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FuriaWhite,
                                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                                )
                            }
                            
                            items(uiState.exclusiveItems) { item ->
                                ShopItemCard(
                                    item = item,
                                    onClick = { showItemDetails = item }
                                )
                            }
                        } else {
                            // Itens filtrados por categoria
                            val filteredItems = when (selectedCategory) {
                                ShopCategory.APPAREL -> uiState.apparelItems
                                ShopCategory.TICKETS -> uiState.ticketsItems
                                ShopCategory.EXPERIENCES -> uiState.experiencesItems
                                ShopCategory.DISCOUNTS -> uiState.discountsItems
                                ShopCategory.DIGITAL -> uiState.digitalItems
                                ShopCategory.COLLECTIBLES -> uiState.collectiblesItems
                                else -> emptyList()
                            }
                            
                            item {
                                Text(
                                    text = getCategoryTitle(selectedCategory!!),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FuriaWhite,
                                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                                )
                            }
                            
                            items(filteredItems) { item ->
                                ShopItemCard(
                                    item = item,
                                    onClick = { showItemDetails = item }
                                )
                            }
                        }
                        
                        // Histórico de compras
                        if (uiState.purchaseHistory.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Seus Cupons",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FuriaWhite,
                                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                                )
                            }
                            
                            items(uiState.purchaseHistory.take(3)) { purchase ->
                                PurchaseHistoryItem(purchase = purchase)
                            }
                            
                            if (uiState.purchaseHistory.size > 3) {
                                item {
                                    TextButton(
                                        onClick = { /* Navegar para histórico completo */ },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = FuriaYellow
                                        )
                                    ) {
                                        Text("Ver todos os cupons")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: ShopCategory?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) FuriaYellow else FuriaBlack.copy(alpha = 0.7f)
    val textColor = if (isSelected) FuriaBlack else FuriaWhite
    
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        border = BorderStroke(1.dp, FuriaYellow)
    ) {
        Text(
            text = category?.let { getCategoryName(it) } ?: "Todos",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun FeaturedItemCard(
    item: ShopItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = FuriaBlack.copy(alpha = 0.8f)
        ),
        border = BorderStroke(2.dp, FuriaYellow),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            // Imagem do item
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                fallback = painterResource(id = R.drawable.ic_arena)
            )
            
            // Detalhes do item
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = FuriaWhite,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = FuriaWhite.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Preço
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = FuriaYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.pointsCost}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FuriaYellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Exclusivo ou limitado
                    if (item.isExclusive || item.isLimited) {
                        Surface(
                            color = if (item.isExclusive) Color(0xFFFFD700).copy(alpha = 0.2f) else Color(0xFFFF4500).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = if (item.isExclusive) "Exclusivo" else "Limitado",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.isExclusive) Color(0xFFFFD700) else Color(0xFFFF4500),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = FuriaBlack.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, FuriaYellow),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            // Imagem do item
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                fallback = painterResource(id = R.drawable.ic_arena)
            )
            
            // Detalhes do item
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FuriaWhite,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = FuriaYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.pointsCost}",
                        style = MaterialTheme.typography.bodySmall,
                        color = FuriaYellow,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (item.isExclusive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseHistoryItem(purchase: com.furia.furiafanapp.data.model.ShopPurchase) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = FuriaBlack.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, FuriaYellow.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone de cupom
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                tint = FuriaYellow,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = purchase.itemName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FuriaWhite,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Código: ${purchase.couponId?.takeLast(8) ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FuriaWhite.copy(alpha = 0.7f)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${purchase.pointsCost} pts",
                    style = MaterialTheme.typography.bodySmall,
                    color = FuriaYellow
                )
                
                Text(
                    text = formatDate(purchase.purchasedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = FuriaWhite.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun ItemDetailsScreen(
    item: ShopItem,
    userPoints: Long,
    onBackClick: () -> Unit,
    onPurchaseClick: (ShopItem) -> Unit,
    isPurchasing: Boolean,
    purchaseResult: com.furia.furiafanapp.data.model.PurchaseResult?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botão de voltar
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .background(FuriaBlack.copy(alpha = 0.7f), CircleShape)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = FuriaWhite
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Imagem do item
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp)),
            fallback = painterResource(id = R.drawable.ic_arena)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Detalhes do item
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = FuriaBlack.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = FuriaWhite,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = FuriaYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.pointsCost}",
                            style = MaterialTheme.typography.titleMedium,
                            color = FuriaYellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFF1E88E5).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = getCategoryName(item.category),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1E88E5),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    if (item.isExclusive) {
                        Surface(
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Exclusivo",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFFD700),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    if (item.isLimited) {
                        Surface(
                            color = Color(0xFFFF4500).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Limitado",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF4500),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Descrição
                Text(
                    text = "Descrição",
                    style = MaterialTheme.typography.titleMedium,
                    color = FuriaWhite,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FuriaWhite.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Informações adicionais
                when (item.category) {
                    ShopCategory.DISCOUNTS -> {
                        Text(
                            text = "Desconto de ${item.discountPercentage}% na loja oficial FURIA",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FuriaYellow
                        )
                    }
                    ShopCategory.TICKETS -> {
                        Text(
                            text = "Válido para um ingresso. Apresente o código no local do evento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FuriaWhite.copy(alpha = 0.8f)
                        )
                    }
                    ShopCategory.EXPERIENCES -> {
                        Text(
                            text = "Experiência exclusiva. Nossa equipe entrará em contato para agendar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FuriaWhite.copy(alpha = 0.8f)
                        )
                    }
                    else -> {}
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Resultado da compra
                purchaseResult?.let {
                    val backgroundColor = if (it.success) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFF44336).copy(alpha = 0.2f)
                    val textColor = if (it.success) Color(0xFF4CAF50) else Color(0xFFF44336)
                    val icon = if (it.success) Icons.Default.CheckCircle else Icons.Default.Error
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = backgroundColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = textColor
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = if (it.success) {
                                    "Compra realizada com sucesso! Seu código: ${it.couponCode}"
                                } else {
                                    it.errorMessage ?: "Erro ao realizar a compra"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Botão de compra
                Button(
                    onClick = { onPurchaseClick(item) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FuriaYellow,
                        contentColor = FuriaBlack
                    ),
                    enabled = userPoints >= item.pointsCost && !isPurchasing && purchaseResult?.success != true
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = FuriaBlack,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (purchaseResult?.success == true) "Comprado" else "Comprar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (userPoints < item.pointsCost) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Você precisa de mais ${item.pointsCost - userPoints} pontos para este item",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF44336),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// Funções auxiliares
fun getCategoryName(category: ShopCategory): String {
    return when (category) {
        ShopCategory.APPAREL -> "Roupas"
        ShopCategory.TICKETS -> "Ingressos"
        ShopCategory.EXPERIENCES -> "Experiências"
        ShopCategory.DISCOUNTS -> "Descontos"
        ShopCategory.DIGITAL -> "Digital"
        ShopCategory.COLLECTIBLES -> "Colecionáveis"
    }
}

fun getCategoryTitle(category: ShopCategory): String {
    return when (category) {
        ShopCategory.APPAREL -> "Roupas e Acessórios"
        ShopCategory.TICKETS -> "Ingressos para Eventos"
        ShopCategory.EXPERIENCES -> "Experiências Exclusivas"
        ShopCategory.DISCOUNTS -> "Descontos na Loja Oficial"
        ShopCategory.DIGITAL -> "Itens Digitais"
        ShopCategory.COLLECTIBLES -> "Itens Colecionáveis"
    }
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = java.text.SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return format.format(date)
}
