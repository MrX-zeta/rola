# Tarea 02 · Reproductor base: escuchar tu música con Rola

**Estado:** lista
**Escrita:** 2026-11-14 · sesión de diseño
**Rama:** `development`. Si trabajas en un worktree, actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.3.0
**Primera tarea con Spec Kit.** Carpeta de la spec: `specs/001-reproductor-base/`.

## Objetivo
Al terminar, Brian instala Rola en su teléfono, concede el permiso de música, ve sus canciones ordenadas por título, toca una y suena en segundo plano con su notificación, y controla la música desde la mini tarjeta. Nada más.

## Antes de empezar (lo hace Brian)
- [ ] Commit y push de la sesión de diseño: `docs: sesión de diseño tarea 02` (grafo 1.3.0, esta tarea, prompt inicial).
- [ ] Plugin de Figma instalado y autenticado en Claude Code (`/plugin install figma@claude-plugins-official`, luego `/mcp` → figma → Authenticate).
- [ ] Ponytail en **off** del Paso 1 al Paso 5 (spec, aclaraciones, plan, tareas, análisis); en **full** desde el Paso 6.

## Lectura obligatoria
- Grafo: INV-01…INV-11; DEC-03 (navegación), DEC-04 (mini tarjeta), DEC-05 (orden), DEC-09 (retroalimentación), DEC-15 y PEND-08 (color de la portada), DEC-17 (splash), DEC-18 (permisos), DEC-20 y PEND-02 (arquitectura, inyección manual), DEC-22 (pruebas), DEC-26 (escaneo), DEC-27 (estilo), PEND-05 y PEND-07 (siguen sin aplicarse).
- Pantallas y nodos de Figma: SCR-05 Permisos, SCR-06 Permiso denegado, SCR-20 Biblioteca · Canciones, SCR-50 Mini reproductor (y sus `figma_nodo_claro`). Usa la skill `figma-design-to-code` antes de leerlas.
- `.specify/memory/constitution.md`.

## Paso 0 · Comprobación
- `git log --oneline -3` debe mostrar `docs: sesión de diseño tarea 02`. Si no, detente y avisa en el reporte.

## Paso 0.5 · Splash redondo en cualquier teléfono (DEC-17)
HyperOS dibuja cuadrado el fondo del ícono del splash. El círculo va dentro del ícono:
- Nuevo `res/drawable/splash_icono.xml` (vector 288 × 288 dp, viewport 288): círculo `@color/ic_launcher_background` centro (144,144) radio 80; encima un `group` con `scaleX/scaleY` 0.628 y `translateX` 63.616 / `translateY` 58.592 con los dos trazos del símbolo (viewBox 256 de `docs/marca/rola-simbolo-*.svg`): la «r» como trazo de 40 con extremos redondos y el disco con `fillType="evenOdd"`, ambos en un color nuevo `@color/marca_tinta` = `#10100E`.
- `Theme.Rola.Starting` hereda de `Theme.SplashScreen` (ya no de `IconBackground`), usa `splash_icono` y **sin** `windowSplashScreenIconBackgroundColor`.
- Brian lo verifica en su Redmi; tú en el emulador. Commit: `fix(marca): círculo del splash dentro del ícono`.

## Paso 1 · `/speckit-specify`
Descripción para la spec (en español, solo qué y por qué, sin tecnología):
> Como dueño de Rola quiero abrir la app, dar permiso a mi música y ver todas mis canciones ordenadas por título, tocar una y que suene aunque salga de la app o apague la pantalla, controlarla desde la notificación y desde una mini tarjeta encima de la barra de navegación (portada, título, artista, siguiente, reproducir/pausa y línea de progreso, con el color de la portada). Si niego el permiso, veo una pantalla que me explica y me lleva a Ajustes. Inicio y Buscar existen en la barra pero todavía muestran un aviso de «próximamente».
Incluye en la spec los ids SCR y DEC de esta tarea.

