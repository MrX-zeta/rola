---

description: "Tareas de implementación de Inicio y Buscar"
---

# Tareas: Inicio y Buscar

**Entrada**: `specs/006-inicio-y-buscar/` (plan.md, spec.md, research.md, data-model.md, contracts/interfaces.md, quickstart.md)

**Pruebas**: obligatorias por DEC-22 y por la tarea 07 (Paso B6). Cada prueba va junto a su código.

**Organización**: por historia de la spec (US1, US2, US4, US3: por prioridad). Commits atómicos por área: `feat(database)`, `feat(media)`, `feat(home)`, `feat(search)`.

## Formato: `[ID] [P?] [Historia] Descripción`

- **[P]**: puede hacerse en paralelo (archivos distintos, sin dependencias pendientes).
- **[USn]**: historia de la spec.

## Convenciones de rutas

- Código: `app/src/main/java/com/luis/rola/` (`rola/`). Unitarias: `app/src/test/java/com/luis/rola/` (`test/`). Emulador: `app/src/androidTest/java/com/luis/rola/` (`androidTest/`).
- Textos en `app/src/main/res/values/strings.xml`, en español, con plurales. Colores, tipos, formas, espacios y movimiento solo desde `rola/core/designsystem` (INV-04).

---

## Fase 1: Preparación

- [ ] T001 Confirmar con Brian la columna `play_order` de `playback_queue` (plan · Seguimiento) y anotar la respuesta en `specs/006-inicio-y-buscar/research.md` §3; sin la columna, `position` es el orden en que suena
- [ ] T002 [P] Agregar `ic_refresh` (Figma 30:538) en `app/src/main/res/drawable/ic_refresh.xml` como vector de 24 dp
- [ ] T003 [P] Comparar por captura los nodos claros 51:2090, 51:2217, 51:2302, 51:4081 y 51:4183 con los oscuros y anotar diferencias en `specs/006-inicio-y-buscar/research.md` §11

---

## Fase 2: Base (bloquea todas las historias)

- [ ] T004 Crear `PlayHistoryEntity` (`play_history`: «id clave autogenerada, media_id, played_at»; índice en `media_id, played_at`) y `QueueEntity` (`playback_queue`: «position clave, media_id puede repetirse, play_order») en `rola/core/database/Entities.kt`
- [ ] T005 Crear `HistoryDao` (`add`, `stats(): Flow<List<PlayStats>>` agrupado por `media_id` con conteo y última) en `rola/core/database/HistoryDao.kt` y `QueueDao` (`rows()` por `position`, `replace()` en una transacción) en `rola/core/database/QueueDao.kt`
- [ ] T006 Subir `RolaDatabase` a versión 2 con `AutoMigration(from = 1, to = 2)` y los DAO nuevos en `rola/core/database/RolaDatabase.kt`; agregar a git `app/schemas/com.luis.rola.core.database.RolaDatabase/2.json` (FR-046)
- [ ] T007 Borrar también `play_history` en `forgetSong` (misma transacción) en `rola/core/database/PlaylistDao.kt` (FR-013)
- [ ] T008 Prueba de migración v1→v2: base v1 creada con las sentencias de `schemas/1.json`, con una favorita y una lista con canciones; abrir con Room v2 y comprobar que siguen y que las tablas nuevas están vacías, en `androidTest/core/database/MigrationTest.kt` (SC-007)
- [ ] T009 [P] Pruebas de `HistoryDao.stats`, `QueueDao.replace` (reemplaza, permite ids repetidos, vacía) y de `forgetSong` borrando historial en `androidTest/core/database/RolaDatabaseTest.kt`
- [ ] T010 Compartir una sola base en `AppContainer` (biblioteca, historial, cola) en `rola/AppContainer.kt`

**Punto de control**: compila, migra sin perder datos. Commit `feat(database): historial y cola con migración`.

---

## Fase 3: Historia 1 · Volver donde me quedé (P1) 🎯 MVP

**Meta**: canción, minuto, cola, modos y origen vuelven en pausa.

**Prueba independiente**: ordenar la cola, mover la canción a la mitad, forzar cierre, reabrir.

