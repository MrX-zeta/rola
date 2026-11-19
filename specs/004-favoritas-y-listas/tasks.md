---

description: "Tareas de implementación de Favoritas y Listas"
---

# Tareas: Favoritas y Listas

**Entrada**: `specs/004-favoritas-y-listas/` (plan.md, spec.md, research.md, data-model.md, contracts/interfaces.md, quickstart.md)

**Pruebas**: obligatorias por DEC-22 y por la tarea 05 (Paso 6). DAO (insertar, quitar, orden, borrado en cascada), repositorio de favoritas, normalización del buscador; Compose: el corazón cambia de estado y con reducir movimiento no escala, Favoritas fija arriba, lista vacía con su botón, menú con sus opciones según el lugar. Cada prueba va junto a su código.

**Organización**: por historia de la spec (US1…US6). Commits atómicos por área: `feat(database): …`, `feat(player): corazón de favoritas`, `feat(playlists): …`, `feat(library): menú de opciones`.

## Formato: `[ID] [P?] [Historia] Descripción`

- **[P]**: puede hacerse en paralelo (archivos distintos, sin dependencias pendientes).
- **[USn]**: historia de la spec.

## Convenciones de rutas

- Código: `app/src/main/java/com/luis/rola/` (en las tareas, `rola/`).
- Unitarias: `app/src/test/java/com/luis/rola/` (`test/`).
- Emulador: `app/src/androidTest/java/com/luis/rola/` (`androidTest/`).
- Recursos: `app/src/main/res/` (`res/`).
- Textos en `res/values/strings.xml` (plurales en `<plurals>`), en español. Colores, tipos, formas, espacios y movimiento solo desde `rola/core/designsystem` (INV-04, `RolaMotion`). Todo plano: sin `Relief.kt` en esta función (DEC-27).
- Antes de construir cada pantalla: skill `figma-design-to-code` y `get_design_context` del nodo (oscuro y claro).

---

## Fase 1: Preparación

- [X] T001 Agregar a `gradle/libs.versions.toml`: `room = "2.8.5"`, `ksp = "2.3.14"`, librerías `androidx-room-runtime`, `androidx-room-ktx`, `androidx-room-compiler` y plugin `ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }`; en `build.gradle.kts` `alias(libs.plugins.ksp) apply false`; en `app/build.gradle.kts` el plugin, `implementation` de runtime y ktx, `ksp(libs.androidx.room.compiler)` y `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`; `androidTestImplementation` de nada nuevo. Comprobar `:app:assembleDebug`. Si KSP rechaza el Kotlin integrado de AGP 9, detenerse y reportar (research §1)
- [X] T002 [P] Descargar de Figma y convertir a vectores en `res/drawable/` (24 dp, relleno blanco como los demás `ic_*`): `ic_favorite` (contorno) e `ic_favorite_filled` (78:3332), `ic_more_vert`, `ic_queue_add`, `ic_playlist_add`, `ic_add`, `ic_arrow_back`, `ic_playlist` (ícono de lista vacía de 34:1032); reusar `ic_delete`, `ic_check`, `ic_search`, `ic_shuffle`, `ic_play` existentes
- [X] T003 [P] Agregar a `res/values/strings.xml`: chips «Listas»; «Nueva lista», «Ponle nombre y agrega canciones», «Favoritas», «Vacía», plural «%d canciones»; diálogo «Nueva lista», «Nombre», «Cancelar», «Crear»; lista «Lista · %1$s», «Tu lista está vacía», «Agrega canciones de tu biblioteca para empezar a escucharla.», «Agregar canciones», «Reproducir», «Aleatorio», «Agregar»; Favoritas vacía «Aún no tienes favoritas» y «Toca el corazón en Reproduciendo o usa ⋮ en una canción.»; hoja «Agregar canciones», «Buscar en tu biblioteca», plural «Agregar %d canciones», «Sin resultados», «Abecedario», «Ir a la letra %s»; menú «Opciones de %s», «Añadir a la cola», «Añadir a una lista», «Favorita», «Quitar de favoritas», «Borrar del dispositivo», «Quitar de la lista»; avisos «Añadida a la cola», «Añadida a %s», plural «Se agregaron %d canciones a %s», «Ya estaba en %s», «Quitaste %s», «Añadida a Favoritas», «Quitada de Favoritas», «Se borró %s», «No se pudo borrar %s», «Volver»

