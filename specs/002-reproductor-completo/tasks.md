---

description: "Tareas de implementación del reproductor completo"
---

# Tareas: Reproductor completo

**Entrada**: `specs/002-reproductor-completo/` (plan.md, spec.md, research.md, data-model.md, contracts/interfaces.md, quickstart.md)

**Pruebas**: obligatorias por DEC-22 y por la tarea 03 (Paso 6). JUnit: temporizador (cada opción y «al terminar la canción»), umbrales de gestos, operaciones de cola (mover, quitar, deshacer), formato de tiempos. Compose: Reproduciendo muestra título, tiempos y estados de aleatorio/repetir; con reducir movimiento la transición no desplaza; la cola muestra la canción actual. Cada prueba va junto a su código.

**Organización**: por historia de la spec (US1…US6). Commits atómicos por área (`feat(media): …`, `feat(player): …`, `feat(motion): …`).

## Formato: `[ID] [P?] [Historia] Descripción`

- **[P]**: puede hacerse en paralelo (archivos distintos, sin dependencias pendientes).
- **[USn]**: historia de la spec.

## Convenciones de rutas

- Código: `app/src/main/java/com/luis/rola/` (en las tareas, `rola/`).
- Unitarias: `app/src/test/java/com/luis/rola/` (`test/`).
- Compose: `app/src/androidTest/java/com/luis/rola/` (`androidTest/`).
- Recursos: `app/src/main/res/` (`res/`).
- Textos en `res/values/strings.xml`, en español. Colores, tipos, formas, espacios y movimiento solo desde `rola/core/designsystem` (INV-04, `RolaMotion`).

---

## Fase 1: Preparación

- [X] T001 Descargar de Figma (nodos de SCR-51, SCR-52, SCR-53) los íconos keyboard_arrow_down, skip_previous_filled, repeat, timer, queue, drag_handle y delete, y convertirlos a vectores de 24 dp con relleno blanco como `res/drawable/ic_skip_next.xml`: `res/drawable/ic_cerrar.xml`, `ic_skip_previous.xml`, `ic_repeat.xml`, `ic_timer.xml`, `ic_queue.xml`, `ic_drag_handle.xml`, `ic_delete.xml`. Buscar `repeat_one` en la librería de Figma → `res/drawable/ic_repeat_one.xml`; si no existe, anotarlo para el reporte y no inventarlo
- [X] T002 [P] Agregar a `res/values/strings.xml` los textos de Reproduciendo, Cola, Temporizador y descripciones accesibles (cerrar reproductor, anterior, aleatorio encendido/apagado, repetir apagado/toda la cola/una canción, temporizador activo con restante, abrir cola, «Sonando ahora», «A continuación · %d», sin más canciones, «Quitaste «%s» de la cola», Deshacer, Subir, Bajar, Quitar de la cola, asa para mover, opciones del temporizador, «La música se pausará sola.», «Iniciar temporizador», avisos de inicio y cancelación)

---

## Fase 2: Base (bloquea todas las historias)

