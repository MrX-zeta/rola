package com.luis.rola.core.media

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import com.luis.rola.core.database.QueueDao
import com.luis.rola.core.database.QueueEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class PlaybackStoreTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    private val dao = object : QueueDao {
        var filas = emptyList<QueueEntity>()
        var escrituras = 0

        override suspend fun rows() = filas.sortedBy { it.position }
        override suspend fun clear() {
            filas = emptyList()
        }

        override suspend fun insertAll(rows: List<QueueEntity>) {
            escrituras++
            filas = rows
        }
    }

    /** Cada llamada abre el DataStore de nuevo, como al reabrir la app. */
    private fun <T> abrir(bloque: suspend (PlaybackStore, DataStore<Preferences>) -> T): T = runBlocking {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val store = PreferenceDataStoreFactory.create(scope = scope) { carpeta.root.resolve("rola_reproduccion.preferences_pb") }
        try {
            bloque(PlaybackStore(dao, store), store)
        } finally {
            scope.cancel()
        }
    }

    private val progreso = PlaybackProgress(index = 2, positionMs = 61_500, shuffle = true, repeat = Repeat.ONE, source = "Ventanas")

    @Test
    fun `cola en orden, indice, minuto, aleatorio, repetir y origen sobreviven`() {
        abrir { s, _ ->
            s.saveQueue(listOf(30, 10, 20, 10), listOf(2, 0, 3, 1))
            s.saveProgress(progreso)
        }
        assertEquals(SavedPlayback(listOf(30, 10, 20, 10), listOf(2, 0, 3, 1), progreso), abrir { s, _ -> s.load() })
    }

    @Test
    fun `guardar el minuto no reescribe la cola y sin origen vuelve a Biblioteca`() {
        abrir { s, _ ->
            s.saveQueue(listOf(1, 2, 3), listOf(0, 1, 2))
            s.saveProgress(progreso)
            s.saveProgress(progreso.copy(positionMs = 66_500, source = null))
        }
        assertEquals(1, dao.escrituras)
        val p = abrir { s, _ -> s.load() }!!.progress
        assertEquals(66_500L, p.positionMs)
        assertNull(p.source)
    }

    @Test
    fun `sin cola no hay nada que restaurar`() {
        assertNull(abrir { s, _ -> s.load() })
        abrir { s, _ ->
            s.saveQueue(listOf(1), listOf(0))
            s.saveQueue(emptyList(), emptyList())
        }
        assertNull(abrir { s, _ -> s.load() })
    }

    @Test
    fun `lo guardado danado se corrige`() {
        dao.filas = listOf(QueueEntity(0, 1, 5), QueueEntity(1, 2, 5))
        val p = abrir { s, store ->
            s.saveProgress(PlaybackProgress(index = 9, positionMs = -4))
            store.edit { it[stringPreferencesKey("repetir")] = "QUIZAS" }
            s.load()
        }
        assertEquals(SavedPlayback(listOf(1, 2), listOf(0, 1), PlaybackProgress(index = 1)), p)
    }

    @Test
    fun `un archivo ilegible no cierra la app`() = runBlocking {
        dao.filas = listOf(QueueEntity(0, 1, 0))
        val roto = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flow { throw IOException() }
            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences = throw IOException()
        }
        val s = PlaybackStore(dao, roto)
        s.saveProgress(progreso)
        assertEquals(PlaybackProgress(), s.load()!!.progress)
    }

    private fun guardado(index: Int, order: List<Int> = listOf(0, 1, 2, 3)) =
        SavedPlayback(listOf(10, 20, 30, 40), order, progreso.copy(index = index))

    @Test
    fun `si todas existen no cambia nada`() = assertEquals(guardado(2), guardado(2).keepExisting(setOf(10, 20, 30, 40, 50)))

    @Test
    fun `una borrada antes o despues se omite y la actual sigue en su minuto`() {
        assertEquals(
            SavedPlayback(listOf(20, 30), listOf(0, 1), progreso.copy(index = 1)),
            guardado(2).keepExisting(setOf(20, 30)),
        )
    }

    @Test
    fun `si se borro la actual queda la siguiente en sonar desde el principio`() {
        assertEquals(progreso.copy(index = 2, positionMs = 0), guardado(2).keepExisting(setOf(10, 20, 40))!!.progress)
        // Con aleatorio la siguiente es la de su orden: 2, 0, 3, 1.
        assertEquals(
            SavedPlayback(listOf(10, 20, 40), listOf(0, 2, 1), progreso.copy(index = 0, positionMs = 0)),
            guardado(2, listOf(2, 0, 3, 1)).keepExisting(setOf(10, 20, 40)),
        )
    }

    @Test
    fun `si la actual era la ultima y ya no existe queda la primera`() {
        assertEquals(progreso.copy(index = 0, positionMs = 0), guardado(3).keepExisting(setOf(10, 20))!!.progress)
    }

    @Test
    fun `si no queda ninguna no hay cola`() = assertNull(guardado(0).keepExisting(setOf(99)))
}
