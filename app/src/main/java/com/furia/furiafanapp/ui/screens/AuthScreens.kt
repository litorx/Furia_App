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

// Helpers para formatação de campos e mapeamento de erros
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

// máscara de telefone: (##) #####-#### usando VisualTransformation
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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    // Animação do logo
    var logoScale by remember { mutableStateOf(1f) }
    val scale by animateFloatAsState(
        targetValue = logoScale,
        animationSpec = tween(durationMillis = 400)
    )
    LaunchedEffect(Unit) {
        logoScale = 1.2f
        delay(200)
        logoScale = 1f
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fundo: wallpaper e overlay preto
        Image(
            painter = painterResource(id = R.drawable.wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_furiav_semtexto),
                contentDescription = null,
                modifier = Modifier.size(150.dp).scale(scale)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Furia Fan",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Sua jornada começa aqui. Venha torcer com a gente!",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = FuriaYellow) },
                placeholder = { Text("Email", color = Color.White) },
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
                value = password,
                onValueChange = { password = it },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FuriaYellow) },
                placeholder = { Text("Senha", color = Color.White) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    cursorColor = FuriaYellow
                ),
                modifier = Modifier.fillMaxWidth()
            )
            errorMessage?.let { Text(text = it, color = FuriaYellow) }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    isLoading = true
                    authViewModel.login(
                        email = email,
                        password = password,
                        onSuccess = onLoginSuccess,
                        onError = { error ->
                            errorMessage = mapAuthError(error)
                            isLoading = false
                        }
                    )
                },
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
            TextButton(onClick = onNavigateToRegister) {
                Text("Criar conta", color = FuriaYellow)
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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    // Animação do logo
    var logoScale by remember { mutableStateOf(1f) }
    val scale by animateFloatAsState(
        targetValue = logoScale,
        animationSpec = tween(durationMillis = 400)
    )
    LaunchedEffect(Unit) {
        logoScale = 1.2f
        delay(200)
        logoScale = 1f
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Wallpaper de fundo
        Image(
            painter = painterResource(id = R.drawable.wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Overlay escuro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        // Botão Voltar no topo esquerdo
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = FuriaYellow)
        }
        // Conteúdo centralizado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo maior sem texto
            Image(
                painter = painterResource(id = R.drawable.logo_furiav_semtexto),
                contentDescription = null,
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale)
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Texto criativo
            Text(
                "Junte-se à Furia e leve sua paixão ao máximo!",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextField(
                value = name,
                onValueChange = { name = it },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = FuriaYellow) },
                placeholder = { Text("Nome", color = Color.White) },
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
                value = email,
                onValueChange = { email = it },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = FuriaYellow) },
                placeholder = { Text("Email", color = Color.White) },
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
                value = phone,
                onValueChange = { phone = it.filter { it.isDigit() }.take(11) },
                visualTransformation = PhoneVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = FuriaYellow) },
                placeholder = { Text("Telefone", color = Color.White) },
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
                value = password,
                onValueChange = { password = it },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FuriaYellow) },
                placeholder = { Text("Senha", color = Color.White) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    cursorColor = FuriaYellow
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FuriaYellow) },
                placeholder = { Text("Confirmar senha", color = Color.White) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    cursorColor = FuriaYellow
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = cpf,
                onValueChange = { cpf = formatCpf(it) },
                placeholder = { Text("CPF (opcional)", color = Color.White) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    cursorColor = FuriaYellow
                ),
                modifier = Modifier.fillMaxWidth()
            )
            errorMessage?.let { Text(text = it, color = FuriaYellow) }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        errorMessage = "As senhas não conferem"
                        return@Button
                    }
                    isLoading = true
                    authViewModel.register(
                        email = email,
                        password = password,
                        name = name,
                        phone = phone,
                        cpf = cpf.ifBlank { null },
                        onSuccess = onRegisterSuccess,
                        onError = { error ->
                            errorMessage = mapAuthError(error)
                            isLoading = false
                        }
                    )
                },
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
    // Animação do logo
    var logoScale by remember { mutableStateOf(1f) }
    val scale by animateFloatAsState(
        targetValue = logoScale,
        animationSpec = tween(durationMillis = 400)
    )
    LaunchedEffect(Unit) {
        logoScale = 1.2f
        delay(200)
        logoScale = 1f
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Wallpaper de fundo
        Image(
            painter = painterResource(id = R.drawable.wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Overlay escuro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        // Conteúdo centralizado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo sem texto
            Image(
                painter = painterResource(id = R.drawable.logo_furiav_semtexto),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale)
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Texto criativo
            Text(
                "Atualize seu perfil e mostre sua paixão pela Furia!",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextField(
                value = nickname,
                onValueChange = { nickname = it },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = FuriaYellow) },
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
                value = instagram,
                onValueChange = { instagram = it },
                leadingIcon = { Icon(painter = painterResource(id = R.drawable.icon_insta), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp)) },
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
                        
                        // Salvar o perfil
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            val profileMap = mutableMapOf<String, Any>(
                                "nickname" to nickname
                            )
                            instagram.takeIf { it.isNotBlank() }?.let { profileMap["instagram"] = it }
                            x.takeIf { it.isNotBlank() }?.let { profileMap["x"] = it }
                            
                            // Usar Firestore diretamente para garantir que podemos verificar o sucesso
                            FirebaseFirestore.getInstance().collection("users").document(uid)
                                .update(profileMap)
                                .addOnSuccessListener {
                                    // Perfil salvo com sucesso, navegar para a próxima tela
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
            
            // Exibir mensagem de erro, se houver
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
