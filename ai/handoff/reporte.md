# Reporte · Tarea 06 · Biblioteca completa y pendientes de listas

**Estado:** esperando revisión.

## Paso 0 · Comprobación
- Worktree actualizado a `origin/development` (avance rápido). `git log` incluía `docs: sesión de diseño tarea 06`. ✅
- Skills disponibles: Spec Kit (con `/speckit-converge`), `figma-design-to-code`, `design-critique`, `ponytail-review`. No faltó ninguna.
- Ponytail: full en la Parte A; off del B1 al B5; full desde el B6. Brian hizo cada cambio.
- **Figma** respondió con error 500 por las dos conexiones mientras escribía la spec (B1–B3). En B6 ya funcionaba y leí los 5 nodos antes de construir.

## Parte A · Pendientes de listas
| Commit | Qué |
|---|---|
| `docs(spec): 004 pendientes` | FR-038 a FR-042 y aclaraciones en la spec 004; `/speckit-converge` agregó T052–T057 |
| `feat(playlists): renombrar y borrar listas` | ⋮ en cada lista de Listas y en la pantalla de lista (Favoritas sin ⋮); «Renombrar» reusa el diálogo de Crear lista (nombre actual, «Guardar»); «Borrar lista» con diálogo propio y aviso con «Deshacer» que restaura la lista, sus canciones en orden y sus fechas; borrar desde su pantalla vuelve a Biblioteca. Hoja «Añadir a una lista» según SCR-55 |
| `feat(player): añadir a una lista y origen real` | Botón «Añadir a una lista» junto al título de Reproduciendo (sin ⋮). «Reproduciendo desde» muestra el origen real; viaja en los metadatos de la cola de Media3 (`playlistMetadata`), sin estado nuevo |

## Parte B · Biblioteca completa (`specs/005-biblioteca-completa/`)
- **B1–B5:** spec (4 historias, 26 requisitos), 5 aclaraciones que vienen todas de la tarea (sin preguntas a Brian), plan, research, data-model, contratos, quickstart y 36 tareas. `/speckit-analyze`: sin problemas críticos y 100 % de cobertura. Commit `docs(spec): 005 biblioteca completa`.
- **B6:**

| Commit | Qué |
|---|---|
| `feat(data): álbumes, artistas y orden de canciones` | `Song` suma `albumId`, `albumArtist`, `track` (disco × 1000 + pista) y `year`. `LibraryGrouping` deriva álbumes y artistas de la misma consulta al MediaStore, sin segunda consulta. `SongSort` con criterio y dirección; `sort_order` en DataStore |
| `feat(library): álbumes, artistas, detalles y orden de canciones` | 4 chips (abre en Canciones; cada uno guarda su posición). Cuadrícula de álbumes, lista de artistas, detalles de álbum y de artista (rutas `album/{id}` y `artista?nombre=` dentro del NavHost; Biblioteca sigue fuera). Hoja de orden SCR-26. Abecedario en Canciones (según el criterio, invertido en Z→A), Álbumes y Artistas. Cascada de DEC-28 compartida por los 4 chips (`Cascada.kt`) |

- Origen real también para álbum y artista. Comprobado: «Reproduciendo desde · Ventanas».

## Decisiones que tomé (por favor revisa)
1. **Artista del álbum antes que el de la canción.** Un artista se agrupa por `ALBUM_ARTIST` y, si no hay, por `ARTIST`. Una canción con invitado (por ejemplo «Sal» de Club Sol en un álbum de Marea Alta) aparece dentro de Marea Alta, y Club Sol no sale como artista. Es la lectura de «`ARTIST`/`ALBUM_ARTIST`» de la tarea. ¿Se queda así?
2. **Desconocidos al final.** «Álbum desconocido» y «Artista desconocido» van en «#», al final de la lista (en Z→A, al principio). Lo mismo pasa con las canciones sin artista o sin álbum al ordenar por esos criterios.
3. **Sin relieve.** Las portadas de Álbumes, del detalle y del carrusel, y la foto de artista, traen sombra «Neumorfismo/Elevado» en Figma. DEC-27 deja Biblioteca plana y manda el grafo, así que van planas. Propongo quitar esas sombras de Figma.
4. **⋮ de los detalles.** Figma pone ⋮ en la barra del detalle de álbum y de artista, pero ni la tarea ni el grafo le dan opciones. No lo mostré. ¿Qué lleva?
5. **Número de pista.** El detalle muestra la posición (1, 2, 3…), no el número de la etiqueta. Si no, un álbum de dos discos diría 1001, 1002, 2001.
6. **Orden de las canciones de un artista:** por álbum (el más reciente primero) y luego por pista. El año de un álbum es el mayor de sus canciones.
7. **«Listo» de la hoja de orden** solo cierra. Cada opción se aplica y se guarda al tocarla (DEC-09: la lista de atrás cambia al momento).
8. **Origen al renombrar.** Si se renombra la lista que está sonando, «Reproduciendo desde» sigue con el nombre viejo hasta que se vuelva a reproducir.
9. **Análisis de la spec:** G1 (posición de cada chip) y G2 (vacíos y carga) ya los pedía la spec y están hechos. I1 (al desaparecer un detalle se «vuelve atrás», no «a Biblioteca») quedó así en el código; la frase de la spec dice «a Biblioteca».

