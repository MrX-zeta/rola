# Especificación: Onboarding completo

**Carpeta**: `specs/003-onboarding`

**Creada**: 2026-11-15

**Estado**: Borrador

**Tarea**: `ai/handoff/siguiente.md` · Tarea 04, Parte B · Grafo 1.7.0

**Entrada**: «La primera vez que abro Rola veo tres pantallas de bienvenida (qué es Rola, letras, sin conexión) que puedo pasar deslizando o con «Siguiente», con indicador de página y «Omitir»; la tercera dice «Empezar». Después me pide el permiso de música. Si ya vi la bienvenida, no vuelve a aparecer; si ya di el permiso, la app abre directo en Biblioteca.»

**Referencias del grafo**: pantallas SCR-02, SCR-03, SCR-04 (Onboarding 1–3), SCR-05 (Permisos), SCR-06 (Permiso denegado); decisiones DEC-17, DEC-18, DEC-27, DEC-28; almacén KV-DATASTORE (`onboarding_completed`); invariante INV-06. Esta spec cita los ids; ante cualquier duda manda el nodo del grafo y, en lo visual, Figma.

## Aclaraciones

### Sesión 2026-11-15

Respondidas por la tarea 04 (Paso B2); no se preguntan a Brian.

- P: ¿Dónde existe el deslizar horizontal entre pantallas? → R: solo dentro de la bienvenida; no hay deslizar entre pestañas (FR-003).
- P: ¿El interruptor de LRCLIB de la pantalla 3 cambia algún ajuste? → R: no, es decorativo; el ajuste real llega con Ajustes (FR-019).
- P: ¿Con qué se dibujan las ilustraciones? → R: con formas y vectores del tema (portadas genéricas, tarjeta de letra, wifi tachado), sin imágenes de discos reales (FR-019).
- P: ¿Cuándo se guarda que la bienvenida se completó? → R: al pulsar «Empezar» u «Omitir», en las preferencias del usuario (`onboarding_completed`) (FR-007).
- P: ¿Cómo cambia de pantalla con «reducir movimiento»? → R: con un fundido corto, sin desplazamiento extra (FR-015).

## Escenarios de usuario y pruebas *(obligatorio)*

### Historia 1 · Conocer Rola la primera vez (Prioridad: P1)

La primera vez que abro Rola, después del splash, veo la bienvenida: tres pantallas seguidas que me cuentan qué es Rola (SCR-02), que muestra letras (SCR-03) y que funciona sin conexión (SCR-04). Paso de una a otra deslizando con el dedo o tocando «Siguiente». Un indicador me dice en qué pantalla estoy. En la tercera, el botón principal dice «Empezar» y me lleva a pedir el permiso de música.

**Por qué esta prioridad**: es lo primero que ve una persona nueva; sin ella la app arranca directo en el permiso, sin contexto.

**Prueba independiente**: instalar la app desde cero, abrirla, recorrer las tres pantallas con «Siguiente» y con el dedo, y tocar «Empezar»: aparece Permisos.

**Escenarios de aceptación**:

1. **Dado** que nunca completé la bienvenida, **cuando** abro Rola, **entonces** veo la pantalla 1 de la bienvenida con el indicador en la primera posición.
2. **Dado** que estoy en la pantalla 1 o 2, **cuando** toco «Siguiente» o deslizo hacia la izquierda, **entonces** paso a la pantalla siguiente y el indicador avanza.
3. **Dado** que estoy en la pantalla 2 o 3, **cuando** deslizo hacia la derecha, **entonces** vuelvo a la pantalla anterior.
4. **Dado** que estoy en la pantalla 3, **cuando** miro el botón principal, **entonces** dice «Empezar»; al tocarlo, la bienvenida queda marcada como vista y aparece Permisos.
5. **Dado** que estoy en la pantalla 1 o 2 de la bienvenida, **cuando** toco «Omitir», **entonces** la bienvenida queda marcada como vista y aparece Permisos.

---

### Historia 2 · Dar el permiso de música (Prioridad: P1)

Después de la bienvenida, Rola me explica que necesita leer mis audios (SCR-05) y me pide el permiso del sistema. Si lo doy, entro a Biblioteca. Si lo niego, veo por qué hace falta y cómo activarlo en Ajustes (SCR-06).

