package com.luis.rola

import android.content.Context
import com.luis.rola.core.data.MediaStoreSongRepository
import com.luis.rola.core.data.SongRepository
import com.luis.rola.core.media.PlayerConnection

/** Inyección manual (PEND-02): se arma una sola vez en [RolaApp]. */
class AppContainer(context: Context) {
    val songs: SongRepository = MediaStoreSongRepository(context.contentResolver)
    val player = PlayerConnection(context)
}
