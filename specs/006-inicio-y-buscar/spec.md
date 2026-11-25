# Especificación: Inicio y Buscar

**Carpeta**: `specs/006-inicio-y-buscar`

**Creada**: 2026-11-25

**Estado**: Borrador

**Tarea**: `ai/handoff/siguiente.md` · Tarea 07 · Parte B · Grafo 1.11.0

**Entrada**: «Al abrir Rola veo Inicio: un saludo según la hora, lo que escuché hace poco en un carrusel, y «Para ti» con mi canción más escuchada lista para reproducir, un mosaico de Aleatorio con el total de canciones y otro de Favoritas. La primera vez, sin historial, me invita a reproducir algo o ir a Biblioteca. Si no hay canciones en el teléfono, me lo dice y puedo escanear de nuevo. Si cierro la app o el sistema la cierra, al volver encuentro la canción y la cola donde las dejé, en pausa. En Buscar escribo y al instante veo canciones, álbumes, artistas y listas que coinciden, con la parte buscada resaltada; si no hay nada, me lo dice.»

**Referencias del grafo**: pantallas SCR-10 (Inicio), SCR-11 (Inicio · primera vez), SCR-12 (Sin canciones), SCR-40 (Buscar), SCR-41 (Buscar · sin resultados), cada una con `figma_nodo` y `figma_nodo_claro`; decisiones DEC-31 (persistencia), DEC-30 (Inicio), DEC-25 (Buscar), DEC-03 (navegación), DEC-07 y DEC-08 (menú y borrar), DEC-09 (retroalimentación), DEC-19 (vacíos), DEC-24 (origen en Reproduciendo), DEC-26 (la biblioteca sale del sistema), DEC-27 (plano y relieve acotado), DEC-28 (movimiento), DEC-29 (Favoritas); almacenes TBL-HISTORY, TBL-QUEUE y KV-DATASTORE; operaciones OP-01, OP-09, OP-16; invariantes INV-01, INV-02, INV-04, INV-05, INV-06, INV-11. Ante cualquier duda manda el nodo del grafo y, en lo visual, Figma.

## Aclaraciones

### Sesión 2026-11-25

Respondidas por la tarea 07 (Paso B2); no se preguntan a Brian, salvo la última.

- P: ¿Cuándo cuenta una reproducción? → R: cuando suena ≥ 30 s, o cuando termina si dura menos. «Escuchado hace poco»: las últimas 10 canciones distintas, la más reciente primero. «Tu más escuchada»: más reproducciones; empate, la más reciente. Al borrar una canción del dispositivo se borra su historial (FR-010 a FR-014).
- P: ¿Qué dice el saludo? → R: «Buenos días» (5:00–11:59), «Buenas tardes» (12:00–18:59), «Buenas noches» (19:00–4:59), con la fecha en español (FR-002).
- P: ¿Qué no se muestra todavía? → R: «Ver todo», el engrane de Ajustes y «Abrir Ajustes» de SCR-12 (FR-008, FR-018).
- P: ¿Qué hace «Escanear de nuevo»? → R: vuelve a consultar la biblioteca del sistema y le pide escanear la carpeta Música; muestra progreso y resultado (FR-016, FR-017).
- P: ¿Cómo busca Buscar? → R: sin texto, solo el campo con ayuda; resultados al escribir (≈ 150 ms de espera), agrupados (Canciones, Álbumes, Artistas, Listas con Favoritas), máximo 5 por grupo, primero lo que empieza con lo buscado y luego lo que lo contiene; sin mayúsculas ni acentos; mismo menú ⋮ que Biblioteca; el teclado se abre al entrar (FR-030 a FR-039).
- P: ¿Dónde abre la app? → R: en Inicio; termina lo temporal de DEC-03 (FR-001).
- P: ¿Qué se conserva al cerrar? → R: la cola en su orden actual, la canción, el minuto exacto, aleatorio, repetir y el origen; se restaura en pausa y nunca suena sola. Las canciones que ya no existen se omiten en silencio; si no queda ninguna, no hay mini reproductor (FR-020 a FR-029).
- P: ¿Se guarda también el temporizador de sueño? → **Preguntado a Brian el 2026-11-25, sin respuesta.** DEC-31 propone «no» y lo deja pendiente; KV-DATASTORE, en cambio, lista `sleep_timer_end`. Por INV-09 no se asume: esta función no lo guarda y queda anotado en el reporte (FR-029).

