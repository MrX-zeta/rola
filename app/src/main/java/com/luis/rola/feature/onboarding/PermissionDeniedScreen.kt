package com.luis.rola.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.elevado
import com.luis.rola.core.designsystem.time

/** Permiso denegado (SCR-06). Relieve en el círculo y el botón; la ruta de Ajustes va plana (DEC-27). */
@Composable
fun PermissionDeniedScreen(onOpenSettings: () -> Unit, onRetry: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().background(c.background).systemBarsPadding()) {
        RolaLogo()
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = RolaSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(RolaSpacing.lg, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(120.dp).elevado(CircleShape).background(c.surfaceContainer, CircleShape), Alignment.Center) {
                Icon(painterResource(R.drawable.ic_music_note), null, tint = c.onSurfaceVariant, modifier = Modifier.size(56.dp))
            }
            Text(
                stringResource(R.string.denegado_titulo),
                style = MaterialTheme.typography.headlineMedium,
                color = c.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = RolaSpacing.xxs),
            )
            Text(stringResource(R.string.denegado_cuerpo), style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant, textAlign = TextAlign.Center)
            Text(
                stringResource(R.string.denegado_ruta),
                style = MaterialTheme.typography.time,
                color = c.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().background(c.surfaceContainer, MaterialTheme.shapes.medium)
                    .padding(horizontal = RolaSpacing.md, vertical = RolaSpacing.sm),
            )
        }
        Column(
            Modifier.padding(start = RolaSpacing.xl, end = RolaSpacing.xl, top = RolaSpacing.xl, bottom = RolaSpacing.xxl + RolaSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(RolaSpacing.xs),
        ) {
            BotonPrincipal(R.string.abrir_ajustes, onOpenSettings)
            TextButton(onRetry, Modifier.fillMaxWidth().height(56.dp)) {
                Text(
                    stringResource(R.string.intentar_de_nuevo),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = c.onSurface,
                )
            }
        }
    }
}
