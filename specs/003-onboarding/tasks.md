---

description: "Tareas de implementación del onboarding completo"
---

# Tareas: Onboarding completo

**Entrada**: `specs/003-onboarding/` (plan.md, spec.md, research.md, data-model.md, contracts/interfaces.md, quickstart.md)

**Pruebas**: obligatorias por DEC-22 y por la tarea 04 (Paso B6). JUnit: repositorio de preferencias con DataStore real en un archivo temporal. Compose (falso en memoria): «Omitir» guarda y lleva a Permisos; «Empezar» en la página 3; con reducir movimiento el cambio de página no desplaza; con `onboarding_completed` y permiso la app abre en Biblioteca. Cada prueba va junto a su código.

**Organización**: por historia de la spec (US1…US3). Commits atómicos por área (`feat(data): …`, `feat(onboarding): …`).

## Formato: `[ID] [P?] [Historia] Descripción`

- **[P]**: puede hacerse en paralelo (archivos distintos, sin dependencias pendientes).
- **[USn]**: historia de la spec.

## Convenciones de rutas

- Código: `app/src/main/java/com/luis/rola/` (en las tareas, `rola/`).
- Unitarias: `app/src/test/java/com/luis/rola/` (`test/`).
- Compose: `app/src/androidTest/java/com/luis/rola/` (`androidTest/`).
- Recursos: `app/src/main/res/` (`res/`).
- Textos en `res/values/strings.xml`, en español. Colores, tipos, formas, espacios, relieve y movimiento solo desde `rola/core/designsystem` (INV-04, `RolaMotion`, `Relief.kt`).

---

## Fase 1: Preparación

- [ ] T001 Agregar `androidx.datastore:datastore-preferences` 1.3.0 a `gradle/libs.versions.toml` (`datastore = "1.3.0"`, alias `androidx-datastore-preferences`) y `implementation(libs.androidx.datastore.preferences)` a `app/build.gradle.kts`; comprobar que compila
- [ ] T002 [P] Descargar de Figma los assets `lyrics` (nodo 22:365) y `wifi` (nodo 22:410) y convertirlos a vectores en `res/drawable/ic_lyrics.xml` (16 dp) e `ic_wifi.xml` (24 dp de viewport, se dibuja más grande), relleno blanco como los demás `ic_*`; el tachado (22:412) se dibuja en código, no como asset
- [ ] T003 [P] Agregar a `rola/core/designsystem/Color.kt` los colores de las portadas de ejemplo de Figma (componente «Portada» 5:44: azul `#2F4BB8` con barras `#9FB4FF`; naranja y verde con sus tonos claros, leídos de los SVG de 22:306 y 22:308) como un objeto de tokens de ilustración, iguales en claro y oscuro
- [ ] T004 [P] Agregar a `res/values/strings.xml` los textos de las tres páginas (títulos y cuerpos de 22:314/315, 22:374/375, 22:422/423), «Omitir», «Empezar», los textos de la tarjeta de letra (22:355–22:372), «Buscar letras en línea», «Apagado: nada sale de tu teléfono» y la descripción del indicador «Página %1$d de %2$d»

---

## Fase 2: Base (bloquea todas las historias)

- [ ] T005 Crear `rola/core/data/PreferencesRepository.kt`: interfaz `PreferencesRepository` (`onboardingCompleted: Flow<Boolean>`, `suspend fun completeOnboarding()`) y `DataStorePreferencesRepository(DataStore<Preferences>)` con la clave `booleanPreferencesKey("onboarding_completed")`, «`false` (clave ausente)» por defecto, `IOException` al leer → preferencias vacías (otros errores se propagan), escritura idempotente con `edit` (contracts/interfaces.md, data-model.md)
- [ ] T006 Prueba `test/core/data/PreferencesRepositoryTest.kt` (JUnit, DataStore real con `PreferenceDataStoreFactory.create` en un archivo temporal y `runTest`): empieza en `false`; tras `completeOnboarding()` emite `true`; llamarlo dos veces no falla; un segundo DataStore sobre el mismo archivo lee `true` (persistencia)
- [ ] T007 Crear el DataStore una sola vez en `rola/AppContainer.kt` (`preferencesDataStore(name = "rola_ajustes")` como extensión de `Context`) y exponer `val preferences: PreferencesRepository`

**Punto de control**: el repositorio pasa su prueba; la app compila sin cambios visibles.

---

## Fase 3: Historia 1 · Conocer Rola la primera vez (P1) 🎯 MVP

**Meta**: la bienvenida de tres páginas con pager, indicador, «Siguiente»/«Empezar» y «Omitir».

**Prueba independiente**: con un repositorio falso en `false`, recorrer las tres páginas con «Siguiente» y con el dedo; «Empezar» y «Omitir» llaman a `onFinish`.

