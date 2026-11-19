# Tarea 06 · Biblioteca completa y pendientes de listas

**Estado:** esperando revisión
**Escrita:** 2026-11-18 · sesión de diseño
**Rama:** `development`. En worktree: actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.9.0 · **Spec Kit:** Parte A, corrección sobre `specs/004-favoritas-y-listas/` con `/speckit-converge`; Parte B, función nueva en `specs/005-biblioteca-completa/`.
**Ponytail:** full en la Parte A; off del Paso B1 al B5; full desde el Paso B6.
**Dispositivo:** emulador; si el Redmi aparece en adb, instálale `perfil` al final.

## Respuestas a la tarea 05
- Decisiones 1–10: **aprobadas**. Figma ya tiene «Favorita» en el menú (35:1458 / 51:4249) y la hoja «Añadir a una lista» como **SCR-55** (85:3348 / 85:3541): ajusta la tuya a ese diseño.
- Abecedario: excepción aceptada en INV-06.
- «Reproduciendo desde» debe mostrar el origen real (DEC-24).
- Tema claro: llega con Ajustes (tarea 08).

## Paso 0 · Comprobación
- `git log --oneline -3` incluye `docs: sesión de diseño tarea 06`. Si no, detente.

---
## Parte A · Pendientes de listas (Ponytail full)
1. **SCR-55:** ajusta la hoja «Añadir a una lista» a Figma (título, canción debajo, «Nueva lista» con ícono en `primaryContainer`, mosaicos de 48 dp).
2. **⋮ de cada lista** (Listas 32:1128 y pantalla de lista 34:1345): **Renombrar** (diálogo como Crear lista) y **Borrar lista** (diálogo de confirmación propio, porque no lo da Android; con «Deshacer» en el aviso). Favoritas no tiene ⋮.
3. **Reproduciendo (36:1465):** botón «Añadir a una lista» junto al título (abre SCR-55). El ⋮ de Reproduciendo queda para Letras (tarea 09): **no lo muestres todavía**.
4. **«Reproduciendo desde»:** Biblioteca, Favoritas o el nombre de la lista (y en la Parte B, álbum o artista).
- Agrega estos requisitos a la spec 004 y `/speckit-converge`. Commits: `docs(spec): 004 pendientes`, `feat(playlists): renombrar y borrar listas`, `feat(player): añadir a una lista y origen real`.

---
## Parte B · Biblioteca completa
### Lectura obligatoria
- Grafo: DEC-05 (orden, abecedario), DEC-26, DEC-28 (`biblioteca_fuera_del_navhost`: los detalles van **dentro** del NavHost), DEC-09, INV-06 (excepción del abecedario), KV-DATASTORE (`sort_order`).
- Pantallas: SCR-21 Álbumes, SCR-22 Artistas, SCR-24 Detalle de álbum, SCR-25 Detalle de artista, SCR-26 Hoja de filtros y orden (y sus `figma_nodo_claro`). Usa `figma-design-to-code`.
### Paso B1 · `/speckit-specify`
> En Biblioteca quiero los chips Listas, Álbumes, Artistas y Canciones. Álbumes en cuadrícula con portada, nombre, artista y año; Artistas en lista con foto redonda (la portada de uno de sus álbumes) y conteo. Al tocar un álbum veo su portada grande, datos, Reproducir/Aleatorio y sus canciones en orden de pista; al tocar un artista, sus álbumes en carrusel y sus canciones. Canciones, Álbumes y Artistas tienen abecedario. En Canciones, el botón de filtros abre una hoja para ordenar por título, artista o álbum, A→Z o Z→A, y se recuerda.
### Paso B2 · `/speckit-clarify`
- Orden de chips: Listas, Álbumes, Artistas, Canciones; Biblioteca sigue abriendo en Canciones.
- Álbumes y artistas salen del MediaStore (`ALBUM_ID`, `ARTIST`/`ALBUM_ARTIST`). Artista: «Artista desconocido» agrupa los vacíos. Canciones de un álbum por número de pista; si no hay, por título.
- Orden de Álbumes y Artistas: por nombre con las reglas de DEC-05. El orden elegido en la hoja aplica solo a Canciones y se guarda en `sort_order` (DataStore).
- Detalles con atrás del sistema y del botón; la raíz de Biblioteca sigue fuera del NavHost.
- La cascada de entrada (DEC-28) aplica a las cuadrículas y listas de cada chip al entrar.
### Paso B3 · `/speckit-plan`
- `core/data` (consultas de álbumes y artistas, orden), `feature/library` (chips, cuadrícula, detalles, hoja de orden), navegación de detalles en el NavHost.
- Portadas al tamaño en que se dibujan (como en la 03b); foto de artista = portada de su álbum más reciente.
- Rendimiento: probar con 2 000 canciones y ~200 álbumes; las cuadrículas no deben bloquear la entrada a Biblioteca (DEC-28).
### Paso B4–B5 · `/speckit-tasks` y `/speckit-analyze`
- Commit `docs(spec): 005 biblioteca completa`. Detente y pide a Brian que pase Ponytail a **full**.
### Paso B6 · `/speckit-implement`
- Pruebas: JUnit de agrupación de álbumes/artistas, orden de pistas, «Artista desconocido», orden de Canciones por cada criterio y dirección; Compose: chips cambian de contenido, detalle de álbum muestra sus canciones, hoja de orden guarda la elección.
- `design-critique` de SCR-21, 22, 24, 25, 26 y SCR-55.

---
## Paso final · Comprobaciones y reporte
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y las de Compose (todas, también tras cualquier arreglo).
- Capturas: `t06-albumes.png`, `t06-artistas.png`, `t06-album.png`, `t06-artista.png`, `t06-orden.png`, `t06-anadir-lista.png`.
- `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`. Reporte y estado «esperando revisión».
