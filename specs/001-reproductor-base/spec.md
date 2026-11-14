# Especificación: Reproductor base

**Carpeta**: `specs/001-reproductor-base`

**Creada**: 2026-11-14

**Estado**: Borrador

**Tarea**: `ai/handoff/siguiente.md` · Tarea 02 · Grafo 1.3.0

**Entrada**: «Como dueño de Rola quiero abrir la app, dar permiso a mi música y ver todas mis canciones ordenadas por título, tocar una y que suene aunque salga de la app o apague la pantalla, controlarla desde la notificación y desde una mini tarjeta encima de la barra de navegación (portada, título, artista, siguiente, reproducir/pausa y línea de progreso, con el color de la portada). Si niego el permiso, veo una pantalla que me explica y me lleva a Ajustes. Inicio y Buscar existen en la barra pero todavía muestran un aviso de «próximamente».»

**Referencias del grafo**: pantallas SCR-05 (Permisos), SCR-06 (Permiso denegado), SCR-20 (Biblioteca · Canciones), SCR-50 (Mini reproductor); decisiones DEC-03, DEC-04, DEC-05, DEC-09, DEC-15, DEC-18, DEC-26, DEC-27, PEND-08 (resuelto, opción A); invariantes INV-01…INV-11. Esta spec cita los ids; ante cualquier duda manda el nodo del grafo y, en lo visual, Figma.

## Aclaraciones

### Sesión 2026-11-14

Respondidas con el grafo; lo que el grafo no responde se marca **supuesto** (opción más simple) y va al reporte.

- P: ¿A qué pestaña llega la app al abrirse con el permiso concedido? → R: a Inicio, destino por defecto según DEC-03; Biblioteca se abre desde la barra. (Grafo. Se propone en el reporte usar Biblioteca como destino temporal mientras Inicio sea «próximamente».)
- P: ¿Qué pasa cuando termina la última canción de la cola? → R: la reproducción se detiene y la tarjeta queda en pausa con esa canción; repetir llega con Reproduciendo (DEC-24). **Supuesto.**
- P: ¿Qué muestra Biblioteca si el teléfono no tiene música? → R: un texto simple con el tema que lo explica; el diseño propio «Sin canciones» (SCR-12, DEC-19) es de Inicio y llega con esa pantalla. **Supuesto.**
- P: ¿Qué se muestra si una canción no tiene título o artista? → R: el nombre del archivo sin extensión como título y «Artista desconocido» como artista. **Supuesto.**
- P: ¿Se recuerda la cola o la canción actual al cerrar y volver a abrir la app? → R: no; sin persistencia en esta tarea (Room y DataStore fuera de alcance). **Supuesto.**

## Escenarios de usuario y pruebas *(obligatorio)*

### Historia 1 · Dar permiso y ver mis canciones (Prioridad: P1)

Abro Rola por primera vez. Una pantalla (SCR-05) me explica que necesita leer los audios del teléfono y nada más. Toco «Permitir acceso», acepto el aviso del sistema y entro a la app; en Biblioteca (SCR-20) veo todas mis canciones ordenadas por título.

**Por qué esta prioridad**: sin permiso ni lista no hay nada que reproducir; es la puerta de entrada.

**Prueba independiente**: instalar en un teléfono con música, conceder el permiso y comprobar que la lista muestra todas las canciones en el orden de DEC-05.

**Escenarios de aceptación**:

1. **Dado** que nunca di el permiso de música, **cuando** abro la app, **entonces** veo Permisos (SCR-05) con «Música y audio» marcado «Ahora» y «Notificaciones» marcado «Después».
2. **Dado** que estoy en Permisos, **cuando** toco «Permitir acceso» y acepto, **entonces** entro a la app en Inicio (DEC-03) y desde la barra abro Biblioteca con la lista de canciones.
3. **Dado** que ya di el permiso antes, **cuando** abro la app, **entonces** entro directo a la app sin pasar por Permisos.
4. **Dado** que estoy en Biblioteca, **cuando** miro la lista, **entonces** cada fila muestra portada, título y artista, y arriba hay un resumen con el número de canciones y el orden («N canciones · Título A–Z»).
5. **Dado** que agrego o borro música en el teléfono con la app abierta, **cuando** el sistema registra el cambio, **entonces** la lista se actualiza sola (DEC-26).

