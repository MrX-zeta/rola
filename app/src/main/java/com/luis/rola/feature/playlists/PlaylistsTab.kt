package com.luis.rola.feature.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.data.PlaylistSummary
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.tile
import com.luis.rola.feature.library.Cover

/** Portada de lista (la caja recorta la forma): mosaico 2×2 con 4 canciones, la primera sola con menos, o vacía con [emptyIcon]. */
@Composable
fun PlaylistCover(covers: List<Song>, size: Dp, shape: Shape, modifier: Modifier = Modifier, emptyIcon: Boolean = false) {
    Box(modifier.size(size).clip(shape).background(MaterialTheme.colorScheme.surfaceContainer), Alignment.Center) {
        when {
            covers.size >= 4 -> Column {
                covers.take(4).chunked(2).forEach { fila ->
                    Row { fila.forEach { Cover(it, Modifier.size(size / 2), shape = RectangleShape) } }
                }
            }
            covers.isNotEmpty() -> Cover(covers.first(), Modifier.size(size), shape = RectangleShape)
            emptyIcon -> Icon(
                painterResource(R.drawable.ic_queue),
                null,
                Modifier.size(size * 0.36f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Fila de lista (Figma 32:1177): portada, nombre y conteo; [trailing] a la derecha (⋮). */
@Composable
fun PlaylistRow(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    padding: PaddingValues = PaddingValues(horizontal = RolaSpacing.lg, vertical = RolaSpacing.xs),
    gap: Dp = 14.dp,
    trailing: @Composable () -> Unit = {},
    cover: @Composable () -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(padding),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        cover()
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        trailing()
    }
}

enum class PlaylistAction { Rename, Delete }

/** ⋮ de una lista del usuario (Figma 32:1128 y 34:1345): renombrar y borrar (FR-038). Favoritas no lo lleva. */
@Composable
fun PlaylistMenuButton(name: String, onAction: (PlaylistAction) -> Unit, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        IconButton({ abierto = true }) {
            Icon(painterResource(R.drawable.ic_more_vert), stringResource(R.string.opciones_de, name), tint = tint)
        }
        // Mismo aspecto que el menú de canción (SCR-54).
        DropdownMenu(
            abierto,
            { abierto = false },
            Modifier.testTag("menu_lista"),
            shape = MaterialTheme.shapes.tile,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            @Composable
            fun Opcion(texto: Int, icono: Int, accion: PlaylistAction) = DropdownMenuItem(
                text = { Text(stringResource(texto), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)) },
                onClick = {
                    abierto = false
                    onAction(accion)
                },
                leadingIcon = { Icon(painterResource(icono), null) },
                modifier = Modifier.padding(end = RolaSpacing.xs),
            )
            Opcion(R.string.renombrar, R.drawable.ic_edit, PlaylistAction.Rename)
            Opcion(R.string.borrar_lista, R.drawable.ic_delete, PlaylistAction.Delete)
        }
    }
}

@Composable
private fun NewPlaylistRow(onClick: () -> Unit) = PlaylistRow(
    stringResource(R.string.nueva_lista),
    stringResource(R.string.nueva_lista_detalle),
    onClick = onClick,
) {
    Box(Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.tile), Alignment.Center) {
        Icon(painterResource(R.drawable.ic_add), null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
internal fun countText(count: Int) =
    if (count == 0) stringResource(R.string.lista_vacia_corto) else pluralStringResource(R.plurals.n_canciones, count, count)

@Composable
private fun SummaryRow(
    summary: PlaylistSummary,
    title: String,
    modifier: Modifier = Modifier,
    onAction: ((PlaylistAction) -> Unit)? = null,
    onClick: () -> Unit,
) = PlaylistRow(
    title,
    countText(summary.count),
    modifier,
    onClick,
    trailing = { onAction?.let { PlaylistMenuButton(title, it) } },
) { PlaylistCover(summary.covers, 56.dp, MaterialTheme.shapes.tile) }

/** Biblioteca · Listas (SCR-23): «Nueva lista», Favoritas siempre primera y las listas del usuario. */
@Composable
fun PlaylistsTab(
    favorites: PlaylistSummary,
    playlists: List<PlaylistSummary>,
    onCreate: () -> Unit,
    onOpen: (Long) -> Unit,
    onAction: (PlaylistSummary, PlaylistAction) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    state: LazyListState = rememberLazyListState(),
    rowModifier: (index: Int) -> Modifier = { Modifier }, // cascada de entrada de Biblioteca (DEC-28)
) {
    LazyColumn(Modifier.fillMaxSize(), state = state, contentPadding = contentPadding) {
        item { Box(rowModifier(0)) { NewPlaylistRow(onCreate) } }
        item {
            SummaryRow(favorites, stringResource(R.string.favoritas), rowModifier(1).testTag("fila_favoritas")) { onOpen(favorites.id) }
        }
        itemsIndexed(playlists, key = { _, p -> p.id }) { i, p ->
            SummaryRow(p, p.name, rowModifier(i + 2), onAction = { onAction(p, it) }) { onOpen(p.id) }
        }
    }
}

/** Fila de la hoja SCR-55 (Figma 85:3348): 24 dp a los lados, mosaico de 48 dp. */
@Composable
private fun SheetRow(title: String, subtitle: String?, onClick: () -> Unit, cover: @Composable () -> Unit) = PlaylistRow(
    title,
    subtitle,
    onClick = onClick,
    padding = PaddingValues(horizontal = RolaSpacing.xl, vertical = 10.dp),
    gap = RolaSpacing.md,
    cover = cover,
)

/** «Añadir a una lista» (SCR-55, Figma 85:3348 / 85:3541): título, qué se añade ([detail]), «Nueva lista» y las listas (sin Favoritas). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoosePlaylistSheet(
    detail: String,
    playlists: List<PlaylistSummary>,
    onChoose: (PlaylistSummary) -> Unit,
    onNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = MaterialTheme.colorScheme
    ModalBottomSheet(onDismiss, containerColor = c.surface) {
        Column(Modifier.padding(start = RolaSpacing.xl, end = RolaSpacing.xl, bottom = RolaSpacing.xs)) {
            Text(stringResource(R.string.anadir_a_lista), style = MaterialTheme.typography.titleLarge, color = c.onSurface)
            Text(
                detail,
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        LazyColumn(Modifier.navigationBarsPadding().padding(bottom = RolaSpacing.lg)) {
            item {
                SheetRow(stringResource(R.string.nueva_lista), null, onNew) {
                    Box(Modifier.size(48.dp).background(c.primaryContainer, MaterialTheme.shapes.tile), Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_add), null, Modifier.size(24.dp), tint = c.onPrimaryContainer)
                    }
                }
            }
            items(playlists, key = { it.id }) { p ->
                SheetRow(p.name, countText(p.count), { onChoose(p) }) { PlaylistCover(p.covers, 48.dp, MaterialTheme.shapes.small) }
            }
        }
    }
}
