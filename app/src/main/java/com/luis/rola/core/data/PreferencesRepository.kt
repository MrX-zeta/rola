package com.luis.rola.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Ajustes del usuario (KV-DATASTORE): la bienvenida (DEC-18), el orden de Canciones, el tema y el último escaneo. */
interface PreferencesRepository {
    /** `false` hasta que se toque «Empezar» u «Omitir». */
    val onboardingCompleted: Flow<Boolean>

    suspend fun completeOnboarding()

    /** Título A→Z si no hay nada guardado o no se puede leer. */
    val sortOrder: Flow<SongSort>

    suspend fun setSortOrder(sort: SongSort)

    /** Oscuro si no hay nada guardado o no se puede leer. */
    val theme: Flow<ThemeMode>

    suspend fun setTheme(mode: ThemeMode)

    /** Milisegundos de época del último escaneo terminado; `null` si nunca. */
    val lastScanAt: Flow<Long?>

    suspend fun setLastScanAt(epochMs: Long)
}

class DataStorePreferencesRepository(private val store: DataStore<Preferences>) : PreferencesRepository {

    // Archivo ilegible: valores por defecto (bienvenida sin completar, título A→Z, tema oscuro, sin escaneo).
    private val datos = store.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }

    override val onboardingCompleted: Flow<Boolean> = datos.map { it[ONBOARDING_COMPLETED] ?: false }

    override val sortOrder: Flow<SongSort> = datos.map { SongSort.parse(it[SORT_ORDER]) }

    override suspend fun setSortOrder(sort: SongSort) = guardar { it[SORT_ORDER] = sort.key() }

    override val theme: Flow<ThemeMode> = datos.map { ThemeMode.parse(it[THEME]) }

    override suspend fun setTheme(mode: ThemeMode) = guardar { it[THEME] = mode.key() }

    override val lastScanAt: Flow<Long?> = datos.map { it[LAST_SCAN_AT] }

    override suspend fun setLastScanAt(epochMs: Long) = guardar { it[LAST_SCAN_AT] = epochMs }

    // Disco lleno o ilegible: el valor se aplica igual en pantalla, solo no se recuerda (FR-075).
    private suspend fun guardar(cambio: (MutablePreferences) -> Unit) {
        try {
            store.edit(cambio)
        } catch (_: IOException) {
        }
    }

    // Si no se guarda, la bienvenida sigue y se puede volver a intentar.
    override suspend fun completeOnboarding() = guardar { it[ONBOARDING_COMPLETED] = true }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val THEME = stringPreferencesKey("theme")
        val LAST_SCAN_AT = longPreferencesKey("last_scan_at")
    }
}
