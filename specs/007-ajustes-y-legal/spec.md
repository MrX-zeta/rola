# Especificación: Ajustes y legal

**Carpeta**: `specs/007-ajustes-y-legal`

**Creada**: 2026-11-26

**Estado**: Borrador

**Tarea**: `ai/handoff/siguiente.md` · Tarea 08 · Grafo 1.12.0

**Entrada**: «En Ajustes cambio el tema (sistema, oscuro o claro) y se aplica al instante; programo el temporizador de apagado y veo cuánto falta; escaneo mis medios y veo cuándo fue la última vez; leo los términos, el aviso de privacidad y las licencias; y veo la versión. Llego desde el engrane de Inicio o desde «Abrir Ajustes» cuando no hay canciones.»

**Referencias del grafo**: pantallas SCR-70 (Ajustes), SCR-72 (Términos), SCR-73 (Privacidad), SCR-74 (Licencias), SCR-12 (Sin canciones) y la hoja del Temporizador (SCR-53), cada una con `figma_nodo` y `figma_nodo_claro`; decisiones DEC-23 (legal), DEC-26 (escaneo), DEC-18 (sin permiso de notificaciones), DEC-09 (retroalimentación), DEC-16 (tema claro), DEC-27 (plano), DEC-28 (movimiento), DEC-30 (no se muestra lo que aún no existe), DEC-12 (LRCLIB, fuera de esta función); almacén KV-DATASTORE; operación OP-16; pendientes PEND-07 (no entra) y PEND-09 (resuelto: sin copia de seguridad); invariantes INV-01, INV-04, INV-05, INV-06, INV-09. Ante cualquier duda manda el nodo del grafo y, en lo visual, Figma.

## Aclaraciones

### Sesión 2026-11-26

Respondidas por la tarea 08 (Paso 2), salvo la última, que respondió Brian.

- P: ¿Qué temas hay y cuál va por defecto? → R: Sistema, Oscuro y Claro; por defecto Oscuro. Hoja de selección con el estilo de la hoja de ordenar; se aplica sin reiniciar, incluida la barra de estado; el ícono y la pantalla de arranque siguen al sistema (FR-010 a FR-015).
- P: ¿Qué hace la fila del temporizador? → R: abre la misma hoja del reproductor y muestra «Apagado» o el tiempo restante; no se guarda al cerrar la app (FR-020 a FR-022).
- P: ¿Qué hace «Escanear medios»? → R: lo mismo que «Escanear de nuevo» de SCR-12 (OP-16), con progreso y resultado; guarda la fecha y muestra «Último escaneo: hace …» (un momento, minutos, horas, días) (FR-040 a FR-043).
- P: ¿De dónde sale la versión? → R: de la compilación, no escrita a mano (FR-053).
- P: ¿Cómo son las pantallas legales? → R: textos en los recursos de la app, con desplazamiento, flecha atrás y encabezado con rol; Licencias suma Coil, Palette y Reorderable (Apache 2.0) (FR-050 a FR-052).
- P: ¿Se permite la copia de seguridad de Android? → R: no (PEND-09) (FR-060).
- P: ¿Qué dice «Escanear medios» si nunca se ha escaneado desde Rola? → R: «Aún no has escaneado»; tras el primer escaneo pasa a «Último escaneo: hace …» (FR-044).

## Escenarios de usuario y pruebas *(obligatorio)*

### Historia 1 · Elegir el tema (Prioridad: P1)

Quiero usar Rola en claro, en oscuro o como diga mi teléfono, y que el cambio se vea al momento sin reiniciar nada.

**Por qué esta prioridad**: el tema claro está diseñado desde hace varias tareas y hoy no se puede ver en el teléfono.

**Prueba independiente**: abrir Ajustes, elegir «Claro», ver toda la app en claro; cerrar la app, volver a abrirla y verla en claro desde el primer cuadro.

**Escenarios de aceptación**:

1. **Dado** que nunca cambié el tema, **cuando** abro la app, **entonces** se ve en oscuro y la fila Tema dice «Oscuro».
2. **Dado** Ajustes, **cuando** toco Tema, **entonces** se abre una hoja con «Sistema», «Oscuro» y «Claro», con la opción actual marcada.
3. **Dado** la hoja, **cuando** elijo «Claro», **entonces** la hoja se cierra y toda la app, incluida la barra de estado, pasa a claro con un fundido corto, sin saltos ni reinicio.
4. **Dado** que elegí «Sistema», **cuando** cambio el modo oscuro del teléfono, **entonces** Rola lo sigue.
5. **Dado** que elegí un tema, **cuando** cierro y abro la app, **entonces** abre ya en ese tema, sin mostrar antes el otro.

