# Investigación: Reproductor completo

Cada punto: **Decisión**, **Por qué** y **Alternativas descartadas**. No quedan «NEEDS CLARIFICATION». Las decisiones de movimiento salen de la skill `animate` y de DEC-28.

## 1. Reproduciendo: capa encima de la app, no destino de navegación

- **Decisión**: `PlayerSheet` se dibuja en `RolaNavHost` encima del `NavHost` y de la barra. Su estado es un `Animatable<Float>` `expansion` (0 = tarjeta, 1 = pantalla completa) guardado con `rememberSaveable` (valor final) para sobrevivir a rotación y cambio de tema. Cola es un booleano dentro de la capa. `BackHandler`: primero cierra la hoja del temporizador (la propia hoja), luego Cola, luego Reproduciendo.
- **Por qué**: la apertura sigue al dedo y parte de los límites de la tarjeta (DEC-04, tarea); las transiciones del `NavHost` no se pueden manejar con un gesto arbitrario. Biblioteca sigue montada debajo (DEC-28 `biblioteca_fuera_del_navhost`).
- **Descartado**: ruta `reproduciendo` en el `NavHost` con `SharedTransitionLayout` (no sigue al dedo; el elemento compartido entre la columna inferior y una ruta obligaría a mover la tarjeta dentro del `NavHost`).

## 2. Transformación de contenedor sin recomponer en cada cuadro

- **Decisión**: la tarjeta mide sus límites en la raíz (`onPlaced` → `boundsInRoot`). La capa ocupa toda la pantalla con un `graphicsLayer { clip = true; shape = … }` cuya forma es un rectángulo redondeado interpolado entre los límites de la tarjeta (radio 20 dp, `shapes.medium`) y la pantalla (radio 0). Dentro, el contenido de la tarjeta (fundido de salida en el primer 30 % del avance, `alpha = 1 − p/0.3`) y Reproduciendo a tamaño completo (fundido de entrada desde el 30 %, con `translationY` que acompaña el borde superior del recorte). Todo `p` se lee en lambdas de `graphicsLayer`/dibujo: el gesto solo invalida capas. Mientras `p > 0` la tarjeta real tiene `alpha = 0` (para no verse doble) y la capa recibe los toques.
- **Curvas** (tokens de `RolaMotion`): soltar un gesto → `resorte()` (dampingRatio 0.8, stiffness 400) con la velocidad del dedo, interrumpible; abrir con un toque → `resorte()`; cerrar con flecha/Atrás → `tween(200, EaseOut)` (salida más corta que la entrada, DEC-28). La apertura arranca en el primer cuadro con la capa dibujada (`withFrameNanos {}` antes de animar, DEC-28 `arranque_en_primer_cuadro`).
- **Reducir movimiento**: la capa no se recorta ni se mueve con el dedo; `p` se anima con `tween(MEDIA)` al abrir y `tween(RAPIDA)` al cerrar y solo maneja la opacidad de Reproduciendo a pantalla completa (fundido corto, FR-005). Los gestos siguen detectándose y deciden al soltar.
- **Descartado**: animar `Modifier.size/offset` (relayout de Reproduciendo en cada cuadro); `AnimatedContent` (no sigue al dedo).

## 3. Gestos: un solo detector con eje dominante

- **Decisión**: en la tarjeta, un `pointerInput` con `awaitEachGesture`: espera el primer toque, `awaitTouchSlopOrCancellation` y fija el eje por el mayor de |dx| y |dy| (FR-021); luego arrastra en ese eje con `VelocityTracker`. Vertical hacia arriba maneja `expansion`; horizontal maneja `offsetX` de la tarjeta. En Reproduciendo, `Modifier.draggable(Vertical)` sobre toda la pantalla (la barra de avance consume su arrastre horizontal y no compite). Tocar la tarjeta fuera de los botones: `clickable` (el arrastre cancela el clic al pasar el umbral táctil).
- **Umbral** (función pura `swipeDecision`, probada con JUnit): se confirma si el desplazamiento en la dirección del gesto supera el **35 %** del recorrido (ancho de la tarjeta o alto de la pantalla) **o** la velocidad en esa dirección supera **700 dp/s**; una velocidad en contra cancela aunque se haya pasado la distancia. Calibración a revisar en el Redmi (no se recorta, INV-06).
- **Horizontal** (decenas de veces al día; propósito: retroalimentación y consistencia espacial): la tarjeta sigue al dedo (`translationX`); bajo el umbral vuelve con `resorte()`; al confirmar sale hacia ese lado con `tween(200, EaseOut)`, se cambia de canción, y el contenido nuevo entra con fundido `MEDIA` en su lugar. Sin canción siguiente, el gesto a la izquierda solo regresa. Reducir movimiento: sin seguir al dedo; al confirmar, solo el fundido.
- **Descartado**: `anchoredDraggable` (dos ejes en el mismo elemento y la apertura vive en otra capa); dos `draggable` (se disparan a la vez).

