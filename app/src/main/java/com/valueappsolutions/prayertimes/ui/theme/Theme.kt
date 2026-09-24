package com.valueappsolutions.prayertimes.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MetallicGold,
    secondary = Golden,
    tertiary = RichGold,
    background = DeepBlack,
    surface = PureBlack,
    onPrimary = PureBlack,
    onSecondary = PureBlack,
    onTertiary = PureBlack,
    onBackground = OffWhite,
    onSurface = OffWhite,
    primaryContainer = MetallicGold.copy(alpha = 0.15f),
    onPrimaryContainer = MetallicGold,
    secondaryContainer = Golden.copy(alpha = 0.15f),
    onSecondaryContainer = Golden,
    tertiaryContainer = RichGold.copy(alpha = 0.15f),
    onTertiaryContainer = RichGold,
    surfaceVariant = MetallicGold.copy(alpha = 0.05f),
    onSurfaceVariant = OffWhite.copy(alpha = 0.7f),
    error = androidx.compose.ui.graphics.Color(0xFFCF6679),
    errorContainer = androidx.compose.ui.graphics.Color(0xFFCF6679).copy(alpha = 0.15f),
    onError = PureBlack,
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFCF6679),
    outline = MetallicGold.copy(alpha = 0.2f)
)

private val LightColorScheme = lightColorScheme(
    primary = DarkGolden,
    secondary = RichGold,
    tertiary = MetallicGold,
    background = OffWhite,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PureWhite,
    onBackground = DeepBlack,
    onSurface = PureBlack,
    primaryContainer = DarkGolden.copy(alpha = 0.1f),
    onPrimaryContainer = DarkGolden,
    secondaryContainer = RichGold.copy(alpha = 0.1f),
    onSecondaryContainer = RichGold,
    tertiaryContainer = MetallicGold.copy(alpha = 0.1f),
    onTertiaryContainer = MetallicGold,
    surfaceVariant = DarkGolden.copy(alpha = 0.05f),
    onSurfaceVariant = DeepBlack.copy(alpha = 0.7f),
    error = androidx.compose.ui.graphics.Color(0xFFB00020),
    errorContainer = androidx.compose.ui.graphics.Color(0xFFB00020).copy(alpha = 0.1f),
    onError = PureWhite,
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFB00020),
    outline = DarkGolden.copy(alpha = 0.2f)
)

@Composable
fun PrayerTimesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val targetColorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val animatedPrimary by animateColorAsState(targetColorScheme.primary, tween(500), label = "primary")
    val animatedSecondary by animateColorAsState(targetColorScheme.secondary, tween(500), label = "secondary")
    val animatedTertiary by animateColorAsState(targetColorScheme.tertiary, tween(500), label = "tertiary")
    val animatedBackground by animateColorAsState(targetColorScheme.background, tween(500), label = "background")
    val animatedSurface by animateColorAsState(targetColorScheme.surface, tween(500), label = "surface")
    val animatedOnPrimary by animateColorAsState(targetColorScheme.onPrimary, tween(500), label = "onPrimary")
    val animatedOnSecondary by animateColorAsState(targetColorScheme.onSecondary, tween(500), label = "onSecondary")
    val animatedOnTertiary by animateColorAsState(targetColorScheme.onTertiary, tween(500), label = "onTertiary")
    val animatedOnBackground by animateColorAsState(targetColorScheme.onBackground, tween(500), label = "onBackground")
    val animatedOnSurface by animateColorAsState(targetColorScheme.onSurface, tween(500), label = "onSurface")
    
    val animatedPrimaryContainer by animateColorAsState(targetColorScheme.primaryContainer, tween(500), label = "primaryContainer")
    val animatedOnPrimaryContainer by animateColorAsState(targetColorScheme.onPrimaryContainer, tween(500), label = "onPrimaryContainer")
    val animatedSurfaceVariant by animateColorAsState(targetColorScheme.surfaceVariant, tween(500), label = "surfaceVariant")
    val animatedOnSurfaceVariant by animateColorAsState(targetColorScheme.onSurfaceVariant, tween(500), label = "onSurfaceVariant")
    
    val colorScheme = targetColorScheme.copy(
        primary = animatedPrimary,
        secondary = animatedSecondary,
        tertiary = animatedTertiary,
        background = animatedBackground,
        surface = animatedSurface,
        onPrimary = animatedOnPrimary,
        onSecondary = animatedOnSecondary,
        onTertiary = animatedOnTertiary,
        onBackground = animatedOnBackground,
        onSurface = animatedOnSurface,
        primaryContainer = animatedPrimaryContainer,
        onPrimaryContainer = animatedOnPrimaryContainer,
        surfaceVariant = animatedSurfaceVariant,
        onSurfaceVariant = animatedOnSurfaceVariant
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
        }
        androidx.compose.runtime.LaunchedEffect(darkTheme) {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
