package com.luis.rola.core.media

import android.database.sqlite.SQLiteException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.luis.rola.core.database.QueueDao
import com.luis.rola.core.database.QueueEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

/** Dónde va la reproducción (DEC-31); [index] es el lugar en la lista de Media3. [source] nulo = Biblioteca. */
data class PlaybackProgress(
    val index: Int = 0,
    val positionMs: Long = 0,
    val shuffle: Boolean = false,
    val repeat: Repeat = Repeat.OFF,
    val source: String? = null,
)

/** Cola guardada: [ids] en el orden de Media3 y [order], sus índices en el orden en que suenan. */
data class SavedPlayback(val ids: List<Long>, val order: List<Int>, val progress: PlaybackProgress) {

    /** Sin las que ya no existen; si faltaba la actual, la siguiente en sonar desde el principio. `null` si no queda ninguna (FR-024, FR-025). */
    fun keepExisting(exists: Set<Long>): SavedPlayback? {
        val quedan = ids.indices.filter { ids[it] in exists }
        if (quedan.isEmpty()) return null
        if (quedan.size == ids.size) return this
        val nuevo = quedan.withIndex().associate { (i, viejo) -> viejo to i }
        val actual = progress.index
        val siguiente = order.drop(order.indexOf(actual)).firstOrNull { it in nuevo } ?: order.first { it in nuevo }
        return SavedPlayback(
            quedan.map(ids::get),
            order.mapNotNull(nuevo::get),
            progress.copy(index = nuevo.getValue(siguiente), positionMs = if (siguiente == actual) progress.positionMs else 0),
        )
    }
}

/** Guarda la cola en Room y los valores sueltos en su propio DataStore, que se escribe cada ~5 s sin tocar la cola (FR-026). */
class PlaybackStore(private val dao: QueueDao, private val store: DataStore<Preferences>) {

    /** `null` sin cola guardada. Lo que venga dañado se corrige o se ignora (FR-028). */
    suspend fun load(): SavedPlayback? {
        val rows = try {
            dao.rows()
        } catch (_: SQLiteException) {
            return null
        }
        if (rows.isEmpty()) return null
        val p = store.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }.first()
        val order = rows.indices.sortedBy { rows[it].playOrder }
        return SavedPlayback(
            rows.map { it.mediaId },
            if (rows.map { it.playOrder }.distinct().size == rows.size) order else rows.indices.toList(),
            PlaybackProgress(
                index = (p[INDICE] ?: 0).coerceIn(rows.indices),
                positionMs = (p[MINUTO] ?: 0).coerceAtLeast(0),
                shuffle = p[ALEATORIO] ?: false,
                repeat = Repeat.entries.find { it.name == p[REPETIR] } ?: Repeat.OFF,
                source = p[ORIGEN],
            ),
        )
    }

    // Disco lleno o ilegible: se sigue reproduciendo y se vuelve a intentar en el siguiente guardado.

    suspend fun saveQueue(ids: List<Long>, order: List<Int>) {
        val lugar = IntArray(ids.size).also { order.forEachIndexed { i, indice -> it[indice] = i } }
        try {
            dao.replace(ids.mapIndexed { i, id -> QueueEntity(i, id, lugar[i]) })
        } catch (_: SQLiteException) {
        }
    }

    suspend fun saveProgress(progress: PlaybackProgress) {
        try {
            store.edit {
                it[INDICE] = progress.index
                it[MINUTO] = progress.positionMs
                it[ALEATORIO] = progress.shuffle
                it[REPETIR] = progress.repeat.name
                if (progress.source == null) it.remove(ORIGEN) else it[ORIGEN] = progress.source
            }
        } catch (_: IOException) {
        }
    }

    private companion object {
        val INDICE = intPreferencesKey("indice")
        val MINUTO = longPreferencesKey("minuto_ms")
        val ALEATORIO = booleanPreferencesKey("aleatorio")
        val REPETIR = stringPreferencesKey("repetir")
        val ORIGEN = stringPreferencesKey("origen")
    }
}

/** Escribe fuera del hilo principal, en orden y solo lo último: una ráfaga (arrastrar en la cola) es un guardado. */
class PlaybackSaver(private val store: PlaybackStore, scope: CoroutineScope) {

    private class Cola(val ids: List<Long>, val order: List<Int>)

    private val cola = AtomicReference<Cola?>()
    private val progreso = AtomicReference<PlaybackProgress?>()
    private val aviso = Channel<Unit>(Channel.CONFLATED)

    init {
        scope.launch(Dispatchers.IO) {
            for (u in aviso) {
                cola.getAndSet(null)?.let { store.saveQueue(it.ids, it.order) }
                progreso.getAndSet(null)?.let { store.saveProgress(it) }
            }
        }
    }

    fun saveQueue(ids: List<Long>, order: List<Int>) = cola.set(Cola(ids, order))

    fun saveProgress(progress: PlaybackProgress) {
        progreso.set(progress)
        aviso.trySend(Unit)
    }

    /** Lo ya pedido se alcanza a escribir. */
    fun close() {
        aviso.close()
    }
}
