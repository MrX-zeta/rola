package com.luis.rola.feature.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.designsystem.RolaIlustracion
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.acento
import com.luis.rola.core.designsystem.elevado
import com.luis.rola.core.designsystem.pill
import com.luis.rola.core.designsystem.timeSmall

// Ilustraciones decorativas de la bienvenida (FR-019): formas del tema con las medidas de Figma, sin semántica.

/** SCR-02 (22:301): tres portadas en abanico y el botón reproducir. Se encoge si no cabe en 340 × 300 dp. */
@Composable
internal fun PortadasEnAbanico(modifier: Modifier = Modifier) = Escalada(340.dp, 300.dp, modifier) {
    Portada(150.dp, RolaIlustracion.azul, Modifier.offset(24.dp, 78.dp).rotate(-10f)) { Barras(it) }
    Portada(150.dp, RolaIlustracion.verde, Modifier.offset(166.dp, 78.dp).rotate(10f)) { CuartoDeAnillo(it) }
    Portada(196.dp, RolaIlustracion.naranja, Modifier.offset(72.dp, 36.dp)) { lado ->
        Box(Modifier.align(Alignment.Center).size(lado * 0.45f).background(RolaIlustracion.naranjaClaro, CircleShape))
    }
    Box(
        Modifier.offset(134.dp, 196.dp).size(72.dp).acento(CircleShape).background(RolaTheme.colors.accent, CircleShape),
        Alignment.Center,
    ) { Icon(painterResource(R.drawable.ic_play), null, tint = RolaTheme.colors.onAccent) }
}

/** SCR-03 (22:348): tarjeta con una canción, su letra con la línea activa y los chips de origen. */
@Composable
internal fun TarjetaDeLetra(modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val forma = MaterialTheme.shapes.large
    Column(
        modifier.clearAndSetSemantics {}.padding(horizontal = RolaSpacing.xxl).widthIn(max = 348.dp).fillMaxWidth()
            .elevado(forma).background(c.surfaceContainer, forma)
            .padding(start = RolaSpacing.xl, end = RolaSpacing.xl, top = RolaSpacing.lg, bottom = RolaSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Portada(44.dp, RolaIlustracion.azul, relieve = false) { Barras(it) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.ejemplo_cancion), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = c.onSurface, maxLines = 1)
                Text(stringResource(R.string.ejemplo_artista), style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1)
            }
            Text(stringResource(R.string.ejemplo_tiempo), style = MaterialTheme.typography.timeSmall, color = c.onSurfaceVariant)
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val linea = MaterialTheme.typography.titleLarge
            Text(stringResource(R.string.ejemplo_letra_1), style = linea, color = c.onSurfaceVariant.copy(alpha = .6f))
            Text(stringResource(R.string.ejemplo_letra_2), style = MaterialTheme.typography.headlineSmall, color = c.primary)
            Text(stringResource(R.string.ejemplo_letra_3), style = linea, color = c.onSurfaceVariant)
            Text(stringResource(R.string.ejemplo_letra_4), style = linea, color = c.onSurfaceVariant.copy(alpha = .4f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(RolaSpacing.xs)) {
            val chip = MaterialTheme.shapes.pill
            Row(
                Modifier.background(c.primaryContainer, chip).padding(start = 10.dp, end = RolaSpacing.sm, top = 6.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_lyrics), null, tint = c.onPrimaryContainer)
                Text(stringResource(R.string.letra_sincronizada), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = c.onPrimaryContainer)
            }
            Text(
                stringResource(R.string.letra_fuente),
                Modifier.border(1.dp, c.outline, chip).padding(horizontal = RolaSpacing.sm, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = c.onSurfaceVariant,
            )
        }
    }
}