- [X] T003 [P] Crear `rola/core/media/QueueOrder.kt` (puro): `upNext(current, next: (Int) -> Int)`, `move(order, currentPos, from, to)`, `shuffledStartingWith(current, size, random)`, `isPermutation(order, size)` según contracts/interfaces.md; prueba `test/core/media/QueueOrderTest.kt` (mover arriba/abajo, extremos, permutación válida e inválida, aleatorio empieza en la actual y no repite)
- [X] T004 [P] Crear `rola/core/media/SleepTimer.kt` (puro): `SleepTimerOption` (`OFF`, `M15`, `M30`, `M45`, `H1`, `END_OF_SONG`) y `SleepTimer` con `start(option, now)`, `endsAt`, `isDue(now)`, `onSongEnded()` («`endsAt = ahora + minutos`; al llegar, pausa y vuelve a `OFF`»; «Pausa al terminar la canción que suena; luego `OFF`»); prueba `test/core/media/SleepTimerTest.kt` con cada opción, cancelar, reemplazar y fin de canción
- [X] T005 [P] Crear `rola/feature/player/Swipe.kt` (puro): `swipeDecision(offset, velocity, travel, density)` → `COMMIT` si distancia ≥ 35 % del recorrido o velocidad ≥ 700 dp/s en la dirección del gesto, `CANCEL` con velocidad en contra o por debajo de ambos; prueba `test/feature/player/SwipeTest.kt`
- [X] T006 Ampliar `rola/core/media/PlayerState` en `PlayerConnection.kt` con `positionAtMs`, `shuffle`, `repeat` (`Repeat.OFF/ALL/ONE`) y `timer`; quitar el ticker de 500 ms (publicar solo en eventos); agregar `previous()`, `seekTo()`, `toggleShuffle()`, `cycleRepeat()` y `queue: StateFlow<QueueState>` recalculado solo con cambios de línea de tiempo, canción actual o aleatorio
- [X] T007 Crear `rememberProgress(state)` en `rola/feature/player/SeekBar.kt`: bucle `withFrameMillis` solo mientras suena, extrapola desde `positionAtMs`, limitado a la duración; redondeo al segundo con `LocalReduceMotion`; la línea de la mini tarjeta en `rola/feature/player/MiniPlayer.kt` lo lee en `drawBehind` (plan 006)
- [X] T008 Ampliar `rola/feature/player/CoverColors.kt`: variante clara (DEC-16 `acento_dinamico_claro`) según el tema, con `ensureContrast` sobre el contenedor (4.5:1 texto, 3:1 componentes), y fundido `animateColorAsState(tween(RolaMotion.MEDIA))` de cada color. Palette necesita Android: el contraste lo cubre `test/core/designsystem/EnsureContrastTest.kt` y la variante clara se verifica en el paso 13 del quickstart
- [X] T009 Commit `feat(media): estado ampliado del reproductor y progreso continuo` (tras `/ponytail:ponytail-review`)

**Punto de control**: compila, pruebas verdes, la mini tarjeta avanza sin saltos.

---

## Fase 3: Historia 1 · Abrir Reproduciendo y controlar la canción (P1) 🎯 MVP

**Objetivo**: tocar o deslizar la tarjeta abre Reproduciendo; se controla y se cierra sin cortar la música.

**Prueba independiente**: quickstart pasos 1–5 y 14.

