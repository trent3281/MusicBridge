package com.example.music_bridge

import org.junit.Assert.*
import org.junit.Test

class QueueWidgetDataTest {
    @Test fun retainsEntireQueueAndLateActiveSong() {
        val songs = (0 until 3000).map { index ->
            mapOf<String, Any?>("id" to index.toLong(), "mediaId" to "song-$index",
                "title" to "Song $index", "artist" to "Artist")
        }
        QueueWidgetData.publish(mapOf("queue" to songs, "activeId" to 2999L), 1, 2, 3)
        val snapshot = QueueWidgetData.current
        assertEquals(3000, snapshot.songs.size)
        assertEquals("song-2999", snapshot.songs.last()["mediaId"])
        assertEquals(2999L, snapshot.activeId)
    }

    @Test fun publishedRowsAreIsolatedFromLaterSourceMutation() {
        val song = mutableMapOf<String, Any?>("title" to "Original")
        val source = mutableListOf(song)
        QueueWidgetData.publish(mapOf("queue" to source), 1, 2, 3)
        val previous = QueueWidgetData.current
        song["title"] = "Changed"
        source.clear()
        QueueWidgetData.publish(mapOf("queue" to emptyList<Any>()), 4, 5, 6)
        assertEquals("Original", previous.songs.single()["title"])
        assertEquals(0, QueueWidgetData.current.songs.size)
    }
}
