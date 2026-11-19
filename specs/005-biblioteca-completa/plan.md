# Plan de implementación: Biblioteca completa

**Carpeta**: `specs/005-biblioteca-completa` | **Fecha**: 2026-11-19 | **Spec**: [spec.md](spec.md)

**Entrada**: especificación de `specs/005-biblioteca-completa/spec.md` y restricciones del Paso B3 de la tarea 06.

## Resumen

La consulta única al MediaStore (compartida en `AppContainer`) suma cuatro columnas a `Song`: identificador de álbum, artista del álbum, número de pista y año. Álbumes y artistas **no** se consultan aparte: se derivan en memoria de esa lista con funciones puras (`LibraryGrouping`), fuera del hilo principal. El orden de Canciones (`SongSort`: criterio + dirección) vive en DataStore (`sort_order`) y se aplica en `LibraryViewModel`. Biblioteca pasa a cuatro chips (Listas, Álbumes, Artistas, Canciones); Álbumes es una cuadrícula de 2 columnas y Artistas una lista con foto redonda; los dos llevan abecedario y la misma cascada de entrada que Canciones. Los detalles de álbum y artista son destinos del `NavHost`, con origen real en «Reproduciendo desde». La hoja de filtros y orden (SCR-26) cambia el orden y lo guarda.

## Contexto técnico

**Lenguaje/versión**: Kotlin 2.4.21 (Kotlin integrado de AGP 9.5.1), Java 11 de destino.

**Dependencias principales**: solo las instaladas (Compose BOM, Material 3, Navigation, Lifecycle, Media3, Coil, Palette, DataStore, Room). **Ninguna nueva** ([research.md](research.md) §1).

**Almacenamiento**: DataStore Preferences `rola_ajustes`, clave nueva `sort_order` (KV-DATASTORE). Room no cambia.

**Pruebas**: JUnit 4 en la JVM: agrupación de álbumes y artistas, orden de pistas (con disco), «Artista desconocido», álbum más reciente, `SongSort` por cada criterio y dirección, lectura de un `sort_order` inválido. En emulador (Compose): los chips cambian de contenido y van en orden, el detalle de álbum muestra sus canciones en orden de pista, la hoja de orden guarda la elección.

**Plataforma**: Android 11 (API 30) a 17 (API 37). Emulador Pixel 7 Pro; Redmi Note 13 Pro+ para `perfil`.

**Tipo de proyecto**: app móvil de un solo módulo `app`.

**Metas de rendimiento**: 2 000 canciones y ~200 álbumes. La agrupación es O(n) sobre la lista ya cargada y corre en `Dispatchers.Default`; cuadrícula y listas perezosas con claves estables; portadas pedidas al tamaño en que se dibujan (el `SongCoverFetcher` de la 03b ya lo hace). Biblioteca sigue fuera del NavHost y no se desmonta (DEC-28); el contenido de un chip solo se compone cuando está elegido, así que la cuadrícula no pesa al entrar a Biblioteca en Canciones.

**Restricciones**: sin red (INV-01); tokens del tema (INV-04); plano (DEC-27); accesibilidad con la excepción del abecedario (INV-06); la música no se corta salvo que se reproduzca otra cosa (INV-11); movimiento DEC-28 con `LocalReduceMotion`.

**Escala**: 4 campos nuevos en `Song`, 1 objeto de agrupación, 1 tipo de orden, 2 métodos en `PreferencesRepository`, 1 cuadrícula, 1 lista de artistas, 2 pantallas de detalle, 1 hoja, 2 rutas.

## Revisión de la constitución

*Puerta: antes de la Fase 0 y otra vez después de la Fase 1.*

