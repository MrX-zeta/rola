package com.luis.rola.feature.player

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaMotion
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.ensureContrast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Colores de la mini tarjeta, Reproduciendo y la fila actual de Cola (PEND-08 A). Todo texto sobre `container` contrasta ≥ 4.5:1 (INV-06). */
data class CoverColors(
    val container: Color,
    val accent: Color,
    val onAccent: Color,
    val content: Color,
    val contentVariant: Color,
    val track: Color,
    /** Relleno del botón de reproducir: el acento de la portada o, sin portada, el lima del tema (DEC-16). */
    val play: Color,
    val onPlay: Color,
)

/** Colores de la portada con Palette; sin portada o sin colores útiles, [fallback]. */
fun coverColors(bitmap: Bitmap, fallback: CoverColors, oscuro: Boolean): CoverColors {
    val p = Palette.from(bitmap).generate()
    val bg: Color
    val vivo: Color
    if (oscuro) {
        // DEC-15: si solo hay un tono vivo, se oscurece un paso para acercarse a Figma.
        bg = p.darkMutedSwatch?.rgb?.let(::Color)
            ?: p.darkVibrantSwatch?.rgb?.let { lerp(Color(it), Color.Black, 0.45f) }
            // Portadas claras y saturadas (p. ej. naranja) no traen tonos oscuros: se oscurece el tono vivo.
            ?: (p.vibrantSwatch ?: p.dominantSwatch)?.rgb?.let { lerp(Color(it), Color.Black, 0.7f) }
            ?: return fallback
        vivo = (p.lightVibrantSwatch ?: p.vibrantSwatch ?: p.lightMutedSwatch)?.rgb?.let(::Color) ?: fallback.accent
    } else {
        // DEC-16 acento_dinamico_claro: contenedor muy claro teñido con el tono de la portada y acento oscuro.
        val tono = (p.vibrantSwatch ?: p.dominantSwatch)?.rgb?.let(::Color) ?: return fallback
        bg = lerp(tono, Color.White, 0.82f)
        vivo = (p.darkVibrantSwatch ?: p.vibrantSwatch ?: p.darkMutedSwatch)?.rgb?.let(::Color) ?: fallback.accent
    }
    val accent = ensureContrast(vivo, bg, 3.0) // 3:1 para el botón y la línea (componentes, no texto)
    val content = ensureContrast(fallback.content, bg)
    return CoverColors(
        container = bg,
        accent = accent,
        onAccent = ensureContrast(bg, accent),
        content = content,
        contentVariant = ensureContrast(fallback.contentVariant, bg),
        track = content.copy(alpha = 0.12f),
        play = accent,
        onPlay = ensureContrast(bg, accent),
    )
}

@Composable
fun rememberCoverColors(song: Song): CoverColors {
    val c = MaterialTheme.colorScheme
    val fallback = CoverColors(
        container = c.surfaceContainer,
        // primary y no accent: el acento también pinta texto y la barra de avance, y el lima no contrasta en claro
        // (DEC-16: primary es lima en oscuro y oliva en claro).
        accent = c.primary,
        onAccent = c.onPrimary,
        content = c.onSurface,
        contentVariant = c.onSurfaceVariant,
        track = c.outline,
        // Sin portada el botón sí es lima en ambos temas: su ícono oscuro contrasta (DEC-16).
        play = RolaTheme.colors.accent,
        onPlay = RolaTheme.colors.onAccent,
    )
    val oscuro = c.background.luminance() < 0.5f
    val context = LocalContext.current
    val colors by produceState(fallback, song.id, fallback) {
        value = fallback
        // Con tamaño fijo: si la portada grande ya está en caché, la reutiliza en vez de decodificarla otra vez.
        val request = ImageRequest.Builder(context).data(song).size(512).allowHardware(false).build()
        val bitmap = (context.imageLoader.execute(request) as? SuccessResult)?.image?.toBitmap()
        if (bitmap != null) value = withContext(Dispatchers.Default) { coverColors(bitmap, fallback, oscuro) }
    }
    // FR-042: el cambio de color entre canciones es un fundido corto.
    @Composable fun Color.fundido() = animateColorAsState(this, tween(RolaMotion.MEDIA), label = "portada").value
    return CoverColors(
        container = colors.container.fundido(),
        accent = colors.accent.fundido(),
        onAccent = colors.onAccent.fundido(),
        content = colors.content.fundido(),
        contentVariant = colors.contentVariant.fundido(),
        track = colors.track.fundido(),
        play = colors.play.fundido(),
        onPlay = colors.onPlay.fundido(),
    )
}