---

### Historia 2 · Tocar una canción y que suene en segundo plano (Prioridad: P1)

Toco una canción de la lista y empieza a sonar. Salgo de la app o apago la pantalla y sigue sonando. Puedo pausarla, reanudarla y pasar a la siguiente desde la notificación del sistema.

**Por qué esta prioridad**: es la razón de ser de la app.

**Prueba independiente**: tocar una canción, salir al escritorio, apagar la pantalla y comprobar que la música continúa y que la notificación controla la reproducción.

**Escenarios de aceptación**:

1. **Dado** que estoy en Biblioteca, **cuando** toco una canción, **entonces** empieza a sonar y la cola es la lista completa, empezando por esa canción.
2. **Dado** que una canción suena, **cuando** salgo de la app, cambio de pestaña o apago la pantalla, **entonces** la música no se detiene (INV-11).
3. **Dado** que una canción suena, **cuando** abro la notificación, **entonces** veo portada, título, artista y los controles de reproducción.
4. **Dado** que es la primera vez que reproduzco en un teléfono con Android 13 o superior, **cuando** toco una canción, **entonces** el sistema me pide el permiso de notificaciones; si lo niego, la música suena igual.
5. **Dado** que la música suena por audífonos, **cuando** se desconectan, **entonces** la reproducción se pausa.
6. **Dado** que otra app empieza a reproducir audio o entra una llamada, **cuando** eso ocurre, **entonces** Rola cede el sonido como cualquier reproductor de música.
7. **Dado** que estoy en Biblioteca, **cuando** toco «Aleatorio», **entonces** suena toda la lista en orden aleatorio.

---

### Historia 3 · Controlar la música desde la mini tarjeta (Prioridad: P2)

Mientras navego por la app, una tarjeta encima de la barra de navegación (SCR-50) me muestra qué suena: portada, título, artista, botón siguiente, botón reproducir/pausa y una línea de progreso. La tarjeta toma el color de la portada (DEC-15, PEND-08 A).

**Por qué esta prioridad**: da control sin salir de la app; la notificación ya cubre lo esencial.

**Prueba independiente**: con una canción cargada, comprobar que la tarjeta aparece, que sus botones funcionan y que la línea avanza; sin canción cargada, que no aparece.

**Escenarios de aceptación**:

1. **Dado** que no hay nada cargado, **cuando** abro Biblioteca, **entonces** la mini tarjeta no aparece.
2. **Dado** que hay una canción cargada, **cuando** estoy en cualquier pestaña, **entonces** la tarjeta aparece encima de la barra de navegación con portada, título, artista, siguiente, reproducir/pausa y progreso.
3. **Dado** que la canción suena, **cuando** toco pausa en la tarjeta, **entonces** se pausa y el botón cambia a reproducir (y al revés).
4. **Dado** que hay una canción siguiente en la cola, **cuando** toco siguiente, **entonces** suena la siguiente y la tarjeta se actualiza.
5. **Dado** que la canción tiene portada, **cuando** se muestra la tarjeta, **entonces** su fondo y su acento salen de la portada y el texto se lee con contraste de al menos 4.5:1 (INV-06); si no se puede sacar color, usa los colores del tema.
6. **Dado** que la tarjeta está visible, **cuando** la toco fuera de sus botones, **entonces** no pasa nada (Reproduciendo y los gestos de DEC-04 llegan en la tarea 03).

---

### Historia 4 · Si niego el permiso (Prioridad: P2)

Si niego el permiso de música, veo una pantalla (SCR-06) que explica que sin él Rola no puede encontrar mis canciones, muestra la ruta en Ajustes y me ofrece abrir Ajustes o intentar de nuevo.

**Por qué esta prioridad**: evita que la app quede vacía sin explicación (INV-05).