## Escenarios de usuario y pruebas *(obligatorio)*

### Historia 1 · Volver donde me quedé (Prioridad: P1)

Cierro la app, o el sistema la cierra, y al volver quiero encontrar la canción, el minuto y la cola tal como los dejé, en pausa, para seguir con un toque.

**Por qué esta prioridad**: es un problema que Brian ya tiene: hoy se pierden la canción y la cola ordenada.

**Prueba independiente**: ordenar la cola, adelantar una canción a la mitad, cerrar la app deslizándola y forzar su cierre, reabrir y comparar.

**Escenarios de aceptación**:

1. **Dado** que suena una canción con una cola reordenada, **cuando** cierro la app y la abro de nuevo, **entonces** el mini reproductor muestra la misma canción con la barra en su minuto, en pausa, y la cola está en el mismo orden.
2. **Dado** que la app se restauró, **cuando** no toco nada, **entonces** nada suena; **cuando** toco reproducir, **entonces** sigue desde ese minuto.
3. **Dado** que tenía aleatorio y repetir encendidos, **cuando** vuelvo, **entonces** siguen igual, y la cola aleatoria conserva su orden; al apagar aleatorio, la cola vuelve a su orden original.
4. **Dado** que la cola venía de un álbum, **cuando** vuelvo, **entonces** «Reproduciendo desde» dice ese álbum.
5. **Dado** que se borró del teléfono una canción de la cola, **cuando** vuelvo, **entonces** la cola aparece sin ella y sin aviso; si era la actual, queda cargada la siguiente que exista, desde el principio.
6. **Dado** que ya no existe ninguna canción de la cola, **cuando** vuelvo, **entonces** no hay mini reproductor.
7. **Dado** que el sistema termina la app de golpe mientras suena, **cuando** vuelvo, **entonces** el minuto restaurado se aleja como mucho unos 5 s del real.

---

### Historia 2 · Inicio me recibe con lo mío (Prioridad: P1)

Al abrir Rola quiero ver un saludo, lo que escuché hace poco y accesos rápidos a lo que más uso.

**Por qué esta prioridad**: es la pantalla de entrada (DEC-03) y hoy dice «Próximamente».

**Prueba independiente**: con historial de varias canciones, abrir la app y ver el saludo, el carrusel y los mosaicos; tocar cada uno.

**Escenarios de aceptación**:

1. **Dado** que tengo historial, **cuando** abro la app, **entonces** estoy en Inicio y veo el saludo de la hora con la fecha, «Escuchado hace poco» y «Para ti».
2. **Dado** «Escuchado hace poco», **entonces** hay hasta 10 canciones distintas, la más reciente primero; **cuando** toco una, **entonces** suena.
3. **Dado** «Tu más escuchada», **entonces** veo portada, título, artista y cuántas veces; **cuando** toco su botón o el mosaico, **entonces** suena.
4. **Dado** el mosaico «Aleatorio», **entonces** dice el total de canciones; **cuando** lo toco, **entonces** suena toda la biblioteca en orden al azar.
5. **Dado** el mosaico «Favoritas», **entonces** dice cuántas hay; **cuando** lo toco, **entonces** se abre Favoritas y «atrás» regresa a Inicio.
6. **Dado** que escucho una canción 30 s, **cuando** vuelvo a Inicio, **entonces** aparece primera en «Escuchado hace poco».

---