- [ ] T011 [P] [US1] Prueba JUnit de `PlaybackStore` y `SavedPlayback` con DataStore real en archivo temporal y DAO falso: cola en orden, índice, minuto, aleatorio con su orden, repetir y origen sobreviven; «index fuera de rango → se recorta», «order que no es permutación → orden de la lista», repetir desconocido = `OFF`; canción borrada omitida (antes, después y la actual, con y sin aleatorio); «sin ids → null»; cola vacía; `saveProgress` no llama a `replace`; `IOException` no cierra, en `test/core/media/PlaybackStoreTest.kt`
- [ ] T012 [US1] Crear `SavedPlayback` (con `keepExisting`) y `PlaybackStore` (`load`, `saveQueue`, `saveProgress`) sobre `QueueDao` y el DataStore `rola_reproduccion` (claves `indice`, `minuto_ms`, `aleatorio`, `repetir`, `origen`) en `rola/core/media/PlaybackStore.kt`, y armarlo en `rola/AppContainer.kt` (FR-020, FR-024, FR-025, FR-028)
- [ ] T013 [US1] Compartir la conversión `Song` → `MediaItem` (hoy privada en `rola/core/media/PlayerConnection.kt`) para que la use el servicio
- [ ] T014 [US1] Restaurar en `PlaybackService.onCreate`: leer, cruzar con las canciones (solo si hay filas), `setMediaItems(items, índice, minuto)`, repetir, origen, aleatorio y su orden, `prepare()` sin `play()`; poner `listo` en `sessionExtras` siempre, también si falla (`syncTimer` debe conservarlo); guardar la cola si se omitió algo, en `rola/core/media/PlaybackService.kt` (FR-022, FR-024, FR-025)
- [ ] T015 [US1] Quitar el `play()` de `onPlayerError` (si sonaba, `playWhenReady` ya es `true`) en `rola/core/media/PlaybackService.kt` (FR-022)
- [ ] T016 [US1] Guardar: cola en `onTimelineChanged` con `PLAYLIST_CHANGED` y al cambiar aleatorio; valores al cambiar canción, pausa, salto, repetir u origen, al detenerse la actividad, en `onTaskRemoved` y cada 5 s mientras suena; canales `CONFLATED` con un solo consumidor en IO y alcance de proceso; nada se guarda mientras restaura, en `rola/core/media/PlaybackService.kt` (FR-021, FR-026)
- [ ] T017 [US1] Dejar las órdenes en `pendientes` hasta que el servicio esté `listo`, y `close()` para pruebas, en `rola/core/media/PlayerConnection.kt` (FR-027)
- [ ] T018 [US1] Prueba instrumentada: insertar 3 audios propios en el MediaStore, reproducir, reordenar, saltar a la mitad, aleatorio y repetir, pausar; recrear la actividad y luego el servicio (cerrar la conexión, `stopService`, conectar otra): misma canción, cola, modos, origen, minuto y en pausa; el mini reproductor la muestra y Cola abre igual. Otra: borrar la canción actual del MediaStore antes de restaurar. En `androidTest/core/media/RestorePlaybackTest.kt` (FR-023, SC-001, SC-002)

**Punto de control**: forzar cierre y reabrir conserva todo. Commit `feat(media): la reproducción se conserva al cerrar`.

---

## Fase 4: Historia 2 · Inicio me recibe con lo mío (P1)

**Meta**: Inicio con saludo, recientes y bento; la app abre ahí.

**Prueba independiente**: con historial, abrir la app y tocar cada elemento.

