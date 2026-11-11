package app.kitsune.stream.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
fun KitsuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = KitsuneBackground.toArgb()
            window.navigationBarColor = KitsuneBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = KitsuneColorScheme,
        typography = KitsuneTypography,
        content = content
    )
}
