package com.furia.furiafanapp.ui.screens.Splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.furia.furiafanapp.R
import com.furia.furiafanapp.ui.theme.FuriaBlack
import com.furia.furiafanapp.ui.theme.FuriaFanAppTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Usar o tema com barra de status preta
    FuriaFanAppTheme(useTransparentStatusBar = false) {
        // Dimensões da tela para logo maior
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        
        // Uma única animação
        var isVisible by remember { mutableStateOf(false) }
        
        // Efeito de aparecimento e desaparecimento
        val alphaAnim by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(1000, easing = LinearEasing),
            label = "alpha"
        )
        
        // Controle de animação - apenas uma vez
        LaunchedEffect(Unit) {
            // Iniciar animação de aparecimento
            isVisible = true
            
            // Aguardar tempo de exibição
            delay(2000)
            
            // Iniciar animação de desaparecimento
            isVisible = false
            
            // Aguardar animação terminar
            delay(1000)
            
            // Navegar para próxima tela
            onSplashFinished()
        }
        
        // Tela de splash
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FuriaBlack),
            contentAlignment = Alignment.Center
        ) {
            // Logo grande
            Image(
                painter = painterResource(id = R.drawable.logo_furia),
                contentDescription = "FURIA Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(screenWidth * 0.9f)
                    .alpha(alphaAnim)
            )
        }
    }
}