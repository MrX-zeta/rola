package com.luis.rola.feature.library

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.pill

/** Biblioteca · Canciones (SCR-20). Solo el chip Canciones; sin filtros, ⋮ ni abecedario en esta tarea. */
@Composable
fun LibraryScreen(
    songs: List<Song>?,
    currentId: Long?,
    onSongClick: (index: Int) -> Unit,
    onShuffle: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val c = MaterialTheme.colorScheme
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
            songs == null -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
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
                LazyColumn(contentPadding = contentPadding) {
                    itemsIndexed(songs, key = { _, s -> s.id }) { i, song ->
                        SongRow(song, isCurrent = song.id == currentId) { onSongClick(i) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SongRow(song: Song, isCurrent: Boolean, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = RolaSpacing.lg, vertical = RolaSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Cover(song, Modifier.size(48.dp))
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
