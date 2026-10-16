package com.example.music_bridge

import org.junit.Assert.*
import org.junit.Test

class ShuffleObservationTest {
    @Test fun confirmsModeAndReorderedQueue() {
        val monitor = ShuffleObservation()
        monitor.observe(0, listOf(1, 2, 3), 0)
        monitor.request(1, listOf(1, 2, 3), 100)
        monitor.observe(1, listOf(3, 1, 2), 800)
        assertTrue(monitor.status.contains("順序已變更"))
        assertEquals(2, monitor.queueRevision)
    }
    @Test fun doesNotInventOrderWhenPlayerOnlyChangesMode() {
        val monitor = ShuffleObservation()
        monitor.request(1, listOf(1, 2), 0)
        monitor.observe(1, listOf(1, 2), 5500)
        assertTrue(monitor.status.contains("無法得知"))
    }
    @Test fun unchangedModeDoesNotConfirmTheCommand() {
        val monitor = ShuffleObservation()
        monitor.request(1, listOf(1, 2), 0)
        monitor.observe(0, listOf(1, 2), 5500)
        assertTrue(monitor.status.contains("尚未確認"))
    }
    @Test fun detectsExternalPlayerReorderingWithoutCommand() {
        val monitor = ShuffleObservation()
        monitor.observe(0, listOf(1, 2, 3), 0)
        assertFalse(monitor.observe(0, listOf(1, 2, 3), 100))
        assertTrue(monitor.observe(1, listOf(2, 3, 1), 200))
        assertEquals(2, monitor.queueRevision)
    }
    @Test fun laterExternalChangesDoNotRewriteCommandResult() {
        val monitor = ShuffleObservation()
        monitor.request(1, listOf(1, 2), 0)
        monitor.observe(1, listOf(1, 2), 5500)
        val result = monitor.status
        monitor.observe(0, listOf(2, 1), 6000)
        assertEquals(result, monitor.status)
    }
}
