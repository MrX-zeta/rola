# Especificación: Favoritas y Listas

**Carpeta**: `specs/004-favoritas-y-listas`

**Creada**: 2026-11-18

**Estado**: Borrador

**Tarea**: `ai/handoff/siguiente.md` · Tarea 05 · Grafo 1.8.0

**Entrada**: «Quiero marcar como favorita la canción que suena tocando un corazón sobre su portada, con una animación que se sienta bien, y encontrarlas todas en «Favoritas», fija arriba en Biblioteca · Listas. También quiero crear listas con un nombre, agregarles canciones desde una hoja con buscador, abecedario y casillas, reproducirlas o en aleatorio, quitar canciones, y desde el menú ⋮ (o pulsación larga) de cualquier canción: añadir a la cola, añadir a una lista, marcar/quitar favorita y borrar del dispositivo con el aviso de Android.»

**Referencias del grafo**: pantallas SCR-51 (corazón en Reproduciendo), SCR-23 (Biblioteca · Listas), SCR-30 (Crear lista), SCR-31 (Lista vacía), SCR-32 (Lista con canciones), SCR-33 (Hoja · agregar canciones), SCR-54 (Menú de opciones); decisiones DEC-29, DEC-06, DEC-07, DEC-08, DEC-05, DEC-09, DEC-27, DEC-28; tablas TBL-FAVORITES, TBL-PLAYLISTS, TBL-PLAYLIST-SONGS; operaciones OP-04 a OP-08; invariantes INV-02, INV-06, INV-11. Esta spec cita los ids; ante cualquier duda manda el nodo del grafo y, en lo visual, Figma.

## Aclaraciones

### Sesión 2026-11-18

Respondidas por la tarea 05 (Paso 2); no se preguntan a Brian.

- P: ¿Favoritas se puede renombrar o borrar, y cómo se ordena? → R: es una lista automática, no se renombra ni se borra; se ordena por fecha de marcado, la más reciente arriba (FR-009).
- P: ¿Qué chips muestra Biblioteca? → R: «Listas» y «Canciones»; «Álbumes» y «Artistas» llegan en la tarea 06 y no se muestran (FR-010).
- P: ¿Cómo se borra del dispositivo y qué se limpia? → R: con el aviso del sistema (`MediaStore.createDeleteRequest`), sin confirmación propia; al terminar, snackbar y limpieza de Favoritas, listas y cola (FR-028, FR-029).
- P: ¿Qué pasa con una canción guardada que ya no existe? → R: las referencias se guardan por el identificador de la canción en el sistema; si ya no existe se omite al mostrar, sin borrar la referencia hasta confirmar que no existe. En esta función, «confirmar» es que Rola misma la borró con el aviso del sistema; si desaparece por fuera, solo se oculta (FR-031).
- P: ¿Qué muestran la lista vacía, la lista con canciones y su menú? → R: SCR-31 y SCR-32 según DEC-06; en una lista el menú lleva «Quitar de la lista» y «Añadir a la cola» (FR-018, FR-019, FR-024).
- P: ¿Qué queda fuera? → R: ~~renombrar y borrar listas~~ (entran en la tarea 06), reordenar canciones dentro de una lista, Álbumes y Artistas (tarea 06) e historial (tarea 07).

### Sesión 2026-11-19 (tarea 06, Parte A)

Respondidas por la tarea 06; no se preguntan a Brian.

- P: ¿Se pueden renombrar y borrar listas? → R: sí, desde el ⋮ de cada lista en Biblioteca · Listas (Figma 32:1128) y en la pantalla de lista (34:1345). Favoritas no tiene ⋮ (FR-038, FR-039).
- P: ¿Cómo se ve «Añadir a una lista»? → R: SCR-55 (Figma 85:3348 / 85:3541) (FR-040).
- P: ¿Reproduciendo tiene «Añadir a una lista»? → R: sí, el botón junto al título; el ⋮ de Reproduciendo queda para Letras (tarea 09) y no se muestra (FR-041).
- P: ¿Qué dice «Reproduciendo desde»? → R: el origen real (DEC-24): Biblioteca, Favoritas o el nombre de la lista (FR-042).

## Escenarios de usuario y pruebas *(obligatorio)*

### Historia 1 · Marcar la canción que suena como favorita (Prioridad: P1)

