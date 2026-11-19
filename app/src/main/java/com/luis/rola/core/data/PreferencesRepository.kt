package com.luis.rola.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Ajustes del usuario (KV-DATASTORE): la bienvenida (DEC-18) y el orden de Canciones (`sort_order`). */
interface PreferencesRepository {
    /** `false` hasta que se toque «Empezar» u «Omitir». */
    val onboardingCompleted: Flow<Boolean>

    suspend fun completeOnboarding()

    /** Título A→Z si no hay nada guardado o no se puede leer. */
    val sortOrder: Flow<SongSort>

    suspend fun setSortOrder(sort: SongSort)
}

class DataStorePreferencesRepository(private val store: DataStore<Preferences>) : PreferencesRepository {

    // Archivo ilegible: se leen los valores por defecto (bienvenida sin completar, título A→Z).
    private val datos = store.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }

    override val onboardingCompleted: Flow<Boolean> = datos.map { it[ONBOARDING_COMPLETED] ?: false }

    override val sortOrder: Flow<SongSort> = datos.map { SongSort.parse(it[SORT_ORDER]) }

    override suspend fun setSortOrder(sort: SongSort) {
        // Disco lleno o ilegible: el orden se aplica igual en pantalla, solo no se recuerda.
        try {
            store.edit { it[SORT_ORDER] = sort.key() }
        } catch (_: IOException) {
        }
    }

    override suspend fun completeOnboarding() {
        // Disco lleno o ilegible: no se cierra la app; la bienvenida sigue y se puede volver a intentar.
        try {
            store.edit { it[ONBOARDING_COMPLETED] = true }
        } catch (_: IOException) {
        }
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SORT_ORDER = stringPreferencesKey("sort_order")
    }
}
