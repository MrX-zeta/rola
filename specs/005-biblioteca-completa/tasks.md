---

description: "Tareas de implementación de Biblioteca completa"
---

# Tareas: Biblioteca completa

**Entrada**: `specs/005-biblioteca-completa/` (plan.md, spec.md, research.md, data-model.md, contracts/interfaces.md, quickstart.md)

**Pruebas**: obligatorias por DEC-22 y por la tarea 06 (Paso B6). JUnit: agrupación de álbumes y artistas, orden de pistas, «Artista desconocido», orden de Canciones por cada criterio y dirección. Compose: los chips cambian de contenido, el detalle de álbum muestra sus canciones, la hoja de orden guarda la elección. Cada prueba va junto a su código.

**Organización**: por historia de la spec (US1…US4). Commits atómicos por área: `feat(data): …`, `feat(library): …`.

## Formato: `[ID] [P?] [Historia] Descripción`

- **[P]**: puede hacerse en paralelo (archivos distintos, sin dependencias pendientes).
- **[USn]**: historia de la spec.

## Convenciones de rutas

- Código: `app/src/main/java/com/luis/rola/` (en las tareas, `rola/`).
- Unitarias: `app/src/test/java/com/luis/rola/` (`test/`).
- Emulador: `app/src/androidTest/java/com/luis/rola/` (`androidTest/`).
- Textos en `app/src/main/res/values/strings.xml` (plurales en `<plurals>`), en español. Colores, tipos, formas, espacios y movimiento solo desde `rola/core/designsystem` (INV-04, `RolaMotion`). Todo plano (DEC-27).
- Antes de construir cada pantalla: skill `figma-design-to-code` y `get_design_context` del nodo (oscuro y claro).

---

## Fase 1: Preparación

- [ ] T001 Leer en Figma (con `figma-design-to-code`) SCR-21 32:801/51:2577, SCR-22 32:961/51:2737, SCR-24 33:836/51:3035, SCR-25 33:950/51:3147 y SCR-26 33:1070/51:3267; anotar en `specs/005-biblioteca-completa/research.md` §10 medidas, tokens e íconos que falten en `res/drawable/`
- [ ] T002 [P] Agregar los textos nuevos (chips Álbumes y Artistas, «Álbum desconocido», conteos de álbumes y canciones, hoja de orden, resumen con el orden) en `app/src/main/res/values/strings.xml`

---

## Fase 2: Base (bloquea todas las historias)

- [ ] T003 Agregar a `Song` los campos `albumId: Long = 0`, `albumArtist: String? = null` («nulo si vacío o `<unknown>`»), `track: Int = 0` («0 = sin número; incluye disco × 1000») y `year: Int = 0` («0 = sin año») en `rola/core/data/Song.kt`, y leerlos en `rola/core/data/SongMapper.kt`
- [ ] T004 Agregar `ALBUM_ID`, `ALBUM_ARTIST`, `TRACK` y `YEAR` a la proyección de `MediaStoreSongRepository` en `rola/core/data/SongRepository.kt`
- [ ] T005 [P] Prueba de `songFrom` con las columnas nuevas (artista del álbum `<unknown>` → nulo; pista y año 0) en `test/core/data/SongMapperTest.kt` (crear o ampliar)
- [ ] T006 Extraer la cascada de entrada de `LibraryScreen` a un modificador compartido `Modifier.cascada(indice, primera, reloj)` con sus constantes (DEC-28 `despliegue_listas`, `ModulateAlpha`) en `rola/feature/library/Cascada.kt`, sin cambiar el comportamiento de Canciones
- [ ] T007 Pasar Biblioteca a 4 chips (Listas, Álbumes, Artistas, Canciones; abre en Canciones) con un `enum` de pestaña guardado al rotar y reinicio del reloj de cascada al cambiar de chip en `rola/feature/library/LibraryScreen.kt` (FR-001 a FR-003)
- [ ] T008 Hacer que `NavHost` trate las rutas `album/…` y `artista/…` como dentro de Biblioteca (píldora y vuelta a la raíz) en `rola/navigation/RolaNavHost.kt` (FR-004)

**Punto de control**: compila; Canciones se ve y se anima igual que antes; los chips Álbumes y Artistas existen (vacíos).

---

## Fase 3: Historia 1 · Recorrer mis álbumes (P1) 🎯 MVP

**Meta**: cuadrícula de álbumes y detalle con canciones en orden de pista.

**Prueba independiente**: tocar «Álbumes», abrir un álbum, ver el orden de pistas y reproducir.

