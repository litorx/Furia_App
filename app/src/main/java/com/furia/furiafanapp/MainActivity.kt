package com.furia.furiafanapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import com.furia.furiafanapp.ui.navigation.NavGraph
import com.furia.furiafanapp.ui.navigation.Screen
import com.furia.furiafanapp.ui.theme.FuriaBlack
import com.furia.furiafanapp.ui.theme.FuriaFanAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import com.furia.furiafanapp.util.onboardingPrefs
import com.furia.furiafanapp.util.ONBOARDING_COMPLETED_KEY

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseFirestore.setLoggingEnabled(true)
        enableEdgeToEdge()
        setContent {
            FuriaFanAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FuriaBlack
                ) {
                    // Carrega NavGraph em background e exibe o splash por cima para evitar flash branco
                    val navController = rememberNavController()
                    var showSplash by remember { mutableStateOf(true) }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(FuriaBlack)
                    ) {
                        val context = LocalContext.current
                        val onboardingCompleted by context.onboardingPrefs.data
                            .map { it[ONBOARDING_COMPLETED_KEY] ?: false }
                            .collectAsState(initial = false)
                        val auth = FirebaseAuth.getInstance()
                        val startDestination = when {
                            auth.currentUser == null -> Screen.Login.route
                            !onboardingCompleted -> Screen.Onboarding.route
                            else -> Screen.Home.route
                        }
                        NavGraph(navController = navController, startDestination = startDestination)
                        if (showSplash) {
                            SplashScreen(onSplashFinished = { showSplash = false })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var animationPhase by remember { mutableStateOf(SplashAnimationPhase.INITIAL) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val alpha by animateFloatAsState(
        targetValue = when (animationPhase) {
            SplashAnimationPhase.INITIAL -> 0f
            SplashAnimationPhase.VISIBLE -> 1f
            SplashAnimationPhase.FADE_OUT -> 0f
        },
        animationSpec = tween(
            durationMillis = when (animationPhase) {
                SplashAnimationPhase.VISIBLE -> 650
                SplashAnimationPhase.FADE_OUT -> 520
                else -> 0
            },
            easing = EaseOutCirc
        )
    )
    val scale by animateFloatAsState(
        targetValue = when (animationPhase) {
            SplashAnimationPhase.INITIAL -> 0.85f
            SplashAnimationPhase.VISIBLE -> 1f
            SplashAnimationPhase.FADE_OUT -> 1f
        },
        animationSpec = tween(
            durationMillis = when (animationPhase) {
                SplashAnimationPhase.VISIBLE -> 650
                else -> 0
            },
            easing = EaseOutCirc
        )
    )
    LaunchedEffect(Unit) {
        animationPhase = SplashAnimationPhase.VISIBLE
        delay(1040)
        animationPhase = SplashAnimationPhase.FADE_OUT
        delay(520)
        onSplashFinished()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FuriaBlack),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_furia),
            contentDescription = "FURIA Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(screenWidth)
                .alpha(alpha)
                .scale(scale)
        )
    }
}

// Fases da animação do splash (simplificadas)
private enum class SplashAnimationPhase {
    INITIAL,   // Estado inicial
    VISIBLE,   // Totalmente visível
    FADE_OUT   // Desaparecendo suavemente
}

// Curva de easing mais suave para a entrada
private val EaseOutCirc = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)