# Plan de implementación: Ajustes y legal

**Carpeta**: `specs/007-ajustes-y-legal` | **Fecha**: 2026-11-26 | **Spec**: [spec.md](spec.md)

**Entrada**: especificación de `specs/007-ajustes-y-legal/spec.md` y restricciones del Paso 3 de la tarea 08.

## Resumen

`PreferencesRepository` suma `theme` (Sistema/Oscuro/Claro, por defecto Oscuro) y `last_scan_at` en el DataStore `rola_ajustes`. `MainActivity` sostiene el splash hasta leer el tema (igual que ya hace con la bienvenida), así el primer cuadro sale en el tema correcto; `RolaTheme` recibe el modo y, al cambiar, muestra un fundido de ≤ 300 ms desde una captura del cuadro anterior, y vuelve a pintar las barras del sistema. El escaneo pasa de `HomeViewModel` a un `LibraryScanner` compartido que evita dos escaneos a la vez y guarda la fecha al terminar. `feature/settings` trae Ajustes (SCR-70 sin LETRAS ni widget), la hoja del tema (estilo de la hoja de ordenar), la fila del temporizador (reusa `SleepTimerSheet` y el estado del reproductor) y una sola pantalla legal que pinta Términos, Privacidad y Licencias desde `strings.xml`. Las rutas nuevas cuelgan de Inicio en el `NavHost`. La copia de seguridad se apaga en el manifiesto y en las reglas de extracción.

## Contexto técnico

**Lenguaje/versión**: Kotlin 2.4.21 (AGP 9.5.1), Java 11 de destino.

**Dependencias principales**: solo las instaladas (Compose, Material 3, Navigation, Lifecycle, Media3, DataStore, splashscreen). **Ninguna nueva** ([research.md](research.md) §1).

**Almacenamiento**: DataStore `rola_ajustes`: claves nuevas `theme` (texto `system|dark|light`) y `last_scan_at` (milisegundos de época). Sin cambios en Room ni en `rola_reproduccion`.

**Pruebas**: JUnit 4 en la JVM: «hace X» (cortes de minuto, hora y día, futuro y nunca), tema por defecto y lectura de valores desconocidos, `LibraryScanner` (guarda la fecha, un solo escaneo a la vez). En emulador (Compose): Ajustes muestra solo las filas permitidas, el tema cambia y persiste, los tres documentos se leen hasta el final y se vuelve, el engrane y «Abrir Ajustes» llevan a Ajustes y «atrás» regresa, «Aún no has escaneado».

**Plataforma**: Android 11 (API 30) a 17 (API 37). Emulador Pixel 7 Pro; Redmi para `perfil` si aparece en adb.

**Tipo de proyecto**: app móvil de un solo módulo `app`.

**Metas de rendimiento**: cambio de tema en un fundido ≤ 300 ms sin tirones; arranque sin cuadro del otro tema (SC-001, SC-002). La captura del cuadro se hace solo al cambiar el tema.

**Restricciones**: sin red (INV-01); tokens (INV-04); plano (DEC-27); accesibilidad (INV-06); movimiento DEC-28 (solo el fundido del tema; la hoja y el interruptor usan la animación existente); textos en español.

**Escala**: 2 claves de DataStore, 1 escáner compartido, 1 función pura de tiempo, 1 ViewModel, 2 pantallas (Ajustes y documento legal) con 3 documentos, 1 hoja, 4 íconos, cambios en `MainActivity`, `RolaTheme`, `HomeScreen`, `RolaNavHost` y el manifiesto.

## Revisión de la constitución

*Puerta: antes de la Fase 0 y otra vez después de la Fase 1.*