**Prueba independiente**: negar el permiso y comprobar que aparece la pantalla, que «Abrir Ajustes» lleva a los ajustes de la app y que al volver con el permiso concedido se entra a la app.

**Escenarios de aceptación**:

1. **Dado** que estoy en Permisos, **cuando** niego el permiso, **entonces** veo Permiso denegado (SCR-06).
2. **Dado** que estoy en Permiso denegado, **cuando** toco «Abrir Ajustes», **entonces** se abren los ajustes del sistema de la app.
3. **Dado** que activé el permiso en Ajustes, **cuando** vuelvo a Rola, **entonces** entro a la app sin tocar nada más.
4. **Dado** que estoy en Permiso denegado, **cuando** toco «Intentar de nuevo», **entonces** el sistema vuelve a pedir el permiso si todavía puede; si ya no puede, sigo en esta pantalla con la ruta a Ajustes visible.
5. **Dado** que retiro el permiso desde Ajustes, **cuando** vuelvo a abrir Rola, **entonces** veo de nuevo Permisos o Permiso denegado, nunca una lista vacía sin explicación.

---

### Historia 5 · Barra de navegación con tres destinos (Prioridad: P3)

La barra en forma de píldora (DEC-03) tiene Inicio, Biblioteca y Buscar. Inicio y Buscar todavía muestran un aviso de «próximamente».

**Por qué esta prioridad**: deja lista la estructura; el contenido de Inicio y Buscar llega después.

**Prueba independiente**: tocar cada destino y comprobar el cambio de pestaña, el aviso en Inicio y Buscar y el comportamiento de atrás.

**Escenarios de aceptación**:

1. **Dado** que estoy en cualquier pestaña, **cuando** miro la barra, **entonces** el destino activo muestra ícono y texto y los otros solo ícono, con descripción para accesibilidad.
2. **Dado** que toco Inicio o Buscar, **cuando** se abre la pestaña, **entonces** veo un aviso de «próximamente» y la música sigue sonando.
3. **Dado** que estoy en una pestaña, **cuando** deslizo de lado, **entonces** no cambia de pestaña: solo se cambia tocando la barra.
4. **Dado** que estoy en una pestaña, **cuando** uso atrás del sistema, **entonces** se respeta DEC-03.

---

### Casos límite

- **Teléfono sin música**: Biblioteca muestra un texto simple que lo explica (DEC-09) y el botón Aleatorio no se ofrece.
- **Canción sin portada**: la fila y la tarjeta muestran un recuadro del tema con un ícono de nota; la tarjeta usa los colores del tema.
- **Título o artista vacíos o desconocidos**: se muestra el nombre del archivo sin extensión como título y «Artista desconocido» como artista.
- **Títulos con acentos, Ñ, números o símbolos**: se ordenan según DEC-05.
- **Archivo borrado o ilegible mientras suena o está en la cola**: se salta a la siguiente y se avisa con un mensaje breve (INV-05).
- **Termina la última canción de la cola**: la reproducción se detiene y la tarjeta queda en pausa con esa canción.
- **Biblioteca muy grande (miles de canciones)**: la lista abre y se desplaza sin bloquear la interfaz.
- **Permiso retirado con la app en segundo plano**: al volver, la app muestra Permisos o Permiso denegado en lugar de fallar.

## Requisitos *(obligatorio)*

### Requisitos funcionales

**Permisos (SCR-05, SCR-06, DEC-18)**

- **FR-001**: Al abrir la app sin permiso de música, el sistema DEBE mostrar Permisos (SCR-05) antes que cualquier otra pantalla. El onboarding de bienvenida queda fuera de esta tarea.
- **FR-002**: «Permitir acceso» DEBE pedir el permiso de lectura de audio que corresponda a la versión de Android.
- **FR-003**: Si el permiso se niega, el sistema DEBE mostrar Permiso denegado (SCR-06) con explicación, la ruta «Ajustes › Apps › Rola › Permisos», un botón «Abrir Ajustes» y un botón «Intentar de nuevo».
- **FR-004**: Al conceder el permiso o volver a la app con él concedido, el sistema DEBE entrar a la app en Inicio (destino por defecto, DEC-03) sin pasos extra.
- **FR-005**: El permiso de notificaciones (Android 13+) DEBE pedirse la primera vez que se reproduce algo; negarlo NO DEBE impedir la reproducción.

