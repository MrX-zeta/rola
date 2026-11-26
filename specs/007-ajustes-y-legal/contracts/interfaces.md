# Contratos internos: Ajustes y legal

Firmas orientativas; los nombres finales pueden ajustarse al implementar sin cambiar el comportamiento.

## core/data

```kotlin
enum class ThemeMode { SYSTEM, DARK, LIGHT;
    companion object { fun parse(value: String?): ThemeMode } // desconocido → DARK
}

interface PreferencesRepository {
    // existentes: onboardingCompleted, completeOnboarding, sortOrder, setSortOrder
    val theme: Flow<ThemeMode>
    suspend fun setTheme(mode: ThemeMode)
    val lastScanAt: Flow<Long?>
    suspend fun setLastScanAt(epochMs: Long)
}

class LibraryScanner(songs: SongRepository, preferences: PreferencesRepository, now: () -> Long) {
    val scanning: StateFlow<Boolean>
    /** Canciones tras escanear; null si ya había un escaneo corriendo. Guarda last_scan_at al terminar. */
    suspend fun scan(): Int?
}

sealed interface TimeAgo {
    data object Moment : TimeAgo
    data class Minutes(val n: Int) : TimeAgo
    data class Hours(val n: Int) : TimeAgo
    data class Days(val n: Int) : TimeAgo
}
fun timeAgo(now: Long, then: Long): TimeAgo
```

## core/designsystem

```kotlin
@Composable
fun RolaTheme(themeMode: ThemeMode = ThemeMode.DARK, content: @Composable () -> Unit)
```

Cambiar `themeMode` aplica el tema con el fundido de research §3.

## feature/settings

```kotlin
class SettingsViewModel(preferences: PreferencesRepository, scanner: LibraryScanner) : ViewModel() {
    val theme: StateFlow<ThemeMode>
    val lastScanAt: StateFlow<Long?>
    val scanning: StateFlow<Boolean>
    fun setTheme(mode: ThemeMode)
}

@Composable
fun SettingsScreen(
    theme: ThemeMode,
    lastScanAt: Long?,
    scanning: Boolean,
    timer: SleepTimerOption,
    timerEndsAtMs: Long?,
    onBack: () -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onScan: () -> Unit,
    onTimer: (SleepTimerOption) -> Unit,
    onOpenDoc: (LegalDoc) -> Unit,
)

enum class LegalDoc { TERMS, PRIVACY, LICENSES }

@Composable
fun LegalScreen(doc: LegalDoc, onBack: () -> Unit)
```

## Navegación

| Ruta | Pantalla | Dueña de la pila |
|---|---|---|
| `ajustes` | SettingsScreen | pestaña desde la que se abre (Inicio) |
| `ajustes/{doc}` | LegalScreen | la misma |

Etiquetas de prueba nuevas: `ajustes_engrane` (Inicio), `abrir_ajustes` (SCR-12); las filas se buscan por texto.