### Historia 3 · La primera vez y sin música (Prioridad: P2)

La primera vez quiero una invitación clara a empezar; y si no hay música en el teléfono, saberlo y poder escanear de nuevo.

**Por qué esta prioridad**: son los estados que ve alguien nuevo; sin ellos Inicio quedaría vacío.

**Prueba independiente**: abrir sin historial; abrir sin canciones, copiar una y escanear.

**Escenarios de aceptación**:

1. **Dado** que hay canciones pero no historial, **cuando** abro Inicio, **entonces** veo «Aún no escuchas nada» con «Aleatorio» e «Ir a Biblioteca», y los mosaicos Aleatorio y Favoritas, sin carrusel ni «Tu más escuchada».
2. **Dado** ese estado, **cuando** toco «Ir a Biblioteca», **entonces** se abre Biblioteca.
3. **Dado** que no hay canciones en el teléfono, **cuando** abro Inicio, **entonces** veo «No encontramos música» con «Escanear de nuevo».
4. **Dado** ese estado, **cuando** toco «Escanear de nuevo», **entonces** veo que está escaneando y, al terminar, el resultado; si hay canciones nuevas, Inicio las muestra.

---

### Historia 4 · Encontrar algo escribiendo (Prioridad: P1)

En Buscar quiero escribir unas letras y ver al instante lo que coincide, sin pulsar ningún botón.

**Por qué esta prioridad**: con bibliotecas grandes es la forma más rápida de llegar a una canción.

**Prueba independiente**: escribir «mar» y ver los grupos, el resaltado y el orden; tocar un resultado de cada grupo.

**Escenarios de aceptación**:

1. **Dado** Buscar, **cuando** entro, **entonces** veo el título, el campo con ayuda y el teclado abierto, sin nada más.
2. **Dado** el campo, **cuando** escribo «mar», **entonces** veo hasta 5 resultados por grupo (Canciones, Álbumes, Artistas, Listas), con «mar» resaltado en cada nombre; los grupos sin resultados no aparecen.
3. **Dado** varios resultados en un grupo, **entonces** los que empiezan con lo buscado van antes que los que lo contienen.
4. **Dado** resultados en pantalla, **cuando** sigo escribiendo, **entonces** la lista cambia sin parpadear ni vaciarse entre letra y letra.
5. **Dado** «lluvia ácida» sin coincidencias, **entonces** veo «Sin resultados para «lluvia ácida»» y un consejo.
6. **Dado** un resultado, **cuando** toco una canción, **entonces** suena; un álbum, artista o lista abre su pantalla y «atrás» regresa a los resultados.
7. **Dado** una canción en los resultados, **cuando** toco ⋮ o la mantengo pulsada, **entonces** veo el mismo menú que en Biblioteca.
8. **Dado** texto en el campo, **cuando** toco «Borrar búsqueda», **entonces** el campo queda vacío y con el cursor listo.

---

### Casos límite

- Canción del historial o de «Tu más escuchada» que ya no existe: se omite y la lista se completa con la siguiente; si no queda ninguna, Inicio se ve como la primera vez.
- Menos de 10 canciones distintas escuchadas: se muestran las que haya.
- Dos canciones con las mismas reproducciones: gana la que sonó más recientemente.
- Canción de menos de 30 s: cuenta solo si llega a su final; saltarla no cuenta.
- Canción repetida (repetir una): cada vuelta cuenta por separado.
- Pausar y seguir: el tiempo que suena se suma; el tiempo en pausa no cuenta. Adelantar la barra no suma.
- Tocar algo justo al abrir, antes de terminar de restaurar: la orden se aplica después de restaurar; no se pierde ni se pisa.
- Canción restaurada que no se puede leer: se pasa a la siguiente **sin** empezar a sonar.
- Sin permiso de música al abrir: no se restaura nada y lo guardado se conserva.
- Guardado dañado o ilegible: la app abre sin cola y sin cerrarse.
- Cola de 2 000 canciones: guardar y restaurar no traba la interfaz.
- Se vacía la cola («Limpiar» deja la actual; borrar la última canción la deja vacía): lo guardado queda igual de vacío.
- Buscar solo espacios: es como no escribir nada. Los espacios a los lados no cuentan.
- Buscar con acentos, mayúsculas o «ñ»: «cancion» encuentra «Canción»; «ñ» es letra propia.
- Nombres desconocidos: no salen en Buscar por «Artista desconocido» ni «Álbum desconocido».
- Escaneo que tarda demasiado o falla: termina con su aviso y el botón vuelve a servir.
- Medianoche o cambio de hora con la app abierta: el saludo se actualiza al volver a la app.
- Letra del sistema grande: Inicio y Buscar se desplazan; ningún texto se corta sin puntos suspensivos.

