# Plan de implementación: Onboarding completo

**Carpeta**: `specs/003-onboarding` | **Fecha**: 2026-11-15 | **Spec**: [spec.md](spec.md)

**Entrada**: especificación de `specs/003-onboarding/spec.md` y restricciones del Paso B3 de la tarea 04.

## Resumen

Antes de Permisos aparece la bienvenida: un `HorizontalPager` de tres páginas (SCR-02 a SCR-04) con «Omitir», indicador de página y botón principal «Siguiente»/«Empezar». Que la bienvenida se completó se guarda en DataStore Preferences (`onboarding_completed`, KV-DATASTORE) detrás de un repositorio con interfaz, para probarlo con un falso en memoria. `MainActivity` mantiene el splash del sistema hasta leer ese valor (sin parpadeo) y decide: bienvenida → Permisos (`MusicPermissionGate`, ya existe) → Biblioteca. Permisos pierde la fila de Notificaciones y se ajusta a Figma 23:332; Permiso denegado se revisa contra 23:391.

## Contexto técnico

**Lenguaje/versión**: Kotlin 2.4.21 (AGP 9.5.1), Java 11 de destino.

**Dependencias principales**: las ya instaladas (Compose BOM 2026.09.00, Material 3, Navigation, Lifecycle, Media3, Coil, Palette, `reorderable`) y **una nueva permitida por la tarea**: `androidx.datastore:datastore-preferences` 1.3.0 (estable, sin red). `HorizontalPager` viene en `compose.foundation` (ya instalado).

**Almacenamiento**: DataStore Preferences, archivo `rola_ajustes`, clave booleana `onboarding_completed` ([data-model.md](data-model.md)).

**Pruebas**: JUnit 4 (`app/src/test`) para el repositorio de DataStore (archivo temporal en la JVM). Compose UI test (`app/src/androidTest`, emulador) con un repositorio falso en memoria.

**Plataforma**: Android 11 (API 30) a 17 (API 37). Emulador Pixel 7 Pro; Redmi Note 13 Pro+ para `perfil`.

**Tipo de proyecto**: app móvil de un solo módulo `app`.

**Metas de rendimiento**: cambio de página sin cuadros perdidos visibles en `perfil` (SC-005). Indicador e ilustraciones leen el desplazamiento del pager en lambdas de dibujo (`graphicsLayer`, `Canvas`): el gesto no recompone.

**Restricciones**: sin red (INV-01); tokens del tema (INV-04); relieve solo donde DEC-27 lo permite; accesibilidad (INV-06); movimiento DEC-28 con `LocalReduceMotion`.

**Escala**: 1 pantalla nueva con 3 páginas, 2 pantallas ajustadas, 1 repositorio nuevo, arranque de `MainActivity`.

## Revisión de la constitución

*Puerta: antes de la Fase 0 y otra vez después de la Fase 1.*

| Artículo | Cómo se cumple | Estado |
|---|---|---|
| I. El grafo manda (INV-08, INV-09) | DEC-18 decide el flujo y `onboarding_completed`; Figma decide lo visual. KV-DATASTORE está «propuesto» pero la tarea pide DataStore y solo `onboarding_completed`; los demás campos no se crean. Diferencias con Figma al reporte. | ✅ |
| II. Sin conexión (INV-01) | DataStore escribe un archivo local; ninguna llamada de red. El interruptor de LRCLIB es decorativo. | ✅ |
| III. Datos y secretos (INV-02, INV-03) | Solo se escribe un booleano propio de la app; sin secretos. | ✅ |
| IV. Tokens (INV-04, DEC-27) | Colores del tema; los colores de las portadas de ejemplo de Figma se agregan como tokens en `Color.kt` (único lugar con hex). Relieve con `elevado`/`hundido`/`acento` de `Relief.kt` solo en ilustraciones, tarjetas y botón principal (no en los íconos de permisos ni en la ruta de Ajustes). | ✅ |
| V. Retroalimentación (INV-05) | El indicador muestra la página; el botón cambia a «Empezar»; el permiso da su resultado en pantalla. | ✅ |
| VI. Accesibilidad (INV-06) | «Omitir» y botones ≥ 48 dp; indicador con `stateDescription` «Página n de 3»; ilustraciones decorativas sin semántica (`clearAndSetSemantics`); textos con `sp`, sin alturas fijas que corten; reducir movimiento con fundido. | ✅ |
| VII. La música no se interrumpe (INV-11) | La bienvenida no toca el reproductor. | ✅ |
| VIII. Arquitectura (DEC-20) | `core/data` (repositorio de preferencias), `feature/onboarding` (pantallas), arranque en `MainActivity`. Flujo en una dirección: el repositorio expone `Flow<Boolean>`, la interfaz manda `completeOnboarding()`. | ✅ |
| IX. Pruebas (DEC-22) | JUnit del repositorio; Compose: «Omitir» guarda y lleva a Permisos, «Empezar» en la página 3, reducir movimiento sin desplazamiento, arranque directo a Biblioteca. | ✅ |
| X. Código mínimo | Sin ViewModel nuevo (un `Flow` y una función suspend); `HorizontalPager` y `Switch` de M3 en lugar de componentes propios; un solo archivo de pantalla para las tres páginas. | ✅ |
| XI. Español | Textos en `strings.xml`; commits en español. | ✅ |

**Revisión después del diseño**: sin cambios; no hay violaciones que justificar.

## Estructura del proyecto

### Documentación (esta función)

```text
specs/003-onboarding/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── interfaces.md
├── checklists/requirements.md
└── tasks.md            # lo crea /speckit-tasks
```

### Código fuente

```text
gradle/libs.versions.toml           # + datastore-preferences 1.3.0
app/build.gradle.kts                # + implementation(libs.androidx.datastore.preferences)

app/src/main/java/com/luis/rola/
├── MainActivity.kt                 # splash hasta leer onboarding_completed; bienvenida → permisos → NavHost
├── AppContainer.kt                 # + preferences: PreferencesRepository
├── core/
│   ├── data/
│   │   └── PreferencesRepository.kt    # nuevo: interfaz + DataStore (onboarding_completed)
│   └── designsystem/
│       └── Color.kt                # + colores de las portadas de ejemplo (ilustraciones)
└── feature/onboarding/
    ├── OnboardingScreen.kt         # nuevo: SCR-02..04, pager, indicador, botón, «Omitir»
    ├── OnboardingIllustrations.kt  # nuevo: portadas en abanico, tarjeta de letra, sin wifi + interruptor
    ├── PermissionScreen.kt         # sin fila de Notificaciones; ajustes a 23:332
    ├── PermissionDeniedScreen.kt   # revisión contra 23:391
    └── MusicPermission.kt          # sin cambios de lógica

app/src/main/res/drawable/          # + ic_wifi, ic_lyrics (assets de Figma convertidos a vector)
app/src/main/res/values/strings.xml # + textos de la bienvenida; − textos de Notificaciones que queden sin uso
app/src/test/java/com/luis/rola/core/data/PreferencesRepositoryTest.kt
app/src/androidTest/java/com/luis/rola/feature/onboarding/OnboardingTest.kt
```

**Decisión de estructura**: la bienvenida no es un destino del `NavHost`: igual que Permisos, es una puerta antes de la app ([research.md](research.md) §2). Biblioteca sigue fuera del `NavHost` (DEC-28 `biblioteca_fuera_del_navhost`) y no se toca.

## Seguimiento de complejidad

Sin violaciones de la constitución.