| Artículo | Cómo se cumple | Estado |
|---|---|---|
| I. El grafo manda (INV-08, INV-09) | DEC-05, DEC-26, DEC-28, DEC-24 y KV-DATASTORE deciden; Figma decide lo visual (SCR-21/22/24/25/26). PEND-05 (artículos) no se asume. | ✅ |
| II. Sin conexión (INV-01) | Todo sale del MediaStore local; ninguna dependencia nueva. | ✅ |
| III. Datos y secretos | Solo lectura del MediaStore; `sort_order` es un ajuste local. Sin secretos. | ✅ |
| IV. Tokens (INV-04, DEC-27) | Colores, formas y tipos del tema; foto de artista con `CircleShape`; sin relieve. | ✅ |
| V. Retroalimentación (INV-05) | El orden elegido se ve al instante en la lista y en el resumen; esqueletos al cargar; el menú de canción mantiene sus avisos. | ✅ |
| VI. Accesibilidad (INV-06) | Celdas y filas con su nombre como descripción; Reproducir/Aleatorio/volver con descripción; opciones de la hoja como `RadioButton` con rol; abecedario con la excepción aceptada. | ✅ |
| VII. La música no se interrumpe (INV-11) | Solo Reproducir/Aleatorio/tocar canción cambian la cola. | ✅ |
| VIII. Arquitectura (DEC-20) | `core/data` (columnas, agrupación, orden, preferencias), `feature/library` (chips, cuadrícula, artistas, detalles, hoja), `navigation` (rutas). VM expone `StateFlow`. | ✅ |
| IX. Pruebas (DEC-22) | Las de «Pruebas». | ✅ |
| X. Código mínimo | Sin segunda consulta al MediaStore ni fetcher nuevo: la portada de un álbum es la de su primera canción (`Cover` existente). Se reusa `SongItem`, `AlphabetBar`, `TitleOrder`, la cascada de Canciones y el patrón de `PlaylistScreen`. | ✅ |
| XI. Español | Textos en `strings.xml` con plurales; commits en español. | ✅ |

**Revisión después del diseño**: sin cambios; ninguna violación que justificar.

## Estructura del proyecto

### Documentación (esta función)

```text
specs/005-biblioteca-completa/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/interfaces.md
├── checklists/requirements.md
└── tasks.md            # /speckit-tasks
```

### Código fuente (repositorio)

```text
app/src/main/java/com/luis/rola/
├── core/data/
│   ├── Song.kt                    # + albumId, albumArtist, track, year
│   ├── SongMapper.kt              # columnas nuevas
│   ├── SongRepository.kt          # proyección con ALBUM_ID, ALBUM_ARTIST, TRACK, YEAR
│   ├── LibraryGrouping.kt         # NUEVO: Album, Artist, albums(), artists(), orden de pistas
│   ├── SongSort.kt                # NUEVO: criterio + dirección, comparador, clave de sección
│   └── PreferencesRepository.kt   # + sortOrder / setSortOrder (sort_order)
├── feature/library/
│   ├── LibraryViewModel.kt        # + albums, artists, sortedSongs, sort
│   ├── LibraryScreen.kt           # 4 chips, abecedario en 3, cascada compartida, botón de filtros
│   ├── AlbumsTab.kt               # NUEVO: cuadrícula (SCR-21)
│   ├── ArtistsTab.kt              # NUEVO: lista (SCR-22)
│   ├── AlbumScreen.kt             # NUEVO: detalle de álbum (SCR-24)
│   ├── ArtistScreen.kt            # NUEVO: detalle de artista (SCR-25)
│   ├── SortSheet.kt               # NUEVO: hoja de filtros y orden (SCR-26)
│   └── AlphabetBar.kt             # + sentido inverso (Z→A)
└── navigation/RolaNavHost.kt      # rutas album/{id} y artista/{nombre}

app/src/test/java/com/luis/rola/core/data/
├── LibraryGroupingTest.kt         # NUEVO
├── SongSortTest.kt                # NUEVO
└── PreferencesRepositoryTest.kt   # + sort_order

app/src/androidTest/java/com/luis/rola/feature/library/
├── LibraryScreenTest.kt           # + chips y su contenido
└── AlbumArtistTest.kt             # NUEVO: detalle de álbum, hoja de orden
```

**Decisión de estructura**: se mantiene el módulo `app` y los paquetes de DEC-20; los álbumes y artistas son parte de Biblioteca (`feature/library`).

## Seguimiento de complejidad

Sin violaciones de la constitución.