## 4. Cola, aleatorio y repetir en Media3

- **Decisión**: el orden real se lee de `controller.currentTimeline` con `getNextWindowIndex(i, REPEAT_MODE_OFF, shuffleModeEnabled)` desde la canción actual (lo que sigue, sin dar la vuelta). Aleatorio = `shuffleModeEnabled`; repetir = `repeatMode` (OFF → ALL → ONE → OFF). Anterior = `seekToPrevious()` (Media3 reinicia si pasaron más de 3 s, FR-012).
- **Orden aleatorio desde la actual**: el `DefaultShuffleOrder` de ExoPlayer deja la canción actual en un lugar al azar, así que parte de la cola nunca sonaría. El servicio, al encenderse el aleatorio o al cargar una lista nueva con él encendido (`onMediaItemTransition` con motivo `PLAYLIST_CHANGED`), pone un `DefaultShuffleOrder(orden, semilla)` que empieza en la actual y baraja el resto (`QueueOrder.shuffledStartingWith`, pura).
- **Operaciones** (posiciones en «A continuación»): sin aleatorio, mover = `moveMediaItem`; con aleatorio, mover cambia solo el orden aleatorio: la interfaz calcula el orden completo nuevo (`QueueOrder.move`) y lo manda con el comando propio `rola.ORDEN_ALEATORIO`. Quitar = `removeMediaItem` (ExoPlayer conserva el orden relativo). Deshacer = `addMediaItem(índiceOriginal, item)` y, con aleatorio, reenviar el orden completo guardado antes de quitar. El servicio valida que el orden sea una permutación de `0 until mediaItemCount` (frontera de confianza) y si no, lo ignora.
- **Por qué**: la cola vive en el reproductor (tarea, Paso 3) y la interfaz solo calcula la orden; nada se duplica.
- **Descartado**: barajar físicamente la lista al encender el aleatorio y guardar el orden original en el servicio (copia paralela; rompe aleatorio desde Bluetooth o la notificación).

## 5. Comandos propios de la sesión

- **Decisión**: `MediaSession.Callback.onConnect` agrega `rola.ORDEN_ALEATORIO` y `rola.TEMPORIZADOR` a los comandos disponibles **solo** para controladores del propio paquete (`controller.packageName == packageName`); `onCustomCommand` los atiende. El estado del temporizador se publica con `session.setSessionExtras` y la conexión lo lee en `MediaController.Listener.onExtrasChanged`.
- **Por qué**: Media3 no expone `setShuffleOrder` ni temporizador al controlador; los extras llegan a todos los controladores de la app sin otra vía de comunicación.

## 6. Temporizador en el servicio

- **Decisión**: `SleepTimer` (pura) guarda la opción y calcula `terminaEn` con el reloj que recibe (`SystemClock.elapsedRealtime` en el servicio, falso en pruebas). El servicio programa `Handler.postDelayed` hasta `terminaEn` → `player.pause()` y vuelve a apagado. «Al terminar la canción» usa `ExoPlayer.setPauseAtEndOfMediaItems(true)`; al recibir `onPlayWhenReadyChanged` con motivo `END_OF_MEDIA_ITEM` se apaga la opción y se vuelve a `false`. Extras publicados: `terminaEn` (elapsedRealtime, mismo reloj en todo el teléfono) o `alTerminar = true`.
- **Opciones**: 15, 30, 45, 60 min y «al terminar la canción». El servicio rechaza cualquier otro valor. Para la prueba manual de 1 minuto (Paso 7) se cambia el valor localmente y no se versiona.
- **Por qué**: FR-031 a FR-033; `elapsedRealtime` sigue contando con la pantalla apagada.
- **Descartado**: `AlarmManager` (no hace falta: el servicio está vivo mientras suena); corrutinas con reloj de prueba (`kotlinx-coroutines-test` sería dependencia nueva).