- [X] T010 [P] [US1] Crear `rola/feature/player/SeekBar.kt` `SeekBar`: pista 4 dp (`track`), avance y control 16 dp (`accent`), alto táctil 48 dp, tocar y arrastrar con posición local, `onSeek` al soltar, semántica `progressBarRangeInfo` + `setProgress`, deshabilitada con duración 0; tiempos con `Typography.time` y `formatDuration`
- [X] T011 [US1] Crear `rola/feature/player/NowPlayingScreen.kt` (SCR-51, nodos 36:1465 y 51:4482) con `figma-design-to-code`: barra superior (cerrar, «REPRODUCIENDO DESDE» / «Biblioteca»), portada 348 dp radio 28 dp sin sombra (DEC-27), título `Space Grotesk` 28 sp, artista, `SeekBar`, controles (aleatorio, anterior 56 dp, reproducir/pausa 80 dp con `accent`, siguiente, repetir), acciones (temporizador, cola); sin pista ni botón «Letra», sin ⋮ ni «Añadir a una lista» (FR-008); fondo degradado `container` → `background` en 560 dp; `testTag("reproduciendo")`
- [X] T012 [US1] Crear `rola/feature/player/PlayerSheet.kt`: capa con `expansion` (`Animatable`), recorte interpolado entre los límites de la tarjeta y la pantalla en `graphicsLayer`, fundidos de tarjeta (0–30 %) y Reproduciendo (30–100 %), apertura con `resorte()` tras `withFrameNanos {}`, cierre por flecha/Atrás con `tween(200, EaseOut)`, arrastre vertical hacia abajo con `swipeDecision`, `BackHandler`, estado abierto/cerrado con `rememberSaveable` (rotación y tema); con `LocalReduceMotion`: sin recorte ni seguimiento, fundido `MEDIA`/`RAPIDA` (FR-005); usar la skill `animate`
- [X] T013 [US1] Modificar `rola/feature/player/MiniPlayer.kt`: tocar la tarjeta abre; arrastre vertical hacia arriba (un `pointerInput` con eje dominante) maneja `expansion`; medir `boundsInRoot`; `alpha = 0` mientras `expansion > 0`; quitar el comentario «no hace nada todavía»
- [X] T014 [US1] Integrar en `rola/navigation/RolaNavHost.kt`: `PlayerSheet` encima del `NavHost` y la barra; `SnackbarHost` encima de la capa; cerrar la capa si `current == null`
- [X] T015 [P] [US1] Prueba `androidTest/feature/player/NowPlayingTest.kt`: título, tiempos «m:ss» y descripciones de aleatorio/repetir según el estado
- [X] T016 [P] [US1] Prueba `androidTest/feature/player/PlayerSheetTest.kt`: con `LocalReduceMotion = true` y reloj manual, a mitad de la transición el nodo `reproduciendo` ya está en su posición final (no se desplaza)
- [X] T017 [US1] Commits `feat(player): pantalla Reproduciendo` y `feat(motion): abrir y cerrar Reproduciendo desde la mini tarjeta`

**Punto de control**: US1 completa y probable por sí sola.

---

## Fase 4: Historia 2 · Aleatorio y repetir (P1)

**Objetivo**: modos que se encienden, se ven y se respetan, también desde la notificación.

**Prueba independiente**: quickstart pasos 6 y 7.

- [X] T018 [US2] En `rola/core/media/PlaybackService.kt`: al encender el aleatorio o cargar lista nueva con él (`onMediaItemTransition` con `PLAYLIST_CHANGED`), `setShuffleOrder(DefaultShuffleOrder(QueueOrder.shuffledStartingWith(...), semilla))`
- [X] T019 [US2] En `rola/feature/player/NowPlayingScreen.kt`: aleatorio con `accent` si está encendido, repetir con `ic_repeat`/`ic_repeat_one` y `accent` si no está apagado; descripciones con el estado (FR-016); conectar `toggleShuffle()` y `cycleRepeat()`; comprobar en el emulador que siguiente/anterior desde la notificación respetan los modos (FR-017)
- [X] T020 [US2] Commit `feat(media): modos aleatorio y repetir`

---

## Fase 5: Historia 3 · Gestos en la mini tarjeta (P2)

**Objetivo**: izquierda = siguiente, derecha = anterior, con umbral e interrupción.

**Prueba independiente**: quickstart paso 8.

- [X] T021 [US3] En `rola/feature/player/MiniPlayer.kt`: eje horizontal del mismo detector; `translationX` sigue al dedo; bajo el umbral vuelve con `resorte()`; al confirmar sale con `tween(200, EaseOut)`, llama `next()`/`previous()` y el contenido entra con fundido `MEDIA`; sin siguiente, solo regresa; con `LocalReduceMotion`, sin seguir al dedo y solo fundido
- [X] T022 [US3] Commit `feat(motion): gestos de la mini tarjeta`

---

## Fase 6: Historia 4 · Ver y ordenar la cola (P2)

**Objetivo**: Cola con actual y siguientes, reordenar, quitar y deshacer.

**Prueba independiente**: quickstart paso 9.