## Paso 2 · `/speckit-clarify`
- Responde las preguntas con el grafo. Lo que el grafo no responda: elige la opción más simple, márcala como **supuesto** en la spec y lístala en el reporte. No inventes funciones.
- Decisiones ya tomadas para esta tarea (ponlas en la spec si salen):
  - El permiso de música se pide al abrir la app (el onboarding llega después) con la pantalla SCR-05; si se niega, SCR-06. Notificaciones (Android 13+) se piden al reproducir por primera vez; si se niegan, la música suena igual.
  - Orden por título con reglas de DEC-05 (sin acentos para agrupar, Ñ propia, números y símbolos en «#»). Sin abecedario ni filtros todavía; sin ignorar artículos (PEND-05) ni duración mínima (PEND-07).
  - Biblioteca muestra solo el chip «Canciones»; el resto de chips llega después. Botón «Aleatorio» funcional; el de filtros y el menú ⋮ todavía no se muestran.
  - Al tocar una canción, la cola es la lista completa empezando por ella. Sin pantalla Reproduciendo ni gestos de la mini tarjeta todavía (llegan en la tarea 03): tocar la tarjeta no hace nada.
  - La mini tarjeta solo aparece si hay algo cargado.

## Paso 3 · `/speckit-plan`
Restricciones técnicas para el plan:
- Paquetes: `core/media` (servicio y conexión), `core/data` (lectura de MediaStore), `feature/library`, `feature/player` (mini tarjeta), `feature/onboarding` (permisos), `app` (navegación y `AppContainer`).
- **Inyección manual (PEND-02):** `RolaApp : Application` crea un `AppContainer` con los repositorios y la conexión al reproductor; los ViewModel los reciben por `ViewModelProvider.Factory`. Sin Hilt.
- **Reproducción:** `MediaSessionService` con ExoPlayer de Media3 (atributos de audio de música, foco de audio, pausa al desconectar audífonos). La interfaz se conecta con `MediaController`. Notificación del propio Media3 con el ícono monocromo. En el manifiesto: servicio con `foregroundServiceType="mediaPlayback"` y los permisos que Media3 necesita.
- **Biblioteca:** consulta a `MediaStore.Audio.Media` con `IS_MUSIC != 0`, expuesta como `Flow` que se actualiza con un `ContentObserver` (DEC-26). Permiso `READ_MEDIA_AUDIO` en 13+ y `READ_EXTERNAL_STORAGE` en 11–12.
- **Portadas:** `ContentResolver.loadThumbnail` con Coil (fetcher propio si hace falta). Sin portada: recuadro de tema con ícono de nota.
- **Color de la portada (PEND-08 A):** Palette sobre la portada para la mini tarjeta; si falla, colores del tema. Contraste del texto ≥ 4.5:1 (INV-06): si el color no alcanza, se ajusta su luminosidad.
- **Navegación:** navigation-compose con la píldora de tres destinos (DEC-03), sin deslizar entre pestañas; atrás del sistema según DEC-03.
- **Dependencias nuevas permitidas:** Media3 (exoplayer, session), navigation-compose, lifecycle-viewmodel-compose y runtime-compose, Coil 3, Palette. Ninguna otra sin justificar en el reporte. Versiones estables.
- Tokens del tema siempre (INV-04); estilo plano, el relieve no se usa en estas pantallas salvo SCR-05/SCR-06 donde Figma lo muestre (DEC-27).

## Paso 4 · `/speckit-tasks`
- Tareas pequeñas, cada una con su prueba cuando aplique.

## Paso 5 · `/speckit-analyze`
- Corrige lo que marque como inconsistente con la constitución o el grafo. Si algo no se puede corregir sin decidir, al reporte.
- Commit: `docs(spec): 001 reproductor base` (spec, plan, tareas).

## Paso 6 · `/speckit-implement` (Ponytail en full)
- Implementa por tareas, con commits atómicos por área (`feat(media): …`, `feat(library): …`, etc.).
- Pruebas mínimas (DEC-22):
  - JUnit: orden y normalización por título (acentos, Ñ, «#»), formato de duración, mapeo de filas de MediaStore a modelo, ajuste de contraste del color de la portada.
  - Compose: Biblioteca muestra la lista con un repositorio falso; la mini tarjeta no aparece sin canción cargada.
- `design-critique`: compara tus capturas de SCR-05, SCR-06, SCR-20 y SCR-50 contra Figma y anota diferencias.

## Paso 7 · Comprobaciones
- `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`. Resultados reales.
- En emulador: permiso concedido y negado, lista, reproducir, salir de la app y que siga sonando, controlar desde la notificación, apagar pantalla.
- Capturas: `t02-permisos.png`, `t02-permiso-denegado.png`, `t02-biblioteca.png`, `t02-mini-reproductor.png`, `t02-notificacion.png`.
- `/ponytail:ponytail-review` antes del último commit. `git log` sin autoría de IA. Push a `origin development`. Sin PR.

## Paso 8 · Reporte y detente
- `ai/handoff/reporte.md`: incluye los **supuestos** de la spec y cualquier dependencia o permiso que hayas agregado. Estado: «esperando revisión». No empieces la tarea 03.

## Fuera de esta tarea
Onboarding, Inicio real, Buscar, álbumes/artistas/listas, abecedario, filtros, menú de opciones, Reproduciendo, cola, gestos, letras, ajustes, widget, Room, DataStore, tema claro seleccionable.
