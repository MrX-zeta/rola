# Guía de validación: Favoritas y Listas

## Requisitos

- Emulador Pixel 7 Pro con música y un archivo de prueba desechable (p. ej. una copia de un `.mp3` en `/sdcard/Music/`).
- Opcional: Redmi Note 13 Pro+ para `perfil`.

## Compilar y probar

```powershell
.\gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug
.\gradlew :app:connectedDebugAndroidTest
```

Esperado: todo en verde; `app/schemas/com.luis.rola.core.database.RolaDatabase/1.json` generado y en git. El corazón se juzga en `perfil` (DEC-28 `fluidez`).

## Recorrido manual (emulador)

| # | Paso | Esperado | Spec |
|---|---|---|---|
| 1 | Reproducir una canción, abrir Reproduciendo, tocar el corazón | Relleno lima, rebote, ráfaga y vibración | FR-001–003 |
| 2 | Tocar el corazón 5 veces rápido | Termina en el estado del último toque, sin trabarse | FR-005 |
| 3 | Biblioteca → chip «Listas» | Solo chips Listas y Canciones; «Nueva lista» y Favoritas primero con su conteo | FR-009–011 |
| 4 | Abrir Favoritas | La última marcada arriba; Reproducir y Aleatorio | FR-009, FR-019 |
| 5 | «Nueva lista» → «Viaje» → Crear | Se abre «Viaje» vacía con la hoja encima | FR-012, FR-013 |
| 6 | Buscar «cancion» (sin acento), tocar una letra, marcar 3, «Agregar 3 canciones» | Filtra sin acentos; salta a la letra; la lista muestra las 3 | FR-014–017 |
| 7 | Reproducir / Aleatorio en «Viaje» | La cola es la lista (en orden / mezclada) | FR-020 |
| 8 | ⋮ de una canción de la lista → Quitar de la lista → Deshacer | Se va y vuelve a su lugar | FR-021, FR-024 |
| 9 | Biblioteca · Canciones: pulsación larga → Añadir a la cola | Snackbar; está al final de la Cola; la música sigue | FR-023, FR-025 |
| 10 | ⋮ → Añadir a una lista → «Viaje» (dos veces) | Agregada; la segunda vez avisa que ya estaba | FR-026 |
| 11 | ⋮ → Favorita; abrir Reproduciendo si suena | Corazón relleno | FR-027 |
| 12 | Archivo de prueba en Favoritas, en «Viaje» y en la cola → ⋮ → Borrar del dispositivo → aceptar | Aviso de Android y nada más; snackbar; desaparece de Canciones, Favoritas, «Viaje» y Cola | FR-028–029, SC-003 |
| 13 | Igual pero cancelar el aviso | Nada cambia | FR-030 |
| 14 | «Quitar animaciones» y tocar el corazón | Solo cambia el relleno | FR-006, SC-005 |
| 15 | Cerrar la app por completo y abrirla | Favoritas y listas siguen | SC-004 |
| 16 | Tema claro y letra al máximo | Pantallas completas y legibles | FR-035, FR-037 |

Capturas en `ai/handoff/capturas/`: `t05-favorita.png`, `t05-listas.png`, `t05-agregar.png`, `t05-menu.png`.
