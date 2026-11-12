package app.kitsune.stream.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KitsuneColorScheme = darkColorScheme(
    primary = KitsuneAccent,
    onPrimary = KitsuneOnBackground,
    primaryContainer = KitsuneAccentSoft,
    onPrimaryContainer = KitsuneBackground,
    secondary = KitsuneAccentSoft,
    background = KitsuneBackground,
    onBackground = KitsuneOnBackground,
    surface = KitsuneSurface,
    onSurface = KitsuneOnBackground,
    surfaceVariant = KitsuneSurfaceHigh,
    onSurfaceVariant = KitsuneOnSurfaceVariant,
    outline = KitsuneOutline
)

@Composable
fun KitsuneTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KitsuneColorScheme,
        typography = KitsuneTypography,
        content = content
    )
}
