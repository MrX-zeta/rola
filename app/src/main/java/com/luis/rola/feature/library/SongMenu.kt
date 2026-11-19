package com.luis.rola.feature.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.tile

/** Dónde está la canción: decide las opciones del menú (DEC-07). */
enum class MenuPlace { Library, Playlist }

enum class SongAction { AddToQueue, AddToPlaylist, ToggleFavorite, Delete, RemoveFromPlaylist }

/** Fila de canción (portada, título, artista). [trailing] va a la derecha; [onLongClick] abre el menú. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongRow(
    song: Song,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
    subtitle: String? = null, // por defecto el artista; en el detalle de artista, el álbum (SCR-25)
    onClick: (() -> Unit)?, // null: el toque lo maneja [modifier] (casillas de la hoja de agregar)
) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .then(if (onClick != null) Modifier.combinedClickable(onLongClick = onLongClick, onClick = onClick) else Modifier)
            .padding(start = RolaSpacing.lg, end = RolaSpacing.xs, top = RolaSpacing.xs, bottom = RolaSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Cover(song, Modifier.size(48.dp), MaterialTheme.shapes.extraSmall)
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isCurrent) c.primary else c.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle ?: song.artist ?: stringResource(R.string.artista_desconocido),
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailing()
    }
}

/** [SongRow] con ⋮ y pulsación larga que abren el menú de opciones (SCR-54). */
@Composable
fun SongItem(
    song: Song,
    isCurrent: Boolean,
    place: MenuPlace,
    favorite: Boolean,
    onAction: (SongAction) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    var abierto by remember { mutableStateOf(false) }
    SongRow(song, isCurrent, modifier, onLongClick = { abierto = true }, subtitle = subtitle, trailing = {
        Box {
            IconButton({ abierto = true }) {
                Icon(
                    painterResource(R.drawable.ic_more_vert),
                    stringResource(R.string.opciones_de, song.title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SongMenu(abierto, place, favorite, onDismiss = { abierto = false }) {
                abierto = false
                onAction(it)
            }
        }
    }, onClick = onClick)
}

/** Menú de opciones (SCR-54, Figma 35:1626). «Favorita» se suma por DEC-29 (no está en ese nodo). */
@Composable
fun SongMenu(expanded: Boolean, place: MenuPlace, favorite: Boolean, onDismiss: () -> Unit, onAction: (SongAction) -> Unit) {
    DropdownMenu(
        expanded,
        onDismiss,
        Modifier.testTag("menu_cancion"),
        shape = MaterialTheme.shapes.tile,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        @Composable
        fun Opcion(texto: Int, icono: Int, accion: SongAction) = DropdownMenuItem(
            text = { Text(stringResource(texto), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)) },
            onClick = { onAction(accion) },
            leadingIcon = { Icon(painterResource(icono), null) },
            modifier = Modifier.padding(end = RolaSpacing.xs),
        )
        when (place) {
            MenuPlace.Library -> {
                Opcion(R.string.anadir_a_cola, R.drawable.ic_queue, SongAction.AddToQueue)
                Opcion(R.string.anadir_a_lista, R.drawable.ic_playlist_add, SongAction.AddToPlaylist)
                if (favorite) {
                    Opcion(R.string.quitar_de_favoritas, R.drawable.ic_favorite_filled, SongAction.ToggleFavorite)
                } else {
                    Opcion(R.string.favorita, R.drawable.ic_favorite, SongAction.ToggleFavorite)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Opcion(R.string.borrar_del_dispositivo, R.drawable.ic_delete, SongAction.Delete)
            }
            MenuPlace.Playlist -> {
                Opcion(R.string.quitar_de_lista, R.drawable.ic_delete, SongAction.RemoveFromPlaylist)
                Opcion(R.string.anadir_a_cola, R.drawable.ic_queue, SongAction.AddToQueue)
            }
        }
    }
}
