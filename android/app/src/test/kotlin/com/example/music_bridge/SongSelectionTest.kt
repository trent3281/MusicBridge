package com.example.music_bridge

import org.junit.Assert.*
import org.junit.Test

class SongSelectionTest {
    private val a = SongSelection("a", null, "Song", "Artist")
    private val b = SongSelection("b", null, "Other", "Artist")
    @Test fun resolvesAfterOrderAndSessionIdsChange() {
        assertEquals(1, a.resolve(listOf(b, a)))
    }
    @Test fun oldCacheWithoutMediaIdUsesUniqueTitleAndArtist() {
        assertEquals(1, a.copy(mediaId = null).resolve(listOf(b, a)))
    }
    @Test fun missingStableIdNeverPlaysAnotherSongWithSameTitle() {
        assertNull(a.resolve(listOf(a.copy(mediaId = "different"))))
    }
    @Test fun ambiguousTitleNeverSelectsArbitrarily() {
        assertNull(a.copy(mediaId = null).resolve(listOf(a, a.copy(mediaId = "duplicate"))))
    }
    @Test fun missingSongDoesNotPlay() {
        assertNull(a.resolve(listOf(b)))
    }
}