- [ ] T019 [P] [US2] Prueba JUnit de `PlayCounter`: cuenta a los 30 s sonando y una sola vez; pausar no suma; adelantar no suma; corta que termina cuenta y saltada no; repetir una cuenta por vuelta; pausa al final no cuenta dos veces, en `test/core/media/PlayCounterTest.kt`
- [ ] T020 [US2] Crear `PlayCounter` puro en `rola/core/media/PlayCounter.kt` (FR-010)
- [ ] T021 [P] [US2] Prueba JUnit de recientes (10 distintas existentes, más reciente primero, se completa al faltar una) y más escuchada (empate = la más reciente, se omite la que no existe) en `test/core/data/HistoryTest.kt`
- [ ] T022 [US2] Crear `HistoryRepository` (`recent`, `mostPlayed`, `record`) en `rola/core/data/HistoryRepository.kt` (FR-011, FR-012)
- [ ] T023 [US2] Contar y registrar desde el servicio (transiciones, `isPlaying`, final de canción, aviso a los 30 s con `Handler`; escribe en IO) en `rola/core/media/PlaybackService.kt` (FR-010, FR-014)
- [ ] T024 [P] [US2] Prueba JUnit del saludo (4:59, 5:00, 11:59, 12:00, 18:59, 19:00, 0:00) y la fecha en español con mayúscula inicial en `test/feature/home/GreetingTest.kt`
- [ ] T025 [US2] Crear `greeting(hour)` y la fecha en `rola/feature/home/Greeting.kt` (FR-002)
- [ ] T026 [US2] Crear `HomeViewModel` (`HomeState`: cargando, sin canciones, contenido) en `rola/feature/home/HomeViewModel.kt` (FR-009)
- [ ] T027 [US2] Reloj de cascada reutilizable (arranca en el primer cuadro con contenido, al final con «reducir movimiento») en `rola/feature/library/Cascada.kt` (FR-042)
- [ ] T028 [US2] Crear `HomeScreen` según SCR-10 (30:408): logo, fecha y saludo (se actualiza en `ON_RESUME`), carrusel, destacado y mosaicos, esqueletos a los 300 ms, cascada; sin «Ver todo», engrane ni Letras, en `rola/feature/home/HomeScreen.kt` (FR-002, FR-003, FR-008, FR-009, FR-042)
- [ ] T029 [US2] Abrir en Inicio, destino Inicio, pestaña dueña de la pila (píldora, tocar la activa vuelve a su raíz, borrar lista vuelve a la raíz de su pestaña) y acciones (FR-004 a FR-006) en `rola/navigation/RolaNavHost.kt` (FR-001, FR-040)
- [ ] T030 [P] [US2] Prueba Compose de `HomeScreen` con historial: saludo, orden del carrusel, «N veces», totales, toques, y que no hay «Ver todo», engrane ni Letras, en `androidTest/feature/home/HomeScreenTest.kt`

**Punto de control**: Commit `feat(home): Inicio con historial`.

---

## Fase 5: Historia 4 · Encontrar algo escribiendo (P1)

**Meta**: resultados agrupados y resaltados al escribir.

**Prueba independiente**: escribir «mar», abrir un resultado de cada grupo y volver.

- [ ] T031 [P] [US4] Prueba JUnit de `matchIn` (acentos, mayúsculas, «ñ», acento suelto, sin coincidencia, tramo sobre el texto original) y de la búsqueda (empieza > contiene, orden estable, 5 por grupo, solo espacios = nulo, desconocidos fuera, Favoritas por su nombre) en `test/core/data/SearchTest.kt`
- [ ] T032 [US4] Crear `LibraryIndex` (nombres normalizados una vez) y `matchIn` en `rola/core/data/Search.kt` reusando `TitleOrder.normalizar` (FR-032 a FR-035)
- [ ] T033 [US4] Crear `SearchViewModel` (texto en `SavedStateHandle`, `debounce(150)`, `mapLatest` en `Dispatchers.Default`, vaciar sin espera, conserva lo anterior mientras calcula) en `rola/feature/search/SearchViewModel.kt` (FR-031)
- [ ] T034 [US4] Permitir el título resaltado en `SongRow`/`SongItem` en `rola/feature/library/SongMenu.kt` (FR-035, FR-038)
- [ ] T035 [US4] Crear `SearchScreen` según SCR-40 (35:1288) y SCR-41 (35:1392): campo hundido con ayuda y «Borrar búsqueda» de 48 dp, teclado al entrar sin texto, grupos con encabezado, filas de álbum, artista y lista con chevron, sin resultados con círculo elevado, relleno por el teclado, cascada solo en los primeros resultados, en `rola/feature/search/SearchScreen.kt` (FR-030 a FR-039, FR-041, FR-042)
- [ ] T036 [US4] Destino Buscar con su ViewModel, acciones y detalles hijos de Buscar en `rola/navigation/RolaNavHost.kt` (FR-037, FR-040); quitar `ComingSoonScreen` y el texto `proximamente`
- [ ] T037 [P] [US4] Prueba Compose: vacío solo campo; agrupa y resalta («mar» en `primary`); grupos vacíos no salen; sin resultados con lo buscado; borrar; ⋮ de canción; toques de cada grupo, en `androidTest/feature/search/SearchScreenTest.kt`