Mientras escucho una canción en Reproduciendo (SCR-51), toco el corazón que está sobre la portada. El corazón se rellena de lima con un pequeño rebote, suelta una ráfaga corta de partículas y el teléfono vibra suave para confirmarlo. Si lo vuelvo a tocar, vuelve al contorno, sin ráfaga, y la canción deja de ser favorita.

**Por qué esta prioridad**: es la forma más rápida de guardar lo que me gusta y la base de «Favoritas».

**Prueba independiente**: reproducir una canción, abrir Reproduciendo, tocar el corazón: queda relleno; cerrar y abrir la app: sigue relleno para esa canción; tocarlo otra vez: vuelve al contorno.

**Escenarios de aceptación**:

1. **Dado** que suena una canción que no es favorita, **cuando** toco el corazón, **entonces** se rellena de lima con rebote, ráfaga de partículas y vibración, y la canción queda guardada como favorita.
2. **Dado** que suena una canción favorita, **cuando** toco el corazón, **entonces** vuelve al contorno sin ráfaga y la canción deja de ser favorita.
3. **Dado** que toco el corazón varias veces seguidas muy rápido, **cuando** termino, **entonces** el estado final corresponde al último toque y la animación no se traba ni se encola.
4. **Dado** que el sistema pide reducir movimiento, **cuando** toco el corazón, **entonces** solo cambia el relleno, sin rebote ni partículas.
5. **Dado** que cambia la canción que suena, **cuando** miro el corazón, **entonces** muestra el estado de la canción nueva.

---

### Historia 2 · Encontrar mis favoritas (Prioridad: P1)

En Biblioteca elijo el chip «Listas» (SCR-23). Arriba de todo, fija, está «Favoritas». Al abrirla veo mis canciones favoritas, la más reciente arriba, y puedo reproducirlas en orden o en aleatorio.

**Por qué esta prioridad**: marcar favoritas no sirve si no puedo volver a ellas.

**Prueba independiente**: marcar dos canciones en orden A, B; abrir Biblioteca · Listas · Favoritas: aparece B arriba y A abajo; tocar Reproducir: suena B.

**Escenarios de aceptación**:

1. **Dado** que estoy en Biblioteca, **cuando** miro los chips, **entonces** veo «Listas» y «Canciones» (sin «Álbumes» ni «Artistas»).
2. **Dado** que estoy en Biblioteca · Listas, **cuando** miro la lista de listas, **entonces** «Favoritas» es la primera lista, justo debajo de «Nueva lista», siempre, aunque tenga cero canciones, y muestra cuántas canciones tiene.
3. **Dado** que abro Favoritas con canciones, **cuando** la miro, **entonces** veo las canciones ordenadas por fecha de marcado, la más reciente arriba, con los botones Reproducir y Aleatorio.
4. **Dado** que Favoritas no tiene canciones, **cuando** la abro, **entonces** veo un estado vacío que explica cómo marcar favoritas.
5. **Dado** que estoy en Favoritas, **cuando** busco cómo renombrarla o borrarla, **entonces** no existe esa opción.

---

### Historia 3 · Crear una lista y llenarla (Prioridad: P1)

En Biblioteca · Listas toco «Crear lista» y escribo solo el nombre (SCR-30). Al crearla se abre la lista (SCR-31) y sube una hoja (SCR-33) con buscador, abecedario a la derecha y casillas junto a cada canción. Marco las que quiero y el botón «Agregar N canciones» las agrega a la lista.

**Por qué esta prioridad**: es la función principal de las listas.

**Prueba independiente**: crear la lista «Viaje», marcar tres canciones en la hoja y confirmar: la lista muestra esas tres canciones y en Listas aparece «Viaje» con 3 canciones.

**Escenarios de aceptación**:

1. **Dado** que toco «Crear lista», **cuando** aparece el diálogo, **entonces** pide solo el nombre y el botón de crear está desactivado mientras el nombre esté vacío o sea solo espacios.
2. **Dado** que escribo un nombre y confirmo, **cuando** se crea la lista, **entonces** se abre la lista y aparece la hoja para agregar canciones.
3. **Dado** que estoy en la hoja, **cuando** escribo en el buscador, **entonces** la lista se filtra por título, artista o álbum sin distinguir mayúsculas ni acentos.
4. **Dado** que estoy en la hoja, **cuando** toco una letra del abecedario, **entonces** salta a la primera canción con esa letra; al desplazarme, se ilumina la letra visible (DEC-05).
5. **Dado** que marqué N canciones, **cuando** miro el botón, **entonces** dice «Agregar N canciones» (en singular si N = 1) y está desactivado con N = 0.
6. **Dado** que confirmo, **cuando** se cierra la hoja, **entonces** las canciones quedan al final de la lista en el orden en que aparecen en la hoja, y veo una confirmación.
7. **Dado** que cierro la hoja sin confirmar, **cuando** vuelvo a la lista, **entonces** no se agregó nada.
8. **Dado** que una canción ya está en la lista, **cuando** abro la hoja, **entonces** aparece marcada como ya incluida y no se puede agregar dos veces.

