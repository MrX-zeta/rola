# Investigación: Onboarding completo

Cada punto: **Decisión**, **Por qué** y **Alternativas descartadas**. No quedan «NEEDS CLARIFICATION». Las decisiones de movimiento salen de la skill `animate` y de DEC-28.

## 1. DataStore Preferences detrás de una interfaz

- **Decisión**: `interface PreferencesRepository { val onboardingCompleted: Flow<Boolean>; suspend fun completeOnboarding() }` en `core/data`. La implementación `DataStorePreferencesRepository(dataStore: DataStore<Preferences>)` lee `booleanPreferencesKey("onboarding_completed")` (falso si no existe) y escribe `true` con `edit`. La instancia de DataStore se crea una sola vez con `preferencesDataStore(name = "rola_ajustes")` en `AppContainer` (DataStore exige un solo dueño por archivo). Si la lectura falla con `IOException`, se emiten preferencias vacías (la bienvenida se vuelve a mostrar: es el lado seguro).
- **Por qué**: lo pide DEC-18 y la tarea (dependencia permitida); la interfaz permite el falso en memoria de las pruebas de Compose y la prueba JUnit usa DataStore real con un archivo temporal (`PreferenceDataStoreFactory.create { archivo }`), sin Android.
- **Descartado**: `SharedPreferences` (DEC-18 y la tarea dicen DataStore); un ViewModel solo para un booleano (Ponytail, artículo X); crear ya los demás campos de KV-DATASTORE (están «propuestos», no se asumen).

## 2. Arranque: la bienvenida es una puerta, sin parpadeo

- **Decisión**: en `MainActivity`, `installSplashScreen().setKeepOnScreenCondition { completado == null }` mientras el `Flow` no emite. Luego: `completado == false` → `OnboardingScreen(onFinish = { scope.launch { repo.completeOnboarding() } })`; `true` → `MusicPermissionGate { RolaNavHost(container) }`. Al escribir `true`, el `Flow` emite y la puerta cambia sola a Permisos (o directo a Biblioteca si el permiso ya estaba). La decisión vive en un composable `OnboardingGate(completed: Boolean?, onComplete, content)` para probarla en Compose sin `MainActivity`.
- **Por qué**: DEC-17 prohíbe un splash propio y demoras artificiales; mantener el splash del sistema solo mientras se lee un archivo pequeño evita mostrar Biblioteca o la bienvenida por un cuadro (SC-002). La puerta repite el patrón de `MusicPermissionGate`.
- **Descartado**: ruta de onboarding en el `NavHost` (Biblioteca vive fuera del `NavHost` y la barra inferior no debe verse en la bienvenida); `runBlocking` para leer el valor (bloquea el hilo principal).

## 3. Pager

- **Decisión**: `HorizontalPager(state = rememberPagerState { 3 })` solo con la ilustración y el texto de cada página; barra superior, indicador y botón quedan fijos fuera del pager (en Figma solo cambian de estado). «Siguiente» → `animateScrollToPage(actual + 1, animationSpec = RolaMotion.resorte())`; en la página 3 el mismo botón llama a `onFinish`. La página actual se guarda con el estado del pager (`rememberPagerState` es guardable: sobrevive a rotación). `BackHandler(enabled = página > 0)` vuelve una página; en la 1 Atrás sale de la app.
- **«Omitir»**: en las páginas 1 y 2, como en Figma (la 3 no lo lleva: ahí está «Empezar»). Al llegar a la 3 se desvanece (`alpha` animado `RAPIDA`) y deja de recibir toques y foco; no se quita del árbol para que el logo no se mueva.
- **Descartado**: `Navigation` entre tres rutas (no se desliza con el dedo); `AnimatedContent` (tampoco).

## 4. Movimiento (skill `animate`, DEC-28)

**Puerta**: la bienvenida se ve una vez (rara, primera vez): cabe una entrada con carácter, pero cada animación debe nombrar su propósito.

