package com.luis.rola.feature.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.data.Album
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.pill
import com.luis.rola.core.designsystem.time
import com.luis.rola.core.designsystem.timeSmall
import com.luis.rola.core.media.formatDuration

/** Detalle de álbum (SCR-24, Figma 33:836): portada, datos, Reproducir/Aleatorio y pistas en orden. Plana (DEC-27). */
@Composable
fun AlbumScreen(
    album: Album,
    currentId: Long?,
    onBack: () -> Unit,
    onPlay: (index: Int) -> Unit,
    onShuffle: () -> Unit,
    onSongAction: (Song, SongAction) -> Unit,
    favoriteIds: Set<Long> = emptySet(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val minutos = (album.durationMs / 60_000).toInt()
    val datos = listOfNotNull(
        album.artist ?: stringResource(R.string.artista_desconocido),
        album.year.takeIf { it > 0 }?.toString(),
        pluralStringResource(R.plurals.n_canciones, album.songs.size, album.songs.size),
        stringResource(R.string.minutos, minutos).takeIf { minutos > 0 },
    ).joinToString(" · ")
    DetailList(onBack, contentPadding) {
        item {
            DetailHeader(album.songs.first(), 200.dp, MaterialTheme.shapes.large, album.name ?: stringResource(R.string.album_desconocido), datos)
        }
        item { PlayShuffle({ onPlay(0) }, onShuffle) }
        itemsIndexed(album.songs, key = { _, s -> s.id }) { i, song ->
            TrackItem(song, i + 1, song.id == currentId, song.id in favoriteIds, { onSongAction(song, it) }) { onPlay(i) }
        }
    }
}

/** Lista de un detalle con la barra de volver arriba. */
@Composable
internal fun DetailList(onBack: () -> Unit, contentPadding: PaddingValues, content: LazyListScope.() -> Unit) {
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), contentPadding = contentPadding) {
        item {
            Row(Modifier.height(64.dp).padding(horizontal = RolaSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onBack) {
                    Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.volver), tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        content()
    }
}

/** Portada (o foto) centrada, título y datos (SCR-24/SCR-25). */
@Composable
internal fun DetailHeader(cover: Song?, size: Dp, shape: Shape, title: String, subtitle: String) {
    val c = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().padding(horizontal = RolaSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (cover != null) Cover(cover, Modifier.size(size), shape) else Box(Modifier.size(size))
        Spacer(Modifier.height(RolaSpacing.xxs))
        Text(title, style = MaterialTheme.typography.headlineMedium, color = c.onSurface, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

/** Reproducir (lima) y Aleatorio (`surfaceContainer`), píldoras de 48 dp centradas. */
@Composable
internal fun PlayShuffle(onPlay: () -> Unit, onShuffle: () -> Unit) {
    val boton = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    Row(
        Modifier.fillMaxWidth().padding(top = 14.dp, bottom = RolaSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm, Alignment.CenterHorizontally),
    ) {
        Button(
            onPlay,
            Modifier.height(48.dp),
            shape = MaterialTheme.shapes.pill,
            colors = ButtonDefaults.buttonColors(containerColor = RolaTheme.colors.accent, contentColor = RolaTheme.colors.onAccent),
        ) {
            Icon(painterResource(R.drawable.ic_play), null, Modifier.size(20.dp))
            Text(stringResource(R.string.reproducir), style = boton, modifier = Modifier.padding(start = RolaSpacing.xs))
        }
        Button(
            onShuffle,
            Modifier.height(48.dp),
            shape = MaterialTheme.shapes.pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Icon(painterResource(R.drawable.ic_shuffle), null, Modifier.size(20.dp))
            Text(stringResource(R.string.aleatorio), style = boton, modifier = Modifier.padding(start = RolaSpacing.xs))
        }
    }
}

/** Pista del álbum (Figma 33:884): número, título, duración y ⋮; pulsación larga abre el menú (SCR-54). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrackItem(song: Song, numero: Int, isCurrent: Boolean, favorite: Boolean, onAction: (SongAction) -> Unit, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    var abierto by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .combinedClickable(onLongClick = { abierto = true }, onClick = onClick)
            .padding(start = RolaSpacing.lg, end = RolaSpacing.xs)
            .testTag("pista"),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            numero.toString(),
            Modifier.width(28.dp),
            style = MaterialTheme.typography.time,
            color = c.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            song.title,
            Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (isCurrent) c.primary else c.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(formatDuration(song.durationMs), style = MaterialTheme.typography.timeSmall, color = c.onSurfaceVariant)
        Box {
            IconButton({ abierto = true }) {
                Icon(painterResource(R.drawable.ic_more_vert), stringResource(R.string.opciones_de, song.title), tint = c.onSurfaceVariant)
            }
            SongMenu(abierto, MenuPlace.Library, favorite, onDismiss = { abierto = false }) {
                abierto = false
                onAction(it)
            }
        }
    }
}
