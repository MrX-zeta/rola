package com.luis.rola.core.media

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.luis.rola.core.data.Song
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

enum class Repeat { OFF, ALL, ONE }

/** `current == null`: no hay nada cargado y la mini tarjeta no se dibuja. Se publica solo en eventos (sin ticker). */
data class PlayerState(
    val current: Song? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    /** `elapsedRealtime` en que se leyó [positionMs]; la interfaz extrapola mientras suena. */
    val positionAtMs: Long = 0,
    val durationMs: Long = 0,
    val hasNext: Boolean = false,
    val shuffle: Boolean = false,
    val repeat: Repeat = Repeat.OFF,
    val timer: SleepTimerOption = SleepTimerOption.OFF,
    /** Fin del temporizador en `elapsedRealtime`; solo opciones por tiempo. */
    val timerEndsAtMs: Long? = null,
)

/** Una fila de la cola; [index] es su lugar en la lista de Media3. */
data class QueueEntry(val song: Song, val index: Int)

/** Lo que sigue a la canción actual en el orden real, y el orden completo (índices de Media3). */
data class QueueState(val upNext: List<QueueEntry> = emptyList(), val order: List<Int> = emptyList())

/** Lo último que se quitó de la cola, para «Deshacer». [orderBefore] solo con aleatorio. */
class Removed(val item: MediaItem, val index: Int, val orderBefore: List<Int>?)

/** Lo que quitó «Limpiar»: tramos contiguos (índice de antes y canciones) y el orden aleatorio, para «Deshacer». */
class Cleared(val runs: List<Pair<Int, List<MediaItem>>>, val orderBefore: List<Int>?)

/** Puente de la interfaz con [PlaybackService] por un `MediaController`. Vive lo que vive el proceso. */
class PlayerConnection(context: Context) {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state

    // Aparte de `state` para que una cola de miles de canciones no viaje con cada evento.
    private val _queue = MutableStateFlow(QueueState())
    val queue: StateFlow<QueueState> = _queue

    private val _errors = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val errors: SharedFlow<Unit> = _errors

    private var controller: MediaController? = null

