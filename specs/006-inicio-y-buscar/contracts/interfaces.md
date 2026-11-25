# Interfaces: Inicio y Buscar

Contratos internos entre capas (la app no expone APIs). Los nombres pueden ajustarse en la implementación si el reporte lo anota.

## core/database

```kotlin
interface HistoryDao {
    suspend fun add(play: PlayHistoryEntity)
    fun stats(): Flow<List<PlayStats>>
}
abstract class QueueDao {
    suspend fun rows(): List<QueueEntity>          // por position
    suspend fun replace(rows: List<QueueEntity>)   // transacción
}
```

## core/data

```kotlin
class HistoryRepository(dao: HistoryDao, songs: Flow<List<Song>>, clock) {
    val recent: Flow<List<Song>>        // 10 distintas existentes, la más reciente primero
    val mostPlayed: Flow<MostPlayed?>
    suspend fun record(mediaId: Long)
}
interface SongRepository { fun songs(): Flow<List<Song>>; suspend fun scan(): Int }   // canciones tras escanear

class LibraryIndex(songs, albums, artists, playlists) { fun search(query: String): SearchResults? }
fun matchIn(text: String, query: String): IntRange?   // sobre el texto original
```

## core/media

```kotlin
data class SavedPlayback(ids, order, index, positionMs, shuffle, repeat, source) { fun keepExisting(exists: Set<Long>): SavedPlayback? }
class PlaybackStore(dao: QueueDao, store: DataStore<Preferences>) {
    suspend fun load(): SavedPlayback?
    suspend fun saveQueue(p: SavedPlayback)      // cola + valores
    suspend fun saveProgress(p: SavedPlayback)   // solo valores
}
class PlayCounter(now: () -> Long) { start(id); playing(Boolean); ended(atMs): Boolean; due(): Boolean; remainingMs(): Long? }
```

- `sessionExtras[listo] = true` cuando el servicio terminó de restaurar; `PlayerConnection` manda sus órdenes pendientes entonces.

## feature

```kotlin
fun greeting(hour: Int): Int                 // recurso de texto
@Composable fun HomeScreen(state: HomeState, scanning: Boolean, onPlay(Song), onShuffle, onFavorites, onLibrary, onScan, ...)
@Composable fun SearchScreen(query, results: SearchResults?, onQuery, onPlay(Song), onSongAction, onOpenAlbum(id), onOpenArtist(name), onOpenPlaylist(id), ...)
```

## navigation

- `startDestination = inicio`.
- La pestaña de la píldora es la dueña de la pila: `lista/…`, `album/…` y `artista…` son hijas de la pestaña desde la que se abrieron.
- Tocar la pestaña activa vuelve a su raíz.
- Reproducir desde Inicio o Buscar: `player.play(canciones en su orden, índice de la canción)` (FR-004).
