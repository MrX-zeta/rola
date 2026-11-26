package com.luis.rola.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PreferencesRepositoryTest {

    @get:Rule
    val carpeta = TemporaryFolder()

    /** DataStore real sobre un archivo temporal; [bloque] recibe el repositorio y luego se cierra el DataStore. */
    private fun conRepositorio(bloque: suspend (PreferencesRepository) -> Unit) = runBlocking {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val store = PreferenceDataStoreFactory.create(scope = scope) { carpeta.root.resolve("rola_ajustes.preferences_pb") }
        try {
            bloque(DataStorePreferencesRepository(store))
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `empieza sin completar`() = conRepositorio { assertFalse(it.onboardingCompleted.first()) }

    @Test
    fun `completar la bienvenida la marca y repetirlo no falla`() = conRepositorio {
        it.completeOnboarding()
        it.completeOnboarding()
        assertTrue(it.onboardingCompleted.first())
    }

    @Test
    fun `lo completado sobrevive a cerrar y abrir el archivo`() {
        conRepositorio { it.completeOnboarding() }
        conRepositorio { assertTrue(it.onboardingCompleted.first()) }
    }

    @Test
    fun `el orden empieza en titulo A-Z y se recuerda al reabrir`() {
        conRepositorio { assertEquals(SongSort(), it.sortOrder.first()) }
        conRepositorio { it.setSortOrder(SongSort(SortBy.ARTIST, ascending = false)) }
        conRepositorio { assertEquals(SongSort(SortBy.ARTIST, ascending = false), it.sortOrder.first()) }
    }

    @Test
    fun `el tema empieza oscuro y se recuerda al reabrir`() {
        conRepositorio { assertEquals(ThemeMode.DARK, it.theme.first()) }
        conRepositorio { it.setTheme(ThemeMode.LIGHT) }
        conRepositorio { assertEquals(ThemeMode.LIGHT, it.theme.first()) }
    }

    @Test
    fun `sin escaneo no hay fecha y la guardada se recuerda`() {
        conRepositorio { assertEquals(null, it.lastScanAt.first()) }
        conRepositorio { it.setLastScanAt(1_700_000_000_000) }
        conRepositorio { assertEquals(1_700_000_000_000, it.lastScanAt.first()) }
    }
}
