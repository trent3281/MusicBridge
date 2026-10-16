package com.example.music_bridge

/**
 * 比對播放器回報的隨機模式與佇列順序，不在本地自行洗牌。
 * Samsung 可能改了內部順序卻仍回傳原公開佇列，這時只能誠實回報無法取得順序。
 */
class ShuffleObservation {
    private var expected: Int? = null
    private var requestedAt = 0L
    private var before = emptyList<Long>()
    private var previous: List<Long>? = null
    var queueRevision = 0
        private set
    var status = "尚未送出隨機播放指令"
        private set

    fun request(mode: Int, ids: List<Long>, now: Long) {
        expected = mode
        before = ids.toList()
        requestedAt = now
        status = "已送出指令，等待 Samsung Music 回報"
    }

    fun observe(mode: Int, ids: List<Long>, now: Long): Boolean {
        val changed = previous != ids
        if (changed) {
            queueRevision++
            previous = ids.toList()
        }
        val target = expected ?: return changed
        status = when {
            mode == target && ids != before -> "已確認隨機模式，播放器回傳清單順序已變更"
            mode == target && now - requestedAt < 3000L -> "模式已確認，等待播放器更新清單"
            mode == target && ids.isEmpty() -> "模式已確認，但 Samsung Music 未公開佇列"
            mode == target -> "模式已確認，但公開清單順序未變；無法得知內部隨機順序"
            now - requestedAt < 5000L -> "等待 Samsung Music 確認隨機播放指令"
            else -> "Samsung Music 尚未確認指令；請在播放器內切換並比對診斷紀錄"
        }
        // End the observation window; later player changes must not rewrite this result.
        if (now - requestedAt >= 5000L) expected = null
        return changed
    }
}