- [ ] T008 [US1] Crear `rola/feature/onboarding/OnboardingIllustrations.kt` con tres composables decorativos (`clearAndSetSemantics {}`), con medidas de Figma y relieve DEC-27: `PortadasEnAbanico` (22:301: tres portadas de 150/196 dp giradas −10°/+10° con `elevado`, botón reproducir lima de 72 dp con `acento` e ícono `ic_play`), `TarjetaDeLetra` (22:348: 348 dp máx. de ancho, `surfaceContainer`, radio 24, `elevado`; portada 44 dp, título, artista, `1:24` en tipo de tiempos, cuatro líneas en tipo de titular con `primary` en la activa, chip «Sincronizada» en `primaryContainer` con `ic_lyrics` y chip «.lrc o LRCLIB» con borde `outline`) y `SinConexion` (22:406: círculo de 148 dp `surfaceContainer` con `elevado`, `ic_wifi` en `onSurfaceVariant` y una línea de tachado; fila «Buscar letras en línea» en `surface`, radio 20, `elevado`, con `Switch` de M3 apagado y `onCheckedChange = null`). Ancho máximo 348 dp y que se reduzca en pantallas angostas
- [ ] T009 [US1] Crear `rola/feature/onboarding/OnboardingScreen.kt` (`testTag("bienvenida")`): fondo `background`, `systemBarsPadding`; barra superior con `RolaLogo()` y «Omitir» (`TextButton`, `titleSmall` semibold en `onSurfaceVariant`, ≥ 48 dp) visible en páginas 1–2 y desvanecido en la 3 (`alpha` animado `RAPIDA`, sin toques ni foco, sin quitarlo del árbol); `HorizontalPager(rememberPagerState { 3 })` con ilustración centrada (peso 1) y bloque de texto (título `headlineLarge`, cuerpo `bodyLarge` en `onSurfaceVariant`, 24 dp de margen); pie fijo con indicador y `BotonPrincipal` (de `PermissionScreen.kt`, 56 dp) cuyo texto pasa de «Siguiente» a «Empezar» en la página 3 con `AnimatedContent` (entra `MEDIA`, sale `RAPIDA`, sin traslado); «Siguiente» → `animateScrollToPage(+1, RolaMotion.resorte())`; «Empezar» y «Omitir» → `onFinish()`; `BackHandler(enabled = página > 0)` vuelve una página
- [ ] T010 [US1] En `OnboardingScreen.kt`, indicador en `Canvas` (`testTag("indicador")`, `stateDescription` «Página n de 3»): tres marcas de 8 dp (activa 24 dp, separación 8 dp, radio 4), activa `primary` e inactivas `onSurfaceVariant`; la posición `currentPage + currentPageOffsetFraction` se lee al dibujar; entre *i* e *i+1* con avance *f*, borde delantero con `min(1, 2f)` y trasero con `max(0, 2f − 1)` (research.md §4)
- [ ] T011 [US1] En `OnboardingScreen.kt`, movimiento de las ilustraciones (research.md §4): `graphicsLayer` con `d = |página − posición|`, `escala = 1 − 0.06·d` (mín. 0.94), `alpha = 1 − 0.4·d`; entrada de la página 1 al primer cuadro dibujado (`withFrameNanos`): `alpha 0→1` y `escala 0.96→1`, `LENTA` con `RolaMotion.EaseOut`. Con `LocalReduceMotion`: cada página aplica `translationX = desplazamiento · ancho` y `alpha = 1 − |desplazamiento|` (fundido sin desplazamiento), «Siguiente» usa `tween(MEDIA)`, sin escala ni entrada
- [ ] T012 [US1] Vistas previas `@Preview` de `OnboardingScreen` en oscuro y claro (`RolaTheme(darkTheme = false)`) en `OnboardingScreen.kt`
- [ ] T013 [US1] Prueba `androidTest/feature/onboarding/OnboardingTest.kt`: «Siguiente» dos veces muestra «Empezar» y oculta «Omitir»; «Empezar» en la página 3 llama a `onFinish`; con `LocalReduceMotion provides true`, durante el cambio de página la ilustración de la página nueva no cambia su posición horizontal (medir `boundsInRoot` a mitad del cambio con el reloj manual) y su opacidad sube

**Punto de control**: la bienvenida funciona sola con un `onFinish` de prueba.

---

## Fase 4: Historia 2 · Dar el permiso de música (P1)

**Meta**: «Empezar»/«Omitir» guardan `onboarding_completed` y llevan a Permisos con una sola fila.

**Prueba independiente**: con el repositorio falso en `false`, «Omitir» lo pone en `true` y aparece Permisos.

