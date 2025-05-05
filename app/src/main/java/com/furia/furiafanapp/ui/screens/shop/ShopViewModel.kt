package com.furia.furiafanapp.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furia.furiafanapp.data.model.PurchaseResult
import com.furia.furiafanapp.data.model.ShopCategory
import com.furia.furiafanapp.data.model.ShopItem
import com.furia.furiafanapp.data.model.ShopPurchase
import com.furia.furiafanapp.data.model.UserProfile
import com.furia.furiafanapp.data.repository.ShopRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()
    
    private val auth = FirebaseAuth.getInstance()

    init {
        loadUserProfile()
        loadShopItems()
        loadPurchaseHistory()
        loadLeaderboard()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                
                // Obter referência para o documento do usuário no Firestore
                val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                val snapshot = userRef.get().await()
                
                if (snapshot.exists()) {
                    // Obter dados do perfil diretamente do Firestore
                    val nickname = snapshot.getString("nickname") ?: "Usuário"
                    val photoUrl = snapshot.getString("photoUrl") ?: ""
                    val points = snapshot.getLong("points") ?: 0
                    
                    val userProfile = UserProfile(
                        id = userId,
                        nickname = nickname,
                        photoUrl = photoUrl,
                        points = points
                    )
                    
                    _uiState.update { it.copy(
                        userProfile = userProfile,
                        userPoints = points
                    ) }
                    
                    Log.d("ShopViewModel", "Perfil do usuário carregado: $nickname, $points pontos")
                } else {
                    Log.e("ShopViewModel", "Documento do usuário não encontrado no Firestore")
                }
            } catch (e: Exception) {
                Log.e("ShopViewModel", "Erro ao carregar perfil do usuário", e)
            }
        }
    }

    private fun loadShopItems() {
        viewModelScope.launch {
            // Carregar todos os itens
            shopRepository.getAllItems()
                .catch { e ->
                    Log.e("ShopViewModel", "Erro ao carregar itens da loja", e)
                }
                .collectLatest { items ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            allItems = items,
                            apparelItems = items.filter { it.category == ShopCategory.APPAREL },
                            ticketsItems = items.filter { it.category == ShopCategory.TICKETS },
                            experiencesItems = items.filter { it.category == ShopCategory.EXPERIENCES },
                            discountsItems = items.filter { it.category == ShopCategory.DISCOUNTS },
                            digitalItems = items.filter { it.category == ShopCategory.DIGITAL },
                            collectiblesItems = items.filter { it.category == ShopCategory.COLLECTIBLES },
                            featuredItems = items.filter { it.isLimited || it.discountPercentage > 0 }.take(5),
                            exclusiveItems = items.filter { it.isExclusive }.take(5)
                        )
                    }
                }
        }
    }

    private fun loadPurchaseHistory() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch

            shopRepository.getUserPurchaseHistory(userId)
                .catch { e ->
                    Log.e("ShopViewModel", "Erro ao carregar histórico de compras", e)
                }
                .collectLatest { purchases ->
                    _uiState.update { currentState ->
                        currentState.copy(purchaseHistory = purchases)
                    }
                }
        }
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                
                // Obter referência para a coleção users no Firestore
                val usersRef = FirebaseFirestore.getInstance().collection("users")
                
                // Obter todos os usuários ordenados por pontos (decrescente)
                val snapshot = usersRef.orderBy("points", com.google.firebase.firestore.Query.Direction.DESCENDING).get().await()
                
                val leaderboard = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val nickname = doc.getString("nickname") ?: "Usuário"
                    val photoUrl = doc.getString("photoUrl") ?: ""
                    val points = doc.getLong("points") ?: 0
                    
                    UserProfile(
                        id = id,
                        nickname = nickname,
                        photoUrl = photoUrl,
                        points = points
                    )
                }
                
                // Verificar se o usuário atual é o top 1
                val isTopOne = leaderboard.isNotEmpty() && leaderboard.first().id == userId
                
                _uiState.update { 
                    it.copy(
                        leaderboard = leaderboard,
                        isTopOne = isTopOne
                    )
                }
                
                Log.d("ShopViewModel", "Leaderboard carregado: ${leaderboard.size} usuários, isTopOne: $isTopOne")
            } catch (e: Exception) {
                Log.e("ShopViewModel", "Erro ao carregar leaderboard", e)
            }
        }
    }

    fun purchaseItem(item: ShopItem) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isPurchasing = true, purchaseResult = null) }

                val userId = auth.currentUser?.uid
                if (userId.isNullOrEmpty()) {
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            purchaseResult = PurchaseResult(
                                success = false,
                                errorMessage = "Usuário não autenticado"
                            )
                        )
                    }
                    return@launch
                }

                // Verificar se tem pontos suficientes
                if (!shopRepository.hasEnoughPoints(userId, item.pointsCost)) {
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            purchaseResult = PurchaseResult(
                                success = false,
                                errorMessage = "Pontos insuficientes"
                            )
                        )
                    }
                    return@launch
                }

                // Verificar ranking para itens exclusivos
                if (item.isExclusive) {
                    val requiredRank = 3 // Rank mínimo para itens exclusivos
                    if (!shopRepository.hasRequiredRank(userId, requiredRank)) {
                        _uiState.update {
                            it.copy(
                                isPurchasing = false,
                                purchaseResult = PurchaseResult(
                                    success = false,
                                    errorMessage = "Ranking insuficiente para este item exclusivo"
                                )
                            )
                        }
                        return@launch
                    }
                }

                // Realizar a compra
                val result = shopRepository.purchaseItem(userId, item.id, item.pointsCost)

                result.fold(
                    onSuccess = { coupon ->
                        // Atualizar pontos do usuário
                        loadUserProfile()
                        // Atualizar histórico de compras
                        loadPurchaseHistory()
                        // Atualizar o leaderboard para verificar se o usuário continua sendo top 1
                        loadLeaderboard()

                        _uiState.update {
                            it.copy(
                                isPurchasing = false,
                                purchaseResult = PurchaseResult(
                                    success = true,
                                    couponCode = coupon.code
                                )
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isPurchasing = false,
                                purchaseResult = PurchaseResult(
                                    success = false,
                                    errorMessage = error.message ?: "Erro ao realizar a compra"
                                )
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("ShopViewModel", "Erro na compra do item", e)
                _uiState.update {
                    it.copy(
                        isPurchasing = false,
                        purchaseResult = PurchaseResult(
                            success = false,
                            errorMessage = "Erro inesperado: ${e.message}"
                        )
                    )
                }
            }
        }
    }
}

data class ShopUiState(
    val userProfile: UserProfile? = null,
    val userPoints: Long = 0,
    val leaderboard: List<UserProfile> = emptyList(),
    val isTopOne: Boolean = false,
    val allItems: List<ShopItem> = emptyList(),
    val apparelItems: List<ShopItem> = emptyList(),
    val ticketsItems: List<ShopItem> = emptyList(),
    val experiencesItems: List<ShopItem> = emptyList(),
    val discountsItems: List<ShopItem> = emptyList(),
    val digitalItems: List<ShopItem> = emptyList(),
    val collectiblesItems: List<ShopItem> = emptyList(),
    val featuredItems: List<ShopItem> = emptyList(),
    val exclusiveItems: List<ShopItem> = emptyList(),
    val purchaseHistory: List<ShopPurchase> = emptyList(),
    val isPurchasing: Boolean = false,
    val purchaseResult: PurchaseResult? = null
)
