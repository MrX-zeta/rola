package com.luis.rola.feature.library

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SongMenuTest {

    @get:Rule
    val compose = createComposeRule()

    private val song = Song(1, "Brillo", "Los Faros", null, 0)
    private var accion: SongAction? = null

    private fun montar(place: MenuPlace, favorite: Boolean) = compose.setContent {
        RolaTheme { SongItem(song, false, place, favorite, { accion = it }) {} }
    }

    @Test
    fun enBibliotecaTieneCuatroOpciones() {
        montar(MenuPlace.Library, favorite = false)
        compose.onNodeWithContentDescription("Opciones de Brillo").performClick()
        listOf("Añadir a la cola", "Añadir a una lista", "Favorita", "Borrar del dispositivo").forEach {
            compose.onNodeWithText(it).assertExists()
        }
        compose.onNodeWithText("Quitar de la lista").assertDoesNotExist()
        compose.onNodeWithText("Borrar del dispositivo").performClick()
        assertEquals(SongAction.Delete, accion)
        compose.onNodeWithText("Añadir a la cola").assertDoesNotExist() // se cerró
    }

    @Test
    fun siEsFavoritaOfreceQuitarla() {
        montar(MenuPlace.Library, favorite = true)
        compose.onNodeWithContentDescription("Opciones de Brillo").performClick()
        compose.onNodeWithText("Quitar de favoritas").performClick()
        assertEquals(SongAction.ToggleFavorite, accion)
    }

    @Test
    fun enUnaListaSoloQuitarYCola_conPulsacionLarga() {
        montar(MenuPlace.Playlist, favorite = false)
        compose.onNodeWithText("Brillo").performTouchInput { longClick() }
        compose.onNodeWithText("Quitar de la lista").assertExists()
        compose.onNodeWithText("Añadir a la cola").assertExists()
        compose.onNodeWithText("Añadir a una lista").assertDoesNotExist()
        compose.onNodeWithText("Borrar del dispositivo").assertDoesNotExist()
    }
}