**Por qué esta prioridad**: sin el permiso no hay música que mostrar.

**Prueba independiente**: tras «Empezar», tocar «Permitir» y aceptar: aparece Biblioteca. Repetir negándolo: aparece Permiso denegado.

**Escenarios de aceptación**:

1. **Dado** que terminé la bienvenida y no tengo el permiso, **cuando** veo Permisos, **entonces** se explica solo el permiso de «Música y audio» (sin la fila de Notificaciones, como en Figma).
2. **Dado** que estoy en Permisos, **cuando** acepto el permiso del sistema, **entonces** entro a Biblioteca.
3. **Dado** que estoy en Permisos, **cuando** niego el permiso, **entonces** veo Permiso denegado con la ruta de Ajustes y la opción de volver a intentarlo.

---

### Historia 3 · Volver a abrir Rola (Prioridad: P2)

Las siguientes veces que abro Rola no veo otra vez la bienvenida. Si ya di el permiso, la app abre directo en Biblioteca; si no, abre en Permisos.

**Por qué esta prioridad**: la bienvenida sirve una vez; repetirla estorba.

**Prueba independiente**: completar la bienvenida y el permiso, cerrar la app por completo y abrirla: aparece Biblioteca sin pasar por la bienvenida ni Permisos.

**Escenarios de aceptación**:

1. **Dado** que completé la bienvenida y tengo el permiso, **cuando** abro Rola, **entonces** aparece Biblioteca.
2. **Dado** que completé la bienvenida pero no tengo el permiso (lo negué o lo quité en Ajustes), **cuando** abro Rola, **entonces** aparece Permisos (o Permiso denegado si lo negué en esta sesión), no la bienvenida.
3. **Dado** que cerré la app a mitad de la bienvenida sin tocar «Empezar» ni «Omitir», **cuando** la abro otra vez, **entonces** la bienvenida empieza de nuevo en la pantalla 1.

---

### Casos límite

- Rotar la pantalla o pasar la app a segundo plano a mitad de la bienvenida conserva la pantalla en la que iba.
- Atrás del sistema en las pantallas 2 o 3 vuelve a la anterior; en la pantalla 1 sale de la app como cualquier pantalla inicial.
- Con «reducir movimiento» activo, el cambio de pantalla de la bienvenida no se desplaza: es un fundido corto (DEC-28).
- Una instalación que ya tenía el permiso de una versión anterior pero nunca vio la bienvenida la ve una vez al actualizar (DEC-18: se muestra hasta que se completa).
- Si al abrir la app todavía no se sabe si la bienvenida se completó, no debe verse un parpadeo de la bienvenida ni de Biblioteca antes de la pantalla correcta.
- Con letra del sistema grande, los textos de la bienvenida no se cortan ni tapan los botones.

## Requisitos *(obligatorio)*

### Requisitos funcionales

**Bienvenida (SCR-02, SCR-03, SCR-04, DEC-18)**

- **FR-001**: La primera vez que se abre la app, y mientras la bienvenida no se haya completado, DEBE mostrarse la bienvenida antes que cualquier otra pantalla.
- **FR-002**: La bienvenida DEBE tener tres pantallas, en este orden: qué es Rola (SCR-02), letras (SCR-03) y sin conexión (SCR-04), con los textos e ilustraciones de sus nodos de Figma.
- **FR-003**: Se DEBE poder pasar entre las pantallas de la bienvenida deslizando horizontalmente y con el botón «Siguiente». El deslizar horizontal solo existe dentro de la bienvenida.
- **FR-004**: Un indicador de página DEBE mostrar cuál de las tres pantallas está visible, y anunciarlo al lector de pantalla.
- **FR-005**: En la tercera pantalla, el botón principal DEBE decir «Empezar» en lugar de «Siguiente».
- **FR-006**: «Omitir» DEBE estar en las pantallas 1 y 2 (en la 3 no aparece, como en Figma 22:383: ahí está «Empezar») y llevar a Permisos.
- **FR-007**: Tocar «Empezar» u «Omitir» DEBE marcar la bienvenida como completada de forma permanente (sobrevive a cerrar la app y a reiniciar el teléfono) y llevar al permiso de música.
- **FR-008**: Una vez completada, la bienvenida NO DEBE volver a mostrarse.