**Biblioteca (SCR-20, DEC-05, DEC-26)**

- **FR-006**: Biblioteca DEBE mostrar todas las canciones del teléfono marcadas como música, cada una con portada, título y artista. Sin título: nombre del archivo sin extensión; sin artista: «Artista desconocido».
- **FR-006a**: La fila de la canción actual DEBE mostrar su título con el color `primary` del tema, como en Figma (SCR-20).
- **FR-007**: La lista DEBE ordenarse por título, ascendente, con las reglas de DEC-05: acentos ignorados al agrupar y comparar, Ñ como letra propia entre N y O, títulos que empiezan con número o símbolo agrupados en «#».
- **FR-008**: NO se ignoran artículos al ordenar (PEND-05 sigue propuesto) ni se ocultan audios cortos (PEND-07 sigue propuesto).
- **FR-009**: Biblioteca DEBE mostrar solo el chip «Canciones» (activo); Listas, Álbumes y Artistas no se muestran todavía.
- **FR-010**: Biblioteca DEBE mostrar el resumen «N canciones · Título A–Z» y un botón «Aleatorio» funcional. El botón de filtros, el menú ⋮ de cada fila y el abecedario NO se muestran en esta tarea.
- **FR-011**: La lista DEBE actualizarse sola cuando cambie la música del teléfono (DEC-26).
- **FR-012**: Si no hay canciones, Biblioteca DEBE mostrar un texto simple que lo explica (DEC-09); sin botón Aleatorio.

**Reproducción (INV-11)**

- **FR-013**: Tocar una canción DEBE reproducirla y cargar como cola la lista completa en el orden mostrado, empezando por ella.
- **FR-014**: «Aleatorio» DEBE reproducir toda la lista en orden aleatorio.
- **FR-015**: La reproducción DEBE continuar al salir de la app, cambiar de pestaña o apagar la pantalla, y solo detenerse por pausa o detener (INV-11).
- **FR-016**: Mientras haya algo cargado, el sistema DEBE mostrar una notificación de reproducción con portada, título, artista y controles de reproducción, con el ícono monocromo de Rola.
- **FR-017**: La reproducción DEBE pausarse al desconectar audífonos y ceder el audio a llamadas u otras apps de audio.
- **FR-018**: Si una canción no se puede reproducir, el sistema DEBE pasar a la siguiente y mostrar un aviso breve (INV-05).
- **FR-018a**: Al terminar la última canción de la cola, la reproducción DEBE detenerse sin repetir; la tarjeta queda en pausa con esa canción.
- **FR-018b**: La cola y la canción actual NO se recuerdan entre reinicios de la app.

**Mini tarjeta (SCR-50, DEC-04, DEC-15, PEND-08 A)**

- **FR-019**: La mini tarjeta DEBE aparecer encima de la barra de navegación solo cuando hay una canción cargada.
- **FR-020**: La tarjeta DEBE mostrar portada, título, artista, botón siguiente, botón reproducir/pausa y una línea de progreso que avanza con la canción.
- **FR-021**: El color de la tarjeta DEBE salir de la portada de la canción actual; si no hay portada o no se puede extraer color, DEBE usar los colores del tema.
- **FR-022**: El texto y los íconos de la tarjeta DEBEN tener contraste de al menos 4.5:1 sobre su fondo; si el color extraído no lo alcanza, se ajusta su luminosidad hasta lograrlo (INV-06).
- **FR-023**: Tocar la tarjeta fuera de sus botones no hace nada en esta tarea, y no hay gestos (DEC-04 y Reproduciendo llegan en la tarea 03).

**Navegación (DEC-03)**

