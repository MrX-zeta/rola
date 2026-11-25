package com.luis.rola.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.data.SearchResults
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.elevado
import com.luis.rola.core.designsystem.hundido
import com.luis.rola.core.designsystem.pill
import com.luis.rola.feature.library.Cover
import com.luis.rola.feature.library.MenuPlace
import com.luis.rola.feature.library.SongAction
import com.luis.rola.feature.library.SongItem
import com.luis.rola.feature.library.cascada
import com.luis.rola.feature.library.rememberRelojCascada
import com.luis.rola.feature.playlists.PlaylistCover
import com.luis.rola.feature.playlists.countText

/** Buscar (SCR-40, Figma 35:1288) y sin resultados (SCR-41, 35:1392). Sin texto, solo el campo (DEC-25). */
@Composable
fun SearchScreen(
    query: String,
    results: SearchResults?,
    onQuery: (String) -> Unit,
    onPlay: (Song) -> Unit,
    onSongAction: (Song, SongAction) -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (String?) -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    favoriteIds: Set<Long> = emptySet(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val foco = remember { FocusRequester() }
    val focos = LocalFocusManager.current
    // Al volver con texto no se abre el teclado: taparía los resultados.
    LaunchedEffect(Unit) { if (query.isEmpty()) foco.requestFocus() }
    val teclado = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val abajo = PaddingValues(bottom = maxOf(contentPadding.calculateBottomPadding(), teclado))
    val visibles = results.takeIf { query.isNotBlank() }
    val reloj = rememberRelojCascada(listo = visibles?.isEmpty == false) // solo los primeros resultados
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            stringResource(R.string.nav_buscar),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = RolaSpacing.lg, vertical = RolaSpacing.lg).semantics { heading() },
        )
        SearchField(query, onQuery, foco)
        when {
            visibles == null -> {}
            visibles.isEmpty -> NoResults(visibles.query, abajo)
            else -> {
                val lista = rememberLazyListState()
                // Con texto nuevo, lo más relevante arriba; al volver de un detalle se conserva el lugar.
                var ultima by rememberSaveable { mutableStateOf(visibles.query) }
                LaunchedEffect(visibles.query) {
                    if (ultima != visibles.query) lista.scrollToItem(0)
                    ultima = visibles.query
                }
                val album = stringResource(R.string.orden_album)
                val desconocido = stringResource(R.string.artista_desconocido)
                LazyColumn(Modifier.testTag("resultados"), state = lista, contentPadding = abajo) {
                    var n = 0
                    val fila = { Modifier.cascada(n++, { 0 }, reloj) }
                    group(R.string.chip_canciones, visibles.songs, { "cancion${it.item.id}" }, fila) { (song, match), m ->
                        SongItem(
                            song,
                            isCurrent = false, // el color ya dice qué coincide
                            place = MenuPlace.Library,
                            favorite = song.id in favoriteIds,
                            onAction = { onSongAction(song, it) },
                            modifier = m,
                            title = highlighted(song.title, match),
                        ) {
                            focos.clearFocus() // sin teclado, para que se vea el mini reproductor
                            onPlay(song)
                        }
                    }
                    group(R.string.chip_albumes, visibles.albums, { "album${it.item.id}" }, fila) { (a, match), m ->
                        ResultRow(
                            highlighted(a.name.orEmpty(), match),
                            stringResource(R.string.separados, album, a.artist ?: desconocido),
                            m,
                            { onOpenAlbum(a.id) },
                        ) { Cover(a.songs.first(), Modifier.size(48.dp), MaterialTheme.shapes.extraSmall) }
                    }
                    group(R.string.chip_artistas, visibles.artists, { "artista${it.item.name}" }, fila) { (a, match), m ->
                        ResultRow(
                            highlighted(a.name.orEmpty(), match),
                            pluralStringResource(R.plurals.n_canciones, a.songs.size, a.songs.size),
                            m,
                            { onOpenArtist(a.name) },
                        ) { Cover(a.songs.first(), Modifier.size(48.dp), CircleShape) }
                    }
                    group(R.string.chip_listas, visibles.playlists, { "lista${it.item.id}" }, fila) { (p, match), m ->
                        ResultRow(highlighted(p.name, match), countText(p.count), m, { onOpenPlaylist(p.id) }) {
                            PlaylistCover(p.covers, 48.dp, MaterialTheme.shapes.extraSmall, emptyIcon = true)
                        }
                    }
                }
            }
        }
    }
}

