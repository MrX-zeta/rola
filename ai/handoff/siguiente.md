# Tarea 03 · Reproductor completo: Reproduciendo, cola, gestos y temporizador

**Estado:** esperando revisión
**Escrita:** 2026-11-14 · sesión de diseño
**Rama:** `development`. En worktree: actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.5.0 · **Spec Kit:** `specs/002-reproductor-completo/`.
**Ponytail:** off del Paso 1 al Paso 5; full del Paso 6 en adelante.
**Dispositivo:** emulador; si el Redmi está conectado, instálale `perfil` al final.

## Objetivo
Desde la mini tarjeta abro Reproduciendo, controlo la canción completa (avance, anterior/siguiente, aleatorio, repetir), veo y ordeno la cola, pongo un temporizador de apagado y uso los gestos de la mini tarjeta.

## Lectura obligatoria
- Grafo: INV-05, INV-06, INV-11; DEC-04 (gestos), DEC-09, DEC-15 y PEND-08 (color de portada), DEC-24 (Reproduciendo y cola), DEC-27, DEC-28 (movimiento, `arranque_en_primer_cuadro`), OP-02, OP-03, OP-04, OP-15.
- Pantallas: SCR-50, SCR-51 Reproduciendo, SCR-52 Cola, SCR-53 Temporizador (y sus `figma_nodo_claro`). Usa `figma-design-to-code` antes de leerlas.
- `ai/handoff/animaciones-auditoria.md` (plan 006: línea de progreso continua).

## Paso 0 · Comprobación
- `git log --oneline -3` incluye `docs: sesión de diseño tarea 03`. Si no, detente.

## Paso 1 · `/speckit-specify`
> Como dueño de Rola quiero tocar o deslizar hacia arriba la mini tarjeta para abrir Reproduciendo, con la portada grande, título, artista, barra de avance que puedo arrastrar con sus tiempos, anterior, reproducir/pausa, siguiente, aleatorio y repetir; deslizar hacia abajo para volver. En la mini tarjeta, deslizar a la izquierda pasa a la siguiente y a la derecha a la anterior. Desde Reproduciendo abro la cola: veo lo que suena y lo que sigue, reordeno arrastrando y quito deslizando, con opción de deshacer. También pongo un temporizador (15, 30, 45 min, 1 h o al terminar la canción) que pausa la música. Reproduciendo, la cola y la mini tarjeta usan el color de la portada.
Incluye los ids SCR, DEC y OP de esta tarea.

## Paso 2 · `/speckit-clarify`
Responde con el grafo; lo demás, la opción más simple marcada como **supuesto**. Ya decidido:
- **Gestos (DEC-04):** solo dentro de la mini tarjeta y en Reproduciendo (abajo para cerrar). Umbral de distancia y velocidad, interrumpibles y con alternativa por botones. Sin deslizar entre pestañas.
- **Repetir:** tres estados (apagado, toda la cola, una canción). **Aleatorio:** modo de reproducción que se puede apagar (no confundir con el botón «Aleatorio» de Biblioteca, que baraja la lista).
- **Temporizador:** vive en el servicio de reproducción; al cumplirse, pausa (no detiene ni cierra). Se muestra el tiempo restante en el ícono o en la hoja, y se puede cancelar. No se recuerda al cerrar la app (DataStore llega en la tarea 07).
- **Cola:** no se recuerda al cerrar la app (Room llega en la tarea 05). Quitar con deslizar muestra snackbar con «Deshacer» (DEC-09).
- **Fuera de esta tarea:** tocar la portada para ver la letra, el botón ⋮ y «Añadir a una lista» (tareas 05 y 08). No muestres la pista «Letra» sobre la portada ni el ⋮ todavía.

## Paso 3 · `/speckit-plan`
- Paquetes: `feature/player` (Reproduciendo, cola, temporizador, gestos), `core/media` (modos, temporizador, operaciones de cola en el `MediaSessionService`).
- La cola y los modos se manejan en el reproductor de Media3 (sin una copia paralela en la interfaz). Las operaciones (mover, quitar, deshacer) pasan por `MediaController`.
- **Transición mini tarjeta ↔ Reproduciendo:** la tarjeta crece hasta pantalla completa (elemento compartido o transformación de contenedor) siguiendo el dedo al deslizar hacia arriba; al soltar, termina con resorte o regresa según umbral. Con reducir movimiento: fundido corto. Usa `animate`.
- Línea de progreso continua (plan 006) en la mini tarjeta y la barra de Reproduciendo, sin recomponer toda la pantalla en cada cuadro.
- Color de portada: reutiliza lo de la tarea 02 (`ensureContrast`).
- Sin dependencias nuevas salvo que el plan las justifique en el reporte.

## Paso 4 · `/speckit-tasks`
## Paso 5 · `/speckit-analyze`
- Corrige inconsistencias con la constitución o el grafo. Commit: `docs(spec): 002 reproductor completo`.
- Detente y pide a Brian que cambie Ponytail a **full**.

## Paso 6 · `/speckit-implement`
- Commits atómicos por área (`feat(player): …`, `feat(media): …`, `feat(motion): …`).
- Pruebas:
  - JUnit: temporizador (cada opción y «al terminar la canción»), umbrales de gestos, operaciones de cola (mover, quitar, deshacer), formato de tiempos.
  - Compose: Reproduciendo muestra título, tiempos y estados de aleatorio/repetir; con reducir movimiento la transición no desplaza; la cola muestra la canción actual.
- `design-critique` de SCR-51, SCR-52 y SCR-53 contra Figma, en oscuro y claro.

## Paso 7 · Comprobaciones
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y las de Compose.
- En emulador: abrir y cerrar Reproduciendo tocando y deslizando, gestos de la mini tarjeta, arrastrar la barra, modos, reordenar/quitar/deshacer en la cola, temporizador de 1 min (temporal para probar, sin versionar), música que sigue al salir de la app (INV-11).
- Capturas y videos: `t03-reproduciendo.png`, `t03-cola.png`, `t03-temporizador.png`, `t03-abrir-reproduciendo.mp4`, `t03-gestos.mp4`.
- `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`.

## Paso 8 · Reporte y detente
- Incluye supuestos, decisiones y diferencias con Figma. Estado «esperando revisión».

## Fuera de esta tarea
Letras, menú de opciones, listas, Room, DataStore, Inicio, Buscar, Ajustes, widget.
