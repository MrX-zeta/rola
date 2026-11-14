# Plan de implementación: Reproductor base

**Carpeta**: `specs/001-reproductor-base` | **Fecha**: 2026-11-14 | **Spec**: [spec.md](spec.md)

**Entrada**: especificación de `specs/001-reproductor-base/spec.md` y restricciones técnicas del Paso 3 de la tarea 02.

## Resumen

Rola pasa de la pantalla temporal a un reproductor usable: pide el permiso de audio (SCR-05/SCR-06), lee las canciones del MediaStore como `Flow` que se refresca con un `ContentObserver` (DEC-26), las ordena por título con las reglas de DEC-05, reproduce en un `MediaSessionService` con ExoPlayer de Media3 (notificación del propio Media3) y muestra una mini tarjeta (SCR-50) teñida con el color de la portada (Palette, PEND-08 A) encima de la barra píldora de tres destinos (DEC-03). Inyección manual con `AppContainer` (PEND-02).

## Contexto técnico

**Lenguaje/versión**: Kotlin 2.2.10 (Kotlin integrado de AGP 9.4.1), Java 11 de destino.

**Dependencias principales**: Jetpack Compose (BOM 2026.02.01) y Material 3 ya instalados. Nuevas, todas permitidas por la tarea y en su última versión estable comprobada en Maven el 2026-11-14 ([research.md](research.md) §1):
- `androidx.media3:media3-exoplayer` y `media3-session` 1.11.1
- `androidx.navigation:navigation-compose` 2.10.2
- `androidx.lifecycle:lifecycle-viewmodel-compose` y `lifecycle-runtime-compose` 2.11.0 (misma versión que `lifecycle-runtime-ktx`)
- `io.coil-kt.coil3:coil-compose` 3.6.3 (sin módulo de red)
- `androidx.palette:palette-ktx` 1.1.0

**Almacenamiento**: ninguno. Sin Room ni DataStore; nada se recuerda entre reinicios (spec FR-018b).

**Pruebas**: JUnit 4 (unitarias, `app/src/test`) y Compose UI test (`app/src/androidTest`, en emulador).

**Plataforma**: Android 11 (API 30) a 17 (API 37). Teléfono de referencia: Redmi Note 13 Pro+ (HyperOS). Emulador: Pixel 7 Pro.

**Tipo de proyecto**: app móvil de un solo módulo `app`.

**Metas de rendimiento**: lista fluida con 2 000 canciones (SC-007); sonido en < 1 s al tocar (SC-003).

**Restricciones**: sin red (INV-01); tokens del tema (INV-04); contraste ≥ 4.5:1 (INV-06); la música no se corta por la interfaz (INV-11).

**Escala**: 5 pantallas o piezas de interfaz (Permisos, Permiso denegado, Biblioteca, Próximamente, mini tarjeta + barra), 1 servicio.

## Revisión de la constitución

*Puerta: antes de la Fase 0 y otra vez después de la Fase 1.*

| Artículo | Cómo se cumple | Estado |
|---|---|---|
| I. El grafo manda (INV-08, INV-09) | Se usan solo DEC/SCR citados; PEND-05 y PEND-07 no se aplican; abecedario, filtros, ⋮ y gestos quedan fuera. Choque detectado: la spec quería abrir en Biblioteca y DEC-03 dice Inicio → manda DEC-03; la propuesta va al reporte. | ✅ |
| II. Sin conexión (INV-01) | Ninguna dependencia de red: Coil sin `coil-network`; Media3 sin módulos de red; no se agrega `INTERNET`. | ✅ |
| III. Datos y secretos (INV-02, INV-03) | Solo lectura del MediaStore; no se borra nada; sin secretos. | ✅ |
| IV. Tokens (INV-04, DEC-27) | Colores, tipos, formas y espacios de `core/designsystem`. Colores de portada calculados en tiempo de ejecución, nunca hex en pantallas. Relieve (`elevado`, `acento`) solo en SCR-05/SCR-06, como en Figma. | ✅ |
| V. Retroalimentación (INV-05) | Aviso breve si una canción falla; texto cuando no hay canciones; indicador mientras carga la lista. | ✅ |
| VI. Accesibilidad (INV-06) | Botones de ícono con `contentDescription` y 48 dp; ajuste de contraste del color de portada probado con JUnit; textos en `sp`. | ✅ |
| VII. La música no se interrumpe (INV-11) | La reproducción vive en el servicio; la interfaz solo manda órdenes por `MediaController`. | ✅ |
| VIII. Arquitectura (DEC-20) | Un módulo `app`, paquetes `core/` y `feature/`, ViewModel + `StateFlow`, `MediaSessionService`. | ✅ |
| IX. Pruebas (DEC-22) | JUnit para orden, normalización, mapeo, duración y contraste; Compose para Biblioteca y mini tarjeta. | ✅ |
| X. Código mínimo | Sin capas de dominio ni casos de uso; repositorio con interfaz solo donde la prueba necesita un falso. | ✅ |
| XI. Español | Textos en `strings.xml` en español; commits en español. | ✅ |

**Revisión después del diseño**: sin cambios; no hay violaciones que justificar.

## Estructura del proyecto

### Documentación (esta función)

```text
specs/001-reproductor-base/
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
app/src/main/java/com/luis/rola/
├── MainActivity.kt                 # splash, edge-to-edge, decide Permisos o app
├── RolaApp.kt                      # Application: crea AppContainer
├── AppContainer.kt                 # repositorio de canciones + conexión al reproductor
├── navigation/
│   ├── RolaNavHost.kt              # NavHost con Inicio, Biblioteca, Buscar
│   ├── RolaNavBar.kt               # píldora de tres destinos (DEC-03)
│   └── ComingSoonScreen.kt         # «Próximamente» de Inicio y Buscar
├── core/
│   ├── designsystem/               # existente; + ajuste de contraste
│   ├── data/
│   │   ├── Song.kt                 # modelo
│   │   ├── SongRepository.kt       # interfaz + implementación MediaStore
│   │   ├── SongMapper.kt           # fila → Song (pura)
│   │   └── TitleOrder.kt           # normalización y orden DEC-05 (pura)
│   └── media/
│       ├── PlaybackService.kt      # MediaSessionService + ExoPlayer
│       ├── PlayerConnection.kt     # MediaController → StateFlow + órdenes
│       └── Duration.kt             # formato m:ss (pura)
└── feature/
    ├── onboarding/
    │   ├── MusicPermission.kt      # permiso según versión + estado
    │   ├── PermissionScreen.kt     # SCR-05
    │   └── PermissionDeniedScreen.kt # SCR-06
    ├── library/
    │   ├── LibraryViewModel.kt
    │   ├── LibraryScreen.kt        # SCR-20 (solo chip Canciones)
    │   └── Cover.kt                # portada con Coil + fetcher de loadThumbnail
    └── player/
        ├── CoverColors.kt          # Palette + ajuste de contraste
        └── MiniPlayer.kt           # SCR-50

app/src/main/res/drawable/          # íconos Material Symbols de Figma como vectores
app/src/test/java/com/luis/rola/    # JUnit
app/src/androidTest/java/com/luis/rola/ # Compose UI test
```

**Decisión de estructura**: se respetan los paquetes que fija la tarea. `navigation/` vive dentro del paquete raíz porque es parte de «app» (navegación y `AppContainer`). `Cover.kt` está en `feature/library` y lo reutiliza la mini tarjeta: si se usa en más lugares, se mueve a `core/designsystem` en otra tarea.

## Seguimiento de complejidad

Sin violaciones de la constitución.