/** Encabezado y filas de un grupo; vacío no se muestra. [fila] da el lugar en la cascada, en orden de lectura. */
private fun <T> LazyListScope.group(
    titulo: Int,
    encontrados: List<T>,
    clave: (T) -> String,
    fila: () -> Modifier,
    contenido: @Composable (T, Modifier) -> Unit,
) {
    if (encontrados.isEmpty()) return
    val encabezado = fila()
    item(key = titulo) {
        Text(
            stringResource(titulo),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = encabezado
                .padding(start = RolaSpacing.lg, end = RolaSpacing.lg, top = 14.dp, bottom = RolaSpacing.xxs)
                .semantics { heading() },
        )
    }
    encontrados.forEach { e ->
        val m = fila()
        item(key = clave(e)) { contenido(e, m) }
    }
}

@Composable
private fun highlighted(text: String, match: IntRange): AnnotatedString {
    val color = MaterialTheme.colorScheme.primary
    return remember(text, match, color) {
        buildAnnotatedString {
            append(text)
            addStyle(SpanStyle(color), match.first, match.last + 1)
        }
    }
}

/** Campo hundido (Figma 35:1307) con lupa y «Borrar búsqueda». */
@Composable
private fun SearchField(query: String, onQuery: (String) -> Unit, foco: FocusRequester) {
    val c = MaterialTheme.colorScheme
    val focos = LocalFocusManager.current
    val forma = MaterialTheme.shapes.pill
    val ayuda = stringResource(R.string.buscar_ayuda)
    Row(
        Modifier
            .padding(start = RolaSpacing.lg, end = RolaSpacing.lg, top = RolaSpacing.xxs, bottom = RolaSpacing.xs)
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(c.surfaceContainer, forma)
            .hundido(forma)
            .padding(start = 18.dp, end = RolaSpacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_search), null, tint = c.onSurfaceVariant)
        BasicTextField(
            query,
            onQuery,
            Modifier.weight(1f).focusRequester(foco).testTag("campo_buscar"),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.onSurface),
            cursorBrush = SolidColor(c.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focos.clearFocus() }), // los resultados ya están: solo cierra el teclado
            decorationBox = { campo ->
                if (query.isEmpty()) {
                    Text(ayuda, style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                campo()
            },
        )
        if (query.isNotEmpty()) {
            IconButton({
                onQuery("")
                foco.requestFocus()
            }) { Icon(painterResource(R.drawable.ic_close), stringResource(R.string.borrar_busqueda), tint = c.onSurfaceVariant) }
        }
    }
}

/** Fila de álbum, artista o lista (Figma 35:1340): portada de 48, nombre, detalle y chevron. */
@Composable
private fun ResultRow(title: AnnotatedString, subtitle: String, modifier: Modifier, onClick: () -> Unit, cover: @Composable () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = RolaSpacing.lg, end = RolaSpacing.xs, top = RolaSpacing.xs, bottom = RolaSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        cover()
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = c.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(painterResource(R.drawable.ic_chevron_forward), null, Modifier.padding(RolaSpacing.sm), tint = c.onSurfaceVariant)
    }
}

@Composable
private fun NoResults(query: String, contentPadding: PaddingValues) {
    val c = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(start = RolaSpacing.xxl, end = RolaSpacing.xxl, top = 80.dp, bottom = RolaSpacing.xl)
            .testTag("sin_resultados"),
        verticalArrangement = Arrangement.spacedBy(RolaSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(112.dp).elevado(CircleShape).background(c.surfaceContainer, CircleShape), Alignment.Center) {
            Icon(painterResource(R.drawable.ic_search), null, Modifier.size(48.dp), tint = c.onSurfaceVariant)
        }
        Spacer(Modifier.size(RolaSpacing.xxs))
        Text(
            stringResource(R.string.sin_resultados_para, query),
            style = MaterialTheme.typography.titleLarge,
            color = c.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Text(stringResource(R.string.sin_resultados_consejo), style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
