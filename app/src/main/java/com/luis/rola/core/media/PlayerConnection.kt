package com.luis.rola.core.media

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.luis.rola.core.data.Song
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** `current == null`: no hay nada cargado y la mini tarjeta no se dibuja. */
data class PlayerState(
    val current: Song? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val hasNext: Boolean = false,
)

/** Puente de la interfaz con [PlaybackService] por un `MediaController`. Vive lo que vive el proceso. */
class PlayerConnection(context: Context) {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state

    private val _errors = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val errors: SharedFlow<Unit> = _errors

    // shortcut: órdenes que llegan antes de conectar (unos ms al abrir) se pierden; encolarlas si alguien lo nota.
    private var controller: MediaController? = null
    private val scope = MainScope()
    private var ticker: Job? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = publish(player)
        override fun onPlayerError(error: PlaybackException) {
            _errors.tryEmit(Unit)
        }
    }

    init {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            controller = future.get().also {
                it.addListener(listener)
                publish(it)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun play(queue: List<Song>, startIndex: Int) {
        controller?.run {
            setMediaItems(queue.map { it.toMediaItem() }, startIndex, 0)
            prepare()
            play()
        }
    }

    fun shuffle(queue: List<Song>) = play(queue.shuffled(), 0)

    fun togglePlayPause() {
        controller?.run {
            if (isPlaying) {
                pause()
            } else {
                if (playbackState == Player.STATE_IDLE) prepare() // tras un error
                if (playbackState == Player.STATE_ENDED) seekToDefaultPosition()
                play()
            }
        }
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    private fun publish(p: Player) {
        _state.value = PlayerState(
            current = p.currentMediaItem?.toSong(),
            isPlaying = p.isPlaying,
            positionMs = p.currentPosition,
            durationMs = p.duration.coerceAtLeast(0),
            hasNext = p.hasNextMediaItem(),
        )
        if (!p.isPlaying) {
            ticker?.cancel()
        } else if (ticker?.isActive != true) {
            ticker = scope.launch {
                while (isActive) {
                    delay(500)
                    publish(p)
                }
            }
        }
    }
}

private fun Song.toMediaItem() = MediaItem.Builder()
    .setMediaId(id.toString())
    .setUri(uri)
    .setMediaMetadata(
        MediaMetadata.Builder().setTitle(title).setArtist(artist).setAlbumTitle(album).build(),
    )
    .build()

private fun MediaItem.toSong(): Song? {
    val id = mediaId.toLongOrNull() ?: return null
    return Song(
        id = id,
        title = mediaMetadata.title?.toString().orEmpty(),
        artist = mediaMetadata.artist?.toString(),
        album = mediaMetadata.albumTitle?.toString(),
        durationMs = 0, // la duración real sale de PlayerState.durationMs
    )
}
