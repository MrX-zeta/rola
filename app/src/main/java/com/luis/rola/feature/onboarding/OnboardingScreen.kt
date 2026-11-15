package com.luis.rola.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaMotion
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val PAGINAS = 3

/** Bienvenida antes de todo mientras no se haya completado (DEC-18). `null`: todavía se lee y el splash sigue. */
@Composable
fun OnboardingGate(completed: Boolean?, onComplete: () -> Unit, content: @Composable () -> Unit) {
    when (completed) {
        null -> Unit
        false -> OnboardingScreen(onComplete)
        true -> content()
    }
}

/** Bienvenida (SCR-02, SCR-03, SCR-04): tres páginas; «Empezar» y «Omitir» llaman a [onFinish]. */
@Composable
fun OnboardingScreen(onFinish: () -> Unit, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val reduce = LocalReduceMotion.current
    val pager = rememberPagerState { PAGINAS }
    val scope = rememberCoroutineScope()
    val ultima = pager.currentPage == PAGINAS - 1
    val ir = { pagina: Int ->
        scope.launch { pager.animateScrollToPage(pagina, animationSpec = if (reduce) tween(RolaMotion.MEDIA) else RolaMotion.resorte()) }
    }
    BackHandler(pager.currentPage > 0) { ir(pager.currentPage - 1) }

    // La primera ilustración entra al primer cuadro ya dibujado, no al abrir (DEC-28 `arranque_en_primer_cuadro`).
    val entrada = remember { Animatable(if (reduce) 1f else 0f) }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        entrada.animateTo(1f, tween(RolaMotion.LENTA, easing = RolaMotion.EaseOut))
    }

    Column(modifier.fillMaxSize().background(c.background).systemBarsPadding().testTag("bienvenida")) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            RolaLogo()
            // En la página 3 no hay «Omitir» (Figma 22:383); el logo no se mueve porque está a la izquierda.
            AnimatedVisibility(!ultima, enter = fadeIn(tween(RolaMotion.MEDIA)), exit = fadeOut(tween(RolaMotion.RAPIDA))) {
                TextButton(onFinish, Modifier.padding(end = RolaSpacing.xxs)) {
                    Text(stringResource(R.string.omitir), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = c.onSurfaceVariant)
                }
            }
        }
        HorizontalPager(pager, Modifier.weight(1f)) { pagina ->
            Pagina(pagina, pager, reduce) { entrada.value }
        }
        Column(
            Modifier.padding(start = RolaSpacing.xl, end = RolaSpacing.xl, top = RolaSpacing.xxl, bottom = RolaSpacing.xxl + RolaSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Indicador(pager)
            BotonPrincipal(if (ultima) R.string.empezar else R.string.siguiente, { if (ultima) onFinish() else ir(pager.currentPage + 1) })
        }
    }
}

@Composable
private fun Pagina(pagina: Int, pager: PagerState, reduce: Boolean, entrada: () -> Float) {
    val (titulo, cuerpo) = listOf(
        R.string.bienvenida_1_titulo to R.string.bienvenida_1_cuerpo,
        R.string.bienvenida_2_titulo to R.string.bienvenida_2_cuerpo,
        R.string.bienvenida_3_titulo to R.string.bienvenida_3_cuerpo,
    )[pagina]
    // Cuánto se aleja esta página del centro: 0 = en su lugar, ±1 = una página de lado.
    val desplazamiento = { (pager.currentPage - pagina) + pager.currentPageOffsetFraction }
    Column(
        Modifier.fillMaxSize().graphicsLayer {
            // Reducir movimiento: la página no se desliza; contrarresta al pager y solo se funde (FR-015).
            if (reduce) {
                val d = desplazamiento()
                translationX = d * size.width
                alpha = 1f - abs(d).coerceAtMost(1f)
            }
        },
    ) {
        Box(
            Modifier.weight(1f).fillMaxWidth().testTag("ilustracion-$pagina").graphicsLayer {
                if (!reduce) {
                    val d = abs(desplazamiento()).coerceAtMost(1f)
                    val e = if (pagina == 0) entrada() else 1f
                    val escala = (1f - .06f * d) * (.96f + .04f * e)
                    scaleX = escala
                    scaleY = escala
                    alpha = (1f - .4f * d) * e
                }
            },
            Alignment.Center,
        ) {
            when (pagina) {
                0 -> PortadasEnAbanico(Modifier.fillMaxSize())
                1 -> AjustarAlto(Modifier.fillMaxSize()) { TarjetaDeLetra() }
                else -> AjustarAlto(Modifier.fillMaxSize()) { SinConexion() }
            }
        }
        Column(Modifier.padding(horizontal = RolaSpacing.xl), verticalArrangement = Arrangement.spacedBy(RolaSpacing.sm)) {
            Text(stringResource(titulo), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(cuerpo), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Indicador de página (Figma 22:317): la marca activa se estira hacia la página nueva y luego se recoge.
 * Lee la posición del pager al dibujar: sigue al dedo y se interrumpe solo, sin animación propia.
 */
@Composable
private fun Indicador(pager: PagerState) {
    val activo = MaterialTheme.colorScheme.primary
    val inactivo = MaterialTheme.colorScheme.onSurfaceVariant
    val estado = stringResource(R.string.pagina_de, pager.currentPage + 1, PAGINAS)
    Canvas(Modifier.size(56.dp, 8.dp).testTag("indicador").semantics { stateDescription = estado }) {
        val paso = 16.dp.toPx() // punto de 8 dp + separación de 8 dp
        val punto = 8.dp.toPx()
        val radio = CornerRadius(punto / 2)
        val pos = (pager.currentPage + pager.currentPageOffsetFraction).coerceIn(0f, PAGINAS - 1f)
        // Los puntos a la derecha de la marca se corren un paso para dejarle sitio.
        repeat(PAGINAS) { k ->
            drawRoundRect(inactivo, Offset(k * paso + paso * (k - pos).coerceIn(0f, 1f), 0f), Size(punto, punto), radio)
        }
        val i = pos.toInt().coerceAtMost(PAGINAS - 2)
        val f = pos - i
        val inicio = i * paso + paso * (2 * f - 1).coerceIn(0f, 1f)
        val fin = i * paso + 24.dp.toPx() + paso * (2 * f).coerceAtMost(1f)
        drawRoundRect(activo, Offset(inicio, 0f), Size(fin - inicio, punto), radio)
    }
}

@Preview(name = "Bienvenida · oscuro")
@Composable
private fun BienvenidaOscuro() = RolaTheme { OnboardingScreen({}) }

@Preview(name = "Bienvenida · claro")
@Composable
private fun BienvenidaClaro() = RolaTheme(darkTheme = false) { OnboardingScreen({}) }
