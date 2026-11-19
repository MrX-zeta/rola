# Investigación: Biblioteca completa

## 1. Dependencias

- **Decisión**: ninguna nueva.
- **Razón**: Compose ya trae `LazyVerticalGrid` y `LazyRow`; DataStore guarda el orden; Coil carga las portadas.
- **Alternativas**: una librería de índice alfabético o de cuadrículas; innecesaria.

## 2. Álbumes y artistas: consulta aparte o derivados

- **Decisión**: derivarlos en memoria de la lista de canciones compartida (`AppContainer.songs`), con funciones puras en `core/data/LibraryGrouping.kt`, ejecutadas en `Dispatchers.Default` desde `LibraryViewModel`.
- **Razón**: una sola consulta al MediaStore (como en la tarea 05); se actualizan solas cuando cambia la biblioteca (DEC-26); con 2 000 canciones agrupar es O(n) y tarda pocos milisegundos; las funciones puras se prueban con JUnit sin emulador.
- **Alternativas**: consultar `MediaStore.Audio.Albums` y `Artists`: dos consultas y observadores más, conteos que no respetan el filtro `IS_MUSIC` y datos que pueden no coincidir con Canciones.

## 3. Columnas nuevas del MediaStore

- **Decisión**: agregar `ALBUM_ID`, `ALBUM_ARTIST`, `TRACK` y `YEAR` a la proyección y a `Song` (con valores por defecto para no romper a quien crea `Song` en pruebas).
- **Razón**: `ALBUM_ID` separa dos álbumes con el mismo nombre; `ALBUM_ARTIST` existe desde API 30 (minSdk); `TRACK` trae disco × 1000 + pista, así que ordenar por el número tal cual deja el disco 1 antes del 2; `YEAR` da el año del álbum.
- **Valores vacíos**: `TRACK` 0 = sin número; `YEAR` 0 = sin año; `ALBUM_ARTIST` vacío o `<unknown>` = nulo (mismo filtro que `artist`).

## 4. Reglas de agrupación

