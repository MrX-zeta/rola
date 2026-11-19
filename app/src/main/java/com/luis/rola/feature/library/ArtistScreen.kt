package com.luis.rola.feature.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.data.Album
import com.luis.rola.core.data.Artist
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaSpacing

/** Detalle de artista (SCR-25, Figma 33:950): foto redonda, Reproducir/Aleatorio, carrusel de álbumes y canciones. */
@Composable
fun ArtistScreen(
    artist: Artist,
    currentId: Long?,
    onBack: () -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onPlay: (index: Int) -> Unit,
    onShuffle: () -> Unit,
    onSongAction: (Song, SongAction) -> Unit,
    favoriteIds: Set<Long> = emptySet(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val nombre = artist.name ?: stringResource(R.string.artista_desconocido)
    val desconocido = stringResource(R.string.album_desconocido)
    DetailList(onBack, contentPadding) {
        item { DetailHeader(artist.cover, 140.dp, CircleShape, nombre, artistCounts(artist)) }
        item { PlayShuffle({ onPlay(0) }, onShuffle) }
        item { Section(R.string.chip_albumes) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = RolaSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm),
            ) {
                items(artist.albums, key = { it.id }) { AlbumCard(it, 120.dp, showArtist = false) { onOpenAlbum(it.id) } }
            }
        }
        item { Section(R.string.chip_canciones) }
        itemsIndexed(artist.songs, key = { _, s -> s.id }) { i, song ->
            SongItem(
                song,
                song.id == currentId,
                MenuPlace.Library,
                song.id in favoriteIds,
                { onSongAction(song, it) },
                subtitle = song.album ?: desconocido,
            ) { onPlay(i) }
        }
    }
}

/** «N canciones · N álbumes» (SCR-22, SCR-25). */
@Composable
internal fun artistCounts(artist: Artist) = stringResource(
    R.string.separados,
    pluralStringResource(R.plurals.n_canciones, artist.songs.size, artist.songs.size),
    pluralStringResource(R.plurals.n_albumes, artist.albums.size, artist.albums.size),
)

@Composable
private fun Section(texto: Int) = Text(
    stringResource(texto),
    style = MaterialTheme.typography.titleLarge,
    color = MaterialTheme.colorScheme.onSurface,
    modifier = Modifier.padding(start = RolaSpacing.lg, end = RolaSpacing.lg, top = RolaSpacing.sm, bottom = 6.dp),
)

/**
 * Álbum con portada cuadrada, nombre y «Artista · año» (cuadrícula SCR-21) o solo el año (carrusel SCR-25).
 * Plana: Figma le pone relieve, pero DEC-27 deja Biblioteca sin sombras.
 */
@Composable
internal fun AlbumCard(album: Album, size: Dp?, showArtist: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val anio = album.year.takeIf { it > 0 }?.toString()
    val segunda = if (showArtist) {
        listOfNotNull(album.artist ?: stringResource(R.string.artista_desconocido), anio).joinToString(" · ")
    } else {
        anio
    }
    Column(
        modifier.then(if (size != null) Modifier.width(size) else Modifier).clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(RolaSpacing.xxs),
    ) {
        Cover(
            album.songs.first(),
            if (size != null) Modifier.size(size) else Modifier.fillMaxWidth().aspectRatio(1f),
            MaterialTheme.shapes.medium,
        )
        Text(
            album.name ?: stringResource(R.string.album_desconocido),
            Modifier.padding(top = RolaSpacing.xxs),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = c.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        segunda?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
