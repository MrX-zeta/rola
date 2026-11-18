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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/** Fila de lista (Figma 32:1177): portada de 56 dp, nombre y conteo. */
@Composable
fun PlaylistRow(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    cover: @Composable () -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = RolaSpacing.lg, vertical = RolaSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
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
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun SummaryRow(summary: PlaylistSummary, title: String, modifier: Modifier = Modifier, onClick: () -> Unit) = PlaylistRow(
    title,
    if (summary.count == 0) {
        stringResource(R.string.lista_vacia_corto)
    } else {
        pluralStringResource(R.plurals.n_canciones, summary.count, summary.count)
    },
    modifier,
    onClick,
) { PlaylistCover(summary.covers, 56.dp, MaterialTheme.shapes.tile) }

/** Biblioteca · Listas (SCR-23): «Nueva lista», Favoritas siempre primera y las listas del usuario. */
@Composable
fun PlaylistsTab(
    favorites: PlaylistSummary,
    playlists: List<PlaylistSummary>,
    onCreate: () -> Unit,
    onOpen: (Long) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = contentPadding) {
        item { NewPlaylistRow(onCreate) }
        item {
            SummaryRow(favorites, stringResource(R.string.favoritas), Modifier.testTag("fila_favoritas")) { onOpen(favorites.id) }
        }
        items(playlists, key = { it.id }) { SummaryRow(it, it.name) { onOpen(it.id) } }
    }
}

/** «Añadir a una lista»: sin nodo en Figma; reusa las filas de Biblioteca · Listas (sin Favoritas). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoosePlaylistSheet(playlists: List<PlaylistSummary>, onChoose: (PlaylistSummary) -> Unit, onNew: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismiss) {
        Text(
            stringResource(R.string.anadir_a_lista),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = RolaSpacing.xl, vertical = RolaSpacing.xs),
        )
        LazyColumn(Modifier.navigationBarsPadding().padding(bottom = RolaSpacing.md)) {
            item { NewPlaylistRow(onNew) }
            items(playlists, key = { it.id }) { SummaryRow(it, it.name) { onChoose(it) } }
        }
    }
}
