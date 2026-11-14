---

description: "Tareas de implementación del reproductor base"
---

# Tareas: Reproductor base

**Entrada**: `specs/001-reproductor-base/` (plan.md, spec.md, research.md, data-model.md, contracts/interfaces.md, quickstart.md)

**Pruebas**: obligatorias por DEC-22 y por la tarea 02 (Paso 6): JUnit para lógica pura y Compose para Biblioteca y mini tarjeta. Cada prueba se escribe junto a su código, en la misma tarea o justo antes.

**Organización**: por historia de la spec (US1…US5). Commits atómicos por área (`feat(media): …`, `feat(library): …`, etc.).

## Formato: `[ID] [P?] [Historia] Descripción`

- **[P]**: puede hacerse en paralelo (archivos distintos, sin dependencias pendientes).
- **[USn]**: historia de la spec.

## Convenciones de rutas

- Código: `app/src/main/java/com/luis/rola/` (en las tareas, `rola/`).
- Unitarias: `app/src/test/java/com/luis/rola/` (`test/`).
- Compose: `app/src/androidTest/java/com/luis/rola/` (`androidTest/`).
- Recursos: `app/src/main/res/` (`res/`).
- Textos de interfaz siempre en `res/values/strings.xml`, en español. Colores, tipos, formas y espacios solo desde `rola/core/designsystem` (INV-04).

---

## Fase 1: Preparación

**Propósito**: dependencias, permisos y recursos compartidos.

- [ ] T001 Agregar a `gradle/libs.versions.toml` y a `app/build.gradle.kts` las dependencias de research.md §1: `media3-exoplayer` y `media3-session` 1.11.1, `navigation-compose` 2.10.2, `lifecycle-viewmodel-compose` y `lifecycle-runtime-compose` 2.11.0 (`version.ref` existente `lifecycleRuntimeKtx`), `coil-compose` 3.6.3 (sin `coil-network-*`), `palette-ktx` 1.1.0. Ninguna otra. Comprobar con `.\gradlew :app:assembleDebug`.
- [ ] T002 [P] Descargar los íconos de Figma (music_note, notifications, check, shuffle, home, library, search, skip_next, play, pause; URLs de `get_design_context` de 23:332, 23:391, 32:581 y 5:51) y convertirlos a vectores en `res/drawable/ic_*.xml` (se tiñen desde Compose con `Icon(tint = …)`).
- [ ] T003 [P] Agregar todos los textos de la tarea a `res/values/strings.xml` (Permisos, Permiso denegado, Biblioteca, «%1$d canciones · Título A–Z» como plurals, «Artista desconocido», «Próximamente», descripciones accesibles de cada botón de ícono, aviso «No se pudo reproducir esta canción»). Borrar `fundaciones_listas` cuando T030 reemplace la pantalla temporal.

---

## Fase 2: Base (bloquea todas las historias)

**Propósito**: modelo, lógica pura, repositorio, inyección manual y esqueleto de navegación.

- [ ] T004 [P] Crear `Song` en `rola/core/data/Song.kt` con `id: Long`, `uri: Uri`, `title: String` («Nunca vacío»), `artist: String?` («`null` si está vacío o es `<unknown>`»), `album: String?` (mismas reglas), `durationMs: Long` («≥ 0»).
- [ ] T005 [P] Escribir `TitleOrderTest` en `test/core/data/TitleOrderTest.kt`: «Árbol» junto a «Arena» en A; «Ñandú» después de «Nube» y antes de «Ola»; «2 Minutos» y «¿Quién?» en «#» al final; sin distinción de mayúsculas; «El Sol» se ordena por «E» (PEND-05 sin aplicar). Luego implementar `TitleOrder.comparator` y `sectionOf(title)` en `rola/core/data/TitleOrder.kt` (research.md §6).
- [ ] T006 [P] Escribir `SongMapperTest` en `test/core/data/SongMapperTest.kt`: título vacío → `DISPLAY_NAME` sin extensión; artista `<unknown>` o vacío → `null`; duración negativa → 0. Luego implementar `songFrom(...)` en `rola/core/data/SongMapper.kt`.
- [ ] T007 Implementar `SongRepository` y `MediaStoreSongRepository` en `rola/core/data/SongRepository.kt`: `callbackFlow` sobre `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` con `IS_MUSIC != 0`, consulta en `Dispatchers.IO`, `ContentObserver` que vuelve a consultar, `conflate()`, orden con `TitleOrder.comparator` (research.md §5). Depende de T004–T006.
- [ ] T008 [P] Escribir `DurationTest` en `test/core/media/DurationTest.kt` (0 → «0:00», 65 000 → «1:05», 3 665 000 → «1:01:05»). Luego implementar `formatDuration(ms)` en `rola/core/media/Duration.kt`.
- [ ] T009 Crear `AppContainer` en `rola/AppContainer.kt` (repositorio; el `ImageLoader` se agrega en T014 y la conexión al reproductor en T019) y `RolaApp : Application` en `rola/RolaApp.kt`; registrar `android:name=".RolaApp"` en `app/src/main/AndroidManifest.xml` (PEND-02, sin Hilt).
- [ ] T010 Crear el esqueleto de navegación en `rola/navigation/RolaNavHost.kt` (rutas `inicio`, `biblioteca`, `buscar`; inicio = `inicio`, DEC-03) y la píldora en `rola/navigation/RolaNavBar.kt` según SCR-20 (nodo 32:786): `surface` con borde `outline`, forma `pill`, activo con `accent`/`onAccent` e ícono + texto, inactivos solo ícono con `contentDescription`, objetivos ≥ 48 dp. Navegar con `popUpTo(startDestination){saveState=true}`, `launchSingleTop`, `restoreState` (research.md §10). Sin `HorizontalPager`.