- [ ] T009 [P] [US1] Prueba JUnit de `LibraryGrouping.albums`: un álbum por `albumId` (dos «Greatest Hits» distintos), nombre, artista (primer `albumArtist`, si no `artist` de la primera pista), año (el mayor), pistas por `track` con 0 al final y empate por título, disco 1 antes del 2, orden de la lista por nombre (DEC-05) en `test/core/data/LibraryGroupingTest.kt`
- [ ] T010 [US1] Crear `Album` y `LibraryGrouping.albums(songs)` según data-model.md en `rola/core/data/LibraryGrouping.kt`
- [ ] T011 [US1] Exponer `albums: StateFlow<List<Album>?>` (agrupado en `Dispatchers.Default`) en `rola/feature/library/LibraryViewModel.kt`
- [ ] T012 [US1] Crear `AlbumsTab` (SCR-21): `LazyVerticalGrid` de 2 columnas, celda con portada (`Cover` de la primera canción), nombre, artista y año, claves `albumId`, cascada, abecedario por nombre, estado vacío y esqueleto en `rola/feature/library/AlbumsTab.kt`
- [ ] T013 [US1] Crear `AlbumScreen` (SCR-24): volver, portada grande, nombre, artista, año, N canciones · duración, Reproducir (lima) y Aleatorio, canciones con `SongItem` y número de pista, en `rola/feature/library/AlbumScreen.kt`
- [ ] T014 [US1] Ruta `album/{id}` en `rola/navigation/RolaNavHost.kt`: abre desde `AlbumsTab`, reproduce con origen = nombre del álbum (DEC-24), vuelve atrás si el álbum desaparece (FR-008)
- [ ] T015 [P] [US1] Prueba Compose: el detalle de álbum muestra sus canciones en orden de pista y Reproducir/Aleatorio llaman a su acción en `androidTest/feature/library/AlbumArtistTest.kt`

**Punto de control**: US1 se prueba sola.

---

## Fase 4: Historia 2 · Recorrer mis artistas (P1)

**Meta**: lista de artistas con foto redonda y detalle con carrusel de álbumes y canciones.

**Prueba independiente**: tocar «Artistas», ver «Artista desconocido» una vez, abrir un artista y un álbum del carrusel.

- [ ] T016 [P] [US2] Prueba JUnit de `LibraryGrouping.artists`: clave `albumArtist ?: artist`, una sola entrada «Artista desconocido» (nombre nulo), álbumes del más reciente al más antiguo (empate por nombre), canciones por álbum y pista, foto = portada del álbum más reciente, orden por nombre en `test/core/data/LibraryGroupingTest.kt`
- [ ] T017 [US2] Crear `Artist` y `LibraryGrouping.artists(songs)` en `rola/core/data/LibraryGrouping.kt`
- [ ] T018 [US2] Exponer `artists: StateFlow<List<Artist>?>` en `rola/feature/library/LibraryViewModel.kt`
- [ ] T019 [US2] Crear `ArtistsTab` (SCR-22): filas con foto redonda (`Cover` con `CircleShape`), nombre y conteo de álbumes y canciones, cascada, abecedario por nombre en `rola/feature/library/ArtistsTab.kt`
- [ ] T020 [US2] Crear `ArtistScreen` (SCR-25): volver, foto, nombre, conteo, Reproducir/Aleatorio, carrusel `LazyRow` de álbumes y canciones con `SongItem` en `rola/feature/library/ArtistScreen.kt`
- [ ] T021 [US2] Ruta `artista/{nombre}` (`Uri.encode`, vacío = desconocido) en `rola/navigation/RolaNavHost.kt`: abre desde `ArtistsTab`, el carrusel abre `album/{id}`, reproduce con origen = nombre del artista, vuelve atrás si desaparece (FR-012)

**Punto de control**: US1 y US2 funcionan juntas.

---

## Fase 5: Historia 3 · Ordenar Canciones (P2)

**Meta**: hoja de orden que reordena Canciones y se recuerda.

**Prueba independiente**: elegir «Artista» y «Z→A», cerrar y reabrir la app.