## Requisitos *(obligatorio)*

### Requisitos funcionales

**Inicio (DEC-30, SCR-10, SCR-11, DEC-03)**

- **FR-001**: La app DEBE abrir en Inicio (DEC-03).
- **FR-002**: Inicio DEBE mostrar el logo, la fecha en español y el saludo según la hora: «Buenos días» de 5:00 a 11:59, «Buenas tardes» de 12:00 a 18:59 y «Buenas noches» de 19:00 a 4:59; se actualiza al volver a la app.
- **FR-003**: Con historial, Inicio DEBE mostrar «Escuchado hace poco» como carrusel horizontal (portada, título y artista) y, en «Para ti», «Tu más escuchada» (portada, título, artista y número de veces, con botón Reproducir), «Aleatorio · N canciones» y «Favoritas · N».
- **FR-004**: Tocar una canción de Inicio (carrusel o «Tu más escuchada») DEBE reproducirla igual que en Biblioteca · Canciones: la cola es Canciones en su orden, desde esa canción, con origen «Biblioteca» (DEC-24).
- **FR-005**: «Aleatorio» DEBE reproducir toda la biblioteca en orden al azar.
- **FR-006**: «Favoritas» DEBE abrir Favoritas como hija de Inicio: la píldora sigue en Inicio y «atrás» regresa a Inicio.
- **FR-007**: Sin historial, Inicio DEBE mostrar SCR-11: la invitación con «Aleatorio» e «Ir a Biblioteca» y los mosaicos Aleatorio y Favoritas.
- **FR-008**: «Ver todo», el engrane de Ajustes y el mosaico Letras NO DEBEN mostrarse (DEC-30).
- **FR-009**: Mientras cargan las canciones o el historial, Inicio DEBE mostrar esqueletos con la forma del contenido si la espera pasa de 300 ms (DEC-09), sin enseñar antes un estado vacío que no corresponde.

**Historial (TBL-HISTORY, DEC-08)**

- **FR-010**: Se DEBE registrar una reproducción cuando una canción suena ≥ 30 s (tiempo realmente sonando) o cuando termina si dura menos de 30 s; una sola vez por cada vez que se escucha.
- **FR-011**: «Escuchado hace poco» DEBE ser las últimas 10 canciones distintas que existan, la más reciente primero.
- **FR-012**: «Tu más escuchada» DEBE ser la canción existente con más reproducciones; con empate, la que sonó más recientemente.
- **FR-013**: Al borrar una canción del dispositivo desde Rola se DEBE borrar su historial (DEC-08). Si desaparece por fuera, solo se omite.
- **FR-014**: El registro DEBE ocurrir también con la pantalla apagada o la app cerrada mientras suena, y no DEBE interrumpir la música (INV-11).

**Sin canciones (SCR-12, DEC-19, DEC-26, OP-16)**

