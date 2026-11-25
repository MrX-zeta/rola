package com.luis.rola.feature.search

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.luis.rola.core.data.FAVORITAS_ID
import com.luis.rola.core.data.LibraryGrouping
import com.luis.rola.core.data.LibraryIndex
import com.luis.rola.core.data.PlaylistSummary
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val songs = listOf(
        Song(1, "Agua de mar", "Ana Vela", "Ventanas", 0, albumId = 1),
        Song(2, "Mar adentro", "Club Sol", "Mar azul", 0, albumId = 2, albumArtist = "Marea Alta"),
        Song(3, "Brillo", "Los Faros", "Faros", 0, albumId = 3),
    )
    private val albums = LibraryGrouping.albums(songs)
    private val indice = LibraryIndex(
        songs,
        albums,
        LibraryGrouping.artists(songs, albums),
        listOf(PlaylistSummary(FAVORITAS_ID, "Favoritas", 2, songs.take(2)), PlaylistSummary(5, "Para el mar", 0, emptyList())),
    )
    private val toques = mutableListOf<Any?>()
    private var primario = Color.Unspecified

    private fun montar(inicial: String) {
        var texto by mutableStateOf(inicial)
        compose.setContent {
            RolaTheme {
                primario = MaterialTheme.colorScheme.primary
                SearchScreen(
                    texto,
                    indice.search(texto),
                    onQuery = { texto = it },
                    onPlay = { toques += it.title },
                    onSongAction = { s, a -> toques += "${s.title} $a" },
                    onOpenAlbum = { toques += it },
                    onOpenArtist = { toques += it },
                    onOpenPlaylist = { toques += it },
                )
            }
        }
    }

    private fun arriba(texto: String) = compose.onNodeWithText(texto).getUnclippedBoundsInRoot().top

    @Test
    fun sinTextoSoloElCampoConElCursorListo() {
        montar("")
        compose.onNodeWithTag("campo_buscar").assertIsFocused()
        compose.onNodeWithText("Canciones, álbumes, artistas o listas").assertIsDisplayed()
        listOf("resultados", "sin_resultados").forEach { compose.onNodeWithTag(it).assertDoesNotExist() }
        compose.onNodeWithContentDescription("Borrar búsqueda").assertDoesNotExist()
        compose.onNodeWithTag("campo_buscar").performTextInput("   ")
        compose.onNodeWithTag("sin_resultados").assertDoesNotExist()
    }

    @Test
    fun agrupaEnOrdenYResaltaLaCoincidencia() {
        montar("MAR")
        assertTrue(arriba("Canciones") < arriba("Mar adentro"))
        assertTrue(arriba("Mar adentro") < arriba("Agua de mar")) // empieza con > contiene
        assertTrue(arriba("Agua de mar") < arriba("Álbumes"))
        assertTrue(arriba("Álbumes") < arriba("Mar azul"))
        assertTrue(arriba("Mar azul") < arriba("Artistas"))
        assertTrue(arriba("Marea Alta") < arriba("Listas"))
        compose.onNodeWithText("Álbum · Marea Alta").assertExists()
        compose.onNodeWithText("Para el mar").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Brillo").assertDoesNotExist()
        compose.onNodeWithText("Favoritas").assertDoesNotExist()

        val titulo = compose.onNodeWithText("Agua de mar", useUnmergedTree = true).fetchSemanticsNode().config[SemanticsProperties.Text].single()
        val tramo = titulo.spanStyles.single { it.item.color == primario }
        assertEquals("mar", titulo.text.substring(tramo.start, tramo.end))
    }

    @Test
    fun losGruposVaciosNoSalenYFavoritasSeEncuentra() {
        montar("favor")
        compose.onNodeWithText("Favoritas").assertIsDisplayed()
        compose.onNodeWithText("2 canciones").assertIsDisplayed()
        listOf("Canciones", "Álbumes", "Artistas").forEach { compose.onNodeWithText(it).assertDoesNotExist() }
    }

    @Test
    fun sinResultadosDiceQueSeBuscoYSePuedeBorrar() {
        montar("lluvia ácida")
        compose.onNodeWithText("Sin resultados para «lluvia ácida»").assertIsDisplayed()
        compose.onNodeWithContentDescription("Borrar búsqueda").performClick()
        compose.onNodeWithTag("sin_resultados").assertDoesNotExist()
        compose.onNodeWithTag("campo_buscar").assertIsFocused()
    }

    @Test
    fun cadaResultadoAbreLoSuyoYLaCancionTieneSuMenu() {
        montar("mar")
        compose.onNodeWithText("Mar adentro").performClick()
        compose.onNodeWithText("Mar azul").performClick()
        compose.onNodeWithText("Marea Alta").performClick()
        compose.onNodeWithText("Para el mar").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Opciones de Agua de mar").performScrollTo().performClick()
        listOf("Añadir a una lista", "Favorita", "Borrar del dispositivo").forEach { compose.onNodeWithText(it).assertExists() }
        compose.onNodeWithText("Añadir a la cola").performClick()
        assertEquals(listOf("Mar adentro", 2L, "Marea Alta", 5L, "Agua de mar AddToQueue"), toques)
    }
}
