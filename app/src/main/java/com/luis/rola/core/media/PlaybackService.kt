package com.luis.rola.core.media

import android.app.PendingIntent
import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import androidx.core.os.bundleOf
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.annotation.OptIn
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.luis.rola.MainActivity
import com.luis.rola.RolaApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * La música vive aquí, no en la interfaz (INV-11): cola, modos y temporizador; se guarda y se restaura en pausa (DEC-31).
 * Notificación de Media3; su ícono, en res/values/drawables.xml.
 */
// Orden aleatorio propio y pausa al final de la canción: API de ExoPlayer marcada inestable en Media3 1.11.
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private var session: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val timer = SleepTimer(SystemClock::elapsedRealtime)
    private val handler = Handler(Looper.getMainLooper())
    private val scope = MainScope()
    private val container get() = (application as RolaApp).container
    private lateinit var saver: PlaybackSaver

    private val contador = PlayCounter(SystemClock::elapsedRealtime)
    private val escucha = Runnable { registrar(contador.due()) }

    /** Hasta terminar de restaurar no se guarda nada y la interfaz retiene sus órdenes (FR-027). */
    private var listo = false

    private val cada5s = object : Runnable {
        override fun run() {
            guardarAvance()
            handler.postDelayed(this, 5_000)
        }
    }

    // postDelayed cuenta con uptime, que se detiene si el teléfono duerme en pausa: puede llegar tarde, nunca antes.
    private val vence = Runnable {
        if (timer.expire()) player.pause() // pausa, no detiene (FR-032)
        syncTimer()
    }

    override fun onCreate() {
        super.onCreate()
        saver = PlaybackSaver(container.playback, container.scope)
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),
                true, // foco de audio: cede ante llamadas y otras apps
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                // FR-018: una canción ilegible no detiene la cola. Sin play(): si sonaba sigue, y si se acaba de restaurar queda en pausa.
                if (player.hasNextMediaItem()) {
                    player.seekToNextMediaItem()
                    player.prepare()
                }
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                // SOURCE_UPDATE (se supo una duración) no cambia la cola; setShuffleOrder sí llega como PLAYLIST_CHANGED.
                if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) guardarCola()
            }

            override fun onEvents(player: Player, events: Player.Events) {
                if (events.containsAny(
                        Player.EVENT_MEDIA_ITEM_TRANSITION,
                        Player.EVENT_POSITION_DISCONTINUITY,
                        Player.EVENT_PLAY_WHEN_READY_CHANGED,
                        Player.EVENT_PLAYBACK_STATE_CHANGED, // terminó la cola
                        Player.EVENT_REPEAT_MODE_CHANGED,
                        Player.EVENT_PLAYLIST_METADATA_CHANGED,
                    )
                ) {
                    guardarAvance()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                handler.removeCallbacks(cada5s)
                if (isPlaying) handler.postDelayed(cada5s, 5_000)
                contador.playing(isPlaying)
                programarEscucha()
            }

            // Llega antes que onMediaItemTransition: el final de la canción que terminó sola.
            override fun onPositionDiscontinuity(old: Player.PositionInfo, new: Player.PositionInfo, reason: Int) {
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) registrar(contador.ended(old.positionMs))
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState != Player.STATE_ENDED) return
                registrar(contador.ended(player.currentPosition))
                empezarEscucha() // si se vuelve a reproducir, es otra
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                if (shuffleModeEnabled) shuffleFromCurrent()
                guardarCola()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                empezarEscucha()
                // Lista nueva con el aleatorio encendido: que también empiece en la canción elegida.
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED && player.shuffleModeEnabled) {
                    shuffleFromCurrent()
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (reason != Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) return
                registrar(contador.ended(player.currentPosition))
                if (timer.songEnded()) syncTimer()
            }
        })
        val abrirApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        session = MediaSession.Builder(this, player)
            .setSessionActivity(abrirApp)
            .setCallback(object : MediaSession.Callback {
                // Los controladores solo mandan el id: la URI se arma aquí y se descarta lo que no sea un id del MediaStore.
                override fun onAddMediaItems(
                    mediaSession: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    mediaItems: MutableList<MediaItem>,
                ) = Futures.immediateFuture(
                    mediaItems.mapNotNull { item ->
                        item.mediaId.toLongOrNull()?.let {
                            item.buildUpon()
                                .setUri(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, it))
                                .build()
                        }
                    },
                )

                // Los comandos propios solo para Rola: ni la notificación ni otras apps pueden mandarlos.
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                ): MediaSession.ConnectionResult {
                    if (controller.packageName != packageName) return super.onConnect(session, controller)
                    val comandos = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                        .add(SessionCommand(ORDEN_ALEATORIO, Bundle.EMPTY))
                        .add(SessionCommand(TEMPORIZADOR, Bundle.EMPTY))
                        .build()
                    return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                        .setAvailableSessionCommands(comandos)
                        .build()
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle,
                ) = Futures.immediateFuture(
                    if (onCommand(customCommand.customAction, args)) {
                        SessionResult(SessionResult.RESULT_SUCCESS)
                    } else {
                        SessionResult(SessionError.ERROR_BAD_VALUE)
                    },
                )
            })
            .build()
        syncTimer()
        scope.launch {
            val recortada = restaurar()
            // En otro turno del hilo: Media3 ya mandó la cola a los controladores antes que este aviso.
            handler.post {
                listo = true
                if (recortada) guardarCola()
                syncTimer()
            }
        }
    }

    /** Todo como estaba y en pausa: nunca suena sola (FR-022). Devuelve si se omitió alguna canción. */
    private suspend fun restaurar(): Boolean {
        val guardado = container.playback.load() ?: return false // sin cola no se toca el MediaStore
        val songs = container.songs.first().associateBy { it.id }
        val p = guardado.keepExisting(songs.keys)
        if (p == null || player.mediaItemCount > 0) return false
        player.setMediaItems(p.ids.map { songs.getValue(it).toMediaItem() }, p.progress.index, p.progress.positionMs)
        player.playlistMetadata = MediaMetadata.Builder().setTitle(p.progress.source).build()
        player.repeatMode = p.progress.repeat.mode
        player.shuffleModeEnabled = p.progress.shuffle // baraja de nuevo; enseguida va el orden guardado
        if (p.progress.shuffle) player.setShuffleOrder(DefaultShuffleOrder(p.order.toIntArray(), Random.nextLong()))
        player.prepare()
        return p.ids.size < guardado.ids.size
    }

    private fun guardarCola() {
        if (!listo) return
        val t = player.currentTimeline
        val order = if (t.isEmpty) {
            emptyList()
        } else {
            QueueOrder.follow(t.getFirstWindowIndex(player.shuffleModeEnabled)) {
                t.getNextWindowIndex(it, Player.REPEAT_MODE_OFF, player.shuffleModeEnabled)
            }
        }
        // Solo entran ids del MediaStore (onAddMediaItems y restaurar).
        saver.saveQueue(List(t.windowCount) { player.getMediaItemAt(it).mediaId.toLong() }, order)
        guardarAvance()
    }

    private fun guardarAvance() {
        if (!listo) return
        saver.saveProgress(
            PlaybackProgress(
                player.currentMediaItemIndex,
                player.currentPosition,
                player.shuffleModeEnabled,
                Repeat.entries.first { it.mode == player.repeatMode },
                player.playlistMetadata.title?.toString(),
            ),
        )
    }

    private fun empezarEscucha() {
        contador.start(player.currentMediaItem?.mediaId?.toLongOrNull())
        programarEscucha()
    }

    private fun programarEscucha() {
        handler.removeCallbacks(escucha)
        contador.remainingMs?.let { handler.postDelayed(escucha, it) }
    }

    private fun registrar(id: Long?) {
        if (id != null) container.scope.launch { container.history.record(id) }
    }

    // La app pasó a segundo plano.
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_UI_HIDDEN) guardarAvance()
    }

    /** Valida y aplica un comando propio; `false` si los argumentos no sirven. */
    private fun onCommand(action: String, args: Bundle): Boolean = when (action) {
        ORDEN_ALEATORIO -> {
            val orden = args.getIntArray(ARG_ORDEN)?.toList()
            if (orden != null && QueueOrder.isPermutation(orden, player.mediaItemCount)) {
                player.setShuffleOrder(DefaultShuffleOrder(orden.toIntArray(), Random.nextLong()))
                true
            } else {
                false
            }
        }
        TEMPORIZADOR -> {
            val opcion = SleepTimerOption.entries.find { it.name == args.getString(ARG_OPCION) }
            if (opcion != null) {
                timer.start(opcion)
                syncTimer()
            }
            opcion != null
        }
        else -> false
    }

    private fun shuffleFromCurrent() {
        if (player.mediaItemCount == 0) return
        val orden = QueueOrder.shuffledStartingWith(player.currentMediaItemIndex, player.mediaItemCount, Random)
        player.setShuffleOrder(DefaultShuffleOrder(orden.toIntArray(), Random.nextLong()))
    }

    /** Programa la pausa, ajusta «al terminar la canción» y avisa a los controladores. */
    private fun syncTimer() {
        handler.removeCallbacks(vence)
        timer.endsAtMs?.let { handler.postDelayed(vence, it - SystemClock.elapsedRealtime()) }
        player.pauseAtEndOfMediaItems = timer.option == SleepTimerOption.END_OF_SONG
        session?.sessionExtras = bundleOf(
            EXTRA_TEMPORIZADOR to timer.option.name,
            EXTRA_TERMINA_EN to (timer.endsAtMs ?: 0L),
            EXTRA_LISTO to listo,
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        guardarAvance()
        val player = session?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        guardarAvance()
        saver.close()
        scope.cancel()
        handler.removeCallbacksAndMessages(null)
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }

    companion object {
        const val ORDEN_ALEATORIO = "rola.ORDEN_ALEATORIO"
        const val TEMPORIZADOR = "rola.TEMPORIZADOR"
        const val ARG_ORDEN = "orden"
        const val ARG_OPCION = "opcion"
        const val EXTRA_TEMPORIZADOR = "temporizador"
        const val EXTRA_TERMINA_EN = "terminaEn"
        const val EXTRA_LISTO = "listo"
    }
}
