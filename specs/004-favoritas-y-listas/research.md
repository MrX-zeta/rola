# Investigación: Favoritas y Listas

## 1. Room y KSP

- **Decisión**: Room **2.8.5** (`androidx.room:room-runtime`, `room-ktx`, `room-compiler` por KSP) y el plugin KSP **2.3.14** (`com.google.devtools.ksp`).
- **Por qué**: la tarea nombra runtime, ktx y compiler, que son los artefactos de Room 2.x (en Room 3 el paquete cambia a `androidx.room3` y ktx desaparece). 2.8.5 es la última estable de 2.x. KSP 2.3.x ya no va atado a la versión de Kotlin y funciona con el Kotlin integrado de AGP 9; si la compilación lo rechaza, se detiene y se reporta (no se cambia de versión a ciegas).
- **Alternativas**: Room 3.0.3 (paquete nuevo, sin `ktx`; no coincide con lo que pide la tarea); kapt (obsoleto, no pedido).

## 2. Esquema exportado sin el plugin de Gradle de Room

- **Decisión**: `exportSchema = true` y el argumento de KSP `room.schemaLocation = $projectDir/schemas`. El JSON del esquema (versión 1) se sube al repositorio en `app/schemas/`.
- **Por qué**: la tarea permite Room y KSP, no el plugin `androidx.room`. El argumento de KSP sigue funcionando en 2.8.
- **Alternativa**: plugin de Gradle `androidx.room` (`room { schemaDirectory(...) }`): más limpio, pero sería una dependencia no pedida. Se menciona en el reporte.

## 3. Dónde corren las pruebas de DAO

- **Decisión**: en `androidTest` (emulador), con `Room.inMemoryDatabaseBuilder`. Las pruebas de la JVM quedan para lógica pura: normalización del buscador, abecedario, armado de la lista de listas.
- **Por qué**: Room en la JVM necesita Robolectric o el driver `sqlite-bundled`; ninguno está permitido.
- **Alternativa**: repositorios con DAO falso en la JVM: no probaría las consultas reales (orden, cascada), que es lo que pide la tarea.

## 4. Identidad de las canciones y canciones que ya no existen

- **Decisión**: las tablas guardan `media_id` = `MediaStore.Audio.Media._ID` (`Song.id`). Los repositorios cruzan las filas con la lista de canciones del `SongRepository`; lo que no está en esa lista se omite al mostrar y no cuenta. Las filas solo se borran cuando Rola confirma un borrado propio (FR-031).
- **Por qué**: pedido por la tarea. Ocultar sin borrar evita perder listas si se retira una SD o el MediaStore se reescanea.
- **Consecuencia**: varias pantallas leen la misma lista de canciones. Hoy `MediaStoreSongRepository.songs()` es un flujo frío que consulta el MediaStore por cada suscriptor. Se comparte una sola consulta: `AppContainer` expone `songs` con `shareIn(..., WhileSubscribed(5_000), replay = 1)` sobre un `CoroutineScope` de la app.

## 5. Borrar del dispositivo (DEC-08, INV-02)

- **Decisión**: `MediaStore.createDeleteRequest(resolver, listOf(song.uri))` → `PendingIntent`; se lanza con `rememberLauncherForActivityResult(StartIntentSenderForResult())`. Con `RESULT_OK` se comprueba que el id ya no esté en el MediaStore; si ya no está: se quitan la favorita, sus filas en `playlist_songs` y las entradas de la cola con ese `mediaId`, y snackbar «Se borró …». Con `RESULT_CANCELED`: nada. Si sigue existiendo o hay excepción: snackbar de error, sin limpiar.
- **Por qué**: es el único camino con el aviso del sistema en API 30+ (minSdk 30). No pide permisos nuevos.
- **Cola**: `PlayerConnection.removeSong(id)` quita, de atrás hacia adelante, todos los `MediaItem` con ese `mediaId`. Si era la actual, Media3 pasa sola a la siguiente o queda sin nada (la capa de Reproduciendo ya se cierra con `current == null`).

## 6. Corazón (DEC-29, skill `animate`)

- **Decisión**:
  - Relleno: el ícono cambia de contorno a relleno lima al instante (el cambio de estado es inmediato; el movimiento lo acompaña).
  - Rebote: `Animatable` de escala que arranca en 0.75 y llega a 1 con un resorte de rebote corto (`dampingRatio ≈ 0.45`, `stiffness ≈ 900`), que se asienta en ~300 ms.
  - Ráfaga: 7 partículas lima dibujadas en un `drawBehind`/`Canvas` desde un `Animatable` 0→1 (`tween` 300 ms, `EaseOut`): salen en anillo desde el centro hasta ~1.6× el radio del corazón, se encogen y se desvanecen. Se leen en la fase de dibujo: no recomponen.
  - Háptica: `HapticFeedbackType.Confirm` al marcar; al desmarcar, ninguna de confirmación.
  - Interrupción: cada toque lanza `animateTo` en el mismo `Animatable` (cancela el anterior); desmarcar corta la ráfaga (`snapTo(1f)`) y la escala vuelve a 1 con el resorte normal de `RolaMotion`.
  - Reducir movimiento: solo el cambio de ícono; sin escala ni ráfaga.
  - Accesibilidad: `Modifier.toggleable(role = Role.Switch)` con `contentDescription` «Favorita»; el lector anuncia activado/desactivado. Área táctil 48 dp (el círculo translúcido de Figma).
