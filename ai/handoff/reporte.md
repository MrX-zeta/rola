# Reporte · Tarea 02 · Reproductor base

**Estado:** esperando revisión.
**REQUIERE REVISIÓN HUMANA:** permisos nuevos del manifiesto (ver «Permisos y dependencias»).

## Paso 0 · Comprobación
- El worktree estaba en `b0b8691` (tarea 01). Lo alineé con `development` (`25c5a0b`, «chore(claude): activar plugin de Figma», con «docs: sesión de diseño tarea 02» justo antes), como avisó Brian.

## Paso 0.5 · Splash redondo (DEC-17)
- `res/drawable/splash_icono.xml` (288 dp, círculo `ic_launcher_background` r 80, símbolo escalado 0.628 en `@color/marca_tinta`). `Theme.Rola.Starting` hereda de `Theme.SplashScreen`, sin `windowSplashScreenIconBackgroundColor`.
- Visto redondo en el emulador (`t02-splash.png`, capturado a mitad de la animación de salida). **Falta que Brian lo vea en el Redmi.**

## Pasos 1–5 · Spec Kit
- `specs/001-reproductor-base/`: `spec.md`, `plan.md`, `research.md`, `data-model.md`, `contracts/interfaces.md`, `quickstart.md`, `tasks.md` (34 tareas, todas hechas), `checklists/requirements.md` (16/16).
- `/speckit-analyze`: 0 críticos. Corregí 7 detalles (portadas: resaltar la canción actual en `primary` como en Figma; desvanecido detrás de la mini tarjeta; `key` en la lista; supuesto del permiso de notificaciones; rutas de tareas).

### Supuestos de la spec (el grafo no los responde)
1. Al terminar la última canción la música se detiene (sin repetir).
2. Sin canciones: Biblioteca muestra un texto simple; SCR-12 llega con Inicio.
3. Sin título: nombre del archivo sin extensión; sin artista: «Artista desconocido».
4. La cola no se recuerda al cerrar la app (sin Room ni DataStore).
5. El permiso de notificaciones se pide como mucho una vez por ejecución (no hay dónde guardar si ya se pidió).
6. «Intentar de nuevo» vuelve a pedir el permiso; si Android ya no muestra el diálogo, se queda en SCR-06.
7. «Aleatorio» baraja la lista una vez (no es un modo).
8. «Próximamente» es un texto centrado en Inicio y Buscar.

## Paso 6 · Implementación
- **Paquetes (DEC-20):** `core/data` (Song, orden DEC-05, mapeo, repositorio MediaStore con `ContentObserver`), `core/media` (PlaybackService, PlayerConnection, duración), `feature/onboarding` (SCR-05, SCR-06, puerta del permiso), `feature/library` (SCR-20, portadas con Coil), `feature/player` (SCR-50, colores de portada), `navigation` (píldora DEC-03, Próximamente), `RolaApp` + `AppContainer` (PEND-02, sin Hilt).
- **Reproducción:** ExoPlayer con atributos de música y foco de audio, pausa al desconectar audífonos, `WAKE_MODE_LOCAL`. Notificación por defecto de Media3; su ícono es el monocromo por el alias `media3_notification_small_icon` (sin API inestable). El servicio arma la URI a partir del id y descarta lo que no sea un id del MediaStore. Si una canción falla salta a la siguiente y la interfaz muestra un snackbar.
- **Color de portada (PEND-08 A):** Palette (`darkMuted`/`darkVibrant` de fondo, `lightVibrant`/`vibrant` de acento) y `ensureContrast` (nuevo en `core/designsystem/Contrast.kt`): texto ≥ 4.5:1 y botón/línea ≥ 3:1 sobre el fondo. Sin portada: colores del tema.
- **Navegación:** sin animaciones entre pestañas (desactivé la de navigation-compose para no meter movimiento nuevo sin pasar por `animate`).
- **Íconos:** vectores bajados de Figma. `ic_play` no está en las 4 pantallas: usé el `play_arrow` de Material Symbols, de la misma familia que `pause`.

