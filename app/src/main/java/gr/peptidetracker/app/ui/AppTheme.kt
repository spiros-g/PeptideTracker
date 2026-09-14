package gr.peptidetracker.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val ElectricBlue = Color(0xFF6D9CFF)
val ElectricViolet = Color(0xFF9A7BFF)
val ElectricCyan = Color(0xFF37C6B8)
val NeonRose = Color(0xFFFF78A8)

val AppBackground = Color(0xFF05080D)
val GlassSurface = Color(0xFF0F1621)
val GlassSurfaceStrong = Color(0xFF141E2B)
val GlassSurfaceSoft = Color(0xFF192433)
val TextPrimary = Color(0xFFF7F9FF)
val TextSecondary = Color(0xFFB8C2D1)
val TextMuted = Color(0xFF8793A5)
val GlassOutline = Color(0x2EFFFFFF)
val GlassOutlineStrong = Color(0x4DFFFFFF)

private val DarkColors = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A2D4F),
    onPrimaryContainer = TextPrimary,

    secondary = ElectricCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF113B38),
    onSecondaryContainer = TextPrimary,

    tertiary = ElectricViolet,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF312652),
    onTertiaryContainer = TextPrimary,

    background = AppBackground,
    onBackground = TextPrimary,

    surface = GlassSurface,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurfaceStrong,
    onSurfaceVariant = TextSecondary,
    surfaceTint = Color.Transparent,

    inverseSurface = TextPrimary,
    inverseOnSurface = AppBackground,
    inversePrimary = ElectricBlue,

    outline = Color(0xFF4B586C),
    outlineVariant = Color(0xFF283344),

    error = Color(0xFFFF858E),
    onError = Color.White,
    errorContainer = Color(0xFF4B1F28),
    onErrorContainer = Color(0xFFFFDADF),

    scrim = Color.Black
)

private val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.8).sp,
        color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 29.sp,
        color = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        color = TextPrimary
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = TextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = TextSecondary
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
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
