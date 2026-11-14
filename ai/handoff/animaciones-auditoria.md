# Auditoría de movimiento · Rola (tarea 02b, Paso 4)

- **Commit auditado**: `6ff469e`
- **Skill**: `improve-animations` (esfuerzo `standard`, sin subagentes: el repositorio es pequeño).
- **Reglas del proyecto**: DEC-28 (≤ 300 ms, salidas más cortas que entradas, resortes interrumpibles) e INV-06 (con «reducir movimiento» solo instantáneo o fundido corto).
- Los valores de la skill son CSS; aquí van traducidos a Compose (`CubicBezierEasing`, `spring`, `tween`).

## Reconocimiento

- **Stack**: Kotlin 2.4.21, Jetpack Compose (BOM 2026.09.00), Material 3, navigation-compose 2.10.2. Sin librerías de movimiento externas.
- **Dónde vive el movimiento hoy**:
  - `navigation/RolaNavHost.kt`: `enterTransition = { EnterTransition.None }` y `exitTransition = { ExitTransition.None }`. Están apagadas a propósito en la tarea 02, para no meter movimiento sin pasar por `animate`.
  - Ripple de Material en filas, `IconButton`, `Button` y `TextButton`.
  - `Snackbar` de M3 (animación propia del componente).
  - `CircularProgressIndicator` mientras carga Biblioteca (lo reemplaza el esqueleto del Paso 3).
- **Tokens de movimiento**: no existen. No hay `Motion.kt` en `core/designsystem` ni manejo de «reducir movimiento».
- **Personalidad**: app de música plana y minimalista (DEC-27) → movimiento sobrio y rápido; rebote solo sutil.
- **Frecuencia**:
  - Decenas de veces al día: cambiar de pestaña, reproducir/pausa, siguiente, tocar una fila.
  - Ocasional: la mini tarjeta entra o sale (primera canción, cola vacía), snackbar de error.
  - Raro: Permisos, Permiso denegado.

## Hallazgos (verificados en el código)

| # | Severidad | Categoría | Lugar | Hallazgo | Arreglo |
|---|---|---|---|---|---|
| 1 | HIGH | Oportunidad perdida / estado | `navigation/RolaNavBar.kt:47-71` | El fondo lima de la pestaña activa salta de golpe a la nueva y la etiqueta aparece o desaparece cambiando el ancho de la píldora de un cuadro a otro. Es la interacción más frecuente de la app. | Indicador único que se desliza y cambia de ancho con resorte interrumpible; la etiqueta entra con fundido y ancho animado. |
| 2 | HIGH | Accesibilidad / cohesión | (no existe) `core/designsystem` | No hay tokens de curva, duración ni resorte, ni forma de saber si el sistema pide reducir movimiento. Cada animación nueva inventaría valores y nadie apagaría el desplazamiento. | `Motion.kt` con curvas, duraciones, resorte y `LocalReduceMotion` leído de `ANIMATOR_DURATION_SCALE`. |
| 3 | MEDIUM | Oportunidad perdida | `feature/player/MiniPlayer.kt:39` | `val song = state.current ?: return`: la tarjeta aparece y desaparece de golpe y la lista salta porque `abajoPx` cambia en un cuadro. | `AnimatedVisibility` con entrada desde abajo + fundido y salida más corta. |
| 4 | MEDIUM | Estado | `feature/player/MiniPlayer.kt:74-80` | El ícono reproducir/pausa cambia de golpe. | `Crossfade`/`AnimatedContent` corto con escala 0.8 → 1. |
| 5 | MEDIUM | Oportunidad perdida | `navigation/RolaNavHost.kt:63-64` | El contenido entre pestañas cambia de golpe (transiciones `None`). | Fundido corto: 150 ms al entrar y 100 ms al salir. |
| 6 | MEDIUM | Oportunidad perdida | `navigation/RolaNavBar.kt:60-64` | Los íconos de la barra no reaccionan al seleccionarse. | Color animado y un rebote de escala sutil (1 → 1.15 → 1) solo en el que se activa. |
| 7 | LOW | Estado | `feature/player/MiniPlayer.kt:83-86` | La línea de progreso avanza a saltos cada 500 ms (con canciones cortas se nota). | `animateFloatAsState` lineal de 500 ms entre actualizaciones. Con canciones de 3 min el salto es de ~1 px: se puede dejar. |

