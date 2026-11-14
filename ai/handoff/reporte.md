# Reporte · Tarea 02b · Mantenimiento, ajustes de la revisión, esqueletos y movimiento

**Estado:** esperando revisión.

## Paso 0 · Comprobación
- `git log` incluye `6c0d66a docs: sesión de diseño tarea 02b`.
- **Incidente:** el registro de este worktree había desaparecido del repositorio principal (`.git/worktrees/` ya no existía). Lo regeneré con un worktree temporal del mismo nombre, `git worktree repair` y `git reset` (sin tocar archivos de trabajo), y adelanté la rama a `origin/development`. No se perdió nada: la tarea 02 ya estaba subida.

## Paso 1 · Herramientas
Un salto por commit, compilando y con las pruebas unitarias y de Compose en verde después de cada uno:

| Herramienta | Antes | Después |
|---|---|---|
| Kotlin (plugin compose) | 2.2.10 | 2.4.21 |
| AGP | 9.4.1 | 9.5.1 |
| BOM de Compose | 2026.02.01 | 2026.09.00 |
| Coil | 3.4.0 | 3.6.3 |

Ninguno necesitó cambios de código. No hay KSP en el proyecto todavía.

## Paso 2 · Ajustes de la revisión
- **DEC-03:** la app abre en Biblioteca (`startDestination`). Atrás desde Inicio o Buscar vuelve a Biblioteca y otro atrás sale.
- **DEC-18:** ya no se pide `POST_NOTIFICATIONS` y se quitó del manifiesto. Verificado en el emulador (Android 16, API 36) con instalación limpia: la notificación del reproductor sale igual (`t02b-notificacion-sin-permiso.png`).
- **DEC-16:** `letterSpacing = 0.sp` en todos los estilos de `RolaTypography` (con prueba en `BrandTokensTest`); `extraSmall = 8.dp` en `RolaShapes`, usado en las portadas de la lista. La mini tarjeta mantiene `small` (12) como en Figma.
- **DEC-15:** si Palette no da tono oscuro apagado, el oscuro vivo se acerca a negro un 45 % (`lerp`); el contraste sigue pasando por `ensureContrast`.

## Paso 3 · Esqueletos (DEC-09)
- `core/designsystem/Skeleton.kt`: `Modifier.esqueleto(shape)` en `surfaceContainer` con un brillo lineal de 1.2 s por pasada; con reducir movimiento queda quieto.
- **Biblioteca:** 8 filas esqueleto (portada 48 + dos líneas) solo si la carga pasa de 300 ms, con descripción accesible «Cargando canciones». Sustituye al indicador circular.
- **Portadas** (lista y mini tarjeta): esqueleto mientras Coil carga; sin portada, el recuadro con nota de siempre.
- **Prueba Compose:** con un repositorio que nunca emite, no hay esqueleto a los 100 ms y sí a los 500 ms.
- Captura: `t02b-esqueleto.png`, sacada con una prueba instrumentada desechable que no se versionó, porque en el emulador la lista carga en menos de 300 ms y el esqueleto no llega a verse.

## Paso 4 · Auditoría de movimiento
- `ai/handoff/animaciones-auditoria.md`: reconocimiento, 7 hallazgos verificados (2 HIGH, 4 MEDIUM, 1 LOW) y 6 planes con valores exactos.
- **Para después:**
  - plan 006: línea de progreso continua (LOW);
  - fundido de Permisos → app y de Permiso denegado (llegan con el onboarding);
  - transición de la mini tarjeta a Reproduciendo (tarea 03).

## Paso 5 · Movimiento (DEC-28)
Tokens en `core/designsystem/Motion.kt`: curva `EaseOut = (0.23, 1, 0.32, 1)`, duraciones 100/150/250 ms, resorte `dampingRatio 0.8, stiffness 400` (≈ 0.3 s, rebote sutil) y `LocalReduceMotion`, que es `true` cuando la escala del animador del sistema es 0 («Quitar animaciones»).