**Permisos (SCR-05, SCR-06, DEC-18)**

- **FR-009**: Permisos DEBE pedir solo el permiso de audio y mostrar solo su fila; la fila de Notificaciones se quita, como en Figma.
- **FR-010**: Al conceder el permiso se DEBE entrar a Biblioteca; al negarlo, Permiso denegado explica cómo activarlo en Ajustes y permite volver a intentarlo (comportamiento actual).
- **FR-011**: Con la bienvenida completada y el permiso concedido, la app DEBE abrir directo en Biblioteca.

**Movimiento (DEC-28)**

- **FR-012**: El indicador de página DEBE acompañar el cambio de pantalla (la marca activa se estira hacia la pantalla nueva).
- **FR-013**: La ilustración de cada pantalla DEBE tener una entrada sutil al aparecer su pantalla.
- **FR-014**: El botón principal NO DEBE saltar de tamaño ni de lugar al cambiar de «Siguiente» a «Empezar».
- **FR-015**: Con «reducir movimiento», el cambio de pantalla DEBE ser un fundido corto sin desplazamiento extra, y las ilustraciones aparecen sin movimiento.
- **FR-016**: Toda animación nueva DEBE durar ≤ 300 ms (las salidas, menos que las entradas) y ser interrumpible.

**Transversales**

- **FR-017**: La bienvenida y Permisos NO DEBEN usar red (INV-01).
- **FR-018**: Colores, tipografía, formas y espacios DEBEN salir de los tokens del tema (INV-04). El relieve se usa solo donde lo marca DEC-27 (bienvenida, Permisos y Permiso denegado, salvo los íconos de permisos y la ruta de Ajustes).
- **FR-019**: Las ilustraciones DEBEN construirse con formas y colores del tema (portadas genéricas, tarjeta de letra, wifi tachado), sin imágenes de discos reales. El interruptor de LRCLIB de la pantalla 3 es decorativo: no se puede tocar ni cambia ningún ajuste.
- **FR-020**: Botones con área táctil ≥ 48 dp, descripciones accesibles en lo que solo es ícono, contraste ≥ 4.5:1 y textos que respetan el tamaño de letra del sistema (INV-06).
- **FR-021**: La bienvenida y Permisos DEBEN verse bien en tema oscuro y claro según sus nodos de Figma.
- **FR-022**: Todos los textos DEBEN estar en español.

### Entidades clave

- **Bienvenida completada**: un valor sí/no guardado en las preferencias del usuario (`onboarding_completed` de KV-DATASTORE). Empieza en «no»; pasa a «sí» al tocar «Empezar» u «Omitir» y no vuelve a «no».
- **Permiso de música**: lo concede o niega el sistema; la app solo lo consulta.

## Criterios de éxito *(obligatorio)*

### Resultados medibles

- **SC-001**: Una persona nueva llega de abrir la app a Biblioteca en menos de 30 segundos recorriendo la bienvenida, o en menos de 10 s usando «Omitir».
- **SC-002**: En el 100 % de los arranques posteriores con permiso, la primera pantalla después del splash es Biblioteca, sin parpadeo de otra pantalla.
- **SC-003**: Las tres pantallas, Permisos y Permiso denegado coinciden con sus nodos de Figma en oscuro y claro (revisión `design-critique` sin diferencias de contenido).
- **SC-004**: Con «reducir movimiento», ningún cambio de pantalla de la bienvenida desplaza contenido.
- **SC-005**: Los cambios de pantalla de la bienvenida se ven fluidos (sin cuadros perdidos visibles) en la compilación `perfil` del Redmi.

## Supuestos

- La bienvenida aparece después del splash del sistema (DEC-17), sin pantalla de splash propia.
- Una instalación con permiso pero sin la bienvenida completada (de las tareas anteriores) la ve una vez: DEC-18 la condiciona solo a `onboarding_completed`. Se pregunta en el reporte por si se prefiere saltarla en ese caso.
- Atrás del sistema en la pantalla 1 sale de la app; en las pantallas 2 y 3 vuelve a la anterior.
- El permiso de notificaciones no se pide en esta función (DEC-18, cambio del 2026-11-14).