- [ ] T014 [US2] Crear en `rola/feature/onboarding/OnboardingScreen.kt` el composable `OnboardingGate(completed: Boolean?, onComplete: () -> Unit, content)`: `null` → nada (el splash sigue), `false` → `OnboardingScreen(onFinish = onComplete)`, `true` → `content()`
- [ ] T015 [US2] En `rola/MainActivity.kt`: leer `container.preferences.onboardingCompleted` con `collectAsStateWithLifecycle(null)`, `installSplashScreen().setKeepOnScreenCondition { completado == null }`, y montar `OnboardingGate(completado, onComplete = { scope.launch { preferences.completeOnboarding() } }) { MusicPermissionGate { RolaNavHost(container) } }`
- [ ] T016 [US2] Ajustar `rola/feature/onboarding/PermissionScreen.kt` a Figma 23:332: una sola fila «Música y audio» (ícono `ic_music_note` en `primaryContainer`/`onPrimaryContainer`), sin divisor ni etiquetas «Ahora/Después», tarjeta con `elevado` (radio 24) y nota de privacidad con `ic_check`; quitar `ic_notifications.xml` y los textos `permiso_notificaciones*`, `etiqueta_ahora`, `etiqueta_despues` si quedan sin uso (comprobar con grep)
- [ ] T017 [P] [US2] Comparar `rola/feature/onboarding/PermissionDeniedScreen.kt` con Figma 23:391 (círculo de 120 dp con `elevado`, `ic_music_note` de 56 dp, título 28 sp centrado, ruta en `surfaceContainer` radio 16 sin relieve, «Abrir Ajustes» con `acento`, «Intentar de nuevo» sin relieve) y corregir solo lo que difiera
- [ ] T018 [US2] Prueba en `androidTest/feature/onboarding/OnboardingTest.kt` con repositorio falso (`MutableStateFlow(false)`): `OnboardingGate` con «Omitir» pone el valor en `true` y muestra el contenido siguiente (Permisos con `PermissionScreen`, comprobando «Un permiso y listo.» y que no existe «Notificaciones»)

**Punto de control**: de la bienvenida se llega a Permisos y de ahí a Biblioteca.

---

## Fase 5: Historia 3 · Volver a abrir Rola (P2)

**Meta**: con la bienvenida completada no se vuelve a ver; con permiso, se abre directo en Biblioteca.

**Prueba independiente**: con el repositorio falso en `true` y el permiso concedido, la primera pantalla es Biblioteca.

- [ ] T019 [US3] Prueba en `androidTest/feature/onboarding/OnboardingTest.kt`: con `GrantPermissionRule` de `musicPermission`, `OnboardingGate(true, …) { MusicPermissionGate { contenido con testTag("biblioteca") } }` muestra el contenido y no existe `bienvenida`; con `null` no se muestra nada
- [ ] T020 [US3] Verificar en el emulador el arranque sin parpadeo (`adb shell am start -W` tras cerrar la app con el valor en `true`; capturas cada ~50 ms en los primeros cuadros no muestran la bienvenida) per SC-002

**Punto de control**: las tres historias funcionan.

---

## Fase 6: Pulido y comprobaciones

- [ ] T021 Recorrer `quickstart.md` en el emulador (`pm clear` antes) y tomar `ai/handoff/capturas/t04-onboarding-1.png`, `t04-onboarding-2.png`, `t04-onboarding-3.png`, `t04-permisos.png`
- [ ] T022 `design-critique` de SCR-02 a SCR-06 contra las capturas de Figma (oscuro y claro) y anotar las diferencias en el reporte
- [ ] T023 Letra del sistema al máximo y «Quitar animaciones»: revisar que nada se corte y que el cambio de página sea un fundido (FR-015, FR-020)
- [ ] T024 `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y `:app:connectedDebugAndroidTest`; instalar `perfil` en el Redmi si aparece en adb
- [ ] T025 Marcar en `specs/003-onboarding/tasks.md` lo completado

---

## Dependencias y orden

- Fase 1 → Fase 2 → US1 → US2 → US3 → Pulido.
- US2 necesita `OnboardingScreen` (US1) y el repositorio (Fase 2). US3 necesita `OnboardingGate` (US2).
- Dentro de US1: T008 antes de T009; T010 y T011 tocan el mismo archivo que T009 (en orden).

## Paralelo

- Fase 1: T002, T003 y T004 a la vez (archivos distintos) después de T001.
- US2: T017 en paralelo con T014–T016.

## Estrategia

- MVP: Fases 1–3 (la bienvenida sola). Luego US2 conecta la bienvenida al arranque y a Permisos; US3 solo prueba y verifica lo que ya hace la puerta.
