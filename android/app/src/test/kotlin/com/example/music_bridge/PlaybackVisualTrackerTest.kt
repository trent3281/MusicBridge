package com.example.music_bridge

import org.junit.Assert.*
import org.junit.Test

class PlaybackVisualTrackerTest {
    private val initial = PlaybackVisualState(0, 0, 10L, 3, 2481023L)
    @Test fun externalRepeatChangeRefreshesWithoutQueueOrTrackChange() {
        val tracker = PlaybackVisualTracker()
        tracker.observe(initial)
        assertTrue(tracker.observe(initial.copy(repeat = 1)))
        assertFalse(tracker.observe(initial.copy(repeat = 1)))
    }
    @Test fun externalShuffleChangeRefreshesWithoutQueueOrTrackChange() {
        val tracker = PlaybackVisualTracker()
        tracker.observe(initial)
        assertTrue(tracker.observe(initial.copy(shuffle = 1)))
    }
    @Test fun fallbackPollingDetectsNewActiveSongAndPause() {
        val tracker = PlaybackVisualTracker()
        tracker.observe(initial)
        assertTrue(tracker.observe(initial.copy(activeId = 11L)))
        assertTrue(tracker.observe(initial.copy(activeId = 11L, playbackState = 2)))
    }
    @Test fun unchangedPollDoesNotRefreshWidgetRepeatedly() {
        val tracker = PlaybackVisualTracker()
        assertTrue(tracker.observe(initial))
        assertFalse(tracker.observe(initial))
    }
}