    /** Órdenes que llegan antes de conectar (el primer toque tras un arranque en frío): salen en orden al conectar (FR-055). */
    private val pendientes = ArrayDeque<(MediaController) -> Unit>()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publish()
            if (events.containsAny(
                    Player.EVENT_TIMELINE_CHANGED,
                    Player.EVENT_MEDIA_ITEM_TRANSITION,
                    Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
                )
            ) {
                publishQueue()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _errors.tryEmit(Unit)
        }
    }

    init {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token)
            .setListener(object : MediaController.Listener {
                override fun onExtrasChanged(controller: MediaController, extras: Bundle) = publish()
            })
            .buildAsync()
        future.addListener({
            val c = future.get().also { it.addListener(listener) }
            controller = c
            while (pendientes.isNotEmpty()) pendientes.removeFirst()(c)
            publish()
            publishQueue()
        }, ContextCompat.getMainExecutor(context))
    }

    /** Todo corre en el hilo principal: la conexión llega por `getMainExecutor`. */
    private fun whenConnected(order: (MediaController) -> Unit) {
        controller?.let(order) ?: pendientes.addLast(order)
    }

    fun play(queue: List<Song>, startIndex: Int) = whenConnected {
        it.setMediaItems(queue.map { s -> s.toMediaItem() }, startIndex, 0)
        it.prepare()
        it.play()
    }

    fun shuffle(queue: List<Song>) = play(queue.shuffled(), 0)

    fun togglePlayPause() = whenConnected {
        it.run {
            if (isPlaying) {
                pause()
            } else {
                if (playbackState == Player.STATE_IDLE) prepare() // tras un error
                if (playbackState == Player.STATE_ENDED) seekToDefaultPosition()
                play()
            }
        }
    }

    fun next() = whenConnected { it.seekToNextMediaItem() }

    /** Media3 reinicia la canción si pasaron más de 3 s; si no, va a la anterior (FR-012). */
    fun previous() = whenConnected { it.seekToPrevious() }

    fun seekTo(positionMs: Long) = whenConnected { it.seekTo(positionMs) }

    fun toggleShuffle() = whenConnected { it.shuffleModeEnabled = !it.shuffleModeEnabled }

    fun cycleRepeat() = whenConnected {
        it.run {
            repeatMode = when (repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    /** [from] y [to] son posiciones en «A continuación». Con aleatorio solo cambia el orden aleatorio. */
    /** Devuelve si mandó la orden: la Cola sostiene su orden de arrastre hasta que la cola cambie. */
    fun moveUpNext(from: Int, to: Int): Boolean {
        val c = controller ?: return false
        val q = _queue.value
        if (from !in q.upNext.indices || to !in q.upNext.indices || from == to) return false
        if (c.shuffleModeEnabled) {
            sendOrder(c, QueueOrder.move(q.order, q.order.indexOf(c.currentMediaItemIndex), from, to))
        } else {
            c.moveMediaItem(q.upNext[from].index, q.upNext[to].index)
        }
        return true
    }

    fun removeUpNext(position: Int): Removed? {
        val c = controller ?: return null
        val q = _queue.value
        val entry = q.upNext.getOrNull(position) ?: return null
        val removed = Removed(c.getMediaItemAt(entry.index), entry.index, q.order.takeIf { c.shuffleModeEnabled })
        c.removeMediaItem(entry.index)
        return removed
    }

    fun undoRemove(removed: Removed) {
        val c = controller ?: return
        c.addMediaItem(removed.index.coerceAtMost(c.mediaItemCount), removed.item)
        // El orden guardado vale solo si la cola volvió a su tamaño de antes (nadie quitó otra en medio).
        removed.orderBefore?.takeIf { c.shuffleModeEnabled && it.size == c.mediaItemCount }?.let { sendOrder(c, it) }
    }

    /** Tocar una fila de «A continuación»: suena esa canción (FR-052). */
    fun playUpNext(position: Int) = whenConnected { c ->
        val entry = _queue.value.upNext.getOrNull(position) ?: return@whenConnected
        if (c.playbackState == Player.STATE_IDLE) c.prepare() // tras un error
        c.seekToDefaultPosition(entry.index)
        c.play()
    }

    /** «Limpiar»: quita todo lo que sigue y deja la actual (FR-054). */
    fun clearUpNext(): Cleared? {
        val c = controller ?: return null
        val q = _queue.value
        if (q.upNext.isEmpty()) return null
        val runs = QueueOrder.runs(q.upNext.map { it.index })
        val cleared = Cleared(runs.map { r -> r.first to r.map(c::getMediaItemAt) }, q.order.takeIf { c.shuffleModeEnabled })
        runs.asReversed().forEach { c.removeMediaItems(it.first, it.last + 1) } // de atrás hacia adelante: los índices no se corren
        return cleared
    }

    fun undoClear(cleared: Cleared) {
        val c = controller ?: return
        // De menor a mayor en su lugar de antes: cada tramo queda donde estaba.
        cleared.runs.forEach { (i, items) -> c.addMediaItems(i.coerceAtMost(c.mediaItemCount), items) }
        cleared.orderBefore?.takeIf { c.shuffleModeEnabled && it.size == c.mediaItemCount }?.let { sendOrder(c, it) }
    }

    fun setSleepTimer(option: SleepTimerOption) = whenConnected {
        it.sendCustomCommand(
            SessionCommand(PlaybackService.TEMPORIZADOR, Bundle.EMPTY),
            bundleOf(PlaybackService.ARG_OPCION to option.name),
        )
    }

    private fun sendOrder(c: MediaController, order: List<Int>) {
        c.sendCustomCommand(
            SessionCommand(PlaybackService.ORDEN_ALEATORIO, Bundle.EMPTY),
            bundleOf(PlaybackService.ARG_ORDEN to order.toIntArray()),
        )
    }

    private fun publish() {
        val p = controller ?: return
        val extras = p.sessionExtras
        _state.value = PlayerState(
            current = p.currentMediaItem?.toSong(),
            isPlaying = p.isPlaying,
            positionMs = p.currentPosition,
            positionAtMs = SystemClock.elapsedRealtime(),
            durationMs = p.duration.coerceAtLeast(0),
            hasNext = p.hasNextMediaItem(),
            shuffle = p.shuffleModeEnabled,
            repeat = when (p.repeatMode) {
                Player.REPEAT_MODE_ALL -> Repeat.ALL
                Player.REPEAT_MODE_ONE -> Repeat.ONE
                else -> Repeat.OFF
            },
            timer = SleepTimerOption.entries.find { it.name == extras.getString(PlaybackService.EXTRA_TEMPORIZADOR) }
                ?: SleepTimerOption.OFF,
            timerEndsAtMs = extras.getLong(PlaybackService.EXTRA_TERMINA_EN).takeIf { it > 0 },
        )
    }

    private fun publishQueue() {
        val c = controller ?: return
        val t = c.currentTimeline
        if (t.isEmpty) {
            _queue.value = QueueState()
            return
        }
        val order = QueueOrder.follow(t.getFirstWindowIndex(c.shuffleModeEnabled)) {
            t.getNextWindowIndex(it, Player.REPEAT_MODE_OFF, c.shuffleModeEnabled)
        }
        val upNext = order.drop(order.indexOf(c.currentMediaItemIndex) + 1)
        _queue.value = QueueState(upNext.mapNotNull { i -> c.getMediaItemAt(i).toSong()?.let { QueueEntry(it, i) } }, order)
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
