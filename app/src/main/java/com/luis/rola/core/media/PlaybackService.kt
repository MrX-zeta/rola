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
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
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
import kotlin.random.Random

/**
 * La música vive aquí, no en la interfaz (INV-11): cola, modos y temporizador.
 * Notificación de Media3; su ícono, en res/values/drawables.xml.
 */
// Orden aleatorio propio y pausa al final de la canción: API de ExoPlayer marcada inestable en Media3 1.11.
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private var session: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val timer = SleepTimer(SystemClock::elapsedRealtime)
    private val handler = Handler(Looper.getMainLooper())

    // postDelayed cuenta con uptime, que se detiene si el teléfono duerme en pausa: puede llegar tarde, nunca antes.
    private val vence = Runnable {
        if (timer.expire()) player.pause() // pausa, no detiene (FR-032)
        syncTimer()
    }

    override fun onCreate() {
        super.onCreate()
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
                // FR-018: una canción ilegible no detiene la cola.
                if (player.hasNextMediaItem()) {
                    player.seekToNextMediaItem()
                    player.prepare()
                    player.play()
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                if (shuffleModeEnabled) shuffleFromCurrent()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Lista nueva con el aleatorio encendido: que también empiece en la canción elegida.
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED && player.shuffleModeEnabled) {
                    shuffleFromCurrent()
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM && timer.songEnded()) syncTimer()
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
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = session?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacks(vence)
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
    }
}
