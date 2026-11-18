package com.luis.rola.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.luis.rola.core.data.FAVORITAS_ID
import com.luis.rola.core.data.LibraryRepository
import com.luis.rola.core.data.PlaylistSummary
import com.luis.rola.core.data.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Biblioteca y las acciones de favoritas y listas que comparten Reproduciendo, las listas y el menú. */
class LibraryViewModel(songs: Flow<List<Song>>, private val library: LibraryRepository) : ViewModel() {

    /** `null` mientras carga. */
    val songs: StateFlow<List<Song>?> = songs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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

    suspend fun forgetSong(mediaId: Long) = library.forgetSong(mediaId)

    companion object {
        fun factory(songs: Flow<List<Song>>, library: LibraryRepository) =
            viewModelFactory { initializer { LibraryViewModel(songs, library) } }
    }
}