- **FR-015**: Sin canciones en el teléfono, Inicio DEBE mostrar SCR-12 con «Escanear de nuevo».
- **FR-016**: «Escanear de nuevo» DEBE volver a consultar la biblioteca del sistema y pedirle escanear la carpeta Música.
- **FR-017**: Durante el escaneo el botón DEBE mostrar progreso y no aceptar otro toque; al terminar, un aviso DEBE decir cuántas canciones hay o que no se encontró música (INV-05).
- **FR-018**: «Abrir Ajustes» y el engrane NO DEBEN mostrarse todavía en SCR-12.

**Persistencia de la reproducción (DEC-31, TBL-QUEUE)**

- **FR-020**: Se DEBEN guardar la cola en su orden, la canción actual, el minuto, aleatorio (con el orden aleatorio en que va la cola), repetir y el origen.
- **FR-021**: Se DEBE guardar al cambiar la cola o la canción, al pausar, al mover la barra, al pasar la app a segundo plano y, mientras suena, cada ~5 s (solo el minuto).
- **FR-022**: Al abrir la app o recrearse el servicio se DEBE restaurar todo **en pausa**; nunca suena sola, ni siquiera si la canción restaurada no se puede leer.
- **FR-023**: Tras restaurar, el mini reproductor DEBE mostrar la canción con la barra en su minuto, y Reproduciendo y Cola DEBEN abrir igual que antes de cerrar.
- **FR-024**: Las canciones que ya no existen se DEBEN omitir en silencio, conservando el orden de las demás. Si falta la actual, queda cargada la siguiente que exista en el orden en que iban a sonar, desde el principio; si no hay siguiente, la primera.
- **FR-025**: Si no queda ninguna canción (o no se pueden leer), NO DEBE haber mini reproductor y lo guardado NO se borra por eso.
- **FR-026**: Guardar y restaurar NO DEBEN bloquear la interfaz, y cambiar solo el minuto NO DEBE reescribir la cola.
- **FR-027**: Las órdenes del usuario que lleguen antes de terminar de restaurar se DEBEN aplicar después, en su orden.
- **FR-028**: Un guardado dañado DEBE ignorarse sin cerrar la app.
- **FR-029**: El temporizador de sueño NO se guarda (propuesto en DEC-31, pendiente de confirmar por Brian).

**Buscar (DEC-25, SCR-40, SCR-41, OP-09)**

- **FR-030**: Buscar DEBE mostrar el título, el campo con texto de ayuda y, sin texto, nada más; al entrar sin texto, el teclado se abre.
- **FR-031**: Los resultados DEBEN aparecer al escribir, sin botón, con una espera de ≈ 150 ms, y reemplazarse sin vaciar la lista ni parpadear.
- **FR-032**: Los resultados DEBEN agruparse en Canciones, Álbumes, Artistas y Listas (con Favoritas), en ese orden, con máximo 5 por grupo; un grupo vacío no se muestra.
- **FR-033**: Cada grupo DEBE comparar por el nombre de su elemento (título de la canción, nombre del álbum, del artista o de la lista) sin mayúsculas ni acentos y con «ñ» como letra propia.
- **FR-034**: Dentro de cada grupo DEBEN ir primero los que empiezan con lo buscado y luego los que lo contienen; a igual relevancia, en el orden de DEC-05.
- **FR-035**: La coincidencia DEBE verse resaltada en el nombre, sobre el texto original (con sus acentos y mayúsculas).
- **FR-036**: Sin coincidencias, DEBE mostrarse SCR-41 con lo buscado entre comillas.
- **FR-037**: Tocar una canción DEBE reproducirla como en FR-004; un álbum, artista o lista abre su pantalla como hija de Buscar, y «atrás» regresa a Buscar con su texto y sus resultados.
- **FR-038**: Cada canción DEBE tener el menú ⋮ y la pulsación larga de Biblioteca (DEC-07).
- **FR-039**: Con texto, el campo DEBE tener «Borrar búsqueda». El teclado no DEBE tapar el final de los resultados.

**Transversales**

