package com.luis.rola.feature.library

import android.util.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.ImageLoader
import coil3.asImage
import coil3.compose.AsyncImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.key.Keyer
import coil3.request.Options
import com.luis.rola.R
import com.luis.rola.core.data.Song

/** Portada de la canción; sin portada, recuadro del tema con una nota. */
@Composable
fun Cover(song: Song, modifier: Modifier = Modifier, shape: Shape = MaterialTheme.shapes.small) {
    Box(modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceContainer), Alignment.Center) {
        Icon(
            painterResource(R.drawable.ic_music_note),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(0.5f),
        )
        AsyncImage(song, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

/** Coil lee la miniatura que guarda el sistema para cada audio (incluye la portada incrustada). */
class SongCoverFetcher(private val song: Song, private val options: Options) : Fetcher {

    override suspend fun fetch(): FetchResult = ImageFetchResult(
        image = options.context.contentResolver.loadThumbnail(song.uri, Size(512, 512), null).asImage(),
        isSampled = true,
        dataSource = DataSource.DISK,
    )

    class Factory : Fetcher.Factory<Song> {
        override fun create(data: Song, options: Options, imageLoader: ImageLoader) = SongCoverFetcher(data, options)
    }

    object Key : Keyer<Song> {
        override fun key(data: Song, options: Options) = "portada:${data.id}"
    }
}