- **Indicador que se estira** (consistencia espacial: dice hacia dónde vas). Se dibuja en un `Canvas` (3 puntos de 8 dp, activo 24 dp, separación 8 dp) a partir de `posición = currentPage + currentPageOffsetFraction`, leída al dibujar. Entre la página *i* e *i+1* con avance *f*: el borde delantero llega a su destino con `min(1, 2f)` y el trasero lo sigue con `max(0, 2f − 1)`, así la marca se estira hacia la página nueva y luego se recoge. Atado al dedo: se interrumpe y revierte solo, sin `tween` propio. Colores `primary` (activo) y `onSurfaceVariant` (inactivos).
- **Ilustraciones** (deleite y explicación; solo en la bienvenida). Cada ilustración lee `distancia = |página − posición|` en su `graphicsLayer`: `escala = 1 − 0.06·d` (nunca menos de 0.94), `alpha = 1 − 0.4·d`, sin traslado propio (ya lo mueve el pager). La página 1, al primer cuadro dibujado (`withFrameNanos`, DEC-28 `arranque_en_primer_cuadro`), entra con `alpha 0→1` y `escala 0.96→1` en `LENTA` (250 ms) con `RolaMotion.EaseOut`. Nunca escala desde 0.
- **Botón sin saltos** (evitar un cambio brusco): ancho completo y alto fijo de 56 dp, así que el cambio de «Siguiente» a «Empezar» no cambia su tamaño. El texto cambia con `AnimatedContent`: entra con fundido `MEDIA` (150 ms) y sale con `RAPIDA` (100 ms), sin traslado.
- **Reducir movimiento** (FR-015, aclaración 5): las páginas no se deslizan: cada página aplica `translationX = desplazamiento · ancho` (contrarresta el movimiento del pager) y `alpha = 1 − |desplazamiento|`, así el cambio es un fundido que sigue al dedo o a «Siguiente»; «Siguiente» usa `animateScrollToPage` con `tween(MEDIA)`. Sin escala de ilustraciones ni entrada de la página 1. El indicador sigue la posición (es estado, no decoración).
- **Revisar a mano** (no se decide desde el código): el estiramiento del indicador y la escala 0.94 en el Redmi con `perfil`, a 1× y con las animaciones del sistema a 5×.

## 5. Ilustraciones con formas del tema

- **Decisión**: se construyen en Compose con las medidas de Figma: portadas genéricas (cuadrados redondeados de color con barras o un círculo, como el componente «Portada» 5:44), tarjeta de letra (textos del tema y un chip), círculo con wifi tachado y la fila «Buscar letras en línea» con el `Switch` de M3 apagado y sin acción (`onCheckedChange = null`), decorativa. Los colores de las portadas de ejemplo son tokens nuevos en `Color.kt` (azul, naranja, verde y sus tonos claros, tomados de los SVG de Figma) porque no son roles del tema. Íconos `wifi` y `lyrics`: assets de Figma convertidos a vector en `res/drawable`; el tachado, una línea dibujada.
- **Relieve (DEC-27)**: portadas y círculo → `elevado`; tarjeta de letra y fila del interruptor → `elevado`; botón reproducir de la ilustración y botón principal → `acento`.
- **Descartado**: exportar las ilustraciones enteras como imágenes (no siguen el tema claro/oscuro y DEC-27 pide relieve del sistema); fotos de discos reales (aclaración 3).

## 6. Permisos y Permiso denegado contra Figma

- **Decisión**: Permisos (23:332) queda con una sola fila («Música y audio», ícono en `primaryContainer`), sin divisor ni etiquetas «Ahora/Después». Se quitan los textos y el ícono de Notificaciones si quedan sin uso. Permiso denegado (23:391) se compara y se corrige solo lo que difiera.
- **Por qué**: DEC-18 (no se pide POST_NOTIFICATIONS) y la tarea.

## 7. Tema claro

- **Decisión**: la app sigue en oscuro fijo hasta el ajuste de tema (DEC-15, `RolaTheme(darkTheme = true)`). Las pantallas usan solo tokens y el relieve ya distingue claro/oscuro, así que el modo claro se revisa con vistas previas `@Preview` claras y una prueba de captura en el emulador con `RolaTheme(darkTheme = false)`.
