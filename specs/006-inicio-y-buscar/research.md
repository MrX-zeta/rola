# Investigación: Inicio y Buscar

## 1. Dependencias

- **Decisión**: ninguna nueva.
- **Razón**: Room trae migraciones automáticas; `MediaScannerConnection` es de Android; Compose trae `LazyRow` y `BasicTextField`.
- **Prueba de migración**: `room-testing` no está en el proyecto. Se arma una base v1 con las sentencias de `schemas/1.json`, se abre con Room v2 (que valida el esquema al abrir) y se comprueba que Favoritas y listas siguen. No necesita dependencia.
- **Segundo plano**: `ProcessLifecycleOwner` pediría `lifecycle-process`; basta `Application.ActivityLifecycleCallbacks` registrado por el servicio (la app tiene una sola actividad).

## 2. Historial

- **Decisión**: una fila por escucha que contó (`id`, `media_id`, `played_at`, TBL-HISTORY) con índice en `media_id, played_at`. Una sola consulta agrupada (`media_id`, cuántas, la última) alimenta los dos cálculos, que son funciones puras: recientes = por última fecha, existentes, 10; más escuchada = por cuántas y luego por última, existente.
- **Razón**: «las que existan» se sabe solo al cruzar con el MediaStore; un `LIMIT 10` en SQL dejaría huecos por canciones borradas. Las funciones puras se prueban con JUnit.
- **Cuándo cuenta**: `PlayCounter(now)` suma tiempo **sonando** (`isPlaying`), no posición: pausar no suma y adelantar no suma. Al llegar a 30 s cuenta una vez. Si la canción termina sola (`MEDIA_ITEM_TRANSITION_REASON_AUTO`/`REPEAT`, `STATE_ENDED` o pausa al final por el temporizador) y su final está antes de los 30 s, también cuenta. Cada vuelta de «repetir una» es una escucha nueva. El servicio programa un aviso con `Handler` para los 30 s, igual que el temporizador.
- **Borrar**: `PlaylistDao.forgetSong` borra también de `play_history` en la misma transacción (DEC-08).

## 3. Cola guardada

- **Decisión**: `playback_queue(position, media_id, play_order)`. `position` es el lugar en la lista de Media3; `play_order`, su lugar en el orden en que suena (igual a `position` sin aleatorio).
- **Razón**: ver «Seguimiento de complejidad» del plan. Brian la confirmó el 2026-11-25; se propone al grafo en el reporte.
- **Escritura**: se reemplaza completa en una transacción (borrar + insertar) solo cuando cambia la lista o el orden aleatorio (`onTimelineChanged` con `PLAYLIST_CHANGED` —Media3 también lo manda con `setShuffleOrder`— y al encender/apagar aleatorio). `SOURCE_UPDATE` (Media3 descubre la duración) no escribe.
- **Ráfagas**: el servicio lee el estado en el hilo principal y lo entrega a un canal `CONFLATED`; un solo consumidor escribe en IO, en orden. Si llegan diez cambios seguidos, se escribe el último. El minuto va por otro canal `CONFLATED` para no arrastrar la cola.
- **Coherencia**: cada guardado de cola escribe después sus valores sueltos. Si la app muere entre los dos, al leer se recorta el índice al tamaño de la cola.

## 4. Valores sueltos

- **Decisión**: DataStore Preferences propio, `rola_reproduccion`: `indice`, `minuto_ms`, `aleatorio`, `repetir`, `origen`.
- **Razón**: se escribe cada ~5 s; en `rola_ajustes` cada escritura reescribiría los ajustes y despertaría a quien los observa (orden de Canciones, bienvenida).
- **Alternativas**: columnas en Room (DEC-31 pide DataStore); SharedPreferences (otra API).
- **Errores**: `IOException` al leer = sin guardado; al escribir = se ignora y se vuelve a intentar en el siguiente guardado (igual que `PreferencesRepository`).

## 5. Cuándo se guarda