### Decisiones que tomé
- **Coil 3.4.0, no 3.6.3:** Coil 3.5+ está compilado con Kotlin 2.4 y el proyecto usa el plugin de Kotlin 2.2.10 (falla con «metadata 2.4.0, expected 2.2.0»). 3.4.0 es estable y lleva stdlib 2.3.10, que el compilador 2.2 sí lee. Subir Kotlin era un cambio de build fuera de la tarea. **Propuesta:** una tarea de mantenimiento que suba Kotlin, AGP y la BOM (lint lo avisa) y luego Coil.
- `PlayerConnection` es una clase, sin interfaz: las pruebas de Compose usan `MiniPlayer(state)` directamente y no necesitaban un falso.
- `MediaItem` → `Song` sale de los metadatos del controlador, así la mini tarjeta funciona aunque la actividad se recree con música sonando.
- Mapeo de tipografía de Figma a M3 (sin estilos nuevos): 34→headlineLarge (32), 30/28→headlineMedium (28), 16 semibold→titleMedium, 15→titleSmall (14), 13→labelLarge/bodyMedium (14).

## Paso 7 · Comprobaciones reales
- `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` → **BUILD SUCCESSFUL**.
- **Unitarias:** 16, 0 fallas (TitleOrder 4, SongMapper 3, Duration 1, EnsureContrast 2, BrandTokens 3, Contrast 3).
- **Compose** (`connectedDebugAndroidTest`, Pixel 7 Pro API 37): 3, 0 fallas (Biblioteca con repositorio falso; mini tarjeta oculta sin canción; mini tarjeta con canción).
- **Lint:** 0 errores, 6 avisos: splash de 288 dp > 200 (exigido por DEC-17), versiones nuevas de AGP/BOM/Kotlin/Coil, y `ExportedService` (el servicio de Media3 debe estar exportado para que lo controlen la notificación, Bluetooth y el sistema).
- `git grep "Color(0x"` fuera de `core/designsystem`: nada.
- **Emulador** (10 FLAC de prueba generados con un script propio en `build/pruebas/`, con etiquetas y portada; no se versionan):
  - Instalación limpia → Permisos ✓. Negar → Permiso denegado ✓. «Abrir Ajustes» abre los ajustes de la app ✓; al volver con el permiso → Inicio sin toques ✓.
  - Biblioteca: «10 canciones · Título A–Z» y orden Agua de mar, Árbol, Brillo, Madrugada, Nube, Ñandú, Ola, pista sin etiquetas, 2 Minutos, ¿Quién? ✓. Archivo sin etiquetas → nombre del archivo y «Artista desconocido» ✓.
  - Tocar canción → pide notificaciones y suena mientras tanto ✓; la cola sigue sola ✓; fila actual en `primary` ✓.
  - Mini tarjeta: pausa, reproducir y siguiente ✓; color de la portada ✓; descripción del progreso «0:03 de 0:19» ✓.
  - Notificación con la app en segundo plano: ícono de Rola, portada, pausa/reproducir/siguiente ✓.
  - Pantalla apagada 12 s: sigue sonando ✓. Atrás desde Buscar → Inicio, otro atrás → sale y la música sigue ✓.
  - Permiso retirado y app reabierta → Permisos; conceder → app ✓.
- **No probado:** biblioteca de 2 000 canciones (SC-007) y 30 min seguidos (SC-004): quedan para el Redmi. Audífonos y llamadas: el emulador no los simula.
- **Capturas** (`ai/handoff/capturas/`): `t02-splash.png`, `t02-permisos.png`, `t02-permiso-denegado.png`, `t02-biblioteca.png`, `t02-mini-reproductor.png`, `t02-notificacion.png`.

## design-critique (capturas vs. Figma)
En general fieles; estructura, colores y relieve coinciden. Diferencias:

