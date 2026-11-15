# Plan de implementación: Reproductor completo

**Carpeta**: `specs/002-reproductor-completo` | **Fecha**: 2026-11-14 | **Spec**: [spec.md](spec.md)

**Entrada**: especificación de `specs/002-reproductor-completo/spec.md` y restricciones del Paso 3 de la tarea 03.

## Resumen

La mini tarjeta (SCR-50) se convierte en la puerta de Reproduciendo (SCR-51): una capa encima de la app que crece desde la tarjeta hasta la pantalla completa siguiendo el dedo (transformación de contenedor) y se cierra deslizando hacia abajo. La tarjeta gana gestos horizontales (siguiente/anterior). Reproduciendo trae barra de avance arrastrable, anterior, aleatorio y repetir, y abre Cola (SCR-52) y la hoja del Temporizador (SCR-53). La cola, los modos y el temporizador viven en `PlaybackService`; la interfaz los lee del `MediaController` y manda órdenes por él (las que Media3 no tiene, como comandos propios de la sesión). La línea de progreso avanza en cada cuadro sin recomponer la pantalla.

## Contexto técnico

**Lenguaje/versión**: Kotlin 2.4.21 (AGP 9.5.1), Java 11 de destino.

**Dependencias principales**: las ya instaladas — Compose (BOM 2026.09.00), Material 3, Media3 1.11.1 (`exoplayer`, `session`), Navigation Compose 2.10.2, Lifecycle 2.11.0, Coil 3.6.3, Palette 1.1.0. **Sin dependencias nuevas** ([research.md](research.md) §10), salvo `sh.calvin.reorderable:reorderable` 3.1.0 (Apache 2.0, sin red) para el arrastre de la Cola, pedida en la tarea 04 tras dos arreglos propios fallidos: mantiene compuesta la fila arrastrada y maneja el desplazamiento automático y los bordes.

**Almacenamiento**: ninguno. Cola, modos y temporizador no se recuerdan al cerrar la app (FR-029, FR-037).

**Pruebas**: JUnit 4 (`app/src/test`) para lógica pura; Compose UI test (`app/src/androidTest`, emulador).

**Plataforma**: Android 11 (API 30) a 17 (API 37). Emulador Pixel 7 Pro; teléfono Redmi Note 13 Pro+.

**Tipo de proyecto**: app móvil de un solo módulo `app`.

**Metas de rendimiento**: transiciones y barra de avance sin cuadros perdidos visibles en `perfil` (SC-009); la barra de avance invalida solo su dibujo en cada cuadro.

**Restricciones**: sin red (INV-01); tokens del tema (INV-04); plano (DEC-27); contraste (INV-06); la música no se corta por la interfaz (INV-11); movimiento DEC-28 con `LocalReduceMotion`.

**Escala**: 3 pantallas nuevas (Reproduciendo, Cola, hoja del Temporizador), 1 componente modificado (mini tarjeta), servicio ampliado.

## Revisión de la constitución

*Puerta: antes de la Fase 0 y otra vez después de la Fase 1.*

| Artículo | Cómo se cumple | Estado |
|---|---|---|
| I. El grafo manda (INV-08, INV-09) | Solo los DEC/SCR/OP de la tarea. Letra, ⋮ y «Añadir a una lista» fuera. Choques con Figma resueltos a favor del grafo y al reporte: sombra de la portada (DEC-27), «Limpiar» (no lo pide la tarea). | ✅ |
| II. Sin conexión (INV-01) | Sin permisos nuevos; la única dependencia nueva (`reorderable`, tarea 04) no usa red. | ✅ |
| III. Datos y secretos (INV-02, INV-03) | Quitar de la cola no toca archivos; sin secretos. | ✅ |
| IV. Tokens (INV-04, DEC-27) | Colores del tema o de `CoverColors` (calculados); espacios `RolaSpacing`, formas `RolaShapes`, tipos `RolaTypography` y `Typography.time`. Sin sombras ni relieve. | ✅ |
| V. Retroalimentación (INV-05) | Aviso con Deshacer al quitar; aviso al iniciar y cancelar el temporizador; estados de modos visibles. | ✅ |
| VI. Accesibilidad (INV-06) | 48 dp, descripciones con estado (aleatorio, repetir, temporizador con tiempo restante), acciones del lector en la cola, barra de avance con `setProgress`, reducir movimiento en cada animación. | ✅ |
| VII. La música no se interrumpe (INV-11) | Abrir/cerrar capas no toca el reproductor; el temporizador solo pausa. | ✅ |
| VIII. Arquitectura (DEC-20) | `feature/player` (pantallas y gestos), `core/media` (servicio, conexión, lógica pura). Estado del reproductor en `PlayerConnection` (`StateFlow`), sin copia paralela de la cola. | ✅ |
| IX. Pruebas (DEC-22) | JUnit: temporizador, umbrales de gestos, operaciones de cola, formato de tiempos. Compose: Reproduciendo, reducir movimiento, cola. | ✅ |
| X. Código mínimo | Sin ViewModel nuevo (la conexión ya expone el estado), sin librerías de reordenar ni de gestos; componentes de M3 donde existen (hoja, deslizar para quitar, aviso). | ✅ |
| XI. Español | Textos en `strings.xml`; commits en español. | ✅ |

**Revisión después del diseño**: sin cambios; no hay violaciones que justificar.

## Estructura del proyecto

### Documentación (esta función)

```text
specs/002-reproductor-completo/
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
├── navigation/
│   └── RolaNavHost.kt              # monta la capa del reproductor encima de todo y el aviso arriba
├── core/
│   └── media/
│       ├── PlaybackService.kt      # + modos, orden aleatorio, temporizador, comandos propios
│       ├── PlayerConnection.kt     # + anterior, buscar, modos, cola, temporizador; sin ticker de 500 ms
│       ├── QueueOrder.kt           # nuevo, puro: orden de reproducción, mover, aleatorio desde la actual
│       ├── SleepTimer.kt           # nuevo, puro: opciones y estado del temporizador
│       └── Duration.kt             # existente
└── feature/
    └── player/
        ├── CoverColors.kt          # + variante clara y fundido de color
        ├── MiniPlayer.kt           # + toque/gestos, línea continua, se oculta mientras la capa crece
        ├── PlayerSheet.kt          # nuevo: capa mini ↔ Reproduciendo (transformación de contenedor)
        ├── NowPlayingScreen.kt     # nuevo: SCR-51
        ├── SeekBar.kt              # nuevo: barra de avance arrastrable + progreso por cuadro
        ├── QueueScreen.kt          # nuevo: SCR-52
        ├── SleepTimerSheet.kt      # nuevo: SCR-53
        └── Swipe.kt                # nuevo, puro: decisión de umbral de los gestos

app/src/main/res/drawable/          # íconos de Figma como vectores (cerrar, anterior, repetir, temporizador, cola, asa, borrar)
app/src/test/java/com/luis/rola/    # JUnit: QueueOrderTest, SleepTimerTest, SwipeTest, DurationTest
app/src/androidTest/java/com/luis/rola/feature/player/  # NowPlayingTest, PlayerSheetTest, QueueScreenTest
```

**Decisión de estructura**: Reproduciendo no es un destino del `NavHost` sino una capa encima (`PlayerSheet`) porque su apertura sigue al dedo y nace de la tarjeta ([research.md](research.md) §1). Cola vive dentro de esa capa. La lógica que se puede probar sin Android (orden de cola, temporizador, umbrales) va en archivos puros.

## Seguimiento de complejidad

Sin violaciones de la constitución.
