---

description: "Tareas de implementación de Ajustes y legal"
---

# Tareas: Ajustes y legal

**Entrada**: `specs/007-ajustes-y-legal/` (plan.md, spec.md, research.md, data-model.md, contracts/interfaces.md, quickstart.md)

**Pruebas**: obligatorias por DEC-22 y por la tarea 08 (Paso 6). Cada prueba va junto a su código.

**Organización**: por historia de la spec. La Historia 2 (llegar a Ajustes) va primero porque las demás viven en esa pantalla; luego Historia 1 (tema), 3, 4 y 5. Commits atómicos por área: `fix(app)`, `feat(data)`, `feat(settings)`, `feat(theme)`, `feat(home)`.

## Formato: `[ID] [P?] [Historia] Descripción`

- **[P]**: puede hacerse en paralelo (archivos distintos, sin dependencias pendientes).
- **[USn]**: historia de la spec.

## Convenciones de rutas

- Código: `app/src/main/java/com/luis/rola/` (`rola/`). Unitarias: `app/src/test/java/com/luis/rola/` (`test/`). Emulador: `app/src/androidTest/java/com/luis/rola/` (`androidTest/`).
- Textos en `app/src/main/res/values/strings.xml`, en español, con plurales. Colores, tipos, formas, espacios y movimiento solo desde `rola/core/designsystem` (INV-04).
- Antes de leer un nodo con `get_design_context`, cargar la skill `figma-design-to-code`.

---

## Fase 1: Preparación

- [X] T001 Desactivar la copia de seguridad: `android:allowBackup="false"` y quitar `android:fullBackupContent` en `app/src/main/AndroidManifest.xml`; borrar `app/src/main/res/xml/backup_rules.xml`; en `app/src/main/res/xml/data_extraction_rules.xml` excluir los dominios `root`, `file`, `database`, `sharedpref` y `external` en `cloud-backup` y en `device-transfer` (FR-060, research §9). Commit `fix(app): desactivar la copia de seguridad` (primer commit de código)
- [X] T002 [P] Activar `buildFeatures.buildConfig = true` en `app/build.gradle.kts` (FR-053)
- [X] T003 [P] Agregar los íconos de Figma como vectores de 24 dp: engrane (encabezado de Inicio, 30:…), `icon/palette`, `icon/document`, `icon/shield` (SCR-70 40:1772, 40:1796, 40:1806) en `app/src/main/res/drawable/ic_settings.xml`, `ic_palette.xml`, `ic_document.xml`, `ic_shield.xml`

---

## Fase 2: Base (bloquea todas las historias)

- [X] T004 [P] Prueba JUnit de `ThemeMode.parse`: `"system"`, `"dark"`, `"light"` → su valor; `null`, `""` y desconocido → `DARK`; ida y vuelta con `key()`, en `test/core/data/ThemeModeTest.kt`
- [X] T005 Crear `ThemeMode` (`SYSTEM`, `DARK`, `LIGHT`; `parse` tolerante con «desconocido → DARK»; `key()` = `system|dark|light`) en `rola/core/data/ThemeMode.kt`
- [X] T006 Sumar a `PreferencesRepository` y `DataStorePreferencesRepository` `theme: Flow<ThemeMode>` / `setTheme` (clave `theme`, por defecto `dark`) y `lastScanAt: Flow<Long?>` / `setLastScanAt` (clave `last_scan_at`, ms de época, ausente → `null`), con el mismo manejo de `IOException` que `sort_order` (FR-075), en `rola/core/data/PreferencesRepository.kt`
- [X] T007 Implementar los miembros nuevos en el doble de pruebas `androidTest/feature/library/PreferenciasEnMemoria.kt` (y en cualquier otro falso de `PreferencesRepository` que exista)

**Punto de control**: compila; `ThemeModeTest` en verde. Commit `feat(data): tema y último escaneo en ajustes`.

---

## Fase 3: Historia 2 · Llegar a Ajustes y volver (P1) 🎯 MVP

**Meta**: Ajustes existe con sus filas permitidas y se llega desde Inicio y desde SCR-12.

**Prueba independiente**: engrane → Ajustes → atrás; sin canciones, «Abrir Ajustes» → Ajustes → atrás.