---

## Fase 2: Base (bloquea todas las historias)

- [X] T004 Crear `rola/core/database/Entities.kt` según data-model.md: `FavoriteEntity(@PrimaryKey media_id: Long, added_at: Long)` en `favorites`; `PlaylistEntity(@PrimaryKey(autoGenerate = true) id: Long = 0, name, created_at, updated_at)` en `playlists`; `PlaylistSongEntity(playlist_id, media_id, position, added_at)` en `playlist_songs` con clave primaria `(playlist_id, media_id)`, `ForeignKey` a `playlists.id` con `onDelete = CASCADE` e índice en `media_id`. Nombres de columna en snake_case con `@ColumnInfo`
- [X] T005 Crear `rola/core/database/FavoriteDao.kt` y `PlaylistDao.kt` según contracts/interfaces.md: `ids()` «ORDER BY added_at DESC»; `add` con `IGNORE`; `remove`; `playlists()` «ORDER BY created_at DESC»; `allSongs()`, `playlist(id)`, `songs(id)` «ORDER BY position»; `create`; `@Transaction append(id, mediaIds, now)` que lee `MAX(position)`, inserta con `IGNORE` en el orden dado desde `max + 1`, cuenta las insertadas (`insert` devuelve −1 si ya estaba) y actualiza `updated_at` si entró alguna; `insert` (Deshacer); `remove` (actualiza `updated_at`); `delete(id)`; `@Transaction forgetSong(mediaId)` que borra de `favorites` y de `playlist_songs`
- [X] T006 Crear `rola/core/database/RolaDatabase.kt` (`@Database(version = 1, exportSchema = true)`) y construirla una vez en `rola/AppContainer.kt` (`Room.databaseBuilder(context, RolaDatabase::class.java, "rola.db")`); compilar para generar `app/schemas/com.luis.rola.core.database.RolaDatabase/1.json` y agregarlo a git
- [X] T007 [P] Prueba `androidTest/core/database/FavoriteDaoTest.kt` (`Room.inMemoryDatabaseBuilder`): agregar dos con `added_at` 1 y 2 → `ids()` = [2, 1]; agregar repetida no duplica; quitar la deja fuera
- [X] T008 [P] Prueba `androidTest/core/database/PlaylistDaoTest.kt`: `append` en orden y sin duplicar (devuelve solo las nuevas); `songs` respeta `position` tras quitar una del medio; `insert` de Deshacer la devuelve a su lugar; `delete` de la lista borra sus `playlist_songs` (cascada); `forgetSong` quita la favorita y la canción de todas las listas; `playlists()` más nueva arriba
- [X] T009 Commit `feat(database): favoritas y listas con Room`
- [X] T010 [P] Crear `rola/core/data/SearchText.kt` (`matches(song, query)`: consulta recortada; vacía = todo; minúsculas sin acentos sobre título, artista y álbum) y `PlaylistName.kt` (`MAX = 60`; `clean(raw)` recorta y devuelve `null` si queda vacío; corta a 60). Pasar `sinAcentos` de `TitleOrder.kt` a `internal` para compartirla
- [X] T011 [P] Pruebas `test/core/data/SearchTextTest.kt` («cancion» encuentra «Canción», busca por artista y álbum, «ñ» no se confunde con «n», consulta vacía) y `PlaylistNameTest.kt` (espacios → `null`, recorte, 61 caracteres → 60)
- [X] T012 Crear `rola/core/data/LibraryRepository.kt` según contracts/interfaces.md: recibe `RolaDatabase`, el `Flow<List<Song>>` compartido y un reloj; arma `favoriteIds`, `favorites` (en orden de `ids()`, solo existentes), `favoritesSummary` y `playlists` (`PlaylistSummary` con conteo y hasta 4 `coverIds` de existentes), `playlist(id)` (`PlaylistDetail` con `totalMs` de las existentes; `null` si no existe), `setFavorite`, `createPlaylist`, `addToPlaylist`, `removeFromPlaylist` (devuelve la fila para Deshacer), `restore`, `forgetSong`. La parte pura (cruzar filas con canciones) en funciones `internal` para probarlas en la JVM
- [X] T013 [P] Prueba `test/core/data/LibraryRepositorySummaryTest.kt` (funciones puras): omite ids inexistentes en conteo, portada y detalle; respeta el orden de las filas; `totalMs` suma solo existentes
- [X] T014 Prueba `androidTest/core/data/LibraryRepositoryTest.kt` (base en memoria, canciones en un `MutableStateFlow`): marcar A y luego B → `favorites` = [B, A]; desmarcar; una favorita que desaparece de las canciones se oculta y su fila sigue; al volver, reaparece
- [X] T015 En `rola/AppContainer.kt`: `CoroutineScope(SupervisorJob() + Dispatchers.Default)` de la app; `val songs = songRepository.songs().shareIn(scope, WhileSubscribed(5_000), replay = 1)`; `val library = LibraryRepository(db, songs)`. `LibraryViewModel` pasa a usar el flujo compartido (research §4)
- [X] T016 En `rola/core/media/PlayerConnection.kt`: `addToQueue(song)` (`addMediaItem` al final; si `mediaItemCount == 0`, `setMediaItems` + `prepare` sin `play`) y `removeSong(id)` (de atrás hacia adelante, `removeMediaItem` de cada índice cuyo `mediaId == id`), ambos con `whenConnected`