## Dudas para la sesión de diseño
- **PEND-05** (ignorar «El», «La» y «The» al ordenar) sigue en «propuesto»: no lo asumí.
- **Duración del álbum menor de 1 min:** no se muestra «0 min». Pasa con los audios de prueba de 1 s.
- **Tamaños sin token exacto en Figma:**
  - títulos de sección «Álbumes» y «Canciones» del artista: Figma usa 18 y yo `titleLarge` (22);
  - carrusel: Figma usa radio 18 y yo `medium` (20);
  - nombres en celdas y filas: Figma usa 15 y yo `titleSmall` (14), como en el resto de la app.

  ¿Se agregan tokens o se acepta?

## design-critique (capturas del emulador contra Figma, oscuro)
Los audios de prueba no traen carátula, así que todas las portadas salen con la nota genérica.

| Pantalla | Diferencias |
|---|---|
| SCR-21 Álbumes | Coincide la cuadrícula: 2 columnas, márgenes 20/36, separación 16/20, «Artista · año» y abecedario con la letra activa. Las portadas van planas (decisión 3). Aparece un álbum «Music» con los WAV sueltos de la tarea 05. |
| SCR-22 Artistas | Coincide: foto redonda de 56, «N canciones · N álbumes», chevron y abecedario. «Artista desconocido» va al final. |
| SCR-24 Álbum | Coincide: portada de 200, título centrado, datos, Reproducir lima y Aleatorio, filas con número en mono, duración y ⋮. No hay ⋮ en la barra (decisión 4) ni «N min» cuando dura menos de 1 min. |
| SCR-25 Artista | Coincide: foto de 140, datos, botones, carrusel y canciones con el álbum como segunda línea. Diferencias: títulos de sección en 22 en lugar de 18 y radio del carrusel 20 en lugar de 18. |
| SCR-26 Hoja de orden | Coincide: «Ordenar y filtrar», radios lima, selector segmentado A → Z / Z → A y «Listo». |
| SCR-55 Añadir a una lista | Coincide: título, «Canción · Artista», «Nueva lista» en `primaryContainer` y mosaicos de 48. El mosaico de «Noche» sale con un solo recuadro porque tiene 1 canción. |
| Accesibilidad | Chips con 48 dp de toque y rol de pestaña. Las opciones de la hoja son radios con rol; el segmentado también. Volver, Reproducir, Aleatorio y filtros tienen descripción. El abecedario sigue la excepción de INV-06. |

## Comprobaciones
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug`: **BUILD SUCCESSFUL**. Lint: 0 errores y 6 avisos. 2 ya existían (`VectorRaster`, `ExportedService`) y 4 son versiones más nuevas de Lifecycle y del BOM de Compose, que no toqué.
- **Pruebas:**
  - **Unitarias: 65, todas pasan.** Las nuevas: `LibraryGroupingTest` (6), `SongSortTest` (5), columnas nuevas en `SongMapperTest` y `sort_order` en `PreferencesRepositoryTest`.
  - **Compose en el emulador: 62, todas pasan.** Las nuevas: menú de lista, renombrar, hoja SCR-55, botón y origen en Reproduciendo, restaurar una lista en Room, chips y su contenido, detalle de álbum en orden de pista, y la hoja de orden que guarda la elección.
  - Corrí las 62 completas después del último cambio de código.
- **Recorrido manual** en el emulador con 12 MP3 etiquetados (`/sdcard/Music/t06`, generados por mí):
  - chips ✅
  - Álbumes ✅
  - Artistas ✅
  - detalle de artista y su carrusel ✅
  - detalle de álbum con el disco 1 antes del 2 ✅
  - Reproducir → «Reproduciendo desde · Ventanas» ✅
  - «Añadir a una lista» y «Nueva lista» desde Reproduciendo ✅
  - orden «Artista Z→A» que se conserva al reiniciar la app, con el abecedario invertido ✅
- **Rendimiento (SC-004):**
  - Cargué 2 025 canciones en 200 álbumes y 60 artistas y probé con `perfil`.
  - Funciona: entra, cambia de chip, desplaza la cuadrícula y salta con el abecedario.
  - Pero el emulador dibuja por software (`ro.hardware.egl=emulation`, mediana de 150 ms por cuadro), igual que en la tarea 05. **La fluidez no se puede juzgar aquí; falta probarla en el Redmi.**
  - Ya borré los archivos de carga del emulador.
- **Redmi:** estaba en adb al empezar, pero al final ya no aparecía, así que **no se instaló `perfil`**.
- **Emulador:**
  - Quedan los 13 WAV de la tarea 05 y los 12 MP3 de `t06`. El orden quedó en Artista Z→A en `perfil`.
  - Se colgó una vez: había 2 085 procesos `adb` atascados. Los cerré y Brian hizo el arranque en frío.
  - `connectedDebugAndroidTest` desinstala la app al terminar, por eso la bienvenida vuelve a salir después de las pruebas. No es un error de la app.

## Capturas (`ai/handoff/capturas/`)
`t06-albumes.png`, `t06-artistas.png`, `t06-album.png`, `t06-artista.png`, `t06-orden.png`, `t06-anadir-lista.png`.

## Ponytail
Corrí `/ponytail:ponytail-review` antes de cada commit de código. Encontró y se corrigió:
1. Los diálogos de renombrar y borrar lista se cerraban al rotar (`remember` → `rememberSaveable`).
2. `research.md` decía otro orden para los desconocidos; se corrigió el texto.

## Git
Rama del worktree subida a `origin development` (avance rápido). Sin autoría de IA en los mensajes.
