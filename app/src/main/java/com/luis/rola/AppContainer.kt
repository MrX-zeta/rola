package com.luis.rola

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.luis.rola.core.data.DataStorePreferencesRepository
import com.luis.rola.core.data.MediaStoreSongRepository
import com.luis.rola.core.data.PreferencesRepository
import com.luis.rola.core.data.SongRepository
import com.luis.rola.core.media.PlayerConnection

// DataStore exige una sola instancia por archivo en el proceso.
private val Context.ajustes by preferencesDataStore(name = "rola_ajustes")

/** Inyección manual (PEND-02): se arma una sola vez en [RolaApp]. */
class AppContainer(context: Context) {
    val songs: SongRepository = MediaStoreSongRepository(context.contentResolver)
    val player = PlayerConnection(context)
    val preferences: PreferencesRepository = DataStorePreferencesRepository(context.ajustes)
}
