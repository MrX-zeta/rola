package com.luis.rola.feature.player

import android.content.res.Resources
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.media.SleepTimerOption
import com.luis.rola.core.media.formatDuration
import kotlinx.coroutines.delay

/** Texto de cada opción de SCR-53. */
fun SleepTimerOption.label() = when (this) {
    SleepTimerOption.OFF -> R.string.temporizador_apagado
    SleepTimerOption.M15 -> R.string.temporizador_15
    SleepTimerOption.M30 -> R.string.temporizador_30
    SleepTimerOption.M45 -> R.string.temporizador_45
    SleepTimerOption.H1 -> R.string.temporizador_60
    SleepTimerOption.END_OF_SONG -> R.string.temporizador_cancion
}

/** Aviso al elegir una opción, igual desde Reproduciendo y desde Ajustes. */
fun Resources.timerMessage(option: SleepTimerOption): String = when (option) {
    SleepTimerOption.OFF -> getString(R.string.temporizador_cancelado)
    SleepTimerOption.END_OF_SONG -> getString(R.string.temporizador_restante_cancion)
    else -> getString(R.string.temporizador_restante, formatDuration(option.minutes * 60_000L))
}

/**
 * Hoja del temporizador (SCR-53, nodos 36:1651 y 51:4662), con los colores del tema (FR-040).
 * Con uno activo muestra cuánto falta; elegir «Apagado» e iniciar lo cancela.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerSheet(
    option: SleepTimerOption,
    endsAtMs: Long?,
    onStart: (SleepTimerOption) -> Unit,
    onDismiss: () -> Unit,
) {
    val m = MaterialTheme.colorScheme
    var elegida by rememberSaveable { mutableStateOf(option) }
    val faltan by produceState(endsAtMs?.let { it - SystemClock.elapsedRealtime() }, endsAtMs) {
        while (endsAtMs != null) {
            value = (endsAtMs - SystemClock.elapsedRealtime()).coerceAtLeast(0)
            delay(1_000)
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = m.surface,
        dragHandle = {
            Box(Modifier.padding(vertical = RolaSpacing.sm).size(36.dp, 4.dp).clip(MaterialTheme.shapes.extraSmall).background(m.onSurfaceVariant))
        },
    ) {
        Column(Modifier.navigationBarsPadding().padding(bottom = RolaSpacing.xl)) {
            Text(
                stringResource(R.string.temporizador),
                Modifier.padding(start = RolaSpacing.xl, end = RolaSpacing.xl, bottom = RolaSpacing.xxs),
                style = MaterialTheme.typography.titleLarge,
                color = m.onSurface,
            )
            Text(
                when {
                    option == SleepTimerOption.END_OF_SONG -> stringResource(R.string.temporizador_restante_cancion)
                    faltan != null -> stringResource(R.string.temporizador_restante, formatDuration(faltan!!))
                    else -> stringResource(R.string.temporizador_subtitulo)
                },
                Modifier.padding(start = RolaSpacing.xl, end = RolaSpacing.xl, bottom = RolaSpacing.xs),
                style = MaterialTheme.typography.bodyMedium,
                color = m.onSurfaceVariant,
            )
            Column(Modifier.selectableGroup()) {
                SleepTimerOption.entries.forEach { o ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(elegida == o, role = Role.RadioButton) { elegida = o }
                            .padding(horizontal = RolaSpacing.xl, vertical = 13.dp),
                        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(elegida == o, onClick = null, Modifier.size(20.dp))
                        Text(
                            stringResource(o.label()),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = m.onSurface,
                        )
                    }
                }
            }
            Button(
                onClick = { onStart(elegida) },
                modifier = Modifier.padding(start = RolaSpacing.xl, end = RolaSpacing.xl, top = RolaSpacing.xs).fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RolaTheme.colors.accent, contentColor = RolaTheme.colors.onAccent),
            ) {
                Text(stringResource(R.string.iniciar_temporizador), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            }
        }
    }
}
