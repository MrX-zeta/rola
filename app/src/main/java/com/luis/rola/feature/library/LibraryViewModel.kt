package com.luis.rola.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.luis.rola.core.data.Song
import com.luis.rola.core.data.SongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LibraryViewModel(repository: SongRepository) : ViewModel() {

    /** `null` mientras carga. */
    val songs: StateFlow<List<Song>?> =
        repository.songs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        fun factory(repository: SongRepository) = viewModelFactory { initializer { LibraryViewModel(repository) } }
    }
}