**Punto de control**: pruebas de DAO y repositorio en verde; la app compila sin cambios visibles.

---

## Fase 3: Historia 1 · Marcar la canción que suena como favorita (P1) 🎯 MVP

**Meta**: corazón sobre la portada de Reproduciendo (SCR-51, 36:1465 / 51:4482).

**Prueba independiente**: con `favorite` en un estado, tocar el corazón llama a `onToggle`; el estado se anuncia como interruptor; con reducir movimiento no hay escala.

- [X] T017 [US1] Cargar la skill `animate` y crear `rola/feature/player/FavoriteButton.kt` (research §6): caja de 48 dp, círculo `scrim` translúcido como en Figma (leer el valor con `get_design_context`), ícono 24 dp: `ic_favorite_filled` en `RolaTheme.colors.accent` si es favorita, `ic_favorite` en blanco del contenido si no; `toggleable(value = favorite, role = Role.Switch)` con `contentDescription` «Favorita» y `testTag("favorita")`. Al pasar a favorita (no en la primera composición): `HapticFeedbackType.Confirm`; escala `Animatable` `snapTo(0.75f)` → `animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 900f))`; ráfaga `Animatable` `snapTo(0f)` → `animateTo(1f, tween(300, easing = RolaMotion.EaseOut))` dibujando 7 círculos lima en `drawBehind` (ángulo `i·360/7`, radio de 0.5× a 1.6× el del ícono, diámetro de 4 dp a 0, alfa 1→0). Al quitar: ráfaga `snapTo(1f)`, escala a 1 con `RolaMotion.resorte()`. Todo leído en `graphicsLayer`/`drawBehind`. Con `LocalReduceMotion`: sin escala ni ráfaga. Total ≤ 350 ms
- [X] T018 [US1] En `rola/feature/player/NowPlayingScreen.kt`: parámetros `favorite: Boolean` y `onToggleFavorite`; `FavoriteButton` alineado abajo a la izquierda sobre la portada con el margen de Figma; actualizar el comentario de cabecera (el botón «Añadir a una lista» junto al título sigue pendiente, research §12)
- [X] T019 [US1] En `rola/navigation/RolaNavHost.kt` (`PlayerOverlay`): `favorite = song.id in favoriteIds` desde `LibraryViewModel.favoriteIds` y `onToggleFavorite = { vm.setFavorite(song.id, !favorite) }`. En `rola/feature/library/LibraryViewModel.kt`: recibe `LibraryRepository`; `favoriteIds: StateFlow<Set<Long>>`; `setFavorite(id, favorite)` en `viewModelScope`
- [X] T020 [US1] Actualizar `androidTest/feature/player/NowPlayingTest.kt` a la firma nueva y crear `androidTest/feature/player/FavoriteButtonTest.kt`: tocar alterna el estado (con un `mutableStateOf` externo) y el nodo `favorita` es `Role.Switch` con `ToggleableState` correcto; con `LocalReduceMotion provides true` y reloj manual, a mitad de la animación el ícono mide lo mismo que en reposo (sin escala); sin reducir movimiento, a los 50 ms mide menos que en reposo
- [X] T021 [US1] Commit `feat(player): corazón de favoritas`