- [X] T023 [US4] En `rola/core/media/PlaybackService.kt`: `onConnect` agrega `rola.ORDEN_ALEATORIO` solo para el propio paquete; `onCustomCommand` valida «Permutación exacta de `0 until mediaItemCount`» y aplica `setShuffleOrder`
- [X] T024 [US4] En `rola/core/media/PlayerConnection.kt`: `moveUpNext(from, to)` (sin aleatorio `moveMediaItem`; con aleatorio orden por comando), `removeUpNext(position): Removed?` y `undoRemove(removed)` («Deshacer reinserta en `index` y, si había aleatorio y el tamaño coincide, reenvía `orderBefore`»)
- [X] T025 [US4] Crear `rola/feature/player/QueueScreen.kt` (SCR-52, nodos 36:1557 y 51:4570) con `figma-design-to-code`: barra (flecha, «Cola»), «SONANDO AHORA» con fila `container`/título `accent` y ecualizador estático, «A CONTINUACIÓN · N», filas con asa (reordenar con vista previa temporal y `animateItem`), `SwipeToDismissBox` solo de fin a inicio con fondo `surfaceContainer` e ícono de borrar, acciones del lector «Subir/Bajar/Quitar de la cola», texto sin más canciones; sin «Limpiar»; `testTag("cola")`
- [X] T026 [US4] En `rola/feature/player/PlayerSheet.kt`: Cola encima de Reproduciendo, abierta/cerrada con `rememberSaveable` (entra desde abajo `LENTA` `EaseOut`, sale en 200 ms; reducir movimiento: fundido), Atrás la cierra primero; aviso «Quitaste «título» de la cola» con Deshacer (colores de Figma) por el `SnackbarHost` de `RolaNavHost`
- [X] T027 [P] [US4] Prueba `androidTest/feature/player/QueueScreenTest.kt`: la canción actual aparece bajo «Sonando ahora» y las siguientes con su número
- [X] T028 [US4] Commits `feat(media): operaciones de cola` y `feat(player): pantalla Cola`

---

## Fase 7: Historia 5 · Temporizador de apagado (P3)

**Objetivo**: pausa sola a la hora elegida o al terminar la canción.

**Prueba independiente**: quickstart pasos 10 y 11.

- [X] T029 [US5] En `rola/core/media/PlaybackService.kt`: comando `rola.TEMPORIZADOR` (solo propio paquete, nombre válido), `SleepTimer` + `Handler.postDelayed` → `pause()`; «al terminar la canción» con `setPauseAtEndOfMediaItems(true)` y apagado en `END_OF_MEDIA_ITEM`; extras `temporizador`/`terminaEn`; cancelar en `onDestroy`
- [X] T030 [US5] En `rola/core/media/PlayerConnection.kt`: `setSleepTimer(option)` y lectura de extras (`onExtrasChanged`) a `PlayerState.timer`
- [X] T031 [US5] Crear `rola/feature/player/SleepTimerSheet.kt` (SCR-53, nodos 36:1651 y 51:4662) con `figma-design-to-code`: `ModalBottomSheet` con colores del tema, asa 36×4 dp, título, subtítulo (o restante si hay temporizador), radios, botón `accent` «Iniciar temporizador»; botón del temporizador activo con `accent` y descripción con restante; avisos de inicio y cancelación
- [X] T032 [US5] Commit `feat(player): temporizador de apagado`

---

## Fase 8: Historia 6 · Color de la portada (P3)

**Objetivo**: tarjeta, Reproduciendo y Cola con el color de la portada, en oscuro y claro.

**Prueba independiente**: quickstart paso 13 y portadas variadas.

- [X] T033 [US6] Verificar que `NowPlayingScreen`, `QueueScreen` y `MiniPlayer` usan `rememberCoverColors` (T008) y que la hoja y el aviso usan el tema (FR-040); ajustar lo que falte
- [X] T034 [US6] Commit `feat(player): color de portada en claro y fundido` (si hubo cambios aparte de T008)

---

## Fase 9: Pulido y cierre