| Artículo | Cómo se cumple | Estado |
|---|---|---|
| I. El grafo manda (INV-08, INV-09) | SCR-70 sin LETRAS, «Carpeta de letras» ni widget (DEC-30, nota de SCR-70). PEND-07 no entra. «Aún no has escaneado» lo respondió Brian. | ✅ |
| II. Sin conexión (INV-01) | Nada usa red; sin dependencias nuevas. | ✅ |
| III. Datos y secretos (INV-02, INV-03) | Sin copia de seguridad ni traspaso entre teléfonos (PEND-09): los ids del MediaStore no viajan a otro teléfono. | ✅ |
| IV. Tokens (INV-04, DEC-27) | Ajustes y documentos planos con `surfaceContainer`, `outline`, tipos del tema; los íconos usan `onSurface`. | ✅ |
| V. Retroalimentación (INV-05) | Escaneo con progreso y aviso; temporizador con su aviso; el tema cambia a la vista. | ✅ |
| VI. Accesibilidad (INV-06) | Filas ≥ 48 dp con rol; opciones del tema como radio con estado; títulos con rol de encabezado; flecha con descripción; texto que escala y se desplaza; contraste revisado en claro. | ✅ |
| VII. La música no se interrumpe (INV-11) | Cambiar el tema recrea solo la composición, no la actividad ni el servicio. | ✅ |
| VIII. Arquitectura (DEC-20) | `core/data` (preferencias, escáner, tiempo), `core/designsystem` (tema), `feature/settings` (nuevo paquete previsto), `navigation`. VM con `StateFlow`. | ✅ |
| IX. Pruebas (DEC-22) | Las de «Pruebas». | ✅ |
| X. Código mínimo | Se reusan `SleepTimerSheet`, el patrón de `SortSheet`, el splash de `MainActivity`, `formatDuration`, `ic_timer`, `ic_refresh`, `ic_chevron_forward` y `ic_arrow_back`; una sola pantalla legal para los tres documentos. | ✅ |
| XI. Español | Textos y plurales en `strings.xml`; commits en español. | ✅ |

**Revisión después del diseño**: sin cambios.

## Estructura del proyecto

### Documentación (esta función)

```text
specs/007-ajustes-y-legal/
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
app/build.gradle.kts                       # buildConfig = true (versión)
app/src/main/AndroidManifest.xml           # allowBackup="false", sin fullBackupContent
app/src/main/res/xml/backup_rules.xml      # se borra (solo servía con la copia activa)
app/src/main/res/xml/data_extraction_rules.xml  # excluye todo en nube y traspaso entre teléfonos
app/src/main/res/drawable/ic_settings.xml, ic_palette.xml, ic_document.xml, ic_shield.xml  # NUEVOS (Figma)
app/src/main/res/values/strings.xml        # Ajustes, tema, «hace…», textos legales, SCR-12 corregido
app/src/main/java/com/luis/rola/
├── MainActivity.kt                # splash hasta leer el tema; barras según el tema
├── AppContainer.kt                # + LibraryScanner
├── core/data/
│   ├── PreferencesRepository.kt   # + theme, lastScanAt
│   ├── ThemeMode.kt               # NUEVO: SYSTEM/DARK/LIGHT, lectura tolerante
│   ├── LibraryScanner.kt          # NUEVO: un escaneo a la vez, guarda last_scan_at
│   └── TimeAgo.kt                 # NUEVO: «hace…» por tramos (puro)
├── core/designsystem/Theme.kt     # RolaTheme(themeMode) + fundido desde captura
├── feature/home/
│   ├── HomeViewModel.kt           # el escaneo pasa a LibraryScanner
│   └── HomeScreen.kt              # engrane en Inicio; «Abrir Ajustes» en SCR-12
├── feature/player/SleepTimerSheet.kt  # + texto del aviso compartido
├── feature/settings/              # NUEVO paquete
│   ├── SettingsViewModel.kt
│   ├── SettingsScreen.kt          # SCR-70
│   ├── ThemeSheet.kt              # hoja Sistema/Oscuro/Claro
│   └── LegalScreen.kt             # SCR-72, 73, 74 (un composable, tres documentos)
└── navigation/RolaNavHost.kt      # rutas ajustes y legales, hijas de Inicio

app/src/test/java/com/luis/rola/core/data/
├── TimeAgoTest.kt, ThemeModeTest.kt, LibraryScannerTest.kt

app/src/androidTest/java/com/luis/rola/
├── feature/settings/SettingsScreenTest.kt, LegalScreenTest.kt
├── feature/home/HomeScreenTest.kt         # engrane y «Abrir Ajustes»
└── feature/library/PreferenciasEnMemoria.kt  # + theme, lastScanAt
```

**Decisión de estructura**: se mantiene el módulo `app` y los paquetes de DEC-20 (`feature.settings` ya está previsto).

## Seguimiento de complejidad

Sin violaciones de la constitución.
