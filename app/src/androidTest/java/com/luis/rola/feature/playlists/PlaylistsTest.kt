package com.luis.rola.feature.playlists

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import com.luis.rola.core.data.FAVORITAS_ID
import com.luis.rola.core.data.PlaylistDetail
import com.luis.rola.core.data.PlaylistSummary
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlaylistsTest {

    @get:Rule
    val compose = createComposeRule()

    private val agua = Song(1, "Agua de mar", "Ana Vela", null, 60_000)
    private val brillo = Song(2, "Brillo", "Los Faros", null, 60_000)
    private val cancion = Song(3, "Canción", "Club Sol", null, 60_000)

    @Test
    fun favoritasVaPrimeraDebajoDeNuevaListaAunqueEsteVacia() {
        compose.setContent {
            RolaTheme {
                PlaylistsTab(
                    PlaylistSummary(FAVORITAS_ID, "", 0, emptyList()),
                    listOf(PlaylistSummary(5, "Noche", 2, listOf(agua, brillo))),
                    onCreate = {},
                    onOpen = {},
                )
            }
        }
        val top = { t: String -> compose.onNodeWithText(t).getUnclippedBoundsInRoot().top }
        assertTrue(top("Nueva lista") < top("Favoritas"))
        assertTrue(top("Favoritas") < top("Noche"))
        compose.onNodeWithText("Vacía").assertExists()
        compose.onNodeWithText("2 canciones").assertExists()
    }

    @Test
    fun listaVaciaTieneSuBotonYFavoritasNo() {
        var agregar = 0
        compose.setContent {
            RolaTheme { PlaylistScreen(PlaylistDetail(5, "Viaje", emptyList()), false, null, {}, {}, {}, { agregar++ }, { _, _ -> }) }
        }
        compose.onNodeWithTag("lista_vacia").assertExists()
        compose.onNodeWithText("Agregar canciones").performClick()
        assertEquals(1, agregar)
    }

    @Test
    fun favoritasSinBotonAgregar() {
        compose.setContent {
            RolaTheme { PlaylistScreen(PlaylistDetail(FAVORITAS_ID, "", listOf(agua)), true, null, {}, {}, {}, {}, { _, _ -> }) }
        }
        compose.onNodeWithText("Favoritas").assertExists()
        compose.onNodeWithContentDescription("Agregar").assertDoesNotExist()
        compose.onNodeWithContentDescription("Aleatorio").assertExists()
    }

    @Test
    fun hojaFiltraSinAcentosYAgregaEnElOrdenDeLaHoja() {
        var agregadas: List<Long>? = null
        compose.setContent {
            RolaTheme { AddSongsSheet(listOf(agua, brillo, cancion), emptySet(), { agregadas = it }, {}) }
        }
        compose.onNodeWithText("Brillo").performClick()
        compose.onNodeWithText("Agua de mar").performClick()
        compose.onNodeWithText("Agregar 2 canciones").performClick()
        assertEquals(listOf(1L, 2L), agregadas)
    }

    @Test
    fun hojaBuscaSinAcentosYSinMarcadasNoAgrega() {
        compose.setContent {
            RolaTheme { AddSongsSheet(listOf(agua, brillo, cancion), emptySet(), {}, {}) }
        }
        compose.onNode(hasText("Agregar canciones") and hasClickAction()).assertIsNotEnabled()
        compose.onNodeWithText("Buscar en tu biblioteca").performTextInput("cancion")
        compose.onNodeWithText("Canción").assertExists()
        compose.onNodeWithText("Brillo").assertDoesNotExist()
    }
}
