package dev.eliminater.atoto_toolkit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** App theme modes that we persist with DataStore. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Simple theme wrapper that chooses dark/light from [ThemeMode]. */
@Composable
fun ATOTOToolkitTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
    }

    val colors = if (dark) {
        darkColorScheme(
            primary = PrimaryBlue,
            secondary = SecondaryTeal,
            background = DarkBackground,
            surface = SurfaceDark,
            onBackground = TextWhite,
            onSurface = TextWhite
        )
    } else {
        // Keep light theme for now, or force dark?
        // Let's make light theme at least consistent
        lightColorScheme(
            primary = PrimaryBlue,
            secondary = SecondaryTeal
        )
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
