package com.yogurtvpn.client.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = YogurtPurple,
    onPrimary = White,
    primaryContainer = YogurtPinkLight,
    onPrimaryContainer = YogurtPurpleDark,
    secondary = YogurtPink,
    onSecondary = White,
    background = YogurtPinkLight,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = GrayLight,
    onSurfaceVariant = Gray,
    outline = GrayBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = YogurtPurple,
    onPrimary = White,
    primaryContainer = YogurtPurpleDark,
    onPrimaryContainer = YogurtPinkLight,
    secondary = YogurtPink,
    onSecondary = Black,
    background = Color(0xFF1A0A2E),
    onBackground = White,
    surface = Color(0xFF2D1B4E),
    onSurface = White,
)

@Composable
fun YogurtVPNTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = YogurtTypography,
        content = content
    )
}