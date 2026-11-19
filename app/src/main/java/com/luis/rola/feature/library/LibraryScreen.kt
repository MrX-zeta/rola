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
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.luis.rola.R
import com.luis.rola.core.data.FAVORITAS_ID
import com.luis.rola.core.data.PlaylistSummary
import com.luis.rola.feature.playlists.PlaylistAction
import com.luis.rola.feature.playlists.PlaylistsTab
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
 * Biblioteca con los chips Listas (SCR-23) y Canciones (SCR-20); Álbumes y Artistas llegan en la tarea 06.
 * Canciones: ⋮ y pulsación larga abren el menú (SCR-54); sin filtros ni abecedario todavía.
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
    favoriteIds: Set<Long> = emptySet(),
    favorites: PlaylistSummary = PlaylistSummary(FAVORITAS_ID, "", 0, emptyList()),
    playlists: List<PlaylistSummary> = emptyList(),
    onCreatePlaylist: () -> Unit = {},
    onOpenPlaylist: (Long) -> Unit = {},
    onPlaylistAction: (PlaylistSummary, PlaylistAction) -> Unit = { _, _ -> },
    onSongAction: (Song, SongAction) -> Unit = { _, _ -> },
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
        // Por defecto Canciones, como antes de la tarea 05; se recuerda al rotar.
        var enListas by rememberSaveable { mutableStateOf(false) }
        Row(
            Modifier.padding(start = RolaSpacing.lg, top = RolaSpacing.xxs, bottom = RolaSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(RolaSpacing.xs),
        ) {
            Chip(R.string.chip_listas, enListas, "chip_listas") { enListas = true }
            Chip(R.string.chip_canciones, !enListas, "chip_canciones") { enListas = false }
        }
        when {
            enListas -> PlaylistsTab(favorites, playlists, onCreatePlaylist, onOpenPlaylist, onPlaylistAction, contentPadding)
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
                        SongItem(
                            song,
                            isCurrent = song.id == currentId,
                            place = MenuPlace.Library,
                            favorite = song.id in favoriteIds,
                            onAction = { onSongAction(song, it) },
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

/** Chip de Biblioteca (Figma 32:1147): lima si está elegido, `surfaceContainer` si no. */
@Composable
private fun Chip(texto: Int, elegido: Boolean, tag: String, onClick: () -> Unit) {
    Text(
        stringResource(texto),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (elegido) FontWeight.SemiBold else FontWeight.Medium),
        color = if (elegido) RolaTheme.colors.onAccent else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .minimumInteractiveComponentSize() // toque de 48 dp (INV-06); el chip se ve de 36 dp
            .clip(MaterialTheme.shapes.pill)
            .background(if (elegido) RolaTheme.colors.accent else MaterialTheme.colorScheme.surfaceContainer)
            .selectable(elegido, role = Role.Tab, onClick = onClick)
            .padding(horizontal = RolaSpacing.md, vertical = RolaSpacing.xs)
            .testTag(tag),
    )
}
