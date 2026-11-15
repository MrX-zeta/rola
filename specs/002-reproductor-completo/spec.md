# Especificación: Reproductor completo

**Carpeta**: `specs/002-reproductor-completo`

**Creada**: 2026-11-14

**Estado**: Borrador · corregida tras la revisión de Brian (tarea 03b, grafo 1.6.0)

**Tarea**: `ai/handoff/siguiente.md` · Tarea 03 · Grafo 1.5.0

**Entrada**: «Como dueño de Rola quiero tocar o deslizar hacia arriba la mini tarjeta para abrir Reproduciendo, con la portada grande, título, artista, barra de avance que puedo arrastrar con sus tiempos, anterior, reproducir/pausa, siguiente, aleatorio y repetir; deslizar hacia abajo para volver. En la mini tarjeta, deslizar a la izquierda pasa a la siguiente y a la derecha a la anterior. Desde Reproduciendo abro la cola: veo lo que suena y lo que sigue, reordeno arrastrando y quito deslizando, con opción de deshacer. También pongo un temporizador (15, 30, 45 min, 1 h o al terminar la canción) que pausa la música. Reproduciendo, la cola y la mini tarjeta usan el color de la portada.»

**Referencias del grafo**: pantallas SCR-50 (Mini reproductor), SCR-51 (Reproduciendo), SCR-52 (Cola), SCR-53 (Temporizador, hoja); decisiones DEC-04, DEC-09, DEC-15, DEC-24, DEC-27, DEC-28, PEND-08 (resuelto, opción A); operaciones OP-02, OP-03, OP-04, OP-15; invariantes INV-05, INV-06, INV-11. Esta spec cita los ids; ante cualquier duda manda el nodo del grafo y, en lo visual, Figma.

## Aclaraciones

### Sesión 2026-11-14

Respondidas con el grafo y con lo «ya decidido» de la tarea 03; lo que ninguno responde se marca **supuesto** (opción más simple) y va al reporte.

- P: ¿Dónde existen gestos y cómo se protegen de toques accidentales? → R: solo dentro de la mini tarjeta (izquierda = siguiente, derecha = anterior, arriba = abrir) y en Reproduciendo (abajo = cerrar), con umbral de distancia y de velocidad, interrumpibles y con alternativa por botones; sin deslizar entre pestañas. (DEC-04 y tarea.)
- P: ¿Cuántos estados tiene repetir y qué es «aleatorio» en Reproduciendo? → R: repetir tiene tres estados (apagado, toda la cola, una canción); aleatorio es un modo de reproducción que se enciende y se apaga, distinto del botón «Aleatorio» de Biblioteca que baraja la lista. (Tarea.)
- P: ¿Dónde vive el temporizador, qué hace al cumplirse y se recuerda? → R: vive en el servicio de reproducción; al cumplirse pausa (no detiene ni cierra); el tiempo restante se ve en la hoja y en la descripción del botón, que queda marcado como activo (**supuesto** entre «ícono u hoja»: ambos, sin texto extra sobre el ícono); se cancela con «Apagado»; no se recuerda al cerrar la app. (OP-15, tarea.)
- P: ¿Qué pasa al quitar una canción de la cola y se recuerda la cola? → R: aviso con «Deshacer» que la devuelve a su lugar (DEC-09); la cola no se recuerda al cerrar la app (Room llega en la tarea 05). (Tarea.)
- P: ¿Qué elementos de Figma de Reproduciendo y Cola quedan fuera? → R: tocar la portada para ver la letra, la pista y el botón «Letra», el menú ⋮ y «Añadir a una lista» (tareas 05 y 08). «Limpiar» de la Cola no lo pide la tarea: queda fuera (**supuesto**, se pregunta en el reporte). La sombra de la portada de Figma no se dibuja porque DEC-27 no pone relieve en Reproduciendo (**supuesto**, manda el grafo; se pregunta en el reporte).

### Revisión 2026-11-14 · tarea 03b (Brian probó en el Redmi, grafo 1.6.0)

Cambios de DEC-04 y DEC-24 y respuestas a las dudas del reporte de la tarea 03. Reemplazan lo anterior donde choquen.

