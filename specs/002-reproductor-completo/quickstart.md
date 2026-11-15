# Guía de validación: Reproductor completo

## Requisitos

- Emulador Pixel 7 Pro (API 37) con música (varias canciones, alguna sin portada y alguna con portada clara y oscura).
- Opcional: Redmi Note 13 Pro+ para calibrar gestos.

## Compilar y probar

```powershell
.\gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug
.\gradlew :app:connectedDebugAndroidTest
```

Esperado: todo en verde. Las animaciones se juzgan en `perfil` (DEC-28 `fluidez`).

## Recorrido manual (emulador)

| # | Paso | Esperado | Spec |
|---|---|---|---|
| 1 | Tocar una canción; tocar la mini tarjeta | Se abre Reproduciendo creciendo desde la tarjeta; la música sigue | FR-001, FR-006 |
| 2 | Cerrar con la flecha; abrir deslizando la tarjeta hacia arriba despacio y soltar a la mitad | Cierra; al soltar sobre el umbral termina de abrir, debajo regresa | FR-002, FR-003 |
| 3 | En Reproduciendo deslizar hacia abajo; Atrás | Vuelve a la tarjeta | FR-003 |
| 4 | Arrastrar la barra a la mitad y soltar; tocar cerca del final | El tiempo muestra el destino al arrastrar; la canción salta | FR-010 |
| 5 | Anterior a los 10 s y a los 2 s | Reinicia / pasa a la anterior | FR-012 |
| 6 | Aleatorio encendido, abrir Cola; apagarlo | Orden distinto que empieza en la actual; vuelve al orden de la lista | FR-014, FR-022 |
| 7 | Repetir tres veces | Apagado → toda la cola → una canción → apagado, íconos y descripciones | FR-015, FR-016 |
| 8 | Deslizar la tarjeta a la izquierda y a la derecha; deslizar poco | Siguiente / anterior; poco no cambia | FR-018, FR-019 |
| 9 | En Cola mover una fila por su asa; quitar otra deslizando; Deshacer | Orden nuevo respetado al pasar a la siguiente; vuelve a su lugar | FR-023, FR-024 |
| 10 | Temporizador de 1 min (valor temporal local, sin versionar); salir de la app y apagar la pantalla | Pausa a los 60–62 s; la notificación sigue | FR-031, FR-032, SC-006 |
| 11 | «Al terminar la canción» | Pausa al terminar la actual | FR-033 |
| 12 | Activar «Quitar animaciones» y repetir 1–3 | Solo fundidos, nada se mueve con el dedo | FR-005, SC-008 |
| 13 | Tema claro: abrir Reproduciendo, Cola y la hoja | Igual a los nodos claros de Figma | FR-047 |
| 14 | Con Reproduciendo abierta, salir al escritorio y volver | La música nunca se cortó; la capa sigue abierta | INV-11 |

Capturas y videos en `ai/handoff/capturas/`: `t03-reproduciendo.png`, `t03-cola.png`, `t03-temporizador.png`, `t03-abrir-reproduciendo.mp4`, `t03-gestos.mp4`.
