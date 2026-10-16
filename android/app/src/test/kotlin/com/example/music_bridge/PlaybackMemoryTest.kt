package com.example.music_bridge

import org.junit.Assert.*
import org.junit.Test

class PlaybackMemoryTest {
    private fun live() = mapOf<String, Any?>(
        "connected" to true, "playing" to true, "actions" to 4096L,
        "activeId" to 42L, "title" to "Song", "artist" to "Artist",
        "repeat" to 2, "shuffle" to 0,
        "queue" to listOf(mapOf("id" to 42L, "title" to "Song", "artist" to "Artist")))

    @Test fun disconnectKeepsSongsButNeverRestoresPlaybackAuthority() {
        val memory = PlaybackMemory()
        assertTrue(memory.record(live(), 123))
        val offline = memory.offline(true)!!
        assertEquals(live()["queue"], offline["queue"])
        assertEquals(0L, offline["actions"])
        assertEquals(-1L, offline["activeId"])
        assertEquals(false, offline["playing"])
        assertEquals(false, offline["connected"])
        assertEquals(true, offline["cached"])
        assertEquals(true, offline["permission"])
    }
    @Test fun emptyClosingQueueDoesNotEraseSavedSongs() {
        val memory = PlaybackMemory()
        memory.record(live(), 123)
        assertFalse(memory.record(live() + ("queue" to emptyList<Any>()), 200))
        assertEquals(live()["queue"], memory.offline(true)!!["queue"])
    }
    @Test fun clearedClosingMetadataDoesNotEraseLastSong() {
        val memory = PlaybackMemory()
        memory.record(live(), 123)
        memory.record(live() + mapOf("title" to null, "artist" to "", "repeat" to -1), 200)
        val cached = memory.offline(true)!!
        assertEquals("Song", cached["title"])
        assertEquals("Artist", cached["artist"])
        assertEquals(2, cached["repeat"])
    }
    @Test fun processRestartRestoresStoredPresentation() {
        val original = PlaybackMemory()
        original.record(live(), 123)
        val restarted = PlaybackMemory(original.saved)
        assertEquals("Song", restarted.offline(true)!!["title"])
        assertEquals(123L, restarted.offline(true)!!["cachedAt"])
    }
    @Test fun revokedPermissionKeepsCacheWithAccurateStatus() {
        val memory = PlaybackMemory()
        memory.record(live(), 123)
        assertEquals(false, memory.offline(false)!!["permission"])
        assertTrue((memory.offline(false)!!["message"] as String).contains("通知存取權"))
    }
    @Test fun liveReconnectionReplacesOldQueue() {
        val memory = PlaybackMemory()
        memory.record(live(), 123)
        val replacement = live() + mapOf("title" to "New", "queue" to listOf(mapOf("id" to 99L)))
        assertTrue(memory.record(replacement, 200))
        assertEquals(replacement["queue"], memory.offline(true)!!["queue"])
        assertEquals("New", memory.offline(true)!!["title"])
    }
    @Test fun unchangedStateIsNotRepeatedlyWrittenAndNoFakeInitialSongs() {
        val memory = PlaybackMemory()
        assertNull(memory.offline(true))
        memory.record(live(), 123)
        assertFalse(memory.record(live(), 200))
    }
}
