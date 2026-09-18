package gr.peptidetracker.app.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

val ElectricBlue = Color(0xFF5B87F7)
val ElectricViolet = Color(0xFF8C6FE8)
val ElectricCyan = Color(0xFF229E94)
val NeonRose = Color(0xFFE75F8F)

private val DarkBackground = Color(0xFF05080D)
private val DarkSurface = Color(0xFF0F1621)
private val DarkSurfaceStrong = Color(0xFF141E2B)
private val DarkText = Color(0xFFF7F9FF)
private val DarkTextSecondary = Color(0xFFB8C2D1)

private val LightBackground = Color(0xFFF3F6FB)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceStrong = Color(0xFFE8EEF7)
private val LightText = Color(0xFF17202E)
private val LightTextSecondary = Color(0xFF586576)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7CA4FF),
    onPrimary = Color(0xFF09162E),
    primaryContainer = Color(0xFF1A2D4F),
    onPrimaryContainer = DarkText,
    secondary = Color(0xFF52D4C8),
    onSecondary = Color(0xFF06231F),
    secondaryContainer = Color(0xFF113B38),
    onSecondaryContainer = DarkText,
    tertiary = Color(0xFFA98DFF),
    onTertiary = Color(0xFF1A1234),
    tertiaryContainer = Color(0xFF312652),
    onTertiaryContainer = DarkText,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceStrong,
    onSurfaceVariant = DarkTextSecondary,
    surfaceTint = Color.Transparent,
    inverseSurface = DarkText,
    inverseOnSurface = DarkBackground,
    inversePrimary = ElectricBlue,
    outline = Color(0xFF66758B),
    outlineVariant = Color(0xFF2D3A4B),
    error = Color(0xFFFF858E),
    onError = Color(0xFF2B080D),
    errorContainer = Color(0xFF4B1F28),
    onErrorContainer = Color(0xFFFFDADF),
    scrim = Color.Black
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF315FCE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6FF),
    onPrimaryContainer = Color(0xFF0B255C),
    secondary = Color(0xFF087B72),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDEFEA),
    onSecondaryContainer = Color(0xFF073C37),
    tertiary = Color(0xFF6A50BF),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8E0FF),
    onTertiaryContainer = Color(0xFF2A1B5B),
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceStrong,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = Color.Transparent,
    inverseSurface = Color(0xFF273241),
    inverseOnSurface = Color.White,
    inversePrimary = Color(0xFFAEC7FF),
    outline = Color(0xFF748196),
    outlineVariant = Color(0xFFC8D1DE),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    scrim = Color.Black
)

val AppBackground: Color
    @Composable get() = MaterialTheme.colorScheme.background

val GlassSurface: Color
    @Composable get() = MaterialTheme.colorScheme.surface

val GlassSurfaceStrong: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant

val GlassSurfaceSoft: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f)

val TextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

val TextSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextMuted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)

val GlassOutline: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)

val GlassOutlineStrong: Color
    @Composable get() = MaterialTheme.colorScheme.outline.copy(alpha = 0.76f)

private val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.8).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 29.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 21.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 25.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 21.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
    )
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun AppTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode.lowercase()) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val view = LocalView.current
    SideEffect {
        val activity = view.context as? Activity ?: return@SideEffect
        WindowCompat.getInsetsController(activity.window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
