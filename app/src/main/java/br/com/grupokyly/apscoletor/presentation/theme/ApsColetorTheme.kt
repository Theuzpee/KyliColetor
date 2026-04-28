package br.com.grupokyly.apscoletor.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryHighlight,
    background = DarkBackground,
    surface = SurfaceDark,
    error = ErrorRed,
    onPrimary = DarkBackground,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onError = TextWhite
)

@Composable
fun ApsColetorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
