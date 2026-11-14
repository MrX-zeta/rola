package com.luis.rola.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Solo vista previa (source set debug): no entra al APK de release.

@Preview(name = "Muestrario", widthDp = 800, heightDp = 1300)
@Composable
private fun Muestrario() {
    Row(horizontalArrangement = Arrangement.spacedBy(RolaSpacing.md)) {
        RolaTheme(darkTheme = true) { Columna(Modifier.width(390.dp)) }
        RolaTheme(darkTheme = false) { Columna(Modifier.width(390.dp)) }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun Columna(modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val colores = listOf(
        "background" to c.background, "surface" to c.surface,
        "surfaceContainer" to c.surfaceContainer, "outline" to c.outline,
        "onSurface" to c.onSurface, "onSurfaceVariant" to c.onSurfaceVariant,
        "primary" to c.primary, "onPrimary" to c.onPrimary,
        "primaryContainer" to c.primaryContainer, "onPrimaryContainer" to c.onPrimaryContainer,
    )
    Surface(modifier, color = c.background) {
        Column(Modifier.padding(RolaSpacing.md), verticalArrangement = Arrangement.spacedBy(RolaSpacing.md)) {
            Text("rola", style = MaterialTheme.typography.displayLarge, color = c.onSurface)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(RolaSpacing.xs), verticalArrangement = Arrangement.spacedBy(RolaSpacing.xs)) {
                colores.forEach { (nombre, color) ->
                    Column {
                        Box(Modifier.size(width = 72.dp, height = 40.dp).background(color, MaterialTheme.shapes.small))
                        Text(nombre, style = MaterialTheme.typography.labelSmall, color = c.onSurfaceVariant)
                    }
                }
            }
            val t = MaterialTheme.typography
            listOf(
                "displayLarge" to t.displayLarge, "headlineMedium" to t.headlineMedium, "titleLarge" to t.titleLarge,
                "titleMedium" to t.titleMedium, "bodyLarge" to t.bodyLarge, "bodyMedium" to t.bodyMedium,
                "labelLarge" to t.labelLarge, "time 2:47" to t.time,
            ).forEach { (nombre, estilo) -> Text(nombre, style = estilo, color = c.onSurface) }
            val s = MaterialTheme.shapes
            Row(horizontalArrangement = Arrangement.spacedBy(RolaSpacing.xs)) {
                listOf(s.small, s.medium, s.large, s.extraLarge, s.pill).forEach {
                    Box(Modifier.size(width = 56.dp, height = 40.dp).background(c.primaryContainer, it))
                }
            }
        }
    }
}