---

### Historia 4 · Usar una lista (Prioridad: P2)

Abro una lista con canciones (SCR-32): veo su nombre, cuántas canciones tiene y los botones Reproducir, Aleatorio y Agregar. Desde el menú ⋮ de cada canción puedo quitarla de la lista o añadirla a la cola. Si la lista está vacía (SCR-31), veo un mensaje y el botón «Agregar canciones».

**Por qué esta prioridad**: completa el ciclo de una lista; depende de la Historia 3.

**Prueba independiente**: en una lista con tres canciones, tocar Aleatorio: suena una de ellas y la cola tiene las tres; quitar una desde ⋮: la lista queda con dos.

**Escenarios de aceptación**:

1. **Dado** que la lista tiene canciones, **cuando** toco Reproducir, **entonces** empieza la primera y la cola es la lista en su orden.
2. **Dado** que la lista tiene canciones, **cuando** toco Aleatorio, **entonces** la cola es la lista mezclada.
3. **Dado** que toco una canción de la lista, **cuando** empieza a sonar, **entonces** la cola es la lista desde esa canción en su orden.
4. **Dado** que abro ⋮ de una canción en una lista, **cuando** elijo «Quitar de la lista», **entonces** desaparece de la lista (el archivo no se toca) y veo una confirmación con Deshacer.
5. **Dado** que quito la última canción, **cuando** miro la lista, **entonces** veo el estado de lista vacía con «Agregar canciones».
6. **Dado** que toco Agregar (o «Agregar canciones» en la lista vacía), **cuando** aparece la hoja, **entonces** funciona igual que en la Historia 3.

---

### Historia 5 · Menú de opciones de una canción (Prioridad: P2)

En Biblioteca · Canciones, cada canción tiene ⋮ y también responde a la pulsación larga. Se abre un menú desplegable (SCR-54) con: Añadir a la cola, Añadir a una lista, Favorita / Quitar de favoritas y Borrar del dispositivo.

**Por qué esta prioridad**: reúne las acciones por canción sin tener que reproducirla primero.

**Prueba independiente**: en Biblioteca · Canciones, pulsación larga sobre una canción, «Añadir a una lista», elegir «Viaje»: la canción aparece al final de «Viaje».

**Escenarios de aceptación**:

1. **Dado** que estoy en Biblioteca · Canciones, **cuando** toco ⋮ o hago pulsación larga sobre una canción, **entonces** aparece el menú junto a esa canción con las cuatro opciones.
2. **Dado** que elijo «Añadir a la cola», **cuando** termina, **entonces** la canción queda al final de la cola, la música no se interrumpe y veo una confirmación. Si no había nada sonando, la canción queda lista para sonar.
3. **Dado** que elijo «Añadir a una lista», **cuando** aparece el selector, **entonces** veo mis listas (no Favoritas) y la opción de crear una nueva; al elegir una, la canción se agrega al final y veo una confirmación; si ya estaba, se avisa y no se duplica.
4. **Dado** que la canción no es favorita, **cuando** abro el menú, **entonces** dice «Favorita»; si ya lo es, dice «Quitar de favoritas». Elegirla cambia el estado igual que el corazón.
5. **Dado** que estoy en una lista, **cuando** abro el menú, **entonces** veo solo «Quitar de la lista» y «Añadir a la cola» (en Favoritas, «Quitar de la lista» quita la favorita).

---

### Historia 6 · Borrar una canción del dispositivo (Prioridad: P3)

Desde el menú elijo «Borrar del dispositivo». Android muestra su propio aviso; Rola no agrega otra confirmación. Si acepto, el archivo se borra, veo un snackbar con el resultado y la canción desaparece de la biblioteca, de Favoritas, de todas mis listas y de la cola.

**Por qué esta prioridad**: es destructiva y poco frecuente, pero sin ella las listas acumulan canciones que ya no quiero.