**Punto de control**: el corazón funciona en Reproduciendo y persiste tras reiniciar la app.

---

## Fase 4: Historia 2 · Encontrar mis favoritas (P1)

**Meta**: chips Listas/Canciones; Favoritas fija; pantalla de Favoritas.

**Prueba independiente**: con dos favoritas, Biblioteca · Listas muestra «Favoritas · 2 canciones» primero; al abrirla, la más reciente arriba.

- [X] T022 [US2] Crear `rola/feature/playlists/PlaylistCover.kt`: mosaico 2×2 con `Cover` de hasta 4 canciones (1–3: la primera a tamaño completo); vacía: recuadro `surfaceContainer` con `ic_playlist` en `onSurfaceVariant`; forma y tamaño por parámetro
- [X] T023 [US2] Crear `rola/feature/playlists/PlaylistsTab.kt` (SCR-23, 32:1128 / 51:2902): fila «Nueva lista» (recuadro 56 dp `primaryContainer`/lima de Figma con `ic_add`, título y «Ponle nombre y agrega canciones»), fila Favoritas (`testTag("fila_favoritas")`) siempre segunda, luego las listas; cada fila: portada 56 dp, nombre (una línea, puntos suspensivos) y «N canciones» o «Vacía»; sin ⋮ (research §12); filas ≥ 64 dp
- [X] T024 [US2] En `rola/feature/library/LibraryScreen.kt`: chips «Listas» y «Canciones» (`testTag("chip_listas")`/`chip_canciones`), píldora lima en el elegido y `surfaceContainer` en el otro como Figma; chip elegido en `rememberSaveable`, por defecto «Canciones» (research §10); con «Listas» se muestra `PlaylistsTab` (con la misma cascada de entrada); actualizar el comentario de cabecera. Actualizar `androidTest/feature/library/LibraryScreenTest.kt` a la firma nueva
- [X] T025 [US2] Crear `rola/feature/playlists/PlaylistScreen.kt` (SCR-32, 34:1345 / 51:3943; SCR-31, 34:1032 / 51:3514): barra con `ic_arrow_back` («Volver»), sin ⋮; cabecera con `PlaylistCover` 96 dp, nombre (`headlineMedium`) y «Lista · N canciones · X min»; con canciones: «Reproducir» (píldora lima con `ic_play`), Aleatorio y Agregar (círculos `surfaceContainer` de 48 dp) y filas de canción (actual en `primary`) con ⋮; vacía (`testTag("lista_vacia")`): título, texto y botón «Agregar canciones». Con `isFavorites`: nombre «Favoritas», sin Agregar, estado vacío con el texto de Favoritas y sin botón
- [X] T026 [US2] Crear `rola/feature/playlists/PlaylistViewModel.kt`: recibe `LibraryRepository` e id (`FAVORITAS_ID = -1L` → detalle de Favoritas); `detail: StateFlow<PlaylistDetail?>`; fábrica como la de `LibraryViewModel`
- [X] T027 [US2] En `rola/navigation/RolaNavHost.kt`: rutas `favoritas` y `lista/{id}?agregar={agregar}` dentro del `NavHost`; `onOpen` desde `PlaylistsTab`; Reproducir → `player.play(songs, 0)`, Aleatorio → `player.shuffle(songs)`, tocar fila → `player.play(songs, i)`; si la lista deja de existir, volver. En `rola/navigation/RolaNavBar.kt`: la pestaña Biblioteca queda activa con rutas que empiezan con `favoritas` o `lista/`; `enBiblioteca` del `NavHost` sigue siendo solo la raíz
- [X] T028 [US2] Prueba `androidTest/feature/playlists/PlaylistsTabTest.kt`: Favoritas aparece justo después de «Nueva lista» y antes de las listas, también con 0 canciones; muestra el conteo; y `PlaylistScreenTest.kt` (primera parte): Favoritas sin botón Agregar

