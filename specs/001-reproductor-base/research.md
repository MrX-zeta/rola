# Investigación: Reproductor base

Cada punto: **Decisión**, **Por qué** y **Alternativas descartadas**. No quedan «NEEDS CLARIFICATION».

## 1. Versiones de las dependencias nuevas

Consultado en `dl.google.com/android/maven2` y `repo1.maven.org` el 2026-11-14 (últimas estables, sin alpha/beta/rc):

| Artefacto | Versión |
|---|---|
| `androidx.media3:media3-exoplayer`, `media3-session` | 1.11.1 |
| `androidx.navigation:navigation-compose` | 2.10.2 |
| `androidx.lifecycle:lifecycle-viewmodel-compose`, `lifecycle-runtime-compose` | 2.11.0 |
| `io.coil-kt.coil3:coil-compose` | 3.6.3 |
| `androidx.palette:palette-ktx` | 1.1.0 |

- **Decisión**: esas versiones, en `gradle/libs.versions.toml`.
- **Por qué**: la tarea pide estables; lifecycle queda alineado con `lifecycle-runtime-ktx` 2.11.0 que ya está.
- **Descartado**: `coil-network-okhttp` (red, INV-01); `kotlinx-coroutines-guava` para esperar el `ListenableFuture` del `MediaController` (se resuelve con `addListener` + `suspendCancellableCoroutine`, sin dependencia).

## 2. Reproducción en segundo plano

- **Decisión**: `PlaybackService : MediaSessionService` con un `ExoPlayer` configurado con `setAudioAttributes(USAGE_MEDIA + AUDIO_CONTENT_TYPE_MUSIC, handleAudioFocus = true)`, `setHandleAudioBecomingNoisy(true)` y `setWakeMode(C.WAKE_MODE_LOCAL)`. La interfaz usa un `MediaController` construido con el `SessionToken` del servicio. `onTaskRemoved`: si no suena nada, `stopSelf()`.
- **Por qué**: cubre FR-015 (segundo plano, pantalla apagada), FR-017 (foco y audífonos) con APIs de Media3 sin código propio. `WAKE_MODE_LOCAL` mantiene la CPU para archivos locales con la pantalla apagada.
- **Descartado**: `MediaLibraryService` (navegación para Android Auto, fuera de alcance); servicio propio con `MediaSessionCompat` (más código).

## 3. Notificación

- **Decisión**: la notificación por defecto de Media3 (`DefaultMediaNotificationProvider`) con `setSmallIcon(R.drawable.ic_launcher_monochrome)`. La portada sale de los metadatos incrustados del archivo, que ExoPlayer extrae al reproducir y Media3 usa en la notificación.
- **Por qué**: FR-016 sin código de notificación propio; los controles anterior/reproducir/siguiente son los estándar del sistema.
- **Hallazgo para el reporte**: desde Android 13 las notificaciones de sesiones multimedia están exentas del permiso `POST_NOTIFICATIONS`. DEC-18 pide igual pedirlo al reproducir por primera vez; se respeta DEC-18 y se pregunta en el reporte si sigue haciendo falta.
- **Descartado**: proveedor de notificación propio.

## 4. Permiso de notificaciones «la primera vez»

- **Decisión**: se pide al tocar una canción o «Aleatorio» si no está concedido, como mucho una vez por ejecución de la app (bandera en memoria del ViewModel de la actividad). Android deja de mostrar el aviso tras dos negativas.
- **Por qué**: «la primera vez» exacta exige guardar estado y DataStore está fuera de alcance. **Supuesto** para el reporte.
- **Descartado**: `SharedPreferences` (persistencia no pedida en esta tarea).

## 5. Lectura de la biblioteca (DEC-26)

- **Decisión**: `MediaStoreSongRepository.songs(): Flow<List<Song>>` con `callbackFlow`: consulta `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` con `IS_MUSIC != 0` en `Dispatchers.IO`, registra un `ContentObserver` y vuelve a consultar en cada cambio (con `conflate`). Columnas: `_ID`, `TITLE`, `ARTIST`, `ALBUM`, `ALBUM_ID`, `DURATION`, `DISPLAY_NAME`. El orden se aplica en Kotlin (DEC-05), no en SQL.
- **Por qué**: el `COLLATE` de SQLite no conoce la Ñ ni agrupa acentos como DEC-05.
- **Descartado**: `MediaStore` con `ORDER BY title` (orden incorrecto para la Ñ); escaneo de carpetas propio (DEC-26 dice MediaStore).

## 6. Orden por título (DEC-05)

