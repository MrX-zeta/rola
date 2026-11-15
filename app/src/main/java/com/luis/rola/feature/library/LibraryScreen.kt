package com.luis.rola.feature.library

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.luis.rola.R
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaMotion
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.esqueleto
import com.luis.rola.core.designsystem.pill

// Despliegue al entrar (DEC-28 despliegue_listas): 7 escalones × 25 ms + 125 ms por fila = 300 ms.
private const val FILAS_CASCADA = 8
private const val ESCALON_MS = 25
private const val FILA_MS = 125
private const val CASCADA_MS = (FILAS_CASCADA - 1) * ESCALON_MS + FILA_MS

/**
 * Biblioteca · Canciones (SCR-20). Solo el chip Canciones; sin filtros, ⋮ ni abecedario en esta tarea.
 * Cada vez que [visible] pasa a `true` las filas visibles entran en cascada; si aún cargan, cuando llegan.
 */
@Composable
fun LibraryScreen(
    songs: List<Song>?,
    currentId: Long?,
    onSongClick: (index: Int) -> Unit,
    onShuffle: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    visible: Boolean = true,
) {
    val c = MaterialTheme.colorScheme
    val lista = rememberLazyListState()
    val reduce = LocalReduceMotion.current
    val listo = !songs.isNullOrEmpty()
    // Se guarda para que al rotar no se repita la cascada.
    var desplegada by rememberSaveable { mutableStateOf(false) }
    // Milisegundos de la cascada; arranca en 0 para que el primer cuadro ya muestre las filas en su inicio.
    val reloj = remember { Animatable(if (reduce || desplegada) CASCADA_MS.toFloat() else 0f) }
    var primera by remember { mutableIntStateOf(0) }
    LaunchedEffect(visible, listo, reduce) {
        when {
            reduce -> reloj.snapTo(CASCADA_MS.toFloat())
            !visible -> {
                desplegada = false
                delay(RolaMotion.RAPIDA.toLong()) // tras el fundido de salida, ya sin dibujarse
                primera = lista.firstVisibleItemIndex
                reloj.snapTo(0f) // así la próxima entrada arranca en su inicio desde el primer cuadro
            }
            listo && !desplegada -> {
                desplegada = true
                primera = lista.firstVisibleItemIndex
                reloj.snapTo(0f)
                withFrameNanos {} // DEC-28 arranque_en_primer_cuadro: cuenta desde que las filas ya están dibujadas
                reloj.animateTo(CASCADA_MS.toFloat(), tween(CASCADA_MS, easing = LinearEasing))
            }
        }
    }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            stringResource(R.string.nav_biblioteca),
            style = MaterialTheme.typography.headlineMedium,
            color = c.onSurface,
            modifier = Modifier.padding(horizontal = RolaSpacing.lg, vertical = RolaSpacing.lg),
        )
        Text(
            stringResource(R.string.chip_canciones),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = RolaTheme.colors.onAccent,
            modifier = Modifier
                .padding(start = RolaSpacing.lg, top = RolaSpacing.xxs, bottom = RolaSpacing.xs)
                .background(RolaTheme.colors.accent, MaterialTheme.shapes.pill)
                .padding(horizontal = RolaSpacing.md, vertical = RolaSpacing.xs),
        )
        when {
            songs == null -> ListaCargando()
            songs.isEmpty() -> Box(Modifier.fillMaxSize().padding(RolaSpacing.xl), Alignment.Center) {
                Text(stringResource(R.string.sin_canciones), style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant)
            }
            else -> {
                Row(Modifier.padding(start = RolaSpacing.lg, end = RolaSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        pluralStringResource(R.plurals.resumen_canciones, songs.size, songs.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = c.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onShuffle) {
                        Icon(painterResource(R.drawable.ic_shuffle), stringResource(R.string.aleatorio), tint = c.onSurface)
                    }
                }
                LazyColumn(state = lista, contentPadding = contentPadding) {
                    itemsIndexed(songs, key = { _, s -> s.id }) { i, song ->
                        SongRow(
                            song,
                            isCurrent = song.id == currentId,
                            // Solo se lee al dibujar: la cascada no recompone. Las filas fuera de las 8 entran normales.
                            modifier = Modifier.graphicsLayer {
                                val k = i - primera
                                val p = if (k in 0 until FILAS_CASCADA) {
                                    RolaMotion.EaseOut.transform(((reloj.value - k * ESCALON_MS) / FILA_MS).coerceIn(0f, 1f))
                                } else {
                                    1f
                                }
                                alpha = p
                                translationY = (1f - p) * 8.dp.toPx()
                                // Sin capa fuera de pantalla por fila (8 por cuadro al entrar, medido en T02e).
                                compositingStrategy = CompositingStrategy.ModulateAlpha
                            },
                        ) { onSongClick(i) }
                    }
                }
            }
        }
    }
}

/** Filas esqueleto con la forma de [SongRow]; solo si la carga pasa de ~300 ms, para no parpadear (DEC-09). */
@Composable
private fun ListaCargando() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }
    if (!visible) return
    val descripcion = stringResource(R.string.cargando_canciones)
    Column(Modifier.padding(top = RolaSpacing.xxl + RolaSpacing.xs).semantics { contentDescription = descripcion }) {
        repeat(8) {
            Row(
                Modifier.heightIn(min = 64.dp).padding(horizontal = RolaSpacing.lg, vertical = RolaSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(48.dp).esqueleto(MaterialTheme.shapes.extraSmall))
                Column(verticalArrangement = Arrangement.spacedBy(RolaSpacing.xs)) {
                    Box(Modifier.size(width = 160.dp, height = 14.dp).esqueleto(MaterialTheme.shapes.extraSmall))
                    Box(Modifier.size(width = 96.dp, height = 12.dp).esqueleto(MaterialTheme.shapes.extraSmall))
                }
            }
        }
    }
}

@Composable
private fun SongRow(song: Song, isCurrent: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = RolaSpacing.lg, vertical = RolaSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Cover(song, Modifier.size(48.dp), MaterialTheme.shapes.extraSmall)
        Column {
            Text(
                song.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isCurrent) c.primary else c.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                song.artist ?: stringResource(R.string.artista_desconocido),
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