- **FR-024**: La barra de navegación DEBE tener tres destinos: Inicio, Biblioteca y Buscar; el activo con ícono y texto, los otros solo con ícono y descripción accesible.
- **FR-025**: Inicio y Buscar DEBEN mostrar un aviso de «próximamente».
- **FR-026**: Las pestañas solo cambian tocando la barra; no hay deslizar entre pestañas.
- **FR-027**: Atrás del sistema DEBE respetar la pila de cada pestaña (DEC-03).

**Transversales**

- **FR-028**: Esta función NO DEBE usar red (INV-01).
- **FR-029**: Colores, tipografía, formas y espacios DEBEN salir de los tokens del tema (INV-04). Estilo plano; el relieve solo en Permisos y Permiso denegado donde Figma lo muestra (DEC-27).
- **FR-030**: Todo botón de solo ícono DEBE tener descripción accesible y un área táctil de al menos 48 dp; los textos DEBEN respetar el tamaño de letra del sistema (INV-06).
- **FR-031**: Todos los textos de la interfaz DEBEN estar en español.

### Entidades clave

- **Canción**: un audio del teléfono marcado como música. Atributos: identificador, título, artista, álbum, duración, portada (si existe), nombre de archivo.
- **Cola**: lista ordenada de canciones que se reproducen una tras otra, con la posición actual.
- **Estado de reproducción**: canción actual, si suena o está en pausa, posición y duración.
- **Estado del permiso de música**: concedido, no pedido todavía o negado.
- **Colores de la tarjeta**: fondo, acento y color de texto derivados de la portada actual, o del tema si no hay portada.

## Criterios de éxito *(obligatorio)*

### Resultados medibles

- **SC-001**: Desde la primera apertura, Brian ve su lista de canciones con un máximo de 3 toques (Permitir acceso, aceptar el aviso del sistema y Biblioteca en la barra).
- **SC-002**: El 100 % de las canciones marcadas como música en el teléfono aparecen en Biblioteca, en el orden de DEC-05.
- **SC-003**: Tras tocar una canción, el sonido empieza en menos de 1 segundo.
- **SC-004**: Con la pantalla apagada o la app en segundo plano, la música sigue sonando al menos 30 minutos seguidos sin cortes.
- **SC-005**: Pausar, reanudar y siguiente funcionan desde la notificación y desde la mini tarjeta en el 100 % de los intentos.
- **SC-006**: En todas las portadas probadas, el texto de la mini tarjeta mantiene contraste ≥ 4.5:1.
- **SC-007**: Una biblioteca de 2 000 canciones se muestra y se desplaza sin saltos visibles.
- **SC-008**: La app no realiza ninguna conexión de red durante estos flujos.

## Supuestos

- El onboarding de bienvenida (SCR-02 a SCR-04) no existe todavía: el permiso se pide al abrir la app desde SCR-05 (decisión de la tarea 02).
- El tema es oscuro por defecto; el claro seleccionable llega con Ajustes.
- **Supuestos** de las aclaraciones: fin de cola sin repetir, texto simple sin canciones, nombre de archivo y «Artista desconocido» para metadatos vacíos, sin persistencia de la cola.
- **Supuesto**: el permiso de notificaciones se pide como mucho una vez por ejecución de la app (sin persistencia no se puede saber si ya se pidió antes); Android deja de mostrarlo tras dos negativas.
- **Supuesto**: «Intentar de nuevo» vuelve a lanzar la petición del sistema; si Android ya no la muestra (negado dos veces), la app se queda en SCR-06 con la ruta a Ajustes.
- **Supuesto**: la notificación usa los controles estándar del sistema (anterior, reproducir/pausa, siguiente); la mini tarjeta no tiene «anterior» (SCR-50).
- **Supuesto**: «Aleatorio» reparte la lista completa en orden aleatorio una vez; no es un modo que se active o desactive (eso llega con Reproduciendo, DEC-24).
- **Supuesto**: el aviso «próximamente» de Inicio y Buscar es un texto centrado en la pestaña, con la barra y la mini tarjeta visibles.
- Fuera de esta función: onboarding, Inicio real, Buscar, álbumes, artistas, listas, abecedario, filtros, menú de opciones, Reproduciendo, cola visible, gestos, letras, ajustes, widget y tema claro seleccionable.
