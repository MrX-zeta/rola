package com.luis.rola

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.luis.rola.core.data.DataStorePreferencesRepository
import com.luis.rola.core.data.LibraryRepository
import com.luis.rola.core.data.MediaStoreSongRepository
import com.luis.rola.core.data.PreferencesRepository
import com.luis.rola.core.data.Song
import com.luis.rola.core.database.RolaDatabase
import com.luis.rola.core.media.PlaybackStore
import com.luis.rola.core.media.PlayerConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

// DataStore exige una sola instancia por archivo en el proceso.
private val Context.ajustes by preferencesDataStore(name = "rola_ajustes")
private val Context.reproduccion by preferencesDataStore(
    name = "rola_reproduccion",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }, // dañado: empieza de cero (FR-028)
)

/** Inyección manual (PEND-02): se arma una sola vez en [RolaApp]. */
class AppContainer(context: Context) {
    /** Vive lo que el proceso: un guardado pedido al cerrar el servicio alcanza a terminar. */
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val db = Room.databaseBuilder(context, RolaDatabase::class.java, "rola.db").build()
    val playback = PlaybackStore(db.queue(), context.reproduccion)

    /** Una sola consulta al MediaStore para todos; sin quien la mire caduca, para no restaurar con una lista vieja. */
    val songs: SharedFlow<List<Song>> = MediaStoreSongRepository(context.contentResolver).songs()
        .shareIn(scope, SharingStarted.WhileSubscribed(5_000, replayExpirationMillis = 0), replay = 1)
    var player = PlayerConnection(context)
        @VisibleForTesting set
    val preferences: PreferencesRepository = DataStorePreferencesRepository(context.ajustes)
    val library = LibraryRepository(db, songs)
}
