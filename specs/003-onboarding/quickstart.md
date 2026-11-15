# Guía de validación: Onboarding completo

## Requisitos

- Emulador Pixel 7 Pro con música.
- Opcional: Redmi Note 13 Pro+ para `perfil`.

## Compilar y probar

```powershell
.\gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug
.\gradlew :app:connectedDebugAndroidTest
```

Esperado: todo en verde. Las animaciones se juzgan en `perfil` (DEC-28 `fluidez`).

## Recorrido manual (emulador)

Para empezar de cero: `adb shell pm clear com.luis.rola` (borra preferencias y permiso).

| # | Paso | Esperado | Spec |
|---|---|---|---|
| 1 | Abrir la app | Splash y luego bienvenida, página 1, indicador en la primera marca | FR-001, US1/AC1 |
| 2 | «Siguiente»; deslizar a la izquierda; deslizar a la derecha | Página 2, página 3, página 2; el indicador se estira hacia la nueva | FR-003, FR-012 |
| 3 | Llegar a la página 3 | Botón «Empezar» sin saltar; «Omitir» desaparece | FR-005, FR-014 |
| 4 | «Empezar» | Permisos con una sola fila | FR-007, FR-009 |
| 5 | Permitir | Biblioteca | FR-010 |
| 6 | Cerrar la app por completo y abrirla | Splash y luego Biblioteca, sin parpadeo | FR-008, FR-011, SC-002 |
| 7 | `pm clear`, abrir, «Omitir» en la página 1, negar el permiso | Permisos y luego Permiso denegado | FR-006, FR-010 |
| 8 | Abrir de nuevo | Permisos (no la bienvenida) | US3/AC2 |
| 9 | `pm clear`, «Quitar animaciones», recorrer las páginas | Fundido sin desplazamiento | FR-015, SC-004 |
| 10 | Rotar en la página 2 | Sigue en la página 2 | Casos límite |
| 11 | Letra del sistema al máximo | Textos completos, botones visibles | FR-020 |

Capturas en `ai/handoff/capturas/`: `t04-onboarding-1.png`, `t04-onboarding-2.png`, `t04-onboarding-3.png`, `t04-permisos.png`.
