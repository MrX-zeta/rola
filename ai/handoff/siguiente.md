# Tarea 02b · Mantenimiento, ajustes de la revisión, esqueletos y movimiento

**Estado:** lista
**Escrita:** 2026-11-14 · sesión de diseño
**Rama:** `development`. En worktree: actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.4.0 · Sin Spec Kit (es mantenimiento y pulido, no una función nueva).

## Antes de empezar (lo hace Brian)
- [ ] Commit y push: `docs: sesión de diseño tarea 02b`.
- [ ] Ponytail en **full** toda la tarea.

## Lectura obligatoria
- Grafo: DEC-03 (abre en Biblioteca, temporal), DEC-09 (esqueletos), DEC-15 (fondo de mini tarjeta), DEC-16 (`letterSpacing` 0, radio de portada 8), DEC-18 (sin `POST_NOTIFICATIONS` al reproducir), DEC-28 (movimiento), INV-06 (reducir movimiento).
- Reporte de la tarea 02: `git show 53a59a6:ai/handoff/reporte.md`.
- Skills: `improve-animations` y `animate` (en `.claude/skills/`).

## Paso 0 · Comprobación
- `git log --oneline -3` incluye `docs: sesión de diseño tarea 02b`. Si no, detente.

## Paso 1 · Actualizar herramientas
- Sube a las versiones **estables** más nuevas, una a la vez y compilando entre cada una: plugin de Kotlin (y KSP/Compose compiler si aplica), AGP, BOM de Compose. Después Coil a la versión estable más nueva.
- Si una versión rompe algo que no se arregla con cambios pequeños, quédate en la anterior y anótalo.
- Pruebas unitarias y de Compose en verde después de cada salto.
- Commits: `build: …` uno por salto.

## Paso 2 · Ajustes de la revisión
- **DEC-03:** la app abre en Biblioteca mientras Inicio no exista.
- **DEC-18:** no pidas `POST_NOTIFICATIONS` al reproducir; quítalo del manifiesto si nada más lo usa. Comprueba en Android 13+ que la notificación del reproductor sigue saliendo.
- **DEC-16:** `letterSpacing` 0 en todos los estilos de `RolaTypography`; `extraSmall` = 8 dp y úsalo en las portadas de lista.
- **DEC-15:** si Palette solo da un tono vivo para el fondo de la mini tarjeta, oscurécelo un paso; conserva el contraste de `ensureContrast`.
- Commits por tema (`fix(…)`, `style(designsystem): …`).

## Paso 3 · Esqueletos de carga (DEC-09)
- Biblioteca: mientras se lee el MediaStore, filas esqueleto con la forma de la fila real (portada 48, dos líneas de texto) en vez de lista vacía. Solo si la carga tarda más de ~300 ms, para no parpadear.
- Portadas (lista y mini tarjeta): recuadro esqueleto mientras carga; si no hay portada, el recuadro con nota de siempre.
- Colores de tema (`surfaceContainer`); el brillo animado se apaga con «reducir movimiento».
- Pruebas: la lista muestra esqueleto mientras el repositorio falso no emite.
- Commit: `feat(designsystem): esqueletos de carga`.

## Paso 4 · Auditoría de movimiento (`improve-animations`)
- Corre la skill sobre la app actual. Guarda la auditoría y los planes en `ai/handoff/animaciones-auditoria.md`.
- No apliques nada en este paso. Si la skill propone algo fuera de las pantallas que existen hoy, déjalo anotado para después.

## Paso 5 · Implementar movimiento (`animate`, DEC-28)
Obligatorios, en este orden:
1. **Píldora de la barra:** el fondo activo se desliza y cambia de ancho hacia la nueva pestaña (resorte interrumpible), y la etiqueta aparece/desaparece sin saltos de tamaño. Hoy el cambio es brusco.
2. **Íconos de la barra:** al seleccionar, el ícono reacciona (p. ej. pequeño rebote de escala o cambio de color animado); el que se deselecciona vuelve suave.
3. **Cambio de sección:** fundido corto del contenido entre pestañas.
4. **Mini tarjeta:** entra desde abajo al cargar la primera canción y sale al vaciar la cola; el ícono reproducir/pausa cambia con transición.
5. Lo que la auditoría del Paso 4 marque como prioridad alta en estas mismas pantallas.
Reglas: ≤ 300 ms (salida más corta que la entrada), interrumpible, sin animar el diseño fotograma a fotograma si hay alternativa; con «reducir movimiento» o animaciones del sistema en 0, sin desplazamiento ni escala (solo instantáneo o fundido corto). Prueba unitaria o de Compose para el caso de reducir movimiento.
- Commits: `feat(motion): …` por elemento.

## Paso 6 · Comprobaciones
- `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` y las de Compose. Resultados reales.
- Graba en el emulador un video corto (o GIF) del cambio de pestañas y de la mini tarjeta: `t02b-pildora.mp4`, `t02b-mini-tarjeta.mp4`. Capturas: `t02b-esqueleto.png`.
- Si Brian conecta su Redmi por USB, instala el APK de depuración ahí y avísale.
- `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`.

## Paso 7 · Reporte y detente
- Incluye versiones antes/después, qué se animó, duraciones elegidas y lo que la auditoría dejó para después. Estado: «esperando revisión».

## Fuera de esta tarea
Reproduciendo, cola, gestos de la mini tarjeta (tarea 03), otras pantallas nuevas.
