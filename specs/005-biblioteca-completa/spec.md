# Especificación: Biblioteca completa

**Carpeta**: `specs/005-biblioteca-completa`

**Creada**: 2026-11-19

**Estado**: Borrador

**Tarea**: `ai/handoff/siguiente.md` · Tarea 06 · Parte B · Grafo 1.9.0

**Entrada**: «En Biblioteca quiero los chips Listas, Álbumes, Artistas y Canciones. Álbumes en cuadrícula con portada, nombre, artista y año; Artistas en lista con foto redonda (la portada de uno de sus álbumes) y conteo. Al tocar un álbum veo su portada grande, datos, Reproducir/Aleatorio y sus canciones en orden de pista; al tocar un artista, sus álbumes en carrusel y sus canciones. Canciones, Álbumes y Artistas tienen abecedario. En Canciones, el botón de filtros abre una hoja para ordenar por título, artista o álbum, A→Z o Z→A, y se recuerda.»

**Referencias del grafo**: pantallas SCR-21 (Biblioteca · Álbumes), SCR-22 (Biblioteca · Artistas), SCR-24 (Detalle de álbum), SCR-25 (Detalle de artista), SCR-26 (Hoja de filtros y orden), cada una con `figma_nodo` y `figma_nodo_claro`; decisiones DEC-05 (chips, orden, abecedario), DEC-26 (la biblioteca sale del sistema), DEC-28 (movimiento, cascada, `biblioteca_fuera_del_navhost`), DEC-09 (retroalimentación), DEC-24 (origen en Reproduciendo), DEC-27 (estilo plano); almacén KV-DATASTORE (`sort_order`); invariantes INV-01, INV-04, INV-06 (con la excepción del abecedario), INV-11. Esta spec cita los ids; ante cualquier duda manda el nodo del grafo y, en lo visual, Figma.

## Aclaraciones

### Sesión 2026-11-19

Respondidas por la tarea 06 (Paso B2); no se preguntan a Brian.

- P: ¿En qué orden van los chips y dónde abre Biblioteca? → R: Listas, Álbumes, Artistas, Canciones; Biblioteca sigue abriendo en Canciones (FR-001).
- P: ¿De dónde salen álbumes y artistas y qué pasa con los vacíos? → R: de la biblioteca del sistema (identificador de álbum, artista y artista del álbum); «Artista desconocido» agrupa los vacíos. Las canciones de un álbum van por número de pista y, si no hay, por título (FR-005, FR-007, FR-009).
- P: ¿Cómo se ordenan Álbumes y Artistas y a qué aplica la hoja de orden? → R: Álbumes y Artistas por nombre con DEC-05; la hoja aplica solo a Canciones y se guarda en `sort_order` (FR-006, FR-010, FR-014, FR-016).
- P: ¿Cómo se sale de un detalle? → R: con «atrás» del sistema y con el botón de volver; la raíz de Biblioteca sigue fuera del NavHost (FR-004).
- P: ¿A qué aplica la cascada de entrada? → R: a las cuadrículas y listas de cada chip al entrar (DEC-28) (FR-003).

## Escenarios de usuario y pruebas *(obligatorio)*

### Historia 1 · Recorrer mis álbumes (Prioridad: P1)

Quiero ver todos mis álbumes como portadas en cuadrícula para elegir uno por su carátula, y abrirlo para escucharlo completo en su orden.

**Por qué esta prioridad**: escuchar un álbum entero en orden es la forma principal de escuchar música para muchos usuarios y hoy no es posible.

**Prueba independiente**: con una biblioteca de prueba de varios álbumes, tocar el chip «Álbumes», ver la cuadrícula ordenada, tocar un álbum, ver sus canciones por número de pista y reproducirlo desde la primera.

**Escenarios de aceptación**:

1. **Dado** que la biblioteca tiene álbumes, **cuando** toco el chip «Álbumes», **entonces** veo una cuadrícula con la portada, el nombre, el artista y el año de cada álbum, ordenada por nombre (DEC-05).
2. **Dado** que estoy en Álbumes, **cuando** toco un álbum, **entonces** se abre su detalle con la portada grande, nombre, artista, año, número de canciones y duración total, los botones Reproducir y Aleatorio y sus canciones por número de pista.
3. **Dado** el detalle de un álbum, **cuando** toco Reproducir, **entonces** suena la primera pista y la cola son las canciones del álbum en orden; «Reproduciendo desde» dice el nombre del álbum (DEC-24).
4. **Dado** el detalle de un álbum, **cuando** toco una canción, **entonces** suena esa canción y la cola sigue el orden del álbum.
5. **Dado** el detalle de un álbum, **cuando** uso «atrás» del sistema o el botón de volver, **entonces** regreso a Álbumes en el mismo lugar de la cuadrícula.

