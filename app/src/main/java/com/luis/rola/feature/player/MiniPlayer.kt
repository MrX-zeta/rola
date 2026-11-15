package com.luis.rola.feature.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaMotion
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.media.PlayerState
import com.luis.rola.core.media.formatDuration
import com.luis.rola.feature.library.Cover
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

/**
 * Mini reproductor (SCR-50). Sin canción cargada no ocupa espacio.
 * Entra desde abajo y sale por abajo, más rápido (DEC-28); con reducir movimiento solo hay fundido.
 * Tocarla o deslizarla hacia arriba abre Reproduciendo; a los lados, siguiente y anterior (DEC-04).
 */
@Composable
fun MiniPlayer(
    state: PlayerState,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    onPrevious: () -> Unit = {},
    sheet: PlayerSheetState? = null,
) {
    val reduce = LocalReduceMotion.current
    AnimatedContent(
        targetState = state,
        contentKey = { it.current != null }, // cambiar de canción no anima: solo aparecer y desaparecer
        transitionSpec = {
            val entra = if (reduce) {
                fadeIn(tween(RolaMotion.MEDIA))
            } else {
                slideInVertically(tween(RolaMotion.LENTA, easing = RolaMotion.EaseOut)) { it } + fadeIn(tween(RolaMotion.MEDIA))
            }
            val sale = if (reduce) {
                fadeOut(tween(RolaMotion.RAPIDA))
            } else {
                slideOutVertically(tween(200, easing = RolaMotion.EaseOut)) { it } + fadeOut(tween(RolaMotion.RAPIDA))
            }
            (entra togetherWith sale).using(
                SizeTransform(clip = false) { _, _ -> RolaMotion.o(reduce, tween(RolaMotion.LENTA, easing = RolaMotion.EaseOut)) },
            )
        },
        modifier = modifier,
        label = "mini",
    ) { s ->
        val song = s.current ?: return@AnimatedContent
        Tarjeta(s, song, onToggle, onNext, onPrevious, sheet, Modifier.padding(bottom = RolaSpacing.sm))
    }
}

@Composable
private fun Tarjeta(
    state: PlayerState,
    song: Song,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    sheet: PlayerSheetState?,
    modifier: Modifier,
) {
    val c = rememberCoverColors(song)
    val reduce = LocalReduceMotion.current
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val entrada = remember { Animatable(1f) } // fundido de la canción nueva tras un gesto
    val hasNext by rememberUpdatedState(state.hasNext)
    val next by rememberUpdatedState(onNext)
    val previous by rememberUpdatedState(onPrevious)
    val position = rememberPosition(state)
    Box(
        modifier
            .fillMaxWidth()
            .testTag("mini")
            .onPlaced { sheet?.cardBounds = it.boundsInRoot() }
            .graphicsLayer {
                translationX = offsetX.value
                // Mientras Reproduciendo crece, la capa dibuja una copia de la tarjeta: esta se oculta.
                alpha = if ((sheet?.expansion?.value ?: 0f) > 0f) 0f else entrada.value
            }
            .clickable(enabled = sheet != null, onClickLabel = stringResource(R.string.abrir_reproductor)) { sheet?.requestOpen() }
            .pointerInput(sheet, reduce) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val vt = VelocityTracker().apply { addPosition(down.uptimeMillis, down.position) }
                    var total = Offset.Zero
                    val primero = awaitTouchSlopOrCancellation(down.id) { change, over ->
                        change.consume()
                        total = over
                    } ?: return@awaitEachGesture
                    val horizontal = abs(total.x) > abs(total.y) // eje dominante: nunca los dos (FR-021)
                    if (!horizontal && sheet == null) return@awaitEachGesture
                    val travel = if (horizontal) size.width.toFloat() else sheet!!.cardBounds.top.coerceAtLeast(1f)
                    drag(primero.id) { ch ->
                        total += ch.positionChange()
                        vt.addPosition(ch.uptimeMillis, ch.position)
                        ch.consume()
                        if (reduce) return@drag // reducir movimiento: nada sigue al dedo
                        scope.launch {
                            if (horizontal) offsetX.snapTo(total.x) else sheet!!.expansion.snapTo((-total.y / travel).coerceIn(0f, 1f))
                        }
                    }
                    val v = vt.calculateVelocity()
                    scope.launch {
                        if (!horizontal) {
                            if (-total.y > 0f && swipeCommits(-total.y, -v.y, travel, density)) sheet!!.open(reduce, -v.y / travel) else sheet!!.cancel(reduce)
                        } else if (swipeCommits(total.x, v.x, travel, density) && (total.x > 0 || hasNext)) {
                            if (!reduce) offsetX.animateTo(sign(total.x) * travel, tween(200, easing = RolaMotion.EaseOut))
                            if (total.x < 0) next() else previous()
                            offsetX.snapTo(0f)
                            entrada.snapTo(0f)
                            entrada.animateTo(1f, tween(RolaMotion.MEDIA))
                        } else {
                            offsetX.animateTo(0f, RolaMotion.o(reduce, RolaMotion.resorte()))
                        }
                    }
                }
            },
    ) {
        MiniCardContent(state, song, c, position, onToggle, onNext)
    }
}

/** Lo que se ve de la tarjeta; la capa de Reproduciendo dibuja una copia mientras crece. */
@Composable
fun MiniCardContent(
    state: PlayerState,
    song: Song,
    c: CoverColors,
    position: State<Long>,
    onToggle: () -> Unit,
    onNext: () -> Unit,
) {
    val reduce = LocalReduceMotion.current
    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(c.container)) {
        Row(
            Modifier.padding(RolaSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Cover(song, Modifier.size(48.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    song.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = c.content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    song.artist ?: stringResource(R.string.artista_desconocido),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.contentVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onNext, enabled = state.hasNext) {
                Icon(
                    painterResource(R.drawable.ic_skip_next),
                    stringResource(R.string.siguiente),
                    tint = if (state.hasNext) c.content else c.content.copy(alpha = 0.38f),
                )
            }
            IconButton(onToggle) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(c.accent), Alignment.Center) {
                    AnimatedContent(
                        state.isPlaying,
                        transitionSpec = {
                            val entra = fadeIn(tween(RolaMotion.MEDIA))
                            (if (reduce) entra else entra + scaleIn(tween(RolaMotion.MEDIA, easing = RolaMotion.EaseOut), 0.9f))
                                .togetherWith(fadeOut(tween(RolaMotion.RAPIDA)))
                        },
                        label = "reproducir",
                    ) { sonando ->
                        Icon(
                            painterResource(if (sonando) R.drawable.ic_pause else R.drawable.ic_play),
                            stringResource(if (sonando) R.string.pausar else R.string.reproducir),
                            tint = c.onAccent,
                        )
                    }
                }
            }
        }
        val progreso = stringResource(R.string.progreso, formatDuration(state.positionMs), formatDuration(state.durationMs))
        // La línea avanza en cada cuadro solo en el dibujo (plan 006).
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .semantics { contentDescription = progreso }
                .drawBehind {
                    drawRect(c.track)
                    drawRect(c.accent, size = size.copy(width = size.width * fraction(position.value, state.durationMs)))
                },
        )
    }
}
