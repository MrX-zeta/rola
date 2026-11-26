package com.luis.rola.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.subhead
import com.luis.rola.core.designsystem.timeSmall

/** Documentos de ACERCA DE (DEC-23): título y pares encabezado/texto; en Licencias, librería/licencia. */
enum class LegalDoc(val titulo: Int, val partes: List<Pair<Int, Int>>) {
    TERMS(
        R.string.terminos,
        listOf(
            R.string.terminos_1_titulo to R.string.terminos_1_texto,
            R.string.terminos_2_titulo to R.string.terminos_2_texto,
            R.string.terminos_3_titulo to R.string.terminos_3_texto,
        ),
    ),
    PRIVACY(
        R.string.privacidad,
        listOf(
            R.string.privacidad_1_titulo to R.string.privacidad_1_texto,
            R.string.privacidad_2_titulo to R.string.privacidad_2_texto,
            R.string.privacidad_3_titulo to R.string.privacidad_3_texto,
        ),
    ),
    LICENSES(
        R.string.licencias,
        listOf(
            R.string.lib_space_grotesk to R.string.licencia_ofl,
            R.string.lib_geist to R.string.licencia_ofl,
            R.string.lib_compose to R.string.licencia_apache,
            R.string.lib_media3 to R.string.licencia_apache,
            R.string.lib_room to R.string.licencia_apache,
            R.string.lib_coil to R.string.licencia_apache,
            R.string.lib_palette to R.string.licencia_apache,
            R.string.lib_reorderable to R.string.licencia_apache,
            R.string.lib_kotlin to R.string.licencia_apache,
            R.string.lib_lrclib to R.string.licencia_lrclib,
        ),
    ),
}

/** Términos (SCR-72, 41:1778), Privacidad (SCR-73, 41:1810) y Licencias (SCR-74, 41:1842). Planos y desplazables. */
@Composable
fun LegalScreen(doc: LegalDoc, onBack: () -> Unit, contentPadding: PaddingValues = PaddingValues()) {
    val c = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(contentPadding).testTag("documento")) {
        BarraConVolver(stringResource(doc.titulo), onBack)
        if (doc == LegalDoc.LICENSES) {
            Column(Modifier.padding(top = RolaSpacing.xxs)) {
                doc.partes.forEach { (libreria, licencia) ->
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = RolaSpacing.xl, vertical = RolaSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(stringResource(libreria), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = c.onSurface)
                        Text(stringResource(licencia), style = MaterialTheme.typography.timeSmall, color = c.onSurfaceVariant)
                    }
                }
            }
        } else {
            Column(Modifier.padding(start = RolaSpacing.xl, end = RolaSpacing.xl, top = RolaSpacing.xs), verticalArrangement = Arrangement.spacedBy(RolaSpacing.sm)) {
                Text(stringResource(R.string.legal_actualizacion), style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
                doc.partes.forEach { (titulo, texto) ->
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(titulo), Modifier.semantics { heading() }, style = MaterialTheme.typography.subhead, color = c.onSurface)
                    Text(stringResource(texto), style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(RolaSpacing.xl))
    }
}
