package com.luis.rola.feature.player

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.ensureContrast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Colores de la mini tarjeta (PEND-08 A). Todo texto sobre `container` contrasta ≥ 4.5:1 (INV-06). */
data class CoverColors(
    val container: Color,
    val accent: Color,
    val onAccent: Color,
    val content: Color,
    val contentVariant: Color,
    val track: Color,
)

/** Colores de la portada con Palette; sin portada o sin colores útiles, [fallback]. */
fun coverColors(bitmap: Bitmap, fallback: CoverColors): CoverColors {
    val p = Palette.from(bitmap).generate()
    val bg = (p.darkMutedSwatch ?: p.darkVibrantSwatch)?.rgb?.let(::Color) ?: return fallback
    val vivo = (p.lightVibrantSwatch ?: p.vibrantSwatch ?: p.lightMutedSwatch)?.rgb?.let(::Color) ?: fallback.accent
    val accent = ensureContrast(vivo, bg, 3.0) // 3:1 para el botón y la línea (componentes, no texto)
    val content = ensureContrast(fallback.content, bg)
    return CoverColors(
        container = bg,
        accent = accent,
        onAccent = ensureContrast(bg, accent),
        content = content,
        contentVariant = ensureContrast(fallback.contentVariant, bg),
        track = content.copy(alpha = 0.12f),
    )
}

@Composable
fun rememberCoverColors(song: Song): CoverColors {
    val c = MaterialTheme.colorScheme
    val fallback = CoverColors(
        container = c.surfaceContainer,
        accent = RolaTheme.colors.accent,
        onAccent = RolaTheme.colors.onAccent,
        content = c.onSurface,
        contentVariant = c.onSurfaceVariant,
        track = c.outline,
    )
    val context = LocalContext.current
    val colors by produceState(fallback, song.id, fallback) {
        value = fallback
        val request = ImageRequest.Builder(context).data(song).allowHardware(false).build()
        val bitmap = (context.imageLoader.execute(request) as? SuccessResult)?.image?.toBitmap()
        if (bitmap != null) value = withContext(Dispatchers.Default) { coverColors(bitmap, fallback) }
    }
    return colors
}