---

### Historia 2 · Recorrer mis artistas (Prioridad: P1)

Quiero ver la lista de artistas con una foto reconocible y cuántas canciones tienen, y abrir uno para ver sus álbumes y todas sus canciones.

**Por qué esta prioridad**: buscar por artista es el segundo camino más común para elegir música.

**Prueba independiente**: con una biblioteca de prueba, tocar «Artistas», ver la lista ordenada con fotos redondas y conteos, abrir un artista con dos álbumes y ver el carrusel de álbumes y sus canciones.

**Escenarios de aceptación**:

1. **Dado** que la biblioteca tiene artistas, **cuando** toco el chip «Artistas», **entonces** veo una lista ordenada por nombre (DEC-05), cada fila con una foto redonda (la portada de su álbum más reciente), el nombre y el conteo de álbumes y canciones.
2. **Dado** que hay canciones sin artista, **cuando** veo Artistas, **entonces** aparecen agrupadas en una sola fila «Artista desconocido».
3. **Dado** Artistas, **cuando** toco un artista, **entonces** se abre su detalle con su foto, nombre, conteo, Reproducir/Aleatorio, un carrusel horizontal con sus álbumes y debajo todas sus canciones.
4. **Dado** el detalle de un artista, **cuando** toco un álbum del carrusel, **entonces** se abre el detalle de ese álbum; con «atrás» vuelvo al artista.
5. **Dado** el detalle de un artista, **cuando** toco Reproducir, una canción o Aleatorio, **entonces** suena y «Reproduciendo desde» dice el nombre del artista.

---

### Historia 3 · Ordenar Canciones a mi gusto (Prioridad: P2)

Quiero ordenar Canciones por título, artista o álbum, de A a Z o de Z a A, y que Rola lo recuerde la próxima vez.

**Por qué esta prioridad**: mejora la búsqueda visual en bibliotecas grandes, pero sin ella la app ya funciona con el orden por título.

**Prueba independiente**: abrir la hoja de orden, elegir «Artista» y «Z→A», ver la lista reordenada, cerrar y volver a abrir la app y comprobar que se mantiene.

**Escenarios de aceptación**:

1. **Dado** Canciones, **cuando** toco el botón de filtros, **entonces** se abre la hoja de filtros y orden (SCR-26) con el criterio (Título, Artista, Álbum) y la dirección (A→Z, Z→A) actuales marcados.
2. **Dado** la hoja de orden, **cuando** elijo un criterio o una dirección, **entonces** la lista de Canciones se reordena y el resumen arriba de la lista dice el orden elegido (p. ej. «120 canciones · Artista Z–A»).
3. **Dado** que elegí un orden, **cuando** cierro y vuelvo a abrir la app, **entonces** Canciones conserva ese orden.
4. **Dado** un orden por artista o por álbum, **cuando** varias canciones comparten artista o álbum, **entonces** dentro de cada grupo se ordenan por título.
5. **Dado** cualquier orden, **cuando** uso el abecedario, **entonces** salta a la primera canción cuyo valor del criterio elegido empieza con esa letra.

---

### Historia 4 · Moverme entre chips y saltar con el abecedario (Prioridad: P2)

Quiero cambiar entre Listas, Álbumes, Artistas y Canciones con los chips y, en las tres últimas, saltar con el abecedario.

**Por qué esta prioridad**: es la navegación que une las vistas anteriores.

**Prueba independiente**: tocar cada chip y comprobar el contenido y el orden de los chips; en Álbumes y Artistas tocar una letra y comprobar el salto.

**Escenarios de aceptación**:

1. **Dado** Biblioteca, **cuando** la abro, **entonces** veo los chips en este orden: Listas, Álbumes, Artistas, Canciones, con «Canciones» seleccionado.
2. **Dado** cualquier chip, **cuando** toco otro, **entonces** el contenido cambia al de ese chip y sus filas o celdas visibles entran en cascada (DEC-28); con «reducir movimiento», sin cascada.
3. **Dado** Canciones, Álbumes o Artistas, **cuando** toco o arrastro sobre una letra del abecedario, **entonces** la lista salta a la primera coincidencia por nombre según DEC-05, y la letra visible se ilumina al desplazarme.
4. **Dado** Listas, **entonces** no hay abecedario.

