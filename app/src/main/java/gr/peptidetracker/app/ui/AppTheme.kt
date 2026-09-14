package gr.peptidetracker.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val ElectricBlue = Color(0xFF79A9FF)
val ElectricViolet = Color(0xFFA889FF)
val ElectricCyan = Color(0xFF54E1D1)
val NeonRose = Color(0xFFFF7AA8)
val DeepBackground = Color(0xFF070A10)
val DeepSurface = Color(0xFF0E131D)
val DeepSurfaceAlt = Color(0xFF151C28)

private val DarkColors = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color(0xFF07101F),
    primaryContainer = Color(0xFF172A4A),
    onPrimaryContainer = Color(0xFFD9E7FF),
    secondary = ElectricCyan,
    onSecondary = Color(0xFF001F1C),
    secondaryContainer = Color(0xFF123936),
    onSecondaryContainer = Color(0xFFCBFFF8),
    tertiary = ElectricViolet,
    background = DeepBackground,
    onBackground = Color(0xFFF4F7FC),
    surface = DeepSurface,
    onSurface = Color(0xFFF4F7FC),
    surfaceVariant = DeepSurfaceAlt,
    onSurfaceVariant = Color(0xFFAEB8C8),
    outline = Color(0xFF354052),
    error = Color(0xFFFF8A8A)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF245EB5),
    secondary = Color(0xFF007D73),
    tertiary = Color(0xFF6847B8),
    background = Color(0xFFF4F7FC),
    surface = Color.White,
    surfaceVariant = Color(0xFFE9EEF7)
)

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
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun AppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
