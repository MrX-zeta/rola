# Tarea 04 · Cola sin fugas (librería) y onboarding completo

**Estado:** esperando revisión
**Escrita:** 2026-11-15 · sesión de diseño
**Rama:** `development`. En worktree: actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.7.0 · **Spec Kit:** Parte A, corrección sobre `specs/002-reproductor-completo/` con `/speckit-converge`; Parte B, función nueva en `specs/003-onboarding/`.
**Ponytail:** full en la Parte A; off del Paso B1 al B5; full desde el Paso B6.
**Dispositivo:** emulador; si el Redmi aparece en adb, instálale `perfil` al final.

## Paso 0 · Comprobación
- `git log --oneline -3` incluye `docs: sesión de diseño tarea 04`. Si no, detente.

---
## Parte A · Cola sin fugas
### Lo que se ve en el video de Brian (`ai/handoff/capturas/t04-bug-cola.mp4`, Redmi, cola de ~40, lista desplazada)
- Arrastra «Call Out My Name» hacia arriba. El autodesplazamiento sube y aparecen filas anteriores (Big in Japan, Black Sabbath, Bloodsport, Brillas…). La fila llega arriba, «hace como que la ordena» y al soltar **regresa a su lugar original** (entre «Bueno es Alabar» y «Can You Hear The Music»).
- Ocurre al acercarse a la zona de «Sonando ahora» (debajo del encabezado «Cola»).
- Hipótesis de la sesión de diseño: cuando la fila arrastrada pasa a un índice fuera de la ventana compuesta (por el autodesplazamiento y `requestScrollToItem`), `LazyColumn` la desecha, el gesto se cancela y el movimiento no se aplica. También puede chocar con los elementos fijos («Sonando ahora», etiquetas) si el destino cae en ellos. Las pruebas pasaban porque no reproducen ese desecho.
### Paso A1 · Cambiar a la librería
- El arreglo propio falló dos veces. Usa **`sh.calvin.reorderable:reorderable`** (Apache 2.0, sin red, versión estable más nueva): mantiene compuesta la fila arrastrada y maneja autodesplazamiento y bordes. Quita el código propio de arrastre que ya no haga falta.
- Solo las filas de «A continuación» se reordenan; «Sonando ahora» y las etiquetas no son destinos válidos: si se suelta encima, la fila queda en el primer lugar de «A continuación».
- Si la cola cambia a mitad del arrastre (termina la canción), el arrastre sigue y el movimiento se aplica sobre la cola nueva (DEC-24); quita el `shortcut:` correspondiente.
- Conserva tocar para reproducir, quitar deslizando con «Deshacer», «Limpiar» y las acciones de accesibilidad «Subir», «Bajar», «Quitar de la cola».
- Actualiza FR-051 (marquesina: «mientras suena, 2 s entre pasadas») y agrega el requisito de la cola que cambia a mitad del arrastre. `/speckit-converge`.
- Prueba en el emulador **reproduciendo el video**: cola de ≥ 40, lista desplazada a la mitad, arrastrar una fila hasta arriba del todo y soltar. Graba `t04-cola-arriba.mp4` y `t04-cola-abajo.mp4`.
- Commits: `docs(spec): 002 cola con librería`, `fix(player): cola con reorderable`.

---
## Parte B · Onboarding completo
### Lectura obligatoria
- Grafo: DEC-18 (con la nota de onboarding), DEC-17, DEC-27 (relieve en el onboarding), DEC-28, KV-DATASTORE (`onboarding_completed`), INV-06.
- Pantallas: SCR-02, SCR-03, SCR-04 (onboarding 1–3), SCR-05 y SCR-06 (ya existen; ajusta a Figma: la fila de Notificaciones ya no está). Usa `figma-design-to-code`.
### Paso B1 · `/speckit-specify`
> La primera vez que abro Rola veo tres pantallas de bienvenida (qué es Rola, letras, sin conexión) que puedo pasar deslizando o con «Siguiente», con indicador de página y «Omitir»; la tercera dice «Empezar». Después me pide el permiso de música. Si ya vi la bienvenida, no vuelve a aparecer; si ya di el permiso, la app abre directo en Biblioteca.
### Paso B2 · `/speckit-clarify`
- El deslizar horizontal solo existe dentro del onboarding (no entre pestañas).
- La ilustración de la pantalla 3 (interruptor de LRCLIB) es decorativa; el ajuste real llega con Ajustes.
- Las ilustraciones se construyen con formas y vectores del tema (portadas genéricas, tarjeta de letra, wifi tachado), no con imágenes de discos reales.
- `onboarding_completed` se guarda en DataStore al pulsar «Empezar» u «Omitir».
- Con reducir movimiento: cambio de página con fundido corto, sin desplazamiento extra.
### Paso B3 · `/speckit-plan`
- `feature/onboarding` (pager con `HorizontalPager` de Compose), `core/data` (DataStore Preferences; dependencia permitida `androidx.datastore:datastore-preferences`), arranque en `MainActivity`/`RolaNavHost`.
- Relieve con `RolaRelief` donde Figma lo muestra (DEC-27).
- Movimiento (skill `animate`): indicador de página que se estira hacia la página nueva, ilustraciones con una entrada sutil al aparecer su página, botón principal sin saltos al cambiar a «Empezar».
### Paso B4–B5 · `/speckit-tasks` y `/speckit-analyze`
- Commit `docs(spec): 003 onboarding`. Detente y pide a Brian que pase Ponytail a **full**.
### Paso B6 · `/speckit-implement`
- Pruebas: JUnit del repositorio de preferencias (falso en memoria); Compose: «Omitir» guarda y lleva a Permisos, «Empezar» en la página 3, con reducir movimiento sin desplazamiento, y que con `onboarding_completed` y permiso la app abre en Biblioteca.
- `design-critique` de SCR-02 a SCR-06 contra Figma.

---
## Paso final · Comprobaciones y reporte
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y las de Compose.
- Capturas: `t04-onboarding-1.png`, `-2`, `-3`, `t04-permisos.png`.
- `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`. Reporte (Parte A y B) y estado «esperando revisión».