**Punto de control**: Favoritas visible y reproducible desde Biblioteca.

---

## Fase 5: Historia 3 · Crear una lista y llenarla (P1)

**Meta**: diálogo de crear (SCR-30) y hoja de agregar (SCR-33) con buscador, abecedario y casillas.

**Prueba independiente**: crear «Viaje», marcar 3 y confirmar: la lista tiene esas 3 en orden.

- [X] T029 [US3] Crear `rola/feature/playlists/CreatePlaylistDialog.kt` (SCR-30, 34:1103 / 51:3585): `AlertDialog` M3 con `OutlinedTextField` «Nombre» enfocado al abrir, `maxLength` «60 caracteres» (`PlaylistName.MAX`), «Cancelar» y «Crear» (píldora lima) desactivado si `PlaylistName.clean` da `null`; Intro confirma
- [X] T030 [P] [US3] Crear `rola/feature/library/AlphabetBar.kt` (research §9): letras A–N, Ñ, O–Z, # en `labelSmall`; activa en círculo lima; sin canciones atenuadas; tocar o arrastrar en vertical llama `onJump` con secciones que tengan canciones; ancho táctil 48 dp; semántica: un solo nodo «Abecedario» con `customActions` «Ir a la letra X». Función pura `firstIndexBySection(songs): Map<String, Int>` con `TitleOrder.sectionOf`
- [X] T031 [P] [US3] Prueba `test/feature/library/AlphabetTest.kt`: `firstIndexBySection` con «Árbol», «avión», «Ñandú», «Nube», «9 crímenes» → A, N, Ñ y # en su primer índice
- [X] T032 [US3] Crear `rola/feature/playlists/AddSongsSheet.kt` (SCR-33, 34:1180 / 51:3731; `testTag("hoja_agregar")`): `ModalBottomSheet` con título «Agregar canciones», buscador (`ic_search`, «Buscar en tu biblioteca», `surfaceContainer`, píldora), `LazyColumn` (claves = id) con portada 48 dp, título, artista y casilla (lima con `ic_check` marcada; `toggleable` con el título como descripción), `AlphabetBar` a la derecha (oculta si no hay resultados o hay búsqueda), «Sin resultados», y botón fijo «Agregar N canciones» (plural; desactivado con 0). Filtro con `SearchText` en `derivedStateOf`; consulta e ids marcados con `rememberSaveable`; las de `already` marcadas y desactivadas; `onAdd` entrega los ids marcados en el orden de la hoja (por título), no en el orden en que se tocaron (FR-017); letra activa con `derivedStateOf` de la primera visible; saltar con `scrollToItem`
- [X] T033 [US3] En `rola/feature/library/LibraryViewModel.kt`: `playlists`, `favoritesSummary`, `suspend createPlaylist(name): Long`, `addToPlaylist(id, ids)` que devuelve cuántas entraron. En `RolaNavHost.kt`: «Nueva lista» abre el diálogo; al crear, navegar a `lista/{id}?agregar=true`; `PlaylistScreen` abre la hoja si `agregar` (una vez, guardado) o al tocar Agregar / «Agregar canciones»; al confirmar, `addToPlaylist` y snackbar «Se agregaron N canciones a X»; cerrar sin confirmar no agrega
- [X] T034 [US3] Prueba `androidTest/feature/playlists/PlaylistScreenTest.kt` (segunda parte): lista vacía muestra «Agregar canciones» y al tocarlo llama a su acción; la hoja con «cancion» filtra «Canción»; marcar 2 cambia el botón a «Agregar 2 canciones»; con 0 está desactivado; marcar «Brillo» y luego «Agua» entrega [Agua, Brillo]
- [X] T035 [US3] Commit `feat(playlists): listas, Favoritas y hoja para agregar canciones`