- [X] T008 [US2] Leer SCR-70 con `get_design_context` (40:1674 y claro 51:5129) y construir `SettingsScreen` plano: barra superior (flecha atrás 48 dp con descripción, título «Ajustes»), etiquetas de sección y tarjetas `surfaceContainer` con filas de ícono, título, subtítulo y valor/chevron; secciones REPRODUCCIÓN (Temporizador de apagado), BIBLIOTECA (Escanear medios), APARIENCIA (Tema), ACERCA DE (Términos y condiciones, Aviso de privacidad, Licencias, Versión); **sin** LETRAS, «Carpeta de letras» ni «Agregar widget» (FR-005, FR-006); filas con rol de botón y ≥ 48 dp, desplazable con letra grande, en `rola/feature/settings/SettingsScreen.kt`
- [X] T009 [US2] ~~Crear `SettingsViewModel~~ (no hizo falta: `RolaNavHost` lee el tema y la fecha del repositorio y el escaneo de `LibraryScanner`, como ya hace con el resto). Crear `SettingsViewModel(preferences, scanner)` con `theme`, `lastScanAt`, `scanning` como `StateFlow` y `setTheme` (contrato), y su `factory(container)`, en `rola/feature/settings/SettingsViewModel.kt` (depende de T020 para `scanner`; hasta entonces puede recibir el escáner como parámetro)
- [X] T010 [US2] Rutas `ajustes` y `ajustes/{doc}` en el `NavHost`, hijas de la pestaña dueña (la píldora no salta), con «atrás» por flecha y gesto, en `rola/navigation/RolaNavHost.kt` (FR-001, FR-002, FR-004)
- [X] T011 [US2] Mostrar el engrane (etiqueta `ajustes_engrane`, descripción «Ajustes») en el encabezado de Inicio según SCR-10 (Figma 51:2090 / oscuro) y el botón «Abrir Ajustes» (etiqueta `abrir_ajustes`) en SCR-12 (30:512 / 51:2302), ambos con `onSettings`, en `rola/feature/home/HomeScreen.kt`
- [X] T012 [US2] Corregir `sin_musica_texto` en `app/src/main/res/values/strings.xml` para que diga solo lo que Ajustes ofrece (escanear medios; sin selector de carpetas) (FR-003)
- [ ] T013 [P] [US2] Prueba Compose: Ajustes muestra las siete filas permitidas y no muestra «Carpeta de letras», «Buscar letras en internet», «Letras descargadas» ni «Agregar widget», en `androidTest/feature/settings/SettingsScreenTest.kt`
- [ ] T014 [P] [US2] Prueba Compose: el engrane y «Abrir Ajustes» llaman a `onSettings`, en `androidTest/feature/home/HomeScreenTest.kt`; y de navegación: engrane → Ajustes → atrás regresa a Inicio con la píldora en Inicio, en `androidTest/navigation/RolaNavBarTest.kt` (o la prueba de navegación existente)

**Punto de control**: Ajustes accesible y vacío de acciones. Commit `feat(settings): pantalla de ajustes y accesos`.

---

## Fase 4: Historia 1 · Elegir el tema (P1)

**Meta**: Sistema/Oscuro/Claro al instante, recordado y sin parpadeo al abrir.

**Prueba independiente**: elegir Claro, ver la app en claro; reabrir y verla en claro desde el primer cuadro.

- [X] T015 [US1] Cargar la skill `animate` y decidir el fundido del tema (duración ≤ 300 ms, curva de salida, qué pasa si se cambia otra vez durante el fundido, «reducir movimiento»); anotar la decisión en `specs/007-ajustes-y-legal/research.md` §3
- [X] T016 [US1] `RolaTheme(themeMode)`: resolver oscuro/claro (`SYSTEM` → `isSystemInDarkTheme()`), grabar la raíz en un `GraphicsLayer`, y al cambiar el tema capturar el cuadro actual antes de aplicar el nuevo y dibujar la captura encima con alfa 1 → 0 según T015, sin semántica ni toques; actualizar el comentario de cabecera, en `rola/core/designsystem/Theme.kt` (FR-012, FR-014, FR-071)
- [X] T017 [US1] `MainActivity`: leer `theme` junto con la bienvenida y sostener el splash hasta tener ambos; pasar el modo a `RolaTheme`; volver a llamar `enableEdgeToEdge` con `SystemBarStyle.dark`/`light` (transparente) cada vez que cambia el tema resuelto, en `rola/MainActivity.kt` (FR-012, FR-013)
- [X] T018 [US1] Leer la hoja de ordenar (33:1070) y crear `ThemeSheet` con «Sistema», «Oscuro» y «Claro» como opciones de radio con estado anunciado, la actual marcada; al elegir se cierra y llama `onTheme`, en `rola/feature/settings/ThemeSheet.kt`; la fila Tema muestra el valor y abre la hoja (FR-011)
- [ ] T019 [P] [US1] Prueba Compose: la fila Tema dice «Oscuro» por defecto; elegir «Claro» cambia la fila y queda guardado en el repositorio en memoria; al recrear la pantalla sigue «Claro», en `androidTest/feature/settings/SettingsScreenTest.kt`

**Punto de control**: tema completo. Commit `feat(theme): tema sistema, oscuro o claro`.

---

## Fase 5: Historia 3 · Escanear mis medios (P2)

**Meta**: escanear desde Ajustes, un escaneo a la vez, y ver «Último escaneo».

**Prueba independiente**: tocar «Escanear medios», ver progreso, aviso y «hace un momento».

- [X] T020 [P] [US3] Prueba JUnit de `LibraryScanner` con `SongRepository` y preferencias falsas: `scanning` es `true` durante el escaneo y `false` después (también si falla); guarda `last_scan_at` con el reloj inyectado; un segundo `scan()` mientras corre devuelve `null` y no escanea dos veces, en `test/core/data/LibraryScannerTest.kt`
- [X] T021 [US3] Crear `LibraryScanner` (contrato) en `rola/core/data/LibraryScanner.kt`, armarlo en `rola/AppContainer.kt` y pasar `HomeViewModel` a usarlo (quitar su `_scanning`) en `rola/feature/home/HomeViewModel.kt` (FR-040, FR-042)
- [X] T022 [P] [US3] Prueba JUnit de `timeAgo`: 0 s y −5 min → `Moment`; 59 s → `Moment`; 60 s → `Minutes(1)`; 59 min → `Minutes(59)`; 60 min → `Hours(1)`; 23 h 59 min → `Hours(23)`; 24 h → `Days(1)`; 2 días → `Days(2)`, en `test/core/data/TimeAgoTest.kt`
- [X] T023 [US3] Crear `TimeAgo` y `timeAgo(now, then)` con los tramos de `data-model.md` («< 60 s o negativa → Moment; < 60 min → Minutes; < 24 h → Hours; ≥ 24 h → Days», redondeo hacia abajo) en `rola/core/data/TimeAgo.kt`
- [X] T024 [US3] Textos y plurales: «Último escaneo: %s», «hace un momento», «hace %d minuto(s)», «hace %d hora(s)», «hace %d día(s)», «Aún no has escaneado», en `app/src/main/res/values/strings.xml` (FR-043, FR-044)
- [X] T025 [US3] Fila «Escanear medios»: subtítulo con `timeAgo` (refresco al entrar y cada minuto visible) o «Aún no has escaneado»; durante el escaneo muestra progreso y no acepta toques; el toque lanza el escaneo en el ámbito de avisos de `RolaNavHost` con el mismo aviso de resultado que SCR-12, en `rola/feature/settings/SettingsScreen.kt` y `rola/navigation/RolaNavHost.kt` (FR-041)
- [ ] T026 [P] [US3] Prueba Compose: sin fecha dice «Aún no has escaneado»; con una fecha de hace 2 días dice «Último escaneo: hace 2 días»; con `scanning = true` la fila no llama a `onScan`, en `androidTest/feature/settings/SettingsScreenTest.kt`

**Punto de control**: escaneo compartido. Commit `feat(settings): escanear medios y último escaneo`.

---

## Fase 6: Historia 4 · Temporizador desde Ajustes (P2)

**Meta**: programar el temporizador desde Ajustes y ver cuánto falta.

**Prueba independiente**: elegir 15 minutos en Ajustes y ver el mismo temporizador en Reproduciendo.

- [X] T027 [US4] Mover el texto del aviso al elegir una opción (hoy dentro de `PlayerOverlay` en `rola/navigation/RolaNavHost.kt`) a una función compartida en `rola/feature/player/SleepTimerSheet.kt`, y usarla en los dos lugares
- [X] T028 [US4] Fila «Temporizador de apagado»: valor «Apagado», el tiempo restante con `formatDuration` (cuenta cada segundo, como la hoja) o «Al terminar la canción»; el toque abre `SleepTimerSheet` con el estado de `PlayerConnection` y al elegir llama `player.setSleepTimer` y muestra el aviso, en `rola/feature/settings/SettingsScreen.kt` y `rola/navigation/RolaNavHost.kt` (FR-020, FR-021)
- [ ] T029 [P] [US4] Prueba Compose: con `timer = OFF` la fila dice «Apagado»; con `END_OF_SONG` dice «Al terminar la canción»; con un fin dentro de 15 min muestra un tiempo; el toque abre la hoja, en `androidTest/feature/settings/SettingsScreenTest.kt`

**Punto de control**: temporizador compartido. Commit `feat(settings): temporizador desde ajustes`.

---

## Fase 7: Historia 5 · Leer lo legal y ver la versión (P2)

**Meta**: tres documentos completos y la versión real.

**Prueba independiente**: abrir cada documento, llegar al final y volver; ver «Rola 0.1.0».

- [X] T030 [US5] Leer completos en Figma Términos (41:1778), Privacidad (41:1810) y Licencias (41:1842) y copiarlos a `app/src/main/res/values/strings.xml` (título, «Última actualización», encabezados y párrafos); Licencias suma Coil, Palette y Reorderable (Apache 2.0) (FR-050 a FR-052)
- [X] T031 [US5] Crear `LegalDoc` (`TERMS`, `PRIVACY`, `LICENSES` con sus recursos) y `LegalScreen` plana: barra con flecha (descripción) y título con rol de encabezado; columna desplazable con la fecha, encabezados (rol de encabezado) y párrafos, en `rola/feature/settings/LegalScreen.kt`; conectar las tres filas a `ajustes/{doc}`
- [X] T032 [US5] Fila «Versión»: «Rola %s» con `BuildConfig.VERSION_NAME`, en `rola/feature/settings/SettingsScreen.kt` (FR-053)
- [ ] T033 [P] [US5] Prueba Compose: cada documento muestra su título como encabezado, se desplaza hasta su último párrafo y la flecha llama a `onBack`; Licencias contiene «Coil», «Palette» y «Reorderable»; la fila Versión muestra `BuildConfig.VERSION_NAME`, en `androidTest/feature/settings/LegalScreenTest.kt`

**Punto de control**: legal completo. Commit `feat(settings): términos, privacidad, licencias y versión`.

---

## Fase 8: Pulido y comprobaciones

- [ ] T034 Tema claro en el emulador: capturas de Inicio, Biblioteca (4 pestañas), Buscar, Reproduciendo, Cola, Temporizador, Ajustes y las tres legales; comparar con los nodos claros (Inicio 51:2090, Canciones 51:2359, Álbumes 51:2577, Artistas 51:2737, Listas 51:2902, Buscar 51:4081, Reproduciendo 51:4482, Cola 51:4570, Temporizador 51:4662, Ajustes 51:5129, legales 51:5538/5570/5602) y anotar cada diferencia para el reporte
- [ ] T035 `design-critique` de SCR-70, 72, 73 y 74 en oscuro y en claro contra Figma; anotar diferencias
- [ ] T036 Revisar el contraste de los textos en claro (≥ 4.5:1) con `rola/core/designsystem/Contrast.kt` donde aplique
- [ ] T037 `.\gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y `:app:connectedDebugAndroidTest`; corregir lo que falle
- [ ] T038 Capturas finales en `ai/handoff/capturas/`: `t08-ajustes.png`, `t08-ajustes-claro.png`, `t08-tema.png`, `t08-terminos.png`, `t08-privacidad.png`, `t08-licencias.png`, `t08-inicio-claro.png`, `t08-reproduciendo-claro.png`
- [ ] T039 `/ponytail:ponytail-review` sobre el diff antes de cada commit; `git log` sin autoría de IA; push a `origin development`

---

## Dependencias

- Fase 1 → Fase 2 → Fase 3 (Ajustes y rutas) → Fases 4, 5, 6 y 7 (independientes entre sí; todas tocan `SettingsScreen.kt` y `RolaNavHost.kt`, así que se hacen en serie) → Fase 8.
- T009 necesita `LibraryScanner` (T021); si se hace antes, el ViewModel recibe el escáner al llegar a la Fase 5.
- T016 necesita T015; T017 necesita T006 y T016.

## Paralelo

- Fase 1: T002 y T003 junto a T001.
- Fase 2: T004 mientras se escribe T006.
- Dentro de cada historia, las pruebas marcadas [P] van en archivos distintos del código que prueban.

## Estrategia

- MVP: Fases 1–3 (copia de seguridad apagada, Ajustes visible y navegable).
- Luego el tema (Fase 4), que es lo que la tarea más necesita ver en el teléfono, y después escaneo, temporizador y legal.