/** SCR-04 (22:406): wifi tachado y el ajuste de letras en línea apagado. El interruptor es decorativo (aclaración 2). */
@Composable
internal fun SinConexion(modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    Column(
        modifier.clearAndSetSemantics {}.padding(horizontal = RolaSpacing.xxl).widthIn(max = 348.dp),
        verticalArrangement = Arrangement.spacedBy(RolaSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(148.dp).elevado(CircleShape).background(c.surfaceContainer, CircleShape), Alignment.Center) {
            Icon(painterResource(R.drawable.ic_wifi), null, tint = c.onSurfaceVariant)
            // Tachado de Figma (22:412): línea gruesa del color del círculo y encima la fina.
            Canvas(Modifier.fillMaxSize()) {
                val a = Offset(44.dp.toPx(), 40.dp.toPx())
                val b = Offset(106.dp.toPx(), 112.dp.toPx())
                drawLine(c.surfaceContainer, a, b, 16.dp.toPx(), StrokeCap.Round)
                drawLine(c.onSurfaceVariant, a, b, 6.dp.toPx(), StrokeCap.Round)
            }
        }
        val forma = MaterialTheme.shapes.medium
        Row(
            Modifier.fillMaxWidth().elevado(forma).background(c.surface, forma).padding(start = RolaSpacing.lg, end = RolaSpacing.md, top = RolaSpacing.md, bottom = RolaSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(RolaSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.buscar_letras_en_linea), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = c.onSurface)
                Text(stringResource(R.string.buscar_letras_apagado), style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
            }
            Switch(checked = false, onCheckedChange = null)
        }
    }
}

/** Encoge [contenido] (nunca agranda) si su alto natural no cabe, por ejemplo con letra del sistema grande (INV-06). */
@Composable
internal fun AjustarAlto(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) = Layout(contenido, modifier) { medibles, c ->
    val p = medibles.first().measure(c.copy(minHeight = 0, maxHeight = Constraints.Infinity))
    val escala = minOf(1f, c.maxHeight / p.height.toFloat())
    layout(c.maxWidth, c.maxHeight) {
        p.placeWithLayer((c.maxWidth - p.width) / 2, (c.maxHeight - p.height) / 2) { scaleX = escala; scaleY = escala }
    }
}

/** Dibuja [contenido] a su tamaño de Figma y lo encoge (nunca agranda) para que quepa. */
@Composable
private fun Escalada(ancho: Dp, alto: Dp, modifier: Modifier, contenido: @Composable BoxScope.() -> Unit) {
    BoxWithConstraints(modifier.clearAndSetSemantics {}, Alignment.Center) {
        val escala = minOf(1f, maxWidth / ancho, maxHeight / alto)
        Box(Modifier.requiredSize(ancho, alto).graphicsLayer { scaleX = escala; scaleY = escala }, content = contenido)
    }
}

/** Portada de ejemplo (componente «Portada» 5:44): cuadrado de color con radio 7.5 % del lado. */
@Composable
private fun Portada(lado: Dp, color: Color, modifier: Modifier = Modifier, relieve: Boolean = true, dibujo: @Composable BoxScope.(Dp) -> Unit) {
    val forma = RoundedCornerShape(lado * 0.075f)
    Box(modifier.size(lado).then(if (relieve) Modifier.elevado(forma) else Modifier).background(color, forma)) { dibujo(lado) }
}

/** Tres barras de la portada azul (15 % de margen a los lados, 8.75 % de alto). */
@Composable
private fun BoxScope.Barras(lado: Dp) {
    listOf(.2875f, .45f, .6125f).forEach { arriba ->
        Box(
            Modifier.offset(lado * .15f, lado * arriba).size(lado * .7f, lado * .0875f)
                .background(RolaIlustracion.azulClaro, RoundedCornerShape(lado * .044f)),
        )
    }
}

/** Cuarto de anillo de la portada verde, con centro cerca de la esquina inferior derecha. */
@Composable
private fun CuartoDeAnillo(lado: Dp) = Canvas(Modifier.size(lado)) {
    val grosor = size.width * .3025f
    val radio = size.width * .399f
    val centro = Offset(size.width * .9f, size.height * .9f)
    drawArc(
        RolaIlustracion.verdeClaro, 180f, 90f, useCenter = false,
        topLeft = centro - Offset(radio, radio), size = Size(radio * 2, radio * 2), style = Stroke(grosor),
    )
}