- P: ¿Lleva sombra la portada de Reproduciendo? → R: no, va **plana** (DEC-27).
- P: ¿Se implementa «Limpiar» en la Cola? → R: **sí**: quita todo lo que sigue, deja la canción actual y muestra un aviso con «Deshacer» (DEC-24).
- P: Sin portada en tema claro, ¿de qué color es el botón de reproducir? → R: **accent** (lima) con `onAccent`; la barra de avance y el título siguen en `primary` por contraste (DEC-16).
- P: ¿La mini tarjeta en claro usa la variante clara del color de la portada? → R: **confirmado**.
- P: ¿`timeSmall` y `repeat_one`? → R: `timeSmall` ya está en DEC-16; `repeat_one` lo agrega la sesión de diseño a Figma.
- P: ¿Cómo se mide el umbral del gesto horizontal? → R: por la **distancia recorrida desde donde empieza el dedo** (≈ 25 % del ancho o ~96 dp, lo que sea menor) o por la velocidad (~400 dp/s), empezando en cualquier punto de la tarjeta; no por la posición (DEC-04).
- P: ¿Qué botones lleva la mini tarjeta? → R: solo reproducir/pausa (sin siguiente); el título corre como marquesina solo si no cabe (DEC-04, Figma 5:51).
- P: ¿Qué hace tocar una fila de la cola? → R: reproduce esa canción (DEC-24).

## Escenarios de usuario y pruebas *(obligatorio)*

### Historia 1 · Abrir Reproduciendo y controlar la canción (Prioridad: P1)

Con una canción sonando, toco la mini tarjeta (o la deslizo hacia arriba) y se abre Reproduciendo (SCR-51): portada grande, título, artista, barra de avance con el tiempo transcurrido y el total, anterior, reproducir/pausa y siguiente. Arrastro la barra para saltar a otra parte de la canción. Deslizo hacia abajo, toco la flecha o uso Atrás del sistema y vuelvo a la mini tarjeta. La música nunca se corta por abrir o cerrar la pantalla.

**Por qué esta prioridad**: es la pantalla principal del reproductor; sin ella no hay acceso a la cola, los modos ni el temporizador.

**Prueba independiente**: reproducir una canción, abrir Reproduciendo tocando la tarjeta, arrastrar la barra a la mitad, pasar a la siguiente y a la anterior, cerrar deslizando hacia abajo y comprobar que la música siguió sonando todo el tiempo.

**Escenarios de aceptación**:

1. **Dado** que hay una canción cargada, **cuando** toco la mini tarjeta fuera de sus botones, **entonces** se abre Reproduciendo con la portada, el título, el artista y los tiempos de esa canción.
2. **Dado** que hay una canción cargada, **cuando** deslizo la mini tarjeta hacia arriba más allá del umbral (distancia o velocidad), **entonces** la tarjeta crece siguiendo mi dedo y, al soltar, termina de abrir Reproduciendo; si suelto antes del umbral, regresa a su lugar.
3. **Dado** que estoy en Reproduciendo, **cuando** deslizo hacia abajo más allá del umbral, toco la flecha de cerrar o uso Atrás, **entonces** vuelvo a la pantalla anterior con la mini tarjeta; si suelto antes del umbral, Reproduciendo se queda abierta. Al soltar, o si el gesto se cancela o lo interrumpe otro toque, Reproduciendo termina siempre cerrada o abierta, nunca a medias.
4. **Dado** que estoy en Reproduciendo, **cuando** arrastro el control de la barra, **entonces** el tiempo transcurrido muestra el punto al que voy y, al soltar, la canción salta a ese punto. Tocar la barra en un punto también salta a él.
5. **Dado** que estoy en Reproduciendo, **cuando** toco reproducir/pausa, siguiente o anterior, **entonces** la canción se pausa, se reanuda o cambia, y la pantalla muestra la nueva canción con su color.
6. **Dado** que una canción lleva más de 3 segundos sonando, **cuando** toco anterior, **entonces** vuelve a empezar; si lleva 3 segundos o menos, pasa a la canción anterior de la cola.
7. **Dado** que una canción suena, **cuando** abro o cierro Reproduciendo, **entonces** la música no se detiene (INV-11).
8. **Dado** que estoy en Reproduciendo, **cuando** deslizo la portada horizontalmente más allá del umbral de la mini tarjeta, **entonces** la portada se desplaza con el dedo y pasa a la siguiente (izquierda) o a la anterior (derecha); por debajo del umbral, vuelve a su lugar.
9. **Dado** que estoy en Reproduciendo en cualquier teléfono, **cuando** miro la pantalla, **entonces** la portada se adapta al alto disponible, los controles son grandes y la fila de acciones queda cerca del borde inferior, sin hueco abajo.

---

### Historia 2 · Aleatorio y repetir (Prioridad: P1)

