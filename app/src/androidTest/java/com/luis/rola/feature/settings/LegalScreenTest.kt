package com.luis.rola.feature.settings

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.luis.rola.core.designsystem.RolaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LegalScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private var atras = 0

    private fun montar(doc: LegalDoc) = compose.setContent { RolaTheme { LegalScreen(doc, onBack = { atras++ }) } }

    private val encabezado = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

    private fun leeHastaElFinalYVuelve(titulo: String, ultimo: String) {
        compose.onNode(hasText(titulo) and encabezado).assertIsDisplayed()
        compose.onNodeWithText(ultimo).performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Volver").performClick()
        assertEquals(1, atras)
    }

    @Test
    fun terminos() {
        montar(LegalDoc.TERMS)
        compose.onNode(hasText("Sin garantía") and encabezado).assertExists()
        leeHastaElFinalYVuelve(
            "Términos",
            "Rola se ofrece tal cual. Al usar «Borrar del dispositivo», Android siempre te pide confirmar; el archivo borrado no se puede recuperar desde Rola.",
        )
    }

    @Test
    fun privacidadSinNotificaciones() {
        montar(LegalDoc.PRIVACY)
        compose.onNodeWithText("Notificaciones", substring = true).assertDoesNotExist()
        leeHastaElFinalYVuelve("Privacidad", "Música y audio: para leer tus canciones. Carpeta de letras: solo la que tú elijas.")
    }

    @Test
    fun licenciasConCoilPaletteYReorderable() {
        montar(LegalDoc.LICENSES)
        listOf("Coil", "Palette", "Reorderable").forEach { compose.onNodeWithText(it).performScrollTo().assertIsDisplayed() }
        leeHastaElFinalYVuelve("Licencias", "Servicio de terceros · letras de la comunidad")
    }
}
