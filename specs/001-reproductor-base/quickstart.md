# Guía de validación: Reproductor base

## Requisitos

- Emulador Pixel 7 Pro (API 37) o el Redmi de Brian con depuración USB.
- Música en el dispositivo. En el emulador: copiar unos `.mp3` con portada y títulos de prueba (acentos, Ñ, números) a `/sdcard/Music` con `adb push` y avisar al MediaStore:

```bash
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Music/
```

## Comprobaciones automáticas

```bash
.\gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

```bash
.\gradlew :app:connectedDebugAndroidTest
```

Esperado: todo en verde. Unitarias de [contracts/interfaces.md](contracts/interfaces.md) §Funciones puras; Compose: Biblioteca con repositorio falso y mini tarjeta oculta sin canción.

## Escenarios manuales (spec)

| # | Pasos | Resultado esperado | Captura |
|---|---|---|---|
| 1 | Instalar limpio y abrir | Permisos (SCR-05) | `t02-permisos.png` |
| 2 | Permitir acceso → Negar | Permiso denegado (SCR-06) | `t02-permiso-denegado.png` |
| 3 | Abrir Ajustes → conceder → volver | Entra a Inicio («próximamente») sin toques extra | — |
| 4 | Tocar Biblioteca | Lista ordenada por DEC-05 y «N canciones · Título A–Z» | `t02-biblioteca.png` |
| 5 | Tocar una canción | Suena; pide notificaciones (13+); aparece la mini tarjeta con el color de la portada | `t02-mini-reproductor.png` |
| 6 | Salir al escritorio y bajar la cortina | Notificación con portada y controles; pausa/siguiente funcionan | `t02-notificacion.png` |
| 7 | Apagar la pantalla 1 min | Sigue sonando | — |
| 8 | Ir a Buscar e Inicio | «Próximamente»; la música sigue | — |
| 9 | Atrás desde Biblioteca | Vuelve a Inicio; otro atrás sale de la app y la música sigue | — |
| 10 | Revocar el permiso en Ajustes y volver | Permisos de nuevo, sin lista vacía | — |

Capturas en `ai/handoff/capturas/`.
