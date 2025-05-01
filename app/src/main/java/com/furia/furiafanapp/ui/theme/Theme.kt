package com.furia.furiafanapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = FuriaYellow,
    onPrimary = FuriaBlack,
    secondary = FuriaGray,
    onSecondary = FuriaWhite,
    tertiary = FuriaLightGray,
    onTertiary = FuriaBlack,
    background = FuriaBackground,
    onBackground = FuriaWhite,
    surface = FuriaSurface,
    onSurface = FuriaWhite,
    error = FuriaError,
    onError = FuriaWhite
)

private val LightColorScheme = lightColorScheme(
    primary = FuriaYellow,
    onPrimary = FuriaBlack,
    secondary = FuriaGray,
    onSecondary = FuriaWhite,
    tertiary = FuriaLightGray,
    onTertiary = FuriaBlack,
    background = FuriaWhite,
    onBackground = FuriaBlack,
    surface = FuriaLightGray,
    onSurface = FuriaBlack,
    error = FuriaError,
    onError = FuriaWhite
)

@Composable
fun FuriaFanAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled by default to maintain brand consistency
    useTransparentStatusBar: Boolean = true, // Por padrão, usar barra de status transparente
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Usar cor transparente para a barra de status se solicitado
            window.statusBarColor = if (useTransparentStatusBar) {
                Color.Transparent.toArgb()
            } else {
                // Caso contrário, usar preto para a tela de introdução
                Color.Black.toArgb()
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}