- **Por qué**: DEC-29 y la tarea fijan los ingredientes; DEC-28 pide resortes interrumpibles para cambios de estado. 350 ms es la excepción que da la tarea al límite de 300 ms.
- **Alternativas**: Lottie (dependencia no pedida); `AnimatedContent` entre íconos (no se interrumpe bien y crea dos nodos).

## 7. Menú de opciones (DEC-07, SCR-54)

- **Decisión**: `DropdownMenu` de M3 anclado a la fila, abierto por el ⋮ (`IconButton` 48 dp) o por pulsación larga (`combinedClickable`). Un solo composable `SongMenu` con un parámetro de lugar (`Biblioteca` o `Lista`) que decide las opciones. Íconos de Figma (cola, añadir a lista, borrar) como vectores.
- **Diferencia con Figma**: el nodo 35:1458 no tiene «Favorita / Quitar de favoritas» (es anterior a DEC-29). Se agrega como tercera opción con el ícono de corazón de 78:3332, y se anota en el reporte.

## 8. «Añadir a una lista»

- **Decisión**: una hoja inferior corta (`ModalBottomSheet`) con «Nueva lista» arriba y las listas del usuario (sin Favoritas), con el mismo estilo de fila que Biblioteca · Listas. «Nueva lista» abre el diálogo de SCR-30 y agrega la canción a la lista nueva.
- **Por qué**: no hay nodo de Figma para este selector; se reusan piezas que ya tienen diseño. Se pregunta en el reporte.

## 9. Hoja de agregar canciones (SCR-33, DEC-05)

- **Decisión**: `ModalBottomSheet` con el título, un `TextField` de búsqueda, un `LazyColumn` con casillas y el abecedario a la derecha, y el botón «Agregar N canciones» fijo abajo. Estado guardado con `rememberSaveable`: texto buscado y ids marcados (`LongArray`). Las que ya están en la lista aparecen marcadas y desactivadas.
- **Buscador**: `SearchText.matches(song, query)`: minúsculas y sin acentos (misma normalización que `TitleOrder`, que pasa a compartir su función `sinAcentos`) sobre título, artista y álbum.
- **Abecedario**: `AlphabetBar` en `feature/library`, reutilizable por Biblioteca en una tarea futura. Letras A–N, Ñ, O–Z, #; la letra activa sale de `TitleOrder.sectionOf` de la primera fila visible (`derivedStateOf`); tocar o arrastrar salta con `scrollToItem` a la primera fila de esa sección. Las letras sin canciones se ven atenuadas y no saltan. Área táctil de 48 dp de ancho. Con el buscador activo y sin resultados, el abecedario se oculta.
- **Accesibilidad**: las letras son de ~16 dp de alto (no alcanzan 48 dp). El abecedario se expone al lector como un solo control «Abecedario» con acciones personalizadas por letra con canciones; el buscador ofrece la misma función. Se anota en el reporte como posible excepción de INV-06.

## 10. Navegación

- **Decisión**: Favoritas y cada lista son destinos del `NavHost` (`favoritas` y `lista/{id}`), como pide DEC-28 `biblioteca_fuera_del_navhost` para las pantallas hijas. La raíz de Biblioteca sigue fuera del `NavHost`. La barra marca Biblioteca también en esas rutas. Al crear una lista se navega a `lista/{id}?agregar=true`, que abre la hoja.
- **Chips**: «Listas» y «Canciones» en Biblioteca, con el elegido guardado con `rememberSaveable`. Por defecto queda «Canciones» (como hoy), para que el arranque no cambie. Se pregunta en el reporte (Figma muestra «Listas» primero).

## 11. Portadas de listas

- **Decisión**: mosaico 2×2 con las portadas de las 4 primeras canciones existentes (reusa `Cover`); con 1–3 canciones, la primera a tamaño completo; vacía, recuadro `surfaceContainer` con el ícono de lista (Figma 34:1032). Favoritas usa el mismo mosaico.

## 12. Lo que no se toca

- «Reproduciendo desde» sigue diciendo «Biblioteca» (no está en la tarea).
- El botón «Añadir a una lista» junto al título en Reproduciendo (Figma 36:1465) y el ⋮ de Reproduciendo y de la lista no se agregan: no están en la tarea. Se preguntan en el reporte.
- Los ⋮ de las filas de Biblioteca · Listas (Figma 32:1128) no se agregan: sus opciones serían renombrar/borrar, fuera de alcance.
