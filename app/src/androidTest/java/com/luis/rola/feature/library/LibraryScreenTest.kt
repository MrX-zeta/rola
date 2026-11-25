package com.luis.rola.feature.library

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luis.rola.core.data.LibraryGrouping
import com.luis.rola.core.data.PreferencesRepository
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import com.luis.rola.core.data.Song
import com.luis.rola.core.data.SongSort
import kotlinx.coroutines.flow.MutableStateFlow
import com.luis.rola.core.data.LibraryRepository
import com.luis.rola.core.data.SongRepository
import com.luis.rola.core.database.memoryDb
import kotlinx.coroutines.flow.Flow
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaTheme
import androidx.compose.ui.test.onNodeWithContentDescription
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class LibraryScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val falso = object : SongRepository {
        override suspend fun scan() = 0
        override fun songs() = flowOf(
            listOf(
                Song(1, "Agua de mar", "Ana Vela", null, 1000),
                Song(2, "Brillo", "Los Faros", null, 1000),
                Song(3, "Calle ocho", null, null, 1000),
            ),
        )
    }

    private fun vm(songs: Flow<List<Song>>) = LibraryViewModel(songs, LibraryRepository(memoryDb(), songs), PreferenciasEnMemoria())

    @Test
    fun muestraLaListaDelRepositorio() {
        val vm = vm(falso.songs())
        compose.setContent {
            RolaTheme {
                val songs by vm.songs.collectAsStateWithLifecycle()
                LibraryScreen(songs, currentId = null, onSongClick = {}, onShuffle = {})
            }
        }
        compose.onNodeWithText("3 canciones · Título A–Z").assertIsDisplayed()
        compose.onNodeWithText("Agua de mar").assertIsDisplayed()
        compose.onNodeWithText("Brillo").assertIsDisplayed()
        compose.onNodeWithText("Calle ocho").assertIsDisplayed()
        compose.onNodeWithText("Artista desconocido").assertIsDisplayed()
    }

    @Test
    fun muestraEsqueletoMientrasNoHayDatos() {
        val vm = vm(flow { awaitCancellation() })
        compose.mainClock.autoAdvance = false
        compose.setContent {
            RolaTheme {
                val songs by vm.songs.collectAsStateWithLifecycle()
                LibraryScreen(songs, currentId = null, onSongClick = {}, onShuffle = {})
            }
        }
        compose.mainClock.advanceTimeBy(100)
        compose.onNodeWithContentDescription("Cargando canciones").assertDoesNotExist() // sin parpadeo antes de 300 ms
        compose.mainClock.advanceTimeBy(400)
        compose.onNodeWithContentDescription("Cargando canciones").assertExists()
    }

    private val titulos = listOf("Agua de mar", "Brillo", "Calle ocho")

    /** Posición de cada fila en el primer cuadro y cuando ya terminó cualquier cascada. */
    private fun posiciones(reduce: Boolean): Pair<List<Float>, List<Float>> {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            RolaTheme { // RolaTheme fija LocalReduceMotion desde el sistema: se sobrescribe adentro.
                CompositionLocalProvider(LocalReduceMotion provides reduce) {
                    LibraryScreen(
                        titulos.mapIndexed { i, t -> Song(i + 1L, t, "Ana Vela", null, 1000) },
                        currentId = null, onSongClick = {}, onShuffle = {},
                    )
                }
            }
        }
        val top = { titulos.map { compose.onNodeWithText(it).getUnclippedBoundsInRoot().top.value } }
        val primerCuadro = top()
        compose.mainClock.advanceTimeBy(1_000)
        return primerCuadro to top()
    }

    @Test
    fun conReducirMovimientoLasFilasEstanEnSuLugarDesdeElPrimerCuadro() {
        val (primerCuadro, final) = posiciones(reduce = true)
        assertEquals(final, primerCuadro)
    }

    @Test
    fun sinReducirMovimientoLasFilasEntranDesdeAbajo() {
        val (primerCuadro, final) = posiciones(reduce = false)
        assertNotEquals(final, primerCuadro) // la prueba de arriba sí distingue una cascada
    }

    @Test
    fun chipsEnOrdenAbrenEnCancionesYCambianElContenido() {
        val songs = listOf(
            Song(1, "Agua de mar", "Ana Vela", "Ventanas", 1000, albumId = 7, year = 2022),
            Song(2, "Brillo", "Los Faros", "Faros", 1000, albumId = 8, year = 2020),
        )
        val albums = LibraryGrouping.albums(songs)
        compose.setContent {
            RolaTheme {
                LibraryScreen(songs, null, {}, {}, albums = albums, artists = LibraryGrouping.artists(songs, albums))
            }
        }
        val left = { tag: String -> compose.onNodeWithTag(tag).getUnclippedBoundsInRoot().left }
        assertTrue(left("chip_listas") < left("chip_albumes"))
        assertTrue(left("chip_albumes") < left("chip_artistas"))
        assertTrue(left("chip_artistas") < left("chip_canciones"))
        compose.onNodeWithTag("chip_canciones").assertIsSelected()
        compose.onNodeWithText("2 canciones · Título A–Z").assertIsDisplayed()

        compose.onNodeWithTag("chip_albumes").performClick()
        compose.onNodeWithText("Ana Vela · 2022").assertIsDisplayed()
        compose.onNodeWithText("2 canciones · Título A–Z").assertDoesNotExist()

        compose.onNodeWithTag("chip_artistas").performClick()
        compose.onNodeWithText("Los Faros").assertIsDisplayed()
        compose.onAllNodesWithText("1 canción · 1 álbum").assertCountEquals(2)

        compose.onNodeWithTag("chip_listas").performClick()
        compose.onNodeWithText("Nueva lista").assertIsDisplayed()
    }
}

/** Ajustes en memoria para las pruebas de Biblioteca. */
class PreferenciasEnMemoria : PreferencesRepository {
    override val onboardingCompleted = MutableStateFlow(true)
    override suspend fun completeOnboarding() {}
    override val sortOrder = MutableStateFlow(SongSort())
    override suspend fun setSortOrder(sort: SongSort) { sortOrder.value = sort }
}