---

### Historia 2 · Llegar a Ajustes y volver (Prioridad: P1)

Quiero entrar a Ajustes desde Inicio o desde el aviso de que no hay música, y regresar a donde estaba.

**Por qué esta prioridad**: sin un camino, ninguna otra historia de esta función se puede usar.

**Prueba independiente**: tocar el engrane de Inicio, ver Ajustes y volver con «atrás»; sin canciones, tocar «Abrir Ajustes» y volver.

**Escenarios de aceptación**:

1. **Dado** Inicio, **cuando** toco el engrane, **entonces** se abre Ajustes y la píldora de la barra sigue en Inicio.
2. **Dado** Ajustes abierto desde Inicio, **cuando** toco «atrás» (flecha o gesto), **entonces** regreso a Inicio como lo dejé.
3. **Dado** que no hay canciones en el teléfono, **cuando** toco «Abrir Ajustes», **entonces** se abre Ajustes y «atrás» regresa a la pantalla sin canciones.
4. **Dado** Ajustes, **entonces** veo solo REPRODUCCIÓN (Temporizador de apagado), BIBLIOTECA (Escanear medios), APARIENCIA (Tema) y ACERCA DE (Términos, Privacidad, Licencias, Versión); no hay filas de cosas que aún no existen.

---

### Historia 3 · Escanear mis medios (Prioridad: P2)

Cuando copio música nueva quiero pedir que Rola la busque y saber cuándo fue la última vez que lo hizo.

**Por qué esta prioridad**: hoy solo se puede escanear cuando la biblioteca está vacía.

**Prueba independiente**: copiar una canción al teléfono, tocar «Escanear medios», ver el progreso, el resultado y «Último escaneo: hace un momento».

**Escenarios de aceptación**:

1. **Dado** Ajustes, **cuando** toco «Escanear medios», **entonces** la fila muestra que está escaneando y no acepta otro toque hasta terminar.
2. **Dado** que terminó, **entonces** un aviso dice cuántas canciones hay (o que no se encontró música) y la fila dice «Último escaneo: hace un momento».
3. **Dado** que el último escaneo fue hace 2 días, **cuando** abro Ajustes, **entonces** dice «Último escaneo: hace 2 días».
4. **Dado** que escaneo desde «Escanear de nuevo» en la pantalla sin canciones, **cuando** luego abro Ajustes, **entonces** «Último escaneo» refleja ese escaneo.

---

### Historia 4 · Programar el temporizador desde Ajustes (Prioridad: P2)

Quiero programar que la música se pause sola desde Ajustes y ver cuánto falta.

**Por qué esta prioridad**: ya existe en el reproductor; aquí solo se suma otro camino y la vista del tiempo restante.

**Prueba independiente**: tocar «Temporizador de apagado», elegir 15 min, ver el tiempo restante en la fila y comprobar que el reproductor muestra lo mismo.

**Escenarios de aceptación**:

1. **Dado** que no hay temporizador, **entonces** la fila dice «Apagado».
2. **Dado** Ajustes, **cuando** toco la fila, **entonces** se abre la misma hoja que en el reproductor, con la opción actual marcada.
3. **Dado** que elijo 15 min, **entonces** la fila muestra el tiempo que falta y se actualiza mientras la veo.
4. **Dado** que programé el temporizador en el reproductor, **cuando** abro Ajustes, **entonces** la fila muestra ese mismo temporizador.
5. **Dado** que cierro la app por completo, **cuando** la abro de nuevo, **entonces** el temporizador está apagado.

---

### Historia 5 · Leer lo legal y ver la versión (Prioridad: P2)

Quiero leer los términos, el aviso de privacidad y las licencias, y saber qué versión tengo.

**Por qué esta prioridad**: lo pide DEC-23 y da confianza, pero no cambia el uso diario.

**Prueba independiente**: abrir cada documento, desplazarlo hasta el final, volver; mirar la versión.

**Escenarios de aceptación**:

