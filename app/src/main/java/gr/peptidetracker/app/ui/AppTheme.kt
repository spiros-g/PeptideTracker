package gr.peptidetracker.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6EA8FE),
    secondary = Color(0xFF54D6C6),
    background = Color(0xFF0B0F14),
    surface = Color(0xFF121820),
    surfaceVariant = Color(0xFF1A2230)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1557B0),
    secondary = Color(0xFF00796B)
)

@Composable
fun AppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