**Revisado y correcto (no se reporta)**: ripples de Material (feedback de toque estándar y breve), Snackbar de M3, sin `scale(0)` en ninguna parte, sin animación de propiedades de layout.

### Oportunidades para después (fuera de las pantallas o de prioridad baja)
- Paso de Permisos a la app: hoy cambia de golpe; es raro (primera vez) y puede llevar un fundido de 200 ms. Llega con el onboarding.
- Permiso denegado aparece de golpe al cerrar el diálogo del sistema: fundido de 150 ms. Llega con el onboarding.
- Reproduciendo (tarea 03): la mini tarjeta debe crecer hacia la pantalla completa (elemento compartido); conviene diseñarlo junto con los gestos de DEC-04.
- Esqueletos de portadas que pasan a la imagen: fundido de 150 ms de la portada al cargar (se hace en el Paso 3 si es barato).

## Orden recomendado

| Plan | Título | Severidad | Depende de | Estado |
|---|---|---|---|---|
| 001 | Tokens de movimiento y «reducir movimiento» | HIGH | — | TODO |
| 002 | Píldora activa que se desliza | HIGH | 001 | TODO |
| 003 | Íconos de la barra que reaccionan | MEDIUM | 001, 002 | TODO |
| 004 | Fundido entre pestañas | MEDIUM | 001 | TODO |
| 005 | Mini tarjeta: entrada/salida y reproducir/pausa | MEDIUM | 001 | TODO |
| 006 | Línea de progreso continua | LOW | 001 | Para después (opcional) |

---

# 001 — Tokens de movimiento y «reducir movimiento»

- **Status**: TODO · **Commit**: `6ff469e` · **Severity**: HIGH · **Category**: Accesibilidad / cohesión · **Scope**: 1 archivo nuevo + 1 prueba

## Problem
No hay tokens de movimiento en `app/src/main/java/com/luis/rola/core/designsystem/` ni lectura de «reducir movimiento». Android expresa «Quitar animaciones» con `Settings.Global.ANIMATOR_DURATION_SCALE = 0`. Compose ya escala sus animaciones con ese valor, pero el proyecto necesita poder **cambiar desplazamiento/escala por fundido o nada** (INV-06) y probarlo.

## Target
Archivo `core/designsystem/Motion.kt`:
```kotlin
object RolaMotion {
    val EaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)      // entradas y salidas
    val EaseInOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)   // movimiento en pantalla
    const val Rapida = 100   // salidas, cambios de ícono
    const val Media = 150    // fundidos de entrada
    const val Lenta = 250    // entradas con desplazamiento
    fun <T> resorte() = spring<T>(dampingRatio = 0.8f, stiffness = 400f) // ≈ 0.3 s, rebote sutil
}
val LocalReduceMotion = staticCompositionLocalOf { false }
fun reduceMotion(animatorScale: Float) = animatorScale == 0f
```
`RolaTheme` provee `LocalReduceMotion` leyendo `Settings.Global.getFloat(resolver, ANIMATOR_DURATION_SCALE, 1f)`.

## Repo conventions to follow
- Tokens como objetos en `core/designsystem` (`RolaSpacing` en `Spacing.kt`, `RolaColors` + `LocalRolaColors` en `Color.kt`). Imitar `LocalRolaColors`.

## Steps
1. Crear `Motion.kt` con el contenido de Target.
2. En `Theme.kt`, dentro de `RolaTheme`, proveer `LocalReduceMotion provides reduceMotion(scale)` con `scale` leído una vez por composición del contexto.
3. Prueba JUnit `MotionTest`: `reduceMotion(0f) == true`, `reduceMotion(1f) == false`, `reduceMotion(0.5f) == false`.

