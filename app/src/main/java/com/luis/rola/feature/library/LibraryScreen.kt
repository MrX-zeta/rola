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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.luis.rola.core.data.Album
import com.luis.rola.core.data.Artist
import com.luis.rola.core.data.SongSort
import com.luis.rola.core.data.TitleOrder
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

/** Chips de Biblioteca en el orden de DEC-05. */
enum class LibraryTab(val label: Int, val tag: String) {
    Listas(R.string.chip_listas, "chip_listas"),
    Albumes(R.string.chip_albumes, "chip_albumes"),
    Artistas(R.string.chip_artistas, "chip_artistas"),
    Canciones(R.string.chip_canciones, "chip_canciones"),
}

/**
 * Biblioteca con los chips Listas (SCR-23), Álbumes (SCR-21), Artistas (SCR-22) y Canciones (SCR-20); abre en Canciones.
 * Canciones: ⋮ y pulsación larga abren el menú (SCR-54); el botón de filtros abre la hoja de orden (SCR-26).
 * Cada vez que [visible] pasa a `true` o se cambia de chip, las filas visibles entran en cascada (DEC-28).
 * Cada chip guarda su posición mientras la app esté abierta (FR-002).
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
    sort: SongSort = SongSort(),
    onSortChange: (SongSort) -> Unit = {},
    albums: List<Album>? = null,
    artists: List<Artist>? = null,
    onOpenAlbum: (Long) -> Unit = {},
    onOpenArtist: (String?) -> Unit = {},
) {
    val c = MaterialTheme.colorScheme
    val reduce = LocalReduceMotion.current
    var pestana by rememberSaveable { mutableStateOf(LibraryTab.Canciones) }
    val listaCanciones = rememberLazyListState()
    val listaListas = rememberLazyListState()
    val listaArtistas = rememberLazyListState()
    val cuadricula = rememberLazyGridState()
    fun primeraVisible() = when (pestana) {
        LibraryTab.Listas -> listaListas.firstVisibleItemIndex
        LibraryTab.Albumes -> cuadricula.firstVisibleItemIndex
        LibraryTab.Artistas -> listaArtistas.firstVisibleItemIndex
        LibraryTab.Canciones -> listaCanciones.firstVisibleItemIndex
    }
    val listo = when (pestana) {
        LibraryTab.Listas -> true
        LibraryTab.Albumes -> !albums.isNullOrEmpty()
        LibraryTab.Artistas -> !artists.isNullOrEmpty()
        LibraryTab.Canciones -> !songs.isNullOrEmpty()
    }
    // Chip ya desplegado (se guarda para que al rotar no se repita la cascada); null al salir de Biblioteca.
    var desplegada by rememberSaveable { mutableStateOf<LibraryTab?>(null) }
    // Milisegundos de la cascada; arranca en 0 para que el primer cuadro ya muestre las filas en su inicio.
    val reloj = remember { Animatable(if (reduce || desplegada != null) CASCADA_MS.toFloat() else 0f) }
    var primera by remember { mutableIntStateOf(0) }
    LaunchedEffect(visible, listo, reduce, pestana) {
        when {
            reduce -> reloj.snapTo(CASCADA_MS.toFloat())
            !visible -> {
                desplegada = null
                delay(RolaMotion.RAPIDA.toLong()) // tras el fundido de salida, ya sin dibujarse
                primera = primeraVisible()
                reloj.snapTo(0f) // así la próxima entrada arranca en su inicio desde el primer cuadro
            }
            listo && desplegada != pestana -> {
                desplegada = pestana
                primera = primeraVisible()
                reloj.snapTo(0f)
                withFrameNanos {} // DEC-28 arranque_en_primer_cuadro: cuenta desde que las filas ya están dibujadas
                reloj.animateTo(CASCADA_MS.toFloat(), tween(CASCADA_MS, easing = LinearEasing))
            }
        }
    }
    val enCascada = { i: Int -> Modifier.cascada(i, { primera }, reloj) }
    val scope = rememberCoroutineScope()
    var ordenando by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            stringResource(R.string.nav_biblioteca),
            style = MaterialTheme.typography.headlineMedium,
            color = c.onSurface,
            modifier = Modifier.padding(horizontal = RolaSpacing.lg, vertical = RolaSpacing.lg),
        )
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(start = RolaSpacing.lg, end = RolaSpacing.lg, top = RolaSpacing.xxs, bottom = RolaSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(RolaSpacing.xs),
        ) {
            LibraryTab.entries.forEach { t -> Chip(t.label, pestana == t, t.tag) { pestana = t } }
        }
        when {
            pestana == LibraryTab.Listas ->
                PlaylistsTab(favorites, playlists, onCreatePlaylist, onOpenPlaylist, onPlaylistAction, contentPadding, listaListas, enCascada)
            songs == null -> ListaCargando()
            songs.isEmpty() -> Box(Modifier.fillMaxSize().padding(RolaSpacing.xl), Alignment.Center) {
                Text(stringResource(R.string.sin_canciones), style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant)
            }
            pestana == LibraryTab.Albumes -> if (albums == null) {
                ListaCargando()
            } else {
                ConAbecedario(
                    remember(albums) { firstIndexBySection(albums) { TitleOrder.sectionOf(it.name.orEmpty()) } },
                    { albums.getOrNull(cuadricula.firstVisibleItemIndex)?.let { TitleOrder.sectionOf(it.name.orEmpty()) } },
                    { i -> scope.launch { cuadricula.scrollToItem(i) } },
                ) { AlbumsTab(albums, cuadricula, onOpenAlbum, { primera }, reloj, contentPadding) }
            }
            pestana == LibraryTab.Artistas -> if (artists == null) {
                ListaCargando()
            } else {
                ConAbecedario(
                    remember(artists) { firstIndexBySection(artists) { TitleOrder.sectionOf(it.name.orEmpty()) } },
                    { artists.getOrNull(listaArtistas.firstVisibleItemIndex)?.let { TitleOrder.sectionOf(it.name.orEmpty()) } },
                    { i -> scope.launch { listaArtistas.scrollToItem(i) } },
                ) { ArtistsTab(artists, listaArtistas, onOpenArtist, { primera }, reloj, contentPadding) }
            }
            else -> {
                Row(Modifier.padding(start = RolaSpacing.lg, end = RolaSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        pluralStringResource(R.plurals.resumen_canciones, songs.size, songs.size, sortSummary(sort)),
                        style = MaterialTheme.typography.labelLarge,
                        color = c.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onShuffle) {
                        Icon(painterResource(R.drawable.ic_shuffle), stringResource(R.string.aleatorio), tint = c.onSurface)
                    }
                    IconButton({ ordenando = true }) {
                        Icon(painterResource(R.drawable.ic_tune), stringResource(R.string.ordenar_y_filtrar), tint = c.onSurface)
                    }
                }
                ConAbecedario(
                    remember(songs, sort) { firstIndexBySection(songs, sort::sectionOf) },
                    { songs.getOrNull(listaCanciones.firstVisibleItemIndex)?.let(sort::sectionOf) },
                    { i -> scope.launch { listaCanciones.scrollToItem(i) } },
                    reversed = !sort.ascending,
                ) {
                    LazyColumn(state = listaCanciones, contentPadding = contentPadding) {
                        itemsIndexed(songs, key = { _, s -> s.id }) { i, song ->
                            SongItem(
                                song,
                                isCurrent = song.id == currentId,
                                place = MenuPlace.Library,
                                favorite = song.id in favoriteIds,
                                onAction = { onSongAction(song, it) },
                                // 22 + 8 de la fila = 30 dp a la derecha: deja libre el abecedario (Figma 33:1090).
                                modifier = enCascada(i).padding(end = 22.dp),
                            ) { onSongClick(i) }
                        }
                    }
                }
            }
        }
    }
    if (ordenando) SortSheet(sort, onSortChange) { ordenando = false }
}

/** Contenido con el abecedario encima a la derecha (DEC-05); [activa] se lee al desplazar. */
@Composable
private fun ConAbecedario(
    primeras: Map<String, Int>,
    activa: () -> String?,
    onJump: (Int) -> Unit,
    reversed: Boolean = false,
    content: @Composable () -> Unit,
) {
    val letra by remember(primeras) { derivedStateOf(activa) }
    Box(Modifier.fillMaxSize()) {
        content()
        AlphabetBar(primeras, letra, onJump, Modifier.align(Alignment.TopEnd).padding(top = RolaSpacing.xl), reversed)
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
