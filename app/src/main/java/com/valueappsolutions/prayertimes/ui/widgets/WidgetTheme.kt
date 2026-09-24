package com.valueappsolutions.prayertimes.ui.widgets

import androidx.compose.ui.graphics.Color
import androidx.glance.material3.ColorProviders
import com.valueappsolutions.prayertimes.ui.theme.DarkGolden
import com.valueappsolutions.prayertimes.ui.theme.DeepBlack
import com.valueappsolutions.prayertimes.ui.theme.Golden
import com.valueappsolutions.prayertimes.ui.theme.MetallicGold
import com.valueappsolutions.prayertimes.ui.theme.OffWhite
import com.valueappsolutions.prayertimes.ui.theme.PureBlack
import com.valueappsolutions.prayertimes.ui.theme.PureWhite
import com.valueappsolutions.prayertimes.ui.theme.RichGold

// We map our custom colors to Glance's ColorProviders to support dynamic theming/dark mode.
val WidgetThemeColors = ColorProviders(
    light = androidx.compose.material3.lightColorScheme(
        primary = MetallicGold,
        onPrimary = PureWhite,
        primaryContainer = Color(0xFFFDFBF5), // Opaque equivalent of MetallicGold with 5% alpha over PureWhite
        onPrimaryContainer = DeepBlack,
        secondary = RichGold,
        onSecondary = PureWhite,
        background = OffWhite,
        onBackground = DeepBlack,
        surface = PureWhite,
        onSurface = DeepBlack,
        error = Color(0xFFB00020),
        onError = PureWhite
    ),
    dark = androidx.compose.material3.darkColorScheme(
        primary = Golden,
        onPrimary = PureBlack,
        primaryContainer = Color(0xFF29271D), // Opaque equivalent of Golden with 5% alpha over DeepBlack
        onPrimaryContainer = OffWhite,
        secondary = MetallicGold,
        onSecondary = PureBlack,
        background = PureBlack,
        onBackground = OffWhite,
        surface = DeepBlack,
        onSurface = OffWhite,
        error = Color(0xFFCF6679),
        onError = PureBlack
    )
)
