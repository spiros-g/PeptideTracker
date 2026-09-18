package gr.peptidetracker.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.R
import kotlin.math.ceil

@Composable
fun PremiumBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "ambientMesh")
    val horizontalShift by transition.animateFloat(
        initialValue = -0.08f,
        targetValue = 0.09f,
        animationSpec = infiniteRepeatable(tween(12_000), RepeatMode.Reverse),
        label = "meshX"
    )
    val verticalShift by transition.animateFloat(
        initialValue = 0.05f,
        targetValue = -0.07f,
        animationSpec = infiniteRepeatable(tween(15_000), RepeatMode.Reverse),
        label = "meshY"
    )

    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.background,
                        colors.surfaceVariant.copy(alpha = 0.55f),
                        colors.background
                    )
                )
            )

            val blueCenter = Offset(
                x = size.width * (0.84f + horizontalShift),
                y = size.height * (0.16f + verticalShift)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = 0.17f),
                        ElectricViolet.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = blueCenter,
                    radius = size.minDimension * 0.58f
                ),
                radius = size.minDimension * 0.58f,
                center = blueCenter
            )

            val cyanCenter = Offset(
                x = size.width * (0.08f - horizontalShift * 0.7f),
                y = size.height * (0.78f - verticalShift * 0.6f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricCyan.copy(alpha = 0.11f),
                        ElectricBlue.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    center = cyanCenter,
                    radius = size.minDimension * 0.55f
                ),
                radius = size.minDimension * 0.55f,
                center = cyanCenter
            )

            val grid = 52.dp.toPx()
            val verticalLines = ceil(size.width / grid).toInt()
            val horizontalLines = ceil(size.height / grid).toInt()
            val gridColor = colors.onBackground.copy(alpha = 0.026f)

            for (index in 0..verticalLines) {
                val x = index * grid
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }

            for (index in 0..horizontalLines) {
                val y = index * grid
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        colors.scrim.copy(alpha = 0.015f),
                        Color.Transparent,
                        colors.scrim.copy(alpha = 0.06f)
                    )
                )
            )
        }

        CompositionLocalProvider(
            LocalContentColor provides colors.onBackground
        ) {
            content()
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(26.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.982f else 1f,
        animationSpec = tween(125),
        label = "glassPress"
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interaction,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 14.dp,
                shape = shape,
                clip = false,
                ambientColor = colors.scrim.copy(alpha = 0.20f),
                spotColor = colors.scrim.copy(alpha = 0.18f)
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.surface.copy(alpha = 0.96f),
                        colors.surfaceVariant.copy(alpha = 0.86f),
                        colors.surface.copy(alpha = 0.94f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        colors.onSurface.copy(alpha = 0.16f),
                        ElectricBlue.copy(alpha = 0.15f),
                        colors.onSurface.copy(alpha = 0.06f)
                    )
                ),
                shape = shape
            )
            .then(clickModifier)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            colors.onSurface.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .align(Alignment.TopCenter)
        )

        CompositionLocalProvider(
            LocalContentColor provides colors.onSurface
        ) {
            Box(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

@Composable
fun premiumButtonColors() = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f)
)

@Composable
fun premiumTextButtonColors() = ButtonDefaults.textButtonColors(
    contentColor = MaterialTheme.colorScheme.onSurface,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f)
)

@Composable
fun premiumFilterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.80f),
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedLeadingIconColor = MaterialTheme.colorScheme.secondary,
    selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
)

@Composable
fun premiumTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.56f),
    errorTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.52f),
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
    errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.32f),
    cursorColor = MaterialTheme.colorScheme.secondary,
    errorCursorColor = MaterialTheme.colorScheme.error,
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.52f),
    errorBorderColor = MaterialTheme.colorScheme.error,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f),
    errorLabelColor = MaterialTheme.colorScheme.error,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun StoreVialImage(
    productKey: String,
    imageIndex: Map<String, String>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxSize(0.88f)
                .background(
                    Brush.radialGradient(
                        listOf(
                            ElectricBlue.copy(alpha = 0.10f),
                            ElectricCyan.copy(alpha = 0.025f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Image(
            painter = painterResource(R.drawable.peptide_vial_generated),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        )
    }
}

@Composable
fun FloatingVial(
    productKey: String,
    imageIndex: Map<String, String>,
    modifier: Modifier = Modifier,
    phase: Int = 0
) {
    val transition = rememberInfiniteTransition(label = "vialFloat$productKey")
    val y by transition.animateFloat(
        initialValue = -4f + phase,
        targetValue = 5f + phase,
        animationSpec = infiniteRepeatable(
            animation = tween(3_000 + phase * 70),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vialY"
    )

    StoreVialImage(
        productKey = productKey,
        imageIndex = imageIndex,
        modifier = modifier.graphicsLayer {
            translationY = y
        }
    )
}