## 7. Progreso continuo (plan 006)

- **Decisión**: `PlayerState` agrega `positionAtMs` (`SystemClock.elapsedRealtime` cuando se leyó la posición) y se elimina el ticker de 500 ms: el estado solo se publica en eventos del reproductor. Un `rememberProgress(state)` corre un bucle `withFrameMillis` solo mientras suena y extrapola `posición + (ahora − positionAtMs)`, limitado a la duración, en un `State<Long>` que solo leen lambdas de dibujo (línea de la tarjeta, barra de avance). Los tiempos de texto leen un `derivedStateOf` por segundo: solo el `Text` se recompone una vez por segundo. Movimiento constante → lineal por naturaleza.
- **Reducir movimiento**: el avance se redondea al segundo (salto de un segundo, sin deslizamiento continuo).
- **Por qué**: hoy el ticker recompone `RolaNavHost` dos veces por segundo; así se recompone menos y la línea no salta.

## 8. Barra de avance propia

- **Decisión**: `SeekBar` dibuja pista (4 dp, `track`), avance (acento) y control (16 dp) con `drawBehind`; `pointerInput` con `detectTapGestures` y `detectHorizontalDragGestures` maneja una posición «buscando» local; al soltar llama a `seekTo`. Semántica: `progressBarRangeInfo` y `setProgress` (lector de pantalla). Altura táctil 48 dp con la pista centrada. Sin duración conocida, deshabilitada.
- **Descartado**: `Slider` de M3 (su valor es parámetro de composición: recompone en cada cuadro; trae indicadores y huecos de M3 que no están en Figma).

## 9. Color de la portada en claro y fundido

- **Decisión**: `coverColors(bitmap, fallback, oscuro)` gana la variante clara de DEC-16 (`acento_dinamico_claro`): contenedor de `lightMuted`/`lightVibrant` aclarado, acento de `darkVibrant`/`vibrant` con `ensureContrast` sobre el contenedor. `rememberCoverColors` anima cada color con `animateColorAsState(tween(MEDIA))` (FR-042). Reproduciendo: degradado vertical de `container` a `background` en los primeros 560 dp (Figma); Cola: fila actual con `container` y título en `accent`.
- **Nota**: la mini tarjeta en claro pasa a la variante clara (SCR-50 claro sigue «pendiente» en el grafo): va al reporte.

## 10. Componentes y dependencias

- **Hoja del temporizador**: `ModalBottomSheet` de M3 (colores del tema, asa propia de 36×4 dp como Figma), `RadioButton` con `primary`, botón con `accent`/`onAccent`.
- **Quitar deslizando**: `SwipeToDismissBox` de M3, solo de fin a inicio (izquierda), fondo `surfaceContainer` con ícono de borrar.
- **Reordenar**: arrastre desde el asa con `detectDragGestures` en un `LazyColumn`; mientras se arrastra, la interfaz guarda un orden de vista previa temporal (estado del gesto, no copia de la cola); al soltar manda una sola orden de mover. Las demás filas se acomodan con `Modifier.animateItem()` (sin animación con reducir movimiento).
- **Aviso con Deshacer**: el `SnackbarHost` de `RolaNavHost` pasa encima de la capa del reproductor; colores de Figma (`onSurface` de fondo, texto `background`, acción `primaryContainer`).
- **Cola encima de Reproduciendo**: entra desde abajo (`slideInVertically` + fundido, `LENTA`, `EaseOut`), sale por abajo (`200 ms`); reducir movimiento: fundido.
- **Íconos**: los SVG de Figma (keyboard_arrow_down, skip_previous_filled, repeat, timer, queue, drag_handle, delete) se convierten a vectores en `res/drawable` como los de la tarea 02. «Repetir una canción» (`repeat_one`) no aparece en las pantallas: se busca en la librería de Figma; si no está, va al reporte.
- **Dependencias nuevas**: ninguna. `SwipeToDismissBox`, `ModalBottomSheet`, `BackHandler` y `animateItem` ya vienen en Material 3, Activity Compose y Foundation.
