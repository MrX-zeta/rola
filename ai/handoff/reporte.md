# Reporte · Tarea 02c · Barra de navegación fluida

**Estado:** esperando revisión.

## Paso 0 · Comprobación
- El worktree estaba en `main` viejo (b0b8691). Lo adelanté a `origin/development` con fast-forward; no tenía commits propios.
- `git log` incluye `d366305 docs: sesión de diseño tarea 02c`.
- El mensaje inicial de Brian hablaba de parar en el Paso 5 por Spec Kit (Tarea 02). Brian confirmó que la tarea a hacer es la **02c completa**. Esta tarea no usa Spec Kit.

## Paso 1 · Barra rehecha (`RolaNavBar.kt`, skill `animate`, DEC-28 `barra_navegacion`)
**Antes:**
- La pestaña activa salía de la ruta confirmada, así que la barra respondía un cuadro tarde.
- El fondo lima perseguía medidas atrasadas (`onGloballyPositioned`).
- La etiqueta crecía con `expandHorizontally`.
- El ancho de la barra cambiaba según el texto y se recentraba.

**Después:**
- **Selección inmediata:** el estado local `elegido` cambia en el mismo toque, antes de `navigate`. Usa `remember(actual)`, así que se resincroniza cuando cambia la ruta (atrás del sistema, etc.).
- **Ancho fijo:** el total sale de los íconos más el texto más largo. Con etiquetas cortas, el espacio que sobra se reparte entre los huecos. La barra ya no se recentra.
- **Un solo progreso 0→1 por pestaña** (`animateFloatAsState` con `RolaMotion.resorte()`, interrumpible). Un `Layout` propio lo lee en la medida y en el dibujo:
  - ancho de la pestaña = ícono + extra × progreso;
  - opacidad de la etiqueta = progreso, sin `AnimatedVisibility`;
  - color del ícono = `lerp` por el mismo progreso;
  - fondo lima = promedio de las pestañas ponderado por su progreso, dibujado en `drawBehind`.
- **La etiqueta no cambia de tamaño** (`wrapContentWidth(unbounded)`): la pestaña la recorta.
- **Se conserva:**
  - el rebote corto del ícono;
  - el máximo de 300 ms;
  - con reducir movimiento, el progreso lee la selección directamente y todo salta.
- **Accesibilidad:** el ícono ya no tiene descripción. La etiqueta, aunque transparente, nombra cada pestaña (`Role.Tab`).
- **Geometría pura** en `GeometriaBarra`, con prueba unitaria.

**Pruebas:**
- `GeometriaBarraTest` (unitaria, 3): el ancho total es fijo en cualquier pestaña y a mitad de animación, el ancho sigue al progreso y los huecos nunca quedan por debajo del mínimo.
- `RolaNavBarTest` (Compose, 3):
  - la selección cambia en el primer cuadro tras el toque, con `onSelect` sin navegar;
  - con reducir movimiento, la pestaña tiene su ancho final en ese mismo cuadro y no cambia después;
  - el ancho de la barra no cambia entre pestañas.

## Paso 2 · Fundido entre pestañas
No hizo falta cambiarlo:
- El `NavHost` ya arranca el fundido de entrada (150 ms) y el de salida (100 ms) juntos, en cuanto se llama a `navigate`.
- La barra ya no depende de la navegación, así que el fundido no la retrasa.

## Paso 3 · Tipo `perfil`
- `create("perfil")`: `initWith(release)`, firmado con la llave de **depuración**, `isProfileable = true`, `isDebuggable = false` y `matchingFallbacks = release`. No se leyó `keystore.properties`.
- **Video:** `ai/handoff/capturas/t02c-barra.mp4`. Se grabó en el emulador con `perfil`, a 720×1280 porque el códec no acepta la resolución nativa. Muestra seis cambios de pestaña y, al final, dos toques a 120 ms para ver la interrupción.
- **Redmi:** estaba conectado por USB y le instalé `perfil` (`app-perfil.apk`). Brian, ya puedes probarlo. Se instaló encima de la versión de depuración y la firma es la misma.

## Paso 4 · Comprobaciones reales
- `.\gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` → **BUILD SUCCESSFUL**.
- **Unitarias:** 22, 0 fallas (19 de antes + 3 nuevas).
- **Compose** (`connectedDebugAndroidTest`, Pixel 7 Pro, API 36): 9, 0 fallas.
- **Lint:** 0 errores y los mismos 2 avisos de antes: `VectorRaster` (el splash de DEC-17) y `ExportedService` (Media3).
- **Letra al 200 %** en el emulador: la barra cabe completa (ver la corrección 1 abajo). Luego regresé la escala a 1.0.
- **Nota del entorno:** en este worktree Gradle no encuentra el SDK porque no hay `local.properties`, y no lo creé. Corrí todo con `ANDROID_HOME` puesto en la sesión.

## Ponytail-review
Apliqué:
1. **Letra grande:** el extra de cada etiqueta se limita a lo que cabe en el ancho disponible. Antes, la pestaña Buscar podía salirse de la barra (INV-06).
2. **Al abrir la app:** `RolaNavHost` pasa `ruta ?: Destino.Biblioteca.ruta`. En el primer cuadro la ruta es `null`, y sin esto la píldora crecía sola al abrir.

No apliqué:
3. Si se tocan dos pestañas dentro del mismo cuadro, antes de que la navegación confirme, el resorte puede titubear un instante. Es raro y se corrige solo; arreglarlo pediría otra fuente de verdad para la selección.

## Para el equipo
1. **Verlo en el Redmi** con `perfil` y confirmar si ya se siente fluido.
2. **Comentario viejo** en `RolaNavHost.kt`: el KDoc dice «sin animaciones entre pestañas», pero desde la 02b sí hay fundido. No lo toqué porque está fuera de la tarea.

## Commits
- 28e5bd1 · fix(motion): barra de navegación fluida
- 8a81a0c · build: tipo perfil para probar fluidez
- (este) docs(handoff): reporte de la tarea 02c
