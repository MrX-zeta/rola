package com.luis.rola.feature.settings

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luis.rola.BuildConfig
import com.luis.rola.R
import com.luis.rola.core.data.ThemeMode
import com.luis.rola.core.data.TimeAgo
import com.luis.rola.core.data.timeAgo
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.timeSmall
import com.luis.rola.core.media.SleepTimerOption
import com.luis.rola.core.media.formatDuration
import com.luis.rola.feature.player.SleepTimerSheet
import kotlinx.coroutines.delay

/**
 * Ajustes (SCR-70, Figma 40:1674 / claro 51:5129). Plano (DEC-27). Solo lo que ya existe (DEC-30):
 * sin LETRAS, «Carpeta de letras» ni «Agregar widget» hasta las tareas 09 y 10.
 */
@Composable
fun SettingsScreen(
    theme: ThemeMode,
    lastScanAt: Long?,
    scanning: Boolean,
    timer: SleepTimerOption,
    timerEndsAtMs: Long?,
    onBack: () -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onScan: () -> Unit,
    onTimer: (SleepTimerOption) -> Unit,
    onOpenDoc: (LegalDoc) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    var hojaTema by rememberSaveable { mutableStateOf(false) }
    var hojaTemporizador by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(contentPadding)) {
        BarraConVolver(stringResource(R.string.ajustes), onBack)
        Seccion(R.string.seccion_reproduccion)
        Tarjeta {
            Fila(
                R.drawable.ic_timer,
                R.string.temporizador_de_apagado,
                stringResource(R.string.temporizador_de_apagado_texto),
                valor = timerText(timer, timerEndsAtMs),
            ) { hojaTemporizador = true }
        }
        Seccion(R.string.seccion_biblioteca)
        Tarjeta {
            Fila(R.drawable.ic_refresh, R.string.escanear_medios, lastScanText(lastScanAt), ocupado = scanning, onClick = onScan)
        }
        Seccion(R.string.seccion_apariencia)
        Tarjeta {
            Fila(R.drawable.ic_palette, R.string.tema, stringResource(theme.label())) { hojaTema = true }
        }
        Seccion(R.string.seccion_acerca_de)
        Tarjeta {
            Fila(R.drawable.ic_document, R.string.terminos_y_condiciones) { onOpenDoc(LegalDoc.TERMS) }
            Divisor()
            Fila(R.drawable.ic_shield, R.string.aviso_de_privacidad) { onOpenDoc(LegalDoc.PRIVACY) }
            Divisor()
            Fila(R.drawable.ic_document, R.string.licencias) { onOpenDoc(LegalDoc.LICENSES) }
            Divisor()
            Fila(null, R.string.version, stringResource(R.string.version_rola, BuildConfig.VERSION_NAME))
        }
        Spacer(Modifier.height(40.dp))
    }
    if (hojaTema) {
        ThemeSheet(theme, onChoose = { hojaTema = false; onTheme(it) }, onDismiss = { hojaTema = false })
    }
    if (hojaTemporizador) {
        SleepTimerSheet(timer, timerEndsAtMs, onStart = { hojaTemporizador = false; onTimer(it) }, onDismiss = { hojaTemporizador = false })
    }
}

/** Flecha atrás y título (Figma «Barra superior» de SCR-70 a 74). */
@Composable
internal fun BarraConVolver(titulo: String, onBack: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().height(64.dp).padding(start = RolaSpacing.xs, end = RolaSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onBack) { Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.volver), tint = c.onSurface) }
        Text(titulo, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge, color = c.onSurface)
    }
}

@Composable
private fun Seccion(texto: Int) = Text(
    stringResource(texto),
    Modifier.padding(start = 28.dp, end = RolaSpacing.lg, top = 18.dp, bottom = RolaSpacing.xs).semantics { heading() },
    style = MaterialTheme.typography.timeSmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
private fun Tarjeta(content: @Composable () -> Unit) = Column(
    Modifier.padding(horizontal = RolaSpacing.md).fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainer),
) { content() }

@Composable
private fun Divisor() = HorizontalDivider(Modifier.padding(start = 72.dp), 1.dp, MaterialTheme.colorScheme.outline)

