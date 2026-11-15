package com.luis.rola.feature.player

import android.os.SystemClock
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.timeSmall
import com.luis.rola.core.media.PlayerState
import com.luis.rola.core.media.formatDuration

/**
 * Posición que avanza en cada cuadro mientras suena, sin publicar estado (plan 006).
 * Léela solo al dibujar o detrás de un [derivedStateOf]: así el avance no recompone la pantalla.
 */
@Composable
fun rememberPosition(state: PlayerState): State<Long> {
    val reduce = LocalReduceMotion.current
    val pos = remember { mutableLongStateOf(state.positionMs) }
    LaunchedEffect(state.positionMs, state.positionAtMs, state.isPlaying, state.durationMs, reduce) {
        fun lee(): Long {
            val corriendo = if (state.isPlaying) SystemClock.elapsedRealtime() - state.positionAtMs else 0
            val p = (state.positionMs + corriendo).coerceAtMost(if (state.durationMs > 0) state.durationMs else Long.MAX_VALUE)
            return if (reduce) p / 1000 * 1000 else p // reducir movimiento: salta de segundo en segundo
        }
        pos.longValue = lee()
        // Sin duración conocida no hay barra que avanzar: no se piden cuadros.
        while (state.isPlaying && state.durationMs > 0) withFrameMillis { pos.longValue = lee() }
    }
    return pos
}

/** Fracción 0..1 de [position] en [durationMs]; 0 sin duración conocida. */
fun fraction(position: Long, durationMs: Long) = if (durationMs > 0) (position.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

/**
 * Barra de avance de Reproduciendo (SCR-51): pista de 4 dp, control de 16 dp y tiempos en Geist Mono.
 * Se toca o se arrastra; la canción salta al soltar. 48 dp de alto táctil (INV-06).
 */
@Composable
fun SeekBar(
    position: State<Long>,
    durationMs: Long,
    colors: CoverColors,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var buscando by remember { mutableStateOf<Float?>(null) }
    val activa = durationMs > 0
    // Los textos y la semántica cambian una vez por segundo; el dibujo, en cada cuadro.
    val segundo by remember(position) { derivedStateOf { position.value / 1000 } }
    val actual = buscando?.let { (it * durationMs).toLong() } ?: (segundo * 1000)
    val desc = stringResource(R.string.barra_avance)
    val estado = stringResource(R.string.progreso, formatDuration(actual), formatDuration(durationMs))
    // La pista va centrada en 48 dp; el espacio negativo deja los tiempos a 8 dp de la pista, como Figma.
    Column(modifier, verticalArrangement = Arrangement.spacedBy((-8).dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("barraAvance")
                .semantics {
                    contentDescription = desc
                    stateDescription = estado
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction(actual, durationMs), 0f..1f)
                    if (activa) {
                        setProgress { onSeek((it.coerceIn(0f, 1f) * durationMs).toLong()); true }
                    }
                }
                .pointerInput(activa, durationMs) {
                    if (!activa) return@pointerInput
                    fun f(x: Float) = (x / size.width).coerceIn(0f, 1f)
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        buscando = f(down.position.x)
                        horizontalDrag(down.id) {
                            buscando = f(it.position.x)
                            it.consume()
                        }
                        buscando?.let { onSeek((it * durationMs).toLong()) }
                        buscando = null
                    }
                }
                .drawBehind {
                    val f = buscando ?: fraction(position.value, durationMs)
                    val h = 4.dp.toPx()
                    val top = (size.height - h) / 2
                    drawRoundRect(colors.track, Offset(0f, top), Size(size.width, h), CornerRadius(h / 2))
                    drawRoundRect(colors.accent, Offset(0f, top), Size(size.width * f, h), CornerRadius(h / 2))
                    if (activa) drawCircle(colors.accent, 8.dp.toPx(), Offset(size.width * f, size.height / 2))
                },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val estilo = MaterialTheme.typography.timeSmall
            Text(formatDuration(actual), style = estilo, color = colors.contentVariant)
            Text(formatDuration(durationMs), style = estilo, color = colors.contentVariant)
        }
    }
}