En Reproduciendo enciendo el aleatorio para que la cola suene en otro orden y lo apago para volver al orden original. Con repetir elijo entre apagado, repetir toda la cola o repetir una canción.

**Por qué esta prioridad**: son controles básicos de cualquier reproductor y forman parte de DEC-24.

**Prueba independiente**: con una cola de varias canciones, encender aleatorio y comprobar que «siguiente» no sigue el orden de la lista; apagarlo y comprobar que vuelve al orden; recorrer los tres estados de repetir y comprobar el comportamiento al terminar la canción y la cola.

**Escenarios de aceptación**:

1. **Dado** que el aleatorio está apagado, **cuando** lo toco, **entonces** se enciende, su ícono se marca con el color activo y las siguientes canciones salen en orden aleatorio sin repetir ninguna hasta agotar la cola.
2. **Dado** que el aleatorio está encendido, **cuando** lo toco, **entonces** se apaga y la cola vuelve a su orden original desde la canción actual.
3. **Dado** que repetir está apagado, **cuando** lo toco tres veces, **entonces** pasa por «repetir toda la cola», «repetir una canción» y vuelve a «apagado»; cada estado tiene su propio ícono y su descripción accesible.
4. **Dado** que repetir está en «toda la cola», **cuando** termina la última canción, **entonces** vuelve a sonar la primera.
5. **Dado** que repetir está en «una canción», **cuando** termina la canción, **entonces** vuelve a empezar la misma; siguiente y anterior siguen cambiando de canción.
6. **Dado** que repetir está apagado, **cuando** termina la última canción de la cola, **entonces** la reproducción se detiene en pausa con esa canción.

---

### Historia 3 · Gestos en la mini tarjeta (Prioridad: P2)

Sin abrir Reproduciendo, deslizo la mini tarjeta a la izquierda para pasar a la siguiente canción y a la derecha para volver a la anterior.

**Por qué esta prioridad**: acelera el uso diario, pero existe la alternativa con botones (DEC-04).

**Prueba independiente**: con una cola de varias canciones, deslizar la tarjeta a la izquierda y a la derecha, y comprobar que cambia la canción; deslizar poco y lento, y comprobar que no cambia.

**Escenarios de aceptación**:

1. **Dado** que hay una canción siguiente, **cuando** deslizo la tarjeta a la izquierda más allá del umbral, empezando en cualquier punto de la tarjeta (también en el centro), **entonces** pasa a la siguiente canción (OP-03).
2. **Dado** que hay una canción cargada, **cuando** deslizo la tarjeta a la derecha más allá del umbral, **entonces** ocurre lo mismo que con el botón anterior (Historia 1, escenario 6).
3. **Dado** que deslizo la tarjeta, **cuando** suelto antes del umbral, **entonces** la tarjeta regresa a su lugar y la canción no cambia.
4. **Dado** que no hay canción siguiente, **cuando** deslizo a la izquierda, **entonces** la tarjeta regresa a su lugar y la canción no cambia.
5. **Dado** que estoy deslizando la tarjeta, **cuando** cambio de dirección o la suelto a medio camino, **entonces** el movimiento se interrumpe y sigue desde donde va, sin saltos (DEC-28).
6. **Dado** que no uso gestos, **cuando** quiero las mismas acciones, **entonces** tengo los botones: anterior, siguiente y cerrar en Reproduciendo, y tocar la tarjeta para abrirla. La tarjeta solo lleva reproducir/pausa.
7. **Dado** que el título no cabe en la tarjeta, **cuando** la veo, **entonces** corre como marquesina; si cabe o está activo «reducir movimiento», queda estático con puntos suspensivos.

---

### Historia 4 · Ver y ordenar la cola (Prioridad: P2)

Desde Reproduciendo abro la cola (SCR-52). Arriba veo la canción que suena; abajo, las que siguen. Arrastro una canción por su asa para cambiarla de lugar, y deslizo una fila para quitarla; un aviso me deja deshacerlo.

**Por qué esta prioridad**: da control sobre lo que suena después; depende de Reproduciendo.

**Prueba independiente**: abrir la cola, mover la tercera canción al primer lugar de «A continuación», quitar otra deslizando, tocar «Deshacer» y comprobar que vuelve a su lugar; pasar a la siguiente y comprobar que suena la que se movió.

**Escenarios de aceptación**:

1. **Dado** que estoy en Reproduciendo, **cuando** toco el botón de cola, **entonces** se abre Cola con la sección «Sonando ahora» (la canción actual, marcada con el color de la portada) y «A continuación · N» con las canciones que siguen, en el orden en que van a sonar.
2. **Dado** que estoy en Cola, **cuando** arrastro una canción de «A continuación» por su asa y la suelto en otro lugar, **entonces** queda en ese lugar y la reproducción respeta el nuevo orden.
3. **Dado** que estoy en Cola, **cuando** deslizo una fila de «A continuación» hacia la izquierda más allá del umbral, **entonces** se quita de la cola y aparece un aviso «Quitaste «título» de la cola» con «Deshacer» (DEC-09).
4. **Dado** que acabo de quitar una canción, **cuando** toco «Deshacer» mientras el aviso está visible, **entonces** la canción vuelve a su lugar anterior.
5. **Dado** que estoy en Cola, **cuando** toco la flecha o uso Atrás, **entonces** vuelvo a Reproduciendo.
6. **Dado** que uso un lector de pantalla, **cuando** me enfoco en una fila de «A continuación», **entonces** tengo las acciones «Subir», «Bajar» y «Quitar de la cola» sin necesidad de arrastrar ni deslizar (INV-06).
7. **Dado** que la canción actual es la última, **cuando** abro Cola, **entonces** «A continuación · 0» muestra un texto que dice que no hay más canciones.
8. **Dado** que estoy en Cola, **cuando** toco una fila de «A continuación», **entonces** esa canción empieza a sonar.
9. **Dado** que arrastro una fila cerca del borde superior o inferior de la lista, **cuando** la mantengo ahí, **entonces** la lista se desplaza sola en esa dirección; al llegar al tope, la fila queda en la posición válida más cercana y nunca se pierde ni tapa a otra.
10. **Dado** que hay canciones en «A continuación», **cuando** toco «Limpiar», **entonces** se quitan todas las que siguen, queda la actual y aparece un aviso con «Deshacer» que las devuelve en su orden.

---

### Historia 5 · Temporizador de apagado (Prioridad: P3)

Antes de dormir abro el temporizador desde Reproduciendo (SCR-53), elijo 30 minutos y la música se pausa sola cuando se cumple. Puedo ver cuánto falta y cancelarlo.

**Por qué esta prioridad**: útil pero independiente del resto (OP-15).

**Prueba independiente**: elegir un tiempo, salir de la app, esperar y comprobar que la música se pausa sola sin cerrarse; volver a abrir la hoja, ver el tiempo restante y cancelarlo.

**Escenarios de aceptación**:

1. **Dado** que estoy en Reproduciendo, **cuando** toco el botón del temporizador, **entonces** se abre una hoja con «Apagado», «15 minutos», «30 minutos», «45 minutos», «1 hora» y «Al terminar la canción», con la opción vigente marcada, y un botón «Iniciar temporizador».
2. **Dado** que la hoja está abierta, **cuando** elijo una duración y toco «Iniciar temporizador», **entonces** la hoja se cierra, aparece un aviso que confirma cuándo se pausará y el botón del temporizador queda marcado como activo.
3. **Dado** que hay un temporizador activo, **cuando** se cumple el tiempo, **entonces** la música se pausa (no se detiene ni se cierra la app ni la notificación) y el temporizador vuelve a «Apagado».
4. **Dado** que elegí «Al terminar la canción», **cuando** termina la canción que suena, **entonces** la música se pausa en lugar de seguir con la siguiente.
5. **Dado** que hay un temporizador activo, **cuando** abro la hoja, **entonces** veo el tiempo restante (o «al terminar la canción»); al elegir «Apagado» e iniciar, se cancela y un aviso lo confirma.
6. **Dado** que hay un temporizador activo, **cuando** salgo de la app o apago la pantalla, **entonces** el temporizador sigue contando y pausa la música a su hora.

---

### Historia 6 · El reproductor toma el color de la portada (Prioridad: P3)

La mini tarjeta, Reproduciendo y la cola se tiñen con el color de la portada de la canción actual (PEND-08 A); el resto de la app sigue con el lima.

**Por qué esta prioridad**: identidad visual; la función sirve igual sin ella.

**Prueba independiente**: reproducir canciones con portadas de colores distintos y sin portada, y comprobar el color de la tarjeta, de Reproduciendo y de la fila actual de la cola, y el contraste del texto.

**Escenarios de aceptación**:

1. **Dado** que la canción tiene portada, **cuando** abro Reproduciendo, **entonces** el fondo superior toma un tono de la portada que se funde con el fondo del tema, y la barra de avance, el botón de reproducir y los modos activos usan el acento sacado de la portada.
2. **Dado** que la canción no tiene portada o no se puede sacar color, **cuando** abro Reproduciendo o Cola, **entonces** se usan los colores del tema (acento lima).
3. **Dado** que estoy en Cola, **cuando** miro «Sonando ahora», **entonces** la fila tiene el fondo y el título con el color de la portada.
4. **Dado** cualquier portada, **cuando** se muestran texto e íconos sobre su color, **entonces** el contraste es de al menos 4.5:1 para texto y 3:1 para componentes (INV-06).
5. **Dado** que cambia la canción, **cuando** llega el nuevo color, **entonces** el cambio es un fundido corto, no un salto.

---

### Casos límite

- Cola de una sola canción: siguiente deshabilitado salvo con repetir «toda la cola»; deslizar a la izquierda regresa la tarjeta.
- Repetir «toda la cola» en la última canción: siguiente pasa a la primera.
- Se quita una canción y luego se quita otra antes de tocar «Deshacer»: el aviso nuevo reemplaza al anterior y «Deshacer» recupera solo la última.
- La canción actual no se puede quitar ni mover desde la cola.
- Se arrastra la barra de avance y la canción termina o cambia antes de soltar: al soltar se busca en la canción que suena entonces, dentro de su duración.
- Canción sin duración conocida: la barra no se puede arrastrar y el total se muestra como «0:00».
- Temporizador «Al terminar la canción» y el usuario cambia de canción a mano: la pausa ocurre al terminar la canción que esté sonando cuando termine una.
- Temporizador activo y el usuario pausa a mano: el tiempo sigue corriendo; si se cumple en pausa, solo vuelve a «Apagado».
- Una canción ilegible durante el aleatorio o la repetición: se salta como en la tarea 02 (FR-018 de 001).
- Gesto vertical en la tarjeta que empieza en diagonal: gana la dirección dominante; no se cambia de canción y se abre a la vez.
- Rotar el teléfono o cambiar de tema con Reproduciendo, Cola o la hoja abiertos: la pantalla abierta se conserva.
- Toques en la mini tarjeta o en la biblioteca justo después de un arranque en frío, antes de que se conecte el reproductor: no se pierden; se guardan y se envían al conectar.
- Reproduciendo abierta cuando la cola se vacía (por ejemplo, la canción deja de existir): se cierra y vuelve a la pantalla anterior sin mini tarjeta.

## Requisitos *(obligatorio)*

### Requisitos funcionales

**Abrir y cerrar Reproduciendo (SCR-50, SCR-51, DEC-04, DEC-28)**

- **FR-001**: Tocar la mini tarjeta fuera de sus botones DEBE abrir Reproduciendo.
- **FR-002**: Deslizar la mini tarjeta hacia arriba DEBE abrir Reproduciendo con una transición que sigue al dedo: la tarjeta crece hasta ocupar la pantalla. Al soltar, si se pasó el umbral de distancia o de velocidad, termina de abrir; si no, regresa.
- **FR-003**: Reproduciendo DEBE cerrarse deslizando hacia abajo (con el mismo criterio de umbral y seguimiento del dedo), con la flecha de la barra superior y con Atrás del sistema. Al soltar, y también si el gesto se cancela o lo interrumpe otro toque, DEBE terminar siempre en cerrado o abierto; nunca queda a medias sobre la pantalla de abajo.
- **FR-004**: Las transiciones de abrir y cerrar DEBEN ser interrumpibles: si el usuario vuelve a tocar a medio camino, el movimiento sigue desde donde va.
- **FR-005**: Con «reducir movimiento» activo, abrir y cerrar Reproduciendo DEBE ser un fundido corto, sin desplazamiento ni cambio de tamaño, y la tarjeta no se mueve con el dedo (DEC-28, INV-06).
- **FR-006**: Abrir, cerrar o interrumpir cualquier transición del reproductor NO DEBE pausar ni detener la música (INV-11).

**Reproduciendo (SCR-51, DEC-24)**

