package com.luis.rola.feature.playlists

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.luis.rola.R
import com.luis.rola.core.data.PlaylistName
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.pill

/** Crear lista (SCR-30, Figma 34:1168): solo el nombre; «Crear» se apaga si queda vacío. */
@Composable
fun CreatePlaylistDialog(onCreate: (String) -> Unit, onDismiss: () -> Unit) {
    var nombre by rememberSaveable { mutableStateOf("") }
    val limpio = PlaylistName.clean(nombre)
    val foco = remember { FocusRequester() }
    LaunchedEffect(Unit) { foco.requestFocus() }
    val boton = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    AlertDialog(
        onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(stringResource(R.string.nueva_lista), style = MaterialTheme.typography.headlineSmall) },
        text = {
            OutlinedTextField(
                nombre,
                { if (it.length <= PlaylistName.MAX) nombre = it },
                Modifier.focusRequester(foco),
                label = { Text(stringResource(R.string.nombre)) },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { limpio?.let(onCreate) }),
            )
        },
        confirmButton = {
            Button(
                { limpio?.let(onCreate) },
                enabled = limpio != null,
                shape = MaterialTheme.shapes.pill,
                colors = ButtonDefaults.buttonColors(containerColor = RolaTheme.colors.accent, contentColor = RolaTheme.colors.onAccent),
            ) { Text(stringResource(R.string.crear), style = boton) }
        },
        dismissButton = {
            TextButton(onDismiss) { Text(stringResource(R.string.cancelar), style = boton, color = MaterialTheme.colorScheme.onSurface) }
        },
    )
}
