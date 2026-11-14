package com.luis.rola.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.pill

enum class Destino(val ruta: String, val icono: Int, val texto: Int) {
    Inicio("inicio", R.drawable.ic_home, R.string.nav_inicio),
    Biblioteca("biblioteca", R.drawable.ic_library, R.string.nav_biblioteca),
    Buscar("buscar", R.drawable.ic_search, R.string.nav_buscar),
}

/** Píldora de tres destinos (DEC-03): el activo con ícono y texto, los demás solo ícono. */
@Composable
fun RolaNavBar(actual: String?, onSelect: (Destino) -> Unit, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val forma = MaterialTheme.shapes.pill
    Row(
        modifier.background(c.surface, forma).border(1.dp, c.outline, forma).padding(RolaSpacing.xxs).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.xxs),
    ) {
        Destino.entries.forEach { d ->
            val activo = d.ruta == actual
            Row(
                Modifier
                    .clip(forma)
                    .background(if (activo) RolaTheme.colors.accent else Color.Transparent)
                    .selectable(activo, role = Role.Tab) { onSelect(d) }
                    .padding(horizontal = RolaSpacing.md, vertical = RolaSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(RolaSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(d.icono),
                    contentDescription = if (activo) null else stringResource(d.texto),
                    tint = if (activo) RolaTheme.colors.onAccent else c.onSurfaceVariant,
                )
                if (activo) {
                    Text(
                        stringResource(d.texto),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = RolaTheme.colors.onAccent,
                    )
                }
            }
        }
    }
}