- **FR-007**: Reproduciendo DEBE mostrar: barra superior con flecha de cerrar y el origen «Reproduciendo desde · Biblioteca»; portada grande; título y artista; barra de avance con tiempo transcurrido y total; aleatorio, anterior, reproducir/pausa, siguiente y repetir; y una fila de acciones con temporizador y cola. El contenido DEBE repartirse en toda la altura: la portada se adapta al alto disponible, reproducir mide ≥ 80 dp, anterior y siguiente ≥ 56 dp (Figma), y la fila de acciones queda cerca del borde inferior respetando la barra del sistema. La portada DEBE ser plana (DEC-27) y pedirse al tamaño en que se dibuja, no a un tamaño fijo, para que no se vea borrosa.
- **FR-008**: NO DEBEN mostrarse en esta tarea: la pista «Letra» sobre la portada, el botón de letra, el menú ⋮ ni «Añadir a una lista». Tocar la portada no hace nada todavía.
- **FR-009**: Los tiempos DEBEN usar la tipografía de tiempos (Geist Mono) y el formato «m:ss» (o «h:mm:ss» desde una hora).
- **FR-010**: La barra de avance DEBE poder arrastrarse y tocarse: mientras se arrastra, el tiempo transcurrido muestra el destino; al soltar, la canción salta a ese punto.
- **FR-011**: La barra de avance y la línea de la mini tarjeta DEBEN avanzar de forma continua, sin saltos visibles de un segundo a otro.
- **FR-012**: Anterior DEBE reiniciar la canción si lleva más de 3 segundos; si no, pasar a la canción anterior de la cola (si no hay anterior, reiniciar).
- **FR-013**: Siguiente DEBE pasar a la canción siguiente; si no hay (y repetir no está en «toda la cola»), el botón queda deshabilitado.
- **FR-014**: Aleatorio DEBE ser un modo que se enciende y se apaga; encendido, las canciones siguientes salen en orden aleatorio sin repetir hasta agotar la cola; apagado, vuelve el orden original desde la canción actual. No se confunde con el botón «Aleatorio» de Biblioteca, que baraja la lista al empezar.
- **FR-015**: Repetir DEBE tener tres estados que se recorren al tocar: apagado → toda la cola → una canción → apagado.
- **FR-016**: Los estados de aleatorio y repetir DEBEN verse (ícono marcado con el color activo; ícono propio para «una canción») y anunciarse al lector de pantalla.
- **FR-017**: Los modos de aleatorio y repetir DEBEN aplicarse también a la reproducción controlada desde la notificación.

**Gestos de la mini tarjeta (SCR-50, DEC-04, OP-03)**

- **FR-018**: Deslizar la mini tarjeta a la izquierda más allá del umbral DEBE pasar a la siguiente canción; a la derecha, hacer lo mismo que el botón anterior (FR-012). El umbral cuenta la distancia recorrida desde donde empieza el dedo (≈ 25 % del ancho de la tarjeta o ~96 dp, lo que sea menor) o la velocidad al soltar (~400 dp/s), y el gesto DEBE funcionar empezando en cualquier punto de la tarjeta. Los valores calibrados viven en `RolaMotion` o junto al gesto.
- **FR-019**: Por debajo del umbral, o sin canción siguiente al deslizar a la izquierda, la tarjeta DEBE regresar a su lugar sin cambiar la canción.
- **FR-020**: Los gestos DEBEN existir solo dentro de la mini tarjeta y de Reproduciendo (cerrar hacia abajo; siguiente/anterior sobre la portada); no hay deslizar entre pestañas.
- **FR-021**: El gesto horizontal y el vertical NO DEBEN dispararse a la vez: gana la dirección dominante.
- **FR-049**: En Reproduciendo, deslizar la portada horizontalmente DEBE pasar a la siguiente (izquierda) o a la anterior (derecha) con el mismo umbral que la mini tarjeta (FR-018). La portada DEBE desplazarse con el dedo y, al soltar, volver a su lugar o cambiar. Con «reducir movimiento», la portada no sigue al dedo, pero el gesto sigue cambiando de canción.
- **FR-050**: La mini tarjeta DEBE llevar solo el botón reproducir/pausa (sin siguiente), como el componente de Figma 5:51.
- **FR-051**: El título de la mini tarjeta DEBE correr como marquesina solo si no cabe; si cabe o está activo «reducir movimiento», queda estático con puntos suspensivos.

**Cola (SCR-52, DEC-24, DEC-09, OP-04)**