**Punto de control**: se crean y llenan listas; Favoritas fija arriba.

---

## Fase 6: Historia 4 · Usar una lista (P2)

**Meta**: reproducir, aleatorio y quitar con Deshacer.

**Prueba independiente**: quitar una canción desde ⋮ y Deshacer la devuelve a su lugar.

- [X] T036 [US4] En `PlaylistScreen.kt`: ⋮ y pulsación larga en cada fila abren `SongMenu` con `MenuPlace.Playlist` (Fase 7); «Quitar de la lista» → `LibraryViewModel.removeFromPlaylist(id, mediaId)` (en Favoritas, `setFavorite(false)`) y snackbar «Quitaste X» con «Deshacer» (`restore` de la fila o volver a marcar favorita); «Añadir a la cola» → `player.addToQueue`
- [X] T037 [US4] Comprobar en emulador Reproducir/Aleatorio/tocar fila (cola = lista) y que quitar la última muestra la lista vacía

---

## Fase 7: Historia 5 · Menú de opciones de una canción (P2)

**Meta**: SCR-54 en Biblioteca · Canciones y en las listas.

**Prueba independiente**: el menú muestra 4 opciones en Biblioteca y 2 en una lista.

- [X] T038 [US5] Crear `rola/feature/library/SongMenu.kt` (SCR-54, 35:1458 / 51:4249; `testTag("menu_cancion")`): `DropdownMenu` M3 con la forma y el fondo de Figma; `Library`: Añadir a la cola (`ic_queue_add`), Añadir a una lista (`ic_playlist_add`), Favorita / Quitar de favoritas (`ic_favorite`/`ic_favorite_filled`), separador y Borrar del dispositivo (`ic_delete`); `Playlist`: Quitar de la lista (`ic_delete`) y Añadir a la cola
- [X] T039 [US5] En `LibraryScreen.kt` (`SongRow`): `IconButton` ⋮ de 48 dp con «Opciones de X» y `combinedClickable(onLongClick)`; el menú se ancla a la fila; acciones hacia arriba con `onSongAction(song, SongAction)`
- [X] T040 [US5] Crear `rola/feature/playlists/ChoosePlaylistSheet.kt` (research §8): `ModalBottomSheet` con «Nueva lista» y las listas (fila de `PlaylistsTab` sin Favoritas)
- [X] T041 [US5] En `RolaNavHost.kt`: resolver `SongAction`: cola → `player.addToQueue` + «Añadida a la cola»; lista → `ChoosePlaylistSheet` → `addToPlaylist` → «Añadida a X» o «Ya estaba en X» (si devolvió 0); «Nueva lista» → diálogo y agregar al crear; favorita → `setFavorite` + «Añadida a Favoritas» / «Quitada de Favoritas»
- [X] T042 [US5] Prueba `androidTest/feature/library/SongMenuTest.kt`: `Library` muestra las 4 opciones y «Quitar de favoritas» si `favorite`; `Playlist` muestra solo «Quitar de la lista» y «Añadir a la cola»; tocar una opción entrega su `SongAction` y cierra
- [X] T043 [US5] Commit `feat(library): menú de opciones`

