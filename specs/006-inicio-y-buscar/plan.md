# Plan de implementación: Inicio y Buscar

**Carpeta**: `specs/006-inicio-y-buscar` | **Fecha**: 2026-11-25 | **Spec**: [spec.md](spec.md)

**Entrada**: especificación de `specs/006-inicio-y-buscar/spec.md` y restricciones del Paso B3 de la tarea 07.

## Resumen

Room suma `play_history` y `playback_queue` con **una** migración automática v1→v2. Un `PlaybackStore` guarda la cola en Room y los valores sueltos en DataStore; `PlaybackService` le avisa cuando cambia algo y, al crearse, restaura con `setMediaItems(items, índice, minuto)` + `prepare()` **sin** `play()`. Un contador puro (`PlayCounter`) decide cuándo una escucha cuenta y el servicio la registra. Inicio (`feature/home`) lee el historial y la biblioteca; Buscar (`feature/search`) filtra en memoria con una función pura que también da el tramo a resaltar. Los dos son destinos del `NavHost`, que ahora abre en Inicio; la píldora sigue a la pestaña dueña de la pila. La raíz de Biblioteca no se toca.

## Contexto técnico

**Lenguaje/versión**: Kotlin 2.4.21 (AGP 9.5.1), Java 11 de destino.

**Dependencias principales**: solo las instaladas (Compose, Material 3, Navigation, Lifecycle, Media3 1.11, Room 2.8.5, DataStore, Coil). **Ninguna nueva** ([research.md](research.md) §1).

**Almacenamiento**: Room `rola.db` v2; DataStore Preferences nuevo `rola_reproduccion` (aparte de `rola_ajustes`, research §4).

**Pruebas**: JUnit 4 en la JVM: guardar/restaurar (cola en orden, índice, minuto, aleatorio, repetir, origen, canción borrada, cola vacía, guardado dañado), saludo por hora, regla de ≥ 30 s, últimas 10 distintas, más escuchada con empate, relevancia y resaltado de Buscar. En emulador: migración v1→v2 sin perder datos, DAO nuevos, Inicio en sus tres estados, Buscar agrupa y resalta, sin resultados, y la restauración real tras recrear la actividad y el servicio.

**Plataforma**: Android 11 (API 30) a 17 (API 37). Emulador Pixel 7 Pro; Redmi para `perfil` si aparece en adb.

**Tipo de proyecto**: app móvil de un solo módulo `app`.

**Metas de rendimiento**: cola de 2 000 canciones. La cola se escribe en una transacción en IO y solo si cambió; las ráfagas (arrastrar) se juntan; el minuto escribe solo un archivo de pocos bytes. Restaurar cruza con la lista de canciones ya compartida. Buscar recorre ~2 000 canciones + álbumes + artistas en `Dispatchers.Default` con nombres normalizados una sola vez por biblioteca (SC-005).

**Restricciones**: sin red (INV-01); tokens (INV-04); plano salvo el relieve de SCR-12/40/41 (DEC-27); accesibilidad (INV-06); la música no se corta (INV-11); movimiento DEC-28.

**Escala**: 2 tablas, 2 DAO, 1 almacén de reproducción, 1 contador, 1 repositorio de historial, 1 método de escaneo, 2 ViewModels, 2 pantallas con 5 estados, 1 ícono.

## Revisión de la constitución

*Puerta: antes de la Fase 0 y otra vez después de la Fase 1.*

| Artículo | Cómo se cumple | Estado |
|---|---|---|
| I. El grafo manda (INV-08, INV-09) | DEC-30 manda sobre Figma en la fecha, el mosaico Favoritas y lo que no se muestra. El temporizador (propuesto) no se guarda. La columna extra de `playback_queue` se propone y se confirma con Brian antes de B6 (research §3). | ⚠️ ver Seguimiento |
| II. Sin conexión (INV-01) | Todo local; sin dependencias nuevas. | ✅ |
| III. Datos y secretos (INV-02, INV-03) | El historial y la cola son datos locales; nada se borra del teléfono. La migración conserva Favoritas y listas. | ✅ |
| IV. Tokens (INV-04, DEC-27) | Colores, formas y tipos del tema; `elevado`, `hundido` y `acento` ya existen. | ✅ |
| V. Retroalimentación (INV-05) | Esqueletos, progreso y aviso del escaneo, sin resultados, vacíos propios. | ✅ |
| VI. Accesibilidad (INV-06) | 48 dp en «Borrar búsqueda» (Figma dibuja 44), encabezados con rol, descripciones, texto que escala y se desplaza. | ✅ |
| VII. La música no se interrumpe (INV-11) | Guardar lee el reproductor en el hilo principal y escribe fuera; restaurar ocurre antes de aceptar órdenes. | ✅ |
| VIII. Arquitectura (DEC-20) | `core/database`, `core/data`, `core/media`, `feature/home`, `feature/search`, `navigation`. VM con `StateFlow`. | ✅ |
| IX. Pruebas (DEC-22) | Las de «Pruebas». | ✅ |
| X. Código mínimo | Se reusan `Cover`, `SongItem`, `cascada`, `esqueleto`, `TitleOrder.normalizar`, `existing()`, los relieves y el logo. | ✅ |
| XI. Español | Textos en `strings.xml` con plurales; commits en español. | ✅ |

