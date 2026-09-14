package gr.peptidetracker.app.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import gr.peptidetracker.app.data.StoreCatalogClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

private object VialImageCache {
    private val memory = LruCache<String, Bitmap>(24)

    suspend fun load(cacheDir: File, url: String): Bitmap? {
        memory.get(url)?.let { return it }

        return withContext(Dispatchers.IO) {
            val imageDir = File(cacheDir, "vial-images").apply { mkdirs() }
            val imageFile = File(imageDir, sha256(url) + ".img")

            val bitmap = runCatching {
                if (imageFile.exists() && imageFile.length() > 0L) {
                    BitmapFactory.decodeFile(imageFile.absolutePath)
                } else {
                    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 7_000
                        readTimeout = 10_000
                        instanceFollowRedirects = true
                        requestMethod = "GET"
                        setRequestProperty("Accept", "image/*")
                        setRequestProperty("User-Agent", "PeptideTrackerGR/2.0")
                    }

                    try {
                        if (connection.responseCode !in 200..299) return@runCatching null
                        val bytes = connection.inputStream.use { it.readBytes() }
                        if (bytes.isEmpty()) return@runCatching null
                        imageFile.writeBytes(bytes)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } finally {
                        connection.disconnect()
                    }
                }
            }.getOrNull()

            if (bitmap != null) memory.put(url, bitmap)
            bitmap
        }
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}

@Composable
fun PremiumBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "ambient")
    val driftX by transition.animateFloat(
        initialValue = -60f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(tween(9_000), RepeatMode.Reverse),
        label = "driftX"
    )
    val driftY by transition.animateFloat(
        initialValue = 20f,
        targetValue = -90f,
        animationSpec = infiniteRepeatable(tween(11_000), RepeatMode.Reverse),
        label = "driftY"
    )

    Box(
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF060910),
                        Color(0xFF0A0E17),
                        Color(0xFF07101A)
                    )
                )
            )
    ) {
        Box(
            Modifier
                .size(280.dp)
                .graphicsLayer {
                    translationX = driftX
                    translationY = driftY
                    alpha = 0.45f
                }
                .blur(95.dp)
                .background(ElectricViolet.copy(alpha = 0.42f), CircleShape)
                .align(Alignment.TopEnd)
        )
        Box(
            Modifier
                .size(250.dp)
                .graphicsLayer {
                    translationX = -driftX * 0.7f
                    translationY = -driftY * 0.6f
                    alpha = 0.35f
                }
                .blur(90.dp)
                .background(ElectricCyan.copy(alpha = 0.30f), CircleShape)
                .align(Alignment.BottomStart)
        )
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = tween(140),
        label = "glassScale"
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
            .shadow(18.dp, shape, clip = false)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.105f),
                        Color.White.copy(alpha = 0.045f),
                        ElectricBlue.copy(alpha = 0.035f)
                    ),
                    start = Offset.Zero,
                    end = Offset(1200f, 1200f)
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.23f),
                        Color.White.copy(alpha = 0.07f),
                        ElectricBlue.copy(alpha = 0.14f)
                    )
                ),
                shape
            )
            .then(clickModifier)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun StoreVialImage(
    productKey: String,
    imageIndex: Map<String, String>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val imageUrl = remember(productKey, imageIndex) {
        StoreCatalogClient.resolveImage(imageIndex, productKey)
    }

    val bitmap by produceState<Bitmap?>(
        initialValue = null,
        key1 = imageUrl
    ) {
        value = imageUrl?.let { VialImageCache.load(context.cacheDir, it) }
    }

    val alpha by animateFloatAsState(
        targetValue = if (bitmap != null) 1f else 0f,
        animationSpec = tween(280),
        label = "imageAlpha"
    )

    Box(modifier, contentAlignment = Alignment.Center) {
        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alpha)
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Science,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxSize(0.38f)
            )
        }
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
        initialValue = -7f + phase,
        targetValue = 8f + phase,
        animationSpec = infiniteRepeatable(
            animation = tween(2_600 + phase * 90),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vialY"
    )

    StoreVialImage(
        productKey = productKey,
        imageIndex = imageIndex,
        modifier = modifier.graphicsLayer {
            translationY = y
            rotationZ = phase * 0.35f
        }
    )
}