## Boundaries
- No tocar pantallas en este plan. Sin dependencias nuevas.

## Verification
- `.\gradlew :app:testDebugUnitTest` en verde.
- **Done when**: `RolaMotion` y `LocalReduceMotion` existen y la prueba pasa.

---

# 002 — Píldora activa que se desliza

- **Status**: TODO · **Commit**: `6ff469e` · **Severity**: HIGH · **Category**: Estado / oportunidad perdida · **Scope**: `navigation/RolaNavBar.kt`

## Problem
`navigation/RolaNavBar.kt:47-71`, actual:
```kotlin
Row(Modifier.clip(forma).background(if (activo) RolaTheme.colors.accent else Color.Transparent)
    .selectable(activo, role = Role.Tab) { onSelect(d) } ...) {
    Icon(...)
    if (activo) Text(...)
}
```
El fondo cambia de pestaña en un cuadro y el ancho de la píldora salta cuando aparece la etiqueta.

## Target
- Un solo indicador lima detrás de los destinos, dibujado en la `Row` padre, con `x` y `ancho` animados hacia los del destino activo con `RolaMotion.resorte()` (interrumpible: si se toca otra pestaña a mitad, se reorienta desde donde está).
- La etiqueta: `AnimatedVisibility(activo, enter = fadeIn(tween(Media, easing = EaseOut)) + expandHorizontally(tween(Lenta, easing = EaseOut)), exit = fadeOut(tween(Rapida)) + shrinkHorizontally(tween(Lenta - 50, easing = EaseOut)))`.
- Con `LocalReduceMotion`: el indicador salta (`snap()`) y la etiqueta solo hace fundido (sin expand/shrink).

## Steps
1. Medir la posición y el ancho de cada destino con `onGloballyPositioned` (relativos a la `Row`) y guardar en un `mutableStateMapOf<Destino, Pair<Float, Float>>`.
2. `val x by animateFloatAsState(objetivo.x, spec)` y `val w by animateFloatAsState(objetivo.w, spec)`, con `spec = if (reduce) snap() else RolaMotion.resorte()`.
3. Dibujar el indicador con `Modifier.drawBehind { drawRoundRect(accent, topLeft = Offset(x, pad), size = Size(w, alto), cornerRadius = CornerRadius(alto/2)) }` en la `Row` (dibujo, no layout por cuadro).
4. Quitar el `background` por destino; dejar `clip` + `selectable` para el ripple.

## Boundaries
- Mismos colores, tamaños, `Role.Tab`, descripciones y objetivos ≥ 48 dp. Sin dependencias nuevas.

## Verification
- **Feel check**: tocar Biblioteca → Buscar → Inicio rápido; el indicador nunca se reinicia desde cero y no hay doble lima. Con «Escala de animación» 10× en Opciones de desarrollador, la etiqueta crece sin saltos. Con «Quitar animaciones» el cambio es instantáneo.
- **Done when**: el video `t02b-pildora.mp4` muestra el deslizamiento.

---

# 003 — Íconos de la barra que reaccionan

- **Status**: TODO · **Commit**: `6ff469e` · **Severity**: MEDIUM · **Category**: Oportunidad perdida · **Scope**: `navigation/RolaNavBar.kt`

## Problem
`navigation/RolaNavBar.kt:60-64`: el tinte del ícono cambia en un cuadro (`if (activo) onAccent else onSurfaceVariant`) y no hay reacción al seleccionar.

## Target
- `val tinte by animateColorAsState(objetivo, tween(RolaMotion.Media, easing = RolaMotion.EaseOut))`.
- Al **activarse** (no al desactivarse): escala `1f → 1.15f → 1f` con `Animatable`, la subida en `tween(Rapida, easing = EaseOut)` y la vuelta con `RolaMotion.resorte()`. Total ≈ 250 ms.
- Con `LocalReduceMotion`: sin escala; el color cambia en `snap()`.