1. **Dado** Ajustes, **cuando** toco «Términos y condiciones», «Aviso de privacidad» o «Licencias», **entonces** se abre el documento completo con su título y una flecha para volver.
2. **Dado** un documento largo, **entonces** puedo desplazarlo hasta el final, también con letra grande.
3. **Dado** un documento, **cuando** toco la flecha o hago el gesto de atrás, **entonces** regreso a Ajustes.
4. **Dado** Licencias, **entonces** aparecen las licencias de Figma más Coil, Palette y Reorderable (Apache 2.0).
5. **Dado** Ajustes, **entonces** «Versión» muestra la versión real de la app instalada.

---

### Casos límite

- Nunca se ha escaneado desde Rola: la fila dice «Aún no has escaneado».
- Reloj del teléfono atrasado (último escaneo «en el futuro»): se muestra «hace un momento», nunca un número negativo.
- Tocar «Escanear medios» dos veces seguidas: solo corre un escaneo.
- Salir de Ajustes mientras escanea: el escaneo termina, guarda la fecha y su aviso aparece donde esté.
- Sin permiso de música: escanear termina con su aviso (no se encontró música) sin cerrarse.
- Temporizador «Al terminar la canción»: la fila lo dice con esas palabras en lugar de un tiempo.
- El temporizador vence con Ajustes abierto: la fila vuelve a «Apagado».
- Tema «Sistema» y el teléfono cambia de modo con la app abierta: el cambio se ve sin reiniciar.
- Ajustes ilegibles o disco lleno: la app abre con los valores por defecto (oscuro, sin fecha de escaneo) y no se cierra; elegir un valor se aplica en pantalla aunque no se recuerde.
- «Reducir movimiento» activado: el cambio de tema es instantáneo o un fundido corto, sin movimiento.
- Letra del sistema grande: Ajustes y los documentos se desplazan; ningún texto se corta sin puntos suspensivos.
- Restaurar la app en otro teléfono desde una copia de seguridad: no ocurre; Rola no participa en la copia de Android (PEND-09).

## Requisitos *(obligatorio)*

### Requisitos funcionales

**Navegación (DEC-30, SCR-12)**

- **FR-001**: Inicio DEBE mostrar el engrane de Ajustes; tocarlo abre Ajustes como hija de Inicio: la píldora sigue en Inicio y «atrás» regresa a Inicio.
- **FR-002**: La pantalla sin canciones (SCR-12) DEBE mostrar «Abrir Ajustes», que abre Ajustes; «atrás» regresa a esa pantalla.
- **FR-003**: El texto de SCR-12 DEBE ser verdadero con lo que ofrece Ajustes (escanear medios; no hay selector de carpetas).
- **FR-004**: Términos, Privacidad y Licencias DEBEN ser hijas de Ajustes; «atrás» regresa a Ajustes.

**Contenido de Ajustes (SCR-70, DEC-30)**

- **FR-005**: Ajustes DEBE mostrar, en este orden, REPRODUCCIÓN (Temporizador de apagado), BIBLIOTECA (Escanear medios con «Último escaneo»), APARIENCIA (Tema) y ACERCA DE (Términos y condiciones, Aviso de privacidad, Licencias, Versión).
- **FR-006**: Ajustes NO DEBE mostrar la sección LETRAS, «Carpeta de letras», «Agregar widget» ni la duración mínima de escaneo (PEND-07).

**Tema (DEC-16, KV-DATASTORE)**

- **FR-010**: El tema DEBE poder ser Sistema, Oscuro o Claro; por defecto, Oscuro.
- **FR-011**: La fila Tema DEBE mostrar el valor actual y abrir una hoja de selección con las tres opciones, con el mismo estilo que la hoja de ordenar.
- **FR-012**: El tema elegido DEBE aplicarse al instante en toda la app, incluida la barra de estado y la de navegación del sistema, sin reiniciar.
- **FR-013**: El tema DEBE recordarse al cerrar la app y aplicarse desde el primer cuadro al abrirla, sin mostrar antes otro tema.
- **FR-014**: Con «Sistema», la app DEBE seguir el modo oscuro del teléfono, también si cambia con la app abierta.
- **FR-015**: El ícono de la app y la pantalla de arranque del sistema siguen al sistema (no al tema elegido).

**Temporizador de apagado (OP-15, SCR-53)**

- **FR-020**: La fila DEBE abrir la misma hoja del temporizador que el reproductor y compartir su estado: lo programado en un lugar se ve en el otro.
- **FR-021**: La fila DEBE mostrar «Apagado», el tiempo que falta (actualizado mientras se ve) o «Al terminar la canción».
- **FR-022**: El temporizador NO DEBE guardarse al cerrar la app (KV-DATASTORE).

