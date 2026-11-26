package com.luis.rola.core.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryScannerTest {

    private class Repo : SongRepository {
        val soltar = CompletableDeferred<Int>()
        var veces = 0
        override fun songs() = emptyFlow<List<Song>>()
        override suspend fun scan(): Int {
            veces++
            return soltar.await()
        }
    }

    private class Prefs : PreferencesRepository {
        override val onboardingCompleted = MutableStateFlow(true)
        override suspend fun completeOnboarding() {}
        override val sortOrder = MutableStateFlow(SongSort())
        override suspend fun setSortOrder(sort: SongSort) {}
        override val theme = MutableStateFlow(ThemeMode.DARK)
        override suspend fun setTheme(mode: ThemeMode) {}
        override val lastScanAt = MutableStateFlow<Long?>(null)
        override suspend fun setLastScanAt(epochMs: Long) { lastScanAt.value = epochMs }
    }

    @Test
    fun `escanea una vez, marca el estado y guarda la hora`() = runBlocking {
        val repo = Repo()
        val prefs = Prefs()
        val escaner = LibraryScanner(repo, prefs) { 42L }
        val primero = async { escaner.scan() }
        yield()
        assertTrue(escaner.scanning.value)
        assertNull("el segundo toque no escanea otra vez", escaner.scan())
        repo.soltar.complete(7)
        assertEquals(7, primero.await())
        assertEquals(1, repo.veces)
        assertFalse(escaner.scanning.value)
        assertEquals(42L, prefs.lastScanAt.value)
    }

    @Test
    fun `si falla vuelve a quedar libre y no guarda hora`() = runBlocking {
        var falla = true
        val repo = object : SongRepository {
            override fun songs() = emptyFlow<List<Song>>()
            override suspend fun scan(): Int = if (falla) throw IllegalStateException() else 3
        }
        val prefs = Prefs()
        val escaner = LibraryScanner(repo, prefs) { 42L }
        assertTrue(runCatching { escaner.scan() }.isFailure)
        assertFalse(escaner.scanning.value)
        assertNull(prefs.lastScanAt.value)
        falla = false
        assertEquals(3, escaner.scan())
    }
}