- **Álbum** = canciones con el mismo `albumId`. Nombre: el `album` de la canción o «Álbum desconocido». Artista: el primer `albumArtist` no nulo; si no, el `artist` de la primera pista; si tampoco, «Artista desconocido». Año: el mayor de sus canciones (0 = sin año). Canciones: por `track` ascendente con 0 al final, y por título (DEC-05) para empatar.
- **Artista** = clave `albumArtist ?: artist`; nula = «Artista desconocido» (una sola entrada). Canciones: por álbum (más reciente primero) y pista; álbumes: los que contienen alguna de sus canciones, del más reciente al más antiguo. Foto: portada de su álbum más reciente (mayor año; empate o sin año, por nombre DEC-05).
- **Orden**: álbumes y artistas por nombre con `TitleOrder.comparator`; «Artista desconocido» y «Álbum desconocido» van al final (en «#»), igual que un artista o álbum vacío al ordenar Canciones.

## 5. Orden de Canciones y `sort_order`

- **Decisión**: `SongSort(by: TITLE|ARTIST|ALBUM, ascending: Boolean)`, guardado como texto `"TITLE_ASC"`, `"ARTIST_DESC"`, etc. Un valor desconocido o ilegible = `TITLE_ASC`.
- **Comparador**: por el campo elegido con `TitleOrder.comparator` (nulos como «Artista/Álbum desconocido»), empate por título; `descending` invierte el resultado completo.
- **Abecedario**: la sección de cada canción sale del campo elegido (`TitleOrder.sectionOf`). Con Z→A las letras se dibujan de Z a A y «#» va arriba, para que bajar por la barra sea bajar por la lista.

## 6. Portadas

- **Decisión**: la portada de un álbum es la de su primera canción (`Cover(song)`); la de un artista, la del álbum más reciente, recortada en círculo.
- **Razón**: el `SongCoverFetcher` ya pide la miniatura al tamaño en que se dibuja (tarea 03b) y la caché comparte la clave `portada:<id>` con Canciones, así que no se vuelve a decodificar.
- **Alternativa**: `loadThumbnail` sobre la Uri del álbum: requiere otro fetcher y otra clave de caché.

## 7. Navegación

- **Decisión**: rutas `album/{id}` (Long) y `artista/{nombre}` (codificado con `Uri.encode`; la cadena vacía representa «Artista desconocido»). Biblioteca sigue fuera del NavHost (DEC-28); `enLista` del NavHost pasa a cubrir también estas rutas para que la píldora siga en Biblioteca y «Biblioteca» vuelva a su raíz.
- **Si el álbum o artista desaparece** con el detalle abierto, la pantalla vuelve atrás (igual que una lista borrada).

## 8. Cascada y rendimiento

- **Decisión**: la cascada de Canciones se extrae a un modificador compartido (`Modifier.cascada`) que leen las filas de Canciones, Artistas y las celdas de Álbumes; el reloj se reinicia al entrar a Biblioteca y al cambiar de chip. En la cuadrícula el índice de cascada es el índice de la celda (máximo 8 celdas, DEC-28).
- **Rendimiento**: solo se compone el chip elegido; la cuadrícula usa claves estables (`albumId`) y `contentType`; las listas agrupadas se calculan una vez por cambio de la biblioteca. Medir con 2 000 audios y ~200 álbumes en `perfil` (SC-004).

## 9. Hoja de filtros y orden

- **Decisión**: `ModalBottomSheet` con dos grupos de `RadioButton` (Ordenar por: Título, Artista, Álbum; Dirección: A→Z, Z→A), según SCR-26. Cada toque aplica y guarda al momento; la hoja se cierra con «atrás», el velo o deslizando. Si el nodo de Figma trae otras secciones (p. ej. filtros), solo se construye el orden y lo demás se anota en el reporte.

## 10. Lo que dice Figma (T001, 2026-11-19)

- **SCR-21 Álbumes (32:801)**: cuadrícula de 2 columnas, márgenes 20 (izq.) y 36 (der., deja lugar al abecedario), separación 16 entre columnas y 20 entre filas; celda: portada cuadrada radio 20, 8 dp, nombre 15 SemiBold, «Artista · año» 13. Sin resumen ni aleatorio arriba. Las portadas traen sombra (Neumorfismo/Elevado): **DEC-27 manda, van planas** (se anota en el reporte).
- **SCR-22 Artistas (32:961)**: filas 20/30 de margen, 8 vertical, foto redonda de 56, nombre 15 SemiBold, «N canciones · N álbumes» 13, chevron a la derecha (ícono nuevo `ic_chevron_forward`).
- **SCR-24 Detalle de álbum (33:836)**: barra con volver y ⋮ (sin función en esta tarea: no se muestra); portada 200 radio 24 centrada (sombra → plana), título 28 centrado, «Artista · año · N canciones · N min» 14; botones píldora de 48: Reproducir (lima, ícono play) y Aleatorio (`surfaceContainer`, ícono shuffle); filas: número de pista en mono 14 en 28 dp, título 15 SemiBold, duración mono 12 y ⋮.
- **SCR-25 Detalle de artista (33:950)**: foto redonda 140 (sombra → plana), nombre 28, «N canciones · N álbumes», los mismos botones; secciones «Álbumes» y «Canciones» (Space Grotesk 18); carrusel de portadas 120 radio 18 con nombre 14 y año 12; filas de canción con portada y el **álbum** como segunda línea.
- **SCR-26 Hoja (33:1070)**: título «Ordenar y filtrar», etiqueta «Ordenar por» con 3 opciones de radio (lima), etiqueta «Dirección» con selector segmentado A → Z / Z → A (`surfaceContainer`, elegido lima) y botón «Listo» lima de 52. La fila de resumen de Canciones lleva aleatorio y el botón de filtros (ícono `ic_tune` nuevo). No hay filtros en el nodo, solo orden.
