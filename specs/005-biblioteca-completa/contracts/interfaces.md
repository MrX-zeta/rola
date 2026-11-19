# Interfaces: Biblioteca completa

Contratos internos entre capas (la app no expone APIs). Los nombres pueden ajustarse en la implementación si el reporte lo anota.

## core/data

```kotlin
data class Album(val id: Long, val name: String?, val artist: String?, val year: Int, val songs: List<Song>)
data class Artist(val name: String?, val albums: List<Album>, val songs: List<Song>)

object LibraryGrouping {
    fun albums(songs: List<Song>): List<Album>     // ordenados por nombre (DEC-05)
    fun artists(songs: List<Song>): List<Artist>   // ordenados por nombre (DEC-05)
}

enum class SortBy { TITLE, ARTIST, ALBUM }
data class SongSort(val by: SortBy = SortBy.TITLE, val ascending: Boolean = true) {
    val comparator: Comparator<Song>
    fun sectionOf(song: Song): String              // letra del abecedario según el criterio
    fun key(): String                              // "TITLE_ASC"
    companion object { fun parse(value: String?): SongSort }
}

interface PreferencesRepository {
    val sortOrder: Flow<SongSort>
    suspend fun setSortOrder(sort: SongSort)
}
```

## feature/library

```kotlin
class LibraryViewModel {
    val songs: StateFlow<List<Song>?>          // por título: hoja de agregar canciones
    val sortedSongs: StateFlow<List<Song>?>    // Canciones según sort
    val sort: StateFlow<SongSort>
    val albums: StateFlow<List<Album>?>
    val artists: StateFlow<List<Artist>?>
    fun setSort(sort: SongSort)
}

@Composable fun AlbumsTab(albums: List<Album>, onOpen: (Long) -> Unit, ...)
@Composable fun ArtistsTab(artists: List<Artist>, onOpen: (String?) -> Unit, ...)
@Composable fun AlbumScreen(album: Album, currentId: Long?, onBack, onPlay(index), onShuffle, onSongAction, ...)
@Composable fun ArtistScreen(artist: Artist, currentId: Long?, onBack, onOpenAlbum(id), onPlay(index), onShuffle, onSongAction, ...)
@Composable fun SortSheet(sort: SongSort, onChange: (SongSort) -> Unit, onDismiss: () -> Unit)
```

## navigation

- `album/{id}` → `AlbumScreen`; Reproducir/Aleatorio/canción → `player.play(album.songs, i, album.name ?: «Álbum desconocido»)`.
- `artista/{nombre}` (`Uri.encode`, vacío = desconocido) → `ArtistScreen`; Reproducir/Aleatorio/canción → `player.play(artist.songs, i, nombre)`.
- Ambas rutas cuentan como «dentro de Biblioteca» para la píldora y la vuelta a la raíz.
