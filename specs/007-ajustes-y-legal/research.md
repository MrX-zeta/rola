# Investigación: Ajustes y legal

## 1. Dependencias

- **Decisión**: ninguna nueva.
- **Por qué**: DataStore, splashscreen, Navigation, Material 3 y `GraphicsLayer` (captura) ya están. La versión sale de `BuildConfig`, que solo requiere `buildFeatures.buildConfig = true`.
- **Alternativas**: librería de licencias (oss-licenses): agrega un plugin y red de Google Play Services; descartada, los textos van en `strings.xml` (tarea 08).

## 2. Tema sin parpadeo al arrancar

- **Decisión**: `MainActivity` lee `theme` en `lifecycleScope` y mantiene el splash (`setKeepOnScreenCondition`) hasta tener el tema y la bienvenida; `setContent` dibuja ya con el valor leído.
- **Por qué**: es el mismo patrón que ya evita el parpadeo de la bienvenida (SC-002 de la tarea 03). Leer con `runBlocking` bloquearía el hilo principal.
- **Alternativas**: tema por defecto y luego cambiar: muestra un cuadro oscuro antes del claro (falla FR-013). `AppCompatDelegate.setDefaultNightMode`: requiere AppCompat, que la app no usa.

## 3. Fundido al cambiar el tema

- **Decisión**: la raíz de `RolaTheme` graba su contenido en un `GraphicsLayer` (`drawWithContent { layer.record { drawContent() }; drawLayer(layer) }`). Al pedir otro tema, primero se captura el cuadro actual (`layer.toImageBitmap()`), luego se aplica el tema nuevo y la captura se dibuja encima con alfa 1 → 0 en ≤ 300 ms (salida, `EaseOut`). La captura no tiene semántica ni recibe toques. Con «reducir movimiento» el fundido se mantiene (DEC-28 permite fundidos cortos) pero sin otro movimiento.
- **Por qué**: cubre todo (esquema de M3, colores de portada de Palette, barras dibujadas) sin animar cada color ni recomponer dos árboles; no hay saltos porque el diseño no cambia, solo el color.
- **Alternativas**: `animateColorAsState` por cada rol del esquema (~35): los colores de portada y los que no salen del esquema saltan. `Crossfade` de toda la app: compone dos árboles, duplica estado y reproductor. Sin animación: válido, pero la tarea pide fundido.
- **Decidido con `animate` (T015)**: cambio poco frecuente; propósito: evitar un cambio brusco. Solo opacidad con `Animatable`; `RolaMotion.EaseOut` (salida fuerte del proyecto) en `RolaMotion.LENTA` (250 ms); con «reducir movimiento», el mismo fundido en `MEDIA` (150 ms). Interrupción: la foto se graba dentro de la capa, así que un segundo cambio a medio fundido captura lo que se ve y sigue sin salto. Juzgar la sensación en `perfil`.

## 4. Barras del sistema

- **Decisión**: en `setContent`, un efecto que vuelve a llamar `enableEdgeToEdge` con `SystemBarStyle.dark` o `SystemBarStyle.light` (fondo transparente) cada vez que cambia si el tema resuelto es oscuro.
- **Por qué**: hoy está fijo en oscuro; así los íconos de la barra de estado se leen en claro (FR-012).
- **Alternativas**: `SystemBarStyle.auto`: sigue al sistema, no a la elección del usuario.

## 5. «Sistema» sigue al teléfono

- **Decisión**: `ThemeMode.SYSTEM` se resuelve con `isSystemInDarkTheme()` dentro de la composición. La actividad no declara `configChanges`, así que al cambiar el modo del teléfono se recrea; el DataStore ya está en memoria y el tema se lee al instante, sin splash.
- **Por qué**: `isSystemInDarkTheme()` sigue la configuración (FR-014). Con Oscuro o Claro elegidos, la recreación no cambia nada visible.
- **Alternativas**: declarar `configChanges="uiMode"`: evita la recreación, pero cambia el comportamiento de toda la actividad sin necesidad.

## 6. Escaneo compartido

- **Decisión**: `LibraryScanner(songs: SongRepository, preferences: PreferencesRepository, now)` en `AppContainer` con `scanning: StateFlow<Boolean>` y `suspend fun scan(): Int?` (`null` si ya hay uno corriendo). Al terminar guarda `last_scan_at`. Inicio (SCR-12) y Ajustes lo usan; el aviso del resultado lo sigue mostrando `RolaNavHost` en su ámbito de avisos, que sobrevive a la navegación.
- **Por qué**: hoy `scanning` vive en `HomeViewModel`; Ajustes debe ver el mismo estado (un toque en cada lado no lanza dos escaneos) y la fecha debe guardarse en ambos casos (FR-042).
- **Alternativas**: obtener `HomeViewModel` desde la entrada de Inicio: acopla Ajustes a Inicio.

## 7. «Último escaneo: hace …»

- **Decisión**: función pura `timeAgo(now, then): TimeAgo` con tramos `Moment` (< 1 min o futuro), `Minutes(n)` (< 60 min), `Hours(n)` (< 24 h), `Days(n)`; la pantalla lo traduce con plurales de `strings.xml`. Sin fecha: «Aún no has escaneado». La fila se refresca al entrar y cada minuto mientras está visible.
- **Por qué**: lógica probable en JUnit sin `Context`; los textos quedan en recursos (FR-043, FR-044).
- **Alternativas**: `DateUtils.getRelativeTimeSpanString`: da «hace 2 días» pero también «ayer» y fechas absolutas, y depende del idioma del teléfono.

## 8. Temporizador en Ajustes

- **Decisión**: la fila lee `PlayerState.timer` y `timerEndsAtMs` de `PlayerConnection`, cuenta el resto con `produceState` (como `SleepTimerSheet`) y abre `SleepTimerSheet`. El texto del aviso al elegir se mueve a una función compartida para que Reproduciendo y Ajustes digan lo mismo.
- **Por qué**: un solo temporizador en el servicio (FR-020); sin guardar (FR-022).

## 9. Copia de seguridad

- **Decisión**: `android:allowBackup="false"`, se quita `fullBackupContent` (y su XML), y `data_extraction_rules.xml` excluye todos los dominios en `cloud-backup` y `device-transfer`.
- **Por qué**: desde Android 12, `allowBackup="false"` apaga la copia en la nube pero **no** el traspaso entre teléfonos; las reglas de extracción lo cierran (PEND-09, FR-060).

## 10. Documentos legales

- **Decisión**: un `enum LegalDoc` con título, fecha y secciones (pares de recursos encabezado/párrafo); una `LegalScreen` con barra superior (flecha + título con rol de encabezado) y columna desplazable. Los textos se copian completos de Figma (41:1778, 41:1810, 41:1842) a `strings.xml`; Licencias suma Coil, Palette y Reorderable (Apache 2.0).
- **Por qué**: las tres pantallas tienen la misma forma en Figma (fecha, encabezados y párrafos).

## 11. Navegación

- **Decisión**: rutas `ajustes` y `ajustes/{doc}` dentro del `NavHost`. No son raíces de pestaña, así que la píldora se queda en la pestaña dueña (Inicio) por el mecanismo de `ultima` que ya existe.
- **Por qué**: FR-001, FR-002, FR-004.
