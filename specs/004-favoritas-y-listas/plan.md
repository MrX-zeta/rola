# Plan de implementación: Favoritas y Listas

**Carpeta**: `specs/004-favoritas-y-listas` | **Fecha**: 2026-11-18 | **Spec**: [spec.md](spec.md)

**Entrada**: especificación de `specs/004-favoritas-y-listas/spec.md` y restricciones del Paso 3 de la tarea 05.

## Resumen

Room (KSP) guarda favoritas, listas y sus canciones por `media_id` del MediaStore. Un `LibraryRepository` cruza esas filas con la lista de canciones (compartida en `AppContainer`) para omitir las que ya no existen. En Reproduciendo, un corazón sobre la portada marca la canción actual con rebote, ráfaga de partículas y háptica. Biblioteca gana los chips «Listas» y «Canciones»; Listas muestra «Nueva lista», Favoritas fija y las listas del usuario. Favoritas y cada lista son destinos del `NavHost` (SCR-31/SCR-32) con la hoja de agregar (SCR-33). Un menú compartido (SCR-54) da cola, lista, favorita y borrar del dispositivo con `createDeleteRequest`, que al confirmarse limpia favoritas, listas y cola.

## Contexto técnico

**Lenguaje/versión**: Kotlin 2.4.21 (Kotlin integrado de AGP 9.5.1), Java 11 de destino.

**Dependencias principales**: las instaladas (Compose BOM 2026.09.00, Material 3, Navigation, Lifecycle, Media3, Coil, Palette, `reorderable`, DataStore) y **las permitidas por la tarea**: `androidx.room:room-runtime`, `room-ktx`, `room-compiler` 2.8.5 y el plugin `com.google.devtools.ksp` 2.3.14 ([research.md](research.md) §1). Ninguna llama a la red.

**Almacenamiento**: Room `rola.db` v1 con esquema exportado en `app/schemas/` ([data-model.md](data-model.md)).

**Pruebas**: JUnit 4 en la JVM (`SearchText`, `PlaylistName`, armado de resúmenes, abecedario). En emulador (`androidTest`): DAO con base en memoria (insertar, quitar, orden, cascada, `forgetSong`), `LibraryRepository` (omitir inexistentes, orden de Favoritas) y Compose (corazón y reducir movimiento, chips, Favoritas fija, lista vacía, menú según el lugar).

**Plataforma**: Android 11 (API 30) a 17 (API 37). Emulador Pixel 7 Pro; Redmi Note 13 Pro+ para `perfil` si aparece en adb.

**Tipo de proyecto**: app móvil de un solo módulo `app`.

**Metas de rendimiento**: la ráfaga y el rebote leen sus `Animatable` en `graphicsLayer`/`drawBehind` (sin recomponer); la hoja filtra 2 000 canciones sin trabas (filtro en `derivedStateOf`, claves estables en `LazyColumn`); una sola consulta al MediaStore compartida por todas las pantallas.

**Restricciones**: sin red (INV-01); nunca se borra sin el aviso del sistema (INV-02); tokens del tema (INV-04); todo plano (DEC-27); accesibilidad (INV-06); la música no se corta (INV-11); movimiento DEC-28 con `LocalReduceMotion`.

**Escala**: 1 base de datos con 3 tablas, 1 repositorio, 4 pantallas/hojas nuevas (Listas, Lista, hoja de agregar, selector de lista), 1 diálogo, 1 menú, 1 botón animado, 2 funciones nuevas del reproductor.

## Revisión de la constitución

*Puerta: antes de la Fase 0 y otra vez después de la Fase 1.*

| Artículo | Cómo se cumple | Estado |
|---|---|---|
| I. El grafo manda (INV-08, INV-09) | DEC-29, DEC-06, DEC-07, DEC-08, DEC-05 y las tablas del grafo deciden; Figma decide lo visual. Diferencias de Figma (menú sin «Favorita», 4 chips, ⋮ en listas y en la lista) y huecos (selector de lista sin nodo) van al reporte, no se improvisan. | ✅ |
| II. Sin conexión (INV-01) | Room es local; ninguna dependencia nueva usa red. | ✅ |
| III. Datos y secretos (INV-02, INV-03) | Borrar solo con `createDeleteRequest`; sin diálogo propio. Las filas solo se borran tras un borrado confirmado. Sin secretos. | ✅ |
| IV. Tokens (INV-04, DEC-27) | Colores, formas y tipos del tema; lima = `RolaTheme.colors.accent`; círculo del corazón con `scrim` del tema. Sin relieve en ninguna de estas pantallas. | ✅ |
| V. Retroalimentación (INV-05, DEC-09) | Snackbar en crear, agregar, quitar (con Deshacer), cola, favorita desde el menú y borrar; estados vacíos con acción. | ✅ |
| VI. Accesibilidad (INV-06) | Corazón como interruptor «Favorita»; ⋮ con descripción; casillas con el título; objetivos ≥ 48 dp; textos en `sp`. Abecedario: letras pequeñas expuestas como un solo control con acciones (research §9; se anota en el reporte). | ✅ con nota |
| VII. La música no se interrumpe (INV-11) | Añadir a la cola usa `addMediaItem`; borrar la canción actual pasa a la siguiente. | ✅ |
| VIII. Arquitectura (DEC-20) | `core/database` (Room), `core/data` (repositorio), `feature/playlists`, `feature/player` (corazón), `feature/library` (chips, menú, abecedario). VM exponen `StateFlow`; la interfaz manda eventos. | ✅ |
| IX. Pruebas (DEC-22) | Las de la sección Pruebas. | ✅ |
| X. Código mínimo | Un repositorio en vez de uno por tabla; un menú con un parámetro de lugar; sin casos de uso ni mapeadores extra; reuso de `Cover`, `TitleOrder`, `SnackbarHost` y el `NavHost` existentes. | ✅ |
| XI. Español | Textos en `strings.xml` (plurales para «N canciones»); commits en español. | ✅ |