**Prueba independiente**: con un archivo de prueba que está en Favoritas, en una lista y en la cola, borrarlo desde el menú y aceptar el aviso de Android: desaparece de Canciones, Favoritas, la lista y la cola; aparece el snackbar.

**Escenarios de aceptación**:

1. **Dado** que elijo «Borrar del dispositivo», **cuando** se abre el aviso, **entonces** es el del sistema y la app no muestra un diálogo propio antes ni después (INV-02).
2. **Dado** que acepto el aviso, **cuando** termina, **entonces** veo un snackbar que confirma el borrado y la canción ya no aparece en Canciones, Favoritas, listas ni cola.
3. **Dado** que cancelo el aviso, **cuando** vuelvo a la app, **entonces** nada cambió y no se muestra error.
4. **Dado** que la canción borrada es la que suena, **cuando** se confirma el borrado, **entonces** la reproducción pasa a la siguiente de la cola (o se detiene si no hay otra), sin error.
5. **Dado** que el borrado falla, **cuando** vuelvo a la app, **entonces** veo un snackbar con el error y no se limpia ninguna referencia.

---

### Casos límite

- Una canción guardada en Favoritas o en una lista desaparece del teléfono por fuera de Rola (otra app, la computadora, una tarjeta SD retirada): no se muestra en Favoritas ni en las listas y los conteos no la cuentan, pero su referencia se conserva; si el archivo vuelve con el mismo identificador, reaparece.
- Dos listas con el mismo nombre: se permiten (son listas distintas).
- Nombre de lista muy largo: se corta con puntos suspensivos en las filas; el diálogo admite como máximo 60 caracteres.
- La hoja de agregar con miles de canciones se desplaza y filtra sin trabas.
- El buscador de la hoja sin resultados muestra un mensaje de «sin resultados» y el abecedario no estorba.
- Girar la pantalla o pasar la app a segundo plano con la hoja abierta conserva las casillas marcadas y el texto buscado.
- Marcar favorita sin nada sonando: el corazón no existe sin una canción en Reproduciendo.
- El menú de una canción que suena: «Añadir a la cola» la agrega igual (se puede repetir en la cola).
- Con letra del sistema grande, el menú, el diálogo y la hoja no cortan textos ni tapan el botón de confirmar.
- Pantallas en tema claro y oscuro según sus nodos de Figma.

## Requisitos *(obligatorio)*

### Requisitos funcionales

**Favoritas (DEC-29, SCR-51, TBL-FAVORITES)**

- **FR-001**: Reproduciendo DEBE mostrar un botón de corazón sobre la portada, en su esquina inferior izquierda, dentro de un círculo oscuro translúcido de 48 dp, según Figma 36:1465 / 51:4482.
- **FR-002**: El botón DEBE alternar si la canción actual es favorita y reflejar siempre el estado de la canción que suena.
- **FR-003**: Al marcar, el corazón DEBE rellenarse de lima con un rebote de escala, soltar una ráfaga de 6 a 8 partículas lima que salen del corazón y se desvanecen, y dar una vibración háptica de confirmación; todo en ≤ 350 ms.
- **FR-004**: Al desmarcar, el corazón DEBE volver al contorno sin ráfaga ni vibración de confirmación.
- **FR-005**: La animación DEBE ser interrumpible: toques rápidos repetidos dejan el estado del último toque y no encolan animaciones.
- **FR-006**: Con «reducir movimiento», el corazón DEBE cambiar solo el relleno, sin escala ni partículas.
- **FR-007**: El botón DEBE anunciarse al lector de pantalla como interruptor «Favorita», con su estado (activado/desactivado).
- **FR-008**: Las favoritas DEBEN guardarse en el teléfono, sobrevivir a cerrar la app y reiniciar, y registrar el momento en que se marcaron.
- **FR-009**: «Favoritas» DEBE ser una lista automática, fija como primera lista de Biblioteca · Listas (justo debajo de la fila «Nueva lista»), que no se puede renombrar ni borrar, ordenada por fecha de marcado, la más reciente arriba.

**Biblioteca · Listas (SCR-23, DEC-05)**

- **FR-010**: Biblioteca DEBE mostrar los chips «Listas» y «Canciones»; «Álbumes» y «Artistas» no se muestran todavía.
- **FR-011**: Biblioteca · Listas DEBE mostrar Favoritas arriba y debajo las listas del usuario con su nombre y número de canciones, y una acción para crear lista, según Figma 32:1128 / 51:2902.