- **FR-040**: Inicio y Buscar son destinos del NavHost; la raíz de Biblioteca sigue fuera (DEC-28). «Atrás» respeta la pila de cada pestaña; tocar la pestaña activa vuelve a su raíz.
- **FR-041**: El relieve DEBE usarse solo donde Figma lo muestra en SCR-12 (círculo y botón), SCR-40 (campo) y SCR-41 (campo y círculo) (DEC-27); Inicio es plano.
- **FR-042**: Al entrar a Inicio, el contenido DEBE aparecer en cascada (DEC-28 `despliegue_listas`); en Buscar, solo cuando aparecen los primeros resultados, no al seguir escribiendo. Duración total ≤ 300 ms; con «reducir movimiento», sin cascada.
- **FR-043**: Nada de esta función usa red (INV-01) y el historial no sale del teléfono.
- **FR-044**: Colores, tipografía, formas y espacios salen del tema (INV-04). Textos en español.
- **FR-045**: Botones con área táctil ≥ 48 dp, descripción en los de solo ícono, contraste ≥ 4.5:1 y tamaño de letra del sistema (INV-06); el resaltado de Buscar no depende solo del color para leerse.
- **FR-046**: Actualizar desde la versión anterior NO DEBE perder Favoritas ni listas.

### Entidades clave

- **Reproducción**: una escucha que contó. Canción (identificador del sistema) y momento. TBL-HISTORY.
- **Cola guardada**: las canciones de la cola en su orden, con su lugar en el orden de reproducción. TBL-QUEUE.
- **Estado de reproducción guardado**: índice actual, minuto, aleatorio, repetir y origen. KV-DATASTORE.
- **Resultado de búsqueda**: elemento (canción, álbum, artista o lista) con el tramo de su nombre que coincide. Se calcula; no se guarda.

## Criterios de éxito *(obligatorio)*

### Resultados medibles

- **SC-001**: En el 100 % de los cierres probados (deslizar, forzar cierre, recrear), la canción, el orden de la cola, aleatorio, repetir y el origen vuelven iguales y en pausa; el minuto, con una diferencia ≤ 5 s.
- **SC-002**: Al abrir la app nunca empieza a sonar algo sin que el usuario lo toque.
- **SC-003**: Con 2 000 canciones en la cola, reproducir, pausar y reordenar no muestran trabas visibles por guardar.
- **SC-004**: Desde abrir la app, volver a escuchar algo reciente toma 1 toque.
- **SC-005**: Con 2 000 canciones, los resultados de Buscar aparecen en menos de 300 ms tras dejar de escribir.
- **SC-006**: SCR-10, 11, 12, 40 y 41 coinciden con Figma en contenido y tokens, salvo lo que DEC-30 retira.
- **SC-007**: Tras actualizar desde la versión anterior, el 100 % de las favoritas y listas siguen ahí.

## Supuestos

- En la línea de la fecha Figma muestra «Viernes, 9:30 p. m.»; DEC-30 y la tarea piden la fecha: se muestra «Viernes, 20 de noviembre».
- El segundo mosaico de Figma es «Letras»; DEC-30 pide «Favoritas» hasta la tarea 09.
- La cola al tocar una canción en Inicio o Buscar no está en el grafo: se toma la de Biblioteca · Canciones (FR-004), que usa un origen que DEC-24 ya contempla.
- Buscar compara por nombre y no por el artista o álbum de cada canción: así se ve en SCR-40 («Marea Alta» sale como artista y sus canciones no) y el resaltado siempre es visible.
- Figma no dibuja el grupo Listas; usa la misma fila que Álbumes, con chevron y conteo.
- Al volver a Buscar con texto (desde un resultado o desde otra pestaña) no se abre el teclado, para ver los resultados.
- Una canción restaurada a la mitad puede volver a contar si suena 30 s más: es otra escucha.
- Retomar desde los audífonos o la notificación con la app cerrada queda fuera de esta función.
- Historial sin límite de tiempo; borrar el historial a mano queda fuera.