**Punto de control**: Commit `feat(search): buscador con resultados agrupados`.

---

## Fase 6: Historia 3 · La primera vez y sin música (P2)

**Meta**: SCR-11 y SCR-12 con escaneo.

**Prueba independiente**: sin historial; sin audios, copiar uno y escanear.

- [ ] T038 [US3] Estado «primera vez» (SCR-11, 30:447) en `rola/feature/home/HomeScreen.kt` (FR-007)
- [ ] T039 [US3] `scan()` en `MediaStoreSongRepository`: `MediaScannerConnection` sobre la carpeta Música con máximo de 15 s, nueva consulta y emisión en el flujo, devuelve cuántas canciones hay, en `rola/core/data/SongRepository.kt` y `rola/AppContainer.kt` (FR-016)
- [ ] T040 [US3] Estado «sin canciones» (SCR-12, 30:512; círculo elevado y botón acento) con progreso, botón bloqueado y aviso del resultado, en `rola/feature/home/HomeScreen.kt`, `HomeViewModel.kt` y `rola/navigation/RolaNavHost.kt` (FR-015, FR-017, FR-018, FR-041)
- [ ] T041 [US3] Prueba Compose: primera vez (invitación, sin carrusel ni destacado), «Ir a Biblioteca», sin canciones, escaneando, sin «Abrir Ajustes», en `androidTest/feature/home/HomeScreenTest.kt`

**Punto de control**: Commit `feat(home): primera vez, sin canciones y escanear`.

---

## Fase 7: Cierre

- [ ] T042 Recorrido manual de `quickstart.md` en el emulador (restaurar deslizando y forzando cierre, pestañas con detalles abiertos, escaneo, reducir movimiento) y 2 000 canciones con `perfil` (SC-003, SC-004, SC-005); letra grande y TalkBack en Inicio y Buscar (FR-045)
- [ ] T043 `.\gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y `connectedDebugAndroidTest` completas tras el último cambio
- [ ] T044 Capturas `t07-inicio.png`, `t07-inicio-primera-vez.png`, `t07-sin-canciones.png`, `t07-buscar.png`, `t07-sin-resultados.png`, `t07-restaurada.png` en `ai/handoff/capturas/` (`t07-opciones-album.png` ya está)
- [ ] T045 `design-critique` de SCR-10, 11, 12, 40 y 41 contra Figma; diferencias al reporte (SC-006, FR-044)
- [ ] T046 Si el Redmi aparece en adb, instalarle `perfil`
- [ ] T047 Comprobar que no se agregó red ni permiso ni se guarda el temporizador (FR-029, FR-043); `/ponytail:ponytail-review` antes de cada commit; `git log` sin autoría de IA; push a `origin development`; `ai/handoff/reporte.md` y estado «esperando revisión»

---

## Dependencias

- T001 → T004 (la columna). Fase 1 → Fase 2 → historias.
- US1 y US2 tocan `PlaybackService.kt`: T014–T016 antes de T023.
- US4 usa la pestaña dueña de la pila de T029 y el reloj de T027.
- US3 amplía `HomeScreen`/`HomeViewModel` de US2 (T026, T028).
- Cierre al final.

## Paralelo

- T002 y T003. T009 con T010.
- Las pruebas JUnit T011, T019, T021, T024 y T031 se escriben en paralelo con la interfaz de su historia.
- T032–T035 (Buscar) pueden avanzar sin Inicio hasta T036.

## Estrategia

MVP = Fases 1–3 (restaurar, lo que Brian reportó). Después US2 (Inicio), US4 (Buscar) y US3 (vacíos y escaneo). Cada fase termina con sus pruebas en verde y su commit.