## Steps
1. `val escala = remember { Animatable(1f) }` y `LaunchedEffect(activo) { if (activo && !reduce) { escala.animateTo(1.15f, tween(100, easing = EaseOut)); escala.animateTo(1f, resorte()) } }`.
2. `Icon(..., modifier = Modifier.graphicsLayer { scaleX = escala.value; scaleY = escala.value })`.

## Boundaries
- No mover el ícono de sitio ni cambiar su tamaño de layout (`graphicsLayer` solamente).

## Verification
- **Feel check**: el rebote es apenas perceptible, no llamativo; tocar la pestaña ya activa no hace nada.

---

# 004 — Fundido entre pestañas

- **Status**: TODO · **Commit**: `6ff469e` · **Severity**: MEDIUM · **Category**: Oportunidad perdida · **Scope**: `navigation/RolaNavHost.kt`

## Problem
`navigation/RolaNavHost.kt:63-64`, actual: `enterTransition = { EnterTransition.None }, exitTransition = { ExitTransition.None }`.

## Target
`enterTransition = { fadeIn(tween(RolaMotion.Media, easing = RolaMotion.EaseOut)) }`, `exitTransition = { fadeOut(tween(RolaMotion.Rapida)) }`. Lo mismo para `popEnter`/`popExit`. Sin desplazamiento (las pestañas son hermanas, no hay dirección espacial). Con reducir movimiento se deja igual: un fundido corto está permitido por INV-06.

## Boundaries
- Sin `slideIn`/`slideOut`. No tocar la píldora ni la mini tarjeta en este plan.

## Verification
- **Feel check**: al cambiar de pestaña no se ven dos pantallas superpuestas más de un instante; la música no se corta.

---

# 005 — Mini tarjeta: entrada/salida y reproducir/pausa

- **Status**: TODO · **Commit**: `6ff469e` · **Severity**: MEDIUM · **Category**: Oportunidad perdida / estado · **Scope**: `feature/player/MiniPlayer.kt`

## Problem
`feature/player/MiniPlayer.kt:39` (`val song = state.current ?: return`): la tarjeta aparece de golpe y la lista salta. `MiniPlayer.kt:74-80`: el ícono reproducir/pausa cambia en un cuadro.

## Target
- Envolver la tarjeta en `AnimatedVisibility(state.current != null)`:
  - entrada: `slideInVertically(tween(RolaMotion.Lenta, easing = EaseOut)) { it } + fadeIn(tween(Media))`;
  - salida: `slideOutVertically(tween(200, easing = EaseOut)) { it } + fadeOut(tween(Rapida))`.
  - Con `LocalReduceMotion`: solo `fadeIn(tween(Media))` / `fadeOut(tween(Rapida))`.
- Recordar la última canción para que la salida se dibuje con datos (no con `null`).
- Ícono: `AnimatedContent(state.isPlaying, transitionSpec = { (fadeIn(tween(Media)) + scaleIn(tween(Media, easing = EaseOut), initialScale = 0.8f)) togetherWith fadeOut(tween(Rapida)) })`. Con reducir movimiento: sin `scaleIn`.

## Boundaries
- Tocar la tarjeta sigue sin hacer nada (tarea 03). Mismos colores y descripciones.

## Verification
- **Prueba Compose**: con `LocalReduceMotion provides true`, la tarjeta aparece sin desplazamiento (nodo visible y en su posición final tras el primer cuadro con el reloj pausado).
- **Feel check**: la tarjeta sube desde detrás de la píldora; al vaciar la cola baja más rápido de lo que subió.

---

# 006 — Línea de progreso continua (opcional)

- **Status**: Para después · **Severity**: LOW
- `feature/player/MiniPlayer.kt:83-86`: animar la fracción con `animateFloatAsState(f, tween(500, easing = LinearEasing))`. Movimiento constante → lineal. Con reducir movimiento: `snap()`. Se deja para después porque con canciones reales el salto es de ~1 px.
