package com.furia.furiafanapp.ui.screens.Auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import android.util.Log

// Estado para o perfil do usuário
sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // Estado observável para o perfil
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()
    
    // Estados para os campos de formulário
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Funções para atualizar os campos
    fun updateEmail(value: String) {
        _email.value = value
    }
    
    fun updatePassword(value: String) {
        _password.value = value
    }
    
    fun updateConfirmPassword(value: String) {
        _confirmPassword.value = value
    }
    
    fun clearError() {
        _errorMessage.value = null
    }

    fun login(onSuccess: () -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null
        
        auth.signInWithEmailAndPassword(_email.value, _password.value)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    onSuccess()
                    // gamificação: 5 pontos por login (1x por dia)
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val docRef = firestore.collection("users").document(uid)
                    docRef.get()
                        .addOnSuccessListener { snap ->
                            val last = snap.getString("lastLoginDate") ?: ""
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            Log.d("Gamification", "Login: lastLoginDate=$last, today=$today")
                            if (last != today) {
                                val updateMap = mapOf(
                                    "points" to FieldValue.increment(5),
                                    "lastLoginDate" to today
                                )
                                docRef.update(updateMap)
                                    .addOnSuccessListener { Log.d("Gamification", "Login: awarded 5 FP") }
                                    .addOnFailureListener { Log.e("Gamification", "Login: award failed", it) }
                            } else {
                                Log.d("Gamification", "Login: already awarded today")
                            }
                        }
                        .addOnFailureListener { Log.e("Gamification", "Login: get lastLoginDate failed", it) }
                } else _errorMessage.value = task.exception?.message ?: "Erro ao fazer login"
            }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                    // gamificação: 5 pontos por login (1x por dia)
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val docRef = firestore.collection("users").document(uid)
                    docRef.get()
                        .addOnSuccessListener { snap ->
                            val last = snap.getString("lastLoginDate") ?: ""
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            Log.d("Gamification", "Login: lastLoginDate=$last, today=$today")
                            if (last != today) {
                                val updateMap = mapOf(
                                    "points" to FieldValue.increment(5),
                                    "lastLoginDate" to today
                                )
                                docRef.update(updateMap)
                                    .addOnSuccessListener { Log.d("Gamification", "Login: awarded 5 FP") }
                                    .addOnFailureListener { Log.e("Gamification", "Login: award failed", it) }
                            } else {
                                Log.d("Gamification", "Login: already awarded today")
                            }
                        }
                        .addOnFailureListener { Log.e("Gamification", "Login: get lastLoginDate failed", it) }
                } else onError(task.exception?.message ?: "Erro ao fazer login")
            }
    }

    fun register(onSuccess: () -> Unit) {
        if (_password.value != _confirmPassword.value) {
            _errorMessage.value = "As senhas não coincidem"
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        
        auth.createUserWithEmailAndPassword(_email.value, _password.value)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userMap = mutableMapOf<String, Any>(
                        "email" to _email.value
                    )
                    firestore.collection("users").document(uid)
                        .set(userMap)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> _errorMessage.value = e.message ?: "Erro ao salvar perfil" }
                } else _errorMessage.value = task.exception?.message ?: "Erro ao registrar"
            }
    }

    fun register(
        email: String,
        password: String,
        name: String,
        phone: String,
        cpf: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userMap = mutableMapOf<String, Any>(
                        "nickname" to name,
                        "phone" to phone
                    )
                    userMap["email"] = email
                    cpf?.let { userMap["cpf"] = it }
                    firestore.collection("users").document(uid)
                        .set(userMap)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError(e.message ?: "Erro ao salvar perfil") }
                } else onError(task.exception?.message ?: "Erro ao registrar")
            }
    }

    fun saveProfile(
        nickname: String,
        instagram: String?,
        twitch: String?,
        x: String?
    ) {
        val uid = auth.currentUser?.uid ?: return
        val profileMap = mutableMapOf<String, Any>(
            "nickname" to nickname
        )
        instagram?.let { profileMap["instagram"] = it }
        twitch?.let { profileMap["twitch"] = it }
        x?.let { profileMap["x"] = it }
        
        _profileState.value = ProfileState.Loading
        
        viewModelScope.launch {
            try {
                firestore.collection("users").document(uid)
                    .update(profileMap)
                    .addOnSuccessListener {
                        _profileState.value = ProfileState.Success
                    }
                    .addOnFailureListener { e ->
                        _profileState.value =
                            ProfileState.Error(e.message ?: "Erro ao salvar perfil")
                    }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Erro ao salvar perfil")
            }
        }
    }

    fun loginWithGoogle(
        idToken: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (idToken == null) {
            onError("Google sign-in falhou")
            return
        }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess()
                else onError(task.exception?.message ?: "Erro ao autenticar com Google")
            }
    }

    /** Salva lista de jogos favoritos do usuário no Firestore */
    fun saveFavoriteGames(games: Set<String>) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .update(mapOf("favorites" to games.toList()))
    }
}
