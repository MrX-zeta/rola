# Tarea 02c · Barra de navegación fluida

**Estado:** esperando revisión
**Escrita:** 2026-11-14 · sesión de diseño
**Rama:** `development`. En worktree: actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.4.1 · Sin Spec Kit (es un arreglo).

## Problema (lo vio Brian en su Redmi)
Al tocar otra pestaña hay un pequeño retraso y la transición no se siente 100 % fluida, sobre todo por el texto.

## Causas que encontró la sesión de diseño en `RolaNavBar.kt` / `RolaNavHost.kt`
1. **Retraso:** la pestaña activa sale de `currentBackStackEntryAsState()`, que cambia hasta que la navegación se confirma. La barra reacciona tarde.
2. **El fondo persigue un blanco que se mueve:** el destino del fondo lima se mide con `onGloballyPositioned` (llega un cuadro tarde) mientras la etiqueta crece con `expandHorizontally` (250 ms); cada cuadro relanza el `LaunchedEffect` con otro destino.
3. **Toda la barra se mueve de lado:** su ancho depende del texto activo («Inicio», «Biblioteca», «Buscar» miden distinto) y está centrada, así que se recentra mientras anima.
4. Las animaciones se probaron en compilación de depuración, que en Compose va más lenta.

## Paso 0 · Comprobación
- `git log --oneline -3` incluye `docs: sesión de diseño tarea 02c`. Si no, detente.

## Paso 1 · Rehacer la animación de la barra (`animate`, DEC-28 `detalles.barra_navegacion`)
- **Selección inmediata:** estado local de la pestaña elegida que cambia en el mismo toque (antes de navegar) y se sincroniza con la navegación (atrás del sistema, etc.).
- **Ancho fijo de la barra:** el que necesita la pestaña con el texto más largo; ya no se recentra.
- **Un solo valor por pestaña:** `progreso` 0→1 animado con el resorte de `RolaMotion`. Con un `Layout` propio, en el mismo cuadro: ancho de cada pestaña = lerp(ancho solo ícono, ancho con texto, progreso); opacidad de la etiqueta = progreso (sin `AnimatedVisibility` ni `expandHorizontally`); el fondo lima se dibuja con esos mismos valores (sin `onGloballyPositioned`).
- La etiqueta no cambia de tamaño: solo se recorta por el ancho de su pestaña y aparece con su opacidad.
- Se conserva lo demás de DEC-28: rebote corto del ícono, color animado, ≤ 300 ms percibidos, interrumpible si se toca otra pestaña a mitad, y con reducir movimiento todo salta.
- Pruebas de Compose: la selección cambia en el primer cuadro tras el toque; con reducir movimiento la barra queda en su estado final de inmediato; el ancho total de la barra no cambia entre pestañas.
- Commit: `fix(motion): barra de navegación fluida`.

## Paso 2 · Fundido entre pestañas
- Revisa que el fundido de contenido (150 ms entrada / 100 ms salida) no retrase la respuesta visible de la barra; si lo hace, que empiecen a la vez.

## Paso 3 · Probar en compilación no depurable
- Agrega un tipo de compilación `perfil` (`initWith(release)`, firmado con la llave de **depuración**, `profileable`, sin depurar). No leas ni uses `keystore.properties` ni llaves de release.
- Graba `t02c-barra.mp4` en el emulador con el tipo `perfil`. Si Brian conecta el Redmi por USB, instálale ese tipo y avísale.
- Commit: `build: tipo perfil para probar fluidez`.

## Paso 4 · Comprobaciones y reporte
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y las de Compose. Resultados reales.
- `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`.
- Reporte con antes/después y estado «esperando revisión». No empieces la tarea 03.