**Escanear medios (OP-16, DEC-26)**

- **FR-040**: «Escanear medios» DEBE hacer lo mismo que «Escanear de nuevo» de SCR-12: volver a consultar la biblioteca del sistema y pedirle escanear.
- **FR-041**: Durante el escaneo la fila DEBE mostrar progreso y no aceptar otro toque; al terminar, un aviso DEBE decir el resultado (INV-05).
- **FR-042**: Al terminar cualquier escaneo (desde Ajustes o desde SCR-12) se DEBE guardar la fecha y hora.
- **FR-043**: La fila DEBE mostrar «Último escaneo: hace …» en español: «hace un momento» (menos de 1 minuto), «hace N minutos», «hace N horas», «hace N días», en singular o plural según corresponda.
- **FR-044**: Si nunca se ha terminado un escaneo desde Rola, la fila DEBE decir «Aún no has escaneado».

**Legal y versión (DEC-23, SCR-72, SCR-73, SCR-74)**

- **FR-050**: Términos, Privacidad y Licencias DEBEN mostrar el texto completo de Figma, con desplazamiento, título marcado como encabezado y flecha para volver con descripción accesible.
- **FR-051**: Licencias DEBE incluir, además de lo que muestra Figma, Coil, Palette y Reorderable (Apache 2.0).
- **FR-052**: Los textos legales DEBEN estar en los recursos de texto de la app, en español.
- **FR-053**: «Versión» DEBE mostrar la versión de la compilación instalada, no un texto fijo.

**Copia de seguridad (PEND-09)**

- **FR-060**: La app NO DEBE participar en la copia de seguridad de Android.

**Transversales**

- **FR-070**: Ajustes y los documentos son planos, sin relieve (DEC-27).
- **FR-071**: Sin animaciones nuevas salvo el fundido del cambio de tema (≤ 300 ms, sin saltos); la hoja del tema y el interruptor usan la animación que ya tienen; con «reducir movimiento», el cambio es instantáneo o un fundido corto (DEC-28).
- **FR-072**: Nada de esta función usa red (INV-01).
- **FR-073**: Colores, tipografía, formas y espacios salen del tema (INV-04); textos en español.
- **FR-074**: Filas con área táctil ≥ 48 dp, opciones con su estado anunciado al lector de pantalla, contraste ≥ 4.5:1 en ambos temas y tamaño de letra del sistema (INV-06).
- **FR-075**: Si los ajustes no se pueden leer o guardar, la app DEBE seguir con los valores por defecto o los elegidos en pantalla, sin cerrarse.

### Entidades clave

- **Tema**: Sistema, Oscuro o Claro. KV-DATASTORE `theme`.
- **Último escaneo**: fecha y hora del último escaneo terminado; vacío si nunca se ha escaneado. KV-DATASTORE `last_scan_at`.
- **Documento legal**: título y texto. Fijo en la app; no se guarda.

## Criterios de éxito *(obligatorio)*

### Resultados medibles

- **SC-001**: Cambiar el tema toma 2 toques desde Ajustes y la app completa cambia en menos de 300 ms, sin reinicio.
- **SC-002**: En el 100 % de las aperturas probadas, la app abre directamente en el tema guardado, sin un cuadro del otro tema.
- **SC-003**: Llegar a Ajustes toma 1 toque desde Inicio y desde la pantalla sin canciones; volver toma 1 gesto.
- **SC-004**: Los tres documentos se leen completos hasta el final con la letra más grande del sistema.
- **SC-005**: SCR-70, 72, 73 y 74 coinciden con Figma en contenido y tokens, en oscuro y en claro, salvo lo que DEC-30 retira.
- **SC-006**: Ninguna pantalla existente pierde legibilidad en tema claro (contraste ≥ 4.5:1 en textos).

## Supuestos

- La hoja del temporizador, su lógica y sus opciones ya existen (reproductor); aquí solo se reutilizan.
- El escaneo de Ajustes reutiliza el de SCR-12 tal cual (tarea 07); no se agrega selector de carpetas aunque DEC-26 diga «carpetas elegidas».
- La sección LETRAS, «Carpeta de letras» (tarea 09), «Agregar widget» (tarea 10) y la duración mínima (PEND-07) quedan fuera.
- Los nodos claros de las pantallas existentes ya están en Figma; las diferencias que aparezcan al ver el tema claro en el teléfono se reportan.