- **Decisión**: función pura `sortKey(title)`:
  1. Primer carácter no letra (número o símbolo) → grupo «#», que va al final.
  2. Se quitan diacríticos con `Normalizer` (NFD) excepto la Ñ: se protege antes de normalizar (Ñ/ñ → marcador que ordena entre N y O).
  3. Comparación sin mayúsculas.
  Más `sectionOf(title)` que devuelve la letra (A–Z, Ñ) o «#»; se usará con el abecedario en otra tarea, hoy solo la usan las pruebas.
- **Por qué**: DEC-05; PEND-05 sigue propuesto, así que no se quitan artículos.
- **Descartado**: `java.text.Collator` en español (ordena la Ñ bien, pero no manda números y símbolos al final ni es estable entre versiones de Android).

## 7. Mapeo de filas

- **Decisión**: función pura `songFrom(id, title, artist, album, albumId, durationMs, displayName)`; título vacío → `displayName` sin extensión; artista vacío o `<unknown>` (valor que pone MediaStore) → `null` y la interfaz muestra «Artista desconocido» desde `strings.xml`.
- **Por qué**: probable con JUnit sin `Cursor`; el texto visible queda en recursos (español).

## 8. Portadas

- **Decisión**: Coil 3 con un `Fetcher` propio para el tipo `SongCover(uri: Uri)` que llama a `ContentResolver.loadThumbnail(uri, Size(512,512), null)` y devuelve el `Bitmap`. Error o sin portada → recuadro `surfaceContainer` con ícono de nota (`onSurfaceVariant`). El `ImageLoader` lo crea `AppContainer` y se pasa con `SingletonImageLoader.Factory` en `RolaApp`.
- **Por qué**: `loadThumbnail` es la vía oficial desde API 29 y Coil da caché y cancelación al desplazarse.
- **Descartado**: `MediaMetadataRetriever` (más lento); `albumart` por `ALBUM_ID` (en desuso desde API 29).

## 9. Color de la portada (PEND-08 A)

- **Decisión**: en `feature/player`, `coverColors(bitmap)` usa `Palette.from(bitmap).generate()` y toma `darkMutedSwatch` (fondo) y `lightVibrantSwatch`/`vibrantSwatch` (acento), con respaldo en el tema. Luego `ensureContrast(fg, bg, 4.5)` (pura, en `core/designsystem`, usa `contrastRatio` existente) sube o baja la luminosidad del color de texto/acento hasta llegar a 4.5:1; el texto principal es `onSurface` o el que alcance contraste.
- **Por qué**: FR-021/022 e INV-06; la parte pura se prueba con JUnit sin bitmaps.
- **Descartado**: color dinámico de Material (`dynamicColor` está apagado por DEC-15).

## 10. Navegación (DEC-03)

- **Decisión**: `NavHost` con rutas de texto `inicio`, `biblioteca`, `buscar`; destino inicial `inicio`. Al tocar la píldora: `navigate(ruta) { popUpTo(startDestination) { saveState = true }; launchSingleTop = true; restoreState = true }`. Así atrás vuelve a Inicio y luego sale (pila por pestaña de DEC-03). Sin `HorizontalPager`, así que no hay deslizar.
- **Por qué**: patrón estándar de navigation-compose; rutas de texto evitan el plugin de serialización.
- **Descartado**: rutas tipadas (requieren `kotlinx-serialization`, dependencia no pedida).

## 11. Permiso de música

- **Decisión**: `musicPermission()` = `READ_MEDIA_AUDIO` en API 33+, `READ_EXTERNAL_STORAGE` en 30–32 (con `maxSdkVersion="32"` en el manifiesto). Estado calculado en `MainActivity` en cada `ON_RESUME` (así detecta el permiso concedido en Ajustes, FR-004). Negado = se pidió y el resultado fue `false`. «Abrir Ajustes» → `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`.
- **Por qué**: DEC-18; volver de Ajustes no necesita toques extra.

## 12. Íconos

- **Decisión**: los íconos de Figma (Material Symbols: music_note, notifications, check, shuffle, home, library, search, skip_next, play, pause) como vectores en `res/drawable/`, descargados con las URLs de `get_design_context`.
- **Por qué**: `material-icons-extended` sería una dependencia no pedida y no trae exactamente los mismos trazos.

## 13. Pruebas de Compose

- **Decisión**: `app/src/androidTest` con `createComposeRule`: `LibraryScreen` con una lista falsa muestra títulos; `MiniPlayer` no se dibuja con estado vacío. Se corren con `connectedDebugAndroidTest` en el emulador.
- **Por qué**: DEC-22; Robolectric sería una dependencia no pedida.
