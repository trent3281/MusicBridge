package com.example.music_bridge

/**
 * 所有會影響控制圖示或目前歌曲高亮的欄位都要參與比較。
 * Kotlin data class 自動產生值相等判斷，避免只比對隨機模式而漏掉循環更新。
 */
data class PlaybackVisualState(
    val repeat: Int, val shuffle: Int, val activeId: Long,
    val playbackState: Int, val actions: Long
)

class PlaybackVisualTracker {
    private var previous: PlaybackVisualState? = null
    fun observe(current: PlaybackVisualState): Boolean {
        val changed = current != previous
        previous = current
        return changed
    }
}
