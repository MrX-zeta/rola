package com.luis.rola

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.luis.rola.core.data.DataStorePreferencesRepository
import com.luis.rola.core.data.LibraryRepository
import com.luis.rola.core.data.MediaStoreSongRepository
import com.luis.rola.core.data.PreferencesRepository
import com.luis.rola.core.data.Song
import com.luis.rola.core.database.RolaDatabase
import com.luis.rola.core.media.PlayerConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

// DataStore exige una sola instancia por archivo en el proceso.
private val Context.ajustes by preferencesDataStore(name = "rola_ajustes")

/** Inyección manual (PEND-02): se arma una sola vez en [RolaApp]. */
class AppContainer(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Una sola consulta al MediaStore para Biblioteca, Favoritas y listas (research §4). */
    val songs: SharedFlow<List<Song>> = MediaStoreSongRepository(context.contentResolver).songs()
        .shareIn(scope, SharingStarted.WhileSubscribed(5_000), replay = 1)
    val player = PlayerConnection(context)
    val preferences: PreferencesRepository = DataStorePreferencesRepository(context.ajustes)
    val library = LibraryRepository(Room.databaseBuilder(context, RolaDatabase::class.java, "rola.db").build(), songs)
}
