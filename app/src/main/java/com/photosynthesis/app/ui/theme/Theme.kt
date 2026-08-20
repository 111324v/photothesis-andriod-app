package com.photosynthesis.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 品牌色：取自原型中的绿色系
val PrimaryGreen = Color(0xFF4CAF50)
val DarkGreen = Color(0xFF2D6E3E)
val LightGreen = Color(0xFF7DD4A8)
val SkyBlue = Color(0xFF87CEEB)
val WarmYellow = Color(0xFFFFD900)
val SoftWhite = Color(0xFFF8FFF8)

// 四要素颜色（与原型一致）
val ElementLight = Color(0xFFFFD97D)   // 光 - 金黄
val ElementWater = Color(0xFF71D4F3)   // 水 - 天蓝
val ElementAir = Color(0xFF74B9FF)     // 气 - 浅蓝
val ElementBiome = Color(0xFF7DD4A8)   // 境 - 薄荷绿

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    secondary = LightGreen,
    background = Color(0xFF0A1A0E),
    surface = Color(0xFF0A1A0E),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = DarkGreen,
    background = SoftWhite,
    surface = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A)
)

@Composable
fun PhotosynthesisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // 让状态栏沉浸
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
