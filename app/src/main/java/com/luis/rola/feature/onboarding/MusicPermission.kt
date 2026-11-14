package com.luis.rola.feature.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

/** Permiso de lectura de audio según la versión (DEC-18). */
val musicPermission: String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

private fun Context.hasMusicPermission() =
    ContextCompat.checkSelfPermission(this, musicPermission) == PackageManager.PERMISSION_GRANTED

/**
 * Sin permiso de música: Permisos (SCR-05) o, si se negó, Permiso denegado (SCR-06).
 * Con permiso: [content]. Se revisa en cada ON_RESUME para detectar el permiso dado en Ajustes.
 */
@Composable
fun MusicPermissionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var granted by rememberSaveable { mutableStateOf(context.hasMusicPermission()) }
    var denied by rememberSaveable { mutableStateOf(false) }
    LifecycleResumeEffect(Unit) {
        granted = context.hasMusicPermission()
        onPauseOrDispose { }
    }
    val launcher = rememberLauncherForActivityResult(RequestPermission()) { ok ->
        granted = ok
        denied = !ok
    }
    when {
        granted -> content()
        denied -> PermissionDeniedScreen(
            onOpenSettings = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                )
            },
            onRetry = { launcher.launch(musicPermission) },
        )
        else -> PermissionScreen(onAllow = { launcher.launch(musicPermission) })
    }
}