**Revisión después del diseño**: sin cambios; ninguna violación que justificar.

## Estructura del proyecto

### Documentación (esta función)

```text
specs/004-favoritas-y-listas/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/interfaces.md
├── checklists/requirements.md
└── tasks.md            # lo crea /speckit-tasks
```

### Código fuente

```text
gradle/libs.versions.toml           # + room 2.8.5 (runtime, ktx, compiler), plugin ksp 2.3.14
build.gradle.kts                    # + alias(libs.plugins.ksp) apply false
app/build.gradle.kts                # + plugin ksp, room.schemaLocation, dependencias de Room
app/schemas/                        # esquema v1 exportado

app/src/main/java/com/luis/rola/
├── AppContainer.kt                 # + base de datos, canciones compartidas (shareIn), LibraryRepository
├── core/
│   ├── database/
│   │   ├── RolaDatabase.kt         # @Database v1
│   │   ├── Entities.kt             # FavoriteEntity, PlaylistEntity, PlaylistSongEntity
│   │   ├── FavoriteDao.kt
│   │   └── PlaylistDao.kt          # incluye append y forgetSong transaccionales
│   ├── data/
│   │   ├── LibraryRepository.kt    # cruza filas con canciones; resúmenes y detalle
│   │   ├── SearchText.kt           # buscador sin acentos
│   │   ├── PlaylistName.kt         # validación del nombre
│   │   └── TitleOrder.kt           # comparte sinAcentos
│   └── media/PlayerConnection.kt   # + addToQueue, removeSong
├── feature/
│   ├── library/
│   │   ├── LibraryScreen.kt        # chips Listas/Canciones; ⋮ y pulsación larga en filas
│   │   ├── LibraryViewModel.kt     # + favoritas, listas y acciones compartidas
│   │   ├── SongMenu.kt             # SCR-54
│   │   └── AlphabetBar.kt          # DEC-05
│   ├── playlists/
│   │   ├── PlaylistsTab.kt         # SCR-23
│   │   ├── CreatePlaylistDialog.kt # SCR-30
│   │   ├── PlaylistScreen.kt       # SCR-31 y SCR-32 (también Favoritas)
│   │   ├── PlaylistViewModel.kt
│   │   ├── PlaylistCover.kt        # mosaico 2×2
│   │   ├── AddSongsSheet.kt        # SCR-33
│   │   └── ChoosePlaylistSheet.kt  # «Añadir a una lista»
│   └── player/
│       ├── FavoriteButton.kt       # corazón animado (DEC-29)
│       └── NowPlayingScreen.kt     # + corazón sobre la portada
└── navigation/
    ├── RolaNavHost.kt              # rutas favoritas y lista/{id}; borrar con createDeleteRequest; avisos
    └── RolaNavBar.kt               # Biblioteca activa también en rutas hijas

app/src/main/res/drawable/          # ic_favorite, ic_favorite_filled, ic_more_vert, ic_queue_add, ic_playlist_add, ic_add, ic_arrow_back, ic_playlist (de Figma)
app/src/main/res/values/strings.xml # textos y plurales nuevos
app/src/test/java/com/luis/rola/core/data/{SearchTextTest,PlaylistNameTest,LibraryRepositorySummaryTest}.kt
app/src/test/java/com/luis/rola/feature/library/AlphabetTest.kt
app/src/androidTest/java/com/luis/rola/core/database/{FavoriteDaoTest,PlaylistDaoTest}.kt
app/src/androidTest/java/com/luis/rola/core/data/LibraryRepositoryTest.kt
app/src/androidTest/java/com/luis/rola/feature/player/FavoriteButtonTest.kt
app/src/androidTest/java/com/luis/rola/feature/playlists/{PlaylistsTabTest,PlaylistScreenTest}.kt
app/src/androidTest/java/com/luis/rola/feature/library/SongMenuTest.kt
```

**Decisión de estructura**: la raíz de Biblioteca sigue fuera del `NavHost` (DEC-28 `biblioteca_fuera_del_navhost`); Favoritas y las listas son pantallas hijas dentro del `NavHost`. Las acciones que tocan varias cosas (borrar, cola, favorita) se resuelven en `RolaNavHost`, que ya tiene el reproductor, el `SnackbarHost` y el lanzador de resultados.

## Orden de trabajo (para /speckit-tasks)

1. Gradle + `core/database` + pruebas de DAO → commit `feat(database): …`.
2. `LibraryRepository`, `SearchText`, `PlaylistName`, canciones compartidas, `PlayerConnection.addToQueue/removeSong` + pruebas.
3. Corazón (`animate`) en Reproduciendo + pruebas → `feat(player): corazón de favoritas`.
4. Chips, Listas, diálogo, pantalla de lista, hoja de agregar, abecedario + pruebas → `feat(playlists): …`.
5. Menú de opciones, selector de lista, borrar del dispositivo + pruebas → `feat(library): menú de opciones`.
6. `design-critique` de SCR-23, SCR-30–33, SCR-51 y SCR-54; comprobaciones del Paso 7.

## Seguimiento de complejidad

Sin violaciones de la constitución. Nota de INV-06 sobre el abecedario en research §9.