- **FR-022**: Cola DEBE abrirse desde Reproduciendo y mostrar «Sonando ahora» con la canción actual y «A continuación · N» con las canciones que siguen, en el orden real de reproducción (con aleatorio, el orden aleatorio).
- **FR-023**: Las canciones de «A continuación» DEBEN poder reordenarse arrastrando su asa; la reproducción DEBE respetar el nuevo orden.
- **FR-024**: Las canciones de «A continuación» DEBEN poder quitarse deslizando la fila hacia la izquierda; al quitar, el sistema DEBE mostrar el aviso «Quitaste «título» de la cola» con «Deshacer», que la regresa a su lugar.
- **FR-025**: La canción actual NO DEBE poder quitarse ni moverse desde la cola.
- **FR-026**: Cada fila de «A continuación» DEBE ofrecer al lector de pantalla las acciones «Subir», «Bajar» y «Quitar de la cola».
- **FR-027**: Cola DEBE cerrarse con la flecha y con Atrás, volviendo a Reproduciendo.
- **FR-028**: Sin canciones siguientes, Cola DEBE mostrar un texto que lo explica.
- **FR-029**: La cola NO se recuerda al cerrar la app (Room llega en la tarea 05).
- **FR-052**: Tocar una fila de «A continuación» DEBE reproducir esa canción.
- **FR-053**: Al arrastrar una fila cerca del borde superior o inferior de la lista, la lista DEBE desplazarse sola en esa dirección. Una fila arrastrada NUNCA debe perderse ni ocupar el lugar de otra al llegar al tope: se coloca en la posición válida más cercana.
- **FR-056**: Mientras se arrastra una fila de la cola, la fila DEBE quedarse siempre bajo el dedo, también al intercambiarse con la primera fila visible (DEC-24).
- **FR-057**: El desplazamiento automático al arrastrar DEBE detenerse al llegar al inicio o al final de la lista; la fila arrastrada NUNCA sale de los límites de la lista ni pasa del primer o el último lugar (DEC-24).
- **FR-054**: «Limpiar» en la barra de Cola DEBE quitar todas las canciones de «A continuación» y dejar la actual, con un aviso y «Deshacer» que las devuelve en su orden. Sin canciones siguientes, «Limpiar» queda deshabilitado.

**Temporizador (SCR-53, OP-15)**

- **FR-030**: El botón de temporizador de Reproduciendo DEBE abrir una hoja inferior con las opciones Apagado, 15 minutos, 30 minutos, 45 minutos, 1 hora y Al terminar la canción, y el botón «Iniciar temporizador».
- **FR-031**: Al iniciar, el temporizador DEBE contar en el servicio de reproducción, independiente de la interfaz: sigue contando con la app en segundo plano o la pantalla apagada.
- **FR-032**: Al cumplirse, el temporizador DEBE pausar la música (no detenerla ni cerrar la notificación) y volver a «Apagado».
- **FR-033**: «Al terminar la canción» DEBE pausar la música cuando termina la canción que suena, en lugar de pasar a la siguiente.
- **FR-034**: Con un temporizador activo, el botón del temporizador DEBE verse activo y su descripción accesible incluir el tiempo restante; la hoja DEBE mostrar el tiempo restante y la opción vigente marcada.
- **FR-035**: Elegir «Apagado» e iniciar DEBE cancelar el temporizador.
- **FR-036**: Iniciar y cancelar el temporizador DEBEN confirmarse con un aviso breve (INV-05).
- **FR-037**: El temporizador NO se recuerda al cerrar la app (DataStore llega en la tarea 07).

**Color de la portada (DEC-15, PEND-08 A)**

- **FR-038**: La mini tarjeta, Reproduciendo y la fila actual de Cola DEBEN usar el color sacado de la portada de la canción actual; sin portada o sin color útil, los colores del tema.
- **FR-039**: En Reproduciendo, el fondo superior DEBE ser un degradado del color de la portada hacia el fondo del tema; la barra de avance, el botón de reproducir y los modos activos usan el acento de la portada. Sin portada en tema claro, el botón de reproducir usa `accent` (lima) con `onAccent`, y la barra y el título siguen en `primary` por contraste (DEC-16). La mini tarjeta en claro usa la variante clara del color de la portada.
- **FR-040**: La hoja del temporizador y el aviso de Deshacer DEBEN usar los colores del tema, no los de la portada, como en Figma.
- **FR-041**: Todo texto sobre colores de la portada DEBE tener contraste ≥ 4.5:1, y los componentes ≥ 3:1 (INV-06).
- **FR-042**: El cambio de color entre canciones DEBE ser un fundido corto.

**Transversales**