| Evento | Qué |
|---|---|
| Cambia la lista o el orden aleatorio | cola + valores |
| Cambia la canción, pausa, salto en la barra, aleatorio, repetir, origen | valores |
| La actividad se detiene (segundo plano) y `onTaskRemoved` | valores |
| Cada 5 s mientras suena | valores |

El alcance de escritura es el del proceso, no el del servicio: un guardado pedido justo antes de `stopSelf()` termina.

## 6. Restaurar

- **Decisión**: en `PlaybackService.onCreate` se lanza una corrutina: leer filas y valores; si hay filas, esperar la primera lista de canciones; recortar lo que no existe (`SavedPlayback.keepExisting`, puro); en el hilo principal `setMediaItems(items, índice, minuto)`, repetir, origen, aleatorio y su orden, y `prepare()`.
- **Nunca suena sola**: no hay `play()`. Además, hoy `onPlayerError` hace `seekToNextMediaItem(); prepare(); play()`: con una canción restaurada ilegible empezaría a sonar. Se quita ese `play()`: si estaba sonando, `playWhenReady` ya es `true` y sigue; si no, queda en pausa (FR-022).
- **Orden aleatorio**: encender `shuffleModeEnabled` dispara `shuffleFromCurrent()`; por eso primero se ponen las canciones, luego el modo y al final `setShuffleOrder` con el orden guardado (sin las que faltan).
- **Sin carreras**: mientras restaura no se guarda nada, y `PlayerConnection` no manda órdenes: las deja en `pendientes` hasta que `sessionExtras` traiga `listo` (FR-027). El servicio pone `listo` siempre, también si algo falla (`try/finally`).
- **Primera instalación**: sin filas no se consulta el MediaStore (antes del permiso la lista compartida quedaría vacía en caché y Inicio parpadearía con «No encontramos música»).
- **Si falta la actual**: la siguiente existente en el orden en que iban a sonar, desde 0; si no hay, la primera de ese orden. Si se recortó algo, se guarda la cola nueva al terminar. Si no queda ninguna, no se toca lo guardado (puede ser un permiso o una tarjeta sin montar).
- **Notificación**: con la cola preparada en pausa, Media3 puede publicar la notificación multimedia sin servicio en primer plano; se revisa en el emulador y se anota.

## 7. Escanear de nuevo

- **Decisión**: `MediaScannerConnection.scanFile(contexto, [carpeta Música])`, que desde Android 10 recorre la carpeta, con un máximo de espera de 15 s; después se vuelve a consultar y se emite en el flujo compartido. El aviso usa el conteo de esa consulta.
- **Riesgo**: en alguna versión podría no recorrer carpetas; se comprueba en el emulador copiando un audio. Si no, se pasan los archivos de audio que el proceso pueda listar.

## 8. Buscar

- **Decisión**: función pura `searchLibrary(query, canciones, álbumes, artistas, listas)`; compara nombres con `TitleOrder.normalizar`, ya normalizados una vez por biblioteca. Dos pasadas estables por grupo (empieza / contiene) y 5 por grupo: las listas de entrada ya vienen en orden DEC-05.
- **Resaltado**: se normaliza carácter por carácter guardando de qué índice original viene cada uno («ñ» da dos caracteres, los acentos sueltos ninguno); así el tramo encontrado en el texto normalizado se traduce al original. Se marca la primera coincidencia, en `primary` y con el mismo peso (Figma); el color no es la única señal: el texto buscado está en el campo y el resultado ya se entiende sin él.
- **Sin parpadeo**: el ViewModel hace `debounce(150)` + `mapLatest`; los resultados anteriores siguen hasta que llegan los nuevos; las filas tienen clave estable. Vaciar el campo limpia al momento, sin espera.
- **Teclado**: `FocusRequester` al entrar sin texto; la lista suma el alto del teclado a su relleno inferior.

## 9. Navegación