**Punto de control**: la app compila, las unitarias pasan y la barra cambia de pestaña.

---

## Fase 3: Historia 1 · Dar permiso y ver mis canciones (P1) 🎯 MVP

**Objetivo**: permiso de música desde SCR-05 y lista ordenada en Biblioteca (SCR-20).

**Prueba independiente**: instalar limpio, conceder el permiso, abrir Biblioteca y ver todas las canciones en el orden de DEC-05.

- [ ] T011 [US1] Declarar en `app/src/main/AndroidManifest.xml` `READ_MEDIA_AUDIO` y `READ_EXTERNAL_STORAGE` con `android:maxSdkVersion="32"`. Implementar `musicPermission()` y `MusicPermissionState` (`Granted`, `NotAsked`, `Denied`) en `rola/feature/onboarding/MusicPermission.kt` (research.md §11). **REQUIERE REVISIÓN HUMANA.**
- [ ] T012 [US1] Construir `PermissionScreen(onAllow)` en `rola/feature/onboarding/PermissionScreen.kt` según SCR-05 (nodo 23:332): logo, «Un permiso y listo.», tarjeta de permisos con `Modifier.elevado` (DEC-27; íconos sin relieve), etiquetas «Ahora»/«Después», nota de privacidad y botón «Permitir acceso» con `Modifier.acento`. Tokens del tema; textos de T003.
- [ ] T013 [US1] Reemplazar la pantalla temporal de `rola/MainActivity.kt`: calcular el estado del permiso en cada `ON_RESUME`; sin permiso → `PermissionScreen` con `rememberLauncherForActivityResult(RequestPermission)`; con permiso → `RolaNavHost` (FR-001, FR-004). Mantener `installSplashScreen()` y `enableEdgeToEdge`.
- [ ] T014 [P] [US1] Crear el `Fetcher` de Coil para `SongCover(uri)` con `ContentResolver.loadThumbnail(uri, Size(512, 512), null)` y el composable `Cover(song, size)` en `rola/feature/library/Cover.kt`: sin portada o error → recuadro `surfaceContainer` con ícono de nota `onSurfaceVariant` (research.md §8). Registrar el `ImageLoader` en `RolaApp` con `SingletonImageLoader.Factory`.
- [ ] T015 [US1] Crear `LibraryViewModel` en `rola/feature/library/LibraryViewModel.kt` (`StateFlow<List<Song>?>`, `null` mientras carga; factory que recibe `SongRepository` del `AppContainer`).
- [ ] T016 [US1] Escribir `LibraryScreenTest` en `androidTest/feature/library/LibraryScreenTest.kt` (lista falsa de 3 canciones → se ven los 3 títulos, «Artista desconocido» si `artist == null` y el resumen «3 canciones · Título A–Z»). Luego construir `LibraryScreen(songs, onSongClick, onShuffle)` en `rola/feature/library/LibraryScreen.kt` según SCR-20 (nodo 32:581): título «Biblioteca», solo el chip «Canciones» activo, resumen + botón «Aleatorio» (sin filtros, sin ⋮, sin abecedario), filas con `Cover` 48 dp, título y artista, `key = song.id` en la `LazyColumn` (SC-007) y título en `primary` si es la canción actual (FR-006a, se conecta en T025); indicador de progreso mientras `songs == null`; texto simple si la lista está vacía y sin «Aleatorio» (FR-009–FR-012). Conectarla a la ruta `biblioteca`.

**Punto de control**: US1 se prueba sola con el escenario 1–4 de quickstart.md.

---

## Fase 4: Historia 2 · Tocar una canción y que suene en segundo plano (P1)

**Objetivo**: reproducción en `MediaSessionService` con notificación de Media3.