- **FR-043**: Esta función NO DEBE usar red (INV-01).
- **FR-044**: Colores, tipografía, formas y espacios DEBEN salir de los tokens del tema (INV-04). Reproduciendo, Cola y la hoja son planas (DEC-27): sin sombras ni relieve.
- **FR-045**: Todo botón de solo ícono DEBE tener descripción accesible y área táctil ≥ 48 dp; los textos respetan el tamaño de letra del sistema (INV-06).
- **FR-046**: Toda animación nueva DEBE durar ≤ 300 ms (las salidas, menos que las entradas), usar resortes interrumpibles en lo que se arrastra y empezar a contar cuando su contenido ya está dibujado (DEC-28 `arranque_en_primer_cuadro`).
- **FR-047**: Reproduciendo, Cola y la hoja DEBEN verse bien en tema oscuro y claro según sus nodos de Figma.
- **FR-048**: Todos los textos de la interfaz DEBEN estar en español.
- **FR-055**: Las órdenes que la interfaz envía antes de que el controlador de reproducción esté conectado (por ejemplo, el primer toque tras un arranque en frío) NO DEBEN perderse: se guardan y se envían en orden al conectar.

### Entidades clave

- **Cola**: lista ordenada de canciones con la posición actual; la gestiona el servicio de reproducción. Operaciones: mover, quitar, reinsertar (deshacer).
- **Modo aleatorio**: encendido o apagado; define el orden real de reproducción sin cambiar el orden original.
- **Modo repetir**: apagado, toda la cola o una canción.
- **Temporizador**: apagado, una hora de término (15, 30, 45 o 60 minutos desde que se inicia) o «al terminar la canción»; vive en el servicio de reproducción.
- **Estado de reproducción**: canción actual, si suena, posición, duración, si hay siguiente o anterior, modos y temporizador.
- **Colores de la portada**: fondo, acento y textos derivados de la portada actual (de la tarea 02), o del tema.

## Criterios de éxito *(obligatorio)*

### Resultados medibles

- **SC-001**: Reproduciendo se abre desde la mini tarjeta con un toque o un deslizamiento, y se cierra con un deslizamiento, un toque o Atrás, en el 100 % de los intentos.
- **SC-002**: Abrir y cerrar Reproduciendo, Cola o la hoja nunca interrumpe la música (0 cortes en 20 aperturas y cierres seguidos).
- **SC-003**: Un deslizamiento corto y lento (por debajo del umbral) en la mini tarjeta no cambia la canción ni abre Reproduciendo en el 100 % de los intentos.
- **SC-004**: Tras arrastrar la barra de avance y soltar, la canción suena desde el punto elegido con un error menor a 1 segundo.
- **SC-005**: Mover, quitar y deshacer en la cola se reflejan en lo que suena después en el 100 % de los casos.
- **SC-006**: Con un temporizador de 1 minuto y la pantalla apagada, la música se pausa entre 60 y 62 segundos después de iniciarlo.
- **SC-007**: En todas las portadas probadas, el texto sobre colores de la portada mantiene contraste ≥ 4.5:1.
- **SC-008**: Con «reducir movimiento» activo, ninguna transición del reproductor desplaza ni escala contenido.
- **SC-009**: Las transiciones de abrir y cerrar y la barra de avance se ven fluidas, sin cuadros perdidos visibles, en una compilación no depurable.
- **SC-010**: La app no realiza ninguna conexión de red durante estos flujos.
- **SC-011**: Un deslizamiento horizontal que empieza en el centro de la mini tarjeta y recorre más que el umbral cambia la canción en el 100 % de los intentos.
- **SC-012**: Tras soltar, cancelar o interrumpir el gesto de cerrar, Reproduciendo queda cerrada o abierta en el 100 % de los intentos (nunca a medias).

## Supuestos

- El origen de Reproduciendo siempre es «Biblioteca», porque es la única fuente de canciones que existe todavía.
- Repetir empieza apagado y aleatorio apagado cada vez que se abre la app (sin persistencia en esta tarea).
- Anterior reinicia la canción si lleva más de 3 segundos (comportamiento habitual de los reproductores).
- La cola solo muestra la canción actual y las siguientes; las ya escuchadas siguen en la cola para «anterior», pero no se listan.
- El temporizador por tiempo sigue corriendo aunque el usuario pause a mano.
- La hoja del temporizador y el aviso de Deshacer usan los colores del tema, como en Figma.
- La portada de Reproduciendo es plana (DEC-27 no incluye Reproduciendo entre las pantallas con relieve, aunque el Figma muestra sombra en la portada).
- Fuera de esta tarea: letras (tocar la portada, pista y botón «Letra»), menú ⋮, «Añadir a una lista», listas, Room, DataStore, Inicio, Buscar, Ajustes y widget.
