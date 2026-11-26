package com.luis.rola.core.designsystem

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext

/**
 * Tema de Rola: oscuro o claro según Ajustes (DEC-16). Sin color dinámico del fondo de pantalla.
 * Al cambiar, el cuadro anterior se desvanece encima del nuevo (≤ 300 ms, solo opacidad, DEC-28).
 */
@Composable
fun RolaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val resolver = LocalContext.current.contentResolver
    val escala = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    val reduce = reduceMotion(escala)
    // La raíz se graba en una capa para poder fotografiar el último cuadro antes de cambiar los colores.
    val capa = rememberGraphicsLayer()
    var mostrado by remember { mutableStateOf(darkTheme) }
    var foto by remember { mutableStateOf<ImageBitmap?>(null) }
    val alfa = remember { Animatable(0f) }
    LaunchedEffect(darkTheme) {
        if (darkTheme == mostrado) return@LaunchedEffect
        foto = runCatching { capa.toImageBitmap() }.getOrNull() // sin cuadro previo: cambio directo
        mostrado = darkTheme
        alfa.snapTo(1f)
        alfa.animateTo(0f, tween(if (reduce) RolaMotion.MEDIA else RolaMotion.LENTA, easing = RolaMotion.EaseOut))
        foto = null
    }
    CompositionLocalProvider(
        LocalRolaColors provides RolaColors(),
        LocalReduceMotion provides reduce,
    ) {
        MaterialTheme(
            colorScheme = if (mostrado) RolaDarkColors else RolaLightColors,
            typography = RolaTypography,
            shapes = RolaShapes,
        ) {
            Box(
                Modifier.drawWithContent {
                    // La foto va dentro de lo grabado: si se cambia otra vez a medio fundido, se sigue sin salto.
                    capa.record {
                        this@drawWithContent.drawContent()
                        foto?.let { drawImage(it, alpha = alfa.value) }
                    }
                    drawLayer(capa)
                },
            ) { content() }
        }
    }
}

object RolaTheme {
    val colors: RolaColors
        @Composable @ReadOnlyComposable get() = LocalRolaColors.current
}
