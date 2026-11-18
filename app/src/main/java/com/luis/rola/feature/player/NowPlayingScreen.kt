package com.luis.rola.feature.player

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import com.luis.rola.core.data.Song
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaMotion
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.timeSmall
import com.luis.rola.core.media.PlayerState
import com.luis.rola.core.media.Repeat
import com.luis.rola.core.media.SleepTimerOption
import com.luis.rola.core.media.formatDuration
import com.luis.rola.feature.library.Cover
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.math.sign

/**
 * Reproduciendo (SCR-51, nodos 36:1465 y 51:4482). Plana (DEC-27): la portada no lleva sombra.
 * Corazón de favoritas sobre la portada (DEC-29). Sin letra, ⋮ ni «Añadir a una lista» todavía.
 */
@Composable
fun NowPlayingScreen(
    state: PlayerState,
    colors: CoverColors,
    position: State<Long>,
    onClose: () -> Unit,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenTimer: () -> Unit,
    favorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val song = state.current ?: return
    val c = colors
    val fondo = MaterialTheme.colorScheme.background
    Box(modifier.fillMaxSize().background(fondo).testTag("reproduciendo")) {
        // Fondo dinámico desde la portada (FR-039).
        Box(
            Modifier
                .fillMaxWidth()
                .height(560.dp)
                .background(Brush.verticalGradient(listOf(c.container, c.container.copy(alpha = 0f)))),
        )
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = RolaSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClose) {
                    Icon(painterResource(R.drawable.ic_cerrar), stringResource(R.string.cerrar_reproductor), tint = c.content)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.reproduciendo_desde), style = MaterialTheme.typography.timeSmall, color = c.contentVariant)
                    Text(
                        stringResource(R.string.nav_biblioteca),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = c.content,
                    )
                }
                Spacer(Modifier.size(48.dp)) // lugar del ⋮ (tarea 08): mantiene el origen centrado
            }
            // La portada toma el alto que sobra (cuadrada, lo más grande que quepa): los controles quedan abajo (FR-007).
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = RolaSpacing.xxl, end = RolaSpacing.xxl, top = RolaSpacing.sm, bottom = RolaSpacing.xl),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.aspectRatio(1f)) {
                    SwipeableCover(song, state.hasNext, onNext, onPrevious, Modifier.fillMaxSize())
                    FavoriteButton(favorite, onToggleFavorite, Modifier.align(Alignment.BottomStart).padding(14.dp))
                }
            }
            Column(Modifier.padding(horizontal = RolaSpacing.xxl), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(song.title, style = MaterialTheme.typography.headlineMedium, color = c.content, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    song.artist ?: stringResource(R.string.artista_desconocido),
                    style = MaterialTheme.typography.bodyLarge,
                    color = c.contentVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            SeekBar(
                position,
                state.durationMs,
                c,
                onSeek,
                Modifier.padding(start = RolaSpacing.xxl, end = RolaSpacing.xxl, top = RolaSpacing.xxs),
            )
            Row(
                Modifier.fillMaxWidth().padding(start = RolaSpacing.xl, end = RolaSpacing.xl, top = RolaSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ModeButton(
                    R.drawable.ic_shuffle,
                    stringResource(if (state.shuffle) R.string.aleatorio_encendido else R.string.aleatorio_apagado),
                    state.shuffle,
                    c,
                    onShuffle,
                )
                IconButton(onPrevious, Modifier.size(56.dp)) {
                    Icon(painterResource(R.drawable.ic_skip_previous), stringResource(R.string.anterior), Modifier.size(36.dp), tint = c.content)
                }
                PlayButton(state.isPlaying, c, onToggle)
                IconButton(onNext, Modifier.size(56.dp), enabled = state.hasNext) {
                    Icon(
                        painterResource(R.drawable.ic_skip_next),
                        stringResource(R.string.siguiente),
                        Modifier.size(36.dp),
                        tint = if (state.hasNext) c.content else c.content.copy(alpha = 0.38f),
                    )
                }
                ModeButton(
                    if (state.repeat == Repeat.ONE) R.drawable.ic_repeat_one else R.drawable.ic_repeat,
                    stringResource(
                        when (state.repeat) {
                            Repeat.OFF -> R.string.repetir_apagado
                            Repeat.ALL -> R.string.repetir_todo
                            Repeat.ONE -> R.string.repetir_una
                        },
                    ),
                    state.repeat != Repeat.OFF,
                    c,
                    onRepeat,
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(start = 40.dp, end = 40.dp, top = RolaSpacing.lg, bottom = RolaSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val timerDesc = when {
                    state.timer == SleepTimerOption.END_OF_SONG -> stringResource(R.string.temporizador_al_terminar)
                    state.timerEndsAtMs != null -> stringResource(
                        R.string.temporizador_activo,
                        formatDuration(state.timerEndsAtMs - SystemClock.elapsedRealtime()),
                    )
                    else -> stringResource(R.string.temporizador)
                }
                ModeButton(R.drawable.ic_timer, timerDesc, state.timer != SleepTimerOption.OFF, c, onOpenTimer)
                IconButton(onOpenQueue) {
                    Icon(painterResource(R.drawable.ic_queue), stringResource(R.string.abrir_cola), tint = c.contentVariant)
                }
            }
        }
    }
}

/**
 * Portada plana (DEC-27) que se desliza a los lados para siguiente/anterior con el umbral de la mini tarjeta (FR-049).
 * Sigue al dedo y, al soltar, sale por su lado y entra la nueva en fundido, o vuelve con un resorte.
 * Con reducir movimiento no se mueve: el gesto solo cambia la canción.
 */
@Composable
private fun SwipeableCover(song: Song, hasNext: Boolean, onNext: () -> Unit, onPrevious: () -> Unit, modifier: Modifier) {
    val reduce = LocalReduceMotion.current
    val density = LocalDensity.current.density
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val entrada = remember { Animatable(1f) }
    var arrastre by remember { mutableFloatStateOf(0f) }
    var ancho by remember { mutableIntStateOf(1) }
    // Toques en la caja quieta y desplazamiento en la portada: la distancia no se mezcla con el movimiento.
    Box(
        modifier
            .onSizeChanged { ancho = it.width }
            .draggable(
                rememberDraggableState { d ->
                    arrastre += d
                    // Sin despachar: ningún salto llega después de la animación de soltar.
                    if (!reduce) scope.launch(start = CoroutineStart.UNDISPATCHED) { offsetX.snapTo(arrastre) }
                },
                Orientation.Horizontal,
                onDragStarted = { arrastre = 0f },
                onDragStopped = { v ->
                    if (swipeCommits(arrastre, v, ancho.toFloat(), density) && (arrastre > 0 || hasNext)) {
                        if (!reduce) offsetX.animateTo(sign(arrastre) * ancho, tween(200, easing = RolaMotion.EaseOut))
                        if (arrastre < 0) onNext() else onPrevious()
                        offsetX.snapTo(0f)
                        entrada.snapTo(0f)
                        entrada.animateTo(1f, tween(RolaMotion.MEDIA))
                    } else {
                        offsetX.animateTo(0f, RolaMotion.o(reduce, RolaMotion.resorte()))
                    }
                },
            ),
    ) {
        Cover(
            song,
            Modifier.fillMaxSize().graphicsLayer {
                translationX = offsetX.value
                alpha = entrada.value
            },
            shape = MaterialTheme.shapes.extraLarge,
        )
    }
}

/** Botón de modo: [activo] con el acento de la portada; su descripción dice el estado (FR-016). */
@Composable
private fun ModeButton(icon: Int, description: String, activo: Boolean, c: CoverColors, onClick: () -> Unit) {
    IconButton(onClick) {
        Icon(painterResource(icon), description, tint = if (activo) c.accent else c.contentVariant)
    }
}

@Composable
private fun PlayButton(isPlaying: Boolean, c: CoverColors, onToggle: () -> Unit) {
    val reduce = LocalReduceMotion.current
    IconButton(onToggle, Modifier.size(80.dp)) {
        Box(Modifier.size(80.dp).clip(CircleShape).background(c.play), Alignment.Center) {
            AnimatedContent(
                isPlaying,
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
                    Modifier.size(36.dp),
                    tint = c.onPlay,
                )
            }
        }
    }
}