- [X] T035 `design-critique` de SCR-51, SCR-52 y SCR-53 contra Figma en oscuro y claro; diferencias al reporte
- [X] T036 Comprobaciones del Paso 7: `.\gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y `:app:connectedDebugAndroidTest`
- [X] T037 Recorrido manual de `quickstart.md` en el emulador (temporizador de 1 min solo local); capturas y videos `t03-*` en `ai/handoff/capturas/`
- [X] T038 Si el Redmi está conectado, instalar `perfil`
- [X] T039 `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`
- [X] T040 `ai/handoff/reporte.md` y estado «esperando revisión» en `ai/handoff/siguiente.md`

---

## Dependencias y orden

- Fase 1 → Fase 2 → historias. T006 bloquea todas; T007 y T008 bloquean US1 y US6.
- US1 bloquea US2 (botones en Reproduciendo), US4 y US5 (se abren desde Reproduciendo). US3 solo necesita T005 y T006.
- US6 se cierra al final porque toca las pantallas de US1 y US4.

## Ejemplos en paralelo

- Fase 2: T003, T004 y T005 (archivos puros distintos).
- US1: T010 y las pruebas T015/T016 una vez existe la interfaz.
- Después de US1: US3 (MiniPlayer) en paralelo con US4 (servicio y QueueScreen).

## Estrategia

MVP = Fases 1–3 (abrir y controlar Reproduciendo). Luego modos, gestos, cola, temporizador y color, cada uno con su commit y comprobado con su paso del quickstart.

---

## Fase 10: Convergencia

Revisión de Brian en el Redmi (tarea 03b, grafo 1.6.0). Cada tarea cita el requisito de `spec.md` que la origina.

- [X] T041 Hacer que el cierre de Reproduciendo termine siempre en cerrado o abierto al soltar, cancelar o interrumpir el gesto: hoy cada delta de `draggable` lanza un `snapTo` en `scope` que puede correr después de `onDragStopped` y cortar su animación, dejando la capa a medias (`feature/player/PlayerSheet.kt`; revisar el mismo patrón en `MiniPlayer.kt`) per FR-003, SC-012, US1/AC3 (contradicts)
- [X] T042 Recalibrar el umbral horizontal: distancia recorrida desde donde empieza el dedo, `min(25 % del ancho, 96 dp)`, o velocidad ≥ 400 dp/s, y que funcione empezando en cualquier punto de la tarjeta (hoy 35 % del recorrido o 700 dp/s en `feature/player/Swipe.kt`); valores en `RolaMotion` o junto al gesto; actualizar `SwipeTest` per FR-018, SC-011, US3/AC1 (partial)
- [X] T043 Repartir Reproduciendo en toda la altura: portada que se adapta al alto disponible, reproducir ≥ 80 dp y anterior/siguiente ≥ 56 dp, fila de acciones cerca del borde inferior respetando la barra del sistema (`feature/player/NowPlayingScreen.kt`) per FR-007, US1/AC9 (partial)
- [X] T044 Agregar el gesto horizontal sobre la portada de Reproduciendo (siguiente/anterior) con el mismo umbral que la mini tarjeta: la portada sigue al dedo y vuelve o cambia; con reducir movimiento, sin seguir al dedo (`feature/player/NowPlayingScreen.kt`) per FR-049, FR-020, US1/AC8 (missing)
- [X] T045 Quitar el botón siguiente de la mini tarjeta y el parámetro `onNext` que ya no se use (`feature/player/MiniPlayer.kt`, `navigation/RolaNavHost.kt`, `MiniPlayerTest`) per FR-050, US3/AC6 (contradicts)
- [X] T046 Tocar una fila de «A continuación» reproduce esa canción (`feature/player/QueueScreen.kt`, `core/media/PlayerConnection.kt`) per FR-052, US4/AC8 (missing)
- [X] T047 Desplazamiento automático al arrastrar cerca de los bordes de la Cola (arriba y abajo) y colocar la fila arrastrada en la posición válida más cercana al llegar al tope, sin perderla ni taparla (`feature/player/QueueScreen.kt`) per FR-053, US4/AC9 (missing)
- [X] T048 «Limpiar» en la barra de Cola: quita todo lo que sigue, deja la actual, aviso con «Deshacer» que lo devuelve en su orden; deshabilitado sin canciones siguientes (`QueueScreen.kt`, `PlayerConnection.kt`, aviso en `RolaNavHost.kt`) per FR-054, US4/AC10 (missing)
- [X] T049 Guardar las órdenes que llegan antes de conectar el `MediaController` y enviarlas en orden al conectar; quitar el `shortcut:` de `core/media/PlayerConnection.kt` per FR-055 (missing)
- [X] T050 Pedir la portada al tamaño en que se dibuja (hoy `loadThumbnail` fijo a 512 px en `feature/library/Cover.kt`), sin agrandar la que usa Palette en `CoverColors.kt` per FR-007 (partial)
- [X] T051 Sin portada en tema claro, el botón de reproducir usa `accent` con `onAccent`; la barra de avance y el título siguen en `primary` (`feature/player/CoverColors.kt` y quien pinte el botón) per FR-039 (partial)
- [X] T052 Título de la mini tarjeta como marquesina (`basicMarquee`) solo si no cabe; estático con puntos suspensivos si cabe o con reducir movimiento (`feature/player/MiniPlayer.kt`) per FR-051, US3/AC7 (missing)
- [X] T053 Pruebas nuevas: umbral desde el centro de la tarjeta (JUnit `SwipeTest` y Compose), cierre que nunca queda a medias (`PlayerSheetTest`), tocar fila de la cola, «Limpiar» + Deshacer (`QueueScreenTest`), marquesina estática con reducir movimiento (`MiniPlayerTest`) per SC-011, SC-012, FR-051, FR-052, FR-054 (missing)

## Fase 11: Convergencia

- [X] T054 Al intercambiar con la primera fila visible, conservar la posición de la lista en el mismo cuadro (`requestScrollToItem`, sin corrutina) para que la fila arrastrada siga bajo el dedo (`feature/player/QueueScreen.kt`) per FR-056 (partial)
- [X] T055 Limitar la fila arrastrada a la lista (ni arriba de la primera posición ni abajo de la última ni fuera de la vista) y detener el desplazamiento automático cuando la lista no puede avanzar (`feature/player/QueueScreen.kt`) per FR-057 (missing)
- [X] T056 Pruebas de Compose: arrastrar al primer lugar deja la fila en índice 0 bajo el dedo; arrastrar más allá del final la deja en el último índice (`QueueScreenTest`) per FR-056, FR-057 (missing)

## Fase 12: Convergencia

Tarea 04 Parte A (video de Brian en el Redmi): el arrastre propio pierde la fila cuando sale de la ventana compuesta.

- [ ] T057 Cambiar el arrastre de la Cola a `sh.calvin.reorderable` 3.1.0 (`ReorderableItem` + `draggableHandle`) y quitar el arrastre propio (vista previa, `reordenar`, `limitar`, autodesplazamiento) (`gradle/libs.versions.toml`, `app/build.gradle.kts`, `feature/player/QueueScreen.kt`) per plan: dependencia `reorderable`, FR-053, FR-056, FR-057 (contradicts)
- [ ] T058 Si la cola cambia a mitad del arrastre, seguir arrastrando y al soltar aplicar el movimiento sobre la cola nueva; quitar el `shortcut:` de la vista previa (`feature/player/QueueScreen.kt`) per FR-058 (missing)
- [ ] T059 Solo las filas de «A continuación» son destinos; soltar sobre «Sonando ahora» o las etiquetas deja la fila primera (`feature/player/QueueScreen.kt`) per FR-059 (partial)
- [ ] T060 Pruebas de Compose: arrastre arriba y abajo con la librería, y cola que cambia a mitad del arrastre (`QueueScreenTest`) per FR-058, FR-059 (missing)