---

## Fase 8: Historia 6 · Borrar una canción del dispositivo (P3)

**Meta**: aviso del sistema y limpieza de favoritas, listas y cola (DEC-08, INV-02).

**Prueba independiente**: con un archivo de prueba en Favoritas, en una lista y en la cola, aceptar el aviso lo quita de todo.

- [X] T044 [US6] En `RolaNavHost.kt`: `rememberLauncherForActivityResult(StartIntentSenderForResult())`; «Borrar del dispositivo» → `MediaStore.createDeleteRequest(resolver, listOf(song.uri))` y lanzar su `intentSender` (sin diálogo propio). `RESULT_OK` → en IO comprobar que el id ya no está en el MediaStore; si no está: `library.forgetSong(id)`, `player.removeSong(id)` y «Se borró X»; si sigue o hay excepción: «No se pudo borrar X». `RESULT_CANCELED` → nada. La canción pendiente se guarda con `rememberSaveable` para sobrevivir a la recreación
- [X] T045 [US6] Comprobar en emulador con un archivo de prueba (quickstart pasos 12–13); commit junto con la Fase 7 o `feat(library): borrar del dispositivo`

---

## Fase 9: Cierre

- [X] T046 Vistas previas `@Preview` en oscuro y claro de `PlaylistsTab`, `PlaylistScreen` (vacía y con canciones), `AddSongsSheet`, `SongMenu`, `CreatePlaylistDialog` y `FavoriteButton`
- [X] T047 `design-critique` de SCR-23, SCR-30, SCR-31, SCR-32, SCR-33, SCR-51 (corazón) y SCR-54 contra Figma (oscuro y claro); corregir diferencias de contenido y anotar las demás
- [X] T048 `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y `:app:connectedDebugAndroidTest`
- [ ] T049 SC-007: con 2 000 canciones (copias de un archivo corto en `/sdcard/Music/` del emulador, borradas al terminar) en la compilación `perfil`, abrir la hoja de agregar, buscar y desplazar; anotar en el reporte si hay trabas visibles (y, si el Redmi está en adb, repetir allí)
- [X] T050 Recorrido de quickstart.md en emulador y capturas `t05-favorita.png`, `t05-listas.png`, `t05-agregar.png`, `t05-menu.png` en `ai/handoff/capturas/`
- [X] T051 `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`; reporte y estado «esperando revisión»

---

## Dependencias

- Fase 1 → Fase 2 → historias.
- US1 (corazón) depende solo de la base.
- US2 depende de la base; US3 usa `PlaylistScreen` de US2.
- US4 y US5 comparten `SongMenu` (T038): hacer T038 antes de T036.
- US6 depende del menú (US5).
- Cierre al final.

## Paralelo

- Fase 1: T002 y T003 junto con T001.
- Fase 2: T007/T008 en paralelo; T010/T011 y T013 en paralelo con los DAO.
- US3: T030/T031 en paralelo con T029.

## Estrategia

MVP = Fases 1–3 (corazón y persistencia). Después US2+US3 (listas), US5+US4 (menú), US6 (borrar). Cada fase termina con su commit y sus pruebas en verde.

## Notas de implementación

- T007/T008 quedaron en un solo archivo `androidTest/core/database/RolaDatabaseTest.kt`.
- T026 (`PlaylistViewModel`) no se creó: el detalle se lee del repositorio con `collectAsStateWithLifecycle`; las acciones están en `LibraryViewModel`.
- `LibraryRepository.remove` devuelve la acción de deshacer (en vez de `removeFromPlaylist`/`restore`); `PlaylistSummary.covers` son canciones.
- T035/T043/T045: un solo commit `feat(playlists): …` (listas, menú y borrar dependen entre sí para compilar).
- T049: en el emulador la fluidez no se puede juzgar (render por software); pendiente en el Redmi.
