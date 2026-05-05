package com.example.testproject

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 1. Определяем наши фирменные цвета
private val LightColors = lightColorScheme(
    primary = Color(0xFF006A4E), // Тёмно-зелёный, как у справки IntelliJ IDEA
    onPrimary = Color.White,
    secondary = Color(0xFF4B6356),
    background = Color(0xFFFBFDF9),
    surface = Color(0xFFFBFDF9),
    onBackground = Color(0xFF191C1A),
    onSurface = Color(0xFF191C1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6CDBB7), // Светло-зелёный, для тёмной темы
    onPrimary = Color(0xFF003827),
    secondary = Color(0xFFB3CCC0),
    background = Color(0xFF191C1A),
    surface = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3E0),
    onSurface = Color(0xFFE1E3E0),
)

// 2. Наша главная функция-тема
@Composable
fun KotlinKoansTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Если Android 12+ и включена динамическая смена цвета, используем её
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        // Иначе используем наши собственные цвета
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // Пока стандартные шрифты
        content = content
    )
}