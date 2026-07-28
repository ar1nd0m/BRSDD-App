package com.rmstu.brsdd.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BrsddDarkScheme = darkColorScheme(
    primary = BrsddGreenLight,
    onPrimary = BrsddInk,
    secondary = BrsddAmber,
    onSecondary = BrsddInk,
    background = BrsddInk,
    onBackground = BrsddOnSurface,
    surface = BrsddSurface,
    onSurface = BrsddOnSurface,
    surfaceVariant = BrsddSurfaceVariant,
    onSurfaceVariant = BrsddMuted,
    error = BrsddDanger,
)

@Composable
fun BRSDDTheme(content: @Composable () -> Unit) {
    val colorScheme = BrsddDarkScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BrsddGreenDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BrsddTypography,
        content = content,
    )
}
