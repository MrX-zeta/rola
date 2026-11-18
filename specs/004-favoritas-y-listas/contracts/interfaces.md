# Contratos internos: Favoritas y Listas

La app no expone interfaces externas. Estos son los contratos entre capas que fijan las pruebas.

## `core/database`

```kotlin
@Database(entities = [FavoriteEntity::class, PlaylistEntity::class, PlaylistSongEntity::class], version = 1, exportSchema = true)
abstract class RolaDatabase : RoomDatabase() {
    abstract fun favorites(): FavoriteDao
    abstract fun playlists(): PlaylistDao
}

@Dao interface FavoriteDao {
    @Query("SELECT media_id FROM favorites ORDER BY added_at DESC") fun ids(): Flow<List<Long>>
    @Insert(onConflict = IGNORE) suspend fun add(f: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE media_id = :id") suspend fun remove(id: Long)
}

@Dao interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY created_at DESC") fun playlists(): Flow<List<PlaylistEntity>>
    @Query("SELECT * FROM playlist_songs ORDER BY playlist_id, position") fun allSongs(): Flow<List<PlaylistSongEntity>>
    @Query("SELECT * FROM playlists WHERE id = :id") fun playlist(id: Long): Flow<PlaylistEntity?>
    @Query("SELECT * FROM playlist_songs WHERE playlist_id = :id ORDER BY position") fun songs(id: Long): Flow<List<PlaylistSongEntity>>
    @Insert suspend fun create(p: PlaylistEntity): Long
    @Transaction suspend fun append(id: Long, mediaIds: List<Long>, now: Long): Int   // devuelve cuántas entraron (sin duplicadas)
    @Insert(onConflict = IGNORE) suspend fun insert(s: PlaylistSongEntity): Long          // Deshacer
    @Query("DELETE FROM playlist_songs WHERE playlist_id = :id AND media_id = :mediaId") suspend fun remove(id: Long, mediaId: Long)
    @Query("DELETE FROM playlists WHERE id = :id") suspend fun delete(id: Long)              // solo pruebas de cascada; sin interfaz
}
```

Una transacción `forgetSong(mediaId)` borra la favorita y todas sus filas de `playlist_songs`.

## `core/data`

```kotlin
class LibraryRepository(db: RolaDatabase, songs: Flow<List<Song>>, clock: () -> Long = System::currentTimeMillis) {
    val favoriteIds: Flow<Set<Long>>                         // para el corazón y el menú
    val favorites: Flow<List<Song>>                          // existentes, más reciente arriba
    val playlists: Flow<List<PlaylistSummary>>               // sin Favoritas; created_at DESC
    val favoritesSummary: Flow<PlaylistSummary>
    fun playlist(id: Long): Flow<PlaylistDetail?>            // null si la lista no existe
    suspend fun setFavorite(id: Long, favorite: Boolean)
    suspend fun createPlaylist(name: String): Long           // name ya validado (recortado, 1–60)
    suspend fun addToPlaylist(id: Long, mediaIds: List<Long>): Int
    suspend fun removeFromPlaylist(id: Long, mediaId: Long): PlaylistSongEntity?   // para Deshacer
    suspend fun restore(row: PlaylistSongEntity)
    suspend fun forgetSong(mediaId: Long)                    // tras un borrado confirmado
}

data class PlaylistSummary(val id: Long, val name: String, val count: Int, val coverIds: List<Long>)
data class PlaylistDetail(val id: Long, val name: String, val songs: List<Song>, val totalMs: Long)

object SearchText { fun matches(song: Song, query: String): Boolean }   // sin mayúsculas ni acentos; título, artista, álbum
object PlaylistName { const val MAX = 60; fun clean(raw: String): String? }  // null si queda vacío
```

`FAVORITAS_ID = -1L` identifica a Favoritas en la navegación y en el menú.

## `core/media/PlayerConnection` (agregados)

```kotlin
fun addToQueue(song: Song)      // al final; si no hay nada cargado, la carga sin reproducir (FR-025)
fun removeSong(id: Long)        // quita todas las entradas con ese mediaId (FR-029)
```

## `feature/player`

```kotlin
/** SCR-51: corazón sobre la portada. Interruptor «Favorita». */
@Composable fun FavoriteButton(favorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier)
```

`NowPlayingScreen` recibe `favorite: Boolean` y `onToggleFavorite: () -> Unit`.

## `feature/library`

```kotlin
enum class MenuPlace { Library, Playlist }
@Composable fun SongMenu(expanded: Boolean, place: MenuPlace, favorite: Boolean, onDismiss: () -> Unit, onAction: (SongAction) -> Unit)
enum class SongAction { AddToQueue, AddToPlaylist, ToggleFavorite, Delete, RemoveFromPlaylist }

@Composable fun AlphabetBar(sections: List<String>, active: String?, onJump: (String) -> Unit, modifier: Modifier = Modifier)
```

## `feature/playlists`

```kotlin
@Composable fun PlaylistsTab(favorites: PlaylistSummary, playlists: List<PlaylistSummary>, onCreate: () -> Unit, onOpen: (Long) -> Unit)
@Composable fun CreatePlaylistDialog(onCreate: (String) -> Unit, onDismiss: () -> Unit)       // SCR-30
@Composable fun PlaylistScreen(detail: PlaylistDetail, isFavorites: Boolean, ...)              // SCR-31 / SCR-32
@Composable fun AddSongsSheet(songs: List<Song>, already: Set<Long>, onAdd: (List<Long>) -> Unit, onDismiss: () -> Unit)  // SCR-33
@Composable fun ChoosePlaylistSheet(playlists: List<PlaylistSummary>, onChoose: (Long) -> Unit, onNew: () -> Unit, onDismiss: () -> Unit)
```

Etiquetas de prueba (`testTag`): `favorita` (corazón), `chip_listas`, `chip_canciones`, `fila_favoritas`, `lista_vacia`, `hoja_agregar`, `menu_cancion`.
