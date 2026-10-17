package com.example.music_bridge

import org.junit.Assert.*
import org.junit.Test

class PlaybackPresentationTest {
    private val queue = listOf(mapOf("id" to 1L, "mediaId" to "track-1"))
    private val saved = mapOf<String, Any?>("queue" to queue, "title" to "Last song",
        "artist" to "Artist", "repeat" to 2, "shuffle" to 0)
    private val loading = mapOf<String, Any?>("queue" to emptyList<Any>(), "title" to null,
        "connected" to true, "cached" to false, "playing" to false, "actions" to 0L,
        "activeId" to 999L, "repeat" to -1, "shuffle" to -1)

    @Test fun reconnectKeepsRowsButDoesNotInventControlsOrActiveSong() {
        val display = PlaybackPresentation.merge(loading, saved, true)
        assertEquals(queue, display["queue"])
        assertEquals("Last song", display["title"])
        assertEquals(2, display["repeat"])
        assertEquals(true, display["connected"])
        assertEquals(0L, display["actions"])
        assertEquals(false, display["playing"])
        assertEquals(-1L, display["activeId"])
        assertEquals(true, display["cached"])
    }

    @Test fun realQueueReplacesCachedRows() {
        val fresh = listOf(mapOf("id" to 50L, "mediaId" to "track-50"))
        val live = loading + mapOf("queue" to fresh, "title" to "New song", "activeId" to 50L)
        val display = PlaybackPresentation.merge(live, saved, true)
        assertEquals(fresh, display["queue"])
        assertEquals("New song", display["title"])
        assertEquals(50L, display["activeId"])
        assertEquals(false, display["cached"])
    }

    @Test fun confirmedEmptyQueueIsNotHiddenForever() {
        val display = PlaybackPresentation.merge(loading, saved, false)
        assertEquals(emptyList<Any>(), display["queue"])
        assertEquals(false, display["connecting"])
        assertEquals(false, display["cached"])
    }

    @Test fun queueCanArriveBeforeMetadataWithoutFlashingGenericTitle() {
        val live = loading + mapOf("queue" to queue, "repeat" to 1, "shuffle" to 1)
        val display = PlaybackPresentation.merge(live, saved, true)
        assertEquals(queue, display["queue"])
        assertEquals("Last song", display["title"])
        assertEquals("Artist", display["artist"])
        assertEquals(1, display["repeat"])
        assertEquals(1, display["shuffle"])
    }

    @Test fun transitionalPresentationDoesNotReplaceSavedPlayback() {
        val memory = PlaybackMemory(saved)
        assertFalse(memory.record(PlaybackPresentation.merge(loading, saved, true), 1000))
        assertEquals(saved, memory.saved)
    }
}