---

### Casos límite

- Biblioteca vacía: Álbumes y Artistas muestran el mismo estado vacío que Canciones.
- Álbum sin nombre: se muestra como «Álbum desconocido» y se agrupa por su identificador del sistema, no con otros álbumes sin nombre.
- Dos álbumes distintos con el mismo nombre (p. ej. «Greatest Hits» de dos artistas): son dos álbumes distintos (los separa su identificador del sistema).
- Álbum sin año: no se muestra año (ni separador vacío).
- Álbum sin portada: se muestra la portada genérica de la app; un artista sin ninguna portada muestra la genérica en círculo.
- Canciones sin número de pista en un álbum: van después de las que sí lo tienen, ordenadas por título; los números de pista que traen el disco (p. ej. 1001, 2001) se respetan para que el disco 1 vaya antes del 2.
- Artista con un solo álbum: el carrusel muestra un solo álbum.
- Un álbum con canciones de varios artistas: el artista del álbum es su «artista del álbum»; si no lo trae, el de la primera pista.
- La biblioteca cambia (se agrega o se borra una canción) con un detalle abierto: el detalle se actualiza; si el álbum o artista desaparece, se vuelve a Biblioteca.
- Un valor de `sort_order` guardado que ya no es válido: se usa el orden por defecto (Título A→Z).
- 2 000 canciones y unos 200 álbumes: entrar a Biblioteca y cambiar de chip no se traba.

## Requisitos *(obligatorio)*

### Requisitos funcionales

**Chips y Biblioteca (DEC-05, DEC-28)**

- **FR-001**: Biblioteca DEBE mostrar los chips Listas, Álbumes, Artistas y Canciones, en ese orden, y abrir en Canciones.
- **FR-002**: Tocar un chip DEBE cambiar el contenido al de ese chip y conservar la posición de desplazamiento de cada chip mientras la app esté abierta.
- **FR-003**: Al entrar a Biblioteca y al cambiar de chip, las filas (listas) o celdas (cuadrícula) visibles de ese chip DEBEN aparecer en cascada según DEC-28 (`despliegue_listas`); al desplazar no hay cascada; con «reducir movimiento», tampoco.
- **FR-004**: La raíz de Biblioteca DEBE seguir fuera del NavHost (DEC-28 `biblioteca_fuera_del_navhost`); los detalles de álbum y artista son pantallas hijas dentro de él, con «atrás» del sistema y botón de volver.

**Álbumes (SCR-21, SCR-24)**

- **FR-005**: Los álbumes DEBEN salir de la biblioteca del sistema (DEC-26), uno por identificador de álbum del sistema.
- **FR-006**: Álbumes DEBE mostrar una cuadrícula con portada, nombre, artista y año de cada álbum, ordenada por nombre con las reglas de DEC-05.
- **FR-007**: El detalle de álbum DEBE mostrar la portada grande, nombre, artista, año, número de canciones y duración total, Reproducir y Aleatorio, y sus canciones por número de pista; sin número de pista, por título.
- **FR-008**: Reproducir, Aleatorio o tocar una canción en el detalle de álbum DEBE poner como cola las canciones del álbum en su orden, y «Reproduciendo desde» DEBE decir el nombre del álbum (DEC-24).

**Artistas (SCR-22, SCR-25)**

- **FR-009**: Los artistas DEBEN salir de la biblioteca del sistema, usando el artista del álbum cuando exista y si no el artista de la canción; las canciones sin artista se agrupan en «Artista desconocido».
- **FR-010**: Artistas DEBE mostrar una lista ordenada por nombre (DEC-05), con una foto redonda (la portada de su álbum más reciente), el nombre y el conteo de álbumes y canciones.
- **FR-011**: El detalle de artista DEBE mostrar su foto, nombre, conteo, Reproducir/Aleatorio, un carrusel horizontal con sus álbumes (del más reciente al más antiguo) y todas sus canciones.
- **FR-012**: Tocar un álbum del carrusel DEBE abrir su detalle; Reproducir, Aleatorio o una canción DEBEN reproducir las canciones del artista y «Reproduciendo desde» DEBE decir su nombre (DEC-24).

