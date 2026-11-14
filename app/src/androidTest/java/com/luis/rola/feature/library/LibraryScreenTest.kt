package com.luis.rola.feature.library

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luis.rola.core.data.Song
import com.luis.rola.core.data.SongRepository
import com.luis.rola.core.designsystem.RolaTheme
import kotlinx.coroutines.flow.flowOf
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
}