**Listas (DEC-06, SCR-30 a SCR-33, OP-05, OP-06, OP-07, TBL-PLAYLISTS, TBL-PLAYLIST-SONGS)**

- **FR-012**: Crear lista DEBE pedir solo el nombre en un diálogo; no se aceptan nombres vacíos ni solo espacios; se recortan los espacios de los extremos; máximo 60 caracteres.
- **FR-013**: Al crear la lista se DEBE abrir la lista y, encima, la hoja para agregar canciones.
- **FR-014**: La hoja de agregar DEBE tener buscador, abecedario vertical a la derecha y una casilla por canción, y un botón «Agregar N canciones» que indica cuántas se agregan y está desactivado con cero.
- **FR-015**: El buscador de la hoja DEBE filtrar por título, artista y álbum sin distinguir mayúsculas ni acentos.
- **FR-016**: El abecedario de la hoja DEBE seguir DEC-05: ilumina la letra visible, al tocar salta a la primera coincidencia, agrupa sin acentos (Á→A), Ñ es letra propia y números y símbolos van en «#».
- **FR-017**: Las canciones confirmadas DEBEN agregarse al final de la lista en el orden en que aparecen en la hoja (por título), no en el orden en que se marcaron, sin duplicar las que ya están.
- **FR-018**: Una lista vacía DEBE mostrar un mensaje y el botón «Agregar canciones» (SCR-31).
- **FR-019**: Una lista con canciones DEBE mostrar su nombre, número de canciones y los botones Reproducir, Aleatorio y Agregar (SCR-32).
- **FR-020**: Reproducir DEBE poner como cola la lista en su orden desde la primera; Aleatorio, la lista mezclada; tocar una canción, la lista desde esa canción.
- **FR-021**: Quitar una canción de una lista DEBE quitar solo la referencia (no el archivo) y ofrecer Deshacer.
- **FR-022**: Si una lista deja de existir, sus canciones de lista DEBEN irse con ella; ninguna pertenencia queda huérfana. (Borrar listas desde la interfaz queda fuera de alcance.)

**Menú de opciones (DEC-07, SCR-54, OP-04)**

- **FR-023**: Cada canción de Biblioteca · Canciones DEBE abrir un menú desplegable desde su botón ⋮ y con pulsación larga, con: Añadir a la cola, Añadir a una lista, Favorita / Quitar de favoritas y Borrar del dispositivo.
- **FR-024**: En una lista (incluida Favoritas), el menú DEBE tener solo: Quitar de la lista y Añadir a la cola. En Favoritas, «Quitar de la lista» quita la favorita.
- **FR-025**: «Añadir a la cola» DEBE poner la canción al final de la cola sin interrumpir lo que suena (INV-11) y confirmar con un snackbar.
- **FR-026**: «Añadir a una lista» DEBE mostrar las listas del usuario (sin Favoritas) y la opción de crear una nueva; agregar al final, avisar si ya estaba y confirmar con un snackbar.
- **FR-027**: «Favorita / Quitar de favoritas» DEBE tener el mismo efecto que el corazón de Reproduciendo, y el corazón lo refleja si esa canción suena.

**Borrar del dispositivo (DEC-08, INV-02, OP-08)**

- **FR-028**: «Borrar del dispositivo» DEBE usar el aviso propio de Android; la app NO DEBE mostrar otra confirmación.
- **FR-029**: Si el borrado se confirma, DEBE mostrarse un snackbar con el resultado y quitarse la canción de Favoritas, de todas las listas y de la cola.
- **FR-030**: Si el usuario cancela el aviso, nada cambia; si el borrado falla, se muestra el error y no se limpia ninguna referencia.

**Transversales**

- **FR-031**: Las favoritas y las listas guardan el identificador de la canción en el sistema; una canción que ya no existe se omite al mostrar y en los conteos. Su referencia solo se borra cuando Rola confirma el borrado (FR-029); si desaparece por fuera de Rola, se conserva oculta.
- **FR-032**: Toda operación (crear lista, agregar, quitar, añadir a la cola, favorita desde el menú, borrar) DEBE mostrar su resultado (DEC-09): snackbar, con Deshacer cuando se pueda.
- **FR-033**: Nada de esta función usa red (INV-01).
- **FR-034**: Colores, tipografía, formas y espacios DEBEN salir del tema (INV-04); todas estas pantallas son planas, sin relieve (DEC-27).
- **FR-035**: Botones con área táctil ≥ 48 dp, descripciones accesibles en los íconos (⋮, corazón, casillas, letras del abecedario), contraste ≥ 4.5:1 y textos que respetan el tamaño de letra del sistema (INV-06).
- **FR-036**: Toda animación nueva (salvo el corazón, FR-003) DEBE durar ≤ 300 ms, ser interrumpible y respetar «reducir movimiento» (DEC-28).
- **FR-037**: Todas las pantallas DEBEN verse según sus nodos de Figma en oscuro y claro. Todos los textos, en español.