/** Fila de ajuste: ícono en círculo, título, subtítulo y a la derecha un valor, progreso o chevron (si se puede tocar). */
@Composable
private fun Fila(
    icono: Int?,
    titulo: Int,
    subtitulo: String? = null,
    valor: String? = null,
    ocupado: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val c = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(enabled = !ocupado, role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = RolaSpacing.md, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icono != null) {
            Box(Modifier.size(40.dp).background(c.surface, CircleShape), Alignment.Center) {
                Icon(painterResource(icono), null, Modifier.size(22.dp), tint = c.onSurface)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(titulo), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = c.onSurface)
            if (subtitulo != null) Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
        }
        when {
            ocupado -> CircularProgressIndicator(Modifier.size(24.dp), color = c.onSurfaceVariant, strokeWidth = 2.dp)
            valor != null -> Text(valor, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = c.onSurfaceVariant)
            onClick != null -> Icon(painterResource(R.drawable.ic_chevron_forward), null, tint = c.onSurfaceVariant)
        }
    }
}

/** «Apagado», el tiempo que falta (cada segundo) o «Al terminar la canción» (FR-021). */
@Composable
private fun timerText(timer: SleepTimerOption, endsAtMs: Long?): String {
    val faltan by produceState(endsAtMs?.let { it - SystemClock.elapsedRealtime() }, endsAtMs) {
        while (endsAtMs != null) {
            value = (endsAtMs - SystemClock.elapsedRealtime()).coerceAtLeast(0)
            delay(1_000)
        }
    }
    return when {
        timer == SleepTimerOption.END_OF_SONG -> stringResource(R.string.temporizador_cancion)
        faltan != null -> formatDuration(faltan!!)
        else -> stringResource(R.string.temporizador_apagado)
    }
}

/** «Último escaneo: hace …» (se refresca cada minuto) o «Aún no has escaneado» (FR-043, FR-044). */
@Composable
private fun lastScanText(lastScanAt: Long?): String {
    if (lastScanAt == null) return stringResource(R.string.sin_escaneo)
    val ahora by produceState(System.currentTimeMillis(), lastScanAt) {
        while (true) {
            value = System.currentTimeMillis()
            delay(60_000)
        }
    }
    val hace = when (val t = timeAgo(ahora, lastScanAt)) {
        TimeAgo.Moment -> stringResource(R.string.hace_un_momento)
        is TimeAgo.Minutes -> pluralStringResource(R.plurals.hace_minutos, t.n, t.n)
        is TimeAgo.Hours -> pluralStringResource(R.plurals.hace_horas, t.n, t.n)
        is TimeAgo.Days -> pluralStringResource(R.plurals.hace_dias, t.n, t.n)
    }
    return stringResource(R.string.ultimo_escaneo, hace)
}

private fun ThemeMode.label() = when (this) {
    ThemeMode.SYSTEM -> R.string.tema_sistema
    ThemeMode.DARK -> R.string.tema_oscuro
    ThemeMode.LIGHT -> R.string.tema_claro
}

/** Hoja del tema con el estilo de la de ordenar (Figma 33:1070): elegir aplica y cierra (FR-011). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSheet(theme: ThemeMode, onChoose: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    val c = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismiss,
        Modifier.testTag("hoja_tema"),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
    ) {
        Column(Modifier.navigationBarsPadding().padding(bottom = RolaSpacing.xl)) {
            Text(
                stringResource(R.string.tema),
                Modifier.padding(start = RolaSpacing.xl, end = RolaSpacing.xl, bottom = RolaSpacing.xs),
                style = MaterialTheme.typography.titleLarge,
                color = c.onSurface,
            )
            Column(Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { modo ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(theme == modo, role = Role.RadioButton) { onChoose(modo) }
                            .padding(horizontal = RolaSpacing.xl, vertical = RolaSpacing.xxs),
                        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(theme == modo, null, colors = RadioButtonDefaults.colors(selectedColor = RolaTheme.colors.accent))
                        Text(
                            stringResource(modo.label()),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = c.onSurface,
                        )
                    }
                }
            }
        }
    }
}