- [ ] T022 [P] [US3] Prueba JUnit de `SongSort`: comparador por TITLE, ARTIST y ALBUM en A→Z y Z→A, empate por título, nulos como desconocidos, `sectionOf` por criterio, `key()`/`parse()` ida y vuelta y valor inválido → `TITLE_ASC` en `test/core/data/SongSortTest.kt`
- [ ] T023 [US3] Crear `SortBy` y `SongSort` («por defecto `TITLE`, `true`»; formato `"<BY>_<ASC|DESC>"`) en `rola/core/data/SongSort.kt`
- [ ] T024 [US3] Agregar `sortOrder: Flow<SongSort>` y `setSortOrder` (clave `sort_order`; archivo ilegible → `TITLE_ASC`; error al escribir no cierra la app) en `rola/core/data/PreferencesRepository.kt` y su prueba en `test/core/data/PreferencesRepositoryTest.kt`
- [ ] T025 [US3] Exponer `sort` y `sortedSongs` (combinando canciones y orden en `Dispatchers.Default`) y `setSort` en `rola/feature/library/LibraryViewModel.kt`; pasar `container.preferences` a la fábrica desde `rola/navigation/RolaNavHost.kt`; la hoja de agregar canciones sigue usando `songs` por título
- [ ] T026 [US3] Crear `SortSheet` (SCR-26): `ModalBottomSheet` con «Ordenar por» (Título, Artista, Álbum) y dirección (A→Z, Z→A) como opciones de radio, aplica al tocar, en `rola/feature/library/SortSheet.kt`
- [ ] T027 [US3] Botón de filtros en Canciones que abre `SortSheet`, resumen «N canciones · Criterio A–Z» y abecedario por el criterio elegido en `rola/feature/library/LibraryScreen.kt`
- [ ] T028 [P] [US3] Prueba Compose: la hoja marca el orden actual y al elegir otro llama a `onChange` con él en `androidTest/feature/library/AlbumArtistTest.kt`

**Punto de control**: el orden se conserva tras reiniciar la app.

---

## Fase 6: Historia 4 · Chips y abecedario (P2)

**Meta**: navegación entre chips y salto por letra en Canciones, Álbumes y Artistas.

**Prueba independiente**: cambiar de chip y tocar letras.

- [ ] T029 [US4] Agregar a `AlphabetBar` el sentido inverso (Z→A con «#» arriba) para Canciones en Z→A en `rola/feature/library/AlphabetBar.kt` (FR-017)
- [ ] T030 [US4] Abecedario en Canciones (criterio elegido), Álbumes y Artistas (nombre), con la letra visible iluminada al desplazarse, en `rola/feature/library/LibraryScreen.kt`, `AlbumsTab.kt` y `ArtistsTab.kt` (FR-017, FR-018)
- [ ] T031 [P] [US4] Prueba Compose: los chips van en orden Listas, Álbumes, Artistas, Canciones, abren en Canciones y cada uno cambia el contenido en `androidTest/feature/library/LibraryScreenTest.kt`

---

## Fase 7: Cierre

- [ ] T032 Rendimiento (SC-004): cargar 2 000 audios con ~200 álbumes, instalar `perfil` y comprobar entrada a Biblioteca y cambio de chips; si el Redmi está en adb, instalarle `perfil`
- [ ] T033 `.\gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y `connectedDebugAndroidTest` (todas, también tras cualquier arreglo)
- [ ] T034 Capturas `t06-albumes.png`, `t06-artistas.png`, `t06-album.png`, `t06-artista.png`, `t06-orden.png`, `t06-anadir-lista.png` en `ai/handoff/capturas/`
- [ ] T035 `design-critique` de SCR-21, 22, 24, 25, 26 y SCR-55 contra Figma; diferencias al reporte
- [ ] T036 `/ponytail:ponytail-review` antes de cada commit; `git log` sin autoría de IA; push a `origin development`; `ai/handoff/reporte.md` y estado «esperando revisión»

---

## Dependencias

- Fase 1 → Fase 2 → historias.
- US1 y US2 comparten `LibraryGrouping.kt` (T010 antes de T017) y la ruta de álbum (T014 antes de T021).
- US3 es independiente de US1/US2 (solo toca Canciones).
- US4 necesita las pestañas de US1 y US2 (T012, T019) y el orden de US3 (T023).
- Cierre al final.

## Paralelo

- T002 junto con T001.
- T005 junto con T006/T007.
- Las pruebas JUnit (T009, T016, T022) pueden escribirse en paralelo con la interfaz de su historia.
- US3 (T022–T028) en paralelo con US1/US2 si se trabaja por separado.

## Estrategia

MVP = Fases 1–3 (álbumes). Después US2 (artistas), US3 (orden) y US4 (abecedario en todas). Cada fase termina con su commit y sus pruebas en verde.
