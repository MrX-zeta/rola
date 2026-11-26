package com.luis.rola.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.luis.rola.AppContainer
import com.luis.rola.core.data.MostPlayed
import com.luis.rola.core.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

sealed interface HomeState {
    data object Loading : HomeState

    /** Sin canciones en el teléfono (SCR-12). */
    data object NoSongs : HomeState

    /** [recent] vacío: la primera vez (SCR-11). */
    data class Content(val recent: List<Song>, val mostPlayed: MostPlayed?, val songs: Int, val favorites: Int) : HomeState
}

class HomeViewModel(container: AppContainer) : ViewModel() {

    val state: StateFlow<HomeState> =
        combine(container.songs, container.history.listened, container.library.favoritesSummary) { songs, listened, favorites ->
            if (songs.isEmpty()) HomeState.NoSongs else HomeState.Content(listened.recent, listened.mostPlayed, songs.size, favorites.count)
        }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState.Loading)

    companion object {
        fun factory(container: AppContainer) = viewModelFactory { initializer { HomeViewModel(container) } }
    }
}