**Pendientes de listas (tarea 06, Parte A)**

- **FR-038**: Cada lista del usuario DEBE tener un ⋮ (en su fila de Biblioteca · Listas y en su pantalla) con «Renombrar» y «Borrar lista». Favoritas no tiene ⋮.
- **FR-039**: «Renombrar» DEBE abrir un diálogo igual a Crear lista con el nombre actual; «Guardar» se apaga si queda vacío. «Borrar lista» DEBE pedir confirmación con un diálogo propio (Android no da uno); al confirmar se borra la lista (no las canciones) y se muestra un snackbar con «Deshacer», que la restaura con sus canciones, su orden y sus fechas. Si se borra desde su pantalla, se vuelve a Biblioteca.
- **FR-040**: La hoja «Añadir a una lista» DEBE seguir SCR-55: título, la canción debajo («Título · Artista»), «Nueva lista» con ícono en `primaryContainer` y las listas con mosaicos de 48 dp.
- **FR-041**: Reproduciendo DEBE tener el botón «Añadir a una lista» junto al título, que abre la hoja de FR-040 para la canción que suena. El ⋮ de Reproduciendo no se muestra todavía.
- **FR-042**: «Reproduciendo desde» DEBE mostrar el origen real de la cola (DEC-24): «Biblioteca», «Favoritas» o el nombre de la lista desde la que se empezó a reproducir.

### Entidades clave

- **Favorita**: una canción marcada por el usuario. Referencia a la canción del sistema y momento en que se marcó (TBL-FAVORITES). Una canción es favorita o no; no se repite.
- **Lista**: creada por el usuario. Nombre, fecha de creación y de última modificación (TBL-PLAYLISTS).
- **Canción de lista**: pertenencia de una canción a una lista, con su posición y momento en que se agregó (TBL-PLAYLIST-SONGS). Una canción aparece como máximo una vez por lista. Si la lista desaparece, desaparecen sus canciones de lista.
- **Canción**: la del sistema (ya existe); estas entidades solo guardan su identificador.

## Criterios de éxito *(obligatorio)*

### Resultados medibles

- **SC-001**: Marcar la canción que suena como favorita toma un solo toque, y la animación completa termina en ≤ 350 ms.
- **SC-002**: Crear una lista y agregarle 10 canciones toma menos de 60 segundos.
- **SC-003**: Tras borrar una canción del dispositivo, en el 100 % de los casos deja de aparecer en Canciones, Favoritas, listas y cola sin reiniciar la app.
- **SC-004**: Favoritas y listas sobreviven al 100 % de cierres de la app y reinicios del teléfono.
- **SC-005**: Con «reducir movimiento», el corazón no escala ni muestra partículas.
- **SC-006**: Biblioteca · Listas, Crear lista, Lista vacía, Lista con canciones, la hoja, el menú y el corazón coinciden con Figma en oscuro y claro (revisión `design-critique` sin diferencias de contenido).
- **SC-007**: La hoja de agregar filtra y se desplaza sin trabas visibles con 2 000 canciones en la compilación `perfil`.

## Supuestos

- El menú de opciones se agrega en Biblioteca · Canciones y en las listas. Buscar aún es «Próximamente»: su menú llega con su tarea.
- Favoritas se abre con el mismo diseño de lista (SCR-32) pero sin botón Agregar: las favoritas se marcan con el corazón o el menú.
- Su estado vacío reutiliza el de Lista vacía (SCR-31) con texto propio y sin «Agregar canciones».
- Al agregar canciones a una lista desde la hoja, las que ya están aparecen marcadas y desactivadas.
- La limpieza de referencias al borrar cubre Favoritas, listas y cola; el historial (DEC-08) llega con la tarea 07.
- Fuera de alcance: renombrar y borrar listas, reordenar canciones dentro de una lista, Álbumes y Artistas (tarea 06), historial (tarea 07), abecedario en Biblioteca · Canciones.