**Revisión después del diseño**: sin cambios; queda un punto por confirmar (Seguimiento).

## Estructura del proyecto

### Documentación (esta función)

```text
specs/006-inicio-y-buscar/
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
app/schemas/com.luis.rola.core.database.RolaDatabase/2.json   # generado
app/src/main/java/com/luis/rola/
├── AppContainer.kt                # base compartida, PlaybackStore, HistoryRepository, escaneo
├── core/database/
│   ├── Entities.kt                # + PlayHistoryEntity, QueueEntity
│   ├── HistoryDao.kt              # NUEVO
│   ├── QueueDao.kt                # NUEVO
│   ├── PlaylistDao.kt             # forgetSong también borra el historial (DEC-08)
│   └── RolaDatabase.kt            # versión 2 + AutoMigration(1, 2)
├── core/data/
│   ├── HistoryRepository.kt       # NUEVO: recientes, más escuchada (puro + flujo), registrar
│   ├── Search.kt                  # NUEVO: coincidencia, relevancia y tramo resaltado (puro)
│   └── SongRepository.kt          # + scan(): MediaScannerConnection y nueva consulta
├── core/media/
│   ├── PlaybackStore.kt           # NUEVO: guardar/leer + recortar lo que ya no existe (puro)
│   ├── PlayCounter.kt             # NUEVO: regla de ≥ 30 s (puro)
│   ├── PlaybackService.kt         # restaurar, guardar, contar; sin play() tras un error
│   └── PlayerConnection.kt        # espera a que el servicio termine de restaurar; MediaItem compartido
├── feature/home/
│   ├── Greeting.kt                # NUEVO: saludo y fecha (puro)
│   ├── HomeViewModel.kt           # NUEVO
│   └── HomeScreen.kt              # NUEVO: SCR-10, SCR-11, SCR-12
├── feature/search/
│   ├── SearchViewModel.kt         # NUEVO
│   └── SearchScreen.kt            # NUEVO: SCR-40, SCR-41
├── feature/library/SongMenu.kt    # título con resaltado opcional en la fila de canción
├── feature/library/Cascada.kt     # reloj de cascada reutilizable
└── navigation/RolaNavHost.kt      # abre en Inicio; Inicio y Buscar; pestaña dueña de la pila
app/src/main/res/drawable/ic_refresh.xml  # NUEVO (Figma 30:538)

app/src/test/java/com/luis/rola/
├── core/data/HistoryTest.kt, SearchTest.kt
├── core/media/PlaybackStoreTest.kt, PlayCounterTest.kt
└── feature/home/GreetingTest.kt

app/src/androidTest/java/com/luis/rola/
├── core/database/MigrationTest.kt, RolaDatabaseTest.kt (+ DAO nuevos)
├── core/media/RestorePlaybackTest.kt
├── feature/home/HomeScreenTest.kt
└── feature/search/SearchScreenTest.kt
```

**Decisión de estructura**: se mantiene el módulo `app` y los paquetes de DEC-20 (`home` y `search` ya están previstos).

## Seguimiento de complejidad

| Punto | Por qué hace falta | Alternativa más simple y por qué no |
|---|---|---|
| Columna `play_order` en `playback_queue` (TBL-QUEUE lista solo `position` y `media_id`) | Con aleatorio, la cola tiene dos órdenes: el original (para cuando se apague) y el aleatorio en que va sonando. DEC-31 pide conservar la cola «en su orden actual» y aleatorio. | Guardar solo el orden en que suena: cabe en las dos columnas, pero al apagar aleatorio tras reabrir ya no se recupera el orden del álbum o lista. Se pregunta a Brian antes de B6. |
