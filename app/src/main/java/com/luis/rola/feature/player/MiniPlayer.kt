package com.luis.rola.feature.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * Mini reproductor (SCR-50). Sin canción cargada no ocupa espacio.
 * Entra desde abajo y sale por abajo, más rápido (DEC-28); con reducir movimiento solo hay fundido.
 * Tocar la tarjeta no hace nada todavía (tarea 03).
 */
@Composable
fun MiniPlayer(state: PlayerState, onToggle: () -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier) {
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
        Tarjeta(s, song, onToggle, onNext, Modifier.padding(bottom = RolaSpacing.sm))
    }
}

@Composable
private fun Tarjeta(state: PlayerState, song: Song, onToggle: () -> Unit, onNext: () -> Unit, modifier: Modifier) {
    val c = rememberCoverColors(song)
    val reduce = LocalReduceMotion.current
    Column(modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(c.container)) {
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
        Box(Modifier.fillMaxWidth().height(3.dp).background(c.track).semantics { contentDescription = progreso }) {
            val f = if (state.durationMs > 0) (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f
            Box(Modifier.fillMaxWidth(f).fillMaxHeight().background(c.accent))
        }
    }
}