**Orden de Canciones (SCR-26, KV-DATASTORE)**

- **FR-013**: El botón de filtros de Canciones DEBE abrir la hoja de filtros y orden con criterio (Título, Artista, Álbum) y dirección (A→Z, Z→A).
- **FR-014**: El orden elegido DEBE aplicarse al instante a Canciones, mostrarse en el resumen arriba de la lista y guardarse en `sort_order` para sobrevivir al cierre de la app.
- **FR-015**: Dentro de un mismo artista o álbum, las canciones DEBEN ordenarse por título. Todas las comparaciones siguen DEC-05.
- **FR-016**: El orden elegido aplica solo a Canciones; Álbumes y Artistas se ordenan siempre por nombre A→Z.

**Abecedario (DEC-05, INV-06)**

- **FR-017**: Canciones, Álbumes y Artistas DEBEN tener el abecedario de DEC-05; en Canciones salta según el criterio de orden elegido, y en Álbumes y Artistas según el nombre. Con Z→A las letras se recorren en el mismo sentido que la lista.
- **FR-018**: El abecedario DEBE seguir la excepción de INV-06 (un control accesible con una acción por letra).

**Transversales**

- **FR-019**: Las portadas DEBEN pedirse al tamaño en que se dibujan.
- **FR-020**: Toda acción DEBE mostrar su resultado (DEC-09); los estados de carga usan esqueletos.
- **FR-021**: Nada de esta función usa red (INV-01).
- **FR-022**: Colores, tipografía, formas y espacios DEBEN salir del tema (INV-04); estilo plano (DEC-27).
- **FR-023**: Botones con área táctil ≥ 48 dp, descripciones en los íconos, contraste ≥ 4.5:1 y tamaño de letra del sistema (INV-06).
- **FR-024**: Toda animación nueva DEBE durar ≤ 300 ms, ser interrumpible y respetar «reducir movimiento» (DEC-28).
- **FR-025**: Todas las pantallas DEBEN verse según sus nodos de Figma en oscuro y claro. Textos en español.
- **FR-026**: Ninguna acción de esta función detiene lo que suena salvo reproducir otra cosa (INV-11).

### Entidades clave

- **Álbum**: agrupación del sistema. Identificador, nombre, artista del álbum, año, portada, canciones con su número de pista. Se deriva de las canciones; no se guarda en la app.
- **Artista**: agrupación por nombre de artista (del álbum o de la canción). Nombre, álbumes, canciones, foto (portada de su álbum más reciente). Se deriva; no se guarda.
- **Orden de Canciones**: criterio (título, artista, álbum) y dirección (A→Z, Z→A). Se guarda en `sort_order` (KV-DATASTORE).
- **Canción**: la del sistema (ya existe); suma número de pista, año, identificador de álbum y artista del álbum.

## Criterios de éxito *(obligatorio)*

### Resultados medibles

- **SC-001**: Desde Biblioteca, empezar a escuchar un álbum completo en orden toma 3 toques o menos (chip, álbum, Reproducir).
- **SC-002**: El 100 % de los álbumes con número de pista suenan en el orden de sus pistas.
- **SC-003**: El orden elegido para Canciones se conserva en el 100 % de los cierres y reinicios de la app.
- **SC-004**: Con 2 000 canciones y unos 200 álbumes, entrar a Biblioteca y cambiar entre chips no muestra trabas visibles en la compilación `perfil`.
- **SC-005**: Álbumes, Artistas, Detalle de álbum, Detalle de artista y la hoja de orden coinciden con Figma en oscuro y claro (revisión `design-critique` sin diferencias de contenido).
- **SC-006**: Con «reducir movimiento», ninguna lista o cuadrícula entra en cascada.

## Supuestos

- Los artículos («The», «El», «La») no se ignoran al ordenar: PEND-05 sigue propuesto y no se asume; se ordena por el texto tal cual, con las reglas de DEC-05.
- El año de un álbum es el mayor año de sus canciones.
- «Álbum más reciente» es el de mayor año; con el mismo año o sin año, el de nombre menor según DEC-05.
- El carrusel de álbumes de un artista y su lista de canciones no tienen abecedario.
- El menú ⋮ de cada canción (DEC-07) funciona igual en los detalles de álbum y artista que en Canciones.
- Buscar, Inicio, filtros por género o carpeta y la duración mínima de escaneo (PEND-07) quedan fuera de esta función.
