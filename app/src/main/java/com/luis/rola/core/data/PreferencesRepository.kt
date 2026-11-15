package com.luis.rola.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Ajustes del usuario (KV-DATASTORE). Por ahora solo si la bienvenida se completó (DEC-18). */
interface PreferencesRepository {
    /** `false` hasta que se toque «Empezar» u «Omitir». */
    val onboardingCompleted: Flow<Boolean>

    suspend fun completeOnboarding()
}

class DataStorePreferencesRepository(private val store: DataStore<Preferences>) : PreferencesRepository {

    override val onboardingCompleted: Flow<Boolean> = store.data
        // Archivo ilegible: se toma «no completada» y la bienvenida vuelve a salir (el lado seguro).
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[ONBOARDING_COMPLETED] ?: false }

    override suspend fun completeOnboarding() {
        // Disco lleno o ilegible: no se cierra la app; la bienvenida sigue y se puede volver a intentar.
        try {
            store.edit { it[ONBOARDING_COMPLETED] = true }
        } catch (_: IOException) {
        }
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}
