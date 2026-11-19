package com.luis.rola.feature.library

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luis.rola.core.data.Album
import com.luis.rola.core.designsystem.RolaSpacing

/** Biblioteca · Álbumes (SCR-21, Figma 32:801): cuadrícula de 2 columnas por nombre; deja a la derecha lugar al abecedario. */
@Composable
fun AlbumsTab(
    albums: List<Album>,
    state: LazyGridState,
    onOpen: (Long) -> Unit,
    primera: () -> Int,
    reloj: Animatable<Float, AnimationVector1D>,
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyVerticalGrid(
        GridCells.Fixed(2),
        Modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(
            start = RolaSpacing.lg,
            end = 36.dp,
            top = RolaSpacing.xs,
            bottom = contentPadding.calculateBottomPadding(),
        ),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.md),
        verticalArrangement = Arrangement.spacedBy(RolaSpacing.lg),
    ) {
        itemsIndexed(albums, key = { _, a -> a.id }) { i, album ->
            AlbumCard(album, null, showArtist = true, Modifier.cascada(i, primera, reloj)) { onOpen(album.id) }
        }
    }
}
