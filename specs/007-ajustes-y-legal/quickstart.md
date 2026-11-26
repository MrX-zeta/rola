# Validación rápida: Ajustes y legal

## Requisitos

- Emulador Pixel 7 Pro (API 37) con música en `Music/`. Para «Sin canciones», un emulador sin audio.
- Desde la raíz: `.\gradlew :app:installDebug`.

## Comprobaciones automáticas

```
.\gradlew :app:testDebugUnitTest
.\gradlew :app:connectedDebugAndroidTest
.\gradlew :app:assembleDebug :app:assemblePerfil :app:lintDebug
```

Esperado: todo en verde; las pruebas nuevas de [interfaces.md](contracts/interfaces.md) incluidas.

## Escenarios a mano

1. **Tema** (Historia 1): abrir la app (oscuro). Inicio → engrane → Tema → «Claro»: toda la app y la barra de estado pasan a claro con un fundido corto. Cerrar la app desde recientes y abrirla: arranca en claro sin cuadro oscuro. «Sistema» + cambiar el modo oscuro del emulador: Rola lo sigue.
2. **Navegación** (Historia 2): engrane → Ajustes, la píldora sigue en Inicio, «atrás» vuelve. Sin canciones: «Abrir Ajustes» → Ajustes → «atrás» vuelve a SCR-12. Ajustes no muestra LETRAS, «Carpeta de letras» ni «Agregar widget».
3. **Escaneo** (Historia 3): con datos limpios la fila dice «Aún no has escaneado». Tocar «Escanear medios»: progreso, aviso con el resultado y «Último escaneo: hace un momento».
4. **Temporizador** (Historia 4): fila «Apagado» → 15 minutos: la fila cuenta hacia atrás y Reproduciendo muestra lo mismo.
5. **Legal** (Historia 5): abrir cada documento, desplazar hasta el final con letra grande, volver. Licencias incluye Coil, Palette y Reorderable. «Versión» dice `Rola 0.1.0`.
6. **Copia de seguridad**: `adb shell dumpsys package com.luis.rola | findstr ALLOW_BACKUP` no muestra la bandera.

## Capturas (tarea 08)

`t08-ajustes.png`, `t08-ajustes-claro.png`, `t08-tema.png`, `t08-terminos.png`, `t08-privacidad.png`, `t08-licencias.png`, `t08-inicio-claro.png`, `t08-reproduciendo-claro.png` en `ai/handoff/capturas/`.
