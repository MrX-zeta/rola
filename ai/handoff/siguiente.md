# Tarea 07 · Inicio y Buscar

**Estado:** lista
**Escrita:** 2026-11-19 · sesión de diseño
**Rama:** `development`. En worktree: actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.10.0 · **Spec Kit:** Parte A, corrección sobre `specs/005-biblioteca-completa/` con `/speckit-converge`; Parte B, función nueva en `specs/006-inicio-y-buscar/`.
**Ponytail:** full en la Parte A; off del Paso B1 al B5; full desde el Paso B6.
**Dispositivo:** emulador; si el Redmi aparece en adb, instálale `perfil` al final.

## Respuestas a la tarea 06
1. Agrupar por artista del álbum: **se queda** (anotado en DEC-05).
2, 5, 6, 7, 8: **aprobadas**.
3. Sin relieve: **correcto**; ya quité las sombras de Figma.
4. ⋮ de los detalles: lleva **«Añadir a la cola»** y **«Añadir a una lista»** (todas las canciones del álbum o del artista), con el mismo estilo de menú que SCR-54. Va en la Parte A.
9. Corrige la frase de la spec: al desaparecer un detalle se «vuelve atrás».
- PEND-05: **resuelto, no** se ignoran artículos.
- Álbum de menos de 1 min: muestra «< 1 min».
- Tamaños sin token exacto: **se aceptan** los tokens actuales.

## Paso 0 · Comprobación
- `git log --oneline -3` incluye `docs: sesión de diseño tarea 07`. Si no, detente.

---
## Parte A · Pendientes de Biblioteca (Ponytail full)
- ⋮ en Detalle de álbum y Detalle de artista con las dos opciones; «Añadir a una lista» abre SCR-55 con todas las canciones (el aviso dice cuántas se agregaron).
- «< 1 min» y la frase de la spec. `/speckit-converge`.
- Commits: `docs(spec): 005 pendientes`, `feat(library): opciones de álbum y artista`.

---
## Parte B · Inicio y Buscar
### Lectura obligatoria
- Grafo: DEC-30 (Inicio), DEC-25 (Buscar, con su nota), DEC-03 (vuelve a abrir en Inicio), DEC-19, DEC-26, DEC-27 (relieve en SCR-12, SCR-40, SCR-41), DEC-28, DEC-09, TBL-HISTORY, OP-01, OP-09, OP-16.
- Pantallas: SCR-10 Inicio, SCR-11 Inicio · primera vez, SCR-12 Sin canciones, SCR-40 Buscar, SCR-41 Sin resultados (y sus `figma_nodo_claro`). Usa `figma-design-to-code`.
### Paso B1 · `/speckit-specify`
> Al abrir Rola veo Inicio: un saludo según la hora, lo que escuché hace poco en un carrusel, y «Para ti» con mi canción más escuchada lista para reproducir, un mosaico de Aleatorio con el total de canciones y otro de Favoritas. La primera vez, sin historial, me invita a reproducir algo o ir a Biblioteca. Si no hay canciones en el teléfono, me lo dice y puedo escanear de nuevo. En Buscar escribo y al instante veo canciones, álbumes, artistas y listas que coinciden, con la parte buscada resaltada; si no hay nada, me lo dice.
### Paso B2 · `/speckit-clarify`
- Historial (TBL-HISTORY): una reproducción cuenta cuando suena ≥ 30 s o termina si dura menos. «Escuchado hace poco»: últimas 10 canciones distintas, la más reciente primero. «Tu más escuchada»: más reproducciones (empate: la más reciente).
- Al borrar una canción del dispositivo, se borra su historial (DEC-08).
- Saludo: «Buenos días» (5–11:59), «Buenas tardes» (12–18:59), «Buenas noches» (19–4:59), con la fecha en español.
- «Ver todo» y el engrane de Ajustes **no se muestran** todavía (DEC-30).
- «Escanear de nuevo» (SCR-12): vuelve a consultar el MediaStore y pide al sistema escanear la carpeta Música con `MediaScannerConnection`; muestra progreso y resultado (DEC-09). «Abrir Ajustes» no se muestra hasta la tarea 08.
- Buscar: sin texto, solo el campo con ayuda; resultados al escribir (≈ 150 ms de espera), agrupados (Canciones, Álbumes, Artistas, Listas incluida Favoritas), máximo 5 por grupo con orden por relevancia (empieza con > contiene); misma normalización que `SearchText`. Mismo menú ⋮ que Biblioteca. El teclado se abre al entrar a Buscar.
- La app vuelve a abrir en Inicio (fin de lo temporal de DEC-03).
### Paso B3 · `/speckit-plan`
- `core/database`: tabla `play_history` con migración automática v1→v2 de Room (prueba de migración).
- `core/media`: registrar reproducciones desde el servicio.
- `feature/home`, `feature/search`. Inicio y Buscar dentro del NavHost; la raíz de Biblioteca sigue fuera.
- Relieve (`RolaRelief`) solo en SCR-12, SCR-40 y SCR-41 donde Figma lo muestra.
- Movimiento (`animate`): cascada al entrar en Inicio (carrusel y bento) y en los resultados de Buscar; los resultados nuevos no deben parpadear al escribir.
### Paso B4–B5 · `/speckit-tasks` y `/speckit-analyze`
- Commit `docs(spec): 006 inicio y buscar`. Detente y pide a Brian que pase Ponytail a **full**.
### Paso B6 · `/speckit-implement`
- Pruebas: JUnit de saludo por hora, regla de ≥ 30 s, últimas 10 distintas, más escuchada con empate, relevancia de búsqueda; migración de Room; Compose: Inicio primera vez vs con historial, sin canciones, Buscar agrupa y resalta, sin resultados.
- `design-critique` de SCR-10, 11, 12, 40, 41.

---
## Paso final · Comprobaciones y reporte
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y todas las de Compose tras el último cambio.
- Capturas: `t07-inicio.png`, `t07-inicio-primera-vez.png`, `t07-sin-canciones.png`, `t07-buscar.png`, `t07-sin-resultados.png`, `t07-opciones-album.png`.
- `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`. Reporte y estado «esperando revisión».
