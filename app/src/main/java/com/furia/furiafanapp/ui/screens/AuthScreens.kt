package com.furia.furiafanapp.ui.screens

import android.util.Patterns
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.furia.furiafanapp.R
import com.furia.furiafanapp.ui.theme.FuriaBlack
import com.furia.furiafanapp.ui.theme.FuriaYellow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

private fun String.onlyDigits() = filter { it.isDigit() }
private fun formatCpf(input: String): String {
    val digits = input.onlyDigits().take(11)
    val part1 = digits.take(3)
    val part2 = digits.drop(3).take(3)
    val part3 = digits.drop(6).take(3)
    val part4 = digits.drop(9).take(2)
    var result = ""
    if (part1.isNotEmpty()) result += part1
    if (part2.isNotEmpty()) result += "." + part2
    if (part3.isNotEmpty()) result += "." + part3
    if (part4.isNotEmpty()) result += "-" + part4
    return result
}

private fun mapAuthError(error: String): String = when {
    error.contains("no user record", ignoreCase = true) -> "Usuário não encontrado"
    error.contains("password is invalid", ignoreCase = true) -> "Senha incorreta"
    error.contains("already in use", ignoreCase = true) -> "Email já cadastrado"
    error.contains("badly formatted", ignoreCase = true) -> "Email inválido"
    error.contains("weak", ignoreCase = true) -> "Senha fraca (mínimo 6 caracteres)"
    else -> error
}

private class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(11)
        val builder = StringBuilder()
        digits.forEachIndexed { i, c ->
            when (i) {
                0 -> builder.append("(").append(c)
                1 -> builder.append(c).append(") ")
                in 2..6 -> builder.append(c)
                7 -> builder.append("-").append(c)
                else -> builder.append(c)
            }
        }
        val masked = builder.toString()
        val offset = object : OffsetMapping {
            override fun originalToTransformed(x: Int) =
                maskedFor(digits.take(x)).length
            override fun transformedToOriginal(x: Int) =
                masked.take(x.coerceAtMost(masked.length)).count { it.isDigit() }
            private fun maskedFor(ds: String): String {
                val sb = StringBuilder()
                ds.forEachIndexed { j, ch ->
                    when (j) {
                        0 -> sb.append("(").append(ch)
                        1 -> sb.append(ch).append(") ")
                        in 2..6 -> sb.append(ch)
                        7 -> sb.append("-").append(ch)
                        else -> sb.append(ch)
                    }
                }
                return sb.toString()
            }
        }
        return TransformedText(AnnotatedString(masked), offset)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val email = authViewModel.email.collectAsState().value
    val password = authViewModel.password.collectAsState().value
    val isLoading = authViewModel.isLoading.collectAsState().value
    val errorMessage = authViewModel.errorMessage.collectAsState().value
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_furia),
                contentDescription = "FURIA Logo",
                modifier = Modifier
                    .width(200.dp)
                    .padding(bottom = 32.dp)
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { authViewModel.updateEmail(it) },
                label = { Text("Email", color = Color.White) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = FuriaYellow) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FuriaYellow,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { authViewModel.updatePassword(it) },
                label = { Text("Senha", color = Color.White) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FuriaYellow) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FuriaYellow,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { authViewModel.login(onLoginSuccess) },
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FuriaYellow,
                    contentColor = FuriaBlack
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = FuriaBlack,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Entrar")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = onNavigateToRegister,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Não tem uma conta? Cadastre-se")
            }
            
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val email = authViewModel.email.collectAsState().value
    val password = authViewModel.password.collectAsState().value
    val confirmPassword = authViewModel.confirmPassword.collectAsState().value
    val isLoading = authViewModel.isLoading.collectAsState().value
    val errorMessage = authViewModel.errorMessage.collectAsState().value
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Cadastro",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Image(
                painter = painterResource(id = R.drawable.logo_furia),
                contentDescription = "FURIA Logo",
                modifier = Modifier
                    .width(150.dp)
                    .padding(bottom = 24.dp)
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { authViewModel.updateEmail(it) },
                label = { Text("Email", color = Color.White) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = FuriaYellow) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FuriaYellow,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                isError = email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches(),
                supportingText = {
                    if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Text("Email inválido", color = Color.Red)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { authViewModel.updatePassword(it) },
                label = { Text("Senha", color = Color.White) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FuriaYellow) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FuriaYellow,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                isError = password.isNotEmpty() && password.length < 6,
                supportingText = {
                    if (password.isNotEmpty() && password.length < 6) {
                        Text("Mínimo de 6 caracteres", color = Color.Red)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { authViewModel.updateConfirmPassword(it) },
                label = { Text("Confirmar Senha", color = Color.White) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FuriaYellow) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FuriaYellow,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                supportingText = {
                    if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                        Text("As senhas não coincidem", color = Color.Red)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val isFormValid = email.isNotBlank() && 
                             password.isNotBlank() && 
                             password.length >= 6 &&
                             password == confirmPassword &&
                             Patterns.EMAIL_ADDRESS.matcher(email).matches()
            
            Button(
                onClick = { authViewModel.register(onRegisterSuccess) },
                enabled = !isLoading && isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FuriaYellow,
                    contentColor = FuriaBlack
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = FuriaBlack,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Cadastrar")
                }
            }
            
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onProfileSaved: () -> Unit
) {
    var nickname by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var x by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Configure seu Perfil",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            
            Image(
                painter = painterResource(id = R.drawable.logo_furia),
                contentDescription = "FURIA Logo",
                modifier = Modifier
                    .width(150.dp)
                    .padding(bottom = 24.dp)
            )
            
            TextField(
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = FuriaYellow) },
                value = nickname,
                onValueChange = { nickname = it },
                placeholder = { Text("Nickname", color = Color.White) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    cursorColor = FuriaYellow
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                leadingIcon = { Icon(painter = painterResource(id = R.drawable.icon_instagram), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp)) },
                value = instagram,
                onValueChange = { instagram = it },
                placeholder = { Text("Instagram (opcional)", color = Color.White) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    cursorColor = FuriaYellow
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                leadingIcon = { Icon(painter = painterResource(id = R.drawable.icon_x), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp)) },
                value = x,
                onValueChange = { x = it },
                placeholder = { Text("X (opcional)", color = Color.White) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    cursorColor = FuriaYellow
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    if (nickname.isNotBlank()) {
                        isLoading = true
                        errorMessage = null
                        
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            val profileMap = mutableMapOf<String, Any>(
                                "nickname" to nickname
                            )
                            instagram.takeIf { it.isNotBlank() }?.let { profileMap["instagram"] = it }
                            x.takeIf { it.isNotBlank() }?.let { profileMap["x"] = it }
                            
                            FirebaseFirestore.getInstance().collection("users").document(uid)
                                .update(profileMap)
                                .addOnSuccessListener {
                                    isLoading = false
                                    onProfileSaved()
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    errorMessage = e.message ?: "Erro ao salvar perfil"
                                }
                        } else {
                            isLoading = false
                            errorMessage = "Usuário não autenticado"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = FuriaYellow,
                    contentColor = FuriaBlack
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && nickname.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = FuriaBlack,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Salvar Perfil")
                }
            }
            
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
