package com.luis.rola.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.luis.rola.core.data.LibraryIndex
import com.luis.rola.core.data.SearchResults
import com.luis.rola.feature.library.LibraryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

/** Busca en lo que ya cargó Biblioteca. [favoritas]: el nombre de la lista automática (DEC-29). */
@OptIn(FlowPreview::class)
class SearchViewModel(library: LibraryViewModel, favoritas: String) : ViewModel() {

    private val query = MutableStateFlow("")

    private val index = combine(library.songs, library.albums, library.artists, library.favorites, library.playlists) { s, al, ar, f, p ->
        LibraryIndex(s.orEmpty(), al.orEmpty(), ar.orEmpty(), listOf(f.copy(name = favoritas)) + p)
    }

    /** Lo anterior sigue hasta que llega lo nuevo, para no parpadear al escribir (FR-031); `null` sin texto. */
    val results: StateFlow<SearchResults?> = combine(index, query.debounce { if (it.isBlank()) 0L else 150L }) { i, q -> i.search(q) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun search(text: String) {
        query.value = text
    }

    companion object {
        fun factory(library: LibraryViewModel, favoritas: String) = viewModelFactory { initializer { SearchViewModel(library, favoritas) } }
    }
}
