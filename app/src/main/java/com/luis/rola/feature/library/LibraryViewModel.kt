package com.luis.rola.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.luis.rola.core.data.Album
import com.luis.rola.core.data.Artist
import com.luis.rola.core.data.FAVORITAS_ID
import com.luis.rola.core.data.LibraryGrouping
import com.luis.rola.core.data.PreferencesRepository
import com.luis.rola.core.data.SongSort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import com.luis.rola.core.data.LibraryRepository
import com.luis.rola.core.data.PlaylistSummary
import com.luis.rola.core.data.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Biblioteca y las acciones de favoritas y listas que comparten Reproduciendo, las listas y el menú. */
class LibraryViewModel(
    songs: Flow<List<Song>>,
    private val library: LibraryRepository,
    private val preferences: PreferencesRepository,
) : ViewModel() {

    /** Por título; `null` mientras carga. La hoja de agregar canciones usa este orden. */
    val songs: StateFlow<List<Song>?> = songs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sort: StateFlow<SongSort> = preferences.sortOrder.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SongSort())

    /** Canciones de Biblioteca en el orden elegido (FR-014); por título no se vuelve a ordenar. */
    val sortedSongs: StateFlow<List<Song>?> = combine(this.songs, sort) { lista, orden ->
        if (lista == null || orden == SongSort()) lista else lista.sortedWith(orden.comparator)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val grouped = songs.map { lista -> LibraryGrouping.albums(lista).let { it to LibraryGrouping.artists(lista, it) } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** `null` mientras carga (research §2: se derivan de las canciones, fuera del hilo principal). */
    val albums: StateFlow<List<Album>?> = grouped.map { it?.first }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val artists: StateFlow<List<Artist>?> = grouped.map { it?.second }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setSort(sort: SongSort) {
        viewModelScope.launch { preferences.setSortOrder(sort) }
    }

    val favoriteIds: StateFlow<Set<Long>> =
        library.favoriteIds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val favorites: StateFlow<PlaylistSummary> = library.favoritesSummary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaylistSummary(FAVORITAS_ID, "", 0, emptyList()))

    val playlists: StateFlow<List<PlaylistSummary>> =
        library.playlists.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFavorite(id: Long, favorite: Boolean) {
        viewModelScope.launch { library.setFavorite(id, favorite) }
    }

    suspend fun createPlaylist(name: String): Long = library.createPlaylist(name)

    suspend fun addToPlaylist(id: Long, mediaIds: List<Long>): Int = library.addToPlaylist(id, mediaIds)

    suspend fun remove(listId: Long, mediaId: Long): suspend () -> Unit = library.remove(listId, mediaId)

    suspend fun renamePlaylist(id: Long, name: String) = library.renamePlaylist(id, name)

    suspend fun deletePlaylist(id: Long): suspend () -> Unit = library.deletePlaylist(id)

    suspend fun forgetSong(mediaId: Long) = library.forgetSong(mediaId)

    companion object {
        fun factory(songs: Flow<List<Song>>, library: LibraryRepository, preferences: PreferencesRepository) =
            viewModelFactory { initializer { LibraryViewModel(songs, library, preferences) } }
    }
}
