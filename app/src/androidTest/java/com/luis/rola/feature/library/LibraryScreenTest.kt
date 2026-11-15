package com.luis.rola.feature.library

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luis.rola.core.data.Song
import com.luis.rola.core.data.SongRepository
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
        override fun songs() = flowOf(
            listOf(
                Song(1, "Agua de mar", "Ana Vela", null, 1000),
                Song(2, "Brillo", "Los Faros", null, 1000),
                Song(3, "Calle ocho", null, null, 1000),
            ),
        )
    }

    @Test
    fun muestraLaListaDelRepositorio() {
        val vm = LibraryViewModel(falso)
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
        val vm = LibraryViewModel(object : SongRepository {
            override fun songs() = flow<List<Song>> { awaitCancellation() }
        })
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
}
