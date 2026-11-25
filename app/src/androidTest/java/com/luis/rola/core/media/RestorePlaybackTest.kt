package com.luis.rola.core.media

import android.Manifest
import android.app.ActivityManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore.Audio.Media
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.luis.rola.MainActivity
import com.luis.rola.RolaApp
import com.luis.rola.core.data.Song
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** DEC-31 de punta a punta: servicio, Room, DataStore y MediaStore reales, con audios propios. */
class RestorePlaybackTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val instr = InstrumentationRegistry.getInstrumentation()
    private val app = instr.targetContext.applicationContext as RolaApp
    private val container = app.container
    private var ids = emptyList<Long>()

    // Único por prueba: con un archivo del mismo nombre, el MediaStore le agregaría « (1)» al título.
    private val marca = SystemClock.elapsedRealtime()

    private fun titulo(n: Int) = "Prueba $marca $n"

    private fun enMain(bloque: () -> Unit) = instr.runOnMainSync(bloque)

    private fun espera(que: String, condicion: () -> Boolean) {
        val fin = SystemClock.elapsedRealtime() + 10_000
        while (!condicion()) {
            assertTrue("Se esperaba: $que", SystemClock.elapsedRealtime() < fin)
            SystemClock.sleep(50)
        }
    }

    @Before
    fun preparar() = runBlocking {
        val permiso = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        instr.uiAutomation.grantRuntimePermission(app.packageName, permiso)
        container.preferences.completeOnboarding()
        ids = List(4) { insertar(titulo(it + 1)) }
        val canciones = withTimeout(10_000) { container.songs.first { l -> l.map(Song::id).containsAll(ids) } }.associateBy { it.id }
        val p = container.player
        // Cola reordenada: 1 · 3 · 2 · 4, a la mitad de la primera, con repetir y en pausa.
        enMain { p.play(ids.map(canciones::getValue), 0, "Ventanas") }
        espera("que suene") { p.state.value.isPlaying }
        enMain { p.togglePlayPause() }
        espera("la pausa") { !p.state.value.isPlaying }
        enMain { p.moveUpNext(1, 0) }
        enMain { p.cycleRepeat() }
        enMain { p.seekTo(4_000) }
        espera("el salto") { p.state.value.positionMs == 4_000L && siguen(p) == listOf(ids[2], ids[1], ids[3]) }
    }

    @After
    fun limpiar() {
        // Otras pruebas comparten el proceso: sin cola, sin aleatorio y sin repetir.
        val p = container.player
        ids.forEach { enMain { p.removeSong(it) } }
        espera("la cola vacía") { p.state.value.current == null }
        if (p.state.value.shuffle) enMain { p.toggleShuffle() }
        while (p.state.value.repeat != Repeat.OFF) {
            enMain { p.cycleRepeat() }
            instr.waitForIdleSync()
        }
        ids.forEach { id ->
            runCatching { app.contentResolver.delete(ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id), null, null) } // una prueba ya borró la suya
        }
    }

    private fun siguen(p: PlayerConnection) = p.queue.value.upNext.map { it.song.id }

    /** Como cuando el sistema cierra el servicio: se destruye y otra conexión lo crea de nuevo. */
    private fun recrearServicio(): PlayerConnection {
        enMain { container.player.close() }
        // Media3 lo vuelve a iniciar mientras actualiza su notificación: se insiste hasta que de verdad se destruya.
        espera("que se destruya el servicio") {
            app.stopService(Intent(app, PlaybackService::class.java))
            SystemClock.sleep(300)
            @Suppress("DEPRECATION") // para los servicios propios sigue funcionando
            app.getSystemService(ActivityManager::class.java).getRunningServices(Int.MAX_VALUE).none { it.service.className == PlaybackService::class.java.name }
        }
        SystemClock.sleep(500) // termina de escribirse el último guardado
        enMain { container.player = PlayerConnection(app) }
        return container.player
    }

    private fun miniEnPausaCon(titulo: String) {
        compose.waitUntil(10_000) { compose.onAllNodes(hasTestTag("mini")).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasTestTag("mini") and hasAnyDescendant(hasText(titulo)) or (hasTestTag("mini") and hasText(titulo))).assertIsDisplayed()
        compose.onNode(hasContentDescription("Reproducir") and hasAnyAncestor(hasTestTag("mini"))).assertExists()
    }

    @Test
    fun trasRecrearLaActividadYElServicioTodoVuelveEnPausa() {
        ActivityScenario.launch(MainActivity::class.java).use { actividad ->
            miniEnPausaCon(titulo(1))
            actividad.recreate()
            miniEnPausaCon(titulo(1))
        }
        val p = recrearServicio()
        espera("la cola restaurada") { p.state.value.current != null }
        SystemClock.sleep(1_000) // tiempo de sobra para que sonara sola
        with(p.state.value) {
            assertFalse(isPlaying)
            assertEquals(ids[0], current?.id)
            assertEquals(4_000L, positionMs)
            assertEquals(Repeat.ALL, repeat)
            assertFalse(shuffle)
            assertEquals("Ventanas", source)
        }
        assertEquals(listOf(ids[2], ids[1], ids[3]), siguen(p))

        ActivityScenario.launch(MainActivity::class.java).use {
            miniEnPausaCon(titulo(1))
            compose.onNodeWithTag("mini").performClick()
            compose.onNodeWithContentDescription("Cola").performClick()
            val enCola = { t: String -> hasText(t) and hasAnyAncestor(hasTestTag("cola")) }
            compose.waitUntil(10_000) { compose.onAllNodes(enCola(titulo(4)), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
            val alto = { t: String -> compose.onNode(enCola(t), useUnmergedTree = true).getUnclippedBoundsInRoot().top }
            assertTrue(alto(titulo(1)) < alto(titulo(3)))
            assertTrue(alto(titulo(3)) < alto(titulo(2)))
            assertTrue(alto(titulo(2)) < alto(titulo(4)))
        }
    }

    @Test
    fun elOrdenAleatorioSeConservaYAlApagarloVuelveElOriginal() {
        val antes = container.player
        enMain { antes.toggleShuffle() }
        espera("aleatorio") { antes.state.value.shuffle && antes.queue.value.upNext.size == 3 }
        SystemClock.sleep(500) // el orden de verdad llega del servicio un turno después del modo
        val aleatoria = siguen(antes)
        val p = recrearServicio()
        espera("la cola restaurada") { p.state.value.shuffle && p.queue.value.upNext.size == 3 }
        assertEquals(aleatoria, siguen(p))
        enMain { p.toggleShuffle() }
        espera("el orden original") { siguen(p) == listOf(ids[2], ids[1], ids[3]) }
    }

    @Test
    fun siSeBorroLaActualQuedaLaSiguienteDesdeElPrincipio() {
        enMain { container.player.close() }
        app.contentResolver.delete(ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, ids[0]), null, null)
        val p = recrearServicio()
        espera("la cola restaurada") { p.state.value.current != null }
        assertEquals(ids[2], p.state.value.current?.id)
        assertEquals(0L, p.state.value.positionMs)
        assertFalse(p.state.value.isPlaying)
        assertEquals(listOf(ids[1], ids[3]), siguen(p))
    }

    @Test
    fun siLaRestauradaNoSePuedeLeerPasaALaSiguienteSinSonar() {
        app.contentResolver.openOutputStream(ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, ids[0]), "wt")!!.use { it.write(ByteArray(64)) }
        val p = recrearServicio()
        espera("la siguiente") { p.state.value.current?.id == ids[2] }
        SystemClock.sleep(1_000)
        assertFalse(p.state.value.isPlaying)
    }

    /** WAV de 8 s en silencio, en Music/RolaPrueba. */
    private fun insertar(titulo: String): Long {
        val uri = app.contentResolver.insert(
            Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(Media.DISPLAY_NAME, "$titulo.wav")
                put(Media.MIME_TYPE, "audio/x-wav")
                put(Media.RELATIVE_PATH, "Music/RolaPrueba")
                put(Media.IS_PENDING, 1)
            },
        )!!
        app.contentResolver.openOutputStream(uri)!!.use { it.write(wav()) }
        app.contentResolver.update(uri, ContentValues().apply { put(Media.IS_PENDING, 0) }, null, null)
        return ContentUris.parseId(uri)
    }

    private fun wav(): ByteArray {
        val datos = 8_000 * 8
        return ByteBuffer.allocate(44 + datos).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray()).putInt(36 + datos).put("WAVEfmt ".toByteArray())
            putInt(16).putShort(1).putShort(1).putInt(8_000).putInt(8_000).putShort(1).putShort(8)
            put("data".toByteArray()).putInt(datos)
            put(ByteArray(datos) { 128.toByte() })
        }.array()
    }
}
