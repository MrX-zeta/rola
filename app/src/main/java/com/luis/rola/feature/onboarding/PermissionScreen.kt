package com.luis.rola.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.acento
import com.luis.rola.core.designsystem.elevado
import com.luis.rola.core.designsystem.pill

/** Permisos (SCR-05). Relieve solo en la tarjeta y el botón (DEC-27). */
@Composable
fun PermissionScreen(onAllow: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val tarjeta = MaterialTheme.shapes.large
    Column(Modifier.fillMaxSize().background(c.background).systemBarsPadding()) {
        RolaLogo()
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(start = RolaSpacing.xl, end = RolaSpacing.xl, top = RolaSpacing.xxl + RolaSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(RolaSpacing.xl),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(RolaSpacing.sm)) {
                Text(stringResource(R.string.permisos_titulo), style = MaterialTheme.typography.headlineLarge, color = c.onSurface)
                Text(stringResource(R.string.permisos_cuerpo), style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant)
            }
            Column(Modifier.fillMaxWidth().elevado(tarjeta).background(c.surfaceContainer, tarjeta)) {
                PermisoFila(
                    R.drawable.ic_music_note, c.primaryContainer, c.onPrimaryContainer,
                    R.string.permiso_musica, R.string.permiso_musica_detalle,
                ) { Etiqueta(R.string.etiqueta_ahora, activa = true) }
                HorizontalDivider(color = c.outline)
                PermisoFila(
                    R.drawable.ic_notifications, c.surface, c.onSurfaceVariant,
                    R.string.permiso_notificaciones, R.string.permiso_notificaciones_detalle,
                ) { Etiqueta(R.string.etiqueta_despues, activa = false) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm)) {
                Icon(painterResource(R.drawable.ic_check), null, tint = c.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.nota_privacidad), style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
            }
        }
        BotonPrincipal(R.string.permitir_acceso, onAllow, Modifier.padding(start = RolaSpacing.xl, end = RolaSpacing.xl, top = RolaSpacing.xl, bottom = RolaSpacing.xxl + RolaSpacing.xs))
    }
}

@Composable
internal fun RolaLogo() {
    Row(
        Modifier.heightIn(min = 56.dp).padding(start = RolaSpacing.lg, end = RolaSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(R.drawable.ic_rola_simbolo), null)
        Text(stringResource(R.string.marca_palabra), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Botón principal lima con relieve de acento (DEC-16, DEC-27). */
@Composable
internal fun BotonPrincipal(texto: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val forma = MaterialTheme.shapes.pill
    Button(
        onClick,
        modifier.fillMaxWidth().height(56.dp).acento(forma),
        shape = forma,
        colors = ButtonDefaults.buttonColors(containerColor = RolaTheme.colors.accent, contentColor = RolaTheme.colors.onAccent),
    ) {
        Text(stringResource(texto), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun PermisoFila(
    icono: Int,
    fondoIcono: Color,
    tinteIcono: Color,
    titulo: Int,
    detalle: Int,
    etiqueta: @Composable () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(RolaSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(fondoIcono), Alignment.Center) {
            Icon(painterResource(icono), null, tint = tinteIcono)
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(titulo), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(detalle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        etiqueta()
    }
}

@Composable
private fun Etiqueta(texto: Int, activa: Boolean) {
    val forma = MaterialTheme.shapes.small
    val c = MaterialTheme.colorScheme
    Text(
        stringResource(texto),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (activa) FontWeight.SemiBold else FontWeight.Medium),
        color = if (activa) RolaTheme.colors.onAccent else c.onSurfaceVariant,
        modifier = (if (activa) Modifier.background(RolaTheme.colors.accent, forma) else Modifier.border(1.dp, c.outline, forma))
            .padding(horizontal = RolaSpacing.sm, vertical = RolaSpacing.xxs),
    )
}