| Pantalla | Diferencia | Por qué / propuesta |
|---|---|---|
| Todas | Los textos se ven un poco más anchos; en SCR-06 «en:» baja a una tercera línea. | Los estilos M3 de `Type.kt` traen `letterSpacing` (0.5 sp en bodyLarge) y Figma usa 0. **Propuesta:** quitar el espaciado en `RolaTypography` (cambio de tokens, no lo hice). |
| SCR-05 | Título 32 sp en lugar de 34; separación entre bloques 24 en lugar de 28. | Tokens más cercanos (headlineLarge, `RolaSpacing.xl`). |
| SCR-06 | Ruta de Ajustes con radio 20 en lugar de 16; Geist Mono 14 en lugar de 13. | No hay token de 16 dp; `Typography.time` es 14. |
| SCR-20 | Título «Biblioteca» con más aire arriba y abajo; título de fila 14 en lugar de 15. | Padding con tokens de 20 y titleSmall. Ajustable si molesta. |
| SCR-20 | Portadas con radio 12; en Figma casi cuadradas (~4). | Usé `shapes.small`. ¿Qué radio quiere el diseño para portadas pequeñas? |
| SCR-20 | Sin botón de filtros, sin ⋮ y sin abecedario. | Fuera de la tarea (FR-010). |
| SCR-50 | Con portada azul el fondo sale más vivo (`#2F4BB8`) que en Figma (`#1B2550`). | Palette no encuentra un tono «oscuro apagado» en esa portada de prueba y usa el «oscuro vivo». Con portadas reales varía. Se podría oscurecer el fondo un paso más. |
| SCR-50 | Separación entre tarjeta y píldora de 12 en lugar de 10; borde interior de la píldora de 4 en lugar de 6. | Tokens más cercanos. |

Accesibilidad: botones de ícono de 48 dp con descripción, pestañas como `Role.Tab`, contraste de la tarjeta probado; el texto escala con la letra del sistema.

## Permisos y dependencias — REQUIERE REVISIÓN HUMANA
- Permisos nuevos: `READ_MEDIA_AUDIO`, `READ_EXTERNAL_STORAGE` (`maxSdkVersion 32`), `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `POST_NOTIFICATIONS`, `WAKE_LOCK`. Sin `INTERNET`.
- Servicio `PlaybackService` exportado (requisito de Media3).
- Dependencias nuevas (todas permitidas por la tarea): media3-exoplayer y media3-session 1.11.1, navigation-compose 2.10.2, lifecycle-viewmodel-compose y lifecycle-runtime-compose 2.11.0, coil-compose **3.4.0**, palette-ktx 1.1.0. Ninguna otra.

## Ponytail-review
- Encontró 3 cosas y las corregí: reproducir no hacía nada tras fallar la última canción (faltaba `prepare()`); el orden creaba un `Regex` en cada comparación; `ensureContrast` podía lanzar excepción en un caso raro.
- Atajo marcado con `shortcut:`: órdenes enviadas antes de que el controlador conecte (primeros ms) se pierden; y el permiso de notificaciones «una vez por ejecución».

## Para el equipo
1. **DEC-03 vs. objetivo:** la app abre en Inicio, que hoy solo dice «Próximamente». ¿Abrir en Biblioteca mientras Inicio no exista?
2. **DEC-18 vs. Android 13+:** las notificaciones de sesión multimedia no necesitan `POST_NOTIFICATIONS` (la notificación sale igual). ¿Seguimos pidiéndolo al reproducir?
3. ¿Tarea de mantenimiento para subir Kotlin/AGP/BOM y Coil a 3.6?
4. ¿Quitar el `letterSpacing` de M3 en `RolaTypography` para igualar Figma?
5. Radio de las portadas pequeñas en listas.
6. Grafo: anotar que el splash ya no usa `IconBackground` (DEC-17 ya lo dice) y que Coil queda en 3.4.0 por Kotlin 2.2.
7. Skills: estaban todas las necesarias (figma-design-to-code, design-critique, ponytail-review, speckit-*). No usé `animate` porque no agregué animaciones.

## Commits
- 4eabdbe · fix(marca): círculo del splash dentro del ícono
- a5ec64c · docs(spec): 001 reproductor base
- 64bc681 · build: dependencias del reproductor base
- ea5b4ee · feat(data): canciones del MediaStore ordenadas por título
- 9903d94 · feat(media): servicio y conexión de reproducción
- b980602 · feat(onboarding): permisos de música
- 7d8505c · feat(library): biblioteca de canciones
- 3ecb6a9 · feat(player): mini tarjeta con color de portada
- 600e2d5 · feat(navegacion): barra píldora, próximamente y arranque de la app
- (este) docs(handoff): reporte de la tarea 02
