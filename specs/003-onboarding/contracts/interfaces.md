# Contratos internos: Onboarding completo

La app no expone interfaces externas. Estos son los contratos entre capas que las pruebas fijan.

## `core/data/PreferencesRepository`

```kotlin
interface PreferencesRepository {
    /** `false` hasta que se toque «Empezar» u «Omitir»; con error de lectura emite `false`. */
    val onboardingCompleted: Flow<Boolean>

    /** Guarda `onboarding_completed = true`. Idempotente. */
    suspend fun completeOnboarding()
}
```

- Implementación real: `DataStorePreferencesRepository(DataStore<Preferences>)`.
- Falso de pruebas (androidTest): `MutableStateFlow` en memoria.

## `feature/onboarding`

```kotlin
/** null = todavía se lee (el splash sigue); false = bienvenida; true = [content]. */
@Composable fun OnboardingGate(completed: Boolean?, onComplete: () -> Unit, content: @Composable () -> Unit)

/** SCR-02..04. [onFinish] lo llaman «Empezar» y «Omitir». */
@Composable fun OnboardingScreen(onFinish: () -> Unit, modifier: Modifier = Modifier)
```

Etiquetas de prueba (`testTag`): `bienvenida` (pantalla), `indicador` (con `stateDescription` «Página n de 3»).

## `MainActivity`

- Mantiene el splash del sistema mientras `completed == null`.
- Orden: `OnboardingGate` → `MusicPermissionGate` → `RolaNavHost`.