| Elemento | Entrada | Salida | Con reducir movimiento |
|---|---|---|---|
| Píldora: fondo lima | se desliza y cambia de ancho con el resorte; se reorienta si se toca otra pestaña a mitad | — | salta |
| Píldora: etiqueta | fundido 150 ms + ancho 250 ms | fundido 100 ms + ancho 200 ms | solo fundido |
| Íconos de la barra | color 150 ms; al activarse, escala 1 → 1.1 en 100 ms y vuelta con el resorte | color 150 ms | color instantáneo, sin escala |
| Entre pestañas | fundido 150 ms | fundido 100 ms | igual (fundido corto permitido) |
| Mini tarjeta | sube desde abajo 250 ms + fundido 150 ms | baja 200 ms + fundido 100 ms | solo fundido |
| Reproducir/pausa | fundido 150 ms + escala 0.9 → 1 | fundido 100 ms | solo fundido |
| Esqueleto | brillo lineal de 1.2 s | — | quieto |

- **Prueba Compose:** con reducir movimiento la mini tarjeta aparece en su posición final desde el primer cuadro; sin reducir movimiento llega desde más abajo.
- **Prueba unitaria:** `reduceMotion(0f)` y el `snap()` con reducir movimiento.
- **Nota:** el ancho de la etiqueta de la píldora se anima en layout (`expandHorizontally`), porque la tarea pide que aparezca sin saltos de tamaño y no hay alternativa sin rehacer la barra. El fondo lima, en cambio, se dibuja (`drawBehind`) y no mueve el layout.
- **Sin probar:** la salida de la mini tarjeta. Todavía no hay forma de vaciar la cola desde la app; la cubren el código y la misma transición que la entrada.

## Paso 6 · Comprobaciones reales
- `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` → **BUILD SUCCESSFUL**.
- **Unitarias:** 19, 0 fallas.
- **Compose** (`connectedDebugAndroidTest`, Pixel 7 Pro): 6, 0 fallas.
- **Lint:** 0 errores, 2 avisos: splash de 288 dp (exigido por DEC-17) y `ExportedService` (requisito de Media3). Ya no hay avisos de versiones viejas.
- **Videos y capturas** en `ai/handoff/capturas/`: `t02b-pildora.mp4`, `t02b-mini-tarjeta.mp4`, `t02b-esqueleto.png`, `t02b-notificacion-sin-permiso.png`.
- **Redmi:** no estaba conectado por USB; no lo instalé.
- **Emulador:**
  - En una sesión dejó de dibujar: pantalla negra en todas las apps, incluida Ajustes. Lo reinicié en frío.
  - Tiene ~445 MB libres. Con eso el instalador rechaza APK de 52 MB (`INSTALL_FAILED_INSUFFICIENT_STORAGE`) hasta reiniciarlo, y llegó a mostrar «System UI isn't responding».
  - **Propuesta:** darle más almacenamiento interno al AVD.

## Ponytail-review
Sin hallazgos que corregir.

## Para el equipo
1. **SCR-05 (Figma) ya no coincide con DEC-18:** el texto «Te lo pedimos al dar play» y la etiqueta «Después» de Notificaciones prometen un permiso que ya no se pide. No lo cambié (lo visual manda desde Figma). ¿Se quita la fila o se cambia el texto?
2. ¿Hacemos el plan 006 (línea de progreso continua) o lo dejamos?
3. ¿Más almacenamiento interno para el emulador?

## Commits
- 9c1850c · build: Kotlin 2.4.21
- af02238 · build: AGP 9.5.1
- f40f71b · build: BOM de Compose 2026.09.00
- e1f2713 · build: Coil 3.6.3
- e74a7ea · fix(navegacion): abrir en Biblioteca (DEC-03) y no pedir notificaciones al reproducir (DEC-18)
- b4c562e · fix(media): quitar POST_NOTIFICATIONS del manifiesto (DEC-18)
- 18e170d · style(designsystem): letterSpacing 0 y radio de portada 8 (DEC-16)
- 6ff469e · fix(player): oscurecer el fondo de la mini tarjeta si la portada solo da un tono vivo (DEC-15)
- 477cefe · feat(motion): tokens de movimiento y reducir movimiento (DEC-28, INV-06)
- ed5cbcc · feat(designsystem): esqueletos de carga
- 8fdc1e2 · feat(motion): píldora que se desliza e íconos que reaccionan
- 6ad09a2 · feat(motion): fundido corto entre pestañas
- b81b5af · feat(motion): mini tarjeta que entra y sale, y reproducir/pausa con transición
- (este) docs(handoff): reporte de la tarea 02b