- **Decisión**: `startDestination = inicio`. La píldora sigue una `pestana` guardada (`rememberSaveable`): cambia al tocar la píldora o al llegar a una raíz. Los detalles abiertos desde Inicio o Buscar son hijos de esa pestaña; tocar la pestaña activa vuelve a su raíz con `popBackStack`.
- **Razón**: hoy «pantalla hija» significa «hija de Biblioteca» por su ruta; con álbumes desde Buscar y Favoritas desde Inicio eso mandaría la píldora y el «atrás» al lugar equivocado (DEC-03).
- **Riesgo**: `popUpTo(inicio) { saveState }` + `restoreState` sobre el destino de arranque; se comprueba en el emulador ir y volver entre las tres pestañas con un detalle abierto en cada una. Si el estado del arranque se confunde, para Inicio se usa `popBackStack(inicio, saveState = true)`.

## 10. Movimiento (skill `animate`)

- **¿Se anima?** Cambiar de pestaña pasa decenas de veces al día: solo algo casi imperceptible. Propósito: evitar un cambio brusco. Se usa la cascada existente (DEC-28): fundido + 8 dp, 8 elementos, 25 ms entre ellos, 300 ms en total, `RolaMotion.EaseOut`.
- **Inicio**: un índice por bloque en orden de lectura (saludo 0, sección 1, tarjetas del carrusel 2–4, «Para ti» 5, destacado 6, mosaicos 7). Arranca en el primer cuadro con contenido.
- **Buscar**: solo al pasar de «sin resultados en pantalla» a «con resultados». Escribir es acción de teclado repetida: el cambio entre resultados **no** se anima.
- **Reducir movimiento**: sin cascada (reloj al final).

## 11. Lo que dice Figma (2026-11-25, nodos oscuros leídos con `figma-design-to-code`)

- **SCR-10 (30:408)**: contenido con 20 de margen y 18 entre bloques; barra con símbolo 30 + «rola» 24; fecha 14 `onSurfaceVariant`, saludo Space Grotesk 34; sección 20; carrusel con 12 de separación, portada 112 radio 12, título 14 Medium, artista 12; destacado `primaryContainer` radio 24, relleno 12/16, portada 84 radio 12, etiqueta Geist Mono 11 en `onPrimaryContainer`, título 22, «Artista · N veces» 13, botón lima 48; mosaicos `surface` radio 24, alto 116, relleno 16, ícono 24 en recuadro 40 `primaryContainer` radio 12, título 16 SemiBold, detalle 13.
- **SCR-11 (30:447)**: tarjeta `surfaceContainer` radio 24 relleno 24, círculo 48 `primaryContainer` con nota, título 22, texto 15, «Aleatorio» lima de 48 con ícono 20 e «Ir a Biblioteca» en `surfaceContainer`; debajo «Para ti» con los dos mosaicos.
- **SCR-12 (30:512)**: barra del logo; centrado: círculo 112 `surfaceContainer` **elevado** con nota 48, título 24, texto 15, botón lima 52 **acento** con ícono refresh 20. Pie de 150 para la píldora.
- **SCR-40 (35:1288)**: título 30 en barra de 72; campo de 56 **hundido**, `surfaceContainer`, píldora, lupa, cursor `primary`, «Borrar búsqueda»; secciones 18 con 14/4; filas de 48 con 14 de separación, nombre 15 SemiBold y coincidencia en `primary`; álbum «Álbum · Artista», artista «N canciones» con portada redonda; chevron en 48.
- **SCR-41 (35:1392)**: círculo 112 **elevado** con lupa 48, título 22 «Sin resultados para «…»», consejo 15.
- **Tokens**: 34 → `displaySmall` (36), 30 → `headlineMedium` (28), 24 → `headlineSmall`, 22 → `titleLarge`, 20/18 → `titleLarge` (22), 15 → `titleSmall`/`bodyLarge`, 13 → `bodyMedium`, 11 mono → `timeSmall` (12). Mismo criterio aceptado en la tarea 06.
- **Faltan**: `ic_refresh`. `ic_cerrar`, `ic_search`, `ic_chevron_forward`, `ic_favorite`, `ic_shuffle`, `ic_music_note` e `ic_play` ya existen.
- **Claro** (51:2090, 51:2217, 51:2302, 51:4081, 51:4183): comparados por captura: iguales salvo el círculo de «Tu más escuchada», que en claro es negro (`onPrimaryContainer`).