**Prueba independiente**: tocar una canción, salir de la app, apagar la pantalla; la música sigue y la notificación la controla.

- [ ] T017 [US2] Crear `PlaybackService : MediaSessionService` en `rola/core/media/PlaybackService.kt` con ExoPlayer: atributos de música con foco de audio, `setHandleAudioBecomingNoisy(true)`, `setWakeMode(C.WAKE_MODE_LOCAL)`, `DefaultMediaNotificationProvider` con ícono `ic_launcher_monochrome`, `onTaskRemoved` que detiene el servicio si no suena nada (research.md §2–3).
- [ ] T018 [US2] Declarar el servicio en `app/src/main/AndroidManifest.xml` (`foregroundServiceType="mediaPlayback"`, `exported="true"`, filtro `androidx.media3.session.MediaSessionService`) y los permisos `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `POST_NOTIFICATIONS`, `WAKE_LOCK`. Sin `INTERNET`. **REQUIERE REVISIÓN HUMANA.**
- [ ] T019 [US2] Implementar `PlayerConnection` en `rola/core/media/PlayerConnection.kt` según contracts/interfaces.md: `MediaController` con `addListener` + `suspendCancellableCoroutine` (sin `kotlinx-coroutines-guava`), `state: StateFlow<PlayerState>` (posición cada ~500 ms mientras suena), `errors`, `play`, `shuffle`, `togglePlayPause`, `next`; `Song` → `MediaItem` con `mediaId`, URI y `MediaMetadata` (título, artista, álbum). Al terminar la cola: pausa en la última canción (FR-018a). Al fallar una canción: `seekToNextMediaItem` + emisión en `errors` (FR-018). Registrar en `AppContainer`.
- [ ] T020 [US2] Conectar `onSongClick` (cola = lista completa desde esa canción, FR-013) y `onShuffle` (FR-014) en `LibraryViewModel`; pedir `POST_NOTIFICATIONS` en API 33+ al primer toque de la ejecución si no está concedido, sin bloquear la reproducción (FR-005, research.md §4).
- [ ] T021 [US2] Mostrar el aviso de error de `PlayerConnection.errors` con un `Snackbar` en el `Scaffold` de `rola/navigation/RolaNavHost.kt` (INV-05).

**Punto de control**: escenarios 5–7 de quickstart.md.

---

## Fase 5: Historia 3 · Mini tarjeta (P2)

**Objetivo**: SCR-50 encima de la barra con el color de la portada.

**Prueba independiente**: con canción cargada aparece y controla la música; sin canción no aparece.

- [ ] T022 [P] [US3] Escribir `EnsureContrastTest` en `test/core/designsystem/EnsureContrastTest.kt` (color que ya cumple se devuelve igual; gris medio sobre gris medio termina ≥ 4.5:1; funciona sobre fondo claro y oscuro). Luego implementar `ensureContrast(fg, bg, min = 4.5)` en `rola/core/designsystem/Contrast.kt` usando `contrastRatio`.
- [ ] T023 [US3] Implementar `CoverColors` y `coverColors(bitmap, fallback)` en `rola/feature/player/CoverColors.kt` con Palette (`darkMutedSwatch` para `container`, `lightVibrantSwatch`/`vibrantSwatch` para `accent`) y todos los pares texto/fondo pasados por `ensureContrast`; sin portada o sin swatch → colores del tema (FR-021, FR-022, research.md §9).
- [ ] T024 [US3] Escribir `MiniPlayerTest` en `androidTest/feature/player/MiniPlayerTest.kt` (estado sin canción → no hay nodo con el título; con canción → título, artista y botones con descripción). Luego construir `MiniPlayer(state, onToggle, onNext)` en `rola/feature/player/MiniPlayer.kt` según SCR-50 (nodo 5:51): forma `large`, portada 48 dp, título y artista con elipsis, siguiente y reproducir/pausa (44 dp visibles, objetivo 48 dp), línea de progreso de 3 dp con descripción accesible «m:ss de m:ss» (`formatDuration`); tocar la tarjeta no hace nada (FR-023).
- [ ] T025 [US3] Colocar `MiniPlayer` encima de `RolaNavBar` en `rola/navigation/RolaNavHost.kt`, visible en las tres pestañas, con los colores de `coverColors` calculados al cambiar de canción (bitmap de `Cover` vía Coil). Agregar el desvanecido de Figma (nodo 32:769) con un degradado de `background` transparente a `background`, sin hex, y el relleno inferior de la lista para que la última fila no quede tapada. La tarjeta aparece y desaparece sin animación nueva en esta tarea (respeta «reducir movimiento», INV-06). Pasar el `mediaId` actual a `LibraryScreen` para FR-006a.

**Punto de control**: escenario 5 de quickstart.md con captura `t02-mini-reproductor.png`.

---

## Fase 6: Historia 4 · Si niego el permiso (P2)

**Objetivo**: SCR-06 con salida a Ajustes y reintento.

**Prueba independiente**: negar el permiso, ver SCR-06, abrir Ajustes, conceder y volver a la app sin toques extra.

- [ ] T026 [US4] Construir `PermissionDeniedScreen(onOpenSettings, onRetry)` en `rola/feature/onboarding/PermissionDeniedScreen.kt` según SCR-06 (nodo 23:391): círculo 120 dp con `Modifier.elevado` y nota, «Sin acceso a tu música», explicación, ruta «Ajustes › Apps › Rola › Permisos» en `Typography.time` sobre `surfaceContainer` (sin relieve, DEC-27), botón «Abrir Ajustes» con `Modifier.acento` y botón de texto «Intentar de nuevo».
- [ ] T027 [US4] En `rola/MainActivity.kt`, mostrar `PermissionDeniedScreen` cuando el estado sea `Denied`; «Abrir Ajustes» abre `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` con el paquete; «Intentar de nuevo» vuelve a lanzar la petición; al volver con el permiso concedido entra a la app (FR-003, FR-004).

---

## Fase 7: Historia 5 · Barra de navegación con tres destinos (P3)

**Objetivo**: Inicio y Buscar con «próximamente»; atrás según DEC-03.

**Prueba independiente**: tocar los tres destinos, ver el aviso, usar atrás.

- [ ] T028 [P] [US5] Crear `ComingSoonScreen()` en `rola/navigation/ComingSoonScreen.kt`: texto «Próximamente» centrado con `onSurfaceVariant`; conectarlo a las rutas `inicio` y `buscar`.
- [ ] T029 [US5] Comprobar en el emulador que atrás desde Biblioteca o Buscar vuelve a Inicio y que desde Inicio sale de la app sin detener la música (FR-027, INV-11); corregir `RolaNavHost` si no.

---

## Fase 8: Pulido y cierre

- [ ] T030 Borrar los textos y el código de la pantalla temporal que ya no se usen (`fundaciones_listas`, `marca_palabra` si queda sin uso) y revisar que no queden hex fuera de `core/designsystem` (`git grep -n "Color(0x" app/src/main/java | grep -v designsystem`).
- [ ] T031 Correr `.\gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` y `.\gradlew :app:connectedDebugAndroidTest`; anotar resultados reales.
- [ ] T032 Recorrer quickstart.md en el emulador (incluye SC-003: sonido en < 1 s; SC-007 con 2 000 canciones queda para el teléfono de Brian) y guardar `t02-permisos.png`, `t02-permiso-denegado.png`, `t02-biblioteca.png`, `t02-mini-reproductor.png` y `t02-notificacion.png` en `ai/handoff/capturas/`.
- [ ] T033 `design-critique`: comparar las capturas de SCR-05, SCR-06, SCR-20 y SCR-50 con Figma y anotar diferencias en `ai/handoff/reporte.md`.
- [ ] T034 `/ponytail:ponytail-review` sobre el diff de la tarea; `git log` sin autoría de IA; push a `origin development`.

---

## Dependencias y orden

- **Fase 1 → Fase 2 → historias.** T001 bloquea todo el código nuevo.
- **US1** necesita la Fase 2. Es el MVP.
- **US2** necesita US1 (se reproduce desde la lista).
- **US3** necesita US2 (`PlayerState`). T022 puede adelantarse en cualquier momento.
- **US4** necesita T011–T013 de US1; es independiente de US2 y US3.
- **US5** solo necesita T010; puede hacerse en paralelo con US1.
- **Fase 8** al final.

## Ejemplos en paralelo

```text
Fase 2: T004, T005, T006 y T008 a la vez (archivos distintos, lógica pura).
US1:    T014 (portadas) mientras se hace T012 (Permisos).
US3:    T022 (contraste) en paralelo con cualquier tarea de US2.
US5:    T028 en paralelo con US1.
```

## Estrategia

1. **MVP**: Fases 1–3 → instalar, conceder permiso, ver la lista. Validar con quickstart 1–4.
2. **Escuchar**: Fase 4 → la app ya cumple el objetivo principal.
3. **Control**: Fase 5 (mini tarjeta) y Fase 6 (permiso denegado).
4. **Estructura**: Fase 7 y cierre en Fase 8.

Commits sugeridos: `build: dependencias del reproductor base`, `feat(data): canciones del MediaStore ordenadas`, `feat(onboarding): permisos de música`, `feat(library): biblioteca de canciones`, `feat(media): servicio y conexión de reproducción`, `feat(player): mini tarjeta con color de portada`, `feat(navegacion): barra píldora y próximamente`.
