package com.luis.rola.feature.library

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.luis.rola.core.data.LibraryGrouping
import com.luis.rola.core.data.Song
import com.luis.rola.core.data.SongSort
import com.luis.rola.core.data.SortBy
import com.luis.rola.core.designsystem.RolaTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AlbumArtistTest {

    @get:Rule
    val compose = createComposeRule()

    private val canciones = listOf(
        Song(1, "Tercera", "Ana Vela", "Ventanas", 60_000, albumId = 7, track = 3, year = 2022),
        Song(2, "Primera", "Ana Vela", "Ventanas", 60_000, albumId = 7, track = 1, year = 2022),
        Song(3, "Segunda", "Ana Vela", "Ventanas", 60_000, albumId = 7, track = 2, year = 2022),
    )

    @Test
    fun detalleDeAlbumMuestraSusCancionesEnOrdenDePista() {
        val album = LibraryGrouping.albums(canciones).single()
        var reproducida = -1
        var aleatorio = false
        compose.setContent {
            RolaTheme { AlbumScreen(album, null, {}, { reproducida = it }, { aleatorio = true }, { _, _ -> }) }
        }
        compose.onNodeWithText("Ventanas").assertIsDisplayed()
        compose.onNodeWithText("Ana Vela · 2022 · 3 canciones · 3 min").assertIsDisplayed()
        val top = { t: String -> compose.onNodeWithText(t).getUnclippedBoundsInRoot().top }
        assertTrue(top("Primera") < top("Segunda"))
        assertTrue(top("Segunda") < top("Tercera"))
        assertEquals(3, compose.onAllNodesWithTag("pista").fetchSemanticsNodes().size)

        compose.onNodeWithText("Reproducir").performClick()
        assertEquals(0, reproducida)
        compose.onNodeWithText("Segunda").performClick()
        assertEquals(1, reproducida)
        compose.onNodeWithText("Aleatorio").performClick()
        assertTrue(aleatorio)
    }

    @Test
    fun albumDeMenosDeUnMinutoYSusOpciones() {
        val acciones = mutableListOf<SongAction>()
        val album = LibraryGrouping.albums(canciones.map { it.copy(durationMs = 1_000) }).single()
        compose.setContent {
            RolaTheme { AlbumScreen(album, null, {}, {}, {}, { _, _ -> }, onGroupAction = { acciones += it }) }
        }
        compose.onNodeWithText("Ana Vela · 2022 · 3 canciones · < 1 min").assertIsDisplayed()
        opcionesDelGrupo("Ventanas")
        assertEquals(listOf(SongAction.AddToQueue, SongAction.AddToPlaylist), acciones)
    }

    @Test
    fun opcionesDelArtista() {
        val acciones = mutableListOf<SongAction>()
        val artista = LibraryGrouping.artists(canciones).single()
        compose.setContent {
            RolaTheme { ArtistScreen(artista, null, {}, {}, {}, {}, { _, _ -> }, onGroupAction = { acciones += it }) }
        }
        opcionesDelGrupo("Ana Vela")
        assertEquals(listOf(SongAction.AddToQueue, SongAction.AddToPlaylist), acciones)
    }

    private fun opcionesDelGrupo(nombre: String) {
        compose.onNodeWithContentDescription("Opciones de $nombre").performClick()
        compose.onNodeWithText("Borrar del dispositivo").assertDoesNotExist()
        compose.onNodeWithText("Añadir a la cola").performClick()
        compose.onNodeWithContentDescription("Opciones de $nombre").performClick()
        compose.onNodeWithText("Añadir a una lista").performClick()
    }

    @Test
    fun hojaDeOrdenMarcaElActualYGuardaLaEleccion() {
        val prefs = PreferenciasEnMemoria()
        compose.setContent {
            val sort by prefs.sortOrder.collectAsState()
            RolaTheme { SortSheet(sort, { runBlocking { prefs.setSortOrder(it) } }, {}) }
        }
        compose.onNodeWithTag("hoja_orden").assertExists()
        compose.onNodeWithText("Título").assertIsDisplayed()
        compose.onNodeWithText("Artista").performClick()
        compose.onNodeWithText("Z → A").performClick()
        assertEquals(SongSort(SortBy.ARTIST, ascending = false), runBlocking { prefs.sortOrder.first() })
        compose.onNodeWithText("Z → A").assertIsSelected()
    }
}
